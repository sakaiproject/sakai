/**
* For toggling the Minimize and Maximize tools menu in Morpheus: Adds classes to the <body>
*/

portal = portal || {};

if (portal.toolsCollapsed === undefined) {
	portal.toolsCollapsed = false;
}

portal.updateToolsCollapsedPref = function (collapsed) {

	var url = '/direct/userPrefs/updateKey/' + portal.user.id + '/sakai:portal:sitenav?toolsCollapsed=' + collapsed;
	$PBJQ.ajax(url, {method: 'PUT', cache: false});
};

portal.toggleMinimizeNav = function () {

	$PBJQ('body').toggleClass('Mrphs-toolMenu-collapsed');
	// Remove any popout div for subsites.  Popout only displayed when portal.showSubsitesAsFlyout is set to true.
	$PBJQ('#subSites.floating').css({'display': 'none'});

	var el = $PBJQ(this);
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

$PBJQ(".js-toggle-nav").on("click", portal.toggleMinimizeNav);
