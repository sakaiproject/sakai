portal = portal || {};
portal.notifications = portal.notifications || {};
portal.notifications.sseCallbacks = new Map();

portal.notifications.debug = false;

if (portal?.user?.id) {

  portal.notifications.setupServiceWorkerListener = () => {

    if (portal.notifications.debug) console.debug("setupServiceWorkerListener");

    // When the worker's EventSource receives an event it will message us (the client). This
    // code looks up the matching callback and calls it.
    navigator.serviceWorker.addEventListener('message', e => {

      if (e.data.type) {

        if (portal.notifications.debug) {
          console.debug(`SSE MESSAGE RECEIVED FOR TYPE: ${e.data.type}`);
          console.debug(e.data.data);
        }

        portal.notifications.sseCallbacks.has(e.data.type) && portal.notifications.sseCallbacks.get(e.data.type)(e.data.data);
      }
    });
  };

  portal.notifications.setupRegisterForMessages = worker => {

    portal.notifications.registerForMessages = (sakaiEvent, callback) => {

      if (portal.notifications.debug) console.debug(`Registering callback on ${sakaiEvent} ...`);
      portal.notifications.sseCallbacks.set(sakaiEvent, callback);

      // Tell the worker we want to listen for this event on the EventSource
      if (portal.notifications.debug) console.debug(`Telling the worker we want an SSE event for ${sakaiEvent} ...`);
      worker.postMessage(sakaiEvent);
    };
  };

  /**
   * Create a promise which will setup the service worker and message registration
   * functions before fulfilling. Consumers can wait on this promise and then register
   * the push event they want to listen for. For an example of this, checkout
   * sui-notifications.js in webcomponents
   */
  portal.notifications.setup = new Promise((resolve, reject) => {

    if (portal.notifications.debug) console.debug("Registering worker ...");

    navigator.serviceWorker.register("/api/sakai-sse-service-worker.js")
      .then(registration => {

        const worker = registration.active;

        if (worker) {

          // The serivce worker is already active, setup the listener and register function.

          portal.notifications.setupServiceWorkerListener();
          portal.notifications.setupRegisterForMessages(worker);
          if (portal.notifications.debug) console.debug("Worker registered and setup");
          resolve();
        } else {
          if (portal.notifications.debug) console.debug("No active worker. Waiting for update ...");

          // Not active. We'll listen for an update then hook things up.

          registration.addEventListener("updatefound", () => {

            if (portal.notifications.debug) console.debug("Worker updated. Waiting for state change ...");

            const installingWorker = registration.installing;

            installingWorker.addEventListener("statechange", e => {

              if (portal.notifications.debug) console.debug("Worker state changed");

              if (e.target.state === "activated") {

                if (portal.notifications.debug) console.debug("Worker activated. Setting up ...");

                // The service worker has been updated, setup the listener and register function.

                portal.notifications.setupServiceWorkerListener();
                portal.notifications.setupRegisterForMessages(installingWorker);
                if (portal.notifications.debug) console.debug("Worker registered and setup");
                resolve();
              }
            });
          });
        }
      })
      .catch (error => {

        console.error(`Failed to register service worker ${error}`);
        reject();
      });
  });

  portal.notifications.setup.then(() => console.debug("Notifications setup complete"));
} else {
  // Logged out. Tell the worker to close the EventSource.
  navigator.serviceWorker.register("/api/sakai-sse-service-worker.js").then(registration => {

    const worker = registration.active;
    if (portal.notifications.debug) console.debug("Logged out. Sending close event source signal to worker ...");
    worker && worker.postMessage({ signal: "closeEventSource" });
  });
}
