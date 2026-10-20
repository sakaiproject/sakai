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
      .post(`/api/sites/${data.siteId}/rubric-evaluations`, ({ options }) => {

        return { ...JSON.parse(options.body), id: "saved-evaluation", version: 0 };
      })
      .put(`/api/sites/${data.siteId}/rubric-evaluations/saved-evaluation`,
        ({ options }) => {
          const payload = JSON.parse(options.body);
          expect(payload.version).to.equal(0);
          return { ...payload, version: 1 };
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
    expect(el.querySelector(`select[aria-label="${el._i18n.rubric_view_selection_title}"]`)).to.exist;

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
    const fineTuneSaved = oneEvent(el, "rubric-ratings-changed");
    fineTuneInput.dispatchEvent(new Event("input"));
    await el.updateComplete;
    // The total points should be 1.8 (criterion1) + 3 (criterion2) = 4.8
    expect(totalPoints.value).to.equal("4.8");
    await fineTuneSaved;
  });

  it ("only marks changed fine-tuned points as adjusted", async () => {

    const saveUrl = `/api/sites/${data.siteId}/rubric-evaluations`;
    fetchMock.get(data.rubric1Url, data.rubric1)
      .get(data.associationUrl, data.association)
      .get(data.evaluationUrl, data.evaluation)
      .post(saveUrl, ({ options }) => JSON.parse(options.body));

    const el = await fixture(html`
      <sakai-rubric-grading
          site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="${data.entityId}"
          evaluated-item-id="${data.evaluatedItemId}"
          evaluated-item-owner-id="${data.evaluatedItemOwnerId}">
      </sakai-rubric-grading>
    `);

    await waitUntil(() => el.querySelector("#rating-item-1"), "No rating rendered");

    let saved = oneEvent(el, "rubric-ratings-changed");
    el.querySelector("#rating-item-1").click();
    await saved;

    let request = JSON.parse(fetchMock.callHistory.lastCall(saveUrl).options.body);
    expect(request.criterionOutcomes.every(outcome => !outcome.pointsAdjusted)).to.be.true;

    const fineTuneInput = el.querySelector(".fine-tune-points");
    fineTuneInput.value = "0";
    saved = oneEvent(el, "rubric-ratings-changed");
    fineTuneInput.dispatchEvent(new Event("input"));
    await saved;

    request = JSON.parse(fetchMock.callHistory.lastCall(saveUrl).options.body);
    expect(request.criterionOutcomes[0].points).to.equal(0);
    expect(request.criterionOutcomes[0].pointsAdjusted).to.be.true;

    fineTuneInput.value = "1";
    saved = oneEvent(el, "rubric-ratings-changed");
    fineTuneInput.dispatchEvent(new Event("input"));
    await saved;

    request = JSON.parse(fetchMock.callHistory.lastCall(saveUrl).options.body);
    expect(request.criterionOutcomes[0].points).to.equal(1);
    expect(request.criterionOutcomes[0].pointsAdjusted).to.be.false;

    fineTuneInput.value = "1.8";
    saved = oneEvent(el, "rubric-ratings-changed");
    fineTuneInput.dispatchEvent(new Event("input"));
    await saved;

    request = JSON.parse(fetchMock.callHistory.lastCall(saveUrl).options.body);
    expect(request.criterionOutcomes[0].points).to.equal(1.8);
    expect(request.criterionOutcomes[0].pointsAdjusted).to.be.true;
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

  it ("hides summary views for peer or self evaluation", async () => {

    fetchMock.get(data.rubric1Url, data.rubric1)
      .get(data.associationUrl, data.association)
      .get(`${data.evaluationUrl}?isPeer=true`, data.evaluation);

    const el = await fixture(html`
      <sakai-rubric-grading
          site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="${data.entityId}"
          evaluated-item-id="${data.evaluatedItemId}"
          evaluated-item-owner-id="${data.evaluatedItemOwnerId}"
          is-peer-or-self>
      </sakai-rubric-grading>
    `);

    await waitUntil(() => el.querySelector(".criterion-row"), "No criterion rows rendered");
    expect(el.querySelector(`select[aria-label="${el._i18n.rubric_view_selection_title}"]`)).to.not.exist;
  });
});


describe("deferred rubric grading", () => {
  const saveUrl = `/api/sites/${data.siteId}/rubric-evaluations`;

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock.get(data.i18nUrl, data.i18n)
      .get(data.rubric1Url, data.rubric1)
      .get(data.associationUrl, data.association);
  });

  afterEach(() => fetchMock.hardReset());

  async function grading(evaluation = data.evaluation) {
    fetchMock.get(data.evaluationUrl, evaluation);
    const el = await fixture(html`
      <sakai-rubric-grading
          site-id="${data.siteId}" tool-id="${data.toolId}"
          entity-id="${data.entityId}" evaluated-item-id="${data.evaluatedItemId}"
          evaluated-item-owner-id="${data.evaluatedItemOwnerId}" defer-save>
      </sakai-rubric-grading>
    `);
    await waitUntil(() => el.querySelector("#rating-item-1"));
    return el;
  }

  function delayedSave() {
    let finish;
    const response = new Promise(resolve => finish = resolve);
    const requests = [];
    fetchMock.post(saveUrl, ({ options }) => {
      requests.push(JSON.parse(options.body));
      return response;
    });
    return { requests, finish: () => finish({ ...requests[0], id: "saved-a" }) };
  }

  it("discards edits without deleting a previously saved draft", async () => {
    const el = await grading({ ...data.evaluation, id: "existing-draft", status: "DRAFT" });
    el.querySelector("#rating-item-1").click();
    expect(el.hasPendingChanges()).to.be.true;
    await el.cancel();
    await elementUpdated(el);
    expect(el.hasPendingChanges()).to.be.false;
    expect(el.querySelector("#sakai-rubrics-total-points").textContent).to.contain("5");
    expect(fetchMock.callHistory.calls(saveUrl)).to.have.length(0);
  });

  it("saves an unchanged released rubric as draft when explicitly saved", async () => {
    const requests = [];
    fetchMock.put(`${saveUrl}/returned`, ({ options }) => {
      requests.push(JSON.parse(options.body));
      return requests[0];
    });
    const el = await grading({ ...data.evaluation, id: "returned", status: "RETURNED" });
    await el.save();
    expect(requests).to.have.length(1);
    expect(requests[0].status).to.equal(1);
    expect(requests[0].criterionOutcomes[0].points).to.equal(2);
  });

  it("keeps edits made during Save pending until explicitly saved", async () => {
    const server = delayedSave();
    const el = await grading();
    el.querySelector("#rating-item-1").click();
    const saving = el.save();
    await waitUntil(() => server.requests.length === 1);
    el.querySelector("#rating-item-2").click();
    server.finish();
    await saving;
    expect(el.hasPendingChanges()).to.be.true;
    expect(server.requests).to.have.length(1);
    expect(server.requests[0].criterionOutcomes[0].selectedRatingId).to.equal(1);
  });

  it("serializes Save and Release using the newly created evaluation id", async () => {
    const server = delayedSave();
    const releases = [];
    fetchMock.put(`${saveUrl}/saved-a`, ({ options }) => {
      releases.push(JSON.parse(options.body));
      return releases[0];
    });
    const el = await grading();
    el.querySelector("#rating-item-1").click();
    const saving = el.save();
    await waitUntil(() => server.requests.length === 1);
    const releasing = el.release();
    server.finish();
    await Promise.all([ saving, releasing ]);
    expect(server.requests[0].status).to.equal(1);
    expect(releases).to.have.length(1);
    expect(releases[0].status).to.equal(2);
    expect(el.hasPendingChanges()).to.be.false;
  });

  it("waits for an explicit Save before canceling subsequent edits", async () => {
    const server = delayedSave();
    const el = await grading();
    el.querySelector("#rating-item-1").click();
    const saving = el.save();
    await waitUntil(() => server.requests.length === 1);
    el.querySelector("#rating-item-2").click();
    let canceled = false;
    const canceling = el.cancel().then(() => canceled = true);
    await Promise.resolve();
    expect(canceled).to.be.false;
    server.finish();
    await Promise.all([ saving, canceling ]);
    await elementUpdated(el);
    expect(el.hasPendingChanges()).to.be.false;
    expect(el.querySelector("#sakai-rubrics-total-points").textContent).to.contain("4");
    expect(server.requests).to.have.length(1);
  });

  it("retains edits and propagates a failed save so it can be retried", async () => {
    fetchMock.post(saveUrl, 409, { repeat: 1 });
    const el = await grading();
    el.querySelector("#rating-item-1").click();
    let failure;
    try {
      await el.save();
    } catch (error) {
      failure = error;
    }
    expect(failure).to.be.instanceOf(Error);
    expect(failure.status).to.equal(409);
    await elementUpdated(el);
    expect(el.renderRoot.querySelector("[role=alert]")).to.exist;
    expect(fetchMock.callHistory.calls(saveUrl, { method: "POST" })).to.have.length(1);
    expect(el.hasPendingChanges()).to.be.true;
    fetchMock.removeRoutes();
    fetchMock.post(saveUrl, ({ options }) => ({ ...JSON.parse(options.body), id: "retry" }));
    await el.save();
    expect(el.hasPendingChanges()).to.be.false;
  });

  it("ignores an old student's save response after switching students", async () => {
    const server = delayedSave();
    const nextUrl = data.evaluationUrl.replace(data.evaluatedItemId, "student-b");
    fetchMock.get(nextUrl, { ...data.evaluation, id: "saved-b" });
    const requestsForB = [];
    fetchMock.put(`${saveUrl}/saved-b`, ({ options }) => {
      requestsForB.push(JSON.parse(options.body));
      return requestsForB[0];
    });
    const el = await grading();
    el.querySelector("#rating-item-1").click();
    const saving = el.save();
    await waitUntil(() => server.requests.length === 1);
    el.setAttribute("evaluated-item-id", "student-b");
    await waitUntil(() => fetchMock.callHistory.calls(nextUrl).length > 0);
    await waitUntil(() => el.querySelector("#rbcs-student-b-entity1-totalpoints")?.value === "5");
    server.finish();
    await saving;
    el.querySelector("#rating-item-1").click();
    await el.save();
    expect(requestsForB).to.have.length(1);
    expect(requestsForB[0].evaluatedItemId).to.equal("student-b");
    expect(requestsForB[0].id).to.equal("saved-b");
  });
});
