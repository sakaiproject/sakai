// turn this into a nice module-like namespace to avoid messing up the global
// (window) namespace
this.kuputoolcollapser = new function() {
    var ToolCollapser = function(toolboxesparentid) {
        this.parent = document.getElementById(toolboxesparentid);
    };

    // make the collapser available in the namespace
    this.Collapser = ToolCollapser;

    ToolCollapser.prototype.initialize = function() {
        var initial_state = {};
        if (navigator.cookieEnabled) {
            var cookie = document.cookie;
            var reg = /initial_state=([^;]+);?/;
            var match = cookie.match(reg);
            if (match) { /*ignore 558 */
                eval(unescape(match[0]));
            };
        };
        for (var i=0; i < this.parent.childNodes.length; i++) {
            var child = this.parent.childNodes[i];
            if (child.className && child.className.match(/\bkupu-toolbox\b/)) {
                var heading = child.getElementsByTagName('h1')[0];
                if (!heading) {
                    throw('heading not found by collapser for toolbox ' +
                            child.id);
                };
                heading.setAttribute('title', _('click to unfold'));
                // find the toolbox's body
                var body = this.getToolBody(child);
                // now set a handler that makes the body display and hide
                // on click, and register it to the heading
                // WAAAAAHHHH!!! since there's some weird shit happening when
                // I just use closures to refer to the body (somehow only the
                // *last* value body is set to in this loop is used?!?) I
                // used a reference to the body as 'this' in the handler
                var handler = function(heading) {
                    if (this.style.display == 'none') {
                        // assume we have a block-level element here...
                        this.style.display = 'block';
                        heading.className = 'kupu-toolbox-heading-opened';
                        heading.setAttribute('title', _('click to fold'));
                    } else {
                        this.style.display = 'none';
                        heading.className = 'kupu-toolbox-heading-closed';
                        heading.setAttribute('title', _('click to unfold'));
                    };
                };
                var wrap_openhandler = function(body, heading) {
                    return function() {
                        body.style.display = 'block';
                        heading.className = 'kupu-toolbox-heading-opened';
                    };
                };
                addEventHandler(heading, 'click', handler, body, heading);
                if (initial_state[child.id] === undefined ||
                        initial_state[child.id] == '0') {
                    body.style.display = 'none';
                } else {
                    heading.className = 'kupu-toolbox-heading-opened';
                    heading.setAttribute('title', _('click to fold'));
                };
                // add a reference to the openhandler on the toolbox div
                // so any toolbox code can use that to open the toolbox if
                // it so desires
                child.open_handler = wrap_openhandler(body, heading);
            };
        };

        addEventHandler(window, 'beforeunload', this.saveState, this);
    };

    ToolCollapser.prototype.getToolBody = function(tool) {
        var heading = tool.getElementsByTagName('h1')[0];
        var currchild = heading.nextSibling;
        while (currchild.nodeType != 1) {
            currchild = currchild.nextSibling;
            if (!currchild) {
                throw('body not found by collapser for toolbox ' +
                        tool.id);
            };
        };
        return currchild;
    };

    ToolCollapser.prototype.saveState = function() {
        /* save collapse state of the toolboxes in a cookie */
        if (!navigator.cookieEnabled) {
            return;
        };
        var current_state = {};
        for (var i=0; i < this.parent.childNodes.length; i++) {
            var child = this.parent.childNodes[i];
            if (child.nodeType != 1) {
                continue;
            };
            var body = this.getToolBody(child);
            current_state[child.id] = body.style.display == 'none' ? '0' : '1';
        };

        var exp = new Date();
        // 100 years before state is lost... should be enough ;)
        exp.setTime(exp.getTime() + (100 * 365 * 24 * 60 * 60 * 1000));
        var cookie = 'initial_state=' +
                            escape(this.serializeMapping(current_state)) +
                            ';' +
                            'expires=' + exp.toGMTString() + ';' +
                            'path=/';
        document.cookie = cookie;
    };

    ToolCollapser.prototype.serializeMapping = function(mapping) {
        /* serializes the config dict into a string that can be evalled

            works only for dicts with string values
        */
        if (typeof(mapping) == 'string') {
            return "'" + mapping + "'";
        };
        var ret = '{';
        var first = true;
        for (var key in mapping) {
            if (!first) {
                ret += ', ';
            };
            ret += "'" + key + "': " +
                this.serializeMapping(mapping[key]);
            first = false;
        };
        return ret + '}';
    };
}();
