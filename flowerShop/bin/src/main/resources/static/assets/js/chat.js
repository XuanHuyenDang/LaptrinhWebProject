const chatBox = document.getElementById("chatBox");
    const messageInput = document.getElementById("messageInput");
    const sendBtn = document.getElementById("sendBtn");

    function addMessage(text, sender) {
      const msg = document.createElement("div");
      msg.classList.add("message", sender);
      msg.innerText = text;
      chatBox.appendChild(msg);
      chatBox.scrollTop = chatBox.scrollHeight;
    }

    sendBtn.addEventListener("click", () => {
      const text = messageInput.value.trim();
      if (!text) return;
      addMessage(text, "user");
      messageInput.value = "";

      // Demo phản hồi (sau này có thể thay bằng WebSocket / API)
      setTimeout(() => {
        addMessage("Cảm ơn bạn! Chúng tôi sẽ phản hồi sớm.", "bot");
      }, 800);
    });

    messageInput.addEventListener("keypress", (e) => {
      if (e.key === "Enter") sendBtn.click();
    });