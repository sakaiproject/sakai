/*****************************************************************************
 *
 * Copyright (c) 2003-2005 Kupu Contributors. All rights reserved.
 *
 * This software is distributed under the terms of the Kupu
 * License. See LICENSE.txt for license text. For a list of Kupu
 * Contributors see CREDITS.txt.
 *
 *****************************************************************************/
// $Id: kupustart.js 928511 2010-03-28 22:53:14Z lu4242 $

// myFaces : added parameter
function startKupu( iframeId ) {
    // first let's load the message catalog
    // if there's no global 'i18n_message_catalog' variable available, don't
    // try to load any translations
    if (window.i18n_message_catalog) {
        var request = new XMLHttpRequest();
        // sync request, scary...
        request.open('GET', 'kupu-pox.cgi', false);
        request.send('');
        if (request.status != '200') {
            alert('Error loading translation (status ' + status +
                    '), falling back to english');
        } else {
            // load successful, continue
            var dom = request.responseXML;
            window.i18n_message_catalog.initialize(dom);
        };
    };
    
    // initialize the editor, initKupu groks 1 arg, a reference to the iframe
    // myFaces : added iframeId
    var frame = getFromSelector( iframeId ); 
    var kupu = initKupu(frame);
    
    // this makes the editor's content_changed attribute set according to changes
    // in a textarea or input (registering onchange, see saveOnPart() for more
    // details)
    kupu.registerContentChanger(getFromSelector('kupu-editor-textarea'));

	/* myFaces : disable this
    // let's register saveOnPart(), to ask the user if he wants to save when 
    // leaving after editing
    if (kupu.getBrowserName() == 'IE') {
        // IE supports onbeforeunload, so let's use that
        addEventHandler(window, 'beforeunload', saveOnPart);
    } else {
        // some versions of Mozilla support onbeforeunload (starting with 1.7)
        // so let's try to register and if it fails fall back on onunload
        var re = /rv:([0-9\.]+)/;
        var match = re.exec(navigator.userAgent);
        if (match && match[1] && parseFloat(match[1]) > 1.6) {
            addEventHandler(window, 'beforeunload', saveOnPart);
        } else {
            addEventHandler(window, 'unload', saveOnPart);
        };
    };
    */

    // and now we can initialize...
    kupu.initialize();

    return kupu;
}
