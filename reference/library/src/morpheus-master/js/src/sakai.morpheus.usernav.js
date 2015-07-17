/**
 * Toggle user nav in header: 
 */

function toggleUserNav(event){
  event.preventDefault();
  $PBJQ('.Mrphs-userNav__subnav').toggleClass('is-hidden');
  $PBJQ(this).toggleClass('is-clicked');
}

$PBJQ(".js-toggle-user-nav a#loginUser > .Mrphs-userNav__drop", "#loginLinks").on("click", toggleUserNav);
$PBJQ(".js-toggle-user-nav .Mrphs-userNav__drop", "#loginLinks").on("click", toggleUserNav);
