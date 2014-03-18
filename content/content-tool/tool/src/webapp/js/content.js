var setupColumnToggle = function(){
    var cols = ['accessTog', 'creatorTog', 'modifiedTog', 'sizeTog']
    $.each(cols, function(i, val){
        var target = val.replace('Tog', '');
        if (readDOMVal(target)==='false') {
            $('.' + target).hide();
            $('#' + val).find('input').attr('checked',false);
        }
        else {
            $('#' + val).find('input').attr('checked',true);
        }
    });
    
    $('#columnTog input').click(function(e){
            var target = $(this).closest('span').attr('id').replace('Tog', '');
            
            if($(this).prop('checked') ===true){
                $('.' + target).show();
                writeDOMDBVal(target, 'true');
            }
            else {
                $('.' + target).hide();
                writeDOMDBVal(target, 'false');
            }
            
    });
}

var readDOMVal = function(name){
	var ret_val="";
	// get the property setting from DOM first
	if (window.localStorage) {
        ret_val=sessionStorage.getItem([name]);
    }
	
	if (ret_val==null)
	{
		// if the property val is not set in DOM yet, do ajax call to retrieve the value from database
	    $.ajax
	    (
	        {
	            type: "GET",
	            url: "/direct/userPrefs/key/" + $('#userId').text() + "/resourcesColumn.json",
	            async:false,
	            cache:false,
	            dataType: 'html',
	            success: function(data)
	            {
	            	var json = $.parseJSON(data);
	            	$(json).each(function(key,val){
	            		$.each(val,function(k,v){
		            		if (k === "data")
		            		{  
		            			$.each(v,function(kk,vv){
		            				// update DOM with values from database
		            				writeDOMVal(kk,vv);
		            				if (kk === name)
		            				{
		            					ret_val = vv;
		            				}
		            			});
		            		}
	            	    });
	            	});
	            },
	            error:function (xhr, textStatus, thrownError)
	            {
	            	
	            }
	        }
	    );
	}
	
	return ret_val;
    
};
var writeDOMVal = function(name, val){
	// write the (name, val) pair into DOM
    if (window.localStorage) {
        sessionStorage.setItem([name], val);
    }
};
var writeDOMDBVal = function(name, val){
	// write into DOM
    writeDOMVal(name,val);
    
    // use userPrefs call to preserve choices into db
	jQuery.ajax({
        type: "PUT",
        url: "/direct/userPrefs/" + $('#userId').text() + "/resourcesColumn?"+name+"=" + val
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
})

