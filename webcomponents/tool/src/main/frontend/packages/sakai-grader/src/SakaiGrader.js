import { SakaiElement } from "@sakai-ui/sakai-element";
import { gradableDataMixin } from "./sakai-gradable-data-mixin.js";
import { graderRenderingMixin } from "./sakai-grader-rendering-mixin.js";
import { Submission } from "./submission.js";
import { getSiteId } from "@sakai-ui/sakai-portal-utils";
import { GRADE_CHECKED, LETTER_GRADE_TYPE, SCORE_GRADE_TYPE, PASS_FAIL_GRADE_TYPE, CHECK_GRADE_TYPE, GRADE_CHANGE_NOTIFY } from "./sakai-grader-constants.js";

export class SakaiGrader extends graderRenderingMixin(gradableDataMixin(SakaiElement)) {

  static properties = {

    gradableId: { attribute: "gradable-id", type: String },
    submissionId: { attribute: "submission-id", type: String },
    currentStudentId: { attribute: "current-student-id", type: String },
    gradableTitle: { attribute: "gradable-title", type: String },
    selectedGroup: { attribute: "selected-group", type: String },
    hasAssociatedRubric: { attribute: "has-associated-rubric", type: String },
    rubricSelfReport: { attribute: "rubric-self-report", type: String },
    entityId: { attribute: "entity-id", type: String },
    toolId: { attribute: "tool-id", type: String },
    userListUrl: { attribute: "user-list-url", type: String },
    enablePdfExport: { attribute: "enable-pdf-export", type: Boolean },
    ltiGradebleLaunch: { attribute: "lti-gradable-launch", type: String },
    allow: { attribute: "allow", type: String },

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
    _rubricStudentShowing: { state: true },
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
    _inlineFeedbackEditorShowing: { state: true },
  };

  constructor() {

    super();

    this.rubricParams = new Map();
    this._submissions = [];
    this.resubmitNumber = "1";
    this._i18nPromise = this.loadTranslations("grader");
    this._i18nPromise.then(t => this._i18n = t);

    if (typeof MathJax !== "undefined") {
      MathJax.Hub.Queue([ "Typeset", MathJax.Hub ]);
    }

    window.addEventListener(
      "message",
      e => {
        // In case the LTI tool serializes its message into a string (some do)
        const message = (typeof e.data === "string") ? JSON.parse(e.data) : e.data;
        if ( message.subject !== GRADE_CHANGE_NOTIFY ) return;
        console.debug("The LTI Tool changed a grade - retrieving new grade");
        console.debug(this._submission);
        fetch(`/direct/assignment/getGrade.json?gradableId=${this.gradableId}&submissionId=${this._submission.id}&courseId=${encodeURIComponent(getSiteId())}&studentId=${this._submission.firstSubmitterId}`, {
          method: "GET",
          cache: "no-cache",
          credentials: "same-origin",
        })
        .then(r => {

          if (r.ok) {
            return r.json();
          }
          throw new Error("Network error while loading getGrade.json");
        })
        .then(data => {

          console.debug(data);
          this._submission.grade = data.grade;
          if ( "feedbackComment" in data && this.feedbackCommentEditor) {
            this._submission.feedbackComment = data.feedbackComment;
            this.feedbackCommentEditor.setData(data.feedbackComment, () => this.modified = false);
          }
          this.requestUpdate();
        });
      },
    );
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

    // If switching submissions and the inline editor for the PREVIOUS submission is open,
    // close and destroy it by simulating a cancel action.
    if (newValue.id !== this._submission?.id && this._inlineFeedbackEditorShowing) {
      this._toggleInlineFeedback(null, true); // Pass true for cancelling
    }

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
    this._isChecked = newValue.grade === this._i18n["gen.checked"] || newValue.grade === GRADE_CHECKED;
    this._allowExtension = this.__submission.extensionAllowed;
    this._submittedTextMode = !!this.__submission.submittedText;
    this._feedbackCommentEditorShowing = false;

    // If there's no submitted text and at least one attachment, show the first attachment
    // by default.
    if (!this._submittedTextMode && this.__submission.submittedAttachments && this.__submission.submittedAttachments.length > 0) {
      this._selectedAttachment = this.__submission.submittedAttachments[0];
      const preview = this.__submission.previewableAttachments[this._selectedAttachment.ref];
      this._selectedPreview = preview || this._selectedAttachment;
    }

    if (this.feedbackCommentEditor) {
      this.feedbackCommentEditor.setData(this.__submission.feedbackComment, () => this.modified = false);
    }

    if (this.privateNotesEditor) {
      this.privateNotesEditor.setData(this.__submission.privateNotes, () => this.modified = false);
    }

    this.querySelector("sakai-rubric-grading")?.setAttribute("evaluated-item-id", this.__submission.id);

    if (this.gradable.allowPeerAssessment) {
      this.updateComplete.then(() => (new bootstrap.Popover(this.querySelector("#peer-info"))));
    }

    // If any grade overrides have been set, check the overrides box
    this._showOverrides = this.__submission.submitters?.some(s => s.overridden);

    this._showRemoveFeedbackComment = (typeof this.__submission.feedbackComment !== "undefined");
    this._showingFullPrivateNotes = false;
    this._showingFullFeedbackComment = false;

    this.updateComplete.then(() => this._resetGradeInputs());
  }

  get _submission() { return this.__submission; }

  _loadData(gradableId, submissionId) {
    Promise.all([ this._i18nPromise, this._loadGradableData(gradableId, submissionId) ]).then(() => this._setup());
  }

  shouldUpdate() {
    return this._i18n;
  }

  _closeGrader(e) {

    this.querySelector("sakai-rubric-grading")?.closeCommentEditors();

    if (this.modified || this.querySelector("sakai-grader-file-picker")?.hasFiles()) {
      e.preventDefault();
      this._save({ bannerTimout: 2000 });
    }

    // Close all the collapses on the hidden event, so we don't have loads of sliding
    // about going on at once.
    bootstrap.Collapse.getInstance(this.querySelector("#feedback-block"))?.hide();
    bootstrap.Collapse.getInstance(this.querySelector("#private-notes-block"))?.hide();
    this._feedbackCommentRemoved = false;
    this._privateNotesRemoved = false;
    this._closeRubric();
    this._closeStudentRubric();

    bootstrap.Collapse.getInstance(this.querySelector("#feedback-block"))?.hide();

    this._toggleGrader();
  }

  _toggleGrader() {

    this.querySelector("#grader").classList.toggle("grader--visible");
    this.querySelector("#grader-gradable-container").classList.toggle("d-none");
  }

  _setup() {

    this.feedbackCommentEditor = this._replaceWithEditor("grader-feedback-comment", data => {
      this._submission.feedbackComment = data;
      this._gradeOrCommentsModified = true;
    });

    this.privateNotesEditor = this._replaceWithEditor("grader-private-notes", data => {
      this._submission.privateNotes = data;
      this._gradeOrCommentsModified = true;
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

    this.updateComplete.then(() => {

      this.querySelector("#grader-rubric-link")?.focus();
      this.querySelector("sakai-rubric-grading-button")?.setHasEvaluation();
      this.querySelector("sakai-rubric-evaluation-remover")?.setHasEvaluation();
    });
  }

  _closeStudentRubric() {

    this._rubricStudentShowing = false;
    this.querySelector("student-rubric-grading")?.displayGradingTab();
  }

  _replaceWithEditor(id, changedCallback) {

    const editor = sakai.editor.launch(id, {
      autosave: { delay: 10000000, messageType: "no" },
      startupFocus: true,
    });

    editor.on("change", e => {

      changedCallback?.(e.editor.getData());
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

    this._inlineFeedbackEditorShowing = !this._inlineFeedbackEditorShowing;

    if (this._inlineFeedbackEditorShowing) {
      this.feedbackTextEditor = this._replaceWithEditor("grader-inline-feedback-editor");
    } else {
      if (!cancelling) {
        this._submission.feedbackText = this.feedbackTextEditor.getData();
        if (this.feedbackTextEditor.checkDirty()) {
          this._save({});
        }
        this.requestUpdate();
      } else {
        this.feedbackTextEditor.setData(this._submission.feedbackText, () => this.modified = false);
      }
      this.feedbackTextEditor.destroy();
    }
  }

  _toggleRubric() {
    this._rubricShowing = !this._rubricShowing;
  }

  _toggleStudentRubric() {
    this._rubricStudentShowing = !this._rubricStudentShowing;
  }

  _togglePrivateNotesEditor() {

    this._privateNotesEditorShowing = !this._privateNotesEditorShowing;

    if (!this._privateNotesEditorShowing) {

      this._showingFullPrivateNotes = false;
      this._allPrivateNotesVisible = false;
      this.updateComplete.then(() => this._setupVisibleFlags());
    } else {
      this._privateNotesRemoved = false;
    }
  }

  _savePrivateNotes() {
    // First toggle the editor (close it)
    this._togglePrivateNotesEditor();

    // Then save the data
    this._save({});
  }

  _toggleFeedbackCommentEditor() {

    this._feedbackCommentEditorShowing = !this._feedbackCommentEditorShowing;

    if (!this._feedbackCommentEditorShowing) {
      this._showingFullFeedbackComment = false;
      this._allFeedbackCommentVisible = false;
      this.updateComplete.then(() => this._setupVisibleFlags());
    } else {
      this._feedbackCommentRemoved = false;
    }
  }

  _saveFeedbackComment() {
    // First toggle the editor (close it)
    this._toggleFeedbackCommentEditor();

    // Then save the data
    this._save({});
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
    this._submission.grade = "";
    this.requestUpdate();
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
    formData.set("courseId", getSiteId());
    formData.set("gradableId", this.gradableId);
    formData.set("submissionId", this._submission.id);

    if (this._showResubmission && this._submission.resubmitDate) {
      formData.set("resubmitNumber", this._submission.resubmitsAllowed);
      formData.set("resubmitDate", this._submission.resubmitDate);
    }

    if (this._allowExtension) {
      formData.set("extensionDate", this._submission.extensionDate);
    }

    formData.set("siteId", getSiteId());

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
      this._submitGradingData(formData, e.bannerTimout);
      const rubricGrading = this.querySelector("sakai-rubric-grading");
      rubricGrading && (release ? rubricGrading.release() : rubricGrading.save());
    }
  }

  _submitGradingData(formData, bannerTimout = 1000) {

    this._saving = true;

    fetch("/direct/assignment/setGrade.json", {
      method: "POST",
      cache: "no-cache",
      body: formData
    })
    .then(r => r.json()).then(data => {

      const submission = new Submission(data.submission, this.groups, this._i18n, data.assignmentCloseTime);
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
        this._gradeOrCommentsModified = false;
      }, bannerTimout);
    })
    .catch (e => {

      console.error(`Failed to save grade for submission ${this._submission.id}: ${e}`);
      this._saveFailed = true;
      setTimeout(() => this._saveFailed = false, 2000);
    });
  }

  _cancel(toggle = true) {

    const originalSubmission = Object.create(this.originalSubmissions.find(os => os.id === this._submission.id));
    const i = this._submissions.findIndex(s => s.id === this._submission.id);
    this._submissions.splice(i, 1, originalSubmission);
    this._submission = this._submissions[i];

    if (this.feedbackCommentEditor) {
      this.feedbackCommentEditor.setData(this._submission.feedbackComment, () => this.feedbackCommentEditor.resetDirty());
    }

    if (this.privateNotesEditor) {
      this.privateNotesEditor.setData(this._submission.privateNotes, () => this.privateNotesEditor.resetDirty());
    }

    this.modified = false;

    this._resetGradeInputs();

    toggle && this._toggleGrader();
  }

  _resetGradeInputs() {

    switch (this.gradeScale) {
      case SCORE_GRADE_TYPE: {
        const input = this.querySelector("#score-grade-input");
        input && (input.value = this._submission.grade);
        break;
      }
      case PASS_FAIL_GRADE_TYPE: {
        const input = this.querySelector("#pass-fail-selector");
        input && (input.value = this._submission.grade);
        break;
      }
      case LETTER_GRADE_TYPE: {
        const input = this.querySelector("#letter-grade-selector");
        input && (input.value = this._submission.grade);
        break;
      }
      case CHECK_GRADE_TYPE: {
        const input = this.querySelector("#check-grade-input");
        input && (input.checked = this._submission.grade === this._i18n["gen.checked"] || this._submission.grade === GRADE_CHECKED);
        break;
      }
      default:
    }
  }

  _clearSubmission() {

    const currentIndex = this._submissions.findIndex(s => s.id === this._submission.id);
    this._submissions[currentIndex] = this._nonEditedSubmission;
    this._submission = this._nonEditedSubmission;
    this.querySelector("sakai-grader-file-picker").reset();
    this.querySelector("sakai-rubric-grading")?.cancel();
    this.querySelector("sakai-rubric-student")?.cancel();
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
    this._gradeOrCommentsModified = true;
  }

  _previous() {

    // Check for unsaved changes before navigating
    if (this.modified) {
      if (!confirm(this._i18n.unsaved_changes_warning)) {
        return;
      }
      // User confirmed - discard the unsaved changes
      this._cancel(false);
    }

    const currentIndex = this._submissions.findIndex(s => s.id === this._submission.id);

    if (currentIndex >= 1) {
      // Check if the previous submission is hydrated before trying to navigate
      const prevSubmission = this._submissions[currentIndex - 1];
      if (!prevSubmission.hydrated) {
        // If not hydrated, fetch it first
        this._hydrateCluster(prevSubmission.id).then(submission => {
          if (submission) {
            this._submission = submission;
          } else {
            // Fallback to normal behavior if hydration fails
            this._hydratePrevious(currentIndex);
            this._submission = this._submissions[currentIndex - 1];
          }
        });
      } else {
        // If already hydrated, just set it
        this._submission = prevSubmission;
      }
    }
  }

  _studentSelected(e) {

    // Check for unsaved changes before navigating
    if (this.modified && e.target.value !== this._submission.id) {
      if (!confirm(this._i18n.unsaved_changes_warning)) {
        // Reset the select to the current submission
        e.target.value = this._submission.id;
        return;
      }
      // User confirmed - discard the unsaved changes
      this._cancel(false);
    }

    const selectedSubmission = this._submissions.find(s => s.id === e.target.value);
    if (!selectedSubmission) {
      console.error("Selected submission not found in filtered submissions");
      return;
    }

    if (!selectedSubmission.hydrated) {
      this._hydrateCluster(selectedSubmission.id).then(s => {
        if (s) {
          this._submission = s;
        } else {
          console.error("Failed to hydrate selected submission");
        }
      });
    } else {
      this._submission = selectedSubmission;
    }
  }

  _next() {

    // Check for unsaved changes before navigating
    if (this.modified) {
      if (!confirm(this._i18n.unsaved_changes_warning)) {
        return;
      }
      // User confirmed - discard the unsaved changes
      this._cancel(false);
    }

    const currentIndex = this._submissions.findIndex(s => s.id === this._submission.id);

    if (currentIndex < this._submissions.length - 1) {
      // Check if the next submission is hydrated before trying to navigate
      const nextSubmission = this._submissions[currentIndex + 1];
      if (!nextSubmission.hydrated) {
        // If not hydrated, fetch it first
        this._hydrateCluster(nextSubmission.id).then(submission => {
          if (submission) {
            this._submission = submission;
          } else {
            // Fallback to normal behavior if hydration fails
            this._hydrateNext(currentIndex);
            this._submission = this._submissions[currentIndex + 1];
          }
        });
      } else {
        // If already hydrated, just set it
        this._submission = nextSubmission;
      }
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

    if (this.currentGroups?.length === 1 && this.currentGroups[0].includes("/group")) {
      const group = this.groups.find(g => g.reference === this.currentGroups[0]);
      filtered = filtered.filter(s => group.users.includes(s.firstSubmitterId));
    }

    // Set the filtered submissions first so navigation functions have the correct array
    this._submissions = [ ...filtered ];

    this._totalGraded = filtered.filter(s => s.graded).length;
    this._totalSubmissions = filtered.length;

    if (filtered.length > 0) {
      // Check if current submission is in the filtered list
      const currentSubmissionInFilter = filtered.some(s => s.id === this._submission.id);

      // Find submission to display
      const submissionToHydrateId = currentSubmissionInFilter ? this._submission.id : filtered[0].id;

      // If current submission is not in filter, we need to immediately show the first filtered one
      if (!currentSubmissionInFilter) {
        // Use the filtered submission directly to ensure UI update happens immediately
        const firstFilteredSubmission = filtered.find(s => s.id === submissionToHydrateId);
        // Make a direct assignment to force immediate update
        this._submission = firstFilteredSubmission;
      }

      // Also do hydration to get full data
      this._hydrateCluster(submissionToHydrateId).then(submission => {
        if (submission) {
          this._submission = submission;
        }
      });
    } else {
      this._submission = new Submission();
    }
  }

  _submittedOnlyChanged(e) {

    this._submittedOnly = e.target.checked;
    this._applyFilters();
  }

  _areSettingsInAction() {
    return (this.currentGroups?.length > 0 && this.currentGroups[0] !== `/site/${getSiteId()}`) || this._submittedOnly || this._ungradedOnly || this._gradedOnly;
  }

  _getSubmitter(submission) {
    return submission.groupId ? submission.groupTitle : submission.firstSubmitterName;
  }

  _removeAttachment(e) {

    e.stopPropagation();
    e.preventDefault();

    if (confirm(this._i18n.confirm_remove_attachment)) {
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

    if (!confirm(this._i18n.confirm_remove_private_notes)) return false;

    this._submission.privateNotes = "";
    this.privateNotesEditor?.setData("");
    this.modified = true;
    this._gradeOrCommentsModified = true;
    this._privateNotesRemoved = true;
  }

  _removeFeedbackComment() {

    if (!confirm(this._i18n.confirm_remove_feedback_comment)) return false;

    this._submission.feedbackComment = "";
    this.feedbackCommentEditor?.setData("");
    this.modified = true;
    this._gradeOrCommentsModified = true;
    this._feedbackCommentRemoved = true;
  }

  _toggleFullFeedbackComment() {
    this._showingFullFeedbackComment = !this._showingFullFeedbackComment;
  }

  _toggleFullPrivateNotes() {
    this._showingFullPrivateNotes = !this._showingFullPrivateNotes;
  }
}
