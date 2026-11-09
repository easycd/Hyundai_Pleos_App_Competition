// Capture animated wireframes (live pages) as frame sequences; ImageMagick turns them into GIFs.
// usage: node render_anim.js <live dir> <frames dir> <ids comma-separated> [seconds=3] [fps=10]
const { chromium } = require('playwright'); const fs = require('fs'); const path = require('path');
(async () => {
  const [,, liveDir, framesDir, idList, secArg, fpsArg] = process.argv;
  const ids = idList.split(','); const sec = parseFloat(secArg || '3'); const fps = parseInt(fpsArg || '10');
  const browser = await chromium.launch();
  const ctx = await browser.newContext({ viewport: { width: 1280, height: 1080 }, deviceScaleFactor: 0.5 });
  const page = await ctx.newPage();
  for (const id of ids) {
    const dir = path.join(framesDir, id); fs.mkdirSync(dir, { recursive: true });
    for (const f of fs.readdirSync(dir)) fs.unlinkSync(path.join(dir, f));
    await page.goto('file://' + path.resolve(liveDir, id + '.html'));
    const n = Math.round(sec * fps); const t0 = Date.now();
    for (let i = 0; i < n; i++) {
      const target = t0 + i * 1000 / fps; const wait = target - Date.now(); if (wait > 0) await page.waitForTimeout(wait);
      await page.screenshot({ path: path.join(dir, String(i).padStart(3, '0') + '.png'), clip: { x: 0, y: 0, width: 1280, height: 1080 } });
    }
  }
  await browser.close(); console.log('frames done');
})();
