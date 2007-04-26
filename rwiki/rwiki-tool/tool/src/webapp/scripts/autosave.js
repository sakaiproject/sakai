
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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

var WikiAutoSave_autoSaveObjects = new Array();
var WikiAutoSave__autoSaveObjectID = 0;

 
function WikiAutoSave_restoreSavedContent(pageVersionId, contentId, restoreId,restoreVersionId, restoreDateId , autosaveId, autosaveClass) { 

	var pageVersion = document.getElementById(pageVersionId);
	var restoreVersion = document.getElementById(restoreVersionId);
	var restoreDate = document.getElementById(restoreDateId);
	var pageVersionValue = "none";
	var restoreVersionValue = "none";
	if ( pageVersion ) { pageVersionValue = pageVersion.value; }
	if ( restoreVersion ) { restoreVersionValue = restoreVersion.value; }
	var pageRestore = document.getElementById(restoreId);
	var pageContent = document.getElementById(contentId);
	var autosaveDiv = document.getElementById(autosaveId);
	
	if ( !pageRestore ) {
		alert("There is no content to restore");
		return;
	}
	if ( !pageContent ) {
		alert("There is no place to restore to");
		return;
	}
	 
	if ( pageVersionValue != restoreVersionValue ) {
	// FIXME: Internationalize
		if ( !confirm("Are you certain you want to restore this content, the saved content was not from the same version of this page as the one you are currently editing\n--------------------------------------------------\nSaved Version: "+restoreVersionValue+"\nCurrent Version:"+pageVersionValue) ) {
			return;
		}
	}
	pageContent.value = pageRestore.value;
	pageRestore.value = "";
	if ( restoreVersion ) { restoreVersion.value = "none" };
	if ( restoreDate ) { restoreDate.value = "none" };
	if ( autosaveDiv ) { 
		autosaveDiv.className = autosaveClass; 
	}
}

function WikiAutoSave_autoSaveOn(siteId,pageNameId, pageVersionId, restoreTimestampId, contentId, restoreContentId,restoreVersionId,restoreDateId,autosaveId,autosaveClass) {
	var pageName = document.getElementById(pageNameId);
	var pageVersion = document.getElementById(pageVersionId);
	var site = document.getElementById(siteId);
	
	var autoSaver = new WikiAutoSave(0,site.value,pageName.value,pageVersion?pageVersion.value:"none");
	autoSaver.setTargetID(contentId);
	autoSaver.setRestoreTimestampId(restoreTimestampId);
	autoSaver.setRestoreContentsId(restoreContentId);
	autoSaver.setRestoreVersionId(restoreVersionId);
	autoSaver.setRestoreDateId(restoreDateId);
	autoSaver.setRestoreTabId(autosaveId);
	autoSaver.setRestoredClass(autosaveClass);
	
	autoSaver.startAutosave();
	return autoSaver;
}

function WikiAutoSave_doRestore(saveID) 
{
    log("Restore Callback");
	var autoSaver = WikiAutoSave_autoSaveObjects[saveID];
	autoSaver.restoreCallback();	
}




function WikiAutoSave_autoSaveClear(siteId,pageName) {
    try {
	    var autoSaver = new WikiAutoSave(0,siteId,pageName,"none");
	    log("Deleting Content siteId=["+siteId+"] page=["+pageName+"]");
	    autoSaver.startClear();
	    log("Deleted Content siteId=["+siteId+"] page=["+pageName+"]")
    } catch (e) {
	   log("Failed to delete AutoSave Content for "+pageName+": problem "+e);
    }
}

function WikiAutoSave_autoSaveOff(saveID) {
	var autoSaver = WikiAutoSave_autoSaveObjects[saveID];
	autoSaver.stopAutosave();
	autoSaver.deleteContent();
}
//-------------------------------------------------------------------------------------
function WikiAutoSave(autoSaveID,targetSite,targetPageName,targetVersion) 
{
// Set this to true to get some debug
	logInfo = false;
	
	
	this.targetID = null;
	this.interval = 100;
	this.siteId = targetSite; 
	this.pageName = targetPageName;
	this.runs = 50;
	this.lastSavedContent = null;
	this.lastSavedDate = "";
	this.lastSavedVersion = "";
	this.currentPageVersion = targetVersion;
	this.hasSavedPageContent = true;
	this.clearLog = false;	
	this.state = 0;
	
	
	this.restoreTimestampId = null;
	this.restoreContentsId = null;
	this.restoreVersionId = null;

	this.restoreDateId = null;
	this.restoreTabId = null;
	this.restoredClass = null;
	
	this.autoSaveObjectID = WikiAutoSave__autoSaveObjectID;
	WikiAutoSave__autoSaveObjectID++;
	
	log("Flash Brisdge Created");
	this.localStoreObj = FlashBridge_getInstance("localstore");
	log("Flash Brisdge Created");
	WikiAutoSave_autoSaveObjects[this.autoSaveObjectID] = this;
	
};
WikiAutoSave.prototype.restoreCallback = function() {

    try {
    log("Restore call back ");
    var restoreVersion = document.getElementById(this.restoreVersionId);
    if ( restoreVersion && restoreVersion.value == "none"  ) {
    	// not a valid version
    	log("Not A valid Version "+restoreVersion.value);
		return;    	
    }
    
	var contentEl = document.getElementById(this.targetID);
	if ( contentEl == null ) 
	{
		log("Failed to find content form with ID "+this.targetID);
	} 
	var content = contentEl.value;

	var restoredContentEl = document.getElementById(this.restoreContentsId);
	var restoredContent = restoredContentEl.value;

	var restoreDate = document.getElementById(this.restoreDateId);
	var restoreTimestamp = document.getElementById(this.restoreTimestampId);
	var dateTS = restoreTimestamp.value;
	dateTS = parseInt(dateTS);
	log("Date is "+dateTS);
	restoreDate.value = new Date(dateTS);
	restoreDate.size = restoreDate.value.length+1;
	
	if ( restoredContent && restoredContent != content && restoredContent != "no restored content" ) {
		var restoreTab = document.getElementById(this.restoreTabId);
		restoreTab.className = this.restoredClass; 
        log("Restore call back :Recovered Content set "+this.restoreTabId+" to "+this.restoredClass);
	} else {
		if ( restoredContent ) {
        	log("Restore call back :Content The Same");
        } else {
        	log("Restore call back :No Saved Content");
        }
    }
    } catch (e) {
    	log("Callback Failed "+e);
    }
}

WikiAutoSave.prototype.doFunction = function() {
	var argList = arguments;
	var args = new Array();
	var j = 0;
	for ( var i = 1; i < argList.length; i+=2 ) {
		var m = new Object();
		m.name = argList[i];
		m.value = argList[i+1];
		args[j++] = m;
	}
	var m = new Object();
	m.name = "method";
	m.value = argList[0];
	args[args.length] = m;
	
	var InternetExplorer = navigator.appName.indexOf("Microsoft") != -1;
	var flashObj = InternetExplorer ? localstore : document.localstore;
	try {
		this.localStoreObj.doFunction(flashObj,1,args);
	} catch (e) {
	 	log("Problem Invoking Flash Object:"+e);
	}
}


WikiAutoSave.prototype.setTargetID = function (targetID) 
{
	this.targetID = targetID;
};
WikiAutoSave.prototype.setRestoreTimestampId = function (restoreTimestampId) 
{
	this.restoreTimestampId = restoreTimestampId;
};
WikiAutoSave.prototype.setRestoreContentsId = function (restoreContentsId) 
{
	this.restoreContentsId = restoreContentsId;
};
WikiAutoSave.prototype.setRestoreVersionId = function (restoreVersionId) 
{
	this.restoreVersionId = restoreVersionId;
};
WikiAutoSave.prototype.setRestoreDateId = function (restoreDateId) 
{
	this.restoreDateId = restoreDateId;
};
WikiAutoSave.prototype.setRestoreTabId = function (autosaveId) 
{
	this.restoreTabId = autosaveId;
};
WikiAutoSave.prototype.setRestoredClass = function (autosaveClass) 
{
	this.restoredClass = autosaveClass;
};



WikiAutoSave.prototype.deleteContent = function () 
{
	this.doFunction("clearData","zone",this.siteId,"savekey",this.pageName);
};

WikiAutoSave.prototype.saveContent = function () 
{
    log("Start Save Content");
	var contentForm = document.getElementById(this.targetID);
	if ( contentForm == null ) 
	{
		log("Failed to find content form with ID "+this.targetID);
	} 
	var content = contentForm.value;
	var ts = new Date().getTime();
	
	this.doFunction("saveData","zone",this.siteId,"savekey",this.pageName,
					               "contents",content,"timestamp",ts,
					               "version",this.currentPageVersion,
					               "timestampID",this.restoreTimestampId );					               
	log("Save Content Done");
};


WikiAutoSave.prototype.restoreContent = function () 
{
	var content = null;
		
	this.doFunction("getData","zone",this.siteId,"savekey",this.pageName,
	                              "timestampID",this.restoreTimestampId,"contentsID",this.restoreContentsId,
	                              "versionID",this.restoreVersionId,"callback","WikiAutoSave_doRestore('"+this.autoSaveObjectID+"');"  );
	
	// the call back is async, the results will go into the 
}

WikiAutoSave.prototype.startClear = function() 
{
		if ( ! this.running ) 
		{
			this.running = true;
			WikiAutoSave_doClear(this.autoSaveObjectID);
		}
};
WikiAutoSave.prototype.stopClear = function() 
{
		if ( ! this.running ) 
		{
			this.running = false;
		}
};


WikiAutoSave.prototype.startAutosave = function() 
{
		if ( ! this.running ) 
		{
			this.running = true;
			WikiAutoSave_doAutoSave(this.autoSaveObjectID);
		}
};
		
WikiAutoSave.prototype.stopAutosave = function() 
{
		if ( this.running ) 
		{
			this.running = false;
		}
};



// this 




function WikiAutoSave_doAutoSave(saveID) 
{
	var autoSaver = WikiAutoSave_autoSaveObjects[saveID];
	if ( autoSaver != null && autoSaver.running  ) 
	{
		try 
		{
			try 
			{
				if ( autoSaver.clearLog ) 
				{
				    clearLog();
				}
				else 
				{
					autoSaver.clearLog = true;
				}
			   if ( autoSaver.state == 0 ) {
				 // let the dom settle down
				 var InternetExplorer = navigator.appName.indexOf("Microsoft") != -1;
	             var flashObj = InternetExplorer ? localstore : document.localstore;
				 if ( flashObj ) {
				 	autoSaver.state = 1;
				 } else {
					autoSaver.runs--;
				 }
			   } else  if ( autoSaver.state == 1 ) {
			   	autoSaver.restoreContent();
			   	log("Restored Content");
				 autoSaver.state = 2;
				 autoSaver.interval = 10000;
			   } else {
				autoSaver.saveContent();
			   	log("Saveded Content");
				}
			} 
			catch (e)  
			{ 
				log("Error "+e);
			}
			if ( autoSaver.runs == 0 ) 
			{
				window.status = "Auto Save Disabled, No Flash Support ";
			}	 
			else
			{
				window.setTimeout("WikiAutoSave_doAutoSave("+saveID+")",autoSaver.interval);			
			}
		} 
		catch (e)  
		{ 
			log("Error "+e);
			autoSaver.runs = 0;
		}
		
	} else {
	   log("Autosave failed, none found "+saveID);
	}
}


function WikiAutoSave_doClear(saveID) 
{
	var autoSaver = WikiAutoSave_autoSaveObjects[saveID];
	if ( autoSaver != null && autoSaver.running  ) 
	{
		try 
		{
			try 
			{
				if ( autoSaver.clearLog ) 
				{
				    clearLog();
				}
				else 
				{
					autoSaver.clearLog = true;
				}
			   if ( autoSaver.state == 0 ) {
				 // let the dom settle down
				 var InternetExplorer = navigator.appName.indexOf("Microsoft") != -1;
	             var flashObj = InternetExplorer ? localstore : document.localstore;
				 if ( flashObj ) {
				 	autoSaver.state = 1;
				 } else {
					autoSaver.runs--;
				 }
			   } else {
				autoSaver.deleteContent();
				autoSaver.runs = 0;
			   	log("Deleted Content");
			   }
			} 
			catch (e)  
			{ 
				log("Error "+e);
			}
			if ( autoSaver.runs != 0 ) 
			{
				window.setTimeout("WikiAutoSave_doClear("+saveID+")",autoSaver.interval);			
			}
		} 
		catch (e)  
		{ 
			log("Error "+e);
			autoSaver.runs = 0;
		}
		
	} else {
	   log("Autosave failed, none found "+saveID);
	}
}


	
