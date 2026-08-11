import "../sakai-rubric-student-button.js";
import "../sakai-rubric-student.js";
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

    document.querySelectorAll("sakai-rubric-modal").forEach(modal => modal.remove());
    document.querySelector("sakai-rubric-student[data-test-stray]")?.remove();
    fetchMock.hardReset();
  });

  async function openRubric({ clicks = 1, instructor = false } = {}) {

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
    for (let i = 0; i < clicks; i++) {
      el.querySelector("a").click();
    }

    await waitUntil(() => document.querySelector("sakai-rubric-modal")?.shadowRoot?.querySelector("sakai-rubric-student"), "Rubric modal was not opened");
    const modal = document.querySelector("sakai-rubric-modal");
    const rubric = modal.shadowRoot.querySelector("sakai-rubric-student");
    await waitUntil(() => rubric.querySelector(".rubric-details"), "Rubric was not rendered in the modal");
    return { modal, rubric };
  }

  it("creates isolated student and instructor modal sessions", async () => {

    const { modal: studentModal, rubric: studentRubric } = await openRubric();
    expect(studentRubric.instructor).to.be.false;
    expect(studentRubric.querySelector("select")).to.not.exist;
    expect(studentModal.ownerDocument).to.equal(document);
    expect(studentModal.shadowRoot).to.exist;
    expect(studentModal.querySelector("sakai-rubric-student")).to.not.exist;
    expect(studentModal.shadowRoot.getElementById("rubric-modal-title").textContent.trim()).to.equal("Grading Rubric");

    studentModal.close();
    await waitUntil(() => !studentModal.isConnected, "Rubric modal was not removed");
    await waitUntil(() => !studentRubric.isConnected, "Rubric modal session was not removed");
    await fetchMock.callHistory.flush(true);

    expect(fetchMock.callHistory.called(incompleteAssociationUrl)).to.be.false;

    const { rubric: instructorRubric } = await openRubric({ instructor: true });
    expect(instructorRubric).to.not.equal(studentRubric);
    expect(instructorRubric.instructor).to.be.true;
    expect(instructorRubric.querySelector("select")).to.exist;
  });

  it("updates only the student component inside the rubric modal", async () => {

    const { modal: firstModal, rubric: firstRubric } = await openRubric();
    firstModal.close();
    await waitUntil(() => !firstRubric.isConnected, "Rubric modal session was not removed");

    const strayRubric = document.createElement("sakai-rubric-student");
    strayRubric.setAttribute("data-test-stray", "");
    document.body.prepend(strayRubric);

    const { rubric } = await openRubric({ instructor: true });

    expect(rubric.instructor).to.be.true;
    expect(strayRubric.instructor).to.not.be.true;
    expect(strayRubric.toolId).to.not.exist;
  });

  it("coalesces rapid opens into one modal session", async () => {

    const { modal } = await openRubric({ clicks: 2 });

    expect(document.querySelectorAll("sakai-rubric-modal")).to.have.length(1);
    expect(modal.shadowRoot.querySelectorAll("sakai-rubric-student")).to.have.length(1);

    modal.close();
    await waitUntil(() => !modal.isConnected, "Rubric modal was not removed");
  });
});
