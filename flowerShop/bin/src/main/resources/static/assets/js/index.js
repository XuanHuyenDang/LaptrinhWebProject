// === Generic strip ticker: shift N items every interval ===
function initStripTicker(
  root,
  { interval = 1000, anim = 350, minToAuto = 5, stepCount = 1 } = {}
) {
  if (!root) return;
  const track = root.querySelector('.strip-track');
  if (!track) return;

  let timer = null, animating = false;

  function forward() {
    const items = Array.from(track.children);
    if (animating || items.length < stepCount) return; // < stepCount thì không chạy

    const gap = parseFloat(getComputedStyle(track).gap || 0);

    // tổng quãng dịch = width của stepCount item đầu + (stepCount-1)*gap
    let shift = 0;
    for (let i = 0; i < stepCount; i++) {
      shift += items[i].getBoundingClientRect().width;
      if (i < stepCount - 1) shift += gap;
    }

    animating = true;
    track.style.transition = `transform ${anim}ms ease`;
    track.style.transform  = `translateX(-${shift}px)`;

    setTimeout(() => {
      // đẩy nguyên nhóm stepCount item ra cuối để lặp vòng
      for (let i = 0; i < stepCount; i++) {
        const first = track.firstElementChild;
        if (first) track.appendChild(first);
      }
      track.style.transition = 'none';
      track.style.transform  = 'translateX(0)';
      void track.offsetWidth; // reflow
      animating = false;
    }, anim);
  }

  function start(){ if (!timer) timer = setInterval(forward, interval); }
  function stop(){ if (timer){ clearInterval(timer); timer = null; } }

  // Auto-run khi số item >= minToAuto
  if (track.children.length >= minToAuto) start();

  root.addEventListener('mouseenter', stop);
  root.addEventListener('mouseleave', start);
}

// Khởi tạo: bán chạy (1 sản phẩm/lần), khuyến mãi (5 sản phẩm/lần)
document.addEventListener('DOMContentLoaded', function () {
  initStripTicker(document.getElementById('bestSellerStrip'), {
    interval: 1500,
    anim: 350,
    minToAuto: 5,
    stepCount: 1
  });

  // Khuyến mãi: nếu < 5 item thì không chạy (nhờ điều kiện items.length < stepCount)
  initStripTicker(document.getElementById('saleStrip'), {
    interval: 3000,
    anim: 750,
    minToAuto: 5,
    stepCount: 5
  });
});

// =========================
//  ADD TO CART
// =========================
(() => {
  // Lấy base API từ global (nếu có) hoặc mặc định
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
  function flash(btn, text, cls = 'text-success') {
    const old = btn.innerHTML;
    btn.innerHTML = text;
    btn.classList.add(cls);
    setTimeout(() => { btn.innerHTML = old; btn.classList.remove(cls); }, 1200);
  }
  function showToast(msg, type = 'danger') {
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

  // ---- Gắn hành vi cho toàn trang: mọi nút .btn-add-to-cart ----
  document.addEventListener('click', async (e) => {
    const btn = e.target.closest('.btn-add-to-cart');
    if (!btn) return;

    const id = Number(btn.dataset.id);
    if (!id) return;

    try {
      spin(btn, true);
      await apiAddItem(id, 1);
      flash(btn, 'Đã thêm ✓', 'text-success');
      showToast('Đã thêm vào giỏ hàng!', 'success'); // ✅ thêm thông báo thành công
      // Cho header/minicart biết là giỏ đã đổi (nếu có lắng nghe)
      document.dispatchEvent(new CustomEvent('cart:updated', { detail: { productId: id } }));
    } catch (err) {
      const ct = String(err.contentType || '');
      if (err.status === 401 || err.status === 403 || !ct.includes('application/json')) {
        showToast('Vui lòng đăng nhập để thêm vào giỏ.', 'warning');
        setTimeout(() => { window.location.href = '/login'; }, 800); // đổi URL nếu login khác
      } else {
        showToast(err.message || 'Không thêm được vào giỏ', 'danger');
      }
    } finally {
      spin(btn, false);
    }
  });
})();
