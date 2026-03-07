import { html, nothing } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import { ifDefined } from "lit/directives/if-defined.js";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import "@sakai-ui/sakai-icon/sakai-icon.js";
import "@sakai-ui/sakai-widgets";
import "@sakai-ui/sakai-widgets/sakai-widget-panel.js";
import "@sakai-ui/sakai-button/sakai-button.js";

export class SakaiHomeDashboard extends SakaiElement {

  static properties = {
    userId: { attribute: "user-id", type: String },
    _data: { state: true },
    _showMotd: { state: true },
    _editing: { state: true },
  };

  constructor() {

    super();

    this.loadTranslations("dashboard");
  }

  set userId(value) {

    this._userId = value;
    this._loadData();
  }

  get userId() { return this._userId; }

  _loadData() {

    const url = `/api/users/${this.userId}/dashboard`;
    fetch(url)
      .then(r => {

        if (r.ok) {
          return r.json();
        }

        throw new Error(`Failed to get dashboard data from ${url}`);
      })
      .then(r => {

        this._data = r;
        this._showMotd = this._data.motd;
      })
      .catch(error => console.error(error));
  }

  widgetLayoutChange(e) {
    this._data.widgetLayout = e.detail.layout;
  }

  edit() {

    this._editing = !this._editing;
    this.layoutBackup = [ ...this._data.widgetLayout ];
  }

  cancel() {

    this._editing = false;
    this._data.widgetLayout = [ ...this.layoutBackup ];
    this.requestUpdate();
  }

  save() {

    this._editing = !this._editing;

    const data = {
      widgetLayout: this._data.widgetLayout,
    };

    const url = `/api/users/${this.userId}/dashboard`;
    fetch(url, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    }).then(r => {

      if (!r.ok) {
        throw new Error(`Failed to update dashboard for url ${url}`);
      }
    }).catch(error => console.error(error.message));
  }

  _toggleMotd() {
    this._showMotd = !this._showMotd;
  }

  shouldUpdate() {
    return this._i18n && this._data;
  }

  _renderWidgetPanel() {

    return html`
      <div class="w-100">
        <sakai-widget-panel
          @changed=${this.widgetLayoutChange}
          .widgetIds=${this._data.widgets}
          .layout=${this._data.widgetLayout}
          site-id=""
          user-id="${ifDefined(this.userId)}"
          ?editing=${this._editing}>
        </sakai-widget-panel>
      </div>
    `;
  }

  _renderHeaderBlock() {

    return html`
      <div class="row mb-3">
        <h2 class="col-8 my-2">${this._i18n.welcome} ${this._data.givenName}</h2>
        <div class="col-4 d-flex justify-content-end align-items-center">
          ${this._editing ? html`
            <div class="mt-1 mx-1 mt-sm-0">
              <button id="dashboard-save-button" type="button"
                  class="btn btn-primary"
                  @click=${this.save}
                  title="${this._i18n.save_tooltip}"
                  aria-label="${this._i18n.save_tooltip}">
                ${this._i18n.save}
              </button>
            </div>
            <div class="mt-1 mt-sm-0">
              <button type="button"
                  class="btn btn-secondary"
                  @click=${this.cancel}
                  title="${this._i18n.cancel_tooltip}"
                  aria-label="${this._i18n.cancel_tooltip}">
                ${this._i18n.cancel}
              </button>
            </div>
          ` : html`
            <div>
              <button slot="invoker"
                  type="button"
                  class="btn btn-secondary"
                  @click=${this.edit}
                  title="${this._i18n.edit_tooltip}"
                  aria-label="${this._i18n.edit_tooltip}">
                <span>${this._i18n.edit}</span>
              </button>
            </div>
          `}
        </div>
      </div>
    `;
  }

  render() {

    return html`

      <div class="home-dashboard-container mt-2">
        <div>
          ${this._renderHeaderBlock()}

          ${this._data.motd ? html`
            <div class="mt-2 mb-3 border border-1 rounded-1 fs-5 fw-normal">
              <div id="dashboard-motd-title-block" class="d-flex p-3 align-items-center" @click=${this._toggleMotd}>
                <div class="me-3 fw-bold">${this._i18n.motd}</div>
                <div>
                  <a href="javascript:;"
                    title="${this._showMotd ? this._i18n.hide_motd_tooltip : this._i18n.show_motd_tooltip}"
                    aria-label="${this._showMotd ? this._i18n.hide_motd_tooltip : this._i18n.show_motd_tooltip}">
                    <sakai-icon type="${this._showMotd ? "up" : "down"}" size="small"></sakai-icon>
                  </a>
                </div>
              </div>
              <div class="mt-4 ps-4 fs-6 ${this._showMotd ? "" : "d-none"}">${unsafeHTML(this._data.motd)}</div>
            </div>
          ` : nothing}

          ${this._renderWidgetPanel()}
        </div>
      </div>
    `;
  }
}
