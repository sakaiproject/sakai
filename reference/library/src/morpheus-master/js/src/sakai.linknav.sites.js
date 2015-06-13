/*
	If there's too many sites on siteNav for sreen size we should organize them a little.
*/
var setupLinkNav = function(){
	var linknav 				 = document.getElementById('Mrphs-sites-nav'); // need real DOM Node, not jQuery wrapper
	
	var h = $PBJQ("#Mrphs-sites-nav ul").height();
	var howManyHidden = 0;
	
	$PBJQ(".Mrphs-sitesNav__menuitem", "#linkNav").each(function(){
	    if ( $PBJQ(this).position().top > h){ howManyHidden++ }
	});

  if( howManyHidden !== 0 ){
  	$PBJQ('#linkNav').addClass( 'scrolled-sites' );
  	$PBJQ('#how-many-hidden').text( howManyHidden );
  }else{
  	$PBJQ('#linkNav').removeClass( 'scrolled-sites' );
  }
};

$PBJQ(document).ready(function(){
	setupLinkNav();
	$PBJQ('#show-all-sites').on('click', function(){
		$PBJQ('#linkNav').toggleClass('opened');
		$PBJQ(this).toggleClass('opened');
	});
	$( window ).resize(function() {
		setupLinkNav();
	});
});