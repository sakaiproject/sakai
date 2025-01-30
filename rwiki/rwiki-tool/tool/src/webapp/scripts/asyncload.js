/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


/**
 * This is a class to perform async loads from the server into a div.
 * It is the responsibility of the calling method to set the targetDiv, and loaderName
 * of the loader.
 * The loader will downgrade gracefully to a hidden IFrame if the browser does not support 
 * XMLHttpRequest (as ActiveX or native ) 
 * The user experiance may not be quite so gracefull as reloading a hidden IFrame effects 
 * the users screen.
 
 To Use
 
 myLoader = new AsyncDIVLoader();
 myLoader.loaderName = "myloader";
 myLoader.loadXMLDoc("/a/URL/that/returns/div/content/or/javascript.jsp?with=optional&query=params","mylocalcallbackfunction");


Tested On
Native & IFrame: Mozilla 1.4/Win XP: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-GB; rv:1.4) Gecko/20030624
ActiveX & IFrame, readyState is valid: IE6 Win XP: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705)
Native & IFrame: Firefox 1.0, Debian Unstable: Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.7.5) Gecko/20050202 Firefox/1.0 (Debian package 1.0+dfsg.1-4)
Native & IFrame: Firebird 0.7, RH 7: Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.5) Gecko/20031007 Firebird/0.7
Native: NS6.2 Win XP: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-GB; rv:0.9.4.1) Gecko/20020508 Netscape6/6.2.3
IFrame on NS6 works, but the final DIV on the test page doesnt display OK.
Native & IFrame, readyState is valid: Safari 1.2.4 OSX 10.3: Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/125.5.7 (KHTML, like Gecko) Safari/125.12
Native & IFrame: Firefox 1.0 OSX: Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-GB; rv:1.7.5) Gecko/20041110 Firefox/1.0"
IFrame : IE5.2 OSX : MSIE 5.2 OSX :Mozilla/4.0 (compatible; MSIE 5.23; Mac_PowerPC)

Should be tested on:
IE5, IE5.5 but I dont have copies, I have put a specific fix in for IE5 on PC, but this may not work



Netscape 4.79, Linux : Mozilla/4.79 [en] (X11; U; Linux 2.4.18-10 i686)
Fails,
Doesnt understand try catch
setTimeout runns away

Konqorer: Mozilla/5.0 (compatible; Konqueror/3; Linux)"
Fails, 
cant perform setTimeout relyably, 
sometimes it breaks,
Doesnt understand try and catch completely
Doesnt fall back to iframe unless forced.

*/
var _asyncObjectID = 0;
var allAsyncLoaders = new Array();
function AsyncDIVLoader() {
  this.req = null; // current Request
  this.hiddenDiv = null;
  this.callBackFunction = null;
  this.loadComplete = false;
  this.loaderID = 0;
  this.loaderName = "default";
  this.lastLoadStatus = "";
  this.lastResponse = null;
  this.verbose = false;
  this.lastLoadMethod = "";


  // This is required since setTimeout ONLY works ok on global variables in some browsers
  // eg IE5 MAC

  this.asyncObjectID = _asyncObjectID;
  _asyncObjectID++;
  allAsyncLoaders[this.asyncObjectID] = this;
}

AsyncDIVLoader.prototype.deleteLoader = function() {
  allAsyncLoaders[this.asyncObjectID] = null;
  // NB this does not deallocate the slot or destroy the object, it JUST removes
  // it from the loader list. The caller MUST drop references.
  };

function getLoaderObject(loaderObjectID) {
  return allAsyncLoaders[loaderObjectID];
}

AsyncDIVLoader.prototype.loadXMLDoc = function(url,callBackFunction) {
    this.fullLoadXMLDoc(url,callBackFunction,"GET",null,null);
}
AsyncDIVLoader.prototype.encodeContent = function(contentObject) {

  let form_contents = '';

  if ( contentObject.length ) {
    for (let i = 0; i < contentObject.length-1; i += 2 ) {
      form_contents += (form_contents ? '&' : '') + contentObject[i] + '=' + this.encodeFormElement(contentObject[i+1]);
    }
  } else {
      if ( contentObject.elements ) {
        for( let i = 0; i < contentObject.elements.length; i++ ) {
          const thisElement = contentObject.elements[i];
          if ( thisElement.tagName.toLowerCase() == 'input' &&
              (thisElement.type.toLowerCase() == 'checkbox' ||
              thisElement.type.toLowerCase() == 'radio') ) {
            form_contents += (form_contents ? '&' : '') + 
                thisElement.name + '=' +this.encodeFormElement(thisElement.checked);
          } else {
            form_contents += (form_contents ? '&' : '') +
                thisElement.name + '=' +this.encodeFormElement(thisElement.value);
          }
        }
      }
  }
  log("FORM Content XXX encoded as "+form_contents);
  return form_contents;
};

AsyncDIVLoader.prototype.encodeFormElement = function(formVar) {

  let result = encodeURIComponent(formVar);
  result = result.replace(/%20/g,"+");
  for ( let p = result.indexOf("%u"); p != -1; p = result.indexOf("%u")  ) {
    const code = result.substr(p,6);
    const rep = '%' + code.substr(2,2) + '%' + code.substr(4,2);
    result = result.replace(code,rep);
  }
  let p = -1;
  for ( p = result.indexOf("%", p + 1); p != -1; p = result.indexOf("%", p + 1)  ) {
    const code = result.substr(p,3);
    const rep = code.toUpperCase();
    result = result.replace(code,rep);
  }
  return result;
};

AsyncDIVLoader.prototype.createFormObject = function(contentObject,url) {

  var formObject = document.createElement("FORM");
  formObject.action = url;
  formObject.method = "POST";
  if ( contentObject.length ) {
    for (let i = 0; i < contentObject.length-1; i += 2 ) {
      const formElement = document.createElement("INPUT");
      formElement.type = "hidden";
      formElement.name = contentObject[i];
      formElement.value = contentObject[i+1];
      formObject.appendChild(formElement);
    }
  } else {
    if ( contentObject.elements ) {
      for( let i = 0; i < contentObject.elements.length; i++ ) {
        const thisElement = contentObject.elements[i];
        if ( thisElement.tagName.toLowerCase() == 'input' &&
            (thisElement.type.toLowerCase() == 'checkbox' ||
            thisElement.type.toLowerCase() == 'radio' )) {
          const formElement = document.createElement("INPUT");
          formElement.type = thisElement.type;
          formElement.name = thisElement.name;
          formElement.checked = thisElement.checked;
          formObject.appendChild(formElement);
        } else {
          const formElement = document.createElement("INPUT");
          formElement.type = "hidden";
          formElement.name = thisElement.name;
          formElement.value = thisElement.value;
          formObject.appendChild(formElement);
        } 
      }
    }
  }
  log("FORM Content encoded as "+formObject.innerHTML);
  return formObject;
};

AsyncDIVLoader.prototype.createFormContent = function(contentObject,url,formID) {

  let form_contents = `<form action="${encodeURIComponent(url)}" method="POST" id="${formID}">`;
  if ( contentObject.length ) {
    for (let i = 0; i < contentObject.length-1; i += 2 ) {
      form_contents += `<input type="hidden" name="${contentObject[i]}" value="${this.encodeAttribute(contentObject[i + 1])}" />`;
    }
  } else {
    if ( contentObject.elements ) {
      for( var i = 0; i < contentObject.elements.length; i++ ) {
        var thisElement = contentObject.elements[i];
        if ( thisElement.tagName.toLowerCase() == 'input' &&
            (thisElement.type.toLowerCase() == 'checkbox' ||
            thisElement.type.toLowerCase() == 'radio') ) {
          form_contents +=
              `<input type="${thisElement.type}" name="${thisElement.name}" ${thisElement.checked ? "checked" : ""} />`;
        } else {
          form_contents +=
              `<input type="hidden" name="${thisElement.name}" value="${this.encodeAttribute(thisElement.value)}" />`;
        } 
      }
    }
  }
  form_contents += '</form>';
  log(`FORM Content encoded as ${form_contents}`);
  return form_contents;
};

AsyncDIVLoader.prototype.createFormContent = function encodeAttribute(text) {

  return text.replace(/&/, "&amp;")
    .replace(/</, "&lt;")
    .replace(/>/, "&gt;")
    .replace(/\r\n/, "<br>")
    .replace(/\n/, "<br>")
    .replace(/\r/, "<br>");
};

AsyncDIVLoader.prototype.fullLoadXMLDoc = function (url, callBackFunction, method, content) {

  try {
    if ( !this.req ) {
      this.req = new XMLHttpRequest();
    } else  {
      this.req.abort();
    }
    if ( this.req ) {
      if ( this.verbose ) alert("Loading via native");
      this.callBackFunction = callBackFunction;
      this.setOnReadyStateChange();
      this.lastLoadMethod = "Native";
      this.lastLoadStatus = this.lastLoadMethod+": starting";
      if ( method == "GET" ) {
        this.req.open("GET", url, true);
        this.req.send(null);
      } else {
        // encode the contents of the post and submit
        log(`Doing a post for ${url}`);
        this.req.open('POST',url,true);
        this.req.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
        this.req.send(this.encodeContent(content));
      }
    }
  } catch(e) {
    alert("Failed to perform native async on "+url+" reason "+e);
  }
};

/*
If the user of the object set a callback when they invoked the loader,
that callback is called once the load is complete
*/
AsyncDIVLoader.prototype.invokeCallback = function(loaderID) {

  if ( loaderID && loaderID != "" && loaderID != this.loaderID ) return; // abort invoked
  this.loadComplete = true;
  if ( this.verbose ) alert("Invoking callback "+this.callBackFunction+" with "+this+" "+this.lastResponse);
  this.callBackFunction && eval(this.callBackFunction+"(this.lastResponse)");
};

/*
Event handler (object safe) for the onreadystatechange event
*/
AsyncDIVLoader.prototype.setOnReadyStateChange = function () {

  const locked_this = this;
  locked_this.loaderID++;
  locked_this.loadComplete = false;
  if ( locked_this.verbose ) alert("ASYNC: Setting ready callback");
  this.req.onreadystatechange = function() {
    // only if req shows "loaded"
    if (locked_this.req.readyState == 4) {
      // only if "OK"
      var responseHeaders = locked_this.req.getAllResponseHeaders();
      if (locked_this.req.status == 200) {
        if ( locked_this.verbose ) alert("Response Ok"+responseHeaders+"::"+locked_this.req.responseText);
        locked_this.lastResponse = locked_this.req.responseText;
        locked_this.lastLoadStatus  = locked_this.lastLoadMethod+":Completed: Ok";
      } else {
        if ( locked_this.verbose ) alert("Response Failed "+responseHeaders+"::"+locked_this.req.statusText);
        locked_this.lastResponse =
            "There was a problem retrieving the data:\n" + locked_this.req.statusText;
        locked_this.lastLoadStatus  = locked_this.lastLoadMethod+":Completed: Failed "+locked_this.req.statusText;
      }
      if ( locked_this.verbose ) alert("Got "+locked_this.lastResponse);
      locked_this.invokeCallback(locked_this.loaderID);
    }
  };
};
