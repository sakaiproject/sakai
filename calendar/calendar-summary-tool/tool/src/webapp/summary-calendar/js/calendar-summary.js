/* Javascript for moving events between multi-select lists */

var highPriorityEvents;
var mediumPriorityEvents;
var lowPriorityEvents;

var moveUpStr;
var moveDownStr;

function prepForms() {
    if(document.getElementById("prefsForm")) {
        populateLists();
        unHighlightEvents();
    }
}

function populateLists(){
    highPriorityEvents = document.getElementById("prefsForm:highPriorityEvents");
    mediumPriorityEvents = document.getElementById("prefsForm:mediumPriorityEvents");
    lowPriorityEvents = document.getElementById("prefsForm:lowPriorityEvents");
}

function moveMediumToHigh(){
	var count = 0;
    var tempArray = new Array();
    var fromArray = mediumPriorityEvents;
    var toArray = highPriorityEvents;
    
    for (var i=0; i<fromArray.options.length; i++) {
        if (fromArray.options[i].selected) {
            tempArray[count++] = fromArray.options[i];
        }
    }
    for (var i=0; i<tempArray.length; i++) {
        toArray.appendChild(tempArray[i]);
        selectNone(toArray,fromArray);
    }
}

function moveHighToMedium(){
	var count = 0;
    var tempArray = new Array();
    var fromArray = highPriorityEvents;
    var toArray = mediumPriorityEvents;
    
    for (var i=0; i<fromArray.options.length; i++) {
        if (fromArray.options[i].selected) {
            tempArray[count++] = fromArray.options[i];
        }
    }
    for (var i=0; i<tempArray.length; i++) {
        toArray.appendChild(tempArray[i]);
        selectNone(toArray,fromArray);
    }
}

function moveLowToMedium(){
	var count = 0;
    var tempArray = new Array();
    var fromArray = lowPriorityEvents;
    var toArray = mediumPriorityEvents;
    
    for (var i=0; i<fromArray.options.length; i++) {
        if (fromArray.options[i].selected) {
            tempArray[count++] = fromArray.options[i];
        }
    }
    for (var i=0; i<tempArray.length; i++) {
        toArray.appendChild(tempArray[i]);
        selectNone(toArray,fromArray);
    }
}

function moveMediumToLow(){
	var count = 0;
    var tempArray = new Array();
    var fromArray = mediumPriorityEvents;
    var toArray = lowPriorityEvents;
    
    for (var i=0; i<fromArray.options.length; i++) {
        if (fromArray.options[i].selected) {
            tempArray[count++] = fromArray.options[i];
        }
    }
    for (var i=0; i<tempArray.length; i++) {
        toArray.appendChild(tempArray[i]);
        selectNone(toArray,fromArray);
    }
}

function highlightEvents() {
    // Select all of the selected events, so they are sent in the form submit
    var selectBox = document.getElementById("prefsForm:highPriorityEvents");
    for(var i = 0; i < selectBox.length; i++) {
        selectBox.options[i].selected = true;
    }
    var selectBox = document.getElementById("prefsForm:mediumPriorityEvents");
    for(var i = 0; i < selectBox.length; i++) {
        selectBox.options[i].selected = true;
    }
    var selectBox = document.getElementById("prefsForm:lowPriorityEvents");
    for(var i = 0; i < selectBox.length; i++) {
        selectBox.options[i].selected = true;
    }
}

function unHighlightEvents() {
    // Unselect all of the selected events, so they are clear on page load
    var selectBox = document.getElementById("prefsForm:highPriorityEvents");
    for(var i = 0; i < selectBox.length; i++) {
        selectBox.options[i].selected = false;
    }
    var selectBox = document.getElementById("prefsForm:mediumPriorityEvents");
    for(var i = 0; i < selectBox.length; i++) {
        selectBox.options[i].selected = false;
    }
    var selectBox = document.getElementById("prefsForm:lowPriorityEvents");
    for(var i = 0; i < selectBox.length; i++) {
        selectBox.options[i].selected = false;
    }
}

function selectNone(list1,list2){
    list1.selectedIndex = -1;
    list2.selectedIndex = -1;
}
