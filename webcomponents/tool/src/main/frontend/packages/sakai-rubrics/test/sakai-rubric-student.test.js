import "../sakai-rubric-student.js";
import "../sakai-rubrics-utils.js";
import * as data from "./data.js";
import { elementUpdated, expect, html, fixture, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

window.sakai = window.sakai || {
  editor: {
    launch: () => ({ focus: () => "", on: () => "" })
  },
};

describe("sakai-rubric-student tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.rubric1Url, data.rubric1)
      .get(data.associationUrl, data.association)
      .get(data.evaluationUrl, data.evaluation)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });


  it ("renders a rubric student correctly", async () => {

    const el = await fixture(html`
      <sakai-rubric-student site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="${data.entityId}"
          evaluated-item-id="${data.evaluatedItemId}"
          evaluated-item-owner-id="${data.evaluatedItemOwnerId}">
      </sakai-rubric-student>
    `);

    await el.updateComplete;

    await waitUntil(() => el.querySelector(".rubric-details"), "No .rubric-details created");
    expect(el.querySelector("sakai-rubric-criterion-preview")).to.not.exist;

    await el.updateComplete;
    expect(el.querySelector("sakai-rubric-criterion-student")).to.exist;
    expect(fetchMock.callHistory.calls(data.associationUrl)).to.have.length(1);

    await expect(el).to.be.accessible();
  });

  it ("rubric student preview renders correctly", async () => {

    const el = await fixture(html`
      <sakai-rubric-student site-id="${data.siteId}" rubric-id="1" preview="true"></sakai-rubric-student>
    `);

    await el.updateComplete;

    await expect(el).to.be.accessible();

    await waitUntil(() => el.querySelector("sakai-rubric-criterion-preview"), "No sakai-rubric-criterion-preview created");
  });

  it("ignores an obsolete association response after the entity changes", async () => {

    const newEntityId = "entity2";
    const newAssociationUrl = `/api/sites/${data.siteId}/rubric-associations/tools/${data.toolId}/items/${newEntityId}`;
    const rubric2Url = `/api/sites/${data.siteId}/rubrics/${data.rubric2.id}`;
    let resolveOldAssociation;
    const oldAssociation = new Promise(resolve => resolveOldAssociation = resolve);

    fetchMock.removeRoutes();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.associationUrl, oldAssociation)
      .get(newAssociationUrl, { ...data.association, rubricId: data.rubric2.id })
      .get(data.rubric1Url, data.rubric1)
      .get(rubric2Url, data.rubric2)
      .get("*", 500);

    const el = await fixture(html`
      <sakai-rubric-student site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="${data.entityId}">
      </sakai-rubric-student>
    `);

    await waitUntil(() => fetchMock.callHistory.called(data.associationUrl), "Initial association was not requested");
    el.setAttribute("entity-id", newEntityId);

    await waitUntil(() => el.querySelector("h3 span")?.textContent === data.rubric2.title, "New rubric was not rendered");
    resolveOldAssociation(data.association);
    await fetchMock.callHistory.flush(true);
    await elementUpdated(el);

    expect(el.querySelector("h3 span").textContent).to.equal(data.rubric2.title);
  });

  it("shows a self-report result without granting instructor summary views", async () => {

    const selfReportEntityId = "self-report";
    const selfReportItemId = "self-report-item";
    const selfReportOwnerId = "self-report-owner";
    const associationUrl = `/api/sites/${data.siteId}/rubric-associations/tools/${data.toolId}/items/${selfReportEntityId}`;
    const evaluationUrl = `/api/sites/${data.siteId}/rubric-evaluations/tools/${data.toolId}/items/${selfReportEntityId}/evaluations/${selfReportItemId}/owners/${selfReportOwnerId}?isPeer=true`;

    fetchMock.removeRoutes();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(associationUrl, {
        ...data.association,
        parameters: { hideStudentPreview: true },
      })
      .get(data.rubric1Url, data.rubric1)
      .get(evaluationUrl, data.evaluation)
      .get("*", 500);

    const el = await fixture(html`
      <sakai-rubric-student site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="${selfReportEntityId}"
          evaluated-item-id="${selfReportItemId}"
          evaluated-item-owner-id="${selfReportOwnerId}"
          student-self-report
          is-peer-or-self>
      </sakai-rubric-student>
    `);

    await waitUntil(() => el.querySelector("sakai-rubric-criterion-student"), "Self-report result was not rendered");

    expect(el.hasAttribute("instructor")).to.be.false;
    expect(el.querySelector("select")).to.not.exist;
  });
});
