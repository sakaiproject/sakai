import "../sakai-notifications.js";
import { html } from "lit";
import * as data from "./data.js";
import { expect, fixture, aTimeout, waitUntil } from "@open-wc/testing";
import { stub } from "sinon";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-notifications tests", () => {

  beforeEach(() =>  {

    window.top.portal = { locale: "en_GB", siteId: data.siteId };
    window.top.portal.notifications = {
      registerPushCallback: (type, callback) => {},
      setup: Promise.resolve(),
    };

    fetchMock
      .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
      .get(data.notificationsUrl, data.notifications, { overwriteRoutes: true })
      .get("/direct/portal/clearNotification?id=noti2", 200, { overwriteRoutes: true })
      .get("/direct/portal/clearAllNotifications", 200, { overwriteRoutes: true })
      .get("*", 500, { overwriteRoutes: true });
  });

  it ("renders correctly", async () => {

    window.Notification = { permission: "granted" };

    let el = await fixture(html`
      <sakai-notifications url="${data.notificationsUrl}"></sakai-notifications>
    `);

    await waitUntil(() => el._i18n);

    expect(el.querySelectorAll(".accordion-item").length).to.equal(3);

    const assnAccordion = document.getElementById("assn-accordion");
    expect(assnAccordion).to.exist;
    expect(assnAccordion.querySelectorAll("li.toast").length).to.equal(1);

    const anncAccordion = document.getElementById("annc-accordion");
    expect(anncAccordion).to.exist;
    expect(anncAccordion.querySelectorAll("li.toast").length).to.equal(1);

    const closeButton = anncAccordion.querySelector("button.btn-close");
    expect(closeButton).to.exist;

    closeButton.click();

    // Wait for the clearNotification fetch call to do its thing
    await aTimeout(200);

    expect(el.querySelectorAll(".accordion-item").length).to.equal(2);

    const profileAccordion = document.getElementById("profile-accordion");
    expect(profileAccordion).to.exist;
    expect(profileAccordion.querySelectorAll("li.toast").length).to.equal(3);

    const clearAllButton = document.getElementById("sakai-notifications-clear-all-button");
    expect(clearAllButton).to.exist;

    clearAllButton.click();

    // Wait for the clearAllNotifications fetch call to do its thing
    await aTimeout(200);

    expect(el.querySelectorAll(".accordion-item").length).to.equal(0);
  });

  it ("shows the notifications denied message correctly", async () => {

    window.Notification = { permission: "denied" };

    let el = await fixture(html`
      <sakai-notifications url="${data.notificationsUrl}"></sakai-notifications>
    `);

    await waitUntil(() => el._i18n);

    const warning = el.querySelector("div.sak-banner-warn");
    expect(warning).to.exist;
    expect(warning.innerText).to.contain(`${el._i18n.notifications_denied} ${el._i18n.notifications_not_allowed2}`);
  });

  it ("shows the notifications default message correctly", async () => {

    window.Notification = { permission: "default" };

    let el = await fixture(html`
      <sakai-notifications url="${data.notificationsUrl}"></sakai-notifications>
    `);

    await waitUntil(() => el._i18n);

    const warning = el.querySelector("div.sak-banner-warn");
    expect(warning).to.exist;
    expect(warning.innerText).to.contain(`${el._i18n.notifications_not_allowed} ${el._i18n.notifications_not_allowed2}`);
  });

  it ("is accessible", async () => {

    window.Notification = { permission: "granted" };

    let el = await fixture(html`
      <sakai-notifications url="${data.notificationsUrl}"></sakai-notifications>
    `);

    await waitUntil(() => el._i18n);

    expect(el).to.be.accessible();
  });
});
