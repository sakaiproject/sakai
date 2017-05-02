/**
 * For Skip Nav in Morpheus
 */

var setupSkipNav = function(){
  // function called from site.vm to enable skip links for all browsers
   $PBJQ('#skipNav a.Mrphs-skipNav__link').click(function(){
     var target = $PBJQ(this).attr('href');
    $PBJQ(target).attr('tabindex','-1').focus();
   });
};

$PBJQ( document ).ready(function() {
	
	var lastScrollTop = 0;

	$PBJQ(document).scroll(function(event){
	   var st = $PBJQ(this).scrollTop();
	   if (st > lastScrollTop && st > 90 ){
	   	   $PBJQ(".Mrphs-topHeader").addClass('moving');
	   	   $PBJQ(".Mrphs-siteHierarchy").addClass('moving');
	   	   $PBJQ(".Mrphs-toolsNav__title--current-site").addClass('moving');
	   	   $PBJQ(".Mrphs-skipNav__menu").addClass('moving');
	   	   $PBJQ(".Mrphs-sitesNav__menuitem--myworkspace").addClass('moving');
	   } else if( st > 90 || st == 0) {
	   	    $PBJQ(".Mrphs-topHeader").removeClass('moving');
	   	   	$PBJQ(".Mrphs-siteHierarchy").removeClass('moving');
	   	   	$PBJQ(".Mrphs-toolsNav__title--current-site").removeClass('moving');
	   	   	$PBJQ(".Mrphs-skipNav__menu").removeClass('moving');
	   	   	$PBJQ(".Mrphs-sitesNav__menuitem--myworkspace").removeClass('moving');
	   }
	   lastScrollTop = st;
	});

});