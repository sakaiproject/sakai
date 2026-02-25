import "../sakai-course-card.js";
import { expect, fixture, html, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import fetchMock from "fetch-mock";
describe("sakai-course-card tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.toolnameMappingsUrl, data.toolnameMappings)
      .get("*", 500);

  });
  afterEach(() => {
    fetchMock.hardReset();


  });

  window.top.portal = { siteId: data.siteId };

  it ("renders correctly", async () => {

    const el = await fixture(html`<sakai-course-card .courseData=${data.course1}></sakai-course-card>`);

    await waitUntil(() => el._i18n);
    await waitUntil(() => el.courseData);

    await expect(el).to.be.accessible();

    expect(el.querySelector("div.info-block")).to.exist;
  });
});
