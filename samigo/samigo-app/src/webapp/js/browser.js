function isMacintosh_Netscape(){
    var userAgent = navigator.userAgent.toLowerCase();
    var is_netscape = (userAgent.indexOf("netscape") > -1);
    var is_mac = (userAgent.indexOf("macintosh") > -1)
    var is_mac_netscape = (is_mac && is_netscape);
    document.forms[0].elements['takeAssessmentForm:isMacNetscapeBrowser'].value=is_mac_netscape;
} 
