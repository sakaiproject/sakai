import { RubricsElement } from "./RubricsElement.js";
import { html, nothing } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import "../sakai-rubric-criteria.js";
import "../sakai-rubric-criteria-readonly.js";
import "../sakai-rubric-edit.js";
import "../sakai-item-delete.js";
import "../sakai-rubric-pdf.js";
import { SharingChangeEvent } from "./SharingChangeEvent.js";

export class SakaiRubric extends RubricsElement {

  static properties = {

    rubric: { type: Object },
    siteId: { attribute: "site-id", type: String },
    enablePdfExport: { attribute: "enable-pdf-export", type: Boolean },

    _shareIcon: { state: true },
    _weightedIcon: { state: true },
    _totalWeight: { state: true },
    _validWeight: { state: true },
    _maxPoints: { state: true },
    _minPoints: { state: true },
    _renderCriteria: { state: true },
  };

  constructor() {

    super();

    this.updateRubricOptions = {
      method: "PATCH",
      credentials: "include",
      headers: { "Content-Type": "application/json-patch+json" },
    };
  }

  set rubric(value) {

    const old = this._rubric;
    this._rubric = value;
    !this._rubric.criteria && (this._rubric.criteria = []);
    this.handleRefreshTotalWeight();
    this.handleDraftBtn();
    this.handleWeightBtn();
    this.handleShareBtn();
    this.requestUpdate("rubric", old);

    // If this is a newly created rubric, show the edit modal when the update's complete
    this.updateComplete.then(() => {

      if (value.new) {
        bootstrap.Collapse.getOrCreateInstance(this.querySelector(`#rubric-collapse-${value.id}`)).show();
      }
    });
  }

  get rubric() { return this._rubric; }

  matches(search) {

    const rubricTitle = this.querySelector(".rubric-name").textContent;
    const rubricAuthor = this.querySelector(".rubric-creator-name").textContent;
    const rubricSite = this.querySelector(".rubric-site-title").textContent;

    return rubricAuthor.toLowerCase().includes(search) ||
            rubricTitle.toLowerCase().includes(search) ||
            rubricSite.toLowerCase().includes(search);
  }

  shouldUpdate() { return super.shouldUpdate() && this.rubric; }

  firstUpdated() {

    const criteriaBlock = this.querySelector(".collapse");

    criteriaBlock.addEventListener("show.bs.collapse", e => {

      this._renderCriteria = true;

      e.stopPropagation();
      this.querySelector(".rubric-toggle span.fa").classList.replace("fa-chevron-right", "fa-chevron-down");
    });

    criteriaBlock.addEventListener("hide.bs.collapse", e => {

      this._renderCriteria = false;

      e.stopPropagation();
      this.querySelector(".rubric-toggle span.fa").classList.replace("fa-chevron-down", "fa-chevron-right");
    });
  }

  render() {

    return html`
      <div class="rubric-title">
        <div>
          <button type="button"
              class="btn btn-icon btn-sm rubric-toggle"
              data-bs-toggle="collapse"
              data-bs-target="#rubric-collapse-${this.rubric.id}"
              aria-controls="rubric-collapse-${this.rubric.id}"
              aria-expanded="false"
              title="${this.tr("toggle_details")} ${this.rubric.title}">
            <span class="fa fa-chevron-right"></span>
          </button>
          <span class="rubric-name">${this.rubric.title}</span>
          ${this.rubric.locked ? html`
            ${this.rubric.weighted ? html`
              <span
                tabindex="0"
                role="tooltip"
                title="${this.tr("weighted_status")}"
                aria-label="${this.tr("weighted_status")}"
                class="fa fa-percent icon-spacer">
              </span>
            ` : nothing }
            <span
              tabindex="0"
              role="tooltip"
              title="${this.rubric.title} ${this.tr("is_locked")}"
              aria-label="${this.rubric.title} ${this.tr("is_locked")}"
              class="locked fa fa-lock icon-spacer">
            </span>`
          : nothing}
          <sakai-rubric-edit
            id="rubric-edit-${this.rubric.id}"
            @update-rubric-title=${this.updateRubricTitle}
            @click=${el => el.stopPropagation()}
            .rubric=${this.rubric}
            class="icon-spacer">
          </sakai-rubric-edit>
          ${this.rubric.draft ? html`
            <span
              tabindex="0"
              role="tooltip"
              title="${this.tr("draft_info")}"
              aria-label="${this.tr("draft_info")}"
              class="highlight bold icon-spacer"
            >
              ${this.tr("draft_label")}
            </span>`
          : nothing}
        </div>

        <div class="d-none d-sm-block rubric-site-title">${this.rubric.siteTitle}</div>
        <div class="d-none d-sm-block rubric-creator-name">${this.rubric.creatorDisplayName}</div>
        <div class="d-none d-sm-block">${this.rubric.formattedModifiedDate}</div>

        <div class="actions">
          ${!this.rubric.locked ? html`
            <div class="action-container">
              <button type="button"
                  class="btn btn-sm ${(this.rubric.draft && this.rubric.weighted && !this._validWeight) ? "disabled" : ""} draft"
                  title="${this.draftLabel}"
                  aria-label="${this.draftLabel}"
                  @keyup=${this.openEditWithKeyboard}
                  @click=${this.draftChange}>
                <span class="fa ${this.draftIcon}" aria-hidden="true"></span>
              </button>
            </div>
            <div class="action-container">
              <button type="button"
                  class="btn btn-sm weighted"
                  title="${this.weightLabel}"
                  aria-label="${this.weightLabel}"
                  @keyup=${this.openEditWithKeyboard}
                  @click=${this.weightedChange}>
                <span class="fa ${this._weightedIcon}" aria-hidden="true"></span>
              </button>
            </div>`
          : nothing}
          <div class="action-container">
            <button type="button"
                class="btn btn-sm share"
                title="${this.tr(this.shareTitleKey, [ this.rubric.title ])}"
                aria-label="${this.tr(this.shareTitleKey, [ this.rubric.title ])}"
                @keyup=${this.openEditWithKeyboard}
                @click=${this.sharingChange}>
              <span class="fa ${this._shareIcon}" aria-hidden="true"></span>
            </button>
          </div>
          <div class="action-container">
            <button type="button"
                class="btn btn-sm clone"
                title="${this.tr("copy")} ${this.rubric.title}"
                aria-label="${this.tr("copy")} ${this.rubric.title}"
                @keyup=${this.openEditWithKeyboard}
                @click=${this.cloneRubric}>
              <span class="fa fa-copy" aria-hidden="true"></span>
            </button>
          </div>
          ${!this.rubric.locked ? html`
            <div class="action-container">
              <sakai-item-delete
                .rubric=${this.rubric}
                site-id="${this.siteId}"
                class="sakai-rubric">
              </sakai-item-delete>
            </div>`
          : nothing}
          ${this.enablePdfExport ? html`
            <div class="action-container">
              <sakai-rubric-pdf
                site-id="${this.siteId}"
                rubric-title="${this.rubric.title}"
                rubric-id="${this.rubric.id}">
              </sakai-rubric-pdf>
            </div>`
          : nothing}
        </div>
      </div>

      <div class="collapse" id="rubric-collapse-${this.rubric.id}">
        ${this._renderCriteria ? html`
        <div class="rubric-details style-scope sakai-rubric" rubric-id="${this.rubric.id}">
          <div class="sak-banner-success d-none" aria-live="polite">${this.tr("saved_successfully")}</div>
          <div class="sak-banner-error d-none" aria-live="polite">${this.tr("save_failed")}</div>
          <sakai-rubric-criteria
            rubric-id="${this.rubric.id}"
            site-id="${this.rubric.ownerId}"
            .criteria="${this.rubric.criteria}"
            @weight-changed=${this.handleCriterionWeightChange}
            @refresh-total-weight=${this.handleRefreshTotalWeight}
            .weighted=${this.rubric.weighted}
            total-weight="${this._totalWeight}"
            ?valid-weight="${this._validWeight}"
            max-points="${ifDefined(this._maxPoints)}"
            min-points="${ifDefined(this._minPoints)}"
            ?is-locked="${this.rubric.locked}"
            ?is-draft="${this.rubric.draft}">
          </sakai-rubric-criteria>
        </div>
        ` : nothing}
      </div>
    `;
  }

  cloneRubric(e) {

    e.stopPropagation();
    this.dispatchEvent(new CustomEvent("clone-rubric", { detail: this.rubric, bubbles: true, composed: true }));
  }

  updateRubricTitle(e) {

    this.updateRubricOptions.body = JSON.stringify([ { "op": "replace", "path": "/title", "value": e.detail } ]);
    const url = `/api/sites/${this.rubric.ownerId}/rubrics/${this.rubric.id}`;
    fetch(url, this.updateRubricOptions)
    .then(r => {

      if (r.ok) {
        this.rubric.title = e.detail;
        this.rubric.new = false;
        this.requestUpdate();
        this.updateOtherItems();
        this.dispatchEvent(new SharingChangeEvent());
      } else {
        throw new Error("Network error while updating rubric title");
      }
    })
    .catch (error => console.error(error));
  }

  saveCriterionWeights() {

    const all = Promise.all(this.rubric.criteria.map(cr => {

      this.updateRubricOptions.body = JSON.stringify([ { "op": "replace", "path": "/weight", "value": cr.weight } ]);

      const url = `/api/sites/${this.rubric.ownerId}/rubrics/${this.rubric.id}/criteria/${cr.id}`;

      return fetch(url, this.updateRubricOptions)
        .then(r => {

          if (!r.ok) {
            throw new Error(`Network error while setting criterion weight for ${cr.id}`);
          }
        });
    }));

    all.then(() => {

      this.dispatchEvent(new SharingChangeEvent());

      const successBanner = this.querySelector(".sak-banner-success");
      successBanner.classList.remove("d-none");

      setTimeout(() => successBanner.classList.add("d-none"), 5000);
    }).catch(() => {

      const errorBanner = this.querySelector(".sak-banner-error");
      errorBanner.classList.remove("d-none");

      setTimeout(() => errorBanner.classList.add("d-none"), 5000);
    });

    return all;
  }

  handleCriterionWeightChange(e) {

    const payload = e.detail;

    this.rubric.criteria = payload.criteria;
    const criterionModified = this.rubric.criteria.find(el => el.id === payload.criterionId);
    const oldValue = criterionModified.weight;

    criterionModified.weight = payload.value;
    if (oldValue === payload.value) {
      return;
    }

    const total = this.rubric.criteria.reduce((acc, el) => acc + el.weight, 0);

    this._validWeight = total == 100;
    this._totalWeight = total.toLocaleString(this.locale);
    this._maxPoints = this.getMaxPoints(this.rubric.criteria);
    this._minPoints = this.getMinPoints(this.rubric.criteria);
    this.requestUpdate();
    this.handleDraftBtn();
  }

  handleRefreshTotalWeight(e) {

    if (e && e.detail.criteria) {
      this.rubric.criteria = e.detail.criteria;
    }
    this._totalWeight = 0;
    this.rubric.criteria.forEach(cr => {
      this._totalWeight = this._totalWeight + cr.weight;
    });
    this._validWeight = this._totalWeight == 100;
    this._maxPoints = this.getMaxPoints(this.rubric.criteria);
    this._minPoints = this.getMinPoints(this.rubric.criteria);
  }

  draftChange() {

    // Draft mode can't be turned off if the total weight doesn't match 100%,
    // this way (+css) also prevents the rubric to be toggled when pressing the disabled button.
    if (this.rubric.draft && this.rubric.weighted && !this._validWeight) {
      return;
    }
    this.rubric.draft = !this.rubric.draft;

    this.updateRubricOptions.body = JSON.stringify([ { "op": "replace", "path": "/draft", "value": this.rubric.draft } ]);
    const url = `/api/sites/${this.rubric.ownerId}/rubrics/${this.rubric.id}`;
    fetch(url, this.updateRubricOptions)
    .then(r => {

      if (r.ok) {
        this.saveCriterionWeights();
        this.handleDraftBtn();
        this.requestUpdate();
      }
    });
  }

  weightedChange(e) {

    this.rubric.weighted = !this.rubric.weighted;
    if (this.rubric.weighted) {
      this.rubric.criteria.forEach(cr => cr.weight = 0);
      //Try to get first criterion, that is not a criterion group
      const firstCriterion = this.rubric.criteria.find(criteria => criteria.ratings?.length > 0);
      if (firstCriterion) {
        //Set weight of first criterion to 100 (%)
        firstCriterion.weight = 100;
        this.saveCriterionWeights(e);
      }
      this.handleRefreshTotalWeight();
    }

    this.updateRubricOptions.body = JSON.stringify([ { "op": "replace", "path": "/weighted", "value": this.rubric.weighted } ]);
    const url = `/api/sites/${this.rubric.ownerId}/rubrics/${this.rubric.id}`;
    fetch(url, this.updateRubricOptions)
    .then(r => {

      if (r.ok) {
        this.handleWeightBtn();
        this.requestUpdate();
      }
    });
  }

  sharingChange() {


    this.rubric.shared = !this.rubric.shared;

    this.updateRubricOptions.body = JSON.stringify([ { "op": "replace", "path": "/shared", "value": this.rubric.shared } ]);
    const url = `/api/sites/${this.rubric.ownerId}/rubrics/${this.rubric.id}`;
    fetch(url, this.updateRubricOptions)
    .then(r => {

      if (r.ok) {
        this.dispatchEvent(new SharingChangeEvent());
        this.handleShareBtn();
      } else {
        throw new Error("Network error while updating rubric");
      }
    })
    .catch (error => console.error(error));
  }

  handleDraftBtn() {

    if (this.rubric.draft) {
      this.draftIcon = "fa-eye-slash highlight";
      if (this.rubric.weighted) {
        if (!this._validWeight) {
          this.draftLabel = this.tr("draft_invalid_weight_publish") + this.tr("total_weight_wrong");
        } else {
          this.draftLabel = this.tr("draft_turn_off") + this.tr("draft_save_weights");
        }
      } else {
        this.draftLabel = this.tr("draft_turn_off");
      }
    } else {
      this.draftIcon = "fa-eye";
      if (this.rubric.weighted) {
        this.draftLabel = this.tr("draft_turn_on") + this.tr("draft_save_weights");
      } else {
        this.draftLabel = this.tr("draft_turn_on");
      }
    }
  }

  handleWeightBtn() {

    if (this.rubric.weighted) {
      this._weightedIcon = "fa-percent";
      this.weightLabel = this.tr("weighted_label");
    } else {
      this._weightedIcon = "fa-hashtag";
      this.weightLabel = this.tr("standard_label");
    }

    this.dispatchEvent(new SharingChangeEvent());
  }

  handleShareBtn() {

    if (this.rubric.shared) {
      this.shareTitleKey = "revoke";
      this._shareIcon = "fa-users";
    } else {
      this.shareTitleKey = "share";
      this._shareIcon = "fa-user";
    }
    this.shareValues = this.rubric.title;
  }

  updateOtherItems() {

    const sakaiItemDelete = this.querySelector("sakai-item-delete");
    if (sakaiItemDelete) {
      sakaiItemDelete.requestUpdate();
    }

    const sakaiRubricEdit = this.querySelector("sakai-rubric-edit");
    sakaiRubricEdit && sakaiRubricEdit.requestUpdate();

    const sakaiRubricPdf = this.querySelector("sakai-rubric-pdf");
    sakaiRubricPdf && sakaiRubricPdf.requestUpdate();
  }

  openEditWithKeyboard(e) {

    const spaceBarKeyCode = (e.keyCode == 32);

    if (spaceBarKeyCode && e.target.classList.contains("weighted")) {
      this.weightedChange(e);
    }

    if (spaceBarKeyCode && e.target.classList.contains("share")) {
      this.sharingChange(e);
    }

    if (spaceBarKeyCode && e.target.classList.contains("clone")) {
      this.cloneRubric(e);
    }

    if (spaceBarKeyCode && e.target.classList.contains("draft")) {
      this.draftChange(e);
    }
  }

  // SAK-47640 - Get the maximum and minimum possible grade of the whole rubric,
  // multiplying the max-min rating points of each criterion by the criterion weight, and adding them up
  getMaxPoints(criterions) {
    return this.getPoints(criterions, Math.max);
  }

  getMinPoints(criterions) {
    return this.getPoints(criterions, Math.min);
  }

  getPoints(criterions, minOrMax) {

    let totalPoints = 0;

    criterions
    .filter(criterion => criterion.ratings.length > 0)
    .forEach(criterion => {
      totalPoints += minOrMax(...criterion.ratings.map(rating => {
        return rating.points * (criterion.weight / 100);
      }));
    });
    return parseFloat(totalPoints).toLocaleString(this.locale);
  }
}
