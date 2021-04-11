process.env.CHROME_BIN = require('puppeteer').executablePath();

module.exports = function(config) {

  config.set({
    basePath: "",
    frameworks: ["jasmine"],
    files: [
      { pattern: "../test/**/*.test.js", type: "module", included: true },
      { pattern: "../node_modules/jasmine-ajax/lib/mock-ajax.js", type: "module", included: true },
      { pattern: "../../**/*.js", type: "module", included: false }
    ],
    exclude: [],
    reporters: ["spec"],
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: true,
    browsers: ["ChromeHeadless"],
    concurrency: Infinity,
    singleRun: true,
  });
};
