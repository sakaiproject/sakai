/**
 * For More Sites in Morpheus
 */

var dhtml_view_sites = function(){

  // first time through set up the DOM
  $PBJQ('#selectSiteModal').addClass('dhtml_more_tabs'); // move the selectSite in the DOM
  $PBJQ('.more-tab').position();

  // then recast the function to the post initialized state which will run from then on
  dhtml_view_sites = function(){

    var modal = $PBJQ('#selectSiteModal');
    
    modal.show();
    
    if (modal.hasClass('outscreen') ) {

      $PBJQ('body').toggleClass('active-more-sites');

      // In mobile mode, hide the tools nav prior to showing sites
      if ($PBJQ('body').hasClass('toolsNav--displayed')) {
        toggleToolsNav();
      }

      // Align with the bottom of the main header in desktop mode
      var allSitesButton = $PBJQ('.view-all-sites-btn:visible');

      var topPadding = 10;

      if (allSitesButton.length > 0) {
        // Raise the button to keep it visible over the modal overlay
        allSitesButton.css('z-index', 1005);

        var topPosition = allSitesButton.offset().top + allSitesButton.outerHeight() + topPadding;
        var rightPosition = $PBJQ('body').outerWidth() - (allSitesButton.offset().left + allSitesButton.outerWidth());
        if( $PBJQ('html').attr('dir') !== "rtl" ){
          modal.css('top', topPosition).css('right', rightPosition);
        }else{
          modal.css('top', topPosition).css('left', $PBJQ('body').outerWidth() - rightPosition );
        }
      }
      
      modal.toggleClass('outscreen');

      var paneHeight = $PBJQ(window).height();

      // Adjust for our offset from the top of the screen
      paneHeight -= $PBJQ('.tab-pane').offset().top;

      // and adjust to show the bottom of the modal frame
      paneHeight -= parseInt(modal.css('padding-bottom'), 10);

      $PBJQ('.tab-pane').css('max-height', paneHeight);


      $PBJQ('#txtSearch').focus();
      createDHTMLMask(dhtml_view_sites);

      $PBJQ('.selectedTab').bind('click', function(e){
        dhtml_view_sites();
        return false;
      });

      $PBJQ('.tab-pane:first').focus();

      $PBJQ(document).trigger('view-sites-shown');
    }
    else {
      // hide the dropdown
      $PBJQ('body').toggleClass('active-more-sites');
      $PBJQ('#selectSiteModal').toggleClass('outscreen'); //hide the box

      // Restore the button's zIndex so it doesn't hover over other overlays
      var allSitesButton = $PBJQ('.view-all-sites-btn');
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

    goToSite.find('a span').addClass('icon-sakai--see-all-tools')

    $PBJQ.getJSON(siteURL, function(data){
      $PBJQ.each(data, function(i, item){

        if (!item.tools[0]) {
          // This item has a page with no tool.  Skip over it.
          return true;
        }

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
    }); // end json call
  }
}

$PBJQ(document).ready(function(){

  if ($PBJQ('#eid').length === 1) {
    $PBJQ('#eid').focus()
  }

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
          return ($PBJQ('.fav-title a span.fullTitle', entry).text().toLowerCase().indexOf(queryString) >= 0);
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

  $PBJQ('#otherSiteSearchClear').on('click', function () {
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

});


$PBJQ(document).ready(function($){
  // The list of favorites currently stored
  var favoritesList = [];

  // True if we've finished fetching and displaying the initial list
  //
  // Used to ensure we don't inadvertently save an empty list of favorites if
  // the user gets in too quickly
  var favoritesLoaded = false;

  var container = $PBJQ('#selectSite');
  var favoritesPane = $PBJQ('#otherSitesCategorWrap');
  var organizePane = $PBJQ('#organizeFavorites');

  // Build up a map of siteid => list item.  Do this instead of an ID
  // selector to cope with Site IDs containing strange characters.
  var itemsBySiteId = {};
  $PBJQ('.site-favorite-btn', favoritesPane).each(function (i, e) {
    itemsBySiteId[$PBJQ(e).data('site-id')] = $PBJQ(e).parent();
  });

  var button_states = {
    favorite: {
      markup: '<i class="site-favorite-icon site-favorite"></i>'
    },
    nonfavorite: {
      markup: '<i class="site-favorite-icon site-nonfavorite"></i>'
    },
    myworkspace: {
      markup: '<i class="site-favorite-icon site-workspace site-favorite"></i>'
    }
  };

  var getUserFavorites = function (callback) {
    $PBJQ.ajax({
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

    $PBJQ(btn).data('favorite-state', state);

    if (state === 'favorite') {
      $PBJQ(btn).attr('title', $PBJQ('#removeFromFavoritesText').text());
    } else if (state === 'nonfavorite') {
      $PBJQ(btn).attr('title', $PBJQ('#addToFavoritesText').text());
    } else {
      $PBJQ(btn).attr('title', null);
    }

    $PBJQ(btn).empty().append($PBJQ(entry.markup));
  };

  var renderFavoriteCount = function () {
    var favoriteCount = $PBJQ('.site-favorite', favoritesPane).length;

    $PBJQ('.favoriteCount', container).text('(' + favoriteCount + ')');

    if (favoriteCount < 2) {
      $PBJQ('.organizeFavorites', container).addClass('tab-disabled');
    } else {
      $PBJQ('.organizeFavorites', container).removeClass('tab-disabled');
    }
  };

  var renderFavorites = function (favorites) {
    $PBJQ('.site-favorite-btn', favoritesPane).each(function (idx, btn) {
      var buttonSiteId = $PBJQ(btn).data('site-id');

      if ($PBJQ(btn).closest('.my-workspace').length > 0) {
        setButton(btn, 'myworkspace');
      } else {
        if ($PBJQ.inArray(buttonSiteId, favorites) >= 0) {
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
    return $PBJQ('.site-favorite-btn', favoritesPane).has('.site-favorite').map(function () {
      return $PBJQ(this).data('site-id');
    }).toArray();
  }

  var loadFromServer = function () {
    getUserFavorites(renderFavorites);
  }

  var showRefreshNotification = function () {
    if ($PBJQ('.moresites-refresh-notification').length > 0) {
      // Already got it
      return;
    }

    var notification = $PBJQ('<div class="moresites-refresh-notification" />')
        .html($PBJQ('#refreshNotificationText').html());

    $PBJQ("#loginLinks").prepend(notification);

    notification.css('top', ($PBJQ('.Mrphs-siteHierarchy').offset().top) + 'px');
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

    // Retain the sort ordering of our original list, adding new items to the end
    newFavorites = newFavorites.sort(function (a, b) {
      if (favoritesList.indexOf(a) === -1) {
        return 1;
      } else if (favoritesList.indexOf(b) === -1) {
        return -1;
      } else {
        return favoritesList.indexOf(a) - favoritesList.indexOf(b);
      }
    });

    $PBJQ.ajax({
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

  $PBJQ(favoritesPane).on('click', '.site-favorite-btn', function () {
    var self = this;

    var siteId = $PBJQ(self).data('site-id');
    var originalState = $PBJQ(self).data('favorite-state');

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

  $PBJQ(container).on('click', '.tab-btn', function () {
    if ($PBJQ(this).hasClass('tab-disabled')) {
      return false;
    }

    $PBJQ('.tab-btn', container).removeClass('active');
    $PBJQ(this).addClass('active');

    var panel = $PBJQ(this).data('tab-target');

    $PBJQ('.tab-box').hide();
    $PBJQ(container).trigger('tab-shown', panel);
    $PBJQ('#' + panel).show();
  });

  $PBJQ(document).on('view-sites-shown', function () {
    loadFromServer();
  });

  $PBJQ(container).on('tab-shown', function (e, panelId) {
    if (panelId === 'organizeFavorites') {
      // Build our organize favorites screen based on the current set of
      // favorites
      var list = $PBJQ('#organizeFavoritesList');
      list.empty();

      // Collapse any visible tool menus
      $PBJQ('#otherSiteTools').remove();

      $PBJQ('#organizeFavoritesPurgatoryList').empty();

      $PBJQ.each(favoritesList, function (idx, siteid) {
        if (!itemsBySiteId[siteid]) {
          // Skip any favorite site that wasn't properly found for some reason
          // (this might happen if the user's favorites list contains sites that
          // they've had their access revoked from)
          return;
        }

        if ($PBJQ(itemsBySiteId[siteid]).hasClass('my-workspace')) {
          // Don't show an entry for the user's workspace since it can't be rearranged anyway.
          return;
        }

        var favoriteItem = itemsBySiteId[siteid].clone(false);

        favoriteItem.addClass('organize-favorite-item').data('site-id', siteid);
        var dragHandle = $PBJQ('<i class="fa fa-bars fav-drag-handle"></i>');

        // Hide the tool dropdown
        $PBJQ('.toolMenus', favoriteItem).remove();

        // Show a drag handle
        favoriteItem.append(dragHandle);

        // And disable the link to site so we don't accidentally hit it while
        // dragging
        $PBJQ(favoriteItem).find('.fav-title a').attr('href', null);

        list.append(favoriteItem);

        // Make sure the item is visible, just in case it was hidden on the other tab
        favoriteItem.show();
      });

      list.sortable({
        stop: function () {
          // Update our ordering based on the new selection
          favoritesList = list.find('.organize-favorite-item').map(function () {
            return $PBJQ(this).data('site-id');
          }).toArray();

          // and send it all to the server
          syncWithServer();
        }
      });

      list.disableSelection();
    }
  });

  $PBJQ(favoritesPane).on('click', '.toolMenus', function (e) {
    e.preventDefault();
    showToolMenu($PBJQ(this));
    return false;
  });

  $PBJQ(organizePane).on('click', '.site-favorite-btn', function () {
    var self = this;

    if ($PBJQ(self).closest('.my-workspace').length > 0) {
      // No unfavoriting your workspace!
      return;
    }

    var li = $PBJQ(self).parent();

    var buttonState;

    if ($PBJQ(self).closest('#organizeFavoritesList').length == 0) {
      // The clicked item was currently in "purgatory", having been unfavorited
      // in the process of organizing favorites.  This click will promote it
      // back to a favorite
      $PBJQ('#organizeFavoritesList').append(li);
      buttonState = 'favorite';
    } else {
      // This item has just been unfavorited.  To purgatory!
      $PBJQ('#organizeFavoritesPurgatoryList').append(li);
      buttonState = 'nonfavorite';
    }

    // Set the favorite state for both the entry under "Organize" and the
    // original entry under "Sites"
    setButton(self, buttonState);
    setButton(itemsBySiteId[$PBJQ(self).data('site-id')].find('.site-favorite-btn'),
              buttonState);

    renderFavoriteCount();

    syncWithServer(function () {
      // If anything goes wrong while saving, refresh from the server.
      loadFromServer();
    });
  });

  $PBJQ('.otherSitesMenuClose').on('click', function () {
    // Close the pane
    dhtml_view_sites();
  });

});
