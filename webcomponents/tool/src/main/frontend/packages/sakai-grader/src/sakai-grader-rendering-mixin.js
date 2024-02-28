import { html, nothing } from "lit";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import { ifDefined } from "lit/directives/if-defined.js";
import "../sakai-grader-file-picker.js";
import { Submission } from "./submission.js";
import "@sakai-ui/sakai-date-picker";
import "@sakai-ui/sakai-group-picker";
import "@sakai-ui/sakai-document-viewer/sakai-document-viewer.js";
import "@sakai-ui/sakai-lti-iframe";
import "@sakai-ui/sakai-user-photo";
import "@sakai-ui/sakai-icon";
import "@sakai-ui/sakai-rubrics/sakai-rubric-grading-button.js";
import "@sakai-ui/sakai-rubrics/sakai-rubric-grading.js";
import "@sakai-ui/sakai-rubrics/sakai-rubric-evaluation-remover.js";
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
                <div>
                  <label>
                    <input type="checkbox" ?disabled=${!this.hasSubmitted} @change=${this._submittedOnlyChanged} .checked=${this._submittedOnly} />
                    ${this.i18n["nav.view.subsOnly"]}
                  </label>
                </div>
                <div class="mt-2">
                  <label>
                  <div>${this.i18n.graded_status_label}</div>
                  <select @change=${this._gradedStatusSelected}>
                    <option value="all" ?selected=${!this._ungradedOnly && !this._gradedOnly}>${this.i18n.all_submissions}</option>
                    ${this.hasUngraded ? html`
                    <option value="ungraded" ?selected=${this._ungradedOnly}>${this.i18n.only_ungraded}</option>
                    ` : nothing }
                    ${this._hasGraded ? html`
                    <option value="graded" ?selected=${this._gradedOnly}>${this.i18n.only_graded}</option>
                    ` : nothing }
                  </select>
                  </label>
                </div>
                ${this.isGroupGradable ? nothing : html`
                  ${this.groups ? html`
                  <div class="grader-groups">
                    <div>${this.i18n.group_label}</div>
                    <sakai-group-picker .groups=${this.groups} @groups-selected=${this._groupsSelected}></sakai-group-picker>
                  </div>
                  ` : nothing }
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
          <span>${this.i18n.grad3}</span>
          <span>${this._totalGraded} / ${this._totalSubmissions}</span>
        </div>

        <div id="grader-navigator">
          <div class="d-flex align-items-center justify-content-center">
            <button class="btn btn-transparent text-decoration-underline"
                title="${this.i18n["nav.list"]}"
                @click=${this._toStudentList}>
              ${this.i18n["nav.list"]}
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
            <button class="btn btn-transparent"
                @click=${this._previous}
                aria-label="${this.i18n.previous_submission_label}">
              <i class="si si-arrow-left-circle-fill"></i>
            </button>
            <select id="grader-submitter-select" aria-label="${this.i18n.student_selector_label}" @change=${this._studentSelected}>
              ${this._submissions.map(s => html`<option value="${s.id}" .selected=${this._submission.id === s.id}>${this._getSubmitter(s)}</option>`)}
            </select>
            <button class="btn btn-transparent"
                @click=${this._next}
                aria-label="${this.i18n.next_submission_label}">
              <i class="si si-arrow-right-circle-fill"></i>
            </button>
          </div>
          <div>${this.currentStudentInfo}</div>
        </div>
      </div>
    `;
  }

  _renderGradable() {

    if (this._submissions.length === 0) {
      return html`<h2>No submitters</h2>`;
    }

    if (!this._submission.hydrated) {
      return html`<div class="sak-banner-info">${this.i18n.loading_submission}</div>`;
    }

    return html`
      <div id="gradable">
        ${this._submission.ltiSubmissionLaunch ? html`
          <div class="sak-banner-info">${unsafeHTML(this.i18n.lti_grade_launch_instructions)}</div>
          <sakai-lti-iframe
            allow-resize="yes"
            new-window-text="${this.i18n.lti_grade_launch_button}"
            launch-url="${this._submission.ltiSubmissionLaunch}">
          </sakai-lti-iframe>
        ` : nothing }
        ${this.ltiGradableLaunch && !this._submission.ltiSubmissionLaunch ? html`
          <div class="sak-banner-info">${unsafeHTML(this.i18n.lti_grade_launch_instructions)}</div>
          <sakai-lti-iframe
            allow-resize="yes"
            new-window-text="${this.i18n.lti_grade_launch_button}"
            launch-url="${this.ltiGradableLaunch}">
          </sakai-lti-iframe>
        ` : nothing }
        ${this._submission.submittedTime || (this._submission.draft && this._submission.visible) ? html`
        <h3 class="d-inline-block">${this.i18n["gen.subm"]}</h3>
        ` : html`
        <h3 class="d-inline-block">${this.i18n.no_submission}</h3>
        `}
        ${this._submission.ltiSubmissionLaunch ? nothing : html`
        <div id="grader-link-block" class="float-end">
          <button class="btn btn-primary active"
              data-bs-toggle="offcanvas"
              data-bs-target="#grader"
              aria-controls="grader">
            ${this.i18n.grade_submission}
          </button>
        </div>
        `}
        ${this._submission.submittedTime || (this._submission.draft && this._submission.visible) ? html`
          ${this._submittedTextMode ? html`
            <div id="grader-submitted-text-block">
              <div class="sak-banner-info">${unsafeHTML(this.i18n.inline_feedback_instruction)}</div>
              <textarea id="grader-feedback-text-editor" class="d-none">${this._submission.feedbackText}</textarea>
              <div id="grader-feedback-text">${unsafeHTML(this._submission.feedbackText)}</div>
              <button id="edit-inline-feedback-button"
                  class="btn btn-link inline-feedback-button"
                  @click=${this._toggleInlineFeedback}
                  aria-haspopup="true">
                ${this.i18n.addfeedback}
              </button>
              <button id="show-inline-feedback-button"
                  class="btn btn-link inline-feedback-button"
                  @click=${this._toggleInlineFeedback}
                  style="display: none;"
                  aria-haspopup="true">
                ${this.i18n["gen.don"]}
              </button>
            </div>
          ` : html`
            ${this._selectedAttachment || this._selectedPreview ? html`
              <div class="preview">
                <sakai-document-viewer
                    .preview=${this._selectedPreview}
                    .content=${this._selectedAttachment}>
                </sakai-document-viewer>
              </div>
            ` : nothing }
          `}
          ${this.gradable.allowPeerAssessment && this._submission.peerReviews?.length > 0 ? html`
          <div class="mt-4">
            <h3 class="mb-3">${this.i18n.peer_reviews}</h3>
            ${this._submission.peerReviews.map(pr => html`

              <div class="card mb-2">
                <div class="card-header fw-bold">${pr.assessorDisplayName}</div>
                <div class="card-body">
                  <div class="card-text">
                    <div>
                      <span class="fw-bold me-2">${this.i18n.grade}</span>
                      <span>${pr.scoreDisplay}</span>
                    </div>
                    <div class="mt-2 mb-2 fw-bold">${this.i18n.reviewer_comments}</div>
                    <div>${unsafeHTML(pr.comment)}</div>
                    ${pr.attachmentRefList && pr.attachmentRefList.length > 0 ? html`
                      <div class="fw-bold mb-2">${this.i18n.reviewer_attachments}</div>
                      ${pr.attachmentRefList.map((ref, i) => html`
                        <div class="feedback-attachment">
                          <a href="/access${ref.reference}" title="${this.i18n.feedback_attachment_tooltip}">${this.i18n.attachment} ${i + 1}</a>
                        </div>
                      `)}
                    ` : nothing}
                  </div>
                </div>
              </div>
            `)}
          </div>
          ` : nothing }
        ` : nothing }
      </div>
    `;
  }

  _renderSaved() {

    return html`<span class="saved fa fa-check-circle ${this._saveSucceeded ? "d-inline" : "d-none"} text-success"
                  title="${this.i18n.saved_successfully}">
                </span>`;
  }

  _renderFailed() {

    return html`<span class="saved failed fa fa-times-circle ${this._saveFailed ? "d-inline" : "d-none"} text-danger"
                  title="${this.i18n.failed_save}">
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
          <option value="">${this.i18n["non.submission.grade.select"]}</option>
          ${this.letterGradeOptions.map(grade => html`
          <option value="${grade}"
              .selected=${submitter ? submitter.overridden && submitter.grade === grade : this._submission.grade === grade}>
            ${grade}
          </option>
          `)}
        </select>
        ${this._renderSaved()}
        ${this._renderFailed()}
      ` : nothing }
      ${this.gradeScale === SCORE_GRADE_TYPE ? html`
        <input id="score-grade-input" aria-label="${this.i18n.number_grade_label}"
          @keydown=${this._validateGradeInput}
          @keyup=${submitter ? undefined : this._gradeSelected}
          data-user-id="${ifDefined(submitter ? submitter.id : undefined)}"
          type="text"
          class="points-input ${ifDefined(submitter ? "grader-grade-override" : undefined)}"
          .value=${submitter ? submitter.overridden ? submitter.grade : "" : this._submission.grade} />
        ${this._renderSaved()}
        ${this._renderFailed()}
        <span>(${this.i18n["grade.max"]} ${this.gradable.maxGradePoint})</span>
        ${this.gradable.allowPeerAssessment ? html`
          <button id="peer-info"
              class="btn transparent-button"
              type="button"
              aria-label="${this.i18n.peer_info_label}"
              data-bs-toggle="popover"
              data-bs-container="body"
              data-bs-placement="auto"
              data-bs-content="${this.assignmentsI18n["peerassessment.peerGradeInfo"]}">
            <span class="fa fa-info-circle"></span>
          </button>
        ` : nothing }
      ` : nothing }
      ${this.gradeScale === PASS_FAIL_GRADE_TYPE ? html`
        <select id="pass-fail-selector"
                  aria-label="${this.i18n.passfail_selector_label}"
                  class=${ifDefined(submitter ? "grader-grade-override" : undefined)}
                  data-user-id="${ifDefined(submitter ? submitter.id : undefined)}"
                  @change=${submitter ? undefined : this._gradeSelected}
                  .value=${submitter ? submitter.grade : this._submission.grade}>
          <option value="ungraded"
              .selected=${submitter ? !submitter.overridden : this._submission.grade === this.i18n.ungra}>
            ${this.i18n.ungra}
          </option>
          <option value="pass"
              .selected=${submitter ? submitter.overridden && submitter.grade === this.i18n.pass : this._submission.grade.match(/^pass$/i)}>
            ${this.i18n.pass}
          </option>
          <option value="fail"
              .selected=${submitter ? submitter.overridden && submitter.grade === this.i18n.fail : this._submission.grade.match(/^fail$/i)}>
            ${this.i18n.fail}
          </option>
        </select>
        ${this._renderSaved()}
        ${this._renderFailed()}
      ` : nothing }
      ${this.gradeScale === CHECK_GRADE_TYPE ? html`
        <input id="check-grade-input"
                type="checkbox"
                data-user-id="${ifDefined(submitter ? submitter.id : undefined)}"
                aria-label="${this.i18n.checkgrade_label}"
                class=${ifDefined(submitter ? "grader-grade-override" : undefined)}
                @click=${submitter ? undefined : this._gradeSelected}
                value=${GRADE_CHECKED}
                .checked=${submitter ? submitter.overridden && submitter.grade === this.i18n["gen.checked"] : this._submission.grade === this.i18n["gen.checked"]}>
        </input>
        <span>${this.i18n["gen.gra2"]} ${this.i18n["gen.checked"]}</span>
        ${this._renderSaved()}
        ${this._renderFailed()}
      ` : nothing }
    `;
  }

  _renderGrader() {

    // Hide the right UI until we have push notifications for grade changes
    if (this._submission.ltiSubmissionLaunch) return "";

    return html`
      ${this._submission.id !== "dummy" ? html`

      <div id="grader" class="offcanvas offcanvas-end" data-bs-backdrop="static" tabindex="-1" aria-labelledby="grader-label">

        <div class="offcanvas-header">
          <h2 class="offcanvas-title" id="grader-label">${this.i18n.grader}</h2>
          <button type="button" class="btn-close text-reset" data-bs-dismiss="offcanvas" aria-label="Close"></button>
        </div>

        <div class="offcanvas-body">

          <div class="fw-bold fs-5">${this._getSubmitter(this._submission)}</div>

          <!-- START ORIGINALITY BLOCK -->
          ${this._submission.originalityShowing ? html`
            <div>
              <label class="grader-label grader-originality-label fw-bold">
                <span>${this._submission.originalityServiceName}</span>
                <span>${this.i18n["review.report"]}</span>
              </label>
              ${this._submission.originalitySupplies.map(result => html`
                <div class="grader-originality-section" >
                  ${result[Submission.originalityConstants.originalityLink] !== "Error" ? html`
                    <a target="_blank"
                        href="${result[Submission.originalityConstants.originalityLink]}"
                        class="grader-originality-link">
                      <i class="${result[Submission.originalityConstants.originalityIcon]}"></i>
                      <span>${result[Submission.originalityConstants.originalityScore]}${this.i18n["content_review.score_display.grader"]}</span>
                    </a>
                    <span>
                      <span class="grader-originality-delimiter">${this.i18n["content_review.delimiter"]}</span>
                      <span>${result[Submission.originalityConstants.originalityName]}</span>
                    </span>
                  ` : html`
                    ${result[Submission.originalityConstants.originalityStatus] === "true" ? html`
                      <a href="#${result[Submission.originalityConstants.originalityKey]}"
                          data-bs-toggle="collapse"
                          role="button"
                          aria-expanded="false"
                          aria-controls="${result[Submission.originalityConstants.originalityKey]}"
                          class="grader-originality-link">
                        <i class="${result[Submission.originalityConstants.originalityIcon]}"></i>
                        <span>${this.i18n["content_review.disclosure.pending"]}</span>
                      </a>
                      <span>
                        <span class="grader-originality-delimiter">${this.i18n["content_review.delimiter"]}</span>
                        <span>${result[Submission.originalityConstants.originalityName]}</span>
                      </span>
                      <div class="collapse grader-originality-caption" id="${result[Submission.originalityConstants.originalityKey]}">
                        <span>${this.i18n["content_review.notYetSubmitted.grader"]}</span>
                        <span>${this._submission.originalityServiceName}</span>
                      </div>
                    ` : html`
                      <a href="#${result[Submission.originalityConstants.originalityKey]}"
                          data-bs-toggle="collapse"
                          role="button"
                          aria-expanded="false"
                          aria-controls="${result[Submission.originalityConstants.originalityKey]}"
                          class="grader-originality-link">
                        <i class="${result[Submission.originalityConstants.originalityIcon]}"></i><span>${this.i18n["content_review.disclosure.error"]}</span>
                      </a>
                      <span>
                        <span class="grader-originality-delimiter">${this.i18n["content_review.delimiter"]}</span>
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
          ` : nothing }
          <!-- END ORIGINALITY BLOCK -->

          <div id="grader-grade-block" class="grader-block">
            ${this._renderGradeInputs(this.i18n["gen.assign.gra"])}
            <!-- start hasAssociatedRubric -->
            ${this.hasAssociatedRubric === "true" ? html`
              <div class="d-flex align-items-center mt-3">
                <div>
                  <button id="grader-rubric-button"
                      class="btn btn-link"
                      @click=${this._toggleRubric}
                      aria-label="${this.i18n.grading_rubric}"
                      title="${this.i18n.grading_rubric}"
                      aria-controls="grader-rubric-block grader-controls-block">
                    ${this.i18n.rubric}
                  </button>
                </div>
                <div>
                  <sakai-rubric-grading-button
                      id="grader-rubric-link"
                      aria-label="${this.i18n.grading_rubric}"
                      title="${this.i18n.grading_rubric}"
                      @click=${this._toggleRubric}
                      site-id="${portal.siteId}"
                      tool-id="${this.toolId}"
                      entity-id="${this.entityId}"
                      evaluated-item-id="${this._submission.id}"
                      aria-controls="grader-rubric-block grader-controls-block"
                      only-show-if-evaluated>
                  </sakai-rubric-grading-button>
                  <sakai-rubric-evaluation-remover
                      class="ms-2"
                      site-id="${portal.siteId}"
                      tool-id="${this.toolId}"
                      entity-id="${this.entityId}"
                      evaluated-item-id="${this._submission.id}"
                      @evaluation-removed=${this._onEvaluationRemoved}
                      only-show-if-evaluated>
                  </sakai-rubric-evaluation-remover>
                </div>
              </div>

              <div id="grader-rubric-block" class="ms-2 ${this._rubricShowing ? "d-block" : "d-none"}">
                <sakai-rubric-grading
                  site-id="${portal.siteId}"
                  tool-id="${this.toolId}"
                  entity-id="${this.entityId}"
                  evaluated-item-id="${this._submission.id}"
                  evaluated-item-owner-id="${this._submission.groupRef || this._submission.firstSubmitterId}"
                  ?group=${this._submission.groupId}
                  ?enable-pdf-export=${this.enablePdfExport}
                  @rubric-rating-changed=${this._onRubricRatingChanged}
                  @rubric-ratings-changed=${this._onRubricRatingsChanged}
                  @rubric-rating-tuned=${this._onRubricRatingTuned}
                  @total-points-updated=${this._onRubricTotalPointsUpdated}
                  @update-comment=${this._onUpdateCriterionComment}>
                </sakai-rubric-grading>
                <button class="btn btn-primary"
                    title="${this.i18n.rubric_done_tooltip}"
                    aria-label="${this.i18n.rubric_done_tooltip}"
                    @click=${this._doneWithRubric}>
                  ${this.i18n["gen.don"]}
                </button>
              </div>
            ` : nothing }
            <!-- end hasAssociatedRubric -->

            ${this._submission.groupId ? html`
              <div id="grader-overrides-wrapper">
                <label>
                  <input type="checkbox" id="grader-override-toggle" ?checked=${this._showOverrides} @click=${e => this._showOverrides = e.target.checked} />
                  <span class="grader-overrides-label">${this.i18n.assign_grade_overrides}</span>
                </label>
                <div id="grader-overrides-block" class="d-${this._showOverrides ? "block" : "none"}">
                ${this._submission.submitters.map(s => html`
                  <div class="grader-override">
                    <div class="grader-overrides-display-name">${s.displayName} (${s.displayId})</div>
                    <div>${this._renderGradeInputs(this.i18n.override_grade_with, s)}</div>
                  </div>
                `)}
                </div>
              </div>
            ` : nothing }
          </div>

          <div id="grader-controls-block" class="${this._rubricShowing ? "d-none" : "d-block"}">
            <div class="grader-block">
              <div class="feedback-label grader-label content-button-block">
                <button id="grader-feedback-button"
                    class="btn btn-link"
                    aria-controls="feedback-block"
                    @click=${this._toggleFeedbackCommentEditor}
                    aria-expanded="${this._feedbackCommentEditorShowing ? "true" : "false"}"
                    aria-label="${this.i18n.add_feedback_tooltip}"
                    title="${this.i18n.add_feedback_tooltip}">
                  ${this._submission.feedbackComment ? this.i18n.edit_feedback_comment : this.i18n.add_feedback_comment}
                </button>
              </div>
              <div class="sak-banner-warn ms-2 ${this._feedbackCommentRemoved ? "d-block" : "d-none"}">${this.i18n.removed}</div>

              ${this._submission.feedbackComment ? html`
                <div id="feedback-snippet"
                    class="grader-snippet rounded-3 ms-3 mt-2 ${this._feedbackCommentEditorShowing ? "d-none" : "d-block"}">
                  <div class="grader-snippet position-relative overflow-hidden rounded-3">
                    <div class="m-2 ${!this._showingFullFeedbackComment && !this._allFeedbackCommentVisible ? "fade-text" : ""}">
                      ${unsafeHTML(this._submission.feedbackComment)}
                    </div>
                    <div class="fade-overlay ${!this._showingFullFeedbackComment && !this._allFeedbackCommentVisible ? "d-block" : "d-none"}">
                    </div>
                  </div>
                  <div class="ms-2 p-2">
                    <button class="btn btn-transparent
                                    text-decoration-underline
                                    ${this._allFeedbackCommentVisible ? "d-none" : "d-inline"}"
                        @click=${this._toggleFullFeedbackComment}>
                      ${this._showingFullFeedbackComment ? this.i18n.show_less : this.i18n.show_all}
                    </button>
                  </div>
                </div>
                <div class="mt-2 ms-3 ${this._showRemoveFeedbackComment ? "d-block" : "d-none"}">
                  <button class="btn btn-transparent text-decoration-underline"
                      @click=${this._removeFeedbackComment}>
                    ${this.i18n["gen.remove"]}
                  </button>
                </div>
              ` : nothing }

              <div id="feedback-block" class="ms-2 ${this._feedbackCommentEditorShowing ? "d-block" : "d-none"}">
                <div class="feedback-instruction sak-banner-info">${this.i18n.feedback_instruction}</div>
                <div id="feedback-comment-unsaved-msg" class="feedback-instruction sak-banner-error d-none">${this.i18n.unsaved_text_warning}</div>
                <textarea id="grader-feedback-comment" .value=${this._submission.feedbackComment}></textarea>
                <div id="grader-media-feedback" class="grader-label">
                  <span class="feedback-label">${this.i18n.recorded_feedback_label}</span>
                  <sakai-icon type="microphone"></sakai-icon>
                  <sakai-icon type="video"></sakai-icon>
                </div>
                <button class="btn btn-primary mt-2" @click=${this._toggleFeedbackCommentEditor}>
                  ${this.i18n["gen.don"]}
                </button>
              </div>
            </div>

            <div id="grader-feedback-attachments-block" class="grader-block grader-label">
              ${this._submission.feedbackAttachments ? html`
                <div class="feedback-attachments-title">${this.i18n["download.feedback.attachment"]}</div>
                <div class="current-feedback-attachments">
                  ${this._submission.feedbackAttachments.map(att => html`
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
                          ${this.i18n["gen.remove"]}
                        </a>
                      </div>
                    </div>
                  `)}
                </div>
              ` : nothing }
              <sakai-grader-file-picker button-text="${this.i18n["gen.addatt"]}"
                  style="display: inline-block;"
                  title="${this.i18n.add_attachments_tooltip}">
              </sakai-grader-file-picker>
            </div>
            ${this._submission.hasHistory ? html`
              <div id="grader-submission-history-wrapper">
                <div id="grader-submission-history-toggle">
                  <a href="javascript:;"
                    @click=${() => this._showingHistory = !this._showingHistory}
                    aria-label="${this._showingHistory ? this.i18n.hide_history_tooltip : this.i18n.show_history_tooltip}"
                    title="${this._showingHistory ? this.i18n.hide_history_tooltip : this.i18n.show_history_tooltip}">
                    ${this._showingHistory ? this.i18n.hide_history : this.i18n.show_history}
                  </a>
                </div>
                <div id="grader-submission-history" class="d-${this._showingHistory ? "block" : "none"}">
                  ${this._submission.history.comments ? html`
                    <div id="grader-history-comments-wrapper">
                      <div class="grader-history-title">${this.i18n.feedback_comments}</div>
                      <div class="grader-history-block">${unsafeHTML(this._submission.history.comments)}</div>
                    </div>
                  ` : nothing }
                  ${this._submission.history.grades ? html`
                    <div id="grader-history-grades-wrapper">
                      <div class="grader-history-title">${this.i18n.previous_grades}</div>
                      <div class="grader-history-block">${unsafeHTML(this._submission.history.grades)}</div>
                    </div>
                  ` : nothing }
                </div>
              </div>
            ` : nothing }
            <div class="grader-block">
              <div class="grader-label content-button-block">
                <button id="grader-private-notes-button"
                    class="btn btn-link"
                    aria-controls="private-notes-block private-notes-snippet"
                    @click=${this._togglePrivateNotesEditor}
                    aria-label="${this.i18n.private_notes_tooltip}"
                    title="${this.i18n.private_notes_tooltip}">
                  ${this._submission.privateNotes ? this.i18n.edit_private_notes : this.i18n.add_private_notes}
                </button>
              </div>
              <div class="sak-banner-warn ms-2 ${this._privateNotesRemoved ? "d-block" : "d-none"}">${this.i18n.removed}</div>

              ${this._submission.privateNotes ? html`
                <div id="private-notes-snippet" class="grader-snippet ms-3 mt-2 ${this._privateNotesEditorShowing ? "d-none" : "d-block"}">
                  <div class="grader-snippet position-relative overflow-hidden">
                    <div class="m-2 ${!this._showingFullPrivateNotes && !this._allPrivateNotesVisible ? "fade-text" : ""}">${unsafeHTML(this._submission.privateNotes)}</div>
                    ${!this._showingFullPrivateNotes && !this._allPrivateNotesVisible ? html`
                    <div class="fade-overlay"></div>
                    ` : nothing }
                  </div>
                  <div class="ms-2 p-2">
                    <button class="btn btn-transparent text-decoration-underline ${this._allPrivateNotesVisible ? "d-none" : "d-inline"}" @click=${this._toggleFullPrivateNotes}>
                      ${this._showingFullPrivateNotes ? this.i18n.show_less : this.i18n.show_all}
                    </button>
                  </div>
                </div>
                ${!this.modified || this._submission.privateNotes === this._nonEditedSubmission.privateNotes ? html`
                <div class="mt-2 ms-3 ${this._privateNotesEditorShowing ? "d-none" : "d-block"}">
                  <button class="btn btn-transparent text-decoration-underline"
                      @click=${this._removePrivateNotes}>
                    ${this.i18n["gen.remove"]}
                  </button>
                </div>
                ` : nothing }
              ` : nothing }

              <div id="private-notes-block" class="ms-2 ${this._privateNotesEditorShowing ? "d-block" : "d-none"}">
                <div class="sak-banner-info">${unsafeHTML(this.i18n.private_notes_tooltip)}</div>
                <div id="private-notes-unsaved-msg" class="sak-banner-error d-none">${this.i18n.unsaved_text_warning}</div>
                <textarea id="grader-private-notes" .value=${this._submission.privateNotes}></textarea>
                <button class="btn btn-primary mt-2"
                    @click=${this._togglePrivateNotesEditor}>
                  ${this.i18n["gen.don"]}
                </button>
              </div>
            </div>

            <div class="text-feedback">
            </div>
            ${this._submission.submittedTime && !this._submission.showExtension ? html`
              <div class="resubmission-checkbox">
                <label>
                  <input type="checkbox" .checked=${this._showResubmission} @change="${this._toggleResubmissionBlock}"/>
                  <span>${this.i18n.allowResubmit}</span>
                </label>
              </div>
              ${this._showResubmission ? html`
                <div class="resubmission-block">
                  <span>${this.i18n["allow.resubmit.number"]}:</span>
                  <select aria-label="${this.i18n.attempt_selector_label}"
                      @change=${e => this._submission.resubmitsAllowed = parseInt(e.target.value)}>
                    ${Array(10).fill().map((_, i) => html`
                      <option value="${i + 1}" .selected=${this._submission.resubmitsAllowed === i + 1}>${i + 1}</option>
                    `)}
                    <option value="-1" .selected=${this._submission.resubmitsAllowed === -1}>${this.i18n.unlimited}</option>
                  </select>
                  <span>${this.i18n["allow.resubmit.closeTime"]}:</span>
                  <sakai-date-picker
                      epoch-millis="${this._submission.resubmitDate}"
                      @datetime-selected=${this._resubmitDateSelected}
                      label="${this.i18n["allow.resubmit.closeTime"]}">
                  </sakai-date-picker>
                </div>
              ` : nothing }
            ` : nothing }
            ${this._submission.showExtension ? html`
              <div id="grader-extension-section" class="mt-2">
                <input type="checkbox" .checked=${this._allowExtension} id="allowExtensionToggle" name="allowExtensionToggle" @change=${this._toggleExtensionBlock} />
                <label for="allowExtensionToggle" >${this.i18n.allowExtension}</label>
                ${this._allowExtension ? html`
                  <div class="ms-2 mt-2">
                    <div>${this.i18n.allowExtensionCaptionGrader}</div>
                    <div id="allowExtensionTime">
                      <label>${this.i18n["gen.acesubunt"]}</label>
                      <sakai-date-picker
                          epoch-millis="${this._submission.extensionDate}"
                          @datetime-selected="${this._extensionDateSelected}"
                          label="${this.i18n["gen.acesubunt"]}">
                      </sakai-date-picker>
                    </div>
                  </div>
                ` : nothing }
              </div>
            ` : nothing }
            <div id="grader-save-buttons" class="action-button-block act">
              <button id="grader-save-button"
                  accesskey="s"
                  class="btn btn-primary active"
                  name="save"
                  @click=${this._save}>
                ${this.i18n["gen.sav"]}
              </button>
              <button accesskey="d"
                  class="btn btn-link"
                  name="return"
                  data-release="true"
                  @click=${this._save}>
                ${this.i18n["gen.retustud"]}
              </button>
              <button class="btn btn-link" accesskey="x" name="cancel" @click=${this._cancel}>${this.i18n["gen.can"]}</button>
            </div>
            ${this._saving ? html`<div class="sak-banner-info">${this.i18n.saving}</div>` : ""}
            ${this._saveSucceeded ? html`<div class="sak-banner-success">${this.i18n.successful_save}</div>` : nothing }
            ${this._saveFailed ? html`<div class="sak-banner-error">${this.i18n.failed_save}</div>` : nothing }
          </div>
        </div>
      </div>
      ` : nothing }
    `;
  }

  render() {

    if (this._loadingData) {
      return html`
        <div class="sak-banner-info">
          <div class="mb-3 fs-5 fw-bold">${this.i18n.loading_1}</div>
          <div>${this.i18n.loading_2}</div>
        </div>
      `;
    }

    return html`
      ${this._areSettingsInAction() ? html`
      <div id="grader-filter-warning" class="sak-banner-warn">${this.i18n.filter_settings_warning}</div>
      ` : nothing }
      ${this._renderTopbar()}
      <div id="grader-submitted-block" class="grader-block">
        <div class="d-flex mb-3">
          <sakai-user-photo user-id="${this._getPhotoUserId()}" classes="grader-photo" profile-popup="on"></sakai-user-photo>
          <div style="flex: 4;">
            <span class="submitter-name">
              ${this._getSubmitter(this._submission)}
            </span>
            ${this._submission.draft && this._submission.visible ? html`
            <span class="draft-submission">(${this.i18n.draft_submission})</span>
            ` : html`
              ${this._submission.submittedTime ? html`
              <div id="grader-submitted-label">${this.i18n.submitted}</div>
              ` : nothing }
            `}
          </div>
        </div>
        <div class="d-flex align-items-center">
          <div class="submitted-time ${this._submission.draft ? "draft-time" : ""}">${this._submission.submittedTime}</div>
          ${this._submission.late ? html`<div class="grader-late ms-2">${this.i18n["grades.lateness.late"]}</div>` : ""}
          ${this._submission.returned ? html`
            <div class="ms-2"><span class="grader-returned fa fa-eye" title="${this.i18n.returned_tooltip}"></span></div>
          ` : nothing }
        </div>
        ${this._submission.groupId && this._submission.submittedTime ? html`<div class="grader-group-members">${this._submission.groupMembers}</div>` : nothing }
        <div class="attachments">
          ${this._submission.submittedText
              && this._submission.visible
              && this.gradable.submissionType === "TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION"
              && this._submission.hasNonInlineAttachments ? html`
          <div>
            <button type="button"
                class="btn btn-transparent text-decoration-underline"
                @click=${() => this._submittedTextMode = true}>
              ${this.i18n.submission_inline}
            </button>
          </div>
          ` : nothing}
          ${this._submission.submittedAttachments.filter(r => !r.ref.includes("InlineSub")).map(r => html`
            <div>
              <button type="button"
                  class="btn btn-transparent text-decoration-underline"
                  data-ref="${r.ref}"
                  @click=${this._previewAttachment}>
                ${r.name}
              </button>
            </div>
          `)}
        </div>
        <div class="timeSpent-block">
          ${this._submission.submitters?.length > 0 && this._submission.submitters[0].timeSpent ? html`
            <span>${this.i18n["gen.assign.spent"]}</span>
            <span> ${this._submission.submitters[0].timeSpent}</span>
          ` : nothing}
        </div>
      </div> <!-- /grader-submitted-block -->

      <div id="grader-container">
        ${this._renderGrader()}
        ${this._renderGradable()}
      </div>
    `;
  }
};

