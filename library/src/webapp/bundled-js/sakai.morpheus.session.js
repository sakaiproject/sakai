/**
 * For Session and Timeouts in Morpheus
 */

//For SAK-13987
//For SAK-16162
//Just use the EB current.json as the session id rather than trying to do a search/replace
var sessionId = "current";
var sessionTimeOut;
var timeoutDialogEnabled = false;
var timeoutDialogFragment;
var timeoutDialogWarningTime;
var timeoutLoggedoutUrl;
var timeoutPortalPath;

$PBJQ(document).ready(function(){

  // note a session exists whether the user is logged in or no
  if (portal.loggedIn && portal.timeoutDialog) {
    setTimeout('setup_timeout_config();', 300000);
  }

});

var setup_timeout_config = function(){

  if ( ! portal || ! portal.timeoutDialog ) return; // SAK-42250
  timeoutDialogEnabled = portal.timeoutDialog.enabled;
  timeoutDialogWarningTime = portal.timeoutDialog.seconds;
  timeoutLoggedoutUrl = portal.loggedOutUrl;
  timeoutPortalPath = portal.portalPath;

  if (timeoutDialogEnabled == true) {
    poll_session_data();
  }

}

var poll_session_data = function(){

  $PBJQ.ajax({
    url: "/direct/session/" + sessionId + ".json?auto=true", //auto=true makes it not refresh the session lastaccessedtime
    dataType: "json",
    cache: false,
    success: function(data){
      //get the maxInactiveInterval in the same ms
      data.maxInactiveInterval = data.maxInactiveInterval * 1000;

      if (data.active && data.userId != null && data.lastAccessedTime + data.maxInactiveInterval > data.currentTime) {

        //User is logged in, so now determine how much time is left
        var remaining = data.lastAccessedTime + data.maxInactiveInterval - data.currentTime;

        //If time remaining is less than timeoutDialogWarningTime minutes, show/update dialog box
        if (remaining < timeoutDialogWarningTime * 1000) {

          //we are within 5 min now - show popup
          min = Math.round(remaining / (1000 * 60));

          if (!timeoutDialogFragment) {
            $PBJQ.ajax({ url: "/portal/timeout?auto=true", cache: true, dataType: "text"})
              .done(function(htmlSegment) {
                timeoutDialogFragment = htmlSegment;
                show_timeout_alert(min);
              })
              .fail(function() {
                timeoutDialogEnabled = false;
              });
          } else {
            show_timeout_alert(min);
          }

          clearTimeout(sessionTimeOut);
          sessionTimeOut = setTimeout("poll_session_data()", Math.max( (remaining/2), (1000 * 60) ) );

        } else {

          //more than timeoutDialogWarningTime min away
          clearTimeout(sessionTimeOut);
          sessionTimeOut = setTimeout("poll_session_data()", (remaining - timeoutDialogWarningTime * 1000));

        }
      } else if (data.userId == null) {
          // if data.userId is null, the session is done; redirect the user to logoutUrl
          location.href = timeoutLoggedoutUrl;
          
        } else {
          //the timeout length has occurred, but there is a slight delay, do this until there isn't a user.
          sessionTimeOut = setTimeout("poll_session_data()", 1000 * 10);
        }
    },
 
    error: function(XMLHttpRequest, status, error){
      // We used to to 404 handling here but now we should always get good session data.
    }

  });
}

function keep_session_alive(){
  dismiss_session_alert();
  $PBJQ.get(timeoutPortalPath);
}

var dismiss_session_alert = function(){
  removeDHTMLMask();
  $PBJQ("#timeout_alert_body").remove();
}

function show_timeout_alert(min){
  if (!timeoutDialogEnabled || !timeoutDialogFragment) {
    return;
  }
  
  if (!$PBJQ("#portalMask").get(0)) {
    createDHTMLMask(dismiss_session_alert);
    $PBJQ("#portalMask").css("z-index", 1000);
  }
  if ($PBJQ("#timeout_alert_body").get(0)) {
    //its there, just update the min
    $PBJQ("#timeout_alert_body span").html(min);
  }
  else {
    var dialog = timeoutDialogFragment.replace("{0}", min);
    $PBJQ("body").append(dialog);
  }
}
