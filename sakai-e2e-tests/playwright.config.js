const { defineConfig } = require('@playwright/test');

module.exports = defineConfig({
  testDir: './tests',
  testMatch: '**/*.spec.js',
  fullyParallel: false,
  // Keep workers at 1: these tests share Sakai accounts and UI/nav state,
  // and parallel workers cause cross-test interference and flaky tool navigation.
  workers: 1,
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
