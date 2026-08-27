import "../sakai-rubric-student.js";
import "../sakai-rubrics-utils.js";
import * as data from "./data.js";
import { elementUpdated, expect, html, fixture, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

window.sakai = window.sakai || {
  editor: {
    launch: () => ({ focus: () => "", on: () => "" })
  },
};

describe("sakai-rubric-student tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.rubric1Url, data.rubric1)
      .get(data.associationUrl, data.association)
      .get(data.evaluationUrl, data.evaluation)
      .get(`${data.evaluationUrl}?isPeer=true`, data.evaluation)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
    document.getElementById("rubric-preview")?.remove();
    if (window.top.rubrics?.utils) {
      window.top.rubrics.utils.lightbox = null;
    }
  });


  it ("renders a rubric student correctly", async () => {

    let el = await fixture(html`
      <sakai-rubric-student site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="${data.entityId}"
          evaluated-item-id="${data.evaluatedItemId}"
          evaluated-item-owner-id="${data.evaluatedItemOwnerId}">
      </sakai-rubric-student>
    `);

    await el.updateComplete;

    await waitUntil(() => el.querySelector(".rubric-details"), "No .rubric-details created");
    expect(el.querySelector("sakai-rubric-criterion-preview")).to.not.exist;
    expect(el.querySelector(`select[aria-label="${el._i18n.rubric_view_selection_title}"]`)).to.not.exist;

    await el.updateComplete;
    expect(el.querySelector("sakai-rubric-criterion-student")).to.exist;

    await expect(el).to.be.accessible();
  });

  it ("rubric student preview renders correctly", async () => {

    let el = await fixture(html`
      <sakai-rubric-student site-id="${data.siteId}" rubric-id="1" preview="true"></sakai-rubric-student>
    `);

    await el.updateComplete;

    await expect(el).to.be.accessible();

    await waitUntil(() => el.querySelector("sakai-rubric-criterion-preview"), "No sakai-rubric-criterion-preview created");
  });

  it ("shows summary views for instructors", async () => {

    const el = await fixture(html`
      <sakai-rubric-student site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="${data.entityId}"
          evaluated-item-id="${data.evaluatedItemId}"
          evaluated-item-owner-id="${data.evaluatedItemOwnerId}"
          instructor>
      </sakai-rubric-student>
    `);

    await waitUntil(() => el.querySelector(".rubric-details"), "No .rubric-details created");
    expect(el.querySelector(`select[aria-label="${el._i18n.rubric_view_selection_title}"]`)).to.exist;
  });

  it ("hides summary views for peer or self evaluation even when instructor is set", async () => {

    const el = await fixture(html`
      <sakai-rubric-student site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="${data.entityId}"
          evaluated-item-id="${data.evaluatedItemId}"
          evaluated-item-owner-id="${data.evaluatedItemOwnerId}"
          instructor
          is-peer-or-self>
      </sakai-rubric-student>
    `);

    await waitUntil(() => el.querySelector(".rubric-details"), "No .rubric-details created");
    expect(el.querySelector(`select[aria-label="${el._i18n.rubric_view_selection_title}"]`)).to.not.exist;
    expect(fetchMock.callHistory.called(`${data.evaluationUrl}?isPeer=true`)).to.be.true;
  });

  it ("showRubric sets is-peer-or-self and hides summary views", async () => {

    window.top.rubrics.utils.initLightbox({ preview_rubric: "Preview", close_dialog: "Close" }, data.siteId);

    window.top.rubrics.utils.showRubric(data.rubric1.id, {
      "tool-id": data.toolId,
      "entity-id": data.entityId,
      "evaluated-item-id": data.evaluatedItemId,
      "evaluated-item-owner-id": data.evaluatedItemOwnerId,
      instructor: true,
      "is-peer-or-self": true,
    });

    const el = document.querySelector("sakai-rubric-student");
    await waitUntil(() => el.querySelector(".rubric-details"), "No .rubric-details created");
    expect(el.hasAttribute("is-peer-or-self")).to.be.true;
    expect(el.isPeerOrSelf).to.be.true;
    expect(el.querySelector(`select[aria-label="${el._i18n.rubric_view_selection_title}"]`)).to.not.exist;
    expect(fetchMock.callHistory.called(`${data.evaluationUrl}?isPeer=true`)).to.be.true;
  });
});
