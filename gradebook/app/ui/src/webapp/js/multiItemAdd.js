var DOT = "_";
var addSecond = true;
var comingBack = false;

//http://www.asilearn.net/asilearn/web-development/javascript-getelementsbyclassname/
//Because it's returning a live node instead of an Array now
document.getElementsByClassNameArray = function(theClass)
{
		var node=this;
    var classElms = [];
    if (node.getElementsByClassName)
    { // check if it's natively available
    	// if it is, loop through the items in the NodeList...
        var tempEls = node.getElementsByClassName(theClass);
        for (var i = 0; i < tempEls.length ; i++)
        {
    		// ... and push them into an Array
                classElms.push(tempEls[i]);
    	}
    }
    else
    {
        // if a native implementation is not available, use a custom one
        var getclass = new RegExp('\\b'+theClass+'\\b');
        var elems = node.getElementsByTagName('*');
        for (var i = 0; i < elems.length; i++)
        {
                 var classes = elems[i].className;
                 if (getclass.test(classes)) classElms.push(elem[i]);
        }
    }
    return classElms;
}

//*********************************************************************
// setMainFrameHeight
//
// set the parent iframe's height to hold our entire contents
// COPIED, MODIFIED, AND RENAMED TO EXPAND APPRORIATE AMOUNT FOR BULK
// GRADEBOOK ITEM CREATION
//*********************************************************************
function setMainFrameHeight(id, direction)
{
	// some browsers need a moment to finish rendering so the height and scroll are correct
 	setTimeout("setMainFrameHeightNow('"+id+"','"+direction+"')",10);
}

function setMainFrameHeightNow(id,direction)
{
	// run the script only if this window's name matches the id parameter
	// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
	if (typeof window.name != "undefined" && id != window.name && id.replace(/-/g,"x") != window.name) return;

	  var frame = parent.document.getElementById(id);
	  if (frame == null) frame = parent.document.getElementById(id.replace(/-/g,"x"));

	  if (frame)
	  {
	 	// reset the scroll
	   //parent.window.scrollTo(0,0);

	   var objToResize = (frame.style) ? frame.style : frame;

	   var height;
	   var offsetH = document.body.offsetHeight;
	   var innerDocScrollH = null;

	   if (typeof(frame.contentDocument) != 'undefined' || typeof(frame.contentWindow) != 'undefined')
	   {
		 // very special way to get the height from IE on Windows!
		 // note that the above special way of testing for undefined variables is necessary for older browsers
		 // (IE 5.5 Mac) to not choke on the undefined variables.
		 var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
		 innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight : null;
	   }

		 if (document.all && innerDocScrollH != null)
		 {
		 	// IE on Windows only
			 height = innerDocScrollH;
		 }
		 else
		 {
			// every other browser!
			height = offsetH;
		 }

		 // here we fudge to get a little bigger
		 // modified 10-18-2007 for gradebook bulk items to enlarge and shrink
		 var newHeight = height + 101;
		 
		 // capture my current scroll position
		 var scroll = findScroll();

		 // resize parent frame (this resets the scroll as well)
		 objToResize.height=newHeight + "px";

		 // reset the scroll, unless it was y=0)
		 if (scroll[1] > 0)
		 {
			 var position = findPosition(frame);
			 parent.window.scrollTo(position[0]+scroll[0], position[1]+scroll[1]);
		 }
	}
}

//*********************************************************************
// addDelX
//
// If more than 1 pane is displayed, add the X remove to the first pane
//*********************************************************************
function addDelX() { 
	var firstDelEl = document.getElementsByClassNameArray('hideRemove');

	firstDelEl[0].style.display='inline';
  firstDelEl[0].className = 'firstDel' + firstDelEl[0].className.substring(10);
}

//*********************************************************************
// addItemScreen
//
// This does the actual work of showing another add item pane
//*********************************************************************
function addItemScreen() 
{
	var numBulkItems = getNumTotalItem();

	var trEls = document.getElementsByClassNameArray("hide");
	trEls[0].style.display = "";
	trEls[0].className = "show" + trEls[0].className.substring(4);

	// set hiddenAdd property to TRUE so if submitted and an
	// error, this will be displayed
	var hiddenAddEl = getHiddenAdd(trEls[0]);		
	hiddenAddEl.value = 'true';
	
	// make sure delete link on first item is displayed
	if (numBulkItems == 1) {
		addDelX();
	}

	if (numBulkItems == MAX_NEW_ITEMS - 1)
		$("gbForm:addSecond").style.display = "none";

	setMainFrameHeight(thisId, 'shrink');
}

//*********************************************************************
// addMultipleItems
//
// If a number is selected from the drop down box, display that many
// Add Gradebook Item screens
//*********************************************************************
function addMultipleItems(itemSelect)
{
	if (document.all)
		itemSelect = document.all["gbForm:numItems"];

	var numItems = parseInt(itemSelect.value);
	var numBulkItems = getNumTotalItem();

	if (numItems > 0) {
		// since only 50 new items max, need to check so
		// we don't try to create more.
    	if ((numBulkItems + numItems) <= MAX_NEW_ITEMS) {
    		adjustSize = numItems;
    	}
    	else {
			adjustSize = MAX_NEW_ITEMS - numBulkItems;
	    }

		for (var i=0; i < adjustSize; i++) {
			addItemScreen();
			adjustNumBulkItems(1);
		}
		
		// since DOM changed, resize
		setMainFrameHeight(thisId, 'grow');

		itemSelect.selectedIndex = 0;
	}
}
 
//*********************************************************************
// copyPanes
//
// This copies the values from rowIndex2 to rowIndex1
//*********************************************************************
function copyPanes(rowIndex1, rowIndex2, idPrefix) {
// Commented out with non-graded items roll back
//	var radioEl1 = document.getElementsByName(idPrefix + rowIndex1 + ':assignNonGraded');
//	var radioEl2 = document.getElementsByName(idPrefix + rowIndex2 + ':assignNonGraded');
//	radioEl1[0].checked = radioEl2[0].checked;
//	radioEl1[1].checked = radioEl2[1].checked;
	
	var curEl1 = getEl(idPrefix + rowIndex1 + ':title');
	var curEl2 = getEl(idPrefix + rowIndex2 + ':title');
	curEl1.value = curEl2.value;
	
	curEl1 = getEl(idPrefix + rowIndex1 + ':points');
	curEl2 = getEl(idPrefix + rowIndex2 + ':points');
	curEl1.value = curEl2.value;
//	if (radioEl1[1].checked) curEl1.style.display = 'none'; Commented out with non-graded item rollback
	
	curEl1 = getEl(idPrefix + rowIndex1 + ':dueDate');
	curEl2 = getEl(idPrefix + rowIndex2 + ':dueDate');
	curEl1.value = curEl2.value;
	
	curEl1 = getEl(idPrefix + rowIndex1 + ':selectCategory');
	if (curEl1) {
		curEl2 = getEl(idPrefix + rowIndex2 + ':selectCategory');
		curEl1.selectedIndex = curEl2.selectedIndex;
	}
	
	curEl1 = getEl(idPrefix + rowIndex1 + ':released');
	curEl2 = getEl(idPrefix + rowIndex2 + ':released');
	curEl1.checked = curEl2.checked;
	
	curEl1 = getEl(idPrefix + rowIndex1 + ':countAssignment');
	curEl2 = getEl(idPrefix + rowIndex2 + ':countAssignment');
	curEl1.checked = curEl2.checked;
	
	curEl1 = getEl(idPrefix + rowIndex1 + ':extraCredit');
	curEl2 = getEl(idPrefix + rowIndex2 + ':extraCredit');
	curEl1.checked = curEl2.checked;
	
	// copy/hide error messages
	curEl1 = getEl(idPrefix + rowIndex1 + ':noTitleErrMsg');
	if (!curEl1) curEl1 = getEl(idPrefix + rowIndex1 + ':noTitleErrMsgH');
	curEl2 = getEl(idPrefix + rowIndex2 + ':noTitleErrMsg');
	if (curEl2) {
		curEl1.style.display = 'inline';
	}
	else { 
		curEl1.style.display = 'none';
	}

	curEl1 = getEl(idPrefix + rowIndex1 + ':dupTitleErrMsg');
	if (!curEl1) curEl1 = getEl(idPrefix + rowIndex1 + ':dupTitleErrMsgH');
	curEl2 = getEl(idPrefix + rowIndex2 + ':dupTitleErrMsg');
	if (curEl2) {
		curEl1.style.display = 'inline';
	}
	else { 
		curEl1.style.display = 'none';
	}

	curEl1 = getEl(idPrefix + rowIndex1 + ':blankPtsErrMsg');
	if (!curEl1) curEl1 = getEl(idPrefix + rowIndex1 + ':blankPtsErrMsgH');
	curEl2 = getEl(idPrefix + rowIndex2 + ':blankPtsErrMsg');
	if (curEl2) {
		curEl1.style.display = 'inline';
	}
	else { 
		curEl1.style.display = 'none';
	}

	curEl1 = getEl(idPrefix + rowIndex1 + ':nanPtsErrMsg');
	if (!curEl1) curEl1 = getEl(idPrefix + rowIndex1 + ':nanPtsErrMsgH');
	curEl2 = getEl(idPrefix + rowIndex2 + ':nanPtsErrMsg');
	if (curEl2) {
		curEl1.style.display = 'inline';
	}
	else { 
		curEl1.style.display = 'none';
	}

	// Added per SAK-13459
	curEl1 = getEl(idPrefix + rowIndex1 + ':invalidPtsErrMsg');
	if (!curEl1) curEl1 = getEl(idPrefix + rowIndex1 + ':invalidPtsErrMsgH');
	curEl2 = getEl(idPrefix + rowIndex2 + ':invalidPtsErrMsg');
	if (curEl2) {
		curEl1.style.display = 'inline';
	}
	else { 
		curEl1.style.display = 'none';
	}

	// Added per SAK-13459
	curEl1 = getEl(idPrefix + rowIndex1 + ':precisionPtsErrMsg');
	if (!curEl1) curEl1 = getEl(idPrefix + rowIndex1 + ':precisionPtsErrMsgH');
	curEl2 = getEl(idPrefix + rowIndex2 + ':precisionPtsErrMsg');
	if (curEl2) {
		curEl1.style.display = 'inline';
	}
	else { 
		curEl1.style.display = 'none';
	}
}

//*********************************************************************
// eraseAndHide
//
// This will remove the values for the pane to be hidden as well as
// hide the pane
//*********************************************************************
function eraseAndHide(idPrefix, rowIndex) {
	var curEl = getEl(idPrefix + ':title');
	curEl.value = "";
	
	curEl = getEl(idPrefix + ':points');
	curEl.value = "";
	curEl.style.display = 'inline';
	
	curEl = getEl(idPrefix + ':dueDate');
	curEl.value = "";
	
	curEl = getEl(idPrefix + ':selectCategory');
	if (curEl) curEl.selectedIndex = 0;
	
	curEl = getEl(idPrefix + ':released');
	curEl.value = true;
	curEl.checked = true;
	
	curEl = getEl(idPrefix + ':countAssignment');
	curEl.value = true;
	curEl.checked = true;
	
	curEl = getEl(idPrefix + ':countAssignment');
	curEl.value = true;
	curEl.checked = true;
	
	curEl = getEl(idPrefix + ':extraCredit');
	curEl.value = 'false';
	curEl.checked = false;
	
//	Commented out with non-graded item roll back
//	curEl = document.getElementsByName(idPrefix + ':assignNonGraded');
//	curEl[0].checked = true;

	// Remove the error messages if they exist
	// for each textbox there can only be at most one
	// error message so once found we can skip the rest
	curEl = getEl(idPrefix + ':noTitleErrMsg');
	if (!curEl || (curEl.className.indexOf('errHide') != -1)) curEl = getEl(idPrefix + ':noTitleErrMsgH');
	if (!curEl || (curEl.className.indexOf('errHide') != -1)) curEl = getEl(idPrefix + ':dupTitleErrMsg');
	if (!curEl || (curEl.className.indexOf('errHide') != -1)) curEl = getEl(idPrefix + ':dupTitleErrMsgH');
	
	if (curEl) curEl.style.display = 'none';
	
	curEl = getEl(idPrefix + ':blankPtsErrMsg');
	if (!curEl || (curEl.className.indexOf('errHide') != -1)) curEl = getEl(idPrefix + ':blankPtsErrMsgH');
	if (!curEl || (curEl.className.indexOf('errHide') != -1)) curEl = getEl(idPrefix + ':nanPtsErrMsg');
	if (!curEl || (curEl.className.indexOf('errHide') != -1)) curEl = getEl(idPrefix + ':nanPtsErrMsgH');
	if (!curEl || (curEl.className.indexOf('errHide') != -1)) curEl = getEl(idPrefix + ':invalidPtsErrMsg');  // Added per SAK-13459
	if (!curEl || (curEl.className.indexOf('errHide') != -1)) curEl = getEl(idPrefix + ':invalidPtsErrMsgH'); // Added per SAK-13459
	if (!curEl || (curEl.className.indexOf('errHide') != -1)) curEl = getEl(idPrefix + ':precisionPtsErrMsg');  // Added per SAK-13459
	if (!curEl || (curEl.className.indexOf('errHide') != -1)) curEl = getEl(idPrefix + ':precisionPtsErrMsgH'); // Added per SAK-13459
	if (curEl) curEl.style.display = 'none';

	// Get the enclosing tr for the enclosing table this pane is nested inside of
	// in order to hide it
	// tbodyPrefix - need to chop off the rowIndex from id prefix
	tbodyPrefix = idPrefix.substring(0,idPrefix.lastIndexOf(':'));
	var element = document.getElementById(tbodyPrefix + ':tbody_element').rows[rowIndex];
  	element.className = "hide" + element.className.substring(4);
   	element.style.display = "none";
 }

//*********************************************************************
// removeItem
//
// This does the actual work of 'hiding' the current add item pane
// by coping all panes after it. The last pane with the class
// show is then hidden.
//*********************************************************************
function removeItem(event, idPrefix, rowIndex) {
	var element = Event.element(event);
	var numBulkItems = getNumTotalItem();
	
    for (i = rowIndex; i < (MAX_NEW_ITEMS-1); i++) {
    	if (getEl(idPrefix + (i+1) + ':hiddenAdd').value == "false") {
    		eraseAndHide(idPrefix + i, i);
    		break;	// all the rest are hidden so no copying needed
    	}
    	else {
	    	copyPanes(i, i+1, idPrefix);
    	}
	}
	
	// just in case we were full up and now we're not
	$("gbForm:addSecond").style.display = "inline";
	
	if (numBulkItems == 2) {
		// make sure delete link on first item is removed since
		// there will only be one item
		var firstDelEl = document.getElementsByClassNameArray('firstDel');
		firstDelEl[0].style.display='none';	
		firstDelEl[0].className = "hideRemove" + firstDelEl[0].className.substring(8);

	}

    adjustNumBulkItems(-1);
	setMainFrameHeight(thisId, 'shrink');
}

//*********************************************************************
// getNumTotalItemsEl
//
// This gets numTotalItems element so can be manipulated
//*********************************************************************
function getNumTotalItemEl() {
	var numTotalItemEl;

	// Get the hidden numTotalItems textbox to update it
	// prefix added by jsf when rendering
	return getEl('gbForm:numTotalItems');
}

//*********************************************************************
// getNumTotalItems
//
// This gets numTotalItems element value so can be updated
//*********************************************************************
function getNumTotalItem() {
	var numTotalItemEl;

	// Get the hidden numTotalItems textbox to update it
	// prefix added by jsf when rendering
	numTotalItemEl = getEl('gbForm:numTotalItems');
	return parseInt(numTotalItemEl.value);
}

//*********************************************************************
// getHiddenAdd
//
// Returns the hiddenAdd element to be set based on container element
// passed in
//*********************************************************************
function getHiddenAdd(container) {
	var inputEls = container.getElementsByTagName('input');
	
	for (var i = 0; i < inputEls.length; i++) {
		if ((inputEls[i].type == 'hidden') && (inputEls[i].name.indexOf('hiddenAdd') != -1)) {
			return inputEls[i];
		}
	}
	
	return null;
}

//*********************************************************************
// adjustNumBulkItems
//
// Returns the hiddenAdd element to be set based on container element
// passed in
//*********************************************************************
function adjustNumBulkItems(amt) {
	var numTotalItemEl = getNumTotalItemEl();
	var numBulkItems = parseInt(numTotalItemEl.value);
	numTotalItemEl.value = numBulkItems + amt;
}

//*********************************************************************
// togglePointEntry
//
// NEEDED WHEN NON-GRADED ENTRY PUT BACK IN
// To hide/show point entry row based on the radio button clicked
//*********************************************************************
function togglePointEntry(event, elementPrefix) {
	var element = Event.element(event);
	var pointsLabelEl;
	var pointsEl;
	
	pointsLabelEl = getEl(elementPrefix + 'pointsLabel');
	pointsEl = getEl(elementPrefix + 'pointsLabel');
	
	if (element.value) {
		pointsLabelEl.style.display='inline';
		pointsEl.style.display='inline';
		pointsEl.parentNode.style.display='block';
	}
	else {
		pointsLabelEl.style.display='none';
		pointsEl.style.display='none';
		pointsEl.parentNode.style.display='none';
	}
}

//*********************************************************************
// getEl
//
// Returns the DOM element for the id passed in
//*********************************************************************
function getEl(elId) {
	if (document.all) {
		return document.all[elId];
	}
	else {
		return document.getElementById(elId);
	}
}
