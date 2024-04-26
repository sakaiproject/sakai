jQuery.fn.extend({
  ksortable: function (options) {

    this.sortable(options);

    $(options.handle, this).bind("keydown", function(event) {

      const $item = $(this.closest(options.itemClass));

      if (event.which === 83 || event.which == 37 || event.which === 69 || event.which == 38) { // s, left, e or up
        $item.insertBefore($item.prev());
      }

      if (event.which === 70 || event.which == 39 || event.which === 68 || event.which == 40) { // f, right, d or down
        $item.insertAfter($item.next());
      }

      options.stop({}, { item: $item[0] });

      $(this).focus();
    });
  }
});
