var setupColumnToggle = function(){
    var cols = ['accessTog', 'creatorTog', 'modifiedTog', 'sizeTog', 'allTog']
    var colspan = 0;
    $.each(cols, function(i, val){
        var target = val.replace('Tog', '');
        if (readDOMVal(target) === 'true') {
            $('.' + target).hide();
            $('#' + val).attr('data-status', 'show');
            $('#' + val).attr('class', 'colShow');
        }
        else {
            $('#' + val).attr('data-status', 'hide');
            $('#' + val).attr('class', 'colHide');
            ++colspan
        }
    });
    if (colspan <= 1) {
        $('.colspan').hide();
    }
    else {
        $('.colspan').show();
    }
    
    $('#columnTog a').click(function(e){
        e.preventDefault(e);
        if ($(this).attr('id') === 'allTog') {
            if ($(this).attr('data-status') === 'show') {
                $(this).attr('data-status', 'hide');
                $(this).attr('class', 'colHide');
                $.each(cols, function(i, val){
                    var target = val.replace('Tog', '');
                    $('.' + target).show();
                    $('#' + val).attr('data-status', 'hide');
                    $('#' + val).attr('class', 'colHide');
                    writeDOMVal(target, 'false');
                });
                $('.colspan').show();
            }
            else {
                $(this).attr('data-status', 'show');
                $(this).attr('class', 'colShow');
                $.each(cols, function(i, val){
                    var target = val.replace('Tog', '');
                    $('.' + target).hide();
                    $('#' + val).attr('data-status', 'show');
                    $('#' + val).attr('class', 'colShow');
                    writeDOMVal(target, 'true');
                    $('.colspan').hide();
                });
            }
        }
        else {
        
            var target = $(this).attr('id').replace('Tog', '');
            if ($(this).attr('data-status') === 'show') {
                $(this).attr('data-status', 'hide');
                $(this).attr('class', 'colHide');
                $('.' + target).show();
                writeDOMVal(target, 'false');
                ++colspan
            }
            else {
                $(this).attr('data-status', 'show');
                $(this).attr('class', 'colShow');
                $('.' + target).hide();
                writeDOMVal(target, 'true');
                --colspan
            }
            if (colspan === 1) {
                $('.colspan').hide();
            }
            else {
                $('.colspan').show();
            }
        }
    });
}

var readDOMVal = function(name){
    if (window.localStorage) {
        return sessionStorage.getItem([name]);
    }
};
var writeDOMVal = function(name, val){
    if (window.localStorage) {
        sessionStorage.setItem([name], val);
    }
};
$(document).ready(function(){
    if ($('#content_print_result_url').length) {
        window.open($('#content_print_result_url').val(), $('#content_print_result_url_title'), "height=800,width=800");
    }
    jQuery('.portletBody').click(function(e){
    
        if (e.target.className != 'menuOpen' && e.target.className != 'dropdn') {
            $('.makeMenuChild').hide();
        }
        else {
            if (e.target.className == 'dropdn') {
                $('.makeMenuChild').hide();
                $(e.target).parent('li').find('ul').show().find('li:first a').focus();
                
            }
            else {
                $('.makeMenuChild').hide();
                $(e.target).find('ul').show().find('li:first a').focus();
            }
        }
    });
    $('.toggleDescription').click(function(e){
        e.preventDefault();
        $('.descPanel').css({
            'top': '-1000px',
            'left': '-1000px',
            'display': 'none'
        }).attr({
            'aria-hidden': 'true',
            'tabindex': '-1'
        });
        $(this).next('div').css({
            'top': e.pageY + 10,
            'left': e.pageX + 10,
            'cursor': 'pointer',
            'display': 'block'
        }).attr({
            'aria-hidden': 'false',
            'tabindex': '0'
        });
    });
    $('.descPanel').blur(function(){
        $(this).css({
            'top': '-1000px',
            'left': '-1000px',
            'display': 'none'
        }).attr({
            'aria-hidden': 'true',
            'tabindex': '-1'
        });
    });
    $('.descPanel').click(function(){
        $(this).css({
            'top': '-1000px',
            'left': '-1000px',
            'display': 'none'
        }).attr({
            'aria-hidden': 'true',
            'tabindex': '-1'
        });
    });
    setupColumnToggle();
})

