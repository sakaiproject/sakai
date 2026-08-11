import "../sakai-rubric-student-button.js";
import "../sakai-rubric-student.js";
import "../sakai-rubrics-utils.js";
import * as data from "./data.js";
import { expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

const incompleteAssociationUrl = /\/rubric-associations\/tools\/(?:null|undefined)\/items\//;

describe("sakai-rubric-student-button tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.associationUrl, data.association)
      .get(data.rubric1Url, data.rubric1)
      .get(data.evaluationUrl, data.evaluation)
      .get(incompleteAssociationUrl, 204)
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
    document.querySelector("sakai-rubric-student[data-test-stray]")?.remove();
    fetchMock.hardReset();
  });

  async function openRubric(attributes = {}) {

    const el = await fixture(html`
      <sakai-rubric-student-button
          site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="${data.entityId}"
          evaluated-item-id="${data.evaluatedItemId}"
          evaluated-item-owner-id="${data.evaluatedItemOwnerId}"
          ?instructor=${attributes.instructor}
          ?is-peer-or-self=${attributes.isPeerOrSelf}>
      </sakai-rubric-student-button>
    `);

    await waitUntil(() => el.querySelector("a"), "Rubric button was not rendered");
    await waitUntil(() => window.top.rubrics.utils.lightbox, "Rubric modal was not initialized");
    el.querySelector("a").click();

    const rubric = window.top.rubrics.utils.getRubricElement();
    await waitUntil(() => rubric.querySelector(".rubric-details"), "Rubric was not rendered in the modal");
    return rubric;
  }

  it("keeps student and instructor views distinct through the modal", async () => {

    const studentRubric = await openRubric();
    expect(studentRubric.hasAttribute("instructor")).to.be.false;
    expect(studentRubric.querySelector("select")).to.not.exist;

    bootstrap.Modal.getInstance(window.top.rubrics.utils.lightbox).hide();
    await waitUntil(() => !studentRubric.hasAttribute("tool-id"), "Rubric modal was not reset");

    const instructorRubric = await openRubric({ instructor: true });
    expect(instructorRubric.hasAttribute("instructor")).to.be.true;
    expect(instructorRubric.querySelector("select")).to.exist;
  });

  it("uses the peer/self attribute and evaluation query consistently", async () => {

    const peerEvaluationUrl = `${data.evaluationUrl}?isPeer=true`;
    fetchMock.removeRoutes();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.associationUrl, data.association)
      .get(data.rubric1Url, data.rubric1)
      .get(data.evaluationUrl, data.evaluation)
      .get(peerEvaluationUrl, data.evaluation)
      .get(incompleteAssociationUrl, 204)
      .get("*", 500);

    const rubric = await openRubric({ isPeerOrSelf: true });

    expect(rubric.hasAttribute("is-peer-or-self")).to.be.true;
    expect(rubric.hasAttribute("peer-or-self")).to.be.false;
    expect(fetchMock.callHistory.called(peerEvaluationUrl)).to.be.true;
  });

  it("updates only the student component inside the rubric modal", async () => {

    const firstRubric = await openRubric();
    bootstrap.Modal.getInstance(window.top.rubrics.utils.lightbox).hide();
    await waitUntil(() => !firstRubric.hasAttribute("tool-id"), "Rubric modal was not reset");

    const strayRubric = document.createElement("sakai-rubric-student");
    strayRubric.setAttribute("data-test-stray", "");
    document.body.prepend(strayRubric);

    const rubric = await openRubric({ instructor: true });

    expect(rubric.hasAttribute("instructor")).to.be.true;
    expect(strayRubric.hasAttribute("instructor")).to.be.false;
    expect(strayRubric.hasAttribute("tool-id")).to.be.false;
  });

  it("registers the modal close handler only once", async () => {

    const utils = window.top.rubrics.utils;
    const originalCloseLightbox = utils.closeLightbox;
    let closeCount = 0;
    utils.closeLightbox = () => closeCount += 1;

    try {
      utils.initLightbox(data.i18n, data.siteId);
      utils.showRubric(data.rubric1.id);
      utils.showRubric(data.rubric1.id);
      await waitUntil(() => utils.getRubricElement().querySelector(".rubric-details"), "Preview rubric was not rendered");
      utils.lightbox.dispatchEvent(new Event("hidden.bs.modal"));
      expect(closeCount).to.equal(1);
    } finally {
      utils.closeLightbox = originalCloseLightbox;
    }
  });
});
