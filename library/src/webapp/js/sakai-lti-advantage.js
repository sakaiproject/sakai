
!function(w) {

let _Sakai_LTI_Iframes = [];
let stored_data = {}
let quota = 300000; // 300K quota

// Future feature: Allow additions / deletions to this from same origin
let supported_messages = [
        { subject: "lti.capabilities" },
        { subject: "lti.put_data", frame: "_parent" },
        { subject: "lti.get_data", frame: "_parent" },
        // Some general things we may or may not support depending on which page
        { subject: "lti.close"},
        { subject: "lti.frameResize" },
        { subject: "lti.pageRefresh" },
        { subject: "org.imsglobal.lti.capabilities" }, // Legacy
        { subject: "org.imsglobal.lti.put_data", frame: "_parent" },     // Legacy
        { subject: "org.imsglobal.lti.get_data", frame: "_parent" },     // Legacy
        { subject: "org.imsglobal.lti.close"},         // Legacy
];

//  https://github.com/MartinLenord/simple-lti-1p3/blob/cookie-shim/src/web/platform/csstorage.php

w.addEventListener('message', function (event) {
    var outer_href = window.location.href;
    var origin = event.origin;
    var same_origin = outer_href.startsWith(origin);
    var approved = false;
    var frame_id = false;

    console.debug('Portal Listener', outer_href, origin, (same_origin ? 'origin match' : 'origin mismatch'));

    // https://stackoverflow.com/questions/15329710/postmessage-source-iframe
    Array.prototype.forEach.call(document.getElementsByTagName('iframe'), function (element) {
      if (element.contentWindow === event.source) {
        frame_id = element.getAttributeNode("id");
        if ( frame_id ) frame_id = frame_id.value;
      }
    });

    if ( ! frame_id ) {
        console.debug('Message from frame without id', event.source, origin);
        console.debug(event);
        return;
    }

    var message = event.data;
    if ( typeof message == 'string' ) message = JSON.parse(message)
    console.debug(message);

    // Check if a frame is approved
    approved = _Sakai_LTI_Iframes.includes(frame_id);
    if ( ! same_origin ) {
        console.debug('id', frame_id, (approved ? 'approved' : 'not approved'));
        if ( ! approved ) {
            console.debug(_Sakai_LTI_Iframes);
            console.debug(event);
        }
    }

    switch (message.subject) {
        case 'org.sakailms.lti.prelaunch': {
            if ( same_origin ) {
                _Sakai_LTI_Iframes.push(frame_id);
                let send_data = {
                    subject: 'org.sakailms.lti.prelaunch.response',
                };
                console.debug('org.sakailms.lti.prelaunch from same origin', origin, 'frame approved', frame_id, 'sending prelaunch.response');
                event.source.postMessage(send_data, event.origin);
            } else {
                console.debug('org.sakailms.lti.prelaunch must come from same origin, not', origin);
            }
            break;
        }
        case 'lti.capabilities': {
            let send_data = {
                subject: 'lti.capabilities.response',
                message_id: message.message_id,
                supported_messages: supported_messages,
            };
            console.debug(w.location.origin + " Replying post message to " + event.origin);
            console.debug(JSON.stringify(send_data, null, '    '));
            event.source.postMessage(send_data, event.origin);
            break;
        }
        case 'lti.put_data': {
            if (!stored_data[origin]) {
                stored_data[origin] = {};
            }

            if ( typeof message.key == 'undefined' || typeof message.value == 'undefined' ) {
                let send_data = {
                    subject: 'lti.put_data.response',
                    message_id: message.message_id,
                    error: {
                        code: "key and value are required",
                    }
                };
                console.debug(JSON.stringify(send_data, null, '    '));
                event.source.postMessage(send_data, event.origin);
                return;
            }

            let storage = JSON.stringify(stored_data).length +
                JSON.stringify(message.key).length + JSON.stringify(message.value).length;
            /// console.debug("new size="+storage);
            if ( storage > quota ) {
                let send_data = {
                    subject: 'lti.put_data.response',
                    message_id: message.message_id,
                    key: message.key,
                    error: {
                        code: "storage_exhaustion",
                        message: storage+" bytes"
                    }
                };
                console.debug(JSON.stringify(send_data, null, '    '));
                event.source.postMessage(send_data, event.origin);
            } else {
                stored_data[origin][message.key] = message.value;
                let send_data = {
                    subject: 'lti.put_data.response',
                    message_id: message.message_id,
                    key: message.key,
                    value: message.value,
                };
                console.debug(w.location.origin + " Replying post message to " + event.origin);
                console.debug(JSON.stringify(send_data, null, '    '));
                event.source.postMessage(send_data, event.origin);
            }
            break;
        }
        case 'lti.get_data':
            console.debug('get_data ',origin, ' ', message.key,' ', message.value);
            if ( typeof message.key == 'undefined' ) {
                let send_data = {
                    subject: 'lti.get_data.response',
                    message_id: message.message_id,
                    error: {
                        code: "key is required",
                    }
                };
                console.debug(JSON.stringify(send_data, null, '    '));
                event.source.postMessage(send_data, event.origin);
                return;
            }
            let retval = false;
            if (stored_data[origin] && stored_data[origin][message.key]) {
                let retval = stored_data[origin][message.key];
                let send_data = {
                    subject: 'lti.get_data.response',
                    message_id: message.message_id,
                    key: message.key,
                    value: stored_data[origin][message.key]
                };
                console.debug(w.location.origin + " Replying post message to " + event.origin);
                console.debug(JSON.stringify(send_data, null, '    '));
                event.source.postMessage(send_data, event.origin);
            } else {
                let send_data = {
                    subject: 'lti.get_data.response',
                    message_id: message.message_id,
                    key: message.key,
                    error: 'Could not find key',
                };
                console.debug(w.location.origin + " Replying post message to " + event.origin);
                console.debug(JSON.stringify(send_data, null, '    '));
                event.source.postMessage(send_data, event.origin);
            }
        break;
        case 'org.sakailms.lti.postverify': {
            if ( approved || same_origin ) {
                console.debug('postverify from approved frame', frame_id);
                // TODO: Verify we like this URL - don't just AJAX anywhere
                var url = message.postverify;
                $PBJQ.ajax({
                    url: url,
                    cache: false,
                 })
                .done(function( data ) {
                    // https://stackoverflow.com/questions/61548354/how-to-postmessage-into-iframe
                    console.debug('PostVerify complete - replying org.sakailms.lti.close');
                    document.getElementById(frame_id).contentWindow.postMessage({subject:'org.sakailms.lti.close'}, '*');
                });
            } else {
                console.debug('org.sakailms.lti.postverify must come from approved frame, not', origin);
            }
            break;
        }

        // Legacy messages since IMS changed its name to 1EdTech
        case 'org.imsglobal.lti.capabilities': {
            let send_data = {
                subject: 'org.imsglobal.lti.capabilities.response',
                message_id: message.message_id,
                supported_messages: supported_messages,
            };
            console.debug(w.location.origin + " Replying post message to " + event.origin);
            console.debug(JSON.stringify(send_data, null, '    '));
            event.source.postMessage(send_data, event.origin);
            break;
        }
        case 'org.imsglobal.lti.put_data': {
            if (!stored_data[origin]) {
                stored_data[origin] = {};
            }

            if ( typeof message.key == 'undefined' || typeof message.value == 'undefined' ) {
                let send_data = {
                    subject: 'org.imsglobal.lti.put_data.response',
                    message_id: message.message_id,
                    error: {
                        code: "key and value are required",
                    }
                };
                console.debug(JSON.stringify(send_data, null, '    '));
                event.source.postMessage(send_data, event.origin);
                return;
            }

            let storage = JSON.stringify(stored_data).length +
                JSON.stringify(message.key).length + JSON.stringify(message.value).length;
            /// console.debug("new size="+storage);
            if ( storage > quota ) {
                let send_data = {
                    subject: 'org.imsglobal.lti.put_data.response',
                    message_id: message.message_id,
                    key: message.key,
                    error: {
                        code: "storage_exhaustion",
                        message: storage+" bytes"
                    }
                };
                console.debug(JSON.stringify(send_data, null, '    '));
                event.source.postMessage(send_data, event.origin);
            } else {
                stored_data[origin][message.key] = message.value;
                let send_data = {
                    subject: 'org.imsglobal.lti.put_data.response',
                    message_id: message.message_id,
                    key: message.key,
                    value: message.value,
                };
                console.debug(w.location.origin + " Replying post message to " + event.origin);
                console.debug(JSON.stringify(send_data, null, '    '));
                event.source.postMessage(send_data, event.origin);
            }
            break;
        }
        case 'org.imsglobal.lti.get_data': {
            console.debug('get_data ',origin, ' ', message.key,' ', message.value);
            if ( typeof message.key == 'undefined' ) {
                let send_data = {
                    subject: 'org.imsglobal.lti.get_data.response',
                    message_id: message.message_id,
                    error: {
                        code: "key is required",
                    }
                };
                console.debug(JSON.stringify(send_data, null, '    '));
                event.source.postMessage(send_data, event.origin);
                return;
            }
            let retval = false;
            if (stored_data[origin] && stored_data[origin][message.key]) {
                let retval = stored_data[origin][message.key];
                 let send_data = {
                    subject: 'org.imsglobal.lti.get_data.response',
                    message_id: message.message_id,
                    key: message.key,
                    value: stored_data[origin][message.key]
                };
                console.debug(w.location.origin + " Replying post message to " + event.origin);
                console.debug(JSON.stringify(send_data, null, '    '));
                event.source.postMessage(send_data, event.origin);
            } else {
                let send_data = {
                    subject: 'org.imsglobal.lti.get_data.response',
                    message_id: message.message_id,
                    key: message.key,
                    error: 'Could not find key',
                };
                console.debug(w.location.origin + " Replying post message to " + event.origin);
                console.debug(JSON.stringify(send_data, null, '    '));
                event.source.postMessage(send_data, event.origin);
            }
            break;
        }
    }
}, false);
}(window);

