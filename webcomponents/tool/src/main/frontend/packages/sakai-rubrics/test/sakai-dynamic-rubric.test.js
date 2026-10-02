import "../sakai-dynamic-rubric.js";
import { expect, fixture, html, waitUntil, elementUpdated } from "@open-wc/testing";
import fetchMock from "fetch-mock";
import { mockTranslations } from "../../../test/migrated-components-i18n.js";

const evaluationUrl = "/api/sites/site/rubric-evaluations/tools/sakai.samigo/items/entity/evaluations/grade/owners/student";
const rubric = { id: "rubric", title: "pub.published.item", criteria: [
  { id: "one", title: "Explanation", ratings: [ { id: "r1", points: 2 } ] },
  { id: "two", title: "Evidence", ratings: [ { id: "r2", points: 3 } ] },
  { id: "three", title: "Penalty", ratings: [ { id: "r3", points: -1 } ] },
] };

async function create() {
  const el = await fixture(html`<sakai-dynamic-rubric site-id="site" entity-id="entity" grading-id="grade"
      evaluated-item-owner-id="student" previous-grade="10" origin="gradeStudentResult.faces"></sakai-dynamic-rubric>`);
  await waitUntil(() => el.renderRoot.querySelectorAll("sakai-dynamic-criterion").length === 3);
  await waitUntil(() => el.renderRoot.querySelector("sakai-dynamic-criterion").renderRoot.querySelector("button"));
  return el;
}

function total(el) { return el.renderRoot.querySelector('input[name="newtotalgrade"]').value; }

describe("sakai-dynamic-rubric", () => {
  beforeEach(() => {
    fetchMock.mockGlobal();
    mockTranslations(fetchMock);
    fetchMock.get("/api/sites/site/rubric-associations/tools/sakai.samigo/items/entity", { id: "association", rubricId: "rubric" });
    fetchMock.get("/api/sites/site/rubrics/rubric", rubric);
    fetchMock.get(evaluationUrl, { id: "evaluation", metadata: { note: "keep" }, criterionOutcomes: [
      { criterionId: "one", selectedRatingId: "r1" },
    ] });
    fetchMock.put("/api/sites/site/rubric-evaluations/evaluation", ({ options }) => JSON.parse(options.body));
  });

  afterEach(() => fetchMock.hardReset());

  it("restores the saved total and cancels newly selected criteria", async () => {
    const el = await create();
    expect(total(el)).to.equal("10.00");
    const criteria = el.renderRoot.querySelectorAll("sakai-dynamic-criterion");
    await waitUntil(() => criteria[1].renderRoot.querySelector("button"));
    criteria[1].renderRoot.querySelector("button").click();
    await elementUpdated(el);
    expect(total(el)).to.equal("13.00");
    el.cancel();
    await elementUpdated(el);
    expect(total(el)).to.equal("10.00");
    await elementUpdated(criteria[1]);
    expect(criteria[1].renderRoot.querySelector("button").getAttribute("aria-pressed")).to.equal("false");
    await expect(el).to.be.accessible();
  });

  it("saves selected criteria and preserves evaluation metadata", async () => {
    const el = await create();
    const penalty = el.renderRoot.querySelectorAll("sakai-dynamic-criterion")[2];
    await waitUntil(() => penalty.renderRoot.querySelector("button"));
    penalty.renderRoot.querySelector("button").click();
    await elementUpdated(el);
    expect(total(el)).to.equal("9.00");
    const saved = await el.release();
    expect(saved.overallComment).to.equal("9.00");
    expect(saved.metadata).to.deep.equal({ note: "keep" });
    expect(saved.criterionOutcomes.map(outcome => outcome.selectedRatingId)).to.deep.equal([ "r1", null, "r3" ]);
    expect(saved.associationId).to.equal("association");
  });

  it("edits criteria with immutable parent state and restores persisted rubric edits", async () => {
    const el = await create();
    el.renderRoot.querySelector(".rubric-actions button").click();
    await elementUpdated(el);
    const child = el.renderRoot.querySelector("sakai-dynamic-criterion");
    await elementUpdated(child);
    child.renderRoot.querySelector('[aria-label="Edit this criterion"]').click();
    await elementUpdated(child);
    const input = child.renderRoot.querySelector('[aria-label="Criterion description"]');
    input.value = "Improved explanation";
    input.dispatchEvent(new InputEvent("input"));
    input.dispatchEvent(new Event("change"));
    await elementUpdated(el);
    expect(el.renderRoot.querySelector('input[name="updatedgrade"]').value).to.equal("true");
    expect(rubric.criteria[0].title).to.equal("Explanation");
    el.renderRoot.querySelectorAll(".rubric-actions button")[2].click();
    await waitUntil(() => el.renderRoot.querySelector("sakai-dynamic-criterion")?.crit.description === "Explanation");
    expect(el.renderRoot.querySelector('input[name="updatedgrade"]').value).to.equal("false");
  });
});
