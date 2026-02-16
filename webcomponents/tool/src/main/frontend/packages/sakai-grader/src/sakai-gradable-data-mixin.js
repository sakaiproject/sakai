import { Submission } from "./submission.js";
import { LETTER_GRADE_TYPE } from "./sakai-grader-constants.js";

export const gradableDataMixin = Base => class extends Base {

  _loadGradableData(gradableId, submissionId) {

    this._loadingData = true;

    // Grab all of the initial data we need, submissions and students. This will come from the grading service in future.
    return new Promise(resolve => {

      // Then, request the full set of data
      const url = `/direct/assignment/gradable.json?gradableId=${gradableId}${submissionId ? `&submissionId=${submissionId}` : ""}`;
      fetch(url, { cache: "no-cache" })
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
        this.ltiFrameHeight = gradableData.gradable.ltiFrameHeight;
        this.groups = gradableData.groups.filter(g => g.users?.length);
        this._totalSubmissions = gradableData.totalSubmissions;

        this._totalGraded = 0;
        gradableData.submissions.forEach(s => {

          if (!s.graded) {
            this.hasUngraded = true;
          } else {
            this._hasGraded = true;
            this._totalGraded += 1;
          }
        });

        this.originalSubmissions = gradableData.submissions.map(s => new Submission(s, gradableData.groups, this._i18n, gradableData.gradable.closeTime));
        this._submissions = gradableData.submissions.map(s => new Submission(s, gradableData.groups, this._i18n, gradableData.gradable.closeTime));

        this._submissions.sort((a, b) => {
          const nameA = a.groupId ? a.groupTitle : a.firstSubmitterName;
          const nameB = b.groupId ? b.groupTitle : b.firstSubmitterName;
          return nameA.localeCompare(nameB);
        });

        this.hasUnsubmitted = this._submissions.some(s => !s.submitted);
        this.hasSubmitted = this._submissions.some(s => s.submitted);

        if (submissionId && !this._submission) {
          this._submission = this._submissions.find(s => s.id === submissionId);
        } else {
          this._submission = this._submissions[0];
        }

        this._loadingData = false;

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

        const submissions = data.submissions.map(s => new Submission(s, data.groups, this._i18n, data.assignmentCloseTime));

        submissions.forEach(hydratedSubmission => {

          const i = this._submissions.findIndex(s => s.id === hydratedSubmission.id);
          this._submissions[i] = hydratedSubmission;
          this.originalSubmissions[i] = hydratedSubmission;
        });

        const requestedSubmissionId = this.currentSubmissionId || currentId;
        if (requestedSubmissionId) {
          this.currentSubmissionId = undefined;
          this._submission = this._submissions.find(s => s.id === requestedSubmissionId);
          resolve(this._submissions.find(s => s.id === requestedSubmissionId));
        } else {
          resolve();
        }
      })
      .catch (error => console.error(error));
    });
  }

  // This routine insures that the next submission is hydrated, but also retrieves up to
  // two more submissions (look ahead) to deal with the fact the sometimes the user presses
  // on the "next" button repeatedly.  This look ahead balances retrieving too much data with
  // making it so in the normal case where next is pressed slowly, the next submission (or two)
  // is/are already pre-hydrated.
  _hydrateNext(currentIndex) {

    let startIndex = currentIndex;

    for (let i = currentIndex; i < this._submissions.length; i++) {

      if (!this._submissions[i].hydrated) {
        startIndex = i;
        if (i > (currentIndex + 2)) {
          startIndex = -1;
        }
        break;
      }

      if (i === (this._submissions.length - 1)) startIndex = -1;
    }

    if (startIndex !== -1) {
      const submissionIds = [];

      submissionIds.push(this._submissions[startIndex].id);

      if ((startIndex + 1) < this._submissions.length && !this._submissions[startIndex + 1].hydrated) {
        submissionIds.push(this._submissions[startIndex + 1].id);
      }

      this.currentSubmissionId = this._submissions[currentIndex + 1].id;

      // Debounce the requests
      clearTimeout(this.hydrateTimer);
      this.hydrateTimer = setTimeout(() => {
        this._fetchHydratedSubmissions(submissionIds, this._submissions[currentIndex + 1].id);
      }, 250);
    }
  }

  _hydrateCluster(submissionId) {

    const i = this._submissions.findIndex(s => s.id === submissionId);

    const submissionIds = [];

    if ((i - 1) >= 0) {
      const leftSubmission = this._submissions[i - 1];
      !leftSubmission.hydrated && submissionIds.push(leftSubmission.id);
    }

    !this._submissions[i]?.hydrated && submissionIds.push(submissionId);

    if ((i + 1) < (this._submissions.length - 1)) {
      const rightSubmission = this._submissions[i + 1];
      !rightSubmission.hydrated && submissionIds.push(rightSubmission.id);
    }

    return this._fetchHydratedSubmissions(submissionIds, submissionId);
  }

  _hydratePrevious(currentIndex) {

    let startIndex = currentIndex;

    for (let i = currentIndex; i >= 0; i--) {

      if (!this._submissions[i].hydrated) {
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

      submissionIds.push(this._submissions[startIndex].id);

      if ((startIndex - 1) >= 0 && !this._submissions[startIndex - 1].hydrated) {
        submissionIds.push(this._submissions[startIndex - 1].id);
      }

      clearTimeout(this.hydrateTimer);
      this.hydrateTimer = setTimeout(() => {
        this._fetchHydratedSubmissions(submissionIds, this._submissions[currentIndex - 1].id);
      }, 250);
    }
  }
};
