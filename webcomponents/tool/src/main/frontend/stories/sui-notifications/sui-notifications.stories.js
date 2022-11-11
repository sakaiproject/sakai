import { html } from 'lit-html';
import fetchMock from "fetch-mock";
import { notifications } from "./data/notifications.js";
import { notificationsI18n } from "./i18n/sui-notifications.js";

import '../../js/sui-notifications/sui-notifications.js';

export default {
  title: 'SUI Notifications',
  decorators: [storyFn => {

    window.portal = {};
    window.portal.registerForMessages = () => { return; };
    window.portal.registerForMessagesPromise = new Promise(resolve => resolve());

    fetchMock
      .get(/.*i18n.*sui-notifications$/, notificationsI18n, { overwriteRoutes: true })
      .get(/\/direct\/portal\/bullhornAlerts.json/, notifications, { overwriteRoutes: true })
      .get("*", 500, {overwriteRoutes: true});

    return storyFn();
  }],
};

export const BasicDisplay = () => {

  return html`
    <div>
      <sui-notifications url="/direct/portal/bullhornAlerts.json"></sui-notifications>
    </div>
  `;
};
