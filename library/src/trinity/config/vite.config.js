import { defineConfig } from "vite";
import pkg from "../package.json";
import getBanner from "./banner";
import resolve from "@rollup/plugin-node-resolve";
export default defineConfig({
  build: {
    lib: {
      entry: "src/js/index.js",
      name: `sakai.${pkg.name}`,
      formats: ["es"],
      fileName: (format) => `sakai.${pkg.name}.${format}.js`,
    },
    outDir: "target/js",
    rollupOptions: {
      plugins: [resolve()],
      output: {
        banner: getBanner(),
        format: "es",
        sourcemap: true,
      },
    },
  },
});
