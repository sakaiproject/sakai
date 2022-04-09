// vite.config.wc.js
// # remove empathise
// ## working in exploded webapp
// - not really going to work anymore, use npm run wc:dev
// # cachebusting
// - uses hashed filenames instead of query strings on source files
// - sakai.es.js filename remains static and easy link in portal
// - compresses lit-element and lit-html and handles import resolution

// TODO
// - storybook needs to read from sakai webapi to get mocked data
// - fetchmock/storybook doesn't feel dry anymore because you're updating the same thing twice

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
import { terser } from 'rollup-plugin-terser';
const copyPost = copy;

// TODO TRINITY-43 review vite config before publishing
const CDN = process.env.CDN ? process.env.CDN : '';
const VITE_BUILD = process.env.VITE_BUILD ? process.env.VITE_BUILD : 'production';

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
const listDependencies = (outputType) => {
  let output = [];
  if (outputType === 'array'){
    Object.keys(dependencies).forEach(key => {
      output.push(new RegExp(`${key}`));
    });
  }
  if (outputType === 'object'){
    output = {};
    Object.keys(dependencies).forEach(key => {
      output[key] = [key];
    });
  }
  return output;
};
// const target = (VITE_BUILD === 'production') ? './target' : '~/dev/profmikegreene/docker-sakai-builder/work/tomcat/deploy/webapps/webcomponents/';
const target = './target';
const rootDir = "./";
export default defineConfig({
  root: rootDir,
  clearScreen: false,
  envDir: ".config",
  // base: "/webcomponents/",
  mode: `${VITE_BUILD}`,
  
  resolve: {
    alias: {
      '@assets': path.resolve(__dirname, '../node_modules'),
    }
      // { find: /lit-element/, replacement: path.resolve(__dirname, './node_modules/lit-element') },
  },
  build: {
    // write: false,
    // cssCodeSplit: false,
    emptyOutDir: false,
    // lib: {
    //   entry: "js/index.js",
    //   formats: ["es"],
    //   fileName: "sakai",
    // },
    target: "es2020",
    manifest: true,
    outDir: target,
    minify: (VITE_BUILD === 'production') ? true : false,
    rollupOptions: {
      // TODO how to make it so we don't define these manually
      input: [
        "./js/sakai-ui/sui-button/sui-button.js",
        "./js/sakai-ui/sui-icon/sui-icon.js",
        "./js/sakai-ui/sui-table/sui-table.js",
        "./js/sakai-search.js",
        "./js/sakai-permissions.js",
        "./js/sakai-maximise-button.js",
        "./js/rubrics/sakai-rubric-student-button.js",
        "./js/rubrics/sakai-rubrics-language.js",

        "./js/sakai.js",
      ],
      output: {
        // entryFileNames: name === 'sakai.es' ? '[name].js' : '[name].[hash].js',
        entryFileNames: (chunkInfo) => {
          if (chunkInfo.name.includes('sakai')
          || chunkInfo.name.includes('sakai-element')) {
            // return `sakai.es.js`;
            return `${chunkInfo.name}.${CDN}.js`;
          }
          return `[name].[hash].js`;
          // return `[name].js?version=${CDN}`;
        },
        chunkFileNames: `assets/[name].[hash].js`,
        assetFileNames: '[name].[hash][extname]',
        // manualChunks: listDependencies('object'),
        manualChunks: {
          'lit-element': ['lit-element'],
          'lit-html': ['lit-html'],
        },
        // format: 'es',
        dir: './target',
      },
      // preserveEntrySignatures: 'strict',
      // external: listDependencies('array'),
      plugins: [
        // copy({
          //   targets: copyDependencies(),
          //   // verbose: true,
        //   flatten: false,
        //   hook: 'buildStart',
        // }),
        (VITE_BUILD === 'production') ? copyPost({
          targets: copyDependencies(),
          // verbose: true,
          flatten: false,
          hook: 'closeBundle',
        }) : '',
        // uncomment for more flexible terser options
        // terser({
        //   mangle: false,
        //   compress: false,
        //   toplevel: false,
        //   keep_classnames: true,
        //   keep_fnames: true,
        // }),
        nodeResolve({
        //   browser: true,
        //   preferBuiltins: false,
          // moduleDirectories: [
            // "../target",
            // './target',
            // 'target',
            // /assets/,
          // ],
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
            '@assets': `${path.resolve("node_modules")}`,
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
