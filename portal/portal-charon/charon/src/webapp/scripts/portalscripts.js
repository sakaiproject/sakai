/* Script for pop-up dhtml more tabs implementation 
 * uses jQuery library
 */


/* dhtml_more_tabs
 * displays the More Sites div 
 * note the technique of recasting the function after initalization
 */

var dhtml_more_tabs = function() {
	// first time through set up the DOM
	$PBJQ('#selectNav').appendTo('#linkNav').addClass('dhtml_more_tabs'); // move the selectNav in the DOM
	$PBJQ('#selectNav').css('top',$PBJQ('#linkNav').height() - 3);       // set its top position

	var width = $PBJQ('#linkNav').width()*0.75;
	if (width < 400) width = 400;
	$PBJQ('div#selectNav').width(width);          // set its width to fix an IE6 bug
	$PBJQ('#selectNav').css('z-index',9899);      // explicitely set the z-index
	$PBJQ('.more-tab').css('z-index',9900);       //  " for the More Tabs div element

	//reset the left offset to avoid "more tabs" dropdown going off the left of the screen
	$PBJQ('div#selectNav').show();
	var left = $PBJQ("#selectNav").get(0).offsetLeft;
	if (left <= 0) { $PBJQ('#selectNav').css('left', 1); }
	
	$PBJQ('#selectNav').hide();
		
	// then recast the function to the post initialized state which will run from then on
	dhtml_more_tabs = function() {
		if ($PBJQ('#selectNav').css('display') == 'none' ) {
			$PBJQ('div#selectNav').show();
			// highlight the more tab
			$PBJQ('.more-tab').addClass('more-active');
			// dim the current tab
			$PBJQ('.selectedTab').addClass('tab-dim');
			// mask the rest of the page
			createDHTMLMask(dhtml_more_tabs) ;
			// bind this function to the More Tabs tab to close More Tabs on click
			$PBJQ('.selectedTab').bind('click',function(){dhtml_more_tabs();return false;});
		} else {
			// unhighlight the more tab
			$PBJQ('.more-tab').removeClass('more-active');
			// hide the dropdown
			$PBJQ('div#selectNav').hide(); // hide the box
			//undim the currently selected tab
			$PBJQ('.selectedTab').removeClass('tab-dim');
			removeDHTMLMask()
			$PBJQ('.selectedTab').unbind('click');
		}
	}
	// finally run the inner function, first time through
	dhtml_more_tabs();
}

function createDHTMLMask(callback) {
	$PBJQ('body').append('<div id="portalMask">&nbsp;</div>');
	$PBJQ('#portalMask').css('height',browserSafeDocHeight()).css('width','100%').css('z-index',1000).bind("click",function(event){
		callback();
		return false;
	});
	$PBJQ('#portalMask').bgiframe();
}

function removeDHTMLMask() {
	$PBJQ('#portalMask').remove();
}

/* Copyright (c) 2010 Brandon Aaron (http://brandonaaron.net)
 * Licensed under the MIT License (LICENSE.txt).
 *
 * Version 2.1.2
 */
(function(a){a.fn.bgiframe=(a.browser.msie&&/msie 6\.0/i.test(navigator.userAgent)?function(d){d=a.extend({top:"auto",left:"auto",width:"auto",height:"auto",opacity:true,src:"javascript:false;"},d);var c='<iframe class="bgiframe"frameborder="0"tabindex="-1"src="'+d.src+'"style="display:block;position:absolute;z-index:-1;'+(d.opacity!==false?"filter:Alpha(Opacity='0');":"")+"top:"+(d.top=="auto"?"expression(((parseInt(this.parentNode.currentStyle.borderTopWidth)||0)*-1)+'px')":b(d.top))+";left:"+(d.left=="auto"?"expression(((parseInt(this.parentNode.currentStyle.borderLeftWidth)||0)*-1)+'px')":b(d.left))+";width:"+(d.width=="auto"?"expression(this.parentNode.offsetWidth+'px')":b(d.width))+";height:"+(d.height=="auto"?"expression(this.parentNode.offsetHeight+'px')":b(d.height))+';"/>';return this.each(function(){if(a(this).children("iframe.bgiframe").length===0){this.insertBefore(document.createElement(c),this.firstChild)}})}:function(){return this});a.fn.bgIframe=a.fn.bgiframe;function b(c){return c&&c.constructor===Number?c+"px":c}})($PBJQ);


//For SAK-13987
//For SAK-16162
//Just use the EB current.json as the session id rather than trying to do a search/replace
var sessionId = "current";
var sessionTimeOut;
var timeoutDialogEnabled = false;
var timeoutDialogWarningTime;
var timeoutLoggedoutUrl;
var timeoutPortalPath;
$PBJQ(document).ready(function(){
	// note a session exists whether the user is logged in or no
	if (portal.loggedIn && portal.timeoutDialog ) {
		setTimeout('setup_timeout_config();', 60000);
	}
	
	//bind directurl checkboxes
    $PBJQ('a.tool-directurl').cluetip({
    	local: true,
    	arrows: true,
		cluetipClass: 'jtip',
		sticky: true,
		cursor: 'pointer',
		activation: 'click',
		closePosition: 'title',
		closeText: '<img src="/library/image/silk/cross.png" alt="close" />'
    });
});

var setup_timeout_config = function() {
	timeoutDialogEnabled = portal.timeoutDialog.enabled;
	timeoutDialogWarningTime = portal.timeoutDialog.seconds;
	timeoutLoggedoutUrl = portal.loggedOutUrl;
	timeoutPortalPath = portal.portalPath;
	if (timeoutDialogEnabled == true) {
		poll_session_data();
		fetch_timeout_dialog();
	}
}

var poll_session_data = function() {
    //Need to append Date.getTime as sakai still uses jquery pre 1.2.1 which doesn't support the cache: false parameter.
	$PBJQ.ajax({
		url: "/direct/session/" + sessionId + ".json?auto=true&_=" + (new Date()).getTime(),    //auto=true makes it not refresh the session lastaccessedtime
		dataType: "json",
		success: function(data){
		//get the maxInactiveInterval in the same ms
		data.maxInactiveInterval = data.maxInactiveInterval * 1000;
		if(data.active && data.userId != null && data.lastAccessedTime + data.maxInactiveInterval
				> data.currentTime) {
			//User is logged in, so now determine how much time is left
			var remaining = data.lastAccessedTime + data.maxInactiveInterval - data.currentTime;
			//If time remaining is less than timeoutDialogWarningTime minutes, show/update dialog box
			if (remaining < timeoutDialogWarningTime * 1000){
				//we are within 5 min now - show popup
				min = Math.round(remaining / (1000 * 60));
				show_timeout_alert(min);
				clearTimeout(sessionTimeOut);
				sessionTimeOut = setTimeout("poll_session_data()", 1000 * 60);
			} else {
				//more than timeoutDialogWarningTime min away
				clearTimeout(sessionTimeOut);
				sessionTimeOut = setTimeout("poll_session_data()", (remaining - timeoutDialogWarningTime*1000));
			}
		} else if (data.userId == null) {
			// if data.userId is null, the session is done; redirect the user to logoutUrl
			location.href=timeoutLoggedoutUrl;
		
		} else {
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
	$PBJQ.get(timeoutPortalPath);
}

var dismiss_session_alert = function(){
	removeDHTMLMask();
	$PBJQ("#timeout_alert_body").remove();
}

var timeoutDialogFragment;
function fetch_timeout_dialog() {
	$PBJQ.ajax({
		url: "/portal/timeout?auto=true",
		cache: true,
		dataType: "text",
		success: function(data) {
		timeoutDialogFragment = data; 
	},
	error: function(XMLHttpRequest, status, error){
		timeoutDialogEnabled = false;
	}
	});
}

function show_timeout_alert(min) {
	if (!timeoutDialogEnabled) {
		return;
	}

	if (!$PBJQ("#portalMask").get(0)){
		createDHTMLMask(dismiss_session_alert);
		$PBJQ("#portalMask").css("z-index", 10000);
	}
	if ($PBJQ("#timeout_alert_body").get(0)) {
		//its there, just update the min
		$PBJQ("#timeout_alert_body span").html(min);
		$PBJQ("#timeout_alert_body span").css('top', (f_scrollTop() + 100) + "px");
	} else {
		var dialog = timeoutDialogFragment.replace("{0}", min);
		$PBJQ("body").append(dialog);
		$PBJQ('#timeout_alert_body').css('top', (f_scrollTop() + 100) + "px");
	}
}

function updatePresence() {
	$PBJQ.ajax( {
		url: sakaiPresenceFragment,
		cache: false,
		success: function(frag){
			var whereHead = frag.indexOf('<head>');
			if ( whereHead > 0 ) {
			 	location.reload();
			} else {
				$PBJQ("#presenceIframe").html(frag);
			}
		},
		// If we get an error, wait 60 seconds before retry
		error: function(request, strError){
			sakaiLastPresenceTimeOut = setTimeout('updatePresence()', 60000);
		}
	});
}

function f_scrollTop() {
	return f_filterResults (
		window.pageYOffset ? window.pageYOffset : 0,
		document.documentElement ? document.documentElement.scrollTop : 0,
		document.body ? document.body.scrollTop : 0
	);
}

function f_filterResults(n_win, n_docel, n_body) {
	var n_result = n_win ? n_win : 0;
	if (n_docel && (!n_result || (n_result > n_docel)))
		n_result = n_docel;
	return n_body && (!n_result || (n_result > n_body)) ? n_body : n_result;
}


/*
Post messages to the portal
 - this parses an update to the portal produced by:
	https://source.sakaiproject.org/contrib/umich/global-alert 
	and courier
	and creates alerts and info messages displayed on the portal
	it depends on tool above, trunk courier, and changes to the skin
*/

function postGlobalAlert(data){
    var messageArray = "";
    var dismissLink = "";
    if (data.messages.length > 0) {
        if ($PBJQ("#portalMessageContainer1_2").length === 0) {
            $PBJQ("#portalOuterContainer").append('<div id=\"portalMessageContainer1_2\" role=\"application\" aria-live\"assertive\" aria-relevant=\"additions\"></div>');
        }
        if ($PBJQ("#portalMessageContainer3").length === 0) {
            $PBJQ("#portalOuterContainer").append('<div id=\"portalMessageContainer3\" role=\"application\" aria-live\"assertive\" aria-relevant=\"additions\"></div>');
        }
        $PBJQ.each(data.messages, function(i, item){
            dismissLink = '';
            // create a safe ID
            var itemId = encodeURIComponent(item.id).replace(/[^a-zA-Z 0-9]+/g,'-');
            // need to check if displayed already - is it in the DOM?
            // TODO: need to check if "was" in the DOM and has been dismissed
            if ($PBJQ('#' + itemId).length > 0) {
                //elem there already, compare timestamps
                if ($PBJQ('#' + itemId).attr('ts') !== item.timestamp) {
                    // message has changed, update it, animate it (and update the ts attribute)
                    flashMessage($PBJQ('#' + itemId), 'Update: ' + item.message);
                    $PBJQ('#' + itemId).attr('ts', item.timestamp);
                }
            }
            else {
                if (item.priority > 1) {
                    dismissLink = '<div class=\"dismissLink\"><span onclick=\"dismissMessage(\'' + itemId + '\',\'' + item.timestamp + '\')\">x</span></div>';
                }
                
                // test for cookie with this safe id as value
                if (!utils_readCookie('messageId' + itemId)) {
                    //no cookie - so new message, add to DOM
                    if (item.priority === 3) {
                        $PBJQ('#portalMessageContainer3').append('<div role=\"alert\" aria-live\"assertive\" aria-relevant=\"text\" id=\"' + itemId + '\" ts=\"' + item.timestamp + '\" class=\"portalMessage portalMessageShadow portalMessagePriority' + item.priority + '\"><div class=\"messageHolder\">' + item.message + '</div>' + dismissLink + '</div>');
                    }
                    else {
                        $PBJQ('#portalMessageContainer1_2').append('<div role=\"alert\" aria-live\"assertive\" aria-relevant=\"text\" id=\"' + itemId + '\" ts=\"' + item.timestamp + '\" class=\"portalMessage portalMessageShadow portalMessagePriority' + item.priority + '\"><div class=\"messageHolder\">' + item.message + '</div>' + dismissLink + '</div>');
                    }
                }
                else {
                    //message has been dismissed, but has been updated - so re-add to DOM
                     //  console.log(utils_readCookie('messageId' + itemId) +  "="  + item.timestamp);
                    if (utils_readCookie('messageId' + itemId) !== item.timestamp) {
                        if (item.priority === 3) {
                            $PBJQ('#portalMessageContainer3').append('<div role=\"alert\" aria-live\"assertive\" aria-relevant=\"text\" id=\"' + itemId + '\" ts=\"' + item.timestamp + '\" class=\"portalMessage portalMessageShadow portalMessagePriority' + item.priority + '\"><div class=\"messageHolder\">Update:&nbsp;&nbsp;' + item.message + '</div>' + dismissLink + '</div>');
                        }
                        else {
                            $PBJQ('#portalMessageContainer1_2').append('<div role=\"alert\" aria-live\"assertive\" aria-relevant=\"text\" id=\"' + itemId + '\" ts=\"' + item.timestamp + '\" class=\"portalMessage portalMessageShadow portalMessagePriority' + item.priority + '\"><div class=\"messageHolder\">Update:&nbsp;&nbsp;' + item.message + '</div>' + dismissLink + '</div>');
                        }
                    }
                }
            }
            // add meessage ID to a string, this should be an actual array instead of a string
            // should also be added only if it is not there (not needed as start from scratch each parse?)
            messageArray = messageArray + '_' + itemId;
        });
    }
    else {
        $PBJQ("#portalMessageContainer1_2").remove();
        $PBJQ("#portalMessageContainer3").remove();
    }
    
    $PBJQ(".portalMessage").each(function(i){
        //if the id of this elem is not in the message bundle just processed, remove it from the DOM 
        if (messageArray.indexOf($PBJQ(this).attr('id')) === -1) {
            $PBJQ(this).fadeOut(2000, function(){
                $PBJQ(this).remove();
            });
        }
    });
    $PBJQ(".portalMessage").removeClass('lastPortalMessage').removeClass('firstPortalMessage');
    $PBJQ("#portalMessageContainer1_2 .portalMessage:last").addClass('lastPortalMessage');
    $PBJQ("#portalMessageContainer3 .portalMessage:first").addClass('firstPortalMessage');
    
    function flashMessage(elem, message){
        $PBJQ(elem).removeClass('portalMessageShadow');
        $PBJQ(elem).fadeOut(2000, function(){
            $PBJQ(this).children('.messageHolder').empty();
            $PBJQ(this).children('.messageHolder').append('<div>' + message + '</div>');
            $PBJQ(this).fadeIn(2000, function(){
                $PBJQ(this).addClass('portalMessageShadow');
            });
        });
    }
}

function dismissMessage(target, timestamp){
    utils_createCookie('messageId' + target, timestamp);
    $PBJQ('#' + target).fadeOut('slow');
    $PBJQ('#' + target).remove();
    $PBJQ(".portalMessage").removeClass('lastPortalMessage').removeClass('firstPortalMessage');
    $PBJQ("#portalMessageContainer1_2 .portalMessage:last").addClass('lastPortalMessage');
    $PBJQ("#portalMessageContainer3 .portalMessage:first").addClass('firstPortalMessage');
}

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

var setupSkipNav = function(){
    //function called from site.vm to enable skip links for all browsers
     $PBJQ('#skipNav a.internalSkip').click(function(){
        var target = $PBJQ(this).attr('href');
        $PBJQ(target).attr('tabindex','-1').focus();
     });
};

//handles showing either the short url or the full url, depending on the state of the checkbox 
//(if configured, otherwise returns url as-is as according to the url shortening entity provder)
function toggleShortUrlOutput(defaultUrl, checkbox, textbox) {		
	
	if($PBJQ(checkbox).is(':checked')) {
		
		$PBJQ.ajax({
			url:'/direct/url/shorten?path='+encodeURI(defaultUrl),
			success: function(shortUrl) {
				$PBJQ('.'+textbox).val(shortUrl);
			}
		}); 
	} else {
		$PBJQ('.'+textbox).val(defaultUrl);
	}
}
