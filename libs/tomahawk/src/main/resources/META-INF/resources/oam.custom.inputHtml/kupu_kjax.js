/*****************************************************************************
 *
 * Copyright (c) 2003-2005 Kupu Contributors. All rights reserved.
 *
 * This software is distributed under the terms of the Kupu
 * License. See LICENSE.txt for license text. For a list of Kupu
 * Contributors see CREDITS.txt.
 * 
 *****************************************************************************/
/* Javascript to aid migration page. */

function KJax() { this.request_queue = [];};
(function(p){
    p._loadXML = function(uri, callback, body, reload, extra) {
        function _sarissaCallback() {
            /* callback for Sarissa
            when the callback is called because the data's ready it
            will get the responseXML DOM and call user_callback
            with the DOM as the first argument and the uri loaded
            as the second

            note that this method should be called in the context of an 
            xmlhttp object
            */
            if (xmlhttp.readyState == 4) {
                self.xmlhttp = null;
                if (xmlhttp.status && xmlhttp.status != 200) {
                    var errmessage = 'Error '+xmlhttp.status+' loading '+(uri||'XML');
                    alert(errmessage);
                    throw "Error loading XML";
                };
                var dom = xmlhttp.responseXML;
                if (!dom || !dom.documentElement) { /* IE bug! */
                    dom = Sarissa.getDomDocument();
                    dom.loadXML(xmlhttp.responseText);
                }
                if (this.request_queue) {
                    /* Kick off the next chained request before trying
                     * to handle the result of the last one.
                     */
                    this._loadXML.apply(this, this.request_queue.splice(0,1));
                }
                callback.apply(self, [dom, uri, extra]);
            };
        };
        var self = this;
        /* Make sure our requests are single-threaded. */
        if (this.xmlhttp) {
            this.request_queue.push([uri, callback, body, reload, extra]);
        }
        /* load the XML from a uri
           calls callback with one arg (the XML DOM) when done
           the (optional) body arg should contain the body for the request
         */
        var xmlhttp = new XMLHttpRequest();
        this.xmlhttp = xmlhttp;
        var method = body?'POST':'GET';
        // be sure that body is null and not an empty string or
        // something
        body=body?body:null;

        try {
            xmlhttp.open(method, uri, true);
            xmlhttp.onreadystatechange = _sarissaCallback;
            if (method == "POST") {
                // by default, we would send a 'text/xml' request, which
                // is a dirty lie; explicitly set the content type to what
                // a web server expects from a POST.
                xmlhttp.setRequestHeader('content-type', 'application/x-www-form-urlencoded');
            };
            xmlhttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");
            xmlhttp.send(body);
        } catch(e) {
            this.xmlhttp = null;
            if (e && e.name && e.message) { // Microsoft
                e = e.name + ': ' + e.message;
            }
            alert(e);
        }
    };
    p._xmlcallback = function(dom) {
        this.xmldata = dom;
        Sarissa.setXpathNamespaces(this.xmldata, "xmlns:kj='http://kupu.oscom.org/namespaces/kjax'");
        var nodes = this.xmldata.selectNodes("//*[@kj:mode]");
        for (var i = 0; i < nodes.length; i++) {
            var n = nodes[i];
            var mode = n.getAttribute('kj:mode');
            n = document.importNode(n, true);
            var id = n.getAttribute('id');
            var target;
            if (id) {
                target = document.getElementById(id);
            } else {
                target = document.getElementById('kupu-default-target');
                mode = 'append';
            }
            if (mode=='append') {
                while(n.firstChild) {
                    target.appendChild(n.firstChild);
                };
            } else if (mode=='replace') {
                Sarissa.copyChildNodes(n, target);
            } else if (mode=='prepend') {
                var t = target.firstChild;
                while (n.firstChild) {
                    target.insertBefore(n.firstChild, t);
                };
            };
        };
        this.nextRequest();
    };
    p.nextRequest = function() {
        var onload = this.xmldata.selectSingleNode('//*[@kj:load]');
        if (onload) {
            var js = onload.getAttribute('kj:load');
            if (js) { eval(js); };
        };
        var next = this.xmldata.selectSingleNode('//*[@kj:next]');
        if (next) {
            var xmluri = next.getAttribute('kj:next');
            var delay = next.getAttribute('kj:delay');
            if (delay) {
                timer_instance.registerFunction(this, this._loadXML, delay*1000, xmluri, this._xmlcallback);
            } else {
                this._loadXML(xmluri, this._xmlcallback);
            };
        } else {
            this.trace("complete");
        };
    };
    p.newRequest = function(uri) {
        this._loadXML(uri, this._xmlcallback);
    };
    p.clearLog = function() {
        var el = document.getElementById("log");
        while (el.firstChild) el.removeChild(el.firstChild);
    };
    p.submitForm = function(form, uri, extra) {
        var fields = [];
        function push(el, v) {
            fields.push(el.name+"="+encodeURIComponent(v));
        }
        for(var i=0; i < form.elements.length; i++)
        {
            var el = form.elements[i];
            var name = /input/i.test(el.tagName)?el.type:el.tagName;
            if (/checkbox|radio/i.test(name) && !el.checked) continue;
            if (/select/i.test(name)) {
                push(el, el.options[el.selectedIndex].value);
                continue;
            }
            if (/text|hidden|checkbox|radio|textarea/i.test(name)) {
                push(el, el.value);
            };
        }
        if (!uri) { uri = form.getAttribute('action'); };
        if (extra) {
            for (var name in extra) {
                fields.push(name+"="+encodeURIComponent(extra[name]));
            }
        }
        this.trace("submit form: "+uri);
        this._loadXML(uri, this._xmlcallback, fields.join('&'));
        return false;
    };
    p.trace = function(s) {
        var el = document.getElementById("log");
        if (el) el.appendChild(newElement("div", [s]));
    };
})(KJax.prototype);

var kj = new KJax();
