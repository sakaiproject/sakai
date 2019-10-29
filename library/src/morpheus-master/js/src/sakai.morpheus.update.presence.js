function updatePresence(){

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
      sakaiLastPresenceTimeOut = setTimeout('updatePresence()', 30000);
    },

    // If we get an error, wait 60 seconds before retry
    error: function(request, strError){
      sakaiLastPresenceTimeOut = setTimeout('updatePresence()', 60000);
    }
  });
}
