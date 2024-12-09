import { SakaiElement } from "@sakai-ui/sakai-element";
import { html, nothing } from "lit";
import { getSiteId } from "@sakai-ui/sakai-portal-utils";
import "@sakai-ui/sakai-group-picker";

export class SakaiPermissions extends SakaiElement {

  static properties = {

    tool: { type: String },
    reference: { type: String },
    disableGroups: { attribute: "disabled-groups", type: Boolean },
    bundleKey: { attribute: "bundle-key", type: String },
    onRefresh: { attribute: "on-refresh", type: String },
    fireEvent: { attribute: "fire-event", type: Boolean },

    roles: { state: true },
    groups: { state: true },
    error: { state: true },
  };

  constructor() {

    super();

    this.loadTranslations("permissions-wc").then(i18n => {

      this.loadTranslations(this.bundleKey ? this.bundleKey : this.tool).then(tool => {

        Object.keys(tool).filter(k => k.startsWith("perm-")).forEach(k => i18n[k.substring(5)] = tool[k]);
        this._i18n = i18n;
      });
    });
  }

  connectedCallback() {

    super.connectedCallback();

    this.reference = this.reference || `/site/${getSiteId()}`;

    this._loadPermissions();
  }

  _handleDescriptionClick(e) {

    e.preventDefault();

    const all = this.querySelectorAll(`input[data-perm='${e.target.dataset.perm}']`);
    const checked = this.querySelectorAll(`input[data-perm='${e.target.dataset.perm}']:checked`);

    if (checked.length < all.length || checked.length === 0) {
      all.forEach(i => i.checked = true);
    } else if (checked.length === all.length) {
      all.forEach(i => i.checked = false);
    }
  }

  _handlePermissionChange(e) {
    e.target.closest("div").classList.toggle("active", e.target.checked);
  }

  _handleRoleHover(e, type) {

    const role = e.target.dataset.role.replace(".", "\\.");
    this.querySelectorAll(`div.${role.replace(" ", "_")}-checkbox-cell`).forEach(cell => {
      cell.classList.toggle("row-hover", type === "mouseenter");
    });
  }

  _handleRoleMouseEnter(e) {
    this._handleRoleHover(e, "mouseenter");
  }

  _handleRoleMouseLeave(e) {
    this._handleRoleHover(e, "mouseleave");
  }

  _handleRoleClick(e) {

    e.preventDefault();

    const role = e.target.dataset.role.replace(".", "\\.");

    const anyChecked = this.querySelectorAll(`#permissions-container .${role.replace(" ", "_")}-checkbox-cell input:checked:not(:disabled)`).length > 0;
    this.querySelectorAll(`#permissions-container .${role.replace(" ", "_")}-checkbox-cell input:not(:disabled)`).forEach(i => i.checked = !anyChecked);
  }

  _handlePermissionClick(e) {

    e.preventDefault();

    const checked = this.querySelectorAll("#permissions-container input:checked");

    this.querySelectorAll("#permissions-container input").forEach(input => {
      input.checked = checked.length === 0;
    });
  }

  firstUpdated() {

    // Save the default selected
    this.querySelectorAll("#permissions-container :checked").forEach(el => {
      el.closest("div").classList.add("defaultSelected");
    });
  }

  shouldUpdate() {
    return this._i18n;
  }

  render() {

    if (this.roles) {
      return html`

        ${!this.reference && this.groups?.length > 0 ? html`
          <div>
            <label for="permissons-group-picker">${this._i18n["per.lis.selectgrp"]}</label>
            <sakai-group-picker id="permissions-group-picker"
                groups="${JSON.stringify(this.groups)}"
                @group-selected=${this._groupSelected}>
            </sakai-group-picker>
          </div>
        ` : nothing }
        <div class="mb-1 pt-3">
          <button class="btn btn-secondary"
              aria-label="${this._i18n["per.lis.restoredef"]}"
              @click=${this._resetPermissions}>
            ${this._i18n["per.lis.restoredef"]}
          </button>
        </div>

        <div id="permissions-container" class="container mt-4">
          <div id="permission-header" class="row flex-nowrap">
            <div class="col-md-6 p-3">
              <button class="btn btn-transparent"
                  title="${this._i18n["per.lis.head.title"]}"
                  @click=${this._handlePermissionClick}>
                ${this._i18n["per.lis.head"]}
              </button>
            </div>
            ${this.roles.map(role => html`
            <div class="col-sm role d-none d-md-block p-3 text-center"
                data-role="${role}"
                @mouseenter=${this._handleRoleMouseEnter}
                @mouseleave=${this._handleRoleMouseLeave}>
              <button class="btn btn-transparent"
                  title="${this._i18n["per.lis.role.title"]}"
                  data-role="${role}"
                  @click=${this._handleRoleClick}>
                ${this.roleNameMappings[role]}
              </button>
            </div>
          `)}
          </div>
          ${this.available.map(perm => html`
          <div class="row permission-row">
            <div class="col-md-6 p-3 fw-bolder fw-md-normal">
              <button class="btn btn-transparent fw-bolder fw-md-normal text-start"
                  title="${this._i18n["per.lis.perm.title"]}"
                  data-perm="${perm}"
                  @click=${this._handleDescriptionClick}>
                ${this._i18n[perm]}
              </button>
            </div>
            ${this.roles.map(role => html`
            <div class="col-md ${role.replace(" ", "_")}-checkbox-cell text-start text-md-center p-3 permission-cell border-left-1">
              <label for="${role}:${perm}" class="sr-only">
                <span>${this._i18n["gen.enable"]} ${role}</span>
              </label>
              <input type="checkbox"
                  class="sakai-permission-checkbox"
                  aria-label="${this._i18n["gen.enable"]} ${role}"
                  .checked=${this.on[role].includes(perm)}
                  data-role="${role}"
                  data-perm="${perm}"
                  @change=${this._handlePermissionChange}
                  id="${role}:${perm}"/>
              <div class="d-inline-block d-md-none ms-1">
                ${this.roleNameMappings[role]}
              </div>
            </div>
            `)}
          </div>
          `)}
        </div>

        <div class="act">
          <input type="button" class="active" value="${this._i18n["gen.sav"]}" aria-label="${this._i18n["gen.sav"]}" @click="${this._savePermissions}"/>
          <input type="button" value="${this._i18n["gen.can"]}" aria-label="${this._i18n["gen.can"]}" @click="${this._completePermissions}"/>
          <span id="${this.tool}-failure-message" class="permissions-save-message" style="display: none;">${this._i18n["per.error.save"]}</span>
        </div>
      `;
    } else if (this.error) {
      return html`<div class="sak-banner-error">${this._i18n.alert_permission}</div>`;
    }

    return html`Waiting for permissions`;
  }

  _loadPermissions() {

    const url = `/direct/permissions/${portal.siteId}/getPerms/${this.tool}.json?ref=${this.reference}`;
    fetch(url, { cache: "no-cache", credentials: "same-origin" })
      .then(res => {

        if (res.status === 403) {
          this.error = true;
        } else {
          this.error = false;
          return res.json();
        }
      })
      .then(data => {

        this.on = data.on;
        this.available = data.available;
        if (!this.disableGroups) {
          this.groups = data.groups;
        }
        this.roles = Object.keys(this.on);
        this.roleNameMappings = data.roleNameMappings;
        this.requestUpdate();
      })
      .catch(error => console.error(`Failed to load permissions for tool ${this.tool} from ${url}`, error));
  }

  _savePermissions() {

    document.body.style.cursor = "wait";

    const boxes = this.querySelectorAll("#permissions-container input[type=\"checkbox\"]");
    const params = `ref=${this.reference}&${ Array.from(boxes).reduce((acc, b) => {

      if (b.checked) {
        return `${acc }${encodeURIComponent(b.id)}=true&`;
      }
      return `${acc }${encodeURIComponent(b.id)}=false&`;

    }, "")}`;

    fetch(`/direct/permissions/${portal.siteId}/setPerms`, { method: "POST", credentials: "same-origin", body: new URLSearchParams(params), timeout: 30000 })
      .then(res => {

        if (res.ok) {
          this._completePermissions();
        } else {
          throw new Error("Network response was not ok.");
        }
      })
      .catch(error => {

        document.querySelector(`#${this.tool.replace(".", "\\.")}-failure-message`).style.display = "inline-block";
        console.error(`Failed to save permissions for tool ${this.tool}`, error);
      })
      .finally(() => document.body.style.cursor = "default");
  }

  _resetPermissions() {

    this.updateComplete.then(() => {

      const inputs = this.renderRoot.querySelectorAll("input[type='checkbox']");
      inputs.forEach(elem => {

        const role = elem.getAttribute("data-role");
        const perm = elem.getAttribute("data-perm");
        if (role && perm) {
          const elemChanged = (elem.checked != this.on[role].includes(perm));
          elem.checked = this.on[role].includes(perm);
          elemChanged && elem.dispatchEvent(new Event("change"));
        }
      });
    });
  }

  _completePermissions() {

    if (this.fireEvent) {
      this.dispatchEvent(new CustomEvent("permissions-complete"));
    } else if (this.onRefresh) {
      window.location.href = this.onRefresh;
    } else {
      window.location.reload();
    }
  }

  _groupSelected(e) {

    this.reference = e.detail.value;
    this._loadPermissions();
  }
}
