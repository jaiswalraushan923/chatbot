let token = localStorage.getItem("token");
let currentSession = null;
let isLoginMode = false;

if (token) {
    showApp();
}

function switchToLogin() {
    isLoginMode = true;
    document.getElementById("authButton").innerText = "Login";
    document.getElementById("authButton").setAttribute("onclick", "login()");
    document.getElementById("switchText").innerHTML =
        `Don't have an account? <span onclick="switchToRegister()">Register</span>`;
}

function switchToRegister() {
    isLoginMode = false;
    document.getElementById("authButton").innerText = "Create Account";
    document.getElementById("authButton").setAttribute("onclick", "register()");
    document.getElementById("switchText").innerHTML =
        `Already have an account? <span onclick="switchToLogin()">Login</span>`;
}

function register() {
    fetch("/auth/register", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            username: authUsername.value,
            password: authPassword.value
        })
    }).then(() => switchToLogin());
}

function login() {
    fetch("/auth/login", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            username: authUsername.value,
            password: authPassword.value
        })
    })
    .then(res => res.json())
    .then(data => {
        token = data.token;
        localStorage.setItem("token", token);
        showApp();
    });
}

function showApp() {
    document.getElementById("authView").classList.add("hidden");
    document.getElementById("appView").classList.remove("hidden");
    loadSessions();
}

function logout() {
    localStorage.removeItem("token");
    location.reload();
}

function loadSessions() {
    fetch("/api/chat/sessions", {
        headers: {"Authorization": "Bearer " + token}
    })
    .then(res => res.json())
    .then(data => {
        const list = document.getElementById("sessionList");
        list.innerHTML = "";

        data.forEach(s => {
            const div = document.createElement("div");
            div.className = "session-item";
            div.innerHTML = `
                <span onclick="openSession(${s.id})">${s.title}</span>
                <span onclick="deleteSession(${s.id})">🗑</span>
            `;
            list.appendChild(div);
        });
    });
}

function createSession() {
    fetch("/api/chat/session", {
        method: "POST",
        headers: {"Authorization": "Bearer " + token}
    })
    .then(res => res.json())
    .then(data => {
        currentSession = data.id;
        loadSessions();
        document.getElementById("chatMessages").innerHTML = "";
    });
}

function openSession(id) {
    currentSession = id;

    fetch("/api/chat/messages/" + id, {
        headers: {"Authorization": "Bearer " + token}
    })
    .then(res => res.json())
    .then(messages => {
        const chat = document.getElementById("chatMessages");
        chat.innerHTML = "";
        messages.forEach(m => appendMessage(m.role, m.content));
    });
}

function deleteSession(id) {
    fetch("/api/chat/session/" + id, {
        method: "DELETE",
        headers: {"Authorization": "Bearer " + token}
    }).then(loadSessions);
}

function sendMessage() {
    const text = chatInput.value;
    chatInput.value = "";

    appendMessage("user", text);

    fetch("/api/chat/send", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },
        body: JSON.stringify({
            sessionId: currentSession,
            message: text
        })
    })
    .then(res => res.json())
    .then(data => appendMessage("ai", data.reply));
}

function appendMessage(role, text) {
    const div = document.createElement("div");
    div.className = "message " + role;
    div.innerText = text;
    document.getElementById("chatMessages").appendChild(div);
}
