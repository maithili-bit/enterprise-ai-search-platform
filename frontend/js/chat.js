// ============================================================
// Enterprise AI Search - Chat & Conversation Logic
// ============================================================

let currentConversationId = null;


// ============================================================
// DOM ELEMENTS
// ============================================================

const conversationList =
    document.getElementById("conversationList");

const newConversationButton =
    document.getElementById("newConversationBtn");

const messageInput =
    document.getElementById("messageInput");

const sendButton =
    document.getElementById("sendButton");

const chatMessages =
    document.getElementById("chatMessages");


// ============================================================
// INITIALIZE
// ============================================================

document.addEventListener(
    "DOMContentLoaded",
    async function () {

        const token = getToken();

        if (!token) {

            window.location.href =
                "login.html";

            return;
        }


        const email =
            localStorage.getItem("userEmail") || "";

        const emailElement =
            document.getElementById("userEmail");

        if (emailElement) {
            emailElement.textContent = email;
        }


        await loadConversations();
    }
);


// ============================================================
// LOAD CONVERSATIONS
// ============================================================

async function loadConversations() {

    if (!conversationList) {
        return;
    }


    conversationList.innerHTML = `
        <div class="loading-conversations">
            Loading conversations...
        </div>
    `;


    try {

        const conversations =
            await getConversations();


        conversationList.innerHTML = "";


        if (
            !conversations ||
            conversations.length === 0
        ) {

            conversationList.innerHTML = `
                <div class="empty-conversations">
                    No conversations yet.
                </div>
            `;

            return;
        }


        conversations.forEach(
            function (conversation) {

                addConversationToSidebar(
                    conversation
                );
            }
        );


    } catch (error) {

        console.error(
            "Unable to load conversations:",
            error
        );


        conversationList.innerHTML = `
            <div class="conversation-error">
                Unable to load conversations
            </div>
        `;
    }
}


// ============================================================
// ADD CONVERSATION TO SIDEBAR
// ============================================================

function addConversationToSidebar(
    conversation
) {

    const item =
        document.createElement("div");


    item.className =
        "conversation-item";


    item.dataset.id =
        conversation.id;


    if (
        String(conversation.id) ===
        String(currentConversationId)
    ) {

        item.classList.add("active");
    }


    const title =
        conversation.title ||
        "New Conversation";


    item.innerHTML = `
        <div class="conversation-title-text">
            ${escapeHtml(title)}
        </div>
    `;


    item.addEventListener(
        "click",
        function () {

            openConversation(
                conversation.id
            );

        }
    );


    conversationList.appendChild(
        item
    );
}


// ============================================================
// NEW CONVERSATION BUTTON
// ============================================================

if (newConversationButton) {

    newConversationButton.addEventListener(
        "click",
        async function () {

            await createNewConversation();

        }
    );
}


// ============================================================
// CREATE NEW CONVERSATION
// IMPORTANT:
// This function is deliberately called
// createNewConversation() so it does NOT conflict
// with api.js createConversation().
// ============================================================

async function createNewConversation() {

    try {

        newConversationButton.disabled =
            true;


        // Call API function from api.js
        const conversation =
            await createConversation(
                ""
            );


        currentConversationId =
            conversation.id;


        clearChat();


        const titleElement =
            document.getElementById(
                "currentTitle"
            );


        if (titleElement) {

            titleElement.textContent =
                conversation.title ||
                "New Conversation";
        }


        await loadConversations();


        highlightConversation(
            conversation.id
        );


        focusMessageInput();


    } catch (error) {

        console.error(
            "Failed to create conversation:",
            error
        );


        showErrorMessage(
            "Unable to create a conversation."
        );


    } finally {

        if (newConversationButton) {

            newConversationButton.disabled =
                false;
        }
    }
}


// ============================================================
// OPEN CONVERSATION
// ============================================================

async function openConversation(
    conversationId
) {

    try {

        currentConversationId =
            conversationId;


        highlightConversation(
            conversationId
        );


        clearChat();


        const messages =
            await getConversationMessages(
                conversationId
            );


        const conversation =
            await getConversation(
                conversationId
            );


        const titleElement =
            document.getElementById(
                "currentTitle"
            );


        if (titleElement) {

            titleElement.textContent =
                conversation.title ||
                "New Conversation";
        }


        if (
            !messages ||
            messages.length === 0
        ) {

            showWelcomeMessage();

            return;
        }


        messages.forEach(
            function (message) {

                addMessage(
                    message.role,
                    message.content
                );
            }
        );


        scrollToBottom();


        focusMessageInput();


    } catch (error) {

        console.error(
            "Failed to open conversation:",
            error
        );


        showErrorMessage(
            "Unable to load this conversation."
        );
    }
}


// ============================================================
// SEND BUTTON
// ============================================================

if (sendButton) {

    sendButton.addEventListener(
        "click",
        sendMessage
    );
}


// ============================================================
// ENTER KEY
// ============================================================

if (messageInput) {

    messageInput.addEventListener(
        "keydown",
        function (event) {

            if (
                event.key === "Enter" &&
                !event.shiftKey
            ) {

                event.preventDefault();

                sendMessage();
            }
        }
    );
}


// ============================================================
// SEND MESSAGE
// ============================================================

async function sendMessage() {

    const question =
        messageInput.value.trim();


    if (!question) {
        return;
    }


    // ----------------------------------------------------------
    // Create conversation if none is selected
    // ----------------------------------------------------------

    if (!currentConversationId) {

        try {

            const conversation =
                await createConversation(
                    ""
                );


            currentConversationId =
                conversation.id;


            await loadConversations();


            highlightConversation(
                conversation.id
            );


        } catch (error) {

            console.error(
                "Failed to create conversation:",
                error
            );


            showErrorMessage(
                "Unable to create a conversation."
            );


            return;
        }
    }


    // ----------------------------------------------------------
    // Clear input
    // ----------------------------------------------------------

    messageInput.value = "";


    // ----------------------------------------------------------
    // Show user message
    // ----------------------------------------------------------

    addMessage(
        "USER",
        question
    );


    scrollToBottom();


    // ----------------------------------------------------------
    // Disable send button
    // ----------------------------------------------------------

    sendButton.disabled =
        true;


    sendButton.textContent =
        "Thinking...";


    // ----------------------------------------------------------
    // Thinking indicator
    // ----------------------------------------------------------

    const thinkingElement =
        addThinkingMessage();


    try {

        const response =
            await askConversationQuestion(
                currentConversationId,
                question,
                5
            );


        // Remove thinking message

        if (thinkingElement) {
            thinkingElement.remove();
        }


        // ------------------------------------------------------
        // AI answer
        // ------------------------------------------------------

        const answer =
            response.answer ||
            response.content ||
            "No answer received.";


        addMessage(
            "ASSISTANT",
            answer
        );


        scrollToBottom();


        // ------------------------------------------------------
        // Reload conversations
        // This also refreshes title/order.
        // ------------------------------------------------------

        await loadConversations();


        highlightConversation(
            currentConversationId
        );


    } catch (error) {

        console.error(
            "Question failed:",
            error
        );


        if (thinkingElement) {
            thinkingElement.remove();
        }


        addMessage(
            "ASSISTANT",
            "Sorry, I couldn't process your question."
        );


    } finally {

        sendButton.disabled =
            false;


        sendButton.textContent =
            "Send";


        focusMessageInput();
    }
}


// ============================================================
// ADD MESSAGE
// ============================================================

function addMessage(
    role,
    content
) {

    if (!chatMessages) {
        return;
    }


    const message =
        document.createElement("div");


    const normalizedRole =
        String(role).toUpperCase();


    if (
        normalizedRole === "USER"
    ) {

        message.className =
            "message user-message";

    } else {

        message.className =
            "message assistant-message";
    }


    message.innerHTML = `
        <div class="message-role">
            ${
        normalizedRole === "USER"
            ? "You"
            : "AI Assistant"
    }
        </div>

        <div class="message-content">
            ${formatMessage(content)}
        </div>
    `;


    chatMessages.appendChild(
        message
    );
}


// ============================================================
// THINKING MESSAGE
// ============================================================

function addThinkingMessage() {

    if (!chatMessages) {
        return null;
    }


    const message =
        document.createElement("div");


    message.className =
        "message assistant-message thinking-message";


    message.innerHTML = `
        <div class="message-role">
            AI Assistant
        </div>

        <div class="message-content">
            Thinking...
        </div>
    `;


    chatMessages.appendChild(
        message
    );


    scrollToBottom();


    return message;
}


// ============================================================
// WELCOME MESSAGE
// ============================================================

function showWelcomeMessage() {

    if (!chatMessages) {
        return;
    }


    chatMessages.innerHTML = `

        <div class="welcome-message">

            <h1>
                Start a conversation
            </h1>

            <p>
                Ask a question about your documents.
            </p>

        </div>

    `;
}


// ============================================================
// CLEAR CHAT
// ============================================================

function clearChat() {

    if (!chatMessages) {
        return;
    }


    chatMessages.innerHTML = "";
}


// ============================================================
// ERROR MESSAGE
// ============================================================

function showErrorMessage(
    message
) {

    addMessage(
        "ASSISTANT",
        message
    );
}


// ============================================================
// HIGHLIGHT CONVERSATION
// ============================================================

function highlightConversation(
    conversationId
) {

    const items =
        document.querySelectorAll(
            ".conversation-item"
        );


    items.forEach(
        function (item) {

            if (
                String(item.dataset.id) ===
                String(conversationId)
            ) {

                item.classList.add(
                    "active"
                );

            } else {

                item.classList.remove(
                    "active"
                );
            }
        }
    );
}


// ============================================================
// SCROLL
// ============================================================

function scrollToBottom() {

    if (!chatMessages) {
        return;
    }


    chatMessages.scrollTop =
        chatMessages.scrollHeight;
}


// ============================================================
// FOCUS INPUT
// ============================================================

function focusMessageInput() {

    if (messageInput) {

        setTimeout(
            function () {

                messageInput.focus();

            },
            100
        );
    }
}


// ============================================================
// FORMAT MESSAGE
// ============================================================

function formatMessage(
    content
) {

    if (
        content === null ||
        content === undefined
    ) {

        return "";
    }


    let text =
        String(content);


    text =
        escapeHtml(text);


    text =
        text.replace(
            /\n/g,
            "<br>"
        );


    return text;
}


// ============================================================
// ESCAPE HTML
// ============================================================

function escapeHtml(
    value
) {

    return String(value)

        .replace(
            /&/g,
            "&amp;"
        )

        .replace(
            /</g,
            "&lt;"
        )

        .replace(
            />/g,
            "&gt;"
        )

        .replace(
            /"/g,
            "&quot;"
        )

        .replace(
            /'/g,
            "&#039;"
        );
}