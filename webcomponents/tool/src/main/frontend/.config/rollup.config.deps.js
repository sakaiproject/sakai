// vite.config.wc.js
// remove empathise

// - needs to still enable working in exploded webapp, don't bundle
// - storybook needs to read from sakai webapi to get mocked data
// - fetchmock/storybook doesn't feel dry anymore because you're updating the same thing

// requirements
// - has to deploy to /webcomponents/assets
// - has to append query string to imports from maven and/or npm
// - deploy dependencies but not devdependencies

//handling version string
// query parameter on import statements
// query parameter on filenames
// directory name on import statements
// move to etags???

// current deploy
// assets folder with dependencies, not devdependencies
// no node_modules folder
// all uncompiled files from js folder
import {dependencies} from '../package.json';
import replace from "@rollup/plugin-replace";
import postcss from "rollup-plugin-postcss";
import postcsslit from "rollup-plugin-postcss-lit";
import resolve from "@rollup/plugin-node-resolve";
import alias from "@rollup/plugin-alias";
import litcss from "rollup-plugin-lit-css";
import copy from 'rollup-plugin-copy'
import Sass from "sass";
import path from "path";

// TODO TRINITY-43 review vite config before publishing
// also renames them with the query string
// duplicates the directories which isn't ideal but doesn't break anything
const copyDependencies = () => {
  let output = [];
  Object.keys(dependencies).forEach(key => {
    output.push({
      src: `node_modules/${key}/**/*`,
      dest: `./target/node_modules`,
      // rename: (name, extension, fullPath) => `${fullPath}/${name}.${extension}?version=${process.env.VITE_BUILD}`,
    });
  });
  output.push({
    src: 'js/**/*',
    dest: './target',
    // rename: (name, extension, fullPath) => `${fullPath}?version=${process.env.VITE_BUILD}`,
  })
  return output;
};
const listDependencies = () => {
  let output = [];
  Object.keys(dependencies).forEach(key => {
    output.push(new RegExp(`${key}`));
  });
  return output;
};

console.log(`Building ${process.env.VITE_BUILD} for ${process.env.NODE_ENV}`);
const rootDir = "./js";
export default [{
  input: `${rootDir}/index.js`,
  output: {
    file: `./target/sakai.es.js`,
    format: "es",
},
external: listDependencies(),
  // optimizeDeps: {
  //   include: ["./assets"],
  // },
  // resolve: {
  //   alias: [
  //     { find: /lit-element/, replacement: path.resolve(__dirname, './node_modules/lit-element') },
  //   ]
  // },
    // cssCodeSplit: false,
    // emptyOutDir: false,
    // lib: {
    //   entry: "js/index.js",
    //   formats: ["es"],
    //   fileName: "sakai",
    // },
      // input: "./js/index.js",
      plugins: [
        // alias({
        //   entries: [
        //     { find: /.*lit-element.*/, replacement: path.resolve(__dirname, './node_modules/lit-element') },
        //   ]
        // }),
        copy({
          targets: copyDependencies(),
        //   verbose: true,
          flatten: false,
        //   hook: 'buildStart'
        }),
        resolve({
        //   // moduleDirectories: ["../target/node_modules"],
          // rootDir: path.join(process.cwd(), '..'),
        //   // hook: 'buildEnd',
        }),
        // replace({
        //   preventAssignment: true,
        //   values: {
            // '__buildNumber__': process.env.VITE_BUILD,
            // '../node_modules/': `./node_modules/`,
            // 'lit-element': path.resolve(__dirname, './node_modules/lit-element'),
            // '@assets': './assets',
        //   },
          // include: [
          //   '../target/sakai.es.js',
          //   './target/sakai.es.js',
          //   './sakai.es.js',
          //   '../sakai.es.js',
          // ],
          // hook: 'buildEnd'
        // }),
      ],
}];
