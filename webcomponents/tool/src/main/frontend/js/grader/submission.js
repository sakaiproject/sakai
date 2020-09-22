export class Submission {

  constructor(init, groups) {

    if (init) {
      this.id = init.id;

      this.hasRubricEvaluation = init.hasRubricEvaluation;

      if (init.dateSubmitted) {
        this.submittedTime = moment.unix(init.dateSubmitted.epochSecond).format("M/D/YYYY @ H:mm");
        this.submittedText = init.submittedText;
      } else {
        this.submittedText = init.submittedText;
        this.submittedText = "No submission";
        this.submittedTime = "";
      }

      this.graded = init.graded;

      this.groupId = init.groupId;

      if (init.groupId) {
        this.groupTitle = groups.find(g => g.id === init.groupId).title;
        this.groupMembers = init.submitters.map(s => s.displayName).join(", ");
      }

      this.submittedAttachments = init.submittedAttachments || [];
      this.previewableAttachments = init.previewableAttachments || [];

      this.submitters = init.submitters;

      if (init.submitters) {
        this.firstSubmitterName = init.submitters[0].sortName;
        this.firstSubmitterId = init.submitters[0].id;
      }
      this.late = init.late;
      this.returned = init.returned;

      // This would be grader stuff, not the submission tool
      this.feedbackAttachments = init.feedbackAttachments;
      this.privateNotes = init.privateNotes || "";
      this.grade = init.grade;
      this.feedbackText = init.feedbackText;
      if (!this.feedbackText || this.feedbackText === "<p>null</p>") {
        this.feedbackText = this.submittedText || "";
      }
      this.feedbackComment = init.feedbackComment || "";

      this.resubmitsAllowed = parseInt(init.properties["allow_resubmit_number"] || 0);
      if (this.resubmitsAllowed > 0) {
        this.resubmitDate = moment(parseInt(init.properties["allow_resubmit_closeTime"], 10)).valueOf();
      }

      // We need this for setting the default resubmission date
      this.assignmentCloseTime = init.assignmentCloseTime.epochSecond * 1000;
    } else {
      this.id = "dummy";
    }
  }

  set resubmitsAllowed(value) {

    let old = this._resubmitsAllowed;
    this._resubmitsAllowed = value;
    if (old === 0 && value > 0) {
      // This is the first time resubmits have been allowed, so set the date to the
      // assignment's close date, by default.
      this.resubmitDate = moment(this.assignmentCloseTime).valueOf();
    }
  }

  get resubmitsAllowed() { return this._resubmitsAllowed; }
}
