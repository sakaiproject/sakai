import { playwrightLauncher } from '@web/test-runner-playwright';
import proxy from 'koa-proxies';

export default {
  files: './packages/**/test/**/*.test.js',
  nodeResolve: true,
  middleware: [
    proxy('/direct/profile/adrian/image/thumb', {
      target: '/test-static-assets/images/topov.jpg',
    }),
  ],
  testFramework: { config: { timeout: '5000' } },
  testRunnerHtml: testFramework => `
  <!DOCTYPE html>
    <html>
      <head>
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous">
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL" crossorigin="anonymous"></script>
      </head>
      <body>
        <script type="module" src="${testFramework}"></script>
      </body>
    </html>
  `,
  browsers: [
    playwrightLauncher({ product: 'chromium' }),
  ],
  filterBrowserLogs(log) {
    return log.type === 'error' || log.type === 'debug';
  },
};
