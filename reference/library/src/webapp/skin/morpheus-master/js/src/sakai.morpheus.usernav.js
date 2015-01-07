/**
 * Toggle user nav in header: 
 */

function toggleUserNav(){
  $PBJQ('.Mrphs-userNav__subnav').toggleClass('is-hidden');
}

$PBJQ(".js-toggle-user-nav", "#loginLinks").on("click", toggleUserNav);