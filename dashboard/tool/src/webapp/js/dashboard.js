/*
 stripe the tables, take out when can figure how to do in wicket
 */
var setupTableStriping = function(){
    $('table').each(function(){
        $(this).find('tr:even').addClass('even');
    });
};

/*
 eliminate whitespace only text nodes
 needed to style the wicket pager
 */
jQuery.fn.htmlClean = function(){
    this.contents().filter(function(){
        if (this.nodeType != 3) {
            $(this).htmlClean();
            return false;
        }
        else {
            return !/\S/.test(this.nodeValue);
        }
    }).remove();
};

/*
 add css classes to wicket pager based on block having a link or not
 */
jQuery.fn.cssInstrument = function(){
    $('.pager > span').each(function(i){
        if ($(this).find('a').length === 1) {
            $(this).addClass('linky');
        }
        else {
            $(this).addClass('nolinky');
        }
    });
};

//toggle a fade
jQuery.fn.fadeToggle = function(speed, easing, callback){
    return this.animate({
        opacity: 'toggle'
    }, speed, easing, callback);
    
};

var setupMenus = function(){
    $('tr').mouseenter(function(){
        $(this).find('.actionPanelTrig').show();
    });
    $('tr').mouseleave(function(){
        $(this).find('.actionPanelTrig').hide();
    });
    $('.actionPanelTrig').click(function(e){
        e.preventDefault();
        $(this).closest('tr').addClass('focusedRow');
        var pos = $(this).closest('td.action').position();
        var height = $(this).closest('td').height();
        $('.actionPanel').hide();
        $(this).parent('td').find('.actionPanel').css({
            'position': 'absolute',
            'left': pos.left - 190,
            'top': pos.top + 3
        }).toggle();
    });
    
    $('.actionPanel').mouseleave(function(){
        $(this).hide();
        $(this).closest('tr').removeClass('focusedRow');
    });
};

var setupLinks = function(){
    $('.itemLink').click(function(e){
        e.preventDefault();
        var actionLink = "";
        var action = "";
        var link = $(this).attr('href');
        var title = $(this).text();
        var parentRow = $(this).closest('tr');
        var colCount = $(parentRow).find('td').length;
        var parentCell = $(this).closest('td');
        
        var itemType = $(this).closest('tr').find('.itemType').text();
        var entityReference = $(this).closest('tr').find('.entityReference').text();
        var callBackUrl = $(this).closest('body').find('.callBackUrl').text();
        
        //if disclosure in DOM, either hide or show, do not request data
        if ($(parentRow).next('tr.newRow').length === 1) {
            $(parentRow).next('tr.newRow').find('.results').fadeToggle('fast', '', function(){
                if ($(parentRow).next('tr.newRow').find('.results:visible').length === 0) {
                    $(parentCell).attr('class', 'tab');
                }
                else {
                    $(parentCell).attr('class', 'activeCell tab');
                }
            });
        }
        else {
            $(parentCell).attr('class', 'activeCell tab');
            params = {
                'entityType': itemType,
                'entityReference': entityReference
            };
            jQuery.ajax({
                url: callBackUrl,
                type: 'post',
                cache: false,
                data: JSON.stringify(params),
                contentType: 'application/json',
                dataType: 'json',
                success: function(json){
                    var results = '<div class=\"results\" style=\"display:none\">';
                    if (json.order.length !== 0) {
                    
                        $(json.order).each(function(i){
                            var o = this;
                            if (o.length > 1) {
                                results = results + '<p class=\"metadataLine\">'
                                for (i = 0; i < o.length; i++) {
                                    if (json[o[i].toString()]) {
                                        results = results + '<span>' + json[o[i].toString()] + ' </span>';
                                    }
                                }
                                results = results + '</p>';
                            }
                            else {
                                var w = o.toString()
                                if (w === 'description' && json[w]) {
                                    results = results + '<div class=\"description\">' + json[w] + '</div>';
                                }
                                if (w === 'attachments' && json[w]) {
                                    var atts = "";
                                    for (i = 0; i < json[w].length; i++) {
                                    
                                        atts = atts + '<li><a href=\"' + json[w][i]['attachment-url'] + '\">' + json[w][i]['attachment-title'] + '</a></li>';
                                    }
                                    results = results + '<ul class=\"attachList\">' + atts + '</ul>';
                                }
                                if (w === 'more-info' && json[w]) {
                                    var moreinfo = "";
                                    for (i = 0; i < json['more-info'].length; i++) {
                                        var target ="";
                                        var size ="";
                                        if (json['more-info'][i]['info_link-target']){
                                            target = 'target=\"' + json['more-info'][i]['info_link-target'] +'\"'
                                        }
                                        if (json['more-info'][i]['info_link-size']){
                                            size = ' (' + json['more-info'][i]['info_link-size'] +') '
                                        }

                                        moreinfo = moreinfo + '<a ' + target + ' href=\"' + json['more-info'][i]['info_link-url'] + '\">' + json['more-info'][i]['info_link-title'] + '<span class=\"size\">' + size + '</span></a>';
                                        ;
                                    }
                                    
                                    results = results + '<div class=\"moreInfo\">' + moreinfo + ' </div>';
                                }
                                
                            }
                             
                        });
                        results = results + '</div>'
                    }
                    else {
                        results = results + 'This item type has not specified an order :( </div>';
                    }
                    $('<tr class=\"newRow\"><td colspan=\"' + colCount + '\">' + results + '</td></tr>').insertAfter(parentRow);
                    $(parentRow).next('tr.newRow').find('.results').slideDown('slow', function(){
                        resizeFrame('grow');
                    });
                    
                },
                error: function(XMLHttpRequest, textStatus, errorThrown){
                    alert("error :" + XMLHttpRequest.responseText);
                }
            });
        }
        
    });
};

var setupLang = function(){
    langdata = eval('(' + $('#lang-holder').text() + ')');
};

var setupIcons = function(){
    $('.itemLink').each(function(i){
        if ($(this).closest('tr').find('.itemType').text() === 'resource') {
            $(this).addClass(getFileExtension($(this).attr('href')));
        }
    });
    function getFileExtension(filename){
        var ext = /^.+\.([^.]+)$/.exec(filename);
        return ext === null ? "" : ext[1].toLowerCase();
    }
};


/*
 resize the iframe based on the contained document height.
 used after DOM operations that add or substract to the doc height
 */
var resizeFrame = function(updown){
    var clientH;
    if (top.location !== self.location) {
        var frame = parent.document.getElementById(window.name);
    }
    if (frame) {
        if (updown === 'shrink') {
            clientH = document.body.clientHeight;
        }
        else {
            clientH = document.body.clientHeight + 50;
        }
        $(frame).height(clientH);
    }
    else {
        // throw( "resizeFrame did not get the frame (using name=" + window.name + ")" );
    }
};
