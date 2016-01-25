/*
	If there's too many sites on siteNav for sreen size we should organize them a little.
*/
var setupLinkNav = function(){
	var linknav  = document.getElementById('Mrphs-sites-nav'); // need real DOM Node, not jQuery wrapper
	var howManyHidden = 0;
	if($PBJQ("#linkNav").css("display")=="block"){
		// for Desktop view get all course 
		$PBJQ(".Mrphs-sitesNav__menuitem", "#linkNav").each(function(){
		    if ( $PBJQ(this).position().top > $PBJQ("#linkNav").height()){ howManyHidden++ }
		});

	}else{
		// for mobile view get all course excluding myworkspace
		$PBJQ(".Mrphs-sitesNav__menuitem", "#linkNav").each(function(){
		     howManyHidden++;
		});
	}

	/*if( howManyHidden !== 0 ){
		$PBJQ('#show-all-sites').css( 'display','block' );
	  	$PBJQ('#linkNav').addClass( 'scrolled-sites' );
		$PBJQ('#show-all-sites-mobile').css( 'display','block' );
	}else{
	  	$PBJQ('#linkNav').removeClass( 'scrolled-sites' );
		$PBJQ('#show-all-sites').css( 'display','none' );
	}*/
};

$PBJQ(document).ready(function(){
	setupLinkNav();
	$PBJQ( window ).resize(function() {
		setupLinkNav();
	});
});
