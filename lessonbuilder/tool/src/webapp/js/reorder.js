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
    $(".reorderItemsContainer").sortable({
        start: function(event, ui) {
            recalculate();
        },
        stop: function(event, ui) {
            recalculate();
        },
        connectWith: ".reorderItemsContainer",
        placeholder: "ui-state-highlight",
        forcePlaceholderSize: true
    }).disableSelection(); 

    recalculate();

    $('#save').click(function(e){
        recalculate();
        return true;
    });

    $('.deleteAnswerTrashLink').click(function(e){
        e.preventDefault();
        $(this).closest('.layoutReorderer-module').addClass('highlightEl').hide().appendTo('#deleteListHead').fadeIn(200, function(){
            $(this).removeClass('highlightEl');
        });

        recalculate();
    });

    $('.deleteAnswerLink').click(function(e){
        e.preventDefault();
        $(this).closest('.layoutReorderer-module').addClass('highlightEl').hide().appendTo('#deleteListHead').fadeIn(200, function(){
            $(this).removeClass('highlightEl');
        });

        recalculate();
    });

    $('.deleteAllLink').on('click', function(e){
        e.preventDefault();
        $(this).parentsUntil('.section-container').parent().find('a.deleteAnswerTrashLink').click();
    });
});
