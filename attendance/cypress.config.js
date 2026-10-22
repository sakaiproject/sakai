module.exports = {
  video: false,
  screenshotOnRunFailure: true,
  reporter: "spec",
  e2e: {
    baseUrl: process.env.CYPRESS_BASE_URL || "http://localhost:8080",
    supportFile: "cypress/support/e2e.js",
    defaultCommandTimeout: 15000,
    requestTimeout: 20000,
    retries: {
      runMode: 1,
      openMode: 0,
    },
    chromeWebSecurity: false,
  },
};
