import "../sakai-rubric-readonly.js";
import "../sakai-rubric-student.js";
import "../sakai-rubric.js";
import "../sakai-rubric-student-preview-button.js";
import "../sakai-rubric-criterion-preview.js";
import "../sakai-rubric-criteria-readonly.js";
import "../sakai-rubrics-utils.js";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

window.sakai = window.sakai || {
  editor: {
    launch: () => ({ focus: () => "", on: () => "", setData: (data, callback) => "" })
  },
};

describe("sakai-rubric-readonly tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });


  it ("renders a readonly rubric", async () => {

    let el = await fixture(html`
      <sakai-rubric-readonly site-id="${data.siteId}"
          .rubric=${data.rubric4}>
      </sakai-rubric-readonly>
    `);

    await waitUntil(() => el._i18n && el.rubric);

    await elementUpdated(el);

    expect(el.querySelector(".rubric-title")).to.exist;

    expect(el.querySelector(".delete-rubric-button")).to.not.exist;

    el.setAttribute("is-super-user", "");
    await elementUpdated(el);

    expect(el.querySelector(".delete-rubric-button")).to.exist;

    // Test the toggling of the criteria
    const toggleEl = el.querySelector(".rubric-toggle");
    expect(toggleEl).to.exist;

    // Wait until the bootstrap Collapse is shown
    const listener = oneEvent(el.querySelector(".collapse"), "shown.bs.collapse");
    toggleEl.click();
    await listener;

    expect(el.querySelector(".rubric-details")).to.exist;
    expect(el.querySelector(".collapse").classList.contains("show")).to.be.true;

    expect(el.querySelector("span.locked")).to.not.exist;

    // Now set the rubric to be one which is locked
    el.rubric = data.rubric1;
    await elementUpdated(el);

    expect(el.querySelector(".delete-rubric-button")).to.not.exist;

    el.setAttribute("is-super-user", "");
    await elementUpdated(el);

    expect(el.querySelector(".delete-rubric-button")).to.not.exist;

    expect(el.querySelector("span.locked")).to.exist;
  });
});
