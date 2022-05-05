import fs from "fs";
import alias from "@rollup/plugin-alias";
import rollupPostcss from "rollup-plugin-postcss";
import rollupResolve from "@rollup/plugin-node-resolve";
import rollupReplace from '@rollup/plugin-replace';
import { fromRollup } from '@web/dev-server-rollup';
import { storybookPlugin } from "@web/dev-server-storybook";

const postcss = fromRollup(rollupPostcss);
const replace = fromRollup(rollupReplace);
const resolve = fromRollup(rollupResolve);

// TODO TRINITY-43 review config before publishing
import { importMapsPlugin } from '@web/dev-server-import-maps';
// const packages = fs
//   .readdirSync('sakai-ui')
//   .filter(dir => fs.statSync(`sakai-ui/${dir}`).isDirectory());

export default {
  concurrency: 10,
  // nodeResolve: {
  //     jail: './js/sakai-ui',
  // },
  nodeResolve: {
    moduleDirectories: ["js/sakai-ui/node_modules", "./assets"],
  },
  preserveSymlinks: true,
  coverage: true,
  // debug: true,
  // in a monorepo you need to set set the root dir to resolve modules
  rootDir: ".",
//   manual: true,
//   open: true,
  // watch: true,
  // files: '../sakai-ui/**/*.test.js',
  // Couldn't get the SCSS files to work with testing. Don't need SCSS in components at the moment, so no issue
  // Might be issue if we move to Shadown DOM
  // Might have to use postcss or something else to get the SCSS working with tests
  mimeTypes: {
    "**/*.scss": "js",
  },
  groups: [
    {
      name: "sui-table",
      files: "js/sakai-ui/sui-table/test/*.test.js",
    },
  ],
  // groups: packages.map(pkg => ({
  //     name: pkg,
  //     files: `sakai-ui/${pkg}/test/**/*.test.js`,
  //   })),
  plugins: [
    // replace({
    //     // 'js/sakai-ui/assets': '/assets'
    //     '../../assets': '/assets',
    //     '/js/sakai-ui/assets': '/assets'
    // }),
    // resolve({
    //     moduleDirectories: ['/assets', './assets', 'assets', '../assets', '../../assets'],
    // }),
    //Works just have to define every import manually
    importMapsPlugin({
        inject: {
            importMap: {
                imports: {
                    '/js/sakai-ui/assets/lit-element/lit-element.js?version=__buildNumber__': '/assets/lit-element/lit-element.js',
                    '/js/sakai-ui/assets/tabulator-tables/dist/js/tabulator_esm.min.js?version=__buildNumber__': '/assets/tabulator-tables/dist/js/tabulator_esm.min.js',
                    '/js/sakai-ui/assets/@fortawesome/fontawesome-svg-core/index.es.js?version=__buildNumber__': '/assets/@fortawesome/fontawesome-svg-core/index.es.js',
                    '/js/sakai-ui/assets/@fortawesome/free-solid-svg-icons/index.es.js?version=__buildNumber__': '/assets/@fortawesome/free-solid-svg-icons/index.es.js'
                }
            }
        }
    }),
    storybookPlugin({
      type: "web-components",
      configDir: "js/sakai-ui/.storybook" })
  ],
};
