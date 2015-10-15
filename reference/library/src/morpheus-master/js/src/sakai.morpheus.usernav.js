/**
 * Toggle user nav in header: 
 */

function toggleUserNav(event){
  event.preventDefault();
  $PBJQ('.Mrphs-userNav__subnav').toggleClass('is-hidden');
  $PBJQ(this).toggleClass('is-clicked');
}

 // Logout Confirm
  $PBJQ('#loginLink1').click(function(e){
    if ($PBJQ(this).attr("data-warning") !== "" && !confirm($PBJQ(this).attr("data-warning"))){
	e.preventDefault();
    }
  });


$PBJQ(".js-toggle-user-nav a#loginUser > .Mrphs-userNav__drop", "#loginLinks").on("click", toggleUserNav);
$PBJQ(".js-toggle-user-nav .Mrphs-userNav__drop", "#loginLinks").on("click", toggleUserNav);
