var _Sakai_LTI_Iframes = [];
window.addEventListener('message', function (e) {
    var outer_href = window.location.href;
    var origin = e.origin;
    var same_origin = outer_href.startsWith(origin);
    var approved = false;
    var element_id = false;

    console.log('Portal Listener', outer_href, origin, (same_origin ? 'origin match' : 'origin mismatch'));

    // https://stackoverflow.com/questions/15329710/postmessage-source-iframe
    Array.prototype.forEach.call(document.getElementsByTagName('iframe'), function (element) {
      if (element.contentWindow === e.source) {
        // console.log('source element', element);
        event_id = element.getAttributeNode("id");
        if ( event_id ) event_id = event_id.value;
      }
    });

    if ( ! event_id ) {
        console.log('Message from frame without id', e.source, origin);
        console.log(e);
        return;
    }

    approved = _Sakai_LTI_Iframes.includes(event_id);
    console.log('id', event_id, (approved ? 'approved' : 'not approved'));
    // console.log(_Sakai_LTI_Iframes);
    // console.log(e);

    var message = e.data;
    if ( typeof message == 'string' ) message = JSON.parse(message)
    console.log(message);

    switch (message.subject) {
        case 'org.imsglobal.lti.prelaunch':
            if ( same_origin ) {
                _Sakai_LTI_Iframes.push(event_id);
                console.log('org.imsglobal.lti.prelaunch from same origin', origin, 'frame approved', event_id);
            } else {
                console.log('org.imsglobal.lti.prelaunch must come from same origin, not', origin);
            }
            break;
        case 'org.imsglobal.lti.postverify':
            if ( approved ) {
                console.log('postverify from approved frame', event_id);
                // TODO: Verify we like this URL - don't just AJAX anywhere
                var url = message.postverify;
                ${d}PBJQ.ajax({
                    url: url,
                    cache: false,
                 })
                .done(function( data ) {
                    // https://stackoverflow.com/questions/61548354/how-to-postmessage-into-iframe
                    console.log('PostVerify complete - sending org.imsglobal.lti.close');
                    document.getElementById(event_id).contentWindow.postMessage(JSON.stringify({subject:'org.imsglobal.lti.close'}), '*');
                });
            } else {
                console.log('org.imsglobal.lti.prelaunch must come from approved frame, not', origin);
            }
            break;
        case 'org.imsglobal.lti.put_data':
            if ( approved ) {
                console.log('put_data from approved frame', event_id);
                var key = event_id + message.key;
                window.sessionStorage.setItem(key, message.value);
                var response = new Object();
                response.key = message.key;
                response.message_id = message.message_id;
                response.subject = 'org.imsglobal.lti.put_data.response';
                response.value = value;
                document.getElementById(event_id).contentWindow.postMessage(JSON.stringify(response), '*');
            }
            break;
        case 'org.imsglobal.lti.get_data':
            if ( approved ) {
                console.log('get_data from approved frame', event_id);
                var key = event_id + message.key;
                var value = window.sessionStorage.getItem(key);
                var response = new Object();
                response.key = message.key;
                response.message_id = message.message_id;
                response.subject = 'org.imsglobal.lti.get_data.response';
                response.value = value;
                document.getElementById(event_id).contentWindow.postMessage(JSON.stringify(response), '*');
            }
            break;
        case 'org.imsglobal.lti.remove_data':
            if ( approved ) {
                console.log('remove_data from approved frame', event_id);
                var key = event_id + message.key;
                window.sessionStorage.removeItem(key, message.value);
                var response = new Object();
                response.key = message.key;
                response.message_id = message.message_id;
                response.subject = 'org.imsglobal.lti.remove_data.response';
                document.getElementById(event_id).contentWindow.postMessage(JSON.stringify(response), '*');
            }
            break;
        case 'org.imsglobal.lti.clear_data':
            if ( approved ) {
                console.log('sessionStorage.clear from approved frame', event_id);
                var key = event_id + message.key;
                for (var i = 0; i < localStorage.length; i++) {
                    var key = localStorage.key(i);
                    console.log('key', key);
                    if ( ! key.startsWith(event_id) ) continue;
                    console.log('removing', key);
                    window.sessionStorage.removeItem(key);
                }
                var response = new Object();
                response.message_id = message.message_id;
                response.subject = 'org.imsglobal.lti.clear_data.response';
                document.getElementById(event_id).contentWindow.postMessage(JSON.stringify(response), '*');
            }
            break;
        case 'lti.frameResize':
            if ( approved ) {
                console.log('frameResize from approved frame', event_id);
            } else {
                console.log('frameResize must come from approved frame, not', origin);
            }
            break;
    }
});
