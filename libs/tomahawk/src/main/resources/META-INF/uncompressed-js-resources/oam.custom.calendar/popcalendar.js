//	based on code written by Tan Ling Wee on 2 Dec 2001
//	last updated 20 June 2003
//	email :	fuushikaden@yahoo.com
//
// Modified to be completely object-oriented, CSS based and using proper DOM-access functions
// @author Martin Marinschek
// @author Sylvain Vieujot


org_apache_myfaces_CalendarInitData = function()
{
    // x position (-1 if to appear below control)
    this.fixedX = -1;

    // y position (-1 if to appear below control)
    this.fixedY = -1;

    // 0 - sunday ; 1 - monday (aka firstDayOfWeek)
    this.startAt = 1;

    // 0 - don't show; 1 - show
    this.showWeekNumber = 1;

    // 0 - don't show; 1 - show
    this.showToday = 1;

    // directory for images ... e.g. this.imgDir="/img/"
    this.imgDir = "images/";
    
    this.imgDirSuffix = "";

    this.themePrefix = "jscalendar-DB";

    this.gotoString = "Go To Current Month";
    this.todayString = "Today is";
    this.todayDateFormat = null;
    this.weekString = "Wk";
    this.scrollLeftMessage = "Click to scroll to previous month. Hold mouse button to scroll automatically.";
    this.scrollRightMessage = "Click to scroll to next month. Hold mouse button to scroll automatically."
    this.selectMonthMessage = "Click to select a month."
    this.selectYearMessage = "Click to select a year."
    this.selectDateMessage = "Select [date] as date." // do not replace [date], it will be replaced by date.

    this.popupLeft=false;

    this.monthName = new Array("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
    this.dayName = this.startAt == 0 ? new Array("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat") : new Array("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");

    this.selectMode = "day";
}

org_apache_myfaces_DateParts = function(sec, min, hour, date, month, year)
{
    this.sec = sec;
    this.min = min;
    this.hour = hour;
    this.date = date;
    this.month = month;
    this.year = year;
}

org_apache_myfaces_HolidayRec = function(d, m, y, desc)
{
    this.d = d;
    this.m = m;
    this.y = y;
    this.desc = desc;
}

org_apache_myfaces_PopupCalendar = function()
{
    this.idPrefix = "org_apache_myfaces_PopupCalendar";

    this.selectedDate = new org_apache_myfaces_DateParts(0, 0, 0, 0, 0, 0);
    this.saveSelectedDate = new org_apache_myfaces_DateParts(0, 0, 0, 0, 0, 0);

    this.monthConstructed = false;
    this.yearConstructed = false;
    this.intervalID1;
    this.intervalID2;
    this.timeoutID1;
    this.timeoutID2;
    this.ctlToPlaceValue;
    this.ctlNow;
    this.containerCtl;
    this.dateFormat="MM/dd/yyyy";
    this.nStartingYear;
    this.bPageLoaded = false;

    // Detect whether the browser is Microsoft Internet Explorer.
    // Testing for the presence of document.all is not sufficient, as Opera provides that.
    // However hopefully nothing but IE implements ActiveX..
    this.ie = window.ActiveXObject ? true : false;

    this.dom = document.getElementById;
    this.ns4 = document.layers;
    this.dateFormatSymbols = new org_apache_myfaces_dateformat_DateFormatSymbols();
    this.initData = new org_apache_myfaces_CalendarInitData();
    this.today = new Date();
    this.dateNow = this.today.getDate();
    this.monthNow = this.today.getMonth();
    this.yearNow = this.today.getFullYear();
    
    // list of images to be preloaded from the server
    this.imgSrc = new Array("drop1.gif", "drop2.gif", "left1.gif", "left2.gif", "right1.gif", "right2.gif");
    this.img = new Array();

    //elements which need to change their dynamical
    //representation over time
    this.calendarDiv;
    this.selectMonthDiv;
    this.selectYearDiv;
    this.todaySpan = null;
    this.captionSpan = null;
    this.contentSpan = null;
    this.acceptMonthSpan = null;
    this.closeCalendarSpan = null;
    this.monthSpan = null;
    this.yearSpan = null
    this.changeMonthImg = null;
    this.changeYearImg = null;

    this.holidaysCounter = 0;
    this.holidays = new Array();

    this.bClickOnCalendar = false;
    this.bCalendarHidden = true;

    this.myFacesCtlType = "x:inputCalendar";
    this.inputDateClientId;
}

org_apache_myfaces_PopupCalendar.prototype._MSECS_PER_DAY = 24*60*60*1000;

/**
popups always have to be
at the dom level nowhere else
*/
org_apache_myfaces_PopupCalendar.prototype._fixPopupDomOrder = function(overDiv) {
	if(document.body != overDiv.parentNode) {
		overDiv.parentNode.removeChild(overDiv);
		document.body.appendChild(overDiv);
	}	
};

// IE bug workaround: hide background controls under the specified div.
org_apache_myfaces_PopupCalendar.prototype._showPopupPostProcess = function(overDiv)
{
	

    if (this.ie)
    {
        // The iframe created here is a hack to work around an IE bug. In IE,
        // "windowed controls" (esp selectboxes) do not respect the z-index
        // setting of "non-windowed controls", meaning they will be drawn on
        // top of components that they should theoretically be underneath.
        // However a selectbox will NOT be drawn on top of an iframe, so the
        // workaround is to create an iframe with no content, then in function
        // _recalculateElement position the iframe under the "popup" div to
        // "mask out" the unwanted elements. 
        var iframe = document.getElementById(overDiv.id + "_IFRAME");

        if (iframe == null)
        {
            // the source attribute is to avoid a IE error message about non secure content on https connections
            iframe = document.createElement("iframe");
            iframe.setAttribute("id", overDiv.id + "_IFRAME'");
            iframe.setAttribute("src", "javascript:false;")
            Element.setStyle(iframe, "visibility:hidden; position: absolute; top:0px;left:0px;");
            
            //we can append it lazily since we are late here anyway and everything is rendered
            document.body.appendChild(iframe);
        }

        // now put the iframe at the appropriate location, and make it visible.
        this._recalculateElement(overDiv);
    }
}

// IE bug workaround: hide background controls under the specified div; see _showPopupPostProcess.
// This should be called whenever a popup div is moved.
org_apache_myfaces_PopupCalendar.prototype._recalculateElement = function(overDiv)
{
    if (this.ie)
    {
        var iframe = document.getElementById(overDiv.id + "_IFRAME");

        if (iframe)
        {
            // ok, there is a "masking iframe" associated with this div, so make its
            // size and position match the div exactly, and set its z-index to just
            // below the div. This hack blocks IE selectboxes from being drawn on top
            // of the div. 
            var popup = overDiv;

            popup.style.zIndex = 98;

            iframe.style.zIndex = popup.style.zIndex - 1;
            iframe.style.width = popup.offsetWidth;
            iframe.style.height = popup.offsetHeight;
            iframe.style.top = popup.style.top;
            iframe.style.left = popup.style.left;
            iframe.style.display = "block";
            iframe.style.visibility = "visible";
            /*we have to set an explicit visible otherwise it wont work*/
        }
    }
}

// IE bug workaround: unhide background controls that are beneath the specified div; see _showPopupPostProcess.
// Note that although this is called _showeElement, it is called when the popup is being *hidden*,
// in order to *show* the underlying controls again.
// we also reallign the floating div 
org_apache_myfaces_PopupCalendar.prototype._hidePopupPostProcess = function(overDiv)
{
	

    if (this.ie)
    {
        var iframe = document.getElementById(overDiv.id + "_IFRAME");
        if (iframe) iframe.style.display = "none";
    }
}

org_apache_myfaces_PopupCalendar.prototype.addHoliday = function(d, m, y, desc)
{
    this.holidays[this.holidaysCounter++] = new org_apache_myfaces_HolidayRec (d, m, y, desc);
}

org_apache_myfaces_PopupCalendar.prototype._swapImage = function(srcImg, destImg)
{

    if (srcImg)
        srcImg.setAttribute("src", this.initData.imgDir + destImg+ this.initData.imgDirSuffix);
}

org_apache_myfaces_PopupCalendar.prototype._keypresshandler = function()
{
    // This method is intended for use on IE browsers only; they are the only
    // browsers that define window.event and window.event.keyCode.
    // Q: Why is this done only for IE?
    if (window["event"] && window.event["keyCode"] && (window.event.keyCode == 27))
    {
        this._hideCalendar();
    }
}

org_apache_myfaces_PopupCalendar.prototype._clickhandler = function()
{
    if (!this.bClickOnCalendar)
    {
        this._hideCalendar();
    }

    this.bClickOnCalendar = false;
}

org_apache_myfaces_PopupCalendar.prototype._isDaySelectable = function()
{
  return (this.initData.selectMode == "day");
}

org_apache_myfaces_PopupCalendar.prototype._isWeekSelectable = function()
{
  return (this.initData.selectMode == "week");
}

org_apache_myfaces_PopupCalendar.prototype._isMonthSelectable = function()
{
  return (this.initData.selectMode == "month");
}

org_apache_myfaces_PopupCalendar.prototype.init = function(containerCtl)
{
    if (this.dom)
    {

        if (!this.calendarDiv)
        {
            for (var i = 0; i < this.imgSrc.length; i++)
            {
                // force preload of all images, so that when DOM nodes have their src set to
                // the name of this image, it has already been loaded from the server.
                this.img[i] = new Image;
                this.img[i].src = this.initData.imgDir + this.imgSrc[i]+ this.initData.imgDirSuffix;
            }

            this.containerCtl = containerCtl;

            this.calendarDiv = document.createElement("div");
            this.calendarDiv.id = containerCtl.id + "_calendarDiv";
            this.calendarDiv.className = this.initData.themePrefix + "-div-style";

            Event.observe(this.calendarDiv, "click", function()
            {
                this.bClickOnCalendar = true;
            }.bind(this), false);

            this.containerCtl.appendChild(this.calendarDiv);

            var mainTable = document.createElement("table");
            Element.setStyle(mainTable, "width:" + ((this.initData.showWeekNumber == 1)?250:220) + "px;");
            mainTable.className = this.initData.themePrefix + "-table-style";

            this.calendarDiv.appendChild(mainTable);

            //This is necessary for IE. If you don't create a tbody element, the table will never show up!
            var mainBody = document.createElement("tbody");
            mainTable.appendChild(mainBody);

            var mainRow = document.createElement("tr");
            mainRow.className = this.initData.themePrefix + "-title-background-style";

            mainBody.appendChild(mainRow);

            var mainCell = document.createElement("td");

            mainRow.appendChild(mainCell);

            var contentTable = document.createElement("table");
            Element.setStyle(contentTable, "width:" + ((this.initData.showWeekNumber == 1)?248:218) + "px;");

            var contentBody = document.createElement("tbody");
            contentTable.appendChild(contentBody);

            mainCell.appendChild(contentTable);

            var headerRow = document.createElement("tr");
            contentBody.appendChild(headerRow);

            var captionCell = document.createElement("td");
            captionCell.className = this.initData.themePrefix + "-title-style";
            headerRow.appendChild(captionCell);

            this.captionSpan = document.createElement("span");
            captionCell.appendChild(this.captionSpan);

            if (this._isMonthSelectable())
            {
                var acceptMonthCell = document.createElement("td");
                Element.setStyle(acceptMonthCell, "text-align:right;");
                headerRow.appendChild(acceptMonthCell);
    
                var acceptMonthLink = document.createElement("a");
                acceptMonthLink.setAttribute("href", "#");
                Event.observe(acceptMonthLink, "click", function(event)
                {
                    this.selectedDate.date = 1; // force first of the selected month
                    this._closeCalendar();
                    Event.stop(event);
                }.bindAsEventListener(this), false);
    
                acceptMonthCell.appendChild(acceptMonthLink);
                this.acceptMonthSpan = document.createElement("span");
                this.acceptMonthSpan.appendChild(document.createTextNode("Y"));
    
                acceptMonthLink.appendChild(this.acceptMonthSpan);
            }

            var closeButtonCell = document.createElement("td");
            Element.setStyle(closeButtonCell, "text-align:right;");
            headerRow.appendChild(closeButtonCell);

            var closeCalendarLink = document.createElement("a");
            closeCalendarLink.setAttribute("href", "#");
            Event.observe(closeCalendarLink, "click", function(event)
            {
                this._hideCalendar();
                Event.stop(event);
            }.bindAsEventListener(this), false);

            closeButtonCell.appendChild(closeCalendarLink);

            this.closeCalendarSpan = document.createElement("span");

            closeCalendarLink.appendChild(this.closeCalendarSpan);

            var contentRow = document.createElement("tr");
            mainBody.appendChild(contentRow);

            var contentCell = document.createElement("td");
            contentCell.className = this.initData.themePrefix + "-body-style";
            contentRow.appendChild(contentCell);

            this.contentSpan = document.createElement("span");
            contentCell.appendChild(this.contentSpan);

            if (this.initData.showToday == 1)
            {
                var todayRow = document.createElement("tr");
                todayRow.className = this.initData.themePrefix + "-today-style";
                mainBody.appendChild(todayRow);

                var todayCell = document.createElement("td");
                todayCell.className = this.initData.themePrefix + "-today-lbl-style";
                todayRow.appendChild(todayCell);

                this.todaySpan = document.createElement("span");
                todayCell.appendChild(this.todaySpan);
            }

            this.selectMonthDiv = document.createElement("div");
            this.selectMonthDiv.id = this.containerCtl.id + "_selectMonthDiv";
            this.selectMonthDiv.className = this.initData.themePrefix + "-div-style";

            this.containerCtl.appendChild(this.selectMonthDiv);

            this.selectYearDiv = document.createElement("div");
            this.selectYearDiv.id = this.containerCtl.id + "_selectYearDiv";
            this.selectYearDiv.className = this.initData.themePrefix + "-div-style";

            this.containerCtl.appendChild(this.selectYearDiv);

            // Catch global keypresses and clicks, so that entering data into any field
            // outside the calendar, or clicking anywhere outside the calendar, will
            // close it.
            //
            // This is ugly, as it's quite a load on the client to check this for every
            // keystroke/click. It would be nice to find an alternative...maybe register
            // these listeners only when a calendar is open?
            Event.observe(document, "keypress", this._keypresshandler.bind(this), false);
            Event.observe(document, "click", this._clickhandler.bind(this), false);
        }
    }


    if (!this.ns4)
    {
        /* Instead use getFullYear() */
        /*if (!this.ie)
            this.yearNow += 1900;*/

        this._hideCalendar();

        this.monthConstructed = false;
        this.yearConstructed = false;

        if (this.initData.showToday == 1)
        {
            // TODO this.dateFormatSymbols is probably never set at this point.
    		this.todayDateFormatter = new org_apache_myfaces_dateformat_SimpleDateFormatter(
    		    this.initData.todayDateFormat? this.initData.todayDateFormat:this.dateFormat,
                this.dateFormatSymbols,
                this.initData.startAt);

            this.todaySpan.appendChild(document.createTextNode(this.initData.todayString + " "))

            var todayLink = document.createElement("a");
            todayLink.className = this.initData.themePrefix + "-today-style";
            todayLink.setAttribute("title", this.initData.gotoString);
            todayLink.setAttribute("href", "#")
            todayLink.appendChild(document.createTextNode(this._todayIsDate()));
            Event.observe(todayLink, "click", function(event)
            {
                this.selectedDate.month = this.monthNow;
                this.selectedDate.year = this.yearNow;
                this._constructCalendar();
                Event.stop(event);
            }.bindAsEventListener(this), false);
            Event.observe(todayLink, "mousemove", function()
            {
                window.status = this.initData.gotoString;
            }.bind(this), false);
            Event.observe(todayLink, "mouseout", function()
            {
                window.status = "";
            }.bind(this), false);

            this.todaySpan.appendChild(todayLink);
        }

        this._appendNavToCaption("left");
        this._appendNavToCaption("right");

        this.monthSpan = document.createElement("span");
        this.monthSpan.className = this.initData.themePrefix + "-title-control-normal-style";

        Event.observe(this.monthSpan, "mouseover", function(event)
        {
            this._swapImage(this.changeMonthImg, "drop2.gif");
            this.monthSpan.className = this.initData.themePrefix + "-title-control-select-style";
            window.status = this.selectMonthMessage;
        }.bindAsEventListener(this), false);

        Event.observe(this.monthSpan, "mouseout", function(event)
        {
            this._swapImage(this.changeMonthImg, "drop1.gif");
            this.monthSpan.className = this.initData.themePrefix + "-title-control-normal-style";
            window.status = "";
        }.bindAsEventListener(this), false);

        Event.observe(this.monthSpan, "click", function(event)
        {
            this._popUpMonth();
            Event.stop(event);
        }.bind(this), false);

        this.captionSpan.appendChild(this.monthSpan);
        this._appendNbsp(this.captionSpan);

        this.yearSpan = document.createElement("span");
        this.yearSpan.className = this.initData.themePrefix + "-title-control-normal-style";

        Event.observe(this.yearSpan, "mouseover", function(event)
        {
            this._swapImage(this.changeYearImg, "drop2.gif");
            this.yearSpan.className = this.initData.themePrefix + "-title-control-select-style";
            window.status = this.selectYearMessage;
        }.bindAsEventListener(this), false);

        Event.observe(this.yearSpan, "mouseout", function(event)
        {
            this._swapImage(this.changeYearImg, "drop1.gif");
            this.yearSpan.className = this.initData.themePrefix + "-title-control-normal-style";
            window.status = "";
        }.bindAsEventListener(this), false);

        Event.observe(this.yearSpan, "click", function(event)
        {
            this._popUpYear();
            Event.stop(event);
        }.bind(this), false);

        this.captionSpan.appendChild(this.yearSpan);
        this._appendNbsp(this.captionSpan);

        this.bPageLoaded = true;

    }
}

org_apache_myfaces_PopupCalendar.prototype._appendNavToCaption = function(direction)
{
    var imgLeft = document.createElement("img");
    imgLeft.setAttribute("src", this.initData.imgDir + direction + "1.gif"+this.initData.imgDirSuffix);
    imgLeft.setAttribute("width","10px");
    imgLeft.setAttribute("height","11px");
    Element.setStyle(imgLeft, "border:0px;");

    var spanLeft = document.createElement("span");

    this._createControl(direction, spanLeft, imgLeft);

    this._appendNbsp(spanLeft);
    spanLeft.appendChild(imgLeft);
    this._appendNbsp(spanLeft);
    this.captionSpan.appendChild(spanLeft);
    this._appendNbsp(spanLeft);
}

org_apache_myfaces_PopupCalendar.prototype._createControl = function(direction, spanLeft, imgLeft)
{
    spanLeft.className = this.initData.themePrefix + "-title-control-normal-style";
    Event.observe(spanLeft, "mouseover", function(event)
    {
        this._swapImage(imgLeft, direction + "2.gif");
        spanLeft.className = this.initData.themePrefix + "-title-control-select-style";
        if (direction == "left")
        {
            window.status = this.scrollLeftMessage;
        }
        else
        {
            window.status = this.scrollRightMessage;
        }
    }.bindAsEventListener(this), false);
    Event.observe(spanLeft, "click", function()
    {
        if (direction == "left")
        {
            this._decMonth();
        }
        else
        {
            this._incMonth();
        }
    }.bind(this), false);
    Event.observe(spanLeft, "mouseout", function(event)
    {
        clearInterval(this.intervalID1);
        this._swapImage(imgLeft, direction + "1.gif");
        spanLeft.className = "" + this.initData.themePrefix + "-title-control-normal-style";
        window.status = "";
    }.bindAsEventListener(this), false);
    Event.observe(spanLeft, "mousedown", function()
    {
        clearTimeout(this.timeoutID1);
        this.timeoutID1 = setTimeout((function()
        {
            if (direction == "left")
            {
                this._startDecMonth();
            }
            else
            {
                this._startIncMonth();
            }
        }).bind(this), 500)
    }.bind(this), false);
    Event.observe(spanLeft, "mouseup", function()
    {
        clearTimeout(this.timeoutID1);
        clearInterval(this.intervalID1);
    }.bind(this), false);
}

org_apache_myfaces_PopupCalendar.prototype._appendNbsp = function(element)
{
    if (element)
        element.appendChild(document.createTextNode(String.fromCharCode(160)));
}

org_apache_myfaces_PopupCalendar.prototype._todayIsDate = function()
{
    return this.todayDateFormatter.format(this.today);
}

org_apache_myfaces_PopupCalendar.prototype._hideCalendar = function()
{
    this.calendarDiv.style.visibility = "hidden"
    this.bCalendarHidden = true;
    if (this.selectMonthDiv.style != null)
    {
        this.selectMonthDiv.style.visibility = "hidden";
    }
    if (this.selectYearDiv.style != null)
    {
        this.selectYearDiv.style.visibility = "hidden";
    }

    this._hidePopupPostProcess(this.selectMonthDiv);
    this._hidePopupPostProcess(this.selectYearDiv);
    this._hidePopupPostProcess(this.calendarDiv);
}

org_apache_myfaces_PopupCalendar.prototype._padZero = function(num)
{
    return (num < 10)? '0' + num : num;
}

org_apache_myfaces_PopupCalendar.prototype._constructDate = function(d, m, y)
{
    var date = new Date(y, m, d, this.selectedDate.hour, this.selectedDate.min, this.selectedDate.sec);
    return this.stdDateFormatter.format(date);
}

org_apache_myfaces_PopupCalendar.prototype._closeCalendar = function()
{
    this._hideCalendar();

    if (this.myFacesCtlType != "x:inputDate")
    {
        this.ctlToPlaceValue.value = this._constructDate(this.selectedDate.date, this.selectedDate.month, this.selectedDate.year)
        var onchange = this.ctlToPlaceValue.getAttribute("onchange");
        if (onchange)
        {
            this.ctlToPlaceValue.onchange();
        }
    }
    else
    {
        document.getElementById(this.myFacesInputDateClientId + ".day").value = this.selectedDate.date;
        document.getElementById(this.myFacesInputDateClientId + ".month").value = this.selectedDate.month + 1;
        document.getElementById(this.myFacesInputDateClientId + ".year").value = this.selectedDate.year;
    }
}

/*** Month Pulldown ***/

org_apache_myfaces_PopupCalendar.prototype._startDecMonth = function()
{
    this.intervalID1 = setInterval((function()
    {
        this._decMonth
    }).bind(this), 80);
}

org_apache_myfaces_PopupCalendar.prototype._startIncMonth = function()
{
    this.intervalID1 = setInterval((function()
    {
        this._incMonth
    }).bind(this), 80);
}

org_apache_myfaces_PopupCalendar.prototype._incMonth = function()
{
    this.selectedDate.month = this.selectedDate.month + 1;
    if (this.selectedDate.month > 11)
    {
        this.selectedDate.month = 0;
        this.selectedDate.year++;
    }
    this._constructCalendar();
}

org_apache_myfaces_PopupCalendar.prototype._decMonth = function()
{
    this.selectedDate.month = this.selectedDate.month - 1;
    if (this.selectedDate.month < 0)
    {
        this.selectedDate.month = 11
        this.selectedDate.year--
    }
    this._constructCalendar()
}


org_apache_myfaces_PopupCalendar.prototype._removeAllChildren = function(element)
{
    while (element && element.hasChildNodes())
        element.removeChild(element.lastChild);
}

org_apache_myfaces_PopupCalendar.prototype._constructMonth = function()
{
    this._popDownYear();
    if (!this.monthConstructed)
    {

        var selectMonthTable = document.createElement("table");
        Element.setStyle(selectMonthTable, "width:70px;border-collapse:collapse;");
        selectMonthTable.className = this.initData.themePrefix + "-dropdown-style";

        this._removeAllChildren(this.selectMonthDiv);

        this.selectMonthDiv.appendChild(selectMonthTable);

        Event.observe(selectMonthTable, "mouseover", function()
        {
            clearTimeout(this.timeoutID1);
        }.bind(this), false);
        Event.observe(selectMonthTable, "mouseout", function(event)
        {
            clearTimeout(this.timeoutID1);
            this.timeoutID1 = setTimeout((function()
            {
                this._popDownMonth()
            }).bind(this), 100);
            Event.stop(event);
        }.bindAsEventListener(this), false);

        var selectMonthTableBody = document.createElement("tbody");
        selectMonthTable.appendChild(selectMonthTableBody);

        for (i = 0; i < 12; i++)
        {
            var sName = this.initData.monthName[i];

            var sNameNode = null;

            if (i == this.selectedDate.month)
            {
                sNameNode = document.createElement("span");
                Element.setStyle(sNameNode, "font-weight:bold;");
                sNameNode.appendChild(document.createTextNode(sName));
                sNameNode.setAttribute("userData",i);
            }
            else
            {
                sNameNode = document.createTextNode(sName);
            }

            var monthRow = document.createElement("tr");
            selectMonthTableBody.appendChild(monthRow);

            var monthCell = document.createElement("td");
            monthCell.setAttribute("userData",i);
            monthRow.appendChild(monthCell);

            Event.observe(monthCell, "mouseover", function(event)
            {
                Event.element(event).className = this.initData.themePrefix + "-dropdown-select-style";
            }.bind(this), false);

            Event.observe(monthCell, "mouseout", function(event)
            {
                Event.element(event).className = this.initData.themePrefix + "-dropdown-normal-style";
            }.bind(this), false);

            Event.observe(monthCell, "click", function(event)
            {
                this.monthConstructed = false;
                this.selectedDate.month = parseInt(Event.element(event).getAttribute("userData"),10);
                this._constructCalendar();
                this._popDownMonth();
                Event.stop(event);
            }.bindAsEventListener(this), false);

            this._appendNbsp(monthCell);
            monthCell.appendChild(sNameNode);
            this._appendNbsp(monthCell);
        }

        this.monthConstructed = true;
    }
}

org_apache_myfaces_PopupCalendar.prototype._popUpMonth = function()
{
    this._constructMonth()
    this._fixPopupDomOrder(this.selectMonthDiv);
    this.selectMonthDiv.style.visibility = (this.dom || this.ie)? "visible"    : "show"
    this.selectMonthDiv.style.left = parseInt(this._formatInt(this.calendarDiv.style.left), 10) + 50 + "px";
    this.selectMonthDiv.style.top = parseInt(this._formatInt(this.calendarDiv.style.top), 10) + 26 + "px";

    this._showPopupPostProcess(this.selectMonthDiv);
}

org_apache_myfaces_PopupCalendar.prototype._popDownMonth = function()
{
    this.selectMonthDiv.style.visibility = "hidden";
    this._hidePopupPostProcess(this.selectMonthDiv);
}

/*** Year Pulldown ***/

org_apache_myfaces_PopupCalendar.prototype._incYear = function()
{
    for (i = 0; i < 7; i++)
    {
        newYear = (i + this.nStartingYear) + 1;

        this._createAndAddYear(newYear, i);
    }
    this.nStartingYear++;
    this.bClickOnCalendar = true;
}

org_apache_myfaces_PopupCalendar.prototype._createAndAddYear = function(newYear, i)
{
    var parentNode = document.getElementById(this.containerCtl.getAttribute("id")+"y" + i);

    this._removeAllChildren(parentNode);

    if (newYear == this.selectedDate.year)
    {
        this._appendNbsp(parentNode);
        var newYearSpan = document.createElement("span");
        newYearSpan.setAttribute("userData",newYear);
        newYearSpan.appendChild(document.createTextNode(newYear));
        parentNode.appendChild(newYearSpan);
        this._appendNbsp(parentNode);
    }
    else
    {
        this._appendNbsp(parentNode);
        parentNode.appendChild(document.createTextNode(newYear));
        this._appendNbsp(parentNode);
    }

    parentNode.setAttribute("userData",newYear);
}


org_apache_myfaces_PopupCalendar.prototype._decYear = function()
{
    for (i = 0; i < 7; i++)
    {
        newYear = (i + this.nStartingYear) - 1;

        this._createAndAddYear(newYear, i);
    }
    this.nStartingYear--;
    this.bClickOnCalendar = true;
}

org_apache_myfaces_PopupCalendar.prototype._constructYear = function()
{
    this._popDownMonth();
    var sHTML = "";
    if (!this.yearConstructed)
    {

        var selectYearTable = document.createElement("table");
        Element.setStyle(selectYearTable, "width:44px;border-collapse:collapse;");
        selectYearTable.className = this.initData.themePrefix + "-dropdown-style";

        this._removeAllChildren(this.selectYearDiv);

        this.selectYearDiv.appendChild(selectYearTable);

        Event.observe(selectYearTable, "mouseover", function()
        {
            clearTimeout(this.timeoutID2);
        }.bind(this), false);
        Event.observe(selectYearTable, "mouseout", function(event)
        {
            clearTimeout(this.timeoutID2);
            this.timeoutID2 = setTimeout((function()
            {
                this._popDownYear()
            }).bind(this), 100);
            Event.stop(event);
        }.bindAsEventListener(this), false);


        var selectYearTableBody = document.createElement("tbody");
        selectYearTable.appendChild(selectYearTableBody);

        var selectYearRowMinus = document.createElement("tr");
        selectYearTableBody.appendChild(selectYearRowMinus);

        var selectYearCellMinus = document.createElement("td");
        selectYearCellMinus.setAttribute("align", "center");

        selectYearCellMinus.appendChild(document.createTextNode("-"));

        selectYearRowMinus.appendChild(selectYearCellMinus);

        Event.observe(selectYearCellMinus, "mouseover", function(event)
        {
            Event.element(event).className = this.initData.themePrefix + "-dropdown-select-style";
        }.bindAsEventListener(this), false);

        Event.observe(selectYearCellMinus, "mouseout", function(event)
        {
            clearInterval(this.intervalID1);
            Event.element(event).className = this.initData.themePrefix + "-dropdown-normal-style";
        }.bindAsEventListener(this), false);

        Event.observe(selectYearCellMinus, "mousedown", function(event)
        {
            clearInterval(this.intervalID1);
            this.intervalID1 = setInterval((function()
            {
                this._decYear();
            }).bind(this), 30);
            Event.stop(event);
        }.bindAsEventListener(this), false);

        Event.observe(selectYearCellMinus, "mouseup", function(event)
        {
            clearInterval(this.intervalID1);
            Event.stop(event);
        }.bindAsEventListener(this), false);


        //sHTML = "<tr><td align='center' onmouseover='this.className=\""+this.initData.themePrefix+"-dropdown-select-style\"' onmouseout='clearInterval(this.intervalID1); this.className=\""+this.initData.themePrefix+"-dropdown-normal-style\"' onmousedown='clearInterval(this.intervalID1);this.intervalID1=setInterval(\"_decYear()\",30)' onmouseup='clearInterval(this.intervalID1)'>-</td></tr>";

        this.nStartingYear = this.selectedDate.year - 3;
        var j = 0;
        for (i = this.selectedDate.year - 3; i <= (this.selectedDate.year + 3); i++)
        {
            var sName = i;

            var sNameNode = null;

            if (i == this.selectedDate.year)
            {
                sNameNode = document.createElement("span");
                Element.setStyle(sNameNode, "font-weight:bold;");
                sNameNode.appendChild(document.createTextNode(sName));
                sNameNode.setAttribute("userData", sName);
            }
            else
            {
                sNameNode = document.createTextNode(sName);
            }

            var yearRow = document.createElement("tr");
            selectYearTableBody.appendChild(yearRow);

            var yearCell = document.createElement("td");
            yearCell.setAttribute("userData",sName);
            yearCell.setAttribute("id",this.containerCtl.getAttribute("id")+"y" + j);
            yearRow.appendChild(yearCell);

            Event.observe(yearCell, "mouseover", function(event)
            {
                Event.element(event).className = this.initData.themePrefix + "-dropdown-select-style";
            }.bind(this), false);

            Event.observe(yearCell, "mouseout", function(event)
            {
                Event.element(event).className = this.initData.themePrefix + "-dropdown-normal-style";
            }.bind(this), false);

            Event.observe(yearCell, "click", function(event)
            {
                var elem = Event.element(event);
                var sYear = null;
                this.selectedDate.year = parseInt(this._formatInt(elem.getAttribute("userData"),10));
                this.yearConstructed = false;
                this._popDownYear();
                this._constructCalendar();
                Event.stop(event);
            }.bindAsEventListener(this), false);

            this._appendNbsp(yearCell);
            yearCell.appendChild(sNameNode);
            this._appendNbsp(yearCell);
            j++;
        }

        var selectYearRowPlus = document.createElement("tr");
        selectYearTableBody.appendChild(selectYearRowPlus);

        var selectYearCellPlus = document.createElement("td");
        selectYearCellPlus.setAttribute("align", "center");

        selectYearCellPlus.appendChild(document.createTextNode("+"));

        selectYearRowPlus.appendChild(selectYearCellPlus);

        Event.observe(selectYearCellPlus, "mouseover", function(event)
        {
            Event.element(event).className = this.initData.themePrefix + "-dropdown-select-style";
        }.bindAsEventListener(this), false);

        Event.observe(selectYearCellPlus, "mouseout", function(event)
        {
            clearInterval(this.intervalID2);
            Event.element(event).className = this.initData.themePrefix + "-dropdown-normal-style";
        }.bindAsEventListener(this), false);

        Event.observe(selectYearCellPlus, "mousedown", function(event)
        {
            clearInterval(this.intervalID2);
            this.intervalID2 = setInterval((function()
            {
                this._incYear();
            }).bind(this), 30);
        }.bindAsEventListener(this), false);

        Event.observe(selectYearCellPlus, "mouseup", function(event)
        {
            clearInterval(this.intervalID2);
        }.bindAsEventListener(this), false);

        this.yearConstructed = true;
    }
}

org_apache_myfaces_PopupCalendar.prototype._popDownYear = function()
{
    clearInterval(this.intervalID1);
    clearTimeout(this.timeoutID1);
    clearInterval(this.intervalID2);
    clearTimeout(this.timeoutID2);
    this.selectYearDiv.style.visibility = "hidden";
    this._hidePopupPostProcess(this.selectYearDiv);
}

org_apache_myfaces_PopupCalendar.prototype._popUpYear = function()
{
    var leftOffset;
	this._fixPopupDomOrder(this.selectYearDiv);
    this._constructYear();
    this.selectYearDiv.style.visibility = (this.dom || this.ie) ? "visible" : "show";
    leftOffset = parseInt(this._formatInt(this.calendarDiv.style.left), 10) + this.yearSpan.offsetLeft;
    if (this.ie)
        leftOffset += 6;
    this.selectYearDiv.style.left = leftOffset + "px";
    this.selectYearDiv.style.top = parseInt(this._formatInt(this.calendarDiv.style.top), 10) + 26 + "px";

    this._showPopupPostProcess(this.selectYearDiv);
}

org_apache_myfaces_PopupCalendar.prototype._appendCell = function(parentElement, value)
{
    var cell = document.createElement("td");
    Element.setStyle(cell, "text-align:right;");

    if (value && value != "")
    {
        cell.appendChild(document.createTextNode(value));
    }
    else
    {
        this._appendNbsp(cell);
    }

    parentElement.appendChild(cell);
}

org_apache_myfaces_PopupCalendar.prototype._getDateStyle = function(datePointer)
{
    var sStyle = this.initData.themePrefix + "-normal-day-style";
    //regular day

    if ((datePointer == this.dateNow) &&
        (this.selectedDate.month == this.monthNow) && (this.selectedDate.year == this.yearNow)) //today
    {
        sStyle = this.initData.themePrefix + "-current-day-style";
    }
    else if (dayPointer % 7 == (this.initData.startAt * -1) + 1) //end-of-the-week day
    {
        sStyle = this.initData.themePrefix + "-end-of-weekday-style";
    }

    //selected day
    if ((datePointer == this.saveSelectedDate.date) &&
        (this.selectedDate.month == this.saveSelectedDate.month) &&
        (this.selectedDate.year == this.saveSelectedDate.year))
    {
        sStyle += " " + this.initData.themePrefix + "-selected-day-style";
    }

    for (k = 0; k < this.holidaysCounter; k++)
    {
        if ((parseInt(this._formatInt(this.holidays[k].d), 10) == datePointer) && (parseInt(this._formatInt(this.holidays[k].m), 10) == (this.selectedDate.month + 1)))
        {
            if ((parseInt(this._formatInt(this.holidays[k].y), 10) == 0) || ((parseInt(this._formatInt(this.holidays[k].y), 10) == this.selectedDate.year) && (parseInt(this._formatInt(this.holidays[k].y), 10) != 0)))
            {
                sStyle += " " + this.initData.themePrefix + "-holiday-style";
            }
        }
    }

    return sStyle;
}

org_apache_myfaces_PopupCalendar.prototype._getHolidayHint = function(datePointer)
{
    var sHint = "";
    for (k = 0; k < this.holidaysCounter; k++)
    {
        if ((parseInt(this._formatInt(this.holidays[k].d), 10) == datePointer) && (parseInt(this._formatInt(this.holidays[k].m), 10) == (this.selectedDate.month + 1)))
        {
            if ((parseInt(this._formatInt(this.holidays[k].y), 10) == 0) || ((parseInt(this._formatInt(this.holidays[k].y), 10) == this.selectedDate.year) && (parseInt(this._formatInt(this.holidays[k].y), 10) != 0)))
            {
                sHint += sHint == ""?this.holidays[k].desc:"\n" + this.holidays[k].desc;
            }
        }
    }

    return sHint;
}


org_apache_myfaces_PopupCalendar.prototype._addWeekCell = function(currentRow, startDate, weekSelectable,
    sNormalStyle, sSelectStyle)
{
    var cell = document.createElement("td");
    Element.setStyle(cell, "text-align:right;");

    var weekDate = this.stdDateFormatter.getWeekDate(startDate);
    if (weekSelectable)
    {
        var link = document.createElement("a");
        link.className = sNormalStyle;
        link.sNormalStyle = sNormalStyle;
        link.sSelectStyle = sSelectStyle;
        link.setAttribute("href", "#");
        link.appendChild(document.createTextNode(weekDate.week + " "));

        link.dateObj = this.stdDateFormatter.getDateForWeekDate(weekDate);

        cell.appendChild(link);

        Event.observe(link, "mousemove", function(event)
        {
            window.status = this.initData.selectDateMessage.replace(
                "[date]",
                this._constructDate(link.dateObj));
        }.bindAsEventListener(this), false);

        Event.observe(link, "mouseout", function(event)
        {
            var elem = Event.element(event);
            elem.className = elem.sNormalStyle;
            window.status = "";
        }.bindAsEventListener(this), false);

        Event.observe(link, "click", function(event)
        {
            var elem = Event.element(event);
            this.selectedDate.year = elem.dateObj.getFullYear();
            this.selectedDate.month = elem.dateObj.getMonth();
            this.selectedDate.date = elem.dateObj.getDate();
            this._closeCalendar();
            Event.stop(event);
        }.bindAsEventListener(this), false);

        Event.observe(link, "mouseover", function(event)
        {
            var elem = Event.element(event);
            elem.className = elem.sSelectStyle;
        }.bindAsEventListener(this), false);
    }
    else
    {
        var span = document.createElement("span");
        span.className=sNormalStyle;
        span.appendChild(document.createTextNode(weekDate.week + " "));
        cell.appendChild(span);
    }

    currentRow.appendChild(cell);
}

org_apache_myfaces_PopupCalendar.prototype._constructCalendar = function()
{
    var aNumDays = Array(31, 0, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);

    var dateMessage;
    var startDate = new Date(this.selectedDate.year, this.selectedDate.month, 1);

    if (this.selectedDate.month == 1)
    {
        // to compute the number of days in february, select first day of march,
        // subtract 24 hours then query the day-of-the-month.
        var msecs = new Date (this.selectedDate.year, this.selectedDate.month + 1, 1).getTime();
        var endDate = new Date (msecs - this._MSECS_PER_DAY);
        numDaysInMonth = endDate.getDate();
    }
    else
    {
        numDaysInMonth = aNumDays[this.selectedDate.month];
    }

    datePointer = 0;
    dayPointer = startDate.getDay() - this.initData.startAt;

    if (dayPointer < 0)
        dayPointer = 6;

    this._removeAllChildren(this.contentSpan);

    var contentTable = document.createElement("table");
    Element.setStyle(contentTable, "border:0px;");
    contentTable.className = this.initData.themePrefix + "-body-style";

    this.contentSpan.appendChild(contentTable);

    var contentBody = document.createElement("tbody");
    contentTable.appendChild(contentBody);

    var contentRow = document.createElement("tr");
    contentBody.appendChild(contentRow);

    if (this.initData.showWeekNumber == 1)
    {
        var showWeekNumberCell = document.createElement("td");
        Element.setStyle(showWeekNumberCell, "width:27px;font-weight:bold;");

        contentRow.appendChild(showWeekNumberCell);

        showWeekNumberCell.appendChild(document.createTextNode(this.initData.weekString));

        var dividerCell = document.createElement("td");
        Element.setStyle(dividerCell, "width:1px;");
        if(this.ie) //fix for https://issues.apache.org/jira/browse/TOMAHAWK-1184
        	dividerCell.setAttribute("rowSpan", "7");
        else	
        	dividerCell.setAttribute("rowspan", "7");
        dividerCell.className = this.initData.themePrefix + "-weeknumber-div-style";

        contentRow.appendChild(dividerCell);

        var dividerImg = document.createElement("img");
        dividerImg.setAttribute("src", this.initData.imgDir + "divider.gif"+ this.initData.imgDirSuffix);
        Element.setStyle(dividerImg, "width:1px;");
        dividerCell.appendChild(dividerImg);
    }

    for (i = 0; i < 7; i++)
    {
        var dayNameCell = document.createElement("td");
        Element.setStyle(dayNameCell, "width:27px;text-align:right;font-weight:bold;");
        contentRow.appendChild(dayNameCell);

        dayNameCell.appendChild(document.createTextNode(this.initData.dayName[i]));
    }

    var currentRow = document.createElement("tr");
    contentBody.appendChild(currentRow);

    var weekSelectable = this._isWeekSelectable();
    var sWeekNormalStyle = this.initData.themePrefix + "-normal-week-style";
    var sWeekSelectStyle = sWeekNormalStyle + " " + this.initData.themePrefix + "-would-be-selected-week-style";

    if (this.initData.showWeekNumber == 1)
    {
        this._addWeekCell(currentRow, startDate, weekSelectable, sWeekNormalStyle, sWeekSelectStyle);
    }

    for (var i = 1; i <= dayPointer; i++)
    {
        this._appendCell(currentRow);
    }

    for (datePointer = 1; datePointer <= numDaysInMonth; datePointer++)
    {
        dayPointer++;
        var dateCell = document.createElement("td");
        Element.setStyle(dateCell, "text-align:right;");

        currentRow.appendChild(dateCell);

        var sStyle = this._getDateStyle(datePointer);
        var sHint = this._getHolidayHint(datePointer);

        var sSelectStyle = sStyle + " " + this.initData.themePrefix + "-would-be-selected-day-style";
        var sNormalStyle = sStyle;

        if (this._isDaySelectable())
        {

            var dateLink = document.createElement("a");
            dateLink.className = sStyle;
            dateLink.setAttribute("href", "#");
            dateLink.setAttribute("title", sHint);

            dateLink.sNormalStyle = sNormalStyle;
            dateLink.sSelectStyle = sSelectStyle;
            dateLink.datePointer = datePointer;

            dateCell.appendChild(dateLink);

            Event.observe(dateLink, "mousemove", function(event)
            {
                window.status = this.initData.selectDateMessage.replace("[date]", this._constructDate(datePointer, this.selectedDate.month, this.selectedDate.year));
            }.bindAsEventListener(this), false);
            Event.observe(dateLink, "mouseout", function(event)
            {
                var elem = Event.element(event);
                elem.className = elem.sNormalStyle;
                window.status = "";
            }.bindAsEventListener(this), false);
            Event.observe(dateLink, "click", function(event)
            {
                var elem = Event.element(event);
                this.selectedDate.date = elem.datePointer;
                this._closeCalendar();
                Event.stop(event);
            }.bindAsEventListener(this), false);
            Event.observe(dateLink, "mouseover", function(event)
            {
                var elem = Event.element(event);
                elem.className = elem.sSelectStyle;
            }.bindAsEventListener(this), false);

            this._appendNbsp(dateLink);
            dateLink.appendChild(document.createTextNode(datePointer));
            this._appendNbsp(dateLink);
        }
        else
        {
            var dateSpan = document.createElement("span");
            dateCell.appendChild(dateSpan);

            dateSpan.appendChild(document.createTextNode(datePointer));
            dateSpan.className = sStyle;
            dateSpan.setAttribute("title", sHint);
        }

        if ((dayPointer + this.initData.startAt) % 7 == this.initData.startAt)
        {
            currentRow = document.createElement("tr");
            contentBody.appendChild(currentRow);

            if ((this.initData.showWeekNumber == 1) && (datePointer < numDaysInMonth))
            {
                var startDate = new Date(this.selectedDate.year, this.selectedDate.month, datePointer + 1);
                this._addWeekCell(currentRow, startDate, weekSelectable, sWeekNormalStyle, sWeekSelectStyle);
            }
        }
    }

    this._removeAllChildren(this.monthSpan);

    this._appendNbsp(this.monthSpan);
    this.monthSpan.appendChild(document.createTextNode(this.initData.monthName[this.selectedDate.month]));
    this._appendNbsp(this.monthSpan);

    this.changeMonthImg = document.createElement("img");
    this.changeMonthImg.setAttribute("src", this.initData.imgDir + "drop1.gif" + this.initData.imgDirSuffix);
    this.changeMonthImg.setAttribute("width","12px");
    this.changeMonthImg.setAttribute("height","10px");
    Element.setStyle(this.changeMonthImg, "border:0px;");

    this.monthSpan.appendChild(this.changeMonthImg);

    // Year dropdown list (YYYY plus dropdown icon)
    this._removeAllChildren(this.yearSpan);
    this._appendNbsp(this.yearSpan);
    this.yearSpan.appendChild(document.createTextNode(this.selectedDate.year));
    this._appendNbsp(this.yearSpan);
    this.changeYearImg = document.createElement("img");
    this.changeYearImg.setAttribute("src", this.initData.imgDir + "drop1.gif" + this.initData.imgDirSuffix);
    this.changeYearImg.setAttribute("width","12px");
    this.changeYearImg.setAttribute("height","10px");
    Element.setStyle(this.changeYearImg, "border:0px;");
    this.yearSpan.appendChild(this.changeYearImg);

    // Accept Month icon
    if (this.acceptMonthSpan)
    {
      this._removeAllChildren(this.acceptMonthSpan);
      var acceptMonthImg = document.createElement("img");
      acceptMonthImg.setAttribute("src", this.initData.imgDir + "accept.gif" + this.initData.imgDirSuffix);
      acceptMonthImg.setAttribute("width","15px");
      acceptMonthImg.setAttribute("height","13px");
      Element.setStyle(acceptMonthImg, "border:0px;");
      acceptMonthImg.setAttribute("alt", "Accept the current month selection");
      this.acceptMonthSpan.appendChild(acceptMonthImg);
    }

    // Close Popup icon
    this._removeAllChildren(this.closeCalendarSpan);
    var closeButtonImg = document.createElement("img");
    closeButtonImg.setAttribute("src", this.initData.imgDir + "close.gif" + this.initData.imgDirSuffix);
    closeButtonImg.setAttribute("width","15px");
    closeButtonImg.setAttribute("height","13px");
    Element.setStyle(closeButtonImg, "border:0px;");
    closeButtonImg.setAttribute("alt", "Close the calendar");
    this.closeCalendarSpan.appendChild(closeButtonImg);

    this._recalculateElement(this.calendarDiv);
}

org_apache_myfaces_PopupCalendar.prototype._popUpCalendar = function(ctl, ctl2, format)
{
    if (!this.bPageLoaded)
    {
        // The user has clicked a button before the initialisation of this
        // class has completed. Just ignore the click.. 
        return;
    }
    
    if (this.calendarDiv.style.visibility == "hidden")
    {
        this.ctlToPlaceValue = ctl2;
        this.dateFormat = format;

        this.stdDateFormatter = new org_apache_myfaces_dateformat_SimpleDateFormatter(
            this.dateFormat, this.dateFormatSymbols, this.initData.startAt);
        var dateSelected = this.stdDateFormatter.parse(ctl2.value);

        if (dateSelected)
        {
            this.selectedDate.sec = dateSelected.getSeconds();
            this.selectedDate.min = dateSelected.getMinutes();
            this.selectedDate.hour = dateSelected.getHours();
            this.selectedDate.date = dateSelected.getDate();
            this.selectedDate.month = dateSelected.getMonth();

            var yearStr = dateSelected.getYear() + "";

            if (yearStr.length < 4)
            {
                yearStr = (parseInt(yearStr, 10) + 1900) + "";
            }

            this.selectedDate.year = parseInt(yearStr, 10);
        }
        else
        {
            this.selectedDate.date = this.dateNow;
            this.selectedDate.month = this.monthNow;
            this.selectedDate.year = this.yearNow;
        }

        this._popUpCalendar_Show(ctl);
    }
    else
    {
        this._hideCalendar();
        if (this.ctlNow != ctl)
        {
            this._popUpCalendar(ctl, ctl2, format);
        }
    }
    this.ctlNow = ctl;
}

org_apache_myfaces_PopupCalendar.prototype._popUpCalendarForInputDate = function(clientId, format)
{
    if (this.bPageLoaded)
    {
        this.myFacesCtlType = "x:inputDate";
        this.myFacesInputDateClientId = clientId;
        this.dateFormat = format;
        
        //Init stdDateFormatter
        this.stdDateFormatter = new org_apache_myfaces_dateformat_SimpleDateFormatter(
            this.dateFormat, this.dateFormatSymbols, this.initData.startAt);

        this.selectedDate.date = document.getElementById(clientId + ".day").value != "" ? parseInt(this._formatInt(document.getElementById(clientId + ".day").value), 10) : this.dateNow;
        this.selectedDate.month = document.getElementById(clientId + ".month").value != "-1" ? parseInt(this._formatInt(document.getElementById(clientId + ".month").value), 10) - 1 : this.monthNow;
        this.selectedDate.year = document.getElementById(clientId + ".year").value != "" ? parseInt(this._formatInt(document.getElementById(clientId + ".year").value), 10) : this.yearNow;
        this.ctlNow = document.getElementById(clientId + ".day");
        this._popUpCalendar_Show(document.getElementById(clientId + ".day"));
    }
}

org_apache_myfaces_PopupCalendar.prototype._getStyle = function(ctl, property)
{
	var value = null;
	
	// local style
	if (ctl.style[property]) {
       value = ctl.style[property];
    }
    // IE syntax
    else if (ctl.currentStyle && ctl.currentStyle[property]) {
       value = ctl.currentStyle[property];
    }
    // DOM syntax
    else if ( document.defaultView && document.defaultView.getComputedStyle ) {
       var computed = document.defaultView.getComputedStyle(ctl, '');
       
       if (computed && computed.getPropertyValue(property)) {
          value = computed.getPropertyValue(property);
       }
    }
	return value;
}

org_apache_myfaces_PopupCalendar.prototype._popUpCalendar_Show = function(ctl)
{
    this.saveSelectedDate.date = this.selectedDate.date;
    this.saveSelectedDate.month = this.selectedDate.month;
    this.saveSelectedDate.year = this.selectedDate.year;

    var leftpos = 0;
    var toppos = 0;

    var aTag = ctl;
    var aCSSPosition = null;
    var aTagPositioningisAbsolute = false;
    // Added try-catch to the next loop (MYFACES-870)
    try
    {
     
        do {
	    	 aTag = aTag.offsetParent;
	         leftpos += aTag.offsetLeft;
	         toppos += aTag.offsetTop;
        }
        while ((null != aTag.offsetParent ) && ('undefined' !=  aTag.offsetParent ) );
    }
    catch (ex)
    {
        // ignore
    }

    var leftScrollOffset = 0;
    var topScrollOffset = 0;

    aTag = ctl;
    // Added try-catch (MYFACES-870)
    try
    {
        do {
        	if('undefined' != typeof(aTag.scrollLeft) && null != aTag.scrollLeft)
            	leftScrollOffset += parseInt(aTag.scrollLeft);
        	if('undefined' != typeof(aTag.scrollTop) && null != aTag.scrollTop)
 	           topScrollOffset += parseInt(aTag.scrollTop);
            
            aTag = aTag.parentNode;
        }
        while((aTag.tagName.toUpperCase() != "BODY") && (aTag.tagName.toUpperCase() != "HTML"));
        //while ('undefined' != typeof(aTag) && null != aTag);
    }
    catch (ex)
    {
        // ignore
    }
    var bodyRect = this._getVisibleBodyRectangle();
    var cal = this.calendarDiv;
    
    var top = 0;
    var left = 0
 
    top = ctl.offsetTop + toppos - topScrollOffset + ctl.offsetHeight + 2;
    left = ctl.offsetLeft + leftpos - leftScrollOffset;

    if(this.initData.popupLeft)
    {
        left-=cal.offsetWidth;
    }

    if (left + cal.offsetWidth > bodyRect.right)
    {
        left = bodyRect.right - cal.offsetWidth;
    }
    if (top + cal.offsetHeight > bodyRect.bottom)
    {
        top = bodyRect.bottom - cal.offsetHeight;
    }
    if (left < bodyRect.left)
    {
        left = bodyRect.left;
    }
    if (top < bodyRect.top)
    {
        top = bodyRect.top;
    }
    
	/*we have to remap the popup so that we can handle the positioning in a neutral way*/
    if(this.calendarDiv.parentNode != document.body) {
		this.calendarDiv.parentNode.removeChild(this.calendarDiv);
		document.body.appendChild(this.calendarDiv);
	}

    this.calendarDiv.style.left = this.initData.fixedX == -1 ? left + "px": this.initData.fixedX;
    this.calendarDiv.style.top = this.initData.fixedY == -1 ? top + "px": this.initData.fixedY;
    this._constructCalendar(1, this.selectedDate.month, this.selectedDate.year);

    this.calendarDiv.style.visibility = (this.dom || this.ie)? "visible" : "show";
    this.bCalendarHidden = false;

    setTimeout((function()
    {
        this._showPopupPostProcess(this.calendarDiv);
    }).bind(this), 200);

    this._showPopupPostProcess(this.calendarDiv);

    this.bClickOnCalendar = true;
}

org_apache_myfaces_PopupCalendar.prototype._getVisibleBodyRectangle = function()
{
    var visibleRect = new org_apache_myfaces_Rectangle();

    if (window.pageYOffset != undefined)
    {
        //Most non IE
        visibleRect.top = window.pageYOffset;
        visibleRect.left = window.pageXOffset;
    }
    else if (document.body && document.body.scrollTop)
    {
        //IE 6 strict mode
        visibleRect.top = document.body.scrollTop;
        visibleRect.left = document.body.scrollLeft;
    }
    else if (document.documentElement && document.documentElement.scrollTop)
    {
        //Older IE
        visibleRect.top = document.documentElement.scrollTop;
        visibleRect.left = document.documentElement.scrollLeft;
    }

    if (window.innerWidth != undefined)
    {
        //Most non-IE
        visibleRect.right = visibleRect.left + window.innerWidth;
        visibleRect.bottom = visibleRect.top + window.innerHeight;
    }
    else if (document.documentElement && document.documentElement.clientHeight)
    {
        //IE 6 strict mode
        visibleRect.right = visibleRect.left + document.documentElement.clientWidth;
        visibleRect.bottom = visibleRect.top + document.documentElement.clientHeight;
    }
    else if (document.body && document.body.clientHeight)
    {
        //IE 4 compatible
        visibleRect.right = visibleRect.left + document.body.clientWidth;
        visibleRect.bottom = visibleRect.top + document.body.clientHeight;
    }
    return visibleRect;
}

org_apache_myfaces_PopupCalendar.prototype._formatInt = function(str)
{

    if (typeof str == 'string')
    {

        //truncate 0 for number less than 10
        if (str.charAt && str.charAt(0) == "0")
        { // <----- Change, added str.charAt for method availability detection (MYFACES)
            return str.charAt(1);
        }

    }
    return str;

}

function org_apache_myfaces_Rectangle()
{
    this.top = 0;
    this.left = 0;
    this.bottom = 0;
    this.right = 0;
}

