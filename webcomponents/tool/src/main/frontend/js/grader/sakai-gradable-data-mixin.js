import { Submission } from "./submission.js";
import { LETTER_GRADE_TYPE } from "./sakai-grader-constants.js";

export const gradableDataMixin = Base => class extends Base {

  _loadGradableData(gradableId, submissionId) {

    this.loadingData = true;

    // Grab all of the initial data we need, submissions and students. This will come from the grading service in future.
    return new Promise(resolve => {

      // Then, request the full set of data
      const url = `/direct/assignment/gradable.json?gradableId=${gradableId}&submissionId=${submissionId}`;
      fetch(url, { cache: "no-cache", credentials: "same-origin" })
      .then(r => {

        if (r.ok) {
          return r.json();
        }
        throw new Error(`Network error while loading gradable from ${url}`);
      })
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

        this.totalGraded = 0;
        gradableData.submissions.forEach(s => {

          if (!s.graded) {
            this.hasUngraded = true;
          } else {
            this.hasGraded = true;
            this.totalGraded += 1;
          }
        });

        this.originalSubmissions = gradableData.submissions.map(s => new Submission(s, gradableData.groups, this.i18n));
        this.submissions = gradableData.submissions.map(s => new Submission(s, gradableData.groups, this.i18n));

        this.submissions.sort((a, b) => a.firstSubmitterName.localeCompare(b.firstSubmitterName));

        this.hasUnsubmitted = this.submissions.some(s => !s.submitted);
        this.hasSubmitted = this.submissions.some(s => s.submitted);

        if (submissionId && !this.submission) {
          this.submission = this.submissions.find(s => s.id === submissionId);
        } else {
          this.submission = this.submissions[0];
        }

        this.loadingData = false;

        resolve(gradableData);
      })
      .catch(error => console.error(error));
    });
  }

  _fetchHydratedSubmissions(submissionIds, currentId) {

    if (!submissionIds.length) {
      return Promise.resolve();
    }

    return new Promise(resolve => {

      const url = `/direct/assignment/fullSubmissions.json?gradableId=${this.gradableId}&submissionIds=${submissionIds.join(",")}`;
      fetch(url, { credentials: "include", cache: "no-cache" })
      .then(r => {

        if (r.ok) {
          return r.json();
        }
        throw new Error(`Network error while getting full submissions from ${url}`);
      })
      .then(data => {

        const submissions = data.submissions.map(s => new Submission(s, data.groups, this.i18n));

        submissions.forEach(hydratedSubmission => {

          const i = this.submissions.findIndex(s => s.id === hydratedSubmission.id);
          this.submissions[i] = hydratedSubmission;
          this.originalSubmissions[i] = hydratedSubmission;
        });

        const requestedSubmissionId = this.currentSubmissionId || currentId;
        if (requestedSubmissionId) {
          this.currentSubmissionId = undefined;
          this.submission = this.submissions.find(s => s.id === requestedSubmissionId);
          resolve(this.submissions.find(s => s.id === requestedSubmissionId));
        } else {
          resolve();
        }
      })
      .catch (error => console.error(error));
    });
  }

  _hydrateNext(currentIndex) {

    let startIndex = currentIndex;

    for (let i = currentIndex; i < this.submissions.length; i++) {

      if (!this.submissions[i].hydrated) {
        startIndex = i;
        if (i > (currentIndex + 2)) {
          startIndex = -1;
        }
        break;
      }

      if (i === (this.submissions.length - 1)) startIndex = -1;
    }

    if (startIndex !== -1) {
      const submissionIds = [];

      submissionIds.push(this.submissions[startIndex].id);

      if ((startIndex + 1) < this.submissions.length && !this.submissions[startIndex + 1].hydrated) {
        submissionIds.push(this.submissions[startIndex + 1].id);
      }

      this.currentSubmissionId = this.submissions[currentIndex + 1].id;

      // Debounce the requests
      clearTimeout(this.hydrateTimer);
      this.hydrateTimer = setTimeout(() => {
        this._fetchHydratedSubmissions(submissionIds, this.submissions[currentIndex + 1].id);
      }, 250);
    }
  }

  _hydrateCluster(submissionId) {

    const i = this.submissions.findIndex(s => s.id === submissionId);

    const submissionIds = [];

    if ((i - 1) >= 0) {
      const leftSubmission = this.submissions[i - 1];
      !leftSubmission.hydrated && submissionIds.push(leftSubmission.id);
    }

    !this.submissions[i].hydrated && submissionIds.push(submissionId);

    if ((i + 1) < (this.submissions.length - 1)) {
      const rightSubmission = this.submissions[i + 1];
      !rightSubmission.hydrated && submissionIds.push(rightSubmission.id);
    }

    return this._fetchHydratedSubmissions(submissionIds, submissionId);
  }

  _hydratePrevious(currentIndex) {

    let startIndex = currentIndex;

    for (let i = currentIndex; i >= 0; i--) {

      if (!this.submissions[i].hydrated) {
        startIndex = i;
        if (i < (currentIndex - 2)) {
          startIndex = -1;
        }
        break;
      }

      if (i === 0) startIndex = -1;
    }

    if (startIndex !== -1) {
      const submissionIds = [];

      submissionIds.push(this.submissions[startIndex].id);

      if ((startIndex - 1) >= 0 && !this.submissions[startIndex - 1].hydrated) {
        submissionIds.push(this.submissions[startIndex - 1].id);
      }

      clearTimeout(this.hydrateTimer);
      this.hydrateTimer = setTimeout(() => {
        this._fetchHydratedSubmissions(submissionIds, this.submissions[currentIndex - 1].id);
      }, 250);
    }
  }
};
