import "../sakai-course-list.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import * as courseCardData from "../../sakai-course-card/test/data.js";
import { dialogI18nUrl, dialogI18n } from "@sakai-ui/sakai-dialog/test-utils";
import { imageEditorI18nUrl, imageEditorI18n } from "@sakai-ui/sakai-image-editor/test-utils";
import fetchMock from "fetch-mock";
describe("sakai-course-list tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.courseListUrl, data.courseList, { name: "courses" })
      .get(courseCardData.i18nUrl, courseCardData.i18n)
      .get(dialogI18nUrl, dialogI18n)
      .get(imageEditorI18nUrl, imageEditorI18n)
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

    expect(el.querySelectorAll("sakai-course-card").length).to.equal(3);

    el.filtersOpen = true;
    await elementUpdated(el);
    expect(el.querySelectorAll("#course-list-term-filter option").length).to.equal(3);
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
    const titles = Array.from(el.querySelectorAll("sakai-course-card"), card => card.courseData.title);
    expect(titles).to.have.members(data.courseList.sites.map(site => site.title));
    expect(titles).to.deep.equal([ ...titles ].sort((a, b) => a.localeCompare(b)));

    el.filtersOpen = true;
    await elementUpdated(el);
    expect(Array.from(el.querySelectorAll("#course-list-term-filter option"), option => option.value))
      .to.deep.equal([ "none", "summer" ]);
  });

  it("filters courses by term id", async () => {

    const el = await fixture(html`<sakai-course-list user-id="${data.userId}"></sakai-course-list>`);

    await waitUntil(() => el.sites?.length);

    el.filtersOpen = true;
    await elementUpdated(el);

    const termFilter = el.querySelector("#course-list-term-filter");
    termFilter.value = "summer";
    termFilter.dispatchEvent(new Event("change"));
    await elementUpdated(el);

    expect(el.querySelectorAll("sakai-course-card").length).to.equal(1);
    expect(el.querySelector("sakai-course-card").courseData.title).to.equal(data.courseList.sites[1].title);
  });

  it("shows empty filtered state when no sites match", async () => {

    const el = await fixture(html`<sakai-course-list user-id="${data.userId}"></sakai-course-list>`);

    await waitUntil(() => el.sites?.length);

    el.filtersOpen = true;
    await elementUpdated(el);

    const siteFilter = el.querySelector("select[aria-label='" + el._i18n.course_filter_label + "']");
    siteFilter.value = "active";
    siteFilter.dispatchEvent(new Event("change"));
    await elementUpdated(el);

    expect(el.querySelectorAll("sakai-course-card").length).to.equal(0);
    expect(el.querySelector(".sak-banner-info").innerHTML).to.contain(el._i18n.no_matching_sites);
  });
});
