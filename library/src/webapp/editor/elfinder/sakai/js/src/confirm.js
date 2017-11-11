(function ($) {

var ui = $.sakai.elfinder.ui;
var elements = ui.elements;

// Set handlers to deal with extra data
$.sakai.elfinder.options.handlers = {
  // When a directory opens set the @cwd to that directory
  open : function(event, instance) {
    var $footer = $(elements.footer);
    $footer.data('cwd', event.data.options.url);
  },

  // When a file is selected, trigger options.getFileCallback
  // Otherwise, set the embed link to the cwd
  select : function(event, instance) {
    var selected = event.data.selected;
    var $footer = $(elements.footer);

    if (!selected.length) {
      // The folder url
      var cwd = $footer.data('cwd');
      $footer.data('embed', cwd);
    } else {
      $footer.data('embed', instance.file(selected[0]).url);
      instance.exec('getfile', selected);
    }
  },
};

// Check for enter key being hit
var enterHit;
$(document).keydown(function(e) {
  enterHit = e.which == 13;
}).click(function() {
  enterHit = false;
});

// When a file has been dblclicked, set the embed link to the file's path
$.sakai.elfinder.options.getFileCallback = function(file, instance) {
  instance.options.commandsOptions.getfile = { folders: false };
  var $footer = $(elements.footer);
  $footer.data('embed', file.url);
  $footer.data('file', file);

  if (enterHit) {
    $(elements.footer).find(elements.ok).click();
  }
};

// Binding embedding functionality to the OK and Cancel buttons
$.sakai.elfinder.confirm = function($elfinder) {
  var $footer = $elfinder.find(elements.footer);
  var funcNum = $.sakai.elfinder.query().CKEditorFuncNum;

  // When OK is clicked, embed the embed link
  $footer.on('click', '.button-ok', function(event) {
    var embed = $footer.data('embed');
    window.opener.CKEDITOR.tools.callFunction(funcNum, embed);
    window.close();
  });

  // When 'Cancel' is clicked, close the window
  $footer.on('click', '.button-cancel', function(event) {
    window.close();
  });
};

})(jQuery);
