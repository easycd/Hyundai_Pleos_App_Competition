// Layout checks on the static wireframe pages: overflow, text clipping, touch sizes, font sizes, callout bounds.
// usage: node check_layout.js <wire dir> <out json>
const { chromium } = require('playwright'); const fs = require('fs'); const path = require('path');
(async () => {
  const [,, wiresDir, outJson] = process.argv;
  const browser = await chromium.launch();
  const ctx = await browser.newContext({ viewport: { width: 1280, height: 1080 }, deviceScaleFactor: 1 });
  const page = await ctx.newPage(); const report = {};
  for (const f of fs.readdirSync(wiresDir).filter(x => x.endsWith('.html')).sort()) {
    await page.goto('file://' + path.resolve(wiresDir, f)); await page.waitForTimeout(100);
    const issues = await page.evaluate(() => {
      const out = []; const W = document.querySelector('.frame').offsetWidth, H = 1080;
      const vis = el => { const s = getComputedStyle(el); if (s.display === 'none' || s.visibility === 'hidden' || parseFloat(s.opacity) === 0) return false; const r = el.getBoundingClientRect(); return r.width > 0 && r.height > 0; };
      const label = el => (el.className && typeof el.className === 'string' ? '.' + el.className.trim().split(/\s+/).join('.') : el.tagName.toLowerCase()) + (el.dataset.co ? `[${el.dataset.co}]` : '') + ' “' + (el.textContent || '').trim().slice(0, 18) + '”';
      const all = [...document.querySelectorAll('.frame *')].filter(vis);
      // inside the frame (allow the pseudo callout 18px outside only when it stays inside the frame)
      for (const el of all) {
        const r = el.getBoundingClientRect();
        if (r.left < -0.5 || r.top < -0.5 || r.right > W + 0.5 || r.bottom > H + 0.5) out.push(`화면 밖으로 나감 ${label(el)} (${Math.round(r.left)},${Math.round(r.top)},${Math.round(r.right)},${Math.round(r.bottom)})`);
        if (el.dataset.co) { const off = el.matches('.top .iconbtn') ? -2 : 18; const offY = el.closest('.top') ? -2 : 18; if (r.left - off < 0 || r.top - offY < 0) out.push(`번호 ${el.dataset.co} 가 화면 밖 (${Math.round(r.left)},${Math.round(r.top)})`); }
      }
      // overflow of children beyond their clipping parent
      for (const el of all) {
        const s = getComputedStyle(el);
        if (['hidden', 'clip'].includes(s.overflow) || ['hidden', 'clip'].includes(s.overflowX)) {
          if (el.classList.contains('frame') || el.dataset.co) continue;
          if (el.scrollWidth > el.clientWidth + 1) out.push(`가로로 잘림 ${label(el)} (${el.scrollWidth}>${el.clientWidth})`);
          if (el.scrollHeight > el.clientHeight + 1) out.push(`세로로 잘림 ${label(el)} (${el.scrollHeight}>${el.clientHeight})`);
        }
      }
      // text wider than its box (nowrap) or buttons whose text wraps to 2 lines
      for (const el of all) {
        const s = getComputedStyle(el);
        if (s.whiteSpace === 'nowrap' && el.scrollWidth > el.clientWidth + 1) out.push(`글자 넘침 ${label(el)}`);
      }
      for (const el of document.querySelectorAll('.btn, .chip, .textbtn')) {
        if (!vis(el)) continue; const r = el.getBoundingClientRect(); const fs = parseFloat(getComputedStyle(el).fontSize);
        if (r.height > fs * 1.3 * 2 + 40 && el.classList.contains('btn') === false) {}
        const lines = Math.round((el.getBoundingClientRect().height - 4) / (fs * 1.3));
        const txt = el.textContent.trim();
        if (txt && el.classList.contains('btn') && el.scrollHeight > el.clientHeight + 1) out.push(`버튼 글자 줄바꿈/넘침 ${label(el)}`);
      }
      // touch targets >= 76 (buttons, rows, icon buttons, toggles, checks)
      for (const el of document.querySelectorAll('.btn, .row, .iconbtn, .textbtn, .toggle, .check, .radio, .seg div, .toast .act, .hit')) {
        if (!vis(el)) continue; const r = el.getBoundingClientRect();
        const min = (el.classList.contains('toggle') || el.classList.contains('check') || el.classList.contains('radio')) ? 48 : 76;
        if (Math.min(r.width, r.height) < min - 0.5) out.push(`터치 크기 ${Math.round(r.width)}×${Math.round(r.height)} < ${min} ${label(el)}`);
      }
      // minimum font size 24
      const walker = document.createTreeWalker(document.querySelector('.frame'), NodeFilter.SHOW_TEXT);
      let n; const seen = new Set();
      while ((n = walker.nextNode())) {
        if (!n.textContent.trim()) continue; const el = n.parentElement; if (!vis(el) || seen.has(el)) continue; seen.add(el);
        const fs = parseFloat(getComputedStyle(el).fontSize);
        if (fs < 24 - 0.5 && !el.closest('[data-co]::after')) out.push(`글자 ${fs}px < 24 ${label(el)}`);
      }
      // overlapping callout badges (pseudo elements): approximate via element top-left corners
      const cos = all.filter(e => e.dataset.co).map(e => { const r = e.getBoundingClientRect(); const off = e.matches('.top .iconbtn') ? -2 : 18; return { co: e.dataset.co, x: r.left - off, y: r.top - off }; });
      for (let i = 0; i < cos.length; i++) for (let j = i + 1; j < cos.length; j++) {
        if (Math.abs(cos[i].x - cos[j].x) < 44 && Math.abs(cos[i].y - cos[j].y) < 44) out.push(`번호 ${cos[i].co} 와 ${cos[j].co} 가 겹침`);
      }
      return out;
    });
    report[f.replace('.html', '')] = issues;
  }
  fs.writeFileSync(outJson, JSON.stringify(report, null, 1));
  const total = Object.values(report).reduce((a, b) => a + b.length, 0);
  console.log('layout issues', total);
  await browser.close();
})();
