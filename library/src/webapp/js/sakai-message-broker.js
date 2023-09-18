portal = portal || {};
portal.notifications = portal.notifications || {};

portal.notifications.pushCallbacks = new Map();

/**
 * Create a promise which will setup the service worker and message registration
 * functions before fulfilling. Consumers can wait on this promise and then register
 * the push event they want to listen for. For an example of this, checkout
 * sakai-notifications.js in webcomponents
 */
portal.notifications.setup = new Promise((resolve, reject) => {

  navigator.serviceWorker.register("/sakai-service-worker.js").then(reg => {

    const worker = reg.active;

    if (worker) {

      // The service worker is already active, setup the listener and register function.

      portal.notifications.setupServiceWorkerListener();
      console.debug("Worker registered and setup");
      resolve();
    } else {
      console.debug("No active worker. Waiting for update ...");

      // Not active. We'll listen for an update then hook things up.

      reg.addEventListener("updatefound", () => {

        console.debug("Worker updated. Waiting for state change ...");

        const installingWorker = reg.installing;

        installingWorker.addEventListener("statechange", e => {

          console.debug("Worker state changed");

          if (e.target.state === "activated") {

            console.debug("Worker activated. Setting up ...");

            // The service worker has been updated, setup the listener and register function.

            portal.notifications.setupServiceWorkerListener();
            console.debug("Worker registered and setup");
            resolve();
          }
        });
      });
    }
  });
});

portal.notifications.registerPushCallback = (toolOrNotifications, cb) => {

  console.debug(`Registering push callback for ${toolOrNotifications}`);

  const callbacks = portal.notifications.pushCallbacks.get(toolOrNotifications) || [];
  callbacks.push(cb);
  portal.notifications.pushCallbacks.set(toolOrNotifications, callbacks);
};

portal.notifications.subscribeIfPermitted = reg => {

  console.debug("subscribeIfPermitted");

  document.body?.querySelectorAll(".portal-notifications-no-permissions-indicator")
    .forEach(el => el.classList.add("d-none"));
  document.body?.querySelectorAll(".portal-notifications-indicator")
    .forEach(el => el.classList.remove("d-none"));
  document.body?.querySelector("sakai-notifications")?.refresh();

  return new Promise(resolve => {

    if (Notification?.permission === "granted") {
      console.debug("Permission already granted. Subscribing ...");
      portal.notifications.subscribe(reg, resolve);
    } else if (Notification?.permission === "default") {

      console.debug("Push permission not set yet");

      Notification.requestPermission().then(permission => {

        if (Notification.permission === "granted") {

          console.debug("Permission granted. Subscribing ...");
          portal.notifications.subscribe(reg, resolve);
        }
      })
      .catch (error => console.error(error));
    } else {
      resolve();
    }
  });
};

navigator.serviceWorker.register("/sakai-service-worker.js").then(reg => {
  // We set this up for other parts of the code to call, without needing to register
  // the service worker first. We capture the registration in the closure.
  portal.notifications.callSubscribeIfPermitted = () => portal.notifications.subscribeIfPermitted(reg);
});

portal.notifications.serviceWorkerMessageListener = e => {

  // When the worker's EventSource receives an event it will message us (the client). This
  // code looks up the matching callback and calls it.

  if (e.data.isNotification) {
    const notificationsCallbacks = portal.notifications.pushCallbacks.get("notifications");
    notificationsCallbacks?.forEach(cb => cb(e.data));
  } else {
    const toolCallbacks = portal.notifications.pushCallbacks.get(e.data.tool);
    toolCallbacks && toolCallbacks.forEach(cb => cb(e.data));
  }
};

portal.notifications.setupServiceWorkerListener = () => {

  console.debug("setupServiceWorkerListener");

  navigator.serviceWorker.addEventListener('message', portal.notifications.serviceWorkerMessageListener);
};

portal.notifications.subscribe = (reg, resolve) => {

  const pushKeyUrl = "/api/keys/sakaipush";
  console.debug(`Fetching the push key from ${pushKeyUrl} ...`);
  fetch(pushKeyUrl).then(r => r.text()).then(key => {

    // Subscribe with the public key
    reg.pushManager.subscribe({
      userVisibleOnly: true,
      applicationServerKey: key,
    })
    .then(sub => {

      console.debug("Subscribed. Sending details to Sakai ...");

      const params = {
        endpoint: sub.endpoint,
        auth: sub.toJSON().keys.auth,
        userKey: sub.toJSON().keys.p256dh,
        browserFingerprint: getBrowserFingerprint(),
      };

      const url = "/api/users/me/pushEndpoint";
      fetch(url, {
        credentials: "include",
        method: "POST",
        body: new URLSearchParams(params),
      })
      .then(r => {

        if (!r.ok) {
          throw new Error(`Network error while posting push endpoint: ${url}`);
        }

        console.debug("Subscription details sent successfully");
      })
      .catch (error => console.error(error))
      .finally(() => resolve());
    });
  });
};

portal.notifications.checkUserChangedThenSet = userId => {

  const lastSubscribedUser = localStorage.getItem("last-sakai-user");
  const differentUser = !lastSubscribedUser || lastSubscribedUser !== portal.user.id;

  localStorage.setItem("last-sakai-user", userId);

  return differentUser;
};

if (portal?.user?.id) {

  portal.notifications.setup.then(() => console.debug("Notifications setup complete"));

  const differentUser = portal.notifications.checkUserChangedThenSet(portal.user.id);

  if (Notification?.permission !== "granted") {
    document.addEventListener("DOMContentLoaded", event => {
      document.querySelectorAll(".portal-notifications-no-permissions-indicator").forEach(b => b.classList.remove("d-none"));
    });
  }

  if (portal.notifications.pushEnabled && (Notification?.permission === "default" || differentUser)) {

    // Permission has neither been granted or denied yet, or the user has changed.

    console.debug("No permission set or user changed");

    navigator.serviceWorker.register("/sakai-service-worker.js").then(reg => {

      if (!reg.pushManager) {
        // This must be Safari < 16, or maybe IE3 or something :)
        console.warn("No pushManager on this registration");
        return;
      }

      if (differentUser) {

        console.debug("The user has changed. Unsubscribing the previous user ...");

        // The user has changed. If there is a subscription, unsubscribe it and try to subscribe
        // for the new user.
        reg.pushManager.getSubscription().then(sub => {

          if (sub) {
            //sub.unsubscribe().finally(() => portal.notifications.subscribeIfPermitted(reg));
            sub.unsubscribe().finally(() => {

              if (Notification?.permission === "granted" && differentUser) {
                portal.notifications.subscribeIfPermitted(reg);
              }
            });
          } else {
            if (Notification?.permission === "granted" && differentUser) {
              portal.notifications.subscribeIfPermitted(reg);
            }
          }
        });
      }
    });
  }
}
