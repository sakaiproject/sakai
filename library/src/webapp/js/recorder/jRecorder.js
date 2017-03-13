/*
 * jRecorder Plugin for jQuery JavaScript Library (Alpha)
 * http://www.sajithmr.me/jrecorder
 *
 * Copyright (c) 2011 - 2013 Sajithmr.ME
 * Dual licensed under the MIT and GPL licenses.
 *  - http://www.opensource.org/licenses/mit-license.php
 *  - http://www.gnu.org/copyleft/gpl.html
 *
 * Author: Sajith Amma
 * Version: 1.1
 * Date: 14 December 2011
 */

/* Code is not verified using http://www.jshint.com/ */

(function ($){
	
	var methods = {
    	play : function( options ) { 
					alert(options);
	 			},
    	pause : function( ) { }
    
  	};

	var jRecorderSettings = {} ;
	
	$.jRecorder = function( options, element ) {
		// allow instantiation without initializing for simple inheritance
		
		if(typeof(options) == "string")
		{
			if ( methods[options] ) 
			{
				return methods[ options ].apply( this, Array.prototype.slice.call( arguments, 1 ));
				
			}
			return false;
		}
		
		//if the element to be appended is not defind, append to body
		if(element == undefined)
		{
			element = $("body");
		}
			
		//default settings
		var settings = {
			'rec_width': '300',
			'rec_height': '200',
			'rec_top': '0px',
			'rec_left': '0px',
			'recorderlayout_id' : 'flashrecarea',
			'recorder_id' : 'audiorecorder',
			'recorder_name': 'arec',
			'wmode' : 'direct',
			'bgcolor': '#ff0000',
			'swf_path': 'jRecorder.swf',
            'swf_object_path' : '',
			'host': 'acceptfile.php?filename=hello.wav',
			'callback_started_recording' : function(){},
			'callback_finished_recording' : function(){},
			'callback_stopped_recording': function(){},
			'callback_error_recording' : function(){},
			'callback_activityTime': function(time){},
			'callback_activityLevel' : function(level){}
		};
	
		//if option array is passed, merget the values
		if ( options ) { 
	        $.extend( settings, options );
	     }

		jRecorderSettings = settings;

		// embed using swfobject
		var attributes = {
			id: settings['recorder_id'],
			name: settings['recorder_name'],
			bgcolor: settings['bgcolor'],
			wmode: settings['wmode'],
		};

		swfobject.embedSWF(
			settings['swf_path'] + '?host='+ settings['host'],
			settings['recorderlayout_id'],
			settings['rec_width'],
			settings['rec_height'],
            "11.7.0",
 			settings['swf_object_path']+"/expressInstall.swf", 
			false, 
			false, 
			attributes
		);
	};
	
	//function call to start a recording
	$.jRecorder.record = function(max_time){
		getFlashMovie(jRecorderSettings['recorder_id']).jStartRecording(max_time);
	} 

	//function call to stop recording					
	$.jRecorder.stop = function(){
		getFlashMovie(jRecorderSettings['recorder_id']).jStopRecording();
	} 

    $.jRecorder.startPreview = function(){
		getFlashMovie(jRecorderSettings['recorder_id']).jStartPreview();
	} 

	$.jRecorder.stopPreview = function() {
		getFlashMovie(jRecorderSettings['recorder_id']).jStopPreview();
	}
		
	//function call to send wav data to server url from the init configuration					
	$.jRecorder.sendData = function(){
		getFlashMovie(jRecorderSettings['recorder_id']).jSendFileToServer();
	}
 
	//function call to add a parameter to the POST url
	$.jRecorder.addParameter = function(key, val){
		getFlashMovie(jRecorderSettings['recorder_id']).jAddParameter(key, val);
	}
 
	//function call to remove a parameter from the POST url
	$.jRecorder.removeParameter = function(key){
		getFlashMovie(jRecorderSettings['recorder_id']).jRemoveParameter(key);
	} 
	
	$.jRecorder.callback_started_recording = function(){
		jRecorderSettings['callback_started_recording']();
	}
	
	$.jRecorder.callback_finished_recording  = function(){
        jRecorderSettings['callback_finished_recording']();
	}
	
	$.jRecorder.callback_error_recording = function(){
		jRecorderSettings['callback_error_recording']();
	}
	
	$.jRecorder.callback_stopped_recording = function(){
		jRecorderSettings['callback_stopped_recording']();
	}
	
	$.jRecorder.callback_finished_sending = function(response){
		jRecorderSettings['callback_finished_sending'](response);
	}
	
	$.jRecorder.callback_activityLevel = function(level){
		jRecorderSettings['callback_activityLevel'](level);	
	}
	
	$.jRecorder.callback_activityTime = function(time){
		//put back flash while recording
		$(  '#' + jRecorderSettings['recorderlayout_id'] ).css('z-index', -1);
		jRecorderSettings['callback_activityTime'](time);
	}
		
	$.jRecorder.callback_hide_the_flash = function() {
		$('#' + jRecorderSettings['recorder_id']).css('z-index', -1).css('height', '1px');
		jRecorderSettings['callback_hide_the_flash']();
	}
					
	//function to return flash object from id
	function getFlashMovie(movieId) {
          return document.getElementById(movieId);
        }

})(jQuery);
