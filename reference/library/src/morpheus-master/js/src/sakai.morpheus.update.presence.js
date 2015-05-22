/**
 * For Footer toggles in Morpheus
 */

function updatePresence(){

  $PBJQ.ajax({
    url: sakaiPresenceFragment,
    cache: false,
    success: function(frag){

      var whereul = frag.indexOf('<ul');
      if (whereul < 1) {
        $PBJQ("#presenceCount").html(' ');
        $PBJQ('#presenceCount').removeClass('present').addClass('empty');
        location.reload();
        return;
      }

      frag = frag.substr(whereul);
      var _s = frag;
      var _m = '<li'; // needle 
      var _c = 0;

      for (var i = 0; i < _s.length; i++) {
        if (_m == _s.substr(i, _m.length)) 
          _c++;
      }
      // No need to attrct attention you are alone

      if (_c > 1) {
        $PBJQ("#presenceCount").html(_c + '');
        $PBJQ('#presenceCount').removeClass('empty').addClass('present');
      }

      else 

        if (_c == 1) {
          $PBJQ("#presenceCount").html(_c + '');
          $PBJQ('#presenceCount').removeClass('present').addClass('empty');
        }

        else {
          $PBJQ("#presenceCount").html(' ');
          $PBJQ('#presenceCount').removeClass('present').addClass('empty');
        }

      $PBJQ("#presenceIframe").html(frag);

      var chatUrl = $PBJQ('.nav-selected .icon-sakai-chat').attr('href');

      $PBJQ('#presenceIframe .presenceList li.inChat span').wrap('<a href="' + chatUrl + '"></a>')
      sakaiLastPresenceTimeOut = setTimeout('updatePresence()', 30000);
    },

    // If we get an error, wait 60 seconds before retry
    error: function(request, strError){
      sakaiLastPresenceTimeOut = setTimeout('updatePresence()', 60000);
    }
  });
}