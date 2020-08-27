var activeTabListenerAttached = false,// initial load
    handleVisibilityChange = function(){}; 

function updatePresenceTimeout(_ms, _go) {
    if (window.sakaiLastPresenceTimeOut !== null) {
        clearTimeout(window.sakaiLastPresenceTimeOut);
        window.sakaiLastPresenceTimeOut = null;
    }
    if (_go) {
        window.sakaiLastPresenceTimeOut = setTimeout('updatePresence()', _ms);
    }
}

function updatePresence() {
  if (!activeTabListenerAttached) {
    document.addEventListener("visibilitychange", handleVisibilityChange, false);
    activeTabListenerAttached = true;
  }

  if (document.hidden) {
    return;
  }

  $PBJQ.ajax({
    url: sakaiPresenceFragment,
    cache: false,
    dataType : 'text',
    success: function(frag){

      var $presenceIframe = $PBJQ("#presenceIframe");
      $presenceIframe.html(frag);

      var $presenceCount = $PBJQ("#presenceCount");

      if ($presenceIframe.is(':empty')) {
        $presenceCount.html(' ');
        $presenceCount.removeClass('present').addClass('empty');
        location.reload();
        return;
      }

      var userCount = $presenceIframe.find('.listUser').length;

      // No need to attrct attention you are alone

      if (userCount > 1) {
        $presenceCount.html(userCount + '');
        $presenceCount.removeClass('empty').addClass('present');
      }

      else if (userCount == 1) {
        $PBJQ("#presenceCount").html(userCount + '');
        $presenceCount.removeClass('present').addClass('empty');
      }

      else {
        $PBJQ("#presenceCount").html(' ');
        $presenceCount.removeClass('present').addClass('empty');
      }

      var chatUrl = $PBJQ('.nav-selected .icon-sakai-chat').attr('href');

      $PBJQ('#presenceIframe .presenceList div.inChat span').wrap('<a href="' + chatUrl + '"></a>')
      updatePresenceTimeout(30000, true); // 30 seconds
    },

    // If we get an error, wait 60 seconds before retry
    error: function(request, strError){
      updatePresenceTimeout(60000, true);
    }
  });
}

// Triggers when visibility changes
handleVisibilityChange = function(event) {
    if (document.hidden) {
        updatePresenceTimeout(window.sakaiPresenceTimeDelay, false); //stop
    } else  {
        updatePresenceTimeout(window.sakaiPresenceTimeDelay, false); 
        updatePresence(); //start immediately
    }
};
