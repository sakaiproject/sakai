// Grunt: The JavaScript Task Runner
module.exports = function(grunt) {

  require('matchdep').filterDev('grunt-*').forEach(grunt.loadNpmTasks);

  grunt.initConfig({

    pkg: grunt.file.readJSON('package.json'),

    connect: {
      server: {
        options: {
          port: 8080,
          protocol: 'http',
          hostname: 'localhost',
          base: '.',
          keepalive: false,
          livereload: true,
          open: true
        }
      }
    },

    watch: {
      sass: {
        files: ['sass/{,**/}*.scss'],
        tasks: ['compass:dev']
      },

      scripts: {
        files: ['js/src/*.js', 'js/plugins/*.js'],
        tasks: ['concat', 'uglify'],
        options: {
          spawn: false,
        }
      },

      css: {
        files: ['*.css']
      },

      livereload: {
        files: [
          '**/*.css'
        ],

        options: {
          livereload: true
        }
      }
    },

    compass: {
      dist: {
        options: {
          environment: 'production',
          outputStyle: 'compressed',
          config: 'config.rb',
          sourcemap: true
        }
      },
      dev: {
        options: {
          environment: 'development',
          outputStyle: 'expanded',
          config: 'config.rb',
          sourcemap: true
        }
      }
    },

    concat: {   
      scripts: {
        src: ['js/src/*.js'], // All JS in the libs folder
        dest: 'js/morpheus.scripts.js',
      },
      plugins: {
        src: ['js/plugins/*.js'], // All JS in the libs folder
        dest: 'js/morpheus.plugins.js',
      }
    },

    uglify: {
      options: {
        banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - ' +
          '<%= grunt.template.today("yyyy-mm-dd") %>' + ' This file is compiled */'
      },
      scripts: {
        src: 'js/morpheus.scripts.js',
        dest: 'js/morpheus.scripts.min.js'
      },
      plugins: {
        src: 'js/morpheus.plugins.js',
        dest: 'js/morpheus.plugins.min.js'
      }
    }

  });

  grunt.registerTask('default', ['connect', 'watch']);
};