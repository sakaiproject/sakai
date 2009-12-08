setupPrefsGen = function(){
    if ($('.success').length) {
        $('.success').fadeOut(5000);
    }
    $('.formButton').click(function(e){
        $('.formButton').hide();
        $('.dummy').show();
    });
};

setupPrefsTabs = function(from, to){
    $('.blockable').click(function(e){
        if ($(this).attr('onclick')) {
            $('.blockable').attr('onclick', '');
            $('.blockable').addClass('blocked', '');
        }
    });
    fromSelLen = $('.' + from).children('option').length;
    toSelLen = $('.' + to).children('option').length;
    
    if (fromSelLen === 0) {
        $('.br').attr('onclick', '');
        $('.br').addClass('blocked');
    }
    if (toSelLen === 0) {
        $('.bl').attr('onclick', '');
        $('.bl').addClass('blocked');
        $('.ud').attr('onclick', '');
        $('.ud').addClass('blocked');
    }
    if (toSelLen === 1) {
        $('.ud').attr('onclick', '');
        $('.ud').addClass('blocked');
    }
	
};
