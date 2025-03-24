const filteredLogs = ['Lit is in dev mode. Not recommended for production! See https://lit.dev/msg/dev-mode for more information.'];

export default ({
  files: 'test/**/*.test.js',

  concurrency: 1,

  rootDir: '../../',

  nodeResolve: true,

  /** Filter out lit dev mode logs */
  filterBrowserLogs(log) {

    for (const arg of log.args) {
      if (typeof arg === 'string' && filteredLogs.some(l => arg.includes(l))) {
        return false;
      }
    }
    return true;
  },

  testRunnerHtml: testFramework =>
    `<html>
      <head>
        <link rel="stylesheet" src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.0.1/css/bootstrap.min.css" />
        <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.0.1/js/bootstrap.bundle.min.js" crossorigin="anonymous"></script>
      </head>
      <body>
        <script>window.process = { env: { NODE_ENV: "development" } }</script>
        <script type="module" src="${testFramework}"></script>
      </body>
    </html>`,
});
