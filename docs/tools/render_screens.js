const { chromium } = require('playwright');
const fs = require('fs'); const path = require('path');
(async () => {
  const [,, wiresDir, outDir, pageHtml, pageShot] = process.argv;
  fs.mkdirSync(outDir, { recursive: true });
  const browser = await chromium.launch();
  const ctx = await browser.newContext({ viewport: { width: 1280, height: 1080 }, deviceScaleFactor: 1, colorScheme: 'dark' });
  const page = await ctx.newPage();
  for (const f of fs.readdirSync(wiresDir).filter(x => x.endsWith('.html')).sort()) {
    await page.goto('file://' + path.resolve(wiresDir, f));
    await page.waitForTimeout(150);
    await page.screenshot({ path: path.join(outDir, f.replace('.html', '.png')), clip: { x: 0, y: 0, width: 1280, height: 1080 } });
  }
  if (pageHtml) {
    const p2 = await ctx.newPage(); await p2.setViewportSize({ width: 1300, height: 1000 });
    await p2.goto('file://' + path.resolve(pageHtml)); await p2.waitForTimeout(300);
    await p2.screenshot({ path: pageShot, fullPage: true });
  }
  await browser.close(); console.log('done');
})();
