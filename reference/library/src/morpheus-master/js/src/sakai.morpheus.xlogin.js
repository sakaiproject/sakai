/* XLogin JS snippets */


$PBJQ(document).ready(function(){

  var xlogin = function(){

      var testEmail = /^[A-Z0-9._%+-]+@([A-Z0-9-]+\.)+[A-Z]{2,4}$/i;

      $PBJQ('#Mrphs-xlogin #eid').focusout( function(){
        if( testEmail.test( $PBJQ(this).val() ) ){
          $PBJQ("#Mrphs-xlogin #gravatar").attr("src","http://www.gravatar.com/avatar/" + MD5( $PBJQ(this).val().toLowerCase() ) );
          $PBJQ('#Mrphs-xlogin #gravatar').parent().addClass('gravatar');
        }else{
          $PBJQ("#Mrphs-xlogin #gravatar").removeAttr("src");
          $PBJQ('#Mrphs-xlogin #gravatar').parent().removeClass('gravatar');
        }
      });
  };

  if( $PBJQ('#Mrphs-xlogin').length == 1 ){
    xlogin();
  }

  if( $PBJQ('#loginLink2').length == 1 ){
    $PBJQ('#loginLink2').click( function( e ){

      $PBJQ('body').append('<div id="Mrphs-xlogin-container" />');
      $PBJQ('#Mrphs-xlogin-container').load('/portal/xlogin #Mrphs-xlogin',function(){
        $PBJQ('#Mrphs-xlogin-container').addClass('loaded');
        $PBJQ('#Mrphs-xlogin').addClass('loadedByAjax');
        xlogin();
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