package com.stackera.document.service;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import jakarta.annotation.PreDestroy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmbeddingService {

    private final HuggingFaceTokenizer tokenizer;
    private final OrtEnvironment env;
    private final OrtSession session;

    public EmbeddingService() throws Exception {
        Path tokenizerPath = extractToTemp("onnx/all-MiniLM-L6-v2/tokenizer.json", "tokenizer", ".json");
        Path modelPath = extractToTemp("onnx/all-MiniLM-L6-v2/model.onnx", "model", ".onnx");

        this.tokenizer = HuggingFaceTokenizer.newInstance(tokenizerPath, Map.of("padding", "true"));
        this.env = OrtEnvironment.getEnvironment();
        this.session = env.createSession(modelPath.toString(), new OrtSession.SessionOptions());
    }

    private Path extractToTemp(String classpathLocation, String prefix, String suffix) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        Path tempFile = Files.createTempFile(prefix, suffix);
        tempFile.toFile().deleteOnExit();
        try (InputStream is = resource.getInputStream()) {
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }

    public float[] generateEmbedding(String text) {

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }

        System.out.println(">>> GENERATING EMBEDDING (direct ONNX path)...");

        Map<String, OnnxTensor> inputs = new HashMap<>();

        try {
            Encoding encoding = tokenizer.encode(text);

            long[] inputIds = encoding.getIds();
            long[] attentionMask = encoding.getAttentionMask();
            long[] tokenTypeIds = encoding.getTypeIds();
            long[] shape = {1, inputIds.length};

            inputs.put("input_ids", OnnxTensor.createTensor(env, LongBuffer.wrap(inputIds), shape));
            inputs.put("attention_mask", OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMask), shape));
            inputs.put("token_type_ids", OnnxTensor.createTensor(env, LongBuffer.wrap(tokenTypeIds), shape));

            try (OrtSession.Result result = session.run(inputs)) {

                // Use the model's already-pooled output directly — this sidesteps
                // the manual pooling logic in TransformersEmbeddingModel that's
                // throwing ClassCastException on spring-ai-transformers 2.0.0/2.0.1
                float[][] sentenceEmbedding = (float[][]) result.get("sentence_embedding")
                        .orElseThrow(() -> new IllegalStateException("sentence_embedding output not found"))
                        .getValue();

                float[] embedding = sentenceEmbedding[0];

                System.out.println(">>> EMBEDDING LENGTH = " + embedding.length);

                if (embedding.length != 384) {
                    throw new IllegalStateException(
                            "Expected 384-dimensional embedding but got " + embedding.length);
                }

                return embedding;
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate embedding", e);
        } finally {
            inputs.values().forEach(OnnxTensor::close);
        }
    }

    @PreDestroy
    public void close() {
        try {
            if (session != null) session.close();
        } catch (Exception ignored) {}
    }
}