import "../sakai-rubric.js";
import "../sakai-rubric-criteria.js";
import "../sakai-rubric-criterion-edit.js";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

fetchMock
  .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
  .put(data.rubric4CriteriaSortUrl, 200, { overwriteRoutes: true })
  .patch(data.rubric4OwnerUrl, 200, { overwriteRoutes: true })
  .patch(data.rubric4Criteria5Url, 200, { overwriteRoutes: true })
  .patch(data.rubric4Criteria6Url, 200, { overwriteRoutes: true })
  .get("*", 500, { overwriteRoutes: true });

window.sakai = window.sakai || {
  editor: {
    launch: () => ({ focus: () => "", on: () => "", setData: (data, callback) => "" })
  },
};

describe("sakai-rubric-criteria tests", () => {

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
      fetchMock.post(url, (u, opts) => JSON.parse(opts.body), { overwriteRoutes: true });
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
      expect(fetchMock.calls(url)).to.have.length(0);
      expect(modal.classList.contains("show")).to.be.true;

      if (criterion.pointrange) {
        input.value = "3";
        save.click();
        expect(input.validity.rangeOverflow).to.be.true;
        expect(fetchMock.calls(url)).to.have.length(0);
        expect(modal.classList.contains("show")).to.be.true;
      }

      const points = criterion.pointrange ? 1.5 : 2.5;
      input.value = String(points);
      const hidden = oneEvent(modal, "hidden.bs.modal");
      save.click();
      expect(input.validity.valid).to.be.true;
      await hidden;
      await waitUntil(() => fetchMock.calls(url).length === 1);
      const saved = JSON.parse(fetchMock.lastCall(url)[1].body);
      expect(Number(saved.points)).to.equal(points);
    }
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
