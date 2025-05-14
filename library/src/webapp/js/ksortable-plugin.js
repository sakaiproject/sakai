jQuery.fn.extend({
  ksortable(options) {
    // 1) Init jQuery UI sortable
    this.sortable(options);

    // 2) Ensure live region exists; create if missing
    let $live = $('#ksortable-live');
    if (!$live.length) {
      $live = $('<div id="ksortable-live" aria-live="polite" aria-atomic="true" class="visually-hidden"></div>');
      $('body').append($live);
    }

    // Enhance only the drag handle
    this.find(options.handle).attr({
      role: 'button',
      tabindex: 0,
      'aria-roledescription': 'draggable',
      'aria-pressed': 'false'
    }).off('keydown').on('keydown', function(e) {
      const $h       = $(this);
      const $item    = $h.closest(options.itemClass);
      const label    = $.trim($item.text());
      const pressed  = $h.attr('aria-pressed') === 'true';

      function announce(msg) {
        $live.text(msg);
      }

      // Space or Enter = toggle grab/drop
      if (e.key === ' ' || e.key === 'Enter') {
        e.preventDefault();
        $h.attr('aria-pressed', String(!pressed));
        announce(!pressed
          ? `${label} grabbed. Use arrow keys to move.`
          : `${label} dropped at position ${$item.index()+1}.`);
        return;
      }

      // movement only when grabbed
      if (!pressed) return;

      if (e.key === 'ArrowUp') {
        e.preventDefault();
        $item.insertBefore($item.prev());
      }
      else if (e.key === 'ArrowDown') {
        e.preventDefault();
        $item.insertAfter($item.next());
      }
      else if (e.key === 'Escape') {
        e.preventDefault();
        $h.attr('aria-pressed', 'false');
        announce('Reordering cancelled.');
        return;
      }
      else return;

      // announce move, fire stop callback, refocus handle
      announce(`${label} moved to position ${$item.index()+1}.`);
      options.stop?.({}, { item: $item[0] });
      $h.focus();
    });

    return this;
  }
});
