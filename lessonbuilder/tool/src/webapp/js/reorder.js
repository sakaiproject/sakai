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
        jQuery('#deleteListHead').attr('class','deleteListMessageEmpty panel-heading');
    }
    else {
        jQuery('#deleteListHead').attr('class','deleteListMessage panel-heading');
    }
    $('.layoutReorderer-module').find('.marker').closest('.layoutReorderer-module').remove();
};

$(document).ready(function(){
    $('.layoutReorderer-module').find('.marker').closest('.layoutReorderer-module').remove();
    recalculate();

    $('#save').click(function(e){
        recalculate();
        return true;
    });

    $('.deleteAnswerTrashLink').click(function(e){
        e.preventDefault();
        $(this).closest('.layoutReorderer-module').addClass('highlightEl').hide().appendTo('#reorderCol2').fadeIn(200, function(){
            $(this).removeClass('highlightEl');
        });

        recalculate();
    });

    $('.deleteAnswerLink').click(function(e){
        e.preventDefault();
        $(this).closest('.layoutReorderer-module').addClass('highlightEl').hide().appendTo('#reorderCol2').fadeIn(200, function(){
            $(this).removeClass('highlightEl');
        });

        recalculate();
    });

    $('.deleteAllLink').on('click', function(e){
        e.preventDefault();
        $(this).parentsUntil('.section-container').parent().find('a.deleteAnswerTrashLink').click();
    });
});
