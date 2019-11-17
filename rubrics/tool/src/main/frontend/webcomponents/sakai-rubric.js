import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SakaiRubricCriteria} from "./sakai-rubric-criteria.js";
import {SakaiRubricCriteriaReadonly} from "./sakai-rubric-criteria-readonly.js";
import {SakaiRubricEdit} from "./sakai-rubric-edit.js";
import {SakaiItemDelete} from "./sakai-item-delete.js";
import {SakaiRubricSiteTitle} from "./sakai-rubric-site-title.js";
import {SakaiRubricModifiedDate} from "./sakai-rubric-modified-date.js";
import {SakaiRubricCreatorName} from "./sakai-rubric-creator-name.js";
import {tr} from "./sakai-rubrics-language.js";
import {SharingChangeEvent} from "./sharing-change-event.js";

export class SakaiRubric extends RubricsElement {

  constructor() {

    super();

    this.rubricExpanded = true;

    this._rubric;
    this.shareIcon;

    this.updateRubricConfig = {
      method: "PATCH",
      contentType: "application/json-patch+json"
    };
  }

  static get properties() {
    return { rubric: {type: Object}, token: { type: String } , shareIcon: { type: String }};
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if ("token" === name) {
      this.updateRubricConfig.headers = { "authorization": newValue };
    }
  }

  set rubric(newValue) {

    var oldValue = this._rubric;
    this._rubric = newValue;
    if (!this._rubric.criterions) this._rubric.criterions = [];
    this.updateRubricConfig.url = `/rubrics-service/rest/rubrics/${newValue.id}?projection=inlineRubric`;
    this.handleShareLink();
    this.requestUpdate("rubric", oldValue);
  }

  get rubric() { return this._rubric; }

  shouldUpdate(changedProperties) {
    return changedProperties.has("rubric");
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
          <div class="action-container">
            <span class="hidden-sm hidden-xs sr-only">
              ${this.rubric.metadata.public ?
                html`<sr-lang key="revoke_label">revoke_label</sr-lang>`
                :
                html`<sr-lang key="share_label">share_label</sr-lang>`
              }
            </span>
            <span role="button" title="${tr(this.shareTitleKey, [this.rubric.title])}" tabindex="0" class="share fa ${this.shareIcon}" @click="${this.sharingChange}"></span>
          </div>
          <div class="action-container">
            <span class="hidden-sm hidden-xs sr-only"><sr-lang key="copy" /></span>
            <span role="button" title="${tr("copy")} ${this.rubric.title}" tabindex="0" class="clone fa fa-copy" @click="${this.cloneRubric}"></span>
          </div>
          ${!this.rubric.metadata.locked ?
            html`
            <div class="action-container">
              <span class="hidden-sm hidden-xs sr-only"><sr-lang key="remove_label" /></span>
              <sakai-item-delete token="${this.token}" rubric="${JSON.stringify(this.rubric)}" class="sakai-rubric"></sakai-item-delete>
            </div>
            `
            :
            html``
          }
        </div>
      </div>

      <div class="collapse-details" role="tabpanel" aria-labelledby="rubric_toggle_${this.rubric.id}" id="collapse_${this.rubric.id}">
        <div class="rubric-details style-scope sakai-rubric">
          ${this.rubric.metadata.locked ?
            html`<sakai-rubric-criteria-readonly criteria="${JSON.stringify(this.rubric.criterions)}" token="${this.token}"></sakai-rubric-criteria-readonly>`
            :
            html`<sakai-rubric-criteria rubric-id="${this.rubric.id}" criteria="${JSON.stringify(this.rubric.criterions)}" token="${this.token}"></sakai-rubric-criteria>`
          }
        </div>
      </div>
    `;
  }

  showToolTip(e) {

    e.stopPropagation();
    this.querySelector(".rubric-title").classList.add("active");
  }

  toggleRubric(e) {

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
  }

  cloneRubric(e) {

    e.stopPropagation();
    this.dispatchEvent(new CustomEvent("clone-rubric", {detail: this.rubric, bubbles: true, composed: true}));
  }

  deactivate(e, id) {

    if (!document.getElementById(`collapse_${this.rubric.id}`).opened && this.rubric.id !== id) {
      this.querySelector(".rubric-title").classList.remove("active");
    };
  }

  updateRubricTitle(e) {

    this.updateRubricConfig.data = JSON.stringify([{"op":"replace","path":"/title","value": e.detail}]);
    this.updateRubric();
  }

  sharingChange(e) {

    e.stopPropagation();
    this.rubric.metadata.public = !this.rubric.metadata.public;
    this.updateRubricConfig.data = JSON.stringify([{"op":"replace","path":"/metadata/shared","value": this.rubric.metadata.public}]);
    this.updateRubric();
  }

  handleShareLink() {

    if (this.rubric.metadata.public) {
      this.shareTitleKey = "revoke";
      this.shareIcon = "fa-share-square-o fa-flip-horizontal";
    } else {
      this.shareTitleKey = "share";
      this.shareIcon = "fa-share-square-o";
    }
    this.shareValues = this.rubric.title;
  }

  updateRubric() {

    var newRubric = this.rubric.new;

    $.ajax(this.updateRubricConfig).done(data => {

      this.rubric = data;
      this.dispatchEvent(new SharingChangeEvent());
      this.handleShareLink();
      var sakaiItemDelete = this.querySelector("sakai-item-delete");
      if (sakaiItemDelete) {
        sakaiItemDelete.requestUpdate("item", this.rubric);
        sakaiItemDelete.requestUpdate("rubric", this.rubric);
      }
    }).fail((jqXHR, textStatus, errorThrown) => {
      console.log("Request failed: " + textStatus);
      console.log("Error: " + errorThrown);
    });
  }
}

customElements.define("sakai-rubric", SakaiRubric);
