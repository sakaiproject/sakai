/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 * Copyright 2004-2008 Emmanouil Batsis, mailto: mbatsis at users full stop sourceforge full stop net
 * Copyright 2004-2008 Emmanouil Batsis, mailto: mbatsis at users full stop sourceforge full stop net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 @project JSF JavaScript Library
 @version 2.2
 @description This is the standard implementation of the JSF JavaScript Library.
 */

// Detect if this is already loaded, and if loaded, if it's a higher version
if (!((jsf && jsf.specversion && jsf.specversion >= 23000 ) &&
      (jsf.implversion && jsf.implversion >= 3))) {

    /**
     * <span class="changed_modified_2_2">The top level global namespace
     * for JavaServer Faces functionality.</span>

     * @name jsf
     * @namespace
     */
    var jsf = {};

    /**

     * <span class="changed_modified_2_2 changed_modified_2_3">The namespace for Ajax
     * functionality.</span>

     * @name jsf.ajax
     * @namespace
     * @exec
     */
    jsf.ajax = function() {

        var eventListeners = [];
        var errorListeners = [];

        var delayHandler = null;
        /**
         * Determine if the current browser is part of Microsoft's failed attempt at
         * standards modification.
         * @ignore
         */
        var isIE = function isIE() {
            if (typeof isIECache !== "undefined") {
                return isIECache;
            }
            isIECache =
                   document.all && window.ActiveXObject &&
                   navigator.userAgent.toLowerCase().indexOf("msie") > -1 &&
                   navigator.userAgent.toLowerCase().indexOf("opera") == -1;
            return isIECache;
        };
        var isIECache;

        /**
         * Determine the version of IE.
         * @ignore
         */
        var getIEVersion = function getIEVersion() {
            if (typeof IEVersionCache !== "undefined") {
                return IEVersionCache;
            }
            if (/MSIE ([0-9]+)/.test(navigator.userAgent)) {
                IEVersionCache = parseInt(RegExp.$1);
            } else {
                IEVersionCache = -1;
            }
            return IEVersionCache;
        }
        var IEVersionCache;

        /**
         * Determine if loading scripts into the page executes the script.
         * This is instead of doing a complicated browser detection algorithm.  Some do, some don't.
         * @returns {boolean} does including a script in the dom execute it?
         * @ignore
         */
        var isAutoExec = function isAutoExec() {
            try {
                if (typeof isAutoExecCache !== "undefined") {
                    return isAutoExecCache;
                }
                var autoExecTestString = "<script>var mojarra = mojarra || {};mojarra.autoExecTest = true;</script>";
                var tempElement = document.createElement('span');
                tempElement.innerHTML = autoExecTestString;
                var body = document.getElementsByTagName('body')[0];
                var tempNode = body.appendChild(tempElement);
                if (mojarra && mojarra.autoExecTest) {
                    isAutoExecCache = true;
                    delete mojarra.autoExecTest;
                } else {
                    isAutoExecCache = false;
                }
                deleteNode(tempNode);
                return isAutoExecCache;
            } catch (ex) {
                // OK, that didn't work, we'll have to make an assumption
                if (typeof isAutoExecCache === "undefined") {
                    isAutoExecCache = false;
                }
                return isAutoExecCache;
            }
        };
        var isAutoExecCache;

        /**
         * @ignore
         */
        var getTransport = function getTransport(context) {
            var returnVal;
            // Here we check for encoding type for file upload(s).
            // This is where we would also include a check for the existence of
            // input file control for the current form (see hasInputFileControl
            // function) but IE9 (at least) seems to render controls outside of
            // form.
            if (typeof context !== 'undefined' && context !== null &&
                context.includesInputFile &&
                context.form.enctype === "multipart/form-data") {
                returnVal = new FrameTransport(context);
                return returnVal;
            }
            var methods = [
                function() {
                    return new XMLHttpRequest();
                },
                function() {
                    return new ActiveXObject('Msxml2.XMLHTTP');
                },
                function() {
                    return new ActiveXObject('Microsoft.XMLHTTP');
                }
            ];

            for (var i = 0, len = methods.length; i < len; i++) {
                try {
                    returnVal = methods[i]();
                } catch(e) {
                    continue;
                }
                return returnVal;
            }
            throw new Error('Could not create an XHR object.');
        };
        
        /**
         * Used for iframe based communication (instead of XHR).
         * @ignore
         */
        var FrameTransport = function FrameTransport(context) {
            this.context = context;
            this.frame = null;
            this.FRAME_ID = "JSFFrameId";
            this.FRAME_PARTIAL_ID = "Faces-Request";
            this.partial = null;
            this.aborted = false;
            this.responseText = null;
            this.responseXML = null;
            this.readyState = 0;
            this.requestHeader = {};
            this.status = null;
            this.method = null;
            this.url = null;
            this.requestParams = null;
        };
        
        /**
         * Extends FrameTransport an adds method functionality.
         * @ignore
         */
        FrameTransport.prototype = {
            
            /**
             *@ignore
             */
            setRequestHeader:function(key, value) {
                if (typeof(value) !== "undefined") {
                    this.requestHeader[key] = value;  
                }
            },
            
            /**
             * Creates the hidden iframe and sets readystate.
             * @ignore
             */
            open:function(method, url, async) {
                this.method = method;
                this.url = url;
                this.async = async;
                this.frame = document.getElementById(this.FRAME_ID);
                if (this.frame) {
                    this.frame.parentNode.removeChild(this.frame);
                    this.frame = null;
                }
                if (!this.frame) {  
                    if ((!isIE() && !isIE9Plus())) {
                        this.frame = document.createElement('iframe');
                        this.frame.src = "about:blank";
                        this.frame.id = this.FRAME_ID;
                        this.frame.name = this.FRAME_ID;
                        this.frame.type = "content";
                        this.frame.collapsed = "true";
                        this.frame.style = "visibility:hidden";   
                        this.frame.width = "0";
                        this.frame.height = "0";
                        this.frame.style = "border:0";
                        this.frame.frameBorder = 0;
                        document.body.appendChild(this.frame);
                        this.frame.onload = bind(this, this.callback);
                    } else {
                        var div = document.createElement("div");
                        div.id = "frameDiv";
                        div.innerHTML = "<iframe id='" + this.FRAME_ID + "' name='" + this.FRAME_ID + "' style='display:none;' src='about:blank' type='content' onload='this.onload_cb();'  ></iframe>";
                        document.body.appendChild(div);
                        this.frame = document.getElementById(this.FRAME_ID);
                        this.frame.onload_cb = bind(this, this.callback);
                    }
                }
                // Create to send "Faces-Request" param with value "partial/ajax"
                // For iframe approach we are sending as request parameter
                // For non-iframe (xhr ajax) it is sent in the request header
                this.partial = document.createElement("input");
                this.partial.setAttribute("type", "hidden");
                this.partial.setAttribute("id", this.FRAME_PARTIAL_ID);
                this.partial.setAttribute("name", this.FRAME_PARTIAL_ID);
                this.partial.setAttribute("value", "partial/ajax");
                this.context.form.appendChild(this.partial);
  
                this.readyState = 1;                         
            },
            
            /**
             * Sets the form target to iframe, sets up request parameters
             * and submits the form.
             * @ignore
             */
            send: function(data) {
                var evt = {};
                this.context.form.target = this.frame.name;
                this.context.form.method = this.method;
                if (this.url) {
                    this.context.form.action = this.url;
                }

                this.readyState = 3;

                this.onreadystatechange(evt);
                
                var ddata = decodeURIComponent(data);
                var dataArray = ddata.split("&");
                var input;
                this.requestParams = new Array();
                for (var i=0; i<dataArray.length; i++) {
                    var nameValue = dataArray[i].split("=");
                    if (nameValue[0] === this.context.namingContainerPrefix + "javax.faces.source" ||
                        nameValue[0] === this.context.namingContainerPrefix + "javax.faces.partial.event" ||
                        nameValue[0] === this.context.namingContainerPrefix + "javax.faces.partial.execute" ||
                        nameValue[0] === this.context.namingContainerPrefix + "javax.faces.partial.render" ||
                        nameValue[0] === this.context.namingContainerPrefix + "javax.faces.partial.ajax" ||
                        nameValue[0] === this.context.namingContainerPrefix + "javax.faces.behavior.event") {
                        input = document.createElement("input");
                        input.setAttribute("type", "hidden");
                        input.setAttribute("id", nameValue[0]);
                        input.setAttribute("name", nameValue[0]);
                        input.setAttribute("value", nameValue[1]);
                        this.context.form.appendChild(input);
                        this.requestParams.push(nameValue[0]);
                    }
                }
                this.requestParams.push(this.FRAME_PARTIAL_ID);
                this.context.form.submit();
            },
            
            /**
             *@ignore
             */
            abort:function() {
                this.aborted = true; 
            },
            
            /**
             *@ignore
             */
            onreadystatechange:function(evt) {
                
            },
            
            /**
             * Extracts response from iframe document, sets readystate.
             * @ignore
             */
            callback: function() {
                if (this.aborted) {
                    return;
                }
                var iFrameDoc;
                var docBody;
                try {
                    var evt = {};
                    iFrameDoc = this.frame.contentWindow.document || 
                        this.frame.contentDocument || this.frame.document;
                    docBody = iFrameDoc.body || iFrameDoc.documentElement;
                    this.responseText = docBody.innerHTML;
                    this.responseXML = iFrameDoc.XMLDocument || iFrameDoc;
                    this.status = 201;
                    this.readyState = 4;  

                    this.onreadystatechange(evt);                
                } finally {
                    this.cleanupReqParams();
                }               
            },
            
            /**
             *@ignore
             */
            cleanupReqParams: function() {
                for (var i=0; i<this.requestParams.length; i++) {
                    var elements = this.context.form.childNodes;
                    for (var j=0; j<elements.length; j++) {
                        if (!elements[j].type === "hidden") {
                            continue;
                        }
                        if (elements[j].name === this.requestParams[i]) {
                            var node = this.context.form.removeChild(elements[j]);
                            node = null;                           
                            break;
                        }
                    }   
                }
            }
        };
        
       
        /**
         *Utility function that binds function to scope.
         *@ignore
         */
        var bind = function(scope, fn) {
            return function () {
                fn.apply(scope, arguments);
            };
        };

        /**
         * Utility function that determines if a file control exists
         * for the form.
         * @ignore
         */
        var hasInputFileControl = function(form) {
            var returnVal = false;
            var inputs = form.getElementsByTagName("input");
            if (inputs !== null && typeof inputs !=="undefined") {
                for (var i=0; i<inputs.length; i++) {
                    if (inputs[i].type === "file") {
                        returnVal = true;
                        break;
                    }
                }    
            }
            return returnVal;
        };
        
        /**
         * Find instance of passed String via getElementById
         * @ignore
         */
        var $ = function $() {
            var results = [], element;
            for (var i = 0; i < arguments.length; i++) {
                element = arguments[i];
                if (typeof element == 'string') {
                    element = document.getElementById(element);
                }
                results.push(element);
            }
            return results.length > 1 ? results : results[0];
        };

        /**
         * Get the form element which encloses the supplied element.
         * @param element - element to act against in search
         * @returns form element representing enclosing form, or first form if none found.
         * @ignore
         */
        var getForm = function getForm(element) {
            if (element) {
                var form = $(element);
                while (form) {

                    if (form.nodeName && (form.nodeName.toLowerCase() == 'form')) {
                        return form;
                    }
                    if (form.form) {
                        return form.form;
                    }
                    if (form.parentNode) {
                        form = form.parentNode;
                    } else {
                        form = null;
                    }
                }
                return document.forms[0];
            }
            return null;
        };

        /**
         * Get an array of all JSF form elements which need their view state to be updated.
         * This covers at least the form that submitted the request and any form that is covered in the render target list.
         * 
         * @param context An object containing the request context, including the following properties:
         * the source element, per call onerror callback function, per call onevent callback function, the render
         * instructions, the submitting form ID, the naming container ID and naming container prefix.
         */
        var getFormsToUpdate = function getFormsToUpdate(context) {
            var formsToUpdate = [];

            var add = function(element) {
                if (element) {
                    if (element.nodeName 
                        && element.nodeName.toLowerCase() == "form" 
                        && element.method == "post" 
                        && element.id 
                        && element.elements 
                        && element.id.indexOf(context.namingContainerPrefix) == 0)
                    {
                        formsToUpdate.push(element);
                    }
                    else {
                        var forms = element.getElementsByTagName("form");
    
                        for (var i = 0; i < forms.length; i++) {
                            add(forms[i]);
                        }
                    }
                }
            }

            if (context.formId) {
                add(document.getElementById(context.formId));
            }

            if (context.render) {
                if (context.render.indexOf("@all") >= 0) {
                    add(document);
                }
                else {
                    var clientIds = context.render.split(" ");

                    for (var i = 0; i < clientIds.length; i++) {
                        if (clientIds.hasOwnProperty(i)) {
                            add(document.getElementById(clientIds[i]));
                        }
                    }
                }
            }

            return formsToUpdate;
        }

        /**
         * <p>Namespace given space separated parameters if necessary (only
         * call this if there is a namingContainerPrefix!).  This
         * function is here for backwards compatibility with manual
         * jsf.ajax.request() calls written before Spec790 changes.</p>

         * @param parameters Spaceseparated string of parameters as
         * usually specified in f:ajax execute and render attributes.

         * @param sourceClientId The client ID of the f:ajax
         * source. This is to be used for prefixing relative target
         * client IDs.

         * It's expected that this already starts with
         * namingContainerPrefix.

         * @param namingContainerPrefix The naming container prefix (the
         * view root ID suffixed with separator character).

         * This is to be used for prefixing absolute target client IDs.
         * @ignore
         */
        var namespaceParametersIfNecessary = function namespaceParametersIfNecessary(parameters, sourceClientId, namingContainerPrefix) {
            if (sourceClientId.indexOf(namingContainerPrefix) != 0) {
                return parameters; // Unexpected source client ID; let's silently do nothing.
            }

        	var targetClientIds = parameters.replace(/^\s+|\s+$/g, '').split(/\s+/g);

        	for (var i = 0; i < targetClientIds.length; i++) {
        	    var targetClientId = targetClientIds[i];

        	    if (targetClientId.indexOf(jsf.separatorchar) == 0) {
        	        targetClientId = targetClientId.substring(1);

        	        if (targetClientId.indexOf(namingContainerPrefix) != 0) {
                        targetClientId = namingContainerPrefix + targetClientId;
                    }
        	    }
        	    else if (targetClientId.indexOf(namingContainerPrefix) != 0) {
        	        var parentClientId = sourceClientId.substring(0, sourceClientId.lastIndexOf(jsf.separatorchar));

        	        if (namingContainerPrefix + targetClientId == parentClientId) {
        	            targetClientId = parentClientId;
        	        }
        	        else {
        	            targetClientId = parentClientId + jsf.separatorchar + targetClientId;
        	        }
				}

				targetClientIds[i] = targetClientId;
			}

        	return targetClientIds.join(' ');
        };

        /**
         * Check if a value exists in an array
         * @ignore
         */
        var isInArray = function isInArray(array, value) {
            for (var i = 0; i < array.length; i++) {
                if (array[i] === value) {
                    return true;
                }
            }
            return false;
        };


        /**
         * Evaluate JavaScript code in a global context.
         * @param src JavaScript code to evaluate
         * @ignore
         */
        var globalEval = function globalEval(src) {
            if (window.execScript) {
                window.execScript(src);
                return;
            }
            // We have to wrap the call in an anon function because of a firefox bug, where this is incorrectly set
            // We need to explicitly call window.eval because of a Chrome peculiarity
            /**
             * @ignore
             */
            var fn = function() {
                window.eval.call(window,src);
            };
            fn();
        };

        /**
         * Get all scripts from supplied string, return them as an array for later processing.
         * @param str
         * @returns {array} of script text
         * @ignore
         */
        var getScripts = function getScripts(str) {
            // Regex to find all scripts in a string
            var findscripts = /<script[^>]*>([\S\s]*?)<\/script>/igm;
            // Regex to find one script, to isolate it's content [2] and attributes [1]
            var findscript = /<script([^>]*)>([\S\s]*?)<\/script>/im;
            // Regex to find type attribute
            var findtype = /type="([\S]*?)"/im;
            var initialnodes = [];
            var scripts = [];
            initialnodes = str.match(findscripts);
            while (!!initialnodes && initialnodes.length > 0) {
                var scriptStr = [];
                scriptStr = initialnodes.shift().match(findscript);
                // check the type - skip if it not javascript type
                var type = [];
                type = scriptStr[1].match(findtype);
                if ( !!type && type[1]) {
                    if (type[1] !== "text/javascript") {
                        continue;
                    }
                }
                scripts.push(scriptStr);
            }
            return scripts;
        };

        var removeScripts = function removeScripts(str) {
            return str.replace(/<script[^>]*type="text\/javascript"[^>]*>([\S\s]*?)<\/script>/igm,"");
        };

        /**
         * Run an array of script nodes,
         * @param scripts Array of script nodes.
         * @ignore
         */
        var runScripts = function runScripts(scripts) {
            if (!scripts || scripts.length === 0) {
                return;
            }

            var loadedScripts = document.getElementsByTagName("script");
            var loadedScriptUrls = [];

            for (var i = 0; i < loadedScripts.length; i++) {
                var scriptNode = loadedScripts[i];
                var url = scriptNode.getAttribute("src");

                if (url) {
                    loadedScriptUrls.push(url);
                }
            }

            var head = document.head || document.getElementsByTagName('head')[0] || document.documentElement;
            runScript(head, loadedScriptUrls, scripts, 0);
        };

        /**
         * Run script at given index.
         * @param head Document's head.
         * @param loadedScriptUrls URLs of scripts which are already loaded.
         * @param scripts Array of script nodes.
         * @param index Index of script to be loaded.
         * @ignore
         */
        var runScript = function runScript(head, loadedScriptUrls, scripts, index) {
            if (index >= scripts.length) {
                return;
            }

            // Regex to find src attribute
            var findsrc = /src="([\S]*?)"/im;
            // Regex to remove leading cruft
            var stripStart = /^\s*(<!--)*\s*(\/\/)*\s*(\/\*)*\s*\n*\**\n*\s*\*.*\n*\s*\*\/(<!\[CDATA\[)*/;

            var scriptStr = scripts[index];
            var src = scriptStr[1].match(findsrc);
            var scriptLoadedViaUrl = false;

            if (!!src && src[1]) {
                // if this is a file, load it
                var url = unescapeHTML(src[1]);
                // if this is already loaded, don't load it
                // it's never necessary, and can make debugging difficult
                if (loadedScriptUrls.indexOf(url) < 0) {
                    // create script node
                    var scriptNode = document.createElement('script');
                    var parserElement = document.createElement('div');
                    parserElement.innerHTML = scriptStr[0];
                    cloneAttributes(scriptNode, parserElement.firstChild);
                    deleteNode(parserElement);
                    scriptNode.type = 'text/javascript';
                    scriptNode.src = url; // add the src to the script node
                    scriptNode.onload = scriptNode.onreadystatechange = function(_, abort) {
                        if (abort || !scriptNode.readyState || /loaded|complete/.test(scriptNode.readyState)) {
                            scriptNode.onload = scriptNode.onreadystatechange = null; // IE memory leak fix.
                            scriptNode = null;
                            runScript(head, loadedScriptUrls, scripts, index + 1); // Run next script.
                        }
                    };
                    head.insertBefore(scriptNode, null); // add it to end of the head (and don't remove it)
                    scriptLoadedViaUrl = true;
                }
            } else if (!!scriptStr && scriptStr[2]) {
                // else get content of tag, without leading CDATA and such
                var script = scriptStr[2].replace(stripStart,"");

                if (!!script) {
                    // create script node
                    var scriptNode = document.createElement('script');
                    scriptNode.type = 'text/javascript';
                    scriptNode.text = script; // add the code to the script node
                    head.appendChild(scriptNode); // add it to the head
                    head.removeChild(scriptNode); // then remove it
                }
            }

            if (!scriptLoadedViaUrl) {
                runScript(head, loadedScriptUrls, scripts, index + 1); // Run next script.
            }
        };

        /**
         * Get all stylesheets from supplied string and run them all.
         * @param str
         * @ignore
         */
        var runStylesheets = function runStylesheets(str) {
            // Regex to find all links in a string
            var findlinks = /<link[^>]*\/>/igm;
            // Regex to find one link, to isolate its attributes [1]
            var findlink = /<link([^>]*)\/>/im;
            // Regex to find type attribute
            var findtype = /type="([\S]*?)"/im;
            var findhref = /href="([\S]*?)"/im;

            var stylesheets = [];
            var loadedStylesheetUrls = null;
            var head = document.head || document.getElementsByTagName('head')[0] || document.documentElement;
            var parserElement = null;

            var initialnodes = str.match(findlinks);
            while (!!initialnodes && initialnodes.length > 0) {
                var linkStr = initialnodes.shift().match(findlink);
                // check the type - skip if it not css type
                var type = linkStr[1].match(findtype);
                if (!type || type[1] !== "text/css") {
                    continue;
                }
                var href = linkStr[1].match(findhref);
                if (!!href && href[1]) {
                    if (loadedStylesheetUrls === null) {
                        var loadedLinks = document.getElementsByTagName("link");
                        loadedStylesheetUrls = [];

                        for (var i = 0; i < loadedLinks.length; i++) {
                            var linkNode = loadedLinks[i];
                            
                            if (linkNode.getAttribute("type") === "text/css") {
                                var url = linkNode.getAttribute("href");

                                if (url) {
                                    loadedStylesheetUrls.push(url);
                                }
                            }
                        }
                    }

                    var url = unescapeHTML(href[1]);

                    if (loadedStylesheetUrls.indexOf(url) < 0) {
                        // create stylesheet node
                        parserElement = parserElement !== null ? parserElement : document.createElement('div');
                        parserElement.innerHTML = linkStr[0];
                        var linkNode = parserElement.firstChild;
                        linkNode.type = 'text/css';
                        linkNode.rel = 'stylesheet';
                        linkNode.href = url;
                        head.insertBefore(linkNode, null); // add it to end of the head (and don't remove it)
                    }
                }
            }

            deleteNode(parserElement);
        };

        /**
         * Replace DOM element with a new tagname and supplied innerHTML
         * @param element element to replace
         * @param tempTagName new tag name to replace with
         * @param src string new content for element
         * @ignore
         */
        var elementReplaceStr = function elementReplaceStr(element, tempTagName, src) {

            var temp = document.createElement(tempTagName);
            if (element.id) {
                temp.id = element.id;
            }

            // Creating a head element isn't allowed in IE, and faulty in most browsers,
            // so it is not allowed
            if (element.nodeName.toLowerCase() === "head") {
                throw new Error("Attempted to replace a head element - this is not allowed.");
            } else {
                var scripts = [];
                if (isAutoExec()) {
                    temp.innerHTML = src;
                } else {
                    // Get scripts from text
                    scripts = getScripts(src);
                    // Remove scripts from text
                    src = removeScripts(src);
                    temp.innerHTML = src;
                }
            }

            replaceNode(temp, element);            
            cloneAttributes(temp, element);
            runScripts(scripts);

        };

        /**
         * Get a string with the concatenated values of all string nodes under the given node
         * @param  oNode the given DOM node
         * @param  deep boolean - whether to recursively scan the children nodes of the given node for text as well. Default is <code>false</code>
         * @ignore
         * Note:  This code originally from Sarissa: http://dev.abiss.gr/sarissa
         * It has been modified to fit into the overall codebase
         */
        var getText = function getText(oNode, deep) {
            var Node = {ELEMENT_NODE: 1, ATTRIBUTE_NODE: 2, TEXT_NODE: 3, CDATA_SECTION_NODE: 4,
                ENTITY_REFERENCE_NODE: 5,  ENTITY_NODE: 6, PROCESSING_INSTRUCTION_NODE: 7,
                COMMENT_NODE: 8, DOCUMENT_NODE: 9, DOCUMENT_TYPE_NODE: 10,
                DOCUMENT_FRAGMENT_NODE: 11, NOTATION_NODE: 12};

            var s = "";
            var nodes = oNode.childNodes;
            for (var i = 0; i < nodes.length; i++) {
                var node = nodes[i];
                var nodeType = node.nodeType;
                if (nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE) {
                    s += node.data;
                } else if (deep === true && (nodeType == Node.ELEMENT_NODE ||
                                             nodeType == Node.DOCUMENT_NODE ||
                                             nodeType == Node.DOCUMENT_FRAGMENT_NODE)) {
                    s += getText(node, true);
                }
            }
            return s;
        };

        var PARSED_OK = "Document contains no parsing errors";
        var PARSED_EMPTY = "Document is empty";
        var PARSED_UNKNOWN_ERROR = "Not well-formed or other error";
        var getParseErrorText;
        if (isIE()) {
            /**
             * Note: This code orginally from Sarissa: http://dev.abiss.gr/sarissa
             * @ignore
             */
            getParseErrorText = function (oDoc) {
                var parseErrorText = PARSED_OK;
                if (oDoc && oDoc.parseError && oDoc.parseError.errorCode && oDoc.parseError.errorCode !== 0) {
                    parseErrorText = "XML Parsing Error: " + oDoc.parseError.reason +
                                     "\nLocation: " + oDoc.parseError.url +
                                     "\nLine Number " + oDoc.parseError.line + ", Column " +
                                     oDoc.parseError.linepos +
                                     ":\n" + oDoc.parseError.srcText +
                                     "\n";
                    for (var i = 0; i < oDoc.parseError.linepos; i++) {
                        parseErrorText += "-";
                    }
                    parseErrorText += "^\n";
                }
                else if (oDoc.documentElement === null) {
                    parseErrorText = PARSED_EMPTY;
                }
                return parseErrorText;
            };
        } else { // (non-IE)

            /**
             * <p>Returns a human readable description of the parsing error. Useful
             * for debugging. Tip: append the returned error string in a &lt;pre&gt;
             * element if you want to render it.</p>
             * @param  oDoc The target DOM document
             * @returns {String} The parsing error description of the target Document in
             *          human readable form (preformated text)
             * @ignore
             * Note:  This code orginally from Sarissa: http://dev.abiss.gr/sarissa
             */
            getParseErrorText = function (oDoc) {
                var parseErrorText = PARSED_OK;
                if ((!oDoc) || (!oDoc.documentElement)) {
                    parseErrorText = PARSED_EMPTY;
                } else if (oDoc.documentElement.tagName == "parsererror") {
                    parseErrorText = oDoc.documentElement.firstChild.data;
                    parseErrorText += "\n" + oDoc.documentElement.firstChild.nextSibling.firstChild.data;
                } else if (oDoc.getElementsByTagName("parsererror").length > 0) {
                    var parsererror = oDoc.getElementsByTagName("parsererror")[0];
                    parseErrorText = getText(parsererror, true) + "\n";
                } else if (oDoc.parseError && oDoc.parseError.errorCode !== 0) {
                    parseErrorText = PARSED_UNKNOWN_ERROR;
                }
                return parseErrorText;
            };
        }

        if ((typeof(document.importNode) == "undefined") && isIE()) {
            try {
                /**
                 * Implementation of importNode for the context window document in IE.
                 * If <code>oNode</code> is a TextNode, <code>bChildren</code> is ignored.
                 * @param oNode the Node to import
                 * @param bChildren whether to include the children of oNode
                 * @returns the imported node for further use
                 * @ignore
                 * Note:  This code orginally from Sarissa: http://dev.abiss.gr/sarissa
                 */
                document.importNode = function(oNode, bChildren) {
                    var tmp;
                    if (oNode.nodeName == '#text') {
                        return document.createTextNode(oNode.data);
                    }
                    else {
                        if (oNode.nodeName == "tbody" || oNode.nodeName == "tr") {
                            tmp = document.createElement("table");
                        }
                        else if (oNode.nodeName == "td") {
                            tmp = document.createElement("tr");
                        }
                        else if (oNode.nodeName == "option") {
                            tmp = document.createElement("select");
                        }
                        else {
                            tmp = document.createElement("div");
                        }
                        if (bChildren) {
                            tmp.innerHTML = oNode.xml ? oNode.xml : oNode.outerHTML;
                        } else {
                            tmp.innerHTML = oNode.xml ? oNode.cloneNode(false).xml : oNode.cloneNode(false).outerHTML;
                        }
                        return tmp.getElementsByTagName("*")[0];
                    }
                };
            } catch(e) {
            }
        }
        // Setup Node type constants for those browsers that don't have them (IE)
        var Node = {ELEMENT_NODE: 1, ATTRIBUTE_NODE: 2, TEXT_NODE: 3, CDATA_SECTION_NODE: 4,
            ENTITY_REFERENCE_NODE: 5,  ENTITY_NODE: 6, PROCESSING_INSTRUCTION_NODE: 7,
            COMMENT_NODE: 8, DOCUMENT_NODE: 9, DOCUMENT_TYPE_NODE: 10,
            DOCUMENT_FRAGMENT_NODE: 11, NOTATION_NODE: 12};

        // PENDING - add support for removing handlers added via DOM 2 methods
        /**
         * Delete all events attached to a node
         * @param node
         * @ignore
         */
        var clearEvents = function clearEvents(node) {
            if (!node) {
                return;
            }

            // don't do anything for text and comment nodes - unnecessary
            if (node.nodeType == Node.TEXT_NODE || node.nodeType == Node.COMMENT_NODE) {
                return;
            }

            var events = ['abort', 'blur', 'change', 'error', 'focus', 'load', 'reset', 'resize', 'scroll', 'select', 'submit', 'unload',
            'keydown', 'keypress', 'keyup', 'click', 'mousedown', 'mousemove', 'mouseout', 'mouseover', 'mouseup', 'dblclick' ];
            try {
                for (var e in events) {
                    if (events.hasOwnProperty(e)) {
                        node[e] = null;
                    }
                }
            } catch (ex) {
                // it's OK if it fails, at least we tried
            }
        };

        /**
         * Determine if this current browser is IE9 or greater
         * @param node
         * @ignore
         */
        var isIE9Plus = function isIE9Plus() {
            var iev = getIEVersion();
            if (iev >= 9) {
                return true;
            } else {
                return false;
            }
        }


        /**
         * Deletes node
         * @param node
         * @ignore
         */
        var deleteNode = function deleteNode(node) {
            if (!node) {
                return;
            }
            if (!node.parentNode) {
                // if there's no parent, there's nothing to do
                return;
            }
            if (!isIE() || (isIE() && isIE9Plus())) {
                // nothing special required
                node.parentNode.removeChild(node);
                return;
            }
            // The rest of this code is specialcasing for IE
            if (node.nodeName.toLowerCase() === "body") {
                // special case for removing body under IE.
                deleteChildren(node);
                try {
                    node.outerHTML = '';
                } catch (ex) {
                    // fails under some circumstances, but not in RI
                    // supplied responses.  If we've gotten here, it's
                    // fairly safe to leave a lingering body tag rather than
                    // fail outright
                }
                return;
            }
            var temp = node.ownerDocument.createElement('div');
            var parent = node.parentNode;
            temp.appendChild(parent.removeChild(node));
            // Now clean up the temporary element
            try {
                temp.outerHTML = ''; //prevent leak in IE
            } catch (ex) {
                // at least we tried.  Fails in some circumstances,
                // but not in RI supplied responses.  Better to leave a lingering
                // temporary div than to fail outright.
            }
        };

        /**
         * Deletes all children of a node
         * @param node
         * @ignore
         */
        var deleteChildren = function deleteChildren(node) {
            if (!node) {
                return;
            }
            for (var x = node.childNodes.length - 1; x >= 0; x--) { //delete all of node's children
                var childNode = node.childNodes[x];
                deleteNode(childNode);
            }
        };

        /**
         * <p> Copies the childNodes of nodeFrom to nodeTo</p>
         *
         * @param  nodeFrom the Node to copy the childNodes from
         * @param  nodeTo the Node to copy the childNodes to
         * @ignore
         * Note:  This code originally from Sarissa:  http://dev.abiss.gr/sarissa
         * It has been modified to fit into the overall codebase
         */
        var copyChildNodes = function copyChildNodes(nodeFrom, nodeTo) {

            if ((!nodeFrom) || (!nodeTo)) {
                throw "Both source and destination nodes must be provided";
            }

            deleteChildren(nodeTo);
            var nodes = nodeFrom.childNodes;
            // if within the same doc, just move, else copy and delete
            if (nodeFrom.ownerDocument == nodeTo.ownerDocument) {
                while (nodeFrom.firstChild) {
                    nodeTo.appendChild(nodeFrom.firstChild);
                }
            } else {
                var ownerDoc = nodeTo.nodeType == Node.DOCUMENT_NODE ? nodeTo : nodeTo.ownerDocument;
                var i;
                if (typeof(ownerDoc.importNode) != "undefined") {
                    for (i = 0; i < nodes.length; i++) {
                        nodeTo.appendChild(ownerDoc.importNode(nodes[i], true));
                    }
                } else {
                    for (i = 0; i < nodes.length; i++) {
                        nodeTo.appendChild(nodes[i].cloneNode(true));
                    }
                }
            }
        };


        /**
         * Replace one node with another.  Necessary for handling IE memory leak.
         * @param node
         * @param newNode
         * @ignore
         */
        var replaceNode = function replaceNode(newNode, node) {
               if(isIE()){
                    node.parentNode.insertBefore(newNode, node);
                    deleteNode(node);
               } else {
                    node.parentNode.replaceChild(newNode, node);
               }
        };

        /**
         * @ignore
         */
        var propertyToAttribute = function propertyToAttribute(name) {
            if (name === 'className') {
                return 'class';
            } else if (name === 'xmllang') {
                return 'xml:lang';
            } else {
                return name.toLowerCase();
            }
        };

        /**
         * @ignore
         */
        var isFunctionNative = function isFunctionNative(func) {
            return /^\s*function[^{]+{\s*\[native code\]\s*}\s*$/.test(String(func));
        };

        /**
         * @ignore
         */
        var detectAttributes = function detectAttributes(element) {
            //test if 'hasAttribute' method is present and its native code is intact
            //for example, Prototype can add its own implementation if missing
            if (element.hasAttribute && isFunctionNative(element.hasAttribute)) {
                return function(name) {
                    return element.hasAttribute(name);
                }
            } else {
                try {
                    //when accessing .getAttribute method without arguments does not throw an error then the method is not available
                    element.getAttribute;

                    var html = element.outerHTML;
                    var startTag = html.match(/^<[^>]*>/)[0];
                    return function(name) {
                        return startTag.indexOf(name + '=') > -1;
                    }
                } catch (ex) {
                    return function(name) {
                        return element.getAttribute(name);
                    }
                }
            }
        };

        /**
         * copy all attributes from one element to another - except id
         * @param target element to copy attributes to
         * @param source element to copy attributes from
         * @ignore
         */
        var cloneAttributes = function cloneAttributes(target, source) {

            // enumerate core element attributes - without 'dir' as special case
            var coreElementProperties = ['className', 'title', 'lang', 'xmllang'];
            // enumerate additional input element attributes
            var inputElementProperties = [
                'name', 'value', 'size', 'maxLength', 'src', 'alt', 'useMap', 'tabIndex', 'accessKey', 'accept', 'type'
            ];
            // enumerate additional boolean input attributes
            var inputElementBooleanProperties = [
                'checked', 'disabled', 'readOnly'
            ];

            // Enumerate all the names of the event listeners
            var listenerNames =
                [ 'onclick', 'ondblclick', 'onmousedown', 'onmousemove', 'onmouseout',
                    'onmouseover', 'onmouseup', 'onkeydown', 'onkeypress', 'onkeyup',
                    'onhelp', 'onblur', 'onfocus', 'onchange', 'onload', 'onunload', 'onabort',
                    'onreset', 'onselect', 'onsubmit'
                ];

            var sourceAttributeDetector = detectAttributes(source);
            var targetAttributeDetector = detectAttributes(target);

            var isInputElement = target.nodeName.toLowerCase() === 'input';
            var propertyNames = isInputElement ? coreElementProperties.concat(inputElementProperties) : coreElementProperties;
            var isXML = !source.ownerDocument.contentType || source.ownerDocument.contentType == 'text/xml';
            for (var iIndex = 0, iLength = propertyNames.length; iIndex < iLength; iIndex++) {
                var propertyName = propertyNames[iIndex];
                var attributeName = propertyToAttribute(propertyName);
                if (sourceAttributeDetector(attributeName)) {
                
                    //With IE 7 (quirks or standard mode) and IE 8/9 (quirks mode only), 
                    //you cannot get the attribute using 'class'. You must use 'className'
                    //which is the same value you use to get the indexed property. The only 
                    //reliable way to detect this (without trying to evaluate the browser
                    //mode and version) is to compare the two return values using 'className' 
                    //to see if they exactly the same.  If they are, then use the property
                    //name when using getAttribute.
                    if( attributeName == 'class'){
                        if( isIE() && (source.getAttribute(propertyName) === source[propertyName]) ){
                            attributeName = propertyName;
                        }
                    }

                    var newValue = isXML ? source.getAttribute(attributeName) : source[propertyName];
                    var oldValue = target[propertyName];
                    if (oldValue != newValue) {
                        target[propertyName] = newValue;
                    }
                } else {
                    //setting property to '' seems to be the only cross-browser method for removing an attribute
                    //avoid setting 'value' property to '' for checkbox and radio input elements because then the
                    //'value' is used instead of the 'checked' property when the form is serialized by the browser
                    if (attributeName == "value" && (target.type != 'checkbox' && target.type != 'radio')) {
                         target[propertyName] = '';
                    }
                    target.removeAttribute(attributeName);
                }
            }

            var booleanPropertyNames = isInputElement ? inputElementBooleanProperties : [];
            for (var jIndex = 0, jLength = booleanPropertyNames.length; jIndex < jLength; jIndex++) {
                var booleanPropertyName = booleanPropertyNames[jIndex];
                var newBooleanValue = source[booleanPropertyName];
                var oldBooleanValue = target[booleanPropertyName];
                if (oldBooleanValue != newBooleanValue) {
                    target[booleanPropertyName] = newBooleanValue;
                }
            }

            //'style' attribute special case
            if (sourceAttributeDetector('style')) {
                var newStyle;
                var oldStyle;
                if (isIE()) {
                    newStyle = source.style.cssText;
                    oldStyle = target.style.cssText;
                    if (newStyle != oldStyle) {
                        target.style.cssText = newStyle;
                    }
                } else {
                    newStyle = source.getAttribute('style');
                    oldStyle = target.getAttribute('style');
                    if (newStyle != oldStyle) {
                        target.setAttribute('style', newStyle);
                    }
                }
            } else if (targetAttributeDetector('style')){
                target.removeAttribute('style');
            }

            // Special case for 'dir' attribute
            if (!isIE() && source.dir != target.dir) {
                if (sourceAttributeDetector('dir')) {
                    target.dir = source.dir;
                } else if (targetAttributeDetector('dir')) {
                    target.dir = '';
                }
            }

            for (var lIndex = 0, lLength = listenerNames.length; lIndex < lLength; lIndex++) {
                var name = listenerNames[lIndex];
                target[name] = source[name] ? source[name] : null;
                if (source[name]) {
                    source[name] = null;
                }
            }

            //clone HTML5 data-* attributes
            try{
                var targetDataset = target.dataset;
                var sourceDataset = source.dataset;
                if (targetDataset || sourceDataset) {
                    //cleanup the dataset
                    for (var tp in targetDataset) {
                        delete targetDataset[tp];
                    }
                    //copy dataset's properties
                    for (var sp in sourceDataset) {
                        targetDataset[sp] = sourceDataset[sp];
                    }
                }
            } catch (ex) {
                //most probably dataset properties are not supported
            }
        };

        /**
         * Replace an element from one document into another
         * @param newElement new element to put in document
         * @param origElement original element to replace
         * @ignore
         */
        var elementReplace = function elementReplace(newElement, origElement) {
            copyChildNodes(newElement, origElement);
            // sadly, we have to reparse all over again
            // to reregister the event handlers and styles
            // PENDING do some performance tests on large pages
            origElement.innerHTML = origElement.innerHTML;

            try {
                cloneAttributes(origElement, newElement);
            } catch (ex) {
                // if in dev mode, report an error, else try to limp onward
                if (jsf.getProjectStage() == "Development") {
                    throw new Error("Error updating attributes");
                }
            }
            deleteNode(newElement);

        };

        /**
         * Create a new document, then select the body element within it
         * @param docStr Stringified version of document to create
         * @return element the body element
         * @ignore
         */
        var getBodyElement = function getBodyElement(docStr) {

            var doc;  // intermediate document we'll create
            var body; // Body element to return

            if (typeof DOMParser !== "undefined") {  // FF, S, Chrome
                doc = (new DOMParser()).parseFromString(docStr, "text/xml");
            } else if (typeof ActiveXObject !== "undefined") { // IE
                doc = new ActiveXObject("MSXML2.DOMDocument");
                doc.loadXML(docStr);
            } else {
                throw new Error("You don't seem to be running a supported browser");
            }

            if (getParseErrorText(doc) !== PARSED_OK) {
                throw new Error(getParseErrorText(doc));
            }

            body = doc.getElementsByTagName("body")[0];

            if (!body) {
                throw new Error("Can't find body tag in returned document.");
            }

            return body;
        };

        /**
         * Find encoded url field for a given form.
         * @param form
         * @ignore
         */
        var getEncodedUrlElement = function getEncodedUrlElement(form) {
            var encodedUrlElement = form['javax.faces.encodedURL'];

            if (encodedUrlElement) {
                return encodedUrlElement;
            } else {
                var formElements = form.elements;
                for (var i = 0, length = formElements.length; i < length; i++) {
                    var formElement = formElements[i];
                    if (formElement.name && (formElement.name.indexOf('javax.faces.encodedURL') >= 0)) {
                        return formElement;
                    }
                }
            }

            return undefined;
        };

        /**
         * Update hidden state fields from the server into the DOM for any JSF forms which need to be updated.
         * This covers at least the form that submitted the request and any form that is covered in the render target list.
         * 
         * @param updateElement The update element of partial response holding the state value.
         * @param context An object containing the request context, including the following properties:
         * the source element, per call onerror callback function, per call onevent callback function, the render
         * instructions, the submitting form ID, the naming container ID and naming container prefix.
         * @param hiddenStateFieldName The hidden state field name, e.g. javax.faces.ViewState or javax.faces.ClientWindow 
         */
        var updateHiddenStateFields = function updateHiddenStateFields(updateElement, context, hiddenStateFieldName) {
            var firstChild = updateElement.firstChild;
            var state = (typeof firstChild.wholeText !== 'undefined') ? firstChild.wholeText : firstChild.nodeValue;
            var formsToUpdate = getFormsToUpdate(context);

            for (var i = 0; i < formsToUpdate.length; i++) {
                var formToUpdate = formsToUpdate[i];
                var field = getHiddenStateField(formToUpdate, hiddenStateFieldName, context.namingContainerPrefix);
                if (typeof field == "undefined") {
                    field = document.createElement("input");
                    field.type = "hidden";
                    field.name = context.namingContainerPrefix + hiddenStateFieldName;
                    formToUpdate.appendChild(field);
                }
                field.value = state;
            }
        }

        /**
         * Find hidden state field for a given form.
         * @param form The form to find hidden state field in.
         * @param hiddenStateFieldName The hidden state field name, e.g. javax.faces.ViewState or javax.faces.ClientWindow 
         * @param namingContainerPrefix The naming container prefix, if any (the view root ID suffixed with separator character).
         * @ignore
         */
        var getHiddenStateField = function getHiddenStateField(form, hiddenStateFieldName, namingContainerPrefix) {
            namingContainerPrefix = namingContainerPrefix || "";
            var field = form[namingContainerPrefix + hiddenStateFieldName];

            if (field) {
                return field;
            }
            else {
                var formElements = form.elements;

                for (var i = 0, length = formElements.length; i < length; i++) {
                    var formElement = formElements[i];

                    if (formElement.name && (formElement.name.indexOf(hiddenStateFieldName) >= 0)) {
                        return formElement;
                    }
                }
            }

            return undefined;
        };

        /**
         * Do update.
         * @param updateElement The update element of partial response.
         * @param context An object containing the request context, including the following properties:
         * the source element, per call onerror callback function, per call onevent callback function, the render
         * instructions, the submitting form ID, the naming container ID and naming container prefix.
         * @ignore
         */
        var doUpdate = function doUpdate(updateElement, context) {
            var id, content, markup;
            var scripts = []; // temp holding value for array of script nodes

            id = updateElement.getAttribute('id');
            var viewStateRegex = new RegExp(context.namingContainerPrefix + "javax.faces.ViewState" + jsf.separatorchar + ".+$");
            var windowIdRegex = new RegExp(context.namingContainerPrefix + "javax.faces.ClientWindow" + jsf.separatorchar + ".+$");

            if (id.match(viewStateRegex)) {
                updateHiddenStateFields(updateElement, context, "javax.faces.ViewState");
                return;
            } else if (id.match(windowIdRegex)) {
                updateHiddenStateFields(updateElement, context, "javax.faces.ClientWindow");
                return;
            }

            // join the CDATA sections in the markup
            markup = '';
            for (var j = 0; j < updateElement.childNodes.length; j++) {
                content = updateElement.childNodes[j];
                markup += content.nodeValue;
            }

            var src = markup;

            // If our special render all markup is present..
            if (id === "javax.faces.ViewRoot" || id === "javax.faces.ViewBody") {

                // spec790: If UIViewRoot is currently being updated,
                // then it means that ajax navigation has taken place.
                // So, ensure that context.render has correct value for this condition,
                // because this is not necessarily correclty specified during the request.
                context.render = "@all";

                var bodyStartEx = new RegExp("< *body[^>]*>", "gi");
                var bodyEndEx = new RegExp("< */ *body[^>]*>", "gi");
                var newsrc;

                var docBody = document.getElementsByTagName("body")[0];
                var bodyStart = bodyStartEx.exec(src);

                if (bodyStart !== null) { // replace body tag
                    // First, try with XML manipulation
                    try {
                        runStylesheets(src);
                        // Get scripts from text
                        scripts = getScripts(src);
                        // Remove scripts from text
                        newsrc = removeScripts(src);
                        elementReplace(getBodyElement(newsrc), docBody);
                        runScripts(scripts);
                    } catch (e) {
                        // OK, replacing the body didn't work with XML - fall back to quirks mode insert
                        var srcBody, bodyEnd;
                        // if src contains </body>
                        bodyEnd = bodyEndEx.exec(src);
                        if (bodyEnd !== null) {
                            srcBody = src.substring(bodyStartEx.lastIndex, bodyEnd.index);
                        } else { // can't find the </body> tag, punt
                            srcBody = src.substring(bodyStartEx.lastIndex);
                        }
                        // replace body contents with innerHTML - note, script handling happens within function
                        elementReplaceStr(docBody, "body", srcBody);

                    }

                } else {  // replace body contents with innerHTML - note, script handling happens within function
                    elementReplaceStr(docBody, "body", src);
                }
            } else if (id === "javax.faces.ViewHead") {
                throw new Error("javax.faces.ViewHead not supported - browsers cannot reliably replace the head's contents");
            } else if (id === "javax.faces.Resource") {
                runStylesheets(src);
                scripts = getScripts(src);
                runScripts(scripts);
            } else {
                var element = $(id);
                if (!element) {
                    throw new Error("During update: " + id + " not found");
                }

                if (context.namingContainerId && id == context.namingContainerId) {
                    // spec790: If UIViewRoot is a NamingContainer and this is currently being updated,
                    // then it means that ajax navigation has taken place.
                    // So, ensure that context.render has correct value for this condition,
                    // because this is not necessarily correclty specified during the request.
                    context.render = context.namingContainerId;
                }

                var parent = element.parentNode;
                // Trim space padding before assigning to innerHTML
                var html = src.replace(/^\s+/g, '').replace(/\s+$/g, '');
                var parserElement = document.createElement('div');
                var tag = element.nodeName.toLowerCase();
                var tableElements = ['td', 'th', 'tr', 'tbody', 'thead', 'tfoot'];
                var isInTable = false;
                for (var tei = 0, tel = tableElements.length; tei < tel; tei++) {
                    if (tableElements[tei] == tag) {
                        isInTable = true;
                        break;
                    }
                }
                if (isInTable) {

                    if (isAutoExec()) {
                        // Create html
                        parserElement.innerHTML = '<table>' + html + '</table>';
                    } else {
                        // Get the scripts from the text
                        scripts = getScripts(html);
                        // Remove scripts from text
                        html = removeScripts(html);
                        parserElement.innerHTML = '<table>' + html + '</table>';
                    }
                    var newElement = parserElement.firstChild;
                    //some browsers will also create intermediary elements such as table>tbody>tr>td
                    while ((null !== newElement) && (id !== newElement.id)) {
                        newElement = newElement.firstChild;
                    }
                    parent.replaceChild(newElement, element);
                    runScripts(scripts);
                } else if (element.nodeName.toLowerCase() === 'input') {
                    // special case handling for 'input' elements
                    // in order to not lose focus when updating,
                    // input elements need to be added in place.
                    parserElement = document.createElement('div');
                    parserElement.innerHTML = html;
                    newElement = parserElement.firstChild;

                    cloneAttributes(element, newElement);
                    deleteNode(parserElement);
                } else if (html.length > 0) {
                    if (isAutoExec()) {
                        // Create html
                        parserElement.innerHTML = html;
                    } else {
                        // Get the scripts from the text
                        scripts = getScripts(html);
                        // Remove scripts from text
                        html = removeScripts(html);
                        parserElement.innerHTML = html;
                    }
                    replaceNode(parserElement.firstChild, element);
                    deleteNode(parserElement);
                    runScripts(scripts);
                }
            }
        };

        /**
         * Delete a node specified by the element.
         * @param element
         * @ignore
         */
        var doDelete = function doDelete(element) {
            var id = element.getAttribute('id');
            var target = $(id);
            deleteNode(target);
        };

        /**
         * Insert a node specified by the element.
         * @param element
         * @ignore
         */
        var doInsert = function doInsert(element) {
            var tablePattern = new RegExp("<\\s*(td|th|tr|tbody|thead|tfoot)", "i");
            var scripts = [];
            var target = $(element.firstChild.getAttribute('id'));
            var parent = target.parentNode;
            var html = element.firstChild.firstChild.nodeValue;
            var isInTable = tablePattern.test(html);

            if (!isAutoExec())  {
                // Get the scripts from the text
                scripts = getScripts(html);
                // Remove scripts from text
                html = removeScripts(html);
            }
            var tempElement = document.createElement('div');
            var newElement = null;
            if (isInTable)  {
                tempElement.innerHTML = '<table>' + html + '</table>';
                newElement = tempElement.firstChild;
                //some browsers will also create intermediary elements such as table>tbody>tr>td
                //test for presence of id on the new element since we do not have it directly
                while ((null !== newElement) && ("" == newElement.id)) {
                    newElement = newElement.firstChild;
                }
            } else {
                tempElement.innerHTML = html;
                newElement = tempElement.firstChild;
            }

            if (element.firstChild.nodeName === 'after') {
                // Get the next in the list, to insert before
                target = target.nextSibling;
            }  // otherwise, this is a 'before' element
            if (!!tempElement.innerHTML) { // check if only scripts were inserted - if so, do nothing here
                parent.insertBefore(newElement, target);
            }
            runScripts(scripts);
            deleteNode(tempElement);
        };

        /**
         * Modify attributes of given element id.
         * @param element
         * @ignore
         */
        var doAttributes = function doAttributes(element) {

            // Get id of element we'll act against
            var id = element.getAttribute('id');

            var target = $(id);

            if (!target) {
                throw new Error("The specified id: " + id + " was not found in the page.");
            }

            // There can be multiple attributes modified.  Loop through the list.
            var nodes = element.childNodes;
            for (var i = 0; i < nodes.length; i++) {
                var name = nodes[i].getAttribute('name');
                var value = nodes[i].getAttribute('value');

                //boolean attribute handling code for all browsers
                if (name === 'disabled') {
                    target.disabled = value === 'disabled' || value === 'true';
                    return;
                } else if (name === 'checked') {
                    target.checked = value === 'checked' || value === 'on' || value === 'true';
                    return;
                } else if (name == 'readonly') {
                    target.readOnly = value === 'readonly' || value === 'true';
                    return;
                }

                if (!isIE()) {
                    if (name === 'value') {
                        target.value = value;
                    } else {
                        target.setAttribute(name, value);
                    }
                } else { // if it's IE, then quite a bit more work is required
                    if (name === 'class') {
                        target.className = value;
                    } else if (name === "for") {
                        name = 'htmlFor';
                        target.setAttribute(name, value, 0);
                    } else if (name === 'style') {
                        target.style.setAttribute('cssText', value, 0);
                    } else if (name.substring(0, 2) === 'on') {
                        var c = document.body.appendChild(document.createElement('span'));
                        try {
                            c.innerHTML = '<span ' + name + '="' + value + '"/>';
                            target[name] = c.firstChild[name];
                        } finally {
                            document.body.removeChild(c);
                        }
                    } else if (name === 'dir') {
                        if (jsf.getProjectStage() == 'Development') {
                            throw new Error("Cannot set 'dir' attribute in IE");
                        }
                    } else {
                        target.setAttribute(name, value, 0);
                    }
                }
            }
        };

        /**
         * Eval the CDATA of the element.
         * @param element to eval
         * @ignore
         */
        var doEval = function doEval(element) {
            var evalText = '';
            var childNodes = element.childNodes;
            for (var i = 0; i < childNodes.length; i++) {
                evalText += childNodes[i].nodeValue;
            }
            globalEval(evalText);
        };

        /**
         * Ajax Request Queue
         * @ignore
         */
        var Queue = new function Queue() {

            // Create the internal queue
            var queue = [];


            // the amount of space at the front of the queue, initialised to zero
            var queueSpace = 0;

            /** Returns the size of this Queue. The size of a Queue is equal to the number
             * of elements that have been enqueued minus the number of elements that have
             * been dequeued.
             * @ignore
             */
            this.getSize = function getSize() {
                return queue.length - queueSpace;
            };

            /** Returns true if this Queue is empty, and false otherwise. A Queue is empty
             * if the number of elements that have been enqueued equals the number of
             * elements that have been dequeued.
             * @ignore
             */
            this.isEmpty = function isEmpty() {
                return (queue.length === 0);
            };

            /** Enqueues the specified element in this Queue.
             *
             * @param element - the element to enqueue
             * @ignore
             */
            this.enqueue = function enqueue(element) {
                // Queue the request
                queue.push(element);
            };


            /** Dequeues an element from this Queue. The oldest element in this Queue is
             * removed and returned. If this Queue is empty then undefined is returned.
             *
             * @returns Object The element that was removed from the queue.
             * @ignore
             */
            this.dequeue = function dequeue() {
                // initialise the element to return to be undefined
                var element = undefined;

                // check whether the queue is empty
                if (queue.length) {
                    // fetch the oldest element in the queue
                    element = queue[queueSpace];

                    // update the amount of space and check whether a shift should occur
                    if (++queueSpace * 2 >= queue.length) {
                        // set the queue equal to the non-empty portion of the queue
                        queue = queue.slice(queueSpace);
                        // reset the amount of space at the front of the queue
                        queueSpace = 0;
                    }
                }
                // return the removed element
                try {
                    return element;
                } finally {
                    element = null; // IE 6 leak prevention
                }
            };

            /** Returns the oldest element in this Queue. If this Queue is empty then
             * undefined is returned. This function returns the same value as the dequeue
             * function, but does not remove the returned element from this Queue.
             * @ignore
             */
            this.getOldestElement = function getOldestElement() {
                // initialise the element to return to be undefined
                var element = undefined;

                // if the queue is not element then fetch the oldest element in the queue
                if (queue.length) {
                    element = queue[queueSpace];
                }
                // return the oldest element
                try {
                    return element;
                } finally {
                    element = null; //IE 6 leak prevention
                }
            };
        }();


        /**
         * AjaxEngine handles Ajax implementation details.
         * @ignore
         */
        var AjaxEngine = function AjaxEngine(context) {

            var req = {};                  // Request Object
            req.url = null;                // Request URL
            req.context = context;              // Context of request and response
            req.context.sourceid = null;   // Source of this request
            req.context.onerror = null;    // Error handler for request
            req.context.onevent = null;    // Event handler for request
            req.context.namingContainerId = null;   // If UIViewRoot is an instance of NamingContainer this represents its ID.
            req.context.namingContainerPrefix = null;   // If UIViewRoot is an instance of NamingContainer this represents its ID suffixed with separator character, else an empty string.
            req.xmlReq = null;             // XMLHttpRequest Object
            req.async = true;              // Default - Asynchronous
            req.parameters = {};           // Parameters For GET or POST
            req.queryString = null;        // Encoded Data For GET or POST
            req.method = null;             // GET or POST
            req.status = null;             // Response Status Code From Server
            req.fromQueue = false;         // Indicates if the request was taken off the queue before being sent. This prevents the request from entering the queue redundantly.

            req.que = Queue;
            
            // Get a transport Handle
            // The transport will be an iframe transport if the form
            // has multipart encoding type.  This is where we could
            // handle XMLHttpRequest Level2 as well (perhaps 
            // something like:  if ('upload' in req.xmlReq)'
            req.xmlReq = getTransport(context);

            if (req.xmlReq === null) {
                return null;
            }

            /**
             * @ignore
             */
            function noop() {}
            
            // Set up request/response state callbacks
            /**
             * @ignore
             */
            req.xmlReq.onreadystatechange = function() {
                if (req.xmlReq.readyState === 4) {
                    req.onComplete();
                    // next two lines prevent closure/ciruclar reference leaks
                    // of XHR instances in IE
                    req.xmlReq.onreadystatechange = noop;
                    req.xmlReq = null;
                }
            };

            /**
             * This function is called when the request/response interaction
             * is complete.  If the return status code is successfull,
             * dequeue all requests from the queue that have completed.  If a
             * request has been found on the queue that has not been sent,
             * send the request.
             * @ignore
             */
            req.onComplete = function onComplete() {
                if (req.xmlReq.status && (req.xmlReq.status >= 200 && req.xmlReq.status < 300)) {
                    sendEvent(req.xmlReq, req.context, "complete");
                    jsf.ajax.response(req.xmlReq, req.context);
                } else {
                    sendEvent(req.xmlReq, req.context, "complete");
                    sendError(req.xmlReq, req.context, "httpError");
                }

                // Regardless of whether the request completed successfully (or not),
                // dequeue requests that have been completed (readyState 4) and send
                // requests that ready to be sent (readyState 0).

                var nextReq = req.que.getOldestElement();
                if (nextReq === null || typeof nextReq === 'undefined') {
                    return;
                }
                while ((typeof nextReq.xmlReq !== 'undefined' && nextReq.xmlReq !== null) &&
                       nextReq.xmlReq.readyState === 4) {
                    req.que.dequeue();
                    nextReq = req.que.getOldestElement();
                    if (nextReq === null || typeof nextReq === 'undefined') {
                        break;
                    }
                }
                if (nextReq === null || typeof nextReq === 'undefined') {
                    return;
                }
                if ((typeof nextReq.xmlReq !== 'undefined' && nextReq.xmlReq !== null) &&
                    nextReq.xmlReq.readyState === 0) {
                    nextReq.fromQueue = true;
                    nextReq.sendRequest();
                }
            };

            /**
             * Utility method that accepts additional arguments for the AjaxEngine.
             * If an argument is passed in that matches an AjaxEngine property, the
             * argument value becomes the value of the AjaxEngine property.
             * Arguments that don't match AjaxEngine properties are added as
             * request parameters.
             * @ignore
             */
            req.setupArguments = function(args) {
                for (var i in args) {
                    if (args.hasOwnProperty(i)) {
                        if (typeof req[i] === 'undefined') {
                            req.parameters[i] = args[i];
                        } else {
                            req[i] = args[i];
                        }
                    }
                }
            };

            /**
             * This function does final encoding of parameters, determines the request method
             * (GET or POST) and sends the request using the specified url.
             * @ignore
             */
            req.sendRequest = function() {
                if (req.xmlReq !== null) {
                    // if there is already a request on the queue waiting to be processed..
                    // just queue this request
                    if (!req.que.isEmpty()) {
                        if (!req.fromQueue) {
                            req.que.enqueue(req);
                            return;
                        }
                    }
                    // If the queue is empty, queue up this request and send
                    if (!req.fromQueue) {
                        req.que.enqueue(req);
                    }
                    // Some logic to get the real request URL
                    if (req.generateUniqueUrl && req.method == "GET") {
                        req.parameters["AjaxRequestUniqueId"] = new Date().getTime() + "" + req.requestIndex;
                    }
                    var content = null; // For POST requests, to hold query string
                    for (var i in req.parameters) {
                        if (req.parameters.hasOwnProperty(i)) {
                            if (req.queryString.length > 0) {
                                req.queryString += "&";
                            }
                            req.queryString += encodeURIComponent(i) + "=" + encodeURIComponent(req.parameters[i]);
                        }
                    }
                    if (req.method === "GET") {
                        if (req.queryString.length > 0) {
                            req.url += ((req.url.indexOf("?") > -1) ? "&" : "?") + req.queryString;
                        }
                    }
                    req.xmlReq.open(req.method, req.url, req.async);
                    // note that we are including the charset=UTF-8 as part of the content type (even
                    // if encodeURIComponent encodes as UTF-8), because with some
                    // browsers it will not be set in the request.  Some server implementations need to 
                    // determine the character encoding from the request header content type.
                    if (req.method === "POST") {
                        if (typeof req.xmlReq.setRequestHeader !== 'undefined') {
                            req.xmlReq.setRequestHeader('Faces-Request', 'partial/ajax');
                            req.xmlReq.setRequestHeader('Content-type', 'application/x-www-form-urlencoded;charset=UTF-8');
                        }
                        content = req.queryString;
                    }
                    // note that async == false is not a supported feature.  We may change it in ways
                    // that break existing programs at any time, with no warning.
                    if(!req.async) {
                        req.xmlReq.onreadystatechange = null; // no need for readystate change listening
                    }
                    sendEvent(req.xmlReq, req.context, "begin");
                    req.xmlReq.send(content);
                    if(!req.async){
                        req.onComplete();
                }
                }
            };

            return req;
        };

        /**
         * Error handling callback.
         * Assumes that the request has completed.
         * @ignore
         */
        var sendError = function sendError(request, context, status, description, serverErrorName, serverErrorMessage) {

            // Possible errornames:
            // httpError
            // emptyResponse
            // serverError
            // malformedXML

            var sent = false;
            var data = {};  // data payload for function
            data.type = "error";
            data.status = status;
            data.source = context.sourceid;
            data.responseCode = request.status;
            data.responseXML = request.responseXML;
            data.responseText = request.responseText;

            // ensure data source is the dom element and not the ID
            // per 14.4.1 of the 2.0 specification.
            if (typeof data.source === 'string') {
                data.source = document.getElementById(data.source);
            }

            if (description) {
                data.description = description;
            } else if (status == "httpError") {
                if (data.responseCode === 0) {
                    data.description = "The Http Transport returned a 0 status code.  This is usually the result of mixing ajax and full requests.  This is usually undesired, for both performance and data integrity reasons.";
                } else {
                    data.description = "There was an error communicating with the server, status: " + data.responseCode;
                }
            } else if (status == "serverError") {
                data.description = serverErrorMessage;
            } else if (status == "emptyResponse") {
                data.description = "An empty response was received from the server.  Check server error logs.";
            } else if (status == "malformedXML") {
                if (getParseErrorText(data.responseXML) !== PARSED_OK) {
                    data.description = getParseErrorText(data.responseXML);
                } else {
                    data.description = "An invalid XML response was received from the server.";
                }
            }

            if (status == "serverError") {
                data.errorName = serverErrorName;
                data.errorMessage = serverErrorMessage;
            }

            // If we have a registered callback, send the error to it.
            if (context.onerror) {
                context.onerror.call(null, data);
                sent = true;
            }

            for (var i in errorListeners) {
                if (errorListeners.hasOwnProperty(i)) {
                    errorListeners[i].call(null, data);
                    sent = true;
                }
            }

            if (!sent && jsf.getProjectStage() === "Development") {
                if (status == "serverError") {
                    alert("serverError: " + serverErrorName + " " + serverErrorMessage);
                } else {
                    alert(status + ": " + data.description);
                }
            }
        };

        /**
         * Event handling callback.
         * Request is assumed to have completed, except in the case of event = 'begin'.
         * @ignore
         */
        var sendEvent = function sendEvent(request, context, status) {

            var data = {};
            data.type = "event";
            data.status = status;
            data.source = context.sourceid;
            // ensure data source is the dom element and not the ID
            // per 14.4.1 of the 2.0 specification.
            if (typeof data.source === 'string') {
                data.source = document.getElementById(data.source);
            }
            if (status !== 'begin') {
                data.responseCode = request.status;
                data.responseXML = request.responseXML;
                data.responseText = request.responseText;
            }

            if (context.onevent) {
                context.onevent.call(null, data);
            }

            for (var i in eventListeners) {
                if (eventListeners.hasOwnProperty(i)) {
                    eventListeners[i].call(null, data);
                }
            }
        };

        var unescapeHTML = function unescapeHTML(escapedHTML) {
            return escapedHTML
                .replace(/&apos;/g, "'")
                .replace(/&quot;/g, '"')
                .replace(/&gt;/g, '>')
                .replace(/&lt;/g, '<')
                .replace(/&amp;/g, '&');
        };

        // Use module pattern to return the functions we actually expose
        return {
            /**
             * Register a callback for error handling.
             * <p><b>Usage:</b></p>
             * <pre><code>
             * jsf.ajax.addOnError(handleError);
             * ...
             * var handleError = function handleError(data) {
             * ...
             * }
             * </pre></code>
             * <p><b>Implementation Requirements:</b></p>
             * This function must accept a reference to an existing JavaScript function.
             * The JavaScript function reference must be added to a list of callbacks, making it possible
             * to register more than one callback by invoking <code>jsf.ajax.addOnError</code>
             * more than once.  This function must throw an error if the <code>callback</code>
             * argument is not a function.
             *
             * @member jsf.ajax
             * @param callback a reference to a function to call on an error
             */
            addOnError: function addOnError(callback) {
                if (typeof callback === 'function') {
                    errorListeners[errorListeners.length] = callback;
                } else {
                    throw new Error("jsf.ajax.addOnError:  Added a callback that was not a function.");
                }
            },
            /**
             * Register a callback for event handling.
             * <p><b>Usage:</b></p>
             * <pre><code>
             * jsf.ajax.addOnEvent(statusUpdate);
             * ...
             * var statusUpdate = function statusUpdate(data) {
             * ...
             * }
             * </pre></code>
             * <p><b>Implementation Requirements:</b></p>
             * This function must accept a reference to an existing JavaScript function.
             * The JavaScript function reference must be added to a list of callbacks, making it possible
             * to register more than one callback by invoking <code>jsf.ajax.addOnEvent</code>
             * more than once.  This function must throw an error if the <code>callback</code>
             * argument is not a function.
             *
             * @member jsf.ajax
             * @param callback a reference to a function to call on an event
             */
            addOnEvent: function addOnEvent(callback) {
                if (typeof callback === 'function') {
                    eventListeners[eventListeners.length] = callback;
                } else {
                    throw new Error("jsf.ajax.addOnEvent: Added a callback that was not a function");
                }
            },
            /**

             * <p><span class="changed_modified_2_2">Send</span> an
             * asynchronous Ajax req uest to the server.

             * <p><b>Usage:</b></p>
             * <pre><code>
             * Example showing all optional arguments:
             *
             * &lt;commandButton id="button1" value="submit"
             *     onclick="jsf.ajax.request(this,event,
             *       {execute:'button1',render:'status',onevent: handleEvent,onerror: handleError});return false;"/&gt;
             * &lt;/commandButton/&gt;
             * </pre></code>
             * <p><b>Implementation Requirements:</b></p>
             * This function must:
             * <ul>
             * <li>Be used within the context of a <code>form</code><span class="changed_added_2_3">,
             * else throw an error</span>.</li>
             * <li>Capture the element that triggered this Ajax request
             * (from the <code>source</code> argument, also known as the
             * <code>source</code> element.</li>
             * <li>If the <code>source</code> element is <code>null</code> or
             * <code>undefined</code> throw an error.</li>
             * <li>If the <code>source</code> argument is not a <code>string</code> or
             * DOM element object, throw an error.</li>
             * <li>If the <code>source</code> argument is a <code>string</code>, find the
             * DOM element for that <code>string</code> identifier.
             * <li>If the DOM element could not be determined, throw an error.</li>
             * <li class="changed_added_2_3">If the <code>javax.faces.ViewState</code> 
             * element could not be found, throw an error.</li>
             * <li class="changed_added_2_3">If the ID of the <code>javax.faces.ViewState</code> 
             * element has a <code>&lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt;&lt;SEP&gt;</code>
             * prefix, where &lt;SEP&gt; is the currently configured
             * <code>UINamingContainer.getSeparatorChar()</code> and
             * &lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt; is the return from
             * <code>UIViewRoot.getContainerClientId()</code> on the
             * view from whence this state originated, then remember it as <i>namespace prefix</i>.
             * This is needed during encoding of the set of post data arguments.</li>
             * <li>If the <code>onerror</code> and <code>onevent</code> arguments are set,
             * they must be functions, or throw an error.
             * <li>Determine the <code>source</code> element's <code>form</code>
             * element.</li>
             * <li>Get the <code>form</code> view state by calling
             * {@link jsf.getViewState} passing the
             * <code>form</code> element as the argument.</li>
             * <li>Collect post data arguments for the Ajax request.
             * <ul>
             * <li>The following name/value pairs are required post data arguments:
             * <table border="1">
             * <tr>
             * <th>name</th>
             * <th>value</th>
             * </tr>
             * <tr>
             * <td><code>javax.faces.ViewState</code></td>
             * <td><code>Contents of javax.faces.ViewState hidden field.  This is included when
             * {@link jsf.getViewState} is used.</code></td>
             * </tr>
             * <tr>
             * <td><code>javax.faces.partial.ajax</code></td>
             * <td><code>true</code></td>
             * </tr>
             * <tr>
             * <td><code>javax.faces.source</code></td>
             * <td><code>The identifier of the element that triggered this request.</code></td>
             * </tr>
             * <tr class="changed_added_2_2">
             * <td><code>javax.faces.ClientWindow</code></td>

             * <td><code>Call jsf.getClientWindow(), passing the current
             * form.  If the return is non-null, it must be set as the
             * value of this name/value pair, otherwise, a name/value
             * pair for client window must not be sent.</code></td>

             * </tr>
             * </table>
             * </li>
             * </ul>
             * </li>
             * <li>Collect optional post data arguments for the Ajax request.
             * <ul>
             * <li>Determine additional arguments (if any) from the <code>options</code>
             * argument. If <code>options.execute</code> exists:
             * <ul>
             * <li>If the keyword <code>@none</code> is present, do not create and send
             * the post data argument <code>javax.faces.partial.execute</code>.</li>
             * <li>If the keyword <code>@all</code> is present, create the post data argument with
             * the name <code>javax.faces.partial.execute</code> and the value <code>@all</code>.</li>
             * <li>Otherwise, there are specific identifiers that need to be sent.  Create the post
             * data argument with the name <code>javax.faces.partial.execute</code> and the value as a
             * space delimited <code>string</code> of client identifiers.</li>
             * </ul>
             * </li>
             * <li>If <code>options.execute</code> does not exist, create the post data argument with the
             * name <code>javax.faces.partial.execute</code> and the value as the identifier of the
             * element that caused this request.</li>
             * <li>If <code>options.render</code> exists:
             * <ul>
             * <li>If the keyword <code>@none</code> is present, do not create and send
             * the post data argument <code>javax.faces.partial.render</code>.</li>
             * <li>If the keyword <code>@all</code> is present, create the post data argument with
             * the name <code>javax.faces.partial.render</code> and the value <code>@all</code>.</li>
             * <li>Otherwise, there are specific identifiers that need to be sent.  Create the post
             * data argument with the name <code>javax.faces.partial.render</code> and the value as a
             * space delimited <code>string</code> of client identifiers.</li>
             * </ul>
             * <li>If <code>options.render</code> does not exist do not create and send the
             * post data argument <code>javax.faces.partial.render</code>.</li>

             * <li class="changed_added_2_2">If
             * <code>options.delay</code> exists let it be the value
             * <em>delay</em>, for this discussion.  If
             * <code>options.delay</code> does not exist, or is the
             * literal string <code>'none'</code>, without the quotes,
             * no delay is used.  If less than <em>delay</em>
             * milliseconds elapses between calls to <em>request()</em>
             * only the most recent one is sent and all other requests
             * are discarded.</li>


             * <li class="changed_added_2_2">If
             * <code>options.resetValues</code> exists and its value is
             * <code>true</code>, ensure a post data argument with the
             * name <code>javax.faces.partial.resetValues</code> and the
             * value <code>true</code> is sent in addition to the other
             * post data arguments.  This will cause
             * <code>UIViewRoot.resetValues()</code> to be called,
             * passing the value of the "render" attribute.  Note: do
             * not use any of the <code>@</code> keywords such as
             * <code>@form</code> or <code>@this</code> with this option
             * because <code>UIViewRoot.resetValues()</code> does not
             * descend into the children of the listed components.</li>


             * <li>Determine additional arguments (if any) from the <code>event</code>
             * argument.  The following name/value pairs may be used from the
             * <code>event</code> object:
             * <ul>
             * <li><code>target</code> - the ID of the element that triggered the event.</li>
             * <li><code>captured</code> - the ID of the element that captured the event.</li>
             * <li><code>type</code> - the type of event (ex: onkeypress)</li>
             * <li><code>alt</code> - <code>true</code> if ALT key was pressed.</li>
             * <li><code>ctrl</code> - <code>true</code> if CTRL key was pressed.</li>
             * <li><code>shift</code> - <code>true</code> if SHIFT key was pressed. </li>
             * <li><code>meta</code> - <code>true</code> if META key was pressed. </li>
             * <li><code>right</code> - <code>true</code> if right mouse button
             * was pressed. </li>
             * <li><code>left</code> - <code>true</code> if left mouse button
             * was pressed. </li>
             * <li><code>keycode</code> - the key code.
             * </ul>
             * </li>
             * </ul>
             * </li>
             * <li>Encode the set of post data arguments. <span class="changed_added_2_3">
             * If the <code>javax.faces.ViewState</code> element has a namespace prefix, then
             * make sure that all post data arguments are prefixed with this namespace prefix.
             * </span></li>
             * <li>Join the encoded view state with the encoded set of post data arguments
             * to form the <code>query string</code> that will be sent to the server.</li>
             * <li>Create a request <code>context</code> object and set the properties:
             * <ul><li><code>source</code> (the source DOM element for this request)</li>
             * <li><code>onerror</code> (the error handler for this request)</li>
             * <li><code>onevent</code> (the event handler for this request)</li></ul>
             * The request context will be used during error/event handling.</li>
             * <li>Send a <code>begin</code> event following the procedure as outlined
             * in the Chapter 13 "Sending Events" section of the spec prose document <a
             *  href="../../javadocs/overview-summary.html#prose_document">linked in the
             *  overview summary</a></li>
             * <li>Set the request header with the name: <code>Faces-Request</code> and the
             * value: <code>partial/ajax</code>.</li>
             * <li>Determine the <code>posting URL</code> as follows: If the hidden field
             * <code>javax.faces.encodedURL</code> is present in the submitting form, use its
             * value as the <code>posting URL</code>.  Otherwise, use the <code>action</code>
             * property of the <code>form</code> element as the <code>URL</code>.</li>

             * <li> 

             * <p><span class="changed_modified_2_2">Determine whether
             * or not the submitting form is using 
             * <code>multipart/form-data</code> as its
             * <code>enctype</code> attribute.  If not, send the request
             * as an <code>asynchronous POST</code> using the
             * <code>posting URL</code> that was determined in the
             * previous step.</span> <span
             * class="changed_added_2_2">Otherwise, send the request
             * using a multi-part capable transport layer, such as a
             * hidden inline frame.  Note that using a hidden inline
             * frame does <strong>not</strong> use
             * <code>XMLHttpRequest</code>, but the request must be sent
             * with all the parameters that a JSF
             * <code>XMLHttpRequest</code> would have been sent with.
             * In this way, the server side processing of the request
             * will be identical whether or the request is multipart or
             * not.</span></p>
            
             * <div class="changed_added_2_2">

             * <p>The <code>begin</code>, <code>complete</code>, and
             * <code>success</code> events must be emulated when using
             * the multipart transport.  This allows any listeners to
             * behave uniformly regardless of the multipart or
             * <code>XMLHttpRequest</code> nature of the transport.</p>

             * </div></li>
             * </ul>
             * Form serialization should occur just before the request is sent to minimize 
             * the amount of time between the creation of the serialized form data and the 
             * sending of the serialized form data (in the case of long requests in the queue).
             * Before the request is sent it must be put into a queue to ensure requests
             * are sent in the same order as when they were initiated.  The request callback function
             * must examine the queue and determine the next request to be sent.  The behavior of the
             * request callback function must be as follows:
             * <ul>
             * <li>If the request completed successfully invoke {@link jsf.ajax.response}
             * passing the <code>request</code> object.</li>
             * <li>If the request did not complete successfully, notify the client.</li>
             * <li>Regardless of the outcome of the request (success or error) every request in the
             * queue must be handled.  Examine the status of each request in the queue starting from
             * the request that has been in the queue the longest.  If the status of the request is
             * <code>complete</code> (readyState 4), dequeue the request (remove it from the queue).
             * If the request has not been sent (readyState 0), send the request.  Requests that are
             * taken off the queue and sent should not be put back on the queue.</li>
             * </ul>
             *
             * </p>
             *
             * @param source The DOM element that triggered this Ajax request, or an id string of the
             * element to use as the triggering element.
             * @param event The DOM event that triggered this Ajax request.  The
             * <code>event</code> argument is optional.
             * @param options The set of available options that can be sent as
             * request parameters to control client and/or server side
             * request processing. Acceptable name/value pair options are:
             * <table border="1">
             * <tr>
             * <th>name</th>
             * <th>value</th>
             * </tr>
             * <tr>
             * <td><code>execute</code></td>
             * <td><code>space seperated list of client identifiers</code></td>
             * </tr>
             * <tr>
             * <td><code>render</code></td>
             * <td><code>space seperated list of client identifiers</code></td>
             * </tr>
             * <tr>
             * <td><code>onevent</code></td>
             * <td><code>function to callback for event</code></td>
             * </tr>
             * <tr>
             * <td><code>onerror</code></td>
             * <td><code>function to callback for error</code></td>
             * </tr>
             * <tr>
             * <td><code>params</code></td>
             * <td><code>object containing parameters to include in the request</code></td>
             * </tr>

             * <tr class="changed_added_2_2">

             * <td><code>delay</code></td>

             * <td>If less than <em>delay</em> milliseconds elapses
             * between calls to <em>request()</em> only the most recent
             * one is sent and all other requests are discarded. If the
             * value of <em>delay</em> is the literal string
             * <code>'none'</code> without the quotes, or no delay is
             * specified, no delay is used. </td>

             * </tr>

             * <tr class="changed_added_2_2">

             * <td><code>resetValues</code></td>

             * <td>If true, ensure a post data argument with the name
             * javax.faces.partial.resetValues and the value true is
             * sent in addition to the other post data arguments. This
             * will cause UIViewRoot.resetValues() to be called, passing
             * the value of the "render" attribute. Note: do not use any
             * of the @ keywords such as @form or @this with this option
             * because UIViewRoot.resetValues() does not descend into
             * the children of the listed components.</td>

             * </tr>


             * </table>
             * The <code>options</code> argument is optional.
             * @member jsf.ajax
             * @function jsf.ajax.request

             * @throws Error if first required argument
             * <code>element</code> is not specified, or if one or more
             * of the components in the <code>options.execute</code>
             * list is a file upload component, but the form's enctype
             * is not set to <code>multipart/form-data</code>
             */

            request: function request(source, event, options) {

                var element, form, viewStateElement;   //  Element variables
                var all, none;
                
                var context = {};

                if (typeof source === 'undefined' || source === null) {
                    throw new Error("jsf.ajax.request: source not set");
                }
                if(delayHandler) {
                    clearTimeout(delayHandler);
                    delayHandler = null;
                }

                // set up the element based on source
                if (typeof source === 'string') {
                    element = document.getElementById(source);
                } else if (typeof source === 'object') {
                    element = source;
                } else {
                    throw new Error("jsf.ajax.request: source must be object or string");
                }
                // attempt to handle case of name unset
                // this might be true in a badly written composite component
                if (!element.name) {
                    element.name = element.id;
                }
                
                context.element = element;

                if (typeof(options) === 'undefined' || options === null) {
                    options = {};
                }

                // Error handler for this request
                var onerror = false;

                if (options.onerror && typeof options.onerror === 'function') {
                    onerror = options.onerror;
                } else if (options.onerror && typeof options.onerror !== 'function') {
                    throw new Error("jsf.ajax.request: Added an onerror callback that was not a function");
                }

                // Event handler for this request
                var onevent = false;

                if (options.onevent && typeof options.onevent === 'function') {
                    onevent = options.onevent;
                } else if (options.onevent && typeof options.onevent !== 'function') {
                    throw new Error("jsf.ajax.request: Added an onevent callback that was not a function");
                }

                form = getForm(element);
                if (!form) {
                    throw new Error("jsf.ajax.request: Method must be called within a form");
                }

                viewStateElement = getHiddenStateField(form, "javax.faces.ViewState");
                if (!viewStateElement) {
                    throw new Error("jsf.ajax.request: Form has no view state element");
                }
                
                context.form = form;
                context.formId = form.id;
                
                var viewState = jsf.getViewState(form);

                // Set up additional arguments to be used in the request..
                // Make sure "javax.faces.source" is set up.
                // If there were "execute" ids specified, make sure we
                // include the identifier of the source element in the
                // "execute" list.  If there were no "execute" ids
                // specified, determine the default.

                var args = {};

                var namingContainerPrefix = viewStateElement.name.substring(0, viewStateElement.name.indexOf("javax.faces.ViewState"));

                args[namingContainerPrefix + "javax.faces.source"] = element.id;

                if (event && !!event.type) {
                    args[namingContainerPrefix + "javax.faces.partial.event"] = event.type;
                }

                if ("resetValues" in options) {
                    args[namingContainerPrefix + "javax.faces.partial.resetValues"] = options.resetValues;
                }

                // If we have 'execute' identifiers:
                // Handle any keywords that may be present.
                // If @none present anywhere, do not send the
                // "javax.faces.partial.execute" parameter.
                // The 'execute' and 'render' lists must be space
                // delimited.

                if (options.execute) {
                    none = options.execute.indexOf("@none");
                    if (none < 0) {
                        all = options.execute.indexOf("@all");
                        if (all < 0) {
                            options.execute = options.execute.replace("@this", element.id);
                            options.execute = options.execute.replace("@form", form.id);
                            var temp = options.execute.split(' ');
                            if (!isInArray(temp, element.name)) {
                                options.execute = element.name + " " + options.execute;
                            }
                            if (namingContainerPrefix) {
                            	options.execute = namespaceParametersIfNecessary(options.execute, element.name, namingContainerPrefix);
                            }
                        } else {
                            options.execute = "@all";
                        }
                        args[namingContainerPrefix + "javax.faces.partial.execute"] = options.execute;
                    }
                } else {
                    options.execute = element.name + " " + element.id;
                    args[namingContainerPrefix + "javax.faces.partial.execute"] = options.execute;
                }

                if (options.render) {
                    none = options.render.indexOf("@none");
                    if (none < 0) {
                        all = options.render.indexOf("@all");
                        if (all < 0) {
                            options.render = options.render.replace("@this", element.id);
                            options.render = options.render.replace("@form", form.id);
                            if (namingContainerPrefix) {
                            	options.render = namespaceParametersIfNecessary(options.render, element.name, namingContainerPrefix);
                            }
                        } else {
                            options.render = "@all";
                        }
                        args[namingContainerPrefix + "javax.faces.partial.render"] = options.render;
                    }
                }
                var explicitlyDoNotDelay = ((typeof options.delay == 'undefined') || (typeof options.delay == 'string') &&
                                            (options.delay.toLowerCase() == 'none'));
                var delayValue;
                if (typeof options.delay == 'number') {
                    delayValue = options.delay;
                } else  {
                    var converted = parseInt(options.delay);
                    
                    if (!explicitlyDoNotDelay && isNaN(converted)) {
                        throw new Error('invalid value for delay option: ' + options.delay);
                    }
                    delayValue = converted;
                }

                var checkForTypeFile

                // check the execute ids to see if any include an input of type "file"
                context.includesInputFile = false;
                var ids = options.execute.split(" ");
                if (ids == "@all") { ids = [ form.id ]; }
                if (ids) {
                    for (i = 0; i < ids.length; i++) {
                        var elem = document.getElementById(ids[i]);
                        if (elem) {
                            var nodeType = elem.nodeType;
                            if (nodeType == Node.ELEMENT_NODE) {
                                var elemAttributeDetector = detectAttributes(elem);
                                if (elemAttributeDetector("type")) {
                                    if (elem.getAttribute("type") === "file") {
                                        context.includesInputFile = true;
                                        break;
                                    }
                                } else {
                                    if (hasInputFileControl(elem)) {
                                        context.includesInputFile = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                // copy all params to args
                var params = options.params || {};
                for (var property in params) {
                    if (params.hasOwnProperty(property)) {
                        args[namingContainerPrefix + property] = params[property];
                    }
                }

                // remove non-passthrough options
                delete options.execute;
                delete options.render;
                delete options.onerror;
                delete options.onevent;
                delete options.delay;
                delete options.resetValues;
                delete options.params;

                // copy all other options to args (for backwards compatibility on issue 4115)
                for (var property in options) {
                    if (options.hasOwnProperty(property)) {
                        args[namingContainerPrefix + property] = options[property];
                    }
                }

                args[namingContainerPrefix + "javax.faces.partial.ajax"] = "true";
                args["method"] = "POST";

                // Determine the posting url

                var encodedUrlField = getEncodedUrlElement(form);
                if (typeof encodedUrlField == 'undefined') {
                    args["url"] = form.action;
                } else {
                    args["url"] = encodedUrlField.value;
                }
                var sendRequest = function() {
                    var ajaxEngine = new AjaxEngine(context);
                    ajaxEngine.setupArguments(args);
                    ajaxEngine.queryString = viewState;
                    ajaxEngine.context.onevent = onevent;
                    ajaxEngine.context.onerror = onerror;
                    ajaxEngine.context.sourceid = element.id;
                    ajaxEngine.context.render = args[namingContainerPrefix + "javax.faces.partial.render"] || "";
                    ajaxEngine.context.namingContainerPrefix = namingContainerPrefix;
                    ajaxEngine.sendRequest();

                    // null out element variables to protect against IE memory leak
                    element = null;
                    form = null;
                    sendRequest = null;
                    context = null;
                };

                if (explicitlyDoNotDelay) {
                    sendRequest();
                } else {
                    delayHandler = setTimeout(sendRequest, delayValue);
                }

            },
            /**
             * <p><span class="changed_modified_2_2">Receive</span> an Ajax response 
             * from the server.
             * <p><b>Usage:</b></p>
             * <pre><code>
             * jsf.ajax.response(request, context);
             * </pre></code>
             * <p><b>Implementation Requirements:</b></p>
             * This function must evaluate the markup returned in the
             * <code>request.responseXML</code> object and perform the following action:
             * <ul>
             * <p>If there is no XML response returned, signal an <code>emptyResponse</code>
             * error. If the XML response does not follow the format as outlined
             * in Appendix A of the spec prose document <a
             *  href="../../javadocs/overview-summary.html#prose_document">linked in the
             *  overview summary</a> signal a <code>malformedError</code> error.  Refer to
             * section "Signaling Errors" in Chapter 13 of the spec prose document <a
             *  href="../../javadocs/overview-summary.html#prose_document">linked in the
             *  overview summary</a>.</p>
             * <p>If the response was successfully processed, send a <code>success</code>
             * event as outlined in Chapter 13 "Sending Events" section of the spec prose
             * document <a
             * href="../../javadocs/overview-summary.html#prose_document">linked in the
             * overview summary</a>.</p>
             * <p><i>Update Element Processing</i></p>
             * The <code>update</code> element is used to update a single DOM element.  The
             * "id" attribute of the <code>update</code> element refers to the DOM element that
             * will be updated.  The contents of the <code>CDATA</code> section is the data that 
             * will be used when updating the contents of the DOM element as specified by the
             * <code>&lt;update&gt;</code> element identifier.
             * <li>If an <code>&lt;update&gt;</code> element is found in the response
             * with the identifier <code>javax.faces.ViewRoot</code>:
             * <pre><code>&lt;update id="javax.faces.ViewRoot"&gt;
             *    &lt;![CDATA[...]]&gt;
             * &lt;/update&gt;</code></pre>
             * Update the entire DOM replacing the appropriate <code>head</code> and/or
             * <code>body</code> sections with the content from the response.</li>

             * <li class="changed_modified_2_2">If an
             * <code>&lt;update&gt;</code> element is found in the 
             * response with an identifier containing
             * <code>javax.faces.ViewState</code>:

             * <pre><code>&lt;update id="&lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt;&lt;SEP&gt;javax.faces.ViewState&lt;SEP&gt;&lt;UNIQUE_PER_VIEW_NUMBER&gt;"&gt;
             *    &lt;![CDATA[...]]&gt;
             * &lt;/update&gt;</code></pre>

             * locate and update the submitting form's
             * <code>javax.faces.ViewState</code> value with the
             * <code>CDATA</code> contents from the response.
             * &lt;SEP&gt; is the currently configured
             * <code>UINamingContainer.getSeparatorChar()</code>.
             * &lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt; is the return from
             * <code>UIViewRoot.getContainerClientId()</code> on the
             * view from whence this state originated.
             * &lt;UNIQUE_PER_VIEW_NUMBER&gt; is a number that must be
             * unique within this view, but must not be included in the
             * view state.  This requirement is simply to satisfy XML
             * correctness in parity with what is done in the
             * corresponding non-partial JSF view.  Locate and update
             * the <code>javax.faces.ViewState</code> value for all
             * JSF forms covered in the <code>render</code> target
             * list whose ID starts with the same 
             * &lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt; value.</li>

             * <li class="changed_added_2_2">If an
             * <code>update</code> element is found in the response with
             * an identifier containing
             * <code>javax.faces.ClientWindow</code>:

             * <pre><code>&lt;update id="&lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt;&lt;SEP&gt;javax.faces.ClientWindow&lt;SEP&gt;&lt;UNIQUE_PER_VIEW_NUMBER&gt;"&gt;
             *    &lt;![CDATA[...]]&gt;
             * &lt;/update&gt;</code></pre>

             * locate and update the submitting form's
             * <code>javax.faces.ClientWindow</code> value with the
             * <code>CDATA</code> contents from the response.
             * &lt;SEP&gt; is the currently configured
             * <code>UINamingContainer.getSeparatorChar()</code>.
             * &lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt; is the return from
             * <code>UIViewRoot.getContainerClientId()</code> on the
             * view from whence this state originated.             
             * &lt;UNIQUE_PER_VIEW_NUMBER&gt; is a number that must be
             * unique within this view, but must not be included in the
             * view state.  This requirement is simply to satisfy XML
             * correctness in parity with what is done in the
             * corresponding non-partial JSF view.  Locate and update
             * the <code>javax.faces.ClientWindow</code> value for all
             * JSF forms covered in the <code>render</code> target
             * list whose ID starts with the same 
             * &lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt; value.</li>

             * <li class="changed_added_2_3">If an <code>update</code> element is found in the response with the
             * identifier <code>javax.faces.Resource</code>:
             * <pre><code>&lt;update id="javax.faces.Resource"&gt;
             *    &lt;![CDATA[...]]&gt;
             * &lt;/update&gt;</code></pre>
             * append any element found in the <code>CDATA</code> contents which is absent in the document to the
             * document's <code>head</code> section.
             * </li>

             * <li>If an <code>update</code> element is found in the response with the identifier
             * <code>javax.faces.ViewHead</code>:
             * <pre><code>&lt;update id="javax.faces.ViewHead"&gt;
             *    &lt;![CDATA[...]]&gt;
             * &lt;/update&gt;</code></pre>
             * update the document's <code>head</code> section with the <code>CDATA</code>
             * contents from the response.</li>
             * <li>If an <code>update</code> element is found in the response with the identifier
             * <code>javax.faces.ViewBody</code>:
             * <pre><code>&lt;update id="javax.faces.ViewBody"&gt;
             *    &lt;![CDATA[...]]&gt;
             * &lt;/update&gt;</code></pre>
             * update the document's <code>body</code> section with the <code>CDATA</code>
             * contents from the response.</li>
             * <li>For any other <code>&lt;update&gt;</code> element:
             * <pre><code>&lt;update id="update id"&gt;
             *    &lt;![CDATA[...]]&gt;
             * &lt;/update&gt;</code></pre>
             * Find the DOM element with the identifier that matches the
             * <code>&lt;update&gt;</code> element identifier, and replace its contents with
             * the <code>&lt;update&gt;</code> element's <code>CDATA</code> contents.</li>
             * </li>
             * <p><i>Insert Element Processing</i></p>
    
             * <li>If an <code>&lt;insert&gt;</code> element is found in
             * the response with a nested <code>&lt;before&gt;</code>
             * element:
            
             * <pre><code>&lt;insert&gt;
             *     &lt;before id="before id"&gt;
             *        &lt;![CDATA[...]]&gt;
             *     &lt;/before&gt;
             * &lt;/insert&gt;</code></pre>
             * 
             * <ul>
             * <li>Extract this <code>&lt;before&gt;</code> element's <code>CDATA</code> contents
             * from the response.</li>
             * <li>Find the DOM element whose identifier matches <code>before id</code> and insert
             * the <code>&lt;before&gt;</code> element's <code>CDATA</code> content before
             * the DOM element in the document.</li>
             * </ul>
             * </li>
             * 
             * <li>If an <code>&lt;insert&gt;</code> element is found in 
             * the response with a nested <code>&lt;after&gt;</code>
             * element:
             * 
             * <pre><code>&lt;insert&gt;
             *     &lt;after id="after id"&gt;
             *        &lt;![CDATA[...]]&gt;
             *     &lt;/after&gt;
             * &lt;/insert&gt;</code></pre>
             * 
             * <ul>
             * <li>Extract this <code>&lt;after&gt;</code> element's <code>CDATA</code> contents
             * from the response.</li>
             * <li>Find the DOM element whose identifier matches <code>after id</code> and insert
             * the <code>&lt;after&gt;</code> element's <code>CDATA</code> content after
             * the DOM element in the document.</li>
             * </ul>
             * </li>
             * <p><i>Delete Element Processing</i></p>
             * <li>If a <code>&lt;delete&gt;</code> element is found in the response:
             * <pre><code>&lt;delete id="delete id"/&gt;</code></pre>
             * Find the DOM element whose identifier matches <code>delete id</code> and remove it
             * from the DOM.</li>
             * <p><i>Element Attribute Update Processing</i></p>
             * <li>If an <code>&lt;attributes&gt;</code> element is found in the response:
             * <pre><code>&lt;attributes id="id of element with attribute"&gt;
             *    &lt;attribute name="attribute name" value="attribute value"&gt;
             *    ...
             * &lt/attributes&gt;</code></pre>
             * <ul>
             * <li>Find the DOM element that matches the <code>&lt;attributes&gt;</code> identifier.</li>
             * <li>For each nested <code>&lt;attribute&gt;</code> element in <code>&lt;attribute&gt;</code>,
             * update the DOM element attribute value (whose name matches <code>attribute name</code>),
             * with <code>attribute value</code>.</li>
             * </ul>
             * </li>
             * <p><i>JavaScript Processing</i></p>
             * <li>If an <code>&lt;eval&gt;</code> element is found in the response:
             * <pre><code>&lt;eval&gt;
             *    &lt;![CDATA[...JavaScript...]]&gt;
             * &lt;/eval&gt;</code></pre>
             * <ul>
             * <li>Extract this <code>&lt;eval&gt;</code> element's <code>CDATA</code> contents
             * from the response and execute it as if it were JavaScript code.</li>
             * </ul>
             * </li>
             * <p><i>Redirect Processing</i></p>
             * <li>If a <code>&lt;redirect&gt;</code> element is found in the response:
             * <pre><code>&lt;redirect url="redirect url"/&gt;</code></pre>
             * Cause a redirect to the url <code>redirect url</code>.</li>
             * <p><i>Error Processing</i></p>
             * <li>If an <code>&lt;error&gt;</code> element is found in the response:
             * <pre><code>&lt;error&gt;
             *    &lt;error-name&gt;..fully qualified class name string...&lt;error-name&gt;
             *    &lt;error-message&gt;&lt;![CDATA[...]]&gt;&lt;error-message&gt;
             * &lt;/error&gt;</code></pre>
             * Extract this <code>&lt;error&gt;</code> element's <code>error-name</code> contents
             * and the <code>error-message</code> contents. Signal a <code>serverError</code> passing
             * the <code>errorName</code> and <code>errorMessage</code>.  Refer to
             * section "Signaling Errors" in Chapter 13 of the spec prose document <a
             *  href="../../javadocs/overview-summary.html#prose_document">linked in the
             *  overview summary</a>.</li>
             * <p><i>Extensions</i></p>
             * <li>The <code>&lt;extensions&gt;</code> element provides a way for framework
             * implementations to provide their own information.</li>
             * <p><li>The implementation must check if &lt;script&gt; elements in the response can
             * be automatically run, as some browsers support this feature and some do not.  
             * If they can not be run, then scripts should be extracted from the response and
             * run separately.</li></p> 
             * </ul>
             *
             * </p>
             *
             * @param request The <code>XMLHttpRequest</code> instance that
             * contains the status code and response message from the server.
             *
             * @param context An object containing the request context, including the following properties:
             * the source element, per call onerror callback function, and per call onevent callback function.
             *
             * @throws  Error if request contains no data
             *
             * @function jsf.ajax.response
             */
            response: function response(request, context) {
                if (!request) {
                    throw new Error("jsf.ajax.response: Request parameter is unset");
                }

                // ensure context source is the dom element and not the ID
                // per 14.4.1 of the 2.0 specification.  We're doing it here
                // *before* any errors or events are propagated becasue the
                // DOM element may be removed after the update has been processed.
                if (typeof context.sourceid === 'string') {
                    context.sourceid = document.getElementById(context.sourceid);
                }

                var xml = request.responseXML;
                if (xml === null) {
                    sendError(request, context, "emptyResponse");
                    return;
                }

                if (getParseErrorText(xml) !== PARSED_OK) {
                    sendError(request, context, "malformedXML");
                    return;
                }

                var partialResponse = xml.getElementsByTagName("partial-response")[0];
                var namingContainerId = partialResponse.getAttribute("id");
                var namingContainerPrefix = namingContainerId ? (namingContainerId + jsf.separatorchar) : "";
                var responseType = partialResponse.firstChild;
                
                context.namingContainerId = namingContainerId;
                context.namingContainerPrefix = namingContainerPrefix;

                for (var i = 0; i < partialResponse.childNodes.length; i++) {
                    if (partialResponse.childNodes[i].nodeName === "error") {
                        responseType = partialResponse.childNodes[i];
                        break;
                    }
                }

                if (responseType.nodeName === "error") { // it's an error
                    var errorName = "";
                    var errorMessage = "";
                    
                    var element = responseType.firstChild;
                    if (element.nodeName === "error-name") {
                        if (null != element.firstChild) {
                            errorName = element.firstChild.nodeValue;
                        }
                    }
                    
                    element = responseType.firstChild.nextSibling;
                    if (element.nodeName === "error-message") {
                        if (null != element.firstChild) {
                            errorMessage = element.firstChild.nodeValue;
                        }
                    }
                    sendError(request, context, "serverError", null, errorName, errorMessage);
                    sendEvent(request, context, "success");
                    return;
                }


                if (responseType.nodeName === "redirect") {
                    window.location = responseType.getAttribute("url");
                    return;
                }


                if (responseType.nodeName !== "changes") {
                    sendError(request, context, "malformedXML", "Top level node must be one of: changes, redirect, error, received: " + responseType.nodeName + " instead.");
                    return;
                }


                var changes = responseType.childNodes;

                try {
                    for (var i = 0; i < changes.length; i++) {
                        switch (changes[i].nodeName) {
                            case "update":
                                doUpdate(changes[i], context);
                                break;
                            case "delete":
                                doDelete(changes[i]);
                                break;
                            case "insert":
                                doInsert(changes[i]);
                                break;
                            case "attributes":
                                doAttributes(changes[i]);
                                break;
                            case "eval":
                                doEval(changes[i]);
                                break;
                            case "extension":
                                // no action
                                break;
                            default:
                                sendError(request, context, "malformedXML", "Changes allowed are: update, delete, insert, attributes, eval, extension.  Received " + changes[i].nodeName + " instead.");
                                return;
                        }
                    }
                } catch (ex) {
                    sendError(request, context, "malformedXML", ex.message);
                    return;
                }
                sendEvent(request, context, "success");

            }
        };
    }();

    /**
     *
     * <p>Return the value of <code>Application.getProjectStage()</code> for
     * the currently running application instance.  Calling this method must
     * not cause any network transaction to happen to the server.</p>
     * <p><b>Usage:</b></p>
     * <pre><code>
     * var stage = jsf.getProjectStage();
     * if (stage === ProjectStage.Development) {
     *  ...
     * } else if stage === ProjectStage.Production) {
     *  ...
     * }
     * </code></pre>
     *
     * @returns String <code>String</code> representing the current state of the
     * running application in a typical product development lifecycle.  Refer
     * to <code>javax.faces.application.Application.getProjectStage</code> and
     * <code>javax.faces.application.ProjectStage</code>.
     * @function jsf.getProjectStage
     */
    jsf.getProjectStage = function() {
        // First, return cached value if available
        if (typeof mojarra !== 'undefined' && typeof mojarra.projectStageCache !== 'undefined') {
            return mojarra.projectStageCache;
        }
        var scripts = document.getElementsByTagName("script"); // nodelist of scripts
        var script; // jsf.js script
        var s = 0; // incremental variable for for loop
        var stage; // temp value for stage
        var match; // temp value for match
        while (s < scripts.length) {
            if (typeof scripts[s].src === 'string' && scripts[s].src.match('\/javax\.faces\.resource\/jsf\.js\?.*ln=javax\.faces')) {
                script = scripts[s].src;
                break;
            }
            s++;
        }
        if (typeof script == "string") {
            match = script.match("stage=(.*)");
            if (match) {
                stage = match[1];
            }
        }
        if (typeof stage === 'undefined' || !stage) {
            stage = "Production";
        }

        mojarra = mojarra || {};
        mojarra.projectStageCache = stage;

        return mojarra.projectStageCache;
    };


    /**
     * <p>Collect and encode state for input controls associated
     * with the specified <code>form</code> element.  This will include
     * all input controls of type <code>hidden</code>.</p>
     * <p><b>Usage:</b></p>
     * <pre><code>
     * var state = jsf.getViewState(form);
     * </pre></code>
     *
     * @param form The <code>form</code> element whose contained
     * <code>input</code> controls will be collected and encoded.
     * Only successful controls will be collected and encoded in
     * accordance with: <a href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.2">
     * Section 17.13.2 of the HTML Specification</a>.
     *
     * @returns String The encoded state for the specified form's input controls.
     * @function jsf.getViewState
     */
    jsf.getViewState = function(form) {
        if (!form) {
            throw new Error("jsf.getViewState:  form must be set");
        }
        var els = form.elements;
        var len = els.length;
        // create an array which we'll use to hold all the intermediate strings
        // this bypasses a problem in IE when repeatedly concatenating very
        // large strings - we'll perform the concatenation once at the end
        var qString = [];
        var addField = function(name, value) {
            var tmpStr = "";
            if (qString.length > 0) {
                tmpStr = "&";
            }
            tmpStr += encodeURIComponent(name) + "=" + encodeURIComponent(value);
            qString.push(tmpStr);
        };
        for (var i = 0; i < len; i++) {
            var el = els[i];
            if (el.name === "") {
                continue;
            }
            if (!el.disabled) {
                switch (el.type) {
                    case 'submit':
                    case 'reset':
                    case 'image':
                    case 'file':
                        break;
                    case 'select-one':
                        if (el.selectedIndex >= 0) {
                            addField(el.name, el.options[el.selectedIndex].value);
                        }
                        break;
                    case 'select-multiple':
                        for (var j = 0; j < el.options.length; j++) {
                            if (el.options[j].selected) {
                                addField(el.name, el.options[j].value);
                            }
                        }
                        break;
                    case 'checkbox':
                    case 'radio':
                        if (el.checked) {
                            addField(el.name, el.value || 'on');
                        }
                        break;
                    default:
                        // this is for any input incl.  text', 'password', 'hidden', 'textarea'
                        var nodeName = el.nodeName.toLowerCase();
                        if (nodeName === "input" || nodeName === "select" ||
                            nodeName === "button" || nodeName === "object" ||
                            nodeName === "textarea") {                                 
                            addField(el.name, el.value);
                        }
                        break;
                }
            }
        }
        // concatenate the array
        return qString.join("");
    };

    /**
     * <p class="changed_added_2_2">Return the windowId of the window
     * in which the argument form is rendered.</p>

     * @param {optional String|DomNode} node. Determine the nature of
     * the argument.  If not present, search for the windowId within
     * <code>document.forms</code>.  If present and the value is a
     * string, assume the string is a DOM id and get the element with
     * that id and start the search from there.  If present and the
     * value is a DOM element, start the search from there.

     * @returns String The windowId of the current window, or null 
     *  if the windowId cannot be determined.

     * @throws an error if more than one unique WindowId is found.

     * @function jsf.getClientWindow
     */
    jsf.getClientWindow = function(node) {
        var FORM = "form";
        var WIN_ID = "javax.faces.ClientWindow";

        /**
         * Find javax.faces.ClientWindow field for a given form.
         * @param form
         * @ignore
         */
        var getWindowIdElement = function getWindowIdElement(form) {
            var windowIdElement = form[WIN_ID];

            if (windowIdElement) {
                return windowIdElement;
            } else {
                var formElements = form.elements;
                for (var i = 0, length = formElements.length; i < length; i++) {
                    var formElement = formElements[i];
                    if (formElement.name && (formElement.name.indexOf(WIN_ID) >= 0)) {
                        return formElement;
                    }
                }
            }

            return undefined;
        };

        var fetchWindowIdFromForms = function (forms) {
            var result_idx = {};
            var result;
            var foundCnt = 0;
            for (var cnt = forms.length - 1; cnt >= 0; cnt--) {
                var UDEF = 'undefined';
                var currentForm = forms[cnt];
                var windowIdElement = getWindowIdElement(currentForm);
                var windowId = windowIdElement && windowIdElement.value;
                if (UDEF != typeof windowId) {
                    if (foundCnt > 0 && UDEF == typeof result_idx[windowId]) throw Error("Multiple different windowIds found in document");
                    result = windowId;
                    result_idx[windowId] = true;
                    foundCnt++;
                }
            }
            return result;
        }

        /**
         * @ignore
         */
        var getChildForms = function (currentElement) {
            //Special condition no element we return document forms
            //as search parameter, ideal would be to
            //have the viewroot here but the frameworks
            //can deal with that themselves by using
            //the viewroot as currentElement
            if (!currentElement) {
                return document.forms;
            }
            
            var targetArr = [];
            if (!currentElement.tagName) return [];
            else if (currentElement.tagName.toLowerCase() == FORM) {
                targetArr.push(currentElement);
                return targetArr;
            }
            
            //if query selectors are supported we can take
            //a non recursive shortcut
            if (currentElement.querySelectorAll) {
                return currentElement.querySelectorAll(FORM);
            }
            
            //old recursive way, due to flakeyness of querySelectorAll
            for (var cnt = currentElement.childNodes.length - 1; cnt >= 0; cnt--) {
                var currentChild = currentElement.childNodes[cnt];
                targetArr = targetArr.concat(getChildForms(currentChild, FORM));
            }
            return targetArr;
        }
        
        /**
         * @ignore
         */
        var fetchWindowIdFromURL = function () {
            var href = window.location.href;
            var windowId = "windowId";
            var regex = new RegExp("[\\?&]" + windowId + "=([^&#\\;]*)");
            var results = regex.exec(href);
            //initial trial over the url and a regexp
            if (results != null) return results[1];
            return null;
        }
        
        //byId ($)
        var finalNode = (node && (typeof node == "string" || node instanceof String)) ?
            document.getElementById(node) : (node || null);
        
        var forms = getChildForms(finalNode);
        var result = fetchWindowIdFromForms(forms);
        return (null != result) ? result : fetchWindowIdFromURL();
        

    };

    /**
     * <p class="changed_added_2_3">
     * The Push functionality.
     * </p>
     * @name jsf.push
     * @namespace
     * @exec
     */
    jsf.push = (function(window) {

        // "Constant" fields ----------------------------------------------------------------------------------------------

        var URL_PROTOCOL = window.location.protocol.replace("http", "ws") + "//";
        var RECONNECT_INTERVAL = 500;
        var MAX_RECONNECT_ATTEMPTS = 25;
        var REASON_EXPIRED = "Expired";
        var REASON_UNKNOWN_CHANNEL = "Unknown channel";

        // Private static fields ------------------------------------------------------------------------------------------

        var sockets = {};
        var self = {};

        // Private constructor functions ----------------------------------------------------------------------------------

        /**
         * Creates a reconnecting websocket. When the websocket successfully connects on first attempt, then it will
         * automatically reconnect on timeout with cumulative intervals of 500ms with a maximum of 25 attempts (~3 minutes).
         * The <code>onclose</code> function will be called with the error code of the last attempt.
         * @constructor
         * @param {string} url The URL of the websocket.
         * @param {string} channel The channel name of the websocket.
         * @param {function} onopen The function to be invoked when the websocket is opened.
         * @param {function} onmessage The function to be invoked when a message is received.
         * @param {function} onclose The function to be invoked when the websocket is closed.
         * @param {Object} behaviors Client behavior functions to be invoked when specific message is received.
         */
        function ReconnectingWebsocket(url, channel, onopen, onmessage, onclose, behaviors) {

            // Private fields -----------------------------------------------------------------------------------------

            var socket;
            var reconnectAttempts;
            var self = this;

            // Public functions ---------------------------------------------------------------------------------------

            /**
             * Opens the reconnecting websocket.
             */
            self.open = function() {
                if (socket && socket.readyState == 1) {
                    return;
                }

                socket = new WebSocket(url);

                socket.onopen = function(event) {
                    if (reconnectAttempts == null) {
                        onopen(channel);
                    }

                    reconnectAttempts = 0;
                }

                socket.onmessage = function(event) {
                    var message = JSON.parse(event.data).data;
                    onmessage(message, channel, event);
                    var functions = behaviors[message];

                    if (functions && functions.length) {
                        for (var i = 0; i < functions.length; i++) {
                            functions[i]();
                        }
                    }
                }

                socket.onclose = function(event) {
                    if (!socket
                            || (event.code == 1000 && event.reason == REASON_EXPIRED)
                            || (event.code == 1008 || event.reason == REASON_UNKNOWN_CHANNEL) // Older IE versions incorrectly return 1005 instead of 1008, hence the fallback check on the message.
                            || (reconnectAttempts == null)
                            || (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS))
                    {
                        onclose(event.code, channel, event);
                    }
                    else {
                        setTimeout(self.open, RECONNECT_INTERVAL * reconnectAttempts++);
                    }
                }
            }

            /**
             * Closes the reconnecting websocket.
             */
            self.close = function() {
                if (socket) {
                    var s = socket;
                    socket = null;
                    reconnectAttempts == null;
                    s.close();
                }
            }

        }

        // Public static functions ----------------------------------------------------------------------------------------

        /**
         * Initialize a websocket on the given client identifier. When connected, it will stay open and reconnect as
         * long as URL is valid and <code>jsf.push.close()</code> hasn't explicitly been called on the same client
         * identifier.
         * @param {string} clientId The client identifier of the websocket.
         * @param {string} url The URL of the websocket. All open websockets on the same URL will receive the
         * same push notification from the server.
         * @param {string} channel The channel name of the websocket.
         * @param {function} onopen The JavaScript event handler function that is invoked when the websocket is opened.
         * The function will be invoked with one argument: the client identifier.
         * @param {function} onmessage The JavaScript event handler function that is invoked when a message is received from
         * the server. The function will be invoked with three arguments: the push message, the client identifier and
         * the raw <code>MessageEvent</code> itself.
         * @param {function} onclose The JavaScript event handler function that is invoked when the websocket is closed.
         * The function will be invoked with three arguments: the close reason code, the client identifier and the raw
         * <code>CloseEvent</code> itself. Note that this will also be invoked on errors and that you can inspect the
         * close reason code if an error occurred and which one (i.e. when the code is not 1000). See also
         * <a href="http://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455 section 7.4.1</a> and
         * <a href="http://docs.oracle.com/javaee/7/api/javax/websocket/CloseReason.CloseCodes.html">CloseCodes</a> API
         * for an elaborate list.
         * @param {Object} behaviors Client behavior functions to be invoked when specific message is received.
         * @param {boolean} autoconnect Whether or not to automatically connect the socket. Defaults to <code>false</code>.
         * @member jsf.push
         * @function jsf.push.init
         */
        self.init = function(clientId, url, channel, onopen, onmessage, onclose, behaviors, autoconnect) {
            onclose = resolveFunction(onclose);

            if (!window.WebSocket) { // IE6-9.
                onclose(-1, clientId);
                return;
            }

            if (!sockets[clientId]) {
                sockets[clientId] = new ReconnectingWebsocket(url, channel, resolveFunction(onopen), resolveFunction(onmessage), onclose, behaviors);
            }

            if (autoconnect) {
                self.open(clientId);
            }
        }

        /**
         * Open the websocket on the given client identifier.
         * @param {string} clientId The client identifier of the websocket.
         * @throws {Error} When client identifier is unknown. You may need to initialize it first via <code>init()</code> function.
         * @member jsf.push
         * @function jsf.push.open
         */
        self.open = function(clientId) {
            getSocket(clientId).open();
        }

        /**
         * Close the websocket on the given client identifier.
         * @param {string} clientId The client identifier of the websocket.
         * @throws {Error} When client identifier is unknown. You may need to initialize it first via <code>init()</code> function.
         * @member jsf.push
         * @function jsf.push.close
         */
        self.close = function(clientId) {
            getSocket(clientId).close();
        }

        // Private static functions ---------------------------------------------------------------------------------------

        /**
         * If given function is actually not a function, then try to interpret it as name of a global function.
         * If it still doesn't resolve to anything, then return a NOOP function.
         * @param {Object} fn Can be function, or string representing function name, or undefined.
         * @return {function} The intented function, or a NOOP function when undefined.
         */
        function resolveFunction(fn) {
            return (typeof fn !== "function") && (fn = window[fn] || function(){}), fn;
        }

        /**
         * Get socket associated with given client identifier.
         * @param {string} clientId The client identifier of the websocket.
         * @return {Socket} Socket associated with given client identifier.
         * @throws {Error} When client identifier is unknown. You may need to initialize it first via <code>init()</code> function.
         */
        function getSocket(clientId) {
            var socket = sockets[clientId];

            if (socket) {
                return socket;
            }
            else {
                throw new Error("Unknown clientId: " + clientId);
            }
        }

        // Expose self to public ------------------------------------------------------------------------------------------

        return self;

    })(window);
    

    /**
     * The namespace for JavaServer Faces JavaScript utilities.
     * @name jsf.util
     * @namespace
     */
    jsf.util = {};

    /**
     * <p>A varargs function that invokes an arbitrary number of scripts.
     * If any script in the chain returns false, the chain is short-circuited
     * and subsequent scripts are not invoked.  Any number of scripts may
     * specified after the <code>event</code> argument.</p>
     *
     * @param source The DOM element that triggered this Ajax request, or an
     * id string of the element to use as the triggering element.
     * @param event The DOM event that triggered this Ajax request.  The
     * <code>event</code> argument is optional.
     *
     * @returns boolean <code>false</code> if any scripts in the chain return <code>false</code>,
     *  otherwise returns <code>true</code>
     * 
     * @function jsf.util.chain
     */
    jsf.util.chain = function(source, event) {

        if (arguments.length < 3) {
            return true;
        }

        // RELEASE_PENDING rogerk - shouldn't this be getElementById instead of null
        var thisArg = (typeof source === 'object') ? source : null;

        // Call back any scripts that were passed in
        for (var i = 2; i < arguments.length; i++) {

            var f = new Function("event", arguments[i]);
            var returnValue = f.call(thisArg, event);

            if (returnValue === false) {
                return false;
            }
        }
        return true;
        
    };

    /**
     * <p class="changed_added_2_2">The result of calling
     * <code>UINamingContainer.getNamingContainerSeparatorChar().</code></p>
     */
    jsf.separatorchar = '#{facesContext.namingContainerSeparatorChar}';

    /**
     * <p class="changed_added_2_3">
     * The result of calling <code>ExternalContext.getRequestContextPath()</code>.
     */
    jsf.contextpath = '#{facesContext.externalContext.requestContextPath}';

    /**
     * <p>An integer specifying the specification version that this file implements.
     * Its format is: rightmost two digits, bug release number, next two digits,
     * minor release number, leftmost digits, major release number.
     * This number may only be incremented by a new release of the specification.</p>
     */
    jsf.specversion = 23000;

    /**
     * <p>An integer specifying the implementation version that this file implements.
     * It's a monotonically increasing number, reset with every increment of
     * <code>jsf.specversion</code>
     * This number is implementation dependent.</p>
     */
    jsf.implversion = 3;


} //end if version detection block
/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 @project JSF Ajax Library
 @version 2.0
 @description This is the standard implementation of the JSF Ajax Library.
 */

/**
 * Register with OpenAjax
 */
if (typeof OpenAjax !== "undefined" &&
    typeof OpenAjax.hub.registerLibrary !== "undefined") {
    OpenAjax.hub.registerLibrary("mojarra", "www.sun.com", "1.0", null);
}

/**
 * @name mojarra
 * @namespace
 */

/*
 * Create our top level namespaces - mojarra
 */
var mojarra = mojarra || {};


/**
 * This function deletes any hidden parameters added
 * to the form by checking for a variable called 'adp'
 * defined on the form.  If present, this variable will
 * contain all the params added by 'apf'.
 *
 * @param f - the target form
 */
mojarra.dpf = function dpf(f) {
    var adp = f.adp;
    if (adp !== null) {
        for (var i = 0; i < adp.length; i++) {
            f.removeChild(adp[i]);
        }
    }
};

/*
 * This function adds any parameters specified by the
 * parameter 'pvp' to the form represented by param 'f'.
 * Any parameters added will be stored in a variable
 * called 'adp' and stored on the form.
 *
 * @param f - the target form
 * @param pvp - associative array of parameter
 *  key/value pairs to be added to the form as hidden input
 *  fields.
 */
mojarra.apf = function apf(f, pvp) {
    var adp = new Array();
    f.adp = adp;
    var i = 0;
    for (var k in pvp) {
        if (pvp.hasOwnProperty(k)) {
            var p = document.createElement("input");
            p.type = "hidden";
            p.name = k;
            p.value = pvp[k];
            f.appendChild(p);
            adp[i++] = p;
        }
    }
};

/*
 * This is called by command link and command button.  It provides
 * the form it is nested in, the parameters that need to be
 * added and finally, the target of the action.  This function
 * will delete any parameters added <em>after</em> the form
 * has been submitted to handle DOM caching issues.
 *
 * @param f - the target form
 * @param pvp - associative array of parameter
 *  key/value pairs to be added to the form as hidden input
 *  fields.
 * @param t - the target of the form submission
 */
mojarra.jsfcljs = function jsfcljs(f, pvp, t) {
    mojarra.apf(f, pvp);
    var ft = f.target;
    if (t) {
        f.target = t;
    }

    var input = document.createElement('input');
    input.type = 'submit';
    f.appendChild(input);
    input.click();
    f.removeChild(input);

    f.target = ft;
    mojarra.dpf(f);
};

/*
 * This is called by functions that need access to their calling
 * context, in the form of <code>this</code> and <code>event</code>
 * objects.
 *
 *  @param f the function to execute
 *  @param t this of the calling function
 *  @param e event of the calling function
 *  @return object that f returns
 */
mojarra.jsfcbk = function jsfcbk(f, t, e) {
    return f.call(t,e);
};

/*
 * This is called by the AjaxBehaviorRenderer script to
 * trigger a jsf.ajax.request() call.
 *
 *  @param s the source element or id
 *  @param e event of the calling function
 *  @param n name of the behavior event that has fired
 *  @param ex execute list
 *  @param re render list
 *  @param op options object
 */
mojarra.ab = function ab(s, e, n, ex, re, op) {
    if (!op) {
        op = {};
    }

    if (n) {
        op["javax.faces.behavior.event"] = n;
    }

    if (ex) {
        op["execute"] = ex;
    }

    if (re) {
        op["render"] = re;
    }

    jsf.ajax.request(s, e, op);
};

/*
 * This is called by command script when autorun=true.
 * 
 * @param l window onload callback function
 */
mojarra.l = function l(l) {
    if (document.readyState === "complete") {
        setTimeout(l);
    }
    else if (window.addEventListener) {
        window.addEventListener("load", l, false);
    }
    else if (window.attachEvent) {
        window.attachEvent("onload", l);
    }
    else if (typeof window.onload === "function") {
        var oldListener = window.onload;
        window.onload = function() { oldListener(); l(); };
    }
    else {
        window.onload = l;
    }
};
