import { SakaiElement } from "/webcomponents/sakai-element.js";
import { css, html, LitElement } from "/webcomponents/assets/lit-element/lit-element.js";
import { unsafeHTML } from "/webcomponents/assets/lit-html/directives/unsafe-html.js";
import "/webcomponents/fa-icon.js";
import "./sakai-grader-file-picker.js";
import "../sakai-date-picker.js";
import "../sakai-group-picker.js";
import "../sakai-document-viewer.js";
import { gradableDataMixin } from "./sakai-gradable-data-mixin.js";
import { Submission } from "./submission.js";
import "/rubrics-service/webcomponents/rubric-association-requirements.js";
import "/rubrics-service/webcomponents/sakai-rubric-grading-button.js";

class SakaiGrader extends gradableDataMixin(SakaiElement) {

  constructor() {

    super();

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

    this.assignmentsI18n = {};

    this.i18nPromise = this.loadTranslations("grader");
    this.i18nPromise.then(t => this.i18n = t);

    this.loadTranslations("assignment").then(t => this.assignmentsI18n = t);
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
      // State vars we want to trigger a render
      submittedTextMode: Boolean,
      submission: Object,
      graderOnLeft: Boolean,
      selectedAttachmentRef: Boolean,
      saved: Boolean,
      submissions: { type: Array },
      ungradedOnly: Boolean,
      submissionsOnly: Boolean,
      showResubmission: Boolean,
      resubmitDate: String,
      totalGraded: Number,
      token: { type: String },
      rubric: { type: Object },
      assignmentsI18n: Object
    };
  }

  set gradableId(newValue) {

    this._gradableId = newValue;
    this.i18nPromise.then(() => this.loadData(newValue));
  }

  get gradableId() {
    return this._gradableId;
  }

  set submission(newValue) {

    let oldValue = this._submission;
    this._submission = newValue;
    this.saved = false;
    this.modified = false;
    this.rubricParams = new Map();
    if (newValue.properties && newValue.properties["allow_resubmit_number"] && newValue.properties["allow_resubmit_number"] !== "0") {
      this.showResubmission = true;
      this.resubmitDate = moment(parseInt(newValue.properties["allow_resubmit_closeTime"], 10)).valueOf();
    }

    this.submittedTextMode = this._submission.submittedText;

    if (this.feedbackCommentEditor) {
      this.feedbackCommentEditor.setData(this._submission.feedbackComment, () => this.modified = false);
    }

    if (this.privateNotesEditor) {
      this.privateNotesEditor.setData(this._submission.privateNotes, () => this.modified = false);
    }

    const rubric = this.querySelector("sakai-rubric-grading");
    if (rubric) {
      rubric.setAttribute("evaluated-item-id", this._submission.id);
    }

    this.requestUpdate();

    if (this.gradable.allowPeerAssessment) {
      this.updateComplete.then(() => $("#peer-info").popover());
    }
  }

  get submission() {
    return this._submission;
  }

  shouldUpdate(changed) {
    return this.i18n && this.submission;
  }

  renderNav() {

    return html`
      <div class="left-block">
        <div class="assessment-info">
          <div class="course-title">${unsafeHTML(portal.siteTitle)}</div>
          <div class="assessment-title">${this.gradableTitle}</div>
        </div>
        <div id="grader-settings-wrapper">
          <a id="settings-link" href="javascript;" @click=${this.toggleSettings} title="${this.i18n["settings"]}">
            <fa-icon size="1.3em" i-class="fas cogs" path-prefix="/webcomponents/assets" />
          </a>
          <div id="grader-settings" @keydown=${this.onSettingsKeydown} class="settings">
            <div><label><input type="checkbox" ?disabled=${!this.hasUnsubmitted} @change=${this.submittedOnlyChanged} .checked=${this.submittedOnly} />${this.assignmentsI18n["nav.view.subsOnly"]}</label></div>
            <div><label><input type="checkbox" ?disabled=${!this.hasUngraded} @change=${this.ungradedOnlyChanged} .checked=${this.ungradedOnly} />${this.i18n["only_ungraded"]}</label></div>
            <div><label><input type="checkbox" @change=${this.graderOnLeftChanged} .checked=${this.graderOnLeft} />${this.i18n["grader_on_left"]}</label></div>
            ${this.isGroupGradable ? "" : html`
              <div class="grader-groups">
                <span>${this.assignmentsI18n["please_select_group"]}</span>
                <sakai-group-picker groups="${JSON.stringify(this.groups)}" @group-selected=${this.groupSelected}></sakai-group-picker>
              </div>
            `}
          </div>
        </div>
      </div>
      <div class="total-block">
        <div>
          <div class="total-label">${this.assignmentsI18n["grad3"]}</div>
          <div class="total-graded">${this.totalGraded} / ${this.submissions.length}</div>
        </div>
      </div>
      <div class="grader-navigator">
        <div><a class="user-list-link" href="${this.userListUrl}" title="${this.assignmentsI18n["nav.list"]}">${this.assignmentsI18n["nav.list"]}</a></div>
        <div>
          <a href="javascript:;" @click=${this.previous}><fa-icon size="2em" i-class="fas arrow-circle-left" path-prefix="/webcomponents/assets" style="vertical-align: middle;" /></a>
          <select aria-label="${this.i18n["student_selector_label"]}" @change=${this.studentSelected}>
            ${this.submissions.map(s => html`<option value="${s.id}" .selected=${this.submission.id === s.id}>${s.groupId ? s.groupTitle : s.firstSubmitterName}</option>`)}
          </select>
          ${this.showPhoto() ? html`
            <span class="profile-image">
              <img src="/direct/profile/${this.submission.firstSubmitterId}/image/official?siteId=${portal.siteId}" alt="${this.submission.firstSubmitterName}${this.i18n["profile_image"]}"/>
            </span>
          ` : ""}
          <a href="javascript:;" @click=${this.next}><fa-icon size="2em" i-class="fas arrow-circle-right" path-prefix="/webcomponents/assets" style="vertical-align: middle;" /></a>
        </div>
        <div>${this.currentStudentInfo}</div>
      </div>
    `;
  }

  renderGradable() {

    return html`
      <div class="gradable">
        ${this.submission.submittedTime ? html`
          ${this.submittedTextMode ? html`
            <div class="sak-banner-info">${unsafeHTML(this.i18n["inline_feedback_instruction"])}</div>
            <textarea id="grader-feedback-text-editor" style="display: none">${this.submission.feedbackText}</textarea>
            <div id="grader-feedback-text">${unsafeHTML(this.submission.feedbackText)}</div>
            <button id="edit-inline-feedback-button" class="inline-feedback-button" @click=${this.toggleInlineFeedback} aria-haspopup="true">${this.assignmentsI18n["feedbacktext"]}</button>
            <button id="show-inline-feedback-button" class="inline-feedback-button" @click=${this.toggleInlineFeedback} aria-haspopup="true" style="display: none;">${this.assignmentsI18n["gen.don"]}</button>
          ` : html`
            ${this.selectedAttachmentRef ? html`
              <div class="preview"><sakai-document-viewer ref="${this.selectedAttachmentRef}"></sakai-document-viewer></div>
            ` : ""}
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
          <div style="display: flex;">
            ${this.showPhoto() ? html`
              <div class="profile-image" style="flex: 1;">
                <img src="/direct/profile/${this.submission.firstSubmitterId}/image/official?siteId=${portal.siteId}" alt="${this.submission.firstSubmitterName}${this.i18n["profile_image"]}" />
              </div>
            ` : ""}
            <div class="submitted-time" style="flex: 4;">
              ${this.submission.submittedTime ? html`
                <span class="submitter-name">${this.renderSubmitter()}</span><span> ${this.assignmentsI18n["gen.at"]}</span>
                <span class="submitted-time">${this.submission.submittedTime}</span>
                ${this.submission.late ? html`<span class="grader-late">${this.assignmentsI18n["grades.lateness.late"]}</span>` : ""}
                ${this.submission.returned ? html`<span class="grader-returned fa fa-eye" title="${this.i18n["returned_tooltip"]}" />` : ""}
                ${this.submission.submittedText && this.submission.submittedTime ? html`<div class="attachments-header"><a href="javascript;" @click=${this.displaySubmittedText}>${this.assignmentsI18n["gen.submittedtext"]}</a></div>` : ""}
              ` : html`
                <span>${this.i18n["no_submission_for"]} ${this.renderSubmitter()}</span>
              `}
            </div>
          </div>
          ${this.submission.groupId && this.submission.submittedTime ? html`<div class="grader-group-members">${this.submission.groupMembers}</div>` : ""}
          <div class="attachments">
            ${this.submission.submittedAttachments.length > 0 ? html`
              <div class="attachments-header">${this.assignmentsI18n["gen.stuatt"]}:</div>
              ${this.submission.submittedAttachments.map(r => html`
                <span class="attachment-link"><a href="javascript;" data-url="${r}" @click=${this.previewAttachment}>${this.fileNameFromRef(r)}</a></span>
              `)}` : ""}
          </div>
        </div> <!-- /submitted-block -->
        <div class="grade-block">
          ${this.gradeScale === "LETTER_GRADE_TYPE" ? html`
            <span>${this.assignmentsI18n["gen.assign.gra"]}</span>
            <select aria-label="${this.i18n["lettergrade_selector_label"]}" @change=${this.gradeSelected}>
              <option value="">${this.assignmentsI18n["non.submission.grade.select"]}</option>
              ${this.letterGradeOptions.map(grade => html`<option value="${grade}" .selected=${this.submission.grade === grade}>${grade}</option>`)}
            </select>
            ${this.renderSaved()}
          ` : ""}
          ${this.gradeScale === "SCORE_GRADE_TYPE" ? html`
            <span>${this.assignmentsI18n["gen.assign.gra"]}</span>
            <input aria-label="${this.i18n["number_grade_label"]}"
              @keydown=${this.validateGradeInput}
              @keyup=${this.gradeSelected}
              type="text"
              class="points-input"
              .value=${this.submission.grade} />
            ${this.renderSaved()}
            <span>(${this.assignmentsI18n["grade.max"]} ${this.gradable.maxGradePoint})</span>
            ${this.gradable.allowPeerAssessment ? html`
              <a id="peer-info" class="fa fa-info-circle" data-toggle="popover" data-container="body" data-placement="auto" data-content="${this.assignmentsI18n["peerassessment.peerGradeInfo"]}"></a>
            ` : ""}
          ` : ""}
          ${this.gradeScale === "PASS_FAIL_GRADE_TYPE" ? html`
            <span>${this.assignmentsI18n["gen.assign.gra"]}</span>
            <select id="pass-fail-selector" aria-label="${this.i18n["passfail_selector_label"]}" @change=${this.gradeSelected} .value=${this.submission.grade}>
              <option value="ungraded" ?selected=${this.submission.grade === "ungraded"}>${this.assignmentsI18n["non.submission.grade.select"]}</option>
              <option value="pass" ?selected=${this.submission.grade === "pass"}>${this.assignmentsI18n["pass"]}</option>
              <option value="fail" ?selected=${this.submission.grade === "fail"}>${this.assignmentsI18n["fail"]}</option>
            </select>
            ${this.renderSaved()}
          ` : ""}
          ${this.gradeScale === "CHECK_GRADE_TYPE" ? html`
            <input aria-label="${this.i18n["checkgrade_label"]}" @click=${this.gradeSelected} type="checkbox" value="Checked" .checked=${this.submission.grade === this.assignmentsI18n["gen.checked"]}></input><span>${this.assignmentsI18n["gen.gra2"]} ${this.assignmentsI18n["gen.checked"]}</span>
            ${this.renderSaved()}
          ` : ""}
          <!-- start hasAssociatedRubric -->
          ${this.hasAssociatedRubric === "true" ? html`
            <sakai-rubric-grading-button
              id="grader-rubric-link"
              title="${this.assignmentsI18n["grading_rubric"]}"
              token="${this.token}"
              tool-id="${this.toolId}"
              entity-id="${this.entityId}"
              evaluated-item-id="${this.submission.id}"
              evaluated-item-owner-id="${this.submission.firstSubmitterId}"
              @click=${this.toggleRubric}></sakai-rubric-grading-button>
            <div id="rubric-panel" title="${this.i18n["rubric"]}" style="display: none;">
              <sakai-rubric-grading
                token="${this.token}"
                tool-id="${this.toolId}"
                entity-id="${this.entityId}"
                evaluated-item-id="${this.submission.id}"
                evaluated-item-owner-id="${this.submission.firstSubmitterId}"
                @total-points-updated=${this.onTotalPointsUpdated}
                @rubric-rating-changed=${this.onRubricRatingChanged}
                @rubric-ratings-changed=${this.onRubricRatingsChanged}
                @rubric-rating-tuned=${this.onRubricRatingTuned}
                @update-comment=${this.onUpdateCriterionComment}
              ></sakai-rubric-grading>
              <button @click=${this.doneWithRubricDialog}>${this.assignmentsI18n["gen.don"]}</button>
          ` : ""}
          <!-- end hasAssociatedRubric -->
        </div>
        <div class="feedback-label grader-label content-button-block">
          <button id="grader-feedback-button" @click=${this.toggleFeedback} aria-haspopup="true" title="${this.i18n["add_feedback_tooltip"]}" >${this.assignmentsI18n["feedbackcomment"]}</button>
          ${this.submission.feedbackComment ? html`
            <div class="active-indicator" aria-label="${this.i18n["comment_present"]}" title="${this.i18n["comment_present"]}"></div>` : ""}
        </div>
        <div id="feedback-panel" class="grader-panel" title="${this.assignmentsI18n["feedbackcomment"]}" style="display: none;">
          <div class="feedback-title">${this.assignmentsI18n["gen.instrcomment"]}</div>
          <div class="feedback-instruction sak-banner-info">${this.assignmentsI18n["gradingsub.usethebel1"]}</div>
          <textarea id="grader-feedback-comment">${this.submission.feedbackComment}</textarea>
          <div class="media-feedback grader-label">
            <span class="feedback-label">${this.i18n["recorded_feedback_label"]}</span>
            <fa-icon size="1.5em" i-class="fas microphone" path-prefix="/webcomponents/assets" style="vertical-align: middle;"></fa-icon>
            <fa-icon size="1.5em" i-class="fas video" path-prefix="/webcomponents/assets" style="vertical-align: middle;"></fa-icon>
          </div>
          <button @click=${this.doneWithFeedbackDialog}>${this.assignmentsI18n["gen.don"]}</button>
        </div>
        <div class="feedback-attachments-block grader-label">
          ${this.submission.feedbackAttachments ? html`
            <div class="feedback-attachments-title">${this.assignmentsI18n["download.feedback.attachment"]}</div>
            <div class="current-feedback-attachments">
              ${this.submission.feedbackAttachments.map(ref => html`
                <div class="feedback-attachments-row">
                <div class="feedback-attachment"><a href="/access${ref}" title="${this.i18n["feedback_attachment_tooltip"]}"><span>${this.fileNameFromRef(ref)}</span></a></div>
                <div class="feedback-attachment-remove"><a data-ref="${ref}" @click=${this.removeAttachment} href="javascript:;">${this.assignmentsI18n["gen.remove"]}</a></div>
                </div>
              `)}
            </div>` : ""}
          <sakai-grader-file-picker button-text="${this.assignmentsI18n["gen.addatt"]}" style="display: inline-block;" title="${this.i18n["add_attachments_tooltip"]}">
          </sakai-grader-file-picker>
        </div>
        <div class="grader-label content-button-block">
          <button id="grader-private-notes-button" @click=${this.togglePrivateNotes} aria-haspopup="true" title="${this.i18n["private_notes_tooltip"]}" >${this.assignmentsI18n["note.label"]}</button>
          ${this.submission.privateNotes ? html`<div class="active-indicator" aria-label="${this.i18n["notes_present"]}" title="${this.i18n["notes_present"]}"></div>` : ""}
        </div>
        <div id="private-notes-panel" class="grader-panel" title="${this.assignmentsI18n["note.label"]}" style="display: none;">
          <div class="sak-banner-info">${unsafeHTML(this.i18n["private_notes_tooltip"])}</div>
          <div><textarea id="grader-private-notes" .value=${this.submission.privateNotes}></textarea></div>
          <button @click=${this.doneWithPrivateNotesDialog}>${this.assignmentsI18n["gen.don"]}</button>
        </div>
        <div class="text-feedback">
        </div>
        ${this.submission.submittedTime ? html`
          <div class="resubmission-checkbox">
            <label>
              <input type="checkbox" .checked=${this.showResubmission} @change=${this.toggleResubmissionBlock}/>
              <span>${this.assignmentsI18n["allowResubmit"]}</span>
            </label>
          </div>
          ${this.showResubmission ? html`
            <div class="resubmission-block">
              <span>${this.assignmentsI18n["allow.resubmit.number"]}:</span>
              <select aria-label="${this.i18n["attempt_selector_label"]}" @change=${e => this.resubmitNumber = e.target.value}>
                ${Array(10).fill().map((_, i) => html`
                  <option value="${i + 1}" .selected=${this.submission.allowResubmitNumber == (i + 1)}>${i + 1}</option>
                `)}
                <option value="-1" .selected=${this.submission.allowResubmitNumber === "-1"}>${this.i18n["unlimited"]}</option>
              </select>
              <span>${this.assignmentsI18n["allow.resubmit.closeTime"]}:</span>
              <sakai-date-picker
                epoch-millis="${this.resubmitDate}"
                @datetime-selected=${this.resubmitDateSelected}>
              </sakai-date-picker>
            </div>
          ` : ""}
        ` : ""}
        <div class="action-button-block act">
          <button accesskey="s" class="btn btn-primary active" name="save" @click=${this.save}>${this.assignmentsI18n["gen.sav"]}</button>
          <button accesskey="d" name="return" @click=${this.saveAndRelease}>${this.assignmentsI18n["gen.retustud"]}</button>
          <button accesskey="x" name="cancel" @click=${this.cancel}>${this.assignmentsI18n["gen.can"]}</button>
        </div>
      </div>` : ""}
    `;
  }

  renderSubmitter() {

    return html`
      ${this.submission.groupId ? html`${this.submission.groupTitle}` : html`${this.submission.firstSubmitterName}`}
    `;
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

  showPhoto() {
    return this.showOfficialPhoto && !this.gradable.anonymousGrading && this.submission.firstSubmitterId && !this.submission.groupId;
  }

  fileNameFromRef(ref) {
    return ref.substring(ref.lastIndexOf("\/") + 1);
  }

  toggleRubric(e) {

    if (!this.rubricShowing) {
      $("#rubric-panel").dialog({
        width: "70%",
        close: e => this.rubricShowing = false
      });
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
      toolbar: [['Bold', 'Italic', 'Underline', 'TextColor'], ['NumberedList', 'BulletedList', 'Blockquote']],
      width: width,
      height: height,
      startupFocus: true
    });
    editor.on("change", () => this.modified = true);
    return editor;
  }

  toggleInlineFeedback(e, cancelling) {

    if (!this.feedbackTextEditor) {
      this.feedbackTextEditor = this.replaceWithEditor("grader-feedback-text-editor", "100%", 600);
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

  toggleFeedback() {

    let feedbackPanel = $("#feedback-panel");

    if (!feedbackPanel.dialog("instance")) {
      feedbackPanel.dialog({
        width: "auto",
        close: (e, ui) => this.toggleFeedback()
      });
      this.feedbackCommentEditor = this.replaceWithEditor("grader-feedback-comment", "100%", 120);
    } else {
      this.submission.feedbackComment = this.feedbackCommentEditor.getData();
      this.feedbackCommentEditor.destroy();
      feedbackPanel.dialog("destroy");
    }
  }

  doneWithFeedbackDialog(e) {

    this.toggleFeedback();
    this.querySelector("#grader-feedback-button").focus();
    this.requestUpdate();
  }

  togglePrivateNotes(e) {

    let privateNotesPanel = $("#private-notes-panel");

    if (!privateNotesPanel.dialog("instance")) {
      privateNotesPanel.dialog({
        width: "auto",
        close: (e, ui) => this.togglePrivateNotes()
      });
      this.privateNotesEditor = this.replaceWithEditor("grader-private-notes", "100%", 120);
    } else {
      this.submission.privateNotes = this.privateNotesEditor.getData();
      this.privateNotesEditor.destroy();
      privateNotesPanel.dialog("destroy");
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

  addRubricParam(e, type) {

    let name = `rbcs-${e.detail.evaluatedItemId}-${e.detail.entityId}-${type}`;
    if ("totalpoints" !== type && "state-details" !== type) name += "-" + e.detail.criterionId;
    this.rubricParams.set(name, "criterionrating" === type ? e.detail.ratingId : e.detail.value);
  }

  onTotalPointsUpdated(e) {

    if (this.rubricShowing) {
      this.submission.grade = e.detail.value;
      this.requestUpdate();
    }
  }

  onRubricRatingChanged(e) {

    this.addRubricParam(e, "criterion");
    this.addRubricParam(e, "criterionrating");
    this.submission.hasRubricEvaluation = true;
    this.requestUpdate();
  }

  onRubricRatingsChanged(e) {
    this.modified = true;
  }

  onRubricRatingTuned(e) {
    this.addRubricParam(e, "criterion-override");
  }

  onUpdateCriterionComment(e) {
    this.addRubricParam(e, "criterion-comment");
  }

  loadData(gradableId) {

    let doIt = data => {

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

  /**
   * Bundle up and all the needed stuff, like the grade, rubric, instructor comments and attachments.
   */
  getFormData() {

    let formData = new FormData();

    formData.valid = true;

    this.querySelector("sakai-grader-file-picker").files.forEach((f, i) => formData.set(`attachment${i}`, f, f.name));
    formData.set("grade", this.submission.grade);

    if (this.gradeScale === "SCORE_GRADE_TYPE" && parseFloat(this.submission.grade.replace(",", ".")) > parseFloat(this.gradable.maxGradePoint.replace(",", "."))) {
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

    if (this.showResubmission) {
      formData.set("resubmitNumber", this.resubmitNumber);
      formData.set("resubmitDate", this.resubmitDate);
    }

    formData.set("siteId", portal.siteId);

    if (this.debug) {
      // Display the key/value pairs
      for (var pair of formData.entries()) {
        console.log(pair[0] + ': ' + pair[1]);
      }
    }

    return formData;
  }

  /**
   * Submit the data, but don't release.
   */
  save() {

    let formData = this.getFormData();
    formData.set("gradeOption", "retract");
    if (formData.valid) {
      this.submitGradingData(formData);
    }
  }

  /**
   * Same as save, but release.
   */
  saveAndRelease() {

    let formData = this.getFormData();
    if (formData.valid) {
      formData.set("gradeOption", "return");
      this.submitGradingData(formData);
    }
  }

  submitGradingData(formData) {

    fetch("/direct/assignment/setGrade.json", { method: "POST", cache: "no-cache", credentials: "same-origin", body: formData }).then(r => r.json()).then(data => {

      let submission = new Submission(data, this.groups);

      submission.grade = formData.get("grade");

      this.querySelector("sakai-grader-file-picker").reset();
      this.submissions.splice(this.submissions.findIndex(s => s.id === submission.id), 1, submission);
      this.originalSubmissions.splice(this.originalSubmissions.findIndex(s => s.id === submission.id), 1, submission);
      this.modified = false;
      this.submission = submission;
      this.totalGraded = this.submissions.filter(s => s.graded).length;
      this.saved = true;
    }).catch(e => console.error(`Failed to save grade for submission ${this.submission.id}: ${e}`));
  }

  resetEditors(cancelling) {

    if (this.feedbackCommentEditor) {
      this.feedbackCommentEditor.setData(this.submission.feedbackComment, () => this.feedbackCommentEditor.resetDirty());
    }

    if (this.privateNotesEditor) {
      this.privateNotesEditor.setData(this.submission.privateNotes, () => this.privateNotesEditor.resetDirty());
    }

    if (this.inlineFeedbackMode) {
      this.toggleInlineFeedback(null, cancelling);
    }
  }

  cancel() {

    let oldSubmission = this.submission;

    let os = Object.create(this.originalSubmissions.find(os => os.id === this.submission.id));
    let i = this.submissions.findIndex(s => s.id === this.submission.id);
    this.submissions.splice(i, 1, os);

    this.submission = this.submissions[i];

    this.resetEditors(true);

    this.modified = false;
  }

  canNavigate() {
    return this.modified ? confirm(this.i18n["confirm_discard_changes"]) : true;
  }

  validateGradeInput(e) {

    if (e.key === "Backspace" || e.key === "ArrowLeft" || e.key === "ArrowRight") {
      return true;
    } else if (!e.key.match(/[0-9\.,]/)) {
      e.preventDefault();
      return false;
    } else {
      const number = e.target.value.replace(",", ".");

      const numDecimals = number.includes(".") ? number.split(".")[1].length : 0;

      if (numDecimals == 2) {
        e.preventDefault();
        return false;
      }
    }
  }

  gradeSelected(e) {

    if (this.gradeScale === "CHECK_GRADE_TYPE") {
      if (e.target.checked) {
        this.submission.grade = e.target.value;
      } else {
        this.submission.grade = "Unchecked";
      }
    } else {
      this.submission.grade = e.target.value;
    }
    this.modified = true;
  }

  previous() {

    const currentIndex = this.submissions.findIndex(s => s.id === this.submission.id);

    if (currentIndex >= 1) {
      if (!this.canNavigate()) {
        return;
      }
      if (this.feedbackTextEditor) {
        this.toggleInlineFeedback(null, true);
      }
      this.submission = this.submissions[currentIndex - 1];
    }
  }

  studentSelected(e) {

    if (!this.canNavigate()) {
      return;
    }

    this.submission = this.submissions.find(s => s.id === e.target.value);
  }

  next() {

    const currentIndex = this.submissions.findIndex(s => s.id === this.submission.id);

    if (currentIndex < this.submissions.length - 1) {
      if (!this.canNavigate()) {
        return;
      }
      if (this.feedbackTextEditor) {
        this.toggleInlineFeedback(null, true);
      }
      this.submission = this.submissions[currentIndex + 1];
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

    if (this.currentGroup && this.currentGroup !== `/site/${portal.siteId}`) {
      let group = this.groups.find(g => g.reference === this.currentGroup);
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

  submittedOnlyChanged(e) {

    this.submittedOnly = e.target.checked;
    this.applyFilters(e);
  }

  removeAttachment(e) {

    e.stopPropagation();
    e.preventDefault();

    if (confirm(this.i18n["confirm_remove_attachment"])) {

      let ref = e.target.dataset.ref;

      fetch(`/direct/assignment/removeFeedbackAttachment?gradableId=${this.gradableId}&submissionId=${this.submission.id}&ref=${ref}`).then(r => {

        if (r.status === 200) {
          this.submission.feedbackAttachments.splice(this.submission.feedbackAttachments.findIndex(fa => fa === ref), 1);
          this.requestUpdate();
        }
      }).catch(error => console.error(`Failed to remove attachment on server: ${error}`));
    }
  }

  groupSelected(e) {

    this.currentGroup = e.detail.value;
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

  resubmitDateSelected(e) {

    this.resubmitDate = e.detail.epochMillis;
    this.modified = true;
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
