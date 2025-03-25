import baseConfig from '../../web-test-runner.config.mjs';

export default ({
  ...baseConfig,

  rootDir: "../../",

  files: 'test/**/*.test.js',
});
