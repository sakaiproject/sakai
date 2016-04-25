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
    var origin = $PBJQ(this).position();
    $PBJQ(this).siblings('.Mrphs-directUrl').toggleClass('active').css( { 'left' : origin.left + 'px' } );
    e.preventDefault();
  });
  $PBJQ('.Mrphs-directUrl .dropDown_close').click( function( e ){
    $PBJQ(this).parent().toggleClass('active');
    e.preventDefault();
  });
});