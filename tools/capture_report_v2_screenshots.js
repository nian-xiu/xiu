const { chromium } = require("playwright");
const fs = require("fs");
const path = require("path");

const root = process.cwd();
const out = path.join(root, "docs", "report-v2-assets");
fs.mkdirSync(out, { recursive: true });

async function shot(page, url, name, options = {}) {
  await page.goto(url, { waitUntil: "networkidle" });
  if (options.waitFor) {
    await page.waitForSelector(options.waitFor, { timeout: 8000 });
  }
  await page.screenshot({
    path: path.join(out, name),
    fullPage: false,
  });
}

(async () => {
  const browser = await chromium.launch({
    headless: true,
    executablePath: "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
  });
  const page = await browser.newPage({
    viewport: { width: 1440, height: 900 },
    deviceScaleFactor: 1,
  });

  await shot(page, "http://localhost:8080/", "current-home.png", { waitFor: ".hero" });
  await shot(page, "http://localhost:8080/products", "current-products.png", { waitFor: ".product-grid" });
  await shot(page, "http://localhost:8080/products/1", "current-detail.png", { waitFor: ".detail-layout" });
  await shot(page, "http://localhost:8080/login", "current-login.png", { waitFor: "form" });

  await page.goto("http://localhost:8080/login", { waitUntil: "networkidle" });
  await page.fill("input[name='username']", "customer");
  await page.fill("input[name='password']", "123456");
  await Promise.all([
    page.waitForNavigation({ waitUntil: "networkidle" }),
    page.click("button[type='submit']"),
  ]);
  await shot(page, "http://localhost:8080/activity", "current-activity.png", { waitFor: ".page-hero" });
  await shot(page, "http://localhost:8080/backpack", "current-backpack.png", { waitFor: ".page-hero" });
  await shot(page, "http://localhost:8080/orders", "current-orders.png", { waitFor: "main" });

  await page.goto("http://localhost:8080/logout", { waitUntil: "networkidle" });
  await page.goto("http://localhost:8080/login", { waitUntil: "networkidle" });
  await page.fill("input[name='username']", "admin");
  await page.fill("input[name='password']", "admin123");
  await Promise.all([
    page.waitForNavigation({ waitUntil: "networkidle" }),
    page.click("button[type='submit']"),
  ]);
  await shot(page, "http://localhost:8080/admin", "current-admin-dashboard.png", { waitFor: ".admin-hero" });
  await shot(page, "http://localhost:8080/admin/activities", "current-admin-rewards.png", { waitFor: ".admin-hero" });
  await shot(page, "http://localhost:8080/admin/products", "current-admin-products.png", { waitFor: ".admin-shell" });

  await browser.close();
  console.log(out);
})();
