export class Submission {

  constructor(init, groups, i18n) {

    if (init) {
      this.id = init.id;

      if (init.properties) {
        // Build a history object for this submission
        this.history = {
          grades: init.properties["CHEF:submission_scaled_previous_grades"],
          comments: init.properties["CHEF:submission_previous_feedback_text"]
        };

        this.hasHistory = this.history.grades || this.history.comments;
      }

      this.hasRubricEvaluation = init.hasRubricEvaluation;

      if (init.dateSubmitted) {
        this.submittedTime = init.dateSubmitted;
        this.submittedText = init.submittedText;
      } else if (init.draft && init.visible) {
        this.submittedTime = i18n["draft_not_submitted"];
        this.submittedText = init.submittedText;
      } else {
        this.submittedText = i18n["no_submission"];
        this.submittedTime = "";
      }

      this.visible = init.visible;

      this.draft = init.draft;

      this.graded = init.graded;

      this.groupId = init.groupId;

      if (init.groupId) {
        const group = groups.find(g => g.id === init.groupId);
        this.groupTitle = group.title;
        this.groupRef = group.reference;
        this.groupMembers = init.submitters.map(s => s.displayName).join(", ");
      }

      this.submittedAttachments = init.submittedAttachments || [];
      this.previewableAttachments = init.previewableAttachments || [];

      this.submitters = init.submitters;

      this.ltiSubmissionLaunch = init.ltiSubmissionLaunch;

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
      if (this.resubmitsAllowed === -1 || this.resubmitsAllowed > 0) {
        this.resubmitDate = moment(parseInt(init.properties["allow_resubmit_closeTime"], 10)).valueOf();
      }


      // We need this for setting the default resubmission date
      this.assignmentCloseTime = init.assignmentCloseTime.epochSecond * 1000;
    } else {
      this.id = "dummy";
    }
  }

  set resubmitsAllowed(value) {

    const old = this._resubmitsAllowed;
    this._resubmitsAllowed = value;
    if (old === 0 && (value === -1 || value > 0)) {
      // This is the first time resubmits have been allowed, so set the date to the
      // assignment's close date, by default.
      this.resubmitDate = moment(this.assignmentCloseTime).valueOf();
    }
  }

  get resubmitsAllowed() { return this._resubmitsAllowed; }
}
