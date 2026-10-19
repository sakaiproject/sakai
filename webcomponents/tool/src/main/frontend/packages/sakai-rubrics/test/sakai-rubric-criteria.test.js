import "../sakai-rubric.js";
import "../sakai-rubric-criteria.js";
import "../sakai-rubric-criterion-edit.js";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

window.sakai = window.sakai || {
  editor: {
    launch: () => ({ focus: () => "", on: () => "", setData: (data, callback) => "" })
  },
};

describe("sakai-rubric-criteria tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .put(data.rubric4CriteriaSortUrl, () => ({ ...data.rubric4, version: (data.rubric4.version ?? 0) + 1 }))
      .patch(data.rubric4OwnerUrl, () => ({ ...data.rubric4, version: (data.rubric4.version ?? 0) + 1 }))
      .patch(data.rubric4Criteria5Url, 200)
      .patch(data.rubric4Criteria6Url, 200)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });


  it ("validates rating points with and without a configured range", async () => {

    const rubric = structuredClone(data.rubric4);
    rubric.criteria[1].pointrange = { low: 0, high: 2 };
    const el = await fixture(html`
      <sakai-rubric site-id="${data.siteId}" .rubric=${rubric}></sakai-rubric>
    `);

    await waitUntil(() => el.querySelector(".rubric-toggle"));
    el.querySelector(".rubric-toggle").click();
    await waitUntil(() => el.querySelectorAll("sakai-rubric-criterion-rating-edit button").length);

    for (const criterion of rubric.criteria) {
      const rating = criterion.ratings[0];
      const url = `/api/sites/${rubric.ownerId}/rubrics/${rubric.id}/criteria/${criterion.id}/ratings/${rating.id}`;
      fetchMock.post(url, ({ options }) => ({
        ...JSON.parse(options.body),
        version: (rating.version ?? 0) + 1,
        rubricVersion: (rubric.version ?? 0) + 1,
      }));
      const editor = el.querySelector(`[data-rating-id="${rating.id}"] sakai-rubric-criterion-rating-edit`);
      const modal = editor.querySelector(".modal");
      const shown = oneEvent(modal, "shown.bs.modal");
      editor.querySelector("button[data-bs-toggle=modal]").click();
      await shown;

      const input = editor.querySelector("input[type=number]");
      const save = modal.querySelector("button.btn-primary");
      input.value = "";
      save.click();
      expect(input.validity.valid).to.be.false;
      expect(fetchMock.callHistory.calls(url)).to.have.length(0);
      expect(modal.classList.contains("show")).to.be.true;

      if (criterion.pointrange) {
        input.value = "3";
        save.click();
        expect(input.validity.rangeOverflow).to.be.true;
        expect(fetchMock.callHistory.calls(url)).to.have.length(0);
        expect(modal.classList.contains("show")).to.be.true;
      }

      const points = criterion.pointrange ? 1.5 : 2.5;
      input.value = String(points);
      const hidden = oneEvent(modal, "hidden.bs.modal");
      save.click();
      expect(input.validity.valid).to.be.true;
      await hidden;
      await waitUntil(() => fetchMock.callHistory.calls(url).length === 1);
      const saved = JSON.parse(fetchMock.callHistory.lastCall(url).options.body);
      expect(Number(saved.points)).to.equal(points);
    }
  });

  for (const { draft, weights, canSave } of [
    { draft: true, weights: [20, 30], canSave: true },
    { draft: false, weights: [20, 30], canSave: false },
    { draft: true, weights: [40, 60], canSave: true },
    { draft: false, weights: [40, 60], canSave: true },
  ]) {
    it (`${draft ? "draft" : "published"} weights ${weights.join(" + ")} ${canSave ? "can" : "cannot"} be saved`, async () => {
      const rubric = structuredClone(data.rubric4);
      rubric.version = 0;
      rubric.draft = draft;
      rubric.weighted = true;
      rubric.criteria.forEach((criterion, index) => criterion.weight = weights[index]);
      const el = await fixture(html`
        <sakai-rubric site-id="${data.siteId}" .rubric=${rubric}></sakai-rubric>
      `);
      await waitUntil(() => el.renderRoot.querySelector(".rubric-toggle"));
      el.renderRoot.querySelector(".rubric-toggle").click();
      await waitUntil(() => el.renderRoot.querySelector(".save-weights"));

      const criteria = el.renderRoot.querySelector("sakai-rubric-criteria");
      const save = criteria.renderRoot.querySelector(".save-weights");
      expect(save.disabled).to.equal(!canSave);
      if (draft && weights.reduce((total, weight) => total + weight, 0) !== 100) {
        const publish = el.renderRoot.querySelector("button.draft");
        expect(publish.classList.contains("disabled")).to.be.true;
        publish.click();
        expect(rubric.draft).to.be.true;
        expect(fetchMock.callHistory.calls(data.rubric4OwnerUrl)).to.have.length(0);
      }

      save.click();
      // Also exercise the handler guard before the pending save completes.
      criteria.saveWeights();
      if (canSave) {
        await waitUntil(() => !el.renderRoot.querySelector(".sak-banner-success").classList.contains("d-none"));
      }
      const calls = fetchMock.callHistory.calls(data.rubric4OwnerUrl);
      expect(calls).to.have.length(canSave ? 1 : 0);
      if (canSave) {
        expect(new Headers(calls[0].options.headers).get("If-Match")).to.equal('"0"');
        expect(JSON.parse(calls[0].options.body)).to.deep.equal(rubric.criteria.flatMap((criterion, index) => [
          { op: "test", path: `/criteria/${index}/id`, value: criterion.id },
          { op: "replace", path: `/criteria/${index}/weight`, value: criterion.weight },
        ]));
      }
    });
  }

  it ("Criterion reorder, then mark as draft", async () => {

    const el = await fixture(html`
      <sakai-rubric site-id="${data.siteId}"
                    .rubric=${data.rubric4}
                    enable-pdf-export>
      </sakai-rubric>
    `);

    await waitUntil(() => el._i18n);
    await el.updateComplete;

    el.querySelector(".rubric-toggle").click();

    await el.updateComplete;

    // Check initial ordering (should be id 5 then 6)
    let reorderableRows = el.querySelectorAll("div.criterion-row");
    expect(reorderableRows[0].getAttribute('data-criterion-id')).to.equal(String(data.criteria3[0].id));
    expect(reorderableRows[1].getAttribute('data-criterion-id')).to.equal(String(data.criteria3[1].id));

    // Reorder criteria
    let eventData = { detail: { reorderedIds: [ data.criteria3[1].id, data.criteria3[0].id],
                                        data: {'criterionId': data.criteria3[1].id, 'reorderableId': data.criteria3[1].id} }
    };

    let reorderer = el.querySelector("sakai-reorderer[drop-class='criterion-row']");
    reorderer.dispatchEvent(new CustomEvent("reordered", eventData));

    await reorderer.updateComplete;

    // Check new ordering (should be 6 then 5)
    reorderableRows = el.querySelectorAll("div.criterion-row");
    expect(reorderableRows[0].getAttribute('data-criterion-id')).to.equal(String(data.criteria3[1].id));
    expect(reorderableRows[1].getAttribute('data-criterion-id')).to.equal(String(data.criteria3[0].id));

    // Mark rubric as draft
    el.querySelectorAll("button.draft")[0].click();
    await reorderer.updateComplete;

    // Verify criteria are still in correct (new) order
    reorderableRows = el.querySelectorAll("div.criterion-row");
    expect(reorderableRows[0].getAttribute('data-criterion-id')).to.equal(String(data.criteria3[1].id));
    expect(reorderableRows[1].getAttribute('data-criterion-id')).to.equal(String(data.criteria3[0].id));
  });
});
