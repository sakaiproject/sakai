import { defineConfig } from 'vite'
import postcss from 'rollup-plugin-postcss';
import postcsslit from 'rollup-plugin-postcss-lit';
import resolve from "@rollup/plugin-node-resolve";

// https://vitejs.dev/config/
export default defineConfig({
  build: {
    lib: {
      entry: 'js/sakai-bootstrap.js',
      formats: ['es']
    },
    rollupOptions: {
        plugins: [
            resolve({moduleDirectories: ['assets', 'node_modules']}),
            postcss({
                // ...
              }),
            postcsslit({
                importPackage: 'lit-element',
              }),
            // enforce: 'post'
        ],
        external: /^lit/,
    },
    outDir: ".",
  },
});