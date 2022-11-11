self.addEventListener("message", clientEvent => {

  if (clientEvent.data === "close") {
    self.eventSource && self.eventSource.close();
    self.eventSource = undefined;
  } else if (!self.eventSource) {
    self.eventSource = new EventSource("/api/users/me/events");
    self.eventSource.onerror = e => console.debug("events connection failed");

    self.eventSource.onopen = e => {

      self.eventSource.addEventListener(clientEvent.data, async message => {

        const stripped = { type: message.type, data: JSON.parse(message.data) };
        const clients = await self.clients.matchAll({ includeUncontrolled: true });
        clients && clients.filter(c => c.visibilityState === "visible").forEach(c => c.postMessage(stripped));
      });
    };
  }
});
