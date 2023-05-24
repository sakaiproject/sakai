import { Submission } from "./submission.js";
import { LETTER_GRADE_TYPE } from "./sakai-grader-constants.js";

export const gradableDataMixin = Base => class extends Base {

  _loadGradableData(gradableId, submissionId) {

    this.loadingData = true;

    // Grab all of the initial data we need, submissions and students. This will come from the grading service in future.
    return new Promise(resolve => {

      // Then, request the full set of data
      fetch(`/direct/assignment/gradable.json?gradableId=${gradableId}`, {cache: "no-cache", credentials: "same-origin"})
      .then(res => res.json())
      .then(gradableData => {

        this.showOfficialPhoto = gradableData.showOfficialPhoto;
        this.previewMimetypes = gradableData.previewMimetypes;
        this.gradable = gradableData.gradable;

        this.gradeScale = this.gradable.gradeScale;
        if (this.gradeScale === LETTER_GRADE_TYPE) {
          this.letterGradeOptions = gradableData.letterGradeOptions.split(",");
        }

        this.isGroupGradable = gradableData.gradable.access === "GROUP";
        this.gradableTitle = gradableData.gradable.title;
        this.anonymousGrading = gradableData.gradable.anonymousGrading;
        this.closeTime = gradableData.gradable.closeTimeString;
        this.ltiGradableLaunch = gradableData.gradable.ltiGradableLaunch;
        this.groups = gradableData.groups;
        this.totalSubmissions = gradableData.totalSubmissions;
        this.totalGraded = gradableData.totalGraded;

        this.originalSubmissions = gradableData.submissions.map(s => new Submission(s, gradableData.groups, this.i18n));
        this.submissions = gradableData.submissions.map(s => new Submission(s, gradableData.groups, this.i18n));

        this.submissions.sort((a, b) => a.firstSubmitterName.localeCompare(b.firstSubmitterName));

        this.hasUnsubmitted = this.submissions.some(s => s.submittedTime == "");

        if (submissionId && !this.submission) {
          this.submission = this.submissions.find(s => s.id === submissionId);
        } else {
          this.submission = this.submissions[0];
        }

        this.submissions.forEach(s => {

          if (!s.graded) {
            this.hasUngraded = true;
          } else {
            this.hasGraded = true;
          }
        });

        this.loadingData = false;

        resolve(gradableData);
      })
      .catch(e => console.error(`Failed to load gradable data for ${gradableId}`, e));
    });
  }
};
