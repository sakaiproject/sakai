import "../sakai-rubrics-shared-list.js";
import { expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";
import * as data from "./data.js";

describe("sakai-rubrics-shared-list", () => {

  beforeEach(() => fetchMock.mockGlobal());
  afterEach(() => fetchMock.hardReset());

  it("deletes a shared rubric using its owning site", async () => {
    const rubric = { ...data.rubric1, ownerId: "source-site", shared: true, locked: false, version: 1 };
    const url = `/api/sites/${rubric.ownerId}/rubrics/${rubric.id}`;
    fetchMock.get(data.i18nUrl, data.i18n)
      .get(data.sharedRubricsUrl, [ rubric ])
      .delete(url, 200);
    const el = await fixture(html`
      <sakai-rubrics-shared-list site-id="current-site" is-super-user></sakai-rubrics-shared-list>
    `);
    await waitUntil(() => el.renderRoot.querySelector("sakai-rubric-readonly"));
    const row = el.renderRoot.querySelector("sakai-rubric-readonly");
    await waitUntil(() => row.renderRoot.querySelector(".delete-rubric-button"));
    row.renderRoot.querySelector(".delete-rubric-button").click();
    await waitUntil(() => el.renderRoot.querySelector("#delete-modal .btn-danger"));
    el.renderRoot.querySelector("#delete-modal .btn-danger").click();
    await waitUntil(() => el._rubrics.length === 0);
    expect(fetchMock.callHistory.called(url, { method: "DELETE" })).to.be.true;
    expect(fetchMock.callHistory.called(`/api/sites/current-site/rubrics/${rubric.id}`)).to.be.false;
  });
});
