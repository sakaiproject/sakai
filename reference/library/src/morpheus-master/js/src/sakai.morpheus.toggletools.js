/**
* For toggling the Minimize and Maximize tools menu in Morpheus: Adds classes to the <body> and changes the label text for accessibility
*/

function toggleMinimizeNav(){

  $PBJQ('body').toggleClass('Mrphs-toolMenu-collapsed');

  var el = $PBJQ(this);
  var label = $PBJQ('.accessibility-btn-label' , el);

  el.toggleClass('min max');
  
  if (label.text() == el.data("title-expand") || collapsed) {
	document.cookie = "sakai_nav_minimized=false; path=/";
	collapsed = false;
	label.text(el.data("text-original"));
    el.attr('title', (el.data("text-original")));
    el.attr('aria-pressed', true);
  } else {
	document.cookie = "sakai_nav_minimized=true; path=/";
	collapsed = true;
	el.data("text-original", label.text());
    label.text(el.data("title-expand"));
    el.attr('title', (el.data("title-expand")));
    el.attr('aria-pressed', false);
  }
}

$PBJQ(".js-toggle-nav").on("click", toggleMinimizeNav);

var collapsed = false;

var initialOffset;
var $window = $PBJQ(window),
	offset 	= $PBJQ("#toolMenu").offset(),
	$tools	= $PBJQ("#toolMenu"),
	padding	= $PBJQ(".Mrphs-siteHierarchy").height() + $PBJQ(".Mrphs-topHeader").height();

$PBJQ(document).ready(function(){
	if(getCookieVal('sakai_nav_minimized') === 'true') {
		$PBJQ(".js-toggle-nav").click();
		collapsed = true;
	}
	initialOffset = $PBJQ("#toolMenu").offset().top - $window.scrollTop();
	$PBJQ(window).scroll(function(){
		var follow = ($window.height()- padding) > $tools.height();
		var _top   = 0;
		if( $PBJQ("#toggleSubsitesLink").length > 0 ){
			_top = (-1 * ( $PBJQ('#toolMenu').height() - $PBJQ("#toggleSubsitesLink").position().top ) );
		}
		if( $PBJQ("#toolMenuWrap").css('position') !== 'fixed' && follow ) {
			if($window.scrollTop() > offset.top ) {
				$PBJQ("#toolMenu").stop().animate({
	                top: $window.scrollTop() - offset.top
	            });
				$PBJQ("#subSites.floating").css('top', ( $window.scrollTop() - offset.top + _top ) );
			} else {
				$PBJQ("#toolMenu").stop().animate({
					top: 0
	            });
	            $PBJQ("#subSites.floating").css('top', _top );
			}
		}else{
	        $PBJQ("#subSites.floating").css('top', _top );
		}
	});

	// Shows or hides the subsites in a popout div. This isn't used unless
	// portal.showSubsitesAsFlyout is set to true in sakai.properties.
	$PBJQ("#toggleSubsitesLink").click(function (e) {
	  var subsitesLink = $PBJQ(this);
	  if($PBJQ('#subSites').css('display') == 'block') {
	    $PBJQ('#subSites').hide();
	    $PBJQ('#subSites').removeClass('floating');
	  } else {
	    var position = subsitesLink.position();
	    var _top = ( -1 * ( $PBJQ('#toolMenu').height() - position.top ) );
	    $PBJQ('#subSites').css({'display': 'block','left': position.left + subsitesLink.width() + 6 + 'px','top': _top + 'px'});
	    $PBJQ('#subSites').addClass('floating');
	  	if( $PBJQ("#toggleSubsitesLink").position().top < 240 ){
	  		$PBJQ("#subSites.floating").addClass('ontop');
	  	}
	  }
	});

});

function getCookieVal(cookieName) {
	var cks = document.cookie.split(';');
	for (var i = 0; i < cks.length; ++i) {
		var curCookie = (cks[i].substring(0,cks[i].indexOf('='))).trim();
		if(curCookie === cookieName) {
			return ((cks[i].split('='))[1]).trim();;
		}
	}
	return undefined;
}
