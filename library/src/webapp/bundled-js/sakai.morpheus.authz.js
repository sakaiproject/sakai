(function ($) {

  $(function () {

    // When the checkboxes change update the cell.
    $('.permissions-table input:checkbox').change(function () {
      $(this).parents('td').toggleClass('active', this.checked);
    }).change();
    $(".permissions-table tr:even").addClass("evenrow");
    // Save the default selected
    $('.permissions-table :checked').parents('td').addClass('defaultSelected');

    $('.permissions-table .permissionDescription').hover(function (e) {
      $(this).parents('tr').children('td').toggleClass('rowHover', e.type === "mouseenter");
    });

    $('.permissions-table th').hover(function (event) {

      var col = ($(this).prevAll().size());
      $('.' + col).add(this).toggleClass('rowHover', event.type === "mouseenter");
    });

    $('.permissions-table th#permission').hover(function (event) {
      $('.permissions-table td.checkboxCell').toggleClass('rowHover', event.type === "mouseenter");
    });

    $('.permissions-table th#permission a').click(function (e) {

      $('.permissions-table .checkGrid input').prop('checked', ($('.checkGrid :checked').length === 0)).change();
      e.preventDefault();
    });
    $('.permissions-table .permissionDescription a').click(function (e) {

        var anyChecked = $(this).parents('tr').find('input:checked').not('[disabled]').length > 0;
        $(this).parents('tr').find('input:checkbox').not('[disabled]').prop('checked', !anyChecked).change();
        e.preventDefault();
    });
    $('.permissions-table th.role a').click(function (e) {

        var col = ($(this).parent('th').prevAll().size());
        var anyChecked = $('.permissions-table .' + col + ' input:checked').not('[disabled]').length > 0;
        $('.permissions-table .' + col + ' input').not('[disabled]').prop('checked', !anyChecked).change();
        e.preventDefault();
    });

    $('#clearall').click(function (e) {

        $(".permissions-table input").not('[disabled]').prop("checked", false).change();
        e.preventDefault();
    });
  });
}) ($PBJQ);
