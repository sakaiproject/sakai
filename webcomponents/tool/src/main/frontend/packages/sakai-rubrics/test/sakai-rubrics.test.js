import "../sakai-rubric-association.js";
import "../sakai-rubric-student.js";
import "../sakai-rubric-student-preview-button.js";
import "../sakai-rubrics-manager.js";
import "../sakai-rubric-grading.js";
import "../sakai-rubric-criterion-edit.js";
import "../sakai-rubric-criterion-preview.js";
import "../sakai-rubrics-utils.js";
import { html } from "lit";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, oneEvent, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

window.top.portal = { locale: "en_GB" };

fetchMock
  .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
  .get(data.sharedRubricsUrl, data.sharedRubrics, { overwriteRoutes: true })
  .get(data.rubricsUrl, data.rubrics, { overwriteRoutes: true })
  .get(data.rubric1Url, data.rubric1, { overwriteRoutes: true })
  .get(data.associationUrl, data.association, { overwriteRoutes: true })
  .get(data.evaluationUrl, data.evaluation, { overwriteRoutes: true })
  .post(`/api/sites/${data.siteId}/rubric-evaluations`, (url, opts) => {

      return Object.assign({
        id: "" + Math.floor(Math.random() * 20) + 1,
        creator: "adrian",
        created: Date.now(),
        creatorDisplayName: "Adrian Fish",
      }, JSON.parse(opts.body));
    }, { overwriteRoutes: true })
  .get("*", 500, { overwriteRoutes: true });

window.sakai = window.sakai || {
  editor: {
    launch: () => ({ focus: () => "", on: () => "" })
  },
};

describe("sakai-rubrics tests", () => {

  it ("renders a rubric association correctly", async () => {

    console.debug("association test");

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
    await waitUntil(() => document.querySelector("#rubric-preview sakai-rubric-student[preview]"), "No lightbox displayed");

    await waitUntil(() => document.querySelector("#rubric-preview.show"), "No lightbox displayed");
    expect(document.querySelector("#rubric-preview.show")).to.exist;
    expect(rubricStudent.hasAttribute("preview")).to.be.true;

    expect(rubricStudent.hasAttribute("rubric-id")).to.be.true;
    expect(rubricStudent.hasAttribute("site-id")).to.be.true;

    await waitUntil(() => rubricStudent.querySelector(".itemSeparator"), "No .itemSeparator created");
    expect(rubricStudent.querySelector(".itemSeparator")).to.exist;
    expect(rubricStudent.querySelector("#rubric-grading-or-preview")).to.exist;
    expect(rubricStudent.querySelector("sakai-rubric-criterion-student")).to.not.exist;
    expect(rubricStudent.querySelector("sakai-rubric-criterion-preview")).to.exist;
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

    await waitUntil(() => el._i18n);

    await el.updateComplete;

    await waitUntil(() => el.querySelector(".rubric-details"), "No .rubric-details created");
    expect(el.querySelector("sakai-rubric-criterion-preview")).to.not.exist;

    await el.updateComplete;
    expect(el.querySelector("sakai-rubric-criterion-student")).to.exist;
  });

  it ("rubric student preview renders correctly", async () => {

    let el = await fixture(html`
      <sakai-rubric-student site-id="${data.siteId}" rubric-id="1" preview="true"></sakai-rubric-student>
    `);

    await waitUntil(() => el.querySelector("sakai-rubric-criterion-preview"), "No sakai-rubric-criterion-preview created");
  });

  it ("rubric student preview button renders correctly", async () => {

    let el = await fixture(html`
      <sakai-rubric-student-preview-button
          site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="${data.entityId}">
      </sakai-rubric-student-preview-button>
    `);

    await waitUntil(() => el._i18n);

    await waitUntil(() => el.querySelector("h3"), "No h3 rendered");

    const button = el.querySelector("button");
    expect(button).to.exist;
    button.click();

    await waitUntil(() => document.getElementById("rubric-preview"), "No lightbox displayed");

    el.setAttribute("display", "span");
    await waitUntil(() => el.querySelector("span"), "No span rendered");
    expect(el.querySelector("button")).to.not.exist;
  });

  it ("rubric grading renders correctly", async () => {

    let el = await fixture(html`
      <sakai-rubric-grading
          site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="${data.entityId}"
          evaluated-item-id="${data.evaluatedItemId}"
          evaluated-item-owner-id="${data.evaluatedItemOwnerId}"
          enable-pdf-export>
      </sakai-rubric-grading>
    `);

    await waitUntil(() => el._i18n && el.association);

    // Some basic element checks
    expect(el.querySelector(".grading")).to.exist;
    expect(el.querySelector("sakai-rubric-pdf")).to.exist;
    expect(el.querySelector(".sakai-rubric-criteria-grading")).to.exist;

    // We need to wait until the evaluation data has been fetched, then count the rows.
    await waitUntil(() => el.querySelector(".criterion-row"), "No criterion rows rendered");
    expect(el.querySelectorAll(".criterion-row").length).to.equal(2);

    // Initially, with our test evaluation load, we have selected one of the ratings worth 2 points
    const totalPoints = el.querySelector(`#rbcs-${data.evaluatedItemId}-${data.entityId}-totalpoints`);
    expect(totalPoints).to.exist;
    expect(totalPoints.value).to.equal("2");

    // Now, let's select rating 1, which should updated our total points to 1 and fire a
    // total-points-updated event.
    const ratingItem1 = el.querySelector("#rating-item-1");
    expect(ratingItem1).to.exist;
    const listener = oneEvent(el, "total-points-updated");
    ratingItem1.click();
    const { detail } = await listener;
    expect(detail.evaluatedItemId).to.equal(data.evaluatedItemId);
    expect(detail.entityId).to.equal(data.entityId);
    expect(detail.value).to.equal("1");
    await el.updateComplete;
    expect(totalPoints.value).to.equal("1");

    expect(el.querySelectorAll(".fine-tune-points").length).to.equal(1);
    const fineTuneInput = el.querySelector(".fine-tune-points");
    fineTuneInput.value = "1.8";
    fineTuneInput.dispatchEvent(new Event("input"));
    await el.updateComplete;
    expect(totalPoints.value).to.equal("1.8");
  });
  
  it ("manager renders correctly", async () => {

    let el = await fixture(html`
      <sakai-rubrics-manager site-id="${data.siteId}"></sakai-rubrics-manager>
    `);

    await waitUntil(() => el._i18n);

    expect(el.querySelector("h1:first-child")).to.exist;
    expect(el.querySelector("h1:first-child").innerHTML).to.contain(el._i18n.manage_rubrics);
  });

  it ("criterion edit with textarea works correctly", async () => {

    let el = await fixture(html`
      <sakai-rubric-criterion-edit
          site-id="${data.siteId}"
          rubric-id="${data.rubric1.Id}"
          .criterion=${data.criterion1}
          textarea>
      </sakai-rubric-criterion-edit>
    `);

    await waitUntil(() => el.querySelector("button.edit-criterion-button"), "edit button does not exist");
    expect(el.querySelector(`#edit-criterion-${data.criterion1.id}`)).to.exist;
    expect(el.querySelector("sakai-editor")).to.exist;
    const button = el.querySelector("button.edit-criterion-button");
    expect(button.getAttribute("title")).to.equal(el._i18n.edit_criterion);
    let modal = el.querySelector(`#edit-criterion-${data.criterion1.id}`);

    const listener = oneEvent(modal, "shown.bs.modal");
    button.click();
    await listener;

    modal = document.querySelector(".modal.show");
    expect(modal).to.exist;
  });

  it ("criterion preview renders correctly", async () => {

    let el = await fixture(html`
      <sakai-rubric-criterion-preview .criteria=${data.criteria1}></sakai-rubric-criterion-preview>
    `);

    await waitUntil(() => el._i18n);

    const criteriaRows = el.querySelectorAll(".criterion-row");
    expect(criteriaRows.length).to.equal(data.criteria1.length);
    const ratingItems = criteriaRows[0].querySelectorAll(".rating-item");
    expect(ratingItems.length).to.equal(data.criteria1[0].ratings.length);
    expect(ratingItems[0].querySelector(".div-description").innerHTML).to.contain(data.criteria1[0].ratings[0].description);
    expect(el.querySelectorAll(".criterion-group").length).to.equal(1);
  });

  it ("is rubrics manager accessible", async () => {

    let el = await fixture(html`
      <sakai-rubrics-manager site-id="${data.siteId}"></sakai-rubrics-manager>
    `);

    await waitUntil(() => el._i18n);

    expect(el).to.be.accessible();
  });
});
