import moment from "moment/src/moment";

class Submission {

  constructor(init, groups, i18n, assignmentCloseTime) {

    if (init) {
      this.id = init.id;

      if (init.properties) {
        this.submissionLog = Object.keys(init.properties).filter(p => p.startsWith("log"))
          .map(p => init.properties[p]);
      }

      init.properties || (init.properties = {});
      init.feedbackComment || (init.feedbackComment = "");
      init.privateNotes || (init.privateNotes = "");
      this.submissionLog = init.submissionLog || [];

      if (init.properties) {
        // Build a history object for this submission
        this.history = {
          grades: init.properties["CHEF:submission_scaled_previous_grades"],
          comments: init.properties["CHEF:submission_previous_feedback_text"]
        };

        this.hasHistory = this.history.grades || this.history.comments;
      }

      this.peerReviews = init.peerReviews;

      this.hasRubricEvaluation = init.hasRubricEvaluation;
      this.showExtension = true;

      if (init.dateSubmitted) {
        this.submittedTime = init.dateSubmitted;
        this.submittedText = init.submittedText;
        this.showExtension = false;
      } else if (init.draft && init.visible) {
        this.submittedTime = i18n.draft_not_submitted;
        this.submittedText = init.submittedText;
      } else {
        this.submittedText = i18n.no_submission;
        this.submittedTime = "";
      }

      this.visible = init.visible;

      this.draft = init.draft;

      this.graded = init.graded;

      this.groupId = init.groupId;

      if (init.groupId) {
        const group = groups.find(g => g.id === init.groupId);
        this.groupTitle = group.title;
        this.groupMembers = init.submitters.map(s => s.displayName).join(", ");
      }

      this.submittedAttachments = init.submittedAttachments || [];
      this.previewableAttachments = init.previewableAttachments || [];

      this.submitters = init.submitters;

      this.ltiSubmissionLaunch = init.ltiSubmissionLaunch;

      if (init.submitters) {
        this.firstSubmitterName = `${init.submitters[0].sortName}${init.submitters[0].displayId !== null ? ` (${init.submitters[0].displayId})` : ""}`;
        this.firstSubmitterId = init.submitters[0].id;
      }
      this.late = init.late;
      this.returned = init.returned;

      // This would be grader stuff, not the submission tool
      this.feedbackAttachments = init.feedbackAttachments;
      this.privateNotes = init.privateNotes || "";
      this.grade = (typeof init.grade === "undefined") ? "" : init.grade;
      this.feedbackText = init.feedbackText;
      if (!this.feedbackText || this.feedbackText === "<p>null</p>") {
        this.feedbackText = this.submittedText || "";
      }
      this.feedbackComment = init.feedbackComment || "";

      this.resubmitsAllowed = parseInt(init.properties.allow_resubmit_number || 0);
      if (this.resubmitsAllowed === -1 || this.resubmitsAllowed > 0) {
        this.resubmitDate = moment(parseInt(init.properties.allow_resubmit_closeTime, 10)).valueOf();
      }
      this.extensionAllowed = init.properties.allow_extension_closeTime != null;
      if (this.extensionAllowed) {
        this.extensionDate = moment(parseInt(init.properties.allow_extension_closeTime, 10)).valueOf();
      }
      this.originalityServiceName = init.originalityServiceName;
      this.originalitySupplies = [];
      for (let index = 1; init[`originalityLink${index}`] != null; index++) {
        this.originalityShowing = true;
        const originalityDataNow = [];
        originalityDataNow.push(`originalityKey${index}`); //put in an ID for every set of originality data
        originalityDataNow.push(init[`originalityLink${index}`]);
        originalityDataNow.push(init[`originalityIcon${index}`]);
        originalityDataNow.push(init[`originalityScore${index}`]);
        if (init.properties[`originalityInline${index}`] === "true") {
          originalityDataNow.push(i18n.submission_inline);
        } else {
          originalityDataNow.push(init[`originalityName${index}`]);
        }
        originalityDataNow.push(init[`originalityInline${index}`]);
        originalityDataNow.push(init[`originalityStatus${index}`]);
        originalityDataNow.push(init[`originalityError${index}`]);
        this.originalitySupplies.push(originalityDataNow);
      }

      // We need this for setting the default resubmission date
      this.assignmentCloseTime = assignmentCloseTime.epochSecond * 1000;
      this.hydrated = init.hydrated;
      this.submitted = init.submitted;
      this.hasNonInlineAttachments = this.submittedAttachments.filter(r => r.type !== "text/html").length > 0;
    } else {
      this.id = "dummy";
      this.submittedAttachments = [];
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

  set extensionAllowed(value) {

    this._extensionAllowed = value;
    this.extensionDate = moment(this.assignmentCloseTime).valueOf();
  }

  get resubmitsAllowed() { return this._resubmitsAllowed; }

  get extensionAllowed() { return this._extensionAllowed; }
}

Submission.originalityConstants = {
  originalityKey: 0,
  originalityLink: 1,
  originalityIcon: 2,
  originalityScore: 3,
  originalityName: 4,
  originalityInline: 5,
  originalityStatus: 6,
  originalityError: 7
};

export { Submission };
