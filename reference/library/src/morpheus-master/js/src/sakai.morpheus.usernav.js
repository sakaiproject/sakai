/**
 * Toggle user nav in header: 
 */

function toggleUserNav(event){
  event.preventDefault();
  $PBJQ('.Mrphs-userNav__subnav').toggleClass('is-hidden');
  $PBJQ('.Mrphs-userNav__drop').toggleClass('is-clicked');
}

 // Logout Confirm
  $PBJQ('#loginLink1').click(function(e){
    if ($PBJQ(this).attr("data-warning") !== "" && !confirm($PBJQ(this).attr("data-warning"))){
	e.preventDefault();
    }
  });


$PBJQ(".js-toggle-user-nav a#loginUser > .Mrphs-userNav__drop-btn", "#loginLinks").on("click", toggleUserNav);
$PBJQ(".js-toggle-user-nav .Mrphs-userNav__drop-btn", "#loginLinks").on("click", toggleUserNav);
