var setupColumnToggle = function(){
    var massageColWidths = function(){
        if ($('#columnTog :checked').length === 0) {
            $('.actions2').css({
                'width': '30%'
            });
        }
        else {
            $('.actions2').css('width', '18px');
        }
    };
    
    var setupColUI = function(data){
        // hides columns and sets the checkbox values
        $.each(data, function(key, value){
            if (value === false) {
                $('.' + key).hide();
                $('#' + key + 'Tog').find('input').attr('checked', false);
            }
            else {
                $('#' + key + 'Tog').find('input').attr('checked', true);
            }
            massageColWidths();
        });
    };
    
    var jsonify = function(str){
        var jsonObj = {};
        var keyVals = str.split('&');
        var keyValsLength = keyVals.length;
        for (var i = 0; i < keyValsLength; i++) {
            var thiskeyVal = keyVals[i].split('=');
            if (thiskeyVal[1] === 'true') {
                thiskeyVal[1] = true;
            }
            else {
                thiskeyVal[1] = false;
            }
            jsonObj[thiskeyVal[0]] = thiskeyVal[1];
        }
        //return jsonObj
        return JSON.stringify(jsonObj);
    };
    

    var writeDOMVal = function(name, val){
        if (window.localStorage) {
            sessionStorage.setItem([name], val);
        }
    };
    
    var readDOMVal = function(name){
        if (window.localStorage) {
            return sessionStorage.getItem([name]);
        }
    };
    
    var readDBVal = function(name){
        // name = resourcesColumn
        var ret_val = '';
        $.ajax({
            type: 'GET',
            url: '/direct/userPrefs/' + $('#userId').text() + '/' + name + '.json',
            cache: false,
            dataType: 'json'
        }).done(function(data){
            // this callback will use the data sent to write to the DOM;
            writeDOMVal('resourcesColumn', JSON.stringify(data));
            // and then show/hide cols and check/uncheck checkboxes
            setupColUI(data);
        }).fail(function(){
            // what here?
            // checkboxes will be all checked and columns will all show
        });
    };
    
    var writeDBVal = function(name, val){
        // use userPrefs call to preserve choices into db
        // name will be the setting type (i.e.'resourcesColumn')
        // val will be a prepared query string like 'access=true&creator=true&modified=true&size=true'
        
        jQuery.ajax({
            type: "PUT",
            url: '/direct/userPrefs/' + $('#userId').text() + '/' + name + '?' + val
        }).done(function(){
            // this callback will use the data sent to write to the DOM;
            writeDOMVal('resourcesColumn', jsonify(val));
        }).fail(function(){
            // write to the DOM
            // TODO: maybe message to user that 
            // setting will only last a session
            writeDOMVal('resourcesColumn', jsonify(val));
        });
    };
    
    //setting up on page load
    if (readDOMVal('resourcesColumn') === null) {
        // DOM storage null - ask the db and write to the DOM in the callback')
        // if the DB is null write to it with the known set with all key set to false
        // check all the checkboxes
    
        //TODO: activate: readDBVal('resourcesColumn');
    }
    else {
        // use DOM values
        var val = $.parseJSON(readDOMVal('resourcesColumn'));
        setupColUI(val);
    }
    $('#columnTog label').click(function(e){
        e.stopPropagation();
    });
    $('#columnTog input').click(function(e){
        var target = $(this).closest('span').attr('id').replace('Tog', '');
        e.stopPropagation();
        if ($(this).prop('checked') === true) {
            $('.' + target).show();
        }
        else {
            $('.' + target).hide();
        }
        massageColWidths();
    });
    
    $('#columnTog #saveCols').click(function(e){
        var str = '';
        $("#columnTog input").each(function(index){
            str = str + $(this).closest('span').attr('id').replace('Tog', '') + '=' + $(this).prop('checked');
            //  obj[$(this).closest('span').attr('id').replace('Tog', '')] = $(this).prop('checked');
            if (index !== 3) {
                str = str + '&';
            }
        });
        //store str in db
        //TODO: activate:  writeDBVal('resourcesColumn',str);
        // and store it locally
        writeDOMVal('resourcesColumn', jsonify(str));
    });
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
});

