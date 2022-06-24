self.addEventListener("message", clientEvent => {

  if (clientEvent.data === "close") {
    self.eventSource && self.eventSource.close();
    self.eventSource = undefined;
  } else if (!self.eventSource) {
    self.eventSource = new EventSource("/api/users/me/events");
    self.eventSource.onerror = e => console.debug("events connection failed");

    self.eventSource.onopen = e => {

      self.eventSource.addEventListener(clientEvent.data, async message => {

        const clients = await self.clients.matchAll({ includeUncontrolled: true });
        const client = clients?.length && clients[0];
        const strippedMessage = { type: message.type, data: JSON.parse(message.data) };
        client && client.postMessage(strippedMessage);
      });
    };
  }
});
