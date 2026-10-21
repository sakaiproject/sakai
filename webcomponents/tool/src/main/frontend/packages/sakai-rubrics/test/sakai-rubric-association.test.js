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


  it ("keeps an attached draft selected and excludes unrelated drafts", async () => {
    const attached = { ...structuredClone(data.rubric1), draft: true };
    const unrelated = { ...attached, id: 999, title: "Unrelated draft" };
    const published = { ...attached, id: 998, title: "Published rubric", draft: false };
    fetchMock.removeRoutes();
    fetchMock.get(data.i18nUrl, data.i18n).get(data.rubricsUrl, [published, attached, unrelated]);
    const el = await fixture(html`
      <sakai-rubric-association site-id="${data.siteId}"
          .association=${{ rubricId: String(attached.id), parameters: {} }}>
      </sakai-rubric-association>
    `);
    await waitUntil(() => el.renderRoot.querySelector("select"));
    const select = el.renderRoot.querySelector("select[name=rbcs-rubricslist]");
    expect(select.options).to.have.length(2);
    expect(select.value).to.equal(String(attached.id));
    expect(select.selectedOptions[0].textContent).to.contain(attached.title);
    expect(el.renderRoot.querySelector("#do-associate-radio").checked).to.be.true;
    expect(el.renderRoot.querySelector("#dont-associate-radio").checked).to.be.false;
  });

  it ("does not offer draft rubrics for a new association", async () => {
    const published = { ...structuredClone(data.rubric1), draft: false };
    fetchMock.removeRoutes();
    fetchMock.get(data.i18nUrl, data.i18n).get(data.rubricsUrl, [published, { ...published, id: 999, draft: true }]);
    const el = await fixture(html`<sakai-rubric-association site-id="${data.siteId}"></sakai-rubric-association>`);
    await waitUntil(() => el.renderRoot.querySelector("select"));
    const select = el.renderRoot.querySelector("select[name=rbcs-rubricslist]");
    expect(select.options).to.have.length(1);
    expect(select.value).to.equal(String(published.id));
    expect(el.renderRoot.querySelector("#dont-associate-radio").checked).to.be.true;
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
    let rubricStudent = document.querySelector("#rubric-preview sakai-rubric-student");
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
    rubricStudent = document.querySelector("#rubric-preview sakai-rubric-student");
    expect(rubricStudent.hasAttribute("preview")).to.be.true;

    expect(rubricStudent.hasAttribute("rubric-id")).to.be.true;
    expect(rubricStudent.hasAttribute("site-id")).to.be.true;
  });
});
