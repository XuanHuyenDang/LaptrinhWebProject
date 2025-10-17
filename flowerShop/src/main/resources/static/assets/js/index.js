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
// Giữ nguyên tốc độ và animation như cũ
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
