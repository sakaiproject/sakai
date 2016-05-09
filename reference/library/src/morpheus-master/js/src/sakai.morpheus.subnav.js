/**
 * Sets up subnav on the sitenav
 */

var closeAllDropdownMenus = function() {
  $PBJQ('.Mrphs-sitesNav__menuitem').removeClass('dropdown-is-visible');
  $PBJQ('.Mrphs-sitesNav__menuitem').find('.is-visible').removeClass('is-visible');
  $PBJQ('.Mrphs-sitesNav__menuitem').find('.is-clicked').removeClass('is-clicked');

  $PBJQ('.sitenav-dropdown-overlay').remove();
};

var buildDropdownMenu = function(container, siteId, callback) {
  var navsubmenu = "<ul class=\"Mrphs-sitesNav__submenu\" role=\"menu\">";
  var maxToolsInt = parseInt($PBJQ('#linkNav').attr('data-max-tools-int'));
  var maxToolsText = $PBJQ('#linkNav').attr('data-max-tools-anchor');
  var goToSite = '<li class=\"Mrphs-sitesNav__submenuitem\"><a role=\"menuitem\" href=\"' + portal.portalPath + '/site/' + siteId + '\" title=\"' + maxToolsText + '\"><span class=\"toolMenuIcon icon-sakai-see-all-tools\"></span>' + maxToolsText + '</a></li>';
  var siteURL = '/direct/site/' + siteId + '/pages.json';
  var currentSite = window.location.pathname.split('/').pop();

  $PBJQ.ajax({
    url: siteURL,
    dataType: "json",
    success: function(data){

      $PBJQ.each(data, function(i, item) {

    	// Ignore the tool if is not available
    	if (!item.tools || item.tools.length<=0) return;
    	
        // Check to see if this is the current tool in the site
        var isCurrent = "";
        if (currentSite == item.tools[0].id) {
          isCurrent = " is-current";
        }

        if (i <= maxToolsInt) {
          var li_template;

          if (item.toolpopup) {
            var link_attrs = ' role="menuitem" href="{{tool_url}}?sakai.popup=yes" title="{{item_title}}" onclick="window.open(\'{{item_toolpopupurl}}\');"';
            li_template = '<li class="Mrphs-sitesNav__submenuitem" >' +
              '<a class="Mrphs-sitesNav__submenuitem-icon"' + link_attrs + '><span class="toolMenuIcon icon-{{icon}}"></span></a>' +
              '<a class="Mrphs-sitesNav__submenuitem-title"' + link_attrs + '>{{item_title}}</a>' +
              '</li>';
          } else {
            var link_attrs = ' role="menuitem" href="{{tool_url}}" title="{{item_title}}"';

            li_template = '<li class="Mrphs-sitesNav__submenuitem{{is_current}}">' +
              '<a class="Mrphs-sitesNav__submenuitem-icon"' + link_attrs + '><span class="toolMenuIcon icon-{{icon}}"></span></a>' +
              '<a class="Mrphs-sitesNav__submenuitem-title"' + link_attrs + '>{{item_title}}</a>' +
              '</li>';
          }

          navsubmenu += (li_template
                         .replace(/{{tool_url}}/g, item.tools[0].url)
                         .replace(/{{item_title}}/g, item.title)
                         .replace(/{{item_toolpopupurl}}/g, item.toolpopupurl)
                         .replace(/{{icon}}/g, item.tools[0].toolId.replace(/\./gi, '-'))
                         .replace(/{{is_current}}/g, isCurrent));
        }
      });

      if((data.length - 1) > maxToolsInt) {
        navsubmenu += goToSite
      }

      navsubmenu += "</ul>"

      navsubmenu = $(navsubmenu);

      container.append(navsubmenu);

      addArrowNavAndDisableTabNav(navsubmenu);

      callback(navsubmenu);
    },

    error: function(XMLHttpRequest, status, error){
      // Something happened getting the tool list.
    }
  });
};


var setupSiteNav = function(){

  $PBJQ("ul.Mrphs-sitesNav__menu").each(function(){

    // Add an escape key handler to slide the page menu up
    $PBJQ(this).keydown(function(e) {
      if (e.keyCode == 27) {
        closeAllDropdownMenus();
      }
    });
  });

  $PBJQ(document).on('keydown', '.Mrphs-sitesNav__menu > li.Mrphs-sitesNav__menuitem > a',
                    function (e) {
                      if (e.keyCode == 40) {
                        // downarrow
                        e.preventDefault();
                        // Trigger click on the drop <span>, passing true to set focus on
                        // the first tool in the dropdown.
                        var dropdown = $PBJQ(this).parent().find(".Mrphs-sitesNav__dropdown");

                        if (dropdown.data('clicked')) {
                          // If the user has already triggered a click, give the
                          // AJAX a chance to finish.
                        } else {
                          dropdown.data('clicked', true);
                          dropdown.trigger('click', [true]);
                        }
                      } else if (e.keyCode == 27) {
                        // escape
                        e.preventDefault();
                        closeAllDropdownMenus();
                      }

                    });

  // focusFirstLink is only ever passed from the keydown handler. We
  // don't want to focus on click; it looks odd.
  $PBJQ("ul.Mrphs-sitesNav__menu li span.Mrphs-sitesNav__dropdown").click(function(e, focusFirstLink) {
    e.preventDefault()

    var jqObjDrop = $PBJQ(e.target);
    var container = jqObjDrop.closest('.Mrphs-sitesNav__menuitem');

    var dropdownWasShown = container.hasClass('dropdown-is-visible');

    // Hide any currently shown menus so we don't end up with multiple dropdowns shown
    closeAllDropdownMenus();

    if (dropdownWasShown) {
      // We've hidden the dropdown now, so all done.
      return;
    }

    var dropdownArrow = $PBJQ(this);

    var displayDropdown = function (navsubmenu) {
      // Mark the dropdown arrow and the menu itself as clicked
      dropdownArrow.addClass("is-clicked");
      container.addClass('dropdown-is-visible');

      // now display the menu
      navsubmenu.addClass('is-visible');

      if(focusFirstLink) {
        container.find('a.Mrphs-sitesNav__submenuitem-title').first().focus();
      }

      // Add an invisible overlay to allow clicks to close the dropdown
      var overlay = $('<div class="sitenav-dropdown-overlay" />');

      overlay.on('click', function (e) {
        closeAllDropdownMenus();
      });

      $('body').prepend(overlay);

      dropdownArrow.removeData('clicked');
    };

    if (!container.find('ul').length) {
      // We haven't yet built the dropdown menu for this item.  Do that now.
      buildDropdownMenu(container, jqObjDrop.attr('data-site-id'), displayDropdown);
    } else {
      displayDropdown(container.find('ul'));
    }

  }).hover(function(){
    $PBJQ(this).toggleClass("Mrphs-sitesNav__dropdown--hover"); //On hover over, add
  });
}

/* Callback is a function and is called after sliding up ul */
function addArrowNavAndDisableTabNav(ul) {
  ul.find('li a').attr('tabindex','-1').keydown(function (e) {
    var obj = $PBJQ(e.target);
    if (e.keyCode == 40) {
      // Down arrow.  Move to the next item, or loop around if we're at the bottom.
      e.preventDefault();
      var next = obj.closest('li').next();

      if (next.length === 0 || next.find('a.Mrphs-sitesNav__submenuitem-title').length == 0) {
        // loop around
        next = ul.find('li').first();
      }

      next.find('a.Mrphs-sitesNav__submenuitem-title').focus();

    } else if (e.keyCode == 38) {
      // Up arrow.  Move to the previous item, or loop around if we're at the top.
      e.preventDefault();
      var prev = obj.closest('li').prev();

      if (prev.length === 0) {
        // jump to the bottom
        prev = ul.find('a.Mrphs-sitesNav__submenuitem-title').closest('ul')
      }

      prev.find('a.Mrphs-sitesNav__submenuitem-title').focus();

    } else if (e.keyCode == 9) { // Suck up the menu if tab is pressed
        closeAllDropdownMenus();
    }
  });
}
