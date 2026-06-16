import { spawn } from "node:child_process";
import { mkdir, rm, writeFile } from "node:fs/promises";
import path from "node:path";

const root = process.cwd();
const outDir = path.join(root, "docs", "report-v2-assets");
const userDataDir = path.join(root, "tmp", "chrome-report-v2-profile");
const chromePath = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
const port = 9223;
const base = `http://127.0.0.1:${port}`;

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

async function fetchJson(url, options) {
  const response = await fetch(url, options);
  if (!response.ok) {
    throw new Error(`${response.status} ${response.statusText}: ${url}`);
  }
  return response.json();
}

async function waitForChrome() {
  for (let i = 0; i < 60; i += 1) {
    try {
      await fetchJson(`${base}/json/version`);
      return;
    } catch {
      await delay(250);
    }
  }
  throw new Error("Chrome remote debugging port did not become ready");
}

class Cdp {
  constructor(webSocketDebuggerUrl) {
    this.nextId = 1;
    this.pending = new Map();
    this.ws = new WebSocket(webSocketDebuggerUrl);
  }

  async open() {
    await new Promise((resolve, reject) => {
      this.ws.addEventListener("open", resolve, { once: true });
      this.ws.addEventListener("error", reject, { once: true });
      this.ws.addEventListener("message", (event) => {
        const message = JSON.parse(event.data);
        if (message.id && this.pending.has(message.id)) {
          const { resolve: ok, reject: fail } = this.pending.get(message.id);
          this.pending.delete(message.id);
          if (message.error) fail(new Error(message.error.message));
          else ok(message.result);
        }
      });
    });
  }

  send(method, params = {}) {
    const id = this.nextId;
    this.nextId += 1;
    this.ws.send(JSON.stringify({ id, method, params }));
    return new Promise((resolve, reject) => {
      this.pending.set(id, { resolve, reject });
    });
  }

  close() {
    this.ws.close();
  }
}

async function newPage() {
  const target = await fetchJson(`${base}/json/new?about:blank`, { method: "PUT" });
  const cdp = new Cdp(target.webSocketDebuggerUrl);
  await cdp.open();
  await cdp.send("Page.enable");
  await cdp.send("Runtime.enable");
  await cdp.send("Network.enable");
  await cdp.send("Emulation.setDeviceMetricsOverride", {
    width: 1440,
    height: 900,
    deviceScaleFactor: 1,
    mobile: false,
  });
  return cdp;
}

async function navigate(cdp, url) {
  await cdp.send("Page.navigate", { url });
  for (let i = 0; i < 80; i += 1) {
    const result = await cdp.send("Runtime.evaluate", {
      expression: "document.readyState",
      returnByValue: true,
    });
    if (result.result.value === "complete") {
      await delay(600);
      return;
    }
    await delay(150);
  }
}

async function evalJs(cdp, expression) {
  const result = await cdp.send("Runtime.evaluate", {
    expression,
    awaitPromise: true,
    returnByValue: true,
  });
  if (result.exceptionDetails) {
    throw new Error(result.exceptionDetails.text || "Runtime evaluation failed");
  }
  return result.result.value;
}

async function fill(cdp, selector, value) {
  await evalJs(
    cdp,
    `(() => {
      const element = document.querySelector(${JSON.stringify(selector)});
      if (!element) throw new Error("Missing selector: ${selector}");
      element.focus();
      element.value = ${JSON.stringify(value)};
      element.dispatchEvent(new Event("input", { bubbles: true }));
      element.dispatchEvent(new Event("change", { bubbles: true }));
    })()`,
  );
}

async function click(cdp, selector) {
  await evalJs(
    cdp,
    `(() => {
      const element = document.querySelector(${JSON.stringify(selector)});
      if (!element) throw new Error("Missing selector: ${selector}");
      element.click();
    })()`,
  );
  await delay(900);
}

async function login(cdp, username, password) {
  await navigate(cdp, "http://localhost:8080/login");
  const result = await evalJs(
    cdp,
    `(async () => {
      const token = document.querySelector('meta[name="csrf-token"]')?.content || '';
      const body = new URLSearchParams();
      body.set('username', ${JSON.stringify(username)});
      body.set('password', ${JSON.stringify(password)});
      body.set('_csrf', token);
      const response = await fetch('/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body,
        redirect: 'follow',
      });
      document.open();
      document.write(await response.text());
      document.close();
      return { url: response.url, status: response.status, title: document.title };
    })()`,
  );
  console.log(`login ${username}: ${JSON.stringify(result)}`);
  await delay(800);
}

async function screenshot(cdp, url, fileName) {
  await navigate(cdp, url);
  const title = await evalJs(cdp, "document.title");
  const data = await cdp.send("Page.captureScreenshot", {
    format: "png",
    captureBeyondViewport: false,
    fromSurface: true,
  });
  await writeFile(path.join(outDir, fileName), Buffer.from(data.data, "base64"));
  console.log(`${fileName}: ${title}`);
}

async function main() {
  console.log("Preparing current-version screenshots...");
  await mkdir(outDir, { recursive: true });
  await rm(userDataDir, { recursive: true, force: true });
  await mkdir(userDataDir, { recursive: true });

  const chrome = spawn(chromePath, [
    "--headless=new",
    `--remote-debugging-port=${port}`,
    `--user-data-dir=${userDataDir}`,
    "--disable-gpu",
    "--disable-software-rasterizer",
    "--disable-dev-shm-usage",
    "--no-sandbox",
    "--hide-scrollbars",
    "--no-first-run",
    "--no-default-browser-check",
    "about:blank",
  ], { stdio: ["ignore", "pipe", "pipe"] });

  chrome.on("error", (error) => {
    console.error("Chrome launch error:", error);
  });
  chrome.stderr.on("data", (chunk) => {
    const text = chunk.toString();
    if (text.trim()) {
      console.error(text.trim());
    }
  });

  try {
    console.log("Waiting for Chrome debugging port...");
    await waitForChrome();
    console.log("Chrome ready.");
    const publicPage = await newPage();
    await screenshot(publicPage, "http://localhost:8080/", "current-home.png");
    await screenshot(publicPage, "http://localhost:8080/products", "current-products.png");
    await screenshot(publicPage, "http://localhost:8080/products/1", "current-detail.png");
    await screenshot(publicPage, "http://localhost:8080/login", "current-login.png");
    publicPage.close();

    const customerPage = await newPage();
    await login(customerPage, "customer", "123456");
    await screenshot(customerPage, "http://localhost:8080/activity", "current-activity.png");
    await screenshot(customerPage, "http://localhost:8080/backpack", "current-backpack.png");
    await screenshot(customerPage, "http://localhost:8080/orders", "current-orders.png");
    customerPage.close();

    const adminPage = await newPage();
    await login(adminPage, "admin", "admin123");
    await screenshot(adminPage, "http://localhost:8080/admin", "current-admin-dashboard.png");
    await screenshot(adminPage, "http://localhost:8080/admin/activities", "current-admin-rewards.png");
    await screenshot(adminPage, "http://localhost:8080/admin/products", "current-admin-products.png");
    adminPage.close();

    console.log(outDir);
  } finally {
    chrome.kill("SIGTERM");
  }
}

await main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
