/**
* For toggling the Minimize and Maximize tools menu in Morpheus: Adds classes to the <body>
*/

function toggleMinimizeNav(){

  $PBJQ('body').toggleClass('Mrphs-toolMenu-collapsed');
  // Remove any popout div for subsites.  Popout only displayed when portal.showSubsitesAsFlyout is set to true.
  $PBJQ('#subSites.floating').css({'display': 'none'});

  var el = $PBJQ(this);
  el.toggleClass('min max').parent().toggleClass('min max');

  if (collapsed) {
    document.cookie = "sakai_nav_minimized=false; path=/";
    collapsed = false;
    el.attr('aria-pressed', false);
  } else {
    document.cookie = "sakai_nav_minimized=true; path=/";
    collapsed = true;
    el.attr('aria-pressed', true);
  }
}

$PBJQ(".js-toggle-nav").on("click", toggleMinimizeNav);

var collapsed = false;

$PBJQ(document).ready(function(){
	if(getCookieVal('sakai_nav_minimized') === 'true') {
		$PBJQ(".js-toggle-nav").click();
		collapsed = true;
	}
});

function getCookieVal(cookieName) {
	var cks = document.cookie.split(';');
	for (var i = 0; i < cks.length; ++i) {
		var curCookie = (cks[i].substring(0,cks[i].indexOf('='))).trim();
		if(curCookie === cookieName) {
			return ((cks[i].split('='))[1]).trim();;
		}
	}
	return undefined;
}
