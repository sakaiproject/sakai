/* dhtml_view_sites
 * displays the More Sites div
 * note the technique of recasting the function after initalization
 */
var dhtml_view_sites = function(){
    // first time through set up the DOM
    jQuery('#selectSite').appendTo('#linkNav').addClass('dhtml_more_tabs'); // move the selectSite in the DOM
    jQuery('.more-tab').position();
    
    // then recast the function to the post initialized state which will run from then on
    dhtml_view_sites = function(){
        if (jQuery('#selectSite').css('display') == 'none') {
            jQuery('#selectSite div').show();
            jQuery('#selectSite').slideDown('fast', function(){
                // check if $('#otherSiteList li').length > some number, then show search
                // otherwise not
                   if(jQuery('#otherSitesCategorWrap').height() > 300){
                    $('#otherSitesCategorWrap').height(300).css({overflow:"auto"});
                }


                jQuery('#txtSearch').focus();
            });
            createDHTMLMask(dhtml_view_sites);
            jQuery('.selectedTab').bind('click', function(e){
                dhtml_view_sites();
                return false;
            });

            jQuery('#selectSite a:first').focus();

            // If we hit escape or the up arrow on any of the links in the drawer, slide it
            // up and focus on the more tab.
            jQuery('#selectSite a').keydown(function (e) {
                if(e.keyCode == 38 || e.keyCode == 27) {
                    e.preventDefault();
                    closeDrawer();
                }
            });

            // Show the tool popup on the down arrow, or slide up the drawer on escape.
            $('.moreSitesLink').keydown(function (e){
                if (e.keyCode == 40) {
                    showToolMenu(e,0);
                }
            });

            // If we've tabbed backwards to the first element in the drawer, it could be the
            // search box or the all sites list, stop tabbing. This is a hack as we are
            // currently attaching keydown handlers to the list item text rather that the link
            // and you can only explicitly set focus to links and form elements.
            var txtSearch = $('#txtSearch');
            if(txtSearch.length) {
                $(txtSearch[0]).keydown(function (e) {
                    if (e.keyCode == 9 && e.shiftKey) {
                        e.preventDefault();
                    }
                });
            } else {
                $('#allSites').keydown(function (e) {
                    if (e.keyCode == 9 && e.shiftKey) {
                        e.preventDefault();
                    }
                });
            }

            // If we tab off the right of the sites list, cycle the focus.
            $('#otherSiteList > li:last').keydown(function (e) {
                if (e.keyCode == 9 && !e.shiftKey) {
                    e.preventDefault();
                    if(txtSearch.length) {
                        txtSearch[0].focus();
                    } else {
                        $('#allSites').focus();
                    }
                }
            });
        }
        else {
            // hide the dropdown
            jQuery('#selectSite div').hide();
            jQuery('#selectSite').slideUp('fast'); // hide the box
            removeDHTMLMask()
            jQuery('#otherSiteTools').remove();
            jQuery('.selectedTab').unbind('click');
        }
    }
    // finally run the inner function, first time through
    dhtml_view_sites();
}

function closeDrawer() {
    jQuery('#selectSite div').hide();
    jQuery('#selectSite').slideUp('fast'); // hide the box
    removeDHTMLMask()
    jQuery('#otherSiteTools').remove();
    jQuery('.selectedTab').unbind('click');
    jQuery('.moreSitesLink').unbind('keydown');
    jQuery('.more-tab a').focus();
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
        $('#portalContainer').addClass('minimize-site-nav')
    }
    if (portal.toggle.tools) {
        $('#toolMenuWrap').addClass('minimize-tool-nav');
    }
    $('#toggleToolMax').addClass('hidden').removeClass('show');
    $('#toggleToolMenu').attr('title',$('#toggleNormal em').text());
    $('#toggleNormal').addClass('show').removeClass('hidden');
}

function sakaiRestoreNavigation(){
    if (!(portal.toggle.sitenav || portal.toggle.tools)) 
        return;
    if (portal.toggle.sitenav) {
        $('#portalContainer').removeClass('minimize-site-nav')
    }
    if (portal.toggle.tools) {
        $('#toolMenuWrap').removeClass('minimize-tool-nav');
    }
	$('#toggleToolMax').addClass('show').removeClass('hidden');
    $('#toggleToolMenu').attr('title',$('#toggleToolMax em').text());
	$('#toggleNormal').addClass('hidden').removeClass('show');
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

/** Shows a drawer site tool dropdown **/
function showToolMenu(e, xOffset){
    e.preventDefault();
    var jqObj = $(e.target);
    var classId = jqObj.attr('id');
    // We need to escape special chars, like exclamations, or else jQuery selectors don't work.
    var id = classId.replace(/!/g,'\\!').replace(/~/g,'\\~');
    $('.toolMenus').removeClass('toolMenusActive');
    if ($('.' + id).length) {
        $('#otherSiteTools').remove();
    }
    else {
        $('#otherSiteTools').remove();
        var subsubmenu = "<ul id=\"otherSiteTools\" class=\"" + classId + "\" role=\"menu\">";
        var siteURL = '/direct/site/' + classId + '/pages.json';
        scroll(0, 0)
        var pos = jqObj.offset();
        var maxToolsInt = parseInt($('#maxToolsInt').text());
        var maxToolsText = $('#maxToolsAnchor').text();
        var goToSite = '<li class=\"otherSiteTool\"><span><a role=\"menuitem\" href=\"' + portal.portalPath + '/site/' + classId + '\" title=\"' + maxToolsText + '\"><span class=\"toolMenuIcon icon-sakai-see-all-tools\"> </span>' + maxToolsText + '</a></span></li>';
        jQuery.getJSON(siteURL, function(data){
            $.each(data, function(i, item){
                if (i <= maxToolsInt) {
                    if (item.toolpopup) {
                        subsubmenu = subsubmenu + '<li class=\"otherSiteTool\"><span><a role=\"menuitem\"  href=\"' + item.tools[0].url + "?sakai.popup=yes\" title=\"" + item.title + "\" onclick=\"window.open('" + item.toolpopupurl + "');\"><span class=\"toolMenuIcon icon-" + item.tools[0].toolId.replace(/\./gi, '-') + "\"> </span>" + item.title + "</a></span></li>";
                    } else if (item.tools.length === 1) {
                        subsubmenu = subsubmenu + '<li class=\"otherSiteTool\"><span><a role=\"menuitem\"  href=\"' + item.tools[0].url + "\" title=\"" + item.title + "\"><span class=\"toolMenuIcon icon-" + item.tools[0].toolId.replace(/\./gi, '-') + "\"> </span>" + item.title + "</a></span></li>";
                    }
                }
                
            });
            if ((data.length - 1) > maxToolsInt) {
                subsubmenu = subsubmenu + goToSite
            }
            subsubmenu = subsubmenu + "</ul>"
            $('#portalOuterContainer').append(subsubmenu);
            $('#otherSiteTools').css({
                'top': pos.top + 28,
                'left': pos.left - xOffset
            });
            $('#otherSiteTools li a:first').focus();
            jqObj.parent().find('.toolMenus').addClass("toolMenusActive");
            // On up arrow or escape, hide the popup
            $('#otherSiteTools').keydown(function(e){
                if (e.keyCode == 27) {
                    e.preventDefault();
                    jqObj.focus();
                    $(this).remove();
                    $('.' + id).remove();
                    jqObj.parent().find('.toolMenus').removeClass("toolMenusActive");
                }
            });
            
            addArrowNavAndDisableTabNav($('#otherSiteTools'), function () {
                jqObj.focus();
                $('.' + id).remove();
                // Switch the arrows
                jqObj.parent().find('.toolMenus').removeClass("toolMenusActive");
            });
        }); // end json call
    }
}

jQuery(document).ready(function(){
    if ($('#eid').length === 1) {
        $('#eid').focus()
    }

    // SAK-22026. Attach down and up arrow handlers to the more sites tab.
    $('.more-tab a').keydown(function (e) {
        if (e.keyCode == 40 || e.keyCode == 38 || e.keyCode == 27) {
            e.preventDefault();
            return dhtml_view_sites();
        }
    });
    
    // open tool menus in "other sites" panel
    $('.toolMenus').click(function(e){
        showToolMenu(e,173);
    });
    
    // prepend site title to tool title
    // here as reminder to work on an actual breadcrumb integrated with neo style tool updates
    //var siteTitle = ($('.nav-selected span:first').text())
    var siteTitle = portal.siteTitle;
    if (siteTitle) {
	if (portal.shortDescription) {
	    siteTitle = siteTitle + " ("+portal.shortDescription+")"
	}
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
            jQuery('#otherSiteList li, .otherSitesCategorList li').hide();
            jQuery('#otherSitesCategorWrap h4').hide();
            jQuery('#otherSiteList li a span.fullTitle:Contains(\'' + jQuery('#txtSearch').val() + '\')').parent('a').parent('li').show();
            jQuery('.otherSitesCategorList li a span.fullTitle:Contains(\'' + jQuery('#txtSearch').val() + '\')').parent('a').parent('li').show().closest('ul').prev('h4').show();
            jQuery('#imgSearch').fadeIn('slow');
        }
        if (jQuery('#txtSearch').val().length == 0) {
            resetSearch();
        }
        // Should be <=1 if there is a header line
        if (jQuery('#otherSiteList li:visible').length < 1 && jQuery('.otherSitesCategorList li:visible').length < 1) {
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
        jQuery('.otherSitesCategorList li').show();
        jQuery('#otherSitesCategorWrap h4').show();
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
    });
    
    //bind directurl checkboxes
    if ( jQuery('a.tool-directurl').length ) jQuery('a.tool-directurl').cluetip({
    	local: true,
    	arrows: true,
		cluetipClass: 'jtip',
		sticky: true,
		cursor: 'pointer',
		activation: 'click',
		closePosition: 'title',
		closeText: '<img src="/library/image/silk/cross.png" alt="close">'
    });

	// Shows or hides the subsites in a popout div. This isn't used unless
	// portal.showSubsitesAsFlyout is set to true in sakai.properties.
	jQuery("#toggleSubsitesLink").click(function (e) {
        var subsitesLink = $(this);
        if($('#subSites').css('display') == 'block') {
            $('#subSites').hide();
        } else {
            var position = subsitesLink.position();
            $('#subSites').css({'position': 'absolute','display': 'block','left': position.left + subsitesLink.width() + 14 + 'px','top': position.top + 'px'});
        }
    });

});

var setupSiteNav = function(){
    $("ul.subnav").each(function(){
        // Add an escape key handler to slide the page menu up
        $(this).keydown(function(e) {
            if (e.keyCode == 27) {
                $(this).parent().children('a').focus();
                $(this).slideUp('fast');
            }
        });
        $(this).children('li:last').addClass('lastMenuItem')
    });
    
    jQuery("ul.topnav > li").mouseleave(function(){
        $(this).find('ul').slideUp('fast')
    });

    jQuery("#loginLinks ul.nav-submenu").mouseleave(function(){
        $(this).slideUp('fast')
    });

    jQuery("#loginLinks span.drop").click(function(e){
        $(this).prev('ul').slideDown('fast')
     });

	fixTopNav = function(e){
        if (e.keyCode == 40) { // downarrow
            e.preventDefault();
            jQuery('#selectSite').hide();
            $('.nav-submenu').hide();
            // Trigger click on the drop <span>, passing true to set focus on
            // the first tool in the dropdown.
            jQuery(this).parent().find(".drop").trigger('click',[true]);
        } else if (e.keyCode == 27) { // uparrow
            $(this).parent().children('a').focus();
            $(this).slideUp('fast');
        }
    }

	// SAK-25505 - Switch from live() to on()
	// $( "a.offsite" ).live( "click", function() {
	// $('.topnav > li.nav-menu > a').live('keydown', function(e){
	if ( $(document).on ) {
		$(document).on('keydown', '.topnav > li.nav-menu > a', fixTopNav);
	} else {
		$('.topnav > li.nav-menu > a').live('keydown', fixTopNav);
	}
    
    jQuery("ul.topnav > li").mouseleave(function(){
        $(this).find('ul').slideUp('fast')
    });
    // focusFirstLink is only ever passed from the keydown handler. We
    // don't want to focus on click; it looks odd.
    jQuery("ul.topnav li span.drop").click(function(e, focusFirstLink){
        /*
         * see if there is a menu sibling
         *      if there is a child, display it
         *  if no menu sibling
         *       retrieve data, construct the menu, append
         */
        e.preventDefault()
        var jqObjDrop = $(e.target);
        if (jqObjDrop.parent('li').find('ul').length) {
            jqObjDrop.parent('li').find('ul').slideDown('fast')
            if(focusFirstLink) {
                jqObjDrop.parent().find("ul.subnav a:first").focus();
            }
        }
        else {
            var navsubmenu = "<ul class=\"nav-submenu subnav\" role=\"menu\" class=\"show\">";
            var siteId = jqObjDrop.attr('data');
            var maxToolsInt = parseInt($('#maxToolsInt').text());
            var maxToolsText = $('#maxToolsAnchor').text();
            var goToSite = '<li class=\"submenuitem\"><a role=\"menuitem\" href=\"' + portal.portalPath + '/site/' + siteId + '\" title=\"' + maxToolsText + '\"><span class=\"toolMenuIcon icon-sakai-see-all-tools\"></span>' + maxToolsText + '</a></li>';
            var siteURL = '/direct/site/' + jqObjDrop.attr('data') + '/pages.json';
            jQuery.ajax({
                url: siteURL,
                dataType: "json",
                success: function(data){
                    $.each(data, function(i, item){
                        if (i <= maxToolsInt) {
                            if (item.toolpopup) {
                                navsubmenu = navsubmenu + '<li class=\"submenuitem\" ><span><a role=\"menuitem\" href=\"' + item.tools[0].url + "?sakai.popup=yes\" title=\"" + item.title + "\" onclick=\"window.open('" + item.toolpopupurl + "');\"><span class=\"toolMenuIcon icon-" + item.tools[0].toolId.replace(/\./gi, '-') + "\"></span>" + item.title + "</a></span></li>";
                            } else if (item.tools.length === 1) {
                                navsubmenu = navsubmenu + '<li class=\"submenuitem\" ><span><a role=\"menuitem\" href=\"' + item.tools[0].url + "\" title=\"" + item.title + "\"><span class=\"toolMenuIcon icon-" + item.tools[0].toolId.replace(/\./gi, '-') + "\"></span>" + item.title + "</a></span></li>";
                            }
                        }
                    });
                    if ((data.length - 1) > maxToolsInt) {
                        navsubmenu = navsubmenu + goToSite
                    }
                    navsubmenu = navsubmenu + "</ul>"
                    jqObjDrop.after(navsubmenu);
                    if(focusFirstLink) {
                        jqObjDrop.parent().find("ul.subnav a:first").focus();
                    }
                    addArrowNavAndDisableTabNav($("ul.subnav"));
                },
                error: function(XMLHttpRequest, status, error){
                    // Something happened getting the tool list. 
                }
            });
        }
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
    
	$('#toggleToolMenu').hover(function () {
         $(this).find('span').addClass('toggleToolMenuHover')
	}, 
      function () {
         $(this).find('span').removeClass('toggleToolMenuHover')
      }
  );
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

function publishSite(siteId) { 
    var reqUrl = '/direct/site/'+siteId+"/edit"; 
    var resp = $.ajax({ 
      type: 'POST', 
      data: 'published=true', 
      url: reqUrl, 
      success: function() { location.reload(); } 
    }).responseText; 
}

var setupSkipNav = function(){
    // function called from site.vm to enable skip links for all browsers
     $('#skipNav a.internalSkip').click(function(){
         var target = $(this).attr('href');
        $(target).attr('tabindex','-1').focus();
     });
};

//handles showing either the short url or the full url, depending on the state of the checkbox 
//(if configured, otherwise returns url as-is as according to the url shortening entity provder)
function toggleShortUrlOutput(defaultUrl, checkbox, textbox) {		
	
	if($(checkbox).is(':checked')) {
		
		$.ajax({
			url:'/direct/url/shorten?path='+encodeURI(defaultUrl),
			success: function(shortUrl) {
				$('.'+textbox).val(shortUrl);
			}
		}); 
	} else {
		$('.'+textbox).val(defaultUrl);
	}
}

/* Callback is a function and is called after sliding up ul */
function addArrowNavAndDisableTabNav(ul,callback) {
    ul.find('li a').attr('tabindex','-1').keydown(function (e) {
        var obj = $(e.target);
        if(e.keyCode == 40) {
            e.preventDefault();
            var next = obj.parent().parent().next();
            if(next[0] === undefined) {
                ul.slideUp('fast');
                if(callback !== undefined) {
                    callback();
                } else {
                    obj.parent().parent().parent().parent().children('a').focus();
                }
            } else {
                next.find('a').focus();
            }
        } else if(e.keyCode == 9) { // Suck up the menu if tab is pressed 
            ul.slideUp('fast');
        } else if(e.keyCode == 38) {
            // Up arrow
            e.preventDefault();
            var prev = obj.parent().parent().prev();
            if(prev[0] === undefined) {
                ul.slideUp('fast');
                if(callback !== undefined) {
                    callback();
                } else {
                    obj.parent().parent().parent().parent().children('a').focus();
                }
            } else {
                prev.find('a').focus();
            }
        }
    });
}
