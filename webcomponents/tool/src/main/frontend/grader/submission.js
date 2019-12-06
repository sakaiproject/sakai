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

      this.properties = init.properties;

      this.submittedAttachments = init.submittedAttachments || [];

      this.submitters = init.submitters;

      if (init.submitters) {
        this.firstSubmitterName = init.submitters[0].displayName;
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

      this.allowResubmitNumber = this.properties["allow_resubmit_number"];
    } else {
      this.id = "dummy";
    }
  }
}
