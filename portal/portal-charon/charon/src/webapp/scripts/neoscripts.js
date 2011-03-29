
/* Script for pop-up dhtml more tabs implementation 
 * uses jQuery library
 */
/* dhtml_more_tabs
 * displays the More Sites div
 * note the technique of recasting the function after initalization
 */
var dhtml_more_tabs = function(){
    // first time through set up the DOM
    jQuery('#selectNav').appendTo('#linkNav').addClass('dhtml_more_tabs'); // move the selectNav in the DOM
    jQuery('#selectNav').css('top', jQuery('#linkNav').height() - 3); // set its top position
    jQuery('#selectNav').width(jQuery('#linkNav').width() * 0.75); // set its width to fix an IE6 bug
    jQuery('#selectNav').css('z-index', 9900); // explicitely set the z-index
    jQuery('.more-tab').css('z-index', 9800); //  " for the More Tabs div element
    // then recast the function to the post initialized state which will run from then on
    dhtml_more_tabs = function(){
        if (jQuery('#selectNav').css('display') == 'none') {
            jQuery('div#selectNav').show();
            // highlight the more tab
            jQuery('.more-tab').addClass('more-active');
            // dim the current tab
            jQuery('.selectedTab').addClass('tab-dim');
            // mask the rest of the page
            createDHTMLMask(dhtml_more_tabs);
            // bind this function to the More Tabs tab to close More Tabs on click
            jQuery('.selectedTab').bind('click', function(e){
                console.log(e.pageX)
                dhtml_more_tabs();
                return false;
            });
        }
        else {
            // unhighlight the more tab
            jQuery('.more-tab').removeClass('more-active');
            // hide the dropdown
            jQuery('div#selectNav').hide(); // hide the box
            //undim the currently selected tab
            jQuery('.selectedTab').removeClass('tab-dim');
            removeDHTMLMask()
            jQuery('.selectedTab').unbind('click');
        }
    }
    // finally run the inner function, first time through
    dhtml_more_tabs();
}

/* dhtml_view_sites
 * displays the More Sites div
 * note the technique of recasting the function after initalization
 */
var dhtml_view_sites = function(){
    // first time through set up the DOM
    jQuery('#selectSite').appendTo('#linkNav').addClass('dhtml_more_tabs'); // move the selectSite in the DOM
    jQuery('.more-tab').position();
    jQuery('#selectSite').css('top', '38px'); // set its top position
    jQuery('#selectSite').css('left', '4px');
    jQuery('#selectSite').css('margin', '0 auto');
    jQuery('#selectSite').css('width', '70%');
    jQuery('#selectSite').css('z-index', 9900); // explicitely set the z-index
    //	jQuery('.more-tab').css('z-index',9800);                                  //  " for the More Tabs div element
    
    // then recast the function to the post initialized state which will run from then on
    dhtml_view_sites = function(){
        if (jQuery('#selectSite').css('display') == 'none') {
            jQuery('div#selectSite').slideDown('slow', function(){
                // check if $('#otherSiteList li').length > some number, then show search
                // otherwise not
                jQuery('div#selectSite div').show();
                jQuery('#txtSearch').focus();
            });
            createDHTMLMask(dhtml_view_sites);
            jQuery('.selectedTab').bind('click', function(e){
                console.log(e.pageX)
                dhtml_view_sites();
                return false;
            });
        }
        else {
            // hide the dropdown
            jQuery('div#selectSite div').hide();
            jQuery('div#selectSite').slideUp('fast').hide(); // hide the box
            removeDHTMLMask()
            jQuery('#otherSiteTools').remove();
            jQuery('.selectedTab').unbind('click');
        }
    }
    // finally run the inner function, first time through
    dhtml_view_sites();
}

function createDHTMLMask(callback){

    jQuery('body').append('<div id="portalMask">&nbsp;</div>');
    jQuery('#portalMask').css('height', browserSafeDocHeight()).css({
        'width': '100%',
        'z-index': 1000,
        'top': 0,
        'left': 0
    }).bind("click", function(event){
        callback();
        return false;
    });
    jQuery('#portalMask').bgiframe();
}

function removeDHTMLMask(){
    jQuery('#portalMask').remove();
}

/* Copyright (c) 2006 Brandon Aaron (http://brandonaaron.net)
 * Dual licensed under the MIT (http://www.opensource.org/licenses/mit-license.php)
 * and GPL (http://www.opensource.org/licenses/gpl-license.php) licenses.
 *
 * $LastChangedDate$
 * $Rev$
 *
 * Version 2.1.1
 */
(function($){
    $.fn.bgIframe = $.fn.bgiframe = function(s){
        if ($.browser.msie && /6.0/.test(navigator.userAgent)) {
            s = $.extend({
                top: 'auto',
                left: 'auto',
                width: 'auto',
                height: 'auto',
                opacity: true,
                src: 'javascript:false;'
            }, s || {});
            var prop = function(n){
                return n && n.constructor == Number ? n + 'px' : n;
            }, html = '<iframe class="bgiframe"frameborder="0"tabindex="-1"src="' + s.src + '"' + 'style="display:block;position:absolute;z-index:-1;' + (s.opacity !== false ? 'filter:Alpha(Opacity=\'0\');' : '') + 'top:' + (s.top == 'auto' ? 'expression(((parseInt(this.parentNode.currentStyle.borderTopWidth)||0)*-1)+\'px\')' : prop(s.top)) + ';' + 'left:' + (s.left == 'auto' ? 'expression(((parseInt(this.parentNode.currentStyle.borderLeftWidth)||0)*-1)+\'px\')' : prop(s.left)) + ';' + 'width:' + (s.width == 'auto' ? 'expression(this.parentNode.offsetWidth+\'px\')' : prop(s.width)) + ';' + 'height:' + (s.height == 'auto' ? 'expression(this.parentNode.offsetHeight+\'px\')' : prop(s.height)) + ';' + '"/>';
            return this.each(function(){
                if ($('> iframe.bgiframe', this).length == 0) 
                    this.insertBefore(document.createElement(html), this.firstChild);
            });
        }
        return this;
    };
})(jQuery);

//For SAK-13987
//For SAK-16162
//Just use the EB current.json as the session id rather than trying to do a search/replace
var sessionId = "current";
var sessionTimeOut;
var timeoutDialogEnabled = false;
var timeoutDialogWarningTime;
var timeoutLoggedoutUrl;
var timeoutPortalPath;
jQuery(document).ready(function(){
    // note a session exists whether the user is logged in or no
    if (portal.loggedIn && portal.timeoutDialog) {
        setTimeout('setup_timeout_config();', 60000);
    }
});

var setup_timeout_config = function(){
    timeoutDialogEnabled = portal.timeoutDialog.enabled;
    timeoutDialogWarningTime = portal.timeoutDialog.seconds;
    timeoutLoggedoutUrl = portal.loggedOutUrl;
    timeoutPortalPath = portal.portalPath;
    if (timeoutDialogEnabled == true) {
        poll_session_data();
        fetch_timeout_dialog();
    }
}

var poll_session_data = function(){
    //Need to append Date.getTime as sakai still uses jquery pre 1.2.1 which doesn't support the cache: false parameter.
    jQuery.ajax({
        url: "/direct/session/" + sessionId + ".json?auto=true&_=" + (new Date()).getTime(), //auto=true makes it not refresh the session lastaccessedtime
        dataType: "json",
        success: function(data){
            //get the maxInactiveInterval in the same ms
            data.maxInactiveInterval = data.maxInactiveInterval * 1000;
            if (data.active && data.userId != null &&
            data.lastAccessedTime + data.maxInactiveInterval >
            data.currentTime) {
                //User is logged in, so now determine how much time is left
                var remaining = data.lastAccessedTime + data.maxInactiveInterval - data.currentTime;
                //If time remaining is less than timeoutDialogWarningTime minutes, show/update dialog box
                if (remaining < timeoutDialogWarningTime * 1000) {
                    //we are within 5 min now - show popup
                    min = Math.round(remaining / (1000 * 60));
                    show_timeout_alert(min);
                    clearTimeout(sessionTimeOut);
                    sessionTimeOut = setTimeout("poll_session_data()", 1000 * 60);
                }
                else {
                    //more than timeoutDialogWarningTime min away
                    clearTimeout(sessionTimeOut);
                    sessionTimeOut = setTimeout("poll_session_data()", (remaining - timeoutDialogWarningTime * 1000));
                }
            }
            else 
                if (data.userId == null) {
                    // if data.userId is null, the session is done; redirect the user to logoutUrl
                    location.href = timeoutLoggedoutUrl;
                    
                }
                else {
                    //the timeout length has occurred, but there is a slight delay, do this until there isn't a user.
                    sessionTimeOut = setTimeout("poll_session_data()", 1000 * 10);
                }
        },
        error: function(XMLHttpRequest, status, error){
            // We used to to 404 handling here but now we should always get good session data.
        }
    });
}

function keep_session_alive(){
    dismiss_session_alert();
    jQuery.get(timeoutPortalPath);
}

var dismiss_session_alert = function(){
    removeDHTMLMask();
    jQuery("#timeout_alert_body").remove();
}

var timeoutDialogFragment;
function fetch_timeout_dialog(){
    jQuery.ajax({
        url: "/portal/timeout?auto=true",
        cache: true,
        dataType: "text",
        success: function(data){
            timeoutDialogFragment = data;
        },
        error: function(XMLHttpRequest, status, error){
            timeoutDialogEnabled = false;
        }
    });
}

function show_timeout_alert(min){
    if (!timeoutDialogEnabled) {
        return;
    }
    
    if (!jQuery("#portalMask").get(0)) {
        createDHTMLMask(dismiss_session_alert);
        jQuery("#portalMask").css("z-index", 10000);
    }
    if (jQuery("#timeout_alert_body").get(0)) {
        //its there, just update the min
        jQuery("#timeout_alert_body span").html(min);
        jQuery("#timeout_alert_body span").css('top', (f_scrollTop() + 100) + "px");
    }
    else {
        var dialog = timeoutDialogFragment.replace("{0}", min);
        jQuery("body").append(dialog);
        jQuery('#timeout_alert_body').css('top', (f_scrollTop() + 100) + "px");
    }
}

// The official way for a tool to request minimized navigation
function portalMaximizeTool(){
    if (!(portal.toggle.sitenav || portal.toggle.tools)) 
        return;
    if (!portal.toggle.allowauto) 
        return;
    sakaiMinimizeNavigation();
}

function sakaiMinimizeNavigation(){
    if (!(portal.toggle.sitenav || portal.toggle.tools)) 
        return;
    if (portal.toggle.sitenav) {
        $('#portalContainer').addClass('sakaiMinimizeSiteNavigation')
    }
    if (portal.toggle.tools) {
        $('#container').addClass('sakaiMinimizePageNavigation');
    }
    $('#toggleToolMax').hide();
    $('#toggleNormal').css({
        'display': 'block'
    });
}

function sakaiRestoreNavigation(){
    if (!(portal.toggle.sitenav || portal.toggle.tools)) 
        return;
    if (portal.toggle.sitenav) {
        $('#portalContainer').removeClass('sakaiMinimizeSiteNavigation')
    }
    if (portal.toggle.tools) {
        $('#container').removeClass('sakaiMinimizePageNavigation');
    }
    $('#toggleToolMax').show();
    $('#toggleNormal').css({
        'display': 'none'
    });
}

function updatePresence(){
    jQuery.ajax({
        url: sakaiPresenceFragment,
        cache: false,
        success: function(frag){
            var whereul = frag.indexOf('<ul');
            if (whereul < 1) {
                $("#presenceCount").html(' ');
                $('#presenceCount').removeClass('present').addClass('empty');
                location.reload();
                return;
            }
            frag = frag.substr(whereul);
            var _s = frag;
            var _m = '<li'; // needle 
            var _c = 0;
            for (var i = 0; i < _s.length; i++) {
                if (_m == _s.substr(i, _m.length)) 
                    _c++;
            }
            // No need to attrct attention you are alone
            if (_c > 1) {
                $("#presenceCount").html(_c + '');
                $('#presenceCount').removeClass('empty').addClass('present');
            }
            else 
                if (_c == 1) {
                    $("#presenceCount").html(_c + '');
                    $('#presenceCount').removeClass('present').addClass('empty');
                }
                else {
                    $("#presenceCount").html(' ');
                    $('#presenceCount').removeClass('present').addClass('empty');
                }
            $("#presenceIframe").html(frag);
            var chatUrl = $('.nav-selected .icon-sakai-chat').attr('href');
            $('#presenceIframe .presenceList li.inChat span').wrap('<a href="' + chatUrl + '"></a>')
            sakaiLastPresenceTimeOut = setTimeout('updatePresence()', 30000);
        },
        // If we get an error, wait 60 seconds before retry
        error: function(request, strError){
            sakaiLastPresenceTimeOut = setTimeout('updatePresence()', 60000);
        }
    });
}

function f_scrollTop(){
    return f_filterResults(window.pageYOffset ? window.pageYOffset : 0, document.documentElement ? document.documentElement.scrollTop : 0, document.body ? document.body.scrollTop : 0);
}

function f_filterResults(n_win, n_docel, n_body){
    var n_result = n_win ? n_win : 0;
    if (n_docel && (!n_result || (n_result > n_docel))) 
        n_result = n_docel;
    return n_body && (!n_result || (n_result > n_body)) ? n_body : n_result;
}

jQuery(document).ready(function(){
    if ($('#eid').length === 1) {
        $('#eid').focus()
    }
    
    // open tool menus in "other sites" panel
    $('.toolMenus').click(function(e){
        e.preventDefault();
        $('#otherSiteTools').remove();
        var subsubmenu = "<ul id=\"otherSiteTools\">";
        var siteURL = '/direct/site/' + $(this).attr('id') + '/pages.json';
        scroll(0, 0)
        var pos = $(this).offset();
        jQuery.getJSON(siteURL, function(data){
            $.each(data, function(i, item){
                if (item.tools.length === 1) {
                    subsubmenu = subsubmenu + '<li class=\"otherSiteTool\"><span><a class=\"icon-' + item.tools[0].toolId.replace(/\./gi, '-') + '\" href=' + item.tools[0].url + ">" + item.tools[0].title + "</a></span></li>"
                }
            });
            subsubmenu = subsubmenu + "</ul>"
            $('#portalOuterContainer').append(subsubmenu);
            $('#otherSiteTools').css({
                'top': pos.top,
                'left': pos.left + 30
            });
            $('#otherSiteTools li:first').attr('tabindex', '-1')
            $('#otherSiteTools li:first').focus();
        });
        
    });
    
    // prepend site title to tool title
    // here as reminder to work on an actual breadcrumb integrated with neo style tool updates
    var siteTitle = ($('.nav-selected span:first').text())
    if (siteTitle) {
        $('.portletTitle h2').prepend('<span class=\"siteTitle\">' + siteTitle + ':</span> ')
    }
    
    // other site search handlers
    jQuery('#imgSearch').click(function(){
        resetSearch();
    });
    
    jQuery('#txtSearch').keyup(function(event){
        if (event.keyCode == 27) {
            resetSearch();
        }
        if (jQuery('#txtSearch').val().length > 0) {
            jQuery('#otherSiteList li').hide();
            //jQuery('#otherSiteList li:first').show();
            jQuery('#otherSiteList li a span.fullTitle:Contains(\'' + jQuery('#txtSearch').val() + '\')').parent('a').parent('li').show();
            jQuery('#imgSearch').fadeIn('slow');
        }
        if (jQuery('#txtSearch').val().length == 0) {
            resetSearch();
        }
        // Should be <=1 if there is a header line
        if (jQuery('#otherSiteList li:visible').length < 1) {
            jQuery('.norecords').remove();
            jQuery('#otherSiteSearch #noSearchResults').fadeIn('slow');
        }
    });
    // case insensitive version of :contains
    jQuery.expr[':'].Contains = function(a, i, m){
        return jQuery(a).text().toUpperCase().indexOf(m[3].toUpperCase()) >= 0;
    };
    function resetSearch(){
        jQuery('#txtSearch').val('');
        jQuery('#otherSiteList li').show();
        jQuery('#noSearchResults').fadeOut();
        jQuery('#imgSearch').fadeOut();
        jQuery('#txtSearch').focus();
    }
    
    //toggle presence panel
    $("#presenceToggle").click(function(e){
        e.preventDefault();
        $('#presenceArea').toggle();
    });
    //explicitly close presence panel
    $('.trayPopupClose').click(function(e){
        e.preventDefault();
        $(this).closest('.trayPopup').hide();
    })
});

var setupSiteNav = function(){
    jQuery("ul.subnav").parent().append('<span class="drop" tabindex="-1"></span>');
    $("ul.subnav").each(function(){
        $(this).children('li:last').addClass('lastMenuItem')
    });
    $('.lastMenuItem a').blur(function(e){
        jQuery(this).parents('ul.subnav').slideUp('fast').hide();
    });
    $('.topnav a').keydown(function(e){
        if (e.keyCode == 40) {
            jQuery('#selectSite').hide();
            jQuery(this).parent().find("ul.subnav").slideDown('fast').show();
            jQuery(this).parent().find("ul.subnav a:first").focus();
        }
        if (e.keyCode == 38) {
            jQuery(this).parent().find("ul.subnav").slideUp('fast').hide();
        }
        
    });
    
    jQuery("ul.topnav li span.drop").click(function(){
        jQuery('#selectSite').hide();
        jQuery('#otherSiteTools').remove();
        
        jQuery(this).parent().find("ul.subnav").slideDown('fast').show();
        
        jQuery(this).parent().hover(function(){
        }, function(){
            jQuery(this).parent().find("ul.subnav").slideUp('slow');
        });
        
    }).hover(function(){
        jQuery(this).addClass("subhover"); //On hover over, add class "subhover"
    }, function(){ //On Hover Out
        jQuery(this).removeClass("subhover"); //On hover out, remove class "subhover"
    });
    
}

var setupToolToggle = function(toggleClass){
    $('#toggler').prependTo('#toolMenuWrap');
    $('#toggler').css({
        'display': 'inline'
    });
    $('#toggler').addClass(toggleClass)
    
    $('#toggleToolMenu').click(function(){
        if ($('#toggleNormal').is(':visible')) {
            sakaiRestoreNavigation();
            document.cookie = "sakai_nav_minimized=false; path=/";
        }
        else {
            sakaiMinimizeNavigation();
            document.cookie = "sakai_nav_minimized=true; path=/";
        }
    });
}
