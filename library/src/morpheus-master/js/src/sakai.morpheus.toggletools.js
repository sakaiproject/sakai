/**
* For toggling the Minimize and Maximize tools menu in Morpheus: Adds classes to the <body>
*/

portal = portal || {};

if (portal.toolsCollapsed === undefined) {
	portal.toolsCollapsed = false;
}

portal.updateToolsCollapsedPref = function (collapsed) {
	if (portal.user.id) {
		var url = '/direct/userPrefs/updateKey/' + portal.user.id + '/sakai:portal:sitenav?toolsCollapsed=' + collapsed;
		$PBJQ.ajax(url, {method: 'PUT', cache: false});
	}
};

portal.updateMaximisedToolsPref = function (maximised) {

	if (portal.user.id) {
		var url = '/direct/userPrefs/updateKey/' + portal.user.id + '/sakai:portal:sitenav?toolMaximised=' + maximised;
		$PBJQ.ajax(url, {method: 'PUT', cache: false});
	}
};

portal.maximiseTool = function () {

  document.getElementsByTagName("body").item(0).classList.add("tool-maximised");
  portal.updateMaximisedToolsPref(true);
  document.querySelectorAll("sakai-maximise-button").forEach(e => e.setMaximised());
  $PBJQ(document).off('keyup.usernav');
}

portal.minimiseTool = function () {

  document.getElementsByTagName("body").item(0).classList.remove("tool-maximised");
  portal.updateMaximisedToolsPref(false);
  document.querySelectorAll("sakai-maximise-button").forEach(e => e.setMinimised());
}

portal.toggleMinimizeNav = function () {

  $PBJQ("body").toggleClass("Mrphs-toolMenu-collapsed");

  // Remove any popout div for subsites.  Popout only displayed when portal.showSubsitesAsFlyout is set to true.
  $PBJQ('#subSites.floating').css({'display': 'none'});

  var el = $PBJQ("#toolsNav-toggle-li button");
  el.toggleClass('min max').parent().toggleClass('min max');

  if (portal.toolsCollapsed) {
    portal.updateToolsCollapsedPref(false);
    portal.toolsCollapsed = false;
    el.attr('aria-pressed', false);
  } else {
    portal.updateToolsCollapsedPref(true);
    portal.toolsCollapsed = true;
    el.attr('aria-pressed', true);
  }
};

const indicator = document.querySelector("#maximised-indicator a");
indicator && indicator.addEventListener("click", portal.minimiseTool);

$PBJQ("#toolsNav-toggle-li button").on("click", portal.toggleMinimizeNav);

$PBJQ(document).ready(function () {
//Shows or hides the subsites in a popout div. This isn't used unless
// portal.showSubsitesAsFlyout is set to true in sakai.properties.
    $PBJQ("#toggleSubsitesLink").click(function (e) {
        var subsitesLink = $PBJQ(this);
        if ($PBJQ('#subSites').css('display') == 'block') {
            $PBJQ('#subSites').hide();
            $PBJQ('#subSites').removeClass('floating');
        } else {
            var position = subsitesLink.position();
            var _top = ( -1 * ( $PBJQ('#toolMenu').height() - position.top ) );
            var subsitesPosition = ( MorpheusViewportHelper.isPhone() ) ? {
            	'display': 'block',
            	'top': 0,
            	'overflow' : 'hidden'
            }:{
            	'display': 'block',
            	'left': $PBJQ('#toolMenu').width() + 7 + 'px',	// width of arrow border
            	'top': _top + 'px'
            }
            $PBJQ('#subSites').css(subsitesPosition);
            $PBJQ('#subSites').addClass('floating');

            // focus on first subsite for accessibility recommendations
            $PBJQ('#subSites').find('li a').first().focus();

            if ($PBJQ("#toggleSubsitesLink").position().top < 240) {
                $PBJQ("#subSites.floating").addClass('ontop');
            }
        }
    });
});
