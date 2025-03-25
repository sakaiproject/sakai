import "../sakai-notifications.js";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, html, aTimeout, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-notifications tests", () => {

  beforeEach(() =>  {

    window.top.portal = { siteId: data.siteId };
    window.top.portal.notifications = {
      registerPushCallback: (type, callback) => {},
      setup: Promise.resolve(),
    };

    fetchMock
      .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
      .get(data.profileI18nUrl, data.profileI18n, { overwriteRoutes: true })
      .get(data.notificationsUrl, data.notifications, { overwriteRoutes: true })
      .post("/api/users/me/notifications/noti2/clear", 200, { overwriteRoutes: true })
      .post("/api/users/me/notifications/clear", 200, { overwriteRoutes: true })
      .get("*", 500, { overwriteRoutes: true });
  });

  it ("renders correctly", async () => {

    window.Notification = { permission: "granted" };

    let el = await fixture(html`
      <sakai-notifications url="${data.notificationsUrl}"></sakai-notifications>
    `);

    await waitUntil(() => el._i18n);
    await waitUntil(() => el.notifications.length);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelectorAll(".accordion-item").length).to.equal(2);

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

    await expect(el).to.be.accessible();

    expect(el.querySelectorAll(".accordion-item").length).to.equal(1);

    const clearAllButton = document.getElementById("sakai-notifications-clear-all-button");
    expect(clearAllButton).to.exist;

    clearAllButton.click();

    // Wait for the clearAllNotifications fetch call to do its thing
    await aTimeout(200);

    await expect(el).to.be.accessible();

    expect(el.querySelectorAll(".accordion-item").length).to.equal(0);
  });
});
