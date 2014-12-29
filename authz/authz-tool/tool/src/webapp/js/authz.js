$(document).ready(function(){
    $("table.checkGrid tr:even").addClass("evenrow");
    $(':checked').parents('td').addClass('active');
    $(':checked').parents('td').addClass('defaultSelected');
    $('input:checkbox').click(function(){
        if (this.checked) {
            $(this).parents('td').addClass('active');
        }
        else {
            $(this).parents('td').removeClass('active');
        }
    });
    
    $('.permissionDescription').mouseenter(function(){
        $(this).parents('tr').children('td').addClass('rowHover');
    }).mouseleave(function(){
        $(this).parents('tr').children('td').removeClass('rowHover');
    });
    
    $('th').mouseenter(function(){
        var col = ($(this).prevAll().size());
        $(this).addClass('rowHover');
        $('.' + col).addClass('rowHover');
    }).mouseleave(function(){
        var col = ($(this).prevAll().size());
        $(this).removeClass('rowHover');
        $('.' + col).removeClass('rowHover');
    });
    $('th#permission').mouseenter(function(){
        $('.checkGrid td.checkboxCell').addClass('rowHover');
    }).mouseleave(function(){
        $('.checkGrid td.checkboxCell').removeClass('rowHover');
    });
    
    $('th#permission a').click(function(e){
        if ($('.checkGrid :checked').length > 0) {
            $('.checkGrid input').attr('checked', '');
            $('.checkGrid td.checkboxCell').removeClass('active');
        }
        else {
            $('.checkGrid input').attr('checked', 'checked');
            $('.checkGrid td.checkboxCell').addClass('active');
            
        }
        e.preventDefault();
        
    });
    $('.permissionDescription a').click(function(e){
        if ($(this).parents('tr').children('td').children('label').children('input:checked').length > 0) {
            $(this).parents('tr').children('td').children('label').children('input').attr('checked', '');
            $(this).parents('tr').children('td').removeClass('active');
            
        }
        else {
            $(this).parents('tr').children('td').children('label').children('input').attr('checked', 'checked');
            $(this).parents('tr').children('td').addClass('active');
            $(this).removeClass('active');
        }
        e.preventDefault();
        
    });
    $('th.role a').click(function(e){
        var col = ($(this).parent('th').prevAll().size());
        if ($('.' + col + ' label input:checked').length > 0) {
            $('.' + col + ' label input').attr('checked', '');
            $('.' + col).removeClass('active');
        }
        else {
            $('.' + col + ' label input').attr('checked', 'checked');
            $('.' + col).addClass('active');
        }
        e.preventDefault();
        
    });
    
    $('#clearall').click(function(e){
        $("input").attr("checked", "");
        $('td').removeClass('active');
        e.preventDefault();
    });
    $('#restdef').click(function(e){
        $("input").attr("checked", "");
        $('td').removeClass('active');
        $(".defaultSelected").addClass("active");
        $(".defaultSelected input ").attr("checked", "checked");
        e.preventDefault();
    });
    
});
