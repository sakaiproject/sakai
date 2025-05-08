import "../sakai-rubric-grading.js";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

window.sakai = window.sakai || {
  editor: {
    launch: () => ({ focus: () => "", on: () => "", setData: (data, callback) => "" })
  },
};

describe("sakai-rubric-grading tests", () => {

  beforeEach(async () => {
    fetchMock.get(data.i18nUrl, data.i18n);
  });

  afterEach(() => {
    fetchMock.restore();
  });

  it ("rubric grading renders correctly", async () => {

    fetchMock.get(data.rubric1Url, data.rubric1)
      .get(data.associationUrl, data.association)
      .get(data.evaluationUrl, data.evaluation)
      .post(`/api/sites/${data.siteId}/rubric-evaluations`, (url, opts) => {

        return Object.assign({
          id: "" + Math.floor(Math.random() * 20) + 1,
          creator: "adrian",
          created: Date.now(),
          creatorDisplayName: "Adrian Fish",
        }, JSON.parse(opts.body));
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
    expect(el.querySelectorAll(".criterion-row").length).to.equal(2);

    // Initially, with our test evaluation load, we have selected one of the ratings worth 2 points
    const totalPoints = el.querySelector(`#rbcs-${data.evaluatedItemId}-${data.entityId}-totalpoints`);
    expect(totalPoints).to.exist;
    expect(totalPoints.value).to.equal("2");

    // Now, let's select rating 1, which should updated our total points to 1 and fire a
    // total-points-updated event.
    const ratingItem1 = el.querySelector("#rating-item-1");
    expect(ratingItem1).to.exist;
    const listener = oneEvent(el, "total-points-updated");
    ratingItem1.click();
    const { detail } = await listener;
    expect(detail.evaluatedItemId).to.equal(data.evaluatedItemId);
    expect(detail.entityId).to.equal(data.entityId);
    expect(detail.value).to.equal("1");
    await el.updateComplete;
    expect(totalPoints.value).to.equal("1");

    expect(el.querySelectorAll(".fine-tune-points").length).to.equal(1);
    const fineTuneInput = el.querySelector(".fine-tune-points");
    fineTuneInput.value = "1.8";
    fineTuneInput.dispatchEvent(new Event("input"));
    await el.updateComplete;
    expect(totalPoints.value).to.equal("1.8");
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

    await waitUntil(() => el.querySelector("#sakai-rubrics-total-points"));
    expect(el.querySelector("#sakai-rubrics-total-points").textContent).to.contain("66.67 %");
  });
});
