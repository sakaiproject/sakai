import "../sakai-course-card.js";
import { expect, fixture, waitUntil } from "@open-wc/testing";
import { html } from "lit";
import * as data from "./data.js";

describe("sakai-course-card tests", () => {

  beforeEach(() =>  {

    window.top.portal = { locale: "en_GB", siteId: data.siteId };

    window.fetch = url => {

      if (url === data.coursecardI18nUrl) {
        return Promise.resolve({ ok: true, text: () => Promise.resolve(data.coursecardI18n) });
      } else if (url === data.toolnameMappingsUrl) {
        return Promise.resolve({ ok: true, text: () => Promise.resolve(data.toolnameMappings) });
      } else {
        console.error(`Miss on ${url}`);
        return Promise.reject();
      }
    };
  });

  it ("renders correctly", async () => {
 
    const el = await fixture(html`<sakai-course-card></sakai-course-card>`);

    await waitUntil(() => el.i18n);

    expect(el.shadowRoot.querySelector("div.info-block")).to.exist;
  });

  it ("is accessible", async () => {

    const el = await fixture(html`<sakai-course-card></sakai-course-card>`);

    await waitUntil(() => el.i18n);

    await expect(el).to.be.accessible();
  });
});
