import "../sakai-notifications.js";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, html, aTimeout, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";
describe("sakai-notifications tests", () => {

  beforeEach(() =>  {
    fetchMock.mockGlobal();
    window.top.portal = { siteId: data.siteId };
    window.top.portal.notifications = {
      registerPushCallback: (type, callback) => {},
      setup: Promise.resolve(),
    };
    setRoutes(data.notifications);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  function setRoutes(notificationsPayload) {
    fetchMock.removeRoutes();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.profileI18nUrl, data.profileI18n)
      .get(data.notificationsUrl, notificationsPayload)
      .post("/api/users/me/notifications/noti2/clear", 200)
      .post("/api/users/me/notifications/clear", 200)
      .get("*", 500);
  }


  it ("renders correctly", async () => {

    window.Notification = { permission: "granted" };

    let el = await fixture(html`
      <sakai-notifications url="${data.notificationsUrl}"></sakai-notifications>
    `);

    await waitUntil(() => el._i18n);
    await waitUntil(() => el.notifications?.length);

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
  it ("does not duplicate site title when notifications are filtered multiple times", async () => {

    window.Notification = { permission: "granted" };

    const assignmentNotification = {
      event: "asn.new.assignment",
      fromUser: "instructor",
      fromDisplayName: "Instructor Example",
      formattedEventDate: "15 Oct, 2025",
      id: "noti-assignment",
      siteTitle: "Biology 101",
      title: "Lab Report",
      url: "http://example.com/assignments/1",
    };

    setRoutes([assignmentNotification]);

    const el = await fixture(html`
      <sakai-notifications url="${data.notificationsUrl}"></sakai-notifications>
    `);

    await waitUntil(() => el._i18n);
    await waitUntil(() => el.notifications?.length);
    await waitUntil(() => el._filteredNotifications.get("asn")?.length);

    const decoratedTitle = el._filteredNotifications.get("asn")[0].title;

    el._filterIntoToolNotifications();

    const refreshedTitle = el._filteredNotifications.get("asn")[0].title;
    expect(refreshedTitle).to.equal(decoratedTitle);
    const escapeRe = s => s.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
    const siteTitleOccurrences = (decoratedTitle.match(new RegExp(escapeRe(assignmentNotification.siteTitle), "g")) || []).length;
    expect(siteTitleOccurrences).to.equal(1);
  });
});
