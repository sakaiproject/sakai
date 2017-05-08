/*
 * HANDLERS
 */
var isMobile = "";

if ($('.mobilePage').length === 0) {
    isMobile = false;
}
else {
    isMobile = true;
}
var setupLinks = function(){
    $('.tabnav a').click(function(){
        if ($(this).attr('href') !=='#'){
             $(this).closest('div[data-role="page"]').find('.nav-tabs li:nth-child(1)').find('a').trigger('click');
        }
    })


    $('#normalEvents').click(function(){
        $('#events').find('.nav-tabs li:nth-child(1)').find('a').trigger('click');
    })
    $('#normalActiv').click(function(){
        $('#activ').find('.nav-tabs li:nth-child(1)').find('a').trigger('click');
    })

    
    $('#starredEvents').click(function(){
        $('#events').find('.nav-tabs li:nth-child(2)').find('a').trigger('click')
    })
    $('#starredActiv').click(function(){
        $('#activ').find('.nav-tabs li:nth-child(2)').find('a').trigger('click')
    })

    
    /*
     need to make the whole row clickable and send a trigger to the link
     but since it is a live action cannot escape propagation
     $('.row-fluid').click(function(){});
     */
  
    /*
     * go to site link handler (used only to report event)
     */
    $(".siteLink").on("click", function(e){
        // DO NOT CALL:  e.preventDefault();
        if (!isMobile) {
            var itemType = $(this).closest('li').find('.itemType').text();
            var entityReference = $(this).closest('li').find('.entityReference').text();
            reportEvent(e.target, entityReference, itemType, "dash.follow.site.link");
        }
    });
    /*
     * expand an item's contents
     */
    $(".itemLink").on("click", function(e){

        var parentRow = $(this).closest('li');
        var colCount = $(parentRow).find('div').length;
        var parentCell = $(this).closest('div');
        var itemType = $(this).closest('li').find('.itemType').text();
        var entityReference = $(this).closest('li').find('.entityReference').text();
        var itemCount = $(this).closest('li').find('.itemCount').text();
        var callBackUrl = $(this).closest('body').find('.callBackUrl').text();
        
        
        //if disclosure in DOM, either hide or show, do not request data again
        if ($(parentRow).next('li.newRow').length === 1) {
            $(parentRow).next('li.newRow').find('.results').fadeToggle('fast', '', function(){
                $(parentCell).toggleClass('activeCell');
                $(parentRow).next('li.newRow').toggle();
            });
        }
        else {
            $(parentCell).attr('class', 'activeCell tab span6');
            params = {
                'entityType': itemType,
                'entityReference': entityReference,
                'itemCount': itemCount
            };
            // a single item we are retrieving data for    
            if (itemCount == 1) {
                jQuery.ajax({
                    url: callBackUrl,
                    type: 'post',
                    cache: false,
                    data: JSON.stringify(params),
                    contentType: 'application/json',
                    dataType: 'json',
                    success: function(json){
                        var delimitLeft = "{";
                        var delimitRight = "}";
                        
                        var resultsDiv = '<div class=\"results\" tabindex=\"-1\" style=\"display:none\">';
                        var title = '';
                        var results = '';
                        if (json.order.length !== 0) {
                            $(json.order).each(function(i){
                                var o = this;
                                var w = o.toString();
                                
                                if (get_type(json[w]) === "String") {
                                    // a string
                                    if (json[w].split(delimitLeft).length - 1 > 0) {
                                        // a string that has substitions, replace them
                                        var endString = json[w];
                                        var arr = json[w].split(delimitRight);
                                        for (i = 0; i < arr.length; i++) {
                                            var arr2 = arr[i].split(delimitLeft);
                                            if (arr2[1]) {
                                                endString = endString.replace(delimitLeft + arr2[1] + delimitRight, json[arr2[1]]);
                                            }
                                        }
                                        // should do a check here, to make sure that all the keys had a value
                                        // increase a counter for each successful arr2[1] and compare in the end with
                                        // the length of json[w].split('{').length - 1
                                        results = results + '<div class="metadataLine">' + endString + '</div>';
                                    }
                                    else 
                                        if (json[w + '-label']) {
                                            // a string with a label counterpart
                                            title = '<h5>' + json[w + '-label'] + '</h5><div class="block muted">' + json[w] + '</div>';
                                            
                                        }
                                        else {
                                            if (w === 'title') {
                                                // a title string
                                                title = '<h4>' + json[w] + '</h4>';
                                            }
                                            else {
                                                //all other strings
                                                results = results + '<div class="block muted">' + json[w] + '</div>';
                                            }
                                        }
                                    
                                }
                                else {
                                    // is an object, treat special
                                    if (w === 'attachments' && json[w]) {
                                        var atts = "";
                                        for (i = 0; i < json[w].length; i++) {
                                            atts = atts + '<li><a target=\"_blank\" href=\"' + json[w][i]['attachment-url'] + '\" onClick=\"reportEvent(this,\'/dashboard/link' + entityReference + '\',\'' + itemType + '\',\'dash.view.attachment\');\">' + json[w][i]['attachment-title'] + '</a></li>';
                                            
                                        }
                                        results = results + '<ul class=\"attachList\">' + atts + '</ul>';
                                    }
                                    if (w === 'more-info' && json[w]) {
                                        var moreinfo = "";
                                        for (i = 0; i < json['more-info'].length; i++) {
                                            var target = "";
                                            var size = "";
                                            var dashEvent = "dash.access.url";
                                            if (json['more-info'][i]['info_link-target']) {
                                                target = 'target=\"' + json['more-info'][i]['info_link-target'] + '\"';
                                                if (json['more-info'][i]['info_link-target'] === '_top') {
                                                    dashEvent = "dash.follow.tool.link";
                                                }
                                            }
                                            if (json['more-info'][i]['info_link-size']) {
                                                size = ' (' + json['more-info'][i]['info_link-size'] + ') ';
                                            }
                                            moreinfo = moreinfo + '<li><a class=\"btn btn-small\"' + target + ' href=\"' + json['more-info'][i]['info_link-url'] + '\" onClick=\"reportEvent(this,\'/dashboard/link' + entityReference + '\',\'' + itemType + '\',\'' + dashEvent + '\');\">' + json['more-info'][i]['info_link-title'] + '<span class=\"size\">' + size + '</span></a></li>';
                                            
                                        }
                                        results = '<ul class=\"moreInfo\">' + moreinfo + ' </ul>'  + '<div class="clearfix"></div>' + results;
                                    }
                                }
                                
                            });
                            results = resultsDiv + title + results + '</div>';
                        }
                        else {
                            results = resultsDiv + title + results + 'This item type has not specified an order :( </div>';
                        }
                        
                        if (isMobile) {
                            if ($(parentRow).closest('ul.itemCollection').length) {
                                $('<li class=\"newRow\"><div>' + results + '</div></li>').insertAfter(parentRow);
                                $(parentRow).next('li.newRow').find('.results').fadeIn('fast');
                                resizeFrame('grow');
                            }
                            else {
                                $('#itemEvent #itemHolder').html('<div>' + results + '</div>');
                                $('#itemEvent #itemHolder .results').fadeIn('fast');
                            }
                        }
                        else {
                        
                            $('<li class=\"newRow\"><div>' + results + '</div></li>').insertAfter(parentRow);
                            $(parentRow).next('li.newRow').find('.results').fadeIn('slow', function(){
                                resizeFrame('grow');
                                //$(parentRow).next('li.newRow').find('.results').focus();
                            });
                            
                        }
                    },
                    error: function(xhr, status, error){
                        reportError(error)
                    }
                });
            }
            else {
                var initChunk = true;
                renderCollection(callBackUrl, params, parentRow, colCount, initChunk);
            }
        }
    });
    $('.getMore a').on("click", function(){
        var parentRow = '';
        var isMobile = "";
        if ($('.mobilePage').length === 0) {
            //console.log('is mobile')
            parentRow = $(this).closest('li').prev('li');
            isMobile = false;
        }
        else {
            parentRow = $(this).closest('.newList').find('#paramContainer');
            isMobile = true;
        };
        
        var colCount = '0';
        var callBackUrl = $(this).closest('body').find('.callBackUrl').text();
        var paramContainer = $(parentRow).find('.one');
        $(paramContainer).css('border', '1px solid blue')
        
        params = {
            'entityType': $(paramContainer).find('.itemType').text(),
            'entityReference': $(paramContainer).find('.entityReference').text(),
            'itemCount': $(paramContainer).find('.itemCount').text(),
            'offset': $(parentRow).next('li.newRow').find('li').length
        };
        var initChunk = false;
        renderCollection(callBackUrl, params, parentRow, colCount, initChunk);
    });
};

var renderCollection = function(callBackUrl, params, parentRow, colCount, initChunk){
    jQuery.ajax({
        url: callBackUrl,
        type: 'post',
        cache: false,
        data: JSON.stringify(params),
        contentType: 'application/json',
        dataType: 'json',
        success: function(json){
            var items = json.items;
            var totalCount = json.totalCount;
            var count = json.count;
            var offset = json.offset;
            var moreLabel = json['more-label'];
            var results = '';
            $(items).each(function(i){
                var icon = "";
                var starIcon = "#";
                var starAction = "starThis";
                var hideIcon = "#";
                var hideAction = "hideThis";
                var hideLink = "";
                if (this.iconUrl) {
                    icon = '<img class=\"resIcon\" src=\"' + this.iconUrl + '\"/> ';
                }
                else {
                    icon = '';
                }
                if (this.hidden) {
                    hideAction = "showThis";
                }
                else {
                    hideAction = "hideThis";
                }
                if (this.sticky) {
                    starAction = "unstarThis";
                    hideLink = '<a style=\"display:none\" class="' + hideAction + '" href="#"><img alt="[ Hide/Show This ]" src="' + this.hidingActionIcon + '" /></a>';
                }
                else {
                    starAction = "starThis";
                    hideLink = '<a class="' + hideAction + '" href="#"><img alt="[ Hide/Show This ]" src="' + this.hidingActionIcon + '" /></a>';
                }
                
                var link = '';
                row = '<div class="one span2" style=\"visibility:hidden;\">\n<span class="itemType" style="display:none;">' + this.entityType + '</span>\n' +
                '<span class="actionTargetId" style="display:none;">' +
                this.newsItemId +
                '</span>\n' +
                '<span class="itemCount" style="display:none;">1</span>\n' +
                '<span class="entityReference" style="display:none;">' +
                this.entityReference +
                '</span>\n' +
                '</div>\n' +
                '<div class="tab three span6">\n' +
                '<a href="#" class="itemLink">' +
                icon +
                ' ' +
                this.title +
                ' <span class="skip">' +
                json.details +
                '</span></a>' +
                '<span class="itemLabel muted">' +
                this.label +
                '</span>\n' +
                '</div>\n' +
                '<div class="action span2">\n &nbsp;&nbsp;<a class="' +
                starAction +
                '" href="#"><img alt="[ Star/Unstar This ]" src="' +
                this.starringActionIcon +
                '" /></a> ' +
                hideLink +
                '\n' +
                '</div>\n';
                results = results + '<li class=\"row-fluid ' + this.entityType + ' row' + i % 2 + '\">' + row + '</li>';
            });
            
            //need to just add rows if it is a "get More" action, otherwise add below
            var isMobile = "";
            if ($('.mobilePage').length === 0) {
                //console.log('is not mobile') //desktop version
                isMobile = false;
            }
            else {
                //console.log('is mobile')
                isMobile = true;
            };
            
            
            if (isMobile) {
                if (initChunk) {
                    $('#itemEvent #itemHolder').html('<div class=\"newList\"><div id=\"paramContainer\" style=\"display:none\"><div class=\"one\"><span class=\"itemType\">' + $(parentRow).find('.itemType').text() +
                    
                    '</span>  | <span class=\"itemCount\">' +
                    $(parentRow).find('.itemCount').text() +
                    '</span>  | <span class=\"entityReference\">' +
                    $(parentRow).find('.entityReference').text() +
                    '</span>  | <span class=\"offset\">' +
                    $(parentRow).find('.offset').text() +
                    '</span></div></div><ul class=\"itemCollection\">' +
                    results +
                    '</ul></div>');
                }
                else {
                    $(results).appendTo('.itemCollection')
                }
            }
            else {
                if (initChunk) {
                    $('<li class=\"newRow\"><div class=\"results newList\"><ul class=\"itemCollection\">' + results + '</ul></div></li>').insertAfter(parentRow);
                }
                else {
                    $(results).insertAfter((parentRow).next('li').find('ul.itemCollection').find('li:last')).fadeIn('slow');
                    //$(parentRow).next('li').find('ul').find('li').eq(params.offset).attr('tabindex', '-1').focus();
                }
            }
            //this needs to be conditional on if being a "get More" action
            var showingRows = '';
            if (isMobile) {
                showingRows = $('#itemHolder').find('.itemCollection').find('li').length
            }
            else {
                showingRows = $(parentRow).next('li.newRow').find('li').length
            }
            
            if (showingRows < totalCount) {
                if (isMobile) {
                    if ($('#itemHolder').find('.getMore').length === 0) {
                        $('<div class="getMore"><a class=\"btn btn-small\" href="#">' + json['more-link'] + '</a>&nbsp;&nbsp;&nbsp;<span class=\"showingCount instruction textPanelFooter"></span></div>').insertAfter($('.itemCollection'));
                    }
                    $('#itemHolder').find('.showingCount').text(updateCount(json['more-status-last'], showingRows, totalCount, parentRow))
                }
                else {
                    if ($(parentRow).next('li.newRow').find('.getMore').length === 0) {
                        $('<div class="getMore"><a class=\"btn btn-small\" href="#">' + json['more-link'] + '</a>&nbsp;&nbsp;&nbsp;<span class=\"showingCount instruction textPanelFooter"></span></div>').insertAfter((parentRow).next('li.newRow').find('.itemCollection'));
                    }
                    $(parentRow).next('li.newRow').find('.getMore').find('.showingCount').text(updateCount(json['more-status-last'], showingRows, totalCount, parentRow))
                }
                
            }
            else {
                $(parentRow).next('li.newRow').find('.getMore').fadeOut('slow').remove();
                $('#itemHolder').find('.getMore').fadeOut('slow').remove();
            }
            
            // add click handlers to star and hide links
            $('div.newList .starThis').on('click', function(e){
                var targetItemId = $(e.target).closest('li').find('.actionTargetId').text();
                updateItemStatus(e.target, 'star', targetItemId);
            });
            $('div.newList .unstarThis').on('click', function(e){
                var targetItemId = $(e.target).closest('li').find('.actionTargetId').text();
                updateItemStatus(e.target, 'unstar', targetItemId);
            });
            $('div.newList .hideThis').on('click', function(e){
                var targetItemId = $(e.target).closest('li').find('.actionTargetId').text();
                updateItemStatus(e.target, 'hide', targetItemId);
            });
            $('div.newList .showThis').on('click', function(e){
                var targetItemId = $(e.target).closest('li').find('.actionTargetId').text();
                updateItemStatus(e.target, 'show', targetItemId);
            });
            
            $(parentRow).next('li.newRow').find('.results').fadeIn('slow', function(){
                resizeFrame('grow');
                if (initChunk) {
                    $(parentRow).next('li.newRow').find('.results').focus();
                }
            });
        },
        error: function(XMLHttpRequest, textStatus, errorThrown){
            reportError()
        }
    });
};
function updateItemStatus(element, dashAction, itemId){
    var dashActionHandler = $(element).closest('body').find('.dashActionHandler').text();
    var params = {
        'dashAction': dashAction,
        'itemId': itemId
    };
    jQuery.ajax({
        url: dashActionHandler,
        type: 'post',
        cache: false,
        data: JSON.stringify(params),
        contentType: 'application/json',
        dataType: 'json',
        success: function(json){
            if (dashAction === 'hide' || dashAction === 'unHide') {
                $(element).closest('tr').fadeOut('slow');
                reportSuccess(json.message);
            }
            else {
                $(element).attr('src', json.newIcon);
                if (dashAction === 'unstar') {
                    $(element).parent('a').attr('class', 'starThis');
                    if (!isMobile) {
                        $(element).closest('li').find('.hideThis').show();
                    }
                }
                else {
                    $(element).closest('li').find('.hideThis').hide();
                    $(element).parent('a').attr('class', 'unstarThis');
                }
            }
        },
        error: function(XMLHttpRequest, textStatus, errorThrown){
            reportError();
        }
    });
}

function reportEvent(element, entityRef, entityType, dashEvent){
    var callBackUrl = $(element).closest('body').find('.callBackUrl').text();
    var params = {
        'entityType': entityType,
        'entityReference': entityRef,
        'dashEvent': dashEvent,
        'itemCount': '0'
    };
    jQuery.ajax({
        url: callBackUrl,
        type: 'post',
        cache: false,
        data: JSON.stringify(params),
        contentType: 'application/json',
        dataType: 'json',
        success: function(json){
        }
    });
    
}


var updateCount = function(label, showingRows, totalCount, rowId){
    var repLabel = label.replace('{0}', showingRows).replace('{1}', totalCount);
    return (repLabel)
    // $(parentRow).next('tr.newRow').find('.getMore').find('.showingCount').text(showingRows);
}



/*
 * used in the json returned by actions that
 * need to be notified to the user (unstarring a starred
 * item in the starred view, hiding items in main, unhiding in hidden
 * ie. any time an item dissapears from the curerent view)
 */
var reportSuccess = function(msg, item, url){
    $('#messagePanel').html(msg).fadeTo("slow", 1).animate({
        opacity: 1.0
    }, 5000).fadeTo(3000, 0);
};


/*
 * There has been an error
 *
 *
 */
var reportError = function(){
    var genericErrorMsg = $('#genericErrorMessage').text();
    $('#messageError').html(genericErrorMsg).fadeTo("slow", 1).animate({
        opacity: 1.0
    }, 5000).fadeTo(3000, 0);
};


/*
 * MOTD messages can be dismissed. If so a cookie gets set so that
 * they are not displayed again in the session
 * TODO: ability to dismiss more that one - so need to track their id in the
 * cookie
 * TODO: ability to just collapse/expand them them and show them in that state for the
 * session
 * See: DASH-152
 */
var setupDismissMOTD = function(){
    var hiddenMOTD = utils_readCookie('motdHide');
    var currentMOTD = $('.motdPanel .motdId').text();
    if (hiddenMOTD && currentMOTD && hiddenMOTD === currentMOTD) {
        $('.motdPanel').css('display', 'none');
    }
    else {
        if ($('mobilePage').length !== 0) {
            $('.motdPanel').css('display', 'block');
        }
    }
    $('#motdTextDivDismiss').click(function(){
        dismissMessage('.motdPanel', currentMOTD);
    });
};

/*
 * hide MOTD and set a cookie
 * TODO: ability to dismiss more that one - so need to track their id in the
 * cookie
 * TODO: ability to just collapse/expand them them and show them in that state for the
 * session
 */
function dismissMessage(target, msgId){
    utils_createCookie('motdHide', msgId);
    $(target).fadeToggle(1000, 0);
    // report that MOTD has been hidden
    reportEvent(target, '/dashboard/MOTD', 'MOTD', 'dash.hide.motd');
}


/*
 * UTILS
 */
/*
 * what type of data is contained in an [order] node
 * (to decide what to do with it)
 */
function get_type(thing){
    if (thing === null) {
        return "[object Null]";
    }
    return Object.prototype.toString.call(thing).match(/^\[object (.*)\]$/)[1];
}

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

/*
 * cookie utils
 */
/**
 * cookie create
 * @param {Object} name
 * @param {Object} value
 * @param {Object} days
 */
utils_createCookie = function(name, value, days){
    var expires = "";
    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        expires = "; expires=" + date.toGMTString();
    }
    else {
        expires = "";
        document.cookie = name + "=" + value + expires + "; path=/";
    }
};

/**
 * cookie read
 * @param {Object} name
 */
utils_readCookie = function(name){
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1, c.length);
        }
        if (c.indexOf(nameEQ) === 0) {
            return c.substring(nameEQ.length, c.length);
        }
    }
    return null;
};
/**
 * cookie delete
 * @param {Object} name
 */
utils_eraseCookie = function(name){
    createCookie(name, "", -1);
};

utils_trim = function(stringToTrim){
    return stringToTrim.replace(/^\s+|\s+$/g, "");
};

/*
 * wicket pager is lacking some style hooks so
 * eliminate whitespace only text nodes
 * needed to style the wicket pager
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



/*
 * DEPRECATED?
 */
/*
 stripe the tables, take out when can figure how to do in wicket
 */
var setupTableStriping = function(){
    $('table').each(function(){
        $(this).find('tr:even').addClass('even');
    });
};
/*
 * initial design for inteacting with menus
 */
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

/*
 * if there is language that needs to be available to the js
 */
var setupLang = function(){
    //langdata = eval('(' + $('#lang-holder').text() + ')');
};

/*
 * No longer adding icons client side?
 */
var setupIcons = function(){
    $('.itemLink').each(function(i){
        var itemType = $(this).closest('tr').find('.itemType').text();
        if (itemType === 'resource') {
            $(this).addClass(getFileExtension($(this).attr('href')));
        }
        else {
            $(this).addClass(itemType + '-icon');
        }
    });
    function getFileExtension(filename){
        var ext = /^.+\.([^.]+)$/.exec(filename);
        return ext === null ? "" : ext[1].toLowerCase();
    }
    
};
