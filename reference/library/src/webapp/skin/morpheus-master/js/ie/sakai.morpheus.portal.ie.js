// no longer relies on sakai_portal_ie_Detection script is called by conditional comment
// Original Issue SAK-22308
var $ul_topnav = $PBJQ('ul#topnav');
var aria_label_val = $ul_topnav.attr('aria-label');
$PBJQ('h1#sitetabs').attr('role','navigation').attr('aria-label', aria_label_val);
$PBJQ('div#linkNav').removeAttr('role').removeAttr('aria-labelledby');
$ul_topnav.removeAttr('aria-label');