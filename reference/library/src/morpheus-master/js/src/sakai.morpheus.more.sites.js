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

      $PBJQ('#txtSearch').focus();
      createDHTMLMask(dhtml_view_sites);
      $PBJQ('#selectSite').attr('tabindex', '0');

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
      $PBJQ('#selectSite').toggleClass('outscreen'); //hide the box
      $PBJQ('#selectSite').attr('tabindex', '-1');
      removeDHTMLMask()
      $PBJQ('#otherSiteTools').remove();
      $PBJQ('.selectedTab').unbind('click');
    }
  }

  // finally run the inner function, first time through
  dhtml_view_sites();
}

function closeDrawer() {

  $PBJQ('#selectSite').toggleClass('outscreen');  //hide the box
  removeDHTMLMask();
  $PBJQ('#selectSite').attr('tabindex', '-1');
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

  // Open all Sites with mobile view 	
   $PBJQ(".js-toggle-sites-nav", "#skipNav").on("click", dhtml_view_sites);
  
  // Open all Sites with Desktop view
  $PBJQ("#show-all-sites").on("click", dhtml_view_sites);

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
