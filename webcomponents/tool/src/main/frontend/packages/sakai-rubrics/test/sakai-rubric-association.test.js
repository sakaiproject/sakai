import "../sakai-rubric-association.js";
import "../sakai-rubrics-utils.js";
import * as data from "./data.js";
import { expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

window.sakai = window.sakai || {
  editor: {
    launch: () => ({ focus: () => "", on: () => "", setData: (data, callback) => "" })
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
    fetchMock.hardReset();
  });


  it ("renders a rubric association correctly", async () => {

    let el = await fixture(html`
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

    expect(document.querySelectorAll("#rubric-preview sakai-rubric-student").length).to.equal(1);;
    const rubricStudent = document.querySelector("#rubric-preview sakai-rubric-student");
    expect(rubricStudent).to.exist;
    expect(rubricStudent.hasAttribute("preview")).to.be.false

    // Check that the preview button exists and click it
    const previewButton = el.querySelector(".rubrics-selections > button");
    expect(previewButton).to.exist;
    expect(document.querySelector("#rubric-preview.show")).to.not.exist;
    previewButton.click();
    await waitUntil(() => document.querySelector("#rubric-preview sakai-rubric-student[preview]"), "No lightbox displayed", { timeout: 5000 });

    await waitUntil(() => document.querySelector("#rubric-preview.show"), "No lightbox displayed", { timeout: 5000 });
    expect(document.querySelector("#rubric-preview.show")).to.exist;
    expect(rubricStudent.hasAttribute("preview")).to.be.true;

    expect(rubricStudent.hasAttribute("rubric-id")).to.be.true;
    expect(rubricStudent.hasAttribute("site-id")).to.be.true;
  });
});
