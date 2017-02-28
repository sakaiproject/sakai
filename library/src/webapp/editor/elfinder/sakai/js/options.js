(function($) {

var $window = $(window);
var query = $.sakai.elfinder.query();
var url = '/' + (query.connector || '');
var startDir = query.startdir;
var lang = query.langCode;

if (startDir) {
  // Remove first /prefix/
  startDir = startDir.split('/');
  startDir = startDir.filter(function(elem) { return elem; });
  startDir = startDir.slice(1);
  startDir = '/' + startDir.join('/');

  // Hashing
  startDir = btoa(startDir);
  startDir = startDir.replace('+', '-')
                     .replace('/', '_')
                     .replace('=', '.');
}

var type = query.type;
var onlyMimes = [];

if (type == 'image') {
  onlyMimes = ['image'];
} else if (type == 'flash') {
  onlyMimes = ['application/x-shockwave-flash'];
}


var testHelp = {
  type: 'dialog',
  title: 'Help',
  content: 'example',
};

var ui = $.sakai.elfinder.ui;
var tools = $.sakai.tools;

$.sakai.elfinder.options = {
  // Connector script
  url : url,

  // Starting directory
  startPathHash : startDir,

  // Language
  lang: lang,

  // Default view
  defaultView: 'list',

  // When a file is clicked, its data will be sent back to the editor that
  // instantiated it, and this window will close
  getFileCallback : function(file) {
    //setFileData(file);
    //$elfinder.find('#sakai-ui-footer .button-ok').click();
  },

  // Restrict to certain file types
  onlyMimes: onlyMimes,

  // Dimensions
  height: $window.height(),

  // Buttons available on the toolbar
  uiOptions: {
    toolbar : [
      ['help', testHelp],
      ['back', 'forward'],
      ['reload'],
      ['home', 'up'],
      ['mkdir', 'upload'],
      ['open', 'download', 'getfile'],
      ['info'],
      ['quicklook'],
      ['copy', 'cut', 'paste'],
      ['search'],
      ['view'],
    ],

    tree : {
      // Styles tool icons in the navbar
      // Currently not functional until directories get a 'tool' property
      // This implementation assumes that the property will just be the tool's
      // name, which will be set to lower case and used for a css class
      // such as 'sakai-tool-attachments'
      getClass: function(directory) {
        var classes = '';

        if (directory.tool && $.inArray(directory.tool, tools)) {
          classes = 'sakai-tool-' + directory.tool.toLowerCase();
        }

        return classes;
      }
    }
  },

  // Command-specific options
  commandsOptions : {
    info: {
      // Custom properties for the info dialog
      custom: {
        // ...
      }
    },

    edit : {
      // File editors (done in full on the editors.js file)
      editors : [],
    }
  },

  // Fullscreen editor, so no resizing
  resizable: false,
};

})(jQuery);
