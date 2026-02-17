import { html, nothing } from "lit";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import { ifDefined } from "lit/directives/if-defined.js";
import "../sakai-grader-file-picker.js";
import { Submission } from "./submission.js";
import "@sakai-ui/sakai-date-picker";
import "@sakai-ui/sakai-group-picker/sakai-group-picker.js";
import "@sakai-ui/sakai-document-viewer/sakai-document-viewer.js";
import "@sakai-ui/sakai-lti-iframe/sakai-lti-iframe.js";
import "@sakai-ui/sakai-user-photo";
import "@sakai-ui/sakai-icon";
import "@sakai-ui/sakai-rubrics/sakai-rubric-grading-button.js";
import "@sakai-ui/sakai-rubrics/sakai-rubric-grading.js";
import "@sakai-ui/sakai-rubrics/sakai-rubric-evaluation-remover.js";
import "@sakai-ui/sakai-rubrics/sakai-rubric-student.js";
import { getSiteId } from "@sakai-ui/sakai-portal-utils";
import {
  GRADE_CHECKED,
  LETTER_GRADE_TYPE,
  SCORE_GRADE_TYPE,
  PASS_FAIL_GRADE_TYPE,
  CHECK_GRADE_TYPE,
  TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION
} from "./sakai-grader-constants.js";

export const graderRenderingMixin = Base => class extends Base {

  _renderTopbar() {

    return html`
      <div id="grader-topbar">

        <div class="modal fade" id="grader-settings" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="grader-settings-modal-label" aria-hidden="true">
          <div class="modal-dialog">
            <div class="modal-content">
              <div class="modal-header">
                <h5 class="modal-title" id="grader-settings-modal-label">${this._i18n.settings}</h5>
                <button type="button" class="btn-close d-lg-none" data-bs-dismiss="modal" aria-label="Close"></button>
              </div>
              <div class="modal-body">
                <div>
                  <label>
                    <input type="checkbox" ?disabled=${!this.hasSubmitted} @change=${this._submittedOnlyChanged} .checked=${this._submittedOnly} />
                    ${this._i18n["nav.view.subsOnly"]}
                  </label>
                </div>
                <div class="mt-2">
                  <label>
                  <div>${this._i18n.graded_status_label}</div>
                  <select @change=${this._gradedStatusSelected}>
                    <option value="all" ?selected=${!this._ungradedOnly && !this._gradedOnly}>${this._i18n.all_submissions}</option>
                    ${this.hasUngraded ? html`
                    <option value="ungraded" ?selected=${this._ungradedOnly}>${this._i18n.only_ungraded}</option>
                    ` : nothing }
                    ${this._hasGraded ? html`
                    <option value="graded" ?selected=${this._gradedOnly}>${this._i18n.only_graded}</option>
                    ` : nothing }
                  </select>
                  </label>
                </div>
                ${this.isGroupGradable ? nothing : html`
                  ${this.groups ? html`
                  <div class="grader-groups">
                    <div>${this._i18n.group_label}</div>
                    <sakai-group-picker .groups=${this.groups}
                        @groups-selected=${this._groupsSelected}
                        group-ref=${ifDefined(this.selectedGroup)}>
                    </sakai-group-picker>
                  </div>
                  ` : nothing }
                `}
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-primary" data-bs-dismiss="modal">${this._i18n.done}</button>
              </div>
            </div>
          </div>
        </div>

        <div id="grader-title-block">
          <div class="grader-title">
            <div class="assessment-title fs-5">${this.gradableTitle}</div>
          </div>
        </div>

        <div id="grader-total" class="fs-6">
          <span>${this._i18n.grad3}</span>
          <span>${this._totalGraded} / ${this._totalSubmissions}</span>
        </div>

        <div id="grader-navigator">
          <div class="d-flex align-items-center justify-content-center">
            <button class="btn btn-transparent text-decoration-underline"
                title="${this._i18n["nav.list"]}"
                @click=${this._toStudentList}>
              ${this._i18n["nav.list"]}
            </button>
            <button id="grader-settings-link"
                class="btn icon-button ms-2"
                data-bs-toggle="modal"
                data-bs-target="#grader-settings"
                title="${this._i18n.settings}"
                aria-label="${this._i18n.settings}"
                aria-controls="grader-settings">
              <i class="si si-settings"></i>
            </button>
          </div>
          <div>
            <button class="btn btn-transparent"
                @click=${this._previous}
                aria-label="${this._i18n.previous_submission_label}">
              <i class="si si-arrow-left-circle-fill"></i>
            </button>
            <select id="grader-submitter-select" aria-label="${this._i18n.student_selector_label}" @change=${this._studentSelected}>
              ${this._submissions.map(s => html`<option value="${s.id}" .selected=${this._submission.id === s.id}>${this._getSubmitter(s)}</option>`)}
            </select>
            <button class="btn btn-transparent"
                @click=${this._next}
                aria-label="${this._i18n.next_submission_label}">
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
      return html`<div class="sak-banner-info">${this._i18n.loading_submission}</div>`;
    }

    return html`
      <div id="gradable">
        ${this._submission.ltiSubmissionLaunch ? html`
          ${this._renderGraderLinkBlock()}
          <div class="sak-banner-info">${unsafeHTML(this._i18n.lti_grade_launch_instructions)}</div>
          <sakai-lti-iframe
            allow-resize="yes"
            height="${this.ltiFrameHeight || "1200"}"
            new-window-text="${this._i18n.lti_grade_launch_button}"
            launch-url="${this._submission.ltiSubmissionLaunch}"
            allow="${ifDefined(this.allow)}">
          </sakai-lti-iframe>
        ` : nothing }
        ${this.ltiGradableLaunch && !this._submission.ltiSubmissionLaunch ? html`
          ${this._renderGraderLinkBlock()}
          <div class="sak-banner-info">${unsafeHTML(this._i18n.lti_grade_launch_instructions)}</div>
          <sakai-lti-iframe
            allow-resize="yes"
            height="${this.ltiFrameHeight || "1200"}"
            new-window-text="${this._i18n.lti_grade_launch_button}"
            launch-url="${this.ltiGradableLaunch}"
            allow="${ifDefined(this.allow)}">
          </sakai-lti-iframe>
        ` : nothing }
        ${this._submission.ltiSubmissionLaunch ? nothing : html`
          ${this._submission.hasSubmittedDate || (this._submission.draft && this._submission.visible) ? html`
            <h3 class="d-inline-block">${this._i18n["gen.subm"]}</h3>
          ` : html`
            <h3 class="d-inline-block">${this._i18n.no_submission}</h3>
          `}
          ${this._renderGraderLinkBlock()}
        `}
        ${this._submission.hasSubmittedDate || (this._submission.draft && this._submission.visible) ? html`
          ${this._submittedTextMode ? html`
            <div>
              <div class="sak-banner-info">${unsafeHTML(this._i18n.inline_feedback_instruction)}</div>
              <textarea id="grader-inline-feedback-editor"
                  class="${!this._inlineFeedbackEditorShowing ? "d-none" : ""}"
                  .value=${this._submission.feedbackText}>
              </textarea>
              <div id="grader-feedback-text" class="${this._inlineFeedbackEditorShowing ? "d-none" : ""}">${unsafeHTML(this._submission.feedbackText)}</div>
              <button class="btn btn-secondary inline-feedback-button ${this._inlineFeedbackEditorShowing ? "d-none" : ""}"
                  @click=${this._toggleInlineFeedback}
                  aria-haspopup="true">
                ${this._i18n.addfeedback}
              </button>
              <button class="btn btn-secondary inline-feedback-button ${!this._inlineFeedbackEditorShowing ? "d-none" : ""}"
                  @click=${this._toggleInlineFeedback}
                  aria-haspopup="true">
                ${this._i18n["gen.don"]}
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
          <div class="mt-5">
            <h3 class="mb-3">${this._i18n.peer_reviews}</h3>
            <div class="accordion" id="peer-reviews">
              ${this._submission.peerReviews.map(pr => html`
                <div class="accordion-item">
                  <h2 class="accordion-header" id="peer-heading-${pr.assessorUserId}">
                    <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#peer-collapse-${pr.assessorUserId}" aria-expanded="false" aria-controls="peer-collapse-${pr.assessorUserId}">
                      ${pr.assessorDisplayName}
                    </button>
                  </h2>
                  <div id="peer-collapse-${pr.assessorUserId}" class="accordion-collapse collapse" aria-labelledby="peer-heading-${pr.assessorUserId}" data-bs-parent="#peer-reviews">
                    <div class="accordion-body">
                      ${this.hasAssociatedRubric === "true" ? html`
                        <sakai-rubric-student
                          site-id="${getSiteId()}"
                          tool-id="${this.toolId}"
                          entity-id="${this.entityId}"
                          instructor
                          is-peer-or-self
                          evaluated-item-id="${pr.assessorUserId}"
                          evaluated-item-owner-id="${this._submission.groupId || this._submission.firstSubmitterId}">
                        </sakai-rubric-student>
                        <hr class="itemSeparator">
                      ` : nothing}
                      <div class="mt-2 mb-3">
                        <span class="grader-title me-2">${this._i18n.grade}</span>
                        <span>${pr.scoreDisplay}</span>
                      </div>
                      <div class="mt-2 mb-2 grader-title">${this._i18n.reviewer_comments}</div>
                      <div>${unsafeHTML(pr.comment)}</div>
                      ${pr.attachmentUrlList?.length > 0 ? html`
                        <div class="grader-title mb-2">${this._i18n.reviewer_attachments}</div>
                        ${pr.attachmentUrlList.map((url, i) => html`
                          <div class="feedback-attachment">
                            <a href="${url}" title="${this._i18n.feedback_attachment_tooltip}" target="_blank">${this._i18n.attachment} ${i + 1}</a>
                          </div>
                        `)}
                      ` : nothing}
                    </div>
                  </div>
                </div>
              `)}
            </div>
          </div>
          ` : nothing }
        ` : nothing }
      </div>
    `;
  }

  _renderSaved() {

    return html`<span class="saved fa fa-check-circle ${this._saveSucceeded ? "d-inline" : "d-none"} text-success"
                  title="${this._i18n.saved_successfully}">
                </span>`;
  }

  _renderFailed() {

    return html`<span class="saved failed fa fa-times-circle ${this._saveFailed ? "d-inline" : "d-none"} text-danger"
                  title="${this._i18n.failed_save}">
                </span>`;
  }

  _renderGraderLinkBlock() {

    return html`
      <div id="grader-link-block" class="float-end d-lg-none">
        <button @click=${this._toggleGrader} class="btn btn-primary active">
          ${this._i18n.grade_submission}
        </button>
      </div>`;
  }

  _renderGradeInputs(label, submitter) {

    return html`
      <span>${label}</span>
      ${this.gradeScale === LETTER_GRADE_TYPE ? html`
        <select id="letter-grade-selector"
            aria-label="${this._i18n.lettergrade_selector_label}"
            class=${ifDefined(submitter ? "grader-grade-override" : undefined)}
            data-user-id="${ifDefined(submitter ? submitter.id : undefined)}"
            @change=${submitter ? undefined : this._gradeSelected}>
          <option value="">${this._i18n["non.submission.grade.select"]}</option>
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
        <input id=${ifDefined(submitter ? undefined : "score-grade-input")} aria-label="${this._i18n.number_grade_label}"
          @keydown=${this._validateGradeInput}
          @keyup=${submitter ? undefined : this._gradeSelected}
          data-user-id="${ifDefined(submitter ? submitter.id : undefined)}"
          type="text"
          class="points-input ${ifDefined(submitter ? "grader-grade-override" : "")}"
          .value=${submitter ? submitter.overridden ? submitter.grade : "" : this._submission.grade} />
        ${this._renderSaved()}
        ${this._renderFailed()}
        <span id="grader-max-point-label">(${this._i18n["grade.max"]} ${this.gradable.maxGradePoint})</span>
        ${this.gradable.allowPeerAssessment ? html`
          <button id="peer-info"
              class="btn transparent-button"
              type="button"
              aria-label="${this._i18n.peer_info_label}"
              data-bs-toggle="popover"
              data-bs-container="body"
              data-bs-placement="auto"
              data-bs-content="${this._i18n["peerassessment.peerGradeInfo"]}">
            <span class="fa fa-info-circle"></span>
          </button>
        ` : nothing }
      ` : nothing }
      ${this.gradeScale === PASS_FAIL_GRADE_TYPE ? html`
        <select id="pass-fail-selector"
                  aria-label="${this._i18n.passfail_selector_label}"
                  class=${ifDefined(submitter ? "grader-grade-override" : undefined)}
                  data-user-id="${ifDefined(submitter ? submitter.id : undefined)}"
                  @change=${submitter ? undefined : this._gradeSelected}
                  .value=${submitter ? submitter.grade : this._submission.grade}>
          <option value="ungraded"
              .selected=${submitter ? !submitter.overridden : this._submission.grade === this._i18n.ungra}>
            ${this._i18n.ungra}
          </option>
          <option value="pass"
              .selected=${submitter ? submitter.overridden && submitter.grade === this._i18n.pass : this._submission.grade.match(/^pass$/i)}>
            ${this._i18n.pass}
          </option>
          <option value="fail"
              .selected=${submitter ? submitter.overridden && submitter.grade === this._i18n.fail : this._submission.grade.match(/^fail$/i)}>
            ${this._i18n.fail}
          </option>
        </select>
        ${this._renderSaved()}
        ${this._renderFailed()}
      ` : nothing }
      ${this.gradeScale === CHECK_GRADE_TYPE ? html`
        <input id="check-grade-input"
                type="checkbox"
                data-user-id="${ifDefined(submitter ? submitter.id : undefined)}"
                aria-label="${this._i18n.checkgrade_label}"
                class=${ifDefined(submitter ? "grader-grade-override" : undefined)}
                @click=${submitter ? undefined : this._gradeSelected}
                value=${GRADE_CHECKED}
                .checked=${submitter ? submitter.overridden && submitter.grade === this._i18n["gen.checked"] : this._submission.grade === this._i18n["gen.checked"]}>
        </input>
        <span>${this._i18n["gen.gra2"]} ${this._i18n["gen.checked"]}</span>
        ${this._renderSaved()}
        ${this._renderFailed()}
      ` : nothing }
    `;
  }

  _renderGrader() {

    return html`
      ${this._submission.id !== "dummy" ? html`

      <div id="grader" class="d-none d-lg-block m-lg-0 p-3" tabindex="-1" aria-labelledby="grader-label">

        <div class="d-lg-none d-flex justify-content-between mb-3">
          <div class="d-flex align-self-center">
            <sakai-user-photo site-id="${getSiteId()}" user-id="${this._getPhotoUserId()}" classes="grader-photo" profile-popup="on"></sakai-user-photo>
            <h2 id="grader-label" class="ms-2">
              ${this._getSubmitter(this._submission)}
            </h2>
          </div>
          <button type="button" @click=${this._closeGrader} class="btn-close text-reset" aria-label="Close"></button>
        </div>

        <div>

          <!-- START ORIGINALITY BLOCK -->
          ${this._submission.originalityShowing ? html`
            <div class="grader-block">
              <label class="grader-label grader-originality-label">
                <span>${this._submission.originalityServiceName} ${this._i18n["review.report"]}</span>
              </label>
              ${this._submission.originalitySupplies.map(result => html`
                <div class="grader-originality-section" >
                  ${result[Submission.originalityConstants.originalityLink] !== "Error" ? html`
                    <a target="_blank"
                        href="${result[Submission.originalityConstants.originalityLink]}"
                        class="grader-originality-link">
                      <i class="${result[Submission.originalityConstants.originalityIcon]}"></i>
                      <span>${result[Submission.originalityConstants.originalityScore]}${this._i18n["content_review.score_display.grader"]}</span>
                    </a>
                    <span>
                      <span class="grader-originality-delimiter">${this._i18n["content_review.delimiter"]}</span>
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
                        <span>${this._i18n["content_review.disclosure.pending"]}</span>
                      </a>
                      <span>
                        <span class="grader-originality-delimiter">${this._i18n["content_review.delimiter"]}</span>
                        <span>${result[Submission.originalityConstants.originalityName]}</span>
                      </span>
                      <div class="collapse grader-originality-caption" id="${result[Submission.originalityConstants.originalityKey]}">
                        <span>${this._i18n["content_review.notYetSubmitted.grader"]} ${this._submission.originalityServiceName}</span>
                      </div>
                    ` : html`
                      <a href="#${result[Submission.originalityConstants.originalityKey]}"
                          data-bs-toggle="collapse"
                          role="button"
                          aria-expanded="false"
                          aria-controls="${result[Submission.originalityConstants.originalityKey]}"
                          class="grader-originality-link">
                        <i class="${result[Submission.originalityConstants.originalityIcon]}"></i><span>${this._i18n["content_review.disclosure.error"]}</span>
                      </a>
                      <span>
                        <span class="grader-originality-delimiter">${this._i18n["content_review.delimiter"]}</span>
                        <span>${result[Submission.originalityConstants.originalityName]}</span>
                      </span>
                      <div class="collapse grader-originality-caption" id="${result[Submission.originalityConstants.originalityKey]}">
                        <div>${result[Submission.originalityConstants.originalityError]}</div>
                      </div>
                    `}
                  `}
                </div>
              `)}
            </div>
          ` : nothing }
          <!-- END ORIGINALITY BLOCK -->

          <div id="grader-grade-block" class="grader-block">
            ${this._renderGradeInputs(this._i18n["gen.assign.gra"])}
            <!-- start hasAssociatedRubric -->
            ${this.hasAssociatedRubric === "true" ? html`
              ${!this._rubricShowing && !this._rubricStudentShowing ? html`
                <div class="d-flex align-items-center mt-3">
                  <div>
                    <button id="grader-rubric-button"
                        class="btn btn-link"
                        @click=${this._toggleRubric}
                        aria-label="${this._i18n.grading_rubric}"
                        title="${this._i18n.grading_rubric}"
                        aria-controls="grader-rubric-block grader-controls-block">
                      ${this._i18n.rubric}
                    </button>
                  </div>
                  <div>
                    <sakai-rubric-grading-button
                        id="grader-rubric-link"
                        aria-label="${this._i18n.grading_rubric}"
                        title="${this._i18n.grading_rubric}"
                        @click=${this._toggleRubric}
                        site-id="${getSiteId()}"
                        tool-id="${this.toolId}"
                        entity-id="${this.entityId}"
                        evaluated-item-id="${this._submission.id}"
                        aria-controls="grader-rubric-block grader-controls-block"
                        only-show-if-evaluated>
                    </sakai-rubric-grading-button>
                    <sakai-rubric-evaluation-remover
                        class="ms-2"
                        site-id="${getSiteId()}"
                        tool-id="${this.toolId}"
                        entity-id="${this.entityId}"
                        evaluated-item-id="${this._submission.id}"
                        @evaluation-removed=${this._onEvaluationRemoved}
                        only-show-if-evaluated>
                    </sakai-rubric-evaluation-remover>
                  </div>
                </div>
              ` : nothing}

              <div id="grader-rubric-block" class="ms-2 ${this._rubricShowing ? "d-block" : "d-none"}">
                <sakai-rubric-grading
                  site-id="${getSiteId()}"
                  tool-id="${this.toolId}"
                  entity-id="${this.entityId}"
                  evaluated-item-id="${this._submission.id}"
                  evaluated-item-owner-id="${this._submission.groupId || this._submission.firstSubmitterId}"
                  ?group=${this._submission.groupId}
                  ?enable-pdf-export=${this.enablePdfExport}
                  @rubric-rating-changed=${this._onRubricRatingChanged}
                  @rubric-ratings-changed=${this._onRubricRatingsChanged}
                  @rubric-rating-tuned=${this._onRubricRatingTuned}
                  @total-points-updated=${this._onRubricTotalPointsUpdated}
                  @update-comment=${this._onUpdateCriterionComment}>
                </sakai-rubric-grading>
                <button class="btn btn-primary"
                    title="${this._i18n.rubric_done_tooltip}"
                    aria-label="${this._i18n.rubric_done_tooltip}"
                    @click=${this._closeRubric}>
                  ${this._i18n["gen.don"]}
                </button>
              </div>
            ` : nothing }
            ${this.rubricSelfReport ? html`
              ${!this._rubricShowing && !this._rubricStudentShowing ? html`
                <div class="d-flex align-items-center mt-3">
                  <div>
                  <button id="student-rubric-button"
                      class="btn btn-link"
                      @click=${this._toggleStudentRubric}
                      aria-label="${this._i18n.studentrubric}"
                      title="${this._i18n.studentrubric}"
                      aria-controls="student-rubric-block grader-controls-block">
                    ${this._i18n.openAutoevaluation}
                  </button>
                  </div>
                </div>
              ` : nothing}
              <div id="student-rubric-block" class="ms-2 ${this._rubricStudentShowing ? "d-block" : "d-none"}">
                <h1>${this._i18n.autoevaluation}</h1>
                <p>${this._i18n.studentrubric}</p>
                <sakai-rubric-student
                  site-id="${getSiteId()}"
                  tool-id="${this.toolId}"
                  entity-id="${this.entityId}"
                  instructor
                  is-peer-or-self
                  evaluated-item-id="${this._submission.groupId || this._submission.firstSubmitterId}"
                  evaluated-item-owner-id="${this._submission.groupId || this._submission.firstSubmitterId}">
                </sakai-rubric-student>
                <button class="btn btn-primary"
                    title="${this._i18n.rubric_done_tooltip}"
                    aria-label="${this._i18n.rubric_done_tooltip}"
                    @click=${this._closeStudentRubric}>
                  ${this._i18n["gen.don"]}
                </button>
              </div>
            ` : nothing }
            <!-- end hasAssociatedRubric -->

            ${this._submission.groupId && !this.gradable.anonymousGrading ? html`
              <div id="grader-overrides-wrapper">
                <label>
                  <input type="checkbox" id="grader-override-toggle" ?checked=${this._showOverrides} @click=${e => this._showOverrides = e.target.checked} />
                  <span class="grader-overrides-label">${this._i18n.assign_grade_overrides}</span>
                </label>
                <div id="grader-overrides-block" class="d-${this._showOverrides ? "block" : "none"}">
                ${this._submission.submitters.map(s => html`
                  <div class="grader-override">
                    <div class="grader-overrides-display-name">${s.displayName} (${s.displayId})</div>
                    <div>${this._renderGradeInputs(this._i18n.override_grade_with, s)}</div>
                  </div>
                `)}
                </div>
              </div>
            ` : nothing }
          </div>

          <div id="grader-controls-block" class="${this._rubricShowing || this._rubricStudentShowing ? "d-none" : "d-block"}">
            <div class="grader-block">
              <div class="feedback-label grader-label content-button-block">
                <button id="grader-feedback-button"
                    class="btn btn-link"
                    aria-controls="feedback-block"
                    @click=${this._toggleFeedbackCommentEditor}
                    aria-expanded="${this._feedbackCommentEditorShowing ? "true" : "false"}"
                    aria-label="${this._i18n.add_feedback_tooltip}"
                    title="${this._i18n.add_feedback_tooltip}">
                  ${this._submission.feedbackComment ? this._i18n.edit_feedback_comment : this._i18n.add_feedback_comment}
                </button>
              </div>
              <div class="sak-banner-warn ms-2 ${this._feedbackCommentRemoved ? "d-block" : "d-none"}">${this._i18n.removed}</div>

              ${this._submission.feedbackComment ? html`
                <div id="feedback-snippet"
                    class="card ms-3 mt-2 ${this._feedbackCommentEditorShowing ? "d-none" : "d-block"}">
                  <div class="card-body">
                    ${unsafeHTML(this._submission.feedbackComment)}
                    <div class="fade-overlay ${!this._showingFullFeedbackComment ? "d-block" : "d-none"}">
                    </div>
                  </div>
                  <div class="card-footer bg-transparent">
                    <button class="btn btn-link p-0"
                        @click=${this._toggleFullFeedbackComment}>
                      ${this._showingFullFeedbackComment ? this._i18n.show_less : this._i18n.show_all}
                    </button>
                  </div>
                </div>
                <div class="mt-2 ms-3 ${!this._feedbackCommentEditorShowing ? "d-block" : "d-none"}">
                  <button class="btn btn-transparent text-decoration-underline"
                      @click=${this._removeFeedbackComment}>
                    ${this._i18n["gen.remove"]}
                  </button>
                </div>
              ` : nothing }

              <div id="feedback-block" class="ms-2 ${this._feedbackCommentEditorShowing ? "d-block" : "d-none"}">
                <div id="feedback-comment-unsaved-msg" class="feedback-instruction sak-banner-error d-none">${this._i18n.unsaved_text_warning}</div>
                <textarea id="grader-feedback-comment" aria-label="${this._i18n.feedback_comment_label}" .value=${this._submission.feedbackComment}></textarea>
                <div id="grader-media-feedback" class="grader-label">
                  <span class="feedback-label">${this._i18n.recorded_feedback_label}</span>
                  <sakai-icon type="microphone"></sakai-icon>
                  <sakai-icon type="video"></sakai-icon>
                </div>
                <button id="grader-feedback-comment-save" class="btn btn-primary mt-2" @click=${this._saveFeedbackComment}>
                  ${this._i18n["gen.don"]}
                </button>
              </div>
            </div>

            <div id="grader-feedback-attachments-block" class="grader-block">
              ${this._submission.feedbackAttachments ? html`
                <div class="feedback-attachments-title">${this._i18n["download.feedback.attachment"]}</div>
                <div class="current-feedback-attachments">
                  ${this._submission.feedbackAttachments.map(att => html`
                    <div class="feedback-attachments-row">
                      <div class="feedback-attachment">
                        <a href="${att.url}" title="${this._i18n.feedback_attachment_tooltip}" target="_blank">
                          <span>${att.name}</span>
                        </a>
                      </div>
                      <div class="feedback-attachment-remove">
                        <button class="btn btn-transparent text-decoration-underline"
                            data-ref="${att.ref}"
                            @click=${this._removeAttachment}>
                          ${this._i18n["gen.remove"]}
                        </button>
                      </div>
                    </div>
                  `)}
                </div>
              ` : nothing }
              <sakai-grader-file-picker button-text="${this._i18n["gen.addatt"]}"
                  class="mt-2"
                  style="display: inline-block;"
                  title="${this._i18n.add_attachments_tooltip}">
              </sakai-grader-file-picker>
            </div>
            ${this._submission.hasHistory ? html`
              <div id="grader-submission-history-wrapper">
                <div id="grader-submission-history-toggle">
                  <a href="javascript:;"
                    @click=${() => this._showingHistory = !this._showingHistory}
                    aria-label="${this._showingHistory ? this._i18n.hide_history_tooltip : this._i18n.show_history_tooltip}"
                    title="${this._showingHistory ? this._i18n.hide_history_tooltip : this._i18n.show_history_tooltip}">
                    ${this._showingHistory ? this._i18n.hide_history : this._i18n.show_history}
                  </a>
                </div>
                <div id="grader-submission-history" class="d-${this._showingHistory ? "block" : "none"}">
                  ${this._submission.history.comments ? html`
                    <div id="grader-history-comments-wrapper">
                      <div class="grader-history-title">${this._i18n.previous_submissions}</div>
                      <div class="grader-history-block">${unsafeHTML(this._submission.history.comments)}</div>
                    </div>
                  ` : nothing }
                  ${this._submission.history.grades ? html`
                    <div id="grader-history-grades-wrapper">
                      <div class="grader-history-title">${this._i18n.previous_grades}</div>
                      <div class="grader-history-block">${unsafeHTML(this._submission.history.grades)}</div>
                    </div>
                  ` : nothing }
                  ${this._submission.history.feedbackComment ? html`
                    <div id="grader-history-feedback-wrapper">
                      <div class="grader-history-title">${this._i18n.instructor_feedback}</div>
                      <div class="grader-history-block">${unsafeHTML(this._submission.history.feedbackComment)}</div>
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
                    aria-label="${this._i18n.private_notes_tooltip}"
                    title="${this._i18n.private_notes_tooltip}">
                  ${this._submission.privateNotes ? this._i18n.edit_private_notes : this._i18n.add_private_notes}
                </button>
              </div>
              <div class="sak-banner-warn ${this._privateNotesRemoved ? "d-block" : "d-none"}">${this._i18n.removed}</div>

              ${this._submission.privateNotes ? html`
                <div id="private-notes-snippet" class="card ms-3 mt-2 ${this._privateNotesEditorShowing ? "d-none" : "d-block"}">
                  <div class="card-body">
                    ${unsafeHTML(this._submission.privateNotes)}
                    <div class="fade-overlay ${!this._showingFullPrivateNotes ? "d-block" : "d-none"}">
                    </div>
                  </div>
                  <div class="card-footer bg-transparent">
                    <button class="btn btn-link p-0"
                        @click=${this._toggleFullPrivateNotes}>
                      ${this._showingFullPrivateNotes ? this._i18n.show_less : this._i18n.show_all}
                    </button>
                  </div>
                </div>
                <div class="mt-2 ms-3 ${this._privateNotesEditorShowing ? "d-none" : "d-block"}">
                  <button class="btn btn-transparent text-decoration-underline"
                      @click=${this._removePrivateNotes}>
                    ${this._i18n["gen.remove"]}
                  </button>
                </div>
              ` : nothing }

              <div id="private-notes-block" class="ms-2 ${this._privateNotesEditorShowing ? "d-block" : "d-none"}">
                <div class="sak-banner-info">${unsafeHTML(this._i18n.private_notes_tooltip)}</div>
                <div id="private-notes-unsaved-msg" class="sak-banner-error d-none">${this._i18n.unsaved_text_warning}</div>
                <textarea id="grader-private-notes" aria-label="${this._i18n.private_notes_label}" .value=${this._submission.privateNotes}></textarea>
                <button id="grader-private-notes-save" class="btn btn-primary mt-2"
                    @click=${this._savePrivateNotes}>
                  ${this._i18n["gen.don"]}
                </button>
              </div>
            </div>

            <div class="text-feedback">
            </div>
            ${this._submission.hasSubmittedDate ? html`
              <div class="resubmission-checkbox">
                <label>
                  <input type="checkbox" .checked=${this._showResubmission} @change="${this._toggleResubmissionBlock}"/>
                  <span>${this._i18n.allowResubmit}</span>
                </label>
              </div>
              ${this._showResubmission ? html`
                <div class="resubmission-block">
                  <span>${this._i18n["allow.resubmit.number"]}:</span>
                  <select aria-label="${this._i18n.attempt_selector_label}"
                      @change=${e => this._submission.resubmitsAllowed = parseInt(e.target.value)}>
                    ${Array(10).fill().map((_, i) => html`
                      <option value="${i + 1}" .selected=${this._submission.resubmitsAllowed === i + 1}>${i + 1}</option>
                    `)}
                    <option value="-1" .selected=${this._submission.resubmitsAllowed === -1}>${this._i18n.unlimited}</option>
                  </select>
                  <div>${this._i18n["allow.resubmit.closeTime"]}:</div>
                  <sakai-date-picker
                      epoch-millis="${this._submission.resubmitDate}"
                      @datetime-selected=${this._resubmitDateSelected}
                      label="${this._i18n["allow.resubmit.closeTime"]}">
                  </sakai-date-picker>
                </div>
              ` : nothing }
            ` : nothing }
            ${!this._submission.hasSubmittedDate ? html`
              <div id="grader-extension-section" class="mt-2">
                <input type="checkbox" .checked=${this._allowExtension} id="allowExtensionToggle" name="allowExtensionToggle" @change=${this._toggleExtensionBlock} />
                <label for="allowExtensionToggle" >${this._i18n.allowExtension}</label>
                ${this._allowExtension ? html`
                  <div class="ms-2 mt-2">
                    <div>${this._i18n.allowExtensionCaptionGrader}</div>
                    <div id="allowExtensionTime">
                      <label>${this._i18n["gen.acesubunt"]}</label>
                      <sakai-date-picker
                          epoch-millis="${this._submission.extensionDate}"
                          @datetime-selected="${this._extensionDateSelected}"
                          label="${this._i18n["gen.acesubunt"]}">
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
                ${this._i18n["gen.sav"]}
              </button>
              <button accesskey="d"
                  class="btn btn-link"
                  name="return"
                  data-release="true"
                  @click=${this._save}>
                ${this._i18n["gen.retustud"]}
              </button>
              <button class="btn btn-link d-lg-none" accesskey="x" name="cancel" @click=${this._cancel}>${this._i18n["gen.can"]}</button>
            </div>
            ${this._saving ? html`<div class="sak-banner-info">${this._i18n.saving}</div>` : ""}
            ${this._saveSucceeded && this._gradeOrCommentsModified ? html`<div class="sak-banner-success">${this._i18n.successful_save}</div>` : nothing }
            ${this._saveFailed ? html`<div class="sak-banner-error">${this._i18n.failed_save}</div>` : nothing }
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
          <div class="mb-3 fs-5 grader-title">${this._i18n.loading_1}</div>
          <div>${this._i18n.loading_2}</div>
        </div>
      `;
    }

    return html`
      ${this._areSettingsInAction() ? html`
      <div id="grader-filter-warning" class="sak-banner-warn">${this._i18n.filter_settings_warning}</div>
      ` : nothing }
      ${this._renderTopbar()}
      <div class="d-flex flex-column flex-lg-row">
        <div id="grader-gradable-container" class="flex-grow-1">
          <div id="grader-submitted-block" class="grader-block">
            <div class="d-flex align-items-center mb-3">
              <sakai-user-photo site-id="${getSiteId()}" user-id="${this._getPhotoUserId()}" classes="grader-photo" profile-popup="on"></sakai-user-photo>
              <div class="ms-2">
                <span class="submitter-name">
                  ${this._getSubmitter(this._submission)}
                </span>
                ${this._submission.draft ? html`
                <span class="draft-submission">${this._i18n.draft_submission}</span>
                ` : html`
                  ${this._submission.hasSubmittedDate ? html`
                  <div id="grader-submitted-label">${this._i18n.submitted}</div>
                  ` : nothing }
                `}
              </div>
            </div>
            <div class="d-flex align-items-center">
              <div class="submitted-time ${this._submission.draft ? "draft-time" : ""}">${this._submission.submittedTime}</div>
              ${this._submission.late ? html`<div class="grader-late ms-2">${this._i18n["grades.lateness.late"]}</div>` : ""}
              ${this._submission.returned ? html`
                <div class="ms-2"><span class="grader-returned fa fa-eye" title="${this._i18n.returned_tooltip}"></span></div>
              ` : nothing }
            </div>
            ${this._submission.groupId && this._submission.hasSubmittedDate ? html`<div class="grader-group-members">${this._submission.groupMembers}</div>` : nothing }
            <div class="attachments">
              ${this._submission.submittedText
                  && this._submission.visible
                  && this.gradable.submissionType === TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION
                  && this._submission.hasNonInlineAttachments ? html`
              <div>
                <button type="button"
                    class="btn btn-transparent text-decoration-underline"
                    @click=${() => this._submittedTextMode = true}>
                  ${this._i18n.submission_inline}
                </button>
              </div>
              ` : nothing}
              ${this._submission.submissionLog.length > 0 ? html`
              <button type="button"
                  class="btn btn-link mb-2"
                  data-bs-toggle="collapse"
                  data-bs-target="#grader-submission-history"
                  aria-controls="grader-submission_history"
                  aria-expanded="false">
                ${this._i18n.submission_history}
              </button>
              <div class="collapse mb-2" id="grader-submission-history">
                <div class="card card-body">
                ${this._submission.submissionLog.map(message => html`
                  <div>${message}</div>
                `)}
                </div>
              </div>
              ` : nothing}
              ${this._submission.submittedAttachments.filter(r => !r.ref.includes("InlineSub")).map(r => html`
                <div>
                  <button type="button"
                      class="btn btn-transparent text-decoration-underline"
                      data-ref="${r.ref}"
                      @click=${this._previewAttachment}>
                    <i class="${r.iconClass} me-2"></i>
                    ${r.name} (${r.contentLength} ${this._i18n.at} ${r.creationDate})
                  </button>
                </div>
              `)}
            </div>
            <div>
            ${this._submission.submitters?.length > 0 && this._submission.submitters[0].timeSpent ? html`
              <span>${this._i18n["gen.assign.spent"]}</span>
              <span> ${this._submission.submitters[0].timeSpent}</span>
            ` : nothing}
            </div>
          </div> <!-- /grader-submitted-block -->

          <div id="grader-gradable-content">
            ${this._renderGradable()}
          </div>
        </div>
        ${this._renderGrader()}
      </div>
    `;
  }
};
