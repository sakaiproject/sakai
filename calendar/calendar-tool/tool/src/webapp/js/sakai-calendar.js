const sakaiCalendar = {
  calendar: {},

  // Initialize the calendar and attach it to the calendar div.
  initializeSakaiCalendar (calendarDiv) {
    const userLanguage = window.top.portal.locale || navigator.language;
    const userTimeZone = window.top.portal.user.timezone || 'local';
    // Get the event color from the skin variables.
    const computedStyle = getComputedStyle(document.querySelector(':root'));
    const eventBackgroundColor = computedStyle.getPropertyValue('--infoBanner-bgcolor', '#e9f5fc');
    const eventTextColor = computedStyle.getPropertyValue('--infoBanner-color', '#196390');
    const sakaiOrigin = window.location.origin;
    const siteId = window.top.portal.siteId;
    // We need the updated calendar events, do not cache requests.
    const requestHeaders = new Headers();
    requestHeaders.append('pragma', 'no-cache');
    requestHeaders.append('cache-control', 'no-cache');
    const requestInit = {
      method: 'GET',
      headers: requestHeaders,
    };
    // Initialize fullcalendar and render it in the calendarDiv.
    this.calendar = new FullCalendar.Calendar(calendarDiv, {
      initialView: 'timeGridWeek',
      timeZone: userTimeZone,
      displayEventTime: false,
      allDaySlot: false,
      headerToolbar: {
        left: 'prev,next today',
        center: 'title',
        right: 'dayGridMonth,timeGridWeek,timeGridDay,listWeek'
      },
      eventSources: [{
        events: function(event, successCallback, failureCallback) {
          const requestStartDate = moment(event.start).format('YYYY-MM-DD');
          const requestEndDate = moment(event.end).format('YYYY-MM-DD');
          const requestData = {
            merged: true,
            firstDate: requestStartDate,
            lastDate: requestEndDate
          };
          // Fetch the site calendar events from the REST endpoint.
          const calendarRequestUrl = new URL(`${sakaiOrigin}/direct/calendar/site/${siteId}.json`);
          Object.keys(requestData).forEach( (key) => calendarRequestUrl.searchParams.append(key, requestData[key]));
          window.fetch(calendarRequestUrl, requestInit).then( (response) => {
           if (response.ok) {
             return response.json()
           } else throw new Error("Network error while fetching calendar data");
          }).then( (responseData) => {
            const events = [];
            responseData.calendar_collection.forEach( (event) => {
              // Every Sakai Calendar event has to be mapped with a full calendar event.
              const startDate = moment(new Date(event.firstTime.time)).tz(userTimeZone).format();
              const endDate = moment(new Date(event.firstTime.time + event.duration)).tz(userTimeZone).format();
              // The calendar event url needs to be a link to view or edit the event.
              const eventReference = event.eventReference;
              const eventLink = new URL(window.location);
              eventLink.searchParams.append("eventReference", eventReference);
              eventLink.searchParams.append("sakai_action", "doDescription");
              eventLink.searchParams.append("panel", "Main");
              events.push({
                url: eventLink.href,
                title: event.title,
                start: startDate,
                site_name: event.siteName,
                type: event.type,
                icon: event.eventIcon,
                event_id:  event.eventId,
                attachments: event.attachments,
                eventReference: event.eventReference,
                end: endDate
              });
            });
            successCallback(events);
          }).catch( (error) => console.error(error));
        }
      }],
      eventSourceSuccess: function(content, xhr) {
        return content.eventArray;
      },
      eventBackgroundColor: eventBackgroundColor,
      eventTextColor: eventTextColor,
      eventContent: function (args) {
        const isListView = sakaiCalendar.calendar.getCurrentData() && sakaiCalendar.calendar.getCurrentData().currentViewType.includes('list');
        // Create the default event structure to benefit from the theme.
        const eventTitle = document.createElement('div');
        eventTitle.classList.add(!isListView ? 'fc-event-title' : 'fc-list-event-title');
        // Build our event HTML including the default icon for the event type.
        const icon = args.event._def.extendedProps.icon;
        const title = args.event._def.title;
        const titleElement = document.createTextNode(title);
        const type = args.event._def.extendedProps.type;
        const eventUrl = args.event._def.url;
        const eventLink = document.createElement('a');
        eventLink.href = eventUrl;
        eventLink.title = title;
        eventLink.appendChild(titleElement);
        const iconSpan = document.createElement('span');
        iconSpan.classList.add('icon');
        iconSpan.classList.add(icon);
        iconSpan.title = type;
        // Append our custom event with icon to the default event structure.
        eventTitle.appendChild(iconSpan);
        eventTitle.appendChild(!isListView ? titleElement : eventLink);
        // Return the modified event including out event structure.
        return {
          html: eventTitle.outerHTML
        };
      }
    });

    userLanguage && this.calendar.setOption('locale', userLanguage);
    this.calendar.render();
  },

  // Go to the current date set by the backend.
  gotoDate (currentDate) {
    this.calendar.gotoDate(currentDate);
  },

    // When the user changes the view, reflect the change in a param to set the default view.
  onChangeCalendarView () {
    const currentView = this.calendar.currentData.currentViewType;
    const defaultViewParams = document.getElementsByName('calendar_default_subview');
    if (defaultViewParams && defaultViewParams.length > 0) {
      defaultViewParams[0].value = currentView;
    }
    // Reenable the button when the subview changes.
    const changeDefaultViewButton = document.getElementsByName('eventSubmit_doDefaultview');
    if (changeDefaultViewButton && changeDefaultViewButton.length > 0) {
      changeDefaultViewButton[0].removeAttribute('disabled');
    }
  },

  // This logic is associated to set the default subview, by day, month, week or list.
  setDefaultSubview (defaultSubview) {
    switch (defaultSubview) {
      case 'day':
        this.calendar.changeView('timeGridDay');
        break;
      case 'month':
        this.calendar.changeView('dayGridMonth');
        break;
      case 'list':
        this.calendar.changeView('listWeek');
        break;
      case 'week':
      default:
        this.calendar.changeView('timeGridWeek');
    }

    document.querySelectorAll('.fc-timeGridWeek-button, .fc-dayGridMonth-button, .fc-timeGridDay-button, .fc-listWeek-button').forEach( (viewButton) => viewButton.setAttribute('onclick', 'sakaiCalendar.onChangeCalendarView();'));

  },

  // Utility method to help with date formats.
  formatDateForRange (date) {
    const hours = date.getUTCHours() === 0 ? '00' : date.getUTCHours();
    const minutes = date.getUTCMinutes() === 0 ? '00' : date.getUTCMinutes();
    const seconds = date.getUTCSeconds() === 0 ? '00' : date.getUTCSeconds();
    let month = date.getUTCMonth() + 1;
    const monthString = month < 10 ? '0' + month : ''+month;
    const dayString = date.getUTCDate() < 10 ? '0' + date.getUTCDate() : ''+date.getUTCDate();
    return `${date.getUTCFullYear()}${monthString}${dayString}${hours}${minutes}${seconds}000`;
  },

  printCalendar (printableVersionUrl) {
    const currentSelectedDate = new Date(this.calendar.currentData.currentDate.getTime());
    const currentView = this.calendar.currentData.currentViewType.toLowerCase();

    /** Calendar Printing Views. */
    // DAY_VIEW = 0;
    // WEEK_VIEW = 2;
    // MONTH_VIEW = 3;
    // LIST_VIEW = 5;
    // Week is the default subview
    let currentPrintview = 2;
    let currentTimeRange = '';

    // Set the selected day from 00:00 to 23:59
    const selectedDate = new Date(currentSelectedDate.getTime());
    selectedDate.setHours(0);
    selectedDate.setMinutes(0);
    selectedDate.setSeconds(0);
    const startDateFormatted = this.formatDateForRange(selectedDate);
    // Nice trick to go to the last moment of the day
    selectedDate.setDate(selectedDate.getDate() + 1);
    selectedDate.setSeconds(selectedDate.getSeconds() - 1);
    const endDateFormatted = this.formatDateForRange(selectedDate);
    const dailyStartTime = startDateFormatted + '-' + endDateFormatted;

    // Different views have different time ranges
    if (currentView.includes('list') || currentView.includes('week')) {
        currentPrintview = currentView.includes('list') ? 5 : 2;
        // Set the selected week from Sunday@00:00 to Monday@23:59
        const weekSelectedDate = new Date(currentSelectedDate.getTime());
        weekSelectedDate.setHours(0);
        weekSelectedDate.setMinutes(0);
        weekSelectedDate.setSeconds(0);
        // Set the date to the first day of the week.
        weekSelectedDate.setDate(weekSelectedDate.getDate() - weekSelectedDate.getDay());
        const startListDateFormatted = this.formatDateForRange(weekSelectedDate);
        // Set the date to the last day of the week.
        weekSelectedDate.setDate(weekSelectedDate.getDate() + 7);
        weekSelectedDate.setSeconds(weekSelectedDate.getSeconds() - 1);
        const endListDateFormatted = this.formatDateForRange(weekSelectedDate);
        currentTimeRange =  startListDateFormatted + '-' + endListDateFormatted;
    } else if (currentView.includes('month')) {
        currentPrintview = 3;
        // Set the selected month from the 1st@00:00 to the last@23:59
        const monthSelectedDate = new Date(currentSelectedDate.getTime());
        monthSelectedDate.setDate(1);
        monthSelectedDate.setHours(0);
        monthSelectedDate.setMinutes(0);
        monthSelectedDate.setSeconds(0);
        const startMonthDateFormatted = this.formatDateForRange(monthSelectedDate);
        // Nice trick to go back to the last moment of the month.
        monthSelectedDate.setMonth(monthSelectedDate.getMonth() + 1);
        monthSelectedDate.setSeconds(monthSelectedDate.getSeconds() - 1);
        const endMonthDateFormatted = this.formatDateForRange(monthSelectedDate);
        currentTimeRange =  startMonthDateFormatted + '-' + endMonthDateFormatted;
    } else if (currentView.includes('day')) {
        currentPrintview = 0;
        currentTimeRange = dailyStartTime;
    }

    // Now we have the right time ranges, we must replace the query params.
    const defaultPrintCalendarUrl = new URL(printableVersionUrl);
    const defaultPrintCalendarParams = defaultPrintCalendarUrl.searchParams;
    defaultPrintCalendarParams.forEach((value, key) => {
      switch(key) {
        case 'scheduleType':
          defaultPrintCalendarParams.set('scheduleType', currentPrintview);
          break;
        case 'timeRange':
            defaultPrintCalendarParams.set('timeRange', currentTimeRange);
          break;
        case 'dailyStartTime':
            defaultPrintCalendarParams.set('dailyStartTime', dailyStartTime);
          break;
        default:
      }
    });

    // Now we have the right URL, make the print request.
    window.open(defaultPrintCalendarUrl.href, '_blank');
  }

};
