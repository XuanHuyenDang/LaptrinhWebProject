function formatCurrency(value) {
      return value.toLocaleString('vi-VN') + 'đ';
    }

    function updateCartTotals() {
      let subtotal = 0;
      document.querySelectorAll('#cart-body tr').forEach(row => {
        const price = parseInt(row.querySelector('.price').dataset.price);
        const quantity = parseInt(row.querySelector('.quantity').value);
        const total = price * quantity;
        row.querySelector('.subtotal').textContent = formatCurrency(total);
        subtotal += total;
      });

      const shipping = 30000;
      document.getElementById('subtotal').textContent = formatCurrency(subtotal);
      document.getElementById('total').textContent = formatCurrency(subtotal + shipping);
    }

    // Khi thay đổi số lượng
    document.querySelectorAll('.quantity').forEach(input => {
      input.addEventListener('input', updateCartTotals);
    });

    // Xóa sản phẩm
    document.querySelectorAll('.delete-item').forEach(btn => {
      btn.addEventListener('click', e => {
        e.target.closest('tr').remove();
        updateCartTotals();
      });
    });

    // Cập nhật lần đầu khi tải trang
    updateCartTotals();