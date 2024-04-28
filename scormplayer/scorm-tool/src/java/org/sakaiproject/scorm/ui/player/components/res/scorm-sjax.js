/*
 * This code borrows heavily from the Apache Wicket project file wicket-ajax.js
 *   authored by Igor Vaynberg and Matej Knopp and licensed under the following license:
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Scorm Sjax Namespace
if( typeof (ScormSjax) == "undefined" )
	ScormSjax = {};

ScormSjax.sjaxCall = function sjaxCall( prefix, arg1, arg2, successHandler, failureHandler, precondition, channel )
{
	var url = prefix + '&arg1=' + encodeURIComponent( arg1 ) + '&arg2=' + encodeURIComponent( arg2 ) + '&callNumber=' + encodeURIComponent( call_number );

	var transport = null;

	if( window.ActiveXObject )
	{
		transport = new ActiveXObject( "Microsoft.XMLHTTP" );
	}
	else if( window.XMLHttpRequest )
	{
		transport = new XMLHttpRequest();
	}

	if( transport == null )
	{
		alert( "Could not locate ajax transport. Your browser does not support the required XMLHttpRequest object or the Scorm Player could not gain access to it." );
	}

	transport.open( "GET", url, false );
	transport.onreadystatechange = function() { };
	transport.setRequestHeader( "Wicket-Ajax", "true" );
	transport.setRequestHeader( "Wicket-Ajax-BaseURL", prefix );
	transport.setRequestHeader( "Accept", "text/xml" );
	transport.send( null );

	var responseAsText = "";
	var resultValue = "";

	if( transport.status == 200 || transport.status == "" )
	{
		responseAsText = transport.responseText;

		var envelope;
		if( typeof (window.XMLHttpRequest) != "undefined" && typeof (DOMParser) != "undefined" )
		{
			var parser = new DOMParser();
			envelope = parser.parseFromString( responseAsText, "text/xml" );
		}
		else if( window.ActiveXObject )
		{
			envelope = transport.responseXML;
		}
		var root = envelope.getElementsByTagName( "ajax-response" )[0];

		// the root element must be <ajax-response	
		if( root == null || root.tagName != "ajax-response" )
		{
			Wicket.Log.error( "SCORM: Could not find root <ajax-response> element" );
			return;
		}

		//Wicket.Log.info("SCORM: root element " + root);

		// Initialize the array for steps (closures that execute each action)
		var steps = new Array();

		// go through the ajax response and for every action (component, js evaluation, header contribution)
		// ad the proper closure to steps
		for( var i = 0; i < root.childNodes.length; ++i )
		{
			var node = root.childNodes[i];

			if( node.tagName == "component" )
			{
				// get the component id
				var compId = node.getAttribute( "id" );

				Wicket.Log.info( "Sjax: Process component " + compId );

				var text = "";

				// get the new component body
				if( node.hasChildNodes() )
				{
					text = node.firstChild.nodeValue;
				}

				// if the text was escaped, unascape it
				// (escaping is done when the component body contains a CDATA section)
				var encoding = node.getAttribute( "encoding" );
				if( encoding != null && encoding != "" )
				{
					text = Wicket.decode( encoding, text );
				}

				// get existing component
				var element = Wicket.$( compId );

				if( element == null || typeof (element) == "undefined" )
				{
					Wicket.Log.error( "Component with id [[" + compId + "]] a was not found while trying to perform markup update. Make sure you called component.setOutputMarkupId(true) on the component whose markup you are trying to update." );
				}
				else
				{
					Wicket.Log.info( "Sjax: Replace " + text );
					// replace the component
					Wicket.replaceOuterHtml( element, text );
				}
				//var call = new Wicket.Ajax.Call();
				//call.processComponent(steps, node);
				//ScormSjax.processComponent(steps, node);
			}
			else if( node.tagName == "evaluate" )
			{
				var text = node.firstChild.nodeValue;

				// unescape it if necessary
				var encoding = node.getAttribute( "encoding" );
				if( encoding != null )
				{
					text = Wicket.decode( encoding, text );
				}

				// Wicket 8 now returns something like "(function(){scormresult=true;})();"
				var scormresult = text.match( new RegExp( "^\\(function\\(\\)\\{scormresult=(.*);\\}\\)\\(\\);$" ) );

				if( scormresult != null )
				{
					resultValue = scormresult[1];

					Wicket.Log.info( "Sjax: " + prefix + "('" + arg1 + "', '" + arg2 + "') results in '" + resultValue + "'" );
				}
				else
				{
					// test if the javascript is in form of identifier|code
					// if it is, we allow for letting the javascript decide when the rest of processing will continue
					// by invoking identifier();
					var res = text.match( new RegExp( "^([a-z|A-Z_][a-z|A-Z|0-9_]*)\\|((.|\\n)*)$" ) );

					if( res != null )
					{
						Wicket.Log.info( "Sjax javascript " + res );

						text = "var f = function(" + res[1] + ") {" + res[2] + "};";

						try
						{
							// do the evaluation
							eval( text );
							f( notify );
						}
						catch( exception )
						{
							Wicket.Log.error( "Exception evaluating javascript: " + exception );
						}

					}
					else
					{
						// just evaluate the javascript
						try
						{
							// do the evaluation
							eval( text );
						}
						catch( exception )
						{
							Wicket.Log.error( "Exception evaluating javascript: " + exception );
						}
					}
				}
			}
			else if( node.tagName == "header-contribution" )
			{
				var c = new Wicket.Head.Contributor();
				c.processContribution( steps, node );
			}
		}
	}

	transport.abort();
	return resultValue;
};

ScormSjax.xyzsjaxCall = function xyzsjaxCall( prefix, arg1, arg2, successHandler, failureHandler, precondition, channel )
{
	var url = prefix + '&arg1=' + encodeURIComponent( arg1 ) + '&arg2=' + encodeURIComponent( arg2 ) + '&callNumber=' + encodeURIComponent( call_number );
	var t = Wicket.Ajax.getTransport();

	Wicket.Log.info( "GET " + url );

	//Wicket.Ajax.invokePreCallHandlers();

	if( t != null )
	{
		t.open( "GET", url, false );
		//t.onreadystatechange = this.stateChangeCallback.bind(this);
		// set a special flag to allow server distinguish between ajax and non-ajax requests
		t.setRequestHeader( "Wicket-Ajax", "true" );
		transport.setRequestHeader( "Wicket-Ajax-BaseURL", prefix );
		t.setRequestHeader( "Accept", "text/xml" );
		t.send( null );

		if( t.status == 200 || t.status == "" )
		{ // as stupid as it seems, IE7 sets status to "" on ok
			// response came without error
			var responseAsText = t.responseText;
			var parseResponse = true;

			// first try to get the redirect header
			var redirectUrl;
			try
			{
				redirectUrl = t.getResponseHeader( 'Ajax-Location' );
			}
			catch( ignore )
			{ // might happen in older mozilla
			}

			// the redirect header was set, go to new url
			if( typeof (redirectUrl) != "undefined" && redirectUrl != null && redirectUrl != "" )
			{
				t.onreadystatechange = Wicket.emptyFunction;

				// support/check for non-relative redirectUrl like as provided and needed in a portlet context
				if( redirectUrl.charAt( 0 ) == ('/') || redirectUrl.match( "^http://" ) == "http://" || redirectUrl.match( "^https://" ) == "https://" )
				{
					window.location = redirectUrl;
				}
				else
				{
					var urlDepth = 0;
					while( redirectUrl.substring( 0, 3 ) == "../" )
					{
						urlDepth++;
						redirectUrl = redirectUrl.substring( 3 );
					}
					// Make this a string.
					var calculatedRedirect = window.location.pathname;
					while( urlDepth > -1 )
					{
						urlDepth--;
						i = calculatedRedirect.lastIndexOf( "/" );
						if( i > -1 )
						{
							calculatedRedirect = calculatedRedirect.substring( 0, i );
						}
					}
					calculatedRedirect += "/" + redirectUrl;
					window.location = calculatedRedirect;
				}
			}
			else
			{
				// no redirect, just regular response
				var log = Wicket.Log.info;
				log( "Received ajax response (" + responseAsText.length + " characters)" );
				if( this.debugContent != false )
				{
					log( "\n" + responseAsText );
				}

				// parse the response if the callback needs a DOM tree
				if( parseResponse == true )
				{
					var xmldoc;
					if( typeof (window.XMLHttpRequest) != "undefined" && typeof (DOMParser) != "undefined" )
					{
						var parser = new DOMParser();
						xmldoc = parser.parseFromString( responseAsText, "text/xml" );
					}
					else if( window.ActiveXObject )
					{
						xmldoc = t.responseXML;
					}
					// invoke the loaded callback with an xml document
					ScormSjax.loadedCallback( xmldoc );
				}
				else
				{
					// invoke the loaded callback with raw string
					ScormSjax.loadedCallback( responseAsText );
				}
			}
		}
		else
		{
			// when an error happened
			var log = Wicket.Log.error;
			log( "Received Ajax response with code: " + status );
			if( status == 500 )
			{
				log( "500 error had text: " + t.responseText );
			}
		}
		t.onreadystatechange = Wicket.emptyFunction;
		t.abort();
	}

	//Wicket.Log.info("sjaxCall resultCode = " + resultCode);
	var resultValue = api_result[call_number];
	//Wicket.Log.info("sjaxCall resultValue = " + resultValue);
	call_number++;
	return resultValue;
};

ScormSjax.loadedCallback = function loadedCallback( envelope )
{
	// To process the response, we go through the xml document and add a function for every action (step).
	// After this is done, a FunctionExecuter object asynchronously executes these functions.
	// The asynchronous execution is necessary, because some steps might involve loading external javascript,
	// which must be asynchronous, so that it doesn't block the browser, but we also have to maintain
	// the order in which scripts are loaded and we have to delay the next steps until the script is
	// loaded.
	try
	{
		var root = envelope.getElementsByTagName( "ajax-response" )[0];

		// the root element must be <ajax-response	
		if( root == null || root.tagName != "ajax-response" )
		{
			Wicket.Log.error( "Could not find root <ajax-response> element" );
			return;
		}

		// iinitialize the array for steps (closures that execute each action)
		var steps = new Array();

		// start it a bit later so that the browser does handle the next event 
		// before the component is or can be replaced. We could do (if (!posponed))
		// because if there is already something in the queue then we could execute that immedietly 
		steps.push( function( notify )
		{
			window.setTimeout( notify, 2 );
		}.bind( this ) );

		if( Wicket.Browser.isKHTML() )
		{
			// there's a nasty bug in KHTML that makes the browser crash
			// when the methods are delayed. Therefore we have to fire it
			// ASAP. The javascripts that would cause dependency problems are
			// loaded synchronously in konqueror.
			steps.push = function( method )
			{
				method( function()
				{ } );
			}
		}

		// go through the ajax response and for every action (component, js evaluation, header contribution)
		// ad the proper closure to steps
		for( var i = 0; i < root.childNodes.length; ++i )
		{
			var node = root.childNodes[i];

			if( node.tagName == "component" )
			{
				ScormSjax.processComponent( steps, node );
			}
			else if( node.tagName == "evaluate" )
			{
				ScormSjax.processEvaluation( steps, node );
			}
			else if( node.tagName == "header-contribution" )
			{
				ScormSjax.processHeaderContribution( steps, node );
			}
		}

		// add the last step, which should trigger the success call the done method on request
		Wicket.Log.info( "Response processed successfully." );
		Wicket.Ajax.invokePostCallHandlers();
		// retach the events to the new components (a bit blunt method...)
		// This should be changed for IE See comments in wicket-event.js add (attachEvent/detachEvent)
		// IE this will cause double events for everything.. (mostly because of the Function.prototype.bind(element))
		Wicket.Focus.attachFocusEvent();

		// set the focus to the last component
		setTimeout( "Wicket.Focus.requestFocus();", 0 );

		// continue to next step (which should make the processing stop, as success should be the final step)		
		notify();

		if( Wicket.Browser.isKHTML() == false )
		{
			Wicket.Log.info( "Response parsed. Now invoking steps..." );
			var executer = new Wicket.FunctionsExecuter( steps );
			executer.start();
		}
	}
	catch( e )
	{
		Wicket.Log.error( e.message );
	}
}

ScormSjax.processComponent = function processComponent( steps, node )
{
	steps.push( function( notify )
	{
		// get the component id
		var compId = node.getAttribute( "id" );
		var text = "";

		// get the new component body
		if( node.hasChildNodes() )
		{
			text = node.firstChild.nodeValue;
		}

		// if the text was escaped, unascape it
		// (escaping is done when the component body contains a CDATA section)
		var encoding = node.getAttribute( "encoding" );
		if( encoding != null && encoding != "" )
		{
			text = Wicket.decode( encoding, text );
		}

		// get existing component
		var element = Wicket.$( compId );

		if( element == null || typeof (element) == "undefined" )
		{
			Wicket.Log.error( "Component with id [[" + compId + "]] a was not found while trying to perform markup update. Make sure you called component.setOutputMarkupId(true) on the component whose markup you are trying to update." );
		}
		else
		{
			// replace the component
			Wicket.replaceOuterHtml( element, text );
		}
		// continue to next step
		notify();
	} );
};

// Adds a closure that evaluates javascript code
ScormSjax.processEvaluation = function processEvaluation( steps, node )
{
	steps.push( function( notify )
	{
		// get the javascript body
		var text = node.firstChild.nodeValue;

		// unescape it if necessary
		var encoding = node.getAttribute( "encoding" );
		if( encoding != null )
		{
			text = Wicket.decode( encoding, text );
		}

		// test if the javascript is in form of identifier|code
		// if it is, we allow for letting the javascript decide when the rest of processing will continue 
		// by invoking identifier();
		var res = text.match( new RegExp( "^([a-z|A-Z_][a-z|A-Z|0-9_]*)\\|((.|\\n)*)$" ) );

		if( res != null )
		{
			text = "var f = function(" + res[1] + ") {" + res[2] + "};";

			try
			{
				// do the evaluation
				eval( text );
				f( notify );
			}
			catch( exception )
			{
				Wicket.Log.error( "Exception evaluating javascript: " + exception );
			}
		}
		else
		{
			// just evaluate the javascript
			try
			{
				// do the evaluation
				eval( text );
			}
			catch( exception )
			{
				Wicket.Log.error( "Exception evaluating javascript: " + exception );
			}
			// continue to next step
			notify();
		}
	} );
};

// Adds a closure that processes a header contribution
ScormSjax.processHeaderContribution = function processHeaderContribution( steps, node )
{
	var c = new Wicket.Head.Contributor();
	c.processContribution( steps, node );
};

ScormSjax.xsjaxCall = function xsjaxCall( url, arg1, arg2, successHandler, failureHandler, precondition, channel )
{
	Wicket.Log.info( "Calling sjaxCall with url: " + url + " sco: " + scoId + " arg1: " + arg1 + " arg2: " + arg2 );

	var call = new Wicket.Ajax.Call( url + '&arg1=' + encodeURIComponent( arg1 ) + '&arg2=' + encodeURIComponent( arg2 ) + '&callNumber=' + encodeURIComponent( call_number ),
										function() {}, function() {}, null );

	if( typeof (precondition) != "undefined" && precondition != null )
	{
		call.request.precondition = precondition;
	}

	call.request.async = false;
	var resultCode = call.call();

	ScormSjax.pausecomp( call_number, call );

	//Wicket.Log.info("sjaxCall resultCode = " + resultCode);
	var resultValue = api_result[call_number];
	//Wicket.Log.info("sjaxCall resultValue = " + resultValue);
	call_number++;
	return resultValue;
};

ScormSjax.pausecomp = function pausecomp( call_number, call )
{
	//Wicket.Log.info("URL is: " + call.request.url);
	var startDate = new Date();
	var curDate = null;

	var intervalId = setInterval( "ScormSjax.lookForResponse(call.request)", 30 );
	do
	{
		curDate = new Date();
		//Wicket.Log.info("api result: " + api_result[call_number]);
		var diff = curDate - startDate;
		//Wicket.Log.info("diff: " + diff);
		if( diff > 500 )
		{
			//Wicket.Log.info("Timed out--unable to get result: " + api_result[call_number] + " for " + call_number);
			clearInterval( intervalId );
			break;
		}
	} while( api_result[call_number] == undefined );
	clearInterval( intervalId );
}

ScormSjax.lookForResponse = function lookForResponse( request )
{
	var t = request.transport;

	if( t != null && t.readyState == 4 )
	{
		try
		{
			status = t.status;
		}
		catch( e )
		{
			Wicket.Log.error( "Exception evaluating AJAX status: " + e );
			status = "unavailable";
		}
		if( status == 200 || status == "" )
		{ // as stupid as it seems, IE7 sets status to "" on ok
			// response came without error
			var responseAsText = t.responseText;

			// first try to get the redirect header
			var redirectUrl;
			try
			{
				redirectUrl = t.getResponseHeader( 'Ajax-Location' );
			}
			catch( ignore )
			{ // might happen in older mozilla
			}

			// the redirect header was set, go to new url
			if( typeof (redirectUrl) != "undefined" && redirectUrl != null && redirectUrl != "" )
			{
				t.onreadystatechange = Wicket.emptyFunction;

				// support/check for non-relative redirectUrl like as provided and needed in a portlet context
				if( redirectUrl.charAt( 0 ) == ('/') || redirectUrl.match( "^http://" ) == "http://" || redirectUrl.match( "^https://" ) == "https://" )
				{
					window.location = redirectUrl;
				}
				else
				{
					var urlDepth = 0;
					while( redirectUrl.substring( 0, 3 ) == "../" )
					{
						urlDepth++;
						redirectUrl = redirectUrl.substring( 3 );
					}
					// Make this a string.
					var calculatedRedirect = window.location.pathname;
					while( urlDepth > -1 )
					{
						urlDepth--;
						i = calculatedRedirect.lastIndexOf( "/" );
						if( i > -1 )
						{
							calculatedRedirect = calculatedRedirect.substring( 0, i );
						}
					}
					calculatedRedirect += "/" + redirectUrl;
					window.location = calculatedRedirect;
				}
			}
			else
			{
				// no redirect, just regular response
				var log = Wicket.Log.info;
				log( "Received ajax response (" + responseAsText.length + " characters)" );

				// parse the response if the callback needs a DOM tree
				if( request.parseResponse == true )
				{
					var xmldoc;
					if( typeof (window.XMLHttpRequest) != "undefined" && typeof (DOMParser) != "undefined" )
					{
						var parser = new DOMParser();
						xmldoc = parser.parseFromString( responseAsText, "text/xml" );
					}
					else if( window.ActiveXObject )
					{
						xmldoc = t.responseXML;
					}
					// invoke the loaded callback with an xml document
					request.loadedCallback( xmldoc );
				}
				else
				{
					// invoke the loaded callback with raw string
					request.loadedCallback( responseAsText );
				}
				if( request.suppressDone == false )
					request.done();
			}
		}
		else
		{
			// when an error happened
			var log = Wicket.Log.error;
			log( "Received Ajax response with code: " + status );
			if( status == 500 )
			{
				log( "500 error had text: " + t.responseText );
			}
			request.done();
			request.failure();
		}
		t.onreadystatechange = Wicket.emptyFunction;
		t.abort();
		request.transport = null;
	}
}
