import "../sakai-course-card.js";
import { expect, fixture, waitUntil } from "@open-wc/testing";
import { html } from "lit";
import * as data from "./data.js";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-course-card tests", () => {

  window.top.portal = { locale: "en_GB", siteId: data.siteId };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get(data.toolnameMappingsUrl, data.toolnameMappings, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders correctly", async () => {
 
    const el = await fixture(html`<sakai-course-card .courseData=${data.course1}></sakai-course-card>`);

    await waitUntil(() => el._i18n);
    await waitUntil(() => el.courseData);

    expect(el.shadowRoot.querySelector("div.info-block")).to.exist;
  });

  it ("is accessible", async () => {

    const el = await fixture(html`<sakai-course-card .courseData=${data.course1}></sakai-course-card>`);

    await waitUntil(() => el._i18n);

    await expect(el).to.be.accessible();
  });
});
