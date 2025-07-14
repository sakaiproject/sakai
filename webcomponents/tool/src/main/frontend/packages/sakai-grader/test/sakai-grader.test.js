import "../sakai-grader.js";
import { elementUpdated, expect, fixture, oneEvent, waitUntil } from "@open-wc/testing";
import { html } from "lit";
import * as data from "./data.js";
import * as groupPickerData from "../../sakai-group-picker/test/data.js";
import fetchMock from "fetch-mock/esm/client";
import * as sinon from "sinon";
import { faker } from "@faker-js/faker/locale/en_GB";
import { generateSubmission } from "./submission-generator.js";

describe("sakai-grader tests", () => {

  window.top.portal = { locale: "en_GB", siteId: data.siteId, siteTitle: data.siteTitle };
  window.top.sakai = {
    editor: {
      launch: () => ({ setData: () => {}, on: () => {} }),
    }
  };

  const assignmentCloseTime = data.closeTime;

  beforeEach(async () => {

    fetchMock.get(data.i18nUrl, data.i18n)
      .get(groupPickerData.i18nUrl, groupPickerData.i18n)
      .get(data.filePickerI18nUrl, data.filePickerI18n);
  });

  afterEach(() => {
    fetchMock.restore();
  });

  it ("loads a set of submissions and displays the specified submission correctly", async () => {

    // This test covers the scenerio where the instructor specifies a particular submission to load
    // up and grade. No rubric is specified and there are no attachments. So, just a rich text
    // submission.

    // Fake up 10 submissions
    const submissions = Array.from({ length: 10 }, () => generateSubmission());

    const selectedSubmission = submissions[0];
    const past = faker.date.past();
    selectedSubmission.dateSubmittedEpochSeconds = past.getTime() / 1000;
    selectedSubmission.dateSubmitted = past.toDateString();
    selectedSubmission.submittedText = faker.lorem.paragraph(4);
    selectedSubmission.submitted = true;
    const firstSubmitterId = selectedSubmission.submitters[0].id;

    const selectedSubmissionId = selectedSubmission.id;
    const gradingData = { ...data.gradableData, submissions, totalSubmissions: submissions.length };

    const url = `/direct/assignment/gradable.json?gradableId=${gradingData.gradable.id}&submissionId=${selectedSubmissionId}`;
    fetchMock.get(url, gradingData);

    const editorLaunchSpy = sinon.spy(sakai.editor, "launch");

    const el = await fixture(html`
      <sakai-grader gradable-id="${gradingData.gradable.id}"
          submission-id="${selectedSubmissionId}">
      </sakai-grader>
    `);

    await waitUntil(() => !el._loadingData);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // An editor should be created for the feedback comment and private notes
    expect(editorLaunchSpy.calledTwice).to.be.true;

    expect(el.querySelector("#grader-topbar")).to.exist;

    expect(el.querySelector(".assessment-title").innerText).to.equal(gradingData.gradable.title);
    expect(el.querySelector("#grader-total").innerText).to.equal(`${el._i18n.grad3} 0 / ${submissions.length}`);

    const submitterSelect = el.querySelector("#grader-submitter-select");
    expect(submitterSelect).to.exist;

    expect(submitterSelect.options.length).to.equal(submissions.length);

    // The submissions should be sorted by the first submitter name
    const sortedSubmissions = [ ...submissions ].sort((a, b) => a.submitters[0].sortName.localeCompare(b.submitters[0].sortName));

    // Have the sumbmission submitters been rendered correctly?
    Array.from(submitterSelect.options).forEach((o, i) => {

      if (o.selected) expect(o.value).to.equal(selectedSubmissionId);
      expect(o.value).to.equal(sortedSubmissions[i].id);
    });

    // Have we rendered the first submitter's display name and photo?
    expect(el.querySelector("#grader-label").innerHTML).to.contain(selectedSubmission.submitters[0].sortName);
    expect(el.querySelector(`#grader sakai-user-photo[user-id="${selectedSubmission.submitters[0].id}"]`)).to.exist;

    expect(el._submittedTextMode).to.be.true;

    expect(el.querySelector("#gradable div.sak-banner-info").innerHTML).to.contain(el._i18n.inline_feedback_instruction);

    // This is janky. submission.js sets feedbackText to equal submittedText, just because we use
    // the same field for both.
    expect(el.querySelector("#grader-feedback-text").innerHTML).to.contain(selectedSubmission.submittedText);

    const gradeInput = el.querySelector("input.points-input[type='text']");
    expect(gradeInput).to.exist;
    expect(el.querySelector("#grader-max-point-label").innerHTML).to.contain(gradingData.gradable.maxGradePoint);

    // Rather than mess about with editor mocks, just setup the resulting state in the component
    const feedbackComment = faker.lorem.sentence();
    el._submission.feedbackComment = feedbackComment;
    el._gradeOrCommentsModified = true;

    const privateNotes = faker.lorem.sentence();
    el._submission.privateNotes = privateNotes;
    el._gradeOrCommentsModified = true;

    const grade = "23";
    gradeInput.value = grade;
    gradeInput.dispatchEvent(new Event("keyup"));
    expect(el._submission.grade).to.equal(grade);

    // Test _getFormData
    const formData = el._getFormData();
    expect(formData.get("gradableId")).to.equal(gradingData.gradable.id);
    expect(formData.get("submissionId")).to.equal(selectedSubmissionId);
    expect(formData.get("studentId")).to.equal(firstSubmitterId);
    expect(formData.get("courseId")).to.equal(data.siteId);
    expect(formData.get("siteId")).to.equal(data.siteId);
    expect(formData.get("grade")).to.equal(grade);
    expect(formData.get("feedbackComment")).to.equal(feedbackComment);
    expect(formData.get("privateNotes")).to.equal(privateNotes);

    fetchMock.post("/direct/assignment/setGrade.json", (url, opts) => {

      const submission = { ...Object.fromEntries(opts.body), graded: true };
      return {
        submission,
        assignmentCloseTime: data.closeTime,
      };
    });

    el.querySelector("button[data-release='true']").click();

    await waitUntil(() => el._hasGraded);
    expect(el._submission.graded).to.be.true;
    expect(el._submission.grade).to.be.equal(grade);
    expect(el._submission.feedbackComment).to.be.equal(feedbackComment);
    expect(el._submission.privateNotes).to.be.equal(privateNotes);
  });

  it ("renders settings correctly", async () => {

    // Fake up 10 submissions
    const submissions = Array.from({ length: 10 }, () => generateSubmission());

    const gradingData = { ...data.gradableData, submissions, totalSubmissions: submissions.length };

    const selectedSubmission = submissions[0];
    selectedSubmission.submittedText = faker.lorem.paragraph(4);
    selectedSubmission.submitted = true;
    const selectedSubmissionId = selectedSubmission.id;
    const firstSubmitterId = selectedSubmission.submitters[0].id;

    const url = `/direct/assignment/gradable.json?gradableId=${gradingData.gradable.id}&submissionId=${selectedSubmissionId}`;
    fetchMock.get(url, gradingData);
 
    const el = await fixture(html`
      <sakai-grader gradable-id="${gradingData.gradable.id}"
          submission-id="${selectedSubmissionId}">
      </sakai-grader>
    `);

    await waitUntil(() => el.gradable);

    expect(el.querySelector("#grader-settings-link")).to.exist;
    el.querySelector("#grader-settings-link").click();
    const graderSettings = el.querySelector("#grader-settings");
    expect(graderSettings).to.exist;

    const { detail } = await oneEvent(graderSettings, "shown.bs.modal");

    const submittedOnlyCheckbox = graderSettings.querySelector("[type='checkbox']");
    submittedOnlyCheckbox.click();
    await elementUpdated(el);
    await expect(el).to.be.accessible();
    expect(el._submittedOnly).to.be.true;
    expect(el.querySelector("#grader-filter-warning")).to.exist;
    expect(el.querySelector("#grader-filter-warning").innerHTML).to.contain(el._i18n.filter_settings_warning);
    submittedOnlyCheckbox.click();
    await elementUpdated(el);
    await expect(el).to.be.accessible();
    expect(el._submittedOnly).to.be.false;
    expect(el.querySelector("#grader-filter-warning")).to.not.exist;
  });
});
