'use strict'

const pkg = require('../package.json')
const year = new Date().getFullYear()

function getBanner(pluginFilename) {
  return `/*!
  * Sakai${pluginFilename ? ` ${pluginFilename}` : ''} ${pkg.name} v${pkg.version} (${pkg.homepage})
  * Copyright ${year}
  * Licensed under ECL (https://github.com/sakaiproject/sakai/blob/master/LICENSE)
  */`
}

module.exports = getBanner