// =========================
//  ADD TO CART for product detail
// =========================
(() => {
  const API_BASE = (window.CART_CFG && window.CART_CFG.API_BASE) || '/api/cart';

  // ---- CSRF & fetch helpers ----
  function buildCsrfHeaders() {
    const headers = {};
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const headerName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    if (token && headerName) headers[headerName] = token;
    return headers;
  }
  function buildJsonHeaders() {
    return { 'Content-Type': 'application/json', ...buildCsrfHeaders() };
  }
  async function fetchJson(url, options = {}) {
    const res = await fetch(url, options);
    const ct = res.headers.get('content-type') || '';
    if (!res.ok) {
      let detail = '';
      try { detail = ct.includes('application/json') ? JSON.stringify(await res.json()) : await res.text(); } catch {}
      const err = new Error(`HTTP ${res.status} – ${detail || 'Yêu cầu thất bại'}`);
      err.status = res.status; err.contentType = ct;
      throw err;
    }
    if (!ct.includes('application/json')) {
      const err = new Error('Phản hồi không phải JSON (có thể bị chuyển hướng đến trang đăng nhập).');
      err.status = 0; err.contentType = ct;
      throw err;
    }
    return res.json();
  }

  // ---- API ----
  async function apiAddItem(productId, quantity = 1) {
    return fetchJson(`${API_BASE}/items`, {
      method: 'POST',
      headers: buildJsonHeaders(),
      body: JSON.stringify({ productId, quantity })
    });
  }

  // ---- UI helpers ----
  function spin(btn, on = true) {
    if (on) {
      btn.dataset.oldHtml = btn.innerHTML;
      btn.disabled = true;
      btn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>`;
    } else {
      btn.disabled = false;
      if (btn.dataset.oldHtml) btn.innerHTML = btn.dataset.oldHtml;
    }
  }
  function showToast(msg, type = 'success') {
    let box = document.getElementById('global-toast');
    if (!box) {
      box = document.createElement('div');
      box.id = 'global-toast';
      box.style.position = 'fixed';
      box.style.right = '16px';
      box.style.bottom = '16px';
      box.style.zIndex = '1080';
      document.body.appendChild(box);
    }
    const el = document.createElement('div');
    el.className = `alert alert-${type} shadow-sm`;
    el.textContent = msg;
    box.appendChild(el);
    setTimeout(() => el.remove(), 2500);
  }

  // ---- Event: click trên mọi nút .btn-add-to-cart ----
  document.addEventListener('click', async (e) => {
    const btn = e.target.closest('.btn-add-to-cart');
    if (!btn) return;

    const id = Number(btn.dataset.id);
    if (!id) return;

    // Nếu có chỉ định selector số lượng (nút chính), lấy từ đó; ngược lại qty=1 (related)
    let qty = 1;
    const qtySel = btn.getAttribute('data-qty-selector');
    if (qtySel) {
      const input = document.querySelector(qtySel);
      const val = Number(input?.value || 1);
      qty = isNaN(val) || val < 1 ? 1 : Math.floor(val);
    }

    try {
      spin(btn, true);
      await apiAddItem(id, qty);
      showToast('Đã thêm vào giỏ hàng!', 'success');
      // Thông báo cho header/minicart (nếu có lắng nghe)
      document.dispatchEvent(new CustomEvent('cart:updated', { detail: { productId: id, quantity: qty } }));
    } catch (err) {
      const ct = String(err.contentType || '');
      if (err.status === 401 || err.status === 403 || !ct.includes('application/json')) {
        showToast('Vui lòng đăng nhập để thêm vào giỏ.', 'warning');
        setTimeout(() => { window.location.href = '/login'; }, 800);
      } else {
        showToast(err.message || 'Không thêm được vào giỏ', 'danger');
      }
    } finally {
      spin(btn, false);
    }
  });
})();
