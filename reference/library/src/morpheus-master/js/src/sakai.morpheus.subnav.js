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
      $PBJQ(this).parent().find(".Mrphs-sitesNav__dropdown").trigger('click',[true]);

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

  $PBJQ("ul.Mrphs-sitesNav__menu li span.Mrphs-sitesNav__dropdown").click(function(e, focusFirstLink) {

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
    $PBJQ(this).toggleClass("Mrphs-sitesNav__dropdown--hover"); //On hover over, add 
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
