var compressor = require('node-minify');

// Compress scripts to js/build.min.js
new compressor.minify({
  type: 'uglifyjs',
  fileIn: [
    'js/sakai.js',
    'js/query.js',
    'js/tools.js',
    'js/ui.js',
    'js/resizer.js',
    'js/options.js',
    'js/editors.js',
    'js/confirm.js',
    'js/i18n.js',
    'js/init.js'
  ],
  fileOut: 'js/build.min.js',
  callback: function(err){
    if (!err) {
      console.log("Build successful");
    } else {
      console.log("Build unsuccessful", error);
    }
  }
});
