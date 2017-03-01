/**********************************************************************************
*
* $Header: /cvs/sakai2/reference/library/src/webapp/calendar/sakai-calendar.js,v 1.3 2005/05/28 20:16:19 ggolden.umich.edu Exp $
*
***********************************************************************************
@license@
**********************************************************************************/

// This Sakai-specific JavaScript does setup for using the SAM pop-up calendar widget

if (document.calendarcounter == undefined) 
{
	document.calendars = new Array();
	document.calendarcounter = 0;
	document.write('<script type="text/javascript" src="/library/calendar/js/calendar2.js"></script>');
}

//Inserts a node after another node, license granted on page for use permission
//http://www.netlobo.com/javascript-insertafter.html
function insertAfter( referenceNode, newNode )
{
    if (referenceNode != undefined && newNode != undefined) {
    	referenceNode.parentNode.insertBefore( newNode, referenceNode.nextSibling );
    }
}

/**
 * This function does setup for a single popup calendar widget.
 * $yearselect_id  The id attribute of the year selection dropdown list (html SELECT tag)
 * $monthselect_id The id attribute of the month selection dropdown list (html SELECT tag)
 * $dayselect_id   The id attribute of the day selection dropdown list (html SELECT tag)
 */
function chef_dateselectionwidgetpopup(yearselect_id, monthselect_id, dayselect_id, calendar_id, calendar_title, locale)
{		

	// if calendar_id not defined, generate it 
	// TBD: replace all invokations of chef_dateselectionwidgetpopup with
	// an explicitly defined calendar_id (currently none do). This will
	// get rid of this hard-coded dependency, where calendar_id _must_
	// be constructed like this due to dependencies in the content tool
	if (calendar_id == undefined) {
        calendar_id = yearselect_id.toLowerCase().replace('year','calendar'); //SAK-19250
	}

	//Check to see if the this calendar id already exists and reinsert it (currently an issue with webkit)
	if (document.getElementById(calendar_id)) {
		return;
	}

	//Otherwise continue
	var calendarcounter = document.calendarcounter++;
	var inputfield_id = "chef_calendarhiddenfield"+calendarcounter;
	//Get the value of the year selectbox
	if (yearselect_id == undefined) {
		return;
	} else {
 		var yearselectNode  = document.getElementById(yearselect_id);
	}

	if (calendar_title == undefined || calendar_title == '') {
		calendar_title = 'Popup date selector';
	}

	if (locale == undefined) {
		locale = '';
	}

	//Replace document.write as they break the div replication in webkit and they are flaky in firefox

	//Create the popup calendar image and insert after the year selectbox)
	var imageNode = document.createElement('img');
	imageNode.src='/library/calendar/images/calendar/cal.gif';
	imageNode.alt='';
	imageNode.id=calendar_id;
	imageNode.title=calendar_title;
	imageNode.style.cursor="pointer";
/*
	old - IE7 fails to register setAttribute in dynam. added elems
	imageNode.setAttribute("onClick", "popupCalendar('"+inputfield_id+"')"); 
*/
	imageNode.onclick = new Function("popupCalendar('"+inputfield_id+"', '"+locale+"')");
	insertAfter(yearselectNode,imageNode);

	//Insert the hidden field node and insert it after the year select box (doesn't really matter it's hidden)
	var hiddenNode = document.createElement('input');
	hiddenNode.type='hidden';
	hiddenNode.name=inputfield_id;
	hiddenNode.id=inputfield_id;
	insertAfter(yearselectNode,hiddenNode);

	// stuff away variables specific to this particular calendar instance so that updateXXX() can get them   
	document.calendars[calendarcounter] = new Array(yearselect_id, monthselect_id, dayselect_id, inputfield_id);
}
 
 
/**
 * This function updates the dropdown date selection form elements
 * when the user chooses a date via the popup calendar widget.  
 * This function sets the drop-down ("<select>") form elements to the new date value.
 */
function updateDropdownsFromPopups()
{
	var i;
    for (i=0; i<document.calendars.length; i++)
    {
   		var calendarinfo = document.calendars[i];
 		// grab the form elements
    			
 		var yearselect  = document.getElementById(calendarinfo[0]);
		var monthselect = document.getElementById(calendarinfo[1]);
		var dayselect   = document.getElementById(calendarinfo[2]);   			
 		var inputfield  = document.getElementById(calendarinfo[3]);
 		
 		var date = new Date(inputfield.value);
 		
		// update each of them
	    updateSelect(yearselect, date.getFullYear());
	    updateSelect(monthselect, date.getMonth()+1);
	    updateSelect(dayselect, date.getDate());
	}
}


function updatePopupsFromDropdowns()
{
	var i;
	// synchronize the pop-up calendars to the drop-down dates
	for (i=0; i<document.calendars.length; i++)
	{
		var calendarinfo = document.calendars[i];
 		var yearselect  = document.getElementById(calendarinfo[0]);
		var monthselect = document.getElementById(calendarinfo[1]);
		var dayselect   = document.getElementById(calendarinfo[2]);
		var inputfield  = document.getElementById(calendarinfo[3]);
		
		var year = yearselect.options[yearselect.selectedIndex].value;
		var month = monthselect.options[monthselect.selectedIndex].value;
		var day = dayselect.options[dayselect.selectedIndex].value;
		
		// format the selected date like "12/31/2004" and put it into the input
		// field which the pop-up calendar uses
		inputfield.value = month + '/' + day + '/' + year;
	}
}


/** 
 * This function sets the given select form element (drop-down list) to a new selection
 * selectObj - the select form element object
 * newSelection - the value to select in the select form element (drop-down list)
 */
function updateSelect(selectObj, newSelection)
{
	for (i=0; i<selectObj.length; i++)
	{
  		if (selectObj.options[i].value == newSelection)
  		{
  			selectObj.selectedIndex = i;
  			return;
  		}
  	}
}


/** 
 * Called when the calendar button is clicked on - causes the popup
 * calendar to be displayed.
 */
function popupCalendar(inputfield_id, locale)
{
	// make sure calendar date is synchronized with dropdown date
	updatePopupsFromDropdowns();
	var inputfield = document.getElementById(inputfield_id);
	var calObj = new calendar2(inputfield);
	calObj.callback = updateDropdownsFromPopups;
	calObj.locale = locale;
	calObj.popup(null, "/sakai-calendar-connector/calendar/jsp/");
}


/**********************************************************************************
*
* $Header: /cvs/sakai2/reference/library/src/webapp/calendar/sakai-calendar.js,v 1.3 2005/05/28 20:16:19 ggolden.umich.edu Exp $
*
**********************************************************************************/

