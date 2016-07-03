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
	offset 	= $("#toolMenu").offset();

$PBJQ(document).ready(function(){
	if(getCookieVal('sakai_nav_minimized') === 'true') {
		$PBJQ(".js-toggle-nav").click();
		collapsed = true;
	}
	initialOffset = $("#toolMenu").offset().top - $window.scrollTop();
	$PBJQ(window).scroll(function(){
		if($("#toolMenuWrap").css('position') !== 'fixed') {
			var fromTop = $("#toolMenu").offset().top - $window.scrollTop();
			if($window.scrollTop() > offset.top) {
				$("#toolMenu").stop().animate({
	                top: $window .scrollTop() - offset.top
	            });
			} else {
				$("#toolMenu").stop().animate({
					top: 0
	            });
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
