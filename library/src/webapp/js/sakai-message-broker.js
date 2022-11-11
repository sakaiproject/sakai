portal = portal || {};

if (portal?.user?.id) {
  portal.sseCallbacks = new Map();

  portal.setupServiceWorkerListener = () => {

    // When the worker's EventSource receives an event it will message us (the client). This
    // code looks up the matching callback and calls it.
    navigator.serviceWorker.addEventListener('message', e => {
      portal.sseCallbacks.has(e.data.type) && portal.sseCallbacks.get(e.data.type)(e.data.data);
    });
  };

  portal.setupRegisterForMessages = worker => {

    portal.registerForMessages = (sakaiEvent, callback) => {

      portal.sseCallbacks.set(sakaiEvent, callback);
      // Tell the worker we want to listen for this event on the EventSource
      worker.postMessage(sakaiEvent);
    };
  };

  /**
   * Create a promise which will setup the service worker and message registration
   * functions before fulfilling. Consumers can wait on this promise and then register
   * the push event they want to listen for. For an example of this, checkout
   * sakai.morpheus.bullhorns.js.
   */
  portal.registerForMessagesPromise = new Promise((resolve, reject) => {

    navigator.serviceWorker.register("/api/sakai-sse-service-worker.js")
      .then(registration => {

        const worker = registration.active;

        if (worker) {
          portal.setupServiceWorkerListener();
          portal.setupRegisterForMessages(worker);
          resolve();
        } else {
          registration.addEventListener("updatefound", () => {

            const installingWorker = registration.installing;

            installingWorker.addEventListener("statechange", e => {

              if (e.target.state === "activated") {

                portal.setupServiceWorkerListener();
                portal.setupRegisterForMessages(installingWorker);
                resolve();
              }
            });
          });
        }
      })
      .catch (error => {

        console.error("Failed to register my shit");
        reject();
      });
  });
} else {
  navigator.serviceWorker.register("/api/sakai-sse-service-worker.js")
    .then(registration => {

      // Logged out. Tell the worker to close the EventSource.
      const worker = registration.active;
      worker && worker.postMessage("close");
    });
}
