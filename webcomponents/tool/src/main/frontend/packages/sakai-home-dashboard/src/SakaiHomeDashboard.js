import { html, css } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import { ifDefined } from "lit/directives/if-defined.js";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import "@sakai-ui/sakai-icon/sakai-icon.js";
import "@sakai-ui/sakai-course-list/sakai-course-list.js";
import "@sakai-ui/sakai-widgets";
import "@sakai-ui/sakai-widgets/sakai-widget-panel.js";
import "@sakai-ui/sakai-button/sakai-button.js";

export class SakaiHomeDashboard extends SakaiElement {

  static properties = {

    data: Object,
    state: String,
    courses: { type: Array },
    userId: { attribute: "user-id", type: String },
    showSites: { attribute: "show-sites", type: Boolean },
    _showMotd: { state: true },
    _editing: { state: true },
  };

  constructor() {

    super();

    this.loadTranslations("dashboard").then(r => this._i18n = r);
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
    return this._i18n && this.data;
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
        ${this.data.worksiteSetupUrl ? html`
          <div id="toolbar">
            <sakai-button href="${this.data.worksiteSetupUrl}" title="${this._i18n.worksite_setup_tooltip}" aria-label="${this._i18n.worksite_setup_tooltip}">
              <div id="add-worksite">
                <div><sakai-icon type="add" size="small"></sakai-icon></div>
                <div>${this._i18n.worksite_setup}</div>
              </div>
            </sakai-button>
          </div>
        ` : ""}
        ${this.data.motd ? html`
          <div id="motd">
            <div id="motd-title-block" @click=${this._toggleMotd}>
              <div id="motd-title">${this._i18n.motd}</div>
              <div id="motd-icon">
                <a href="javascript:;"
                  title="${this._showMotd ? this._i18n.hide_motd_tooltip : this._i18n.show_motd_tooltip}"
                  aria-label="${this._showMotd ? this._i18n.hide_motd_tooltip : this._i18n.show_motd_tooltip}">
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
              ?editing=${this._editing}>
            </sakai-widget-panel>
          </div>
        </div>
      </div>
    `;
  }

  static styles = css`
    #container {
      font-family: var(--sakai-font-family);
      background-color: var(--sakai-tool-bg-color);
    }
      #welcome-and-edit-block {
        display: flex;
        align-items: center;
      }
        #welcome {
          flex: 1;
          font-size: var(--sakai-dashboard-welcome-font-size);
        }
        #edit-block {
          flex: 1;
        }
          #save {
            margin-bottom: 4px;
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
