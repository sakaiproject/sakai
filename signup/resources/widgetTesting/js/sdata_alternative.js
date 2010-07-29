if (typeof XMLHttpRequest === "undefined") 
{
	if (window.ActiveXObject) {
		XMLHttpRequest = function () {
			return new ActiveXObject(navigator.userAgent.indexOf("MSIE 5") >= 0 ?
			"Microsoft.XMLHTTP" : "Msxml2.XMLHTTP");
		};
	} 
	
}

jQuery = $;

//document.body.style.display = "none";
document.body.style.display = "";


var SDATA_IGNORE_JS_LIB = {
			"lib": 1
};
		
var SDATA_DEMOSITES = {
			"dev": 1
};

/**
 * @fileoverview
 * SData provides a name space that interfaces to the SData REST API's provided on the server
 * The aim of this namespace is to make it easier to write widgets
 * 
 */

/**
 * 
 * Mechanism that will be used for registering widgets
 * 
 */

var sdata = {};
var sakai = {};

sdata.registerForLoad = function(id){
	sdata.toLoad[sdata.toLoad.length] = id;
	if (sdata.readyToLoad){
		sdata.performLoad();
	}
}

sdata.performLoad = function(){
	for (var i = 0; i < sdata.toLoad.length; i++){
		var fct = eval(sdata.toLoad[i]);
		try {
			fct();
		} catch (err){}
	}
	sdata.toLoad = []
}

sdata.readyToLoad = false;
sdata.toLoad = [];

/**
 * 
 * Easy form binding
 * 
 */

sdata.FormBinder = {};

sdata.FormBinder.serialize = function(form){
	var ret = {};

	// Input fields
	var fields = $("input", form);

	// Text fields
	for (var i = 0; i < fields.length; i++){
		var el = fields[i];
		if (el.name) {
			if (el.type.toLowerCase() == "text") {
				var name = el.name;
				var value = el.value;
				ret[name] = value;
			}
		}
	}

	// Checkboxes
	var chkboxesnames = [];
	for (var i = 0; i < fields.length; i++){
		var el = fields[i];
		if (el.name) {
			if (el.type.toLowerCase() == "checkbox") {
				var name = el.name;
				var exists = false;
				for (var ii = 0; ii < chkboxesnames.length; ii++) {
					if (name == chkboxesnames[ii]) {
						exists = true;
					}
				}
				if (exists == false) {
					chkboxesnames[chkboxesnames.length] = name
				}
			}
		}
	}
	for (var ii = 0; ii < chkboxesnames.length; ii++){
		var name = chkboxesnames[ii];
		var checkdones = [];
		for (var i = 0; i < fields.length; i++){
			var el = fields[i];
			if (el.type.toLowerCase() == "checkbox"){
				if (el.name == name && el.checked){
					checkdones[checkdones.length] = el.value;
				}
			}
		}
		ret[name] = checkdones;
	}

	// Radio buttons
	var radionames = [];
	for (var i = 0; i < fields.length; i++){
		var el = fields[i];
		if (el.name) {
			if (el.type.toLowerCase() == "radio") {
				var name = el.name;
				var exists = false;
				for (var ii = 0; ii < radionames.length; ii++) {
					if (name == radionames[ii]) {
						exists = true;
					}
				}
				if (exists == false) {
					radionames[radionames.length] = name
				}
			}
		}
	}
	for (var ii = 0; ii < radionames.length; ii++){
		var name = radionames[ii];
		var selected = null;
		for (var i = 0; i < fields.length; i++){
			var el = fields[i];
			if (el.type.toLowerCase() == "radio"){
				if (el.name == name && el.checked){
					selected = el.value;
				}
			}
		}
		ret[name] = selected;
	}

	// Select box
	fields = $("select", form);
	for (var i = 0; i < fields.length; i++){
		var el = fields[i];
		if (el.name) {
			var name = el.name;
			var selected = [];
			for (var ii = 0; ii < el.options.length; ii++) {
				if (el.options[ii].selected) {
					selected[selected.length] = el.options[ii].value;
				}
			}
			ret[name] = selected;
		}
	}

	// Textarea
	fields = $("textarea", form);
	for (var i = 0; i < fields.length; i++){
		if (el.name) {
			var el = fields[i];
			var name = el.name;
			var val = el.value;
			ret[name] = val;
		}
	}

	return ret;
}

sdata.FormBinder.deserialize = function(form, json){
	//i will be the name of the field
	var fields1 = $("input", form);
	for (var ii = 0; ii < fields1.length; ii++){
		var el = fields1[ii];
		if (el.type.toLowerCase() == "checkbox" || el.type.toLowerCase() == "radio"){
			el.checked = false;
		}
	}
	var fields2 = $("select", form);
	for (var ii = 0; ii < fields2.length; ii++){
		var el = fields2[ii];
		for (var iii = 0; iii < el.options.length; iii++){
			el.options[iii].selected = false;
		}
	}
	var fields3 = $("textarea", form);
	for (var i in json){
		for (var ii = 0; ii < fields1.length; ii++){
			var el = fields1[ii];
			if (el.name == i){
				//Text field
				if (el.type.toLowerCase() == "text"){
					el.value = json[i];
				}			

				//Checkbox
				if (el.type.toLowerCase() == "checkbox"){
					for (var iii = 0; iii < json[i].length; iii++){
						if (el.value == json[i][iii]){
							el.checked = true;
						}
					}
				}

				//Radio button
				if (el.type.toLowerCase() == "radio"){
					if (el.value == json[i]){
						el.checked = true;
					}
				}
			}
		}

		//Select
		for (var ii = 0; ii < fields2.length; ii++){
			var el = fields2[ii];
			if (el.name == i){
				for (var iii = 0; iii < el.options.length; iii++){
					for (var iiii = 0; iiii < json[i].length; iiii++){
						if (json[i][iiii] == el.options[iii].value){
							el.options[iii].selected = true;
						}
					}
				}
			}
		}

		//Textarea
		for (var ii = 0; ii < fields3.length; ii++){
			var el = fields3[ii];
			if (el.name == i){
				el.value = json[i];			
			}
		}
	}
}

/**
 * 
 * JSON Functions for serializing and deserializing
 * 
 */

sdata.JSON = function () {

        function f(n) {
            // Format integers to have at least two digits.
            return n < 10 ? '0' + n : n;
        }

        Date.prototype.toJSON = function (key) {

            return this.getUTCFullYear()   + '-' +
                 f(this.getUTCMonth() + 1) + '-' +
                 f(this.getUTCDate())      + 'T' +
                 f(this.getUTCHours())     + ':' +
                 f(this.getUTCMinutes())   + ':' +
                 f(this.getUTCSeconds())   + 'Z';
        };

        String.prototype.toJSON =
        Number.prototype.toJSON =
        Boolean.prototype.toJSON = function (key) {
            return this.valueOf();
        };

        var cx = /[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
            escapeable = /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
            gap,
            indent,
            meta = {    // table of character substitutions
                '\b': '\\b',
                '\t': '\\t',
                '\n': '\\n',
                '\f': '\\f',
                '\r': '\\r',
                '"' : '\\"',
                '\\': '\\\\'
            },
            rep;


        function quote(string) {

// If the string contains no control characters, no quote characters, and no
// backslash characters, then we can safely slap some quotes around it.
// Otherwise we must also replace the offending characters with safe escape
// sequences.

            escapeable.lastIndex = 0;
            return escapeable.test(string) ?
                '"' + string.replace(escapeable, function (a) {
                    var c = meta[a];
                    if (typeof c === 'string') {
                        return c;
                    }
                    return '\\u' + ('0000' +
                            (+(a.charCodeAt(0))).toString(16)).slice(-4);
                }) + '"' :
                '"' + string + '"';
        }


        function str(key, holder) {

// Produce a string from holder[key].

            var i,          // The loop counter.
                k,          // The member key.
                v,          // The member value.
                length,
                mind = gap,
                partial,
                value = holder[key];

// If the value has a toJSON method, call it to obtain a replacement value.

            if (value && typeof value === 'object' &&
                    typeof value.toJSON === 'function') {
                value = value.toJSON(key);
            }

// If we were called with a replacer function, then call the replacer to
// obtain a replacement value.

            if (typeof rep === 'function') {
                value = rep.call(holder, key, value);
            }

// What happens next depends on the value's type.

            switch (typeof value) {
            case 'string':
                return quote(value);

            case 'number':

// JSON numbers must be finite. Encode non-finite numbers as null.

                return isFinite(value) ? String(value) : 'null';

            case 'boolean':
            case 'null':

// If the value is a boolean or null, convert it to a string. Note:
// typeof null does not produce 'null'. The case is included here in
// the remote chance that this gets fixed someday.

                return String(value);

// If the type is 'object', we might be dealing with an object or an array or
// null.

            case 'object':

// Due to a specification blunder in ECMAScript, typeof null is 'object',
// so watch out for that case.

                if (!value) {
                    return 'null';
                }

// Make an array to hold the partial results of stringifying this object value.

                gap += indent;
                partial = [];

// If the object has a dontEnum length property, we'll treat it as an array.

                if (typeof value.length === 'number' &&
                        !(value.propertyIsEnumerable('length'))) {

// The object is an array. Stringify every element. Use null as a placeholder
// for non-JSON values.

                    length = value.length;
                    for (i = 0; i < length; i += 1) {
                        partial[i] = str(i, value) || 'null';
                    }

// Join all of the elements together, separated with commas, and wrap them in
// brackets.

                    v = partial.length === 0 ? '[]' :
                        gap ? '[\n' + gap +
                                partial.join(',\n' + gap) + '\n' +
                                    mind + ']' :
                              '[' + partial.join(',') + ']';
                    gap = mind;
                    return v;
                }

// If the replacer is an array, use it to select the members to be stringified.

                if (rep && typeof rep === 'object') {
                    length = rep.length;
                    for (i = 0; i < length; i += 1) {
                        k = rep[i];
                        if (typeof k === 'string') {
                            v = str(k, value);
                            if (v) {
                                partial.push(quote(k) + (gap ? ': ' : ':') + v);
                            }
                        }
                    }
                } else {

// Otherwise, iterate through all of the keys in the object.

                    for (k in value) {
                        if (Object.hasOwnProperty.call(value, k)) {
                            v = str(k, value);
                            if (v) {
                                partial.push(quote(k) + (gap ? ': ' : ':') + v);
                            }
                        }
                    }
                }

// Join all of the member texts together, separated with commas,
// and wrap them in braces.

                v = partial.length === 0 ? '{}' :
                    gap ? '{\n' + gap + partial.join(',\n' + gap) + '\n' +
                            mind + '}' : '{' + partial.join(',') + '}';
                gap = mind;
                return v;
            }
        }

// Return the JSON object containing the stringify and parse methods.

        return {
            stringify: function (value, replacer, space) {

// The stringify method takes a value and an optional replacer, and an optional
// space parameter, and returns a JSON text. The replacer can be a function
// that can replace values, or an array of strings that will select the keys.
// A default replacer method can be provided. Use of the space parameter can
// produce text that is more easily readable.

                var i;
                gap = '';
                indent = '';

// If the space parameter is a number, make an indent string containing that
// many spaces.

                if (typeof space === 'number') {
                    for (i = 0; i < space; i += 1) {
                        indent += ' ';
                    }

// If the space parameter is a string, it will be used as the indent string.

                } else if (typeof space === 'string') {
                    indent = space;
                }

// If there is a replacer, it must be a function or an array.
// Otherwise, throw an error.

                rep = replacer;
                if (replacer && typeof replacer !== 'function' &&
                        (typeof replacer !== 'object' ||
                         typeof replacer.length !== 'number')) {
                    throw new Error('JSON.stringify');
                }

// Make a fake root object containing our value under the key of ''.
// Return the result of stringifying the value.

                return str('', {'': value});
            },


            parse: function (text, reviver) {

// The parse method takes a text and an optional reviver function, and returns
// a JavaScript value if the text is a valid JSON text.

                var j;

                function walk(holder, key) {

// The walk method is used to recursively walk the resulting structure so
// that modifications can be made.

                    var k, v, value = holder[key];
                    if (value && typeof value === 'object') {
                        for (k in value) {
                            if (Object.hasOwnProperty.call(value, k)) {
                                v = walk(value, k);
                                if (v !== undefined) {
                                    value[k] = v;
                                } else {
                                    delete value[k];
                                }
                            }
                        }
                    }
                    return reviver.call(holder, key, value);
                }


// Parsing happens in four stages. In the first stage, we replace certain
// Unicode characters with escape sequences. JavaScript handles many characters
// incorrectly, either silently deleting them, or treating them as line endings.

                cx.lastIndex = 0;
                if (cx.test(text)) {
                    text = text.replace(cx, function (a) {
                        return '\\u' + ('0000' +
                                (+(a.charCodeAt(0))).toString(16)).slice(-4);
                    });
                }

// In the second stage, we run the text against regular expressions that look
// for non-JSON patterns. We are especially concerned with '()' and 'new'
// because they can cause invocation, and '=' because it can cause mutation.
// But just to be safe, we want to reject all unexpected forms.

// We split the second stage into 4 regexp operations in order to work around
// crippling inefficiencies in IE's and Safari's regexp engines. First we
// replace the JSON backslash pairs with '@' (a non-JSON character). Second, we
// replace all simple value tokens with ']' characters. Third, we delete all
// open brackets that follow a colon or comma or that begin the text. Finally,
// we look to see that the remaining characters are only whitespace or ']' or
// ',' or ':' or '{' or '}'. If that is so, then the text is safe for eval.

                if (/^[\],:{}\s]*$/.
test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, '@').
replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']').
replace(/(?:^|:|,)(?:\s*\[)+/g, ''))) {

// In the third stage we use the eval function to compile the text into a
// JavaScript structure. The '{' operator is subject to a syntactic ambiguity
// in JavaScript: it can begin a block or an object literal. We wrap the text
// in parens to eliminate the ambiguity.

                    j = eval('(' + text + ')');

// In the optional fourth stage, we recursively walk the new structure, passing
// each name/value pair to a reviver function for possible transformation.

                    return typeof reviver === 'function' ?
                        walk({'': j}, '') : j;
                }

// If the text is not JSON parseable, then a SyntaxError is thrown.

                throw new SyntaxError('JSON.parse');
            }
        };
}();
 
/**
 * @static
 * @class Ajax Loader
 */  
sdata.Ajax =  {
	

	get_response_cache : {}, 			
	/**
	 * <pre>
	 * Sends a Ajax Request to the server based on the options passed in.
	 * options.httpMethod is the standard method (string default GET)
	 * options.url is the URL to send to (string required)
	 * options.onFail is the function that is invoked on a failed request
	 * options.onSuccess is the function that is invokec on sucess
	 * options.onComplete is called when the request is complete (fail or success)
	 * options.onTimeout is called when the requests timesout
	 * options.timeout is the timeout in ms (default 30000ms)
	 * options.responseType is the response type (default text)
	 * 		text : just return the text
	 *      xml : return a dom
	 *      script : run the script against the window
	 *      If there is anything that indicates XML in the content type a DOM will be returned.
	 * 
	 * options.sync true of synchronouse, false if async (default false)
	 * options.contentType the content type of the POST, if a post (default text/plain)
	 * options.postData an array of data 
	 * options.getData a fucntion to get the data to be sent in the body.
	 * 
	 * GET,HEAD.DELETE
	 * If the options.httpMethod is a GET,HEAD,DELETE then the url is invoked with no body
	 * 
	 * PUT
	 * If getData is defined, this is used to retrieve the body of the post, if that is not 
	 * set postData is used.
	 * 
	 * POST
	 * If getData is defined, this is used, otherwise, getData is assumed to be an array.
	 * If the form is url encoded, then postData should be a name value array.
	 * If the form is multipart, then the postData should be a name value array, where 
	 * the value of the array is an object
	 * The name of the element in the array is used as the form element name.
	 * value.fileName is used as a filename for the form element
	 * value.contentType is used as the content type for the element
	 * value.data is the data for the element
	 * 
	 * The fucntion will not perform file uplaods from a file input form, for that you should use
	 * SWFUpload and do it all via flash.
	 * </pre>
	 * 
	 * 
	 * 	  
	 * @param options a structure of options
	 * @static 
	 */
	request : function (options) {
		/**
		 * The options structure with defaults
		 */
		var opt = {
			httpMethod : options.httpMethod || "GET",
			url : options.url || "",
			onFail : options.onFail || function () {},
			onSuccess : options.onSuccess || function () {},
			onComplete : options.onComplete || function () {},
			onTimeout : options.onTimeout || function () {},
			sync : !options.async || false,
			contentType : options.contentType || "text/plain",
			postData : options.postData || null,
			getData : options.getData || null,
			timepout : options.timeout || 30000,
			responseType : options.responseType || "text",
			sendToLoginOnFail : options.sendToLoginOnFail || "true",
			useCache : options.useCache || "false"
		};
		/**
		 * is the response Ok
		 * @private
		 */
		function httpOk(response) {
			try {
				try {
					if ((response.status == 401 || response.status == 403) && opt.sendToLoginOnFail == "true"){
						/*
if (response.getAllResponseHeaders().indexOf("/p/widgets/loggedIn") == -1 && response.getAllResponseHeaders().indexOf("/p/widgets/search") == -1){
							var redirect = document.location;
							var redirectURL = sdata.util.URL.encode(redirect.pathname + redirect.search + redirect.hash);
							document.location = "/dev/index.html?url=" + redirectURL;
						}
*/

						if (response.getAllResponseHeaders().indexOf("/p/widgets/loggedIn") == -1 && response.getAllResponseHeaders().indexOf("/p/widgets/search") == -1) {
						
						var _resp = response.getAllResponseHeaders();
						
							var decideLoggedIn = function(response, exists){
								var redirect = document.location;
								var redirectURL = sdata.util.URL.encode(redirect.pathname + redirect.search + redirect.hash);
								var redirecturl = "/dev/index.html?url=" + redirectURL;
								if (exists == false) {
									if (response == "401" || response == "403" || response == "error") {
										//alert(_resp);
										document.location = redirecturl;
									}
								}
								/*
								else {
									document.location = redirecturl;
								}*/
							}
							
							sdata.widgets.WidgetPreference.get("loggedIn", decideLoggedIn, true);
							
						}
						
					}
				} catch (err){}
				return !response.status && location.protocol === "file:" ||
				(response.status >= 200 && response.status < 300) ||
				(response.status === 304) ||
				(navigator.userAgent.indexOf("Safari") >= 0 && 
					typeof response.status === "undefined");
			} catch (e) {
				
			}
			return false;
		};
		/**
		 * get the response data and if a script, run the script
		 * @private
		 */
		function getResponseData(response, responseType) {
		    var rct = response.getResponseHeader("content-type");
		    var data = !responseType && rct && rct.indexOf("xml") >= 0;
		    data = responseType === "xml" || data ? response.responseXML : response.responseText;
		    if (responseType === "script") {
		 	   eval.call(window, data);
		    }
		    else if (responseType === 'json' && data) {
		    	data = eval('(' + data + ')');
		    }
		    return data;
		};
		
		if ( opt.useCache && opt.httpMethod === "GET" &&  sdata.Ajax.get_response_cache[opt.url] ) {
			options.onSuccess(sdata.Ajax.get_response_cache[opt.url]);
			return;
		}
		
		
		
		try {		
			var sdata_xmlHttp = new XMLHttpRequest();
			//sdata_xmlHttp.setRequestHeader("x-ajax-client", "SData Client 1.0");
			//sdata_xmlHttp.setRequestHeader("sdata-url", url);
			sdata_xmlHttp.open(opt.httpMethod, opt.url, true);
			//
			/**
			 * A timeout function
			 */
			
			var requestTimeout = false;
			//setTimeout("try { timeo(); } catch (err) {}", opt.timeout);
			
			/**
			 * the on ready event
			 */
		    sdata_xmlHttp.onreadystatechange = function () {
				if (sdata_xmlHttp.readyState === 4 && !requestTimeout) {
					//for (var l in sdata_xmlHttp){
					//	alert(l + " = " + sdata_xmlHttp[l]);
					//}
					if (httpOk(sdata_xmlHttp)) {
					   var responseData = getResponseData(sdata_xmlHttp, opt.responseType);
					   if ( opt.useCache && opt.httpMethod === "GET" ) {
					   		sdata.Ajax.get_response_cache[opt.url] = responseData;
					   }
				 	   opt.onSuccess(responseData);
					} else {
					    opt.onFail(sdata_xmlHttp.status);
					}
				    opt.onComplete();
				    sdata_xmlHttp = null;
				}
		    };
		    
			
			
			/**
			 * get the request body
			 */
			var out = [];
			var outputData = null;
			if (opt.httpMethod === "POST" || opt.httpMethod === "PUT") {
				if (opt.getData !== null) {
					outputData = opt.getData();
				} else if (opt.httpMethod === "POST" || opt.postData !== null) {
					if (opt.contentType === "application/x-www-form-urlencoded") {
						if (opt.postData.constructor === Array) {
							for ( var i = 0; i < opt.postData.length; i++ ) {
								out.push( opt.postData[i].name + "=" + encodeURIComponent(opt.postData[i].value));
							}		
						} else {
							for ( var j in opt.postData ) {
								var item = opt.postData[j];
								if ( item.constructor == Array ) {
									for ( var i = 0; i < item.length; i++ ) {
										out.push( j + "=" + encodeURIComponent(item[i]));
									}               
								} else {
									out.push(j+"="+encodeURIComponent(item));
								}
							}
						}
						outputData = out.join("&");
					} else if ( opt.contentType === "multipart/form-data" )  {
						if ( opt.postData.constructor === Array ) {
							for ( var k = 0; k < opt.postData.length; k++ ) {
								var name = opt.postData[k].name;
								var value = opt.postData[k].value;
								var fileName = value.fileName || null;
								var contentType = value.contentType || 'text/plain';
								if ( fileName !== null ) {
									fileName = ' filename="' + fileName + '"'; 
								}
								out.push(
									'\r\n'+ 
									'Content-Disposition: form-data; name="' + name + '";' + fileName + '\r\n'+ 
									'Content-Type: '+contentType+ '\r\n' +
									'\r\n'+
									value.data+
									'\r\n');
							}		
						} else {
							for ( var l in opt.postData ) {
								var fileName = opt.postData[l].fileName || null;
								var fileName2 = opt.postData[l].fileName || null;
								var contentType = opt.postData[l].contentType || 'text/plain';
								if ( fileName !== null ) {
									fileName = ' filename="' + fileName + '"'; 
								}
								out.push(
									'\r\n' +
									'Content-Disposition: form-data; name="' + fileName2 + '";' + fileName + '\r\n'+ 
									'Content-Type: '+contentType+ '\r\n' +
									'\r\n'+
									opt.postData[l].data+
									'\r\n');
							}
						}
						var boundaryString = "bound"+Math.floor(Math.random() * 9999999999999);
						var boundary = '--' + boundaryString;
						opt.contentType = opt.contentType +"; boundary=" + boundaryString + "";
						outputData = out.join(boundary) + "--";
	
						outputData = boundary + '\r\n' +
									'Content-Disposition: form-data; name="' + fileName2 + '";' + fileName + '\r\n'+ 
									'Content-Type: '+contentType+ '\r\n' +
									'\r\n'+
									opt.postData[l].data+
									'\r\n'+
									boundary + "--";
	
					} else {
						outputData = opt.postData;
					}
				} else {
					outputData = opt.postData;
				}
			}
			
			/**
			 * set the content type and send the request
			 */
			sdata_xmlHttp.setRequestHeader("Content-type",opt.contentType);
			if ( sdata_xmlHttp.overrideMimeType ) {
				// Mozilla browsers have problems with content length
				sdata_xmlHttp.setRequestHeader("Connection","Close");
				
			} else {
				if ( outputData !== null ) {		
		    		//sdata_xmlHttp.setRequestHeader("Content-length", outputData.length);
				}
			}
		
			sdata_xmlHttp.send(outputData);
		} catch ( e ) {
			alert("Failed to invoke ajax request to url=["+opt.url+"] exception "+e);
		}
		
	}
};


/**
 * @static 
 * @class Logger Class
 * @name sdata.Log
 */
sdata.Log =  {
	/**
	 * Appends a message to a an element with the id 'log' in the current document.
	 * @param msg the log message
	 * @static
	 * 
	 */
	info : function(msg) {
		var logWindow = document.getElementById('log');
		if ( logWindow ) {
			logWindow.innerHTML += "<br />"+msg;
		}
	},
	
	/**
	 * Clear the log element in the current dom, identified by id='log' 
	 */
	clear : function() {
		var logWindow = document.getElementById('log');
		if ( logWindow ) {
			logWindow.innerHTML = "";
		}
	}
};

	
sdata.events = {};

/**
 * @static 
 * @class Listener for managing events 
 * @name sdata.events.Listener
 */
sdata.events.Listener =  {
	/**
	 * Attach a function to the onload event, maintaining existing onload functions.
	 * @param {fucntion} func a function
	 */
	onLoad : function (func) {    
	    var oldonload = window.onload;
	    if (typeof window.onload != 'function')
	    {
	        window.onload = func;
	    } 
	    else 
	    {
	        window.onload = function()
	        {
	            oldonload();
	            func();
	        }
	    }
	}
};

sdata.widgets = {};

/**
 * Create a Loader Object to pull HTML for widgets
 * @constructor
 * @class 
 */
/*
sdata.widgets.Loader = function(){
	
		this.div = "";
		this.url = "";
		this.id = "";
		this.bundle = null;

};
*/
/**
 * initialise the widget
 * @param {Object} divName
 * @param {Object} loadurl
 */
/*
sdata.widgets.Loader.prototype.init = function(divName,loadurl) {
	this.div = divName;
	this.url = loadurl;	
	this.id =  Math.random();	
};
*/

/**
 * load the widget and bundles
 */
sdata.widgets.Loader = {};
sdata.widgets.Loader.load = function(id,url,div,response) {
	var obj = {};
	obj.id = id; obj.url = url; obj.div = div; obj.bundle = null;
	var thisobj = obj;
	var thisobj2 = thisobj;
	thisobj2.bundle = sdata.i18n.getBundles(response);
	sdata.i18n.process(response,function(i18nrespone) {
		sdata.widgets.WidgetLoader.sethtmlover(id.id,i18nrespone);                            
	},
	thisobj2.bundle,"default");			    				
};



/**
 * @static 
 * @class The Main Widget Loader
 * @name sdata.widgets.WidgetLoader
 */
sdata.widgets.WidgetLoader =  {

	mountable_widgets : [],
	bigarray : false,
	toload : {},
	showSettings : false,	

	informOnLoad : function(widgetname){
		try {
			sdata.widgets.WidgetLoader.toload[widgetname].done++;
			if (sdata.widgets.WidgetLoader.toload[widgetname].done == sdata.widgets.WidgetLoader.toload[widgetname].todo){
				var initfunction = eval('sakai.' + widgetname);
				for (var i = 0; i < sdata.widgets.WidgetLoader.bigarray[widgetname].length; i++){
					try {
						if (sdata.widgets.WidgetLoader.showSettings){
							var obj = initfunction(sdata.widgets.WidgetLoader.bigarray[widgetname][i].uid, sdata.widgets.WidgetLoader.bigarray[widgetname][i].placement, true);
						} else {
							var obj = initfunction(sdata.widgets.WidgetLoader.bigarray[widgetname][i].uid, sdata.widgets.WidgetLoader.bigarray[widgetname][i].placement);
						}
					} catch (err){ alert("Plaats 1 : " + err) }
				}
			}
		} catch (err){
			try {
				var initfunction = eval('sakai.' + widgetname);
				if (sdata.readyToLoad) {
					initfunction();
				} else {
					sdata.toLoad[sdata.toLoad.length] = initfunction;	
				}
			} catch (err){ alert (err)}
		}
	},

	/**
	 * Insert inline widgets into divs in the template
	 */
	insertWidgetsAdvanced : function(id, showSettings){

		//"widget_uid_" + rnd + "_" + currentsite.id + "_" + selectedpage.replace(/ /g,"%20");
		var divarray = sdata.widgets.WidgetLoader.getElementsByClassName("widget_inline", id);
		var bigarray = {};
		if (showSettings){
			sdata.widgets.WidgetLoader.showSettings = true;
		} else {
			sdata.widgets.WidgetLoader.showSettings = false;
		}
		for (var i = 0; i < divarray.length; i++){
			try {
				var id = divarray[i].id;
				var split = id.split("_");
				var widgetname = split[1];
				if (Widgets.widgets[widgetname] && Widgets.widgets[widgetname].iframe == 0){
					var widgetid = split[2];
					var placement = split[3];
					if (! bigarray[widgetname]){
						bigarray[widgetname] = [];
					}
					var index = bigarray[widgetname].length;
					bigarray[widgetname][index] = [];
					bigarray[widgetname][index].uid = widgetid;
					bigarray[widgetname][index].placement = placement;
					bigarray[widgetname][index].id = id;
					var float = "inline_class_widget_nofloat";
					if (divarray[i].style.cssFloat) {
						if (divarray[i].style.cssFloat == "left") {
							float = "inline_class_widget_leftfloat";
						}
						else 
							if (divarray[i].style.cssFloat == "right") {
								float = "inline_class_widget_rightfloat";
							}
					} else {
						if (divarray[i].style.styleFloat == "left") {
							float = "inline_class_widget_leftfloat";
						}
						else 
							if (divarray[i].style.styleFloat == "right") {
								float = "inline_class_widget_rightfloat";
							}
					}
					bigarray[widgetname][index].float = float;
				} else if (Widgets.widgets[widgetname] && Widgets.widgets[widgetname].iframe == 1) {
					
					var portlet = Widgets.widgets[widgetname];
					var html = '<div style="padding:0 0 0 0; border-left: 5px solid #EEEEEE; border-right: 5px solid #EEEEEE" allowtransparency="false" id="widget_content_'+ portlet.id+ '">' +
		    	   			'<iframe src="'+ portlet.url+'" ' +
		    	   			'id="widget_frame_'+ portlet.id+'" ' +
		    	   			'name="widget_frame_'+ portlet.id+'" ' +
		    	   			'frameborder="0" ' +
		    	   			'height="'+ portlet.height +'px" ' +
		    	   			'width="100%" ' +
		    	   			'scrolling="no"' +
		    	   			'></iframe></div>';
					
					document.getElementById(id).innerHTML = html;
					
				}
			} catch (err){alert("An error occurred NOW")};
		}

		for (var i in bigarray){
			try {
				for (var ii = 0; ii < bigarray[i].length; ii++){
					var el = document.getElementById(bigarray[i][ii].id);
					var newel = document.createElement("div");
					newel.id = bigarray[i][ii].uid;	
					newel.className = bigarray[i][ii].float;
					newel.innerHTML = "";
					el.parentNode.replaceChild(newel,el);
				}
			} catch(err){alert("Another error")};
		}

		sdata.widgets.WidgetLoader.bigarray = bigarray;

		var initialUrl = "/direct/batch?_refs=";
		
		for (var i in bigarray){
			initialUrl += Widgets.widgets[i].url + ","
		}
		
		var initialResponse = false;
		
		sdata.Ajax.request({
			url : initialUrl,
			onSuccess : function(response) {
				initialResponse = eval('(' + response + ')');
				var index = 0;
				for (var i in sdata.widgets.WidgetLoader.bigarray){
					if (initialResponse["ref" + index].status == 200) {
						var response = initialResponse["ref" + index].content;
						sdata.widgets.WidgetLoader.loadWidgetFiles(sdata.widgets.WidgetLoader.bigarray, i, response);
						index++;
					}
				}
					
			}
		});

	},

	loadWidgetFiles : function(bigarray,widgetname,response){
		var thisobj2 = {};
		thisobj2.bundle = sdata.i18n.getBundles(response);
		sdata.i18n.process(response,function(i18nrespone) {
			sdata.widgets.WidgetLoader.sethtmloverspecial(null,i18nrespone,bigarray,widgetname);                            
		},
		thisobj2.bundle,"default");		
	},

	sethtmloverspecial : function (div,content,bigarray,widgetname){
   
   		var anotherone = true;

		while (anotherone === true){
			
			var startscript = content.indexOf("<link");
   			var eindscript = content.indexOf("<\/link>");

			if (startscript !== -1 && eindscript !== -1){
   			
   				var linktag = content.substring(startscript, eindscript);
   				linktag = linktag.substring(linktag.indexOf("href=") + 6);
   				linktag = linktag.substring(0, linktag.indexOf("\""));
				
				if (linktag !== "/resources/css/ext-all.css"){
   			
   					var oScript = document.createElement('link');
  					oScript.setAttribute('rel','stylesheet');
  					oScript.setAttribute('type','text/css');
   					oScript.setAttribute('href',linktag);
   					document.getElementsByTagName("head").item(0).appendChild(oScript);
	
				}

				var tussencontent = content;
				content = tussencontent.substring(0, startscript);
				content += tussencontent.substring(eindscript + 7);
   			
   			} else {

				anotherone = false;

			}

		}

		anotherone = true;

		var scripttags = [];
		while (anotherone === true){
			
			startscript = content.indexOf("<script");
   			eindscript = content.indexOf("<\/script>");

			if (startscript !== -1 && eindscript !== -1){
   			
   				var linktag = content.substring(startscript, eindscript);
				
   				linktag = linktag.substring(linktag.indexOf("src=") + 5);
   				linktag = linktag.substring(0, linktag.indexOf("\""));

				if ( sdata.widgets.WidgetLoader.acceptJS(linktag) ) {
	   				var oScript = document.createElement('script');
	  				oScript.setAttribute('language','JavaScript');
	  				oScript.setAttribute('type','text/javascript');
	   				oScript.setAttribute('src',linktag);
			
					scripttags[scripttags.length] = oScript;									
				}
				
				var tussencontent = content;
				content = tussencontent.substring(0, startscript);
				content += tussencontent.substring(eindscript + 9);
   			
   			} else {

				anotherone = false;

			}

		}
		
		for (var ii = 0; ii < bigarray[widgetname].length; ii++){
			var newel = document.createElement("div");
			newel.innerHTML = content;
			document.getElementById(bigarray[widgetname][ii].uid).appendChild(newel);
		}
	
		sdata.widgets.WidgetLoader.toload[widgetname] = {};
		sdata.widgets.WidgetLoader.toload[widgetname].todo = scripttags.length;
		sdata.widgets.WidgetLoader.toload[widgetname].done = 0;
	
		//sdata.widgets.WidgetLoader.loadDelayScriptTags(scripttags, 0);		
	   	for (var iii = 0; iii < scripttags.length; iii++){
			document.getElementsByTagName("head").item(0).appendChild(scripttags[iii]);
		}	
			
	},
	
	loadDelayScriptTags : function(scripttags, id){
		if (id >= scripttags.length){
			
		} else {
			document.getElementsByTagName("head").item(0).appendChild(scripttags[id]);
			id = id + 1;
			setTimeout(function(){
				sdata.widgets.WidgetLoader.loadDelayScriptTags(scripttags, id);
			}, 750);
		}
	},
	
	loadScript : function(id){
		
	},

	/**
	 * Insert inline widgets into divs in the template
	 */
	insertWidgets : function(id){
		
		var divarray = sdata.widgets.WidgetLoader.getElementsByClassName("widget_inline", id);
		
		var items = [];
		
		for (var i = 0; i < divarray.length; i++){
			var portlet = Widgets.widgets[divarray[i].id.substring(7)];
			try {
	    		if ( portlet.url !== null ) {
					if ( portlet.iframe === 1 ) {
		    	   		var html = '<div style="padding:0 0 0 0" id="widget_content_'+ portlet.id+ '">' +
		    	   				'<iframe src="'+ portlet.url+'" ' +
		    	   				'id="widget_frame_'+ portlet.id+'" ' +
		    	   				'name="widget_frame_'+ portlet.id+'" ' +
		    	   				'frameborder="0" ' +
		    	   				'height="'+ portlet.height +'px" ' +
		    	   				'width="100%" ' +
		    	   				'scrolling="no"' +
		    	   				'></iframe></div>';
						document.getElementById(divarray[i].id).innerHTML = html;
		    	   } else {
				   		var index = items.length;
						items[index] = {};
						items[index].id = divarray[i];
						items[index].url = portlet.url;
						items[index].div = portlet.divid;
						
	    				/*
						var divName = portlet.divid;
	    				var loader = new sdata.widgets.Loader();
	    				loader.init(divarray[i].id,portlet.url);
	    				loader.load();	
						*/
					}    			
	    		}
			} catch (err){ 
				// alert(divarray[i].id + ' didn\'t find a portlet (' + err + ')')
			}
		}
		
		if (items.length > 0) {
			var url = "/direct/batch?_refs=";
			for (var i = 0; i < items.length; i++) {
				url += items[i].url + ",";
			}
			
			sdata.Ajax.request({
				url: url,
				httpMethod: "GET",
				onSuccess: function(data){
					var resp = eval('(' + data + ')');
					for (var _index = 0; _index < items.length; _index++){
						var response = resp["ref" + _index].content;
						sdata.widgets.Loader.load(items[_index].id,items[_index].url,items[_index].div,response);
					}
				},
				onFail: function(data){
					// Ignore, continue
				}
			});	
		}

		var divarray = sdata.widgets.WidgetLoader.getElementsByClassName("widget_mountable", id);
		for (var i = 0; i < divarray.length; i++){
			
			var portlet = Widgets.widgets[divarray[i].id.substring(7)];
			try {
	
				var button = Ext.get(divarray[i].id);
				if (!sdata.widgets.WidgetLoader.mountable_widgets[divarray[i].id.substring(7)]){
					sdata.widgets.WidgetLoader.mountable_widgets[divarray[i].id.substring(7)] = new Object();
				}

				var el = document.getElementById(divarray[i].id);

    			el.onclick = function() {
					var tid = this.id;

					var ihtml = "<div id='floatable_widget_" + tid.substring(7) + "'><img src='/resources/images/loader.gif'/></div>";
					var iheight = 400;
					var iwidth = 600;
					var ititle = "Widget"

					var portlet = Widgets.widgets[tid.substring(7)];
					try {
	    				if ( portlet.url !== null ) {
							ititle = portlet.name;
							if ( portlet.iframe === 1 ) {
		    	   				ihtml = '<div id="floatable_widget_' + tid.substring(7) + '">' +
										'<div style="padding:0 0 0 0" id="widget_content_'+ portlet.id+ '">' +
		    	   						'<iframe src="'+ portlet.url+'" ' +
		    	   						'id="widget_frame_'+ portlet.id+'" ' +
		    	   						'name="widget_frame_'+ portlet.id+'" ' +
		    	   						'frameborder="0" ' +
		    	   						'height="'+ portlet.height +'px" ' +
		    	   						'width="100%" ' +
		    	   						'scrolling="no"' +
		    	   						'></iframe></div></div>';	
								iheight = portlet.height + 70;	
							}
						}
					} catch (err){ }

        			if(!sdata.widgets.WidgetLoader.mountable_widgets[tid.substring(7)].win){
            			sdata.widgets.WidgetLoader.mountable_widgets[tid.substring(7)].win = new Ext.Window({
							title : ititle,
							header : true,
                			layout:'fit',
                			width: iwidth,
                			height: iheight,
                			closeAction:'hide',
                			plain: true,
							html : ihtml,
							border:true,
                    		bodyStyle:'position:relative',
                    		anchor:'100% 100%',
                    		overflow:'auto',
                    		fitToFrame:'true',
                    		autoScroll:'true',
							style:'background-color:#FFFFFF',
                    		defaults:{autoHeight:true,autoWidth:true,bodyStyle:'padding:10px'},
                
	                		buttons: [{
    	                		text: 'Close',
        	            		handler: function(){
            	            		sdata.widgets.WidgetLoader.mountable_widgets[tid.substring(7)].win.hide();
                	    		}
                			}]
            			});

						var portlet = Widgets.widgets[tid.substring(7)];
						try {
	    					if ( portlet.url !== null ) {
								if ( portlet.iframe === 0 ) {
	    							var loader = new sdata.widgets.Loader();
	    							loader.init("floatable_widget_" + tid.substring(7),portlet.url);
	    							loader.load();	    			
	    						}
							}
						} catch (err){ alert(err) }
	
        			}
        			sdata.widgets.WidgetLoader.mountable_widgets[tid.substring(7)].win.show(this);
    			};

			} catch (err){ alert(err) }
		}

	},	

	getElementsByClassName : function (needle, divid) { 
   		var s, i, r = [], l = 0, e; 
    	var re = new RegExp('(^|\\s)' + needle + '(\\s|$)'); 

		var div = document.body;
		if (divid){
			div = document.getElementById(divid);
		}

    	if (navigator.userAgent.indexOf('Opera') > -1) 
   	 	{ 
        	//s = [document.documentElement || document.body], i = 0; 
			s = [div], i = 0; 

        	do 
        	{ 
            	e = s[i]; 

 	           	while (e) 
            	{ 
                	if (e.nodeType == 1) 
                	{ 
                    	if (e.className && re.test(e.className)) r[l++] = e; 

                    	s[i++] = e.firstChild; 
                	} 

                	e = e.nextSibling; 
            	} 
        	} 
        	while (i--); 
    	} 
    	else 
    	{ 
        	s = div.getElementsByTagName('*'), i = s.length; 

        	while (i--) 
        	{ 
            	e = s[i]; 
            	if (e.className && re.test(e.className)) r[l++] = e; 
        	} 
    	} 

    	return r; 
	},

	/**
	 * Gets an associative array of bundles from the widget. 
	 * Bundles are defined using <link href="budlesrc" hreflang="en_US"  type="messagebundle/json" />
     * where bundle src is the source url of the bundle for the widget
	 * @param {String} tag
	 * @param {String} widgetMarkup
	 */
	getLinks : function(tag,widgetMarkup) {
		var findLinks = new RegExp("<"+tag+".*?>","gim");
		var extractAttributes = /\s\S*?="(.*?)"/gim;	
		var bundle = [];	
		while (findLinks.test(widgetMarkup)) {
			var linkMatch = RegExp.lastMatch;
			var linkTag = new Array();
			while (extractAttributes.test(linkMatch)) {
				var attribute = RegExp.lastMatch;
				
				var value = RegExp.lastParen;
				var attributeName = attribute.substring(1,attribute.indexOf("="));
				linkTag[attributeName] = value;
			}
			bundle.push(linkTag);
		}
		return bundle;				
	},


	/**
	 * Load Widget HTML
	 * @param div the target div
	 * @param the content of the widget as HTML
	 */
	sethtmlover : function (div,content,callback){
   
   		var anotherone = true;

		while (anotherone === true){
			
			var startscript = content.indexOf("<link");
   			var eindscript = content.indexOf("<\/link>");

			if (startscript !== -1 && eindscript !== -1){
   			
   				var linktag = content.substring(startscript, eindscript);
   				linktag = linktag.substring(linktag.indexOf("href=") + 6);
   				linktag = linktag.substring(0, linktag.indexOf("\""));
				
				if (linktag !== "/resources/css/ext-all.css"){
   			
   					var oScript = document.createElement('link');
  					oScript.setAttribute('rel','stylesheet');
  					oScript.setAttribute('type','text/css');
   					oScript.setAttribute('href',linktag);
   					document.getElementsByTagName("head").item(0).appendChild(oScript);
	
				}

				var tussencontent = content;
				content = tussencontent.substring(0, startscript);
				content += tussencontent.substring(eindscript + 7);
   			
   			} else {

				anotherone = false;

			}

		}

		anotherone = true;

		var scripttags = [];
		while (anotherone === true){
			
			startscript = content.indexOf("<script");
   			eindscript = content.indexOf("<\/script>");

			if (startscript !== -1 && eindscript !== -1){
   			
   				var linktag = content.substring(startscript, eindscript);
				
   				linktag = linktag.substring(linktag.indexOf("src=") + 5);
   				linktag = linktag.substring(0, linktag.indexOf("\""));

				if ( sdata.widgets.WidgetLoader.acceptJS(linktag) ) {
	   				var oScript = document.createElement('script');
	  				oScript.setAttribute('language','JavaScript');
	  				oScript.setAttribute('type','text/javascript');
	   				oScript.setAttribute('src',linktag);
			
					scripttags[scripttags.length] = oScript;									
				}
				
				var tussencontent = content;
				content = tussencontent.substring(0, startscript);
				content += tussencontent.substring(eindscript + 9);
   			
   			} else {

				anotherone = false;

			}

		}
		
		if ($("#" + div)) {
			$("#" + div).html(content);
		} else {
			//console.debug("help");
		}
			
		for (var iii = 0; iii < scripttags.length; iii++){
			document.getElementsByTagName("head").item(0).appendChild(scripttags[iii]);
		}
	   		
	},
	
	acceptJS : function(link) {
		
		
		var elements = link.split("/");
		if ( elements.length > 0 ) {
			var locate = 0;
			if ( elements[locate] === "" ) {
				locate++;
			}
			if ( SDATA_DEMOSITES[elements[locate]] === 1 ) {
				locate++;
			}
			if ( SDATA_IGNORE_JS_LIB[elements[locate]] === 1 ) {
				return false;
			}
		}
		return true;
	}

	
};
	
/**
 * @static 
 * @class Widget Preference persistance
 * @name sdata.widgets.WidgetPreference
 * <pre>
 *	In your widget you can use the following functions to save/get widget preferences
 *	
 *		* Save a preference with feedback:	var response = WidgetPreference.save(preferencename:String, preferencontent:String, myCallbackFunction);	
 *		
 *			This will warn the function myCallbackFunction, which should look like this:
 *			
 *				function myCallbackFunction(success){
 *					if (success) {
 *						//Preference saved successfull
 *						//Do something ...
 *					} else {
 *						//Error saving preference
 *						//Do something ...
 *					}
 *				}
 *		
 *		* Save a preference without feedback:	var response = WidgetPreference.quicksave(preferencename:String, preferencontent:String);
 *		
 *			This will not warn you when saving the preference was successfull or unsuccessfull
 *		
 *		* Get the content of a preference:	var response = WidgetPreference.get(preferencename:String, myCallbackFunction);
 *		
 *			This will warn the function myCallbackFunction, which should look like this:
 *			
 *				function myCallbackFunction(response, exists){
 *					if (exists) {
 *						//Preference exists
 *						//Do something with response ...
 *					} else {
 *						//Preference does not exists
 *						//Do something ...
 *					}
 *				}
 *	 </pre>
 */
sdata.widgets.WidgetPreference =  {
	/**
	 * Get a preference from personal storage
	 * @param {string} prefname the preference name
	 * @param {function} callback the function to call on sucess
	 * 
	 */
	get : function(prefname, callback, requireslogin){ 
		var url= "/sdata/p/widgets/" + prefname;
		url = url +"?sid="+Math.random();
		var args = "true";
		if (requireslogin){
			args = "false";
		}
		sdata.Ajax.request( {
			url : url,
			onSuccess : function(data) {
				callback(data,true);
			},
			onFail : function(status) {
				callback(status,false);
			},
			sendToLoginOnFail: args
		});

	},

	/**
	 * Save a preference to a name
	 * @param {string} prefname the preference name
	 * @param prefcontent the content to be saved
	 * @param {function} callback, the call back to call when the save is complete
	 */
	save : function(url, prefname, prefcontent, callback, requireslogin){
		var cb = callback || function() {}; 
		var args = "true";
		if (requireslogin){
			args = "false";
		}
		var url= url + "?sid=" + Math.random();
		var data = {"items":{"data": prefcontent,"fileName": prefname,"contentType":"text/plain"}};
		sdata.Ajax.request({
			url :url,
			httpMethod : "POST",
			onSuccess : function(data) {
				cb(data,true);
			},
			onFail : function(status) {
				cb(status,false);
			},
			postData : data,
			contentType : "multipart/form-data",
			sendToLoginOnFail: args
		});
			
 	}
};
	
sdata.util = {};	

/**
 * Strip out all HTML tags from an HTML string
 * @param {string} the HTML string
 * @return the stripped string
 * @type String
 */
sdata.util.stripHTML = function(htmlstring, limit){
	if (htmlstring){
		var endstring = htmlstring.replace(/&nbsp;/ig," ").replace(/<br([^>]+)>/ig," ").replace(/(<([^>]+)>)/ig,"");
		if (limit){
			if (endstring.length > limit){
				endstring = endstring.substring(0,limit) + " ...";
			}
		} 
		return endstring;
	} else {
		return null;
	}
}

/**
 * @static 
 * @class String utilities
 * @name sdata.util.String
 */
sdata.util.String = {
	/**
	 * Display bytes with a size string, B,MB,GB,TB
	 * @param {integer} nbytes the bytes to be formatted
	 * @return the formatted byte string
	 * @type String
	 */
	formatBytes: function(nbytes) {
		if ( nbytes < 1024 ) {
			return nbytes.toString()+" B";
		}
		nbytes = nbytes/1024;
		if ( nbytes <  1024 ) {				
			return nbytes.toFixed(2)+" KB";
		}
		nbytes = nbytes/1024;
		if ( nbytes <  1024 ) {
			return nbytes.toFixed(2)+" MB";
		}
		nbytes = nbytes/1024;
		return nbytes.toFixed(2)+" GB";			
	},
	
	/**
	 * Format a time interval into the form hh:mm:ss
	 * @param {integer} t the time to be formatted in seconds.
	 * @return a formatted time interval string
	 * @type String
	 */
	formatTime: function(t) {
		if ( t < 0 ) {
			t = 1;
		}
		t = Math.ceil(t);
		var s = t%60;
		var sec = s<10?"0"+s.toString():s.toString();
		t = (t-s)/60;
		var m = t%60;
		var min = m<10?"0"+m.toString():m.toString();
		var h = (t-m)/60;
		var hour = h<10?"0"+h.toString():h.toString();
		return hour+":"+min+":"+sec;
	},
	
	/**
	 * Replace all occurances of replacements in str
	 * @param {string} str the string to be searched for replacements
	 * @param {array} replacements an array of 2 element arrays containing the search string and optionally the replace string
	 * @return The string after replacements
	 * @type String
	 */
	replaceAll : function( str, replacements ) {
	    for ( var i = 0; i < replacements.length; i++ ) {
	        var idx = str.indexOf( replacements[i][0] );
	
	        while ( idx > -1 ) {
	            str = str.replace( replacements[i][0], replacements[i][1] ); 
	            idx = str.indexOf( replacements[i][0] );
	        }
	
	    }
		return str;
	}				
};

sdata.util.URL = {

    // public method for url encoding
    encode : function (string) {
        return escape(this._utf8_encode(string));
    },

    // public method for url decoding
    decode : function (string) {
        return this._utf8_decode(unescape(string));
    },

    // private method for UTF-8 encoding
    _utf8_encode : function (string) {
		try {
        string = string.replace(/\r\n/g,"\n");
        var utftext = "";

        for (var n = 0; n < string.length; n++) {

            var c = string.charCodeAt(n);

            if (c < 128) {
                utftext += String.fromCharCode(c);
            }
            else if((c > 127) && (c < 2048)) {
                utftext += String.fromCharCode((c >> 6) | 192);
                utftext += String.fromCharCode((c & 63) | 128);
            }
            else {
                utftext += String.fromCharCode((c >> 12) | 224);
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                utftext += String.fromCharCode((c & 63) | 128);
            }

        }

        return utftext;
		} catch (err){
			return string;
		}
    },

    // private method for UTF-8 decoding
    _utf8_decode : function (utftext) {
        var string = "";
        var i = 0;
        var c = c1 = c2 = 0;

        while ( i < utftext.length ) {

            c = utftext.charCodeAt(i);

            if (c < 128) {
                string += String.fromCharCode(c);
                i++;
            }
            else if((c > 191) && (c < 224)) {
                c2 = utftext.charCodeAt(i+1);
                string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
                i += 2;
            }
            else {
                c2 = utftext.charCodeAt(i+1);
                c3 = utftext.charCodeAt(i+2);
                string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
                i += 3;
            }

        }

        return string;
    }
	
}

sdata.lib = {};

/**
 * @static 
 * @class Load to load javascript libraries
 */
sdata.lib.Load = {
	/**
	 * @desc A cache of JS files to avoid duplicate load attempts from Require Once
	 * @private
	 * @static
	 */
	js_cache : [],
	

	/**
	 * Require a JS library
	 * @param {String} url is the URL of the script file relative to the parent dom.
	 * @static
	 */
	requireOnce : function(url) {
		if ( ! sdata.lib.Load.js_cache[url] ) {
   			var script = document.createElement("script");
   			var head = document.getElementsByTagName('head').item(0); 
   			script.src = url;
   			script.type="text/javascript";
   			script.language="JavaScript";
   			head.appendChild(script); 
   			sdata.lib.Load.js_cache[url] = url;
		}
	},
	/**
	 * Require a CSS library, however since we need page context on this, we dont attempt to 
	 * prevent loading if its already there as the JS may cover more than one frame.
	 * @param {String} url is the URL of the script file relative to the parent dom.
	 * @static
	 */
	requireCSS : function(url) {
   		var script = document.createElement("link");
   		var head = document.getElementsByTagName('head').item(0); 
   		script.href = url;
   		script.type = "text/css";
   		script.rel = "stylesheet";
   		head.appendChild(script); 
	}
	
};

sdata.html = {};

/**
 * @static 
 * @class Template utilities
 * @name sdata.html.Template
 */
 
sdata.html.Template = {
	
	/**
	 * A persistant cache for the templates
	 * @private
	 */
	templateCache : [],
	
	/**
	 * render the temlate with a context
	 * @param {string} templateName the name of the element ID.
	 * @param {object} contextObject the javascript object containing the data to be rendered
	 * @param {string} [optional] where the rendered output should be placed.
	 * @return The rendered template
	 * @type String
	 */
	render : function(templateName, contextObject, output)  {
		 try {
			
			if ( ! sdata.html.Template.templateCache[templateName] ) {
				 var templateNode = document.getElementById(templateName);
				 var firstNode = templateNode.firstChild;
				 var template = null;
				 if ( firstNode && ( firstNode.nodeType === 8 || firstNode.nodeType === 4)) {
				 	template = templateNode.firstChild.data.toString();
				 	
				 } else {
				 	template = templateNode.innerHTML.toString();
				 }
				 sdata.html.Template.templateCache[templateName] = TrimPath.parseTemplate(template,templateName);
			}

			var render = sdata.html.Template.templateCache[templateName].process(contextObject);

			if (output) {
				output.html(render);
			}
				
			return render;
		} catch (error){
			alert(error);
		}
	},
	
	/**
	 * test the template and inject it into test div 
	 * @param {string} target the target test div
	 * @param {array} templates an array of template names
	 */
	test : function(target,templates) {
			var rc = {
				response : {},
            	id : 'f' + Math.floor(Math.random() * 999999999)
			};
			for ( var name in templates ) {
				document.getElementById(target).innerHTML += sdata.html.Template.render(templates[name],rc);
			}
	}

};

sdata.client = {};

sdata.client.me = function() {
	var person = null;
	function _get(callback, failcallback){
		_getWithFail(callback,function(httpstatus) {
					if ( httpstatus != 401 ) {
						failcallback(httpstatus);	
	        		}			
		});
	};
	function _getWithFail(callback,failcallback) {
		if ( person == null ) {
			sdata.Ajax.request( { 
				url : "/sdata/me",
				sendToLoginOnFail : "false",
				onSuccess :  function (response) {
					person = eval('(' + response + ')');
					callback(person);
				},
				onFail : function(httpstatus) {
					failcallback(httpstatus);
	        	}
	    	});
		} else {
			callback(person);
		}
	};
	return {
		get : _get,
		getWithFail : _getWithFail
	};
	
}();


/**
 * Provides i18n capabilities to SData. 
 * Message bundles are retrieved over ajax, the bundle is used to process
 * inbound strings.
 */
sdata.i18n = function() {
	var preferences = {};

	/**
	 * get the bundle and invoke a callback
	 * @param {Object} callback the sucess callback invoked as callback(bundle)
	 * @param {Object} failcallback the failure callback invoked as failcallback();
	 * @param {Object} bundleLocations an associative array of locations keyed on the locale
	 * @param {Object} defaultLocale the default locale key
	 */	
	function _get(callback,failcallback,bundleLocations,defaultLocale) {
		sdata.client.me.getWithFail(
			/** 
			 * Sucess callback
			 * @param {Object} person
			 */
			function(person) {
				// we have a person object, work out the prefered locale
				var localeKey = "";
				try {
					localeKey = person.items.userLocale.language+"_"+person.items.userLocale.country;
				} catch (err){}
				
				if ( !preferences[localeKey] ) {
					// not loaded, get the target URL
					var targetUrl = bundleLocations[defaultLocale];
					var defaultTargetUrl = targetUrl;
					if ( bundleLocations[localeKey] ) {
						targetUrl = bundleLocations[localeKey];
					}
					if ( !targetUrl ) {
						failcallback();
						return;
					}
					// load the target URL
					_loadBundle(callback,failcallback,localeKey,targetUrl,defaultTargetUrl);				
				} else {
					var bundle = preferences[localeKey];
					callback(bundle);
					return;
				}
				
			}, 
			/**
			 * The failure callback
			 * @param {Object} httpstatus
			 */
			function(httpstatus) {
			    // we have no personal information, 
			    // get the default Locale
				var localeKey = defaultLocale;
				if ( !preferences[defaultLocale] ) {
					var targetUrl = bundleLocations[defaultLocale];
					if ( targetUrl == null ) {
						failcallback();
						return;
					}
					_loadBundle(callback,failcallback,localeKey,targetUrl,targetUrl);
				} else {
					var bundle = preferences[localeKey];
					callback(bundle);
					return;
				}				
		   });
	};
	/**
	 * Load a bundle using async calls and callbacks
	 * @param {Object} callback the callback on sucess invoked as callback(bundle)
	 * @param {Object} failcallback the failure callback invoked as failcallbacl()
	 * @param {Object} localeKey the localeKey to cache the result into 
	 * @param {Object} url the location of the prefered bundle
	 * @param {Object} defaultUrl the location of the default bundle
	 */
	function _loadBundle(callback,failcallback,localeKey,url,defaultUrl) {
		if ( !url ) {
			failcallback();
			return;
		}
		sdata.Ajax.request({
			url : url,
			sendToLoginOnFail : "false",
			/**
			 * sucess callback
			 * @param {Object} response
			 */
			onSuccess :  function (response) {
				bundle = eval('(' + response + ')');
				preferences[localeKey] = bundle;
				callback(bundle);
			},
			/**
			 * Failure Callback
			 * @param {Object} httpstatus
			 */
			onFail : function(httpstatus) {
				if ( defaultUrl == null ) {
					failcallback();
					return;
				}
				// requested bundle was not available
				sdata.Ajax.request({
					url : defaultUrl,
					sendToLoginOnFail : "false",
					/**
					 * Sucess callback
					 * @param {Object} response
					 */
					onSuccess :  function (response) {
						bundle = eval('(' + response + ')');
						preferences[localeKey] = bundle;
						callback(bundle);
					},
					/**
					 * Failiure callback
					 * @param {Object} httpstatus
					 */
					onFail : function(httpstatus) {
						failcallback();
			        }
				})
	        }
			
			
		});
	};
	/**
	 * get the replacement for the target in the bundle. The target is of the form __MSG__key__
	 * @param {Object} target
	 * @param {Object} bundle
	 */
	function _replaceTarget(target,bundle) {
		var name = target.substring(7,target.length-2);
		return bundle[name];
	};

	function _processhtml(toprocess, localbundle, defaultbundle) {
		var re = new RegExp("__MSG__(.*?)__", "gm");
		var processed = "";
		var lastend = 0;
		while(re.test(toprocess)) {
			var replace = RegExp.lastMatch;
			var toreplace = "";
			var lastParen = RegExp.lastParen;
			try {
				if (localbundle[lastParen]){
					toreplace = localbundle[lastParen];
				} else {
					throw "Not in local file";
				}
			} catch (err){
				try {
					if (defaultbundle[lastParen]){
						toreplace = defaultbundle[lastParen];
					} else {
						throw "Not in default file";
					}
				} catch (err){};
			}
			processed += toprocess.substring(lastend,re.lastIndex-replace.length)+ toreplace;
			lastend = re.lastIndex;
		}
		processed += toprocess.substring(lastend)
		return processed;
	};
	
	/**
		 * Process the string
		 * @param {String} toprocess The string to process
		 * @param {function} callback the callback that gets the processed string
		 * @param {Array} bundleLocations an associative array of locations
		 * @param {String} defaultLocale the default Locale
		 */
	function _process(toprocess,callback,bundleLocations,defaultLocale) {
		if ( bundleLocations.length === 0 ) {
			callback(toprocess);
			return;
		}	
		_get(
			/**
			 * Callback for a sucessfull bundle get
			 * @param {Object} bundle
			 */
			function(bundle){
				var re = new RegExp("__MSG__(.*?)__", "gm");
				var processed = "";
				var lastend = 0;
				while(re.test(toprocess)) {
					var replace = RegExp.lastMatch;
					if ( bundle[RegExp.lastParen] ) {
						replace = bundle[RegExp.lastParen];
					}
					processed += toprocess.substring(lastend,re.lastIndex-RegExp.lastMatch.length)+replace;
					lastend = re.lastIndex;
				}
				processed += toprocess.substring(lastend)
				callback(processed);
			},
			/**
			 * Failed
			 */
			function(){
				callback(toprocess);
			},
			bundleLocations,
			defaultLocale
		);
	};
	

	/**
	 * Gets an associative array of bundles from the widget. 
	 * Bundles are defined using <link href="budlesrc" hreflang="en_US"  type="messagebundle/json" />
     * where bundle src is the source url of the bundle for the widget
	 * @param {Object} widgetMarkup
	 */
	function _getBundles(widgetMarkup) {
		var scripttags = sdata.widgets.WidgetLoader.getLinks("link",widgetMarkup);
		var bundle = {};
		for( var i = 0; i < scripttags.length; i++ ) {
			if ( scripttags[i].type === "messagebundle/json" ) {
				var locale = "default";
				if ( scripttags[i].hreflang ) {
					locale = scripttags[i].hreflang;
				}
				bundle[locale] = scripttags[i].src;
			}
		}
		return bundle;				
	};
	
	return {
		/**
		 * Process the string
		 * @param {String} toprocess The string to process
		 * @param {function} callback the callback that gets the processed string
		 * @param {Array} bundleLocations an associative array of locations
		 * @param {String} defaultLocale the default Locale
		 */
		process : _process,
		processhtml : _processhtml,
		/**
		 * Async Get into the callback or failcallback of the bundle for the user, specified by the bundleLocations.
		 * If the bundle cant be found, the defaultLocale is tried. If that cant be found failcallback is invoked.
		 * @param {Object} callback The successfull callback, invoked as callback(bundle)
		 * @param {Object} failcallback The failure callback, invokec as failcallback()
		 * @param {Object} bundleLocations an associative array of bundle locations.
		 * @param {Object} defaultLocale the default locale key
		 */
		get : _get,
				
		getBundles : _getBundles

	};	

}();
	
// Import the widget definitions
if (document.location.protocol === "file:") {
	sdata.lib.Load.requireOnce('widgets.js');
} else {
	sdata.lib.Load.requireOnce('widgets.js');
}
