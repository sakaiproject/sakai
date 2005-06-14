// Grabbed from MyFaces 1.0.9 and revised to work around problems relating
// to MyFaces's transition to a new way of loading component resource
// dependencies. Changes commented as "// *** GRADEBOOK ***".

//	written	by Tan Ling	Wee	on 2 Dec 2001
//	last updated 20 June 2003
//	email :	fuushikaden@yahoo.com
//
// Modified to use the MyFaces lib resources

var	jscalendarFixedX = -1			// x position (-1 if to appear below control)
var	jscalendarFixedY = -1			// y position (-1 if to appear below control)
var jscalendarStartAt = 1			// 0 - sunday ; 1 - monday
var jscalendarShowWeekNumber = 1	// 0 - don't show; 1 - show
var jscalendarShowToday = 1		// 0 - don't show; 1 - show

// *** GRADEBOOK ***
// var jscalendarImgDir = "jscalendar/jscalendar-DB/"			// directory for images ... e.g. var jscalendarImgDir="/img/"
var jscalendarImgDir = "calendar/"			// directory for images ... e.g. var jscalendarImgDir="/img/"

var jscalendarThemePrefix = "jscalendar-DB"

var jscalendarGotoString = "Go To Current Month"
var jscalendarTodayString = "Today is"
var jscalendarWeekString = "Wk"
var jscalendarScrollLeftMessage = "Click to scroll to previous month. Hold mouse button to scroll automatically."
var jscalendarScrollRightMessage = "Click to scroll to next month. Hold mouse button to scroll automatically."
var jscalendarSelectMonthMessage = "Click to select a month."
var jscalendarSelectYearMessage = "Click to select a year."
var jscalendarSelectDateMessage = "Select [date] as date." // do not replace [date], it will be replaced by date.

var	jscalendarCrossobj, jscalendarCrossMonthObj, jscalendarCrossYearObj,
    jscalendarMonthSelected, jscalendarYearSelected, jscalendarDateSelected,
    jscalendarOmonthSelected, jscalendarOyearSelected, jscalendarOdateSelected,
    jscalendarMonthConstructed, jscalendarYearConstructed, jscalendarIntervalID1, jscalendarIntervalID2,
    jscalendarTimeoutID1, jscalendarTimeoutID2, jscalendarCtlToPlaceValue, jscalendarCtlNow, jscalendarDateFormat, jscalendarNStartingYear

var	jscalendarBPageLoaded=false
var	jscalendarIe=document.all
var	jscalendarDom=document.getElementById

var	jscalendarNs4=document.layers
var	jscalendarToday =	new	Date()
var	jscalendarDateNow	 = jscalendarToday.getDate()
var	jscalendarMonthNow = jscalendarToday.getMonth()
var	jscalendarYearNow	 = jscalendarToday.getYear()
var	jscalendarImgsrc = new Array("drop1.gif","drop2.gif","left1.gif","left2.gif","right1.gif","right2.gif")
var	jscalendarImg	= new Array()

var jscalendarBShow = false;

var jscalendarMyFacesCtlType = "x:inputCalendar";
var jscalendarMyFacesInputDateClientId;

function jscalendarSetImageDirectory(dir){ // For MyFaces only
// *** GRADEBOOK ***
//	jscalendarImgDir = dir;
}

/* hides <select> and <applet> objects (for IE only) */
function jscalendarHideElement( elmID, overDiv ){
  if( jscalendarIe ){
    for( i = 0; i < document.all.tags( elmID ).length; i++ ){
      obj = document.all.tags( elmID )[i];
      if( !obj || !obj.offsetParent )
        continue;

      // Find the element's offsetTop and offsetLeft relative to the BODY tag.
      objLeft   = obj.offsetLeft;
      objTop    = obj.offsetTop;
      objParent = obj.offsetParent;

      while( objParent.tagName.toUpperCase() != "BODY" ){
        objLeft  += objParent.offsetLeft;
        objTop   += objParent.offsetTop;
        objParent = objParent.offsetParent;
      }

      objHeight = obj.offsetHeight;
      objWidth = obj.offsetWidth;

      if(( overDiv.offsetLeft + overDiv.offsetWidth ) <= objLeft );
      else if(( overDiv.offsetTop + overDiv.offsetHeight ) <= objTop );
      else if( overDiv.offsetTop >= ( objTop + objHeight ));
      else if( overDiv.offsetLeft >= ( objLeft + objWidth ));
      else
        obj.style.visibility = "hidden";
    }
  }
}

/*
* unhides <select> and <applet> objects (for IE only)
*/
function jscalendarShowElement( elmID ){
  if( jscalendarIe ){
    for( i = 0; i < document.all.tags( elmID ).length; i++ ){
      obj = document.all.tags( elmID )[i];

      if( !obj || !obj.offsetParent )
        continue;

      obj.style.visibility = "";
    }
  }
}

function jscalendarHolidayRec (d, m, y, desc){
	this.d = d;
	this.m = m;
	this.y = y;
	this.desc = desc;
}

var jscalendarHolidaysCounter = 0;
var jscalendarHolidays = new Array();

function jscalendarAddHoliday (d, m, y, desc){
	jscalendarHolidays[jscalendarHolidaysCounter++] = new jscalendarHolidayRec ( d, m, y, desc );
}

if (jscalendarDom){
	for	(i=0;i<jscalendarImgsrc.length;i++)
		jscalendarImg[i] = new Image;

	document.write ("<div onclick='jscalendarBShow=true' id='calendar'	class='"+jscalendarThemePrefix+"-div-style'><table	width="+((jscalendarShowWeekNumber==1)?250:220)+" class='"+jscalendarThemePrefix+"-table-style'><tr class='"+jscalendarThemePrefix+"-title-background-style'><td><table width='"+((jscalendarShowWeekNumber==1)?248:218)+"'><tr><td class='"+jscalendarThemePrefix+"-title-style'><span id='caption'></span></td><td align=right><a href='javascript:jscalendarHideCalendar()'><span id='jscalendarCloseButton'></span></a></td></tr></table></td></tr><tr><td class='"+jscalendarThemePrefix+"-body-style'><span id='content'></span></td></tr>")

	if (jscalendarShowToday==1)
		document.write ("<tr class='"+jscalendarThemePrefix+"-today-style'><td class='"+jscalendarThemePrefix+"-today-lbl-style'><span id='lblToday'></span></td></tr>")

	document.write ("</table></div><div id='selectMonth' class='"+jscalendarThemePrefix+"-div-style'></div><div id='selectYear' class='"+jscalendarThemePrefix+"-div-style'></div>");
}

var	jscalendarMonthName = new Array("January","February","March","April","May","June","July","August","September","October","November","December");
var	jscalendarMonthName2 = new Array("JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC");
var	jscalendarDayName = jscalendarStartAt==0 ? new Array("Sun","Mon","Tue","Wed","Thu","Fri","Sat") : new Array("Mon","Tue","Wed","Thu","Fri","Sat","Sun");

function jscalendarSwapImage(srcImg, destImg){
	if (jscalendarIe)
		document.getElementById(srcImg).setAttribute("src",jscalendarImgDir + destImg);
}

function jscalendarInit(){
	if (!jscalendarNs4){
		if (!jscalendarIe)
			jscalendarYearNow += 1900;

		jscalendarCrossobj=(jscalendarDom) ? document.getElementById("calendar").style : jscalendarIe ? document.all.calendar : document.calendar;
		jscalendarHideCalendar();

		jscalendarCrossMonthObj=(jscalendarDom) ? document.getElementById("selectMonth").style : jscalendarIe ? document.all.selectMonth : document.selectMonth;

		jscalendarCrossYearObj=(jscalendarDom) ? document.getElementById("selectYear").style : jscalendarIe ? document.all.jscalendarSelectYear : document.jscalendarSelectYear;

		jscalendarMonthConstructed=false;
		jscalendarYearConstructed=false;

		if (jscalendarShowToday==1)
			document.getElementById("lblToday").innerHTML =	jscalendarTodayString + " <a onmousemove='window.status=\""+jscalendarGotoString+"\"' onmouseout='window.status=\"\"' title='"+jscalendarGotoString+"' class='"+jscalendarThemePrefix+"-today-style' href='javascript:jscalendarMonthSelected=jscalendarMonthNow;jscalendarYearSelected=jscalendarYearNow;jscalendarConstructCalendar();'>"+jscalendarDayName[(jscalendarToday.getDay()-jscalendarStartAt==-1)?6:(jscalendarToday.getDay()-jscalendarStartAt)]+", " + jscalendarDateNow + " " + jscalendarMonthName[jscalendarMonthNow].substring(0,3)	+ "	" +	jscalendarYearNow	+ "</a>";

		var sHTML1 ="<span id='spanLeft'  class='"+jscalendarThemePrefix+"-title-control-normal-style' onmouseover='jscalendarSwapImage(\"changeLeft\",\"left2.gif\");  this.className=\""+jscalendarThemePrefix+"-title-control-select-style\"; window.status=\""+jscalendarScrollLeftMessage+"\"' onclick='javascript:jscalendarDecMonth()' onmouseout='clearInterval(jscalendarIntervalID1);jscalendarSwapImage(\"changeLeft\",\"left1.gif\"); this.className=\""+jscalendarThemePrefix+"-title-control-normal-style\"; window.status=\"\"' onmousedown='clearTimeout(jscalendarTimeoutID1);jscalendarTimeoutID1=setTimeout(\"jscalendarStartDecMonth()\",500)'	onmouseup='clearTimeout(jscalendarTimeoutID1);clearInterval(jscalendarIntervalID1)'>&nbsp<IMG id='changeLeft' SRC='"+jscalendarImgDir+"left1.gif' width=10 height=11 BORDER=0>&nbsp</span>&#160;"
		sHTML1+="<span id='spanRight' class='"+jscalendarThemePrefix+"-title-control-normal-style' onmouseover='jscalendarSwapImage(\"changeRight\",\"right2.gif\");this.className=\""+jscalendarThemePrefix+"-title-control-select-style\"; window.status=\""+jscalendarScrollRightMessage+"\"' onmouseout='clearInterval(jscalendarIntervalID1);jscalendarSwapImage(\"changeRight\",\"right1.gif\"); this.className=\""+jscalendarThemePrefix+"-title-control-normal-style\"; window.status=\"\"' onclick='jscalendarIncMonth()' onmousedown='clearTimeout(jscalendarTimeoutID1);jscalendarTimeoutID1=setTimeout(\"jscalendarStartIncMonth()\",500)'	onmouseup='clearTimeout(jscalendarTimeoutID1);clearInterval(jscalendarIntervalID1)'>&nbsp<IMG id='changeRight' SRC='"+jscalendarImgDir+"right1.gif'	width=10 height=11 BORDER=0>&nbsp</span>&nbsp"
		sHTML1+="<span id='spanMonth' class='"+jscalendarThemePrefix+"-title-control-normal-style' onmouseover='jscalendarSwapImage(\"changeMonth\",\"drop2.gif\"); this.className=\""+jscalendarThemePrefix+"-title-control-select-style\"; window.status=\""+jscalendarSelectMonthMessage+"\"' onmouseout='jscalendarSwapImage(\"changeMonth\",\"drop1.gif\"); this.className=\""+jscalendarThemePrefix+"-title-control-normal-style\"; window.status=\"\"' onclick='jscalendarPopUpMonth()'></span>&#160;"
		sHTML1+="<span id='spanYear'  class='"+jscalendarThemePrefix+"-title-control-normal-style' onmouseover='jscalendarSwapImage(\"changeYear\",\"drop2.gif\");  this.className=\""+jscalendarThemePrefix+"-title-control-select-style\"; window.status=\""+jscalendarSelectYearMessage+"\"'	onmouseout='jscalendarSwapImage(\"changeYear\",\"drop1.gif\"); this.className=\""+jscalendarThemePrefix+"-title-control-normal-style\"; window.status=\"\"'	onclick='jscalendarPopUpYear()'></span>&#160;"

		document.getElementById("caption").innerHTML = sHTML1;

		jscalendarBPageLoaded = true;
	}
}

function jscalendarHideCalendar(){
	jscalendarCrossobj.visibility="hidden"
	if (jscalendarCrossMonthObj != null){jscalendarCrossMonthObj.visibility="hidden"}
	if (jscalendarCrossYearObj !=	null){jscalendarCrossYearObj.visibility="hidden"}

    jscalendarShowElement( 'SELECT' );
	jscalendarShowElement( 'APPLET' );
}

function jscalendarPadZero(num){
	return (num	< 10)? '0' + num : num ;
}

function jscalendarConstructDate(d,m,y){
	var sTmp = jscalendarDateFormat
	sTmp = sTmp.replace	("dd","<e>")
	sTmp = sTmp.replace	("d","<d>")
	sTmp = sTmp.replace	("<e>",jscalendarPadZero(d))
	sTmp = sTmp.replace	("<d>",d)
	sTmp = sTmp.replace	("mmmm","<p>")
	sTmp = sTmp.replace	("MMMM","<p>")
	sTmp = sTmp.replace	("mmm","<o>")
	sTmp = sTmp.replace	("MMM","<o>")
	sTmp = sTmp.replace	("mm","<n>")
	sTmp = sTmp.replace	("MM","<n>")
	sTmp = sTmp.replace	("m","<m>")
	sTmp = sTmp.replace	("M","<m>")
	sTmp = sTmp.replace	("<m>",m+1)
	sTmp = sTmp.replace	("<n>",jscalendarPadZero(m+1))
	sTmp = sTmp.replace	("<o>",jscalendarMonthName[m])
	sTmp = sTmp.replace	("<p>",jscalendarMonthName2[m])
	sTmp = sTmp.replace	("yyyy",y)
	return sTmp.replace ("yy",jscalendarPadZero(y%100))
}

function jscalendarCloseCalendar() {
	jscalendarHideCalendar();

	if( jscalendarMyFacesCtlType!="x:inputDate" )
		jscalendarCtlToPlaceValue.value = jscalendarConstructDate(jscalendarDateSelected,jscalendarMonthSelected,jscalendarYearSelected)
	else{
		document.getElementById(jscalendarMyFacesInputDateClientId+".day").value = jscalendarDateSelected;
		document.getElementById(jscalendarMyFacesInputDateClientId+".month").value = jscalendarMonthSelected+1;
		document.getElementById(jscalendarMyFacesInputDateClientId+".year").value = jscalendarYearSelected;
	}
}

/*** Month Pulldown	***/

function jscalendarStartDecMonth(){
	jscalendarIntervalID1=setInterval("jscalendarDecMonth()",80);
}

function jscalendarStartIncMonth(){
	jscalendarIntervalID1=setInterval("jscalendarIncMonth()",80);
}

function jscalendarIncMonth(){
	jscalendarMonthSelected++;
	if (jscalendarMonthSelected>11) {
		jscalendarMonthSelected=0;
		jscalendarYearSelected++;
	}
	jscalendarConstructCalendar();
}

function jscalendarDecMonth () {
	jscalendarMonthSelected--
	if (jscalendarMonthSelected<0) {
		jscalendarMonthSelected=11
		jscalendarYearSelected--
	}
	jscalendarConstructCalendar()
}

function jscalendarConstructMonth(){
	jscalendarPopDownYear();
	if (!jscalendarMonthConstructed) {
		var sHTML =	"";
		for	(i=0; i<12;	i++) {
			var sName = jscalendarMonthName[i];
			if (i==jscalendarMonthSelected)
				sName =	"<b>" +	sName +	"</b>";
			sHTML += "<tr><td id='m" + i + "' onmouseover='this.className=\""+jscalendarThemePrefix+"-dropdown-select-style\"' onmouseout='this.className=\""+jscalendarThemePrefix+"-dropdown-normal-style\"' onclick='jscalendarMonthConstructed=false;jscalendarMonthSelected=" + i + ";jscalendarConstructCalendar();jscalendarPopDownMonth();event.cancelBubble=true'>&#160;" + sName + "&#160;</td></tr>";
		}

		document.getElementById("selectMonth").innerHTML = "<table width='70' class='"+jscalendarThemePrefix+"-dropdown-style'  cellspacing=0 onmouseover='clearTimeout(jscalendarTimeoutID1)'	onmouseout='clearTimeout(jscalendarTimeoutID1);jscalendarTimeoutID1=setTimeout(\"jscalendarPopDownMonth()\",100);event.cancelBubble=true'>" +	sHTML +	"</table>";

		jscalendarMonthConstructed=true;
	}
}

function jscalendarPopUpMonth() {
	jscalendarConstructMonth()
	jscalendarCrossMonthObj.visibility = (jscalendarDom||jscalendarIe)? "visible"	: "show"
	jscalendarCrossMonthObj.left = parseInt(jscalendarCrossobj.left) + 50 + "px";
	jscalendarCrossMonthObj.top =	parseInt(jscalendarCrossobj.top) + 26 + "px";

	jscalendarHideElement( 'SELECT', document.getElementById("selectMonth") );
	jscalendarHideElement( 'APPLET', document.getElementById("selectMonth") );
}

function jscalendarPopDownMonth()	{
	jscalendarCrossMonthObj.visibility= "hidden"
}

/*** Year Pulldown ***/

function jscalendarIncYear() {
	for	(i=0; i<7; i++){
		newYear	= (i+jscalendarNStartingYear)+1
		if (newYear==jscalendarYearSelected)
		{ txtYear =	"&#160;<B>"	+ newYear +	"</B>&#160;" }
		else
		{ txtYear =	"&#160;" + newYear + "&#160;" }
		document.getElementById("y"+i).innerHTML = txtYear
	}
	jscalendarNStartingYear++;
	jscalendarBShow=true;
}

function jscalendarDecYear() {
	for	(i=0; i<7; i++){
		newYear	= (i+jscalendarNStartingYear)-1
		if (newYear==jscalendarYearSelected)
		{ txtYear =	"&#160;<B>"	+ newYear +	"</B>&#160;" }
		else
		{ txtYear =	"&#160;" + newYear + "&#160;" }
		document.getElementById("y"+i).innerHTML = txtYear
	}
	jscalendarNStartingYear--;
	jscalendarBShow=true;
}

function jscalendarSelectYear(nYear) {
	jscalendarYearSelected=parseInt(nYear+jscalendarNStartingYear);
	jscalendarYearConstructed=false;
	jscalendarConstructCalendar();
	jscalendarPopDownYear();
}

function jscalendarConstructYear() {
	jscalendarPopDownMonth();
	var sHTML =	"";
	if (!jscalendarYearConstructed) {

		sHTML =	"<tr><td align='center'	onmouseover='this.className=\""+jscalendarThemePrefix+"-dropdown-select-style\"' onmouseout='clearInterval(jscalendarIntervalID1); this.className=\""+jscalendarThemePrefix+"-dropdown-normal-style\"' onmousedown='clearInterval(jscalendarIntervalID1);jscalendarIntervalID1=setInterval(\"jscalendarDecYear()\",30)' onmouseup='clearInterval(jscalendarIntervalID1)'>-</td></tr>";

		var j =	0;
		jscalendarNStartingYear = jscalendarYearSelected-3;
		for	(i=jscalendarYearSelected-3; i<=(jscalendarYearSelected+3); i++) {
			var sName =	i;
			if (i==jscalendarYearSelected)
				sName =	"<b>"+sName+"</b>";

			sHTML += "<tr><td id='y"+j+"' onmouseover='this.className=\""+jscalendarThemePrefix+"-dropdown-select-style\"' onmouseout='this.className=\""+jscalendarThemePrefix+"-dropdown-normal-style\"' onclick='jscalendarSelectYear("+j+");event.cancelBubble=true'>&#160;"+sName+"&#160;</td></tr>";
			j++;
		}

		sHTML += "<tr><td align='center' onmouseover='this.className=\""+jscalendarThemePrefix+"-dropdown-select-style\"' onmouseout='clearInterval(jscalendarIntervalID2); this.className=\""+jscalendarThemePrefix+"-dropdown-normal-style\"' onmousedown='clearInterval(jscalendarIntervalID2);jscalendarIntervalID2=setInterval(\"jscalendarIncYear()\",30)'	onmouseup='clearInterval(jscalendarIntervalID2)'>+</td></tr>";

		document.getElementById("selectYear").innerHTML	= "<table width='44' class='"+jscalendarThemePrefix+"-dropdown-style' onmouseover='clearTimeout(jscalendarTimeoutID2)' onmouseout='clearTimeout(jscalendarTimeoutID2);jscalendarTimeoutID2=setTimeout(\"jscalendarPopDownYear()\",100)' cellspacing='0'>"+sHTML+"</table>";

		jscalendarYearConstructed = true;
	}
}

function jscalendarPopDownYear() {
	clearInterval(jscalendarIntervalID1);
	clearTimeout(jscalendarTimeoutID1);
	clearInterval(jscalendarIntervalID2);
	clearTimeout(jscalendarTimeoutID2);
	jscalendarCrossYearObj.visibility= "hidden";
}

function jscalendarPopUpYear() {
	var	leftOffset;

	jscalendarConstructYear();
	jscalendarCrossYearObj.visibility = (jscalendarDom||jscalendarIe) ? "visible" : "show";
	leftOffset = parseInt(jscalendarCrossobj.left) + document.getElementById("spanYear").offsetLeft;
	if (jscalendarIe)
		leftOffset += 6;
	jscalendarCrossYearObj.left =	leftOffset + "px";
	jscalendarCrossYearObj.top = parseInt(jscalendarCrossobj.top) +	26 + "px";
}

/*** calendar ***/
function jscalendarWeekNbr(n) {
	// Algorithm used:
	// From Klaus Tondering's Calendar document (The Authority/Guru)
	// hhtp://www.tondering.dk/claus/calendar.html
	// a = (14-month) / 12
	// y = year + 4800 - a
	// m = month + 12a - 3
	// J = day + (153m + 2) / 5 + 365y + y / 4 - y / 100 + y / 400 - 32045
	// d4 = (J + 31741 - (J mod 7)) mod 146097 mod 36524 mod 1461
	// L = d4 / 1460
	// d1 = ((d4 - L) mod 365) + L
	// WeekNumber = d1 / 7 + 1

	year = n.getFullYear();
	month = n.getMonth() + 1;
	if (jscalendarStartAt == 0)
		day = n.getDate() + 1;
	else
		day = n.getDate();

	a = Math.floor((14-month) / 12);
	y = year + 4800 - a;
	m = month + 12 * a - 3;
	b = Math.floor(y/4) - Math.floor(y/100) + Math.floor(y/400);
	J = day + Math.floor((153 * m + 2) / 5) + 365 * y + b - 32045;
	d4 = (((J + 31741 - (J % 7)) % 146097) % 36524) % 1461;
	L = Math.floor(d4 / 1460);
	d1 = ((d4 - L) % 365) + L;
	week = Math.floor(d1/7) + 1;

	return week;
}

function jscalendarConstructCalendar () {
	var aNumDays = Array (31,0,31,30,31,30,31,31,30,31,30,31);

	var dateMessage;
	var	startDate =	new	Date (jscalendarYearSelected,jscalendarMonthSelected,1);
	var endDate;

	if (jscalendarMonthSelected==1){
		endDate	= new Date (jscalendarYearSelected,jscalendarMonthSelected+1,1);
		endDate	= new Date (endDate	- (24*60*60*1000));
		numDaysInMonth = endDate.getDate();
	}else
		numDaysInMonth = aNumDays[jscalendarMonthSelected];


	datePointer	= 0;
	dayPointer = startDate.getDay() - jscalendarStartAt;

	if (dayPointer<0)
		dayPointer = 6;

	var sHTML = "<table border=0 class='"+jscalendarThemePrefix+"-body-style'><tr>"

	if (jscalendarShowWeekNumber==1)
		sHTML += "<td width=27><b>" + jscalendarWeekString + "</b></td><td width=1 rowspan=7 class='"+jscalendarThemePrefix+"-weeknumber-div-style'><img src='"+jscalendarImgDir+"divider.gif' width=1></td>";

	for	(i=0; i<7; i++)
		sHTML += "<td width='27' align='right'><B>"+ jscalendarDayName[i]+"</B></td>";

	sHTML +="</tr><tr>";

	if (jscalendarShowWeekNumber==1)
		sHTML += "<td align=right>" + jscalendarWeekNbr(startDate) + "&#160;</td>";

	for	( var i=1; i<=dayPointer;i++ )
		sHTML += "<td>&#160;</td>";

	for	( datePointer=1; datePointer<=numDaysInMonth; datePointer++ ){
		dayPointer++;
		sHTML += "<td align=right>";

		var sStyle=jscalendarThemePrefix+"-normal-day-style"; //regular day

		if ((datePointer==jscalendarDateNow)&&(jscalendarMonthSelected==jscalendarMonthNow)&&(jscalendarYearSelected==jscalendarYearNow)) //today
		{ sStyle = jscalendarThemePrefix+"-current-day-style"; }
		else if	(dayPointer % 7 == (jscalendarStartAt * -1) +1) //end-of-the-week day
		{ sStyle = jscalendarThemePrefix+"-end-of-weekday-style"; }

		//selected day
		if ((datePointer==jscalendarOdateSelected) &&	(jscalendarMonthSelected==jscalendarOmonthSelected)	&& (jscalendarYearSelected==jscalendarOyearSelected))
		{ sStyle += " "+jscalendarThemePrefix+"-selected-day-style"; }

		sHint = ""
		for (k=0;k<jscalendarHolidaysCounter;k++)
		{
			if ((parseInt(jscalendarHolidays[k].d)==datePointer)&&(parseInt(jscalendarHolidays[k].m)==(jscalendarMonthSelected+1)))
			{
				if ((parseInt(jscalendarHolidays[k].y)==0)||((parseInt(jscalendarHolidays[k].y)==jscalendarYearSelected)&&(parseInt(jscalendarHolidays[k].y)!=0)))
				{
					sStyle += " "+jscalendarThemePrefix+"-holiday-style";
					sHint+=sHint==""?jscalendarHolidays[k].desc:"\n"+jscalendarHolidays[k].desc
				}
			}
		}

		var regexp= /\"/g
		sHint=sHint.replace(regexp,"&quot;");

		sSelectStyle = sStyle+" "+jscalendarThemePrefix+"-would-be-selected-day-style";
		sNormalStyle = sStyle;

		dateMessage = "onmousemove='window.status=\""+jscalendarSelectDateMessage.replace("[date]",jscalendarConstructDate(datePointer,jscalendarMonthSelected,jscalendarYearSelected))+"\"' onmouseout='this.className=\""+sNormalStyle+"\"; window.status=\"\"' "

		sHTML += "<a class='"+sStyle+"' "+dateMessage+" title=\"" + sHint + "\" href='javascript:jscalendarDateSelected="+datePointer+";jscalendarCloseCalendar();' onmouseover='this.className=\""+sSelectStyle+"\";' >&#160;" + datePointer + "&#160;</a>";

		if ((dayPointer+jscalendarStartAt) % 7 == jscalendarStartAt) {
			sHTML += "</tr><tr>";
			if ((jscalendarShowWeekNumber==1)&&(datePointer<numDaysInMonth))
				sHTML += "<td align=right>" + (jscalendarWeekNbr(new Date(jscalendarYearSelected,jscalendarMonthSelected,datePointer+1))) + "&#160;</td>";
		}
	}

	document.getElementById("content").innerHTML = sHTML;
	document.getElementById("spanMonth").innerHTML = "&#160;" +	jscalendarMonthName[jscalendarMonthSelected] + "&#160;<IMG id='changeMonth' SRC='"+jscalendarImgDir+"drop1.gif' WIDTH='12' HEIGHT='10' BORDER=0>";
	document.getElementById("spanYear").innerHTML =	"&#160;" + jscalendarYearSelected	+ "&#160;<IMG id='changeYear' SRC='"+jscalendarImgDir+"drop1.gif' WIDTH='12' HEIGHT='10' BORDER=0>";
	document.getElementById("jscalendarCloseButton").innerHTML = "<img src='"+jscalendarImgDir+"close.gif' width='15' height='13' border='0' alt='Close the Calendar'>";
}

function jscalendarPopUpCalendar(ctl, ctl2, format){
	if (jscalendarBPageLoaded){
		if ( jscalendarCrossobj.visibility == "hidden" ) {
			jscalendarCtlToPlaceValue = ctl2;
			jscalendarDateFormat=format;

			var formatChar = " ";
			aFormat	= jscalendarDateFormat.split(formatChar)
			if (aFormat.length<3){
				formatChar = "/";
				aFormat	= jscalendarDateFormat.split(formatChar)
				if (aFormat.length<3){
					formatChar = ".";
					aFormat	= jscalendarDateFormat.split(formatChar)
					if (aFormat.length<3){
						formatChar = "-";
						aFormat	= jscalendarDateFormat.split(formatChar)
						if (aFormat.length<3){
							// invalid date	format
							formatChar="";
						}
					}
				}
			}

			var tokensChanged =	0;
			if ( formatChar	!= "" ){
				// use user's date
				aData =	ctl2.value.split(formatChar)

				for	(i=0;i<3;i++){
					if ((aFormat[i]=="d") || (aFormat[i]=="dd")){
						jscalendarDateSelected = parseInt(aData[i]);
						tokensChanged++;
					}else if ((aFormat[i]=="m") || (aFormat[i]=="mm") || (aFormat[i]=="M") || (aFormat[i]=="MM")){
						jscalendarMonthSelected = parseInt(aData[i]) - 1;
						tokensChanged++;
					}else if (aFormat[i]=="yyyy"){
						jscalendarYearSelected = parseInt(aData[i]);
						tokensChanged++;
					}else if (aFormat[i]=="yy"){
					    newYear = parseInt(aData[i]);

					    if(newYear>50)
						    jscalendarYearSelected = 1900+newYear;
						else
						    jscalendarYearSelected = 2000+newYear;

						tokensChanged++
					}else if (aFormat[i]=="mmm" || aFormat[i]=="MMM"){
						for	(j=0; j<12;	j++){
							if (aData[i]==jscalendarMonthName[j]){
								jscalendarMonthSelected=j;
								tokensChanged++;
							}
						}
					}else if (aFormat[i]=="mmmm" || aFormat[i]=="MMMM"){
						for	(j=0; j<12;	j++){
							if (aData[i]==jscalendarMonthName2[j]){
								jscalendarMonthSelected=j;
								tokensChanged++;
							}
						}
					}
				}
			}

			if ((tokensChanged!=3)||isNaN(jscalendarDateSelected)||isNaN(jscalendarMonthSelected)||isNaN(jscalendarYearSelected)){
				jscalendarDateSelected = jscalendarDateNow;
				jscalendarMonthSelected =	jscalendarMonthNow;
				jscalendarYearSelected = jscalendarYearNow;
			}

			jscalendarPopUpCalendar_Show(ctl);
		}else{
			jscalendarHideCalendar();
			if (jscalendarCtlNow!=ctl)
				jscalendarPopUpCalendar(ctl, ctl2, format);
		}
		jscalendarCtlNow = ctl;
	}
}

function jscalendarPopUpCalendarForInputDate(clientId, format){
	if (jscalendarBPageLoaded){
		jscalendarMyFacesCtlType = "x:inputDate";
		jscalendarMyFacesInputDateClientId = clientId;
		jscalendarDateFormat=format;

		jscalendarDateSelected = parseInt( document.getElementById(clientId+".day").value );
		jscalendarMonthSelected = parseInt( document.getElementById(clientId+".month").value )-1;
		jscalendarYearSelected = parseInt( document.getElementById(clientId+".year").value );
		jscalendarCtlNow = document.getElementById(clientId+".day");
		jscalendarPopUpCalendar_Show(document.getElementById(clientId+".day"));
	}
}

function jscalendarPopUpCalendar_Show(ctl){
	jscalendarOdateSelected = jscalendarDateSelected;
	jscalendarOmonthSelected = jscalendarMonthSelected;
	jscalendarOyearSelected = jscalendarYearSelected;

	var	leftpos = 0;
	var	toppos = 0;

	var aTag = ctl;
	do {
		aTag = aTag.offsetParent;
		leftpos	+= aTag.offsetLeft;
		toppos += aTag.offsetTop;
	} while(aTag.tagName!="BODY");

	jscalendarCrossobj.left = jscalendarFixedX==-1 ? ctl.offsetLeft	+ leftpos + "px": jscalendarFixedX;
	jscalendarCrossobj.top = jscalendarFixedY==-1 ?	ctl.offsetTop +	toppos + ctl.offsetHeight +	2 + "px": jscalendarFixedY;
	jscalendarConstructCalendar (1, jscalendarMonthSelected, jscalendarYearSelected);
	jscalendarCrossobj.visibility=(jscalendarDom||jscalendarIe)? "visible" : "show";

	jscalendarHideElement( 'SELECT', document.getElementById("calendar") );
	jscalendarHideElement( 'APPLET', document.getElementById("calendar") );

	jscalendarBShow = true;
}

document.onkeypress = function jscalendarHidecal1 () {
	// ***GRADEBOOK*** Avoid "event is not defined" JavaScript errors.
	//	if (event.keyCode==27)
	if (jscalendarBShow && event.keyCode==27)
		jscalendarHideCalendar();
}
document.onclick = function jscalendarHidecal2 () {
	if (!jscalendarBShow)
		jscalendarHideCalendar();
	jscalendarBShow = false;
}

if(jscalendarIe)
{
	jscalendarInit();
}
else
{
	// chain the calendar's onload to the existing onload
	if (window.onload)
	{
		var chainedOnload = window.onload;
		window.onload = function chainedOnloadFunction () {
			jscalendarInit();
			chainedOnload();
		}
	}
	else
	{
		window.onload = jscalendarInit();
	}
}