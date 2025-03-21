const filteredLogs = ['Lit is in dev mode. Not recommended for production! See https://lit.dev/msg/dev-mode for more information.'];

export default ({
  files: 'test/**/*.test.js',

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
  testRunnerHtml: testFramework => `
  <!DOCTYPE html>
    <html>
      <head>
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous">
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL" crossorigin="anonymous"></script>
        <script src="https://cdn.ckeditor.com/4.23.0-lts/standard/ckeditor.js"></script>
      </head>
      <body>
        <script>window.process = { env: { NODE_ENV: "development" } }</script>
        <script type="module" src="${testFramework}"></script>
      </body>
    </html>
  `,
});
