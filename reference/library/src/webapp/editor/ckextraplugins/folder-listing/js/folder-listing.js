/**
  * Title:        folderListing (Oxford WebLearn integration of jQuery fileTree)
  * Description:  Provides a listing of folders in the Resources section of
                  Weblearn given a relative path (e.g. /groups/...)
  * Author:       Lawrence Okoth-Odida
  * Version:      0.1
  * Date:         30/07/2014

  * BASIC USAGE

  * ADVANCED USAGE

  */

(function($) {
$.fn.folderListing = function(options) {
  // Default settings
  var settings = $.extend({
    openToFolder: false,
    enableHighlight: true,
    displayRootDirectory: false,
    onFolderEvent: function(folder) {},
    onFileEvent: function(file) {
      window.open(file);
    },
    afterShowTree: function(element) {},
    afterBindTree: function(element) {}
  }, options);

  var urlPrefix = '/direct/content/resources/';

  var getHtmlResults = function(json, $div, root) {
    var html = $('<ul/>').addClass('jqueryFileTree').hide();
    var urlRoot = json[0].resourceId.trim();
    var container = html;
    //If we've already added the root folder?.
    var rootFolderCheck = $div.children().length === 0;
    if(settings.displayRootDirectory && urlRoot === root && rootFolderCheck){
        var liRoot = $('<li/>');
        var aRoot = $('<a/>').attr('href', '#');
        aRoot.attr('rel', urlRoot);
        aRoot.html($('<span/>').addClass('name').html(json[0].name));
        liRoot.addClass('directory active expanded');
        liRoot.append(aRoot);
        var ul = $('<ul/>').addClass('jqueryFileTree').hide();
        liRoot.append(ul);
        html.append(liRoot);
        container = ul;
    }
    for (i in json[0]['resourceChildren']) {
      var file = json[0]['resourceChildren'][i];
        if (i!='contains') {

      var li = $('<li/>');
      var a = $('<a/>').attr('href', '#');
      var url = file.resourceId.trim();

      a.attr('rel', url);
      a.html($('<span/>').addClass('name').html(file.name));
      a.append($('<span/>').addClass('icons'));
      li.append($('<div/>').addClass('tooltips'));

      // adding the copyright data
      if ($div.data('copyright') && file['properties']['CHEF:copyrightalert']) {
        var copyrightNotice = $('<div/>').addClass('copyright').html(file['properties']['CHEF:copyrightchoice']).hide();
        var copyrightSymbol = $('<span/>').addClass('copyright').html('&copy;');

        a.find('.icons').append(copyrightSymbol);
        li.find('.tooltips').append(copyrightNotice);
      }

      // adding the description
      if (file['description']) {
        var description = $('<div/>').addClass('description').html(file['description']).hide();
        var descriptionIcon = $('<span/>').addClass('description');

        a.attr('id', file['description']).find('.icons').append(descriptionIcon);

        li.find('.tooltips').append(description);
      }

      // show correct class for directory or folder
      if (file.mimeType) {
        // file
        var ext = file.url.split('.');

        if (ext.length > 1) {
          ext = ext[ext.length-1];
        }

        li.addClass('file ext_' + ext);
        a.attr('rel', file.url);
      } else {
        // folder
        if (!file.hidden) {
            li.addClass('directory collapsed');
        } else {
            li.addClass('directory collapsed inactive');
        }

        // show the number of files
        if ($div.data('files')) {
          var numIcon = $('<span/>').addClass('files').html('(Counting files...)');
          a.find('.icons').append(numIcon);

          $.ajax({
            url: urlPrefix + url + '.json',
            dataType: 'json',
            cache: false,
            success: function(data) {
              var span = $div.find('[rel="' + data['content_collection'][0]['resourceId'] + '"] .files');
              var files = data['content_collection'][0]['resourceChildren'].length;
              var message = files + ' ' + ((files == 1) ? 'file' : 'files');
              span.html('(' + message + ')');
            }
          });
        }
      }
      li.append(a);
      container.append(li);
    }}

    $div
      // show/hide copyright notices
      .on('mouseenter', 'span.copyright', function() {
        $(this).closest('li').find('.tooltips .copyright').show();
      })
      .on('mouseleave', 'span.copyright', function() {
        $(this).closest('li').find('.tooltips .copyright').hide();
      })
      // show/hide descriptions
      .on('mouseenter', 'span.description', function() {
        $(this).closest('li').find('.tooltips .description').show();
      })
      .on('mouseleave', 'span.description', function() {
        $(this).closest('li').find('.tooltips .description').hide();
      });

    return $('<div/>').append(html).html();
  };

  return this.each(function(i, div){
    var $div = $(div);

    $div.fileTree({
      // general display settings
      root: $div.data('directory'),
      openToFolder: settings.openToFolder,
      enableHighlight: settings.enableHighlight,
      // configuring the ajax call
      ajaxUrl: function(dir) {
        return urlPrefix + dir + '.json';
      },
      ajaxDataType: 'json',
      ajaxType: 'GET',
      ajaxData: function(dir) {
        return {};
      },
      formatResults: function(data) {
        return getHtmlResults(data['content_collection'], $div, $div.data('directory'));
      },

      // events and callbacks
      onFolderEvent: settings.onFolderEvent,
      onFileEvent:   settings.onFileEvent,
      afterShowTree: settings.afterShowTree,
      afterBindTree: settings.afterBindTree
    });
  });
};

})(jQuery);
