import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import "./sakai-rubric-criteria.js";
import "./sakai-rubric-criteria-readonly.js";
import "./sakai-rubric-edit.js";
import "./sakai-item-delete.js";
import "./sakai-rubric-site-title.js";
import "./sakai-rubric-modified-date.js";
import "./sakai-rubric-creator-name.js";
import {tr} from "./sakai-rubrics-language.js";
import {SharingChangeEvent} from "./sharing-change-event.js";

export class SakaiRubric extends RubricsElement {

  constructor() {

    super();

    this.updateRubricOptions = {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json-patch+json",
      },
    };
  }

  static get properties() {

    return {
      rubric: { type: Object},
      token: { type: String },
      shareIcon: { type: String },
      weightedIcon: String,
      totalWeight: String,
      validWeight: Boolean
    };
  }

  set token(newValue) {

    this._token = newValue;
    this.updateRubricOptions.headers["Authorization"] = newValue;
  }

  get token() { return this._token; }

  set rubric(newValue) {

    var oldValue = this._rubric;
    this._rubric = newValue;
    if (!this._rubric.criterions) this._rubric.criterions = [];
    this.handleWeightLink();
    this.handleShareLink();
    this.requestUpdate("rubric", oldValue);
  }

  get rubric() { return this._rubric; }

  shouldUpdate() {
    return this.rubric;
  }

  render() {

    return html`
      <div class="rubric-title" @click="${this.toggleRubric}">
        <div>
          <a href="#" class="rubric-name" id="rubric_toggle_${this.rubric.id}" aria-expanded="${this.rubricExpanded}" role="tab" title="${tr("toggle_details")} ${this.rubric.title}" tabindex="0" >
            <span class="fa fa-chevron-right"></span>
            ${this.rubric.title}
          </a>

          ${this.rubric.metadata.locked ?
            html`<span tabindex="0" role="display" title="${this.rubric.title} ${tr("is_locked")}" class="locked fa fa-lock"></span>`
            :
            html`<sakai-rubric-edit @show-tooltip="${this.showToolTip}" @update-rubric-title="${this.updateRubricTitle}" rubric="${JSON.stringify(this.rubric)}" token="${this.token}"></sakai-rubric-edit>`
          }
        </div>

        <div class="hidden-xs"><sakai-rubric-site-title site-id="${this.rubric.metadata.ownerId}"></sakai-rubric-site-title></div>
        <div class="hidden-xs"><sakai-rubric-creator-name creator-id="${this.rubric.metadata.creatorId}"></sakai-rubric-creator-name></div>
        <div class="hidden-xs"><sakai-rubric-modified-date modified="${this.rubric.metadata.modified}"></sakai-rubric-modified-date></div>

        <div class="actions">
          ${!this.rubric.metadata.locked ? html`
            <div class="action-container">
              <span class="hidden-sm hidden-xs sr-only">
                ${this.rubric.weighted ?
                  html`<sr-lang key="weighted_label">weighted_label</sr-lang>`
                  :
                  html`<sr-lang key="standard_label">standard_label</sr-lang>`
                }
              </span>
              <a role="button" title="${this.weightLabel}" tabindex="0" class="linkStyle weighted fa ${this.weightedIcon}" @keyup="${this.openEditWithKeyboard}" @click="${this.weightedChange}" href="#"></a>
            </div>`
          : ""
          }
          <div class="action-container">
            <span class="hidden-sm hidden-xs sr-only">
              ${this.rubric.metadata.public ?
                html`<sr-lang key="revoke_label">revoke_label</sr-lang>`
                :
                html`<sr-lang key="share_label">share_label</sr-lang>`
              }
            </span>
            <a role="button" title="${tr(this.shareTitleKey, [this.rubric.title])}" tabindex="0" class="linkStyle share fa ${this.shareIcon}" @keyup="${this.openEditWithKeyboard}" @click="${this.sharingChange}" href="#"></a>
          </div>
          <div class="action-container">
            <span class="hidden-sm hidden-xs sr-only"><sr-lang key="copy" /></span>
            <a role="button" title="${tr("copy")} ${this.rubric.title}" tabindex="0" class="linkStyle clone fa fa-copy" @keyup="${this.openEditWithKeyboard}" @click="${this.cloneRubric}" href="#"></a>
          </div>
          ${!this.rubric.metadata.locked ? html`
            <div class="action-container">
              <span class="hidden-sm hidden-xs sr-only"><sr-lang key="remove_label" /></span>
              <sakai-item-delete token="${this.token}" rubric="${JSON.stringify(this.rubric)}" class="sakai-rubric"></sakai-item-delete>
            </div>
            `
            :
            ""
          }
        </div>
      </div>

      <div class="collapse-details" role="tabpanel" aria-labelledby="rubric_toggle_${this.rubric.id}" id="collapse_${this.rubric.id}">
        <div class="rubric-details style-scope sakai-rubric">
          ${this.rubric.metadata.locked ? html`
              <sakai-rubric-criteria-readonly
                criteria="${JSON.stringify(this.rubric.criterions)}"
                token="${this.token}"
                .weighted=${this.rubric.weighted}
              />`
            : html`
              <sakai-rubric-criteria
                rubric-id="${this.rubric.id}"
                criteria="${JSON.stringify(this.rubric.criterions)}"
                token="${this.token}"
                @save-weights="${this.handleSaveWeights}"
                @weight-changed="${this.handleCriterionWeightChange}"
                @refresh-total-weight="${this.handleRefreshTotalWeight}"
                .weighted=${this.rubric.weighted}
                total-weight="${this.totalWeight}"
                ?valid-weight="${this.validWeight}"
              />`
          }
        </div>
      </div>
    `;
  }

  toggleRubric(e) {

    e && e.preventDefault();

    var titlecontainer = this.querySelector(".rubric-title");

    var collapse = $(`#collapse_${this.rubric.id}`);
    collapse.toggle();

    var icon = $(`#rubric_toggle_${this.rubric.id} span`);

    if (collapse.is(":visible")) {
      this.rubricExpanded = "true";
      titlecontainer.classList.add("active");
      icon.removeClass("fa-chevron-right").addClass("fa-chevron-down");
    } else {
      this.rubricExpanded = "false";
      titlecontainer.classList.remove("active");
      icon.removeClass("fa-chevron-down").addClass("fa-chevron-right");
    }

    this.handleRefreshTotalWeight(e);
  }

  cloneRubric(e) {

    e.stopPropagation();
    this.dispatchEvent(new CustomEvent("clone-rubric", {detail: this.rubric, bubbles: true, composed: true}));
  }

  updateRubricTitle(e) {

    this.updateRubricOptions.body = JSON.stringify([{"op":"replace","path":"/title","value": e.detail}]);

    fetch(`/rubrics-service/rest/rubrics/${this.rubric.id}`, this.updateRubricOptions)
      .then(r => {

        if (r.ok) {
          this.rubric.title = e.detail;
          this.rubric.new = false;
          this.requestUpdate();
          this.updateItemDelete();
          this.dispatchEvent(new SharingChangeEvent());
        }
      });
  }

  handleSaveWeights() {

    var saveWeightsBtn = document.querySelector(`[rubric-id='${this.rubric.id}'] .save-weights`);
    var saveSuccessLbl = document.querySelector(`[rubric-id='${this.rubric.id}'] .save-success`);

    if(saveWeightsBtn) saveWeightsBtn.setAttribute('disabled', true);

    this.rubric.criterions.forEach(cr => {
      $.ajax({
        url: `/rubrics-service/rest/criterions/${cr.id}`,
        headers: { "authorization": this.token },
        method: "PATCH",
        contentType: "application/json",
        data: JSON.stringify({ weight: cr.weight })
      }).done(() => {

        if(saveSuccessLbl) saveSuccessLbl.classList.add('in');
        setTimeout(() => {
          if(saveWeightsBtn) saveWeightsBtn.removeAttribute('disabled');
        }, 1000);
        setTimeout(() => {
          if(saveSuccessLbl) saveSuccessLbl.classList.remove('in');
        }, 5000);
        this.requestUpdate();
      }).fail((jqXHR, error, message) => {
        console.log(error);console.log(message);
      });
    });
  }

  handleCriterionWeightChange(e) {

    var payload = e.detail;

    this.rubric.criterions = payload.criteria;
    var criterionModified = this.rubric.criterions.find(el => el.id === payload.criterionId);
    var oldValue = criterionModified.weight;

    criterionModified.weight = payload.value;
    if (oldValue === payload.value) {
      return;
    }

    const total = this.rubric.criterions.reduce((acc, el) => acc + el.weight, 0);

    this.validWeight = total == 100;

    this.totalWeight = total.toLocaleString(this.locale);
    this.requestUpdate();
  }

  handleRefreshTotalWeight(e) {

    if (e && e.detail.criteria) {
      this.rubric.criterions = e.detail.criteria;
    }
    this.totalWeight = 0;
    this.rubric.criterions.forEach(cr => {
      this.totalWeight = this.totalWeight + cr.weight;
    });
    this.validWeight = this.totalWeight == 100;
  }

  weightedChange(e) {

    e.stopPropagation();
    this.rubric.weighted = !this.rubric.weighted;
    if (this.rubric.weighted) {
      this.rubric.criterions.forEach(cr => cr.weight = 0);
      this.rubric.criterions[0].weight = 100;
      this.handleSaveWeights(e);
      this.handleRefreshTotalWeight();
    }

    this.updateRubricOptions.body = JSON.stringify([{ "op": "replace", "path": "/weighted", "value": this.rubric.weighted }]);
    fetch(`/rubrics-service/rest/rubrics/${this.rubric.id}`, this.updateRubricOptions)
      .then(r => {

        if (r.ok) {
          this.handleWeightLink();
          this.requestUpdate();
        }
      });
  }

  sharingChange(e) {

    e.stopPropagation();

    this.rubric.metadata.public = !this.rubric.metadata.public;

    this.updateRubricOptions.body = JSON.stringify([{"op":"replace","path":"/metadata/shared","value": this.rubric.metadata.public}]);
    fetch(`/rubrics-service/rest/rubrics/${this.rubric.id}`, this.updateRubricOptions)
      .then(r => {

        if (r.ok) {
          this.dispatchEvent(new SharingChangeEvent());
          this.handleShareLink();
        }
      });
  }

  handleWeightLink() {

    if (this.rubric.weighted) {
      this.weightedIcon = "fa-percent"
      this.weightLabel = tr("weighted_label")
    } else {
      this.weightedIcon = "fa-hashtag"
      this.weightLabel = tr("standard_label")
    }
  }

  handleShareLink() {

    if (this.rubric.metadata.public) {
      this.shareTitleKey = "revoke";
      this.shareIcon = "fa-globe text-primary";
    } else {
      this.shareTitleKey = "share";
      this.shareIcon = "fa-eye-slash text-muted";
    }
    this.shareValues = this.rubric.title;
  }

  updateItemDelete() {

    const sakaiItemDelete = this.querySelector("sakai-item-delete");
    if (sakaiItemDelete) {
      sakaiItemDelete.requestUpdate("item", this.rubric);
      sakaiItemDelete.requestUpdate("rubric", this.rubric);
    }
  }

  openEditWithKeyboard(e) {

    const spaceBarKeyCode = (e.keyCode == 32);	

    if (spaceBarKeyCode && e.target.classList.contains('weighted')) {
      this.weightedChange(e);
    }

    if (spaceBarKeyCode && e.target.classList.contains('share')) {
      this.sharingChange(e);
    }

    if (spaceBarKeyCode && e.target.classList.contains('clone')) {
      this.cloneRubric(e);
    }
  }
}

if (!customElements.get("sakai-rubric")) {
  customElements.define("sakai-rubric", SakaiRubric);
}
