import { html, css, LitElement } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import "@sakai-ui/sakai-icon/sakai-icon.js";
import { loadProperties } from "@sakai-ui/sakai-i18n";
import "@sakai-ui/sakai-course-list/sakai-course-list.js";
import "@sakai-ui/sakai-widgets";
import "@sakai-ui/sakai-widgets/sakai-widget-panel.js";
import "@sakai-ui/sakai-button/sakai-button.js";

export class SakaiHomeDashboard extends LitElement {

  static properties = {

    data: Object,
    i18n: Object,
    state: String,
    courses: { type: Array },
    userId: { attribute: "user-id", type: String },
    showSites: { attribute: "show-sites", type: Boolean },
    _showMotd: { state: true },
    _editing: { state: true },
  };

  constructor() {

    super();

    loadProperties("dashboard").then(r => this.i18n = r);
  }

  set userId(value) {

    this._userId = value;
    this._loadData();
  }

  get userId() { return this._userId; }

  _loadData() {

    const url = `/api/users/${this.userId}/dashboard`;
    fetch(url, { credentials: "include" })
      .then(r => {

        if (r.ok) {
          return r.json();
        }
        throw new Error(`Failed to get dashboard data from ${url}`);

      })
      .then(r => {

        this.data = r;
        this._showMotd = this.data.motd;
      })
      .catch(error => console.error(error));
  }

  shouldUpdate() {
    return this.i18n && this.data;
  }

  widgetLayoutChange(e) {
    this.data.layout = e.detail.layout;
  }

  edit() {

    this._editing = !this._editing;
    this.layoutBackup = [ ...this.data.layout ];
  }

  cancel() {

    this._editing = false;
    this.data.layout = [ ...this.layoutBackup ];
    this.requestUpdate();
  }

  save() {

    this._editing = !this._editing;

    const url = `/api/users/${this.userId}/dashboard`;
    fetch(url, {
      method: "PUT",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ layout: this.data.layout }),
    }).then(r => {

      if (!r.ok) {
        throw new Error(`Failed to update dashboard for url ${url}`);
      }
    }).catch(error => console.error(error.message));
  }

  _toggleMotd() {
    this._showMotd = !this._showMotd;
  }

  render() {

    return html`

      <div id="container">
        <div id="welcome-and-edit-block">
          <div id="welcome">${this.i18n.welcome} ${this.data.givenName}</div>
          <div id="edit-block">
          ${this._editing ? html`
            <div id="save">
              <sakai-button @click=${this.save} title="${this.i18n.save_tooltip}" aria-label="${this.i18n.save_tooltip}">${this.i18n.save}</sakai-button>
            </div>
            <div id="cancel">
              <sakai-button @click=${this.cancel} title="${this.i18n.cancel_tooltip}" aria-label="${this.i18n.cancel_tooltip}">${this.i18n.cancel}</sakai-button>
            </div>
          ` : html`
            <div id="edit">
              <sakai-button slot="invoker" @click=${this.edit} title="${this.i18n.edit_tooltip}" arial-label="${this.i18n.edit_tooltip}">${this.i18n.edit}</sakai-button>
            </div>
          `}
          </div>
        </div>
        ${this.data.worksiteSetupUrl ? html`
          <div id="toolbar">
            <sakai-button href="${this.data.worksiteSetupUrl}" title="${this.i18n.worksite_setup_tooltip}" aria-label="${this.i18n.worksite_setup_tooltip}">
              <div id="add-worksite">
                <div><sakai-icon type="add" size="small"></sakai-icon></div>
                <div>${this.i18n.worksite_setup}</div>
              </div>
            </sakai-button>
          </div>
        ` : ""}
        ${this.data.motd ? html`
          <div id="motd">
            <div id="motd-title-block" @click=${this._toggleMotd}>
              <div id="motd-title">${this.i18n.motd}</div>
              <div id="motd-icon">
                <a href="javascript:;"
                  title="${this._showMotd ? this.i18n.hide_motd_tooltip : this.i18n.show_motd_tooltip}"
                  aria-label="${this._showMotd ? this.i18n.hide_motd_tooltip : this.i18n.show_motd_tooltip}">
                  <sakai-icon type="${this._showMotd ? "up" : "down"}" size="small"></sakai-icon>
                </a>
              </div>
            </div>
            <div id="motd-message" style="display: ${this._showMotd ? "block" : "none"}">${unsafeHTML(this.data.motd)}</div>
          </div>
        ` : ""}
        <div id="courses-and-widgets">
          ${this.showSites ? html`
          <div id="courses"><sakai-course-list></div>
          ` : ""}
          <div id="widgets">
            <sakai-widget-panel
              id="widget-grid"
              @changed=${this.widgetLayoutChange}
              .widgetIds=${this.data.widgets}
              .layout=${this.data.layout}
              site-id=""
              user-id="${ifDefined(this.userId ? this.userId : "")}"
              columns="2"
              ?editing=${this._editing}>
            </sakai-widget-panel>
          </div>
        </div>
      </div>
    `;
  }

  static styles = css`
    #container {
      padding: var(--sakai-dashboard-container-padding);
      font-family: var(--sakai-font-family);
      background-color: var(--sakai-tool-bg-color);
    }
      #welcome-and-edit-block {
        display: flex;
      }
        #welcome {
          flex: 1;
          font-size: var(--sakai-dashboard-welcome-font-size);
        }
        #edit-block {
          flex: 1;
        }
      #toolbar {
        display: flex;
        justify-content: flex-end;
        margin-top: 20px;
      }
        #add-worksite {
          flex: 0;
          display: flex;
          justify-content: space-between;
          text-align: center;
          white-space: nowrap;
        }
          #add-worksite div {
            flex: 1;
          }
          #add-worksite sakai-icon {
            margin-right: 10px;
          }
      #motd {
        border-radius: var(--sakai-course-card-border-radius);
        background-color: var(--sakai-motd-bg-color);
        font-size: var(--sakai-motd-font-size);
        font-weight: var(--sakai-motd-font-weight);
        border: solid 1px #e0e0e0;
        padding: var(--sakai-motd-padding);
        margin-top: var(--sakai-motd-margin-top);
      }

        #motd-title-block {
          display: flex;
          align-items: center;
        }

        #motd-title {
          margin-right: 14px;
        }
        #motd-message {
          font-size: var(--sakai-motd-message-font-size);
          margin-top: 30px;
          padding-left: 20px;
        }
      #courses-and-widgets {
        display: flex;
        margin-top: 30px;
      }
        #courses {
          flex: 0;
          margin-right: 20px;
        }
        #widgets {
          flex: 1;
        }
          sakai-widget-panel {
            width: 100%;
          }

        #edit-block {
          flex: 1;
          text-align:right;
        }
        #edit-block div {
          display: inline-block;
        }
  `;
}
