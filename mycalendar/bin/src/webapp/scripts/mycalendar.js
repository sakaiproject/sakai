(function($) {

    var currentSiteId;
    var timeZone;
    var eventsList = [];

    $(document).ready(function () {

        moment.locale(sakai.locale.userLocale);
        
        // data
        currentSiteId = $(".mycalendar-data").attr("data-siteid");
        timeZone = $(".mycalendar-data").attr("data-tz");
        
        //get all events for the site for this month
        var todaysDate = moment().format('YYYY-MM-DD');
        eventList = getEvents(todaysDate);

        var init = function () {
        
                $(".mycalendar").datepicker({
                    beforeShowDay: function(date) {
                        var numEvents = countEvents(date);
                        if (numEvents > 0) {
                            return [true, "mycalendar-eventful-day", numEvents + (numEvents > 1 ? " events" : " event")];
                        }
                        else {
                            return [true, '', ''];
                        } 
                    },
                    nextText: '',
                    prevText: '',
                    onSelect: function(date) {
                        // format the date
                        var formattedDate = getISO8601Date(date);
                        
                        // display events
                        displayEvents(formattedDate, moment().format('YYYY-MM-DD') === formattedDate);
                    },
                    onChangeMonthYear: function(year, month) {
                        // The -1 is because javascript dates are numbered from 0
                        var monthFirstDate = new Date(year, month-1);
                        $('.mycalendar').datepicker("setDate", monthFirstDate);
                        var monthFirst = year + "-" + (month > 9 ? month : "0" + month) + "-01";
                        eventList = getEvents(monthFirst);
                        displayEvents(monthFirst, moment().format('YYYY-MM-DD') === monthFirst);
                    },
                    // config
                    showOtherMonths: true,
                    // Get localized names for things
                    monthNames: moment.months(),
                    monthNamesShort: moment.monthsShort(),
                    dayNames: moment.weekdays(),
                    dayNamesShort: moment.weekdaysShort(),
                    dayNamesMin: moment.weekdaysMin()
                });
                
                //show heading and events for today
                displayEvents(moment().format('YYYY-MM-DD'), true);
            };

        // i18n
        jQuery.i18n.properties({
            name: 'Messages', 
            path: '/mycalendar/bundle/',
            namespace: 'mycalendar',
            mode: 'both',
            async: true,
            cache: true,
            callback: init
        });
    });

    //change the day when arrow buttons are selected
    // DRY
    function doCalendarArrow(sign) {
        var newDate = $('.mycalendar').datepicker('getDate');
        // Daylight Savings Time proof way to add or subtract a day
        newDate.setDate(newDate.getDate() + sign);
        $('.mycalendar').datepicker("setDate", newDate);
        // If the month changed on this arrow, the date will be set to the first of the month
        // make sure we have the correct day by setting it again, which will not
        // re-invoke onChangeMonthYear
        $('.mycalendar').datepicker("setDate", newDate);
        var formattedDate = moment(newDate).format('YYYY-MM-DD');
        displayEvents(formattedDate, moment().format('YYYY-MM-DD') === formattedDate);
    }

    $(document).on("click", '.left-arrow', function () {
        doCalendarArrow(-1);
    });

    $(document).on("click", '.right-arrow', function () {
        doCalendarArrow(+1);
    });

    /**
     * Get the events for the selected day, parse and display below the calendar
     * @param formattedDate YYYY-MM-DD
     * @param today true or false
     */
    function displayEvents(formattedDate, today) {
        // set heading
        var headingTmpl = $.templates("#mycalendar-heading-template");
        
        if (today) {
            var headingText = {"heading": jQuery.i18n.prop('event_heading_today')};
        } else {
            var headingText = {"heading": jQuery.i18n.prop('event_heading', moment(formattedDate).format("LL"))};
        }
        
        var headingHtml = headingTmpl.render(headingText);
        $(".mycalendar-event-heading").html(headingHtml);
        
        // show the events
        var selectedDayEvents = getEvents(formattedDate, true);
        if (selectedDayEvents.length > 0) {
            $(".mycalendar-events").text("");
            var eventsDisplay = getEventsDisplay(selectedDayEvents);
            
            var template = $.templates("#mycalendar-event-template");
            var rendered = template.render(eventsDisplay);
            $(".mycalendar-events").html(rendered);
            
        } else {
             $(".mycalendar-events").html("<tr><td>"+jQuery.i18n.prop('no_events')+"</td></tr>");
        }
        
        setMainFrameHeightNow(window.name, -1);
    };


    /**
     * Parse the event list and count the number of events that match the date
     * Note: the way this is written it will work even if the timezone on the Sakai
     * end is not the same as the browser's.
     * @param date from the datepicker - it's a javascript date object
     */
    function countEvents(date) {
        var numEvents = 0;
        for (i = 0; i < eventList.length; i++) {
            var ar = moment(eventList[i].firstTime.time).tz(timeZone).toArray();
            if (ar[0] == date.getFullYear() && ar[1] == date.getMonth() && ar[2] == date.getDate()) {
                numEvents += 1;
            }
        }
        return numEvents;
    }

    /**
     * Format a date into YYYY-MM-DD format.
     * @param date from the datepicker, is in the format mm/dd/yyyy so we need to pick out the parts
     */
    function getISO8601Date(date) {
        return date.substring(6,10) + "-" + date.substring(0,2) + "-" + date.substring(3,5); //get date in YYYY-MM-DD
    }


    /**
     * Get events for a given date in ISO8601 format 
     * @param date YYYY-MM-DD format
     * @param singleDay boolean to specify whether to fetch events for the whole month or just a single day
     */
    function getEvents(date, singleDay) {
        
        // test for myworkspace (site starting with ~) to handle different call
        var url;
        if (currentSiteId.substr(0,1) == "~") {
            url = "/direct/calendar/my.json"
        } else {
            url = "/direct/calendar/site/" + currentSiteId + ".json";
        }
        if(singleDay) {
            url += "?firstDate=" + date + "&lastDate=" + date + "&detailed=true";
        }
        else {
            var startDate = date.substring(0,4) + "-" + date.substring(5,7) + "-01";
            var endDate = date.substring(0,4) + "-" + date.substring(5,7) + "-31";
            url += "?firstDate=" + startDate + "&lastDate=" + endDate;
        }
        
        //add randomness to refresh the request
        url += "&z=" + moment().unix();
        
        var events = [];
        jQuery.ajax({
            url: url,
            success: function (result) {
                events = result.calendar_collection;
            },
            async: false
        });
        return events;
    }

    /**
     * Helper to turn a list of events into the display array ready for rendering by the template
     * @param events array of events
     */
    function getEventsDisplay(events) {
        
        var eventsDisplay = [];
        var regexps = [
          /* Samigo */
          /http(s)?:\/\/[A-Za-z0-9:_.-]+(\/samigo-app\/servlet\/Login\?id=[A-Za-z0-9-]*)/ ,
          /*
             Example URL for Adobe Connect via eSyncTraining
             http://sakai.noodle-partners.com:8080/egcint/redirect.jsf\?url=http://sakai.noodle-partners.com:8080/access/basiclti/site/sakai-demo/null/?ltiId=15169%26ltiAction=join%2673ad790e-b17e-46e2-89c3-412cb0167823
           */
          /http(s)?:\/\/[A-Za-z0-9:_.-]+\/egcint\/redirect.jsf\?url=http(s)?:\/\/[-A-Za-z0-9+&@#/%?=~_|!:,.;]*\/\?ltiId=\d*%26ltiAction=join%26[0-9A-Fa-f-]*/,
          /*
             Example URL for a direct link to Adobe Connect
             https://meet85612477.adobeconnect.com/personname/
          */
          /http(s)?:\/\/[A-Za-z0-9.-]+\.adobeconnect\.com\/[-A-Za-z0-9+&@#/%?=~_|!:,.;]*/
        ];
        var nRegexps = regexps.length;
        
        for (var i = 0; i < events.length; i++) {

            // loop event
            var evnt = events[i];

            // get the event time (need to use millisecond value as the display value can differ in format)
            // Note: we want to use the timeZone set on the server, not whatever the browser has
            var time = moment(evnt.firstTime.time).tz(timeZone).format("LT");
                    
            var eventUrl = undefined;
            var target = undefined;
            // assignments deeplink
            if (evnt.assignmentId.length > 0) {
                jQuery.ajax({
                    url: "/direct/assignment/deepLink/" + evnt.siteId + "/" + evnt.assignmentId + ".json",
                    success: function (result) {
                        eventUrl = result.data.assignmentUrl;
                        target = "_top";
                    },
                    async: false
                }); 
            }
            
            // samigo 'deeplink', eSyncTraining meeting link, or Adobe Connect link
            // extract the URL from the description
            var ed = evnt.descriptionFormatted;
            if (ed.length > 0) {
                for(var j = 0; j<nRegexps; j++) {
                    var reg = regexps[j];
                    var matches = reg.exec(ed);
                    if(matches && matches.length > 0) {
                        eventUrl = matches[0];
                        target = "_blank";
                        break;
                    }
                }
            }
            
            //add the event to the list
            eventsDisplay.push({"eventTime": time, "eventType": evnt.type, "eventTitle": evnt.title, "eventUrl": eventUrl, "target": target,
                "siteName": evnt.siteName, "siteUrl": "/portal/site/" + evnt.siteId});
        }
        return eventsDisplay;
    }
})(jQuery);
