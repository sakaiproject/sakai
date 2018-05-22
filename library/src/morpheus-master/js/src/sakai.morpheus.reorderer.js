$PBJQ(document).ready(function(){
    if ($PBJQ("#reorder-list li").size() - 1 > 15) {
        $PBJQ('.grabHandle').show();
        $PBJQ('#inputFieldMessage').show();
        $PBJQ('#inputKbdMessage').remove();
    }
    //get the initial order TODO - make an  array instead of putting the values in a span
    $PBJQ('#reorder-list li').each(function(n){
        $PBJQ('#lastMoveArrayInit').append($PBJQ(this).attr('id') + ' ');
        $PBJQ('#lastMoveArray').append($PBJQ(this).attr('id') + ' ');
    });
    
    //allow user to click on a field to edit
    $PBJQ('input[id^="index"]').click(function(event){
        event.stopPropagation();
    });
    //trap return key
    $PBJQ('input[id^="index"]').bind("keypress", function(e){
        var code = e.charCode || e.keyCode;
        return (code == 13) ? false : true;
    });
    
    $PBJQ('#undo-all').click(function(event){
        var initOrder;
        initOrder = $PBJQ.trim($PBJQ('#lastMoveArrayInit').text()).split(" ");
        for (z in initOrder) {
            thisRow = document.getElementById(initOrder[z]);
            $PBJQ(thisRow).appendTo('#reorder-list');
        }
        
        event.preventDefault();
        registerChange();
        $PBJQ('#undo-all').hide();
        $PBJQ('#undo-all-inact').show();
        $PBJQ('#undo-last-inact').show();
        $PBJQ('#undo-last').hide();
    });
    $PBJQ('#undo-last').click(function(event){
        var prevOrder;
        var lastMovedT;
        var lastMoved;
        prevOrder = $PBJQ.trim($PBJQ('#lastMoveArray').text()).split(" ");
        for (z in prevOrder) {
            thisRow = document.getElementById(prevOrder[z]);
            $PBJQ(thisRow).appendTo('#reorder-list');
        }
        lastMovedT = $PBJQ.trim($PBJQ('#lastItemMoved').text());
        lastMoved = $PBJQ('li:eq(' + lastMovedT.substr(20) + ')');
        $PBJQ(lastMoved).addClass('recentMove');
        event.preventDefault();
        registerChange('notfluid', lastMoved);
        $PBJQ('#undo-last-inact').fadeIn('slow');
        $PBJQ('#undo-last').hide();
    });
    
    
    
    // handle changing the order text field
    $PBJQ('input[id^="index"]').change(function(){
        // get existing order
        var that = this;
        preserveStatus();
        //what the value was (plucked from a hidden input)
        var oldVal = parseInt($PBJQ(this).siblings('input[id^="holder"]').attr('value'));
        // the new value in the text field
        var newVal = parseInt(this.value);
        if (isNaN(newVal) || newVal > $PBJQ('input[id^="index"]').size()) {
            var failedValidMessage = $PBJQ('#failedValidMessage').text();
            $PBJQ('#messageHolder').text(failedValidMessage.replace('#', $PBJQ('input[id^="index"]').size()));
            $PBJQ('.orderable-selected').removeClass('orderable-selected');
            $PBJQ('#messageHolder').removeClass('messageSuccess');
            $PBJQ('#messageHolder').addClass('messageValidation');
			var messagePos = $PBJQ(that).position();
			$PBJQ("#messageHolder").css({
				'position':'absolute',
				'height':'1.3em',
				'top':messagePos.top,
				'left':55
			});
            $PBJQ('#messageHolder').fadeIn('slow');
            $PBJQ("#messageHolder").animate({
                opacity: 1.0
            }, 2000, function(){
                $PBJQ(that).val(oldVal);
                that.focus();
                that.select();
            });
            $PBJQ("#messageHolder").fadeOut('slow');
            $PBJQ(this).parents('li').addClass('orderable-selected');
            return (null);
        }
        
        var inputs = $PBJQ('input[id^="index"]');
        // handle the things that happen after a move
        $PBJQ('#undo-last').fadeIn('slow');
        $PBJQ('#undo-last-inact').hide();
        $PBJQ('#undo-all').fadeIn('slow');
        $PBJQ('#undo-all-inact').hide();
        
        //insert the row in new location - if new value is 1, insert before, if it is the last possible
        // insert after, otherwise insert before or after depending on if it is going up or down
        if (newVal === '1') {
            $PBJQ($PBJQ(this).parents('li')).insertBefore($PBJQ(this).parents('li').siblings('li').children('span').children('input[value=' + newVal + ']').parents('li'));
        }
        else 
            if (newVal == inputs.length) {
                $PBJQ($PBJQ(this).parents('li')).insertAfter($PBJQ(this).parents('li').siblings('li').children('span').children('input[value=' + newVal + ']').parents('li'));
            }
            else {
                if (newVal > oldVal) {
                    $PBJQ($PBJQ(this).parents('li')).insertAfter($PBJQ(this).parents('li').siblings('li').children('span').children('input[value=' + newVal + ']').parents('li'));
                }
                else {
                    $PBJQ($PBJQ(this).parents('li')).insertBefore($PBJQ(this).parents('li').siblings('li').children('span').children('input[value=' + newVal + ']').parents('li'));
                }
            }
        registerChange('notfluid', $PBJQ(this).parents('li'));
    });

    // the jquery-ui sortable initialization
    return $PBJQ("#reorder-list").keyboardSortable({
      items: 'li:not(.notsortable)',
      start: function( event, ui ) {
        preserveStatus(ui);
      },
      update: function( event, ui ) {
        registerChange(event, ui);
      },
    });
});


// handle things that happen after a move
var registerChange = function(originEvent, movedEl){

    var rows = $PBJQ("#reorder-list li").size();
    if (originEvent !== 'notfluid') {
        movedEl = $PBJQ("li[aria-selected='true']");
    }
    
    $PBJQ('#lastItemMoved').text($PBJQ(movedEl).attr('id'));
    
    $PBJQ(movedEl).addClass('recentMove');
    var newVal = 0;
    newVal = $PBJQ((movedEl).prevAll('li').length + 1);
    // change the value of all the text fields (and value holders) to reflect new order
    var inputsX = $PBJQ('input[id^="index"]');
    var holderinputs = $PBJQ('input[id^="holder"]');
    var selectItems = $PBJQ("select.selectSet");
    for (var i = 0; i < inputsX.length; i = i + 1) {
        inputsX[i].value = i + 1;
    }
    for (var x = 0; x < holderinputs.length; x = x + 1) {
        holderinputs[x].value = x + 1;
    }
    for (var y = 0; y < selectItems.length; y = y + 1) {
        selectItems[y].value = y + 1;
    }
    
    $PBJQ('#undo-last').fadeIn('slow');
    $PBJQ('#undo-last-inact').hide();
    $PBJQ('#undo-all').fadeIn('slow');
    $PBJQ('#undo-all-inact').hide();
    $PBJQ(movedEl).animate({
        opacity: 1.0
    }, 2000, function(){
        $PBJQ(movedEl).removeClass('recentMove');
    });
};



var preserveStatus = function(item){
    $PBJQ('#lastMoveArray').text('');
    $PBJQ('#reorder-list li').each(function(n){
        if ($PBJQ(this).attr('id') !== undefined && $PBJQ(this).attr('id') !== 'undefined_avatar') {
            $PBJQ('#lastMoveArray').append($PBJQ(this).attr('id') + ' ');
        }
    });
};


