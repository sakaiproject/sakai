import { html, nothing } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-button/sakai-button.js";
import "@sakai-ui/sakai-widgets/sakai-widget-panel.js";
import "@lion/dialog/define";
import "../sakai-course-dashboard-template-picker.js";
import "../sakai-course-header.js";
import "../sakai-course-overview.js";

export class SakaiCourseDashboard extends SakaiElement {

  static properties = {

    state: String,
    userId: { attribute: "user-id", type: String },
    siteId: { attribute: "site-id", type: String },
    editing: { type: Boolean },

    data: { state: true },
  };

  constructor() {

    super();

    this.loadTranslations("dashboard").then(r => this._i18n = r);
  }

  connectedCallback() {

    super.connectedCallback();

    this._loadData();
  }

  _loadData() {

    const url = `/api/sites/${this.siteId}/dashboard`;
    fetch(url, { credentials: "include" })
      .then(r => {

        if (r.ok) {
          return r.json();
        }
        throw new Error(`Failed to get dashboard data from ${url}`);

      })
      .then(r => this.data = r)
      .catch(error => console.error(error));
  }

  widgetLayoutChanged(e) {
    this.data.layout = e.detail.layout;
  }

  overviewChanged(e) {

    this.data.overview = e.detail.content;
    this.requestUpdate();
  }

  edit() {

    this.editing = !this.editing;
    this.imageBackup = this.data.image;
    this.overviewBackup = this.data.overview;
    this.programmeBackup = this.data.programme;
    this.layoutBackup = [ ...this.data.layout ];
    this.templateBackup = this.data.template;
  }

  cancel() {

    this.editing = false;
    this.data.overview = this.overviewBackup;
    this.data.programme = this.programmeBackup;
    document.getElementById("course-dashboard-programme").innerHTML = this.data.programme;
    this.data.layout = [ ...this.layoutBackup ];
    this.data.template = this.templateBackup;
    URL.revokeObjectURL(this.image);
    this.data.image = this.imageBackup;
    this.requestUpdate();
  }

  save() {

    this.editing = !this.editing;

    if (this.newImageBlob) {

      const fd = new FormData();
      fd.append("siteImage", this.newImageBlob);

      const imageUrl = `/api/sites/${this.siteId}/image`;
      fetch(imageUrl, {
        method: "POST",
        credentials: "include",
        body: fd,
      }).then(r => {

        if (!r.ok) {
          throw new Error(`Failed to update image for url ${imageUrl}`);
        } else {
          URL.revokeObjectURL(this.data.image);
          return r.text();
        }
      })
      .then(newUrl => {

        // Add a version string so the browser picks up the new image
        this.data.image = `${newUrl}?version=${Math.random()}`;
        this.requestUpdate();
      })
      .catch(error => console.error(error.message));
    }

    const data = {
      layout: this.data.layout,
      overview: this.data.overview,
      programme: this.data.programme,
      template: this.data.template,
    };

    const url = `/api/sites/${this.siteId}/dashboard`;
    fetch(url, {
      method: "PUT",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    }).then(r => {

      if (!r.ok) {
        throw new Error(`Failed to update dashboard for url ${url}`);
      }
    }).catch(error => console.error(error.message));
  }

  imageEdited(e) {

    this.data.image = e.detail.url;
    this.newImageBlob = e.detail.blob;
    this.querySelector("sakai-course-header").requestUpdate();
    this.requestUpdate();
  }

  showTemplates() {
    document.getElementById("templates").__toggle();
  }

  templateSelected(e) {

    this.data.template = e.detail.template;

    this.requestUpdate();

    this.updateComplete.then(() => {
      this.querySelector("#course-dashboard-save sakai-button").focus();
    });
  }

  programmeUpdated(e) {
    this.data.programme = e.target.innerText;
  }

  shouldUpdate() {
    return this._i18n && this.data;
  }

  titleBlock() {

    return html`
      <div id="course-dashboard-title-and-edit-block">
        <div id="course-dashboard-edit-block">
          ${this.data.editable ? html`
            ${this.editing ? html`
              <div id="course-dashboard-layout">
                <sakai-button title="${this._i18n.layout_tooltip}" @click=${this.showTemplates}>${this._i18n.layout}</sakai-button>
              </div>
              <div id="course-dashboard-save" class="mt-1 mt-sm-0">
                <sakai-button @click=${this.save} title="${this._i18n.save_tooltip}" aria-label="${this._i18n.save_tooltip}" primary>${this._i18n.save}</sakai-button>
              </div>
              <div id="course-dashboard-cancel" class="mt-1 mt-sm-0">
                <sakai-button @click=${this.cancel} title="${this._i18n.cancel_tooltip}" aria-label="${this._i18n.cancel_tooltip}">${this._i18n.cancel}</sakai-button>
              </div>
            ` : nothing }
          ` : nothing}
        </div>
      </div>
    `;
  }

  widgetPanel(columns) {

    return html`
      <sakai-widget-panel
        id="course-dashboard-widget-grid"
        @changed=${this.widgetLayoutChanged}
        .widgetIds=${this.data.widgets}
        .layout=${this.data.layout}
        site-id="${this.siteId}"
        user-id="${this.userId}"
        columns=${columns}
        ?editing=${this.editing}>
      </sakai-widget-panel>
    `;
  }

  template1() {

    return html`
      ${this.titleBlock()}
      <div id="course-dashboard-l1-overview-and-widgets-block">
        <div id="course-dashboard-l1-widgets">
          ${this.widgetPanel(1)}
        </div>
      </div>
    `;
  }

  template2() {

    return html`
      ${this.titleBlock()}
      <div id="course-dashboard-l2-widgets">
        ${this.widgetPanel(3)}
      </div>
    `;
  }

  template3() {

    return html`
      ${this.titleBlock()}
      <div id="course-dashboard-l3-overview-and-widgets-block">
        <div id="course-dashboard-l3-widgets">
          ${this.widgetPanel(2)}
        </div>
      </div>
    `;
  }

  renderOverview() {

    return html`
      <sakai-course-overview @changed=${this.overviewChanged}
          overview="${this.data.overview || ""}"
          ?editing=${this.editing}>
      </sakai-course-overview>
    `;
  }

  render() {

    return html`
      <lion-dialog id="templates">
        <sakai-course-dashboard-template-picker .data=${this.data} .template=${this.data.template} slot="content" @template-selected=${this.templateSelected}></sakai-course-dashboard-template-picker>
        <div slot="invoker" style="display: none;"></div>
      </lion-dialog>
      <div class="course-dashboard-container mt-2">
        ${this.data.template === 1 ? this.template1() : nothing }
        ${this.data.template === 2 ? this.template2() : nothing }
        ${this.data.template === 3 ? this.template3() : nothing }
      </div>
    `;
  }
}
