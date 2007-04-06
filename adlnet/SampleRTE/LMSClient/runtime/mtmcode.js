// Morten's JavaScript Tree Menu
// version 2.3.0, dated 2001-04-30
// http://www.treemenu.com/

// Copyright (c) 2001, Morten Wang & contributors
// All rights reserved.

// This software is released under the following copyright statement which can 
// also be accessed through the WWW at http://www.treemenu.com/license.txt.
// Copyright (c) 2001, Morten Wang & contributors
// All rights reserved.

// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:

// * Redistributions of source code must retain the above copyright notice,
//    this list of conditions and the following disclaimer.

//  * Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.

//  * Neither the name "Morten's JavaScript Tree Menu" nor the names of its
//    contributors may be used to endorse or promote products derived from this
//    software without specific prior written permission.

// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
// THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


/******************************************************************************
* Define the MenuItem object.                                                 *
******************************************************************************/
function MTMenuItem(text, url, target, tooltip, icon) {
  this.text = text;
  this.url = url ? url : "";
  this.target =  target ? target : "";
  this.tooltip = tooltip;
  this.icon = icon ? icon : "";

  this.number = MTMNumber++;

  this.submenu     = null;
  this.expanded    = true;
  this.MTMakeSubmenu = MTMakeSubmenu;
}

function MTMakeSubmenu(menu, isExpanded, collapseIcon, expandIcon) {
  this.submenu = menu;
  this.expanded = true;
  this.collapseIcon = collapseIcon ? collapseIcon : "menu_folder_closed.gif";
  this.expandIcon = expandIcon ? expandIcon : "menu_folder_open.gif";
}

/******************************************************************************
* Define the Menu object.                                                     *
******************************************************************************/

function MTMenu() {
  this.items   = new Array();
  this.MTMAddItem = MTMAddItem;
}

function MTMAddItem(item) {
  this.items[this.items.length] = item;
}

/******************************************************************************
* Define the icon list, addIcon function and MTMIcon item.                    *
******************************************************************************/

function IconList() {
  this.items = new Array();
  this.addIcon = addIcon;
}

function addIcon(item) {
  this.items[this.items.length] = item;
}

function MTMIcon(iconfile, match, type) {
  this.file = iconfile;
  this.match = match;
  this.type = type;
}

/******************************************************************************
* The MTMBrowser object.  A custom "user agent" that'll define the browser    *
* seen from the menu's point of view.                                         *
******************************************************************************/

function MTMBrowser() {
  this.cookieEnabled = false;
  this.preHREF = "";
  this.MTMable = false;
  this.cssEnabled = true;
  this.browserType = "other";

  if(navigator.appName == "Netscape" && navigator.userAgent.indexOf("WebTV") == -1) {
    if(parseInt(navigator.appVersion) == 3 && (navigator.userAgent.indexOf("Opera") == -1)) {
      this.MTMable = true;
      this.browserType = "NN3";
      this.cssEnabled = false;

    } else if(parseInt(navigator.appVersion) >= 4) {
      this.MTMable = true;
      this.browserType = parseInt(navigator.appVersion) == 4 ? "NN4" : "NN5";
    }
  } else if(navigator.appName == "Microsoft Internet Explorer" && parseInt(navigator.appVersion) >= 4) {
    this.MTMable = true;
    this.browserType = "IE4";
  } else if(navigator.appName == "Opera" && parseInt(navigator.appVersion) >= 5) {
    this.MTMable = true;
    this.browserType = "O5";
  }

  if(this.browserType != "NN4") {
    this.preHREF = location.href.substring(0, location.href.lastIndexOf("/") +1)
  }
}

/******************************************************************************
* Global variables.  Not to be altered unless you know what you're doing.     *
* User-configurable options are at the end of this document.                  *
******************************************************************************/

var MTMLoaded = false;
var MTMLevel;
var MTMBar = new Array();
var MTMIndices = new Array();

var MTMUA = new MTMBrowser();

var MTMClickedItem = false;
var MTMExpansion = false;

var MTMNumber = 1;
var MTMTrackedItem = false;
var MTMTrack = true;
var MTMFrameNames;

var MTMFirstRun = true;
var MTMCurrentTime = 0; // for checking timeout.
var MTMUpdating = false;
var MTMWinSize, MTMyval, MTMxval;
var MTMOutputString = "";

var MTMCookieString = "";
var MTMCookieCharNum = 0; // cookieString.charAt()-number

var anchorSet = false;	// an anchor tag is associated with a menu choice


/*****************************************************************************
*  Function to check status of current item so user cannot move to next before *
*  finishing current item.  Encodes launch string to ensure non-Latin          *
*  characters can be sent without corruption.  Added by ADL team, Feb, 2002    *
*****************************************************************************/
function launchItem(launch)
{	
   var launchString = "pleaseWait.jsp?scoID=" + launch;	
   window.top.frames['Content'].location.href = (encodeURI(launchString));
}


/******************************************************************************
* Code that picks up frame names of frames in the parent frameset.            *
******************************************************************************/

function MTMgetFrames() {
  if(MTMUA.MTMable) {
    MTMFrameNames = new Array();
    for(i = 0; i < parent.frames.length; i++) {
      MTMFrameNames[i] = parent.frames[i].name;
    }
  }
}

/******************************************************************************
* Functions to draw the menu.                                                 *
******************************************************************************/

function MTMSubAction(SubItem) {

  SubItem.expanded = (SubItem.expanded) ? false : true;
  if(SubItem.expanded) {
    MTMExpansion = true;
  }

  MTMClickedItem = SubItem.number;

  if(MTMTrackedItem && MTMTrackedItem != SubItem.number) {
    MTMTrackedItem = false;
  }

  if(MTMEmulateWE || SubItem.url == "" || !SubItem.expanded) {
    setTimeout("MTMDisplayMenu()", 10);
    return false;
  } else {
    return true;
  }
}

function MTMStartMenu() {
  MTMLoaded = true;
  if(MTMFirstRun) {
    MTMCurrentTime++;
    if(MTMCurrentTime == MTMTimeOut) { // call MTMDisplayMenu
      setTimeout("MTMDisplayMenu()",1);
    } else {
      setTimeout("MTMStartMenu()",1);
    }
  } 
}

function MTMDisplayMenu() {
  if(MTMUA.MTMable && !MTMUpdating) {
    MTMUpdating = true;

    if(MTMFirstRun) {
      MTMgetFrames();
      if(MTMUseCookies) { MTMFetchCookie(); }
    }

    if(MTMTrack) { MTMTrackedItem = MTMTrackExpand(menu); }

    if(MTMExpansion && MTMSubsAutoClose) { MTMCloseSubs(menu); }

    MTMLevel = 0;
    MTMDoc = parent.frames[MTMenuFrame].document
    MTMDoc.open("text/html", "replace");
    MTMOutputString = '<html><head>\n';
        
        
    if(MTMLinkedSS) {
      MTMOutputString += '<link rel="stylesheet" type="text/css" href="' + MTMUA.preHREF + MTMSSHREF + '>\n';
    } else if(MTMUA.cssEnabled) {
      MTMOutputString += '<style type="text/css">\nbody {\n\tcolor:' + MTMTextColor + ';\n}\n';
      MTMOutputString += '#root {\n\tcolor:' + MTMRootColor + ';\n\tbackground:transparent;\n\tfont-family:' + MTMRootFont + ';\n\tfont-size:' + MTMRootCSSize + ';\n}\n';
      MTMOutputString += 'a {\n\tfont-family:' + MTMenuFont + ';\n\tfont-size:' + MTMenuCSSize + ';\n\ttext-decoration:none;\n\tcolor:' + MTMLinkColor + ';\n\tbackground:transparent;\n}\n';
      MTMOutputString += MTMakeA('pseudo', 'hover', MTMAhoverColor);
      MTMOutputString += MTMakeA('class', 'tracked', MTMTrackColor);
      MTMOutputString += MTMakeA('class', 'subexpanded', MTMSubExpandColor);
      MTMOutputString += MTMakeA('class', 'subclosed', MTMSubClosedColor) + MTMExtraCSS + '\n<\/style>\n';
    }
        
    MTMOutputString += '<\/head>\n<body ';
    
    if(MTMBackground != "") {
      MTMOutputString += 'background="' + MTMUA.preHREF + MTMenuImageDirectory + MTMBackground + '" ';
    }
    MTMOutputString += 'bgcolor="' + MTMBGColor + '" text="' + MTMTextColor + '" link="' + MTMLinkColor + '" vlink="' + MTMLinkColor + '" alink="' + MTMLinkColor + '">\n';
    
    MTMOutputString += MTMHeader + '\n<table border="0" cellpadding="0" cellspacing="0" width="' + MTMTableWidth + '">\n';
    MTMOutputString += '<tr valign="top"><td nowrap><img src="' + MTMUA.preHREF + MTMenuImageDirectory + MTMRootIcon + '" align="left" border="0" vspace="0" hspace="0"  alt="ADL Logo">';
    if(MTMUA.cssEnabled) {
      MTMOutputString += '<span id="root">&nbsp;' + MTMenuText + '<\/span>';
    } else {
      MTMOutputString += '<font size="' + MTMRootFontSize + '" face="' + MTMRootFont + '" color="' + MTMRootColor + '">' + MTMenuText + '<\/font>';
    }
    MTMDoc.writeln(MTMOutputString + '</td></tr>');

    MTMListItems(menu);


	// Only center list if anchor tag is created
	var MenuCenterScript = '';	
	if(anchorSet == true)
	{
		MenuCenterScript += '\n<script type="text/javascript">\n' +
		   'var anchor = document.getElementById("currentAnchor");\n' +
		   'var distance = 0;if (anchor.offsetParent){while ' +
		   '(anchor.offsetParent){distance += anchor.offsetTop; anchor' +
		   ' = anchor.offsetParent;}}else if (anchor.y)distance += anchor.y;\n' +
		   'frameHeight = document.body.clientHeight;\n' +
		   'scrollBy(0,(distance-frameHeight/2));\n' +
		   '</script>\n\n';
	}			   	
	MTMDoc.writeln('<\/table>\n' + MTMFooter + '\n' + MenuCenterScript + '<\/body>\n<\/html>');	 	
	

	    
    MTMDoc.close();

    if(MTMUA.browserType == "NN5") {
      parent.frames[MTMenuFrame].scrollTo(0, 0);
    }

    if((MTMClickedItem || MTMTrackedItem) && MTMUA.browserType != "NN3" && !MTMFirstRun) {
      MTMItemName = "sub" + (MTMClickedItem ? MTMClickedItem : MTMTrackedItem);
      if(document.layers && parent.frames[MTMenuFrame].scrollbars) {
        MTMyval = parent.frames[MTMenuFrame].document.anchors[MTMItemName].y;
        MTMWinSize = parent.frames[MTMenuFrame].innerHeight;
      } else if(MTMUA.browserType != "O5") {
        if(MTMUA.browserType == "NN5") {
          parent.frames[MTMenuFrame].document.all = parent.frames[MTMenuFrame].document.getElementsByTagName("*");
        }
        MTMyval = MTMGetYPos(parent.frames[MTMenuFrame].document.all[MTMItemName]);
        MTMWinSize = MTMUA.browserType == "NN5" ? parent.frames[MTMenuFrame].innerHeight : parent.frames[MTMenuFrame].document.body.offsetHeight;
      }
      if(MTMyval > (MTMWinSize - 60)) {
        parent.frames[MTMenuFrame].scrollBy(0, parseInt(MTMyval - (MTMWinSize * 1/3)));
      }
    }

    if(!MTMFirstRun && MTMUA.cookieEnabled) { 
      if(MTMCookieString != "") {
        setCookie(MTMCookieName, MTMCookieString.substring(0,4000), MTMCookieDays);
      } else {
        setCookie(MTMCookieName, "", -1);
      }
    }

    MTMFirstRun = false;
    MTMClickedItem = false;
    MTMExpansion = false;
    MTMTrack = false;
    MTMCookieString = "";
  }
MTMUpdating = false;
}

function MTMListItems(menu) {
  var i, isLast;
  for (i = 0; i < menu.items.length; i++) {
    MTMIndices[MTMLevel] = i;
    isLast = (i == menu.items.length -1);
    MTMDisplayItem(menu.items[i], isLast);

    if(menu.items[i].submenu && menu.items[i].expanded) {
      MTMBar[MTMLevel] = (isLast) ? false : true;
      MTMLevel++;
      MTMListItems(menu.items[i].submenu);
      MTMLevel--;
    } else {
      MTMBar[MTMLevel] = false;
    } 
  }
}

function MTMDisplayItem(item, last) {
  var i, img;

  var MTMfrm = "parent.frames['code']";
  var MTMref = '.menu.items[' + MTMIndices[0] + ']';

  if(MTMLevel > 0) {
    for(i = 1; i <= MTMLevel; i++) {
      MTMref += ".submenu.items[" + MTMIndices[i] + "]";
    }
  }

  if(MTMUA.cookieEnabled) {
    if(MTMFirstRun && MTMCookieString != "") {
      item.expanded = (MTMCookieString.charAt(MTMCookieCharNum++) == "1") ? true : false;
    } else {
      MTMCookieString += (item.expanded) ? "1" : "0";
    }
  }

  if(item.submenu) {
    var usePlusMinus = false;
    if(MTMSubsGetPlus.toLowerCase() == "always" || MTMEmulateWE) {
      usePlusMinus = true;
    } else if(MTMSubsGetPlus.toLowerCase() == "submenu") {
      for (i = 0; i < item.submenu.items.length; i++) {
        if (item.submenu.items[i].submenu) {
          usePlusMinus = true; break;
        }
      }
    }

    var MTMClickCmd = "return " + MTMfrm + ".MTMSubAction(" + MTMfrm + MTMref + ");";
    var MTMouseOverCmd = "parent.status='" + (item.expanded ? "Collapse " : "Expand ") + (item.text.indexOf("'") != -1 ? MTMEscapeQuotes(item.text) : item.text) + "';return true;";
    var MTMouseOutCmd = "parent.status=parent.defaultStatus;return true;";
  }

  MTMOutputString = '<tr valign="top"><td nowrap>';
  if(MTMLevel > 0) {
    for (i = 0; i < MTMLevel; i++) {
      MTMOutputString += (MTMBar[i]) ? MTMakeImage("menu_bar.gif") : MTMakeImage("menu_pixel.gif");
    }
  }

  if(item.submenu && usePlusMinus) {
      if (item.tooltip == "curr")
      {   
          MTMOutputString += '<font color="' + MTMRootColor + '" size="' + "+1" +  '" face="' + MTMenuFont+ '">';   //'" size="' + MTMenuFontSize + '" face="' + "Tahoma" + '">';
      }


    if(item.url == "") { 
      MTMOutputString += MTMakeLink(item, true, true, true);
    } else {
      if(MTMEmulateWE) {
        MTMOutputString += MTMakeLink(item, true, true, false, MTMClickCmd, MTMouseOverCmd, MTMouseOutCmd);
      } else {
        if(!item.expanded) {
          MTMOutputString += MTMakeLink(item, false, true, true, MTMClickCmd, MTMouseOverCmd, MTMouseOutCmd);
        } else {
          MTMOutputString += MTMakeLink(item, true, true, false, MTMClickCmd, MTMouseOverCmd, MTMouseOutCmd);
        }
      }
    }

    if(item.expanded) {
      img = (last) ? "menu_corner_minus.gif" : "menu_tee_minus.gif";
    } else {
      img = (last) ? "menu_corner_plus.gif" : "menu_tee_plus.gif";
    }
  } else {
    img = (last) ? "menu_corner.gif" : "menu_tee.gif";
  }
  MTMOutputString += MTMakeImage(img);

  if(item.submenu) { 
      
   if(MTMEmulateWE && item.url != "") {  
       if (item.tooltip == "curr")
       {    
           MTMOutputString += '<font color="' + MTMRootColor + '" size="' + "+1" +  '" face="' + MTMenuFont+ '">';   //'" size="' + MTMenuFontSize + '" face="' + "Tahoma" + '">';
       }


      MTMOutputString += '</a>' + MTMakeLink(item, false, false, true);
    } else if(!usePlusMinus) {
      if(item.url == "") {
        MTMOutputString += MTMakeLink(item, true, true, true, MTMClickCmd, MTMouseOverCmd, MTMouseOutCmd);
      } else if(!item.expanded) {
        MTMOutputString += MTMakeLink(item, false, true, true, MTMClickCmd);
      } else {
        MTMOutputString += MTMakeLink(item, true, true, false, MTMClickCmd, MTMouseOverCmd, MTMouseOutCmd);
      }
    }

    img = (item.expanded) ? item.expandIcon : item.collapseIcon;
  } else {
     if (item.URL != "")
     { 
        MTMOutputString += MTMakeLink(item, false, true, true, false, false, false);
        img = (item.icon != "") ? item.icon : MTMFetchIcon(item.url);

     }
      }

  MTMOutputString += MTMakeImage(img);

  if(item.submenu && item.url != "" && item.expanded && !MTMEmulateWE) {
    MTMOutputString += '</a>' + MTMakeLink(item, false, false, true);
  }

  if(MTMUA.browserType == "NN3" && !MTMLinkedSS) {
    var stringColor;
    if(item.submenu && (item.url == "") && (item.number == MTMClickedItem)) {
      stringColor = (item.expanded) ? MTMSubExpandColor : MTMSubClosedColor;
    } else if(MTMTrackedItem && MTMTrackedItem == item.number) {
      stringColor = MTMTrackColor;
    } else {
      stringColor = MTMLinkColor;
    }
    MTMOutputString += '<font color="' + stringColor + '" size="' + MTMenuFontSize + '" face="' + MTMenuFont + '">';
  }
  if (item.tooltip == "curr")
  {    
      MTMOutputString += '<b><font color="' + MTMRootColor + '" size="' + "-1" +  '" face="' + MTMenuFont+ '">';   //'" size="' + MTMenuFontSize + '" face="' + "Tahoma" + '">';
  }
  else if (item.url == "")
  {   
     if ( item.tooltip == "nonsel" )
      {    
     	MTMOutputString += '<font color="' + MTMRootColor + '" size="' + "-1" +  '" face="' + MTMenuFont+ '">';   //'" size="' + MTMenuFontSize + '" face="' + "Tahoma" + '">';
      }
      else
      {
         MTMOutputString += '<font color="#808080"' + '" size="' + "-1" +  '" face="' + MTMenuFont+ '">';
      }
 }
  MTMOutputString += '&nbsp;' + item.text + ((MTMUA.browserType == "NN3" && !MTMLinkedSS) ? '</font>' : '') + ((item.url == "") ? '</font>' : '') + ((item.url != "") ? '</a>' : '') ;
  
  
  	// Place anchor tag at current course
	if (item.tooltip == "curr")
	{
  		MTMOutputString += '<a name="currentAnchor" id="currentAnchor"> </a>';
  		anchorSet = true;  		
	}
	  
  
  MTMDoc.writeln(MTMOutputString + '</td></tr>');
  }

function MTMEscapeQuotes(myString) {
  var newString = "";
  var cur_pos = myString.indexOf("'");
  var prev_pos = 0;
  while (cur_pos != -1) {
    if(cur_pos == 0) {
      newString += "\\";
    } else if(myString.charAt(cur_pos-1) != "\\") {
      newString += myString.substring(prev_pos, cur_pos) + "\\";
    } else if(myString.charAt(cur_pos-1) == "\\") {
      newString += myString.substring(prev_pos, cur_pos);
    }
    prev_pos = cur_pos++;
    cur_pos = myString.indexOf("'", cur_pos);
  }
  return(newString + myString.substring(prev_pos, myString.length));
}

function MTMTrackExpand(thisMenu) {
  var i, targetPath, targetLocation;
  var foundNumber = false;
  for(i = 0; i < thisMenu.items.length; i++) {
    if(thisMenu.items[i].url != "" && MTMTrackTarget(thisMenu.items[i].target)) {
      targetLocation = parent.frames[thisMenu.items[i].target].location;
      targetPath = targetLocation.pathname + targetLocation.search;
      if(MTMUA.browserType == "IE4" && targetLocation.protocol == "file:") {
        var regExp = /\\/g;
        targetPath = targetPath.replace(regExp, "\/");
      }
      if(targetPath.lastIndexOf(thisMenu.items[i].url) != -1 && (targetPath.lastIndexOf(thisMenu.items[i].url) + thisMenu.items[i].url.length) == targetPath.length) {
        return(thisMenu.items[i].number);
      }
    }
    if(thisMenu.items[i].submenu) {
      foundNumber = MTMTrackExpand(thisMenu.items[i].submenu);
      if(foundNumber) {
        if(!thisMenu.items[i].expanded) {
          thisMenu.items[i].expanded = true;
          if(!MTMClickedItem) { MTMClickedItem = thisMenu.items[i].number; }
          MTMExpansion = true;
        }
        return(foundNumber);
      }
    }
  }
return(foundNumber);
}

function MTMCloseSubs(thisMenu) {
  var i, j;
  var foundMatch = false;
  for(i = 0; i < thisMenu.items.length; i++) {
    if(thisMenu.items[i].submenu && thisMenu.items[i].expanded) {
      if(thisMenu.items[i].number == MTMClickedItem) {
        foundMatch = true;
        for(j = 0; j < thisMenu.items[i].submenu.items.length; j++) {
          if(thisMenu.items[i].submenu.items[j].expanded) {
            thisMenu.items[i].submenu.items[j].expanded = false;
          }
        }
      } else {
        if(foundMatch) {
          thisMenu.items[i].expanded = false; 
        } else {
          foundMatch = MTMCloseSubs(thisMenu.items[i].submenu);
          if(!foundMatch) {
            thisMenu.items[i].expanded = false;
          }
        }
      }
    }
  }
return(foundMatch);
}

function MTMFetchIcon(testString) {
  var i;
  for(i = 0; i < MTMIconList.items.length; i++) {
    if((MTMIconList.items[i].type == 'any') && (testString.indexOf(MTMIconList.items[i].match) != -1)) {
      return(MTMIconList.items[i].file);
    } else if((MTMIconList.items[i].type == 'pre') && (testString.indexOf(MTMIconList.items[i].match) == 0)) {
      return(MTMIconList.items[i].file);
    } else if((MTMIconList.items[i].type == 'post') && (testString.indexOf(MTMIconList.items[i].match) != -1)) {
      if((testString.lastIndexOf(MTMIconList.items[i].match) + MTMIconList.items[i].match.length) == testString.length) {
        return(MTMIconList.items[i].file);
      }
    }
  }
return("menu_link_default.gif");
}

function MTMGetYPos(myObj) {
  return(myObj.offsetTop + ((myObj.offsetParent) ? MTMGetYPos(myObj.offsetParent) : 0));
}

function MTMCheckURL(myURL) {
  var tempString = "";
  if((myURL.indexOf("http://") == 0) || (myURL.indexOf("https://") == 0) || (myURL.indexOf("mailto:") == 0) || (myURL.indexOf("ftp://") == 0) || (myURL.indexOf("telnet:") == 0) || (myURL.indexOf("news:") == 0) || (myURL.indexOf("gopher:") == 0) || (myURL.indexOf("nntp:") == 0) || (myURL.indexOf("javascript:") == 0)) {
    tempString += myURL;
  } else {
    tempString += MTMUA.preHREF + myURL;
  }
  
return(tempString);
}

function MTMakeLink(thisItem, voidURL, addName, addTitle, clickEvent, mouseOverEvent, mouseOutEvent) {
   var tempString = "";
   
  if(thisItem.url == "")
  {
     return tempString;
  }
  else
    {
     tempString += '<a href="' + (voidURL ? 'javascript:;' : MTMCheckURL(thisItem.url)) + '" ';
             
     if(MTMUseToolTips && addTitle && thisItem.tooltip) {
       tempString += 'title="' + thisItem.tooltip + '" ';
     }
     if(addName) {
       tempString += 'name="sub' + thisItem.number + '" ';
     }
     if(clickEvent) {
       tempString += 'onclick="' + clickEvent + '" ';
     }
     if(mouseOverEvent && mouseOverEvent != "") {
       tempString += 'onmouseover="' + mouseOverEvent + '" ';
     }
     if(mouseOutEvent && mouseOutEvent != "") {
       tempString += 'onmouseout="' + mouseOutEvent + '" ';
     }
     if(thisItem.submenu && MTMClickedItem && thisItem.number == MTMClickedItem) {
       tempString += 'class="' + (thisItem.expanded ? "subexpanded" : "subclosed") + '" ';
     } else if(MTMTrackedItem && thisItem.number == MTMTrackedItem) {
       tempString += 'class="tracked"';
     }
     if(thisItem.target != "") {
       tempString += 'target="' + thisItem.target + '" ';
     }
     tempString += '>';
  }
    return(tempString);
}

function MTMakeImage(thisImage) {
  var altString = "";
  if ( thisImage == "menu_corner.gif" ) 
  {
     altString = "Menu Connector";
  }
  else if ( thisImage == "menu_tee.gif" )
  {
     altString = "Menu Connector";
  }
  else if ( thisImage == "menu_folder_closed.gif" ) 
  {
     altString = "Collapsed Cluster";
  }
  else if ( thisImage == "menu_folder_open.gif") 
  {
     altString = "Expanded Cluster";
  }
  else if ( thisImage == "menu_link_default.gif" ) 
  {
     altString = "Leaf";
  }
  else if ( thisImage == "menu_corner_minus.gif" )
  {
     altString = "Collapse Cluster";
  }
  else if ( thisImage == "menu_corner_plus.gif" ) 
  {
     altString = "Expand Cluster";
  }
  else if ( thisImage == "menu_tee_minus.gif" ) 
  {
     altString = "Collapse Cluster";
  }
  else if ( thisImage == "menu_tee_plus.gif" ) 
  {
     altString = "Expand Cluster";
  }

  return('<img src="' + MTMUA.preHREF + MTMenuImageDirectory + thisImage + '" alt="' + altString + '" align="left" border="0" vspace="0" hspace="0" width="18" height="18">');
}

function MTMakeBackImage(thisImage) {
  var tempString = 'transparent url("' + ((MTMUA.preHREF == "") ? "" : MTMUA.preHREF);
  tempString += MTMenuImageDirectory + thisImage + '")'
  return(tempString);
}

function MTMakeA(thisType, thisText, thisColor) {
  var tempString = "";
  tempString += 'a' + ((thisType == "pseudo") ? ':' : '.');
  return(tempString + thisText + ' {\n\tcolor:' + thisColor + ';\n\tbackground:transparent;\n}\n');
}

function MTMTrackTarget(thisTarget) {
  if(thisTarget.charAt(0) == "_") {
    return false;
  } else {
    for(i = 0; i < MTMFrameNames.length; i++) {
      if(thisTarget == MTMFrameNames[i]) {
        return true;
      }
    }
  }
  return false;
}

function MTMFetchCookie() {
  var cookieString = getCookie(MTMCookieName);
  if(cookieString == null) { // cookie wasn't found
    setCookie(MTMCookieName, "Say-No-If-You-Use-Confirm-Cookies");
    cookieString = getCookie(MTMCookieName);
    MTMUA.cookieEnabled = (cookieString == null) ? false : true;
    return;
  }

  MTMCookieString = cookieString;
  MTMUA.cookieEnabled = true;
}

// These are from Netscape's Client-Side JavaScript Guide.
// setCookie() is altered to make it easier to set expiry.

function getCookie(Name) {
  var search = Name + "="
  if (document.cookie.length > 0) { // if there are any cookies
    offset = document.cookie.indexOf(search)
    if (offset != -1) { // if cookie exists
      offset += search.length
      // set index of beginning of value
      end = document.cookie.indexOf(";", offset)
      // set index of end of cookie value
      if (end == -1)
        end = document.cookie.length
      return unescape(document.cookie.substring(offset, end))
    }
  }
}

function setCookie(name, value, daysExpire) {
  if(daysExpire) {
    var expires = new Date();
    expires.setTime(expires.getTime() + 1000*60*60*24*daysExpire);
  }
  document.cookie = name + "=" + escape(value) + (daysExpire == null ? "" : (";expires=" + expires.toGMTString())) + ";path=/";
}


  
      