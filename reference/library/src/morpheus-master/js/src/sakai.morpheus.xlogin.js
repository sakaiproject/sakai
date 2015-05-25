/* XLogin over portal body */

$PBJQ(document).ready(function(){

  if( $PBJQ('#loginLink2').length == 1 ){

    $PBJQ('#loginLink2').click( function( e ){

      $PBJQ('body').append('<div id="Mrphs-xlogin-container" />');
      $PBJQ('#Mrphs-xlogin-container').load('/portal/xlogin #Mrphs-xlogin',function(){
        $PBJQ('#Mrphs-xlogin-container').addClass('loaded');
        $PBJQ('#Mrphs-xlogin').addClass('loadedByAjax');
      });
      $('.Mrphs-portalWrapper').addClass('blurry');

      $PBJQ('body').append('<div id="loginPortalMask" />');
      $PBJQ('#loginPortalMask').bgiframe();
      
      $PBJQ('#loginPortalMask').click(function(){
        $PBJQ('#loginPortalMask').remove();
        $PBJQ('#Mrphs-xlogin-container').remove();
        $('.Mrphs-portalWrapper').removeClass('blurry');
      });

      e.preventDefault();

    });
  }

});