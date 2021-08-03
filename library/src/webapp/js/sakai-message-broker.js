portal = portal || {};

if (portal?.user?.id) {
  portal.messagesEventSource = new EventSource("/api/users/me/events");

  portal.messagesEventSource.onopen = e => console.debug("events connection established");

  portal.registerForMessages = function (sakaiEvent, listener) {

    console.debug(`Registering a listener for event ${sakaiEvent} ...`);
    portal.messagesEventSource.addEventListener(sakaiEvent, e => listener(JSON.parse(event.data)));
  };
}
