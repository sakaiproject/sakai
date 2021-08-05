
var _Sakai_LTI_Iframes = [];
window.addEventListener('message', function (e) {
    var outer_href = window.location.href;
    var origin = e.origin;
    var same_origin = outer_href.startsWith(origin);
    var approved = false;
    var frame_id = false;

    console.log('Portal Listener', outer_href, origin, (same_origin ? 'origin match' : 'origin mismatch'));

    // https://stackoverflow.com/questions/15329710/postmessage-source-iframe
    Array.prototype.forEach.call(document.getElementsByTagName('iframe'), function (element) {
      if (element.contentWindow === e.source) {
        // console.log('source element', element);
        frame_id = element.getAttributeNode("id");
        if ( frame_id ) frame_id = frame_id.value;
      }
    });

    if ( ! frame_id ) {
        console.log('Message from frame without id', e.source, origin);
        console.log(e);
        return;
    }

    var message = e.data;
    if ( typeof message == 'string' ) message = JSON.parse(message)
    console.log(message);

    approved = _Sakai_LTI_Iframes.includes(frame_id);
    if ( ! same_origin ) {
        console.log('id', frame_id, (approved ? 'approved' : 'not approved'));
        if ( ! approved ) {
            console.log(_Sakai_LTI_Iframes);
            console.log(e);
        }
    }
 
    switch (message.subject) {
        case 'org.sakailms.lti.prelaunch':
            if ( same_origin ) {
                _Sakai_LTI_Iframes.push(frame_id);
                console.log('org.imsglobal.lti.prelaunch from same origin', origin, 'frame approved', frame_id);
            } else {
                console.log('org.imsglobal.lti.prelaunch must come from same origin, not', origin);
            }
            break;
        case 'org.sakailms.lti.postverify':
            if ( approved || same_origin ) {
                console.log('postverify from approved frame', frame_id);
                // TODO: Verify we like this URL - don't just AJAX anywhere
                var url = message.postverify;
                $PBJQ.ajax({
                    url: url,
                    cache: false,
                 })
                .done(function( data ) {
                    // https://stackoverflow.com/questions/61548354/how-to-postmessage-into-iframe
                    console.log('PostVerify complete - sending org.sakailms.lti.close');
                    document.getElementById(frame_id).contentWindow.postMessage({subject:'org.sakailms.lti.close'}, '*');
                });
            } else {
                console.log('org.sakailms.lti.prelaunch must come from approved frame, not', origin);
            }
            break;
        case 'org.imsglobal.lti.put_data':
            if ( approved || same_origin ) {
                var key = frame_id + message.key;
                window.sessionStorage.setItem(key, message.value);
                console.log('put_data ',key,' ', message.value);
                var response = new Object();
                response.key = message.key;
                response.message_id = message.message_id;
                response.subject = 'org.imsglobal.lti.put_data.response';
                response.value = message.value;
                document.getElementById(frame_id).contentWindow.postMessage(response, '*');
            } else {
                console.log('org.imsglobal.lti.put_data must come from approved frame, not', origin);
            }
            break;
        case 'org.imsglobal.lti.get_data':
            if ( approved || same_origin ) {
                console.log('get_data from approved frame', frame_id);
                var key = frame_id + message.key;
                var value = window.sessionStorage.getItem(key);
                var response = new Object();
                response.key = message.key;
                response.message_id = message.message_id;
                response.subject = 'org.imsglobal.lti.get_data.response';
                response.value = value;
                document.getElementById(frame_id).contentWindow.postMessage(response, '*');
            } else {
                console.log('org.imsglobal.lti.get_data must come from approved frame, not', origin);
            }
            break;
        case 'org.imsglobal.lti.remove_data':
            if ( approved || same_origin ) {
                console.log('remove_data from approved frame', frame_id);
                var key = frame_id + message.key;
                window.sessionStorage.removeItem(key, message.value);
                var response = new Object();
                response.key = message.key;
                response.message_id = message.message_id;
                response.subject = 'org.imsglobal.lti.remove_data.response';
                document.getElementById(frame_id).contentWindow.postMessage(response, '*');
            } else {
                console.log('org.imsglobal.lti.remove_data must come from approved frame, not', origin);
            }
            break;
        case 'org.imsglobal.lti.clear_data':
            if ( approved || same_origin ) {
                console.log('sessionStorage.clear from approved frame', frame_id);
                var key = frame_id + message.key;
                for (var i = 0; i < localStorage.length; i++) {
                    var key = localStorage.key(i);
                    console.log('key', key);
                    if ( ! key.startsWith(frame_id) ) continue;
                    console.log('removing', key);
                    window.sessionStorage.removeItem(key);
                }
                var response = new Object();
                response.message_id = message.message_id;
                response.subject = 'org.imsglobal.lti.clear_data.response';
                document.getElementById(frame_id).contentWindow.postMessage(response, '*');
            } else {
                console.log('org.imsglobal.lti.clear_data must come from approved frame, not', origin);
            }
            break;
        // This is here - but processed elsewhere
        case 'lti.frameResize':
            if ( approved || same_origin ) {
                console.log('frameResize from approved frame', frame_id);
            } else {
                console.log('frameResize must come from approved frame, not', origin);
            }
            break;
    }
});
