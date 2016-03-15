/**
 * For Responsive Menus in Morpheus: Adds classes to the <body>
 */

function toggleToolsNav(event){
  if (event) {
    event.preventDefault();
  }
  $PBJQ('body').toggleClass('toolsNav--displayed');

}

$PBJQ(document).ready(function(){
  $PBJQ('i.clickable', '#roleSwitch').click( function(){
    $PBJQ(this).next('select').toggleClass('active');
  });
});

$PBJQ(".js-toggle-tools-nav", "#skipNav").on("click", toggleToolsNav);
