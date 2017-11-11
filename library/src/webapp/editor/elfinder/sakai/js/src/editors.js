(function($) {

var ui = $.sakai.elfinder.ui;

// CKEditor (html editor)
var ckeditor = (function() {
  var ckloaded = false; // ensures CKEditor is only loaded once
  var instance;         // one reference to the editor instance

  // Sets up the textarea
  var setup = function(textarea) {
    // Set the editor instance
    var editor = instance = CKEDITOR.replace(textarea.id, {
      startupFocus : true,
      fullPage: true,
      allowedContent: true,
      removePlugins: 'resize',
    });

    // Force CKEditor to resize with the dialog
    var $dialog = $(textarea).closest('.elfinder-dialog');
    $dialog.on('resize', function(event, ui) {
      var $content = $dialog.find('.ui-dialog-content');
      var height = $content.height() - 1;
      var width = $content.width();

      editor.resize(width, height);
    });
  };

  // Exposed methods
  return {
    mimes : ['text/html'],
    load : function(textarea) {
      var $dialog = $(textarea).closest('.elfinder-dialog');
      ui.setSaveCloseButtons($dialog);

      if (!ckloaded) {
        $.getScript('//cdn.ckeditor.com/4.5.2/standard/ckeditor.js', function() {
          setup(textarea);
        });
        ckloaded = true;
      } else {
        setup(textarea);
      }
    },

    close : function(textarea) {
      // ...
    },

    save : function(textarea) {
      if (instance) {
        textarea.value = instance.getData();
      }
    },

    focus : function(textarea) {
      // ...
    }
  };
})();

// Codemirror (code editor)
var codemirror = (function() {
  var url = '/library/webjars/codemirror/5.6/';
  var codemirrorjs = url + 'lib/codemirror.js';
  var scripts = []; // keeps track of loaded codemirror js files
  var instance;     // one reference to the editor instance

  // Sets up the textarea
  var setup = function(textarea, mime) {
    var $textarea = $(textarea);
    var $dialog = $textarea.closest('.elfinder-dialog');
    var config = { lineNumbers : true };
    if (mime) config.mode = mime;

    // Set the editor instance
    var editor = instance = CodeMirror.fromTextArea(textarea, config);

    // Set current dimensions
    var $content = $dialog.find('.ui-dialog-content').addClass('elfinder-codemirror');
    var setDimensions = function() {
      var width = $content.width();
      var height = $content.height();

      editor.setSize(width, height);
    };

    // Force CodeMirror to resize with the dialog
    $dialog.on('resize', setDimensions);

    // Force resizing immediately
    setDimensions();
  };

  // Checks if a codemirror script has already been loaded
  var loaded = function(url) {
    return scripts.indexOf(url) !== -1;
  };

  // Exposed methods
  return {
    load : function(textarea) {
      var $dialog = $(textarea).closest('.elfinder-dialog');
      ui.setSaveCloseButtons($dialog);

      var mime = this.file.mime;
      var run = function() {
        var mode = CodeMirror.findModeByMIME(mime).mode;
        var script = url + '/mode/' + mode + '/' + mode + '.js';

        // Do not load the mode script if the type is null
        if (mode === 'null') {
          scripts.push(script);
        }

        // Do not load the mode script if it has already been loaded before
        if (loaded(script)) {
          setup(textarea, mime);
          return;
        }

        $.getScript(url + '/mode/' + mode + '/' + mode + '.js')
        .done(function() {
          scripts.push(script);
          setup(textarea, mime);
        }).fail(function() {
          console.log('Failed to load mode for ' + mode);
          setup(textarea);
        });
      };


      if (!loaded(codemirrorjs)) {
        $('head').append($('<link rel="stylesheet" href="' + url + 'lib/codemirror.css">'));
        $.getScript(codemirrorjs, function() {
          scripts.push(codemirrorjs);
          $.getScript(url + 'mode/meta.js', run);
        });
      } else {
        run();
      }
    },

    close : function(textarea) {
      // ...
    },

    save : function(textarea) {
      if (instance) {
        textarea.value = instance.getValue();
      }
    },

    focus : function(textarea) {
      // ...
    }
  };
})();

$.sakai.elfinder.options.commandsOptions.edit.editors = [
  ckeditor,
  codemirror,
];

})(jQuery);
