function clearIfDefaultString(formField, defaultString) {
	if(formField.value == defaultString) {
		formField.value = "";
	}
}

// We sometimes want to have a default submit button that's not
// the first one in the form.
//
// USAGE:
//
//   <h:inputText id="Score" value="#{scoreRow.score}"
//     onkeypress="return submitOnEnter(event, 'gbForm:saveButton');"/>
//   <h:commandButton id="saveButton" ... />
//
// It's important to specify "onkeypress" rather than "onkeydown". Otherwise,
// IE will work fine, but Mozilla and Co. will send the key release event
// on to the first button on the form no matter what.
//
function submitOnEnter(event, defaultButtonId) {
	var characterCode;
	if (event.which) {
		characterCode = event.which;
	} else if (event.keyCode) {
		characterCode = event.keyCode;
	}

	if (characterCode == 13) {
		event.returnValue = false;
		event.cancel = true;
		document.getElementById(defaultButtonId).click();
		return false;
	} else {
		return true;
	}
}



/* DHTML windows for grading event display */

var openedWindows = new Array();

function toggleWindow(elmnt, title, text) {
	if(openedWindows[elmnt.id] == null || openedWindows[elmnt.id] == undefined) {
		/* Open the window */
		var newWindow = new dhtmlWindow(5,5,300,200,title,text);
		elmnt.parentNode.appendChild(newWindow);
		openedWindows[elmnt.id] = newWindow;
	} else {
		/*  Close the window */
		var oldOpenedWindow = openedWindows[elmnt.id];
		elmnt.parentNode.removeChild(oldOpenedWindow);
		openedWindows[elmnt.id] = null;
	}
}

/* TODO format the table properly */
function parseText(text) {
	var textArray = text.split('|');
	var html = "<table>\n"
	for(var i=0; i < textArray.length; i++) {
		if((i) % 3 == 0) {
			html += "\t<tr>\n"
		}
		html += "\t\t<td>"
		html += textArray[i]
		html += "</td>\n"
		if((i+1) % 3 == 0 || (i+1) == textArray.length) {
			html += "\t</tr>\n"
		}
	}
	html += "</table>"
	return html
}

function dhtmlWindow(x,y,w,h,title,text){

 var winBody = new divElement(x,y,w,h,"#cccccc")
  winBody.style.borderStyle = "outset"
  winBody.style.borderWidth = "2px"
  winBody.style.borderColor = "#aaaaaa"
  winBody.style.zIndex = (dhtmlWindow.zCount++)
  
 
 var toolBar = new divElement(4,4,w-14,18,"#006699")
  toolBar.style.position = "absolute"
  toolBar.style.color = "#ffffff"
  toolBar.style.fontFamily = "arial"
  toolBar.style.fontSize = "10pt"
  toolBar.style.paddingLeft="4px"
  
  toolBar.proxyFor = winBody
 
 var contentArea = new divElement(4,26,w-10,h-40,"#ffffff")
  if (document.all) contentArea.style.width = (parseInt(contentArea.style.width)-4)+"px"
  else contentArea.style.width = (parseInt(contentArea.style.width)-7)+"px"
  contentArea.style.borderColor="#cccccc"
  contentArea.style.borderStyle="inset"
  contentArea.style.borderWidth="1px"
  contentArea.style.overflow="auto"
  contentArea.style.paddingLeft="4px"
  contentArea.style.paddingRight="2px"
  contentArea.style.fontFamily = "arial"
  contentArea.style.fontSize = "10pt"
  winBody.content = contentArea;

 var titleDiv = document.createElement("div")
 titleDiv.appendChild(document.createTextNode(title));
 
 contentArea.innerHTML = parseText(text)

 winBody.appendChild(contentArea)
 toolBar.appendChild(titleDiv)
 winBody.appendChild(toolBar)
 return winBody

}

dhtmlWindow.zCount=0;

function divElement (x,y,w,h,col){
	var lyr = document.createElement("div")
 	 lyr.style.position = "relative"
	 lyr.style.left = x + "px"
	 lyr.style.top = y + "px"
	 lyr.style.width = w + "px"
	 lyr.style.height = h + "px"
	 lyr.style.backgroundColor = col
	 lyr.style.visibility = "visible"
	 lyr.style.padding= "0px 0px 0px 0px"
	return lyr
}

function getTheElement(thisid)
{
  var thiselm = null;
  if (document.getElementById) {
    thiselm = document.getElementById(thisid);
  } else if (document.all) {
    thiselm = document.all[thisid];
  } else if (document.layers) {
    thiselm = document.layers[thisid];
  }

  if(thiselm) {
    if(thiselm == null) {
      return;
    } else {
      return thiselm;
    }
  }
}

// Update the running total
function updateRunningTotal(thisForm) {
	var runningTotal = 0.0;
	
  for (var i=0; i < thisForm.elements.length; ++i) {
  	formElement = thisForm.elements[i];
    elementName = formElement.name;
    var elementNamePieces = elementName.split(":");
    var highlightTotal = true;

    if (elementNamePieces[3] == "weightInput") {
        weight = parseInt(formElement.value);

        if (weight >= 0) {
            runningTotal += weight;
        }
    }
  }
  
  var neededTotal = 100.0 - runningTotal;

  var runningTotalValEl = getTheElement(thisForm.name + ":runningTotalVal");
  var runningTotalEl = getTheElement(thisForm.name + ":runningTotal");
  var neededTotalEl = getTheElement(thisForm.name + ":neededTotalVal");
  runningTotalValEl.innerHTML = runningTotal;
  neededTotalEl.innerHTML = neededTotal;
  if (neededTotal == 0)
  	runningTotalEl.className="courseGrade";
  else
  	runningTotalEl.className = "highlight courseGrade";
}

// for toggling display of gradebook items associated with a category
function showHideDiv(hideDivisionNo, context, expandAlt, collapseAlt, expandTitle, collapseTitle)
{
  var tmpdiv = hideDivisionNo + "__hide_division_";
  var tmpimg = hideDivisionNo + "__img_hide_division_";
  var divisionNo = getTheElement(tmpdiv);
  var imgNo = getTheElement(tmpimg);

  if(divisionNo)
  {
    if(divisionNo.style.display =="block" || divisionNo.style.display =="table-row")
    {
      divisionNo.style.display="none";
      if (imgNo)
      {
        imgNo.src = context + "/images/collapse.gif";
        imgNo.alt = collapseAlt;
        imgNo.title = collapseTitle;
      }
    }
    else
    {
      if(navigator.product == "Gecko")
      {
        divisionNo.style.display="table-row";
      }
      else
      {
        divisionNo.style.display="block";
      }
      if(imgNo)
      {
        imgNo.src = context + "/images/expand.gif";
        imgNo.alt = expandAlt;
        imgNo.title = expandTitle;
      }
    }
  }
}

// for toggling all gradebook items displayed within a category
function showHideAll(numToggles, context, expandAlt, collapseAlt, expandTitle, collapseTitle)
{
  var allimg = "expandCollapseAll";
  var imgAll = getTheElement(allimg);
  var imgAllSrcPieces = imgAll.src.split("/");

  var expanded = false;
	if (imgAllSrcPieces[(imgAllSrcPieces.length - 1)] == "expand.gif")
	 	expanded = true;
	 	
	for (var i=0; i < numToggles; i++) {
	  var tmpdiv = "_id_" + i + "__hide_division_";
	  var tmpimg = "_id_" + i + "__img_hide_division_";
	  var divisionNo = getTheElement(tmpdiv);
	  var imgNo = getTheElement(tmpimg);
	
	  if(divisionNo)
	  {
	  	if (expanded) {
		    divisionNo.style.display="none";
		    
		    if (imgNo) {
		      imgNo.src = context + "/images/collapse.gif";
		      imgNo.alt =  collapseAlt;
		      imgNo.title = collapseTitle;
		    }
		    if (imgAll) {
		      imgAll.src = context + "/images/collapse.gif";
		      imgAll.alt =  collapseAlt;
		      imgAll.title = collapseTitle;
		    }
		  }
		  else {
		    if(navigator.product == "Gecko")
      	{
        	divisionNo.style.display="table-row";
      	}
      	else
      	{
        	divisionNo.style.display="block";
     	 	}
		    
		    if (imgNo) {
		      imgNo.src = context + "/images/expand.gif";
		      imgNo.alt =  expandAlt;
		      imgNo.title = expandTitle;
		    }

		    if (imgAll) {
		    	imgAll.src = context + "/images/expand.gif";
		    	imgAll.alt =  expandAlt;
		      imgAll.title = expandTitle;
		    }
		  }
	  }
  }
}

// if user set the category to "unassigned", the checkbox for counting the
// assignment must be unchecked and disabled. Once he/she assigns category,
// counted will be enabled
function assignmentCategoryChange(myForm, categoriesEnabled) {
	if (categoriesEnabled != true)
		return;
	var categorySelectionEl = getTheElement(myForm + ':selectCategory');
	var releasedCheckboxEl =  getTheElement(myForm + ':released');
	var countedCheckboxEl =   getTheElement(myForm + ':countAssignment');
	
	if (categorySelectionEl.value == 'unassigned' || releasedCheckboxEl.checked == false) {
		countedCheckboxEl.checked = false;
		countedCheckboxEl.disabled = true;
	}	
	else if (categorySelectionEl.value != 'unassigned' && releasedCheckboxEl.checked == true ){
		countedCheckboxEl.disabled = false;
		countedCheckboxEl.checked = true;
	}
}