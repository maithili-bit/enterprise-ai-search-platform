# Enterprise AI Search Platform

An AI-powered document search and RAG platform built using Java and Spring Boot. 
The platform provides secure document management, semantic search, conversational 
question answering, JWT authentication, PostgreSQL vector storage, Kafka event 
integration, and LLM-powered responses.

## 🚀 Key Features

- 🔐 JWT-based authentication and authorization
- 📄 Upload and manage documents securely
- 🔎 Keyword-based document search
- 🧠 Semantic search using vector embeddings
- 🤖 RAG-based question answering
- 💬 Conversation and chat history management
- 🗂️ User-specific document and conversation access
- ⚡ Apache Kafka event publishing and consumption
- 🐘 PostgreSQL database with pgvector
- 🦙 Ollama LLM integration
- 🌐 RESTful APIs using Spring Boot
- 📦 Modular backend structure
- 🎨 Web-based frontend for interacting with the platform

## 🛠️ Technology Stack

### Backend
- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Spring Security
- JWT Authentication
- REST APIs
- Apache Kafka

### Database
- PostgreSQL
- pgvector

### AI / RAG
- Spring AI
- Ollama
- LLM
- Vector Embeddings
- Semantic Search
- Retrieval-Augmented Generation (RAG)

### Document Processing
- Apache PDFBox
- ONNX-based embedding model

### Frontend
- HTML
- CSS
- JavaScript

### Tools
- IntelliJ IDEA
- Maven
- Postman
- Git
- GitHub
- Docker

---
## Backend Responsibilities

• Developed REST APIs using Java and Spring Boot
• Implemented JWT-based authentication using Spring Security
• Built document upload and management APIs
• Implemented PostgreSQL persistence using JPA/Hibernate
• Implemented semantic search using vector embeddings and pgvector
• Built RAG-based question answering
• Integrated Ollama for LLM responses
• Implemented conversation and chat history APIs
• Integrated Apache Kafka for document upload events
• Tested APIs using Postman
```

# 🏗️ Architecture

```text
                    ┌─────────────────────┐
                    │      Frontend       │
                    │   HTML/CSS/JavaScript│
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │   Auth Service      │
                    │   JWT Authentication│
                    └──────────┬──────────┘
                               │
                               ▼
              ┌─────────────────────────────────┐
              │       Document Service          │
              │          Spring Boot            │
              │                                 │
              │  REST APIs                      │
              │  Document Management             │
              │  Semantic Search                 │
              │  RAG                             │
              │  Conversations                   │
              └───────┬──────────────┬──────────┘
                      │              │
             ┌────────▼───────┐   ┌──▼──────────────┐
             │  PostgreSQL    │   │     Kafka       │
             │   + pgvector   │   │ Document Events │
             └────────────────┘   └─────────────────┘
                      │
                      ▼
             ┌─────────────────┐
             │     Ollama      │
             │      LLM        │
             └─────────────────┘
