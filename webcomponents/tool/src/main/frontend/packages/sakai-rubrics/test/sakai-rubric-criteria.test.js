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


  for (const locale of [ "en-US", "es-ES", "ar-EG" ]) {
    it (`validates rating points with and without a configured range (${locale})`, async () => {

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

        editor.locale = locale;
        editor.requestUpdate();
        await editor.updateComplete;
        const input = editor.renderRoot.querySelector("input[name=quantity]");
        const save = modal.querySelector("button.btn-primary");
        // Existing integer ratings must remain editable with ASCII digits in every locale.
        expect(input.value).to.equal(String(rating.points));
        input.value = "";
        save.click();
        expect(input.validity.valid).to.be.false;
        expect(fetchMock.callHistory.calls(url)).to.have.length(0);
        expect(modal.classList.contains("show")).to.be.true;

        if (criterion.pointrange) {
          for (const outsideRange of [ "-1", "3" ]) {
            input.value = outsideRange;
            save.click();
            expect(input.validity.customError).to.be.true;
            expect(fetchMock.callHistory.calls(url)).to.have.length(0);
            expect(modal.classList.contains("show")).to.be.true;
          }
        }

        for (const invalid of [ "abc", "1,2,3", "1.2.3" ]) {
          input.value = invalid;
          save.click();
          expect(input.validity.valid).to.be.false;
          expect(fetchMock.callHistory.calls(url)).to.have.length(0);
        }
        const points = criterion.pointrange ? 1.5 : 2.5;
        input.value = locale === "es-ES" ? String(points).replace(".", ",") : String(points);
        const hidden = oneEvent(modal, "hidden.bs.modal");
        save.click();
        expect(input.validity.valid).to.be.true;
        await hidden;
        await waitUntil(() => fetchMock.callHistory.calls(url).length === 1);
        const saved = JSON.parse(fetchMock.callHistory.lastCall(url).options.body);
        expect(saved.points).to.equal(points);
      }
    });
  }

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

  it ("explains incomplete weights after changing a new published rubric to weighted", async () => {
    const rubric = structuredClone(data.rubric4);
    rubric.draft = false;
    rubric.weighted = false;
    rubric.criteria.forEach(criterion => criterion.weight = 0);
    const el = await fixture(html`
      <sakai-rubric site-id="${data.siteId}" .rubric=${rubric}></sakai-rubric>
    `);
    await waitUntil(() => el.renderRoot.querySelector(".rubric-toggle"));
    el.renderRoot.querySelector(".rubric-toggle").click();
    el.renderRoot.querySelector("button.weighted").click();
    await waitUntil(() => el.renderRoot.querySelector(".save-weights"));
    const save = el.renderRoot.querySelector(".save-weights");
    expect(save.disabled).to.be.false;
    const initialHelp = el.renderRoot.querySelector(".weight-save-help");
    expect(initialHelp.getAttribute("role")).to.equal("status");
    expect(initialHelp.textContent).to.equal("");
    expect(initialHelp.classList.contains("sak-banner-warn")).to.be.false;
    const input = el.renderRoot.querySelector("input[data-criterion-id]");
    expect(input.value).to.equal("100");
    input.value = "50";
    input.dispatchEvent(new Event("input", { bubbles: true }));
    await waitUntil(() => save.disabled);
    const help = el.renderRoot.querySelector(".weight-save-help");
    expect(help).to.equal(initialHelp);
    expect(help.classList.contains("sak-banner-warn")).to.be.true;
    expect(help.textContent).to.contain("100%");
    expect(help.textContent).to.contain("Save as Draft");
    expect(help.textContent).to.contain("unlink it");
    expect(save.getAttribute("aria-describedby")).to.equal(help.id);
    const savesBefore = fetchMock.callHistory.calls(data.rubric4OwnerUrl).length;
    save.click();
    expect(fetchMock.callHistory.calls(data.rubric4OwnerUrl)).to.have.length(savesBefore);
    el.renderRoot.querySelector("button.draft").click();
    await waitUntil(() => !save.disabled);
    expect(el.renderRoot.querySelector(".weight-save-help")).to.equal(help);
    expect(help.textContent).to.equal("");
    expect(help.classList.contains("sak-banner-warn")).to.be.false;
    save.click();
    await waitUntil(() => !el.renderRoot.querySelector(".sak-banner-success").classList.contains("d-none"));
    expect(JSON.parse(fetchMock.callHistory.lastCall(data.rubric4OwnerUrl).options.body))
      .to.deep.include({ op: "replace", path: "/criteria/0/weight", value: 50 });
  });

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
