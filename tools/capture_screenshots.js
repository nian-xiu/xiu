const { chromium } = require("playwright");
const path = require("path");

const root = process.cwd();
const out = path.join(root, "docs", "assets");

async function screenshot(page, url, name) {
  await page.goto(url, { waitUntil: "networkidle" });
  await page.screenshot({ path: path.join(out, name), fullPage: true });
}

(async () => {
  const browser = await chromium.launch({
    headless: true,
    executablePath: "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe"
  });
  const page = await browser.newPage({ viewport: { width: 1366, height: 900 }, deviceScaleFactor: 1 });

  await screenshot(page, "http://localhost:8080/", "screenshot-home.png");
  await screenshot(page, "http://localhost:8080/products", "screenshot-products.png");
  await screenshot(page, "http://localhost:8080/products/5", "screenshot-detail.png");
  await screenshot(page, "http://localhost:8080/login", "screenshot-login.png");

  await page.goto("http://localhost:8080/login", { waitUntil: "networkidle" });
  await page.fill("input[name='username']", "admin");
  await page.fill("input[name='password']", "admin123");
  await Promise.all([
    page.waitForNavigation({ waitUntil: "networkidle" }),
    page.click("button[type='submit']")
  ]);
  await page.screenshot({ path: path.join(out, "screenshot-admin.png"), fullPage: true });

  await browser.close();
})();
