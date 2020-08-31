portal = portal || {};

portal.messagesEventSource = new EventSource("/api/users/me/events", { withCredentials: true });

portal.registerForMessages = function (sakaiEvent, listener) {

  console.debug(`Registering a listener for event ${sakaiEvent} ...`);
  portal.messagesEventSource.addEventListener(sakaiEvent, (e) => listener(JSON.parse(event.data)));
};
