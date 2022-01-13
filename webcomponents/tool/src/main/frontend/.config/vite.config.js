// vite.config.js

import { defineConfig } from 'vite'
import replace from '@rollup/plugin-replace';
import postcss from 'rollup-plugin-postcss';
import postcsslit from 'rollup-plugin-postcss-lit';
import resolve from "@rollup/plugin-node-resolve";
import litcss from "rollup-plugin-lit-css";
import Sass from "sass";
import path from 'path';

// https://vitejs.dev/config/
export default defineConfig({
  // root: 'js/sakai-ui',
  envDir: '.config',
  build: {
    // cssCodeSplit: false,
    lib: {
      entry: 'js/sakai-ui/sui.js',
      formats: ['es'],
      fileName: 'sui'
    },
    outDir: ".",
    rollupOptions: {
      external: [
        /assets/,
        /node_modules/,
      ],
      plugins: [
        replace({
          preventAssignment: true,
          values: {
            '__buildNumber__': process.env.VITE_BUILD,
          }
        }),
        // resolve({
        //   moduleDirectories: ['../assets', '../node_modules'],
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
});