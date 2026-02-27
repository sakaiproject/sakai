import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { visualizer } from 'rollup-plugin-visualizer';
import path from 'node:path';

// https://vitejs.dev/config/
export default defineConfig(() => {
  const generateStats = process.env.GENERATE_STATS === '1';
  const statsPath = path.resolve(process.cwd(), '../../../target/stats.json');

  return {
    plugins: [
      vue(),
      generateStats && visualizer({
        template: 'raw-data',
        filename: statsPath,
        gzipSize: false,
        brotliSize: false,
      }),
    ].filter(Boolean),
    build: {
      rollupOptions: {
        output: {
          entryFileNames: `assets/[name].js`,
          chunkFileNames: `assets/[name].js`,
          assetFileNames: `assets/[name].[ext]`
        }
      }
    },
  };
});
