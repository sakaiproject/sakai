function clearIfDefaultString(formField, defaultString) {
	if(formField.value === defaultString) {
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

	if (characterCode === 13) {
		event.returnValue = false;
		event.cancel = true;
		var defaultButton = document.getElementById(defaultButtonId);
		if (defaultButton.style.display !== "none")
		{
			defaultButton.click();
		}
		return false;
	} else {
		return true;
	}
}


//Seeting a form's onsubmit='return blockDoubleSubmit(this);' will fix any issues where the form submits multiple times when the user hammers the enter key or such.
var allowSubmit = true;
function blockDoubleSubmit()
{
	if (allowSubmit)
	{
		allowSubmit = false;
		return true;
	}
	return false;
}

/* DHTML windows for grading event display */

var openedWindows = new Array();

function toggleWindow(elmnt, title, text) {
	if(openedWindows[elmnt.id] === null || openedWindows[elmnt.id] === undefined) {
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
	var html = "<table>\n";
	for(var i=0; i < textArray.length; i++) {
		if((i) % 3 === 0) {
			html += "\t<tr>\n";
		}
		html += "\t\t<td>";
		html += textArray[i];
		html += "</td>\n";
		if((i+1) % 3 === 0 || (i+1) === textArray.length) {
			html += "\t</tr>\n";
		}
	}
	html += "</table>";
	return html;
}

function dhtmlWindow(x,y,w,h,title,text){

 var winBody = new divElement(x,y,w,h,"#cccccc");
  winBody.style.borderStyle = "outset";
  winBody.style.borderWidth = "2px";
  winBody.style.borderColor = "#aaaaaa";
  winBody.style.zIndex = (dhtmlWindow.zCount++);
  
 
 var toolBar = new divElement(4,4,w-14,18,"#006699");
  toolBar.style.position = "absolute";
  toolBar.style.color = "#ffffff";
  toolBar.style.fontFamily = "arial";
  toolBar.style.fontSize = "10pt";
  toolBar.style.paddingLeft="4px";
  
  toolBar.proxyFor = winBody;
 
 var contentArea = new divElement(4,26,w-10,h-40,"#ffffff");
  if (document.all) contentArea.style.width = (parseInt(contentArea.style.width)-4)+"px";
  else contentArea.style.width = (parseInt(contentArea.style.width)-7)+"px";
  contentArea.style.borderColor="#cccccc";
  contentArea.style.borderStyle="inset";
  contentArea.style.borderWidth="1px";
  contentArea.style.overflow="auto";
  contentArea.style.paddingLeft="4px";
  contentArea.style.paddingRight="2px";
  contentArea.style.fontFamily = "arial";
  contentArea.style.fontSize = "10pt";
  winBody.content = contentArea;

 var titleDiv = document.createElement("div");
 titleDiv.appendChild(document.createTextNode(title));
 
 contentArea.innerHTML = parseText(text);

 winBody.appendChild(contentArea);
 toolBar.appendChild(titleDiv);
 winBody.appendChild(toolBar);
 return winBody;

}

dhtmlWindow.zCount=0;

function divElement (x,y,w,h,col){
	var lyr = document.createElement("div");
 	 lyr.style.position = "relative";
	 lyr.style.left = x + "px";
	 lyr.style.top = y + "px";
	 lyr.style.width = w + "px";
	 lyr.style.height = h + "px";
	 lyr.style.backgroundColor = col;
	 lyr.style.visibility = "visible";
	 lyr.style.padding= "0px 0px 0px 0px";
	return lyr;
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
    if(thiselm === null) {
      return;
    } else {
      return thiselm;
    }
  }
}

// Update the running total
function updateRunningTotal(thisForm) {
	var runningTotal = 0.0;

  	var row = 0;
  	var weightInput = getTheElement(thisForm.name + ":categoriesTable:" + row + ":weightInput");
  	//just threw 10000 in there just in case as an out
  	while(weightInput && row < 10000){
		weight = parseFloat(weightInput.value.replace(/,/, '.'));
  		var extraCreditCheckbox = getTheElement(thisForm.name + ":categoriesTable:" + row + ":catExtraCredit");
		if (weight >= 0 && extraCreditCheckbox !== null && !extraCreditCheckbox.checked) {
            runningTotal += weight;
        }
  		row++;
  		weightInput = getTheElement(thisForm.name + ":categoriesTable:" + row + ":weightInput");
  	}

  var neededTotal = 100.0 - runningTotal;

  var runningTotalValEl = getTheElement(thisForm.name + ":runningTotalVal");
  var runningTotalEl = getTheElement(thisForm.name + ":runningTotal");
  var neededTotalEl = getTheElement(thisForm.name + ":neededTotalVal");
  runningTotalValEl.innerHTML = (Math.round(runningTotal*100)/100).toFixed(2);
  neededTotalEl.innerHTML = (Math.round(neededTotal*100)/100).toFixed(2);
  if (neededTotal === 0)
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
    if(divisionNo.style.display ==="block" || divisionNo.style.display ==="table-row")
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
      divisionNo.style.display="table-row";

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
	if (imgAllSrcPieces[(imgAllSrcPieces.length - 1)] === "expand.gif")
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
			  //Gecko specific fix no longer needed
			  divisionNo.style.display="table-row";

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

function reEnableCategoryDropInputs(component) {
    if(component === null || component === undefined) {
        // Enable all of the category drop scores inputs on the page
        // This is required because of the lack of support for
        // disabled components in myfaces
        var allElements = document.forms[0].elements;
        for(i=0; i < allElements.length; i++) {
                var currentElement = allElements[i];
                var currentElementName = currentElement.getAttribute('name');

                if(currentElementName !== null && (currentElementName.indexOf(":pointValue") !== -1
                        || currentElementName.indexOf(":relativeWeight") !== -1
                        || currentElementName.indexOf(":dropHighest") !== -1
                        || currentElementName.indexOf(":dropLowest") !== -1
                        || currentElementName.indexOf(":keepHighest") !== -1
					)) {
                        // Recursive function call
                    reEnableCategoryDropInputs(currentElement);
                }
        }
    } else {
		var dropElement = getTheElement(component.getAttribute('name'));
        dropElement.disabled = false;
    }
}

function toggleVisibilityDropScoresFields() {
    var formName = "gbForm";
    var showDropHighest = getTheElement(formName + ":showDropHighest");
    var showDropLowest = getTheElement(formName + ":showDropLowest");
    var showKeepHighest = getTheElement(formName + ":showKeepHighest");
    var dropHighestVisibility = ""; // an unspecified display makes the column and column header visible
    var dropLowestVisibility = ""; // an unspecified display makes the column and column header visible
    var keepHighestVisibility = ""; // an unspecified display makes the column and column header visible
    var tbl  = document.getElementById(formName + ":categoriesTable");
    if(!tbl) {
        // No categories currently defined
        return;
    }

    var thead = tbl.getElementsByTagName('thead');
    var header = thead.item(0);
    var headerRows = header.getElementsByTagName('th');

    if(headerRows.length === 7) {
        var dropHighestIdx = 3;  // the index of 1st drop column, if Categories is selected
    } else {
        var dropHighestIdx = 4;  // the index of 1st drop column, if Categories & Weighting is selected
    }

    if(showDropHighest === undefined || showDropHighest.checked === false) {
        dropHighestVisibility = "none";      // make the column and column header not visible
    }
    if(showDropLowest === undefined || showDropLowest.checked === false) {
        dropLowestVisibility = "none";      // make the column and column header not visible
    }
    if(showKeepHighest === undefined || showKeepHighest.checked === false) {
        keepHighestVisibility = "none";      // make the column and column header not visible
    }
    if((showDropHighest === undefined || showDropHighest.checked === false)
            && (showDropLowest === undefined || showDropLowest.checked === false)
            && (showKeepHighest === undefined || showKeepHighest.checked === false)) {
        itemValueVisibility = "none";      // make the column and column header not visible
    }

    headerRows[dropHighestIdx].style.display=dropHighestVisibility;
    headerRows[dropHighestIdx+1].style.display=dropLowestVisibility;
    headerRows[dropHighestIdx+2].style.display=keepHighestVisibility;
  //  headerRows[dropHighestIdx+3].style.display=itemValueVisibility;
    var rows = tbl.getElementsByTagName('tr');
    for (var row=0; row<rows.length;row++) {
        var cels = rows[row].getElementsByTagName('td');
        if(cels.length > 0) {
            cels[dropHighestIdx].style.display=dropHighestVisibility;
            cels[dropHighestIdx+1].style.display=dropLowestVisibility;
            cels[dropHighestIdx+2].style.display=keepHighestVisibility;
  //          cels[dropHighestIdx+3].style.display=itemValueVisibility; 
        }
    }
    dropScoresAdjust();
}


function dropScoresAdjust() {
    var formName = "gbForm";
    var showDropHighest = getTheElement(formName + ":showDropHighest");
    var showDropLowest = getTheElement(formName + ":showDropLowest");
    var showKeepHighest = getTheElement(formName + ":showKeepHighest");
    var numPossibleCategories = 51;

    for (var i=0; i < numPossibleCategories; ++i) {
        var dropHighest =  getTheElement(formName + ":categoriesTable:" + i + ":dropHighest");
        var dropLowest =  getTheElement(formName + ":categoriesTable:" + i + ":dropLowest");
        var keepHighest =  getTheElement(formName + ":categoriesTable:" + i + ":keepHighest");
        var pointValue =  getTheElement(formName + ":categoriesTable:" + i + ":pointValue");
        var relativeWeight =  getTheElement(formName + ":categoriesTable:" + i + ":relativeWeight");
        var pointValueLabelAsterisk = getTheElement(formName + ":categoriesTable:" + i + ":pointValueLabelAsterisk");
        var relativeWeightLabelAsterisk = getTheElement(formName + ":categoriesTable:" + i + ":relativeWeightLabelAsterisk");
        
        var dropHighestEnabled = true;
        var dropLowestEnabled = true;
        var keepHighestEnabled = true;
        
        var pointsPossibleUnequal = false;
        if(showDropHighest === undefined || showDropHighest.checked === false) {
            dropHighestEnabled = false;
            if(dropHighest !== undefined) {
                dropHighest.value = 0;
            }
        }
        if(showDropLowest === undefined || showDropLowest.checked === false) {
            dropLowestEnabled = false;
            if(dropLowest !== undefined) {
                dropLowest.value = 0;
            }
        }
        if(showKeepHighest === undefined || showKeepHighest.checked === false) {
            keepHighestEnabled = false;
            if(keepHighest !== undefined) {
                keepHighest.value = 0;
            }
        }
        if(dropHighestEnabled === false && dropLowestEnabled === false && keepHighestEnabled === false) {
            if(pointValue !== undefined) {
                pointValue.value = 0;
            }
            if(relativeWeight !== undefined) {
                relativeWeight.value = 0;
            }
        }
        // if all are disabled, this means that the category was disabled for entering drop scores (because items pointsPossible are unequal)
        if(dropHighest !== undefined && dropLowest !== undefined && keepHighest !== undefined) {
            if(dropHighest.disabled === true && dropLowest.disabled === true && keepHighest.disabled === true) {
                pointsPossibleUnequal = true;
            } else {
                pointsPossibleUnequal = false;
            }
        }        
        if(!pointsPossibleUnequal) {
            if(dropHighest !== undefined && (dropHighest.value > 0 || dropLowest.value > 0)) {
                if(keepHighest !== undefined) {
                    keepHighest.value = 0;
                    keepHighest.disabled = true;
                }
            } else if(keepHighest !== undefined) {
                keepHighest.disabled = false;
                
                if(pointValue !== undefined) {
                    pointValue.disabled = true;
                    if(pointValueLabelAsterisk !== undefined) {
                        pointValueLabelAsterisk.style.visibility="hidden";
                    }
                }
                if(relativeWeight !== undefined) {
                    relativeWeight.disabled = true;
                    if(relativeWeightLabelAsterisk !== undefined) {
                        relativeWeightLabelAsterisk.style.visibility="hidden";
                    }
                }
                
            }    
            if(keepHighest !== undefined && keepHighest.value > 0) {
                if(dropLowest !== undefined) {
                    dropLowest.value = 0;
                    dropLowest.disabled = true;
                }
                if(dropHighest !== undefined) {
                    dropHighest.value = 0;
                    dropHighest.disabled = true;
                }
            } else if(dropLowest !== undefined && dropHighest !== undefined) {
                dropLowest.disabled = false;
                dropHighest.disabled = false;
            }
            
            if((dropHighest !== undefined && dropHighest.value > 0) 
                    || (dropLowest !== undefined && dropLowest.value > 0)
                    || (keepHighest !== undefined && keepHighest.value > 0)) {
                if(pointValue !== undefined) {
                    pointValue.disabled = false;
                    if(pointValueLabelAsterisk !== undefined) {
                        pointValueLabelAsterisk.style.visibility="visible";
                    }
                }
                if(relativeWeight !== undefined) {
                    relativeWeight.disabled = false;
                    if(relativeWeightLabelAsterisk !== undefined) {
                        relativeWeightLabelAsterisk.style.visibility="visible";
                    }
                }
            } else {
                if(pointValue !== undefined) {
                    pointValue.disabled = true;
                    if(pointValueLabelAsterisk !== undefined) {
                        pointValueLabelAsterisk.style.visibility="hidden";
                    }
                }
                if(relativeWeight !== undefined) {
                    relativeWeight.disabled = true;
                    if(relativeWeightLabelAsterisk !== undefined) {
                        relativeWeightLabelAsterisk.style.visibility="hidden";
                    }
                }
            }

            if((dropHighest === undefined || dropHighest.value < 1) 
                    && (dropLowest === undefined || dropLowest.value < 1)
                    && (keepHighest === undefined || keepHighest.value < 1)) {
                if(pointValue !== undefined) {
                    pointValue.value = 0.0;
                }
                if(relativeWeight !== undefined) {
                    relativeWeight.value = 0.0;
                }
            }
        }
    }
}

// if user unchecks box to release items, we must uncheck
// and disable the option to include item in cumulative score
function assignmentReleased(myForm, releasedChanged) {
	var releasedCheckboxEl =  getTheElement(myForm + ':released');
	var countedCheckboxEl =   getTheElement(myForm + ':countAssignment');
	var categoryDDEl = getTheElement(myForm + ':selectCategory');

	if (releasedCheckboxEl.checked === false) {
		countedCheckboxEl.checked = false;
		countedCheckboxEl.disabled = true;
	} else if (releasedCheckboxEl.checked === true) {
		if (undefined !== categoryDDEl)
		{
			if (categoryDDEl.options[categoryDDEl.selectedIndex].value !== "unassigned")
			{
				countedCheckboxEl.disabled = false;
				if (releasedChanged)
					countedCheckboxEl.checked = true;
			}
		}
		else
		{
			countedCheckboxEl.disabled = false;
			if (releasedChanged)
				countedCheckboxEl.checked = true;
		}
	}
}

// if categories are enabled, we don't want to be able to check the calculate box 
// for unassigned items.  This function will ensure this happens in the UI
function categorySelected(myForm, extraCreditCategories)
{
	var extraCreditCatsArr = new Array();
	if(typeof extraCreditCategories === 'string'){
		extraCreditCatsArr = extraCreditCategories.split(",");
	}
	var categoryDDEl = getTheElement(myForm + ':selectCategory');
	var countedCheckboxEl = getTheElement(myForm + ':countAssignment');
	var releasedCheckboxEl =  getTheElement(myForm + ':released');
	var extraCreditCheckboxEl =  getTheElement(myForm + ':extraCredit');
	if (undefined !== categoryDDEl)
	{
		if (categoryDDEl.options[categoryDDEl.selectedIndex].value === "unassigned")
		{
			countedCheckboxEl.checked = false;
			countedCheckboxEl.disabled = true;
		}
		else
		{
			if (undefined !== releasedCheckboxEl)
			{
				if (releasedCheckboxEl.checked === true)
				{
					countedCheckboxEl.disabled = false;
				}
			}
			else
			{
				countedCheckboxEl.disabled = false;
			}
		}
		//check if category is EC, if so, disable EC item checkbox:
		if(extraCreditCatsArr.indexOf(categoryDDEl.options[categoryDDEl.selectedIndex].value) > -1){
			extraCreditCheckboxEl.disabled = true;
			extraCreditCheckboxEl.checked = false;
		}else{
			extraCreditCheckboxEl.disabled = false;
		}
	}
	return;
}

// if the containing frame is small, then offsetHeight is pretty good for all but ie/xp.
// ie/xp reports clientHeight == offsetHeight, but has a good scrollHeight
function mySetMainFrameHeight(id)
{
	// run the script only if this window's name matches the id parameter
	// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
	if (typeof window.name !== "undefined" && id !== window.name) return;

	var frame = parent.document.getElementById(id);
	if (frame)
	{

		var objToResize = (frame.style) ? frame.style : frame;

		var height; 
		
		var scrollH = document.body.scrollHeight;
		var offsetH = document.body.offsetHeight;
		var clientH = document.body.clientHeight;
		var innerDocScrollH = null;

		if (typeof(frame.contentDocument) !== 'undefined' || typeof(frame.contentWindow) !== 'undefined')
		{
			// very special way to get the height from IE on Windows!
			// note that the above special way of testing for undefined variables is necessary for older browsers
			// (IE 5.5 Mac) to not choke on the undefined variables.
 			var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
			innerDocScrollH = (innerDoc !== null) ? innerDoc.body.scrollHeight : null;
		}

		if (document.all && innerDocScrollH !== null)
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
		//gsilver: changing this from 50 to 10, and adding extra bottom padding to the portletBody		
		var newHeight = height + 150;
		//contributed patch from hedrick@rutgers.edu (for very long documents)
		if (newHeight > 32760)
		newHeight = 32760;

		// no need to be smaller than...
		//if (height < 200) height = 200;
		objToResize.height=newHeight + "px";
	
		var s = " scrollH: " + scrollH + " offsetH: " + offsetH + " clientH: " + clientH + " innerDocScrollH: " + innerDocScrollH + " Read height: " + height + " Set height to: " + newHeight;

	}
}
	
function displayHideElement(myForm, displayId, radioId, radioElementValue) {
	displayElement = getTheElement(myForm.name + ":" + displayId);
	radioElement = getTheElement(myForm.name + ":" + radioId);
	
	var inputs = radioElement.getElementsByTagName ('input');
  for (i=0;i<inputs.length;i++){
    if (inputs[i].checked===true){
      var selection = inputs[i].value;
      if (selection === radioElementValue) 
				 displayElement.style.display="block";
			else
				displayElement.style.display="none";
			
			break;
    }
  }
}

function initCategoryDisplay() {
	var tr_hidden = $("tr.hide");
	tr_hidden.each(function(i){
		var hideRow = true;
		
		// first, check for the existence of a category name in the input
		var input_name = $(this).children("td").get(0);
		if (input_name) {
			var input_name_children = $(input_name).children("input.catNameInput");
			if (input_name_children && input_name_children.length > 0) {
				var input_name_val = input_name_children.get(0).value;
				if (input_name_val && input_name_val.length > 0) {
					hideRow = false;
				}
			}
		}
		
		// now let's check if they input a weight!
		var input_weight = $(this).children("td").get(1);
		if (input_weight) {
			var input_weight_children = $(input_weight).children("input.catWeightInput");
			if (input_weight_children && input_weight_children.length > 0) {
				var input_weight_val = input_weight_children.get(0).value;
				if (input_weight_val && input_weight_val.length > 0) {
					hideRow = false;
				}
			}
		}
		
		// if there is data in the input, we display that row. otherwise, hide it
		if (hideRow) {
			$(this).addClass("hide");
			$(this).hide();
		} else {
			$(this).removeClass("hide");
		}
	});

	$("a.more_categories").click(function(event){
		$("tr.hide:first").removeClass("hide").show();
		mySetMainFrameHeight('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
		if(!$("tr.hide").size()){
			$(event.target).hide();
		}
	});
}
