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
