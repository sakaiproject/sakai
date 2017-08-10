var utils = require('./utils')
var config = require('../config')
var isProduction = process.env.NODE_ENV === 'production'

let loaders = utils.cssLoaders({
  sourceMap: isProduction
    ? config.build.productionSourceMap
    : config.dev.cssSourceMap,
  extract: isProduction
})
loaders.i18n = '@kazupon/vue-i18n-loader'

module.exports = {
  loaders: loaders
}
