const { defineConfig } = require('@playwright/test');

const requestedWorkers = Number.parseInt(process.env.PLAYWRIGHT_WORKERS || '', 10);
const workers = Number.isFinite(requestedWorkers) && requestedWorkers > 0
  ? requestedWorkers
  : 1;

module.exports = defineConfig({
  testDir: './tests',
  testMatch: '**/*.spec.js',
  fullyParallel: false,
  workers,
  timeout: 180000,
  expect: {
    timeout: 20000,
  },
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI
    ? [['github'], ['html', { open: 'never', outputFolder: 'playwright-report' }]]
    : [['list'], ['html', { open: 'never', outputFolder: 'playwright-report' }]],
  use: {
    baseURL: process.env.PLAYWRIGHT_BASE_URL || 'http://127.0.0.1:8080',
    headless: true,
    ignoreHTTPSErrors: true,
    serviceWorkers: 'block',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
    video: 'retain-on-failure',
    actionTimeout: 30000,
    navigationTimeout: 120000,
  },
});
