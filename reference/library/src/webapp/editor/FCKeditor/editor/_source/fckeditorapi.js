/*
 * FCKeditor - The text editor for internet
 * Copyright (C) 2003-2006 Frederico Caldeira Knabben
 * 
 * Licensed under the terms of the GNU Lesser General Public License:
 * 		http://www.opensource.org/licenses/lgpl-license.php
 * 
 * For further information visit:
 * 		http://www.fckeditor.net/
 * 
 * "Support Open Source software. What about a donation today?"
 * 
 * File Name: fckeditorapi.js
 * 	Create the FCKeditorAPI object that is available as a global object in
 * 	the page where the editor is placed in.
 * 
 * File Authors:
 * 		Frederico Caldeira Knabben (fredck@fckeditor.net)
 */

var FCKeditorAPI ;

function InitializeAPI()
{
	if ( !( FCKeditorAPI = window.parent.FCKeditorAPI ) )
	{
		// Make the FCKeditorAPI object available in the parent window. Use 
		// eval so it is independent from this window and so it will still be 
		// available if the editor instance is removed ("Can't execute code
		// from a freed script" error).
		var sScript = '\
			var FCKeditorAPI = {\
				Version			: \'2.3.2\',\
				VersionBuild	: \'1082\',\
				__Instances		: new Object(),\
				GetInstance		: function( instanceName )\
				{\
					return this.__Instances[ instanceName ] ;\
				},\
				_FunctionQueue	: {\
					Functions	: new Array(),\
					IsRunning	: false,\
					Add			: function( functionToAdd )\
					{\
						this.Functions.push( functionToAdd ) ;\
						if ( !this.IsRunning )\
							this.StartNext() ;\
					},\
					StartNext	: function()\
					{\
						var aQueue = this.Functions ;\
						if ( aQueue.length > 0 )\
						{\
							this.IsRunning = true ;\
							aQueue[0].call() ;\
						}\
						else\
							this.IsRunning = false ;\
					},\
					Remove		: function( func )\
					{\
						var aQueue = this.Functions ;\
						var i = 0, fFunc ;\
						while( fFunc = aQueue[ i ] )\
						{\
							if ( fFunc == func )\
								aQueue.splice( i,1 ) ;\
							i++ ;\
						}\
						this.StartNext() ;\
					}\
				}\
			}' ;
		
		// In IE, the "eval" function is not always available (it works with
		// the JavaScript samples, but not with the ASP ones, for example).
		// So, let's use the execScript instead.
		if ( window.parent.execScript )
			window.parent.execScript( sScript, 'JavaScript' ) ;
		else
		{
			if ( FCKBrowserInfo.IsGecko10 )
			{
				// FF 1.0.4 gives an error with the above request. The
				// following seams to work well. It could become to official
				// implementation for all browsers, but we need to check it.
				eval.call( window.parent, sScript ) ;
			}
			else
				window.parent.eval( sScript ) ;
		}
		
		FCKeditorAPI = window.parent.FCKeditorAPI ;
	}

	// Add the current instance to the FCKeditorAPI's instances collection.
	FCKeditorAPI.__Instances[ FCK.Name ] = FCK ;
}

function FCKeditorAPI_Cleanup()
{
	FCKeditorAPI.__Instances[ FCK.Name ] = null ;
}
FCKTools.AddEventListener( window, 'unload', FCKeditorAPI_Cleanup ) ;	