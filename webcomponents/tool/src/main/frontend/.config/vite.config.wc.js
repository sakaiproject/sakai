// vite.config.wc.js
// remove empathise

// - needs to still enable working in exploded webapp, don't bundle
// - storybook needs to read from sakai webapi to get mocked data
// - fetchmock/storybook doesn't feel dry anymore because you're updating the same thing

// requirements
// - has to deploy to /webcomponents/assets
// - has to append query string to imports from maven and/or npm
// - deploy dependencies but not devdependencies

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

const rootDir = "./js";
export default defineConfig({
  root: rootDir,
  clearScreen: false,
  envDir: ".config",
  base: "/webcomponents/",
  mode: "development",
  // optimizeDeps: {
  //   include: ["assets"],
  // },
  build: {
    // cssCodeSplit: false,
    // emptyOutDir: false,
    // lib: {
    //   entry: "index.js",
    //   formats: ["es"],
    //   fileName: "sakai",
    // },
    target: "es2020",
    manifest: true,
    // outDir: "../dist",
    rollupOptions: {
      // input: "./js/index.js",
      external: [/assets/, /node_modules/],
      plugins: [
        resolve({
          // moduleDirectories: ["./assets"],
        }),
        // replace({
        //   preventAssignment: true,
        //   values: {
        //     '__buildNumber__': process.env.VITE_BUILD,
        //     // 'assets': `${process.cwd}assets`,

        //     // '@assets': './assets',
        //   }
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
