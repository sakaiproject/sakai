import "../sakai-rubric-grading.js";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";
window.sakai = window.sakai || {
  editor: {
    launch: () => ({ focus: () => "", on: () => "", setData: () => "" })
  },
};

describe("sakai-rubric-grading tests", () => {

  beforeEach(async () => {
    fetchMock.mockGlobal();
    fetchMock.get(data.i18nUrl, data.i18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it ("rubric grading renders correctly", async () => {

    fetchMock.get(data.rubric1Url, data.rubric1)
      .get(data.associationUrl, data.association)
      .get(data.evaluationUrl, data.evaluation)
      .post(`/api/sites/${data.siteId}/rubric-evaluations`, ({ url, options }) => {

        return Object.assign({
          id: "" + Math.floor(Math.random() * 20) + 1,
          creator: "adrian",
          created: Date.now(),
          creatorDisplayName: "Adrian Fish",
        }, JSON.parse(options.body));
      });

    const el = await fixture(html`
      <sakai-rubric-grading
          site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="${data.entityId}"
          evaluated-item-id="${data.evaluatedItemId}"
          evaluated-item-owner-id="${data.evaluatedItemOwnerId}"
          enable-pdf-export>
      </sakai-rubric-grading>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Some basic element checks
    expect(el.querySelector(".grading")).to.exist;
    expect(el.querySelector("sakai-rubric-pdf")).to.exist;
    expect(el.querySelector(".sakai-rubric-criteria-grading")).to.exist;

    // We need to wait until the evaluation data has been fetched, then count the rows.
    await waitUntil(() => el.querySelector(".criterion-row"), "No criterion rows rendered");
    expect(el.querySelectorAll(".criterion-row").length).to.equal(3);

    // Initially, with our test evaluation load, we have selected ratings worth 2 points for criterion1 and 3 points for criterion2
    const totalPoints = el.querySelector(`#rbcs-${data.evaluatedItemId}-${data.entityId}-totalpoints`);
    expect(totalPoints).to.exist;
    expect(totalPoints.value).to.equal("5");

    // Now, let's select rating 1, which should updated our total points to 1 and fire a
    // total-points-updated event.
    const ratingItem1 = el.querySelector("#rating-item-1");
    expect(ratingItem1).to.exist;
    const listener = oneEvent(el, "total-points-updated");
    ratingItem1.click();
    const { detail } = await listener;
    expect(detail.evaluatedItemId).to.equal(data.evaluatedItemId);
    expect(detail.entityId).to.equal(data.entityId);
    expect(detail.value).to.equal("4");
    await el.updateComplete;
    expect(totalPoints.value).to.equal("4");

    expect(el.querySelectorAll(".fine-tune-points").length).to.equal(2);
    const fineTuneInput = el.querySelector(".fine-tune-points");
    fineTuneInput.value = "1.8";
    fineTuneInput.dispatchEvent(new Event("input"));
    await el.updateComplete;
    // The total points should be 1.8 (criterion1) + 3 (criterion2) = 4.8
    expect(totalPoints.value).to.equal("4.8");
  });

  it ("calculates percentage totals correctly", async () => {

    fetchMock.get(data.rubric1Url, data.rubric1)
      .get(data.associationUrl, data.association)
      .get(data.evaluationUrl, data.evaluation);

    const el = await fixture(html`
      <sakai-rubric-grading
          site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="${data.entityId}"
          evaluated-item-id="${data.evaluatedItemId}"
          evaluated-item-owner-id="${data.evaluatedItemOwnerId}"
          enable-pdf-export
          total-as-percentage>
      </sakai-rubric-grading>
    `);

    await elementUpdated(el);

    // Wait until the criterion rows are rendered
    await waitUntil(() => el.querySelector(".criterion-row"), "No criterion rows rendered");

    // Check that there are three criterion rows (two for actual criteria and one for the criterion group)
    expect(el.querySelectorAll(".criterion-row").length).to.equal(3);

    // Check the points display for criterion1 (id: 1)
    const criterion1Points = el.querySelector("#points-display-1");
    expect(criterion1Points).to.exist;
    expect(criterion1Points.textContent.trim()).to.equal("2");

    // Check the points display for the second criterion (id: 3)
    const criterion2Points = el.querySelector("#points-display-3");
    expect(criterion2Points).to.exist;
    expect(criterion2Points.textContent.trim()).to.equal("3");

    // Calculate the percentage:
    // The maximum points for criterion1 is 2 (the highest rating)
    // The selected points for criterion1 is 2 (from the evaluation)
    // The maximum points for criterion2 is 3 (the highest rating)
    // The selected points for criterion2 is 3 (from the evaluation)
    // Total points: 2 + 3 = 5
    // Maximum possible points: 2 + 3 = 5
    // Percentage: (5/5) * 100 = 100%

    // Verify that the total percentage is 100%
    await waitUntil(() => el.querySelector("#sakai-rubrics-total-points"));
    expect(el.querySelector("#sakai-rubrics-total-points").textContent).to.contain("100 %");

    // Verify that the hidden input field for total points has the correct value (100)
    const totalPointsInput = el.querySelector(`#rbcs-${data.evaluatedItemId}-${data.entityId}-totalpoints`);
    expect(totalPointsInput).to.exist;
    expect(totalPointsInput.value).to.equal("100");
  });

  it ("calculates weighted rubric points correctly", async () => {

    fetchMock.get(data.weightedRubricUrl, data.weightedRubric)
      .get(data.weightedAssociationUrl, data.weightedAssociation)
      .get(data.weightedEvaluationUrl, data.weightedEvaluation);

    const el = await fixture(html`
      <sakai-rubric-grading
          site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="weighted-entity"
          evaluated-item-id="weighted-item"
          evaluated-item-owner-id="${data.evaluatedItemOwnerId}"
          enable-pdf-export>
      </sakai-rubric-grading>
    `);

    await elementUpdated(el);

    // Wait until the evaluation data has been fetched and rendered
    await waitUntil(() => el.querySelector(".criterion-row"), "No criterion rows rendered");

    // Verify that there are two criterion rows (one for each weighted criterion)
    expect(el.querySelectorAll(".criterion-row").length).to.equal(2);

    // Check that the total points are calculated correctly
    // For criterion 1 (weight 60%): 2 points * 0.6 = 1.2 points
    // For criterion 2 (weight 40%): 2 points * 0.4 = 0.8 points
    // Total: 1.2 + 0.8 = 2 points
    const totalPoints = el.querySelector("#rbcs-weighted-item-weighted-entity-totalpoints");
    expect(totalPoints).to.exist;
    expect(totalPoints.value).to.equal("2");

    // Verify that the weighted points are displayed correctly in the UI
    const criterion1Points = el.querySelector("#points-display-9");
    const criterion2Points = el.querySelector("#points-display-10");
    expect(criterion1Points.textContent.trim()).to.equal("1.2");
    expect(criterion2Points.textContent.trim()).to.equal("0.8");
  });
});
