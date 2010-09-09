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

/* Converts implicit form control labeling to explicit by
 * adding an unique id to form controls if they don't already
 * have one and then setting the corresponding label element's
 * for attribute to form control's id value. This explicit 
 * linkage is better supported by adaptive technologies.
 * See SAK-18851.
 */
fixImplicitLabeling = function(){
    var idCounter = 0;
    $('label select,label input').each(function (idx, oInput) {
        if (!oInput.id) {
            idCounter++;
            $(oInput).attr('id', 'a11yAutoGenInputId' + idCounter.toString());
        }
        if (!$(oInput).parents('label').eq(0).attr('for')) {
            $(oInput).parents('label').eq(0).attr('for', $(oInput).attr('id'));
        }
    });
}

