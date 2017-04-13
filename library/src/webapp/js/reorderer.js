$(document).ready(function(){
    if ($("#reorder-list li").size() - 1 > 15) {
        $('.grabHandle').show();
        $('#inputFieldMessage').show();
		$('#inputKbdMessage').remove();
    }
    //get the initial order TODO - make an  array instead of putting the values in a span
    $('#reorder-list li').each(function(n){
        $('#lastMoveArrayInit').append($(this).attr('id') + ' ');
        $('#lastMoveArray').append($(this).attr('id') + ' ');
    });
    
    //allow user to click on a field to edit
    $('input[id^="index"]').click(function(event){
        event.stopPropagation();
    });
    //trap return key
    $('input[id^="index"]').bind("keypress", function(e){
        var code = e.charCode || e.keyCode;
        return (code == 13) ? false : true;
    });
    
    $('#undo-all').click(function(event){
        var initOrder;
        initOrder = $.trim($('#lastMoveArrayInit').text()).split(" ");
        for (z in initOrder) {
            thisRow = document.getElementById(initOrder[z]);
            $(thisRow).appendTo('#reorder-list');
        }
        
        event.preventDefault();
        registerChange();
        $('#undo-all').hide();
        $('#undo-all-inact').show();
        $('#undo-last-inact').show();
        $('#undo-last').hide();
    });
    $('#undo-last').click(function(event){
        var prevOrder;
        var lastMovedT;
        var lastMoved;
        prevOrder = $.trim($('#lastMoveArray').text()).split(" ");
        for (z in prevOrder) {
            thisRow = document.getElementById(prevOrder[z]);
            $(thisRow).appendTo('#reorder-list');
        }
        lastMovedT = $.trim($('#lastItemMoved').text());
        lastMoved = $('li:eq(' + lastMovedT.substr(20) + ')');
        $(lastMoved).addClass('recentMove');
        event.preventDefault();
        registerChange('notfluid', lastMoved);
        $('#undo-last-inact').fadeIn('slow');
        $('#undo-last').hide();
    });
    
    
    
    // handle changing the order text field
    $('input[id^="index"]').change(function(){
        // get existing order
        var that = this;
        preserveStatus();
        //what the value was (plucked from a hidden input)
        var oldVal = parseInt($(this).siblings('input[id^="holder"]').attr('value'));
        // the new value in the text field
        var newVal = parseInt(this.value);
        if (isNaN(newVal) || newVal > $('input[id^="index"]').size()) {
            var failedValidMessage = $('#failedValidMessage').text();
            $('#messageHolder').text(failedValidMessage.replace('#', $('input[id^="index"]').size()));
            $('.orderable-selected').removeClass('orderable-selected');
            $('#messageHolder').removeClass('messageSuccess');
            $('#messageHolder').addClass('messageValidation');
			var messagePos = $(that).position();
			$("#messageHolder").css({
				'position':'absolute',
				'height':'1.3em',
				'top':messagePos.top,
				'left':55
			});
            $('#messageHolder').fadeIn('slow');
            $("#messageHolder").animate({
                opacity: 1.0
            }, 2000, function(){
                $(that).val(oldVal);
                that.focus();
                that.select();
            });
            $("#messageHolder").fadeOut('slow');
            $(this).parents('li').addClass('orderable-selected');
            return (null);
        }
        
        var inputs = $('input[id^="index"]');
        // handle the things that happen after a move
        $('#undo-last').fadeIn('slow');
        $('#undo-last-inact').hide();
        $('#undo-all').fadeIn('slow');
        $('#undo-all-inact').hide();
        
        //insert the row in new location - if new value is 1, insert before, if it is the last possible
        // insert after, otherwise insert before or after depending on if it is going up or down
        if (newVal === '1') {
            $($(this).parents('li')).insertBefore($(this).parents('li').siblings('li').children('span').children('input[value=' + newVal + ']').parents('li'));
        }
        else 
            if (newVal == inputs.length) {
                $($(this).parents('li')).insertAfter($(this).parents('li').siblings('li').children('span').children('input[value=' + newVal + ']').parents('li'));
            }
            else {
                if (newVal > oldVal) {
                    $($(this).parents('li')).insertAfter($(this).parents('li').siblings('li').children('span').children('input[value=' + newVal + ']').parents('li'));
                }
                else {
                    $($(this).parents('li')).insertBefore($(this).parents('li').siblings('li').children('span').children('input[value=' + newVal + ']').parents('li'));
                }
            }
        registerChange('notfluid', $(this).parents('li'));
    });
    
    // the standard Fluid initialization
    var opts = {
        selectors: {
            movables: '[id^="listitem.orderable"]'
        },
        listeners: {
            onBeginMove: preserveStatus,
            afterMove: registerChange
        }
    };
    return fluid.reorderList("#reorder-list", opts);
});


// handle things that happen after a move
var registerChange = function(originEvent, movedEl){

    var rows = $("#reorder-list li").size();
    if (originEvent !== 'notfluid') {
        movedEl = $("li[aria-selected='true']");
    }
    
    $('#lastItemMoved').text($(movedEl).attr('id'));
    
    $(movedEl).addClass('recentMove');
    var newVal = 0;
    newVal = $((movedEl).prevAll('li').length + 1);
    // change the value of all the text fields (and value holders) to reflect new order
    var inputsX = $('input[id^="index"]');
    var holderinputs = $('input[id^="holder"]');
    var selectItems = $("select.selectSet");
    for (var i = 0; i < inputsX.length; i = i + 1) {
        inputsX[i].value = i + 1;
    }
    for (var x = 0; x < holderinputs.length; x = x + 1) {
        holderinputs[x].value = x + 1;
    }
    for (var y = 0; y < selectItems.length; y = y + 1) {
        selectItems[y].value = y + 1;
    }
    
    $('#undo-last').fadeIn('slow');
    $('#undo-last-inact').hide();
    $('#undo-all').fadeIn('slow');
    $('#undo-all-inact').hide();
    $(movedEl).animate({
        opacity: 1.0
    }, 2000, function(){
        $(movedEl).removeClass('recentMove');
    });
};



var preserveStatus = function(item){
    $('#lastMoveArray').text('');
    $('#reorder-list li').each(function(n){
        if ($(this).attr('id') !== undefined && $(this).attr('id') !== 'undefined_avatar') {
            $('#lastMoveArray').append($(this).attr('id') + ' ');
        }
    });
};


