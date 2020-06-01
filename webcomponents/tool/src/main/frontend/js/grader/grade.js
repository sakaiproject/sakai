export class Grade {

  constructor(init) {

    if (init.dateSubmitted) {
      this.submittedTime = moment.unix(init.dateSubmitted.epochSecond).format("M/D/YYYY @ H:mm");
    }
    this.submittedText = init.submittedText;
    this.submittedAttachments = init.submittedAttachments || [];
    if (init.submitters) {
      this.firstSubmitterName = init.submitters[0].displayName;
    }
    this.late = init.late;
    this.returned = init.returned;
    this.feedbackAttachments = init.feedbackAttachments;
    this.privateNotes = init.privateNotes;
    this.grade = init.grade;
  }
}
