const bankOption = document.getElementById("bank");
    const codOption = document.getElementById("cod");
    const bankInfo = document.getElementById("bankInfo");
    const confirmBtn = document.getElementById("confirmBtn");

    // Hiện/ẩn thông tin ngân hàng
    bankOption.addEventListener("change", () => {
      if (bankOption.checked) bankInfo.style.display = "block";
    });
    codOption.addEventListener("change", () => {
      if (codOption.checked) bankInfo.style.display = "none";
    });

    // Xử lý xác nhận đặt hàng
    confirmBtn.addEventListener("click", (e) => {
      e.preventDefault();
      const name = document.getElementById("name").value.trim();
      const phone = document.getElementById("phone").value.trim();
      const address = document.getElementById("address").value.trim();

      if (!name || !phone || !address) {
        alert("Vui lòng điền đầy đủ thông tin giao hàng!");
        return;
      }

      const payment = document.querySelector('input[name="payment"]:checked').value;
      alert(`Cảm ơn ${name}! Đơn hàng của bạn đã được ghi nhận.\nPhương thức thanh toán: ${payment === "COD" ? "Tiền mặt" : "Chuyển khoản"}.`);

      // Chuyển về trang chủ sau 2 giây
      setTimeout(() => {
        window.location.href = "index.html";
      }, 2000);
    });