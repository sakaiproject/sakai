import {Submission} from "./submission.js";

let gradableDataMixin = Base => class extends Base {

  loadGradableData(gradableId, courseId, submissionId) {

    // Grab all of the initial data we need, submissions and students. This will come from the grading service in future.
    return new Promise((resolve, reject) => {

      fetch(`/direct/assignment/gradable.json?gradableId=${gradableId}`, {cache: "no-cache", credentials: "same-origin"})
        .then(res => res.json())
        .then(gradableData => {

          this.showOfficialPhoto = gradableData.showOfficialPhoto;

          this.gradable = gradableData.gradable;

          this.isGroupGradable = gradableData.gradable.access === "GROUP";

          this.gradableTitle = gradableData.gradable.title;

          this.anonymousGrading = gradableData.gradable.anonymousGrading;

          this.closeTime = gradableData.gradable.closeTimeString;

          this.groups = gradableData.groups;

          this.originalSubmissions = gradableData.submissions.map(s => new Submission(s, gradableData.groups));
          this.submissions = gradableData.submissions.map(s => new Submission(s, gradableData.groups));

          this.submissions.sort((a,b) => a.firstSubmitterName.localeCompare(b.firstSubmitterName));

          this.hasUnsubmitted = this.submissions.some(s => s.submittedTime == "");

          this.groups = gradableData.groups;

          // Load up the graded status for each submission. We need that for the next/prev ungraded type of navigations.
          fetch(`/direct/assignment/grades.json?gradableId=${gradableId}&courseId=${courseId}`, {cache: "no-cache", credentials: "same-origin"})
            .then(res => res.json())
            .then(gradesData => {

              this.students = gradesData.students;
              this.grades = gradesData.grades

              this.totalGraded = 0;
              this.submissions.forEach(s => {

                s.grade = gradesData.grades[s.id];

                if (!s.graded) {
                  this.hasUngraded = true;
                } else {
                  this.totalGraded += 1;
                }
              });

              this.originalSubmissions.forEach(s => s.grade = gradesData.grades[s.id] );

              if (submissionId) {
                this.submission = this.submissions.find(s => s.id === submissionId);
              } else {
                this.submission = this.submissions[0];
              }

              resolve(gradableData);
            })
            .catch(e => console.error(`Failed to load grades data for gradable id ${this.gradableId}`, e));

        }).catch(e => console.error(`Failed to load gradable data for ${gradableId}`, e));
    });
  }
}

export {gradableDataMixin};
