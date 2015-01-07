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