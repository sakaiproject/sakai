import "../sakai-course-list.js";
import { expect, fixture, waitUntil } from "@open-wc/testing";
import { html } from "lit";
import * as data from "./data.js";

describe("sakai-course-list tests", () => {

  beforeEach(() =>  {

    window.top.portal = { locale: "en_GB", siteId: data.siteId };

    window.fetch = url => {

      if (url === data.courselistI18nUrl) {
        return Promise.resolve({ ok: true, text: () => Promise.resolve(data.courselistI18n) });
      } else if (url === data.courseListUrl) {
        return Promise.resolve({ ok: true, json: () => Promise.resolve(data.courseList) });
      } else {
        console.error(`Miss on ${url}`);
        return Promise.reject();
      }
    };
  });

  it ("renders correctly", async () => {
 
    const el = await fixture(html`<sakai-course-list user-id="${data.userId}"></sakai-course-list>`);

    await waitUntil(() => el.i18n);
    await waitUntil(() => el.sites);

    expect(el.shadowRoot.getElementById("course-list-controls")).to.exist;
  });

  it ("is accessible", async () => {

    const el = await fixture(html`<sakai-course-list user-id="${data.userId}"></sakai-course-list>`);

    await waitUntil(() => el.i18n);
    await waitUntil(() => el.sites);

    await expect(el).to.be.accessible();
  });
});
