// Title: Tigra Calendar
// URL: http://www.softcomplex.com/products/tigra_calendar/
// Version: 3.2 (American date format)
// Date: 10/14/2002 (mm/dd/yyyy)
// Feedback: feedback@softcomplex.com (specify product title in the subject)
// Note: Permission given to use this script in ANY kind of applications if
//    header lines are left unchanged.
// Note: Script consists of two files: calendar?.js and calendar.html
// About us: Our company provides offshore IT consulting services.
//    Contact us at sales@softcomplex.com if you have any programming task you
//    want to be handled by professionals. Our typical hourly rate is $20.

// Sakai project: $Id$

// if two digit year input dates after this year considered 20 century.
var NUM_CENTYEAR = 90;// sakai, changed to 90
// is time input control required by default
var BUL_TIMECOMPONENT = false;
// are year scrolling buttons required by default
var BUL_YEARSCROLL = true;

// sakai, AM/PM
var ampm = "AM";

var calendars = [];
var RE_NUM = /^\-?\d+$/;

function calendar2(obj_target) {

	// assing methods
	this.gen_date = cal_gen_date2;
	this.gen_time = cal_gen_time2;
	this.gen_tsmp = cal_gen_tsmp2;
	this.prs_date = cal_prs_date2;
	this.prs_time = cal_prs_time2;
	this.prs_tsmp = cal_prs_tsmp2;
	this.popup    = cal_popup2;

	// validate input parameters
	if (!obj_target)
		return cal_error("Error calling the calendar: no target control specified");
	if (obj_target.value == null)
		return cal_error("Error calling the calendar: parameter specified is not valid tardet control");
	this.target = obj_target;
	this.time_comp = BUL_TIMECOMPONENT;
	this.year_scroll = BUL_YEARSCROLL;

	// register in global collections
	this.id = calendars.length;
	calendars[this.id] = this;
}

function cal_popup2 (str_datetime,root) {
	this.dt_current = this.prs_tsmp(str_datetime ? str_datetime : this.target.value);
	if (!this.dt_current) return;

	var obj_calwindow = window.open(
// sakai modification
		root+'calendar.html?datetime=' + this.dt_current.valueOf()+ '&id=' + this.id + '&root=' + root,
//		root+'datepicker.faces?datetime=' + this.dt_current.valueOf()+ '&id=' + this.id + '&root=' + root,
		'Calendar', 'width=200,height='+(this.time_comp ? 215 : 190)+
		',status=no,resizable=no,top=200,left=200,dependent=yes,alwaysRaised=yes'
	);
	obj_calwindow.opener = window;
	obj_calwindow.focus();
}

// timestamp generating function
function cal_gen_tsmp2 (dt_datetime) {
	return(this.gen_date(dt_datetime) + ' ' + this.gen_time(dt_datetime) +
        // sakai mod
        ' ' + ampm
         );
}

// date generating function, --sakai modified to use dashes
function cal_gen_date2 (dt_datetime) {
	return (
		(dt_datetime.getMonth() < 9 ? '0' : '') + (dt_datetime.getMonth() + 1) + "/"
		+ (dt_datetime.getDate() < 10 ? '0' : '') + dt_datetime.getDate() + "/"
		+ dt_datetime.getFullYear()
	);
}


// time generating function
function cal_gen_time2 (dt_datetime) {
  //sakai mod, calculate AM/PM
  if (dt_datetime.getHours() < 13)
  {
    ampm = "AM";
    hourPart = (dt_datetime.getHours() < 10 ? '0' : '') + dt_datetime.getHours();
  }
  else
  {
     ampm = "PM";
     hourPart = dt_datetime.getHours() - 12;
     hourPart = (hourPart < 10 ? '0' : '') + hourPart;
  }
	return (
// sakai mod -- was (dt_datetime.getHours() < 10 ? '0' : '') + dt_datetime.getHours() + ":"
		hourPart + ":"
		+ (dt_datetime.getMinutes() < 10 ? '0' : '') + (dt_datetime.getMinutes()) + ":"
		+ (dt_datetime.getSeconds() < 10 ? '0' : '') + (dt_datetime.getSeconds())
// sakai mod
//    + ' ' + ampm
	);
}

// timestamp parsing function
function cal_prs_tsmp2 (str_datetime) {
	// if no parameter specified return current timestamp
	if (!str_datetime)
		return (new Date());

	// if positive integer treat as milliseconds from epoch
	if (RE_NUM.exec(str_datetime))
		return new Date(str_datetime);

	// else treat as date in string format
	var arr_datetime = str_datetime.split(' ');
	return this.prs_time(arr_datetime[1], this.prs_date(arr_datetime[0]));
}

// date parsing function
function cal_prs_date2 (str_date) {

	var arr_date = str_date.split('/');// sakai mod from '/'

	if (arr_date.length != 3) return alert ("Invalid date format: '" + str_date + "'.\nFormat accepted is dd-mm-yyyy.");
	if (!arr_date[1]) return alert ("Invalid date format: '" + str_date + "'.\nNo day of month value can be found.");
	if (!RE_NUM.exec(arr_date[1])) return alert ("Invalid day of month value: '" + arr_date[1] + "'.\nAllowed values are unsigned integers.");
	if (!arr_date[0]) return alert ("Invalid date format: '" + str_date + "'.\nNo month value can be found.");
	if (!RE_NUM.exec(arr_date[0])) return alert ("Invalid month value: '" + arr_date[0] + "'.\nAllowed values are unsigned integers.");
	if (!arr_date[2]) return alert ("Invalid date format: '" + str_date + "'.\nNo year value can be found.");
	if (!RE_NUM.exec(arr_date[2])) return alert ("Invalid year value: '" + arr_date[2] + "'.\nAllowed values are unsigned integers.");

	var dt_date = new Date();
	dt_date.setDate(1);

	if (arr_date[0] < 1 || arr_date[0] > 12) return alert ("Invalid month value: '" + arr_date[0] + "'.\nAllowed range is 01-12.");
	dt_date.setMonth(arr_date[0]-1);

	if (arr_date[2] < 100) arr_date[2] = Number(arr_date[2]) + (arr_date[2] < NUM_CENTYEAR ? 2000 : 1900);
	dt_date.setFullYear(arr_date[2]);

	var dt_numdays = new Date(arr_date[2], arr_date[0], 0);
	dt_date.setDate(arr_date[1]);
	if (dt_date.getMonth() != (arr_date[0]-1)) return alert ("Invalid day of month value: '" + arr_date[1] + "'.\nAllowed range is 01-"+dt_numdays.getDate()+".");

	return (dt_date)
}

// time parsing function
function cal_prs_time2 (str_time, dt_date) {

	if (!dt_date) return null;
	var arr_time = String(str_time ? str_time : '').split(':');

	if (!arr_time[0]) dt_date.setHours(0);
	else if (RE_NUM.exec(arr_time[0]))
		if (arr_time[0] < 24) dt_date.setHours(arr_time[0]);
		else return cal_error ("Invalid hours value: '" + arr_time[0] + "'.\nAllowed range is 00-23.");
	else return cal_error ("Invalid hours value: '" + arr_time[0] + "'.\nAllowed values are unsigned integers.");

	if (!arr_time[1]) dt_date.setMinutes(0);
	else if (RE_NUM.exec(arr_time[1]))
		if (arr_time[1] < 60) dt_date.setMinutes(arr_time[1]);
		else return cal_error ("Invalid minutes value: '" + arr_time[1] + "'.\nAllowed range is 00-59.");
	else return cal_error ("Invalid minutes value: '" + arr_time[1] + "'.\nAllowed values are unsigned integers.");

	if (!arr_time[2]) dt_date.setSeconds(0);
	else if (RE_NUM.exec(arr_time[2]))
		if (arr_time[2] < 60) dt_date.setSeconds(arr_time[2]);
		else return cal_error ("Invalid seconds value: '" + arr_time[2] + "'.\nAllowed range is 00-59.");
	else return cal_error ("Invalid seconds value: '" + arr_time[2] + "'.\nAllowed values are unsigned integers.");

	dt_date.setMilliseconds(0);
	return dt_date;
}

function cal_error (str_message) {
	alert (str_message);
	return null;
}

///////////////////////////////////////////////////////////////////////////////
// Sakai project developed JavaScript
// todo: hook up to individual controls
///////////////////////////////////////////////////////////////////////////////

// this code handles a date change from the color picker popup
// and sets the drop down options accordingly
// todo: need to only set the selections that are rendered
function onDateChange(id)
{
  var date = document.getElementById(id)
  var dayMenu = document.getElementById(id + "_day")
  var monthMenu = document.getElementById(id + "_month")
  var yearMenu = document.getElementById(id + "_year")
  var hourMenu = document.getElementById(id + "_hour")
  var minuteMenu = document.getElementById(id + "_minutes")
  var secondMenu = document.getElementById(id + "_seconds")
  var ampmMenu = document.getElementById(id + "_ampm")
  var dateInfo = cal_prs_date2 (date.value);
  var timeInfo = cal_prs_time2 (date.value);
  var day = (dateInfo.getDate() < 10 ? '0' : '') + dateInfo.getDate();
  var month = (dateInfo.getMonth() < 9 ? '0' : '') + (dateInfo.getMonth() + 1);
  var year = dateInfo.getFullYear();
  var hour = timeInfo.getHours();
  var minute = timeInfo.getMinutes();
  var second = timeInfo.getSeconds();
//  var ampm = ;
  swapSelected(dayMenu, day);
  swapSelected(monthMenu, month);
  swapSelected(yearMenu, year);
  swapSelected(hourMenu, hour);
  swapSelected(minuteMenu, minute);
  swapSelected(secondMenu, second);
//  swapSelected(ampmMenu, ampm);
}

// helper routine for above
// takes a date select option matching a value and set the selected attribute
function swapSelected(select, newSelection)
{
  for (loop=0; loop < select.options.length; loop++)
  {
    if (select.options[loop].text == newSelection)
    {
      select.selectedIndex = loop;
      return;
    }
  }
}

// this does the opposite, take a selection event on the drop down and set the
// main date control value
// id: clientId
// idExtension: _hour, _day, etc.
//
function onDateMenuChange(id, idExtension)
{
  var selection = document.getElementById(id + idExtension);
  var date = document.getElementById(id)
  var dateInfo = cal_prs_date2 (date.value);
  var timeInfo = cal_prs_time2 (date.value);

  if (idExtension == "_year")
  {
    dateInfo.setFullYear(selection.options[selection.options.selectedIndex].value)
  }
  else if (idExtension == "_month")
  {
    dateInfo.setMonth(selection.options[selection.options.selectedIndex].value)
  }
  else if (idExtension == "_day")
  {
    dateInfo.setDate(selection.options[selection.options.selectedIndex].value)
  }
   else if (idExtension == "_minute")
  {
    timeInfo.setMinutes(selection.options[selection.options.selectedIndex].value)
  }
  else if (idExtension == "_second")
  {
    timeInfo.setseconds(selection.options[selection.options.selectedIndex].value)
  }
//  else if (idExtension == "_ampm")
//  {
//    timeInfo.setAmPm(selection.options[selection.options.selectedIndex].value)
//  }
}
