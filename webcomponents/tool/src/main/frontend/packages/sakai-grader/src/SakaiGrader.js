import { SakaiElement } from "@sakai-ui/sakai-element";
import { gradableDataMixin } from "./sakai-gradable-data-mixin.js";
import { graderRenderingMixin } from "./sakai-grader-rendering-mixin.js";
import { Submission } from "./submission.js";
import { GRADE_CHECKED, LETTER_GRADE_TYPE, SCORE_GRADE_TYPE, PASS_FAIL_GRADE_TYPE, CHECK_GRADE_TYPE } from "./sakai-grader-constants.js";

export class SakaiGrader extends graderRenderingMixin(gradableDataMixin(SakaiElement)) {

  static properties = {

    gradableId: { attribute: "gradable-id", type: String },
    submissionId: { attribute: "submission-id", type: String },
    currentStudentId: { attribute: "current-student-id", type: String },
    gradableTitle: { attribute: "gradable-title", type: String },
    hasAssociatedRubric: { attribute: "has-associated-rubric", type: String },
    entityId: { attribute: "entity-id", type: String },
    toolId: { attribute: "tool-id", type: String },
    userListUrl: { attribute: "user-list-url", type: String },
    enablePdfExport: { attribute: "enable-pdf-export", type: Boolean },
    ltiGradebleLaunch: { attribute: "lti-gradable-launch", type: String },

    _saving: { state: true },
    _submittedTextMode: { state: true },
    _submission: { state: true },
    _nonEditedSubmission: { state: true },
    _selectedAttachment: { state: true },
    _selectedPreview: { state: true },
    _saveSucceeded: { state: true },
    _saveFailed: { state: true },
    _submissions: { state: true },
    _gradedOnly: { state: true },
    _ungradedOnly: { state: true },
    _showResubmission: { state: true },
    _isChecked: { state: true },
    _allowExtension: { state: true },
    _totalGraded: { state: true },
    _totalSubmissions: { state: true },
    _showingHistory: { state: true },
    _showOverrides: { state: true },
    _rubricShowing: { state: true },
    _privateNotesEditorShowing: { state: true },
    _feedbackCommentEditorShowing: { state: true },
    _showingFullFeedbackComment: { state: true },
    _allFeedbackCommentVisible: { state: true },
    _showingFullPrivateNotes: { state: true },
    _allPrivateNotesVisible: { state: true },
    _privateNotesRemoved: { state: true },
    _feedbackCommentRemoved: { state: true },
    _showRemoveFeedbackComment: { state: true },
    _loadingData: { state: true },
    _hasGraded: { state: true },
  };

  constructor() {

    super();

    this.rubricParams = new Map();
    this._submissions = [];
    this.resubmitNumber = "1";
    this.i18nPromise = this.loadTranslations("grader");
    this.i18nPromise.then(t => this.i18n = t);

    if (typeof MathJax !== "undefined") {
      MathJax.Hub.Queue([ "Typeset", MathJax.Hub ]);
    }
  }

  set gradableId(value) {

    const oldValue = this._gradableId;
    this._gradableId = value;

    if (this.submissionId) {
      this._loadData(value, this.submissionId);
    }

    this.requestUpdate("gradableId", oldValue);
  }

  get gradableId() { return this._gradableId; }

  set submissionId(value) {

    const oldValue = this._submissionId;
    this._submissionId = value;

    if (this.gradableId) {
      this._loadData(this.gradableId, value);
    }

    this.requestUpdate("submissionId", oldValue);
  }

  get submissionId() { return this._submissionId; }

  set _submission(newValue) {

    if (!this._nonEditedSubmission || newValue.id !== this._nonEditedSubmission.id) {
      this._nonEditedSubmission = {};
      Object.assign(this._nonEditedSubmission, newValue);
    }

    this.__submission = newValue;
    this._saveSucceeded = false;
    this._saveFailed = false;
    this.modified = false;
    this.rubricParams = new Map();
    this._showResubmission = this.__submission.resubmitsAllowed === -1 || this.__submission.resubmitsAllowed > 0;
    this._isChecked = newValue.grade === this.i18n["gen.checked"] || newValue.grade === GRADE_CHECKED;
    this._allowExtension = this.__submission.extensionAllowed;
    this._submittedTextMode = this.__submission.submittedText;

    // If there's no submitted text and at least one attachment, show the first attachment
    // by default.
    if (!this._submittedTextMode && this.__submission.submittedAttachments && this.__submission.submittedAttachments.length > 0) {
      this._selectedAttachment = this.__submission.submittedAttachments[0];
      const preview = this.__submission.previewableAttachments[this._selectedAttachment.ref];
      this._selectedPreview = preview || this._selectedAttachment;
    }

    if (this.feedbackCommentEditor && this.__submission.feedbackComment) {
      this.feedbackCommentEditor.setData(this.__submission.feedbackComment, () => this.modified = false);
    }

    if (this.privateNotesEditor && this.__submission.privateNotes) {
      this.privateNotesEditor.setData(this.__submission.privateNotes, () => this.modified = false);
    }

    this.querySelector("sakai-rubric-grading")?.setAttribute("evaluated-item-id", this.__submission.id);
    this.requestUpdate();

    if (this.gradable.allowPeerAssessment) {
      this.updateComplete.then(() => (new bootstrap.Popover(this.querySelector("#peer-info"))));
    }

    // If any grade overrides have been set, check the overrides box
    this._showOverrides = this.__submission.submitters?.some(s => s.overridden);

    this._showRemoveFeedbackComment = (typeof this.__submission.feedbackComment !== "undefined");
    this._showingFullPrivateNotes = false;
    this._showingFullFeedbackComment = false;
  }

  get _submission() { return this.__submission; }

  _loadData(gradableId, submissionId) {

    this.i18nPromise.then(() => {
      this._loadGradableData(gradableId, submissionId).then(() => this._setup());
    });
  }

  shouldUpdate() {
    return this.i18n;
  }

  _setup() {

    this.feedbackCommentEditor = this._replaceWithEditor("grader-feedback-comment", data => {
      this._submission.feedbackComment = data;
    });

    this.privateNotesEditor = this._replaceWithEditor("grader-private-notes", data => {
      this._submission.privateNotes = data;
    });

    document.getElementById("grader").addEventListener("hide.bs.offcanvas", e => {

      if (this.modified || this.querySelector("sakai-grader-file-picker")?.hasFiles()) {
        e.preventDefault();
        this._save({ closeSidebarTimeout: 2000 });
      }
    });

    document.getElementById("grader").addEventListener("hide.bs.offcanvas", () => {

      this.querySelectorAll(".rubric-comment-trigger").forEach(trigger => {
        bootstrap.Popover.getInstance(trigger).hide();
      });
    });

    document.getElementById("grader").addEventListener("hidden.bs.offcanvas", () => {

      // Close all the collapses on the hidden event, so we don't have loads of sliding
      // about going on at once.
      bootstrap.Collapse.getInstance(document.getElementById("feedback-block"))?.hide();
      bootstrap.Collapse.getInstance(document.getElementById("private-notes-block"))?.hide();
      this._feedbackCommentRemoved = false;
      this._privateNotesRemoved = false;
      this._closeRubric();

      bootstrap.Collapse.getInstance(document.getElementById("feedback-block"))?.hide();
    });

    this._setupVisibleFlags();
  }

  _setupVisibleFlags() {

    this._allFeedbackCommentVisible = !this._checkOverflow(this.querySelector("#feedback-snippet > .grader-snippet"));
    this._allPrivateNotesVisible = !this._checkOverflow(this.querySelector("#private-notes-snippet > .grader-snippet"));
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

    if (this._submission.groupId || this.gradable.anonymousGrading || !this._submission.firstSubmitterId) {
      return "blank";
    }

    return this._submission.firstSubmitterId;
  }

  _closeRubric() {

    this._rubricShowing = false;
    this.querySelector("sakai-rubric-grading")?.displayGradingTab();
  }

  _doneWithRubric() {

    this.querySelector("#grader-rubric-link").focus();

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

      e.editor.dataProcessor.writer.setRules("p", {
        breakAfterClose: false
      });
    });

    return editor;
  }

  _toggleInlineFeedback(e, cancelling) {

    if (!this.feedbackTextEditor) {
      this.feedbackTextEditor = this._replaceWithEditor("grader-feedback-text-editor");
      this.feedbackTextEditor.setData(this._submission.feedbackText, () => this.modified = false);
      this.querySelector("#grader-feedback-text").style.display = "none";
      this.querySelector("#edit-inline-feedback-button").style.display = "none";
      this.querySelector("#show-inline-feedback-button").style.display = "block";
    } else {
      if (!cancelling) {
        this._submission.feedbackText = this.feedbackTextEditor.getData();
        bootstrap.Offcanvas.getOrCreateInstance(document.getElementById("grader")).show();
        this.requestUpdate();
      } else {
        this.feedbackTextEditor.setData(this._submission.feedbackText, () => this.modified = false);
      }

      this.feedbackTextEditor.destroy();
      this.feedbackTextEditor = undefined;
      this.querySelector("#grader-feedback-text").style.display = "block";
      this.querySelector("#edit-inline-feedback-button").style.display = "block";
      this.querySelector("#show-inline-feedback-button").style.display = "none";
      this.querySelector("#grader-feedback-text-editor").style.display = "none";
    }
  }

  _toggleRubric() {
    this._rubricShowing = !this._rubricShowing;
  }

  _togglePrivateNotesEditor() {

    this._privateNotesEditorShowing = !this._privateNotesEditorShowing;

    if (!this._privateNotesEditorShowing) {

      this._showingFullPrivateNotes = false;
      this._allPrivateNotesVisible = false;
      this.updateComplete.then(() => this._setupVisibleFlags());
    }
  }

  _toggleFeedbackCommentEditor() {

    this._feedbackCommentEditorShowing = !this._feedbackCommentEditorShowing;

    this._showRemoveFeedbackComment
      = !this._feedbackCommentEditorShowing && (!this.modified
              || this._submission.feedbackComment === this._nonEditedSubmission.feedbackComment);

    if (!this._feedbackCommentEditorShowing) {

      this._showingFullFeedbackComment = false;
      this._allFeedbackCommentVisible = false;
      this.updateComplete.then(() => this._setupVisibleFlags());
    }
  }

  _previewAttachment(e) {

    e.preventDefault();

    this._selectedAttachment = this._submission.submittedAttachments.find(sa => sa.ref === e.target.dataset.ref);
    const type = this._selectedAttachment.type;

    this._submittedTextMode = false;
    this.previewMode = true;
    let preview = this._submission.previewableAttachments[this._selectedAttachment.ref];
    preview = !preview && (type.startsWith("image/") || type === "text/html" || type.startsWith("video/") || this.previewMimetypes.includes(type)) ? this._selectedAttachment : preview;

    if (preview) {
      this._selectedPreview = preview;
    } else {
      this._selectedPreview = this._selectedAttachment; // If there's no preview, open in a new tab or download the attachment.

      window.open(this._selectedPreview.url, "_blank");
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
    this._submission.hasRubricEvaluation = true;
    this.requestUpdate();
  }

  _onRubricRatingsChanged() {
    this.modified = true;
  }

  _onRubricRatingTuned(e) {
    this._addRubricParam(e, "criterion-override");
  }

  _onRubricTotalPointsUpdated(e) {

    this._submission.grade = e.detail.value;
    this.requestUpdate();
  }

  _onEvaluationRemoved() {

    this.querySelector("sakai-rubric-grading").clear();
    this.querySelector("sakai-rubric-grading-button").setHasEvaluation();
  }

  _onUpdateCriterionComment(e) {
    this._addRubricParam(e, "criterion-comment");
  }

  /**
   * Bundle up and all the needed stuff, like the grade, rubric, instructor comments and attachments.
   */
  _getFormData() {

    const formData = new FormData();
    formData.valid = true;
    this.querySelector("sakai-grader-file-picker").getFiles().forEach((f, i) => formData.set(`attachment${i}`, f, f.name));
    formData.set("grade", this._submission.grade);
    this.querySelectorAll(".grader-grade-override").forEach(el => {
      if (el?.type !== "checkbox") {
        formData.set(`grade_submission_grade_${el.dataset.userId}`, el.value);
      } else if (el.checked) {
        formData.set(`grade_submission_grade_${el.dataset.userId}`, GRADE_CHECKED);
      }
    });

    if (this.gradeScale === SCORE_GRADE_TYPE && parseFloat(this._submission.grade.replace(",", ".")) > parseFloat(this.gradable.maxGradePoint.replace(",", "."))) {
      if (!confirm(this.tr("confirm_exceed_max_grade", [ this.gradable.maxGradePoint ], "grader"))) {
        formData.valid = false;
      } else {
        formData.set("grade", this._submission.grade);
      }
    }

    formData.set("feedbackText", this._submission.feedbackText);
    formData.set("feedbackComment", this._submission.feedbackComment);
    formData.set("privateNotes", this._submission.privateNotes);
    this.rubricParams.forEach((value, name) => formData.set(name, value));
    formData.set("studentId", this._submission.firstSubmitterId);
    formData.set("courseId", portal.siteId);
    formData.set("gradableId", this.gradableId);
    formData.set("submissionId", this._submission.id);

    if (this._showResubmission && this._submission.resubmitDate) {
      formData.set("resubmitNumber", this._submission.resubmitsAllowed);
      formData.set("resubmitDate", this._submission.resubmitDate);
    }

    if (this._allowExtension) {
      formData.set("extensionDate", this._submission.extensionDate);
    }

    formData.set("siteId", portal.siteId);

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
    }
  }

  _submitGradingData(formData, closeSidebarTimeout) {

    this._saving = true;

    fetch("/direct/assignment/setGrade.json", {
      method: "POST",
      cache: "no-cache",
      credentials: "same-origin",
      body: formData
    })
    .then(r => r.json()).then(data => {

      const submission = new Submission(data.submission, this.groups, this.i18n, data.assignmentCloseTime);
      submission.grade = formData.get("grade");
      this._hasGraded = true;
      this.querySelector("sakai-grader-file-picker").reset();
      this._submissions.splice(this._submissions.findIndex(s => s.id === submission.id), 1, submission);
      this.originalSubmissions.splice(this.originalSubmissions.findIndex(s => s.id === submission.id), 1, submission);
      this.modified = false;
      this._feedbackCommentRemoved = false;
      this._privateNotesRemoved = false;
      this._submission = submission;
      this._totalGraded = this._submissions.filter(s => s.graded).length;
      this._saving = false;
      this._saveSucceeded = true;
      setTimeout(() => {

        this._saveSucceeded = false;
        const graderEl = document.getElementById("grader");
        bootstrap.Offcanvas.getInstance(graderEl).hide();
      }, closeSidebarTimeout || 1000);
    })
    .catch (e => {

      console.error(`Failed to save grade for submission ${this._submission.id}: ${e}`);
      this._saveFailed = true;
      setTimeout(() => this._saveFailed = false, 2000);
    });
  }

  _resetEditors(cancelling) {

    if (this.feedbackCommentEditor) {
      this.feedbackCommentEditor.setData(this._submission.feedbackComment, () => this.feedbackCommentEditor.resetDirty());
    }

    if (this.privateNotesEditor) {
      this.privateNotesEditor.setData(this._submission.privateNotes, () => this.privateNotesEditor.resetDirty());
    }

    if (this.inlineFeedbackMode) {
      this._toggleInlineFeedback(null, cancelling);
    }
  }

  _cancel() {

    const originalSubmission = Object.create(this.originalSubmissions.find(os => os.id === this._submission.id));
    const i = this._submissions.findIndex(s => s.id === this._submission.id);
    this._submissions.splice(i, 1, originalSubmission);
    this._submission = this._submissions[i];
    this._resetEditors(true);

    this.modified = false;

    switch (this.gradeScale) {
      case SCORE_GRADE_TYPE: {
        const input = document.getElementById("score-grade-input");
        input && (input.value = this._submission.grade);
        break;
      }
      case PASS_FAIL_GRADE_TYPE: {
        const input = document.getElementById("pass-fail-selector");
        input && (input.value = this._submission.grade);
        break;
      }
      case LETTER_GRADE_TYPE: {
        const input = document.getElementById("letter-grade-selector");
        input && (input.value = this._submission.grade);
        break;
      }
      case CHECK_GRADE_TYPE: {
        const input = document.getElementById("check-grade-input");
        input && (input.checked = this._submission.grade === this.i18n["gen.checked"] || this._submission.grade === GRADE_CHECKED);
        break;
      }
      default:
    }

    bootstrap.Offcanvas.getInstance(document.getElementById("grader")).hide();
  }

  _clearSubmission() {

    const currentIndex = this._submissions.findIndex(s => s.id === this._submission.id);
    this._submissions[currentIndex] = this._nonEditedSubmission;
    this._submission = this._nonEditedSubmission;
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
    } else if (e.key && !e.key.match(rgxp)) {
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
        this._submission.grade = GRADE_CHECKED;
      } else {
        this._submission.grade = "Unchecked";
      }

      this._isChecked = e.target.checked;
    } else {
      this._submission.grade = e.target.value;
    }

    this.modified = true;
  }

  _previous() {

    const currentIndex = this._submissions.findIndex(s => s.id === this._submission.id);

    if (currentIndex >= 1) {
      if (this.feedbackTextEditor) {
        this._toggleInlineFeedback(null, true);
      }

      this._hydratePrevious(currentIndex);

      this._submission = this._submissions[currentIndex - 1];
    }
  }

  _studentSelected(e) {

    const test = this._submissions.find(s => s.id === e.target.value);
    if (!test.hydrated) {
      this._hydrateCluster(test.id).then(s => this._submission = s);
    } else {
      this._submission = this._submissions.find(s => s.id === e.target.value);
    }
  }

  _next() {

    const currentIndex = this._submissions.findIndex(s => s.id === this._submission.id);

    if (currentIndex < this._submissions.length - 1) {
      if (this.feedbackTextEditor) {
        this._toggleInlineFeedback(null, true);
      }

      this._hydrateNext(currentIndex);

      this._submission = this._submissions[currentIndex + 1];
    }
  }

  _applyFilters() {

    let filtered = [ ...this.originalSubmissions ];

    if (this._submittedOnly) {
      if (this._ungradedOnly) {
        filtered = filtered.filter(s => s.submitted && !s.graded);
      } else if (this._gradedOnly) {
        filtered = filtered.filter(s => s.submitted && s.graded);
      } else {
        filtered = filtered.filter(s => s.submitted);
      }
    } else if (this._ungradedOnly) {
      filtered = filtered.filter(s => !s.graded);
    } else if (this._gradedOnly) {
      filtered = filtered.filter(s => s.graded);
    }

    if (this.currentGroups && this.currentGroups.length === 1 && this.currentGroups[0].includes("/group")) {
      const group = this.groups.find(g => g.reference === this.currentGroups[0]);
      filtered = filtered.filter(s => group.users.includes(s.firstSubmitterId));
    }

    if (filtered.length > 0) {
      const firstSubmissionId = filtered[0].id;
      this._hydrateCluster(firstSubmissionId).then(submission => {

        if (submission) {
          this._submissions = [ ...filtered ];
          this._submission = submission;
        }
      });
    } else {
      this._submission = new Submission();
    }

    this._totalGraded = filtered.filter(s => s.graded).length;
    this._totalSubmissions = filtered.length;

    this._submissions = [ ...filtered ];
  }

  _submittedOnlyChanged(e) {

    this._submittedOnly = e.target.checked;
    this._applyFilters();
  }

  _areSettingsInAction() {
    return (this.currentGroups && this.currentGroups.length > 0 && this.currentGroups[0] !== `/site/${portal.siteId}`) || this._submittedOnly || this._ungradedOnly || this._gradedOnly;
  }

  _getSubmitter(submission) {
    return submission.groupId ? submission.groupTitle : submission.firstSubmitterName;
  }

  _removeAttachment(e) {

    e.stopPropagation();
    e.preventDefault();

    if (confirm(this.i18n.confirm_remove_attachment)) {
      const ref = e.target.dataset.ref;
      fetch(`/direct/assignment/removeFeedbackAttachment?gradableId=${this.gradableId}&submissionId=${this._submission.id}&ref=${encodeURIComponent(ref)}`)
      .then(r => {

        if (r.status === 200) {
          this._submission.feedbackAttachments.splice(this._submission.feedbackAttachments.findIndex(fa => fa.ref === ref), 1);
          this.requestUpdate();
        }
      })
      .catch (error => console.error(`Failed to remove attachment on server: ${error}`));
    }
  }

  _groupsSelected(e) {

    this.currentGroups = e.detail.value;
    if (this.currentGroups.length === 1 && !this.currentGroups[0].includes("/group")) {
      this.currentGroups = [];
    }
    this._applyFilters();
  }

  _gradedStatusSelected(e) {

    switch (e.target.value) {
      case "ungraded":
        this._ungradedOnly = true;
        this._gradedOnly = false;
        break;
      case "graded":
        this._gradedOnly = true;
        this._ungradedOnly = false;
        break;
      default:
        this._ungradedOnly = false;
        this._gradedOnly = false;
    }
    this._applyFilters();
  }

  _resubmitDateSelected(e) {

    this._submission.resubmitDate = e.detail.epochMillis;
    this.modified = true;
  }

  _extensionDateSelected(e) {

    this._submission.extensionDate = e.detail.epochMillis;
    this.modified = true;
  }

  _toggleResubmissionBlock(e) {

    if (!e.target.checked) {
      this._submission.resubmitsAllowed = 0;
    } else {
      this._submission.resubmitsAllowed = 1;
    }

    this._showResubmission = e.target.checked;
  }

  _toggleExtensionBlock(e) {

    this._submission.extensionAllowed = !e.target.checked;
    this._allowExtension = e.target.checked;
  }

  _removePrivateNotes() {

    this._submission.privateNotes = "";
    this.privateNotesEditor && this.privateNotesEditor.setData("");
    this.modified = true;
    this._privateNotesRemoved = true;
  }

  _removeFeedbackComment() {

    this._submission.feedbackComment = "";
    this.feedbackCommentEditor && this.feedbackCommentEditor.setData("");
    this.modified = true;
    this._feedbackCommentRemoved = true;
  }

  _toggleFullFeedbackComment() {
    this._showingFullFeedbackComment = !this._showingFullFeedbackComment;
  }

  _toggleFullPrivateNotes() {
    this._showingFullPrivateNotes = !this._showingFullPrivateNotes;
  }
}
