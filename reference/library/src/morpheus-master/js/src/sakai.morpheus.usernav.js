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

    var overlay = $PBJQ('<div class="user-dropdown-overlay" />');
    overlay.on('click', function (e) {toggleUserNav(e)});

    $PBJQ('body').prepend(overlay);

    // ESC key also closes it
    $PBJQ(document).on('keyup',userNavEscHandler);

  } else {
    $PBJQ('.user-dropdown-overlay').remove();
    $PBJQ(document).off('keyup',userNavEscHandler);    
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

var header = $(".Mrphs-topHeader");
var currentHeaderWidth = -1;
var mainHeaderSize  = $(header).height();

$PBJQ(document).ready( function(){
	
  $(header).data("sticked",false);
	
  if( $PBJQ('.Mrphs-hierarchy--parent-sites').length > 0 && $PBJQ(window).width() <= 800 ){
    $PBJQ('#content').css( 'margin-top', ( parseInt( $PBJQ('#content').css('margin-top').replace('px', '') ) +  $PBJQ('.Mrphs-hierarchy--parent-sites').outerHeight(true) ) + 'px' );
  }
  
  $PBJQ(window).resize(function() {
	  currentHeaderWidth = $(".Mrphs-mainHeader").width();
	  $(header).css('height', 'auto');
	  mainHeaderSize = $(header).height();
  });
  
  $PBJQ(window).scroll(function(){
	if(currentHeaderWidth > 799) {
		var size = 0;
		var stick = (($(document).height() - $(window).height()) > $(header).height()) === true;
		if($(window).scrollTop() > 0) {
		  if($(header).data("sticked") === false && stick === true) {
		    $(header).data("sticked",true);
			$(".Mrphs-mainHeader").addClass("is-fixed");
			$(header).stop().animate({
				height: $('.is-fixed').css('height')	
			}, 200);
		  }
		} else {
		  $(".Mrphs-mainHeader").removeClass("is-fixed");
		  $(header).data("sticked",false);
		  $(header).css('height', null);
		  $(header).stop().animate({
			 height: mainHeaderSize 
		  }, 200);
		}
		animateToolBar();
	} else $(".Mrphs-mainHeader").removeClass("is-fixed");
  });
  
  currentHeaderWidth = $(".Mrphs-mainHeader").width();
  
});

