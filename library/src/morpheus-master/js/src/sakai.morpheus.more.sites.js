/**
 * For More Sites in Morpheus
 */

var selectSiteModalLinks, selectSiteLastModalInTab;

var dhtml_view_sites = function(){

  // first time through set up the DOM
  $PBJQ('#selectSiteModal').addClass('dhtml_more_tabs'); // move the selectSite in the DOM
  $PBJQ('.more-tab').position();
  var paneHeight = $PBJQ(window).height()*0.8;

  // then recast the function to the post initialized state which will run from then on
  dhtml_view_sites = function(){

    var modal = $PBJQ('#selectSiteModal');
    
    modal.show();

    // Find all focusable items
    if (typeof selectSiteModalLinks == 'undefined' || typeof selectSiteLastModalInTab == 'undefined') {
      selectSiteModalLinks = modal.find('button, a');
      selectSiteLastModalInTab = modal.find('.tab-box a:last');
    }

    // Lock the focus into the modal links
    modal.on('keydown', function (e) {
      var cancel = false;
      if (e.ctrlKey || e.metaKey || e.altKey) {
        return;
      }
      switch(e.which) {
        case 27: // ESC
          closeDrawer();
          cancel = true;
          break;
        case 9: // TAB
          if (e.shiftKey) {
            if (e.target === selectSiteModalLinks[0]) {
              selectSiteModalLinks[selectSiteModalLinks.length - 1].focus();
              cancel = true;
            }
          } else {
            if (e.target === selectSiteModalLinks[selectSiteModalLinks.length - 1] || e.target === selectSiteLastModalInTab[selectSiteLastModalInTab.length - 1]) {
              selectSiteModalLinks[0].focus();
              cancel = true;
            }
          }
          break;
      }
      if (cancel) {
        e.preventDefault();
      }
    });
    
    if (modal.hasClass('outscreen') ) {
      $PBJQ('body').toggleClass('active-more-sites');

      // Align with the bottom of the main header in desktop mode
      var allSitesButton = $PBJQ('.view-all-sites-btn:visible');

      var topPadding = allSitesButton.height() + 5;

      if (allSitesButton.length > 0) {
        // Raise the button to keep it visible over the modal overlay
        allSitesButton.css('z-index', 1005);

        var topPosition = allSitesButton.offset().top - $PBJQ(window).scrollTop() + topPadding;
        var rightPosition = $PBJQ('body').outerWidth() - (allSitesButton.offset().left + allSitesButton.outerWidth());
        if( $PBJQ('html').attr('dir') !== "rtl" ){
          modal.css('top', topPosition).css('right', rightPosition);
        }else{
          modal.css('top', topPosition).css('left', $PBJQ('body').outerWidth() - rightPosition );
        }
      }
      
      modal.toggleClass('outscreen');
      
      // Adjust for our offset from the top of the screen
      paneHeight -= topPosition;

      // and adjust to show the bottom of the modal frame
      paneHeight -= parseInt(modal.css('padding-bottom'), 20);

      // Avoid auto zoom to focus text field on touch devices
      if (MorpheusViewportHelper.isNonPhone()) {
        $PBJQ('#txtSearch').focus();
      }

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
  
  
  if($PBJQ(window).width() < 800) {
	  paneHeight = paneHeight*0.85;
  }
  $PBJQ('.tab-pane').css('height', paneHeight);
  
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

  // For desktop screen size
  if ($PBJQ('.view-all-sites-btn a:visible').length) {
    $PBJQ('.view-all-sites-btn a').focus();
  }
  else {
    $PBJQ('.js-toggle-sites-nav').focus();
  }

}

function createDHTMLMask(callback){
  $PBJQ('body').append('<div id="portalMask">&nbsp;</div>');

  $PBJQ('#portalMask')
  .css('height', browserSafeDocHeight())
  .css({
    'width': '100%',
    'z-index': 1000,
    'top': 0,
    'left': 0
  })
  .attr('tabindex', -1)
  .bind("click", function(event){
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
  $PBJQ('.toolMenus').removeClass('toolMenusActive').attr('aria-expanded', 'false');

  if ($PBJQ('.' + id).length) {
    $PBJQ('#otherSiteTools').remove();
  } else {
    var subsubmenu_elt = $PBJQ('<ul id="otherSiteTools" role="menu" />').addClass(classId);
    var siteURL = '/direct/site/' + classId + '/pages.json';
    scroll(0, 0);
    var maxToolsInt = parseInt($PBJQ('#maxToolsInt').text());
    var maxToolsText = $PBJQ('#maxToolsText').text();

    var li_template = $PBJQ('<li class="otherSiteTool" >' +
                            '<span>' +
                            '<a role="menuitem" tabindex="-1"><span class="Mrphs-toolsNav__menuitem--icon"> </span></a>' +
                            '</span>' +
                            '</li>');

    var goToSite = li_template.clone();

    goToSite.addClass('gotosite');

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
            .addClass('icon-sakai--' + item.tools[0].toolId.replace(/\./gi, '-'))
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
      // Move focus to first option and setup menu tools for arrow navigation
      jqObj.closest('li').find('ul li a').first().focus();
      addArrowNavAndDisableTabNav($PBJQ('ul#otherSiteTools'));

      jqObj.parent().find('.toolMenus').addClass("toolMenusActive").attr('aria-expanded', 'true');
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
  var autoFavoritesEnabled = true;

  // Keep a copy of the favoritesList as it was before any changes were made.
  // If the user makes a set of changes that ultimately revert us back to where we
  // started, we don't need to show the indicator to reload the page.
  var initialFavoritesList = undefined;

  var favoritesList = [];

  var maxFavoriteEntries = $PBJQ('#max-favorite-entries').text().trim();

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
      dataType: 'json',
      success: function (data) {
        autoFavoritesEnabled = data.autoFavoritesEnabled;

        favoritesList = data.favoriteSiteIds.filter(function (e, i) {
          return e != '';
        });

        if (initialFavoritesList == undefined) {
          initialFavoritesList = favoritesList;
        }

        callback(favoritesList);
      }
    });
  };

  var setButton = function (btn, state) {
    var entry = button_states[state];

    $PBJQ(btn).data('favorite-state', state);

    if (state === 'favorite') {
      $PBJQ(btn).attr('title', $PBJQ('#removeFromFavoritesText').text().replace("[site]", $PBJQ(btn).parent().find('span.fullTitle').text() ));
    } else if (state === 'nonfavorite') {
      $PBJQ(btn).attr('title', $PBJQ('#addToFavoritesText').text().replace("[site]", $PBJQ(btn).parent().find('span.fullTitle').text() ));
    } else {
      $PBJQ(btn).attr('title', null);
    }

    $PBJQ(btn).empty().append($PBJQ(entry.markup));
  };

  var renderFavoriteCount = function () {
    var favoriteCount = $PBJQ('.fav-sites-entry .site-favorite', favoritesPane).length;

    $PBJQ('.favoriteCount', container).text('(' + favoriteCount + ')');

    if (favoriteCount > maxFavoriteEntries) {
      $PBJQ('.favoriteCountAndWarning').addClass('maxFavoritesReached');
    } else {
      $PBJQ('.favoriteCountAndWarning').removeClass('maxFavoritesReached');
    }
  };

  var setAllOrNoneStarStates = function () {
    $PBJQ('.favorites-select-all-none', favoritesPane).each(function (idx, selectAllNone) {
      var termContainer = $PBJQ(selectAllNone).closest('.fav-sites-term');

      var siteCount = termContainer.find('.fav-sites-entry:not(.my-workspace)').length;
      var favoritedSiteCount = termContainer.find('.fav-sites-entry .site-favorite').length;

      if (siteCount == 0) {
        // No favoritable sites under this section
        $PBJQ(selectAllNone).hide();
      } else {
        if (favoritedSiteCount == siteCount) {
          $PBJQ(selectAllNone).data('favorite-state', 'favorite');
          $PBJQ(selectAllNone).html(button_states.favorite.markup);
        } else {
          $PBJQ(selectAllNone).data('favorite-state', 'nonfavorite');
          $PBJQ(selectAllNone).html(button_states.nonfavorite.markup);
        }

        $PBJQ(selectAllNone).show();
      }
    });
  };

  var hideFavoriteButtons = function () {
    $PBJQ('.site-favorite-btn', favoritesPane).empty();
    $PBJQ('.favorites-select-all-none', favoritesPane).empty();
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

    $PBJQ('.favorites-help-text').hide();

    if (autoFavoritesEnabled) {
      $PBJQ('.favorites-help-text.autofavorite-enabled').show();
    } else {
      $PBJQ('.favorites-help-text.autofavorite-disabled').show();
    }

    setAllOrNoneStarStates();
    renderFavoriteCount();

    favoritesLoaded = true;
  };

  var listFavorites = function () {
    // Any favorite button with the 'site-favorite' class has been starred.
    return $PBJQ('.site-favorite-btn', favoritesPane).has('.site-favorite').map(function () {
      return $PBJQ(this).data('site-id');
    }).toArray();
  }

  var loadFromServer = function (attempt) {
    if (syncInProgress) {
      // Don't let the user edit the current state if we know it's going to be invalidated.
      favoritesLoaded = false;
      hideFavoriteButtons();
    }

    if (!attempt) {
      attempt = 0;
    }

    if (syncInProgress && attempt < 100) {
      setTimeout(function () {
        loadFromServer(attempt + 1);
      }, 50);
    } else {
      getUserFavorites(renderFavorites);
    }
  };

  var arrayEqual = function (a1, a2) {
    if (a1.length != a2.length) {
      return false;
    }

    for (var i = 0; i < a1.length; i++) {
      if (a1[i] != a2[i]) {
        return false;
      }
    }

    return true;
  };

  var showRefreshNotification = function () {

    if (arrayEqual(favoritesList, initialFavoritesList)) {
      // The user is back to where they started!
      $PBJQ('.moresites-refresh-notification').remove();
      return;
    }

    if ($PBJQ('.moresites-refresh-notification').length > 0) {
      // Already got it
      return;
    }

    var notification = $PBJQ('<div class="moresites-refresh-notification" />')
        .html($PBJQ('#refreshNotificationText').html());

    $PBJQ("#loginLinks").prepend(notification);

    notification.css('top', ($PBJQ('.Mrphs-siteHierarchy').offset().top) + 'px');
  };

  var syncInProgress = false;
  var nextToSync = [];

  // The user might go crazy with the clicky, so queue our updates so they run
  // in a defined order.
  var runNextServerUpdate = function (onError) {
    var newState;

    // we can skip intermediate updates because they'll just get overwritten anyway.
    while (nextToSync.length > 0) {
      newState = nextToSync.shift();
    }

    if (newState) {
      $PBJQ.ajax({
        url: '/portal/favorites/update',
        method: 'POST',
        data: {
          userFavorites: JSON.stringify(newState),
        },
        error: onError,
        complete: runNextServerUpdate
      });
    } else {
      // All done!
      syncInProgress = false;
    }
  };

  var syncWithServer = function (onError) {
    if (!favoritesLoaded) {
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

    var newState = {
      favoriteSiteIds: newFavorites,
      autoFavoritesEnabled: autoFavoritesEnabled,
    };

    nextToSync.push(newState);

    if (syncInProgress) {
      /* It'll up our next state when it next runs */
    } else {
      syncInProgress = true;
      runNextServerUpdate(onError);
    };

    // Finally, update our stored list of favorites
    favoritesList = newFavorites;
    showRefreshNotification();
  };

  var returnElementToOriginalPositionIfPossible = function (siteId) {
    if (initialFavoritesList && initialFavoritesList.indexOf(siteId) > -1) {
      var idx = initialFavoritesList.indexOf(siteId);

      // We'll attempt to place our item to the right its original left
      // neighbor.  If the left neighbor was removed too, keep scanning left
      // until we find one of the original elements and place it to the right.
      // Otherwise, insert at the beginning of the array.
      //
      // The intention here is to allow multiple elements to be removed and
      // re-added in arbitrary order, and to reproduce the original ordering.

      var placed = false;

      for (var neighborIdx = idx - 1; neighborIdx >= 0; neighborIdx--) {
        var neighbor = initialFavoritesList[neighborIdx];

        var neighborCurrentIndex = favoritesList.indexOf(neighbor);

        if (neighborCurrentIndex >= 0 && neighborCurrentIndex < idx) {
          /* Place our element after it */
          favoritesList.splice(neighborCurrentIndex + 1, 0, siteId)
          placed = true;
          break;
        }
      }

      if (!placed) {
        // place at the beginning
        favoritesList.splice(idx, 0, siteId)
      }
    }
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

    // If a favorite has been added that was removed and re-added during this
    // same session, put it back in the same slot rather than sending it to the
    // end
    if (newState == 'favorite') {
      returnElementToOriginalPositionIfPossible(siteId);
    }

    setButton(self, newState);
    setAllOrNoneStarStates();
    renderFavoriteCount();

    syncWithServer(function () {
      // If anything goes wrong while saving, refresh from the server.
      loadFromServer();
    });
  });

  $PBJQ(favoritesPane).on('click', '.favorites-select-all-none', function () {
    var state = $PBJQ(this).data('favorite-state');
    var buttons = $PBJQ(this).closest('.fav-sites-term').find('.fav-sites-entry:not(.my-workspace) .site-favorite-btn');

    var newState;

    if (state == 'favorite') {
      newState = 'nonfavorite';
    } else {
      newState = 'favorite';
    }

    buttons.each(function (idx, button) {
      setButton($PBJQ(button), newState);
    });

    renderFavoriteCount();
    setAllOrNoneStarStates();

    syncWithServer(function () {
      // If anything goes wrong while saving, refresh from the server.
      loadFromServer();
    });
  });

  $PBJQ(container).on('click', '.tab-btn', function () {
    $PBJQ('.tab-btn', container).removeClass('active').attr('aria-selected', 'false').attr('tabindex', '-1');
    $PBJQ(this).addClass('active').attr('aria-selected', 'true').attr('tabindex', '0');

    var panel = $PBJQ(this).data('tab-target');

    $PBJQ('.tab-box').hide();
    $PBJQ(container).trigger('tab-shown', panel);
    $PBJQ('#' + panel).show();
  });

  // Arrow and spacebar nav for tabs
  $PBJQ(container).on('keydown', '.tab-btn', function (e) {
    if (e.keyCode == 32) {
      $PBJQ(this).click();
      e.preventDefault();
    }
    if (e.keyCode == 37) {
      $PBJQ("[aria-selected=true]").prev().click().focus();
      e.preventDefault();
    }
    if (e.keyCode == 38) {
      $PBJQ("[aria-selected=true]").prev().click().focus();
      e.preventDefault();
    }
    if (e.keyCode == 39) {
      $PBJQ("[aria-selected=true]").next().click().focus();
      e.preventDefault();
    }
    if (e.keyCode == 40) {
      $PBJQ("[aria-selected=true]").next().click().focus();
      e.preventDefault();
    }
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

      $PBJQ('#noFavoritesToShow').hide();
      $PBJQ('#favoritesToShow').hide();

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
        var dragHandle = $PBJQ('<a href="javascript:void(0);" class="fav-drag-handle"><i class="fa fa-bars"></i></a>');

        // Hide the tool dropdown
        $PBJQ('.toolMenus', favoriteItem).remove();

        // Show a drag handle
        favoriteItem.append(dragHandle);

        list.append(favoriteItem);

        // Make sure the item is visible, just in case it was hidden on the other tab
        favoriteItem.show();
      });

      if (list.find('li').length == 0) {
        // No favorites are present
        $PBJQ('#noFavoritesToShow').show();
      } else {
        $PBJQ('#favoritesToShow').show();
      }

      var highlightMaxItems = function () {
        var items = $PBJQ('.organize-favorite-item');

        items.removeClass('site-favorite-is-past-max');
        $PBJQ('.favorites-max-marker').remove();

        $PBJQ.each(items, function (idx, li) {
          if (idx >= maxFavoriteEntries) {
            $PBJQ(li).addClass('site-favorite-is-past-max');
          }

          if (idx == maxFavoriteEntries) {
            $PBJQ(li).before($PBJQ('<li class="favorites-max-marker"><i class="fa fa-warning warning-icon"></i> ' + $PBJQ('#maxFavoritesLimitReachedText').text() + '</li>'));
          }
        });
      };

      highlightMaxItems();

      list.keyboardSortable({
        items: "li:not(.favorites-max-marker)",
        handle: ".fav-drag-handle",
        update: function () {
          // Rehighlight the first N items
          highlightMaxItems();

          // Update our ordering based on the new selection
          favoritesList = list.find('.organize-favorite-item').map(function () {
            return $PBJQ(this).data('site-id');
          }).toArray();

          // and send it all to the server
          syncWithServer();
        }
      });

      list.disableSelection();

      $PBJQ('#autoFavoritesEnabled').attr('aria-checked', autoFavoritesEnabled);
      $PBJQ('#organizeFavorites .onoffswitch').show();
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
      var siteId = $PBJQ(self).data('site-id');
      returnElementToOriginalPositionIfPossible(siteId)

      var newIndex = favoritesList.indexOf(siteId);

      if (newIndex == 0) {
        $PBJQ('#organizeFavoritesList').prepend(li);
      } else if (newIndex > 0) {
        // Put it into the right position (note: nth-child starts indexing at 1)
        $PBJQ('#organizeFavoritesList li:nth-child(' + newIndex + ')').after(li);
      } else {
        // Just tack it on the end
        $PBJQ('#organizeFavoritesList').append(li);
      }

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

    setAllOrNoneStarStates();
    renderFavoriteCount();

    syncWithServer(function () {
      // If anything goes wrong while saving, refresh from the server.
      loadFromServer();
    });

  });

  $PBJQ("#autoFavoritesEnabled").click(function() {
	$PBJQ(this).attr('aria-checked', function(index, clicked) {
		var pressed = (clicked === 'true');
		return String(!pressed);
	});
	$PBJQ(this).trigger('change');
  });

  $PBJQ('#autoFavoritesEnabled').on('change', function () {
    autoFavoritesEnabled = $PBJQ(this).attr('aria-checked') === 'true';

    $PBJQ('.favorites-help-text').hide();

    if (autoFavoritesEnabled) {
      $PBJQ('.favorites-help-text.autofavorite-enabled').show();
    } else {
      $PBJQ('.favorites-help-text.autofavorite-disabled').show();
    }

    syncWithServer();
    return true;
  })

  $PBJQ('.otherSitesMenuClose').on('click', function () {
    // Close the pane
    dhtml_view_sites();
  });

});
