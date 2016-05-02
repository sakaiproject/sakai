/**
 * For Responsive Menus in Morpheus: Adds classes to the <body>
 */

function toggleToolsNav(event){
  if (event) {
    event.preventDefault();
  }
  $PBJQ('body').toggleClass('toolsNav--displayed');

}

$PBJQ(document).ready(function(){
  $PBJQ('i.clickable', '#roleSwitch').click( function(){
    $PBJQ(this).next('select').toggleClass('active');
  });

  $PBJQ('#roleSwitchSelect').on("change", function(){

  	if( $PBJQ('option:selected', this ).text() !== '' ){
  		document.location = $PBJQ('option:selected', this ).val();
  	}else{
  		$PBJQ(this)[0].selectedIndex = 0;
  	}

  });

});

$PBJQ(".js-toggle-tools-nav", "#skipNav").on("click", toggleToolsNav);
