document.getElementById('resetForm').addEventListener('submit', function(e) {
      e.preventDefault();
      const email = document.getElementById('email').value.trim();

      if (email === "") {
        alert("Vui lòng nhập email hợp lệ!");
        return;
      }

      // Giả lập gửi yêu cầu reset
      alert("Liên kết đặt lại mật khẩu đã được gửi đến " + email);
      window.location.href = "login.html"; // Quay lại đăng nhập sau khi gửi
    });