/**
 * ESC handler to dismiss user nav
 */

function userNavEscHandler(e){
  if (e.keyCode === 27) { // esc keycode
    toggleUserNav(e);
  }
}

/**
 * Toggle user nav in header: 
 */

function toggleUserNav(event){
  event.preventDefault();
  $PBJQ('.Mrphs-userNav__subnav').toggleClass('is-hidden');

  if (!$PBJQ('.Mrphs-userNav__subnav').hasClass('is-hidden')) {
    // Add an invisible overlay to allow clicks to close the dropdown

    var overlay = $('<div class="user-dropdown-overlay" />');
    overlay.on('click', function (e) {toggleUserNav(e)});

    $('body').prepend(overlay);

    // ESC key also closes it
    $(document).on('keyup',userNavEscHandler);

  } else {
    $('.user-dropdown-overlay').remove();
    $(document).off('keyup',userNavEscHandler);    
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
