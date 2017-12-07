var recalculate = function(){
    var keepList = '';
    var removeList = '';
    $('.col1 .layoutReorderer-module').each(function(i){
        i > 0 ? keepList = keepList + ' ' + $(this).find('.reorderSeq').text() : keepList = $(this).find('.reorderSeq').text();
    });
    $('.col2 .layoutReorderer-module').each(function(i){
        i > 0 ? removeList = removeList + ' ' + $(this).find('.reorderSeq').text() : removeList = $(this).find('.reorderSeq').text();
    });

    keepList=keepList + ' --- ';
    keepList=keepList.replace('  ',' ');
    removeList=removeList.replace('  ',' ');
    $('input[id=order]').val(keepList + removeList);

    if ($('.col2 .layoutReorderer-module').length===0){
        $('#deleteListHead').attr('class','deleteListMessageEmpty panel-heading');
    }
    else {
        $('#deleteListHead').attr('class','deleteListMessage panel-heading');
    }
    $('.layoutReorderer-module').find('.marker').closest('.layoutReorderer-module').remove();
};

$(document).ready(function(){
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

        $(".reorderItemsContainer").sortable('refresh');

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
        $(this).parentsUntil('.section-container').parent().find('a.deleteAnswerLink').click();
    });
});
