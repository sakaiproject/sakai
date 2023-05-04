import { html } from "/webcomponents/assets/lit-element/lit-element.js";
import { unsafeHTML } from "/webcomponents/assets/lit-html/directives/unsafe-html.js";
import { ifDefined } from "/webcomponents/assets/lit-html/directives/if-defined.js";
import "./sakai-grader-file-picker.js";
import { Submission } from "./submission.js";
import "../sakai-date-picker.js";
import "../sakai-group-picker.js";
import "../sakai-document-viewer.js";
import "../sakai-lti-iframe.js";
import "../sakai-user-photo.js";
import "../fa-icon.js";
import "../rubrics/sakai-rubric-grading-button.js";
import "../rubrics/sakai-rubric-grading.js";
import "../rubrics/sakai-rubric-evaluation-remover.js";
import { GRADE_CHECKED, LETTER_GRADE_TYPE, SCORE_GRADE_TYPE, PASS_FAIL_GRADE_TYPE, CHECK_GRADE_TYPE } from "./sakai-grader-constants.js";

export const graderRenderingMixin = Base => class extends Base {

  _renderTopbar() {

    return html`
      <div id="grader-topbar">

        <div class="modal fade" id="grader-settings" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="grader-settings-modal-label" aria-hidden="true">
          <div class="modal-dialog">
            <div class="modal-content">
              <div class="modal-header">
                <h5 class="modal-title" id="grader-settings-modal-label">${this.i18n.settings}</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
              </div>
              <div class="modal-body">
                <div><label><input type="checkbox" ?disabled=${!this.hasUnsubmitted} @change=${this._submittedOnlyChanged} .checked=${this.submittedOnly} />${this.assignmentsI18n["nav.view.subsOnly"]}</label></div>
                <div><label><input type="checkbox" ?disabled=${!this.hasUngraded} @change=${this._ungradedOnlyChanged} .checked=${this.ungradedOnly} />${this.i18n.only_ungraded}</label></div>
                ${this.isGroupGradable ? "" : html`
                  <div class="grader-groups">
                    <span>${this.assignmentsI18n.please_select_group}</span>
                    <sakai-group-picker .groups="${this.groups}" @group-selected=${this._groupSelected}></sakai-group-picker>
                  </div>
                `}
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-primary" data-bs-dismiss="modal">${this.i18n.done}</button>
              </div>
            </div>
          </div>
        </div>

        <div id="grader-title-block">
          <div class="fw-bold">
            <div class="assessment-title fs-5">${this.gradableTitle}</div>
          </div>
        </div>

        <div id="grader-total" class="fs-6">
          <span>${this.assignmentsI18n.grad3}</span>
          <span>${this.totalGraded} / ${this.submissions.length}</span>
        </div>

        <div id="grader-navigator">
          <div class="d-flex align-items-center justify-content-center">
            <button class="btn btn-transparent text-decoration-underline"
                title="${this.assignmentsI18n["nav.list"]}"
                @click=${this._toStudentList}
                ?disabled=${!this.canSave}>
              ${this.assignmentsI18n["nav.list"]}
            </button>
            <button id="grader-settings-link"
                class="btn icon-button ms-2"
                data-bs-toggle="modal"
                data-bs-target="#grader-settings"
                title="${this.i18n.settings}"
                aria-label="${this.i18n.settings}"
                aria-controls="grader-settings">
              <i class="si si-settings"></i>
            </button>
          </div>
          <div>
            <button class="btn btn-transparent" @click=${this._previous} aria-label="${this.i18n.previous_submission_label}" ?disabled=${!this.canSave}>
              <i class="si si-arrow-left-circle-fill"></i>
            </button>
            <select aria-label="${this.i18n.student_selector_label}" @change=${this._studentSelected} ?disabled=${!this.canSave}>
              ${this.submissions.map(s => html`<option value="${s.id}" .selected=${this.submission.id === s.id}>${s.groupId ? s.groupTitle : s.firstSubmitterName}</option>`)}
            </select>
            <button class="btn btn-transparent" @click=${this._next} aria-label="${this.i18n.next_submission_label}" ?disabled=${!this.canSave}>
              <i class="si si-arrow-right-circle-fill"></i>
            </button>
          </div>
          <div>${this.currentStudentInfo}</div>
        </div>
      </div>
    `;
  }

  _renderGradable() {

    return html`
      <div id="gradable">
        ${this.submission.ltiSubmissionLaunch ? html`
          <div class="sak-banner-info">${unsafeHTML(this.i18n.lti_grade_launch_instructions)}</div>
          <sakai-lti-iframe
            allow-resize="yes"
            new-window-text="${this.i18n.lti_grade_launch_button}"
            launch-url="${this.submission.ltiSubmissionLaunch}">
          </sakai-lti-iframe>
        ` : ""}
        ${this.ltiGradableLaunch && !this.submission.ltiSubmissionLaunch ? html`
          <div class="sak-banner-info">${unsafeHTML(this.i18n.lti_grade_launch_instructions)}</div>
          <sakai-lti-iframe
            allow-resize="yes"
            new-window-text="${this.i18n.lti_grade_launch_button}"
            launch-url="${this.ltiGradableLaunch}">
          </sakai-lti-iframe>
        ` : ""}
        ${this.submission.submittedTime || this.submission.draft && this.submission.visible ? html`
        <h3 class="d-inline-block">${this.assignmentsI18n["gen.subm"]}</h3>
        ` : html`
        <h3 class="d-inline-block">${this.i18n.no_submission}</h3>
        `}
        <div id="grader-link-block" class="float-end">
          <button class="btn btn-primary active"
              data-bs-toggle="offcanvas"
              data-bs-target="#grader"
              aria-controls="grader">
          ${this.i18n.grade_submission}
          </button>
        </div>
        ${this.submission.submittedTime || this.submission.draft && this.submission.visible ? html`
          ${this.submittedTextMode ? html`
            <div id="grader-submitted-text-block">
              <div class="sak-banner-info">${unsafeHTML(this.i18n.inline_feedback_instruction)}</div>
              <textarea id="grader-feedback-text-editor" class="d-none">${this.submission.feedbackText}</textarea>
              <div id="grader-feedback-text">${unsafeHTML(this.submission.feedbackText)}</div>
              <button id="edit-inline-feedback-button" class="btn btn-link inline-feedback-button" @click=${this._toggleInlineFeedback} aria-haspopup="true">${this.assignmentsI18n.addfeedback}</button>
              <button id="show-inline-feedback-button" class="btn btn-link inline-feedback-button d-none" @click=${this._toggleInlineFeedback} aria-haspopup="true">${this.assignmentsI18n["gen.don"]}</button>
            </div>
          ` : html`
            ${this.selectedAttachment || this.selectedPreview ? html`
              <div class="preview">
                <sakai-document-viewer
                    preview="${ifDefined(this.selectedPreview ? JSON.stringify(this.selectedPreview) : undefined)}"
                    content="${ifDefined(this.selectedAttachment ? JSON.stringify(this.selectedAttachment) : undefined)}">
                </sakai-document-viewer>
              </div>
            ` : ""}
          `}
        ` : ""}
      </div>
    `;
  }

  _renderSaved() {

    return html`<span class="saved fa fa-check-circle"
                  title="${this.i18n.saved_successfully}"
                  style="display: ${this.saveSucceeded ? "inline" : "none"};">
                </span>`;
  }

  _renderFailed() {

    return html`<span class="saved failed fa fa-times-circle"
                  title="${this.i18n.failed_save}"
                  style="display: ${this.saveFailed ? "inline" : "none"};">
                </span>`;
  }

  _renderGradeInputs(label, submitter) {

    return html`
      <span>${label}</span>
      ${this.gradeScale === LETTER_GRADE_TYPE ? html`
        <select id="letter-grade-selector"
            aria-label="${this.i18n.lettergrade_selector_label}"
            class=${ifDefined(submitter ? "grader-grade-override" : undefined)}
            data-user-id="${ifDefined(submitter ? submitter.id : undefined)}"
            @change=${submitter ? undefined : this._gradeSelected}>
          <option value="">${this.assignmentsI18n["non.submission.grade.select"]}</option>
          ${this.letterGradeOptions.map(grade => html`
          <option value="${grade}"
              .selected=${submitter ? submitter.overridden && submitter.grade === grade : this.submission.grade === grade}>
            ${grade}
          </option>
          `)}
        </select>
        ${this._renderSaved()}
        ${this._renderFailed()}
      ` : ""}
      ${this.gradeScale === SCORE_GRADE_TYPE ? html`
        <input id="score-grade-input" aria-label="${this.i18n.number_grade_label}"
          @keydown=${this._validateGradeInput}
          @keyup=${submitter ? undefined : this._gradeSelected}
          data-user-id="${ifDefined(submitter ? submitter.id : undefined)}"
          type="text"
          class="points-input ${ifDefined(submitter ? "grader-grade-override" : undefined)}"
          .value=${submitter ? submitter.overridden ? submitter.grade : "" : this.submission.grade} />
        ${this._renderSaved()}
        ${this._renderFailed()}
        <span>(${this.assignmentsI18n["grade.max"]} ${this.gradable.maxGradePoint})</span>
        ${this.gradable.allowPeerAssessment ? html`
          <a id="peer-info" class="fa fa-info-circle" data-bs-toggle="popover" data-container="body" data-placement="auto" data-content="${this.assignmentsI18n["peerassessment.peerGradeInfo"]}"></a>
        ` : ""}
      ` : ""}
      ${this.gradeScale === PASS_FAIL_GRADE_TYPE ? html`
        <select id="pass-fail-selector"
                  aria-label="${this.i18n.passfail_selector_label}"
                  class=${ifDefined(submitter ? "grader-grade-override" : undefined)}
                  data-user-id="${ifDefined(submitter ? submitter.id : undefined)}"
                  @change=${submitter ? undefined : this._gradeSelected}
                  .value=${submitter ? submitter.grade : this.submission.grade}>
          <option value="ungraded"
              .selected=${submitter ? !submitter.overridden : this.submission.grade === this.assignmentsI18n.ungra}>
            ${this.assignmentsI18n.ungra}
          </option>
          <option value="pass"
              .selected=${submitter ? submitter.overridden && submitter.grade === this.assignmentsI18n.pass : this.submission.grade.match(/^pass$/i)}>
            ${this.assignmentsI18n.pass}
          </option>
          <option value="fail"
              .selected=${submitter ? submitter.overridden && submitter.grade === this.assignmentsI18n.fail : this.submission.grade.match(/^fail$/i)}>
            ${this.assignmentsI18n.fail}
          </option>
        </select>
        ${this._renderSaved()}
        ${this._renderFailed()}
      ` : ""}
      ${this.gradeScale === CHECK_GRADE_TYPE ? html`
        <input id="check-grade-input"
                type="checkbox"
                data-user-id="${ifDefined(submitter ? submitter.id : undefined)}"
                aria-label="${this.i18n.checkgrade_label}"
                class=${ifDefined(submitter ? "grader-grade-override" : undefined)}
                @click=${submitter ? undefined : this._gradeSelected}
                value=${GRADE_CHECKED}
                .checked=${submitter ? submitter.overridden && submitter.grade === this.assignmentsI18n["gen.checked"] : this.submission.grade === this.assignmentsI18n["gen.checked"]}>
        </input>
        <span>${this.assignmentsI18n["gen.gra2"]} ${this.assignmentsI18n["gen.checked"]}</span>
        ${this._renderSaved()}
        ${this._renderFailed()}
      ` : ""}
    `;
  }

  _renderGrader() {

    // Hide the right UI until we have push notifications for grade changes
    if (this.submission.ltiSubmissionLaunch) return "";
    return html`
      ${this.submission.id !== "dummy" ? html`

      <div id="grader" class="offcanvas offcanvas-end" data-bs-backdrop="static" tabindex="-1" aria-labelledby="grader-label">

        <div class="offcanvas-header">
          <h2 class="offcanvas-title" id="grader-label">${this.i18n.grader}</h2>
          <button type="button" class="btn-close text-reset" data-bs-dismiss="offcanvas" aria-label="Close"></button>
        </div>

        <div class="offcanvas-body">

          <!-- START ORIGINALITY BLOCK -->
          ${this.submission.originalityShowing ? html`
            <div>
              <label class="grader-label grader-originality-label"><span>${this.submission.originalityServiceName}</span><span>${this.assignmentsI18n["review.report"]}</span></label>
              ${this.submission.originalitySupplies.map(result => html`
                <div class="grader-originality-section" >
                  ${result[Submission.originalityConstants.originalityLink] !== 'Error' ? html`
                    <a target="_blank"
                        href="${result[Submission.originalityConstants.originalityLink]}"
                        class="grader-originality-link">
                      <i class="${result[Submission.originalityConstants.originalityIcon]}"></i>
                      <span>${result[Submission.originalityConstants.originalityScore]}${this.assignmentsI18n["content_review.score_display.grader"]}</span>
                    </a>
                    <span>
                      <span class="grader-originality-delimiter">${this.assignmentsI18n["content_review.delimiter"]}</span>
                      <span>${result[Submission.originalityConstants.originalityName]}</span>
                    </span>
                  ` : html`
                    ${result[Submission.originalityConstants.originalityStatus] === 'true' ? html`
                      <a href="#${result[Submission.originalityConstants.originalityKey]}"
                          data-bs-toggle="collapse"
                          role="button"
                          aria-expanded="false"
                          aria-controls="${result[Submission.originalityConstants.originalityKey]}"
                          class="grader-originality-link">
                        <i class="${result[Submission.originalityConstants.originalityIcon]}"></i>
                        <span>${this.assignmentsI18n["content_review.disclosure.pending"]}</span>
                      </a>
                      <span>
                        <span class="grader-originality-delimiter">${this.assignmentsI18n["content_review.delimiter"]}</span>
                        <span>${result[Submission.originalityConstants.originalityName]}</span>
                      </span>
                      <div class="collapse grader-originality-caption" id="${result[Submission.originalityConstants.originalityKey]}">
                        <span>${this.assignmentsI18n["content_review.notYetSubmitted.grader"]}</span>
                        <span>${this.submission.originalityServiceName}</span>
                      </div>
                    ` : html`
                      <a href="#${result[Submission.originalityConstants.originalityKey]}"
                          data-bs-toggle="collapse"
                          role="button"
                          aria-expanded="false"
                          aria-controls="${result[Submission.originalityConstants.originalityKey]}"
                          class="grader-originality-link">
                        <i class="${result[Submission.originalityConstants.originalityIcon]}"></i><span>${this.assignmentsI18n["content_review.disclosure.error"]}</span>
                      </a>
                      <span>
                        <span class="grader-originality-delimiter">${this.assignmentsI18n["content_review.delimiter"]}</span>
                        <span>${result[Submission.originalityConstants.originalityName]}</span>
                      </span>
                      <div class="collapse grader-originality-caption" id="${result[Submission.originalityConstants.originalityKey]}">
                        <div>${result[Submission.originalityConstants.originalityError]}</div>
                      </div>
                    `}
                  `}
                  <br />
                </div>
              `)}
            </div>
          ` : ""}
          <!-- END ORIGINALITY BLOCK -->

          <div id="grader-grade-block" class="grader-block">
            ${this._renderGradeInputs(this.assignmentsI18n["gen.assign.gra"])}
            <!-- start hasAssociatedRubric -->
            ${this.hasAssociatedRubric === "true" ? html`
              <div class="d-flex align-items-center mt-3">
                <div>
                  <button id="grader-rubric-button"
                      class="btn btn-link"
                      @click=${this._toggleRubric}
                      aria-label="${this.assignmentsI18n.grading_rubric}"
                      title="${this.assignmentsI18n.grading_rubric}"
                      aria-controls="grader-rubric-block grader-controls-block">
                    ${this.i18n.rubric}
                  </button>
                </div>
                <div>
                  <sakai-rubric-grading-button
                      id="grader-rubric-link"
                      aria-label="${this.assignmentsI18n.grading_rubric}"
                      title="${this.assignmentsI18n.grading_rubric}"
                      @click=${this._toggleRubric}
                      site-id="${portal.siteId}"
                      tool-id="${this.toolId}"
                      entity-id="${this.entityId}"
                      evaluated-item-id="${this.submission.id}"
                      aria-controls="grader-rubric-block grader-controls-block"
                      only-show-if-evaluated>
                  </sakai-rubric-grading-button>
                  <sakai-rubric-evaluation-remover
                      class="ms-2"
                      site-id="${portal.siteId}"
                      tool-id="${this.toolId}"
                      entity-id="${this.entityId}"
                      evaluated-item-id="${this.submission.id}"
                      @evaluation-removed=${this._onEvaluationRemoved}
                      only-show-if-evaluated>
                  </sakai-rubric-evaluation-remover>
                </div>
              </div>

              <div id="grader-rubric-block" class="ms-2 ${this.rubricShowing ? "d-block" : "d-none"}">
                <sakai-rubric-grading
                  site-id="${portal.siteId}"
                  tool-id="${this.toolId}"
                  entity-id="${this.entityId}"
                  evaluated-item-id="${this.submission.id}"
                  evaluated-item-owner-id="${this.submission.groupRef || this.submission.firstSubmitterId}"
                  ?group=${this.submission.groupId}
                  ?enable-pdf-export=${this.enablePdfExport}
                  @rubric-rating-changed=${this._onRubricRatingChanged}
                  @rubric-ratings-changed=${this._onRubricRatingsChanged}
                  @rubric-rating-tuned=${this._onRubricRatingTuned}
                  @update-comment=${this._onUpdateCriterionComment}
                ></sakai-rubric-grading>
                <button class="btn btn-primary"
                    title="${this.i18n.rubric_done_tooltip}"
                    aria-label="${this.i18n.rubric_done_tooltip}"
                    @click=${this._doneWithRubric}>
                  ${this.assignmentsI18n["gen.don"]}
                </button>
              </div>
            ` : ""}
            <!-- end hasAssociatedRubric -->

            ${this.submission.groupId ? html`
              <div id="grader-overrides-wrapper">
                <label>
                  <input type="checkbox" id="grader-override-toggle" ?checked=${this.showOverrides} @click=${e => this.showOverrides = e.target.checked} />
                  <span class="grader-overrides-label">${this.i18n.assign_grade_overrides}</span>
                </label>
                <div id="grader-overrides-block" class="d-${this.showOverrides ? 'block' : 'none'}">
                ${this.submission.submitters.map(s => html`
                  <div class="grader-override">
                    <div class="grader-overrides-display-name">${s.displayName} (${s.displayId})</div>
                    <div>${this._renderGradeInputs(this.i18n.override_grade_with, s)}</div>
                  </div>
                `)}
                </div>
              </div>
            ` : ""}
          </div>

          <div id="grader-controls-block" class="${this.rubricShowing ? "d-none" : "d-block"}">
            <div class="grader-block">
              <div class="feedback-label grader-label content-button-block">
                <button id="grader-feedback-button"
                    class="btn btn-link"
                    aria-controls="feedback-block"
                    @click=${this._toggleFeedbackCommentEditor}
                    aria-expanded="${this.feedbackCommentEditorShowing ? "true" : "false"}"
                    aria-label="${this.i18n.add_feedback_tooltip}"
                    title="${this.i18n.add_feedback_tooltip}">
                  ${this.submission.feedbackComment ? this.i18n.edit_feedback_comment : this.i18n.add_feedback_comment}
                </button>
              </div>
              <div class="sak-banner-warn ms-2 ${this.feedbackCommentRemoved ? "d-block" : "d-none"}">${this.i18n.removed}</div>

              ${this.submission.feedbackComment ? html`
                <div id="feedback-snippet"
                    class="grader-snippet rounded-3 ms-3 mt-2 ${this.feedbackCommentEditorShowing ? "d-none" : "d-block"}">
                  <div class="grader-snippet position-relative overflow-hidden rounded-3">
                    <div class="m-2 ${!this.showingFullFeedbackComment && !this.allFeedbackCommentVisible ? "fade-text" : ""}">
                      ${unsafeHTML(this.submission.feedbackComment)}
                    </div>
                    <div class="fade-overlay ${!this.showingFullFeedbackComment && !this.allFeedbackCommentVisible ? "d-block" : "d-none"}">
                    </div>
                  </div>
                  <div class="ms-2 p-2">
                    <button class="btn btn-transparent
                                    text-decoration-underline
                                    ${this.allFeedbackCommentVisible ? "d-none" : "d-inline"}"
                        @click=${this._toggleFullFeedbackComment}>
                      ${this.showingFullFeedbackComment ? this.i18n.show_less : this.i18n.show_all}
                    </button>
                  </div>
                </div>
                <div class="mt-2 ms-3 ${this.showRemoveFeedbackComment ? "d-block" : "d-none"}">
                  <button class="btn btn-transparent text-decoration-underline"
                      @click=${this._removeFeedbackComment}>
                    ${this.assignmentsI18n["gen.remove"]}
                  </button>
                </div>
              ` : ""}

              <div id="feedback-block" class="ms-2 ${this.feedbackCommentEditorShowing ? "d-block" : "d-none"}">
                <div class="feedback-instruction sak-banner-info">${this.i18n.feedback_instruction}</div>
                <div id="feedback-comment-unsaved-msg" class="feedback-instruction sak-banner-error d-none">${this.i18n.unsaved_text_warning}</div>
                <textarea id="grader-feedback-comment" .value=${this.submission.feedbackComment}></textarea>
                <div id="grader-media-feedback" class="grader-label">
                  <span class="feedback-label">${this.i18n.recorded_feedback_label}</span>
                  <fa-icon size="1.5em" i-class="fas microphone" path-prefix="/webcomponents/assets" style="vertical-align: middle;"></fa-icon>
                  <fa-icon size="1.5em" i-class="fas video" path-prefix="/webcomponents/assets" style="vertical-align: middle;"></fa-icon>
                </div>
                <button class="btn btn-primary mt-2" @click=${this._toggleFeedbackCommentEditor}>
                  ${this.assignmentsI18n["gen.don"]}
                </button>
              </div>
            </div>

            <div id="grader-feedback-attachments-block" class="grader-block grader-label">
              ${this.submission.feedbackAttachments ? html`
                <div class="feedback-attachments-title">${this.assignmentsI18n["download.feedback.attachment"]}</div>
                <div class="current-feedback-attachments">
                  ${this.submission.feedbackAttachments.map(att => html`
                    <div class="feedback-attachments-row">
                      <div class="feedback-attachment">
                        <a href="/access${att.ref}" title="${this.i18n.feedback_attachment_tooltip}">
                          <span>${att.name}</span>
                        </a>
                      </div>
                      <div class="feedback-attachment-remove">
                        <a data-ref="${att.ref}"
                            @click=${this._removeAttachment}
                            href="javascript:;">
                          ${this.assignmentsI18n["gen.remove"]}
                        </a>
                      </div>
                    </div>
                  `)}
                </div>` : ""}
              <sakai-grader-file-picker button-text="${this.assignmentsI18n["gen.addatt"]}"
                  style="display: inline-block;"
                  title="${this.i18n.add_attachments_tooltip}">
              </sakai-grader-file-picker>
            </div>
            ${this.submission.hasHistory ? html`
              <div id="grader-submission-history-wrapper">
                <div id="grader-submission-history-toggle">
                  <a href="javascript:;"
                    @click=${() => this.showingHistory = !this.showingHistory}
                    aria-label="${this.showingHistory ? this.i18n.hide_history_tooltip : this.i18n.show_history_tooltip}"
                    title="${this.showingHistory ? this.i18n.hide_history_tooltip : this.i18n.show_history_tooltip}">
                    ${this.showingHistory ? this.i18n.hide_history : this.i18n.show_history}
                  </a>
                </div>
                <div id="grader-submission-history" class="d-${this.showingHistory ? 'block' : 'none'}">
                  ${this.submission.history.comments ? html`
                    <div id="grader-history-comments-wrapper">
                      <div class="grader-history-title">${this.i18n.feedback_comments}</div>
                      <div class="grader-history-block">${unsafeHTML(this.submission.history.comments)}</div>
                    </div>
                  ` : ""}
                  ${this.submission.history.grades ? html`
                    <div id="grader-history-grades-wrapper">
                      <div class="grader-history-title">${this.i18n.previous_grades}</div>
                      <div class="grader-history-block">${unsafeHTML(this.submission.history.grades)}</div>
                    </div>
                  ` : ""}
                </div>
              </div>
            ` : ""}
            <div class="grader-block">
              <div class="grader-label content-button-block">
                <button id="grader-private-notes-button"
                    class="btn btn-link"
                    aria-controls="private-notes-block private-notes-snippet"
                    @click=${this._togglePrivateNotesEditor}
                    aria-label="${this.i18n.private_notes_tooltip}"
                    title="${this.i18n.private_notes_tooltip}">
                  ${this.submission.privateNotes ? this.i18n.edit_private_notes : this.i18n.add_private_notes}
                </button>
              </div>
              <div class="sak-banner-warn ms-2 ${this.privateNotesRemoved ? "d-block" : "d-none"}">${this.i18n.removed}</div>

              ${this.submission.privateNotes ? html`
                <div id="private-notes-snippet" class="grader-snippet ms-3 mt-2 ${this.privateNotesEditorShowing ? "d-none" : "d-block"}">
                  <div class="grader-snippet position-relative overflow-hidden">
                    <div class="m-2 ${!this.showingFullPrivateNotes && !this.allPrivateNotesVisible ? "fade-text" : ""}">${unsafeHTML(this.submission.privateNotes)}</div>
                    ${!this.showingFullPrivateNotes && !this.allPrivateNotesVisible ? html`
                    <div class="fade-overlay"></div>
                    ` : ""}
                  </div>
                  <div class="ms-2 p-2">
                    <button class="btn btn-transparent text-decoration-underline ${this.allPrivateNotesVisible ? "d-none" : "d-inline"}" @click=${this._toggleFullPrivateNotes}>
                      ${this.showingFullPrivateNotes ? this.i18n.show_less : this.i18n.show_all}
                    </button>
                  </div>
                </div>
                ${!this.modified || this.submission.privateNotes === this.nonEditedSubmission.privateNotes ? html`
                <div class="mt-2 ms-3 ${this.privateNotesEditorShowing ? "d-none" : "d-block"}">
                  <button class="btn btn-transparent text-decoration-underline"
                      @click=${this._removePrivateNotes}>
                    ${this.assignmentsI18n["gen.remove"]}
                  </button>
                </div>
                ` : ""}
              ` : ""}

              <div id="private-notes-block" class="ms-2 ${this.privateNotesEditorShowing ? "d-block" : "d-none"}">
                <div class="sak-banner-info">${unsafeHTML(this.i18n.private_notes_tooltip)}</div>
                <div id="private-notes-unsaved-msg" class="sak-banner-error d-none">${this.i18n.unsaved_text_warning}</div>
                <textarea id="grader-private-notes" .value=${this.submission.privateNotes}></textarea>
                <button class="btn btn-primary mt-2"
                    @click=${this._togglePrivateNotesEditor}>
                  ${this.assignmentsI18n["gen.don"]}
                </button>
              </div>
            </div>

            <div class="text-feedback">
            </div>
            ${this.submission.submittedTime && !this.submission.showExtension ? html`
              <div class="resubmission-checkbox">
                <label>
                  <input type="checkbox" .checked=${this.showResubmission} @change="${this._toggleResubmissionBlock}"/>
                  <span>${this.assignmentsI18n.allowResubmit}</span>
                </label>
              </div>
              ${this.showResubmission ? html`
                <div class="resubmission-block">
                  <span>${this.assignmentsI18n["allow.resubmit.number"]}:</span>
                  <select aria-label="${this.i18n.attempt_selector_label}"
                      @change=${e => this.submission.resubmitsAllowed = parseInt(e.target.value)}>
                    ${Array(10).fill().map((_, i) => html`
                      <option value="${i + 1}" .selected=${this.submission.resubmitsAllowed === i + 1}>${i + 1}</option>
                    `)}
                    <option value="-1" .selected=${this.submission.resubmitsAllowed === -1}>${this.i18n.unlimited}</option>
                  </select>
                  <span>${this.assignmentsI18n["allow.resubmit.closeTime"]}:</span>
                  <sakai-date-picker
                      epoch-millis="${this.submission.resubmitDate}"
                      @datetime-selected=${this._resubmitDateSelected}
                      label="${this.assignmentsI18n["allow.resubmit.closeTime"]}">
                  </sakai-date-picker>
                </div>
              ` : ""}
            ` : ""}
            ${this.submission.showExtension ? html`
              <div id="grader-extension-section" class="mt-2">
                <input type="checkbox" .checked=${this.allowExtension} id="allowExtensionToggle" name="allowExtensionToggle" @change=${this._toggleExtensionBlock}" />
                <label for="allowExtensionToggle" >${this.assignmentsI18n.allowExtension}</label>
                ${this.allowExtension ? html`
                  <div class="ms-2 mt-2">
                    <div>${this.assignmentsI18n.allowExtensionCaptionGrader}</div>
                    <div id="allowExtensionTime">
                      <label>${this.assignmentsI18n["gen.acesubunt"]}</label>
                      <sakai-date-picker
                          epoch-millis="${this.submission.extensionDate}"
                          @datetime-selected="${this._extensionDateSelected}"
                          label="${this.assignmentsI18n["gen.acesubunt"]}">
                      </sakai-date-picker>
                    </div>
                  </div>
                ` : ""}
              </div>
            ` : ""}
            <div id="grader-save-buttons" class="action-button-block act">
              <button accesskey="s"
                  class="btn btn-primary active"
                  name="save"
                  @click=${this._save}
                  ?disabled=${!this.canSave}>
                ${this.assignmentsI18n["gen.sav"]}
              </button>
              <button accesskey="d"
                  class="btn btn-link"
                  name="return"
                  data-release="true"
                  @click=${this._save}
                  ?disabled=${!this.canSave}>
                ${this.assignmentsI18n["gen.retustud"]}
              </button>
              <button class="btn btn-link" accesskey="x" name="cancel" @click=${this._cancel}>${this.assignmentsI18n["gen.can"]}</button>
            </div>
            ${this.saveSucceeded ? html`<div class="sak-banner-success">${this.i18n.successful_save}</div>` : ""}
            ${this.saveFailed ? html`<div class="sak-banner-error">${this.i18n.failed_save}</div>` : ""}
          </div>
        </div>
      </div>` : ""}
    `;
  }

  render() {

    return html`
      ${this._areSettingsInAction() ? html`
      <div class="sak-banner-warn">${this.i18n.filter_settings_warning}</div>
      ` : ""}
      ${this._renderTopbar()}
      <div id="grader-submitted-block" class="grader-block">
        <div class="d-flex mb-3">
          <sakai-user-photo user-id="${this._getPhotoUserId()}" classes="grader-photo" profile-popup="on"></sakai-user-photo>
          <div style="flex: 4;">
            <span class="submitter-name">
              ${this.submission.groupId ? this.submission.groupTitle : this.submission.firstSubmitterName}
            </span>
            ${this.submission.draft && this.submission.visible ? html`
            <span class="draft-submission">(${this.i18n.draft_submission})</span>
            ` : html`
              ${this.submission.submittedTime ? html`
              <div id="grader-submitted-label">${this.i18n.submitted}</div>
              ` : ""}
            `}
          </div>
        </div>
        <div class="d-flex align-items-center">
          <div class="submitted-time ${this.submission.draft ? "draft-time" : ""}">${this.submission.submittedTime}</div>
          ${this.submission.late ? html`<div class="grader-late ms-2">${this.assignmentsI18n["grades.lateness.late"]}</div>` : ""}
          ${this.submission.returned ? html`
            <div class="ms-2"><span class="grader-returned fa fa-eye" title="${this.i18n.returned_tooltip}" /></div>
          ` : ""}
        </div>
        ${this.submission.groupId && this.submission.submittedTime ? html`<div class="grader-group-members">${this.submission.groupMembers}</div>` : ""}
        <div class="attachments">
          ${this.submission.submittedAttachments?.length > 0 ? html`
            ${this.submission.submittedAttachments.map(r => html`
              <div>
                <button type="button" class="btn btn-transparent text-decoration-underline" data-url="${r.url}" @click=${this._previewAttachment}>${r.name}</button>
              </div>
            `)}` : ""}
        </div>
        <div class="timeSpent-block">
          ${this.submission.submitters?.length > 0 && this.submission.submitters[0].timeSpent ? html`
            <span>${this.assignmentsI18n["gen.assign.spent"]}</span>
            <span> ${this.submission.submitters[0].timeSpent}</span>
          ` : ""}
        </div>
      </div> <!-- /grader-submitted-block -->

      <div id="grader-container">
        ${this._renderGrader()}
        ${this._renderGradable()}
      </div>
    `;
  }
};

