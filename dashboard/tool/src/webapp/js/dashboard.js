/*
 stripe the tables, take out when can figure how to do in wicket
 */
var setupTableStriping = function(){
    $('table').each(function(){
        $(this).find('tr:even').addClass('even');
    });
};

/*
 eliminate whitespace only text nodes
 needed to style the wicket pager
 */
jQuery.fn.htmlClean = function(){
    this.contents().filter(function(){
        if (this.nodeType != 3) {
            $(this).htmlClean();
            return false;
        }
        else {
            return !/\S/.test(this.nodeValue);
        }
    }).remove();
};

/*
 add css classes to wicket pager based on block having a link or not
 */
jQuery.fn.cssInstrument = function(){
    $('.pager > span').each(function(i){
        if ($(this).find('a').length === 1) {
            $(this).addClass('linky');
        }
        else {
            $(this).addClass('nolinky');
        }
    });
};

var setupMenus = function(){
    $('tr').mouseenter(function(){
        $(this).find('.actionPanelTrig').show();
    });
    $('tr').mouseleave(function(){
        $(this).find('.actionPanelTrig').hide();
    });
    $('.actionPanelTrig').click(function(e){
        e.preventDefault();
        $(this).closest('tr').addClass('focusedRow')
        var pos = $(this).closest('td.action').position();
        var height = $(this).closest('td').height();
        $('.actionPanel').hide();
        $(this).parent('td').find('.actionPanel').css({
            'position': 'absolute',
            'left': pos.left,
            'top': pos.top +3
        }).toggle();
    });
    
    $('.actionPanel').mouseleave(function(){
        $(this).hide();
        $(this).closest('tr').removeClass('focusedRow')
    });
};

