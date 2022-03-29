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
import { defineConfig } from "vite";
import replace from "@rollup/plugin-replace";
import postcss from "rollup-plugin-postcss";
import postcsslit from "rollup-plugin-postcss-lit";
import nodeResolve from "@rollup/plugin-node-resolve";
import alias from "@rollup/plugin-alias";
import litcss from "rollup-plugin-lit-css";
import copy from 'rollup-plugin-copy';
import Sass from "sass";
import path from "path";

const copyPost = copy;

// TODO TRINITY-43 review vite config before publishing


const copyDependencies = () => {
  let output = [];
  Object.keys(dependencies).forEach(key => {
    output.push({
      src: `node_modules/${key}/**/*`,
      dest: `./target/assets`,
      // rename: (name, extension, fullPath) => `${fullPath}/${name}.${extension}?version=${process.env.VITE_BUILD}`,
    });
  });
  // output.push({
  //   src: 'js/**/*',
  //   dest: './target',
  //   // rename: (name, extension, fullPath) => `${fullPath}?version=${process.env.VITE_BUILD}`,
  // })
  return output;
};
const listDependencies = () => {
  let output = [];
  Object.keys(dependencies).forEach(key => {
    output.push(new RegExp(`${key}`));
  });
  return output;
};
console.log(path.resolve(__dirname, "../node_modules/lit-element/lit-element.js"));
console.log(`Building ${process.env.VITE_BUILD} for ${process.env.NODE_ENV}`);
const rootDir = "./";
export default defineConfig({
  root: rootDir,
  clearScreen: false,
  envDir: ".config",
  // base: "/webcomponents/",
  mode: "development",
  
  // resolve: {
  //   alias: {
  //     '@assets': path.resolve(__dirname, './target/assets'),
  //   }
  //     // { find: /lit-element/, replacement: path.resolve(__dirname, './node_modules/lit-element') },
  // },
  build: {
    // write: false,
    // cssCodeSplit: false,
    // emptyOutDir: false,
    lib: {
      entry: "js/index.js",
      formats: ["es"],
      fileName: "sakai",
    },
    target: "es2020",
    manifest: true,
    outDir: "./target/bundle",
    rollupOptions: {
      // input: "./js/index.js",
      external: listDependencies(),
      plugins: [
        copy({
          targets: copyDependencies(),
          // verbose: true,
          flatten: false,
          hook: 'buildStart',
        }),
        // copyPost({
        //   targets: copyDependencies(),
        //   // verbose: true,
        //   flatten: false,
        //   hook: 'closeBundle',
        // }),
        nodeResolve({
        //   browser: true,
        //   preferBuiltins: false,
        //   moduleDirectories: [
        //     // "../target",
        //     // './target',
        //     // 'target',
        //     /assets/,
        //   ],
        //   // rootDir: path.join(process.cwd(), '..'),
        //   // rootDir: path.join(process.cwd(), '/target'),
        //   // hook: 'buildEnd',
        }),
        // alias({
        //   entries: [
        //     // { find:/lit-element/, replacement:path.resolve(__dirname, '/webcomponents/assets')},

        //     // {
        //       // find: "lit-element/lit-element.js",
        //       // replacement: path.resolve(__dirname, "../node_modules/lit-element/lit-element.js")
        //     // },
        //   ]
        // }),
        replace({
          preventAssignment: true,
          delimiters: ['', ''],
          values: {
            // '__buildNumber__': process.env.VITE_BUILD,
            // '../node_modules/': `./node_modules/`,
            // 'lit-element/lit-element.js': './node_modules/lit-element/lit-element.js',
            '@assets': `${path.resolve("assets")}`,
          },
          // include: [
          //   '../target/sakai.es.js',
          //   './target/sakai.es.js',
          //   './sakai.es.js',
          //   '../sakai.es.js',
          // ],
          // hook: 'buildEnd'
        }),
      ],
    },
  },
  server: {
    port: 8001,
    plugins: [
      // resolve({
      //   moduleDirectories: ["../assets"],
      // }),
    ],
  },
});
