import "../sakai-rubric.js";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

window.sakai = window.sakai || {
  editor: {
    launch: () => ({ focus: () => "", on: () => "", setData: (data, callback) => "" })
  },
};

describe("sakai-rubrics tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.rubric1Url, data.rubric1)
      .patch(data.rubric1OwnerUrl, () => ({ ...data.rubric1, version: (data.rubric1.version ?? 0) + 1 }))
      .patch(data.rubric3OwnerUrl, () => ({ ...data.rubric3, version: (data.rubric3.version ?? 0) + 1 }))
      .get(data.associationUrl, data.association)
      .put(data.rubric4CriteriaSortUrl, () => ({ ...data.rubric4, version: (data.rubric4.version ?? 0) + 1 }))
      .patch(data.rubric4OwnerUrl, () => ({ ...data.rubric4, version: (data.rubric4.version ?? 0) + 1 }))
      .patch(data.rubric4Criteria5Url, 200)
      .patch(data.rubric4Criteria6Url, 200)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });


  it ("updating rubric title updates the UI in all appropriate places for an unlocked rubric", async () => {
    await checkRubricTitleChanges(data.rubric1);
  });

  it ("updating rubric title updates the UI in all appropriate places for a locked rubric", async () => {
    await checkRubricTitleChanges(data.rubric3);
  });

  it ("restores the grading mode and weights when the mode save fails", async () => {
    fetchMock.removeRoutes();
    let finishSave;
    fetchMock.get(data.i18nUrl, data.i18n).patch(data.rubric4OwnerUrl, () => new Promise(resolve => {
      finishSave = () => resolve(409);
    }));

    for (const weighted of [false, true]) {
      const rubric = structuredClone(data.rubric4);
      rubric.weighted = weighted;
      rubric.criteria.forEach((criterion, index) => criterion.weight = weighted ? [40, 60][index] : 0);
      const weights = rubric.criteria.map(criterion => criterion.weight);
      const el = await fixture(html`
        <sakai-rubric site-id="${data.siteId}" .rubric=${rubric}></sakai-rubric>
      `);
      await waitUntil(() => el.renderRoot.querySelector("button.weighted"));
      const button = el.renderRoot.querySelector("button.weighted");
      const label = button.title;
      button.click();
      await waitUntil(() => button.disabled, "Mode toggle was not disabled during the save");
      await waitUntil(() => finishSave, "Mode save did not start");
      button.click();
      el.weightedChange();
      finishSave();
      finishSave = null;
      await waitUntil(() => el.renderRoot.querySelector("[role=alert]"), "Mode save error was not displayed");
      await waitUntil(() => !button.disabled, "Mode toggle was not re-enabled after the save failed");
      expect(rubric.weighted).to.equal(weighted);
      expect(rubric.criteria.map(criterion => criterion.weight)).to.deep.equal(weights);
      expect(button.title).to.equal(label);
    }
    expect(fetchMock.callHistory.calls(data.rubric4OwnerUrl)).to.have.length(2);
  });

  it("refreshes the displayed maximum when switching grading modes", async () => {
    const rubric = structuredClone(data.rubric4);
    rubric.weighted = true;
    rubric.criteria[0].weight = 100;
    rubric.criteria[1].weight = 0;
    const el = await fixture(html`<sakai-rubric site-id="${data.siteId}" .rubric=${rubric}></sakai-rubric>`);
    await waitUntil(() => el.querySelector(".rubric-toggle"));
    el.querySelector(".rubric-toggle").click();
    await waitUntil(() => el.querySelector(".card"));
    expect(el.querySelector(".card > div:last-child > span:last-child").textContent.trim()).to.equal("2");

    el.querySelector("button.weighted").click();
    await waitUntil(
      () => el.querySelector(".rubric-totals")?.textContent.includes("4") && !el.querySelector("button.weighted").disabled,
      "Standard maximum did not refresh or mode save did not finish"
    );

    el.querySelector("button.weighted").click();
    await waitUntil(
      () => el.querySelector(".card > div:last-child > span:last-child")?.textContent.trim() === "2",
      "Weighted maximum did not refresh"
    );
    expect(el.querySelector(".card > div:last-child > span:last-child").textContent.trim()).to.equal("2");
  });

  /**
   * Perform a title update and make sure all places are changed
   **/
  async function checkRubricTitleChanges(rubricData) {
    let el = await fixture(html`
      <sakai-rubric site-id="${data.siteId}"
                    .rubric=${rubricData}
                    enable-pdf-export>
      </sakai-rubric>
    `);

    await waitUntil(() => el._i18n);
    //await el.updateComplete;
    await elementUpdated(el);

    // Validate that current data is the original title
    validateRubricTitle(rubricData, el, rubricData.title);

    const newTitle = 'UPDATED TITLE';
    const editElement = el.querySelector(`#rubric-edit-${rubricData.id}`);

    // Call update-rubric-title event
    editElement.dispatchEvent(new CustomEvent("update-rubric-title", { detail: newTitle }));

    await elementUpdated(editElement);
    await elementUpdated(el);

    await waitUntil(() => el._i18n);

    await waitUntil(() => el.querySelector(".rubric-name").textContent === newTitle);
    // Validate that current data is the updated title
    validateRubricTitle(rubricData, el, newTitle);

    fetchMock.removeRoutes();
    const url = `/api/sites/${rubricData.ownerId}/rubrics/${rubricData.id}`;
    fetchMock.patch(url, 409);
    editElement.querySelector("button.edit-button").click();
    await waitUntil(() => editElement.querySelector(".modal.show"));
    editElement.querySelector("input").value = "Unsaved concurrent title";
    editElement.querySelector("button.btn-primary").click();
    await waitUntil(() => editElement.querySelector("[role=alert]"));
    expect(editElement.querySelector(".modal.show")).to.exist;
    expect(editElement.querySelector("input").value).to.equal("Unsaved concurrent title");
    expect(fetchMock.callHistory.calls(url, { method: "PATCH" })).to.have.length(2);
    editElement.saved();
  }

  /**
   * Look for all places in the dom that should render any sort of rubric title
   **/
  function validateRubricTitle(rubricData, el, titleToCheck) {

    expect(el.querySelector(".rubric-name").textContent).to.equal(titleToCheck);
    expect(el.querySelector(".rubric-toggle").title).to.equal(`${el._i18n.toggle_details} ${titleToCheck}`);

    if (rubricData.locked) {
      elementChecks(el, "span.locked", titleToCheck);
    }

    elementChecks(el, "button.share", titleToCheck);
    elementChecks(el, "button.clone", titleToCheck);
    elementChecks(el, "button.edit-button", titleToCheck);
    elementChecks(el, "a.pdf", titleToCheck);

    if (!rubricData.locked) {
      elementChecks(el, `button[aria-controls="delete-rubric-${rubricData.id}"]`, titleToCheck);
    }
  }

  /**
   * Check that the element exists, the title matches, and the ariaLabel matches
   **/
  function elementChecks(el, elementSelector, titleToCheck) {
    expect(el.querySelector(elementSelector)).to.exist;
    expect(el.querySelector(elementSelector).title).to.contain(titleToCheck);
    expect(el.querySelector(elementSelector).ariaLabel).to.contain(titleToCheck);
  }
});
