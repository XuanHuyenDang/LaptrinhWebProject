"use strict";

(() => {
  // =========================
  // Config & Utils
  // =========================
  const API_BASE = (window.CART_CFG && window.CART_CFG.API_BASE) || "/api/cart";

  const fmtVND = (n) =>
    new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
      maximumFractionDigits: 0,
    }).format(n ?? 0);

  function buildCsrfHeaders() {
    const headers = {};
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const headerName = document
      .querySelector('meta[name="_csrf_header"]')
      ?.getAttribute("content");
    if (token && headerName) headers[headerName] = token;
    return headers;
  }

  function buildJsonHeaders() {
    return { "Content-Type": "application/json", ...buildCsrfHeaders() };
  }

  async function fetchJson(url, options = {}) {
    const res = await fetch(url, options);
    const ct = res.headers.get("content-type") || "";

    if (!res.ok) {
      let detail = "";
      try {
        detail = ct.includes("application/json")
          ? JSON.stringify(await res.json())
          : await res.text();
      } catch {
        /* ignore */
      }
      throw new Error(`HTTP ${res.status} – ${detail || "Yêu cầu thất bại"}`);
    }

    if (!ct.includes("application/json")) {
      // Thường là bị redirect về trang đăng nhập
      throw new Error("Phiên đăng nhập đã hết hạn hoặc máy chủ trả về phản hồi không hợp lệ.");
    }

    return res.json();
  }

  function getImageUrl(line) {
    return `https://picsum.photos/seed/p${line.productId}/100/100`;
  }

  function showError(msg) {
    const el = document.getElementById("cart-error");
    if (!el) return;
    el.textContent = msg;
    el.classList.remove("d-none");
    setTimeout(() => el.classList.add("d-none"), 3500);
    // log cho dev
    console.error("[Cart]", msg);
  }

  function debounce(fn, delay = 400) {
    let t;
    return (...a) => {
      clearTimeout(t);
      t = setTimeout(() => fn(...a), delay);
    };
  }

  // =========================
  // State & DOM refs
  // =========================
  const elLoading = document.getElementById("cart-loading");
  const elEmpty = document.getElementById("cart-empty");
  const elTableWrap = document.getElementById("cart-table-wrap");
  const elTbody = document.getElementById("cart-body");

  const elSubtotal = document.getElementById("summary-subtotal");
  const elShipping = document.getElementById("summary-shipping");
  const elTotal = document.getElementById("summary-total");
  const elCheckout = document.getElementById("checkout-btn");

  let CART = null;

  // =========================
  // API calls
  // =========================
  async function apiGetCart() {
    return fetchJson(API_BASE, { headers: buildCsrfHeaders() });
  }

  async function apiUpdateQty(productId, qty) {
    return fetchJson(`${API_BASE}/items/${productId}`, {
      method: "PUT",
      headers: buildJsonHeaders(),
      body: JSON.stringify({ quantity: qty }),
    });
  }

  async function apiRemove(productId) {
    // Ưu tiên DELETE, nếu server không hỗ trợ thì fallback sang PUT quantity = 0
    try {
      return await fetchJson(`${API_BASE}/items/${productId}`, {
        method: "DELETE",
        headers: buildCsrfHeaders(),
      });
    } catch (e) {
      console.warn("DELETE failed, fallback to PUT 0. Reason:", e.message);
      return fetchJson(`${API_BASE}/items/${productId}`, {
        method: "PUT",
        headers: buildJsonHeaders(),
        body: JSON.stringify({ quantity: 0 }),
      });
    }
  }

  // =========================
  // Render
  // =========================
  function renderSummary(cart) {
    elSubtotal.textContent = fmtVND(cart.subtotal);
    elShipping.textContent = fmtVND(cart.shipping);
    elTotal.textContent = fmtVND(cart.total);

    if ((cart.lines?.length || 0) > 0) {
      elCheckout.classList.remove("disabled");
      elCheckout.removeAttribute("aria-disabled");
    } else {
      elCheckout.classList.add("disabled");
      elCheckout.setAttribute("aria-disabled", "true");
    }
  }

  function renderEmpty() {
    elTableWrap.classList.add("d-none");
    elEmpty.classList.remove("d-none");
    renderSummary({ subtotal: 0, shipping: 0, total: 0, lines: [] });
  }

  function rowTemplate(line) {
    const row = document.createElement("tr");
    row.dataset.pid = line.productId;
    row.innerHTML = `
      <td>
        <div class="d-flex align-items-center">
          <img src="${getImageUrl(line)}" alt="${line.productName}">
          <div class="ms-3">
            <h6 class="mb-1">${line.productName}</h6>
            <small class="text-muted">Mã: ${line.productId}</small>
          </div>
        </div>
      </td>
      <td class="text-end">${fmtVND(line.price)}</td>
      <td class="text-center">
        <input type="number" min="0" value="${line.quantity}" class="form-control form-control-sm w-75 mx-auto qty-input">
      </td>
      <td class="text-end line-total">${fmtVND(line.lineTotal)}</td>
      <td class="text-danger text-center">
        <button type="button" class="btn btn-link p-0 text-danger delete-item" title="Xóa">🗑️</button>
      </td>`;
    return row;
  }

  function renderTable(cart) {
    elTbody.innerHTML = "";
    (cart.lines || []).forEach((l) => elTbody.appendChild(rowTemplate(l)));
    elTableWrap.classList.remove("d-none");
    elEmpty.classList.add("d-none");
  }

  function hydrate(cart) {
    CART = cart;
    if (!cart || !cart.lines || cart.lines.length === 0) {
      renderEmpty();
    } else {
      renderTable(cart);
      renderSummary(cart);
    }
  }

  // =========================
  // Events (delegation)
  // =========================
  if (elTbody) {
    // Xóa sản phẩm
    elTbody.addEventListener("click", async (e) => {
      const btn = e.target.closest(".delete-item");
      if (!btn) return;

      const row = btn.closest("tr");
      const pid = Number(row?.dataset.pid);
      if (!pid) return;

      try {
        btn.disabled = true;
        btn.textContent = "...";
        CART = await apiRemove(pid);
        hydrate(CART);
      } catch (err) {
        showError(err.message || "Xóa sản phẩm thất bại");
        btn.disabled = false;
        btn.textContent = "🗑️";
      }
    });

    // Cập nhật số lượng (debounced)
    elTbody.addEventListener(
      "input",
      debounce(async (e) => {
        const input = e.target.closest(".qty-input");
        if (!input) return;

        const row = input.closest("tr");
        const pid = Number(row?.dataset.pid);
        const newQty = Number(input.value || 0);
        if (!pid) return;

        try {
          row.querySelector(
            ".line-total"
          ).innerHTML = `<span class="spinner-border spinner-sm text-success" role="status" aria-hidden="true"></span>`;
          CART = await apiUpdateQty(pid, newQty);
          hydrate(CART);
        } catch (err) {
          showError(err.message || "Cập nhật số lượng thất bại");
          // Đồng bộ lại từ server nếu có lỗi
          try {
            hydrate(await apiGetCart());
          } catch {
            /* ignore */
          }
        }
      }, 500)
    );
  }

  // =========================
  // Init
  // =========================
  (async function init() {
    try {
      elLoading?.classList.remove("d-none");
      const cart = await apiGetCart();
      hydrate(cart);
    } catch (err) {
      showError(err.message || "Không tải được giỏ hàng. Vui lòng thử lại.");
      renderEmpty();
    } finally {
      elLoading?.classList.add("d-none");
    }
  })();
})();
