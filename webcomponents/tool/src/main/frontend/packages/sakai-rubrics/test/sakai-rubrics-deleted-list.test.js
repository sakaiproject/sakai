import "../sakai-rubrics-deleted-list.js";
import * as data from "./data.js";
import { expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

describe("sakai-rubrics-deleted-list", () => {

  const deletedRubric = {
    ...data.rubric1,
    softRemovedDate: Date.now(),
    formattedSoftRemovedDate: "29 Oct 2026",
  };

  let deletedRubrics;
  let permanentDeleteResponse;

  beforeEach(() => {
    deletedRubrics = [ deletedRubric ];
    permanentDeleteResponse = 200;
    globalThis.bootstrap = {
      Modal: {
        getOrCreateInstance: () => ({
          show() {},
          hide() {},
        }),
      },
    };

    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.deletedRubricsUrl, () => deletedRubrics)
      .post(`/api/sites/${data.siteId}/rubrics/${deletedRubric.id}/restore`, 200)
      .delete(`/api/sites/${data.siteId}/rubrics/${deletedRubric.id}/permanent`, () => permanentDeleteResponse)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
    delete globalThis.bootstrap;
  });

  it("renders deleted rubrics with restore and permanent delete actions", async () => {

    const el = await fixture(html`
      <sakai-rubrics-deleted-list site-id="${data.siteId}"></sakai-rubrics-deleted-list>
    `);

    await waitUntil(() => el._rubrics?.length === 1);

    expect(el.querySelector(".rubric-name").textContent).to.equal(deletedRubric.title);
    expect(el.querySelector("button.restore-rubric")).to.exist;
    expect(el.querySelector("button.permanently-delete-rubric")).to.exist;
  });

  it("restores a deleted rubric", async () => {

    const el = await fixture(html`
      <sakai-rubrics-deleted-list site-id="${data.siteId}"></sakai-rubrics-deleted-list>
    `);

    await waitUntil(() => el._rubrics?.length === 1);

    let restored = false;
    el.addEventListener("rubric-restored", () => { restored = true; });

    el.querySelector("button.restore-rubric").click();
    await waitUntil(() => restored);
    expect(el._rubrics.length).to.equal(0);
  });

  it("permanently deletes a deleted rubric", async () => {

    const el = await fixture(html`
      <sakai-rubrics-deleted-list site-id="${data.siteId}"></sakai-rubrics-deleted-list>
    `);

    await waitUntil(() => el._rubrics?.length === 1);

    el.querySelector("button.permanently-delete-rubric").click();
    await el.updateComplete;

    el.querySelector("#permanent-delete-modal button.btn-danger").click();
    await waitUntil(() => el._rubrics?.length === 0);
    expect(fetchMock.callHistory.calls(
      `/api/sites/${data.siteId}/rubrics/${deletedRubric.id}/permanent`,
      { method: "DELETE" }
    )).to.have.length(1);
  });

  it("keeps a pending permanent deletion tied to the confirmed rubric", async () => {

    deletedRubrics.push({ ...data.rubric2, softRemovedDate: Date.now() });
    const pending = Promise.withResolvers();
    permanentDeleteResponse = pending.promise;

    const el = await fixture(html`
      <sakai-rubrics-deleted-list site-id="${data.siteId}"></sakai-rubrics-deleted-list>
    `);
    await waitUntil(() => el.querySelectorAll("button.permanently-delete-rubric").length === 2);

    const [ first, second ] = el.querySelectorAll("button.permanently-delete-rubric");
    first.click();
    await el.updateComplete;

    const confirm = el.querySelector("#permanent-delete-modal button.btn-danger");
    confirm.click();
    await el.updateComplete;
    expect(confirm.disabled).to.be.true;
    expect(second.disabled).to.be.true;
    second.click();
    confirm.click();

    pending.resolve(200);
    await waitUntil(() => el.querySelectorAll(".rubric-name").length === 1);
    expect(el.querySelector(".rubric-name").textContent).to.equal(data.rubric2.title);
    expect(fetchMock.callHistory.calls(
      `/api/sites/${data.siteId}/rubrics/${deletedRubric.id}/permanent`,
      { method: "DELETE" }
    )).to.have.length(1);
  });
});
