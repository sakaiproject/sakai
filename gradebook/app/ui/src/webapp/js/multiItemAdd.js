var DOT = "_";
var addSecond = true;

//*********************************************************************
// setMainFrameHeight
//
// set the parent iframe's height to hold our entire contents
// COPIED AND MODIFIED TO EXPAND APPRORIATE AMOUNT FOR BULK
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
// addItemScreen
//
// This does the actual work of showing another add item pane
//*********************************************************************
function addItemScreen() 
{
	var numBulkItems = getNumTotalItem();

	var trEls = document.getElementsByClassName("hide");
	trEls[0].className = "show" + trEls[0].className.substring(4);
	trEls[0].style.display = "block";

	// set hiddenAdd property to TRUE so if submitted and an
	// error, this will be displayed
	var hiddenAddEl = getHiddenAdd(trEls[0]);		
	hiddenAddEl.value = 'true';
	
	if (numBulkItems == MAX_NEW_ITEMS - 1)
		$("gbForm:addSecond").style.display = "none";
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
		setMainFrameHeightNow(thisId, 'grow');

		itemSelect.selectedIndex = 0;
	}
}
 
//*********************************************************************
// removeItem
//
// This does the actual work of hiding the current add item pane
//*********************************************************************
function removeItem(event) {
	var element = Event.element(event);
	var numBulkItems = getNumTotalItem();

	// hack to get the enclosing tr for the enclosing table this pane is nested inside of
	// in order to hide it and/or set prop so not saved
	element = element.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode;   		
  	element.className = "hide" + element.className.substring(4);
   	element.style.display = "none";    	
	setMainFrameHeight(thisId, 'shrink');

	// just in case we were full up and now we're not
	$("gbForm:addSecond").style.display = "inline";

	setMainFrameHeightNow(thisId, 'shrink');
	
   	// set saveThisItem property to TRUE so it will be saved
   	var hiddenAddEl = getHiddenAdd(element);
	hiddenAddEl.value = 'false';

    adjustNumBulkItems(-1);
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
	if (document.all) {
		numTotalItemEl = document.all['gbForm:numTotalItems'];
	}
	else {
		numTotalItemEl = document.getElementById('gbForm:numTotalItems');
	}
	return numTotalItemEl;
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
	if (document.all) {
		numTotalItemEl = document.all['gbForm:numTotalItems'];
	}
	else {
		numTotalItemEl = document.getElementById('gbForm:numTotalItems');
	}
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