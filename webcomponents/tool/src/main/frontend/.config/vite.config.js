// vite.config.js

import { defineConfig } from "vite";
import replace from "@rollup/plugin-replace";
import postcss from "rollup-plugin-postcss";
import postcsslit from "rollup-plugin-postcss-lit";
import resolve from "@rollup/plugin-node-resolve";
import alias from "@rollup/plugin-alias";
import litcss from "rollup-plugin-lit-css";
import Sass from "sass";
import path from "path";

// https://vitejs.dev/config/
// TODO TRINITY-43 review vite config before publishing
console.log(`Building ${process.env.VITE_BUILD} for ${process.env.NODE_ENV}`);

const rootDir = './';
export default defineConfig({
  root: rootDir,
  clearScreen: false,
  envDir: ".config",
  // optimizeDeps: {
  //   include: ['assets']
  // },
  build: {
    // cssCodeSplit: false,
    emptyOutDir: false,
    lib: {
      entry: "js/sakai-ui/sui.js",
      formats: ["es"],
      fileName: "sui",
    },
    target: "es2020",
    outDir: ".",
    rollupOptions: {
      external: [/assets/],
      plugins: [
        // alias({
        //   entries: [
        //     {
        //       find: /lit-element/,
        //       replacement: "../assets/lit-element.js?version=__buildNumber__",
        //     },
        //   ],
        // }),
        replace({
          preventAssignment: true,
          values: {
            '__buildNumber__': process.env.VITE_BUILD,
            // 'assets': `${process.cwd}assets`,

            // '@assets': './assets',
          }
        }),
        // resolve({
        //   moduleDirectories: ['./assets'],
        // }),
        // postcss({
        //     inject: false,
        //   }),
        // postcsslit({
        //   // importPackage: 'lit-element',
        // }),
        // litcss({
        //   specifier: 'lit-element',
        //   include: '/js/sakai-ui/styles/*.scss',
        //   transform: (data, { filePath }) =>
        //     Sass.renderSync({ data, file: filePath })
        //       .css.toString(),
        // }),
      ],
    },
  },
  server: {
    port: 8001,
    plugins: [
      resolve({
        moduleDirectories: ["../assets"],
      }),
    ],
  },
});
