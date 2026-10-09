/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * Utilities used by MyFaces x:inputHtml tag
 * @author Sylvain Vieujot (latest modification by $Author: lu4242 $)
 * @version $Revision: 928334 $ $Date: 2010-03-27 23:24:30 -0500 (Sáb, 27 Mar 2010) $
 */
var myFacesKupuTextsToLoad = new Array();
var myFacesKupuClientIDs = new Array();
var myFacesKupuFormsIds = new Array();
var myFacesKupuIFramesIds = new Array();
var myFacesKupuResourceBaseURL;
var myFacesKupuDocOnLoadSet = false;
var myFacesKupuOriginalDocOnLoad;
var myFacesKupuProcessedFormsIds = new Array();
var myFacesKupuOriginalFormsOnSubmits = new Array();
var myFacesKupuEditors;

function myFacesKupuSet(text, clientId, formId, resourceBaseURL){
	myFacesKupuTextsToLoad.push( text );
	myFacesKupuClientIDs.push( clientId );
	myFacesKupuFormsIds.push( formId );
	myFacesKupuIFramesIds.push( myFacesKupuGetIFrameId(clientId) );
	myFacesKupuResourceBaseURL = resourceBaseURL;

	if( ! myFacesKupuDocOnLoadSet ){
		var onLoadSrc;
	    if( document.all ) // IE
			onLoadSrc = document.body;
		else // Mozilla
			onLoadSrc = window;

		myFacesKupuOriginalDocOnLoad = onLoadSrc.onload;
		onLoadSrc.onload = myFacesKupuInit;
		
		myFacesKupuDocOnLoadSet = true;
	}
	
	var formAlreadyProcessed = false;
	for(var i=0 ; i<myFacesKupuProcessedFormsIds.length && ! formAlreadyProcessed ; i++){
		if( myFacesKupuProcessedFormsIds[i] == formId )
			formAlreadyProcessed = true;
	}
	if( ! formAlreadyProcessed ){
		myFacesKupuProcessedFormsIds.push( formId );
		var form = document.forms[formId];
		myFacesKupuOriginalFormsOnSubmits.push( form.onsubmit );
		form.onsubmit = myFacesKupuFormSubmit;
	}
}

// Must match InputHtmlRenderer.getIFrameID
function myFacesKupuGetIFrameId(clientId){
	return clientId+"_iframe";
}

function myFacesKupuInit(){
	if( myFacesKupuOriginalDocOnLoad )
		myFacesKupuOriginalDocOnLoad();

	for(var i=0 ; i<myFacesKupuTextsToLoad.length ; i++){
		myFacesKupuEditors = startKupu( myFacesKupuIFramesIds[i] );
		document.getElementById(myFacesKupuIFramesIds[i]).contentWindow.document.getElementsByTagName('body')[0].innerHTML=myFacesKupuTextsToLoad[i];
	}
}

function myFacesKupuReactivateDesignMode(iframe) {
    var isIE = document.all && window.ActiveXObject && navigator.userAgent.toLowerCase().indexOf("msie") > -1 && navigator.userAgent.toLowerCase().indexOf("opera") == -1;
    if (isIE) {
        var body = iframe.contentWindow.document.getElementsByTagName('body')[0];
        body.setAttribute('contentEditable', 'true');
    }
    else {
        iframe.contentWindow.document.designMode='on';
    }
}

function myFacesKupuFormSubmit(){
	for(var i=0 ; i<myFacesKupuFormsIds.length ; i++){
		myFacesKupuEditors.prepareForm(document.forms[myFacesKupuFormsIds[i]], myFacesKupuClientIDs[i]);
	}
	
	originalFormOnSubmit = myFacesKupuOriginalFormsOnSubmits[0]; // TODO : Fix (How do we get the calling frame ??)
	if( originalFormOnSubmit ){
		return originalFormOnSubmit();
	}
	return true;
}

// Redefine or extend buggy kupu functions

function openPopup(url, width, height){
    /* open and center a popup window */
    var sw = screen.width;
    var sh = screen.height;
    var left = sw / 2 - width / 2;
    var top = sh / 2 - height / 2;
    var win = window.open(myFacesKupuResourceBaseURL+url, 'someWindow', 
                'width=' + width + ',height=' + height + ',left=' + left + ',top=' + top);
    return win;
}