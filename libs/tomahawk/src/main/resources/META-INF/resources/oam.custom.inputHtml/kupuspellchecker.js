function KupuSpellChecker(buttonid, scripturl, spanstyle, 
                            winwidth, winheight, skip_tags) {
    this.button = document.getElementById(buttonid);
    this.scripturl = scripturl;
    this.spanstyle = spanstyle || 'color: red; ' +
                                    'text-decoration: underline;';
    this.winwidth = winwidth || '600';
    this.winheight = winheight || '400';
    this.skip_tags = skip_tags || ['head', 'script'];
};

KupuSpellChecker.prototype = new KupuTool;

KupuSpellChecker.prototype.initialize = function(editor) {
    this.editor = editor;
    if (this.button) {
        addEventHandler(this.button, 'click', this.check, this);
    }
};

KupuSpellChecker.prototype.check = function() {
    var request = new XMLHttpRequest();
    request.open('POST', this.scripturl, true);
    request.setRequestHeader('Content-Type', 
                                'application/x-www-form-urlencoded');
    request.onreadystatechange = new ContextFixer(
                                    this.stateChangeHandler,
                                    this,
                                    request).execute;
    var result = this.getCurrentContents();
    result = encodeURIComponent(result.reduceWhitespace().strip());
    request.send('text=' + result);
};

KupuSpellChecker.prototype.stateChangeHandler = function(request) {
    if (request.readyState == 4) {
        if (request.status == '200') {
            var result = request.responseXML;
            result = this.xmlToMapping(result);
            if (!result) {
                alert(_('There were no errors.'));
            } else {
                this.displayUnrecognized(result);
            };
        } else {
            alert(_('Error loading data, status ${status}',
                    {'status': request.status}));
        };
    };
};

KupuSpellChecker.prototype.getCurrentContents = function() {
    var doc = this.editor.getInnerDocument().documentElement;
    var iterator = new NodeIterator(doc);
    var bits = [];
    while (true) {
        var node = iterator.next();
        if (!node || node.nodeName.toLowerCase() == 'body') {
            break;
        };
        while (this.skip_tags.contains(node.nodeName.toLowerCase())) {
            node = node.nextSibling;
            iterator.setCurrent(node);
        };
        if (node.nodeType == 3) {
            bits.push(node.nodeValue);
        };
    };
    return bits.join(' ');
};

KupuSpellChecker.prototype.displayUnrecognized = function(mapping) {
    // copy the current editable document into a new window
    var doc = this.editor.getInnerDocument();
    var docel = doc.documentElement;
    var win = window.open('kupublank.html', 'spellchecker', 
                            'width=' + this.winwidth + ',' +
                            'height=' + this.winheight + ',toolbar=no,' +
                            'menubar=no,scrollbars=yes,status=yes');
    if (!win) {
        alert(
            _('This feature requires pop-ups to be enabled on your browser!'));
        return;
    };
    var html = docel.innerHTML;
    // when Moz tries to set the content-type, for some reason leaving this
    // in breaks the feature(?!?)
    html = html.replace(/<meta[^>]*http-equiv="[Cc]ontent-[Tt]ype"[^>]*>/gm, 
                        '');
    win.document.write('<html>' + html + '</html>');
    win.deentitize = function(str) {return str.deentitize();};
    win.document.close();
    if (!win.document.getElementsByTagName('body').length) {
        addEventHandler(win, 'load', this.continueDisplay, this, win, mapping);
    } else {
        this.continueDisplay(win, mapping);
    };
};

KupuSpellChecker.prototype.continueDisplay = function(win, mapping) {
    /* walk through all elements of the body, colouring the text nodes */
    // start it all with a timeout to make Mozilla render the content first
    timer_instance.registerFunction(this, this.continueDisplayHelper,
                                    1000, win, mapping);
};

KupuSpellChecker.prototype.continueDisplayHelper = function(win, mapping) {
    var body = win.document.getElementsByTagName('body')[0];
    body.setAttribute('contentEditable', 'false');
    var iterator = new NodeIterator(body);
    var node = iterator.next();
    timer_instance.registerFunction(this, this.displayHelperNodeLoop,
                                    10, iterator, node, win, mapping);
};

KupuSpellChecker.prototype.displayHelperNodeLoop = function(iterator, node, 
                                                                win, mapping) {
    if (!node || node.nodeName.toLowerCase() == 'body') {
        return;
    };
    var next = iterator.next();
    if (node.nodeType == 3) {
        if (win.closed) {
            return;
        };
        var span = win.document.createElement('span');
        var before = node.nodeValue;
        var after = this.colourText(before, mapping);
        if (before != after) {
            span.innerHTML = after;
            var last = span.lastChild;
            var parent = node.parentNode;
            parent.replaceChild(last, node);
            while (span.hasChildNodes()) {
                parent.insertBefore(span.firstChild, last);
            };
        };
    } else if (node.nodeType == 1 && node.nodeName.toLowerCase() == 'a') {
        var cancelEvent = function(e) {
            if (e.preventDefault) {
                e.preventDefault();
            } else {
                e.returnValue = false;
            };
            return false;
        };
        addEventHandler(node, 'click', cancelEvent);
        addEventHandler(node, 'mousedown', cancelEvent);
        addEventHandler(node, 'mouseup', cancelEvent);
    };
    // using a timeout here makes Moz render the coloring while it's busy, and
    // will make it stop popping up 'do you want to continue' prompts...
    timer_instance.registerFunction(this, this.displayHelperNodeLoop,
                                    10, iterator, next, win, mapping);
};

KupuSpellChecker.prototype.colourText = function(text, mapping) {
    var currtext = text;
    var newtext = '';
    for (var word in mapping) {
        var replacements = mapping[word];
        replacements = replacements.entitize();
        replacements = replacements.replace(/\'/g, "&apos;");
        var reg = new RegExp('^(.*\\\W)?(' + word + ')(\\\W.*)?$', 'mg');
        while (true) {
            var match = reg.exec(currtext);
            if (!match) {
                newtext += currtext;
                currtext = newtext;
                newtext = '';
                break;
            };
            var m = (match[1] || '') + match[2];
            newtext += currtext.substr(0, currtext.indexOf(m));
            newtext += (match[1] || '') +
                        '<span style="' + this.spanstyle + '" ' +
                        'onclick="alert(deentitize(\'' + 
                        replacements + '\'));" ' +
                        'title="' + replacements + '">' +
                        match[2] +
                        '</span>';
            currtext = currtext.substr(currtext.indexOf(m) + m.length);
        };
    };
    return currtext;
};

KupuSpellChecker.prototype.xmlToMapping = function(docnode) {
    var docel = docnode.documentElement;
    var result = {};
    var incorrect = docel.getElementsByTagName('incorrect');
    for (var i=0; i < incorrect.length; i++) {
        var word = incorrect[i].firstChild.firstChild.nodeValue;
        var replacements = '';
        if (incorrect[i].lastChild.hasChildNodes()) {
            replacements = incorrect[i].lastChild.firstChild.nodeValue;
        };
        result[word] = replacements;
    };
    return result;
};
