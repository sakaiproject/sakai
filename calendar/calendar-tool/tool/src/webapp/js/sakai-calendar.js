const sakaiCalendar = {
  calendar: {},
  userLanguage: window.top.portal.locale || navigator.language,
  userTimeZone: window.top.portal.user.timezone || 'local',

  // Initialize the calendar and attach it to the calendar div.
  initializeSakaiCalendar (calendarDiv) {
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
      timeZone: sakaiCalendar.userTimeZone,
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
              // .tz() converts the event to the timezone of the user.
              const startDate = moment(new Date(event.firstTime.time)).tz(sakaiCalendar.userTimeZone).format();
              const endDate = moment(new Date(event.firstTime.time + event.duration)).tz(sakaiCalendar.userTimeZone).format();
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

    // Set the user language to the fullcalendar UI.
    sakaiCalendar.userLanguage && this.calendar.setOption('locale', sakaiCalendar.userLanguage);
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

  printCalendar (printableVersionUrl) {
    const currentSelectedDate = new Date(this.calendar.currentData.currentDate.getTime());
    const currentView = this.calendar.currentData.currentViewType.toLowerCase();

    /** Calendar Printing Views. */
    // LIST_SUBVIEW = 1;
    // MONTH_VIEW = 3;
    // List is the default subview for weekly, daily and list views.
    let currentPrintview = 1;
    // Different views have different time ranges
    if (currentView.includes('month')) {
      currentPrintview = 3;
    }

    // Now we have the right time ranges, we must replace the query params.
    const defaultPrintCalendarUrl = new URL(printableVersionUrl);
    const defaultPrintCalendarParams = defaultPrintCalendarUrl.searchParams;
    defaultPrintCalendarParams.forEach((value, key) => {
      switch(key) {
        case 'scheduleType':
          defaultPrintCalendarParams.set('scheduleType', currentPrintview);
          break;
        default:
      }
    });
    defaultPrintCalendarParams.set('selectedCalendarDate', currentSelectedDate.toISOString());
    // Now we have the right URL, make the print request.
    window.open(defaultPrintCalendarUrl.href, '_blank');
  }

};
