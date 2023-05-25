import { SakaiElement } from "/webcomponents/sakai-element.js";
import { gradableDataMixin } from "./sakai-gradable-data-mixin.js";
import { graderRenderingMixin } from "./sakai-grader-rendering-mixin.js";
import { Submission } from "./submission.js";
import { GRADE_CHECKED, LETTER_GRADE_TYPE, SCORE_GRADE_TYPE, PASS_FAIL_GRADE_TYPE, CHECK_GRADE_TYPE } from "./sakai-grader-constants.js";

export class SakaiGrader extends graderRenderingMixin(gradableDataMixin(SakaiElement)) {

  constructor() {

    super();

    this.sumittedTextMode = false;
    this.rubricParams = new Map();
    this.graderOnLeft = this.getSetting("grader", "graderOnLeft");
    this.saveSucceeded = false;
    this.saveFailed = false;
    this.submissions = [];
    this.ungradedOnly = false;
    this.submittedOnly = false;
    this.hasUngraded = false;
    this.hasUnsubmitted = false;
    this.resubmitNumber = "1";
    this.confirmedNotSavePvtNotes = false;
    this.confirmedNotSaveFeedback = false;
    this.savedPvtNotes = true;
    this.savedFeedbackComment = true;
    this.assignmentsI18n = {};
    this.i18nPromise = this.loadTranslations("grader");
    this.i18nPromise.then(t => this.i18n = t);
    this.loadTranslations("assignment").then(t => this.assignmentsI18n = t);

    if (typeof MathJax !== "undefined") {
      MathJax.Hub.Queue(["Typeset", MathJax.Hub]);
    }

    this.canSave = true;
    this.debug = true;
  }

  static get properties() {

    return {
      // Actual attributes
      gradableId: { attribute: "gradable-id", type: String },
      submissionId: { attribute: "submission-id", type: String },
      currentStudentId: { attribute: "current-student-id", type: String },
      gradableTitle: { attribute: "gradable-title", type: String },
      hasAssociatedRubric: { attribute: "has-associated-rubric", type: String },
      entityId: { attribute: "entity-id", type: String },
      toolId: { attribute: "tool-id", type: String },
      userListUrl: { attribute: "user-list-url", type: String },
      enablePdfExport: { attribute: "enable-pdf-export", type: Boolean },
      // State vars we want to trigger a render
      submittedTextMode: { attribute: false, type: Boolean },
      submission: { attribute: false, type: Object },
      nonEditedSubmission: { attribute: false, type: Object },
      graderOnLeft: { attribute: false, type: Boolean },
      selectedAttachment: { attribute: false, type: Object },
      saveSucceeded: { attribute: false, type: Boolean },
      saveFailed: { attribute: false, type: Boolean },
      savedPvtNotes: { attribute: false, type: Boolean },
      savedFeedbackComment: { attribute: false, type: Boolean },
      submissions: { attribute: false, type: Array },
      ungradedOnly: { attribute: false, type: Boolean },
      submissionsOnly: { attribute: false, type: Boolean },
      showResubmission: { attribute: false, type: Boolean },
      isChecked: { attribute: false, type: Boolean },
      allowExtension: { attribute: false, type: Boolean },
      totalGraded: { attribute: false, type: Number },
      token: { attribute: false, type: String },
      rubric: { attribute: false, type: Object },
      assignmentsI18n: { attribute: false, type: Object },
      showingHistory: { attribute: false, type: Boolean },
      ltiGradebleLaunch: { attribute: "lti-gradable-launch", type: String },
      showOverrides: { attribute: false, type: Boolean },
      canSave: { attribute: false, type: Boolean },
      rubricShowing: { attribute: false, type: Boolean },
      privateNotesEditorShowing: { attribute: false, type: Boolean },
      feedbackCommentEditorShowing: { attribute: false, type: Boolean },
      showingFullFeedbackComment: { attribute: false, type: Boolean },
      allFeedbackCommentVisible: { attribute: false, type: Boolean },
      showingFullPrivateNotes: { attribute: false, type: Boolean },
      allPrivateNotesVisible: { attribute: false, type: Boolean },
      privateNotesRemoved: { attribute: false, type: Boolean },
      feedbackCommentRemoved: { attribute: false, type: Boolean },
      showRemoveFeedbackComment: { attribute: false, type: Boolean },
    };
  }

  set gradableId(newValue) {

    this._gradableId = newValue;
    this.i18nPromise.then(() => this._loadData(newValue));
  }

  get gradableId() {
    return this._gradableId;
  }

  set submission(newValue) {

    if (!this.nonEditedSubmission || newValue.id !== this.nonEditedSubmission.id) {
      this.nonEditedSubmission = {};
      Object.assign(this.nonEditedSubmission, newValue);
    }

    this._submission = newValue;
    this.saveSucceeded = false;
    this.saveFailed = false;
    this.modified = false;
    this.rubricParams = new Map();
    this.showResubmission = this._submission.resubmitsAllowed === -1 || this._submission.resubmitsAllowed > 0;
    this.isChecked = newValue.grade === this.assignmentsI18n["gen.checked"] || newValue.grade === GRADE_CHECKED;
    this.allowExtension = this._submission.extensionAllowed;
    this.submittedTextMode = this._submission.submittedText;

    // If there's no submitted text and at least one attachment, show the first attachment
    // by default.
    if (!this.submittedTextMode && this._submission.submittedAttachments && this._submission.submittedAttachments.length > 0) {
      this.selectedAttachment = this._submission.submittedAttachments[0];
      const preview = this.submission.previewableAttachments[this.selectedAttachment.ref];
      this.selectedPreview = preview || this.selectedAttachment;
    }

    if (this.feedbackCommentEditor) {
      this.feedbackCommentEditor.setData(this._submission.feedbackComment, () => this.modified = false);
    }

    if (this.privateNotesEditor) {
      this.privateNotesEditor.setData(this._submission.privateNotes, () => this.modified = false);
    }

    this.querySelector("sakai-rubric-grading")?.setAttribute("evaluated-item-id", this._submission.id);
    this.requestUpdate();

    if (this.gradable.allowPeerAssessment) {
      this.updateComplete.then(() => $("#peer-info").popover());
    }

    // If any grade overrides have been set, check the overrides box
    this.showOverrides = this.submission.submitters?.some(s => s.overridden);

    this.showRemoveFeedbackComment = (typeof this._submission.feedbackComment !== "undefined");
    this.showingFullPrivateNotes = false;
    this.showingFullFeedbackComment = false;
  }

  get submission() {
    return this._submission;
  }

  shouldUpdate() {
    return this.i18n && this.submission;
  }

  firstUpdated() {

    this.feedbackCommentEditor = this._replaceWithEditor("grader-feedback-comment", data => {
      this.submission.feedbackComment = data;
    });

    this.privateNotesEditor = this._replaceWithEditor("grader-private-notes", data => {
      this.submission.privateNotes = data;
    });

    document.getElementById("grader").addEventListener('hide.bs.offcanvas', e => {

      const nFiles = this.querySelector("sakai-grader-file-picker")?.files.length;
      if (this.modified || nFiles) {
        e.preventDefault();
        this._save({ closeSidebarTimeout: 2000 });
      }
    });

    document.getElementById("grader").addEventListener('hidden.bs.offcanvas', () => {

      // Close all the collapses on the hidden event, so we don't have loads of sliding
      // about going on at once.
      bootstrap.Collapse.getInstance(document.getElementById("feedback-block"))?.hide();
      bootstrap.Collapse.getInstance(document.getElementById("private-notes-block"))?.hide();
      this.feedbackCommentRemoved = false;
      this.privateNotesRemoved = false;
      this._closeRubric();
    });

    this._setupVisibleFlags();
  }

  _setupVisibleFlags() {

    this.allFeedbackCommentVisible = !this._checkOverflow(this.querySelector("#feedback-snippet > .grader-snippet"));
    this.allPrivateNotesVisible = !this._checkOverflow(this.querySelector("#private-notes-snippet > .grader-snippet"));
  }

  _checkOverflow(el) {

    if (!el) return false;

    const curOverflow = el.style.overflow;

    if (!curOverflow || curOverflow === "visible") el.style.overflow = "hidden";

    const isOverflowing = el.clientWidth < el.scrollWidth
      || el.clientHeight < el.scrollHeight;

    el.style.overflow = curOverflow;

    return isOverflowing;
  }

  _getPhotoUserId() {

    if (this.submission.groupId || this.gradable.anonymousGrading || !this.submission.firstSubmitterId) {
      return "blank";
    }

    return this.submission.firstSubmitterId;
  }

  _closeRubric() {
    this.rubricShowing = false;
  }

  _doneWithRubric() {

    this.querySelector("#grader-rubric-link").focus();

    this.submission.grade = this.querySelector("sakai-rubric-grading").totalPoints.toString();
    this.querySelector("sakai-rubric-grading-button").setHasEvaluation();
    this.querySelector("sakai-rubric-evaluation-remover").setHasEvaluation();
    this.requestUpdate();

    this._closeRubric();
  }

  _replaceWithEditor(id, changedCallback) {

    const editor = sakai.editor.launch(id, {
      autosave: { delay: 10000000, messageType: "no" },
      startupFocus: true,
      toolbarSet: "Basic"
    });

    editor.on("change", e => {

      changedCallback && changedCallback(e.editor.getData());
      this.modified = true;
    });

    editor.on("instanceReady", e => {

      e.editor.dataProcessor.writer.setRules('p', {
        breakAfterClose: false
      });
    });

    return editor;
  }

  _toggleInlineFeedback(e, cancelling) {

    if (!this.feedbackTextEditor) {
      this.feedbackTextEditor = this._replaceWithEditor("grader-feedback-text-editor");
      this.feedbackTextEditor.setData(this.submission.feedbackText, () => this.modified = false);
      this.querySelector("#grader-feedback-text").style.display = "none";
      this.querySelector("#edit-inline-feedback-button").style.display = "none";
      this.querySelector("#show-inline-feedback-button").style.display = "block";
    } else {
      if (!cancelling) {
        this.submission.feedbackText = this.feedbackTextEditor.getData();
        this.requestUpdate();
      } else {
        this.feedbackTextEditor.setData(this.submission.feedbackText, () => this.modified = false);
      }

      this.feedbackTextEditor.destroy();
      this.feedbackTextEditor = undefined;
      this.querySelector("#grader-feedback-text").style.display = "block";
      this.querySelector("#edit-inline-feedback-button").style.display = "block";
      this.querySelector("#show-inline-feedback-button").style.display = "none";
      this.querySelector("#grader-feedback-text-editor").style.display = "none";
    }
  }

  _feedbackCommentPresentMsg() {
    return this.submission.feedbackComment && this.savedFeedbackComment ? this.i18n.comment_present : this.i18n.unsaved_comment_present;
  }

  _pvtNotePresentMsg() {
    return this.privateNotes && this.savedPvtNotes ? this.i18n.notes_present : this.i18n.unsaved_notes_present;
  }

  _toggleRubric() {
    this.rubricShowing = !this.rubricShowing;
  }

  _togglePrivateNotesEditor() {

    this.privateNotesEditorShowing = !this.privateNotesEditorShowing;

    if (!this.privateNotesEditorShowing) {

      this.showingFullPrivateNotes = false;
      this.allPrivateNotesVisible = false;
      this.updateComplete.then(() => this._setupVisibleFlags());
    }
  }

  _toggleFeedbackCommentEditor() {

    this.feedbackCommentEditorShowing = !this.feedbackCommentEditorShowing;

    this.showRemoveFeedbackComment
      = !this.feedbackCommentEditorShowing && (!this.modified
              || this.submission.feedbackComment === this.nonEditedSubmission.feedbackComment);

    if (!this.feedbackCommentEditorShowing) {

      this.showingFullFeedbackComment = false;
      this.allFeedbackCommentVisible = false;
      this.updateComplete.then(() => this._setupVisibleFlags());
    }
  }

  _previewAttachment(e) {

    e.preventDefault();
    this.selectedAttachment = this.submission.submittedAttachments.find(sa => sa.url === e.target.dataset.url);
    const type = this.selectedAttachment.type;

    if (type === "text/html") {
      this.submittedTextMode = true;
    } else {
      this.submittedTextMode = false;
      this.previewMode = true;
      let preview = this.submission.previewableAttachments[this.selectedAttachment.ref];
      preview = !preview && (type.startsWith("image/") || type.startsWith("video/") || this.previewMimetypes.includes(type)) ? this.selectedAttachment : preview;

      if (preview) {
        this.selectedPreview = preview;
      } else {
        this.selectedPreview = this.selectedAttachment; // If there's no preview, open in a new tab or download the attachment.

        window.open(this.selectedPreview.url, '_blank');
      }
    }
  }

  _addRubricParam(e, type) {

    let name = `rbcs-${e.detail.evaluatedItemId}-${e.detail.entityId}-${type}`;
    if ("totalpoints" !== type && "state-details" !== type) name += `-${e.detail.criterionId}`;
    this.rubricParams.set(name, type === "criterionrating" ? e.detail.ratingId : e.detail.value);
  }

  _onRubricRatingChanged(e) {

    this._addRubricParam(e, "criterion");
    this._addRubricParam(e, "criterionrating");
    this.submission.hasRubricEvaluation = true;
    this.requestUpdate();
  }

  _onRubricRatingsChanged() {
    this.modified = true;
  }

  _onRubricRatingTuned(e) {
    this._addRubricParam(e, "criterion-override");
  }

  _onRubricTotalPointsUpdated(e) {

    this.submission.grade = e.detail.value;
    this.requestUpdate();
  }

  _onEvaluationRemoved() {
    this.querySelector("sakai-rubric-grading-button").setHasEvaluation();
  }

  _onUpdateCriterionComment(e) {
    this._addRubricParam(e, "criterion-comment");
  }

  _loadData(gradableId) {

    const doIt = data => {
      this.gradeScale = data.gradable.gradeScale;

      if (this.gradeScale === LETTER_GRADE_TYPE) {
        this.letterGradeOptions = data.letterGradeOptions.split(",");
      }

      if (this.submissionId) {
        this.submission = this.submissions.find(s => s.id === this.submissionId);
      }

      this.requestUpdate();
    };

    if (!this.gradableDataLoader) {
      this.gradableDataLoader = this.loadGradableData(gradableId, portal.siteId, this.submissionId);
    }

    this.gradableDataLoader.then(data => doIt(data));
  }

  /**
   * Bundle up and all the needed stuff, like the grade, rubric, instructor comments and attachments.
   */
  _getFormData() {

    const formData = new FormData();
    formData.valid = true;
    this.querySelector("sakai-grader-file-picker").files.forEach((f, i) => formData.set(`attachment${i}`, f, f.name));
    formData.set("grade", this.submission.grade);
    this.querySelectorAll(".grader-grade-override").forEach(el => {
      if (el?.type !== "checkbox") {
        formData.set(`grade_submission_grade_${el.dataset.userId}`, el.value);
      } else if (el.checked) {
        formData.set(`grade_submission_grade_${el.dataset.userId}`, GRADE_CHECKED);
      }
    });

    if (this.gradeScale === SCORE_GRADE_TYPE && parseFloat(this.submission.grade.replace(",", ".")) > parseFloat(this.gradable.maxGradePoint.replace(",", "."))) {
      if (!confirm(this.tr("confirm_exceed_max_grade", [this.gradable.maxGradePoint], "grader"))) {
        formData.valid = false;
      } else {
        formData.set("grade", this.submission.grade);
      }
    }

    formData.set("feedbackText", this.submission.feedbackText);
    formData.set("feedbackComment", this.submission.feedbackComment);
    formData.set("privateNotes", this.submission.privateNotes);
    this.rubricParams.forEach((value, name) => formData.set(name, value));
    formData.set("studentId", this.submission.firstSubmitterId);
    formData.set("courseId", portal.siteId);
    formData.set("gradableId", this.gradableId);
    formData.set("submissionId", this.submission.id);

    if (this.showResubmission && this.submission.resubmitDate) {
      formData.set("resubmitNumber", this.submission.resubmitsAllowed);
      formData.set("resubmitDate", this.submission.resubmitDate);
    }

    if (this.allowExtension) {
      formData.set("extensionDate", this.submission.extensionDate);
    }

    formData.set("siteId", portal.siteId);

    if (this.debug) {
      // Display the key/value pairs
      for (const pair of formData.entries()) {
        console.debug(`${pair[0]}: ${pair[1]}`);
      }
    }

    return formData;
  }

  /**
   * Submit the data, optionally releasing based on a data attribute
   */
  _save(e) {

    const release = e.target ? e.target.dataset.release : false;
    const formData = this._getFormData();

    if (formData.valid) {
      formData.set("gradeOption", release ? "return" : "retract");
      this._submitGradingData(formData, e.closeSidebarTimeout);
      const rubricGrading = document.getElementsByTagName("sakai-rubric-grading").item(0);
      rubricGrading && (release ? rubricGrading.release() : rubricGrading.save());
      this.savedFeedbackComment = true;
      this.savedPvtNotes = true;
    }
  }

  _submitGradingData(formData, closeSidebarTimeout) {

    fetch("/direct/assignment/setGrade.json", {
      method: "POST",
      cache: "no-cache",
      credentials: "same-origin",
      body: formData
    })
    .then(r => r.json()).then(data => {

      const submission = new Submission(data, this.groups, this.i18n);
      submission.grade = formData.get("grade");
      this.querySelector("sakai-grader-file-picker").reset();
      this.submissions.splice(this.submissions.findIndex(s => s.id === submission.id), 1, submission);
      this.originalSubmissions.splice(this.originalSubmissions.findIndex(s => s.id === submission.id), 1, submission);
      this.modified = false;
      this.feedbackCommentRemoved = false;
      this.privateNotesRemoved = false;
      this.submission = submission;
      this.totalGraded = this.submissions.filter(s => s.graded).length;
      this.saveSucceeded = true;
      setTimeout(() => {

        this.saveSucceeded = false;
        const graderEl = document.getElementById("grader");
        bootstrap.Offcanvas.getInstance(graderEl).hide();
      }, closeSidebarTimeout || 1000);
    })
    .catch (e => {

      console.error(`Failed to save grade for submission ${this.submission.id}: ${e}`);
      this.saveFailed = true;
      setTimeout(() => this.saveFailed = false, 2000);
    });
  }

  _resetEditors(cancelling) {

    if (this.feedbackCommentEditor) {
      this.feedbackCommentEditor.setData(this.submission.feedbackComment, () => this.feedbackCommentEditor.resetDirty());
    }

    if (this.privateNotesEditor) {
      this.privateNotesEditor.setData(this.submission.privateNotes, () => this.privateNotesEditor.resetDirty());
    }

    if (this.inlineFeedbackMode) {
      this._toggleInlineFeedback(null, cancelling);
    }
  }

  _cancel() {

    const originalSubmission = Object.create(this.originalSubmissions.find(os => os.id === this.submission.id));
    const i = this.submissions.findIndex(s => s.id === this.submission.id);
    this.submissions.splice(i, 1, originalSubmission);
    this.submission = this.submissions[i];
    this._resetEditors(true);

    this.modified = false;

    switch (this.gradeScale) {
      case SCORE_GRADE_TYPE:
      {
        const input = document.getElementById("score-grade-input");
        input && (input.value = this.submission.grade);
        break;
      }
      case PASS_FAIL_GRADE_TYPE:
      {
        const input = document.getElementById("pass-fail-selector");
        input && (input.value = this.submission.grade);
        break;
      }
      case LETTER_GRADE_TYPE:
      {
        const input = document.getElementById("letter-grade-selector");
        input && (input.value = this.submission.grade);
        break;
      }
      case CHECK_GRADE_TYPE:
      {
        const input = document.getElementById("check-grade-input");
        input && (input.checked = this.submission.grade === this.assignmentsI18n["gen.checked"] || this.submission.grade === GRADE_CHECKED);
        break;
      }
      default:
    }

    bootstrap.Offcanvas.getInstance(document.getElementById("grader")).hide();
  }

  _clearSubmission() {

    const currentIndex = this.submissions.findIndex(s => s.id === this.submission.id);
    this.submissions[currentIndex] = this.nonEditedSubmission;
    this.submission = this.nonEditedSubmission;
    this.querySelector("sakai-grader-file-picker").reset();
    this.querySelector("sakai-rubric-grading")?.cancel();
    return true;
  }

  _toStudentList(e) {

    e.preventDefault();
    location.href = this.userListUrl;
  }

  _validateGradeInput(e) {

    if (e.key === "Tab") return;
    const decimalSeparator = 1.1.toLocaleString(portal.locale).substring(1, 2);
    const rgxp = new RegExp(`[\\d${decimalSeparator}]`);

    if (e.key === "Backspace" || e.key === "ArrowLeft" || e.key === "ArrowRight") {
      return true;
    } else if (!e.key.match(rgxp)) {
      e.preventDefault();
      return false;
    }

    const number = e.target.value.replace(",", ".");
    const numDecimals = number.includes(".") ? number.split(".")[1].length : 0; // If the user has highlighted the current entry, they want to replace it.

    if (numDecimals === 2 && e.target.selectionEnd - e.target.selectionStart < e.target.value.length) {
      e.preventDefault();
      return false;
    }
  }

  _gradeSelected(e) {

    if (this.gradeScale === CHECK_GRADE_TYPE) {
      if (e.target.checked) {
        this.submission.grade = GRADE_CHECKED;
      } else {
        this.submission.grade = "Unchecked";
      }

      this.isChecked = e.target.checked;
    } else {
      this.submission.grade = e.target.value;
    }

    this.modified = true;
  }

  _previous() {

    const currentIndex = this.submissions.findIndex(s => s.id === this.submission.id);

    if (currentIndex >= 1) {
      if (this.feedbackTextEditor) {
        this._toggleInlineFeedback(null, true);
      }

      this.submission = this.submissions[currentIndex - 1];
    }
  }

  _studentSelected(e) {
    this.submission = this.submissions.find(s => s.id === e.target.value);
  }

  _next() {

    const currentIndex = this.submissions.findIndex(s => s.id === this.submission.id);

    if (currentIndex < this.submissions.length - 1) {
      if (this.feedbackTextEditor) {
        this._toggleInlineFeedback(null, true);
      }

      this.submission = this.submissions[currentIndex + 1];
    }
  }

  _applyFilters(e) {

    e.stopPropagation();
    this.submissions = [...this.originalSubmissions];

    if (this.ungradedOnly) {
      this.submissions = this.submissions.filter(s => !s.graded);
    }

    if (this.submittedOnly) {
      this.submissions = this.submissions.filter(s => s.submittedTime !== "");
    }

    if (!this.ungradedOnly && !this.submittedOnly) {
      this.submissions = [...this.originalSubmissions];
    }

    if (this.currentGroup && this.currentGroup !== `/site/${portal.siteId}`) {
      const group = this.groups.find(g => g.reference === this.currentGroup);
      this.submissions = this.submissions.filter(s => group.users.includes(s.firstSubmitterId));
    }

    this.submissions.sort((a, b) => a.firstSubmitterName.localeCompare(b.firstSubmitterName));

    if (this.submissions.length > 0) {
      this.submission = this.submissions[0];
    } else {
      this.submission = new Submission();
    }

    this.totalGraded = this.submissions.filter(s => s.graded).length;
  }

  _submittedOnlyChanged(e) {
    this.submittedOnly = e.target.checked;
    this._applyFilters(e);
  }

  _areSettingsInAction() {
    return this.currentGroup && this.currentGroup !== `/site/${portal.siteId}` || this.submittedOnly || this.ungradedOnly;
  }

  _removeAttachment(e) {

    e.stopPropagation();
    e.preventDefault();

    if (confirm(this.i18n.confirm_remove_attachment)) {
      const ref = e.target.dataset.ref;
      fetch(`/direct/assignment/removeFeedbackAttachment?gradableId=${this.gradableId}&submissionId=${this.submission.id}&ref=${encodeURIComponent(ref)}`)
      .then(r => {

        if (r.status === 200) {
          this.submission.feedbackAttachments.splice(this.submission.feedbackAttachments.findIndex(fa => fa.ref === ref), 1);
          this.requestUpdate();
        }
      })
      .catch (error => console.error(`Failed to remove attachment on server: ${error}`));
    }
  }

  _groupSelected(e) {

    this.currentGroup = e.detail.value;
    this._applyFilters(e);
  }

  _ungradedOnlyChanged(e) {

    this.ungradedOnly = e.target.checked;
    this._applyFilters(e);
  }

  _resubmitDateSelected(e) {

    this.submission.resubmitDate = e.detail.epochMillis;
    this.modified = true;
  }

  _extensionDateSelected(e) {

    this.submission.extensionDate = e.detail.epochMillis;
    this.modified = true;
  }

  _toggleResubmissionBlock(e) {

    if (!e.target.checked) {
      this.submission.resubmitsAllowed = 0;
    } else {
      this.submission.resubmitsAllowed = 1;
    }

    this.showResubmission = e.target.checked;
  }

  _toggleExtensionBlock(e) {

    this.submission.extensionAllowed = !e.target.checked;
    this.allowExtension = e.target.checked;
  }

  _removePrivateNotes() {

    this.submission.privateNotes = "";
    this.privateNotesEditor && this.privateNotesEditor.setData("");
    this.modified = true;
    this.privateNotesRemoved = true;
    this.requestUpdate();
  }

  _removeFeedbackComment() {

    this.submission.feedbackComment = "";
    this.feedbackCommentEditor && this.feedbackCommentEditor.setData("");
    this.modified = true;
    this.feedbackCommentRemoved = true;
  }

  _toggleFullFeedbackComment() {
    this.showingFullFeedbackComment = !this.showingFullFeedbackComment;
  }

  _toggleFullPrivateNotes() {
    this.showingFullPrivateNotes = !this.showingFullPrivateNotes;
  }
}
const tagName = "sakai-grader";
!customElements.get(tagName) && customElements.define(tagName, SakaiGrader);
