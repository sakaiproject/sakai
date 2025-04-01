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
});
