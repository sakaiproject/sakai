var path = require('path');
var webpack = require('webpack');
module.exports = {
  entry: './sakai-element.js',
  output: {
    path: path.resolve(__dirname, 'build'),
    filename: 'app.bundle.js'
  },
  module: {
    use: {
      loader: 'babel-loader',
      options: {
        presets: ['env']
      }
    }
  },
  stats: {
    colors: true
  },
  devtool: 'source-map'
};
