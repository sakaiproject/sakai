/**
 * For Responsive Menus in Morpheus: Adds classes to the <body>
 */

function toggleToolsNav(){
  event.preventDefault();
  $PBJQ('body').toggleClass('nav-tools-displayed');
}

function toggleSitesNav(){
  event.preventDefault();
  $PBJQ('body').toggleClass('nav-sites-displayed');
}

$PBJQ(".js-toggle-sites-nav", "#skipNav").on("click", toggleSitesNav);
$PBJQ(".js-toggle-tools-nav", "#skipNav").on("click", toggleToolsNav);
