/**
 * For inline Chat in Morpheus: 
 */
/**
 * For <details> support in FF and IE
 */

$PBJQ('details').details();
/**
 * For Footer toggles in Morpheus
 */
/**
 * For More Sites in Morpheus
 */

var dhtml_view_sites = function(){

  // first time through set up the DOM
  $PBJQ('#selectSite').addClass('dhtml_more_tabs'); // move the selectSite in the DOM
  $PBJQ('.more-tab').position();

  // then recast the function to the post initialized state which will run from then on
  dhtml_view_sites = function(){

    if ($PBJQ('#selectSite').hasClass('outscreen') ) {

      $PBJQ('#selectSite').toggleClass('outscreen');

      /*$PBJQ('#selectSite').slideDown('fast', function(){

        // check if $PBJQ('#otherSiteList li').length > some number, then show search
        // otherwise not
        if ($PBJQ('#otherSitesCategorWrap').height() > 300) {
          $PBJQ('#otherSitesCategorWrap').height(300).css( {overflow:"auto"} );
        }

      });*/
      
      $PBJQ('#txtSearch').focus();
      createDHTMLMask(dhtml_view_sites);

      $PBJQ('.selectedTab').bind('click', function(e){
        dhtml_view_sites();
        return false;
      });

      $PBJQ('#selectSite a:first').focus();

      // If we hit escape or the up arrow on any of the links in the drawer, slide it
      // up and focus on the more tab.
      $PBJQ('#selectSite a').keydown(function (e) {

        if(e.keyCode == 38 || e.keyCode == 27) {
          e.preventDefault();
          closeDrawer();
        }

      });

      // Show the tool popup on the down arrow, or slide up the drawer on escape.
      $PBJQ('.moreSitesLink').keydown(function (e){

        if (e.keyCode == 40) {
          showToolMenu(e,0);
        }
 
      });

      // If we've tabbed backwards to the first element in the drawer, it could be the
      // search box or the all sites list, stop tabbing. This is a hack as we are
      // currently attaching keydown handlers to the list item text rather that the link
      // and you can only explicitly set focus to links and form elements.
      var txtSearch = $PBJQ('#txtSearch');

      if(txtSearch.length) {

        $PBJQ(txtSearch[0]).keydown(function (e) {

          if (e.keyCode == 9 && e.shiftKey) {
            e.preventDefault();
          }

        });

      } else {

        $PBJQ('#allSites').keydown(function (e) {

          if (e.keyCode == 9 && e.shiftKey) {
            e.preventDefault();
          }

        });
      }

      // If we tab off the right of the sites list, cycle the focus.
      $PBJQ('#otherSiteList > li:last').keydown(function (e) {

        if (e.keyCode == 9 && !e.shiftKey) {

          e.preventDefault();

          if (txtSearch.length) {

            txtSearch[0].focus();

          } else {

            $PBJQ('#allSites').focus();

          }

        }
      });
    }

    else {

      // hide the dropdown
      //$PBJQ('#selectSite div').hide();
      //$PBJQ('#selectSite').slideUp('fast'); // hide the box
      $PBJQ('#selectSite').toggleClass('outscreen'); //hide the box
      removeDHTMLMask()
      $PBJQ('#otherSiteTools').remove();
      $PBJQ('.selectedTab').unbind('click');
    }
  }

  // finally run the inner function, first time through
  dhtml_view_sites();
}

function closeDrawer() {

  $PBJQ('#selectSite div').hide();
  $PBJQ('#selectSite').slideUp('fast'); // hide the box
  removeDHTMLMask()
  $PBJQ('#otherSiteTools').remove();
  $PBJQ('.selectedTab').unbind('click');
  $PBJQ('.moreSitesLink').unbind('keydown');
  $PBJQ('.more-tab a').focus();

}

function createDHTMLMask(callback){

  $PBJQ('body').append('<div id="portalMask">&nbsp;</div>');

  $PBJQ('#portalMask').css('height', browserSafeDocHeight()).css({
    'width': '100%',
    'z-index': 1000,
    'top': 0,
    'left': 0
  }).bind("click", function(event){
    callback();
    return false;
  });

  $PBJQ('#portalMask').bgiframe();
}

function removeDHTMLMask(){
  $PBJQ('#portalMask').remove();
}

/** Shows a drawer site tool dropdown **/
function showToolMenu(e, xOffset){
  e.preventDefault();
  var jqObj = $PBJQ(e.target);
  var classId = jqObj.attr('id');
  // We need to escape special chars, like exclamations, or else $PBJQ selectors don't work.
  var id = classId.replace(/!/g,'\\!').replace(/~/g,'\\~');
  $PBJQ('.toolMenus').removeClass('toolMenusActive');

  if ($PBJQ('.' + id).length) {
    $PBJQ('#otherSiteTools').remove();
  }

  else {
    $PBJQ('#otherSiteTools').remove();
    var subsubmenu = "<ul id=\"otherSiteTools\" class=\"" + classId + "\" role=\"menu\">";
    var siteURL = '/direct/site/' + classId + '/pages.json';
    scroll(0, 0)
    var pos = jqObj.offset();
    var maxToolsInt = parseInt($PBJQ('#maxToolsInt').text());
    var maxToolsText = $PBJQ('#maxToolsAnchor').text();
    var goToSite = '<li class=\"otherSiteTool\"><span><a role=\"menuitem\" href=\"' + portal.portalPath + '/site/' + classId + '\" title=\"' + maxToolsText + '\"><span class=\"toolMenuIcon icon-sakai-see-all-tools\"> </span>' + maxToolsText + '</a></span></li>';
    $PBJQ.getJSON(siteURL, function(data){
      $PBJQ.each(data, function(i, item){

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

      $PBJQ('#portalOuterContainer').append(subsubmenu);
      $PBJQ('#otherSiteTools').css({
        'top': pos.top + 28,
        'left': pos.left - xOffset
      });

      $PBJQ('#otherSiteTools li a:first').focus();
      jqObj.parent().find('.toolMenus').addClass("toolMenusActive");
      // On up arrow or escape, hide the popup
      $PBJQ('#otherSiteTools').keydown(function(e){

        if (e.keyCode == 27) {

          e.preventDefault();
          jqObj.focus();
          $PBJQ(this).remove();
          $PBJQ('.' + id).remove();
          jqObj.parent().find('.toolMenus').removeClass("toolMenusActive");

        }

      });
      
      addArrowNavAndDisableTabNav($PBJQ('#otherSiteTools'), function () {
        jqObj.focus();
        $PBJQ('.' + id).remove();
        // Switch the arrows
        jqObj.parent().find('.toolMenus').removeClass("toolMenusActive");
      });
    }); // end json call
  }
}

$PBJQ(document).ready(function(){

  if ($PBJQ('#eid').length === 1) {
    $PBJQ('#eid').focus()
  }

  // SAK-22026. Attach down and up arrow handlers to the more sites tab.
  $PBJQ('.more-tab a').keydown(function (e) {

    if (e.keyCode == 40 || e.keyCode == 38 || e.keyCode == 27) {
      e.preventDefault();
      return dhtml_view_sites();
    }

  });

  // open tool menus in "other sites" panel
  $PBJQ('.toolMenus').click(function(e){
    showToolMenu(e,173);
  });

  // prepend site title to tool title
  // here as reminder to work on an actual breadcrumb integrated with neo style tool updates
  //var siteTitle = ($PBJQ('.nav-selected span:first').text())
  var siteTitle = portal.siteTitle;

  if (siteTitle) {

    if (portal.shortDescription) {
      siteTitle = siteTitle + " ("+portal.shortDescription+")"
    }

    $PBJQ('.portletTitle h2').prepend('<span class=\"siteTitle\">' + siteTitle + ':</span> ')
  }

  // other site search handlers
  $PBJQ('#imgSearch').click(function(){
    resetSearch();
  });

  $PBJQ('#txtSearch').keyup(function(event){

    if (event.keyCode == 27) {
      resetSearch();
    }

    if ($PBJQ('#txtSearch').val().length > 0) {
      $PBJQ('#otherSiteList li, .otherSitesCategorList li').hide();
      $PBJQ('#otherSitesCategorWrap h4').hide();
      $PBJQ('#otherSiteList li a span.fullTitle:Contains(\'' + $PBJQ('#txtSearch').val() + '\')').parent('a').parent('li').show();
      $PBJQ('.otherSitesCategorList li a span.fullTitle:Contains(\'' + $PBJQ('#txtSearch').val() + '\')').parent('a').parent('li').show().closest('ul').prev('h4').show();
      $PBJQ('#imgSearch').fadeIn('slow');
    }

    if ($PBJQ('#txtSearch').val().length == 0) {
      resetSearch();
    }

    // Should be <=1 if there is a header line
    if ($PBJQ('#otherSiteList li:visible').length < 1 && $PBJQ('.otherSitesCategorList li:visible').length < 1) {
      $PBJQ('.norecords').remove();
      $PBJQ('#otherSiteSearch #noSearchResults').fadeIn('slow');
    }
  });

  // case insensitive version of :contains
  $PBJQ.expr[':'].Contains = function(a, i, m){
    return $PBJQ(a).text().toUpperCase().indexOf(m[3].toUpperCase()) >= 0;
  };

  function resetSearch(){
    $PBJQ('#txtSearch').val('');
    $PBJQ('#otherSiteList li').show();
    $PBJQ('.otherSitesCategorList li').show();
    $PBJQ('#otherSitesCategorWrap h4').show();
    $PBJQ('#noSearchResults').fadeOut();
    $PBJQ('#imgSearch').fadeOut();
    $PBJQ('#txtSearch').focus();
  }

  //toggle presence panel
  $PBJQ("#presenceToggle").click(function(e){
    e.preventDefault();
    $PBJQ('#presenceArea').toggle();
  });

  //explicitly close presence panel
  $PBJQ('.trayPopupClose').click(function(e){
    e.preventDefault();
    $PBJQ(this).closest('.trayPopup').hide();
  });

  //bind directurl checkboxes
  if ( $PBJQ('a.tool-directurl').length ) $PBJQ('a.tool-directurl').cluetip({
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
  $PBJQ("#toggleSubsitesLink").click(function (e) {
    var subsitesLink = $PBJQ(this);
    if($PBJQ('#subSites').css('display') == 'block') {
      $PBJQ('#subSites').hide();
    } else {
      var position = subsitesLink.position();
      $PBJQ('#subSites').css({'position': 'absolute','display': 'block','left': position.left + subsitesLink.width() + 14 + 'px','top': position.top + 'px'});
    }
  });

});
/* dhtml_view_sites
 * displays the More Sites div
 * note the technique of recasting the function after initalization
 */
// var dhtml_view_sites = function(){
//     // first time through set up the DOM
//     $PBJQ('#selectSite').appendTo('#linkNav').addClass('dhtml_more_tabs'); // move the selectSite in the DOM
//     $PBJQ('.more-tab').position();
//
//     // then recast the function to the post initialized state which will run from then on
//     dhtml_view_sites = function(){
//         if ($PBJQ('#selectSite').css('display') == 'none') {
//             $PBJQ('#selectSite div').show();
//             $PBJQ('#selectSite').slideDown('fast', function(){
//                 // check if $PBJQ('#otherSiteList li').length > some number, then show search
//                 // otherwise not
//                    if($PBJQ('#otherSitesCategorWrap').height() > 300){
//                     $PBJQ('#otherSitesCategorWrap').height(300).css({overflow:"auto"});
//                 }
//
//
//                 $PBJQ('#txtSearch').focus();
//             });
//             createDHTMLMask(dhtml_view_sites);
//             $PBJQ('.selectedTab').bind('click', function(e){
//                 dhtml_view_sites();
//                 return false;
//             });
//
//             $PBJQ('#selectSite a:first').focus();
//
//             // If we hit escape or the up arrow on any of the links in the drawer, slide it
//             // up and focus on the more tab.
//             $PBJQ('#selectSite a').keydown(function (e) {
//                 if(e.keyCode == 38 || e.keyCode == 27) {
//                     e.preventDefault();
//                     closeDrawer();
//                 }
//             });
//
//             // Show the tool popup on the down arrow, or slide up the drawer on escape.
//             $PBJQ('.moreSitesLink').keydown(function (e){
//                 if (e.keyCode == 40) {
//                     showToolMenu(e,0);
//                 }
//             });
//
//             // If we've tabbed backwards to the first element in the drawer, it could be the
//             // search box or the all sites list, stop tabbing. This is a hack as we are
//             // currently attaching keydown handlers to the list item text rather that the link
//             // and you can only explicitly set focus to links and form elements.
//             var txtSearch = $PBJQ('#txtSearch');
//             if(txtSearch.length) {
//                 $PBJQ(txtSearch[0]).keydown(function (e) {
//                     if (e.keyCode == 9 && e.shiftKey) {
//                         e.preventDefault();
//                     }
//                 });
//             } else {
//                 $PBJQ('#allSites').keydown(function (e) {
//                     if (e.keyCode == 9 && e.shiftKey) {
//                         e.preventDefault();
//                     }
//                 });
//             }
//
//             // If we tab off the right of the sites list, cycle the focus.
//             $PBJQ('#otherSiteList > li:last').keydown(function (e) {
//                 if (e.keyCode == 9 && !e.shiftKey) {
//                     e.preventDefault();
//                     if(txtSearch.length) {
//                         txtSearch[0].focus();
//                     } else {
//                         $PBJQ('#allSites').focus();
//                     }
//                 }
//             });
//         }
//         else {
//             // hide the dropdown
//             $PBJQ('#selectSite div').hide();
//             $PBJQ('#selectSite').slideUp('fast'); // hide the box
//             removeDHTMLMask()
//             $PBJQ('#otherSiteTools').remove();
//             $PBJQ('.selectedTab').unbind('click');
//         }
//     }
//     // finally run the inner function, first time through
//     dhtml_view_sites();
// }
//
// function closeDrawer() {
//     $PBJQ('#selectSite div').hide();
//     $PBJQ('#selectSite').slideUp('fast'); // hide the box
//     removeDHTMLMask()
//     $PBJQ('#otherSiteTools').remove();
//     $PBJQ('.selectedTab').unbind('click');
//     $PBJQ('.moreSitesLink').unbind('keydown');
//     $PBJQ('.more-tab a').focus();
// }
//
// function createDHTMLMask(callback){
//
//     $PBJQ('body').append('<div id="portalMask">&nbsp;</div>');
//     $PBJQ('#portalMask').css('height', browserSafeDocHeight()).css({
//         'width': '100%',
//         'z-index': 1000,
//         'top': 0,
//         'left': 0
//     }).bind("click", function(event){
//         callback();
//         return false;
//     });
//     $PBJQ('#portalMask').bgiframe();
// }
//
// function removeDHTMLMask(){
//     $PBJQ('#portalMask').remove();
// }

// //For SAK-13987
// //For SAK-16162
// //Just use the EB current.json as the session id rather than trying to do a search/replace
// var sessionId = "current";
// var sessionTimeOut;
// var timeoutDialogEnabled = false;
// var timeoutDialogWarningTime;
// var timeoutLoggedoutUrl;
// var timeoutPortalPath;
// $PBJQ(document).ready(function(){
//     // note a session exists whether the user is logged in or no
//     if (portal.loggedIn && portal.timeoutDialog) {
//         setTimeout('setup_timeout_config();', 60000);
//     }
// });
//
// var setup_timeout_config = function(){
//     timeoutDialogEnabled = portal.timeoutDialog.enabled;
//     timeoutDialogWarningTime = portal.timeoutDialog.seconds;
//     timeoutLoggedoutUrl = portal.loggedOutUrl;
//     timeoutPortalPath = portal.portalPath;
//     if (timeoutDialogEnabled == true) {
//         poll_session_data();
//         fetch_timeout_dialog();
//     }
// }
//
// var poll_session_data = function(){
//     //Need to append Date.getTime as sakai still uses jquery pre 1.2.1 which doesn't support the cache: false parameter.
//     $PBJQ.ajax({
//         url: "/direct/session/" + sessionId + ".json?auto=true&_=" + (new Date()).getTime(), //auto=true makes it not refresh the session lastaccessedtime
//         dataType: "json",
//         success: function(data){
//             //get the maxInactiveInterval in the same ms
//             data.maxInactiveInterval = data.maxInactiveInterval * 1000;
//             if (data.active && data.userId != null &&
//             data.lastAccessedTime + data.maxInactiveInterval >
//             data.currentTime) {
//                 //User is logged in, so now determine how much time is left
//                 var remaining = data.lastAccessedTime + data.maxInactiveInterval - data.currentTime;
//                 //If time remaining is less than timeoutDialogWarningTime minutes, show/update dialog box
//                 if (remaining < timeoutDialogWarningTime * 1000) {
//                     //we are within 5 min now - show popup
//                     min = Math.round(remaining / (1000 * 60));
//                     show_timeout_alert(min);
//                     clearTimeout(sessionTimeOut);
//                     sessionTimeOut = setTimeout("poll_session_data()", 1000 * 60);
//                 }
//                 else {
//                     //more than timeoutDialogWarningTime min away
//                     clearTimeout(sessionTimeOut);
//                     sessionTimeOut = setTimeout("poll_session_data()", (remaining - timeoutDialogWarningTime * 1000));
//                 }
//             }
//             else
//                 if (data.userId == null) {
//                     // if data.userId is null, the session is done; redirect the user to logoutUrl
//                     location.href = timeoutLoggedoutUrl;
//
//                 }
//                 else {
//                     //the timeout length has occurred, but there is a slight delay, do this until there isn't a user.
//                     sessionTimeOut = setTimeout("poll_session_data()", 1000 * 10);
//                 }
//         },
//         error: function(XMLHttpRequest, status, error){
//             // We used to to 404 handling here but now we should always get good session data.
//         }
//     });
// }
//
// function keep_session_alive(){
//     dismiss_session_alert();
//     $PBJQ.get(timeoutPortalPath);
// }
//
// var dismiss_session_alert = function(){
//     removeDHTMLMask();
//     $PBJQ("#timeout_alert_body").remove();
// }
//
// var timeoutDialogFragment;
// function fetch_timeout_dialog(){
//     $PBJQ.ajax({
//         url: "/portal/timeout?auto=true",
//         cache: true,
//         dataType: "text",
//         success: function(data){
//             timeoutDialogFragment = data;
//         },
//         error: function(XMLHttpRequest, status, error){
//             timeoutDialogEnabled = false;
//         }
//     });
// }
//
// function show_timeout_alert(min){
//     if (!timeoutDialogEnabled) {
//         return;
//     }
//
//     if (!$PBJQ("#portalMask").get(0)) {
//         createDHTMLMask(dismiss_session_alert);
//         $PBJQ("#portalMask").css("z-index", 10000);
//     }
//     if ($PBJQ("#timeout_alert_body").get(0)) {
//         //its there, just update the min
//         $PBJQ("#timeout_alert_body span").html(min);
//         $PBJQ("#timeout_alert_body span").css('top', (f_scrollTop() + 100) + "px");
//     }
//     else {
//         var dialog = timeoutDialogFragment.replace("{0}", min);
//         $PBJQ("body").append(dialog);
//     }
// }

// // The official way for a tool to request minimized navigation
// function portalMaximizeTool(){
//     if (!(portal.toggle.sitenav || portal.toggle.tools))
//         return;
//     if (!portal.toggle.allowauto)
//         return;
//     sakaiMinimizeNavigation();
// }

// function sakaiMinimizeNavigation(){
//     if (!(portal.toggle.sitenav || portal.toggle.tools))
//         return;
//     if (portal.toggle.sitenav) {
//         $PBJQ('#portalContainer').addClass('minimize-site-nav')
//     }
//     if (portal.toggle.tools) {
//         $PBJQ('.nav-tools').addClass('is-minimized');
//     }
//     $PBJQ('.js-nav-toggle__icon--max').addClass('is-hidden').removeClass('is-visible');
//     $PBJQ('#toggleToolMenu').attr('title',$PBJQ('#toggleNormal em').text());
//     $PBJQ('.js-nav-toggle__icon--normal').addClass('is-visible').removeClass('is-hidden');
// }
//
// function sakaiRestoreNavigation(){
//     if (!(portal.toggle.sitenav || portal.toggle.tools))
//         return;
//     if (portal.toggle.sitenav) {
//         $PBJQ('#portalContainer').removeClass('minimize-site-nav')
//     }
//     if (portal.toggle.tools) {
//         $PBJQ('#toolMenuWrap').removeClass('minimize-tool-nav');
//     }
//     $PBJQ('.js-nav-toggle__icon--max').addClass('is-visible').removeClass('is-hidden');
//
//
//     $PBJQ('js-nav-toggle__icon--normal').addClass('is-hidden').removeClass('is-visible');
// }

// function updatePresence(){
//     $PBJQ.ajax({
//         url: sakaiPresenceFragment,
//         cache: false,
//         success: function(frag){
//             var whereul = frag.indexOf('<ul');
//             if (whereul < 1) {
//                 $PBJQ("#presenceCount").html(' ');
//                 $PBJQ('#presenceCount').removeClass('present').addClass('empty');
//                 location.reload();
//                 return;
//             }
//             frag = frag.substr(whereul);
//             var _s = frag;
//             var _m = '<li'; // needle
//             var _c = 0;
//             for (var i = 0; i < _s.length; i++) {
//                 if (_m == _s.substr(i, _m.length))
//                     _c++;
//             }
//             // No need to attrct attention you are alone
//             if (_c > 1) {
//                 $PBJQ("#presenceCount").html(_c + '');
//                 $PBJQ('#presenceCount').removeClass('empty').addClass('present');
//             }
//             else
//                 if (_c == 1) {
//                     $PBJQ("#presenceCount").html(_c + '');
//                     $PBJQ('#presenceCount').removeClass('present').addClass('empty');
//                 }
//                 else {
//                     $PBJQ("#presenceCount").html(' ');
//                     $PBJQ('#presenceCount').removeClass('present').addClass('empty');
//                 }
//             $PBJQ("#presenceIframe").html(frag);
//             var chatUrl = $PBJQ('.nav-selected .icon-sakai-chat').attr('href');
//             $PBJQ('#presenceIframe .presenceList li.inChat span').wrap('<a href="' + chatUrl + '"></a>')
//             sakaiLastPresenceTimeOut = setTimeout('updatePresence()', 30000);
//         },
//         // If we get an error, wait 60 seconds before retry
//         error: function(request, strError){
//             sakaiLastPresenceTimeOut = setTimeout('updatePresence()', 60000);
//         }
//     });
// }

// function f_scrollTop(){
//     return f_filterResults(window.pageYOffset ? window.pageYOffset : 0, document.documentElement ? document.documentElement.scrollTop : 0, document.body ? document.body.scrollTop : 0);
// }
//
// function f_filterResults(n_win, n_docel, n_body){
//     var n_result = n_win ? n_win : 0;
//     if (n_docel && (!n_result || (n_result > n_docel)))
//         n_result = n_docel;
//     return n_body && (!n_result || (n_result > n_body)) ? n_body : n_result;
// }

// /** Shows a drawer site tool dropdown **/
// function showToolMenu(e, xOffset){
//     e.preventDefault();
//     var jqObj = $PBJQ(e.target);
//     var classId = jqObj.attr('id');
//     // We need to escape special chars, like exclamations, or else $PBJQ selectors don't work.
//     var id = classId.replace(/!/g,'\\!').replace(/~/g,'\\~');
//     $PBJQ('.toolMenus').removeClass('toolMenusActive');
//     if ($PBJQ('.' + id).length) {
//         $PBJQ('#otherSiteTools').remove();
//     }
//     else {
//         $PBJQ('#otherSiteTools').remove();
//         var subsubmenu = "<ul id=\"otherSiteTools\" class=\"" + classId + "\" role=\"menu\">";
//         var siteURL = '/direct/site/' + classId + '/pages.json';
//         scroll(0, 0)
//         var pos = jqObj.offset();
//         var maxToolsInt = parseInt($PBJQ('#maxToolsInt').text());
//         var maxToolsText = $PBJQ('#maxToolsAnchor').text();
//         var goToSite = '<li class=\"otherSiteTool\"><span><a role=\"menuitem\" href=\"' + portal.portalPath + '/site/' + classId + '\" title=\"' + maxToolsText + '\"><span class=\"toolMenuIcon icon-sakai-see-all-tools\"> </span>' + maxToolsText + '</a></span></li>';
//         $PBJQ.getJSON(siteURL, function(data){
//             $PBJQ.each(data, function(i, item){
//                 if (i <= maxToolsInt) {
//                     if (item.toolpopup) {
//                         subsubmenu = subsubmenu + '<li class=\"otherSiteTool\"><span><a role=\"menuitem\"  href=\"' + item.tools[0].url + "?sakai.popup=yes\" title=\"" + item.title + "\" onclick=\"window.open('" + item.toolpopupurl + "');\"><span class=\"toolMenuIcon icon-" + item.tools[0].toolId.replace(/\./gi, '-') + "\"> </span>" + item.title + "</a></span></li>";
//                     } else if (item.tools.length === 1) {
//                         subsubmenu = subsubmenu + '<li class=\"otherSiteTool\"><span><a role=\"menuitem\"  href=\"' + item.tools[0].url + "\" title=\"" + item.title + "\"><span class=\"toolMenuIcon icon-" + item.tools[0].toolId.replace(/\./gi, '-') + "\"> </span>" + item.title + "</a></span></li>";
//                     }
//                 }
//
//             });
//             if ((data.length - 1) > maxToolsInt) {
//                 subsubmenu = subsubmenu + goToSite
//             }
//             subsubmenu = subsubmenu + "</ul>"
//             $PBJQ('#portalOuterContainer').append(subsubmenu);
//             $PBJQ('#otherSiteTools').css({
//                 'top': pos.top + 28,
//                 'left': pos.left - xOffset
//             });
//             $PBJQ('#otherSiteTools li a:first').focus();
//             jqObj.parent().find('.toolMenus').addClass("toolMenusActive");
//             // On up arrow or escape, hide the popup
//             $PBJQ('#otherSiteTools').keydown(function(e){
//                 if (e.keyCode == 27) {
//                     e.preventDefault();
//                     jqObj.focus();
//                     $PBJQ(this).remove();
//                     $PBJQ('.' + id).remove();
//                     jqObj.parent().find('.toolMenus').removeClass("toolMenusActive");
//                 }
//             });
//
//             addArrowNavAndDisableTabNav($PBJQ('#otherSiteTools'), function () {
//                 jqObj.focus();
//                 $PBJQ('.' + id).remove();
//                 // Switch the arrows
//                 jqObj.parent().find('.toolMenus').removeClass("toolMenusActive");
//             });
//         }); // end json call
//     }
// }

// $PBJQ(document).ready(function(){
//     if ($PBJQ('#eid').length === 1) {
//         $PBJQ('#eid').focus()
//     }
//
//     // SAK-22026. Attach down and up arrow handlers to the more sites tab.
//     $PBJQ('.more-tab a').keydown(function (e) {
//         if (e.keyCode == 40 || e.keyCode == 38 || e.keyCode == 27) {
//             e.preventDefault();
//             return dhtml_view_sites();
//         }
//     });
//
//     // open tool menus in "other sites" panel
//     $PBJQ('.toolMenus').click(function(e){
//         showToolMenu(e,173);
//     });
//
//     // prepend site title to tool title
//     // here as reminder to work on an actual breadcrumb integrated with neo style tool updates
//     //var siteTitle = ($PBJQ('.nav-selected span:first').text())
//     var siteTitle = portal.siteTitle;
//     if (siteTitle) {
//   if (portal.shortDescription) {
//       siteTitle = siteTitle + " ("+portal.shortDescription+")"
//   }
//         $PBJQ('.portletTitle h2').prepend('<span class=\"siteTitle\">' + siteTitle + ':</span> ')
//     }
//
//     // other site search handlers
//     $PBJQ('#imgSearch').click(function(){
//         resetSearch();
//     });
//
//     $PBJQ('#txtSearch').keyup(function(event){
//         if (event.keyCode == 27) {
//             resetSearch();
//         }
//         if ($PBJQ('#txtSearch').val().length > 0) {
//             $PBJQ('#otherSiteList li, .otherSitesCategorList li').hide();
//             $PBJQ('#otherSitesCategorWrap h4').hide();
//             $PBJQ('#otherSiteList li a span.fullTitle:Contains(\'' + $PBJQ('#txtSearch').val() + '\')').parent('a').parent('li').show();
//             $PBJQ('.otherSitesCategorList li a span.fullTitle:Contains(\'' + $PBJQ('#txtSearch').val() + '\')').parent('a').parent('li').show().closest('ul').prev('h4').show();
//             $PBJQ('#imgSearch').fadeIn('slow');
//         }
//         if ($PBJQ('#txtSearch').val().length == 0) {
//             resetSearch();
//         }
//         // Should be <=1 if there is a header line
//         if ($PBJQ('#otherSiteList li:visible').length < 1 && $PBJQ('.otherSitesCategorList li:visible').length < 1) {
//             $PBJQ('.norecords').remove();
//             $PBJQ('#otherSiteSearch #noSearchResults').fadeIn('slow');
//         }
//     });
//     // case insensitive version of :contains
//     $PBJQ.expr[':'].Contains = function(a, i, m){
//         return $PBJQ(a).text().toUpperCase().indexOf(m[3].toUpperCase()) >= 0;
//     };
//     function resetSearch(){
//         $PBJQ('#txtSearch').val('');
//         $PBJQ('#otherSiteList li').show();
//         $PBJQ('.otherSitesCategorList li').show();
//         $PBJQ('#otherSitesCategorWrap h4').show();
//         $PBJQ('#noSearchResults').fadeOut();
//         $PBJQ('#imgSearch').fadeOut();
//         $PBJQ('#txtSearch').focus();
//     }
//
//     //toggle presence panel
//     $PBJQ("#presenceToggle").click(function(e){
//         e.preventDefault();
//         $PBJQ('#presenceArea').toggle();
//     });
//     //explicitly close presence panel
//     $PBJQ('.trayPopupClose').click(function(e){
//         e.preventDefault();
//         $PBJQ(this).closest('.trayPopup').hide();
//     });
//
//     //bind directurl checkboxes
//     if ( $PBJQ('a.tool-directurl').length ) $PBJQ('a.tool-directurl').cluetip({
//       local: true,
//       arrows: true,
//     cluetipClass: 'jtip',
//     sticky: true,
//     cursor: 'pointer',
//     activation: 'click',
//     closePosition: 'title',
//     closeText: '<img src="/library/image/silk/cross.png" alt="close">'
//     });
//
//   // Shows or hides the subsites in a popout div. This isn't used unless
//   // portal.showSubsitesAsFlyout is set to true in sakai.properties.
//   $PBJQ("#toggleSubsitesLink").click(function (e) {
//         var subsitesLink = $PBJQ(this);
//         if($PBJQ('#subSites').css('display') == 'block') {
//             $PBJQ('#subSites').hide();
//         } else {
//             var position = subsitesLink.position();
//             $PBJQ('#subSites').css({'position': 'absolute','display': 'block','left': position.left + subsitesLink.width() + 14 + 'px','top': position.top + 'px'});
//         }
//     });
//
// });

// var setupSiteNav = function(){
//     $PBJQ("ul.subnav").each(function(){
//         // Add an escape key handler to slide the page menu up
//         $PBJQ(this).keydown(function(e) {
//             if (e.keyCode == 27) {
//                 $PBJQ(this).parent().children('a').focus();
//                 $PBJQ(this).slideUp('fast');
//             }
//         });
//         $PBJQ(this).children('li:last').addClass('lastMenuItem')
//     });
//
//     $PBJQ("ul.topnav > li").mouseleave(function(){
//         $PBJQ(this).find('ul').slideUp('fast')
//     });
//
//     $PBJQ("#loginLinks ul.nav-submenu").mouseleave(function(){
//         $PBJQ(this).slideUp('fast')
//     });
//
//     $PBJQ("#loginLinks span.drop").click(function(e){
//         $PBJQ(this).prev('ul').slideDown('fast')
//      });
//
//   fixTopNav = function(e){
//         if (e.keyCode == 40) { // downarrow
//             e.preventDefault();
//             $PBJQ('#selectSite').hide();
//             $PBJQ('.nav-submenu').hide();
//             // Trigger click on the drop <span>, passing true to set focus on
//             // the first tool in the dropdown.
//             $PBJQ(this).parent().find(".drop").trigger('click',[true]);
//         } else if (e.keyCode == 27) { // uparrow
//             $PBJQ(this).parent().children('a').focus();
//             $PBJQ(this).slideUp('fast');
//         }
//     }
//
//   // SAK-25505 - Switch from live() to on()
//   // $PBJQ( "a.offsite" ).live( "click", function() {
//   // $PBJQ('.topnav > li.nav-menu > a').live('keydown', function(e){
//   if ( $PBJQ(document).on ) {
//     $PBJQ(document).on('keydown', '.topnav > li.nav-menu > a', fixTopNav);
//   } else {
//     $PBJQ('.topnav > li.nav-menu > a').live('keydown', fixTopNav);
//   }
//
//     $PBJQ("ul.topnav > li").mouseleave(function(){
//         $PBJQ(this).find('ul').slideUp('fast')
//     });
//     // focusFirstLink is only ever passed from the keydown handler. We
//     // don't want to focus on click; it looks odd.
//     $PBJQ("ul.topnav li span.drop").click(function(e, focusFirstLink){
//         /*
//          * see if there is a menu sibling
//          *      if there is a child, display it
//          *  if no menu sibling
//          *       retrieve data, construct the menu, append
//          */
//         $PBJQ(this).toggleClass("subclicked"); //On click toggle class "subclicked"
//         e.preventDefault()
//         var jqObjDrop = $PBJQ(e.target);
//         if (jqObjDrop.parent('li').find('ul').length) {
//             jqObjDrop.parent('li').find('ul').slideDown('fast')
//             if(focusFirstLink) {
//                 jqObjDrop.parent().find("ul.subnav a:first").focus();
//             }
//         }
//         else {
//             var navsubmenu = "<ul class=\"nav-submenu subnav\" role=\"menu\" class=\"show\">";
//             var siteId = jqObjDrop.attr('data');
//             var maxToolsInt = parseInt($PBJQ('#maxToolsInt').text());
//             var maxToolsText = $PBJQ('#maxToolsAnchor').text();
//             var goToSite = '<li class=\"submenuitem\"><a role=\"menuitem\" href=\"' + portal.portalPath + '/site/' + siteId + '\" title=\"' + maxToolsText + '\"><span class=\"toolMenuIcon icon-sakai-see-all-tools\"></span>' + maxToolsText + '</a></li>';
//             var siteURL = '/direct/site/' + jqObjDrop.attr('data') + '/pages.json';
//             $PBJQ.ajax({
//                 url: siteURL,
//                 dataType: "json",
//                 success: function(data){
//                     $PBJQ.each(data, function(i, item){
//                         if (i <= maxToolsInt) {
//                             if (item.toolpopup) {
//                                 navsubmenu = navsubmenu + '<li class=\"submenuitem\" ><span><a role=\"menuitem\" href=\"' + item.tools[0].url + "?sakai.popup=yes\" title=\"" + item.title + "\" onclick=\"window.open('" + item.toolpopupurl + "');\"><span class=\"toolMenuIcon icon-" + item.tools[0].toolId.replace(/\./gi, '-') + "\"></span>" + item.title + "</a></span></li>";
//                             } else if (item.tools.length === 1) {
//                                 navsubmenu = navsubmenu + '<li class=\"submenuitem\" ><span><a role=\"menuitem\" href=\"' + item.tools[0].url + "\" title=\"" + item.title + "\"><span class=\"toolMenuIcon icon-" + item.tools[0].toolId.replace(/\./gi, '-') + "\"></span>" + item.title + "</a></span></li>";
//                             }
//                         }
//                     });
//                     if ((data.length - 1) > maxToolsInt) {
//                         navsubmenu = navsubmenu + goToSite
//                     }
//                     navsubmenu = navsubmenu + "</ul>"
//                     jqObjDrop.after(navsubmenu);
//                     if(focusFirstLink) {
//                         jqObjDrop.parent().find("ul.subnav a:first").focus();
//                     }
//                     addArrowNavAndDisableTabNav($PBJQ("ul.subnav"));
//                 },
//                 error: function(XMLHttpRequest, status, error){
//                     // Something happened getting the tool list.
//                 }
//             });
//         }
//     }).hover(function(){
//         $PBJQ(this).addClass("subhover"); //On hover over, add class "subhover"
//     }, function(){ //On Hover Out
//         $PBJQ(this).removeClass("subhover"); //On hover out, remove class "subhover"
//     });
//
// }

// var setupToolToggle = function(toggleClass){
//     $PBJQ('#toggler').prependTo('#toolMenuWrap');
//     $PBJQ('#toggler').css({
//         'display': 'inline'
//     });
//     $PBJQ('#toggler').addClass(toggleClass)
//
//   $PBJQ('#toggleToolMenu').hover(function () {
//          $PBJQ(this).find('span').addClass('toggleToolMenuHover')
//   },
//       function () {
//          $PBJQ(this).find('span').removeClass('toggleToolMenuHover')
//       }
//   );
//     $PBJQ('#toggleToolMenu').click(function(){
//         if ($PBJQ('#toggleNormal').is(':visible')) {
//             sakaiRestoreNavigation();
//             document.cookie = "sakai_nav_minimized=false; path=/";
//         }
//         else {
//             sakaiMinimizeNavigation();
//             document.cookie = "sakai_nav_minimized=true; path=/";
//         }
//     });
// }

// function publishSite(siteId) {
//     var reqUrl = '/direct/site/'+siteId+"/edit";
//     var resp = $PBJQ.ajax({
//       type: 'POST',
//       data: 'published=true',
//       url: reqUrl,
//       success: function() { location.reload(); }
//     }).responseText;
// }

// var setupSkipNav = function(){
//     // function called from site.vm to enable skip links for all browsers
//      $PBJQ('#skipNav a.internalSkip').click(function(){
//          var target = $PBJQ(this).attr('href');
//         $PBJQ(target).attr('tabindex','-1').focus();
//      });
// };

// //handles showing either the short url or the full url, depending on the state of the checkbox
// //(if configured, otherwise returns url as-is as according to the url shortening entity provder)
// function toggleShortUrlOutput(defaultUrl, checkbox, textbox) {
//
//   if($PBJQ(checkbox).is(':checked')) {
//
//     $PBJQ.ajax({
//       url:'/direct/url/shorten?path='+encodeURI(defaultUrl),
//       success: function(shortUrl) {
//         $PBJQ('.'+textbox).val(shortUrl);
//       }
//     });
//   } else {
//     $PBJQ('.'+textbox).val(defaultUrl);
//   }
// }

// /* Callback is a function and is called after sliding up ul */
// function addArrowNavAndDisableTabNav(ul,callback) {
//     ul.find('li a').attr('tabindex','-1').keydown(function (e) {
//         var obj = $PBJQ(e.target);
//         if(e.keyCode == 40) {
//             e.preventDefault();
//             var next = obj.parent().parent().next();
//             if(next[0] === undefined) {
//                 ul.slideUp('fast');
//                 if(callback !== undefined) {
//                     callback();
//                 } else {
//                     obj.parent().parent().parent().parent().children('a').focus();
//                 }
//             } else {
//                 next.find('a').focus();
//             }
//         } else if(e.keyCode == 9) { // Suck up the menu if tab is pressed
//             ul.slideUp('fast');
//         } else if(e.keyCode == 38) {
//             // Up arrow
//             e.preventDefault();
//             var prev = obj.parent().parent().prev();
//             if(prev[0] === undefined) {
//                 ul.slideUp('fast');
//                 if(callback !== undefined) {
//                     callback();
//                 } else {
//                     obj.parent().parent().parent().parent().children('a').focus();
//                 }
//             } else {
//                 prev.find('a').focus();
//             }
//         }
//     });
// }

/**
 * For Publishing sites in Morpheus
 */

function publishSite(siteId) { 

  var reqUrl = '/direct/site/'+siteId+"/edit"; 
  var resp = $PBJQ.ajax({ 
    type: 'POST', 
    data: 'published=true', 
    url: reqUrl, 
    success: function() { location.reload(); } 
  }).responseText; 

}

/**
 * For Responsive Menus in Morpheus: Adds classes to the <body>
 */

function toggleToolsNav(){

  event.preventDefault();
  $PBJQ('body').toggleClass('toolsNav--displayed');

}

function toggleSitesNav(){

  event.preventDefault();
  $PBJQ('body').toggleClass('sitesNav--displayed');
  // remove class if siteNav submenus are activated
  $PBJQ('#linkNav .Mrphs-sitesNav__drop').removeClass('is-clicked');
  $PBJQ('#linkNav .Mrphs-sitesNav__submenu').removeClass('is-visible');

}

$PBJQ(".js-toggle-sites-nav", "#skipNav").on("click", toggleSitesNav);
$PBJQ(".js-toggle-tools-nav", "#skipNav").on("click", toggleToolsNav);

/**
 * For Session and Timeouts in Morpheus
 */

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

  $PBJQ.ajax({
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

        } else {

          //more than timeoutDialogWarningTime min away
          clearTimeout(sessionTimeOut);
          sessionTimeOut = setTimeout("poll_session_data()", (remaining - timeoutDialogWarningTime * 1000));

        }
      } else if (data.userId == null) {
          // if data.userId is null, the session is done; redirect the user to logoutUrl
          location.href = timeoutLoggedoutUrl;
          
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
function fetch_timeout_dialog(){
  $PBJQ.ajax({
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
  
  if (!$PBJQ("#portalMask").get(0)) {
    createDHTMLMask(dismiss_session_alert);
    $PBJQ("#portalMask").css("z-index", 10000);
  }
  if ($PBJQ("#timeout_alert_body").get(0)) {
    //its there, just update the min
    $PBJQ("#timeout_alert_body span").html(min);
    $PBJQ("#timeout_alert_body span").css('top', (f_scrollTop() + 100) + "px");
  }
  else {
    var dialog = timeoutDialogFragment.replace("{0}", min);
    $PBJQ("body").append(dialog);
  }
}
/**
 * For Short URL toggles in Morpheus
 */

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
/**
 * For Skip Nav in Morpheus
 */

var setupSkipNav = function(){
  // function called from site.vm to enable skip links for all browsers
   $PBJQ('#skipNav a.Mrphs-skipNav__link').click(function(){
     var target = $PBJQ(this).attr('href');
    $PBJQ(target).attr('tabindex','-1').focus();
   });
};
/**
 * Sets up subnav on the sitenav
 */

var setupSiteNav = function(){

  $PBJQ("ul.Mrphs-sitesNav__menu").each(function(){

    // Add an escape key handler to slide the page menu up
    $PBJQ(this).keydown(function(e) {
      if (e.keyCode == 27) {
        $PBJQ(this).parent().children('a').focus();
        $PBJQ(this).toggleClass('is-visible');
      }
    });

    $PBJQ(this).children('li:last').addClass('is-last-item')
  });

  $PBJQ("ul.Mrphs-sitesNav__menu > li").mouseleave(function(){
    $PBJQ(this).find('ul').toggleClass('is-visible');
  });

  fixTopNav = function(e) {

    if (e.keyCode == 40) { // downarrow
      
      e.preventDefault();
      $PBJQ('#selectSite').hide();
      //$PBJQ('.nav-submenu').hide();
      // Trigger click on the drop <span>, passing true to set focus on
      // the first tool in the dropdown.
      $PBJQ(this).parent().find(".Mrphs-sitesNav__drop").trigger('click',[true]);

    } else if (e.keyCode == 27) { // uparrow ? or ESC

      $PBJQ(this).parent().children('a').focus();
      $PBJQ(this).toggleClass('is-visible');

    }
  }

  // SAK-25505 - Switch from live() to on()
  // $PBJQ( "a.offsite" ).live( "click", function() {
  // $PBJQ('.topnav > li.nav-menu > a').live('keydown', function(e){
  if($PBJQ(document).on) {

    $PBJQ(document).on('keydown', '.Mrphs-sitesNav__menu > li.Mrphs-sitesNav__menuitem > a', fixTopNav);

  } else {

    $PBJQ('.Mrphs-sitesNav__menu > .Mrphs-sitesNav__menuitem > a').live('keydown', fixTopNav);

  }
  
  $PBJQ("ul.Mrphs-sitesNav__menu > li").mouseleave(function(){
    $PBJQ(this).find('ul').toggleClass('is-visible');
  });
  // focusFirstLink is only ever passed from the keydown handler. We
  // don't want to focus on click; it looks odd.

  $PBJQ("ul.Mrphs-sitesNav__menu li span.Mrphs-sitesNav__drop").click(function(e, focusFirstLink) {

  /*
   * see if there is a menu sibling
   *  if there is a child, display it
   *  if no menu sibling
   *  retrieve data, construct the menu, append
   */

  $PBJQ(this).toggleClass("is-clicked"); //On click toggle class "is-clicked"
  e.preventDefault()

  var jqObjDrop = $PBJQ(e.target);

  if(jqObjDrop.next('ul').length) {
    jqObjDrop.next('ul').toggleClass('is-visible');

    if(focusFirstLink) {
      jqObjDrop.next('ul').find("a:first").focus();
    }

  }

  else {

    var navsubmenu = "<ul class=\"Mrphs-sitesNav__submenu is-visible\" role=\"menu\">";
    var siteId = jqObjDrop.attr('data-site-id');
    var maxToolsInt = parseInt($PBJQ('#linkNav').attr('data-max-tools-int'));
    var maxToolsText = $PBJQ('#linkNav').attr('data-max-tools-anchor');
    var goToSite = '<li class=\"Mrphs-sitesNav__submenuitem\"><a role=\"menuitem\" href=\"' + portal.portalPath + '/site/' + siteId + '\" title=\"' + maxToolsText + '\"><span class=\"toolMenuIcon icon-sakai-see-all-tools\"></span>' + maxToolsText + '</a></li>';
    var siteURL = '/direct/site/' + jqObjDrop.attr('data-site-id') + '/pages.json';
    var currentSite = window.location.pathname.split('/').pop();

    $PBJQ.ajax({
      url: siteURL,
      dataType: "json",
      success: function(data){

        $PBJQ.each(data, function(i, item) {

          if (i <= maxToolsInt) {
            
            if (item.toolpopup) {
              navsubmenu = navsubmenu + '<li class=\"Mrphs-sitesNav__submenuitem\" ><a role=\"menuitem\" href=\"' + item.tools[0].url + "?sakai.popup=yes\" title=\"" + item.title + "\" onclick=\"window.open('" + item.toolpopupurl + "');\"><span class=\"toolMenuIcon icon-" + item.tools[0].toolId.replace(/\./gi, '-') + "\"></span>" + item.title + "</a></li>";

            } else if (item.tools.length >= 1) { // changed from item.tools.length === 1

              // Check to see if this is the current tool in the site
              var isCurrent = "";
              if (currentSite == item.tools[0].id) {
                var isCurrent = " is-current";
              }
              navsubmenu = navsubmenu + '<li class=\"Mrphs-sitesNav__submenuitem' + isCurrent + '\"><a role=\"menuitem\" href=\"' + item.tools[0].url + "\" title=\"" + item.title + "\"><span class=\"toolMenuIcon icon-" + item.tools[0].toolId.replace(/\./gi, '-') + "\"></span>" + item.title + "</a></li>";
            }
          }
        });

        if((data.length - 1) > maxToolsInt) {
          navsubmenu = navsubmenu + goToSite
        }
        
        navsubmenu = navsubmenu + "</ul>"
        
        jqObjDrop.after(navsubmenu);

        if(focusFirstLink) {
          jqObjDrop.next('ul').find("a:first").focus();
        }

        addArrowNavAndDisableTabNav($PBJQ(".Mrphs-sitesNav__submenu"));

      },

      error: function(XMLHttpRequest, status, error){
        // Something happened getting the tool list. 
      }
    });
  }

  }).hover(function(){
    $PBJQ(this).toggleClass("Mrphs-sitesNav__drop--hover"); //On hover over, add 
  });

}

/* Callback is a function and is called after sliding up ul */
function addArrowNavAndDisableTabNav(ul,callback) {
    ul.find('li a').attr('tabindex','-1').keydown(function (e) {
        var obj = $PBJQ(e.target);
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

/**
* For toggling the Minimize and Maximize tools menu in Morpheus: Adds classes to the <body> and changes the label text for accessibility
*/

function toggleMinimizeNav(){

  $PBJQ('#container').toggleClass('toggleNav--minimized');

  var el = $PBJQ(this);
  var label = $PBJQ('.accessibility-btn-label' , el);

  el.toggleClass('min max');

  if (label.text() == el.data("title-expand")) {
    label.text(el.data("text-original"));
    el.attr('title', (el.data("text-original")));
    el.attr('aria-pressed', true);
    document.cookie = "sakai_nav_minimized=false; path=/";

  } else {

    el.data("text-original", label.text());
    label.text(el.data("title-expand"));
    el.attr('title', (el.data("title-expand")));
    el.attr('aria-pressed', false);
    document.cookie = "sakai_nav_minimized=true; path=/";

  }
}

$PBJQ(".js-toggle-nav").on("click", toggleMinimizeNav);
/**
 * For Footer toggles in Morpheus
 */

function updatePresence(){

  $PBJQ.ajax({
    url: sakaiPresenceFragment,
    cache: false,
    success: function(frag){

      var whereul = frag.indexOf('<ul');
      if (whereul < 1) {
        $PBJQ("#presenceCount").html(' ');
        $PBJQ('#presenceCount').removeClass('present').addClass('empty');
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
        $PBJQ("#presenceCount").html(_c + '');
        $PBJQ('#presenceCount').removeClass('empty').addClass('present');
      }

      else 

        if (_c == 1) {
          $PBJQ("#presenceCount").html(_c + '');
          $PBJQ('#presenceCount').removeClass('present').addClass('empty');
        }

        else {
          $PBJQ("#presenceCount").html(' ');
          $PBJQ('#presenceCount').removeClass('present').addClass('empty');
        }

      $PBJQ("#presenceIframe").html(frag);

      var chatUrl = $PBJQ('.nav-selected .icon-sakai-chat').attr('href');

      $PBJQ('#presenceIframe .presenceList li.inChat span').wrap('<a href="' + chatUrl + '"></a>')
      sakaiLastPresenceTimeOut = setTimeout('updatePresence()', 30000);
    },

    // If we get an error, wait 60 seconds before retry
    error: function(request, strError){
      sakaiLastPresenceTimeOut = setTimeout('updatePresence()', 60000);
    }
  });
}
/**
 * Toggle user nav in header: 
 */

function toggleUserNav(){
  event.preventDefault();
  $PBJQ('.Mrphs-userNav__subnav').toggleClass('is-hidden');
}

$PBJQ(".js-toggle-user-nav a#loginUser", "#loginLinks").on("click", toggleUserNav);
$PBJQ(".js-toggle-user-nav .Mrphs-userNav__drop", "#loginLinks").on("click", toggleUserNav);
/**
 * Miscellaneous Utils
 */

function f_scrollTop(){
    return f_filterResults(window.pageYOffset ? window.pageYOffset : 0, document.documentElement ? document.documentElement.scrollTop : 0, document.body ? document.body.scrollTop : 0);
}

function f_filterResults(n_win, n_docel, n_body){
    var n_result = n_win ? n_win : 0;
    if (n_docel && (!n_result || (n_result > n_docel))) 
        n_result = n_docel;
    return n_body && (!n_result || (n_result > n_body)) ? n_body : n_result;
}
// Google Webfont
WebFontConfig = {
  google: { families: [ 'Open+Sans:300italic,400italic,600italic,700italic,400,600,300,700:latin' ] }
};
(function() {
  var wf = document.createElement('script');
  wf.src = ('https:' == document.location.protocol ? 'https' : 'http') +
    '://ajax.googleapis.com/ajax/libs/webfont/1/webfont.js';
  wf.type = 'text/javascript';
  wf.async = 'true';
  var s = document.getElementsByTagName('script')[0];
  s.parentNode.insertBefore(wf, s);
})();
