/*****************************************************************************
 *
 * Copyright (c) 2003-2005 Kupu Contributors. All rights reserved.
 *
 * This software is distributed under the terms of the Kupu
 * License. See LICENSE.txt for license text. For a list of Kupu
 * Contributors see CREDITS.txt.
 *
 *****************************************************************************/
// $Id$

function startKupu() {
    // initialize the editor, this version groks an array of iframeids
    var iframeids = ['kupu_1', 'kupu_2', 'kupu_3'];
    var kupu = initKupu(iframeids); 

    // if there's no global 'i18n_message_catalog' variable available, don't
    // try to load any translations
    if (!window.i18n_message_catalog) {
        continueStartKupu(kupu);
        return kupu;
    };
    // loading will be done asynchronously (to keep Mozilla from freezing)
    // so we'll continue in a follow-up function (continueStartKupu() below)
    var handler = function(request) {
        if (this.readyState == 4) {
            var status = this.status;
            if (status != '200') {
                alert(_('Error loading translation (status ${status} ' +
                        '), falling back to english'), {'status': status});
                continueStartKupu(kupu);
                return;
            };
            var dom = this.responseXML;
            window.i18n_message_catalog.initialize(dom);
            continueStartKupu(kupu);
        };
    };
    var request = new XMLHttpRequest();
    request.onreadystatechange = (new ContextFixer(handler, request)).execute;
    request.open('GET', 'kupu.pox', true);
    request.send('');

    // we need to return a reference to the editor here for certain 'external'
    // stuff, developers should note that it's not yet initialized though, we
    // need to wait for i18n data before we can do that
    return kupu;
};

function continueStartKupu(kupu) {
    kupu.initialize();

    return kupu;
}
