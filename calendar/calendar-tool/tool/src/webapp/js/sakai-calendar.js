  const userLanguage = window.top.portal.locale || navigator.language;
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
  const calendarDiv = document.getElementById('calendarDiv');
  // Initialize fullcalendar and render it in the calendarDiv.
  const calendar = new FullCalendar.Calendar(calendarDiv, {
    initialView: 'timeGridWeek',
    displayEventTime: false,
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
        Object.keys(requestData).forEach(key => calendarRequestUrl.searchParams.append(key, requestData[key]));
        window.fetch(calendarRequestUrl, requestInit)
        .then(response => {
          if (response.ok) {
            return response.json()
          } else throw new Error("Network error while fetching calendar data");
        })
        .then(responseData => {
          const events = [];
          responseData.calendar_collection.forEach(event => {
            // Every Sakai Calendar event has to be mapped with a full calendar event.
            const startTime = event.firstTime.time;
            const startDate = new Date(startTime);
            const endTime = event.firstTime.time + event.duration;
            const endDate = new Date(endTime);
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
        }).catch(error => console.error(error));
      }
    }],
    eventSourceSuccess: function(content, xhr) {
      return content.eventArray;
    },
    eventBackgroundColor: eventBackgroundColor,
    eventTextColor: eventTextColor,
    eventContent: function (args) {
      const isListView = calendar.getCurrentData() && calendar.getCurrentData().currentViewType.includes('list');
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
  userLanguage && calendar.setOption('locale', userLanguage);
  calendar.render();
