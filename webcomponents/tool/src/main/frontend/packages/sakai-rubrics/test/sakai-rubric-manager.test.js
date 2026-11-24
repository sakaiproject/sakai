import "../sakai-rubrics-manager.js";
import * as data from "./data.js";
import { expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

describe("sakai-rubric-manager tests", () => {

  let sharedRubrics;
  let deletedRubrics;

  beforeEach(() => {
    sharedRubrics = data.sharedRubrics;
    deletedRubrics = data.deletedRubrics;
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.sharedRubricsUrl, () => sharedRubrics)
      .get(data.rubricsUrl, data.rubrics)
      .get(data.deletedRubricsUrl, () => deletedRubrics)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });


  it ("manager renders correctly", async () => {

    let el = await fixture(html`
      <sakai-rubrics-manager site-id="${data.siteId}"></sakai-rubrics-manager>
    `);

    await waitUntil(() => el._i18n);

    expect(el.querySelector("h1:first-child")).to.exist;
    expect(el.querySelector("h1:first-child").innerHTML).to.contain(el._i18n.manage_rubrics);
    expect(el.querySelector("#deleted_rubrics")).to.exist;
    expect(el.querySelector("sakai-rubrics-deleted-list")).to.exist;
  });

  it("refreshes Public Rubrics after restoring a shared rubric", async () => {

    deletedRubrics = [ { ...data.rubric1, softRemovedDate: 1 } ];
    fetchMock.post(`/api/sites/${data.siteId}/rubrics/${data.rubric1.id}/restore`, () => {
      deletedRubrics = [];
      sharedRubrics = [ { ...data.rubric1, shared: true } ];
      return 200;
    });

    const manager = await fixture(html`
      <sakai-rubrics-manager site-id="${data.siteId}"></sakai-rubrics-manager>
    `);
    await waitUntil(() => manager.querySelector("button.restore-rubric"));
    expect(manager.querySelector("#shared_rubrics sakai-rubric-readonly")).to.be.null;

    manager.querySelector("button.restore-rubric").click();
    await waitUntil(() => manager.querySelector("#shared_rubrics sakai-rubric-readonly .rubric-name"));
    expect(manager.querySelector("#shared_rubrics .rubric-name").textContent).to.contain(data.rubric1.title);
  });
});
