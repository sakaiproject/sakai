(function($, fluid){
    //fluid.setLogging(true);
    initlayoutReorderer = function(){
        fluid.reorderLayout("#layoutReorderer", {
            listeners: {
                afterMove: function(args){
                    recalculate();
                }
            },
            styles: {
                defaultStyle: "layoutReorderer-movable-default",
                selected: "layoutReorderer-movable-selected",
                dragging: "layoutReorderer-movable-dragging",
                mouseDrag: "layoutReorderer-movable-mousedrag",
                dropMarker: "layoutReorderer-dropMarker",
                avatar: "layoutReorderer-avatar"
            },
            disableWrap: true,
            containerRole: fluid.reorderer.roles.LIST
        });
    };
})(jQuery, fluid);

var recalculate = function(){
    var keepList = '';
    var removeList = '';
    jQuery('.col1 .layoutReorderer-module').each(function(i){
        i > 0 ? keepList = keepList + ' ' + $(this).find('.reorderSeq').text() : keepList = $(this).find('.reorderSeq').text();
    });
    jQuery('.col2 .layoutReorderer-module').each(function(i){
        i > 0 ? removeList = removeList + ' ' + $(this).find('.reorderSeq').text() : removeList = $(this).find('.reorderSeq').text();
    });
    
    keepList=keepList + ' --- ';
    keepList=keepList.replace('  ',' ');
    removeList=removeList.replace('  ',' ');
    jQuery('input[id=order]').val(keepList + removeList);
    
    if (jQuery('.col2 .layoutReorderer-module').length===0){
        jQuery('.col2 #deleteListHead').attr('class','deleteListMessageEmpty');
    }
    else {
        jQuery('.col2 #deleteListHead').attr('class','deleteListMessage');
    }
    $('.layoutReorderer-module').find('.marker').closest('.layoutReorderer-module').remove();
};
$(document).ready(function(){
    $('.layoutReorderer-module').find('.marker').closest('.layoutReorderer-module').remove();
    recalculate();

/*
    jQuery('.col1 .layoutReorderer-module').each(function(i){
        i > 0 ? ids = ids + ' ' + $(this).find('.reorderSeq').text() : ids = $(this).find('.reorderSeq').text();
    });
    
    ids=ids + ' --- '
    ids=ids.replace('  ',' ')
    jQuery('input[id=order]').val(ids);

*/    
    $('#save').click(function(e){
	    recalculate();
	    return true;
	});

    $('.deleteAnswerLink').click(function(e){
        e.preventDefault();
        $(this).closest('.layoutReorderer-module').addClass('highlightEl').hide().appendTo('#reorderCol2').fadeIn(2000, function(){
            $(this).removeClass('highlightEl');
        });
        
        recalculate();
    });
});
