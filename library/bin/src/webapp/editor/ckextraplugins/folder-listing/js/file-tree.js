/**
  * jQuery File Tree Plugin

  * Originally authored by Cory S.N. LaViska
  * Edited by Lawrence Okoth-Odida
  * A Beautiful Site (http://abeautifulsite.net/)
  * Authored 24 March 2008
  * Edited   28 July 2014

  * USAGE: $('.fileTreeDemo').fileTree(options)

  * settings:  root           - root folder to display; default = /
  *           folderEvent    - event to trigger expand/collapse; default = click
  *           expandSpeed    - default = 500 (ms); use -1 for no animation
  *           collapseSpeed  - default = 500 (ms); use -1 for no animation
  *           expandEasing   - easing function to use on expand (optional)
  *           collapseEasing - easing function to use on collapse (optional)
  *           multiFolder    - whether or not to limit the browser to one subfolder at a time
  *           enableHighlight - whether or not to show the yellow highlight
  *           loadMessage    - Message to display while initial tree loads (can be HTML)

  * TERMS OF USE
      This plugin is dual-licensed under the GNU General Public License and the MIT License and
      is copyright 2008 A Beautiful Site, LLC.
  */
(function($) {
$.fn.fileTree = function(options) {
  // Default settings
  var settings = $.extend({
    // general display settings
    root: '/',
    script: 'fileTree.php',
    folderEvent: 'click',
    expandSpeed: 500,
    collapseSpeed: 500,
    expandEasing: null,
    collapseEasing: null,
    multiFolder: true,
    loadMessage: 'Loading...',
    openToFolder: false,
    enableHighlight: true,

    // configuring the ajax call
    ajaxUrl: function(dir) {
      return settings.script;
    },
    ajaxDataType: 'html',
    ajaxType: 'POST',
    ajaxData: function(dir) {
      return { dir: dir };
    },
    formatResults: function(data) {
      return data;
    },

    // events and callbacks
    onFolderEvent: function(folder) {},
    onFileEvent:   function(file) {},
    afterShowTree: function(element) {},
    afterBindTree: function(element) {},
  }, options);

  // backwards compatibility
  if (arguments.length > 1) {
    settings.onFileEvent = arguments[1] || settings.onFileEvent;
  }

  var showTree = function(element, dir) {
    var $element = $(element);

    $element.addClass('wait');
    $(".jqueryFileTree.start").remove();

    $.ajax({
      url: settings.ajaxUrl(dir),
      dataType: settings.ajaxDataType,
      type: settings.ajaxType,
      data: settings.ajaxData(dir),
      cache:false,
      success: function(json) {
        var data = settings.formatResults(json);

        $element.find('.start').html('');
        $element.removeClass('wait').append(data);

        if (settings.root == dir) {
          $element.find('ul:hidden').show();
        } else {
          $element.find('ul:hidden').slideDown({ duration: settings.expandSpeed, easing: settings.expandEasing });
        }

        $element.trigger('fileTree_showTree');
        settings.afterShowTree(element);

        bindTree(element);
      },
      error: function() {
        $element.removeClass('wait');
        console.log('Folder "' + dir + '" not found');
      }
    });
  };

  var bindTree = function(element) {
    var $anchors = $(element).find('li a');

    $anchors.on(settings.folderEvent, function() {
      var $this = $(this);
      var $parent = $this.parent();
      //Check if yellow highlight is needed for selection
      if(settings.enableHighlight){
        // reset 'active' element and set it to current directory/file
        $parent.closest('div > .jqueryFileTree').find('.active').removeClass('active');
        $parent.addClass('active');
      }
      if ($parent.hasClass('directory')) {
        if ($parent.hasClass('collapsed')) {
          // expand
          if (!settings.multiFolder) {
            $parent.parent().find('ul').slideUp({
              duration: settings.collapseSpeed,
              easing: settings.collapseEasing
            });
            $parent.parent().find('li.directory').removeClass('expanded').addClass('collapsed');
          }
          $parent.find('ul').remove(); // cleanup
          showTree($parent, encodeURI($this.attr('rel').match( /.*\// )));
          $parent.removeClass('collapsed').addClass('expanded');
        } else {
          // collapse
          $parent.find('ul').slideUp({ duration: settings.collapseSpeed, easing: settings.collapseEasing });
          $parent.removeClass('expanded').addClass('collapsed');
        }

        settings.onFolderEvent($this);
      } else {
        settings.onFileEvent($this.attr('rel'));
      }

      return false;
    });

    // prevent a from triggering the # on non-click events
    if (settings.folderEvent.toLowerCase() != 'click') {
      $anchors.on('click', function() {
        return false;
      });
    }

    $anchors.trigger('fileTree_bindTree');

    settings.afterBindTree(element);
  };

  var openToFolder = function($tree, dir) {
    // make array of remaining folders to enter
    var directoryList = dir.replace(settings.root, '');
        directoryList = directoryList.split('/');
        directoryList = directoryList.filter(function(e) { return e; });

    // note current path (this string will be expanded)
    var currentPath = settings.root;
    var i = 0;

    // when a new folder has been inserted to the tree, find the right link and click it
    $tree.on('DOMNodeInserted', 'ul', function() {
      if (i >= directoryList.length) {
        return;
      }

      var nextElem = directoryList[i];
      currentPath += nextElem + '/';

      var a = $(this).find('[rel="'+ currentPath +'"], [rel="'+ currentPath +'/"]').first();

      if (a.length > 0) {
        a.on('fileTree_bindTree', function() {
          $(this).click();
        });
      }

      i++;
    });
  };

  return this.each(function() {
    var $this = $(this);

    // loading message
    $this.html('<ul class="jqueryFileTree start"><li class="wait">' + settings.loadMessage + '<li></ul>');

    // get the initial file list
    showTree($this, encodeURI(settings.root));

    // open to the correct folder
    if (settings.openToFolder) {
      openToFolder($this, settings.openToFolder);
    }
  });
};
})(jQuery);
