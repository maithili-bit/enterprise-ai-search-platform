// ============================================================
// Enterprise AI Search - API Helper
// ============================================================

const AUTH_SERVICE_URL = "http://localhost:8080";
const DOCUMENT_SERVICE_URL = "http://localhost:8081";


// ============================================================
// Get JWT Token (works with any key the login page saved)
// ============================================================

function getToken() {

    return (
        localStorage.getItem("token") ||
        localStorage.getItem("jwtToken") ||
        localStorage.getItem("accessToken")
    );
}


function clearSession() {

    [
        "token",
        "jwtToken",
        "accessToken",
        "userEmail",
        "email",
        "isLoggedIn"
    ].forEach(function (key) {
        localStorage.removeItem(key);
    });
}


// ============================================================
// Common Headers
// ============================================================

function getAuthHeaders() {

    const token = getToken();

    return {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
    };
}


// ============================================================
// Generic API Request
// ============================================================

async function apiRequest(url, options = {}) {

    const token = getToken();

    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };

    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    const response = await fetch(url, {
        ...options,
        headers
    });

    if (response.status === 401) {

        clearSession();

        window.location.href = "login.html";

        throw new Error("Session expired. Please login again.");
    }

    if (!response.ok) {

        let errorMessage = `Request failed with status ${response.status}`;

        try {

            const errorData = await response.json();

            if (errorData.message) {
                errorMessage = errorData.message;
            } else if (errorData.error) {
                errorMessage = errorData.error;
            }

        } catch (error) {
            // Ignore JSON parsing error
        }

        throw new Error(errorMessage);
    }

    // Some endpoints (e.g. DELETE) may return an empty body
    const text = await response.text();

    return text ? JSON.parse(text) : null;
}


// ============================================================
// AUTH APIs
// ============================================================

async function loginUser(email, password) {

    return apiRequest(
        `${AUTH_SERVICE_URL}/api/v1/auth/login`,
        {
            method: "POST",

            body: JSON.stringify({
                email: email,
                password: password
            })
        }
    );
}


// ============================================================
// CONVERSATION APIs
// ============================================================

// Create new conversation
async function createConversation(title = "") {

    return apiRequest(
        `${DOCUMENT_SERVICE_URL}/api/v1/conversations`,
        {
            method: "POST",
            body: JSON.stringify({ title: title })
        }
    );
}


// Get all conversations
async function getConversations() {

    return apiRequest(
        `${DOCUMENT_SERVICE_URL}/api/v1/conversations`,
        { method: "GET" }
    );
}


// Get one conversation
async function getConversation(conversationId) {

    return apiRequest(
        `${DOCUMENT_SERVICE_URL}/api/v1/conversations/${conversationId}`,
        { method: "GET" }
    );
}


// Get messages
async function getConversationMessages(conversationId) {

    return apiRequest(
        `${DOCUMENT_SERVICE_URL}/api/v1/conversations/${conversationId}/messages`,
        { method: "GET" }
    );
}


// Ask question
async function askConversationQuestion(conversationId, question, limit = 5) {

    return apiRequest(
        `${DOCUMENT_SERVICE_URL}/api/v1/conversations/${conversationId}/ask`,
        {
            method: "POST",
            body: JSON.stringify({
                question: question,
                limit: limit
            })
        }
    );
}


// Rename conversation
async function renameConversation(conversationId, title) {

    return apiRequest(
        `${DOCUMENT_SERVICE_URL}/api/v1/conversations/${conversationId}`,
        {
            method: "PATCH",
            body: JSON.stringify({ title: title })
        }
    );
}


// Delete conversation
async function deleteConversation(conversationId) {

    return apiRequest(
        `${DOCUMENT_SERVICE_URL}/api/v1/conversations/${conversationId}`,
        { method: "DELETE" }
    );
}