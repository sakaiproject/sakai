// ===================================================================
// Author: Matt Kruse <matt@mattkruse.com>
// WWW: http://www.mattkruse.com/
//
// NOTICE: You may use this code for any purpose, commercial or
// private, without any further permission from the author. You may
// remove this notice from your final code if you wish, however it is
// appreciated by the author if at least my web site address is kept.
//
// You may *NOT* re-distribute this code in any way except through its
// use. That means, you can include it in your product, or your web
// site, or any other form where the code is actually being used. You
// may not put the plain javascript up on your site for download or
// include it in your javascript libraries for download. 
// If you wish to share this code with others, please just point them
// to the URL instead.
// Please DO NOT link directly to my .js files from your site. Copy
// the files to your server and use them there. Thank you.
// ===================================================================

/* 
OptionTransfer.js
Last Modified: 7/12/2004

DESCRIPTION: This widget is used to easily and quickly create an interface
where the user can transfer choices from one select box to another. For
example, when selecting which columns to show or hide in search results.
This object adds value by automatically storing the values that were added
or removed from each list, as well as the state of the final list. 

COMPATABILITY: Should work on all Javascript-compliant browsers.

USAGE:
// Create a new OptionTransfer object. Pass it the field names of the left
// select box and the right select box.
var ot = new OptionTransfer("from","to");

// Optionally tell the lists whether or not to auto-sort when options are 
// moved. By default, the lists will be sorted.
ot.setAutoSort(true);

// Optionally set the delimiter to be used to separate values that are
// stored in hidden fields for the added and removed options, as well as
// final state of the lists. Defaults to a comma.
ot.setDelimiter("|");

// You can set a regular expression for option texts which are _not_ allowed to
// be transferred in either direction
ot.setStaticOptionRegex("static");

// These functions assign the form fields which will store the state of
// the lists. Each one is optional, so you can pick to only store the
// new options which were transferred to the right list, for example.
// Each function takes the name of a HIDDEN or TEXT input field.

// Store list of options removed from left list into an input field
ot.saveRemovedLeftOptions("removedLeft");
// Store list of options removed from right list into an input field
ot.saveRemovedRightOptions("removedRight");
// Store list of options added to left list into an input field
ot.saveAddedLeftOptions("addedLeft");
// Store list of options radded to right list into an input field
ot.saveAddedRightOptions("addedRight");
// Store all options existing in the left list into an input field
ot.saveNewLeftOptions("newLeft");
// Store all options existing in the right list into an input field
ot.saveNewRightOptions("newRight");

// IMPORTANT: This step is required for the OptionTransfer object to work
// correctly.
// Add a call to the BODY onLoad="" tag of the page, and pass a reference to
// the form which contains the select boxes and input fields.
BODY onLoad="ot.init(document.forms[0])"

// ADDING ACTIONS INTO YOUR PAGE
// Finally, add calls to the object to move options back and forth, either
// from links in your page or from double-clicking the options themselves.
// See example page, and use the following methods:
ot.transferRight();
ot.transferAllRight();
ot.transferLeft();
ot.transferAllLeft();


NOTES:
1) Requires the functions in selectbox.js

*/ 
function OT_transferLeft() { moveSelectedOptions(this.right,this.left,this.autoSort,this.staticOptionRegex); this.update(); }
function OT_transferRight() { moveSelectedOptions(this.left,this.right,this.autoSort,this.staticOptionRegex); this.update(); }
function OT_transferAllLeft() { moveAllOptions(this.right,this.left,this.autoSort,this.staticOptionRegex); this.update(); }
function OT_transferAllRight() { moveAllOptions(this.left,this.right,this.autoSort,this.staticOptionRegex); this.update(); }
function OT_saveRemovedLeftOptions(f) { this.removedLeftField = f; }
function OT_saveRemovedRightOptions(f) { this.removedRightField = f; }
function OT_saveAddedLeftOptions(f) { this.addedLeftField = f; }
function OT_saveAddedRightOptions(f) { this.addedRightField = f; }
function OT_saveNewLeftOptions(f) { this.newLeftField = f; }
function OT_saveNewRightOptions(f) { this.newRightField = f; }
function OT_update() {
	var removedLeft = new Object();
	var removedRight = new Object();
	var addedLeft = new Object();
	var addedRight = new Object();
	var newLeft = new Object();
	var newRight = new Object();
	for (var i=0;i<this.left.options.length;i++) {
		var o=this.left.options[i];
		newLeft[o.value]=1;
		if (typeof(this.originalLeftValues[o.value])=="undefined") {
			addedLeft[o.value]=1;
			removedRight[o.value]=1;
			}
		}
	for (var i=0;i<this.right.options.length;i++) {
		var o=this.right.options[i];
		newRight[o.value]=1;
		if (typeof(this.originalRightValues[o.value])=="undefined") {
			addedRight[o.value]=1;
			removedLeft[o.value]=1;
			}
		}
	if (this.removedLeftField!=null) { this.removedLeftField.value = OT_join(removedLeft,this.delimiter); }
	if (this.removedRightField!=null) { this.removedRightField.value = OT_join(removedRight,this.delimiter); }
	if (this.addedLeftField!=null) { this.addedLeftField.value = OT_join(addedLeft,this.delimiter); }
	if (this.addedRightField!=null) { this.addedRightField.value = OT_join(addedRight,this.delimiter); }
	if (this.newLeftField!=null) { this.newLeftField.value = OT_join(newLeft,this.delimiter); }
	if (this.newRightField!=null) { this.newRightField.value = OT_join(newRight,this.delimiter); }
	}
function OT_join(o,delimiter) {
	var val; var str="";
	for(val in o){
		if (str.length>0) { str=str+delimiter; }
		str=str+val;
		}
	return str;
	}
function OT_setDelimiter(val) { this.delimiter=val; }
function OT_setAutoSort(val) { this.autoSort=val; }
function OT_setStaticOptionRegex(val) { this.staticOptionRegex=val; }
function OT_init(theform) {
	this.form = theform;
	if(!theform[this.left]){alert("OptionTransfer init(): Left select list does not exist in form!");return false;}
	if(!theform[this.right]){alert("OptionTransfer init(): Right select list does not exist in form!");return false;}
	this.left=theform[this.left];
	this.right=theform[this.right];
	for(var i=0;i<this.left.options.length;i++) {
		this.originalLeftValues[this.left.options[i].value]=1;
		}
	for(var i=0;i<this.right.options.length;i++) {
		this.originalRightValues[this.right.options[i].value]=1;
		}
	if(this.removedLeftField!=null) { this.removedLeftField=theform[this.removedLeftField]; }
	if(this.removedRightField!=null) { this.removedRightField=theform[this.removedRightField]; }
	if(this.addedLeftField!=null) { this.addedLeftField=theform[this.addedLeftField]; }
	if(this.addedRightField!=null) { this.addedRightField=theform[this.addedRightField]; }
	if(this.newLeftField!=null) { this.newLeftField=theform[this.newLeftField]; }
	if(this.newRightField!=null) { this.newRightField=theform[this.newRightField]; }
	this.update();
	}
// -------------------------------------------------------------------
// OptionTransfer()
//  This is the object interface.
// -------------------------------------------------------------------
function OptionTransfer(l,r) {
	this.form = null;
	this.left=l;
	this.right=r;
	this.autoSort=true;
	this.delimiter=",";
	this.staticOptionRegex = "";
	this.originalLeftValues = new Object();
	this.originalRightValues = new Object();
	this.removedLeftField = null;
	this.removedRightField = null;
	this.addedLeftField = null;
	this.addedRightField = null;
	this.newLeftField = null;
	this.newRightField = null;
	this.transferLeft=OT_transferLeft;
	this.transferRight=OT_transferRight;
	this.transferAllLeft=OT_transferAllLeft;
	this.transferAllRight=OT_transferAllRight;
	this.saveRemovedLeftOptions=OT_saveRemovedLeftOptions;
	this.saveRemovedRightOptions=OT_saveRemovedRightOptions;
	this.saveAddedLeftOptions=OT_saveAddedLeftOptions;
	this.saveAddedRightOptions=OT_saveAddedRightOptions;
	this.saveNewLeftOptions=OT_saveNewLeftOptions;
	this.saveNewRightOptions=OT_saveNewRightOptions;
	this.setDelimiter=OT_setDelimiter;
	this.setAutoSort=OT_setAutoSort;
	this.setStaticOptionRegex=OT_setStaticOptionRegex;
	this.init=OT_init;
	this.update=OT_update;
	}
