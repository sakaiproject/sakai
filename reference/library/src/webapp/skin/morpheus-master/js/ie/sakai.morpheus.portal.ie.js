//$PBJQ(document).ready(function() {
  setupSiteNav();
  // no longer relies on sakai_portal_ie_Detection script is called by conditional comment
  // sakai_portal_ie_detected should have been set above
  //if (sakai_portal_ie_detected) {
  // SAK-22308
  //if ($PBJQ.browser.msie && jQuery('ul#topnav[role="navigation"]') && jQuery('h1#sitetabs')) {

  var $ul_topnav = $PBJQ('ul#topnav');
  var aria_label_val = $ul_topnav.attr('aria-label');
  $PBJQ('h1#sitetabs').attr('role','navigation').attr('aria-label', aria_label_val);
  $PBJQ('div#linkNav').removeAttr('role').removeAttr('aria-labelledby');
  $ul_topnav.removeAttr('aria-label');
    //}
//});
