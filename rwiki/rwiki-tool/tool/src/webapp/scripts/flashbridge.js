/*
  Copyright 2007 Ian Boston (ieb@tfd.co.uk)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 
*/
// get a global reference
var global = this;
var flashBridgeObj = new Array();

function FlashBridge_getInstance(flashId) {
	var flashBridge = flashBridgeObj[flashId];
	if ( flashBridge == null ) {
		flashBridge = new FlashBridge(flashId);
	}
	return flashBridge;
}

/**
* This is a bridging object that allows asynchronouse messageing between javascript and 
* flash.
* In the flash component there must be at least 3 frames,
* When the flash comoinent is moved to frame 1 it should process the variables that
* have been set.
* it may also call back with fscommand containing a ; seperated list of variable names
* which will be transfered into hidden input tags of the same name on the javascript site.
*
*/
function FlashBridge(flashid) {
	this.id = flashid;
	uid = (new Date()).getTime();
	flashBridgeObj[flashid] = this;		
}

/**
* This is a call back function 
* it has 3 commands, setdom which sets form values, eval which evaluates javascript
* and writedom which writes the argument to the dom.
*/
FlashBridge.prototype.doUpdate = function(command,args) {
    log("Doing Update "+command+" "+args);
	if ( command == "globaleval" )  {
		global.eval(args);
	} else if ( command == "eval" )  {
		this.eval(args);
	} else if ( command == "writedom" )  {
		document.write(args);
	} else {
		log("Callback Command not recognized "+command+"(["+args+"])");
	}
} 
/**
* This expects the method followed by an array of parameters each parameter is an 
* object with a .name and a .value once the values have been set the flash will be 
* sent the message by advancing to frame 1
*/
FlashBridge.prototype.doFunction = function(flashObj,frame,args) {
	log("Flash Obj is ["+flashObj+"]");
	for(i = 0; i < args.length; i++ ){
	    log("Setting "+args[i].name+" to "+args[i].value);
		flashObj.SetVariable(args[i].name,args[i].value);
	}	
	log("Going to frame "+frame);
	flashObj.GotoFrame(frame);
}
