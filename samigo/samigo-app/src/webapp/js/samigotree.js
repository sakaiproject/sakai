/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * Portions of this software are based upon public domain software
 * originally written at the National Center for Supercomputing Applications,
 * University of Illinois, Urbana-Champaign.
 *
 */

 
var checkflag = "false";

function checkAll(field) {
if (field != null) {
  if (field.length >0){
// for more than one checkbox
    if (checkflag == "false") {
       for (i = 0; i < field.length; i++) {
           field[i].checked = true;}
       checkflag = "true";
       return "Uncheck all"; }
    else {
       for (i = 0; i < field.length; i++) {
           field[i].checked = false; }
       checkflag = "false";
       return "Check all"; }
  }
  else {
// for only one checkbox
    if (checkflag == "false") {
  field.checked = true;
  checkflag = "true";
  return "Uncheck all"; }
else {
  field.checked = false; 
  checkflag = "false";
  return "Check all"; }

   }
}
}


function uncheck(field){
      field.checked = false; 
  checkflag = "false";
    return "uncheck"; 
}



// this is for movePool. uncheck all other radio buttons when one is checked, to make it  behave like they are in an array.

function uncheckOthers(field){
 var fieldname = field.getAttribute("name");
 var tables= document.getElementsByTagName("TABLE");

  for (var i = 0; i < tables.length; i++) {
        if ( tables[i].id.indexOf("radiobtn") >=0){
 		var radiobtn = tables[i].getElementsByTagName("INPUT")[0];
// go through the radio buttons, if it's the one clicked on, uncheck all others
	if (fieldname!=radiobtn.getAttribute("name")){
		radiobtn.checked = false;
}
        } 
  }
 
var selectId =  field.getAttribute("value");
var inputhidden = document.getElementById("movePool:selectedRadioBtn");
inputhidden.setAttribute("value", selectId);
 
}



// The following have been modified based on tigris tree javascript 

function flagRows(){
//return;
  var divs = document.getElementsByTagName("A");
  for (var i = 0; i < divs.length; i++) {
     var d = divs[i];
     if (d.className == "treefolder"){
        d.style.backgroundImage = "url(${pageContext.request.contextPath}/images/folder-closed.gif)";
     }
  }
}

function toggleRows(elm) {
 var tables = document.getElementsByTagName("TABLE");
 var t=0;
 for (t= 0; t< tables.length; t++) {
        if ( tables[t].id.indexOf("TreeTable") >=0){
                break;
        }
 }

 var rows = tables[t].getElementsByTagName("TR");

 //var rows = document.getElementsByTagName("TR");
 elm.style.backgroundImage = "url(${pageContext.request.contextPath}/images/folder-closed.gif)";
 var newDisplay = "none";
 var spannode = elm.parentNode;
 var inputnode = spannode.getElementsByTagName("INPUT")[0];
 var thisID = inputnode.value+ "-";
 // Are we expanding or contracting? If the first child is hidden, we expand
  for (var i = 1; i < rows.length; i++) {
   var r = rows[i];

// get row id 
     var cell= r.getElementsByTagName("TD")[0];
     var sf= cell.getElementsByTagName("SPAN")[0];
     var sfs= cell.getElementsByTagName("SPAN");
	if (sfs.length >0) {   // skip the table for remove checkboxes	
     var ht= sf.getElementsByTagName("INPUT")[0];
   var rowid = ht.value;

   if (matchStart(rowid, thisID, true)) {
    if (r.style.display == "none") {
     if (document.all) newDisplay = "block"; //IE4+ specific code
     else newDisplay = "table-row"; //Netscape and Mozilla
     elm.style.backgroundImage = "url(${pageContext.request.contextPath}/images/folder-open.gif)";
    }
    break;
   }
   }

 }

 // When expanding, only expand one level.  Collapse all desendants.
 var matchDirectChildrenOnly = (newDisplay != "none");

 for (var j = 1; j < rows.length; j++) {
   var s = rows[j];
// get row id 
     var cell= s.getElementsByTagName("TD")[0];
     var sf= cell.getElementsByTagName("SPAN")[0];
     var sfs= cell.getElementsByTagName("SPAN");
	if (sfs.length >0) {   // skip the table for remove checkboxes	
     var ht= sf.getElementsByTagName("INPUT")[0];
   var rowid = ht.value;

   if (matchStart(rowid, thisID, matchDirectChildrenOnly)) {
     s.style.display = newDisplay;
     var cell = s.getElementsByTagName("TD")[0];
     var tier = cell.getElementsByTagName("SPAN")[0];
     var folder = tier.getElementsByTagName("A")[0];
     if (folder.getAttribute("onclick") != null) {
      folder.style.backgroundImage = "url(${pageContext.request.contextPath}/images/folder-closed.gif)";
     }
   }
  }

 }
}

function matchStart(target, pattern, matchDirectChildrenOnly) {
 var pos = target.indexOf(pattern);
 if (pos != 0) return false;
 if (!matchDirectChildrenOnly) return true;
 if (target.slice(pos + pattern.length, target.length).indexOf("-") >= 0) return
 false;
 return true;
}

function collapseAllRows() {
 var tables = document.getElementsByTagName("TABLE");
 var t= 0;
 var hastreetable= "false";

 if (!tables.length > 0){

	return; 
 }
 for (t= 0; t< tables.length; t++) {
	if ( tables[t].id.indexOf("TreeTable") >=0){
	hastreetable= "true";
		break;
        }	
 }

if (hastreetable== "false") {
        return;
}

 var rows = tables[t].getElementsByTagName("TR");
 //var rows = document.getElementsByTagName("TR");
 for (var j = 1; j < rows.length; j++) {
   var r = rows[j];
     var cells= r.getElementsByTagName("TD");
     var cell= r.getElementsByTagName("TD")[0];
     var sfs= cell.getElementsByTagName("SPAN");
	if (sfs.length >0) {   // skip the table for remove checkboxes	
     var sf= cell.getElementsByTagName("SPAN")[0];
     var ht= sf.getElementsByTagName("INPUT")[0];
   if (ht.value.indexOf("-") >= 0) {
     r.style.display = "none";
   }
  }

 }
}



/****************************************************************/
// this is for movePool and copyPool, where there are nested tr, td tags 

function toggleRowsForSelectList(elm) {
 var tables = document.getElementsByTagName("TABLE");
 var t=0;
 for (t= 0; t< tables.length; t++) {
        if ( tables[t].id.indexOf("TreeTable") >=0){
                break;
        }
 }

 var rows = tables[t].getElementsByTagName("TR");

 //var rows = document.getElementsByTagName("TR");
 elm.style.backgroundImage = "url(${pageContext.request.contextPath}/images/folder-closed.gif)";
 var newDisplay = "none";
 var spannode = elm.parentNode;
 var inputnode = spannode.getElementsByTagName("INPUT")[0];
 var thisID = inputnode.value+ "-";
 // Are we expanding or contracting? If the first child is hidden, we expand
  for (var i = 1; i < rows.length; i++) {
   var r = rows[i];

// to skip the cell for radio button or checkbox
      var cells= r.getElementsByTagName("TD");
if (cells.length !=7) {
 continue;
}

     var cell= r.getElementsByTagName("TD")[2];
     var sf= cell.getElementsByTagName("SPAN")[0];
     var ht= sf.getElementsByTagName("INPUT")[0];
   var rowid = ht.value;

   if (matchStart(rowid, thisID, true)) {
    if (r.style.display == "none") {
     if (document.all) newDisplay = "block"; //IE4+ specific code
     else newDisplay = "table-row"; //Netscape and Mozilla
     elm.style.backgroundImage = "url(${pageContext.request.contextPath}/images/folder-open.gif)";
    }
    break;
   }
 }

 // When expanding, only expand one level.  Collapse all desendants.
 var matchDirectChildrenOnly = (newDisplay != "none");

 for (var j = 1; j < rows.length; j++) {
   var s = rows[j];
// get row id
// to skip the cell for radio button or checkbox
      var cells= s.getElementsByTagName("TD");
if (cells.length !=7) {
 continue;
}

     var cell= s.getElementsByTagName("TD")[2];

     var sf= cell.getElementsByTagName("SPAN")[0];
     var ht= sf.getElementsByTagName("INPUT")[0];
   var rowid = ht.value;

   if (matchStart(rowid, thisID, matchDirectChildrenOnly)) {
     s.style.display = newDisplay;
     var cell = s.getElementsByTagName("TD")[2];
     var tier = cell.getElementsByTagName("SPAN")[0];
     var folder = tier.getElementsByTagName("A")[0];
     if (folder.getAttribute("onclick") != null) {
      folder.style.backgroundImage = "url(${pageContext.request.contextPath}/images/folder-closed.gif)";
     }
   }
 }
}




function collapseAllRowsForSelectList() {
 var tables = document.getElementsByTagName("TABLE");
 var t=0;
 for (t= 0; t< tables.length; t++) {
        if ( tables[t].id.indexOf("TreeTable") >=0){
                break;
        }
 }
 var rows = tables[t].getElementsByTagName("TR");
 //var rows = document.getElementsByTagName("TR");
 for (var j = 1; j < rows.length; j++) {
   var r = rows[j];
// to skip the cell for radio button or checkbox
      var cells= r.getElementsByTagName("TD");
if (cells.length !=7) {
 continue;

}
      var cell= r.getElementsByTagName("TD")[2];


     var sf= cell.getElementsByTagName("SPAN")[0];
     var ht= sf.getElementsByTagName("INPUT")[0];
   if (ht.value.indexOf("-") >= 0) {
     r.style.display = "none";
   }
 }
}


/****************************************************************/
function collapseRowsByLevel(level) {

 var tables = document.getElementsByTagName("TABLE");
 var t= 0;
 var hastreetable= "false";
 if (!tables.length > 0){
        return;
 }
 for (t= 0; t< tables.length; t++) {
        if ( tables[t].id.indexOf("TreeTable") >=0){
 		hastreetable= "true";
                break;
        }
 }
if (hastreetable== "false") {
	return;
}

 var rows = tables[t].getElementsByTagName("TR");
 //var rows = document.getElementsByTagName("TR");
 for (var j = 1; j < rows.length; j++) {
   var r = rows[j];
     var cells= r.getElementsByTagName("TD");
     var cell= r.getElementsByTagName("TD")[0];
     var sfs= cell.getElementsByTagName("SPAN");
        if (sfs.length >0) {   // skip the table for remove checkboxes
     var sf= cell.getElementsByTagName("SPAN")[0];
     var ht= sf.getElementsByTagName("INPUT")[0];
   var rtokens =ht.value.split("-");
   if (ht.value.indexOf("-") >= 0) {
     if (rtokens.length > level) {
       r.style.display = "none";
     }
   }
  }

 }
}

/****************************************************************/


function collapseRowsByLevel1(i) {
// not used 
 var tables = document.getElementsByTagName("TABLE");
 var t=0;
 for (t= 0; t< tables.length; t++) {
        if ( tables[t].id.indexOf("TreeTable") >=0){
                break;
        }
 }

 var rows = tables[t].getElementsByTagName("TR");

 //var rows = document.getElementsByTagName("TR");
 for (var j = 0; j < rows.length; j++) {
   var r = rows[j];
   var rtokens =r.id.split("-");

   if (r.id.indexOf("-") >= 0) {
     if (rtokens.length > i) {
       r.style.display = "none";
     }
   }

 }
}


// below is for simple tree
function toggleBullet(elm) {
 var newDisplay = "none";
 var e = elm.nextSibling;
 while (e != null) {
  if (e.tagName == "OL" || e.tagName == "ol") {
   if (e.style.display == "none") newDisplay = "block";
   break;
  }
  e = e.nextSibling;
 }
 while (e != null) {
  if (e.tagName == "OL" || e.tagName == "ol") e.style.display = newDisplay;
  e = e.nextSibling;
 }
}

function collapseAll() {
  var lists = document.getElementsByTagName('OL');
  for (var j = 0; j < lists.length; j++)
   lists[j].style.display = "none";
  lists = document.getElementsByTagName('ol');
  for (var j = 0; j < lists.length; j++)
   lists[j].style.display = "none";
  var e = document.getElementById("root");
  e.style.display = "block";
}

function PopupWin(url)
{
   window.open(url,"ha_fullscreen","toolbar=no,location=no,directories=no,status=no,menubar=yes,"+"scrollbars=yes,resizable=yes,width=640,height=480");

}


function checkUpdate(){
 var tables= document.getElementsByTagName("INPUT");
 for (var i = 0; i < tables.length; i++) {
    if (tables[i].name.indexOf("removeCheckbox") >=0){
         if(tables[i].checked){   
            abledButton();
             break;
         }
         else disabledButton();
    }

 }
}

// convenience function for single button arrays, see next function
function updateButtonStatusOnCheck(button, checkboxContainer)
{
    updateButtonStatusesOnCheck([button], checkboxContainer);
}

// enables/disables the given buttons depending on whether or not
// any checkbox child element of the given container is checked
function updateButtonStatusesOnCheck(buttons, checkboxContainer)
{
    if (buttons === null || checkboxContainer === null)
    {
        return;
    }

    var inputs = checkboxContainer.getElementsByTagName("input");
    var foundChecked = false;
    for (i = 0; i < inputs.length; ++i)
    {
        if (inputs[i].type === "checkbox" && inputs[i].checked)
        {
            foundChecked = true;
            break;
        }
    }

    for (i = 0; i < buttons.length; ++i)
    {
        if (buttons[i] !== null)
        {
            buttons[i].disabled = !foundChecked;
        }
    }
}

function inIt()
{
  var inputs= document.getElementsByTagName("INPUT");
  for (var i = 0; i < inputs.length; i++) {
    if (inputs[i].name.indexOf("Update") >=0) {
      inputs[i].disabled=false;
    }
  }
}

function disableIt()
{
  var inputs= document.getElementsByTagName("INPUT");
  for (var i = 0; i < inputs.length; i++) {
    if (inputs[i].name.indexOf("Update") >=0) {
      inputs[i].disabled=true;
    }
  }
}

function disabledButton(){
  var inputs= document.getElementsByTagName("INPUT");
  for (var i = 0; i < inputs.length; i++){
    if (inputs[i].name.indexOf("Submit") >=0) {
      inputs[i].disabled=true;
	  inputs[i].className='disabled';
	}
  }
}
function abledButton(){
  var inputs= document.getElementsByTagName("INPUT");
  for (var i = 0; i < inputs.length; i++) {
    if (inputs[i].name.indexOf("Submit") >=0) {
      inputs[i].disabled=false;
	  inputs[i].className='enabled';
	}
  }
}

function toggleRemove(){
  var inputs= document.getElementsByTagName("INPUT");
  var selectitem = null;
  for (var i = 0; i < inputs.length; i++) {
    if (inputs[i].name.indexOf("selectall") >=0) {
       selectitem = inputs[i];
       break;
    }
  }

  var checkit = selectitem.checked;  
  if (checkit)
    selectitem.title=textuncheckall;
  else
    selectitem.title=textcheckall;

  for (var i = 0; i < inputs.length; i++){
    if (inputs[i].name.indexOf("removeCheckbox") >=0)
      inputs[i].checked=checkit;
  }
}

// **********************************************
// ****************** SAM-2049 ******************
// **********************************************

function checkChildrenCheckboxes(item) {
	var localItem = jQuery(item);
	var itemChecked = localItem.is(':checked');

	// Go to the item's parent table that has the tier. I would have used a wildcard selector but the version
	// of jquery we are using doesn't seem to have that working well
	var itemParentTable = localItem.parents("table[id$='radiobtn']");
  
	// Make sure we found something.  A length of 0 would be not found
	if (itemParentTable.length == 1) {
		// What is the css style of this parent table? we assume this is "tierX"
		var itemTierClassName = itemParentTable.attr("class");
		var itemCheckboxTierDigit = getTierDigit(itemTierClassName);
		var itemParentTr = itemParentTable.parent().parent().parent();
     
		// Let's get all the other mega table rows
		var itemPeerTrs = itemParentTr.nextAll(); // Get all sibling tr's AFTER this tr.

		// This will be used if we find a tier level that is at a level the same or higher than the one being checked
		var isKeepLooping = true;

		for (var index = 0; (isKeepLooping && index < itemPeerTrs.length); index++) {	    	 
			var siblingTr = jQuery(itemPeerTrs.get(index));
			var siblingTierTable = siblingTr.find("table");
			var siblingTierTableClassName = siblingTierTable.attr("class");	    	 
			var siblingCheckboxTierDigit = getTierDigit(siblingTierTableClassName);

			// If this tier level is greater than the initial item checked then these are child
			// checkboxes that need to be set or unset.
			if (siblingCheckboxTierDigit > itemCheckboxTierDigit) {
				var siblingInputCheckbox = siblingTierTable.find("input:checkbox");
				if (itemChecked) {
					siblingInputCheckbox.attr("checked", "checked");
					siblingInputCheckbox.attr("disabled", "disabled");
				} else {
					siblingInputCheckbox.removeAttr("checked");
					siblingInputCheckbox.removeAttr("disabled");	    			  
				}
			} else {
				// This tier level is NOT a child tier. No need to look at any more checkboxes
				isKeepLooping = false;
			}
		}	      
  	}
}

function getTierDigit(tierString) {
	if (tierString !== undefined) {
		return tierString.substr(4, tierString.length - 4);
	}
	else {
		return -1;
	}
}

function checkAllCheckboxes( selectAllCheckbox )
{
	var allCheckboxes = jQuery(':checkbox');
	var isSelectAllChecked = selectAllCheckbox.checked;
	for( i = 0; i < allCheckboxes.length; i++ )
	{
		var checkbox = allCheckboxes[i];
		if( selectAllCheckbox.id !== checkbox.id )
		{
			if( isSelectAllChecked )
			{
				checkbox.checked = true;
				checkbox.disabled = true;
			}
			else
			{
				checkbox.checked = false;
				checkbox.disabled = false;
			}
		}
	}
}

function passSelectedPoolIds() {
	var allCheckboxes = jQuery('#transferPool\\:TreeTable').find(':checkbox:checked');
	var poolIds = new Array();
	var checkboxValue;
	
	for (var index = 0; index < allCheckboxes.length; index++) {
		checkboxValue = jQuery(allCheckboxes.get(index)).val();
		poolIds[index] = checkboxValue;
	}
	
	var hideInput = jQuery('input[name$=transferPoolIds]');
	hideInput.val(poolIds);
}

function checkChildrenCheckboxesDisable(item) {
	var localItem = jQuery(item);
	var itemChecked = localItem.is(':checked');
	var itemParentTable = localItem.closest('table');

	if (itemParentTable.length == 1) {
		var itemTierClassName = itemParentTable.attr("class");
		var itemCheckboxTierDigit = getTierDigit(itemTierClassName);
		var itemParentTr = itemParentTable.closest('tr');
		var itemPeerTrs = itemParentTr.nextAll(); // Get all sibling tr's AFTER this tr.
		var isKeepLooping = true;

		for (var index = 0; (isKeepLooping && index < itemPeerTrs.length); index++) {
			var siblingTr = jQuery(itemPeerTrs.get(index));
			var siblingTierTable = siblingTr.find("table");
			var siblingTierTableClassName = siblingTierTable.attr("class");
			var siblingCheckboxTierDigit = getTierDigit(siblingTierTableClassName);

			if (siblingCheckboxTierDigit > itemCheckboxTierDigit) {
				var siblingInputCheckbox = siblingTierTable.find("input:checkbox");
				if (itemChecked) {
					siblingInputCheckbox.attr("disabled", "disabled");
				} else {
					siblingInputCheckbox.removeAttr("disabled");
				}
			} else {
				isKeepLooping = false;
			}
		}	      
	}
}

function disableCheckboxes() {
	var allCheckboxes = jQuery(':checkbox');
	var checkAllChecked = jQuery('input[name$=checkAllCheckbox]').is(':checked');

	for (var index = 1; index < allCheckboxes.length; index++) {
		var checkboxItem = jQuery(allCheckboxes.get(index) );
		if (checkAllChecked) {
			checkboxItem.attr("disabled", "disabled");
		} else {
			checkboxItem.removeAttr("disabled");
		}
	}
	
	var otherCheckboxes = jQuery('input[name$=radiobtn]');
	for (var i = 0; i < otherCheckboxes.length; i++) {
		checkChildrenCheckboxesDisable(otherCheckboxes.get(i));
	}
}

function disableButtons() {

	// Get the buttons
	var elements = $(":submit");
	for (i = 0; i < elements.length; i++) {

		// Hide the original
		var button = elements[i];
		button.style.display = "none";

		// Clone and disable the original
		var newButton = document.createElement("input");
		newButton.setAttribute("type", "button" );
		newButton.setAttribute("id", button.getAttribute("id") + "Disabled");
		newButton.setAttribute("name", button.getAttribute("name") + "Disabled");
		newButton.setAttribute("value", button.getAttribute("value"));
		newButton.setAttribute("className", button.getAttribute("className"));
		newButton.setAttribute("disabled", "true");

		// Add the clone where the original is in the DOM
		var parent = button.parentNode;
		parent.insertBefore(newButton, button);
	}
}

/*
  Toggle JSF checkboxes that match a controller checkbox  
*/
function toggleCheckboxes(checkbox,checkboxtargetname) {
    // Render the transfer button if one or more checkboxes are selected
    // Basically this needs to split out the ID from checkbox which will be named 
    // editform:_id142:chk
    //
    // And either check or uncheck all boxes named depending on whether or not it (itself) is checked
    // editform:_id142:#:*
		// Add your controller checkbox with the name: 
		// <h:selectBooleanCheckbox id="importCheckbox_chk" onclick="toggleCheckboxes(this);" value="" />
		
		//debugger;
    if (checkboxtargetname) {
				params = checkbox.name.split(":")
		}
		if (params && params.length == 3) {
				params.pop();
				params.push(checkboxtargetname);
		}
    else 
			return; 

		for (i = 0;; i++) {
        id = params[0] + ":" + params[1] + ":" + i + ":" + params[2];
        elem = null;
        if (document.getElementsByName)
            elem = document.getElementsByName(id);
        if (elem == null || elem.length == 0) {
            break;
        } else {
            if (elem.length == 1)
                elem[0].checked = checkbox.checked;
            //Not sure if it finds multiple elements with the same name, since this is a special JSF case
            //It shouldn't happen
        }
    }
}

function toggleSelectAllCheck(checkbox,checkboxtargetname) {
	if (checkboxtargetname) 
		params = checkbox.name.split(":");
	
	if (params && params.length == 4) {
		params.pop();
		params.pop();
		params.push(checkboxtargetname);
	}
	else
		 return;

	id = params[0] + ":" + params[1] + ":" + params[2];
	elem = document.getElementsByName(id);
	
	if(elem == null || elem.length == 0)
		return;
	else{
		
		if(elem.length == 1){
			if(elem[0].checked&&!checkbox.checked)
				elem[0].checked = checkbox.checked;
			else
				return;
		}
		else 
			return;
	}
}

  
var orderUpdate = null;
  
function enableOrderUpdate() {  
	if(orderUpdate != null) return;  
	var inputs = document.getElementsByTagName("INPUT");  
	for(var i = 0; i < inputs.length; i++) {  
		if(inputs[i].name.indexOf("orderUpdate") != -1) {  
			orderUpdate = inputs[i];  
			orderUpdate.disabled=false;  
			break;  
		}  
	}  
}