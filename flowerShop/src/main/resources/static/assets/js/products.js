const products = [
      // --- BÓ HOA ---
      {id: 1, name: "Bó Hoa Hồng Đỏ", price: 350000, category: "bohoa", img: "rose1.jpg", tag: "banchay"},
      {id: 2, name: "Bó Hoa Hồng Tím", price: 370000, category: "bohoa", img: "rose2.jpg", tag: "moi"},
      {id: 3, name: "Bó Hoa Cẩm Chướng", price: 320000, category: "bohoa", img: "camchuong.jpg"},
      {id: 4, name: "Bó Hoa Hướng Dương", price: 290000, category: "bohoa", img: "huongduong.jpg", tag: "khuyenmai"},
      {id: 5, name: "Bó Hoa Baby Trắng", price: 310000, category: "bohoa", img: "baby.jpg"},

      // --- GIỎ HOA ---
      {id: 6, name: "Giỏ Hoa Hồng Sen", price: 450000, category: "giohoa", img: "gio1.jpg", tag: "banchay"},
      {id: 7, name: "Giỏ Hoa Lan Hồ Điệp", price: 480000, category: "giohoa", img: "gio2.jpg"},
      {id: 8, name: "Giỏ Hoa Tulip Đỏ", price: 500000, category: "giohoa", img: "gio3.jpg"},
      {id: 9, name: "Giỏ Hoa Mix Màu", price: 460000, category: "giohoa", img: "gio4.jpg", tag: "moi"},
      {id: 10, name: "Giỏ Hoa Hồng Pastel", price: 440000, category: "giohoa", img: "gio5.jpg", tag: "khuyenmai"},

      // --- CHẬU HOA ---
      {id: 11, name: "Chậu Hoa Lan Tím", price: 600000, category: "chauhoa", img: "chau1.jpg"},
      {id: 12, name: "Chậu Hoa Cúc Vàng", price: 350000, category: "chauhoa", img: "chau2.jpg", tag: "moi"},
      {id: 13, name: "Chậu Hoa Hồng Leo", price: 520000, category: "chauhoa", img: "chau3.jpg", tag: "banchay"},
      {id: 14, name: "Chậu Hoa Lavender", price: 480000, category: "chauhoa", img: "chau4.jpg"},
      {id: 15, name: "Chậu Hoa Sen Đá", price: 260000, category: "chauhoa", img: "chau5.jpg", tag: "khuyenmai"}
    ];

    const productList = document.getElementById("productList");

    function renderProducts(list) {
      productList.innerHTML = "";
      if (list.length === 0) {
        productList.innerHTML = `<p class="text-center text-muted">Không tìm thấy sản phẩm phù hợp.</p>`;
        return;
      }

      list.forEach(p => {
        const card = `
          <div class="col-6 col-md-3 mb-4">
            <div class="card h-100 shadow-sm">
              <img src="assets/images/${p.img}" class="card-img-top" alt="${p.name}">
              <div class="card-body d-flex flex-column">
                <h6 class="card-title">${p.name}</h6>
                <p class="card-text text-danger fw-bold">${p.price.toLocaleString()} ₫</p>
                <div class="mt-auto d-grid gap-2">
                  <a href="product-detail.html?id=${p.id}" class="btn btn-outline-primary btn-sm">
                    <i class="bi bi-eye"></i> Xem chi tiết
                  </a>
                  <button class="btn btn-primary btn-sm add-to-cart"
                          data-id="${p.id}" data-name="${p.name}" data-price="${p.price}">
                    <i class="bi bi-cart-plus"></i> Thêm vào giỏ
                  </button>
                </div>
              </div>
            </div>
          </div>`;
        productList.insertAdjacentHTML("beforeend", card);
      });

      document.querySelectorAll(".add-to-cart").forEach(btn => {
        btn.addEventListener("click", addToCart);
      });
    }

    function addToCart(e) {
      const btn = e.currentTarget;
      const id = btn.dataset.id;
      const name = btn.dataset.name;
      const price = parseInt(btn.dataset.price);

      let cart = JSON.parse(localStorage.getItem("cart")) || [];
      const existing = cart.find(item => item.id == id);
      if (existing) existing.qty += 1;
      else cart.push({id, name, price, qty: 1});

      localStorage.setItem("cart", JSON.stringify(cart));
      alert(`🛒 Đã thêm "${name}" vào giỏ hàng!`);
    }

    function filterProducts() {
      let name = document.getElementById("searchName").value.toLowerCase();
      let category = document.getElementById("categoryFilter").value;
      let min = parseInt(document.getElementById("minPrice").value) || 0;
      let max = parseInt(document.getElementById("maxPrice").value) || Infinity;
      let sort = document.getElementById("sortOption").value;

      let filtered = products.filter(p =>
        p.name.toLowerCase().includes(name) &&
        (!category || p.category === category) &&
        p.price >= min && p.price <= max
      );

      switch (sort) {
        case "asc": filtered.sort((a,b) => a.price - b.price); break;
        case "desc": filtered.sort((a,b) => b.price - a.price); break;
        case "banchay": filtered = filtered.filter(p => p.tag === "banchay"); break;
        case "popular": filtered = filtered.filter(p => p.tag === "banchay" || p.tag === "moi"); break;
      }

      renderProducts(filtered);
    }

    document.querySelectorAll("#searchName, #categoryFilter, #minPrice, #maxPrice, #sortOption")
      .forEach(el => el.addEventListener("input", filterProducts));

    renderProducts(products);

    async function loadHTML(id, file) {
      try {
        const res = await fetch(file);
        if (!res.ok) throw new Error("Không thể tải " + file);
        document.getElementById(id).innerHTML = await res.text();
      } catch (err) {
        console.error(err);
      }
    }
    loadHTML("header", "header.html");
    loadHTML("footer", "footer.html");