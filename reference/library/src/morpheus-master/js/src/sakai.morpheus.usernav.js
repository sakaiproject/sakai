/**
 * Toggle user nav in header: 
 */

function toggleUserNav(){
  event.preventDefault();
  $PBJQ('.Mrphs-userNav__subnav').toggleClass('is-hidden');
}

$PBJQ(".js-toggle-user-nav a#loginUser", "#loginLinks").on("click", toggleUserNav);
$PBJQ(".js-toggle-user-nav .Mrphs-userNav__drop", "#loginLinks").on("click", toggleUserNav);