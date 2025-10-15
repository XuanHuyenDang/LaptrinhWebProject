// assets/js/main.js (phiên bản mẫu)
(() => {
  // Dữ liệu demo: products array (mỗi category ~20 sản phẩm trong thực tế)
  const PRODUCTS = [
    {id: '1', name: 'Bó Hoa Hồng Đỏ', category:'Hoa hồng', price:350000, season:['Tình yêu','Mùa thu'], image:'assets/images/rose1.jpg', short:'Bó hồng đỏ tươi.'},
    {id: '2', name: 'Hoa Lan Vàng', category:'Hoa lan', price:450000, season:['Mùa xuân'], image:'assets/images/orchid1.jpg', short:'Lan vàng sang trọng.'},
    {id: '3', name: 'Bó Hoa Baby Xinh', category:'Hoa baby', price:180000, season:['Mùa hè'], image:'assets/images/baby1.jpg', short:'Nhẹ nhàng, tinh tế.'},
    // ... thêm tới 20+ cho mỗi loại khi cần
  ];

  // CART helpers
  function getCart(){return JSON.parse(localStorage.getItem('cart')||'[]');}
  function saveCart(c){localStorage.setItem('cart', JSON.stringify(c));}
  function updateCartCount(){const c=getCart().reduce((s,i)=>s+i.qty,0); const el=document.getElementById('cartCount'); if(el) el.textContent=c;}
  function addToCart(item){ const cart=getCart(); const e=cart.find(i=>i.id===item.id); if(e) e.qty+=item.qty; else cart.push(item); saveCart(cart); updateCartCount(); alert('Đã thêm vào giỏ'); }

  // Render top products (index)
  function renderTopProducts(){
    const container=document.getElementById('topProducts');
    if(!container) return;
    container.innerHTML='';
    PRODUCTS.slice(0,8).forEach(p=>{
      const col=document.createElement('div'); col.className='col-6 col-md-3 mb-4';
      col.innerHTML=`
        <div class="card h-100">
          <img src="${p.image}" class="card-img-top" alt="${p.name}">
          <div class="card-body d-flex flex-column">
            <h6 class="card-title">${p.name}</h6>
            <p class="card-text text-danger fw-bold">${(p.price).toLocaleString('vi-VN')} ₫</p>
            <div class="mt-auto d-grid gap-2">
              <a href="product-detail.html?id=${p.id}" class="btn btn-outline-primary btn-sm">Xem</a>
              <button class="btn btn-primary btn-sm add-to-cart" data-id="${p.id}">Thêm vào giỏ</button>
            </div>
          </div>
        </div>`;
      container.appendChild(col);
    });
  }

  // Product grid render + filters (products.html)
  function renderProductGrid(products){
    const grid=document.getElementById('productGrid');
    if(!grid) return;
    grid.innerHTML='';
    if(products.length===0) grid.innerHTML='<p>Không có sản phẩm phù hợp.</p>';
    products.forEach(p=>{
      const col=document.createElement('div'); col.className='col-6 col-md-4 mb-4';
      col.innerHTML=`
        <div class="card h-100">
          <img src="${p.image}" class="card-img-top" alt="${p.name}">
          <div class="card-body d-flex flex-column">
            <h6 class="card-title">${p.name}</h6>
            <p class="card-text text-danger fw-bold">${p.price.toLocaleString('vi-VN')} ₫</p>
            <div class="mt-auto d-flex gap-2">
              <a href="product-detail.html?id=${p.id}" class="btn btn-outline-primary btn-sm">Xem</a>
              <button class="btn btn-primary btn-sm add-to-cart" data-id="${p.id}">Thêm</button>
            </div>
          </div>
        </div>`;
      grid.appendChild(col);
    });
  }

  // Filters / Search logic
  function applyFilters(){
    let results = PRODUCTS.slice();
    // category
    const activeCat = document.querySelector('.category-btn.active')?.dataset?.category;
    if(activeCat && activeCat!=='All') results = results.filter(p=>p.category===activeCat);
    // price
    const pf = document.getElementById('priceFilter')?.value;
    if(pf && pf!=='all'){
      const [min, max] = pf.split('-').map(Number);
      results = results.filter(p => p.price >= min && (isNaN(max) || p.price <= max));
    }
    // season
    const seasons = Array.from(document.querySelectorAll('.season-check:checked')).map(cb=>cb.dataset.season);
    if(seasons.length) results = results.filter(p => p.season.some(s=>seasons.includes(s)));
    // search
    const q = document.getElementById('searchInput')?.value?.trim().toLowerCase();
    if(q) results = results.filter(p => p.name.toLowerCase().includes(q));
    // sort
    const sort = document.getElementById('sortSelect')?.value;
    if(sort==='price-asc') results.sort((a,b)=>a.price-b.price);
    if(sort==='price-desc') results.sort((a,b)=>b.price-a.price);
    renderProductGrid(results);
  }

  // product-detail: load product by id
  function loadProductDetail(){
    const params = new URLSearchParams(location.search);
    const id = params.get('id');
    if(!id) return;
    const p = PRODUCTS.find(x=>x.id===id);
    if(!p) return;
    // set DOM
    document.getElementById('pName').textContent = p.name;
    document.getElementById('pPrice').textContent = p.price.toLocaleString('vi-VN') + ' ₫';
    document.getElementById('pShort').textContent = p.short || '';
    document.getElementById('pDetail').textContent = (p.detail || 'Chi tiết sản phẩm...');
    // images carousel
    const inner = document.getElementById('productImagesInner');
    if(inner){
      inner.innerHTML = `<div class="carousel-item active"><img src="${p.image}" class="d-block w-100"></div>`;
      // add more if needed
    }
    document.getElementById('addToCartBtn')?.addEventListener('click', ()=>{
      const qty = parseInt(document.getElementById('qtyInput').value || '1');
      addToCart({id:p.id, name:p.name, price:p.price, qty, image:p.image});
    });
  }

  // admin products: simple localStorage store (for demo)
  function getAdminProducts(){ return JSON.parse(localStorage.getItem('adminProducts') || '[]'); }
  function saveAdminProducts(arr){ localStorage.setItem('adminProducts', JSON.stringify(arr)); }
  function renderAdminProducts(){
    const tbody = document.querySelector('#adminProductTable tbody');
    if(!tbody) return;
    const arr = getAdminProducts();
    tbody.innerHTML = '';
    arr.forEach((p, idx)=>{
      const tr = document.createElement('tr');
      tr.innerHTML = `<td>${idx+1}</td><td>${p.name}</td><td>${p.category}</td><td>${p.price.toLocaleString('vi-VN')}</td><td>${p.stock}</td>
        <td>
          <button class="btn btn-sm btn-primary btn-edit" data-id="${p.id}">Sửa</button>
          <button class="btn btn-sm btn-danger btn-del" data-id="${p.id}">Xóa</button>
        </td>`;
      tbody.appendChild(tr);
    });
    // attach handlers
    document.querySelectorAll('.btn-del').forEach(b=>b.addEventListener('click', e=>{
      const id=e.target.dataset.id; if(!confirm('Xóa sản phẩm?')) return;
      const newArr = getAdminProducts().filter(x=>x.id!==id); saveAdminProducts(newArr); renderAdminProducts();
    }));
    document.querySelectorAll('.btn-edit').forEach(b=>b.addEventListener('click', e=>{
      const id=e.target.dataset.id; const p=getAdminProducts().find(x=>x.id===id);
      if(!p) return;
      // fill modal
      document.getElementById('prodId').value=p.id;
      document.getElementById('prodName').value=p.name;
      document.getElementById('prodCategory').value=p.category;
      document.getElementById('prodPrice').value=p.price;
      document.getElementById('prodStock').value=p.stock;
      const modal = new bootstrap.Modal(document.getElementById('productModal'));
      modal.show();
    }));
  }

  // init events on DOMContentLoaded
  document.addEventListener('DOMContentLoaded', ()=>{
    // initial
    updateCartCount();
    renderTopProducts();
    renderProductGrid(PRODUCTS);

    // wire add-to-cart buttons (event delegation)
    document.body.addEventListener('click', e=>{
      if(e.target.matches('.add-to-cart')){
        const id = e.target.dataset.id;
        const p = PRODUCTS.find(x=>x.id===id);
        if(!p) return;
        addToCart({id:p.id, name:p.name, price:p.price, qty:1, image:p.image});
      }
    });

    // search on header
    document.getElementById('searchForm')?.addEventListener('submit', e=>{
      e.preventDefault(); applyFilters(); if(location.pathname.endsWith('index.html')) location.href='products.html';
    });

    // filters (products page)
    document.querySelectorAll('.category-btn').forEach(btn=>{
      btn.addEventListener('click', e=>{
        document.querySelectorAll('.category-btn').forEach(b=>b.classList.remove('active'));
        btn.classList.add('active');
        applyFilters();
      });
    });
    document.getElementById('priceFilter')?.addEventListener('change', applyFilters);
    document.querySelectorAll('.season-check').forEach(cb=>cb.addEventListener('change', applyFilters));
    document.getElementById('sortSelect')?.addEventListener('change', applyFilters);

    // load product detail page
    loadProductDetail();

    // admin page bindings
    if(document.querySelector('#adminProductTable')){
      // seed admin products if empty
      if(getAdminProducts().length===0){
        saveAdminProducts(PRODUCTS.map(p=>({id:p.id, name:p.name, category:p.category, price:p.price, stock:10})));
      }
      renderAdminProducts();

      // handle new product submit
      document.getElementById('productForm')?.addEventListener('submit', e=>{
        e.preventDefault();
        const id = document.getElementById('prodId').value || ('p'+Date.now());
        const prod = {
          id,
          name: document.getElementById('prodName').value,
          category: document.getElementById('prodCategory').value,
          price: Number(document.getElementById('prodPrice').value),
          stock: Number(document.getElementById('prodStock').value)
        };
        let arr = getAdminProducts();
        const idx = arr.findIndex(x=>x.id===id);
        if(idx>=0) arr[idx] = prod; else arr.unshift(prod);
        saveAdminProducts(arr); renderAdminProducts();
        new bootstrap.Modal(document.getElementById('productModal')).hide();
      });

      document.getElementById('btnAdd')?.addEventListener('click', ()=>{
        document.getElementById('prodId').value=''; document.getElementById('productForm').reset();
      });
    }
  });
})();
