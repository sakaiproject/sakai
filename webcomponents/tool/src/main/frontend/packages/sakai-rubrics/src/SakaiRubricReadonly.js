import { SakaiRubric } from "./SakaiRubric.js";
import { html } from "lit";
import "../sakai-rubric-criteria-readonly.js";
import "../sakai-rubric-pdf.js";

export class SakaiRubricReadonly extends SakaiRubric {

  static properties = {

    rubric: { type: Object },
    isSuperUser: { type: Boolean },
    enablePdfExport: { attribute: "enable-pdf-export", type: Boolean },
  };

  constructor() {

    super();
    this.isSuperUser = false;
    this.enablePdfExport = false;
    this.fetchUserRoles();
  }

  async fetchUserRoles() {
    try {
      const response = await fetch("/api/user/roles", {
        method: "GET",
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const roles = await response.json();
      this.isSuperUser = roles.isSuperUser;

      console.log("Is SuperUser:", this.isSuperUser);
    } catch (error) {
      console.error("Error fetching user roles:", error);
    }
  }

  shouldUpdate() { return this.rubric; }

  render() {

    return html`
      <div class="rubric-title">
        <div>
          <button class="btn btn-icon"
              id="rubric-toggle-shared-${this.rubric.id}"
              data-bs-toggle="collapse"
              data-bs-target="#rubric-collapse-shared-${this.rubric.id}"
              aria-controls="rubric-collapse-shared-${this.rubric.id}"
              aria-expanded="false"
              title="${this._i18n.toggle_details} ${this.rubric.title}">
            <span class="fa fa-chevron-right"></span>
          </button>
          <span class="rubric-name">${this.rubric.title}</span>
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
            : ""
          }
        </div>

        <div class="d-none d-sm-block rubric-site-title">${this.rubric.siteTitle}</div>
        <div class="d-none d-sm-block rubric-creator-name">${this.rubric.creatorDisplayName}</div>
        <div class="d-none d-sm-block">${this.rubric.formattedModifiedDate}</div>

        <div class="actions">
        ${this.isSuperUser ? html`
          <div class="action-container">
            <span class="d-none d-sm-none d-md-block visually-hidden">${this._i18n.delete_rubric}</span>
            <span role="button" title="${this._i18n.revoke.replace("{}", this.rubric.title)}" tabindex="0" class="fa fa-users" @click="${this.revokeShareRubric}"></span>
          </div>
        ` : ""}
          <div class="action-container">
            <span class="d-none d-sm-none d-md-block visually-hidden">${this._i18n.copy}</span>
            <span role="button" title="${this._i18n.copy_to_site.replace("{}", this.rubric.title)}" tabindex="0" class="clone fa fa-copy" @click="${this.copyToSite}"></span>
          </div>
          ${this.enablePdfExport ? html`
            <div class="action-container">
              <sakai-rubric-pdf
                  site-id="${this.siteId}"
                  rubric-title="${this.rubric.title}"
                  rubric-id="${this.rubric.id}">
              </sakai-rubric-pdf>
            </div>
          ` : ""}
        ${this.isSuperUser ? html`
          <div class="action-container">
              <span class="d-none d-sm-none d-md-block visually-hidden">${this._i18n.delete_rubric}</span>
              <span role="button" title="${this._i18n.remove.replace("{}", this.rubric.title)}" tabindex="0" class="fa fa-times" @click="${this.deleteRubric}"></span>
          </div>
          ` : ""}
        </div>
      </div>

      <div class="collapse" id="rubric-collapse-shared-${this.rubric.id}">
        <div class="rubric-details style-scope sakai-rubric">
          <sakai-rubric-criteria-readonly .criteria="${this.rubric.criteria}" .weighted=${this.rubric.weighted}></sakai-rubric-criteria-readonly>
        </div>
      </div>
    `;
  }

  copyToSite(e) {

    e.stopPropagation();
    this.dispatchEvent(new CustomEvent("copy-to-site", { detail: this.rubric.id }));
  }

  deleteRubric(e) {
    e.stopPropagation();
    this.dispatchEvent(new CustomEvent("delete-rubric", { detail: { id: this.rubric.id, title: this.rubric.title }, bubbles: true, composed: true, }));
  }

  revokeShareRubric(e) {
    e.stopPropagation();
    this.dispatchEvent(new CustomEvent("revoke-shared-rubric", { detail: { id: this.rubric.id, title: this.rubric.title, shared: this.rubric.shared }, bubbles: true, composed: true, }));
  }
}
