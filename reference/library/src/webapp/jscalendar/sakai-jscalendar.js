/**********************************************************************************
*
* $Header: /cvs/sakai2/reference/library/src/webapp/jscalendar/sakai-jscalendar.js,v 1.3 2005/05/28 20:16:19 ggolden.umich.edu Exp $
*
***********************************************************************************
@license@
**********************************************************************************/

// This Sakai-specific JavaScript does setup for using the jscalendar JavaScript library for a pop-up calendar widget

if (document.jscalendarcounter == undefined)
{
	// This section will only be included ONCE per HTML document that contains pop-up calendar widget(s)

	// This variable is a counter of the number of calendars on the page, just used to create unique
	// variable and function names for each popup calendar widget instance.
	document.jscalendarcounter = 0;
	document.jscalendars = new Array();
	
	// funky way to include the jscalendar JavaScript libraries, from within JavaScript
	document.write('<link rel="stylesheet" type="text/css" media="all" href="/library/jscalendar/calendar-blue.css" title="calendar style sheet" />');
	document.write('<script type="text/javascript" src="/library/jscalendar/calendar.js"></script>');
	document.write('<script type="text/javascript" src="/library/jscalendar/lang/calendar-en.js"></script>');
	document.write('<script type="text/javascript" src="/library/jscalendar/calendar-setup.js"></script>');
}

/**
 * This function does setup for a single popup calendar widget.
 * $yearselect_id  The id attribute of the year selection dropdown list (html SELECT tag)
 * $monthselect_id The id attribute of the month selection dropdown list (html SELECT tag)
 * $dayselect_id   The id attribute of the day selection dropdown list (html SELECT tag)
 */
function chef_setuppopupcalendarwidget(yearselect_id, monthselect_id, dayselect_id)
{	
	// Bug fix for Mac Internet Explorer - 
	// pop-up calendar doesn't work on this browser, so don't display it at all, not even the image button.
	if ( /msie 5/i.test(navigator.userAgent) && /mac_/i.test(navigator.userAgent) && !/opera/i.test(navigator.userAgent) )
	{
		return;
	}

	// The image button that the user clicks on to pop up the calendar 
	document.write('<img src="/library/jscalendar/CalendarIcon.gif" alt="" id="chef_calendarbutton'+document.jscalendarcounter+'" style="cursor: pointer;" title="Popup date selector" />');
	  
	// A hidden input field where the selected date value will be stored. 
	document.write('<input type="hidden" name="chef_calendarhiddenfield'+document.jscalendarcounter+'" id="chef_calendarhiddenfield'+document.jscalendarcounter+'" />');
	
	// connect the drop-downs to the calendar, by using an event handler.
	// when the drop-down selection changes, dropdownCallback will update the pop-up calendar.
	var yearselect = document.getElementById(yearselect_id);
	var monthselect = document.getElementById(monthselect_id);
	var dayselect = document.getElementById(dayselect_id);
	yearselect.onchange = dropdownCallback;
	monthselect.onchange = dropdownCallback;
	dayselect.onchange = dropdownCallback;
	dropdownCallback();
		
	var calendarCloseCallback = function(cal) { cal.hide(); };
	
	var inputfield_id = "chef_calendarhiddenfield"+document.jscalendarcounter;
	
	// Call into the jscalendar script library that does setup and 
	// configuration for a new popup calendar instance
	var calendarObj;
	calendarObj = Calendar.setup
	({
	        inputField     :    inputfield_id,     // id of the input field
	        ifFormat       :    "%d-%m-%Y",     // format of the input field (even if hidden, this format will be honored)
	        button         :    "chef_calendarbutton"+document.jscalendarcounter,  // trigger for the calendar (button ID)
	        singleClick    :    true,
	        onUpdate       :    calendarCallback, // Callback function for when date is selected
	        onClose        :    calendarCloseCallback,
	        align          :    "Bl" // specifies the calendar should appear below the button
	});
		 
	// stuff away variables specific to this particular calendar instance so that calendarCallback can get them   
	document.jscalendars[document.jscalendarcounter] = new Array(calendarCloseCallback, yearselect_id, monthselect_id, dayselect_id, inputfield_id );

	// just in case, synchronize the pop-up calendar to the drop-down calendar in a bit
	setTimeout("dropdownCallback();", 500);
	
	// Bug fix for Safari browser
	 var is_safari = /Safari/i.test(navigator.userAgent);
	 if (is_safari) 
	 {
	 	// disable the function that hides form elements underneath the calendar; the function is buggy when run in Safari
	 	Calendar.prototype.hideShowCovered = function() {};
	 }
	
	document.jscalendarcounter++;
}


/**
 * This function updates the dropdown date selection form elements
 * when the user chooses a date via the popup calendar widget.  
 * This function sets the drop-down ("<select>") form elements to the new date value.
 */
function calendarCallback(calendarObj)
{
    var date = calendarObj.date;
    var i;
    var calendarinfo;
    
    // TODO: this is very kludgy - 
    // in order to figure out which calendar we are, use the onClose handler.
    // That is, use the onClose handler of the calendarObj as an ID for the calendar!!
    // it works.... There must be a simpler way though.......
    for (i=0; i<document.jscalendars.length; i++)
    {
    		calendarinfo = document.jscalendars[i];
    		if (calendarObj.onClose == calendarinfo[0])
    		{
   			// grab the form elements
    			
     		var yearselect  = document.getElementById(calendarinfo[1]);
    			var monthselect = document.getElementById(calendarinfo[2]);
    			var dayselect   = document.getElementById(calendarinfo[3]);   			
	 
			// update each of them
		    updateSelect(yearselect, date.getFullYear());
		    updateSelect(monthselect, date.getMonth()+1);
		    updateSelect(dayselect, date.getDate());
		    
		    return;
		}
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

function dropdownCallback()
{
	// synchronize the pop-up calendars to the drop-down dates
	for (i=0; i<document.jscalendars.length; i++)
	{
		var calendarinfo = document.jscalendars[i];
 		var yearselect  = document.getElementById(calendarinfo[1]);
		var monthselect = document.getElementById(calendarinfo[2]);
		var dayselect   = document.getElementById(calendarinfo[3]);
		var inputfield  = document.getElementById(calendarinfo[4]);
		
		var year = yearselect.options[yearselect.selectedIndex].value;
		var month = monthselect.options[monthselect.selectedIndex].value;
		var day = dayselect.options[dayselect.selectedIndex].value;
		
		
		// format the selected date like "31-12-2004" and put it into the input
		// field which the pop-up calendar uses
		inputfield.value = day + "-" + month + "-" + year;
	}
}

/**********************************************************************************
*
* $Header: /cvs/sakai2/reference/library/src/webapp/jscalendar/sakai-jscalendar.js,v 1.3 2005/05/28 20:16:19 ggolden.umich.edu Exp $
*
**********************************************************************************/

