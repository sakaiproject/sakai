/**
 * For Short URL toggles in Morpheus
 */

//handles showing either the short url or the full url, depending on the state of the checkbox 
//(if configured, otherwise returns url as-is as according to the url shortening entity provder)
function toggleShortUrlOutput(defaultUrl, checkbox, textbox) {    
  
  if($PBJQ(checkbox).is(':checked')) {
    
    $PBJQ.ajax({
      url:'/direct/url/shorten?path='+encodeURI(defaultUrl),
      dataType: "text",
      success: function(shortUrl) {
        $PBJQ('.'+textbox).val(shortUrl);
      }
    }); 
  } else {
    $PBJQ('.'+textbox).val(defaultUrl);
  }
}

$PBJQ(document).ready(function(){
  $PBJQ('.Mrphs-toolTitleNav__link--directurl').click( function( e ){
    var origin = $PBJQ(this).position(),
    	$dialog = $PBJQ(this).siblings('.Mrphs-directUrl'); 
    
    $dialog.toggleClass('active').css( { 'left' : origin.left + 'px' } );
    $dialog.attr('aria-expanded', 'true');
    $dialog.find('[tabindex]').first().focus();
    e.preventDefault();
  });
  
  $modal_container = $PBJQ('.Mrphs-directUrl');
  
  $modal_container.each( function (index) {
	 var $invokerTabs = $PBJQ(this).find('[tabindex]'); 
	 $invokerTabs.first().on('keydown', function (e) {
		if( e.keyCode === 9 && e.shiftKey ) {
		  $invokerTabs.last().focus();
		  e.preventDefault();
		  return false;
		}
	 });
	 $invokerTabs.last().on('keydown', function (e) {
		if( e.keyCode === 9 && !e.shiftKey ) {
		  $invokerTabs.first().focus();
		  e.preventDefault();
		  return false;
		}
	 });
  });

  $modal_container.last().on('keypress', function (e) {
	  if( e.keyCode() === 9 ) {
		  console.log("keypress:: primero");
		  $modal_container.fist().focus();
		  e.preventDefault();
		  return false;
	  }
  });
  
  $PBJQ('.Mrphs-directUrl .dropDown_close').on('click keypress', function( e ){
	var $dialog = $PBJQ(this).parent();
	
	$dialog.toggleClass('active');
	$dialog.removeAttr('aria-expanded');
	$PBJQ('.Mrphs-toolTitleNav__link--directurl[rel="#'+$dialog.attr('id')+'"]').focus();
    e.preventDefault();
  });
});