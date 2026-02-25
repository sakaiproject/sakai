import "../sakai-rubrics-list.js";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

describe("sakai-rubrics-list pagination", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.rubricsUrl, data.rubrics)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  function setRoutes(rubrics) {
    fetchMock.removeRoutes();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.rubricsUrl, rubrics)
      .get("*", 500);
  }

  it("should paginate rubrics correctly", async () => {
    // This test checks that pagination works as expected for the main rubrics list
    // Create 25 rubrics for pagination
    const rubrics = Array.from({ length: 25 }, (_, i) => ({
      ...data.rubric1,
      id: String(i + 1),
      title: `Rubric ${i + 1}`
    }));
    setRoutes(rubrics);
    const el = await fixture(html`<sakai-rubrics-list site-id="${data.siteId}"></sakai-rubrics-list>`);
    await waitUntil(() => el._rubrics?.length === rubrics.length, "Rubrics not loaded");
    await elementUpdated(el);
    // Should show first 20 rubrics on first page
    expect(el._paginatedRubrics.length).to.equal(20);
    expect(el._paginatedRubrics[0].title).to.equal(rubrics[0].title);
    expect(el._paginatedRubrics[19].title).to.equal(rubrics[19].title);
    expect(el._totalPages).to.equal(2);
    // Go to next page
    el._onPageSelected({ detail: { page: 2 } });
    await elementUpdated(el);
    expect(el._paginatedRubrics.length).to.equal(5);
    expect(el._paginatedRubrics[0].title).to.equal(rubrics[20].title);
    expect(el._paginatedRubrics[4].title).to.equal(rubrics[24].title);
  });

  it("should go to previous page if last item on last page is deleted", async () => {
    // This test checks that deleting the last item on the last page moves to the previous page
    // 41 rubrics, 20 per page => 3 pages (20, 20, 1)
    const rubrics = Array.from({ length: 41 }, (_, i) => ({
      ...data.rubric1,
      id: String(i + 1),
      title: `Rubric ${i + 1}`
    }));
    setRoutes(rubrics);
    const el = await fixture(html`<sakai-rubrics-list site-id="${data.siteId}"></sakai-rubrics-list>`);
    await waitUntil(() => el._rubrics?.length === rubrics.length, "Rubrics not loaded");
    await elementUpdated(el);
    // Go to last page
    el._onPageSelected({ detail: { page: 3 } });
    await elementUpdated(el);
    expect(el._paginatedRubrics.length).to.equal(1);
    // Simulate delete
    el._rubricDeleted({ detail: { id: "41" }, stopPropagation: () => {} });
    await elementUpdated(el);
    // Should go to previous page (page 2)
    expect(el._currentPage).to.equal(2);
    expect(el._paginatedRubrics.length).to.equal(20);
    expect(el._paginatedRubrics[0].title).to.equal("Rubric 21");
  });

  it("should update pagination when searching by name", async () => {
    // This test checks that searching updates pagination and results correctly
    const rubrics = Array.from({ length: 25 }, (_, i) => ({
      ...data.rubric1,
      id: String(i + 1),
      title: `Rubric ${i + 1}`
    }));
    setRoutes(rubrics);
    const el = await fixture(html`<sakai-rubrics-list site-id="${data.siteId}"></sakai-rubrics-list>`);
    await waitUntil(() => el._rubrics && el._rubrics.length === 25, "Rubrics not loaded");
    await elementUpdated(el);
    // Search for "Rubric 2" (should match 11: 2, 12, 20-25)
    el.search("rubric 2");
    await elementUpdated(el);
    expect(el._paginatedRubrics.length).to.be.greaterThan(0);
    expect(el._totalPages).to.be.greaterThan(0);
    expect(el._paginatedRubrics.some(r => r.title.toLowerCase().includes("rubric 2"))).to.be.true;
  });

  it("should go to last page when a new rubric is created and paginated there", async () => {
    // This test checks that after creating a new rubric, pagination jumps to the last page where the new rubric appears
    const rubrics = Array.from({ length: 25 }, (_, i) => ({
      ...data.rubric1,
      id: String(i + 1),
      title: `Rubric ${i + 1}`
    }));
    setRoutes(rubrics);
    const el = await fixture(html`<sakai-rubrics-list site-id="${data.siteId}"></sakai-rubrics-list>`);
    await waitUntil(() => el._rubrics && el._rubrics.length === 25, "Rubrics not loaded");
    await elementUpdated(el);
    // Simulate create
    const newRubric = { ...data.rubric1, id: "26", title: "Rubric 26" };
    el.createRubricResponse(newRubric);
    await elementUpdated(el);
    expect(el._currentPage).to.equal(2);
    expect(el._paginatedRubrics.some(r => r.id === "26")).to.be.true;
  });

  it("should go to last page when a rubric is duplicated and paginated there", async () => {
    // This test checks that after duplicating a rubric, pagination jumps to the last page where the duplicated rubric appears
    const rubrics = Array.from({ length: 25 }, (_, i) => ({
      ...data.rubric1,
      id: String(i + 1),
      title: `Rubric ${i + 1}`
    }));
    setRoutes(rubrics);
    const el = await fixture(html`<sakai-rubrics-list site-id="${data.siteId}"></sakai-rubrics-list>`);
    await waitUntil(() => el._rubrics && el._rubrics.length === 25, "Rubrics not loaded");
    await elementUpdated(el);
    // Simulate duplicate
    const duplicated = { ...data.rubric1, id: "27", title: "Rubric 27" };
    el.createRubricResponse(duplicated);
    await elementUpdated(el);
    expect(el._currentPage).to.equal(2);
    expect(el._paginatedRubrics.some(r => r.id === "27")).to.be.true;
  });
});
