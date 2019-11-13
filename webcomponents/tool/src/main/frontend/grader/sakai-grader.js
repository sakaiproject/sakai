import {SakaiElement} from "/webcomponents/sakai-element.js";
import {css, html, LitElement} from "/webcomponents/assets/lit-element/lit-element.js";
import {unsafeHTML} from "/webcomponents/assets/lit-html/directives/unsafe-html.js";
import "/webcomponents/fa-icon.js";
import "./sakai-grader-file-picker.js";
import "../sakai-date-picker.js";
import "../sakai-group-picker.js";
import {gradableDataMixin} from "./sakai-gradable-data-mixin.js";
import {Submission} from "./submission.js";
import "/rubrics-service/webcomponents/rubric-association-requirements.js";

class SakaiGrader extends gradableDataMixin(SakaiElement) {

  constructor() {

    super();

    this.debug = true;

    this.sumittedTextMode = false;
    this.rubricParams = new Map();
    this.graderOnLeft = this.getSetting("grader", "graderOnLeft");
    this.saved = false;
    this.submissions = [];
    this.ungradedOnly = false;
    this.submittedOnly = false;
    this.hasUngraded = false;
    this.hasUnsubmitted = false;

    this.resubmitNumber = "1";

    this.updateComplete.then(() => $(".grader-help").tooltip({placement: "top", html: "true", toggle: "click"}));
    this.loadTranslations("grader").then(t => this.i18n = t);
  }

  static get properties() {

    return {
      // Actual attributes
      maxGrade: { attribute: "max-grade" , type: String },
      gradableId: { attribute: "gradable-id", type: String },
      submissionId: { attribute: "submission-id", type: String },
      currentStudentId: { attribute: "current-student-id", type: String },
      gradableTitle: { attribute: "gradable-title", type: String },
      hasAssociatedRubric: { attribute: "has-associated-rubric", type: String },
      entityId: { attribute: "entity-id", type: String },
      toolId: { attribute: "tool-id", type: String },
      evaluatedItemId: { attribute: "evaluated-item-id", type: String },
      userListUrl: { attribute: "user-list-url", type: String },
      // State vars we want to trigger a render
      submittedTextMode: Boolean,
      submission: Object,
      graderOnLeft: Boolean,
      selectedAttachmentRef: Boolean,
      saved: Boolean,
      submissions: { type: Array },
      fullScreen: { type: Boolean },
      ungradedOnly: Boolean,
      submissionsOnly: Boolean,
      showResubmission: Boolean,
      resubmitDate: String,
      totalGraded: Number,
      token: { type: String },
      rubric: { type: Object },
    };
  }

  set gradableId(newValue) {

    this._gradableId = newValue;
    this.loadData(newValue);
  }

  get gradableId() { return this._gradableId; }

  set submission(newValue) {

    let oldValue = this._submission;
    this._submission = newValue;
    this.modified = false;
    this.saved = false;
    if (newValue.properties["allow_resubmit_number"]) {
      this.showResubmission = true;
      this.resubmitDate = moment(parseInt(newValue.properties["allow_resubmit_closeTime"], 10)).format();
    }
    this.submittedTextMode = this._submission.submittedText;
    this.requestUpdate("submission", oldValue);
  }

  get submission() { return this._submission; }

  shouldUpdate(changed) {
    return this.i18n && this.submission;
  }

  renderNav() {

    return html`
      <div class="left-block">
        <div class="assessment-info">
          <div class="course-title">${portal.siteTitle}</div>
          <div class="assessment-title">${this.gradableTitle}</div>
        </div>
        <div style="display: inline-block; margin-left: 20px;">
          <a id="settings-link" href="javascript;" @click=${this.toggleSettings} title="${this.i18n["settings"]}">
            <fa-icon size="1.3em" class="fas cogs" path-prefix="/webcomponents/assets" />
          </a>
          <div id="grader-settings" @keydown=${this.onSettingsKeydown} class="settings">
            <div><label><input type="checkbox" ?disabled=${!this.hasUnsubmitted} @change=${this.submittedOnlyChanged} .checked=${this.submittedOnly} />${this.i18n["only_submitted"]}</label></div>
            <div><label><input type="checkbox" ?disabled=${!this.hasUngraded} @change=${this.ungradedOnlyChanged} .checked=${this.ungradedOnly} />${this.i18n["only_ungraded"]}</label></div>
            <div><label><input type="checkbox" @change=${this.graderOnLeftChanged} .checked=${this.graderOnLeft} />${this.i18n["grader_on_left"]}</label></div>
            <div class="grader-groups">
              <span>${this.i18n["groups_label"]}</span>
              <sakai-group-picker groups="${JSON.stringify(this.groups)}" @change=${this.groupSelected}></sakai-group-picker>
            </div>
          </div>
          </div>
        </div>
      </div>
      </div>
      <div class="total-block">
        <div style="display: inline-block;">
          <div class="total-label">${this.i18n["graded"]}</div>
          <div class="total-graded">${this.totalGraded} / ${this.submissions.length}</div>
        </div>
      </div>
      <div class="grader-navigator">
        <div><a class="user-list-link" href="${this.userListUrl}" title="Click to go back to the user list">Back to user list</a></div>
        <div>
          <a href="javascript:;" @click=${this.firePrevious}><fa-icon size="2em" class="fas arrow-circle-left" path-prefix="/webcomponents/assets" style="vertical-align: middle;" /></a>
          ${this.anonymousGrading ? html`
            <span class="anonymous-grading-label">${this.i18n['anonymous_grading']}</span>
          ` : html`
            <select aria-label="${this.i18n["student_selector_label"]}" @change=${this.studentSelected}>
              ${this.submissions.map(s => html`<option value="${s.id}" .selected=${this.submission.firstSubmitterId === s.firstSubmitterId}>${s.firstSubmitterName}</option>`)}
            </select>
          `}
          <a href="javascript:;" @click=${this.fireNext}><fa-icon size="2em" class="fas arrow-circle-right" path-prefix="/webcomponents/assets" style="vertical-align: middle;" /></a>
        </div>
        <div>${this.currentStudentInfo}</div>
      </div>
      <sakai-maximise-button style="margin-left: 20px;" />
    `;
  }

  renderGradable() {

    return html`
      <div class="gradable">
        ${this.submission.submittedTime ? html`
          ${this.submittedTextMode ? html`
            <div class="sak-banner-info">This is the submitted text, with your feedback. To add more feedback, click 'Add Feedback' at
            the bottom of the submission, then click 'Done' when you're finished. <strong>Your changes won't be saved until you click one of the save buttons in the grader.</strong></div>
            <textarea id="grader-feedback-text-editor" style="display: none">${this.submission.feedbackText}</textarea>
            <div id="grader-feedback-text">${unsafeHTML(this.submission.feedbackText)}</div>
            <button id="edit-inline-feedback-button" class="inline-feedback-button" @click=${this.toggleInlineFeedback} aria-haspopup="true">${this.i18n["add_feedback"]}</button>
            <button id="show-inline-feedback-button" class="inline-feedback-button" @click=${this.toggleInlineFeedback} aria-haspopup="true" style="display: none;">Done</button>
          ` : html`
            ${this.selectedAttachmentRef ? html`
              <div>${this.i18n["previewing"]}: <a href="/access${this.selectedAttachmentRef}">${this.fileNameFromRef(this.selectedAttachmentRef)}</a></div>
            ` : html``}
          `}
        ` : ""}
      </div>
    `;
  }

  renderSaved() {
    return html`<span class="saved fa fa-check-circle" title="${this.i18n["saved_successfully"]}" style="display: ${this.saved ? "inline" : "none"};"></span>`;
  }

  renderGrader() {

    return html`
      ${this.submission.id !== "dummy" ? html`
      <div class="grader ${this.graderOnLeft ? "on-left" : ""}">
        <div class="submitted-block">
          <div class="submitted-time">
            ${this.submission.submittedTime ? html`
              <span>${this.i18n["submitted_by"]} </span><span class="submitter-name">${this.renderSubmitter()}</span><span> ${this.i18n["on"]} </span>
              <span class="submitted-time">${this.submission.submittedTime}</span>
              ${this.submission.late ? html`<span class="grader-late">${this.i18n["late"]}</span>` : html``}
              ${this.submission.returned ? html`<span class="grader-returned fa fa-eye" title="${this.i18n["returned_tooltip"]}" />` : html``}
            ` : html`
              <span>${this.i18n["no_submission_for"]} ${this.renderSubmitter()}</span>
            `}
          </div>
          <div class="attachments">
            ${this.submission.submittedText && this.submission.submittedTime ? html`<div><a href="javascript;" @click=${this.displaySubmittedText}>${this.i18n["submitted_text"]}</a></div>` : html``}
            ${this.submission.submittedAttachments.length > 0 ? html`
              <div class="attachments-header">${this.i18n["submitted_attachments"]}:</div>
              ${Object.keys(this.submission.submittedAttachments).map(k => html`
                <span class="attachment-link"><a href="javascript;" data-url="${this.submission.submittedAttachments[k]}" @click=${this.previewAttachment}>${parseInt(k) + 1}</a></span>
              `)}` : html``}
          </div>
        </div> <!-- /submitted-block -->
        <div class="grade-block">
          ${this.gradeScale === "LETTER_GRADE_TYPE" ? html`
            <span>${this.i18n["grade"]}</span>
            <select aria-label="${this.i18n["lettergrade_selector_label"]}" @change=${this.gradeSelected}>
              <option value="none">Select a grade</option>
              ${this.letterGradeOptions.map(grade => html`<option value="${grade}" .selected=${this.submission.grade === grade}>${grade}</option>`)}
            </select>
            ${this.renderSaved()}
          ` : ""
          }
          ${this.gradeScale === "SCORE_GRADE_TYPE" ? html`
            <span>${this.i18n["grade"]}</span>
            <input aria-label="${this.i18n["number_grade_label"]}" @keydown=${this.validateGradeInput} @keyup=${this.gradeSelected} type="text" size="8" .value=${this.submission.grade} />
            ${this.renderSaved()}
            <span>(${this.i18n["max"]} ${this.maxGrade})</span>
          ` : html``}
          ${this.gradeScale === "PASS_FAIL_GRADE_TYPE" ? html`
            <span>${this.i18n["grade"]}</span>
            <select aria-label="${this.i18n["passfail_selector_label"]}" @change=${this.gradeSelected}>
              <option value="none">Select a grade</option>
              <option value="pass" .selected=${this.submission.grade === "Pass"}>${this.i18n["pass"]}</option>
              <option value="fail" .selected=${this.submission.grade === "Fail"}>${this.i18n["fail"]}</option>
            </select>
            ${this.renderSaved()}
          ` : html``}
          ${this.gradeScale === "CHECK_GRADE_TYPE" ? html`
            <input aria-label="${this.i18n["checkgrade_label"]}" @click=${this.gradeSelected} type="checkbox" value="Checked" ?checked=${this.submission.grade === "Checked"}></input><span>${this.i18n["grade_checked"]}</span>
            ${this.renderSaved()}
          ` : html``}
          <!-- start hasAssociatedRubric -->
          ${this.hasAssociatedRubric === "true" ? html`
            <a href="javascript:;" id="grader-rubric-link" @click=${this.toggleRubric} title="${this.i18n["grading_rubric"]}">
              <span class="icon-sakai--sakai-rubrics ${this.submission.hasRubricEvaluation ? "rubric-active" : ""}"></span>
            </a>
            <div id="rubric-panel" title="${this.i18n["rubric"]}" style="display: none;">
              <sakai-rubric-grading
                token="${this.token}"
                tool-id="${this.toolId}"
                entity-id="${this.entityId}"
                evaluated-item-id="${this.evaluatedItemId}"
                @total-points-updated=${this.onTotalPointsUpdated}
                @rubric-rating-changed=${this.onRubricRatingChanged}
                @rubric-rating-tuned=${this.onRubricRatingTuned}
                @update-comment=${this.onUpdateCriterionComment}
              ></sakai-rubric-grading>
              <button @click=${this.doneWithRubricDialog}>${this.i18n["done"]}</button>
          ` : html``}
          <!-- end hasAssociatedRubric -->
        </div>
        <div class="feedback-label grader-label content-button-block">
          <button id="grader-feedback-button" @click=${this.toggleFeedback} aria-haspopup="true" title="${this.i18n["add_feedback_tooltip"]}" >${this.i18n["add_feedback"]}</button>
          ${this.submission.feedbackComment ? html`
            <div class="active-indicator" aria-label="${this.i18n["comment_present"]}" title="${this.i18n["comment_present"]}"></div>` : ""}
        </div>
        <div id="feedback-panel" class="grader-panel" title="${this.i18n["feedback"]}" style="display: none;">
          <div class="feedback-title">${this.i18n["instructor_comment_title"]}</div>
          <div class="feedback-instruction sak-banner-info">${this.i18n["instructor_comment_instruction"]}</div>
          <textarea id="grader-feedback-comment">${this.submission.feedbackComment}</textarea>
          <div class="media-feedback grader-label">
            <span class="feedback-label">${this.i18n["recorded_feedback_label"]}</span>
            <fa-icon size="1.5em" class="fas microphone" path-prefix="/webcomponents/assets" style="vertical-align: middle;"></fa-icon>
            <fa-icon size="1.5em" class="fas video" path-prefix="/webcomponents/assets" style="vertical-align: middle;"></fa-icon>
          </div>
          <button @click=${this.doneWithFeedbackDialog}>${this.i18n["done"]}</button>
        </div>
        <div class="feedback-attachments-block grader-label">
          ${this.submission.feedbackAttachments ? html`
            <div class="feedback-attachments-title">Feedback Attachments</div>
            <div class="current-feedback-attachments">
              ${this.submission.feedbackAttachments.map(ref => html`
                <div class="feedback-attachments-row">
                <div class="feedback-attachment"><a href="/access${ref}" title="${this.i18n["feedback_attachment_tooltip"]}"><span>${this.fileNameFromRef(ref)}</span></a></div>
                <div class="feedback-attachment-remove"><a data-ref="${ref}" @click=${this.removeAttachment} href="javascript:;">${this.i18n["remove"]}</a></div>
                </div>
              `)}
            </div>` : html``}
          <sakai-grader-file-picker button-text="${this.i18n["add_attachments"]}" style="display: inline-block;" title="${this.i18n["add_attachments_tooltip"]}">
          </sakai-grader-file-picker>
        </div>
        <div class="grader-label content-button-block">
          <button id="grader-private-notes-button" @click=${this.togglePrivateNotes} aria-haspopup="true" title="${this.i18n["private_notes_tooltip"]}" >${this.i18n["private_notes"]}</button>
          ${this.submission.privateNotes ? html`<div class="active-indicator" aria-label="${this.i18n["notes_present"]}" title="${this.i18n["notes_present"]}"></div>` : ""}
        </div>
        <div id="private-notes-panel" class="grader-panel" title="${this.i18n["private_notes"]}" style="display: none;">
          <div class="sak-banner-info">${unsafeHTML(this.i18n["private_notes_tooltip"])}</div>
          <div><textarea id="grader-private-notes" .value=${this.submission.privateNotes}></textarea></div>
          <button @click=${this.doneWithPrivateNotesDialog}>${this.i18n["done"]}</button>
        </div>
        <div class="text-feedback">
        </div>
        ${this.submission.submittedTime ? html`
          <div class="resubmission-checkbox">
            <label>
              <input type="checkbox" .checked=${this.showResubmission} @change=${this.toggleResubmissionBlock}/>
              <span>${this.i18n["allow_resubmission"]}</span>
            </label>
          </div>
          ${this.showResubmission ? html`
            <div class="resubmission-block">
              <span>${this.i18n["number_resubmissions_allowed"]}:</span>
              <select aria-label="${this.i18n["attempt_selector_label"]}" @change=${(e) => this.resubmitNumber = e.target.value}>
                <option value="0" .selected=${this.submission.allowResubmitNumber === "1"}>0</option>
                <option value="1" .selected=${this.submission.allowResubmitNumber === "1"}>1</option>
                <option value="2" .selected=${this.submission.allowResubmitNumber === "2"}>2</option>
              </select>
              <span>${this.i18n["accept_until"]}:</span>
              <sakai-date-picker @datetime-selected=${(e) => this.resubmitDate = e.detail.epochMillis} initial-value="${this.resubmitDate}"></sakai-date-picker>
            </div>
          ` : ""}
        ` : ""}
        <div class="action-button-block act">
          <button accesskey="s" class="btn btn-primary active" name="save" @click=${this.save}>${this.i18n["save"]}</button>
          <button accesskey="d" name="return" @click=${this.saveAndRelease}>${this.i18n["save_and_release"]}</button>
          <button accesskey="x" name="cancel" @click=${this.cancel}>${this.i18n["cancel"]}</button>
        </div>
      </div>`
      : html``}
    `;
  }

  renderSubmitter() {

    return html`
      ${this.anonymousGrading ? html`${this.i18n['anonymous']}` : html`${this.submission.firstSubmitterName}`}`;
  }

  render() {

    return html`
      <div class="grader-nav">
      ${this.renderNav()}
      </div>
      <div class="grader-container">
        ${this.graderOnLeft ? html`
        ${this.renderGrader()}
        ${this.renderGradable()}
        ` : html`
        ${this.renderGradable()}
        ${this.renderGrader()}
        `}
      </div>
    `;
  }

  fileNameFromRef(ref) { return ref.substring(ref.lastIndexOf("\/") + 1); }

  toggleRubric(e) {

    if (!this.rubricShowing) {
      $("#rubric-panel").dialog({ width: "auto", close: (e) => { this.rubricShowing = false; } });
      this.rubricShowing = true;
    } else {
      $("#rubric-panel").dialog("destroy");
      this.rubricShowing = false;
    }
  }

  doneWithRubricDialog(e) {

    this.toggleRubric();
    this.querySelector("#grader-rubric-link").focus();
  }

  replaceWithEditor(id, width, height) {

    if (!width) width = 160;
    if (!height) height = 60;

    let editor = CKEDITOR.replace(id, {
      toolbar : [
        ['Bold', 'Italic', 'Underline', 'TextColor'],
        ['NumberedList','BulletedList', 'Blockquote']
      ],
      width: width,
      height: height,
      startupFocus: true,
    });
    editor.on("change", () => this.modified = true);
    return editor;
  }

  toggleInlineFeedback(e, cancelling) {

    if (!this.inlineFeedbackMode) {
      this.inlineFeedbackMode = true;
      this.feedbackTextEditor = this.replaceWithEditor("grader-feedback-text-editor", "100%", 600);
      this.querySelector("#grader-feedback-text").style.display = "none";
      this.querySelector("#edit-inline-feedback-button").style.display = "none";
      this.querySelector("#show-inline-feedback-button").style.display = "block";
    } else {
      this.inlineFeedbackMode = false;
      if (!cancelling) {
        this.submission.feedbackText = this.feedbackTextEditor.getData();
      }
      this.feedbackTextEditor.destroy();
      this.querySelector("#grader-feedback-text").style.display = "block";
      this.querySelector("#edit-inline-feedback-button").style.display = "block";
      this.querySelector("#show-inline-feedback-button").style.display = "none";
      this.querySelector("#grader-feedback-text-editor").style.display = "none";
      this.requestUpdate();
    }
  }

  toggleFeedback(e) {

    if (!this.feedbackShowing) {
      $("#feedback-panel").dialog({ width: "auto", close: (e) => { this.toggleFeedback(); } });
      this.feedbackShowing = true;
      this.feedbackCommentEditor = this.replaceWithEditor("grader-feedback-comment", "100%", 120);
    } else {
      this.submission.feedbackComment = this.feedbackCommentEditor.getData();
      this.feedbackCommentEditor.destroy();
      $("#feedback-panel").dialog("destroy");
      this.feedbackShowing = false;
    }
  }

  doneWithFeedbackDialog(e) {

    this.toggleFeedback();
    this.querySelector("#grader-feedback-button").focus();
    this.requestUpdate();
  }

  togglePrivateNotes(e) {

    if (!this.privateNotesShowing) {
      $("#private-notes-panel").dialog({ width: "auto", close: (e) => { this.privateNotesShowing = false; } });
      this.privateNotesShowing  = true;
      this.privateNotesEditor = this.replaceWithEditor("grader-private-notes", "100%", 120);
    } else {
      this.submission.privateNotes = this.privateNotesEditor.getData();
      this.privateNotesEditor.destroy();
      $("#private-notes-panel").dialog("destroy");
      this.privateNotesShowing = false;
      this.requestUpdate();
    }
  }

  doneWithPrivateNotesDialog(e) {

    this.togglePrivateNotes();
    this.querySelector("#grader-private-notes-button").focus();
  }

  displaySubmittedText(e) {

    e.preventDefault();
    this.previewMode = false;
    this.submittedTextMode = true;
  }

  previewAttachment(e) {

    e.preventDefault();
    this.submittedTextMode = false;
    this.previewMode = true;
    this.selectedAttachmentRef = e.target.dataset.url;
  }

  onTotalPointsUpdated(e) {

    this.submission.grade = e.detail.value;
    this.requestUpdate();
  }

  addRubricParam(e, type) {

    let name = `rbcs-${e.detail.evaluatedItemId}-${e.detail.entityId}-${type}`;
    if ("totalpoints" !== type && "state-details" !== type) name += "-" + e.detail.criterionId;
    this.rubricParams.set(name, e.detail.value);
  }

  onRubricRatingChanged(e) {

    this.addRubricParam(e, "criterion");
    this.submission.hasRubricEvaluation = true;
    this.requestUpdate();
  }

  onRubricRatingTuned(e) {
    this.addRubricParam(e, "criterion-override");
  }

  onUpdateCriterionComment(e) {
    this.addRubricParam(e, "criterion-comment");
  }

  loadData(gradableId) {

    console.debug("Loading grading data ...");

    let doIt = (data) => {

      this.gradeScale = data.gradable.gradeScale;

      if (this.gradeScale === "LETTER_GRADE_TYPE") {
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

  getFormData() {

    let formData = new FormData();

    this.querySelector("sakai-grader-file-picker").files.forEach((f,i) => formData.set(`attachment${i}`, f, f.name));
    formData.set("grade", this.submission.grade);
    formData.set("feedbackText", this.submission.feedbackText);
    formData.set("feedbackComment", this.submission.feedbackComment);
    formData.set("privateNotes", this.submission.privateNotes);

    this.rubricParams.forEach((value, name) => formData.set(name, value));

    formData.set("studentId", this.submission.firstSubmitterId);
    formData.set("courseId", portal.siteId);
    formData.set("gradableId", this.gradableId);
    formData.set("submissionId", this.submission.id);

    if (this.showResubmission) {
      formData.set("resubmitNumber", this.resubmitNumber);
      formData.set("resubmitDate", this.resubmitDate);
    }

    if (this.debug) {
      // Display the key/value pairs
      for (var pair of formData.entries()) {
        console.log(pair[0]+ ', '+ pair[1]);
      }
    }

    return formData;
  }

  /**
   * Bundle up and post all the needed stuff, like the grade, rubric, instructor comments and attachments.
   */
  save() {

    let formData = this.getFormData();
    this.submitGradingData(formData);
  }

  /**
   * Same as save, but with release being set
   */
  saveAndRelease() {

    let formData = this.getFormData();
    formData.set("gradeOption", "release");
    this.submitGradingData(formData);
  }

  submitGradingData(formData) {

    fetch("/direct/assignment/setGrade.json",
            {method: "POST", cache: "no-cache", credentials: "same-origin", body: formData})
      .then(r => r.json())
      .then(data => {

        let submission = new Submission(data);

        submission.grade = formData.get("grade");

        this.querySelector("sakai-grader-file-picker").reset();
        this.submissions.splice(this.submissions.findIndex(s => s.id === submission.id), 1, submission);
        this.originalSubmissions.splice(this.originalSubmissions.findIndex(s => s.id === submission.id), 1, submission);
        this.modified = false;
        this.submission = submission;
        this.totalGraded = this.submissions.filter(s => s.graded).length;
        this.saved = true;
      })
      .catch (e => console.error(`Failed to save grade for submission ${this.submission.id}: ${e}`));
  }

  resetEditors(cancelling) {

    if (this.feedbackShowing) {
      this.feedbackCommentEditor.setData(this.submission.feedbackComment, function () { CKEDITOR.instances["grader-feedback-comment"].resetDirty(); });
    }

    if (this.inlineFeedbackMode) {
      this.toggleInlineFeedback(null, cancelling);
    }
  }

  restoreSubmission(cancelling) {

    let os = Object.create(this.originalSubmissions.find(os => os.id === this.submission.id));
    let i = this.submissions.findIndex(s => s.id === this.submission.id);
    this.submissions.splice(i, 1, os);

    this.submission = this.submissions[i];

    if (this.gradeScale === "SCORE_GRADE_TYPE") {
      this.querySelector("#grader-number-grade").value = this.submission.grade;
    }

    this.resetEditors(cancelling);

    this.requestUpdate();
  }

  cancel() {

    this.modified = false;

    this.restoreSubmission(true);

    document.getElementById("grader-feedback-text").value = this.submission.feedbackText;
    document.getElementById("grader-feedback-comment").value = this.submission.feedbackComment;
    document.getElementById("grader-private-notes").value = this.submission.privateNotes;
  }

  checkIsModifiedAndConfirm() {

    if (this.modified) {
      let response = confirm("You've made some changes. Do you want to discard them?");

      if (response) {
        this.restoreSubmission();
      }

      return !response;
    } else {
      return false;
    }
  }

  validateGradeInput(e) {

    if (e.keyCode === 190) {
      e.preventDefault();
      return false;
    }
  }

  gradeSelected(e) {

    this.submission.grade = e.target.value;
    this.modified = true;
  }

  firePrevious() {

    const currentIndex = this.submissions.findIndex(s => s.id === this.submission.id);

    if (currentIndex >= 1) {
      if (this.checkIsModifiedAndConfirm()) { return; }
      this.resetEditors();
      this.submission = this.submissions[currentIndex - 1];
      //const name = this.ungradedOnly ? "grade-show-previous-ungraded" : "grade-show-previous";
    }
  }

  fireNext() {

    const currentIndex = this.submissions.findIndex(s => s.id === this.submission.id);

    if (currentIndex < this.submissions.length - 1) {
    if (this.checkIsModifiedAndConfirm()) { return; }
      this.resetEditors();
      this.submission = this.submissions[currentIndex + 1];
      //const name = this.ungradedOnly ? "grade-show-submission" : "grade-show-next";
    }
  }

  closer(e) {

    if (!this.querySelector("#grader-settings").contains(e.target)) {
      this.toggleSettings(e, true);
    }
  }

  toggleSettings(e, hide) {

    e.preventDefault();
    e.stopPropagation();
    const settingsPanel = this.querySelector(`.settings`);
    if ((!settingsPanel.style.display || settingsPanel.style.display === "none") && !hide) {
      settingsPanel.style.display = "block";
      settingsPanel.querySelectorAll("input")[0].focus();
      this.addEventListener("click", this.closer);
    } else {
      settingsPanel.style.display = "none";
      this.removeEventListener("click", this.closer);
    }
  }

  studentSelected(e) {

    if (this.checkIsModifiedAndConfirm()) { return; }

    this.submission = this.submissions.find(s => s.id === e.target.value);
    this.resetEditors();
  }

  applyFilters(e) {

    e.stopPropagation();

    this.submissions = [...this.originalSubmissions];

    if (this.ungradedOnly) {
      this.submissions = this.submissions.filter(s => !s.grade);
    }

    if (this.submittedOnly) {
      this.submissions = this.submissions.filter(s => s.submittedTime !== "");
    }

    if (!this.ungradedOnly && !this.submittedOnly) {
      this.submissions = [...this.originalSubmissions];
    }

    if (this.currentGroup && this.currentGroup !== "any") {
      let group = this.groups.find(g => g.id === this.currentGroup);
      this.submissions = this.submissions.filter(s => group.users.includes(s.firstSubmitterId));
    }

    this.submissions.sort((a,b) => a.firstSubmitterName.localeCompare(b.firstSubmitterName));

    if (this.submissions.length > 0) {
      this.submission = this.submissions[0];
    } else {
      this.submission = new Submission();
    }
    this.totalGraded = this.submissions.filter(s => s.graded).length;
  }

  submittedOnlyChanged(e) {

    this.submittedOnly = e.target.checked;
    this.applyFilters(e);
  }

  removeAttachment(e) {

    e.stopPropagation();
    e.preventDefault();

    if (confirm(this.i18n["confirm_remove_attachment"])) {

      let ref = e.target.dataset.ref;

      fetch(`/direct/assignment/removeFeedbackAttachment?gradableId=${this.gradableId}&submissionId=${this.submission.id}&ref=${ref}`)
        .then(r => {

          if (r.status === 200) {
            this.submission.feedbackAttachments.splice(this.submission.feedbackAttachments.findIndex(fa => fa === ref), 1);
            this.requestUpdate();
          }
        })
        .catch(error => console.error(`Failed to remove attachment on server: ${error}`));
      }
  }

  groupSelected(e) {

    this.currentGroup = e.target.value;
    this.applyFilters(e);
  }

  ungradedOnlyChanged(e) {

    this.ungradedOnly = e.target.checked;
    this.applyFilters(e);
  }

  graderOnLeftChanged(e) {

    e.stopPropagation();

    this.graderOnLeft = e.target.checked;
    this.setSetting("grader", "graderOnLeft", e.target.checked);
  }

  onSettingsKeydown(e) {

    if (e.key === "Escape") {
      this.toggleSettings(e);
      this.querySelector(`#settings-link`).focus();
    }
  }

  toggleResubmissionBlock(e) {

    if (!e.target.checked) {
      this.submission.allowResubmitNumber = "0";
    } else {
      this.submission.allowResubmitNumber = "1";
    }
    this.showResubmission = e.target.checked;
  }
}

customElements.define("sakai-grader", SakaiGrader);
