var gulp = require('gulp');
var path = require("path");
var uglify = require('gulp-uglify');
var concat = require('gulp-concat');
var rename = require('gulp-rename');
var sass = require('gulp-sass');
var stylemod = require('gulp-style-modules');
var cleanCSS = require('gulp-clean-css');
var clean = require('gulp-clean');
var del = require('del');
var vulcanize = require('gulp-vulcanize');
var minifyInline = require('gulp-minify-inline');
var bower = require('gulp-bower');
var runSequence = require('run-sequence');

// for development, change this to point to your tomcat installation
//var server_base = "/opt/tomcat";

// Repository and server paths
var paths = {
    repo: {
        bower: "src/main/webapp/bower_components/",
        lib: "src/main/webapp/lib/",
        templates: "src/main/webapp/imports/",
        html: "../tool/src/java/org/sakaiproject/rubrics/tool/pages/",
        data: "src/main/webapp/data/*.json",
        sass: "src/main/webapp/sass/",
        js: "src/main/webapp/js/",
        css: "src/main/webapp/css/",
    }//,
    //server: {
    //    bower: server_base + "/webapps/rubrics-service/bower_components/",
    //    lib: server_base + "/webapps/rubrics-service/lib/",
    //    templates: server_base + "/webapps/rubrics-service/imports",
    //    html: server_base + "/webapps/rubrics-tool/WEB-INF/classes/org/sakaiproject/rubrics/tool/pages/",
    //    data: server_base + "/webapps/rubrics-service/data",
    //   js: server_base + "/webapps/rubrics-service/js",
    //    css: server_base + "/webapps/rubrics-service/css"
    //}
}

// ----------- Build Tasks
// Build task (called by mvaen-frontend-plugin)
gulp.task('build', function (done) {
  runSequence(
    'bower-install',
    'copy-lib-to-bower',
    'sass',
    done);
});

// Copies modified library files over to bower_components
gulp.task('copy-lib-to-bower', function (done) {
  gulp.src(paths.repo.lib + 'polymer-sortablejs/*.html')
    .pipe(gulp.dest(paths.repo.bower + 'polymer-sortablejs/'));

  done();
});

// Install Bower Components
gulp.task('bower-install',function () {
  return bower();
});

// Builds CSS files from SASS
// Compiles SASS in to Polymer style module
gulp.task('sass', function () {
  gulp.src(paths.repo.sass + 'sakai-rubrics-associate.scss')
    .pipe(sass().on('error', sass.logError))
    .pipe(gulp.dest(paths.repo.css));
    //.pipe(gulp.dest(paths.server.css));

  return gulp.src(paths.repo.sass + 'sakai-rubrics.scss')
    .pipe(sass().on('error', sass.logError))
    .pipe(stylemod())
    .pipe(gulp.dest(paths.repo.templates));
});

// ------------- Development Tasks

//// Watches all HTML, JS, CSS for changes
//gulp.task('watch', function () {
//    gulp.watch(paths.repo.sass + '*.scss', ['sass']);
//    gulp.watch(paths.repo.templates + '*.html', ['copy-templates']);
//    gulp.watch(paths.repo.html + '*.html', ['copy-html']);
//    gulp.watch(paths.repo.data, ['copy-data']);
//    gulp.watch(paths.repo.js + '*.js', ['copy-js']);
//});

//// Copies Web Component HTML to tomcat server
//gulp.task('copy-templates', function (callback) {
//    return gulp.src([
//        paths.repo.templates + '*.html',
//    ])
//    .pipe(gulp.dest(paths.server.templates));
//});

//// Copies Rubrics HTML to tomcat server
//gulp.task('copy-html', function (callback) {
//    return gulp.src([
//        paths.repo.html + '*.html',
//    ])
//    .pipe(gulp.dest(paths.server.html));
//});

//// Copies Rubrics JavaScript to tomcat server
//gulp.task('copy-js', function (callback) {
//    return gulp.src([
//        paths.repo.js + '*.js',
//   ])
//   .pipe(gulp.dest(paths.server.js));
//});

//// Removes bower_components on the tomcat server
//gulp.task('clean-bower', function (callback) {
//    return del(paths.server.bower, {force: true});
//});

//// Copies bower_components to tomcat server
//gulp.task('copy-bower', ['clean-bower'], function (callback) {
//    return gulp.src([
//        paths.repo.bower + "/**",
//    ])
//    .pipe(gulp.dest(paths.server.bower));
//});

// WIP: combine and minify Polymer include chain.
gulp.task('vulcanize-and-minify', function() {

  return gulp.src(paths.repo.templates + 'sakai-rubrics-manager-src.html')

    // Vulcanize, inline all the things
    // except for Polymer, which we leave alone.
    .pipe(vulcanize({
      inlineScripts: true,
      inlineCss: true,
      stripExcludes: false,
      excludes: [paths.repo.bower + 'polymer/polymer.html']
    }))

    // Crush all that JS and CSS and pipe out.
    .pipe(minifyInline())
    .pipe(rename('sakai-rubrics-manager.html'))
    .pipe(gulp.dest(paths.repo.templates));
    //.pipe(gulp.dest(paths.server.templates));

});
