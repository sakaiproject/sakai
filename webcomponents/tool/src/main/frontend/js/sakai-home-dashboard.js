import { html, css, LitElement } from './assets/lit-element/lit-element.js';
import { ifDefined } from './assets/lit-html/directives/if-defined.js';
import { unsafeHTML } from './assets/lit-html/directives/unsafe-html.js';
import './sakai-icon.js';
import { loadProperties } from "./sakai-i18n.js";
import "./sakai-course-list.js";
import "./widgets/sakai-widget-panel.js";
import "./sakai-button.js";

export class SakaiHomeDashboard extends LitElement {

  static get properties() {

    return {
      data: Object,
      i18n: Object,
      state: String,
      courses: { type: Array},
      userId: { attribute: "user-id", type: String },
      showSites: { attribute: "show-sites", type: Boolean },
      showMotd: Boolean,
      editing: { type: Boolean },
    };
  }

  constructor() {

    super();
    loadProperties("dashboard").then((r) => this.i18n = r);
  }

  set userId(value) {

    this._userId = value;
    this.loadData();
  }

  get userId() { return this._userId; }

  loadData() {

    const url = `/api/users/${this.userId}/dashboard`;
    fetch(url, { credentials: "include" })
      .then(r => {

        if (r.ok) {
          return r.json();
        } else {
          throw new Error(`Failed to get dashboard data from ${url}`);
        }
      })
      .then(r => this.data = r)
      .catch(error => console.error(error));
  }

  shouldUpdate() {
    return this.i18n && this.data;
  }

  widgetLayoutChange(e) {
    this.data.layout = e.detail.layout;
  }

  edit() {

    this.editing = !this.editing;
    this.layoutBackup = [...this.data.layout];
  }

  cancel() {

    this.editing = false;
    this.data.layout = [...this.layoutBackup];
    this.requestUpdate();
  }

  save() {

    this.editing = !this.editing;

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


  render() {

    return html`

      <div id="container">
        <div id="welcome-and-edit-block">
          <div id="welcome">${this.i18n["welcome"]} ${this.data.givenName}</div>
          <div id="edit-block">
          ${this.editing ? html`
            <div id="save">
              <sakai-button @click=${this.save} title="${this.i18n["save_tooltip"]}" aria-label="${this.i18n["save_tooltip"]}">${this.i18n["save"]}</sakai-button>
            </div>
            <div id="cancel">
              <sakai-button @click=${this.cancel} title="${this.i18n["cancel_tooltip"]}" aria-label="${this.i18n["cancel_tooltip"]}">${this.i18n["cancel"]}</sakai-button>
            </div>
          ` : html`
            <div id="edit">
              <sakai-button slot="invoker" @click=${this.edit} title="${this.i18n["edit_tooltip"]}" arial-label="${this.i18n["edit_tooltip"]}">${this.i18n["edit"]}</sakai-button>
            </div>
          `}
          </div>
        </div>
        ${this.data.worksiteSetupUrl ? html`
          <div id="toolbar">
            <sakai-button href="${this.data.worksiteSetupUrl}" title="${this.i18n["worksite_setup_tooltip"]}" aria-label="${this.i18n["worksite_setup_tooltip"]}">
              <div id="add-worksite">
                <div><sakai-icon type="add" size="small"></sakai-icon></div>
                <div>${this.i18n["worksite_setup"]}</div>
              </div>
            </sakai-button>
          </div>
        ` : ""}
        ${this.data.motd ? html`
          <div id="motd">
            <div id="motd-title-block" @click=${() => this.showMotd = !this.showMotd}>
              <div id="motd-title">${this.i18n["motd"]}</div>
              <div id="motd-icon">
                <a href="javascript:;"
                  title="${this.showMotd ? this.i18n["hide_motd_tooltip"] : this.i18n["show_motd_tooltip"]}"
                  aria-label="${this.showMotd ? this.i18n["hide_motd_tooltip"] : this.i18n["show_motd_tooltip"]}">
                  <sakai-icon type="${this.showMotd ? "up" : "down"}" size="small"></sakai-icon>
                </a>
              </div>
            </div>
            <div id="motd-message" style="display: ${this.showMotd ? "block" : "none"}">${unsafeHTML(this.data.motd)}</div>
          </div>
        ` : ""}
        <div id="courses-and-widgets">
          ${this.showSites ? html`
          <div id="courses"><sakai-course-list></div>
          `: ""}
          <div id="widgets">
            <sakai-widget-panel
              id="widget-grid"
              @changed=${this.widgetLayoutChange}
              widget-ids=${JSON.stringify(this.data.widgets)}
              layout="${JSON.stringify(this.data.layout)}"
              site-id="${ifDefined(this.siteId ? this.siteId : undefined)}"
              user-id="${ifDefined(this.userId ? this.userId : undefined)}"
              columns="2"
              ?editing=${this.editing}>
          </div>
        </div>
      </div>
    `;
  }

  static get styles() {

    return css`
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
            //align-items: center;
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
          #motd-icon {
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
}

if (!customElements.get("sakai-home-dashboard")) {
  customElements.define("sakai-home-dashboard", SakaiHomeDashboard);
}
