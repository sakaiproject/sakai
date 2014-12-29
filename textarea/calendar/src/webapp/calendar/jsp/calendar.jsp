<%@ page import="java.text.DateFormatSymbols"
%><%@ page import="java.text.SimpleDateFormat"
%><%@ page import="java.util.Arrays"
%><%@ page import="java.util.Calendar"
%><%@ page import="java.util.Date"
%><%@ page import="java.util.List"
%><%@ page import="java.util.Locale"
%><%@ page import="java.util.ResourceBundle"
%><%@ page contentType="text/html; charset=UTF-8" %>
<!--
Title: Tigra Calendar
URL: http://www.softcomplex.com/products/tigra_calendar/
Version: 3.2
Date: 10/14/2002 (mm/dd/yyyy)
Feedback: feedback@softcomplex.com (specify product title in the subject)
Note: Permission given to use this script in ANY kind of applications if
   header lines are left unchanged.
Note: Script consists of two files: calendar?.js and calendar.html
About us: Our company provides offshore IT consulting services.
    Contact us at sales@softcomplex.com if you have any programming task you
    want to be handled by professionals. Our typical hourly rate is $20.
-->
<%! List<String> rtlLangs = Arrays.asList(new String[]{new Locale("ar").getLanguage(), new Locale("fa").getLanguage(), new Locale("iw").getLanguage(), new Locale("ji").getLanguage(), new Locale("ku").getLanguage(), new Locale("ur").getLanguage()});
%><%
	Locale locale = null;
	String localeString = request.getParameter("locale");
	if (localeString != null) {
		String[] params = localeString.split("_", 3);
		if (params.length == 1) {
			locale = new Locale(params[0]);
		} else if (params.length == 2) {
			locale = new Locale(params[0], params[1]);
		} else if (params.length == 3) {
			locale = new Locale(params[0], params[1], params[2]);
		}
	}
	if (locale == null) {
		locale = Locale.getDefault();
	}
	boolean isRtl = rtlLangs.contains(locale.getLanguage());

	String[] weekdays = DateFormatSymbols.getInstance(locale).getShortWeekdays();
	String [] months = DateFormatSymbols.getInstance(locale).getMonths();
	Date date;
	try {
		Long datetime = Long.valueOf(request.getParameter("datetime"));
		date = new Date(datetime);
	} catch (NumberFormatException e) {
		// datetime is null or illegal.
		date = new Date();
	}
	ResourceBundle rb = ResourceBundle.getBundle("calendar", locale);
	String format = rb.getString("cal.format");
	String dateString = new SimpleDateFormat(format, locale).format(date);
%>
<html>
<head>
<title><%= rb.getString("cal.title") %></title>
<style><%
	if (isRtl) {
		out.print("\n\tbody {direction: rtl;}");
	}%>
	td {font-family: Tahoma, Verdana, sans-serif; font-size: 12px;}
</style>
<script language="JavaScript">

// months as they appear in the calendar's title
var ARR_MONTHS = ["<%= months[0] %>", "<%= months[1] %>", "<%= months[2] %>", "<%= months[3] %>", "<%= months[4] %>", "<%= months[5] %>", "<%= months[6] %>", "<%= months[7] %>", "<%= months[8] %>", "<%= months[9] %>", "<%= months[10] %>", "<%= months[11] %>"];
// week day titles as they appear on the calendar
var ARR_WEEKDAYS = ["<%= weekdays[1] %>", "<%= weekdays[2] %>", "<%= weekdays[3] %>", "<%= weekdays[4] %>", "<%= weekdays[5] %>", "<%= weekdays[6] %>", "<%= weekdays[7] %>"];
// day week starts from (normally 0-Su or 1-Mo)
var NUM_WEEKSTART = <%= Calendar.getInstance(locale).getFirstDayOfWeek() - 1 %>;
// path to the directory where calendar images are stored. trailing slash req.
var STR_ICONPATH = '/library/calendar/images/calendar/';

var re_url = new RegExp('datetime=(\\-?\\d+)');
var dt_current = (re_url.exec(String(window.location))
	? new Date(new Number(RegExp.$1)) : new Date());
var re_id = new RegExp('id=(\\d+)');
var num_id = (re_id.exec(String(window.location))
	? new Number(RegExp.$1) : 0);
var obj_caller = (window.opener ? window.opener.calendars[num_id] : null);

if (obj_caller && obj_caller.year_scroll) {
	// get same date in the previous year
	var dt_prev_year = new Date(dt_current);
	dt_prev_year.setFullYear(dt_prev_year.getFullYear() - 1);
	if (dt_prev_year.getDate() != dt_current.getDate())
		dt_prev_year.setDate(0);
	
	// get same date in the next year
	var dt_next_year = new Date(dt_current);
	dt_next_year.setFullYear(dt_next_year.getFullYear() + 1);
	if (dt_next_year.getDate() != dt_current.getDate())
		dt_next_year.setDate(0);
}

// get same date in the previous month
var dt_prev_month = new Date(dt_current);
dt_prev_month.setMonth(dt_prev_month.getMonth() - 1);
if (dt_prev_month.getDate() != dt_current.getDate())
	dt_prev_month.setDate(0);

// get same date in the next month
var dt_next_month = new Date(dt_current);
dt_next_month.setMonth(dt_next_month.getMonth() + 1);
if (dt_next_month.getDate() != dt_current.getDate())
	dt_next_month.setDate(0);

// get first day to display in the grid for current month
var dt_firstday = new Date(dt_current);
dt_firstday.setDate(1);
dt_firstday.setDate(1 - (7 + dt_firstday.getDay() - NUM_WEEKSTART) % 7);

// function passing selected date to calling window
function set_datetime(n_datetime, b_close) {
	if (!obj_caller) return;

	var dt_datetime = obj_caller.prs_time(
		(document.cal ? document.cal.time.value : ''),
		new Date(n_datetime)
	);

	if (!dt_datetime) return;
	if (b_close) {
		obj_caller.target.value = (document.cal
			? obj_caller.gen_tsmp(dt_datetime)
			: obj_caller.gen_date(dt_datetime)
		);
		// %%% JANDERSE Added callback function
		if (obj_caller.callback) obj_caller.callback();
		window.close();
	}
	else 
	{
		obj_caller.popup(dt_datetime.valueOf());
		// %%% JANDERSE Added callback function
		if (obj_caller.callback) obj_caller.callback();
	}
}

</script>
</head>
<body bgcolor="#FFFFFF" marginheight="5" marginwidth="5" topmargin="5" leftmargin="5" rightmargin="5">
<table class="clsOTable" cellspacing="0" border="0" width="100%">
<tr><td bgcolor="#4682B4">
<table cellspacing="1" cellpadding="3" border="0" width="100%">
<tr><td colspan="7"><table cellspacing="0" cellpadding="0" border="0" width="100%">
<tr>
<script language="JavaScript">
document.write(
'<td>'+(obj_caller&&obj_caller.year_scroll?'<a href="javascript:set_datetime('+dt_prev_year.valueOf()+')"><img src="'+STR_ICONPATH+'<%
	if (isRtl) {
		out.print("next_year.gif");
	} else {
		out.print("prev_year.gif");
	}
%>" width="16" height="16" border="0" alt="<%= rb.getString("cal.lasy") %>"></a>&nbsp;':'')+'<a href="javascript:set_datetime('+dt_prev_month.valueOf()+')"><img src="'+STR_ICONPATH+'<%
	if (isRtl) {
		out.print("next.gif");
	} else {
		out.print("prev.gif");
	}
%>" width="16" height="16" border="0" alt="<%= rb.getString("cal.lasm") %>"></a></td>'+
'<td align="center" width="100%"><font color="#ffffff"><%= dateString %></font></td>'+
'<td><a href="javascript:set_datetime('+dt_next_month.valueOf()+')"><img src="'+STR_ICONPATH+'<%
	if (isRtl) {
		out.print("prev.gif");
	} else {
		out.print("next.gif");
	}
%>" width="16" height="16" border="0" alt="<%= rb.getString("cal.nexm") %>"></a>'+(obj_caller && obj_caller.year_scroll?'&nbsp;<a href="javascript:set_datetime('+dt_next_year.valueOf()+')"><img src="'+STR_ICONPATH+'<%
	if (isRtl) {
		out.print("prev_year.gif");
	} else {
		out.print("next_year.gif");
	}
%>" width="16" height="16" border="0" alt="<%= rb.getString("cal.nexy") %>"></a>':'')+'</td>'
);
</script>
</tr>
</table></td></tr>
<tr>
<script language="JavaScript">

// print weekdays titles
for (var n=0; n<7; n++)
	document.write('<td bgcolor="#87cefa" align="center"><font color="#ffffff">'+ARR_WEEKDAYS[(NUM_WEEKSTART+n)%7]+'</font></td>');
document.write('</tr>');

// print calendar table
var dt_current_day = new Date(dt_firstday);
while (dt_current_day.getMonth() == dt_current.getMonth() ||
	dt_current_day.getMonth() == dt_firstday.getMonth()) {
	// print row heder
	document.write('<tr>');
	for (var n_current_wday=0; n_current_wday<7; n_current_wday++) {
		if (dt_current_day.getDate() == dt_current.getDate() &&
			dt_current_day.getMonth() == dt_current.getMonth())
			// print current date
			document.write('<td bgcolor="#ffb6c1" align="center" width="14%">');
		else if (dt_current_day.getDay() == 0 || dt_current_day.getDay() == 6)
			// weekend days
			document.write('<td bgcolor="#dbeaf5" align="center" width="14%">');
		else
			// print working days of current month
			document.write('<td bgcolor="#ffffff" align="center" width="14%">');

		document.write('<a href="javascript:set_datetime('+dt_current_day.valueOf() +', true);">');

		if (dt_current_day.getMonth() == this.dt_current.getMonth())
			// print days of current month
			document.write('<font color="#000000">');
		else 
			// print days of other months
			document.write('<font color="#606060">');
			
		document.write(dt_current_day.getDate()+'</font></a></td>');
		dt_current_day.setDate(dt_current_day.getDate()+1);
	}
	// print row footer
	document.write('</tr>');
}
if (obj_caller && obj_caller.time_comp)
	document.write('<form onsubmit="javascript:set_datetime('+dt_current.valueOf()+', true)" name="cal"><tr><td colspan="7" bgcolor="#87CEFA"><font color="White" face="tahoma, verdana" size="2">Time: <input type="text" name="time" value="'+obj_caller.gen_time(this.dt_current)+'" size="8" maxlength="8"></font></td></tr></form>');
</script>
</table></tr></td>
</table>
</body>
</html>

