/*!
 * jQuery Global Escape Key Event Plugin v0.2
 * http://ben.ferit.im
 *
 * Copyright 2012, S. Ferit Arslan
 * Dual licensed under the MIT or GPL Version 2 licenses.
 *
 * Date: Thu Jun 14 14:33:16 2012 +0200 
 */
var jKeyEvent = jKeyEvent || {};
var jEscapeKeyFunctions = jEscapeKeyFunctions || [];
 
 (function($) {
	var debug = false;
	/**/
	jKeyEvent.warn = function(log) { 
		if (console && debug) {
			console.warn(log);
		}
	};
	
	/*john's array prototype remove*/
	jKeyEvent.remove = function (from, to) {
		var rest = jEscapeKeyFunctions.slice((to || from) + 1 || jEscapeKeyFunctions.length);
		jEscapeKeyFunctions.length = from < 0 ? jEscapeKeyFunctions.length + from : from;
		return jEscapeKeyFunctions.push.apply(jEscapeKeyFunctions, rest);
	};
	
	/**/
	jKeyEvent.keydown = function(e) {
		/*add a document key listener*/
		var code = (e.keyCode ? e.keyCode : e.which);
			if (code === 27) {
			e.preventDefault();
				for (var i = jEscapeKeyFunctions.length-1; i>-1; --i) {
					if (typeof jEscapeKeyFunctions[i].callback === 'function') {
						jEscapeKeyFunctions[i].callback(jEscapeKeyFunctions[i].element);
						jKeyEvent.remove(i,0);
						break;
					}
				} //end for		
			} //end if 
	};
	
	/**/
	jKeyEvent.addEvent = function() {
		if (jKeyEvent.hasKeyDown) { 
			return;
		} 
		else
		{
			jKeyEvent.hasKeyDown=true;
			$(document).keydown(function (e) {			
				jKeyEvent.keydown(e);
			}); //end key down 
		} 
	};
	
	/**/
	jKeyEvent.push = function (obj) {	
		var controlled = $.grep(jEscapeKeyFunctions, function(item) {
			return obj.index === item.index
		});
		
		if (controlled.length===0) 
		{
			jEscapeKeyFunctions.push(obj);
		}
	};
			
	/**/
	jKeyEvent.debug = function() {
		if (console) {
			console.warn(jEscapeKeyFunctions.length);
			console.dir(jEscapeKeyFunctions);
		}
	}
	
	$.fn.removeEscape = function() {
		/*Get Index*/
		var index = this.attr('kb');
		if (index) 
		{
			var indexToInt = parseInt(index,10);
			for (var i = 0; i < jEscapeKeyFunctions.length;i++) {
				if (jEscapeKeyFunctions[i].index === indexToInt) {
						jKeyEvent.remove(i,0);	
				}
			}
		} else {
			return false;
		}
	};
	
	/**/
 	$.fn.escape = function(callback) {
		var keyCode = -1;
		var element = this;	
		var keyBindIndex = jEscapeKeyFunctions.length;
		
		if (!element.attr('keybind')) {
			element.attr('kb', jEscapeKeyFunctions.length); //add key bind index
		}
		
		jKeyEvent.push({
			element: element,
			index: keyBindIndex,			
			callback: callback
		});
		
		jKeyEvent.addEvent();
	}; //end of escape 
 })(jQuery);
 
