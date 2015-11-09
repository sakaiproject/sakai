(function($, window) {

// When the document is ready, launch elfinder
$(function() {
  var options = $.sakai.elfinder.options;
  var ui = $.sakai.elfinder.ui;
  var resizer = $.sakai.elfinder.resizer;
  var confirm = $.sakai.elfinder.confirm;
  var $elfinder = $('#elfinder');
  var $window = $(window);

  // Launch elfinder
  $elfinder.elfinder(options);

  // Bind resizing functionality
  resizer($elfinder, $window);

  // Add extra UI
  ui($elfinder);

  // Confirmation logic (when OK is clicked)
  confirm($elfinder);
});

})(jQuery, window);
