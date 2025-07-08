import getBrowserFingerprint from "get-browser-fingerprint";
import { getUserId } from "@sakai-ui/sakai-portal-utils";

export const NOT_PUSH_CAPABLE = "NOT_PUSH_CAPABLE";

const pushCallbacks = new Map();

export const isIOSSafari = () => {
  const ua = navigator.userAgent;
  return /iPad|iPhone|iPod/.test(ua) && /Safari/.test(ua) && !/Chrome/.test(ua);
};

export const isAndroid = () => {
  return /Android/.test(navigator.userAgent);
};

export const isPWA = () => {
  return window.matchMedia("(display-mode: standalone)").matches;
};

export const getBrowserInfo = () => {
  const ua = navigator.userAgent;

  if (isIOSSafari()) {
    return { platform: "ios", browser: "safari", requiresPWA: true };
  }

  if (isAndroid()) {
    if (/Chrome/.test(ua)) {
      return { platform: "android", browser: "chrome", requiresPWA: false };
    }
    if (/Firefox/.test(ua)) {
      return { platform: "android", browser: "firefox", requiresPWA: false };
    }
    if (/Edge/.test(ua)) {
      return { platform: "android", browser: "edge", requiresPWA: false };
    }
  }

  // Desktop detection
  if (/Chrome/.test(ua)) {
    return { platform: "desktop", browser: "chrome", requiresPWA: false };
  }
  if (/Firefox/.test(ua)) {
    return { platform: "desktop", browser: "firefox", requiresPWA: false };
  }
  if (/Safari/.test(ua)) {
    return { platform: "desktop", browser: "safari", requiresPWA: false };
  }
  if (/Edge/.test(ua)) {
    return { platform: "desktop", browser: "edge", requiresPWA: false };
  }

  return { platform: "unknown", browser: "unknown", requiresPWA: false };
};

export const canRequestPushPermission = () => {
  const browserInfo = getBrowserInfo();

  if (browserInfo.requiresPWA) {
    return isPWA();
  }

  return true;
};

export const getOptimalPermissionTiming = () => {
  const browserInfo = getBrowserInfo();

  if (browserInfo.platform === "ios") {
    return "after_pwa_install";
  }

  if (browserInfo.platform === "android") {
    return "after_user_engagement";
  }

  return "after_user_engagement";
};

export const getPWAInstallationMessage = () => {
  const browserInfo = getBrowserInfo();

  if (browserInfo.platform === "ios") {
    return {
      title: "Install as Web App",
      message: "To receive push notifications, add this site to your home screen:\n1. Tap the Share button\n2. Select 'Add to Home Screen'\n3. Tap 'Add'",
      canInstall: browserInfo.requiresPWA && !isPWA()
    };
  }

  return {
    title: "Push Notifications",
    message: "Push notifications are available for this site",
    canInstall: false
  };
};

export const shouldShowPWAPrompt = () => {
  const browserInfo = getBrowserInfo();
  return browserInfo.requiresPWA && !isPWA();
};

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
      .finally(() => resolve("granted"));
    });
  });
}; // subscribe

export const subscribeIfPermitted = reg => {

  console.debug("subscribeIfPermitted");

  const browserInfo = getBrowserInfo();

  // Check if push permissions can be requested on this platform
  if (!canRequestPushPermission()) {
    console.debug("Push permissions cannot be requested on this platform/browser configuration");

    if (browserInfo.requiresPWA) {
      console.debug("PWA installation required for push notifications");
      return Promise.resolve("pwa_required");
    }

    return Promise.resolve("not_supported");
  }

  document.body?.querySelectorAll(".portal-notifications-indicator")
    .forEach(el => el.classList.remove("d-none"));

  return new Promise(resolve => {

    if (Notification?.permission === "granted") {
      console.debug("Permission already granted. Subscribing ...");
      subscribe(reg, resolve);
    } else if (Notification?.permission === "default") {

      console.debug("Push permission not set yet");

      // For iOS Safari, provide additional context about PWA requirement
      if (browserInfo.platform === "ios" && browserInfo.requiresPWA) {
        console.debug("iOS Safari detected - PWA mode required for push notifications");
      }

      try {
        Notification.requestPermission().then(permission => {

          if (permission === "granted") {

            console.debug("Permission granted. Subscribing ...");
            subscribe(reg, resolve);
          } else {
            console.debug(`Permission ${permission} - user declined or blocked notifications`);
            resolve(permission);
          }
        })
        .catch (error => {
          console.error("Error requesting notification permission:", error);
          resolve("error");
        });
      } catch (err) {
        console.error("Exception requesting notification permission:", err);
        resolve("error");
      }
    } else {
      console.debug(`Permission already ${Notification?.permission}`);
      resolve(Notification?.permission);
    }
  });
}; // subscribeIfPermitted

const serviceWorkerMessageListener = e => {

  // When the worker's EventSource receives an event it will message us (the client). This
  // code looks up the matching callback and calls it.

  const notificationsCallbacks = pushCallbacks.get("notifications");
  notificationsCallbacks?.forEach(cb => cb(e.data));
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
export const pushSetupComplete = new Promise((resolve, reject) => {

  if (!navigator.serviceWorker || !window.Notification) {
    console.error("Service worker not supported");
    reject(NOT_PUSH_CAPABLE);
    return;
  }

  navigator.serviceWorker.register(serviceWorkerPath).then(reg => {

    if (!reg.pushManager) {
      reject(NOT_PUSH_CAPABLE);
      return;
    }

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

  const browserInfo = getBrowserInfo();

  // Provide platform-specific guidance
  if (browserInfo.requiresPWA && !isPWA()) {
    console.debug("PWA installation required before subscribing to push notifications");
    return Promise.resolve("pwa_required");
  }

  // Check optimal timing based on platform
  const timing = getOptimalPermissionTiming();
  if (timing === "after_user_engagement") {
    console.debug("Consider requesting push permission after user engagement rather than immediately");
  }

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
