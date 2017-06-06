// User-Interface changes to elFinder 2.1 for Sakai 11
(function ($) {

var ui = $.sakai.elfinder.ui = function($elfinder) {
  // Footer
  // Adds OK and cancel buttons, so the current folder can be embeded
  this.footer = function() {
    var $footer = $('<div id="sakai-ui-footer"></div>').addClass(toClass(elements.confirmbar));
    var $OK = $('<div>OK</div>').addClass(toClass(elements.button, elements.ok));
    var $Cancel = $('<div>Cancel</div>').addClass(toClass(elements.button, elements.cancel));
    var $buttons = $('<div><div/>').append($OK, $Cancel).addClass(toClass(elements.buttons));

    return $footer.append($buttons);
  };

  // Bind the new UI elements
  $elfinder.append(this.footer());

  // Move breadcrumb trail from the footer to below the toolbar
  $elfinder.find(elements.statusbar).insertAfter('.elfinder-toolbar');

  // Move 'selected' and 'size' information from the statusbar to the footer
  $elfinder.find('.elfinder-stat-size, .elfinder-stat-selected').appendTo(elements.confirmbar);
};

var toClass = ui.toClass = function() {
  return $.makeArray(arguments).join(' ').replace(/\./g, '');
};

var elements = ui.elements = {};

/* add classes to buttons on dialog (file editing) */
ui.setSaveCloseButtons = function($dialog) {
  var $buttons = $dialog.find('.ui-button').addClass(toClass(elements.button));
  var $cancel = $($buttons[0]).addClass(toClass(elements.cancel));
  var $saveandclose = $($buttons[1]).addClass(toClass(elements.ok));
  var $save = $($buttons[2]).addClass(toClass(elements.ok));
  $saveandclose.insertAfter($save);
  $cancel.insertAfter($saveandclose);
};

elements.footer = '#sakai-ui-footer';
elements.confirmbar = 'elfinder-confirm-bar';
elements.buttons = '.buttons';
elements.button = '.button';
elements.ok = '.button-ok';
elements.cancel = '.button-cancel';
elements.statusbar = '.elfinder-statusbar';
elements.confirmbar = '.elfinder-confirm-bar';

})(jQuery);
