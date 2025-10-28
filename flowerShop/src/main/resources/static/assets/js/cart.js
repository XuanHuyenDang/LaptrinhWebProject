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
      // Handle 401/403 specifically for redirection
      if (res.status === 401 || res.status === 403) {
           throw new Error("Phiên đăng nhập đã hết hạn hoặc không hợp lệ. Vui lòng đăng nhập lại."); // More specific error
      }
      throw new Error(`HTTP ${res.status} – ${detail || "Yêu cầu thất bại"}`);
    }

    // Check if the response is actually JSON *before* trying to parse
    if (!ct.includes("application/json")) {
         // This might happen if the server redirects to a login page (HTML) due to session expiry
         console.warn("Received non-JSON response, potentially a redirect.");
         throw new Error("Phiên đăng nhập đã hết hạn hoặc máy chủ trả về phản hồi không hợp lệ.");
    }


    try {
        return await res.json();
    } catch (parseError) {
        console.error("Failed to parse JSON response:", parseError);
        throw new Error("Lỗi xử lý dữ liệu từ máy chủ."); // More generic error for parsing issues
    }
  }

  // *** Updated function to get the correct image URL ***
  function getImageUrl(line) {
    // Check if line.imageUrl exists and is not empty
    if (line.imageUrl && line.imageUrl.trim() !== '') {
        // Construct the path relative to the /assets/ directory
        return `/assets/${line.imageUrl}`;
    }
    // Fallback to a placeholder image if imageUrl is missing or empty
    return '/assets/images/placeholder.png'; // Make sure this placeholder exists
  }
  // ******************************************************

  function showError(msg) {
    const el = document.getElementById("cart-error");
    if (!el) return;
    el.textContent = msg;
    el.classList.remove("d-none");
    setTimeout(() => el.classList.add("d-none"), 3500);
    // log for dev
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
    // Prefer DELETE, fallback to PUT quantity = 0 if server doesn't support or fails
    try {
      return await fetchJson(`${API_BASE}/items/${productId}`, {
        method: "DELETE",
        headers: buildCsrfHeaders(),
      });
    } catch (e) {
      console.warn("DELETE failed, fallback to PUT 0. Reason:", e.message);
      // Ensure quantity is 0 for removal via PUT
       if (e.message.includes("401") || e.message.includes("403") || e.message.includes("hết hạn")) {
           throw e; // Re-throw auth errors to be handled by the caller
       }
      return fetchJson(`${API_BASE}/items/${productId}`, {
        method: "PUT",
        headers: buildJsonHeaders(),
        body: JSON.stringify({ quantity: 0 }), // Explicitly set quantity to 0
      });
    }
  }

  // =========================
  // Render
  // =========================
  function renderSummary(cart) {
    elSubtotal.textContent = fmtVND(cart?.subtotal); // Use optional chaining
    elShipping.textContent = fmtVND(cart?.shipping); // Use optional chaining
    elTotal.textContent = fmtVND(cart?.total); // Use optional chaining

    // Enable/disable checkout button based on cart lines
    if (cart?.lines?.length > 0) { // Check if lines array exists and has items
      elCheckout?.classList.remove("disabled"); // Check if element exists
      elCheckout?.removeAttribute("aria-disabled");
    } else {
      elCheckout?.classList.add("disabled"); // Check if element exists
      elCheckout?.setAttribute("aria-disabled", "true");
    }
  }


  function renderEmpty() {
    elTableWrap?.classList.add("d-none"); // Check if element exists
    elEmpty?.classList.remove("d-none"); // Check if element exists
    renderSummary({ subtotal: 0, shipping: 0, total: 0, lines: [] }); // Render zero summary
  }


  function rowTemplate(line) {
    const row = document.createElement("tr");
    row.dataset.pid = line.productId;
    // Use the updated getImageUrl function here
    // Added onerror handler for image loading errors
    row.innerHTML = `
      <td>
        <div class="d-flex align-items-center">
          <img src="${getImageUrl(line)}"
               alt="${line.productName}"
               class="me-3 rounded"
               style="width: 70px; height: 70px; object-fit: cover;"
               onerror="this.onerror=null; this.src='/assets/images/placeholder.png';">
          <div>
            <h6 class="mb-1">${line.productName}</h6>
            <small class="text-muted">Mã: ${line.productId}</small>
          </div>
        </div>
      </td>
      <td class="text-end align-middle">${fmtVND(line.price)}</td>
      <td class="text-center align-middle">
        <input type="number" min="0" value="${line.quantity}" class="form-control form-control-sm mx-auto qty-input" style="width: 80px;">
      </td>
      <td class="text-end align-middle line-total">${fmtVND(line.lineTotal)}</td>
      <td class="text-center align-middle">
        <button type="button" class="btn btn-sm btn-outline-danger delete-item" title="Xóa">
          <i class="bi bi-trash3"></i>
        </button>
      </td>`;
    return row;
  }

  function renderTable(cart) {
    if (!elTbody) return; // Add check for safety
    elTbody.innerHTML = ""; // Clear previous content
    (cart.lines || []).forEach((l) => elTbody.appendChild(rowTemplate(l))); // Create and append rows
    elTableWrap?.classList.remove("d-none"); // Show table
    elEmpty?.classList.add("d-none"); // Hide empty message
  }


  function hydrate(cart) {
    CART = cart; // Update global state
    // Check if cart is valid and has lines
    if (cart && cart.lines && cart.lines.length > 0) {
      renderTable(cart);
      renderSummary(cart);
    } else {
      renderEmpty(); // Render empty state if no lines or invalid cart
    }
  }

  // =========================
  // Events (delegation)
  // =========================
  if (elTbody) {
    // Remove item
    elTbody.addEventListener("click", async (e) => {
      const btn = e.target.closest(".delete-item");
      if (!btn) return;

      const row = btn.closest("tr");
      const pid = Number(row?.dataset.pid);
      if (!pid) return;

      if (!confirm(`Bạn có chắc muốn xóa sản phẩm #${pid} khỏi giỏ hàng?`)) {
        return;
      }

      const originalIcon = btn.innerHTML; // Store original icon
      try {
        btn.disabled = true;
        btn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>`; // Show spinner
        CART = await apiRemove(pid);
        hydrate(CART); // Re-render the cart
      } catch (err) {
         showError(err.message || "Xóa sản phẩm thất bại");
         btn.disabled = false;
         btn.innerHTML = originalIcon; // Restore icon on error
         // Optional: Reload cart fully on error if state might be inconsistent
         // loadCart();
      }
    });

    // Update quantity (debounced)
    elTbody.addEventListener(
      "input",
      debounce(async (e) => {
        const input = e.target.closest(".qty-input");
        if (!input) return;

        const row = input.closest("tr");
        const pid = Number(row?.dataset.pid);
        // Ensure newQty is at least 0. If less than 0, treat as 0 (remove item)
        const newQty = Math.max(0, Number(input.value || 0));
        if (isNaN(pid)) return; // Check if pid is a valid number

        // Find the corresponding line total element
        const lineTotalEl = row.querySelector(".line-total");

        try {
          // Show spinner in the line total cell while updating
          if (lineTotalEl) {
              lineTotalEl.innerHTML = `<span class="spinner-border spinner-border-sm text-success" role="status" aria-hidden="true"></span>`;
          }
          input.disabled = true; // Disable input during update

          CART = await apiUpdateQty(pid, newQty);
          hydrate(CART); // Re-render the cart with updated data
        } catch (err) {
          showError(err.message || "Cập nhật số lượng thất bại");
          // Re-fetch the cart from the server to ensure UI consistency after error
          loadCart(); // Call loadCart to refresh from server
        } finally {
            // Re-enable input regardless of success/failure if it still exists
            const currentInput = elTbody.querySelector(`tr[data-pid="${pid}"] .qty-input`);
            if (currentInput) {
                currentInput.disabled = false;
            }
        }
      }, 500) // Debounce time of 500ms
    );
  }

  // =========================
  // Init Function
  // =========================
  async function loadCart() {
      try {
          elLoading?.classList.remove("d-none"); // Show loading indicator
          elEmpty?.classList.add("d-none"); // Hide empty message
          elTableWrap?.classList.add("d-none"); // Hide table initially
          const cart = await apiGetCart(); // Fetch cart data
          hydrate(cart); // Render the fetched cart data
      } catch (err) {
          // If the error is auth-related, redirect or show specific message
          if (err.message.includes("hết hạn") || err.message.includes("401") || err.message.includes("403")) {
              showError("Phiên đăng nhập không hợp lệ. Đang chuyển hướng...");
              // Optional: Redirect to login page after a delay
              // setTimeout(() => window.location.href = '/auth/login', 2000);
          } else {
               showError(err.message || "Không tải được giỏ hàng. Vui lòng thử lại.");
          }
          renderEmpty(); // Show empty state on error
      } finally {
          elLoading?.classList.add("d-none"); // Hide loading indicator
      }
  }

  // Initial load when the script runs
  loadCart();

})();