import getBrowserFingerprint from "get-browser-fingerprint";
import { getUserId } from "@sakai-ui/sakai-portal-utils";

const pushCallbacks = new Map();

const serviceWorkerPath = "/sakai-service-worker.js";

const subscribe = (reg, resolve) => {

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
}; // subscribe

export const subscribeIfPermitted = reg => {

  console.debug("subscribeIfPermitted");

  document.body?.querySelectorAll(".portal-notifications-no-permissions-indicator")
    .forEach(el => el.classList.add("d-none"));
  document.body?.querySelectorAll(".portal-notifications-indicator")
    .forEach(el => el.classList.remove("d-none"));

  return new Promise(resolve => {

    if (Notification?.permission === "granted") {
      console.debug("Permission already granted. Subscribing ...");
      subscribe(reg, resolve);
    } else if (Notification?.permission === "default") {

      console.debug("Push permission not set yet");

      try {
        Notification.requestPermission().then(permission => {

          if (permission === "granted") {

            console.debug("Permission granted. Subscribing ...");
            subscribe(reg, resolve);
          }
        })
        .catch (error => console.error(error));
      } catch (err) {
        console.error(err);
      }
    } else {
      resolve();
    }
  });
}; // subscribeIfPermitted

const serviceWorkerMessageListener = e => {

  // When the worker's EventSource receives an event it will message us (the client). This
  // code looks up the matching callback and calls it.

  if (e.data.isNotification) {
    const notificationsCallbacks = pushCallbacks.get("notifications");
    notificationsCallbacks?.forEach(cb => cb(e.data));
  } else {
    const toolCallbacks = pushCallbacks.get(e.data.tool);
    toolCallbacks && toolCallbacks.forEach(cb => cb(e.data));
  }
};

const setupServiceWorkerListener = () => {

  console.debug("setupServiceWorkerListener");

  navigator.serviceWorker.addEventListener("message", serviceWorkerMessageListener);
};

const checkUserChangedThenSet = userId => {

  if (!userId) return false;

  const lastSubscribedUser = localStorage.getItem("last-sakai-user");
  const differentUser = !lastSubscribedUser || lastSubscribedUser !== portal.user.id;

  localStorage.setItem("last-sakai-user", userId);

  return differentUser;
};

/**
 * Create a promise which will setup the service worker and message registration
 * functions before fulfilling. Consumers can wait on this promise and then register
 * the push event they want to listen for. For an example of this, checkout
 * sakai-notifications.js in webcomponents
 */
export const pushSetupComplete = new Promise(resolve => {

  navigator.serviceWorker.register(serviceWorkerPath).then(reg => {

    const worker = reg.active;

    if (worker) {

      // The service worker is already active, setup the listener and register function.

      setupServiceWorkerListener();
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

            setupServiceWorkerListener();
            console.debug("Worker registered and setup");
            resolve();
          }
        });
      });
    }
  });
}); // pushSetupComplete

// We set this up for other parts of the code to call, without needing to register
// the service worker first. We capture the registration in the closure.
export const callSubscribeIfPermitted = async () => {

  const reg = await navigator.serviceWorker.register(serviceWorkerPath);
  return subscribeIfPermitted(reg);
};

export const registerPushCallback = (toolOrNotifications, cb) => {

  console.debug(`Registering push callback for ${toolOrNotifications}`);

  const callbacks = pushCallbacks.get(toolOrNotifications) || [];
  callbacks.push(cb);
  pushCallbacks.set(toolOrNotifications, callbacks);
};

if (checkUserChangedThenSet(getUserId())) {

  console.debug("The user has changed. Unsubscribing the previous user ...");

  navigator.serviceWorker.register(serviceWorkerPath).then(reg => {

    // The user has changed. If there is a subscription, unsubscribe it and try to subscribe
    // for the new user.
    reg.pushManager?.getSubscription().then(sub => {

      if (sub) {
        sub.unsubscribe().finally(() => {

          if (Notification?.permission === "granted") {
            subscribeIfPermitted(reg);
          }
        });
      } else if (Notification?.permission === "granted") {
        subscribeIfPermitted(reg);
      }
    });
  });
}
