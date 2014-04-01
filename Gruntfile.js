/**
 * Build process for CKEditor AutoSave Plugin
 * This file contributed by Timm Stokke <timm@stokke.me>
 *
 * Don't know where to start?
 * Try: http://24ways.org/2013/grunt-is-not-weird-and-hard/
 */
module.exports = function(grunt) {

  // CONFIGURATION
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),

    // Minimize JS
    min: {
      options: {
        report: false
      },
      difflib: {
        src: [
          'autosave/js/difflib.js',
          'autosave/js/diffview.js',
          'autosave/js/jsdiff.js',
          'autosave/js/moment.js',
          'autosave/js/lz-string-1.3.3.js'
          ],
        dest: 'autosave/js/extensions.min.js',
      }
    },

    // CSS Minify
    cssmin: {
      combine: {
        files: {
          'autosave/css/autosave.min.css': ['autosave/css/autosave.css']
        }
      }
    },

  });

  // PLUGINS
  grunt.loadNpmTasks('grunt-yui-compressor');


  grunt.registerTask('default', [
    'min',
    'cssmin'
    ]);

};
