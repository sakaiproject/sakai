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
      .get("*", 500);
  });

  afterEach(async () => {

    await fetchMock.callHistory.flush(true);

    const utils = window.rubrics.utils;
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

  async function openRubric({ instructor = false } = {}) {

    const el = await fixture(html`
      <sakai-rubric-student-button
          site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="${data.entityId}"
          evaluated-item-id="${data.evaluatedItemId}"
          evaluated-item-owner-id="${data.evaluatedItemOwnerId}"
          ?instructor=${instructor}>
      </sakai-rubric-student-button>
    `);

    await waitUntil(() => el.querySelector("a"), "Rubric button was not rendered");
    await waitUntil(() => window.rubrics.utils.lightbox, "Rubric modal was not initialized");
    el.querySelector("a").click();

    const rubric = window.rubrics.utils.getRubricElement();
    await waitUntil(() => rubric.querySelector(".rubric-details"), "Rubric was not rendered in the modal");
    return rubric;
  }

  it("creates isolated student and instructor modal sessions", async () => {

    const studentRubric = await openRubric();
    expect(studentRubric.instructor).to.be.false;
    expect(studentRubric.querySelector("select")).to.not.exist;
    expect(window.rubrics.utils.lightbox.ownerDocument).to.equal(document);

    bootstrap.Modal.getInstance(window.rubrics.utils.lightbox).hide();
    await waitUntil(() => !studentRubric.isConnected, "Rubric modal session was not removed");
    await fetchMock.callHistory.flush(true);

    expect(fetchMock.callHistory.called(incompleteAssociationUrl)).to.be.false;

    const instructorRubric = await openRubric({ instructor: true });
    expect(instructorRubric).to.not.equal(studentRubric);
    expect(instructorRubric.instructor).to.be.true;
    expect(instructorRubric.querySelector("select")).to.exist;
  });

  it("updates only the student component inside the rubric modal", async () => {

    const firstRubric = await openRubric();
    bootstrap.Modal.getInstance(window.rubrics.utils.lightbox).hide();
    await waitUntil(() => !firstRubric.isConnected, "Rubric modal session was not removed");

    const strayRubric = document.createElement("sakai-rubric-student");
    strayRubric.setAttribute("data-test-stray", "");
    document.body.prepend(strayRubric);

    const rubric = await openRubric({ instructor: true });

    expect(rubric.instructor).to.be.true;
    expect(strayRubric.instructor).to.not.be.true;
    expect(strayRubric.toolId).to.not.exist;
  });
});
