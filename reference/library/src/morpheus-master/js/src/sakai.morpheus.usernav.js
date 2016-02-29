/**
 * Toggle user nav in header: 
 */

function toggleUserNav(event){
  event.preventDefault();
  $PBJQ('.Mrphs-userNav__subnav').toggleClass('is-hidden');
  $PBJQ('.Mrphs-userNav__drop').toggleClass('is-clicked');

  if ($PBJQ('.Mrphs-userNav__drop').hasClass('is-clicked')) {
    // Add an invisible overlay to allow clicks to close the dropdown

    var overlay = $('<div class="user-dropdown-overlay" />');
    overlay.css({width: '100%', height: '100%', position: 'fixed', zIndex: 50});
    overlay.on('click', function () { $(event.target).trigger('click'); });

    $('body').prepend(overlay);
  } else {
    $('.user-dropdown-overlay').remove();
  }
}

 // Logout Confirm
  $PBJQ('#loginLink1').click(function(e){
    if ($PBJQ(this).attr("data-warning") !== "" && !confirm($PBJQ(this).attr("data-warning"))){
	e.preventDefault();
    }
  });


$PBJQ(".js-toggle-user-nav a#loginUser > .Mrphs-userNav__drop-btn", "#loginLinks").on("click", toggleUserNav);
$PBJQ(".js-toggle-user-nav .Mrphs-userNav__drop-btn", "#loginLinks").on("click", toggleUserNav);
