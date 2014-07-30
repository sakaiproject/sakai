/**
 * For Responsive Menus in Morpheus: Adds classes to the <body>
 */

function toggleToolsNav(){
  event.preventDefault();
  $('body').toggleClass('nav-tools-displayed');
}

function toggleSitesNav(){
  event.preventDefault();
  $('body').toggleClass('nav-sites-displayed');
}

$(".js-toggle-sites-nav", "#skipNav").on("click", toggleSitesNav);
$(".js-toggle-tools-nav", "#skipNav").on("click", toggleToolsNav);