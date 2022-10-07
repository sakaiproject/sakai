// ESC handler to dismiss user nav
function userNavEscHandler(e){

  if (e.keyCode === 27) { // esc keycode
    toggleUserNav(e);
  }
}

/**
 * Toggle user nav in header: 
 */

function toggleUserNav(event) {

  event.preventDefault();

  const dropBtn = $PBJQ('.Mrphs-userNav__drop-btn');

  $PBJQ('.Mrphs-userNav__subnav').toggleClass('is-hidden');

  if (!$PBJQ('.Mrphs-userNav__subnav').hasClass('is-hidden')) {
    // Add an invisible overlay to allow clicks to close the dropdown

    var overlay = $PBJQ('<div class="user-dropdown-overlay" />');
    overlay.on('click', function (e) {toggleUserNav(e)});

    $PBJQ('body').prepend(overlay);

    // ESC key also closes it
    $PBJQ(document).on('keyup.usernav',userNavEscHandler);
    dropBtn.attr('aria-expanded', 'true');
  } else {
    $PBJQ('.user-dropdown-overlay').remove();
    $PBJQ(document).off('keyup',userNavEscHandler);
    dropBtn.attr('aria-expanded', 'false');
    dropBtn.focus();
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

var header = $PBJQ(".Mrphs-topHeader");
var currentHeaderWidth = -1;

$PBJQ(document).ready( function(){

  $PBJQ(header).data("sticked",false);

  if( $PBJQ('.Mrphs-hierarchy--parent-sites').length > 0 && $PBJQ(window).width() <= 800 ){
    $PBJQ('#content').css( 'margin-top', ( parseInt( $PBJQ('#content').css('margin-top').replace('px', '') ) +  $PBJQ('.Mrphs-hierarchy--parent-sites').outerHeight(true) ) + 'px' );
  }
 
  $PBJQ(window).resize(function() {
      currentHeaderWidth = $PBJQ(".Mrphs-mainHeader").width();
  });
 
  $PBJQ(window).scroll(function(){
        var size = 0;
        var stick = (($PBJQ(document).height() - $PBJQ(window).height()) > $PBJQ(header).height()) === true;
        if($PBJQ(window).scrollTop() > 0) {
            if($PBJQ(header).data("sticked") === false && stick === true) {
                $PBJQ(header).data("sticked",true);
                $PBJQ(".Mrphs-mainHeader").addClass("is-fixed");
          }
        } else {
          $PBJQ(".Mrphs-mainHeader").removeClass("is-fixed");
          $PBJQ(header).data("sticked",false);
        }
  });
  
  currentHeaderWidth = $PBJQ(".Mrphs-mainHeader").width();

    $PBJQ('.Mrphs-headerLogo').on('click', function() {
        // scroll to top on banner click/touch
        document.body.scrollTop = 0;
        document.body.scrollLeft = 0;
        $PBJQ(window).trigger('scroll');
    });

  /////////////////////////////////////////////////
  // Add become user to body as a class
  ////////////////////////////////////////////////
  if (portal && portal.user && portal.user.impersonatorDisplayId) {
    $PBJQ("body").addClass("Mrphs-become-user-enabled");
  }
});
