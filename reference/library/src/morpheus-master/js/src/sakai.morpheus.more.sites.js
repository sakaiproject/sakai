/**
 * For More Sites in Morpheus
 */

var dhtml_view_sites = function(){

  // first time through set up the DOM
  $PBJQ('#selectSiteModal').addClass('dhtml_more_tabs'); // move the selectSite in the DOM
  $PBJQ('.more-tab').position();

  // then recast the function to the post initialized state which will run from then on
  dhtml_view_sites = function(){

    if ($PBJQ('#selectSiteModal').hasClass('outscreen') ) {

      $PBJQ('body').toggleClass('active-more-sites');

      // In mobile mode, hide the tools nav prior to showing sites
      if ($PBJQ('body').hasClass('toolsNav--displayed')) {
        toggleToolsNav();
      }

      // Align with the bottom of the main header in desktop mode
      var allSitesButton = $('.view-all-sites-btn:visible');

      if (allSitesButton.length > 0) {
        // Raise the button to keep it visible over the modal overlay
        allSitesButton.css('z-index', 1005);

        var topPadding = 10;
        var topPosition = allSitesButton.offset().top + allSitesButton.outerHeight() + topPadding;
        var rightPosition = $PBJQ('body').outerWidth() - (allSitesButton.offset().left + allSitesButton.outerWidth());
        $PBJQ('#selectSiteModal').css('top', topPosition).css('right', rightPosition);
      }

      $PBJQ('.tab-pane').css('max-height', $PBJQ('body').height());

      $PBJQ('#selectSiteModal').toggleClass('outscreen');

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
          e.preventDefault();
          showToolMenu($(this));
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

      $PBJQ(document).trigger('view-sites-shown');
    }

    else {

      // hide the dropdown
      $PBJQ('body').toggleClass('active-more-sites');
      $PBJQ('#selectSiteModal').toggleClass('outscreen'); //hide the box

      // Restore the button's zIndex so it doesn't hover over other overlays
      var allSitesButton = $('.view-all-sites-btn');
      allSitesButton.css('z-index', 'auto');

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

  $PBJQ('#selectSiteModal').toggleClass('outscreen');  //hide the box
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
function showToolMenu(jqObj){
  var classId = jqObj.attr('id');
  // We need to escape special chars, like exclamations, or else $PBJQ selectors don't work.
  var id = classId.replace(/!/g,'\\!').replace(/~/g,'\\~');
  $PBJQ('.toolMenus').removeClass('toolMenusActive');

  if ($PBJQ('.' + id).length) {
    $PBJQ('#otherSiteTools').remove();
  } else {
    var subsubmenu_elt = $PBJQ('<ul id="otherSiteTools" role="menu" />').addClass(classId);
    var siteURL = '/direct/site/' + classId + '/pages.json';
    scroll(0, 0)
    var maxToolsInt = parseInt($PBJQ('#maxToolsInt').text());
    var maxToolsText = $PBJQ('#maxToolsText').text();

    var li_template = $PBJQ('<li class="otherSiteTool" >' +
                            '<span>' +
                            '<a role="menuitem"><span class="Mrphs-toolsNav__menuitem--icon"> </span></a>' +
                            '</span>' +
                            '</li>');

    var goToSite = li_template.clone();

    goToSite.find('a')
      .attr('href', portal.portalPath + '/site/' + classId)
      .attr('title', maxToolsText)
      .append(maxToolsText);

    goToSite.find('a span').addClass('icon-sakai-see-all-tools')

    $PBJQ.getJSON(siteURL, function(data){
      $PBJQ.each(data, function(i, item){

        if (i < maxToolsInt) {
          var li = li_template.clone();
          // Set the item URL and text
          li.find('a')
            .attr('href', item.tools[0].url)
            .attr('title', item.title)
            .append(item.title);

          // And its icon
          li.find('a span')
            .addClass('icon-' + item.tools[0].toolId.replace(/\./gi, '-'))
            .addClass('otherSiteToolIcon');

          if (item.toolpopup) {
            // For popups, we add an extra URL parameter and an onclick event
            li.find('a')
            .attr('href', item.tools[0].url + '?sakai.popup=yes')
            .attr('onclick', 'window.open(' + item.toolpopupurl + '); return false');
          }

          subsubmenu_elt.append(li);
        }
      });

      // If we couldn't show all the tools, offer a "go to site" link
      if (data.length > maxToolsInt) {
        subsubmenu_elt.append(goToSite.clone());
      }

      $PBJQ('#otherSiteTools').remove();
      jqObj.closest('li').append(subsubmenu_elt);

      jqObj.parent().find('.toolMenus').addClass("toolMenusActive");
      // On up arrow or escape, hide the popup
      subsubmenu_elt.keydown(function(e){
        if (e.keyCode == 27) {
          e.preventDefault();
          jqObj.focus();
          $PBJQ(this).remove();
          $PBJQ('.' + id).remove();
          jqObj.parent().find('.toolMenus').removeClass("toolMenusActive");
        }
      });

      addArrowNavAndDisableTabNav(subsubmenu_elt, function () {
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
  $PBJQ("#show-all-sites, .view-all-sites-btn").on("click", dhtml_view_sites);

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

  $PBJQ('#txtSearch').keyup(function(event){

    if (event.keyCode == 27) {
      resetSearch();
    }

    if ($PBJQ('#txtSearch').val().length > 0) {
      var queryString = $PBJQ('#txtSearch').val().toLowerCase();

      $PBJQ('.fav-sites-term, .fav-sites-entry').hide();

      var matched_sites = $PBJQ('.fav-sites-entry').filter(function (idx, entry) {
          return ($('.fav-title a', entry).attr('title').toLowerCase().indexOf(queryString) >= 0);
      });

      matched_sites.show();
      matched_sites.closest('.fav-sites-term').show();
    }

    if ($PBJQ('#txtSearch').val().length == 0) {
      resetSearch();
    }

    // Should be <=1 if there is a header line
    if ($PBJQ('#otherSiteList li:visible').length < 1 && $PBJQ('.otherSitesCategorList li:visible').length < 1) {
      $PBJQ('.norecords').remove();
      $PBJQ('#noSearchResults').fadeIn('slow');
    }
  });

  function resetSearch(){
    $PBJQ('#txtSearch').val('');
    $PBJQ('.fav-sites-term, .fav-sites-entry').show();
    $PBJQ('#noSearchResults').hide();
    $PBJQ('#txtSearch').focus();
  }

  $('#otherSiteSearchClear').on('click', function () {
      resetSearch();
  });

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


$PBJQ(document).ready(function($){
  // The list of favorites currently stored
  var favoritesList = [];

  // True if we've finished fetching and displaying the initial list
  //
  // Used to ensure we don't inadvertently save an empty list of favorites if
  // the user gets in too quickly
  var favoritesLoaded = false;

  var container = $('#selectSite');
  var favoritesPane = $('#otherSitesCategorWrap');
  var organizePane = $('#organizeFavorites');

  // Build up a map of siteid => list item.  Do this instead of an ID
  // selector to cope with Site IDs containing strange characters.
  var itemsBySiteId = {};
  $('.site-favorite-btn', favoritesPane).each(function (i, e) {
    itemsBySiteId[$(e).data('site-id')] = $(e).parent();
  });

  var button_states = {
    favorite: {
      markup: '<i class="site-favorite-icon fa fa-star site-favorite" />'
    },
    nonfavorite: {
      markup: '<i class="site-favorite-icon fa fa-star-o site-nonfavorite" />'
    },
    myworkspace: {
      markup: '<i class="site-favorite-icon fa fa-home site-favorite" />'
    }
  };

  var getUserFavorites = function (callback) {
    $.ajax({
      url: '/portal/favorites/list',
      method: 'GET',
      dataType: 'text',
      success: function (data) {
        favoritesList = data.split(';').filter(function (e, i) {
          return e != '';
        });

        callback(favoritesList);
      }
    });
  };

  var setButton = function (btn, state) {
    var entry = button_states[state];

    $(btn).data('favorite-state', state);
    $(btn).empty().append($(entry.markup));
  };

  var renderFavoriteCount = function () {
    // Subtract 1 from the count to avoid counting "My Workspace", which can't be moved anyway.
    var favoriteCount = $('.site-favorite', favoritesPane).length - 1;

    $('.favoriteCount', container).text('(' + favoriteCount + ')');

    if (favoriteCount < 2) {
      $('.organizeFavorites', container).addClass('tab-disabled');
    } else {
      $('.organizeFavorites', container).removeClass('tab-disabled');
    }
  };

  var renderFavorites = function (favorites) {
    $('.site-favorite-btn', favoritesPane).each(function (idx, btn) {
      var buttonSiteId = $(btn).data('site-id');

      if ($(btn).closest('.my-workspace').length > 0) {
        setButton(btn, 'myworkspace');
      } else {
        if ($.inArray(buttonSiteId, favorites) >= 0) {
          setButton(btn, 'favorite');
        } else {
          setButton(btn, 'nonfavorite');
        }
      }
    });

    renderFavoriteCount();

    favoritesLoaded = true;
  };

  var listFavorites = function () {
    // Any favorite button with the 'site-favorite' class has been starred.
    return $('.site-favorite-btn', favoritesPane).has('.site-favorite').map(function () {
      return $(this).data('site-id');
    }).toArray();
  }

  var loadFromServer = function () {
    getUserFavorites(renderFavorites);
  }

  var showRefreshNotification = function () {
    if ($('.moresites-refresh-notification').length > 0) {
      // Already got it
      return;
    }

    var notification = $('<div class="moresites-refresh-notification" />')
        .html($('#refreshNotificationText').html());

    $("#loginLinks").prepend(notification);

    notification.css('top', ($('.Mrphs-siteHierarchy').offset().top) + 'px');
  };

  var syncWithServer = function (onError) {
    if (!favoritesLoaded) {
      console.log("Can't update favorites as they haven't been loaded yet.");
      return;
    }

    if (!onError) {
      onError = function () {};
    }

    var newFavorites = listFavorites();

    // Retain the sort ordering of our original list
    newFavorites = newFavorites.sort(function (a, b) {
      return favoritesList.indexOf(a) - favoritesList.indexOf(b);
    });

    $.ajax({
      url: '/portal/favorites/update',
      method: 'POST',
      data: {
        favorites: newFavorites.join(';')
      },
      error: onError
    });

    // Finally, update our stored list of favorites
    favoritesList = newFavorites;
    showRefreshNotification();
  };

  $(favoritesPane).on('click', '.site-favorite-btn', function () {
    var self = this;

    var siteId = $(self).data('site-id');
    var originalState = $(self).data('favorite-state');

    if (originalState === 'myworkspace') {
      // No unfavoriting your workspace!
      return;
    }

    var newState;

    if (originalState === 'favorite') {
      newState = 'nonfavorite';
    } else {
      newState = 'favorite';
    }

    setButton(self, newState);
    renderFavoriteCount();

    syncWithServer(function () {
      // If anything goes wrong while saving, refresh from the server.
      loadFromServer();
    });
  });

  $(container).on('click', '.tab-btn', function () {
    if ($(this).hasClass('tab-disabled')) {
      return false;
    }

    $('.tab-btn', container).removeClass('active');
    $(this).addClass('active');

    var panel = $(this).data('tab-target');

    $('.tab-box').hide();
    $(container).trigger('tab-shown', panel);
    $('#' + panel).show();
  });

  $(document).on('view-sites-shown', function () {
    loadFromServer();
  });

  $(container).on('tab-shown', function (e, panelId) {
    if (panelId === 'organizeFavorites') {
      // Build our organize favorites screen based on the current set of
      // favorites
      var list = $('#organizeFavoritesList');
      list.empty();

      // Collapse any visible tool menus
      $('#otherSiteTools').remove();

      $('#organizeFavoritesPurgatoryList').empty();

      $.each(favoritesList, function (idx, siteid) {
        if (!itemsBySiteId[siteid]) {
          // Skip any favorite site that wasn't properly found for some reason
          // (this might happen if the user's favorites list contains sites that
          // they've had their access revoked from)
          return;
        }

        if ($(itemsBySiteId[siteid]).hasClass('my-workspace')) {
          // Don't show an entry for the user's workspace since it can't be rearranged anyway.
          return;
        }

        var favoriteItem = itemsBySiteId[siteid].clone(false);

        favoriteItem.addClass('organize-favorite-item').data('site-id', siteid);
        var dragHandle = $('<i class="fa fa-bars fav-drag-handle"></i>');

        // Hide the tool dropdown
        $('.toolMenus', favoriteItem).remove();

        // Show a drag handle
        favoriteItem.append(dragHandle);

        // And disable the link to site so we don't accidentally hit it while
        // dragging
        $(favoriteItem).find('.fav-title a').attr('href', null);

        list.append(favoriteItem);

        // Make sure the item is visible, just in case it was hidden on the other tab
        favoriteItem.show();
      });

      list.sortable({
        stop: function () {
          // Update our ordering based on the new selection
          favoritesList = list.find('.organize-favorite-item').map(function () {
            return $(this).data('site-id');
          }).toArray();

          // and send it all to the server
          syncWithServer();
        }
      });

      list.disableSelection();
    }
  });

  $(favoritesPane).on('click', '.toolMenus', function (e) {
    e.preventDefault();
    showToolMenu($(this));
    return false;
  });

  $(organizePane).on('click', '.site-favorite-btn', function () {
    var self = this;

    if ($(self).closest('.my-workspace').length > 0) {
      // No unfavoriting your workspace!
      return;
    }

    var li = $(self).parent();

    var buttonState;

    if ($(self).closest('#organizeFavoritesList').length == 0) {
      // The clicked item was currently in "purgatory", having been unfavorited
      // in the process of organizing favorites.  This click will promote it
      // back to a favorite
      $('#organizeFavoritesList').append(li);
      buttonState = 'favorite';
    } else {
      // This item has just been unfavorited.  To purgatory!
      $('#organizeFavoritesPurgatoryList').append(li);
      buttonState = 'nonfavorite';
    }

    // Set the favorite state for both the entry under "Organize" and the
    // original entry under "Sites"
    setButton(self, buttonState);
    setButton(itemsBySiteId[$(self).data('site-id')].find('.site-favorite-btn'),
              buttonState);

    renderFavoriteCount();

    syncWithServer(function () {
      // If anything goes wrong while saving, refresh from the server.
      loadFromServer();
    });
  });
});
