// AyeshaMart - AI shopping assistant widget (Phase 8)
// POSTs the message to /api/v1/chat and renders the reply. No API keys ever
// reach the browser - the key stays on the server.
(function () {
    "use strict";

    var toggle = document.getElementById("chatbot-toggle");
    var panel = document.getElementById("chatbot");
    if (!toggle || !panel) {
        return;
    }

    var contextPath = toggle.getAttribute("data-context-path") || "";
    var form = document.getElementById("chatbot-form");
    var input = document.getElementById("chatbot-input");
    var messages = document.getElementById("chatbot-messages");
    var close = document.getElementById("chatbot-close");

    function addMessage(text, who) {
        var div = document.createElement("div");
        div.className = "chat-msg " + who;
        div.textContent = text;
        messages.appendChild(div);
        messages.scrollTop = messages.scrollHeight;
    }

    function setTyping(on) {
        var el = document.getElementById("chat-typing");
        if (on) {
            if (!el) {
                el = document.createElement("div");
                el.id = "chat-typing";
                el.className = "chat-msg bot typing";
                el.innerHTML = "<span></span><span></span><span></span>";
                messages.appendChild(el);
                messages.scrollTop = messages.scrollHeight;
            }
        } else if (el) {
            el.parentNode.removeChild(el);
        }
    }

    toggle.addEventListener("click", function () {
        panel.hidden = !panel.hidden;
        if (!panel.hidden) {
            input.focus();
        }
    });

    close.addEventListener("click", function () {
        panel.hidden = true;
    });

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        var text = input.value.trim();
        if (!text) {
            return;
        }
        input.value = "";
        addMessage(text, "user");
        setTyping(true);

        fetch(contextPath + "/api/v1/chat", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ message: text })
        })
            .then(function (res) {
                return res.json();
            })
            .then(function (data) {
                setTyping(false);
                addMessage(data.reply || "Sorry, I could not answer right now. Please try again.", "bot");
            })
            .catch(function () {
                setTyping(false);
                addMessage("I could not reach the assistant. Please try again in a moment.", "bot");
            });
    });
})();