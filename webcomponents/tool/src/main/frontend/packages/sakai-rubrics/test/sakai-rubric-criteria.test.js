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
      .put(data.rubric4CriteriaSortUrl, 200)
      .patch(data.rubric4OwnerUrl, 200)
      .patch(data.rubric4Criteria5Url, 200)
      .patch(data.rubric4Criteria6Url, 200)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
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
