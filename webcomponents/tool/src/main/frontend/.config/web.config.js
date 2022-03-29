import { copy } from '@web/rollup-plugin-copy';

export default {
  input: './js/index.js',
  output: {
    dir: 'target',
    format: 'es',
  },
  plugins: [
      copy({ 
          patterns: '**/*.{svg,jpg,json}' 
        })
    ],
};