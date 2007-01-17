
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

var autoSaveObjects = new Array();
var _autoSaveObjectID = 0;
var autoSaver;

function restoreSavedContent(pageVersionId, contentId, restoreId,restoreVersionId, restoreDateId , autosaveId, autosaveClass) { 
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

function autoSaveOn(pageNameId, pageVersionId, contentId, restoreId,restoreVersionId,restoreDateId,autosaveId,autosaveClass) {
        return;
	var pageName = document.getElementById(pageNameId);
	var pageVersion = document.getElementById(pageVersionId);
	var pageRestore = document.getElementById(restoreId);
	var restoreVersion = document.getElementById(restoreVersionId);
	var restoreDate = document.getElementById(restoreDateId);
	var autosaveDiv = document.getElementById(autosaveId);
	autoSaver = new WikiAutoSave(0,pageName.value,pageVersion?pageVersion.value:"none");
	autoSaver.setTargetID(contentId);
	try {
		autoSaver.restoreContent();
		var saved = autoSaver.lastSavedContent;
		var savedDate = autoSaver.lastSavedDate;
		var lastSavedVersion = autoSaver.lastSavedVersion;
		var contentTA = document.getElementById(contentId);
		if ( saved && contentTA.value != saved  ) {
		    pageRestore.value = saved;
		    if ( restoreVersion ) { restoreVersion.value = lastSavedVersion; }
		    if ( restoreDate ) { restoreDate.value = savedDate; }
		    if ( autosaveDiv ) { 
				autosaveDiv.className = autosaveClass; 
			}
		}
	} catch (e) {
  		alert("Failed to restore content "+e);
	}
	autoSaver.startAutosave();
}
function autoSaveClear(pageName) {
    try {
	    autoSaver = new WikiAutoSave(0,pageName,"none");
	    autoSaver.deleteContent();
    } catch (e) {
	   alert("Failed to delete AutoSave Content for "+pageName+": problem "+e);
    }
}

function autoSaveOff() {
	autoSaver.stopAutosave();
	autoSaver.deleteContent();
}

function WikiAutoSave(autoSaveID,targetPageName,targetVersion) 
{
	logInfo = true;
	this.targetID = null;
	this.interval = 10000;
	this.pageName = targetPageName;
	this.runs = 10;
	this.lastSavedContent = null;
	this.lastSavedDate = "";
	this.lastSavedVersion = "";
	this.currentPageVersion = targetVersion;
	this.hasSavedPageContent = true;
	this.clearLog = false;
	
	
	this.autoSaveObjectID = _autoSaveObjectID;
	_autoSaveObjectID++;
	autoSaveObjects[this.autoSaveObjectID] = this;
	
};

WikiAutoSave.prototype.setTargetID = function (targetID) 
{
	this.targetID = targetID;
};
WikiAutoSave.prototype.deleteContent = function () 
{
	var cookieNameBase = this.getCookieBase();
	this.delCookie(cookieNameBase);
	this.delCookie(cookieNameBase+"_date");
	this.delCookie(cookieNameBase+"_version");
	for ( i = 0; i < 30; i++ ) 
	{
	 	var cookieName = cookieNameBase+"_"+i+"X";
	 	this.delCookie(cookieName);
	}
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
	var escapedContent = escape(content);
	var cookieNameBase = this.getCookieBase();
	var icookie = 0;
	if ( escapedContent.length > 3000*30 ) 
	{
		this.delCookie(cookieNameBase);
		this.delCookie(cookieNameBase+"_date");
		this.delCookie(cookieNameBase+"_version");
		for ( i = icookie; i < 30; i++ ) 
		{
		 	var cookieName = cookieNameBase+"_"+i+"X";
		 	this.delCookie(cookieName);
		}
		// FIXME: Internationalize
		alert("There is insufficent space to perform \n" 
		    + "autosaving on this page, please save \n"
		    + "your work at regular intervals to reduce \n"
		    + "the size of this page \n"
		    + "Current Size = "+escapedContent.length+"\n"
 		    + "Limit = "+3000*30);
		this.stopAutosave();
		return;
	}
	else 
	{
		this.setCookie(cookieNameBase,escape(this.pageName),10);
		var ts = new Date();
		this.setCookie(cookieNameBase+"_date",escape(ts),10);
		this.setCookie(cookieNameBase+"_version",this.currentPageVersion,10);
		for ( i = 0; i < escapedContent.length; i+=3500 ) 
		{
	 		var cookieName = cookieNameBase+"_"+icookie+"X";
	 		icookie++;
	 		var cookieValue = escapedContent.substring(i,i+3500);
	 		this.delCookie(cookieName);
	 		this.setCookie(cookieName,cookieValue,10,true);
		}
		
	}
	for ( i = icookie; i < 30; i++ ) 
	{
	 	var cookieName = cookieNameBase+"_"+i+"X";
	 	this.delCookie(cookieName);
	}
	var savedContent = this.getSavedContent();
	if ( savedContent != content ) {
		log("ERROR SAVING CONTENT got "+savedContent);
		log("Length was "+savedContent.length+" should have been "+content.length);
	}
	log("Save Content Done");
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



WikiAutoSave.prototype.restoreContent = function () 
{
	var content = null;
	var cookieNameBase = this.getCookieBase();
	
	var savedPageName = unescape(this.getCookie(cookieNameBase));
	var dateSaved = unescape(this.getCookie(cookieNameBase+"_date"));
	var savedVersion = unescape(this.getCookie(cookieNameBase+"_version"));
	if ( savedPageName == this.pageName ) {
		content = this.getSavedContent();
		if ( content != null ) 
		{
			this.lastSavedContent = content;
			this.lastSavedDate = dateSaved;
			this.lastSavedVersion = savedVersion;
			this.hasSavedPageContent = true;
			log("There is saved content for page "+savedPageName+"::"+content);
		} else {
			log("There is no saved content "+content);
			
		}
	} else {
		log("Saved Content not from this page, this page is  "+this.pageName+": savedPage "+savedPageName);
	}
	if ( this.hasSavePageContent )  
	{
		log("No Saved Content Present");
	}
	log("Restore Content Done");
};

WikiAutoSave.prototype.getSavedContent = function() 
{
	var content = null;
	var cookieNameBase = this.getCookieBase();
	for ( i = 0; i < 30; i++ ) 
	{
	 	var cookieName = cookieNameBase+"_"+i+"X";
	 	var value = this.getCookie(cookieName);
	 	if ( value != null ) 
	 	{
	 		if ( content == null ) 
	 		{
	 			content = value;
	 		} 
	 		else 
	 		{
	 				content += value;
	 		}
	 	}
	}
        if ( content != null ) {
		content = unescape(content);
	}
	return content;
}; 

WikiAutoSave.prototype.getCookieBase = function() 
{
	return "was_"+this.autoSaveObjectID;
}; 
	
	
WikiAutoSave.prototype.getCookie = function(NameOfCookie) 
{ 
	if (document.cookie.length > 0) 
	{ 
		begin = document.cookie.indexOf(NameOfCookie+"=");	
		if (begin != -1) 
		{ 
			begin += NameOfCookie.length+1;
			end = document.cookie.indexOf(";", begin);
			if (end == -1) end = document.cookie.length;
			return document.cookie.substring(begin, end); 
		}
	}
	return null;
};


WikiAutoSave.prototype.setCookie = function(NameOfCookie, value, expiredays) 
{ 
	var ExpireDate = new Date ();
	ExpireDate.setTime(ExpireDate.getTime() + (expiredays * 24 * 3600 * 1000));
	var cookieSet = NameOfCookie + "=" + value +
		((expiredays == null) ? "" : "; expires=" + ExpireDate.toGMTString());
	document.cookie = cookieSet;
	var cookieVal = this.getCookie(NameOfCookie);
	if ( cookieVal != value)  {
		log("Failed to set cookie "+NameOfCookie+" got "+ cookieVal);
	}
};


WikiAutoSave.prototype.delCookie = function(NameOfCookie) 
{ 
	if (this.getCookie(NameOfCookie)) 
	{	
		document.cookie = NameOfCookie + "=" +
			"; expires=Thu, 01-Jan-70 00:00:01 GMT";
	}
};

// this 

function WikiAutoSave_doAutoSave(saveID) 
{
	var autoSaver = autoSaveObjects[saveID];
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
				autoSaver.saveContent();
			//	autoSaver.runs--;
			} 
			catch (e)  
			{ 
				alert("Error "+e);
			}
			if ( autoSaver.runs == 0 ) 
			{
				alert("Done all ");
			}	 
			else
			{
				window.setTimeout("WikiAutoSave_doAutoSave("+saveID+")",autoSaver.interval);			
			}
		} 
		catch (e)  
		{ 
			alert("Error "+e);
		}
		
	} else {
	   log("Autosave failed, none found "+saveID);
	}
}

	
