import "../sakai-course-list.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import * as courseCardData from "../../sakai-course-card/test/data.js";
import fetchMock from "fetch-mock";
describe("sakai-course-list tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.courseListUrl, data.courseList, { name: "courses" })
      .get(courseCardData.i18nUrl, courseCardData.i18n)
      .get(courseCardData.toolnameMappingsUrl, courseCardData.toolnameMappings)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });


  window.top.portal = { locale: "en_GB", siteId: data.siteId };


  it ("renders correctly", async () => {

    const el = await fixture(html`<sakai-course-list user-id="${data.userId}"></sakai-course-list>`);

    await waitUntil(() => el.sites);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelectorAll("#course-list-term-filter option").length).to.equal(3);
    expect(el.querySelectorAll("sakai-course-card").length).to.equal(3);
  });

  it("renders all pinned sites when a course term is unmatched", async () => {

    fetchMock.modifyRoute("courses", {
      response: {
        ...data.courseList,
        sites: [
          { ...data.courseList.sites[0], term: "Spring Term " },
          data.courseList.sites[1],
          { ...data.courseList.sites[2], course: false, project: true },
        ],
      },
    });

    const el = await fixture(html`<sakai-course-list user-id="${data.userId}"></sakai-course-list>`);

    await waitUntil(() => el.querySelectorAll("sakai-course-card").length === 3);

    expect(el.querySelector(".sak-banner-info")).to.be.null;
    expect(Array.from(el.querySelectorAll("sakai-course-card"), card => card.courseData.title))
      .to.deep.equal(data.courseList.sites.map(site => site.title));
    expect(Array.from(el.querySelectorAll("#course-list-term-filter option"), option => option.value))
      .to.deep.equal([ "none", "summer" ]);
  });
});
