// page is /portal/site/nnn/tool/xxx
// however if you use that URL for ajax, you'll get a whole page
// you want /portal/tool/xxx

// RSF.js - primitive definitions for parsing RSF-rendered forms and bindings
// definitions placed in RSF namespace, following approach recommended in http://www.dustindiaz.com/namespace-your-javascript/



var RSF = RSF || {};

(function () {

    /** control logging, set this to true to turn on logging or use the setLogging method */
    var logging = false;

    function $it(elementID) {
        return document.getElementById(elementID);
    }

    function invalidate(invalidated, EL, entry) {
        if (!EL) {
            RSF.log("invalidate null EL: " + invalidated + " " + entry);
            return;
        }
        var stack = RSF.parseEL(EL);
        invalidated[stack[0]] = entry;
        invalidated[stack[1]] = entry;
        invalidated.list.push(entry);
        RSF.log("invalidate " + EL);
    }
    ;

    function isInvalidated(invalidated, EL) {
        if (!EL) {
            RSF.log("isInvalidated null EL: " + invalidated);
            return false;
        }
        var stack = RSF.parseEL(EL);
        var togo = invalidated[stack[0]] || invalidated[stack[1]];
        RSF.log("isInvalidated " + EL + " " + togo);
        return togo;
    }

    function isFossil(element, input) {
        if (element.id && input.id == element.id + "-fossil")
            return true;
        return (input.name == element.name + "-fossil");
    }

    function normaliseBinding(element) {
        RSF.log("normaliseBinding name " + element.name + " id " + element.id);
        if (!element.name)
            return element.id;
        else
            return element.name == "virtual-el-binding" ? "el-binding" : element.name;
    }

    var requestactive = false;
    var queuemap = new Object();

    function packAJAXRequest(method, url, parameters, callback, options) {
        return {method: method, url: url, parameters: parameters, callback: callback, options: options};
    }

    function wrapCallbacks(callbacks, wrapper) {
        var togo = new Object();
        for (var i in callbacks) {
            togo[i] = wrapper(callbacks[i]);
        }
        return togo;
    }

    // private defs for addEvent - see attribution comments below
    var addEvent_guid = 1;
    var addEvent_handlers = {};

    function handleEvent(event) {
        event = event || fixEvent(window.event);
        var returnValue = true;
        var handlers = addEvent_handlers[this.$$guid][event.type];

        for (var i in handlers) {
            if (!Object.prototype[i]) {
                this.$$handler = handlers[i];
                if (this.$$handler(event) === false)
                    returnValue = false;
            }
        }

        if (this.$$handler)
            this.$$handler = null;
        return returnValue;
    }

    function fixEvent(event) {
        event.preventDefault = fixEvent.preventDefault;
        event.stopPropagation = fixEvent.stopPropagation;
        return event;
    }

    fixEvent.preventDefault = function () {
        this.returnValue = false;
    }

    fixEvent.stopPropagation = function () {
        this.cancelBubble = true;
    }

    function getEventFirer() {
        var listeners = {};
        return {
            addListener: function (listener, exclusions) {
                if (!listener.$$guid)
                    listener.$$guid = addEvent_guid++;
                excludeids = [];
                if (exclusions) {
                    for (var i in exclusions) {
                        excludeids.push(exclusions[i].id);
                    }
                }
                listeners[listener.$$guid] = {listener: listener, exclusions: excludeids};
            },
            fireEvent: function () {
                for (var i in listeners) {
                    var lisrec = listeners[i];
                    var excluded = false;
                    for (var j in lisrec.exclusions) {
                        var exclusion = lisrec.exclusions[j];
                        RSF.log("Checking exclusion for " + exclusion);
                        if (primaryElements[exclusion]) {
                            RSF.log("Excluded");
                            excluded = true;
                            break;
                        }
                    }
                    if (!excluded) {
                        try {
                            RSF.log("Firing to listener " + i + " with arguments " + arguments);
                            lisrec.listener.apply(null, arguments);
                        } catch (e) {
                            RSF.log("Received exception " + e.message + " e " + e);
                            throw (e);
                        }
                    }
                }
            }
        };
    }

    /** Returns the standard registered firer for this field, creating if
     necessary. This will have method "addListener" and "fireEvent" **/
    function getElementFirer(element) {
        if (!element.$$RSF_firer) {
            element.$$RSF_firer = getEventFirer();
        }
        return element.$$RSF_firer;
    }
    // This is set in getModelFirer, and checked in fireEvent
    var primaryElements = {};

    function clearObject(target, newel) {
        for (var i in newel) {
            delete target[i];
        }
    }
    // a THING, that when given "elements", returns a THING, that when it is
    // given a CALLBACK, returns a THING, that does the SAME as the CALLBACK,
    // only with wrappers which are bound to the value that ELEMENTS had at
    // the function start
    function primaryRestorationWrapper() {
        var elementscopy = {};
        RSF.assign(elementscopy, primaryElements);
        RSF.log("Primary elements storing in wrapper");

        return function (callback) {
            return function () {
                RSF.assign(primaryElements, elementscopy);
                try {
                    callback.apply(null, arguments);
                } catch (e) {
                    RSF.log("Error occurred during event callback: " + e);
                    throw (e);
                } finally {
                    RSF.log("Restoration clearing");
                    clearObject(primaryElements, elementscopy);
                    RSF.log("Restoration cleared");
                }
            }
        }
    }
    // A "ClassLoader"-wide cache of scripts which have been loaded, to avoid
    // duplicate fetches
    var loadedScripts = {};

    var outstandingScripts = 0;

    var queuedScripts = [];

    /* parseUri 1.2; MIT License
     By Steven Levithan <http://stevenlevithan.com> 
     http://blog.stevenlevithan.com/archives/parseuri
     */
    var parseUri = function (source) {
        var o = parseUri.options,
                value = o.parser[o.strictMode ? "strict" : "loose"].exec(source);

        for (var i = 0, uri = {}; i < 14; i++) {
            uri[o.key[i]] = value[i] || "";
        }

        uri[o.q.name] = {};
        uri[o.key[12]].replace(o.q.parser, function ($0, $1, $2) {
            if ($1)
                uri[o.q.name][$1] = $2;
        });

        return uri;
    }
    parseUri.options = {
        strictMode: false,
        key: ["source", "protocol", "authority", "userInfo", "user", "password", "host", "port", "relative", "path", "directory", "file", "query", "anchor"],
        q: {
            name: "queryKey",
            parser: /(?:^|&)([^&=]*)=?([^&]*)/g
        },
        parser: {
            strict: /^(?:([^:\/?#]+):)?(?:\/\/((?:(([^:@]*):?([^:@]*))?@)?([^:\/?#]*)(?::(\d*))?))?((((?:[^?#\/]*\/)*)([^?#]*))(?:\?([^#]*))?(?:#(.*))?)/,
            loose: /^(?:(?![^:@]+:[^:@\/]*@)([^:\/?#.]+):)?(?:\/\/)?((?:(([^:@]*):?([^:@]*))?@)?([^:\/?#]*)(?::(\d*))?)(((\/(?:[^?#](?![^?#\/]*\.[^?#\/.]+(?:[?#]|$)))*\/?)?([^?#\/]*))(?:\?([^#]*))?(?:#(.*))?)/
        }
    }

    function ignorableNode(node) {
        return node.tagName.toLowerCase() == 'select';
    }

    function getNextNode(iterator) {
        if (iterator.node.firstChild && !ignorableNode(iterator.node)) {
            iterator.node = iterator.node.firstChild;
            iterator.depth++;
            return iterator;
        }
        while (iterator.node) {
            if (iterator.node.nextSibling) {
                iterator.node = iterator.node.nextSibling;
                return iterator;
            }
            iterator.node = iterator.node.parentNode;
            iterator.depth--;
        }
        return iterator;
    }


    /** All public functions **/


    // Work around IE circular DOM issue. This is the default max DOM depth on IE.
    // http://msdn2.microsoft.com/en-us/library/ms761392(VS.85).aspx
    RSF.DOM_BAIL_DEPTH = 256;

    // Compute the corrected height of an element, taking into account 
    // any floating elements
    // impl from http://www.three-tuns.net/~andrew/page-height-test2.html#
    RSF.computeCorrectedHeight = function (node) {
        var totalHeight = node.offsetHeight;
        if (node.offsetParent) {
            while (node.offsetParent) {
                totalHeight += node.offsetTop;
                node = node.offsetParent;
            }
        } else if (node.y) {
            totalHeight += node.y;
        }
        return totalHeight;
    };

    RSF.computeDocumentHeight = function (dokkument) {
        var currentNode = {node: dokkument.body, depth: 0};
        var biggestOffset = 0;
        while (currentNode.node != null && currentNode.depth >= 0 && currentNode.depth < RSF.DOM_BAIL_DEPTH) {
            if (currentNode.node.offsetHeight) {
                var tempOffset = RSF.computeCorrectedHeight(currentNode.node);
                if (tempOffset > biggestOffset) {
                    biggestOffset = tempOffset;
                }
            }
            currentNode = getNextNode(currentNode);
        }
        return biggestOffset;
    };

    RSF.decodeRSFStringArray = function (rsfString) {
        var resultArray = new Array();
        var numStart = 0;
        var numEnd = rsfString.indexOf(":");
        var arraySize = rsfString.substring(numStart, numEnd);
        for (var i = 0; i < arraySize; i++) {
            numStart = numEnd + 1;
            numEnd = rsfString.indexOf(":", numStart);
            var num = parseInt(rsfString.substring(numStart, numEnd));
            var value = rsfString.substr(numEnd + 1, num);
            resultArray[i] = value;
            numEnd += num;
        }
        return resultArray;
    };

    /** method to handle logging, logging is off by default so you may need to turn it on */
    RSF.log = function (message) {
        if (logging) {
            if (typeof (YAHOO) != "undefined") {
                YAHOO.log(message);
            } else if (typeof (console) != "undefined") {
                console.log(message);
            } else if (typeof (opera) != "undefined") {
                opera.postError(message);
            }
        }
    };

    RSF.indexOf = function (array, obj) {
        for (var i = 0; i < array.length; ++i) {
            if (array[i] === obj)
                return i;
        }
        return -1;
    };

    RSF.assign = function (target, args) {
        for (var arg in args) {
            target[arg] = args[arg];
        }
    };

    /** method to allow user to enable logging (off by default) */
    RSF.setLogging = function (enabled) {
        if (typeof enabled == "boolean") {
            logging = enabled;
        } else {
            logging = false;
        }
    };

    /** Implementation pass-through for jQuery "data" function, if available. 
     */
    RSF.data = function (elem, name, value) {
        if (typeof jQuery !== "undefined") {
            return jQuery.data(elem, name, value);
        }
    }

    /** Recursively find any data stored under a given name from a node upwards
     * in its DOM hierarchy **/

    RSF.findData = function (elem, name) {
        while (elem) {
            var data = RSF.data(elem, name);
            if (data)
                return data;
            elem = elem.parentNode;
        }
    }
    /**
     * This is a set of three methods to easily accumulate events on elements
     */
    /** adds functions to an element which will trigger when events occur,
     * maintains any existing function and protects the execution to
     * ensure that every function will have a chance to execute
     * RETURN: the return of the final function is sent back, other returns are discarded
     */
    RSF.addEventToElement = function (element, event, newFunction) {
        if (typeof newFunction == "function") {
            var origEvent = element["on" + event];
            if (typeof origEvent == "function") {
                element["on" + event] = function () {
                    var result = false;
                    try {
                        var r = origEvent();
                        if (r != null && typeof r != 'undefined') {
                            result = r;
                        }
                    } catch (e) {
                        RSF.log("function (" + origEvent + ") failure occurred: " + e.message);
                    }
                    try {
                        var r = newFunction();
                        if (r != null && typeof r != 'undefined') {
                            result = r;
                        }
                    } catch (e) {
                        alert("function (" + newFunction + ") failure occurred: " + e.message);
                    }
                    return result;
                }
            } else {
                element["on" + event] = function () {
                    var result = false;
                    try {
                        var r = newFunction();
                        if (r != null && typeof r != 'undefined') {
                            result = r;
                        }
                    } catch (e) {
                        alert("function (" + newFunction + ") failure occurred: " + e.message);
                    }
                    return result;
                }
            }
        }
    };

    // Following definitions taken from PPK's "Event handling challenge" winner
    // thread comments at 
    // http://www.quirksmode.org/blog/archives/2005/10/_and_the_winner_1.html
    // http://dean.edwards.name/weblog/2005/10/add-event/?full#comments
    RSF.addEvent = function (element, type, handler) {
        if (element.addEventListener)
            element.addEventListener(type, handler, false);
        else {
            if (!handler.$$guid)
                handler.$$guid = addEvent_guid++;
            if (!element.$$guid)
                element.$$guid = addEvent_guid++;
            if (!addEvent_handlers[element.$$guid])
                addEvent_handlers[element.$$guid] = {};
            var handlers = addEvent_handlers[element.$$guid][type];
            if (!handlers) {
                handlers = addEvent_handlers[element.$$guid][type] = {};
                if (element['on' + type])
                    handlers[0] = element['on' + type];
            }
            handlers[handler.$$guid] = handler;
            element['on' + type] = handleEvent;
        }
    };

    RSF.removeEvent = function (element, type, handler) {
        if (!element.$$guid)
            return;
        if (element.removeEventListener)
            element.removeEventListener(type, handler, false);
        if (addEvent_handlers[element.$$guid] && addEvent_handlers[element.$$guid][type]) {
            delete addEvent_handlers[element.$$guid][type][handler.$$guid];
        }
    };

    RSF.isPrimaryFirer = function (element) {
        return primaryElements(element.id);
    };

    RSF.primaryFirerCount = function () {
        var count = 0;
        for (var i in primaryElements) {
            ++count;
        }
        return count;
    };

    /** Gets a function that will update this field's value. Supply "oldvalue"
     * explicitly if this has been an "autonomous" change, otherwise it will
     * be taken from the current value. **/
    RSF.getModelFirer = function (element) {
        return function (primary, newvalue, oldvalue) {
            RSF.log("modelFirer element " + element.id + " fire primary=" + primary + " newvalue " + newvalue
                    + " oldvalue " + oldvalue);
            if (!primary && primaryElements[element.id]) {
                RSF.log("Censored model fire for non-primary element " + element.id);
                return;
            }
            var actualold = arguments.length == 3 ? oldvalue : element.value;
            RSF.log("Actual old value " + actualold);
            if (newvalue != actualold) {
                if (primary) {
                    RSF.log("Set primary element for " + element.id);
                    primaryElements[element.id] = true;
                }
                try {
                    var firer = getElementFirer(element);
                    RSF.log("fieldChange: " + actualold + " to " + newvalue);
                    //Preserve selection area
                    var estart = element.selectionStart, eend = element.selectionEnd;
                    element.value = newvalue;
                    element.setSelectionRange(estart, eend);
                    firer.fireEvent();
                } finally {
                    if (primary) {
                        RSF.log("Unset primary element for " + element.id);
                        delete primaryElements[element.id];
                    }
                }
            }
        }
    };
    /** target is the element on which the listener is to be attached.
     */
    RSF.addElementListener = function (target, listener, exclusions) {
        getElementFirer(target).addListener(listener, exclusions);
    };

    RSF.getDOMModifyFirer = function () {
        return getElementFirer(document);
    };

    RSF.queueAJAXRequest = function (token, method, url, parameters, callbacks, options) {
        RSF.log("queueAJAXRequest: token " + token);
        options = options || {};
        var callbacks1 = wrapCallbacks(callbacks, restartWrapper);
        var callbacks2 = wrapCallbacks(callbacks1, primaryRestorationWrapper());
        if (requestactive) {
            RSF.log("Request is active, queuing for token " + token);
            queuemap[token] = packAJAXRequest(method, url, parameters, callbacks2, options);
        } else {
            requestactive = true;
            RSF.issueAJAXRequest(method, url, parameters, callbacks2, options);
        }

        function restartWrapper(callback) {
            return function () {
                requestactive = false;
                RSF.log("Restart callback wrapper begin");
                callback.apply(null, arguments);
                RSF.log("Callback concluded, beginning restart search");
                for (var i in queuemap) {
                    RSF.log("Examining for token " + i);
                    if (requestactive)
                        return;
                    var queued = queuemap[i];
                    delete queuemap[i];
                    RSF.queueAJAXRequest(token, queued.method, queued.url, queued.parameters,
                            queued.callback);
                }
            };
        }
    };

    RSF.issueAJAXRequest = function (method, url, parameters, callback, options) {
        method = method.toUpperCase(); // force method to uppercase for comparison
        options = options || {};
        var is_http_request = url.indexOf("http") === 0;
        var readyCallback = function () {
            if (http_request.readyState == 4) {
                if (http_request.status == 200 || !is_http_request) {
                    RSF.log("AJAX request success status: " + http_request.status);
                    if (callback && callback.success) {
                        callback.success(http_request);
                    }
                    RSF.log("AJAX callback concluded");
                } else {
                    if (callback && callback.failure) {
                        callback.failure(http_request);
                    }
                    RSF.log("AJAX request error status: " + http_request.status);
                }
            }
        }
        var http_request = false;
        if (window.XMLHttpRequest) { // Mozilla, Safari,...
            http_request = new XMLHttpRequest();
            if (method == "POST" && http_request.overrideMimeType) {
                // set type accordingly to anticipated content type
                //http_request.overrideMimeType('text/xml');
                http_request.overrideMimeType('text/xml');
            }
        } else if (window.ActiveXObject) { // IE
            try {
                http_request = new ActiveXObject("Msxml2.XMLHTTP");
            } catch (e) {
                try {
                    http_request = new ActiveXObject("Microsoft.XMLHTTP");
                } catch (e) {
                }
            }
        }
        if (!http_request) {
            RSF.log('Cannot create XMLHTTP instance');
            return false;
        }

        http_request.onreadystatechange = readyCallback;
        if (method == "GET") {
            url = url + "?" + parameters;
        }
        http_request.open(method, url, true);
        if (method == "GET") {
            http_request.send(null);
        } else { // assume POST
            var contentType = "application/x-www-form-urlencoded";
            if (options.headers && options.headers["Content-type"]) {
                contentType = options.headers["Content-type"];
            }
            http_request.setRequestHeader("Content-type", contentType);
            http_request.setRequestHeader("Content-length", parameters.length);
            http_request.setRequestHeader("Connection", "close");
            http_request.send(parameters);
        }
        //delete(http_request); // NOTE: clearing this causes problems for IE7 and lower
        return true; // true if sent to the server
    };



    // From FossilizedConverter.java 
    // key = componentid-fossil, value=[i|o]uitype-name#{bean.member}oldvalue 
    // and
    // key = [deletion|el]-binding, value = [e|o]#{el.lvalue}rvalue 

    RSF.parseFossil = function (fossil) {
        fossilex = /(.)(.*)#\{(.*)\}(.*)/;
        var matches = fossil.match(fossilex);
        var togo = new Object();
        togo.input = matches[1] != 'o';
        togo.uitype = matches[2];
        togo.lvalue = matches[3];
        togo.oldvalue = matches[4];
        return togo;
    };

    RSF.parseBinding = function (binding, deletion) {
        bindingex = /(.)#\{(.*)\}(.*)/;
        var matches = binding.match(bindingex);
        var togo = new Object();
        togo.EL = matches[1] == 'e';
        togo.lvalue = matches[2];
        togo.rvalue = matches[3];
        togo.isdeletion = deletion == "deletion";
        return togo;
    };

    RSF.encodeElement = function (key, value) {
        return encodeURIComponent(key) + "=" + encodeURIComponent(value);
    };

    /** Renders an OBJECT binding, i.e. assigning a concrete value to an EL path **/
    RSF.renderBinding = function (lvalue, rvalue) {
        RSF.log("renderBinding: " + lvalue + " " + rvalue);
        var binding = RSF.encodeElement("el-binding", "o#{" + lvalue + "}" + rvalue);
        RSF.log("Rendered: " + binding);
        return binding;
    };

    RSF.renderUVBQuery = function (readEL) {
        return RSF.renderBinding("UVBBean.paths", readEL);
    };
    RSF.renderActionBinding = function (methodbinding) {
        return RSF.encodeElement("Fast track action", methodbinding);
    };
    RSF.getUVBResponseID = function (readEL) {
        return ":" + readEL + ":";
    };

    /** Accepts a list of elements and a list of EL paths to be queried */
    RSF.getUVBSubmissionBody = function (elements, queryEL) {
        var queries = new Array();
        for (var i = 0; i < elements.length; i++) {
            queries.push(RSF.getPartialSubmissionSegment(elements[i]));
        }
        for (var i = 0; i < queryEL.length; i++) {
            queries.push(RSF.renderUVBQuery(queryEL[i]));
        }
        return queries.join("&");
    };

    /** Accepts a list of elements, returns the params string (body) */
    RSF.getPartialSubmissionBody = function (elements) {
        var queries = new Array();
        for (var i = 0; i < elements.length; i++) {
            queries.push(RSF.getPartialSubmissionSegment(elements[i]));
        }
        return queries.join("&");
    };

    /** Accepts a form, returns the params string (body). If "submittingel" is
     * set, it will exclude submissions from any other input type="submit". */
    RSF.getCompleteSubmissionBody = function (form, submittingel) {
        var queries = new Array();
        var elements = form.elements;
        for (var i = 0; i < elements.length; i++) {
            var element = elements[i];
            if (submittingel != null && typeof (submittingel) != "undefined") {
                if (element.nodeName.toLowerCase() == "input" &&
                        element.getAttribute("type") == "submit" && element != submittingel)
                    continue;
            }
            queries.push(RSF.encodeElement(element.name, element.value));
        }
        return queries.join("&");
    };

    RSF.hasUVBError = function (UVB, namebase) {
        for (var i = 0; i < UVB.message.length; ++i) {
            var message = UVB.message[i];
            if (message.severity == "error" && message.target.indexOf(namebase) == 0)
                return true;
        }
        return false;
    };
    /** Accumulates a response from the UVBView into a compact object 
     * representation.<b>
     * @return o, where o.EL is a map from the requested EL set to the text value
     * returned from the request model, and o.message is a list of {target, text}
     * for any TargettedMessages generated during the request cycle.
     */
    RSF.accumulateUVBResponse = function (responseDOM) {
        var togo = new Object();
        togo.EL = new Object();
        togo.message = new Array();
        togo.isError = false;
        var values = responseDOM.getElementsByTagName("value");

        for (var i = 0; i < values.length; ++i) {
            var value = values[i];
            //if (!value.getAttribute) continue;
            var id = value.getAttribute("id");
            var text = RSF.getElementText(value);
            console.log(text+"nano");
            RSF.log("Value id " + id + " text " + text);
            if (id.substring(0, 4) == "tml:") {
                var target = value.getAttribute("target");
                var severity = value.getAttribute("severity");
                togo.message.push({target: target, severity: severity, text: text});
                if (severity == "error") {
                    togo.isError = true;
                }
            } else {
                // In 0.7.3M2 the Dom is being populated by JSON by default, so we must
                // eval the items. (otherwise the quotes are still on regular String objects)
                togo.EL[id] = eval(text);
            }
        }
        return togo;
    };
    /** Return the element text from the supplied DOM node as a single String */
    RSF.getElementText = function (element) {
        var nodes = element.childNodes;
        var text = "";
        for (var i = 0; i < nodes.length; ++i) {
            var child = nodes[i];
            if (child.nodeType == 3) {
                text = text + child.nodeValue;
            }
        }
        return text;
    };

    RSF.findForm = function (element) {
        while (element) {
            if (element.nodeName.toLowerCase() == "form")
                return element;
            element = element.parentNode;
        }
    };
    /** Returns an decreasingly nested set of paths starting with the supplied
     *  EL, thus for path1.path2.path3 will return the list 
     *  {path1.path2.path3,  path1.path2,  path1} */
    RSF.parseEL = function (EL) {
        var togo = new Array();
        togo.push(EL);
        while (true) {
            var lastdotpos = EL.lastIndexOf(".");
            if (lastdotpos == -1)
                break;
            EL = EL.substring(0, lastdotpos);
            togo.push(EL);
        }
        return togo;
    };
    /** Returns a set of DOM elements (currently of type <input>) 
     * corresponding to the set involved in the EL cascade formed by
     * submission of the supplied element.
     * @param container A DOM element (probably <div>) to be searched for
     * upstream bindings
     * @param element The primary submitting control initiating the cascade.
     */
    RSF.getUpstreamElements = function (element) {
        var container = RSF.findForm(element);
        var inputs = container.getElementsByTagName("input");
        var name = element.name;

        var fossil;
        var bindings = new Array(); // an array of parsed bindings

        var bindingex = /^(.*)-binding$/; // recognises el-binding as well as virtual-el-binding

        for (var i = 0; i < inputs.length; ++i) {
            var input = inputs[i];
            if (input.name || input.id) {
                var name = input.name ? input.name : input.id;
                RSF.log("Discovered input name " + name + " value " + input.value);
                if (isFossil(element, input)) {
                    fossil = RSF.parseFossil(input.value);
                    fossil.element = input;
                    RSF.log("Own Fossil " + fossil.lvalue + " oldvalue " + fossil.oldvalue);
                }
                var matches = name.match(bindingex);
                if (matches != null) {
                    var binding = RSF.parseBinding(input.value, matches[0]);
                    RSF.log("Binding lvalue " + binding.lvalue + " " + binding.rvalue);
                    binding.element = input;
                    bindings.push(binding);
                }
            }
        }

        // a map of EL expressions to DOM elements
        var invalidated = new Object();
        invalidated.list = new Array();
        invalidate(invalidated, fossil.lvalue, fossil.element);
        RSF.log("Beginning invalidation sweep from initial lvalue " + fossil.lvalue);

        // silly O(n^2) algorithm - writing graph algorithms in Javascript is a pain!
        while (true) {
            var expanded = false;
            for (var i in bindings) {
                var binding = bindings[i];
                if (isInvalidated(invalidated, binding.rvalue)) {
                    invalidate(invalidated, binding.lvalue, binding.element);
                    expanded = true;
                }
                delete bindings[i];
            }
            if (!expanded)
                break;
        }
        return invalidated.list;
    }; // end getUpstreamElements

    /** Return the body of a "partial submission" POST corresponding to the
     * section of a form contained within argument "container" rooted at
     * the supplied "element", "as if" that form section were to be submitted
     * with element's value set to "value" */
    RSF.getPartialSubmissionSegment = function (element) {
        var upstream = RSF.getUpstreamElements(element);
        var body = new Array();
        // a "virtual field" has no submitting name, implicitly its id.
        var subname = element.name ? element.name : element.id;
        body.push(RSF.encodeElement(subname, element.value));
        for (var i = 0; i < upstream.length; i++) {
            var upel = upstream[i];

            var fossilex = /(.*)-fossil/;
            var value = upel.value;
            var name = upel.name ? upel.name : upel.id;
            if (name.match(fossilex)) {
                value = 'j' + value.substring(1);
            }
            RSF.log("Upstream " + i + " name " + name + " value " + value + " el " + upel);
            body.push(RSF.encodeElement(normaliseBinding(upel), value));
        }
        return body.join("&");
    };

    /** Duplicates a node corresponding to an RSF branch, with rewriting of
     * all the enclosed IDs. The new branch is returned.
     * @param element The DOM element to be duplicated
     * @param newBranchId The new full ID to be given to the branch
     * @param The last existing replicate of the branch, after which the copy is
     * to be placed
     */
    RSF.duplicateBranch = function (element, newBranchId, lastExistingEl) {
        var duplicate = element.cloneNode(true);
        RSF.rewriteIDs(duplicate, newBranchId);
        RSF.insertAfter(duplicate, lastExistingEl);
        RSF.getDOMModifyFirer().fireEvent();
    };
    /** Mockup of a missing DOM function **/
    RSF.insertAfter = function (newChild, refChild) {
        var nextSib = refChild.nextSibling;
        if (!nextSib) {
            refChild.parentNode.appendChild(newChild);
        } else {
            refChild.parentNode.insertBefore(newChild, nextSib);
        }
    };

    /** Rewrite the ids of ALL recursively descended rsf-allocated nodes to
     * reflect the change in nameBase
     * @param element A DOM element which containing children whose IDs are to be
     * rewritten (assumed freshly cloned and not joined to the main DOM)
     * @param newBranchID The ID to be assigned at the root node of the DOM
     */
    RSF.rewriteIDs = function (element, newBranchId) {
        var colpos = newBranchId.lastIndexOf(':', newBranchId.length - 2);
        var nameBase = newBranchId.substring(0, colpos + 1);
        var localID = newBranchId.substring(colpos + 1); // contains trailing colon

        var elid = element.getAttribute('id');
        if (elid) {
            //      nameBase:dynamic-list-input-row::1:remove
            if (elid.indexOf(nameBase) == 0) {
                var colpos = elid.indexOf(':', nameBase.length);
                var newid = nameBase + localID + (colpos == -1 ? "" : elid.substring(colpos + 1));
                element.setAttribute('id', newid);
            }
        }

        if (element.childNodes) {
            for (var i = 0; i < element.childNodes.length; ++i) {
                var child = element.childNodes[i];
                if (child.nodeType == 1) {
                    RSF.rewriteIDs(child, newBranchId);
                }
            }
        }
    };

    /** Return the ID of another element in the same container as the
     * "base", only with the local ID (rsf:id) given by "targetiD"
     */
    RSF.getRelativeID = function (baseid, targetid) {
        colpos = baseid.lastIndexOf(':');
        return baseid.substring(0, colpos + 1) + targetid;
    };

    RSF.getBaseID = function (id) {
        colpos = id.lastIndexOf(':');
        return id.substring(0, colpos + 1)
    };


    RSF.getLocalID = function (baseid) {
        colpos = baseid.lastIndexOf(':');
        return baseid.substring(colpos + 1);
    };

    /** 
     * Sends a UVB AJAX request
     * sourceFields is a list of the JS form elements which you want to send in this request,
     * AJAXURL is the url to send to (this must be the UVB producer url),
     * bindings is a list of strings which indicate the bindings to return
     */
    RSF.getAJAXUpdater = function (sourceFields, AJAXURL, bindings, callback) {
        var AJAXcallback = {
            success: function (response) {
                RSF.log("Response success: " + response + " " + response.responseText);
                var UVB = RSF.accumulateUVBResponse(response.responseXML);
                RSF.log("Accumulated " + UVB);
                callback(UVB);
            }
        }
        return function () {
            var body = RSF.getUVBSubmissionBody(sourceFields, bindings);
            RSF.log("Firing AJAX request " + body);
            RSF.queueAJAXRequest(bindings[0], "POST", AJAXURL, body, AJAXcallback);
        }
    };

    /** Submit a form via AJAX, the results of the submit are returned in the 
     * response value which is passed to the callback function
     * form: the JS form object to submit via ajax
     * ajaxUrl: optionally allows the user to specify a url to send the request to,
     *   by default this will use the url of the form action
     * submittingel: optionally allows the user to specify that a particular
     *   control is to be used to submit the form (that is, the submitting names
     *   of other submit controls should be suppressed).
     */
    RSF.getAJAXFormUpdater = function (form, callback, ajaxUrl, submittingel) {
        RSF.log("getAJAXFormUpdater: " + form);
        if (!ajaxUrl)
            ajaxUrl = form.action;
        var AJAXcallback = {
            success: function (response) {
                RSF.log("Response success: " + response + " " + response.responseText);
                callback(response.responseText);
            }
        };
        return function () {
            var body = RSF.getCompleteSubmissionBody(form, submittingel);
            RSF.log("Firing AJAX request " + body);
            RSF.queueAJAXRequest(form, form.method, ajaxUrl, body, AJAXcallback);
            // ensure the non-ajax action does not fire
            return false;
        }
    };

    /** Submit a link (anchor) via AJAX,
     * the results of the link submission are returned in the response 
     * value which is passed to the callback function
     * link: a JS anchor element ("A" tag)
     * ajaxUrl: optionally allows the user to specify a url to send the request to,
     * by default this will use the url of the original link
     */
    RSF.getAJAXLinkUpdater = function (link, callback, ajaxUrl) {
        RSF.log("getAJAXLinkUpdater: " + link);
        if (!ajaxUrl)
            ajaxUrl = link.href;
        var body = "";
        if (ajaxUrl.indexOf("?") > 0) {
            var parsed = link.href.split("?");
            ajaxUrl = parsed[0];
            body = parsed[1];
        }
        var AJAXcallback = {
            success: function (response) {
                RSF.log("Response success: " + response + " " + response.responseText);
                callback(response.responseText);
            }
        };
        return function () {
            RSF.log("Firing AJAX request " + ajaxUrl + ":" + body);
            RSF.queueAJAXRequest(link, "get", ajaxUrl, body, AJAXcallback);
            // ensure the non-ajax action does not fire
            return false;
        }
    };

    /** Submit part of a form via AJAX,
     * sourceFields: a list of the JS form elements which you want to send in this request
     * (should include the submit element if you want to trigger a method, can include only this also)
     * the results of the submit are returned in the response value which is passed to the callback function
     * ajaxUrl: optionally allows the user to specify a url to send the request to,
     * by default this will use the url of the form containing the first sourceField
     */
    RSF.getAJAXPartialUpdater = function (sourceFields, callback, ajaxUrl) {
        RSF.log("getAJAXPartialUpdater: " + sourceFields);
        var form = sourceFields[0].form; // form from the first element
        if (!ajaxUrl)
            ajaxUrl = form.action;
        var AJAXcallback = {
            success: function (response) {
                RSF.log("Response success: " + response + " " + response.responseText);
                callback(response.responseText);
            }
        };
        return function () {
            var body = RSF.getPartialSubmissionBody(sourceFields);
            RSF.log("Firing AJAX request " + body);
            RSF.queueAJAXRequest(sourceFields[0], form.method, ajaxUrl, body, AJAXcallback);
            // ensure the non-ajax action does not fire
            return false;
        }
    };
    RSF.parseUri = function (URI) {
        return parseUri(URI);
    };

    RSF.hasCSSClass = function (element, cssClass) {
        return new RegExp('\\b' + cssClass + '\\b').test(element.className)
    };
    RSF.addCSSClass = function (element, cssClass) {
        if (!RSF.hasCSSClass(element, cssClass)) {
            element.className += (element.className ? ' ' + cssClass : cssClass);
        }
    };
    RSF.removeCSSClass = function (element, cssClass) {
        var rep = element.className.match(' ' + cssClass) ? ' ' + cssClass : cssClass;
        element.className = element.className.replace(rep, '');
    };

    RSF.deriveRequiredID = function (id) {
        local = RSF.getLocalID(id);
        return RSF.getRelativeID(id, "required-" + local);
    };

    RSF.deriveMessageID = function (id) {
        local = RSF.getLocalID(id);
        return RSF.getRelativeID(id, "message-for-" + local);
    };

    RSF.getRequiredFirer = function (element, invalidCSSClass, packageBase) {
        return function () {
            var isBlank = !element.value || element.value == "";
            if (isBlank) {
                RSF.addCSSClass(element, invalidCSSClass);
            } else {
                RSF.removeCSSClass(element, invalidCSSClass);
            }
        };
    };

    RSF.addRequiredValidator = function (element, invalidCSSClass, packageBase) {
        var requiredFirer = RSF.getRequiredFirer(element, invalidCSSClass, packageBase);
        RSF.addEventToElement(element, 'blur', requiredFirer);
        RSF.addEventToElement(element, 'change', requiredFirer);
    };

    RSF.setRequiredStyle = function (element, requiredCSSClass) {
        if (!requiredCSSClass)
            requiredCSSClass = 'required-css-class';
        var requiredID = RSF.deriveRequiredID(element.id);
        if (!$it(requiredID)) {
            var node = document.createElement("div");
            node.setAttribute('style', 'display:inline');
            node.setAttribute('class', requiredCSSClass);
            node.setAttribute('id', requiredID);
            element.parentNode.insertBefore(node, element);
        }
    };

    RSF.initValidation = function (formId, requiredCSSClass, invalidCSSClass, packageBase) {
        var form = $it(formId);
        var items = form.elements.length;
        for (var i = 0; i < items; ++i) {
            var element = form.elements[i];
            var valid = element.getAttribute("rsf:valid");
            if (!valid)
                continue;
            if (valid.indexOf("required") != -1) {
                RSF.setRequiredStyle(element, requiredCSSClass);

                RSF.addRequiredValidator(element, invalidCSSClass, packageBase);
            }
        }
    };

    RSF.evaluateScript = function (text, message) {
        if (!message)
            message = "";
        try {
            eval(text);
        } catch (e) {
            RSF.log("Exception evaluating script " + message + ": " + e);
        }
    };

    RSF.evaluateScriptUrl = function (scripturl) {
        var pu = parseUri(scripturl);
        if (pu.authority === "..") {
            // work around relative URL bug in parseUri
            pu.path = ".." + pu.path;
            pu.authority = "";
        }
        var splits = pu.path.split("/");
        var stack = [];
        for (var i = 0; i < splits.length; ++i) {
            var top = stack[stack.length - 1];
            if (splits[i] === '..' && stack.length > 0 && top !== ".." && top !== "") {
                stack.length--;
            } else {
                stack[stack.length] = splits[i];
            }
        }
        var repath = stack.join("/");
        var canon = pu.authority + repath;
        if (!loadedScripts[canon]) {
            ++outstandingScripts;
            RSF.issueAJAXRequest("GET", canon, null, {
                success: function (response) {
                    RSF.evaluateScript(response.responseText, " at URL " + canon);
                    loadedScripts[canon] = true;
                    --outstandingScripts;
                    if (outstandingScripts === 0) {
                        for (var i = 0; i < queuedScripts.length; ++i) {
                            RSF.evaluateScript(queuedScripts[i]);
                            ;
                        }
                        queuedScripts.length = 0;
                    }
                }
            });
        }
    };

    RSF.evaluateScripts = function (parentNode) {
        var scripts = parentNode.getElementsByTagName("script");
        for (var i = 0; i < scripts.length; i++) {
            var script = scripts[i];
            var src = script.getAttribute("src");
            if (src) {
                RSF.evaluateScriptUrl(src);
            } else {
                var strExec = scripts[i].innerHTML;
                if (outstandingScripts > 0) {
                    queuedScripts[queuedScripts.length] = strExec;
                } else {
                    RSF.evaluateScript(strExec);
                }
            }
        }
    };

    /** 
     * Transform all action and navigation elements inside a DOM block into AJAX action elements,
     * This will keep the forms from submitting, the page from changing, and the links
     * from causing navigation to proceed off the page
     * parentNode: all action elements contained within this node will be transformed to
     * having navigation transitions "decorated"
     * targetNode: the effect of the decorated navigation will be to cause replacement of
     * DOM contents of this target node.
     * RETURN: the list of updated action elements
     */
    RSF.transformActionDomToAJAX = function (parentNode, targetNode) {
        if (typeof (targetNode) == "undefined" || targetNode == null) {
            targetNode = parentNode;
        }
        RSF.log("transformActionDomToAJAX: node=" + parentNode);
        var updatedElements = [];

        // define the callback function for the ajax response
        var callback = function (results) {
            var targetNodeRes = targetNode;
            if (typeof (targetNode) === "function") {
                targetNodeRes = targetNode.apply();
            }
            // specifically purge the existing items before putting in the new stuff
            while (targetNodeRes.childNodes[0]) {
                targetNodeRes.removeChild(targetNodeRes.childNodes[0]);
            }
            // now drop in the new xhtml result into this node
            targetNodeRes.innerHTML = results;
            RSF.getDOMModifyFirer().fireEvent();
            // rerun the dom transformer on the replacement xhtml
            RSF.transformActionDomToAJAX(targetNodeRes, targetNodeRes);
        }

        var inputs = parentNode.getElementsByTagName("input");
        for (var i = 0; i < inputs.length; ++i) {
            var input = inputs[i];
            if (input.getAttribute("type").toLowerCase() != "submit")
                continue;
            var updater = RSF.getAJAXFormUpdater(input.form, callback, null, input);
            RSF.addEventToElement(input, "click", updater);
            updatedElements.push(input);
        }
        // get elementsbyname to get the forms
        // this code disused in favor of direct "input" detection as above.
        // however, we probably want the capability to revive it to deal with the
        // case of existing AJAX-ly submitted forms?
//      var forms = parentNode.getElementsByTagName("form");
//      for (var i = 0; i < forms.length; i++) {
//         var form = forms[i];
//
//         var updater = RSF.getAJAXFormUpdater(form, callback);
//
//         RSF.addEventToElement(form, "submit", updater);
//         updatedElements.push(form);
//     }

        // get elementsbyname to get the links (a)
        var links = parentNode.getElementsByTagName("a");
        for (var i = 0; i < links.length; i++) {
            var link = links[i];

            if (!link.href || link.href.length == 0) {
                RSF.log("link is empty or appears to be invalid so skipping it: " + link.href);
                continue;
            }

            var parsed = parseUri(link.href);
            if (parsed.host == "" || parsed.host == document.domain) {
                var updater = RSF.getAJAXLinkUpdater(link, callback);

                RSF.addEventToElement(link, "click", updater);
                updatedElements.push(link);
            } else {
                RSF.log("link is not in this domain so skipping it: " + link.href);
                continue;
            }
        }
        RSF.evaluateScripts(parentNode);

        return updatedElements;
    };


})(); // end namespace RSF

function fixLink(link) {
    var url = link.href;
    var i = url.indexOf("/site/");
    if (i >= 0) {
	var j = url.indexOf("/tool/");
	if (j >= 0) {
	    link.href = url.substring(0, i) + url.substring(j);
	}
    }
}

var MailSender = function()
{
	// attachment count keeps a net count of 
	var attachmentCount = 0;

	return {
		addAttachment : function(containerId)
		{
			// setup the screen to show differently if no attachments are showing
			if (attachmentCount == 0)
			{
				jQuery('#attachOuter').show();
				jQuery('#attachLink').hide();
				jQuery('#attachMoreLink').show();
			}
			Attachment.addAttachment(containerId);
			attachmentCount++;
		},

		removeAttachment : function(containerId, newDivId)
		{
			Attachment.removeAttachment(containerId, newDivId);
			attachmentCount--;
			if (attachmentCount === 0)
			{
				jQuery('#attachOuter').hide();
				jQuery('#attachLink').show();
				jQuery('#attachMoreLink').hide();
			}
		}
	}; // end return
}(); // end namespace

var Attachment = function()
{
	// attachment index will never decrease on a single request no matter how many
	// add/removes happen.  It is easier and safe to keep counting up than to rename and
	// replace removed numbers
	var attachmentIndex = 0;

	return {

		addAttachment : function(containerId)
		{
			// get a handle to the container
			var area = document.getElementById(containerId);

			// create the name for the div and the attachment field
			var newAttachmentId = 'attachment' + (attachmentIndex);
			var newDivId = newAttachmentId + 'Div';
			attachmentIndex++;

			// create the outer div element
			var newDiv = document.createElement('div');
			newDiv.id = newDivId;

			// create the file upload field
			var input = document.createElement('input');
			input.type = 'file';
			input.name = newAttachmentId;
			newDiv.appendChild(input);

			// create the remove link
			var link = document.createElement('a');
			link.className = 'removeAttachment';
			link.href = '#';
			link.onclick = function()
			{
				MailSender.removeAttachment(containerId, newDivId);
				return false;
			}
			link.innerHTML = 'Remove';
			newDiv.appendChild(link);

			// append the new div to the container
			area.appendChild(newDiv);
		},

		/**
		 * Remove a attachment div from a container
		 */
		removeAttachment : function(containerId, divId)
		{
			var area = document.getElementById(containerId);
			var div = document.getElementById(divId);
			area.removeChild(div);
		}
	}; // end return
}(); // end namespace

var Dirty = function()
{
	/**
	 * Collect all fossils on the page along with the user input fields
	 */
	function collectFossils()
	{
		var fossilex =  /^(.*)-fossil$/;
		var fossils = {};
		if (typeof elements !== 'undefined' && elements)
		{
			jQuery(':input').each(function()
			{
				var element = elements[i];
				// see if name exists and matches regex
				if (this.name)
				{
					var matches = this.name.match(fossilex);
					if (matches != null)
					{
						// use the name sans '-fossil' to store the element
						// this saves having to parse the field name again
						// later in processing.
						fossils[matches[1]] = this;
					}
				}
			});
		}
		return fossils;
	}

	return {
		isDirty : function()
		{
			var dirty = false;
			var fossilElements = collectFossils();
			var inputs = [];
			for (propName in fossilElements)
			{
				var fossilElement = fossilElements[propName];
				var fossil = RSF.parseFossil(fossilElement.value);
				jQuery(':' + propName).each(function()
				{
					if (((this.type == 'checkbox') && (this.checked != (fossil.oldvalue == "true")))
							|| ((this.type == 'select') && (this.options[this.selectedIndex].value != fossil.oldvalue))
							|| ((this.type == 'radio') && (this.checked) && (this.value != fossil.oldvalue))
							|| ((this.type == 'text' || this.type == 'textarea' || this.type == 'password') && (this.value != fossil.oldvalue)))
					{
						dirty = true;
						// return false to make jQuery stop processing loop
						return false;
					}
				});
				if (dirty) break;
			}
			return dirty;
		},

		check: function(msg)
		{
			var retval = true;
			if(Dirty.isDirty())
			{
				retval = confirm(msg);
			}
			return retval;
		}
	}; // end return
}(); // end namespace*/

var MailSenderUtil = function()
{
	return {
		/**
		 * Determine if a field is visible or hidden
		 *
		 * elementId: a element id
		 */
		isVisible : function(elementId)
		{
			var el = document.getElementById(elementId);
			return !(el.style.display == 'none' || el.style.visibility == 'hidden');
		},

		/**
		 * Hide a field.
		 *
		 * elementId: a element id or array of element ids
		 */
		hideElement : function(elementId)
		{
			if (elementId instanceof Array)
			{
				for (i in elementId)
					MailSenderUtil.hideElement(elementId[i]);
			}
			else
			{
				var el = document.getElementById(elementId);
				el.style.display = 'none';
				el.style.visibility = 'hidden';
			}
		},

		/**
		 * Show a field.
		 *
		 * elementId: a element id or array of element ids
		 * showInline: whether to show the element as inline or block
		 */
		showElement : function(elementId, showInline)
		{
			if (elementId instanceof Array)
			{
				for (i in elementId)
					MailSenderUtil.showElement(elementId[i]);
			}
			else
			{
				var el = document.getElementById(elementId);
				if (showInline)
					el.style.display = 'inline';
				else
					el.style.display = 'block';
				el.style.visibility = 'visible';
			}
		},

		/**
		 * Toggle the visibility of a field.
		 *
		 * elementId: a element id or array of element ids
		 */
		toggleElement : function(elementId)
		{
			if (elementId instanceof Array)
			{
				for (i in elementId)
					MailSenderUtil.toggleElement(elementId[i]);
			}
			else
			{
				if (MailSenderUtil.isVisible(elementId))
					MailSenderUtil.showElement(elementId);
				else
					MailSenderUtil.hideElement(elementId);
			}
		}
	}; // end return
}(); // end namespace

/* global jQuery */
var RcptSelect = function()
{
	// holds LinkResult objects
	var lastLink = null;
	var lastLinkReplacement = null;

	/**
	 * LinkResults object to hold data
	 *
	 * link: the link to follow (AJAX)
	 * content: the content returned after following link
	 *   showing/hiding results.  Expected values are >= 1.
	 */
	function LinkResult(link, content, resultSelector)
	{
		this.link = link;
		this.content = content;
		this.resultSelector = resultSelector;
	}

	/**
	 * Create an ID that is safe to use by jQuery.  This creates an ID string of the following
	 * syntax:<br/>
	 * <type>[id=<id>]
	 *
	 * @param string type The type of the element
	 * @param string id   The ID of the element.
	 */
	function _safeId(type, id)
	{
	    return type + '[id="' + id + '"]';
	}

	return {
		/**
		 * Show the results obtained from an ajax call.  If the call was made previously and the
		 * results still in the output area, that area is displayed.  Otherwise, the results are
		 * retrieved from the server.
		 *
		 * @param string  link     the link to follow for data retrieval.  should be an anchor object
		 * @param string  resultId the id of the field where results should be placed after retrieval
		 * @param boolean isTLM    flags this link as a top level menu
		 */
		showResults : function(link, resultId, isTLM)
		{
			// throw out the wait sign
			jQuery('body').css('cursor', 'wait');

			var resultArea = null;
			resultArea = jQuery(_safeId('', resultId));

			if (isTLM)
			{
				jQuery('.rolesArea').hide();
			}

			// if no cached content found, request content from server
			if (resultArea.html())
			{
				resultArea.show();
			}
			else
			{
				// setup the function that initiates the AJAX request
			    
			    fixLink(link);

			    var updater = RSF.getAJAXLinkUpdater(link, function(results)
					{
						// put the results on the page
						resultArea.html(results).show();
						// set the checkboxes based on the group level checkbox
						if (!isTLM) {
							RcptSelect.toggleSelectAll(jQuery(link).siblings('input[type=checkbox]:first').attr('id'));
						}
						// We do this in the callback (once the HTML has been added to the DOM).
						//resetFrame();
					});

				// update the page
				updater();
			}

			// create a text version of the link
			var linkText = link.innerHTML;
			var linkTextNode = document.createTextNode(linkText);

			// set the last link to the current clicked link
			if (isTLM)
			{
				// make the last link a clickable link instead of text
				if (lastLink && lastLinkReplacement)
				{
					lastLinkReplacement.parentNode.replaceChild(lastLink, lastLinkReplacement);
				}
				lastLink = link;
				lastLinkReplacement = linkTextNode;

				// replace the link with just the text of the link
				link.parentNode.replaceChild(linkTextNode, link);
			}

			// take down the wait sign
			jQuery('body').css('cursor', 'default');
		},

		/**
		 * Show the 'other recipients' area.
		 *
		 * @return void
		 */
		showOther: function()
		{
			jQuery('#otherRecipientsDiv').show();
			jQuery('#otherRecipientsLink').hide();
			//resetFrame();
                        console.log("tete")
		},

		/**
		 * Show individuals for a group as retrieved from the given link.
		 *
		 * @param string link           The link to follow for results to show.
		 * @param string usersAreaId    The area ID to place the results.
		 * @param string selectLinkId   The ID of the link to use for showing the group.
		 * @param string collapseLinkId The ID of the link to use for collapsing the group.
		 *
		 * @return void
		 */
		showIndividuals: function(link, usersAreaId, selectLinkId, collapseLinkId)
		{
			RcptSelect.showResults(link, usersAreaId, false);
			jQuery(_safeId('a', selectLinkId)).hide();
			jQuery(_safeId('a', collapseLinkId)).show();
			//resetFrame();
		},

		/**
		 * Hide individuals for a group.
		 *
		 * @param string usersAreaId    The area ID to place the results.
		 * @param string selectLinkId   The ID of the link to use for showing the group.
		 * @param string collapseLinkId The ID of the link to use for collapsing the group.
		 *
		 * @return void
		 */
		hideIndividuals: function(usersAreaId, selectLinkId, collapseLinkId)
		{
			jQuery(_safeId('a', collapseLinkId) + ', ' + _safeId('div', usersAreaId)).hide();
			jQuery(_safeId('a', selectLinkId)).show();
			//resetFrame();
		},

		/**
		 * Toggle the children checkboxes within a group based on the parent checkbox status.
		 *
		 * @param string checkboxId ID of the checkbox to use as a base for status.
		 *
		 * @return void
		 */
		toggleSelectAll: function(checkboxId)
		{
			let rcptAll = jQuery('#mailsender-rcpt-all')
			if (!checkboxId)
			{
				var checked = rcptAll.attr('checked');
				if (checked) {
					// Check all boxes in this section
					jQuery('input[type=checkbox]', rcptAll.parents('.section')).attr('checked', true);
				}
			}
			else
			{
				var checkbox = _safeId('input', checkboxId);
				var checked = jQuery(checkbox).is(':checked');
				var context = jQuery(checkbox + ' ~ div');
				jQuery('input[type=checkbox]:enabled', context).attr('checked', checked);
				if (!checked) {
					rcptAll.attr('checked',false);
				}
			}
		},

		/**
		 * Toggle the checkbox status for a child checkbox.  If all enabled children are selected,
		 * the parent checkbox is selected.
		 *
		 * @param string checboxId The ID of the checkbox to check.
		 *
		 * @return void
		 */
		toggleIndividual: function(checkboxId)
		{
			var checkbox = jQuery(_safeId('input', checkboxId));
			/*
			the individual checkboxes are nested down some divs.
			the structure looks like:
			<input id=<selectAll> />
			<div> <-- parent -->
			  <div> <-- parent -->
			    <div> <-- parent -->
			      <input id=<individual> />
			    </div>
			  </div>
			</div>
			*/
			var selectAll = checkbox.parent().parent().parent().siblings('input[type=checkbox]');
			if (!checkbox.is(':checked'))
			{
				selectAll.attr('checked', false);
				jQuery('#mailsender-rcpt-all').attr('checked',false);
			}
			else
			{
				/*
				have to go up 2 parents because each checkbox is held in a div of divs
				<div>
				  <div>
				    <input type=checkbox />
				    <label />
				  </div>
				  <div>
				    <input type=checkbox />
				    <label />
				  </div>
				</div>
				*/
				var allChecked = true;
				jQuery('input[type=checkbox]:enabled', checkbox.parent().parent()).each(function()
					{
						if (!this.checked)
						{
							allChecked = false;
							return false;
						}
					});
				if (allChecked)
				{
					selectAll.attr('checked', true);
				}
			}
		}
	}; // end return
}(); // end namespace
