import "../sakai-rubric-association.js";
import * as data from "./data.js";
import { expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

window.sakai = window.sakai || {
  editor: {
    launch: () => ({ focus: () => "", on: () => "", setData: () => "" })
  },
};

describe("sakai-rubric-association tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.rubricsUrl, data.rubrics)
      .get(data.rubric1Url, data.rubric1)
      .get(data.associationUrl, data.association)
      .get("*", 500);
  });

  afterEach(() => {
    document.querySelectorAll("sakai-rubric-modal").forEach(modal => modal.remove());
    fetchMock.hardReset();
  });


  it ("renders a rubric association correctly", async () => {

    const el = await fixture(html`
      <sakai-rubric-association site-id="${data.siteId}"
          tool-id="sakai.assignment.grades"
          associate-value="1"
          dont-associate-value="0">
      </sakai-rubric-association>
    `);

    await waitUntil(() => el._i18n && el._rubrics);

    // Test that the radios and rubric selector exist
    expect(el.querySelector(".sakai-rubric-association")).to.exist;
    expect(el.querySelectorAll("input[name='rbcs-associate']").length).to.equal(2);
    expect(el.querySelector(".rubrics-list")).to.exist;

    // The rubric picker should be present, disabled, and contain data.rubrics.length options.
    const select = el.querySelector("select[name='rbcs-rubricslist']");
    expect(select).to.exist;
    expect(select.querySelectorAll("option").length).to.equal(data.rubrics.length);
    expect(select.disabled).to.be.true;

    // Select the associate with a rubric radio
    el.querySelectorAll("input[name='rbcs-associate']")[1].click();
    await el.updateComplete;

    // The rubric selector should be be enabled
    expect(select.disabled).to.be.false;

    expect(document.querySelector("sakai-rubric-modal")).to.not.exist;

    // Check that the preview button exists and click it
    const previewButton = el.querySelector(".rubrics-selections > button");
    expect(previewButton).to.exist;
    previewButton.click();
    await waitUntil(() => document.querySelector("sakai-rubric-modal")?.shadowRoot?.querySelector("sakai-rubric-student")?.preview, "No rubric preview loaded", { timeout: 5000 });

    const modal = document.querySelector("sakai-rubric-modal");
    expect(modal.shadowRoot.querySelector("dialog").open).to.be.true;
    const rubricStudent = modal.shadowRoot.querySelector("sakai-rubric-student");
    expect(rubricStudent.preview).to.be.true;

    expect(rubricStudent.rubricId).to.equal(data.rubric1.id);
    expect(rubricStudent.siteId).to.equal(data.siteId);
  });
});
