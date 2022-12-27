self.addEventListener("message", clientEvent => {

  if (clientEvent.data === "close") {
    console.debug("Closing event source ...");
    self.eventSource && self.eventSource.close();
    self.eventSource = undefined;
  } else if (!self.eventSource) {
    console.debug("EventSource not opened yet. Opening ...");
    self.eventSource = new EventSource("/api/users/me/events");
    console.debug("EventSource source created. Waiting for open event ...");
    self.eventSource.onerror = e => console.debug("events connection failed");

    self.eventSource.onopen = e => {

      console.debug("Event source opened ...");

      self.eventSource.addEventListener(clientEvent.data, async message => {

        console.debug("SSE message received");
        console.debug(message);

        const stripped = { type: message.type, data: JSON.parse(message.data) };
        const clients = await self.clients.matchAll({ includeUncontrolled: true });
        clients && clients.filter(c => c.visibilityState === "visible").forEach(c => c.postMessage(stripped));
      });
    };
  }
});
