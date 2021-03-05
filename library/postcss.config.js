module.exports = (ctx) => ({
  parser: 'postcss-sass',
  plugins: [
    require('cssnano')({
        preset: 'default',
      }),
  ],
})