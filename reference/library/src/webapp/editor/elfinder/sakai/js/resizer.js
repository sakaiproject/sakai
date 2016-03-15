(function ($) {

var border = 2;

// Auto resizing
// Solution by @oyejorge on
// https://github.com/Studio-42/elFinder/issues/84
$.sakai.elfinder.resizer = function($elfinder, $window) {
  $window.resize(function() {
    var winHeight = $window.height() - border;
    if ($elfinder.height() !== winHeight) {
      $elfinder.height(winHeight).resize();
    }
  });
};

})(jQuery);
