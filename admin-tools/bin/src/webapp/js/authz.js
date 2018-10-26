$(document).ready(function(){
	// When the checkboxes change update the cell.
	$('input:checkbox').change(function(){
		$(this).parents('td').toggleClass('active', this.checked);
	}).change();
    $("table.checkGrid tr:even").addClass("evenrow");
    // Save the default selected
    $(':checked').parents('td').addClass('defaultSelected');
    
    $('.permissionDescription').hover(function(e){
        $(this).parents('tr').children('td').toggleClass('rowHover', e.type === "mouseenter");
    });
    
    $('th').hover(function(event){
    	var col = ($(this).prevAll().size());
        $('.' + col).add(this).toggleClass('rowHover', event.type === "mouseenter");
    });
    
    $('th#permission').hover(function(event){
        $('.checkGrid td.checkboxCell').toggleClass('rowHover', event.type === "mouseenter");
    });
    
    $('th#permission a').click(function(e){
        $('.checkGrid input').prop('checked', ($('.checkGrid :checked').length === 0)).change();
        e.preventDefault();
    });
    $('.permissionDescription a').click(function(e){
        var anyChecked = $(this).parents('tr').find('input:checked').not('[disabled]').length > 0;
        $(this).parents('tr').find('input:checkbox').not('[disabled]').prop('checked', !anyChecked).change();
        e.preventDefault();
    });
    $('th.role a').click(function(e){
        var col = ($(this).parent('th').prevAll().size());
        var anyChecked = $('.' + col + ' input:checked').not('[disabled]').length > 0;
        $('.' + col + ' input').not('[disabled]').prop('checked', !anyChecked).change();
        e.preventDefault();
    });
    
    $('#clearall').click(function(e){
        $("input").not('[disabled]').prop("checked", false).change();
        e.preventDefault();
    });
});
