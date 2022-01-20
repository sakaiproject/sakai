import fs from 'fs';

// const packages = fs
//   .readdirSync('sakai-ui')
//   .filter(dir => fs.statSync(`sakai-ui/${dir}`).isDirectory());

export default {
    concurrency: 10,
    // nodeResolve: {
    //     jail: './js/sakai-ui',
    // },
    nodeResolve: {
        moduleDirectories: ['node_modules', './assets'],
    },
    preserveSymlinks: true,
    coverage: true,
    debug: true,
    // in a monorepo you need to set set the root dir to resolve modules
    rootDir: '.',
    // manual: true,
    // open: true,
    watch: true,
    // files: '../sakai-ui/**/*.test.js',
    mimeTypes: {
        '**/*.scss': 'js',
    },
    groups: [
        {
            name: 'sui-table',
            files: 'js/sakai-ui/sui-table/test/*.test.js'
        },
    ],
    // groups: packages.map(pkg => ({
    //     name: pkg,
    //     files: `sakai-ui/${pkg}/test/**/*.test.js`,
    //   })),
  };
  