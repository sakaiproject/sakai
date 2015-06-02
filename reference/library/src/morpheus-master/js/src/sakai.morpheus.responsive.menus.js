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

$PBJQ(document).ready(function(){
	if( $PBJQ('#linkNav').length == 0 ){
		$PBJQ('.js-toggle-sites-nav').hide();
	}
});

$PBJQ(".js-toggle-sites-nav", "#skipNav").on("click", toggleSitesNav);
$PBJQ(".js-toggle-tools-nav", "#skipNav").on("click", toggleToolsNav);
