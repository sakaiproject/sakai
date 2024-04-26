import "../sakai-grader.js";
import { aTimeout, expect, fixture, oneEvent, waitUntil } from "@open-wc/testing";
import { html } from "lit";
import * as data from "./data.js";
import * as groupPickerData from "../../sakai-group-picker/test/data.js";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-grader tests", () => {

  window.top.portal = { locale: "en_GB", siteId: data.siteId, siteTitle: data.siteTitle };
  window.top.sakai = {
    editor: { launch: function() { return { setData: function () {}, on: function () {} } } },
  };

  const assignmentCloseTime = data.closeTime;

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get(data.filePickerI18nUrl, data.filePickerI18n, { overwriteRoutes: true })
    .get(groupPickerData.i18nUrl, groupPickerData.i18n, { overwriteRoutes: true })
    .get(`/direct/assignment/gradable.json?gradableId=${data.gradableId}&submissionId=${data.firstSubmissionId}`, data.gradableData, { overwriteRoutes: true })
    .get(`/direct/assignment/grades.json?gradableId=${data.gradableId}&courseId=${data.siteId}`, data.gradesData, { overwriteRoutes: true })
    .get(data.fullSubmissionUrl, () => ({ submissions: [ data.firstSubmission ], assignmentCloseTime }), { overwriteRoutes: true })
    .post("/direct/assignment/setGrade.json", (url, opts) => {

      const submission = Object.fromEntries(opts.body);
      submission.graded = true;
      return {
        submission,
        assignmentCloseTime: data.closeTime,
      };
    }, {overwriteRoutes: true})
    .get("*", 500, { overwriteRoutes: true });

  it ("grades first submission correctly", async () => {

    const el = await fixture(html`<sakai-grader gradable-id="${data.gradableId}" submission-id="${data.firstSubmissionId}"></sakai-grader>`);
    await waitUntil(() => el.i18n);

    expect(el.querySelector("#gradable")).to.exist;
    expect(el.querySelector("sakai-lti-iframe")).to.not.exist;

    expect(el.querySelectorAll("#grader-submitter-select option").length).to.equal(data.gradableData.submissions.length);

    const graderButton = el.querySelector("#grader-link-block button");
    expect(graderButton).to.exist;

    graderButton.click();
    await waitUntil(() => el.querySelector("#grader.show"), "Grader sidebar did not display");

    const scoreGradeInput = el.querySelector("#score-grade-input");
    expect(scoreGradeInput).to.exist;
    scoreGradeInput.value = "60";
    scoreGradeInput.dispatchEvent(new Event("keyup"));

    const saveButton = el.querySelector("#grader-save-button");
    expect(saveButton).to.exist;

    saveButton.click();

    await el._hasGraded && el.updateComplete;

    const graderSettingsLink = el.querySelector("#grader-settings-link");
    expect(graderSettingsLink).to.exist;
    graderSettingsLink.click();
    const graderSettings = el.querySelector("#grader-settings");
    const settingsSelect = graderSettings.querySelector("select");
    expect(settingsSelect).to.exist;
    await waitUntil(() => settingsSelect.querySelector("option[value='graded']"), "Graded option not rendered");
    expect(settingsSelect.querySelector("option[value='graded']")).to.exist;
    settingsSelect.value = "graded";
    settingsSelect.dispatchEvent(new Event("change"));

    await el.updateComplete;
    expect(el.querySelectorAll("#grader-submitter-select option").length).to.equal(1);
  });

  /*
  it ("renders a text submission correctly", async () => {

    const el = await fixture(html`<sakai-grader gradable-id="${data.gradableId}" submission-id="${data.textSubmissionId}"></sakai-grader>`);
    await waitUntil(() => el.i18n);

    expect(el.querySelector("#grader-navigator")).to.exist;

    await waitUntil(() => el.gradable);

    const feedbackText = el.querySelector("#grader-feedback-text");
    expect(feedbackText).to.exist;
    expect(feedbackText.innerHTML).to.contain(data.submittedText);

    const scoreGradeInput = el.querySelector("#score-grade-input");
    expect(scoreGradeInput).to.exist;

    scoreGradeInput.value = "50";
    const evt = document.createEvent("KeyboardEvent");
    evt.initEvent("keyup", false, true);
    scoreGradeInput.dispatchEvent(evt);
    expect(el._submission.grade).to.equal("50");

    const saveButton = el.querySelector("button[name='save']");
    expect(saveButton).to.exist;
    saveButton.click();
  });

  it ("renders settings correctly", async () => {
 
    const el = await fixture(html`<sakai-grader gradable-id="${data.gradableId}" submission-id="${data.textSubmissionId}"></sakai-grader>`);

    await waitUntil(() => el.gradable);

    expect(el.querySelector("#grader-settings-link")).to.exist;
    el.querySelector("#grader-settings-link").click();
    const graderSettings = el.querySelector("#grader-settings");
    expect(graderSettings).to.exist;

    const { detail } = await oneEvent(graderSettings, "shown.bs.modal");

    const submittedOnlyCheckbox = graderSettings.querySelector("[type='checkbox']");
    submittedOnlyCheckbox.click();
    await el.updateComplete;
    expect(el.submittedOnly).to.be.true;
    expect(el.querySelector("#grader-filter-warning")).to.exist;
    expect(el.querySelector("#grader-filter-warning").innerHTML).to.contain(el.i18n.filter_settings_warning);
    submittedOnlyCheckbox.click();
    await el.updateComplete;
    expect(el.submittedOnly).to.be.false;
    expect(el.querySelector("#grader-filter-warning")).to.not.exist;
  });
  */

  /*
  it ("is accessible", async () => {

    const el = await fixture(html`<sakai-grader gradable-id="${data.gradableId}" submission-id="${data.textSubmissionId}"></sakai-grader>`);

    await waitUntil(() => el.i18n);
    await waitUntil(() => el.grades);
    await el.updateComplete;

    await expect(el).to.be.accessible();
  });
  */
});
