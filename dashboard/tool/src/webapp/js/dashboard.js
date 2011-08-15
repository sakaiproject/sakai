$(document).ready(function(){
    $('.schedItem, .newsItem').hover(function(){
        $(this).find('.actionPanelTrig').show();
    }, function(){
        $(this).find('.actionPanelTrig').hide();
        $('.actionPanel').hide();
    });
    $('.actionPanelTrig').click(function(e){
        e.preventDefault();
        $('.actionPanel').hide();
        $(this).parent('td').find('.actionPanel').css({
            'position': 'absolute',
            'right': '25px',
            'margin-top': '4px'
        }).toggle();
    });
    $('.actionPanel').mouseleave(function(){
        $(this).hide();
    });
    $('.actionPanel a').click(function(e){
        e.preventDefault();
        $(this).closest('.actionPanel').fadeOut('slow');
    });
    
    
});
