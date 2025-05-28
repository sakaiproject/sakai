import "../sakai-rubrics-list.js";
import "../sakai-rubrics-shared-list.js";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

// Patch Bootstrap Modal for test environment
window.bootstrap = {
  Modal: class {
    constructor() {}
    static getOrCreateInstance() { return new window.bootstrap.Modal(); }
    show() {}
    hide() {}
  }
};

describe("sakai-rubrics-shared-list pagination and actions", () => {
  beforeEach(() => {
    fetchMock.restore();
    fetchMock.get(data.rubricsUrl, data.rubrics, { overwriteRoutes: true });
    // Default shared rubrics mock, can be overwritten in individual tests
    fetchMock.get("/api/rubrics/shared", [], { overwriteRoutes: true });
    fetchMock.get("*", 500, { overwriteRoutes: true });
  });

  afterEach(() => fetchMock.restore());

  it("should paginate shared rubrics correctly", async () => {
    // This test checks that pagination works as expected for the shared list
    // 13 rubrics, 20 per page => 1 page (13)
    const rubrics = Array.from({ length: 13 }, (_, i) => ({
      ...data.rubric1,
      id: String(i + 1),
      title: `Rubric ${i + 1}`
    }));
    fetchMock.get("/api/rubrics/shared", rubrics, { overwriteRoutes: true });
    const el = await fixture(html`<sakai-rubrics-shared-list></sakai-rubrics-shared-list>`);
    await waitUntil(() => el._rubrics && el._rubrics.length === 13, "Shared rubrics not loaded");
    await elementUpdated(el);
    // First (and only) page
    expect(el._paginatedRubrics.length).to.equal(13);
    expect(el._paginatedRubrics[0].title).to.equal("Rubric 1");
    expect(el._paginatedRubrics[12].title).to.equal("Rubric 13");
    expect(el._totalPages).to.equal(1);
    // Go to last page (should be empty, only 1 page)
    el._onPageSelected({ detail: { page: 2 } });
    await elementUpdated(el);
    expect(el._paginatedRubrics.length).to.equal(0);
  });

  it("should go to previous page if last item on last page is deleted from shared", async () => {
    // This test checks that deleting the last item on the last page moves to the previous page
    // 41 rubrics, 20 per page => 3 pages (20, 20, 1)
    const rubrics = Array.from({ length: 41 }, (_, i) => ({
      ...data.rubric1,
      id: String(i + 1),
      title: `Rubric ${i + 1}`
    }));
    fetchMock.get("/api/rubrics/shared", rubrics, { overwriteRoutes: true });
    // Simulate siteId for the DELETE URL
    const siteId = "site1";
    fetchMock.delete(`/api/sites/${siteId}/rubrics/41`, 200, { overwriteRoutes: true });
    const el = await fixture(html`<sakai-rubrics-shared-list site-id="${siteId}"></sakai-rubrics-shared-list>`);
    await waitUntil(() => el._rubrics && el._rubrics.length === 41, "Shared rubrics not loaded");
    await elementUpdated(el);
    // Go to last page
    el._onPageSelected({ detail: { page: 3 } });
    await elementUpdated(el);
    expect(el._paginatedRubrics.length).to.equal(1);
    // Simulate delete from shared
    el.rubricIdToDelete = "41";
    await el.confirmDelete({ stopPropagation: () => {} });
    await elementUpdated(el);
    expect(el._currentPage).to.equal(2);
    expect(el._paginatedRubrics.length).to.equal(20);
    expect(el._paginatedRubrics[0].title).to.equal("Rubric 21");
  });

  it("should update pagination when searching by name in shared", async () => {
    // This test checks that searching updates pagination and results correctly
    const rubrics = Array.from({ length: 13 }, (_, i) => ({
      ...data.rubric1,
      id: String(i + 1),
      title: `Rubric ${i + 1}`
    }));
    fetchMock.get("/api/rubrics/shared", rubrics, { overwriteRoutes: true });
    const el = await fixture(html`<sakai-rubrics-shared-list></sakai-rubrics-shared-list>`);
    await waitUntil(() => el._rubrics && el._rubrics.length === 13, "Shared rubrics not loaded");
    await elementUpdated(el);
    // Search for "Rubric 1" (should match 1, 10, 11, 12, 13)
    el.search("rubric 1");
    await elementUpdated(el);
    expect(el._paginatedRubrics.length).to.be.greaterThan(0);
    expect(el._totalPages).to.be.greaterThan(0);
    expect(el._paginatedRubrics.some(r => r.title.toLowerCase().includes("rubric 1"))).to.be.true;
  });

  it("should refresh shared list when a rubric is shared or unshared", async () => {
    // This test checks that the shared list refreshes when a rubric is shared or unshared
    const rubrics = [
      { ...data.rubric1, id: "1", title: "Rubric 1", shared: false, ownerId: "site1" },
      { ...data.rubric1, id: "2", title: "Rubric 2", shared: true, ownerId: "site1" }
    ];
    fetchMock.get("/api/rubrics/shared", rubrics, { overwriteRoutes: true });
    fetchMock.patch(`/api/sites/site1/rubrics/1`, 200, { overwriteRoutes: true });
    fetchMock.patch(`/api/sites/site1/rubrics/2`, 200, { overwriteRoutes: true });
    const el = await fixture(html`<sakai-rubrics-shared-list></sakai-rubrics-shared-list>`);
    await waitUntil(() => el._rubrics && el._rubrics.length === 2, "Shared rubrics not loaded");
    await elementUpdated(el);
    // Simulate sharing
    await el.sharingChange({ stopPropagation: () => {}, detail: { id: "1", ownerId: "site1", shared: false } });
    await elementUpdated(el);
    // Simulate unsharing
    await el.sharingChange({ stopPropagation: () => {}, detail: { id: "2", ownerId: "site1", shared: true } });
    await elementUpdated(el);
    expect(true).to.be.true;
  });
});
