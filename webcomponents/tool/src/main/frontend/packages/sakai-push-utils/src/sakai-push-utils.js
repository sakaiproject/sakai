import getBrowserFingerprint from "get-browser-fingerprint";
import { getUserId } from "@sakai-ui/sakai-portal-utils";

export const NOT_PUSH_CAPABLE = "NOT_PUSH_CAPABLE";

// Push permission states
export const PUSH_PERMISSION_STATES = {
  PWA_REQUIRED: "pwa_required",
  NOT_SUPPORTED: "not_supported",
  ERROR: "error"
};

// Permission timing strategies
const PERMISSION_TIMING = {
  AFTER_PWA_INSTALL: "after_pwa_install",
  AFTER_USER_ENGAGEMENT: "after_user_engagement"
};

const pushCallbacks = new Map();

// Get user agent once and reuse
const getUserAgent = () => navigator.userAgent;

export const isIOSSafari = () => {
  const ua = getUserAgent();
  return /iPad|iPhone|iPod/.test(ua) && /Safari/.test(ua) && !/Chrome/.test(ua);
};

export const isAndroid = () => {
  return /Android/.test(getUserAgent());
};

export const isPWA = () => {
  // Check for PWA display mode
  const standaloneMode = window.matchMedia("(display-mode: standalone)").matches;

  // Additional iOS Safari PWA detection
  const iosPWA = window.navigator.standalone === true;

  const isPwaMode = standaloneMode || iosPWA;

  console.debug("PWA detection:", { standaloneMode, iosPWA, isPwaMode, userAgent: getUserAgent() });

  return isPwaMode;
};

export const getBrowserInfo = () => {
  const ua = getUserAgent();

  const isIOS = isIOSSafari();
  const pwaMode = isPWA();

  if (isIOS) {
    const browserInfo = { platform: "ios", browser: "safari", requiresPWA: true };
    console.debug("iOS Safari detected:", browserInfo, "PWA mode:", pwaMode);
    return browserInfo;
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
    return PERMISSION_TIMING.AFTER_PWA_INSTALL;
  }

  if (browserInfo.platform === "android") {
    return PERMISSION_TIMING.AFTER_USER_ENGAGEMENT;
  }

  return PERMISSION_TIMING.AFTER_USER_ENGAGEMENT;
};

export const getPWAInstallationMessage = i18n => {
  const browserInfo = getBrowserInfo();

  if (browserInfo.platform === "ios") {
    return {
      title: i18n.pwa_install_title,
      message: i18n.pwa_install_instructions,
      canInstall: browserInfo.requiresPWA && !isPWA()
    };
  }

  return {
    title: i18n.pwa_notifications_title,
    message: i18n.pwa_notifications_available,
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

    console.debug("Push key received, subscribing to push manager...");

    // Subscribe with the public key
    reg.pushManager.subscribe({
      userVisibleOnly: true,
      applicationServerKey: key,
    })
    .then(sub => {

      console.debug("Push subscription created:", sub);
      console.debug("Subscription endpoint:", sub.endpoint);
      console.debug("Sending details to Sakai ...");

      const params = {
        endpoint: sub.endpoint,
        auth: sub.toJSON().keys.auth,
        userKey: sub.toJSON().keys.p256dh,
        browserFingerprint: getBrowserFingerprint(),
      };

      console.debug("Subscription params:", params);

      const url = "/api/users/me/pushEndpoint";
      fetch(url, {
        credentials: "include",
        method: "POST",
        body: new URLSearchParams(params),
      })
      .then(r => {

        if (!r.ok) {
          console.error(`Push endpoint registration failed with status ${r.status}: ${r.statusText}`);
          throw new Error(`Network error while posting push endpoint: ${url}`);
        }

        console.debug("Subscription details sent successfully to backend");
      })
      .catch (error => {
        console.error("Error sending subscription to backend:", error);
      })
      .finally(() => resolve("granted"));
    })
    .catch(error => {
      console.error("Push subscription failed:", error);
      resolve("error");
    });
  })
  .catch(error => {
    console.error("Error fetching push key:", error);
    resolve("error");
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
      return Promise.resolve(PUSH_PERMISSION_STATES.PWA_REQUIRED);
    }

    return Promise.resolve(PUSH_PERMISSION_STATES.NOT_SUPPORTED);
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
          resolve(PUSH_PERMISSION_STATES.ERROR);
        });
      } catch (err) {
        console.error("Exception requesting notification permission:", err);
        resolve(PUSH_PERMISSION_STATES.ERROR);
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

  // Check if this is iOS Safari without PWA mode
  const browserInfo = getBrowserInfo();
  if (browserInfo.requiresPWA && !isPWA()) {
    console.debug("iOS Safari detected without PWA installation - push notifications require PWA");
    reject(PUSH_PERMISSION_STATES.PWA_REQUIRED);
    return;
  } else if (browserInfo.platform === "ios" && isPWA()) {
    console.debug("iOS Safari PWA detected - push notifications should work with user interaction");
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
    return Promise.resolve(PUSH_PERMISSION_STATES.PWA_REQUIRED);
  }

  // Check optimal timing based on platform
  const timing = getOptimalPermissionTiming();
  if (timing === PERMISSION_TIMING.AFTER_USER_ENGAGEMENT) {
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
