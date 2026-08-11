import "../sakai-rubric-student-button.js";
import "../sakai-rubric-student.js";
import "../sakai-rubrics-utils.js";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

describe("sakai-rubric-student-button state tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.associationUrl, data.association)
      .get(data.rubric1Url, data.rubric1)
      .get(data.evaluationUrl, data.evaluation)
      .get("*", 500);
  });

  afterEach(async () => {

    await fetchMock.callHistory.flush(true);

    const utils = window.top.rubrics.utils;
    const modal = utils.lightbox;
    if (modal) {
      bootstrap.Modal.getInstance(modal)?.dispose();
      modal.remove();
      utils.lightbox = null;
    }

    document.querySelectorAll(".modal-backdrop").forEach(backdrop => backdrop.remove());
    document.body.classList.remove("modal-open");
    document.body.style.removeProperty("overflow");
    document.body.style.removeProperty("padding-right");
    fetchMock.hardReset();
  });

  it("coalesces sequential attributes into one association request", async () => {

    const button = await fixture(html`
      <sakai-rubric-student-button
          site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="${data.entityId}"
          evaluated-item-id="${data.evaluatedItemId}"
          evaluated-item-owner-id="${data.evaluatedItemOwnerId}">
      </sakai-rubric-student-button>
    `);

    await waitUntil(() => button.querySelector("a"), "Rubric button was not rendered");

    expect(fetchMock.callHistory.calls(data.associationUrl)).to.have.length(1);
  });

  it("clears a previously visible button when the new association is hidden", async () => {

    const hiddenEntityId = "hidden-entity";
    const hiddenAssociationUrl = `/api/sites/${data.siteId}/rubric-associations/tools/${data.toolId}/items/${hiddenEntityId}`;

    fetchMock.removeRoutes();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.associationUrl, data.association)
      .get(hiddenAssociationUrl, {
        ...data.association,
        parameters: { hideStudentPreview: true },
      })
      .get("*", 500);

    const button = await fixture(html`
      <sakai-rubric-student-button
          site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="${data.entityId}">
      </sakai-rubric-student-button>
    `);

    await waitUntil(() => button.querySelector("a"), "Initial rubric button was not rendered");
    button.setAttribute("entity-id", hiddenEntityId);
    await waitUntil(() => fetchMock.callHistory.called(hiddenAssociationUrl), "Hidden association was not requested");
    await fetchMock.callHistory.flush(true);
    await elementUpdated(button);

    expect(button.querySelector("a")).to.not.exist;
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

    const button = await fixture(html`
      <sakai-rubric-student-button
          site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="${data.entityId}"
          force-preview>
      </sakai-rubric-student-button>
    `);

    await waitUntil(() => fetchMock.callHistory.called(data.associationUrl), "Initial association was not requested");
    button.setAttribute("entity-id", newEntityId);
    await waitUntil(() => button.querySelector("a"), "New association was not rendered");

    resolveOldAssociation(data.association);
    await fetchMock.callHistory.flush(true);
    button.querySelector("a").click();

    const rubric = window.top.rubrics.utils.lightbox.querySelector("sakai-rubric-student");
    await waitUntil(() => rubric.querySelector("h3 span"), "Preview rubric was not rendered");
    expect(rubric.querySelector("h3 span").textContent).to.equal(data.rubric2.title);
  });
});
