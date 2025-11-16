import { html, nothing } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-widgets/sakai-widget-panel.js";
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

    this.loadTranslations("dashboard");
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
    this.data.widgetLayout = e.detail.layout;
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
    this.widgetLayoutBackup = [ ...this.data.widgetLayout ];
    this.templateBackup = this.data.template;
  }

  cancel() {

    this.editing = false;
    this.data.overview = this.overviewBackup;
    this.data.programme = this.programmeBackup;
    document.getElementById("course-dashboard-programme").innerHTML = this.data.programme;
    this.data.widgetLayout = [ ...this.widgetLayoutBackup ];
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
      widgetLayout: this.data.widgetLayout,
      overview: this.data.overview,
      programme: this.data.programme,
      template: this.data.template,
    };

    const url = `/api/sites/${this.siteId}/dashboard`;
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

  imageEdited(e) {

    this.data.image = e.detail.url;
    this.newImageBlob = e.detail.blob;
    this.querySelector("sakai-course-header").requestUpdate();
    this.requestUpdate();
  }

  templateSelected(e) {

    this.data.template = parseInt(e.target.dataset.template);

    this.requestUpdate();

    this.updateComplete.then(() => {
      this.querySelector("#course-dashboard-save button").focus();
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
        <div id="course-dashboard-title-block">
          <h2>${this.data.title}</h2>
          <div id="course-dashboard-programme" @input=${this.programmeUpdated} ?contenteditable=${this.editing}>${this.data.programme}</div>
        </div>
        <div id="course-dashboard-edit-block">
          ${this.data.editable ? html`
            ${this.editing ? html`
              <div id="course-dashboard-layout">
                <button type="button"
                    class="btn btn-secondary me-3"
                    data-bs-toggle="modal"
                    data-bs-target="#course-dashboard-template-picker">
                  ${this._i18n.layout}
                </button>
              </div>
              <div id="course-dashboard-save" class="mt-1 mt-sm-0">
                <button type="button"
                    class="btn btn-primary"
                    @click=${this.save}
                    title="${this._i18n.save_tooltip}"
                    aria-label="${this._i18n.save_tooltip}">
                  ${this._i18n.save}
                </button>
              </div>
              <div id="course-dashboard-cancel" class="mt-1 mt-sm-0">
                <button type="button"
                    class="btn btn-secondary"
                    @click=${this.cancel}
                    title="${this._i18n.cancel_tooltip}"
                    aria-label="${this._i18n.cancel_tooltip}">
                  ${this._i18n.cancel}
                </button>
              </div>
            ` : html`
              <div id="course-dashboard-edit">
                <button type="button"
                    class="btn btn-secondary"
                    @click=${this.edit}
                    title="${this._i18n.edit_tooltip}"
                    aria-label="${this._i18n.edit_tooltip}">
                  ${this._i18n.edit}
                </button>
              </div>
            `}
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
        .layout=${this.data.widgetLayout}
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
        <div id="course-dashboard-l1-overview-block">
          ${this.renderOverview()}
        </div>
        <div id="course-dashboard-l1-widgets">
          ${this.widgetPanel(1)}
        </div>
      </div>
    `;
  }

  template2() {

    return html`
      ${this.titleBlock()}
      <div id="course-dashboard-l2-header-and-overview-block" class="d-sm-flex mb-4">
        <div id="course-dashboard-l2-header-block" class="me-sm-3 mb-3 mb-sm-0">
          <sakai-course-header .site=${this.data} @image-edited=${this.imageEdited} ?editing=${this.editing}></sakai-course-header>
        </div>
        <div id="course-dashboard-l2-overview-block">
          ${this.renderOverview()}
        </div>
      </div>
      <div id="course-dashboard-l2-widgets">
        ${this.widgetPanel(3)}
      </div>
    `;
  }

  template3() {

    return html`
      ${this.titleBlock()}
      <div id="course-dashboard-l3-overview-and-widgets-block">
        <div id="course-dashboard-l3-overview-block">
          ${this.renderOverview()}
        </div>
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

      <div class="modal fade" id="course-dashboard-template-picker" tabindex="-1" aria-labelledby="course-dashboard-template-picker-label" aria-hidden="true">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <div class="modal-header">
              <h1 class="modal-title fs-5" id="course-dashboard-template-picker-label">Pick a layout template</h1>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="${this._i18n.close}"></button>
            </div>
            <div class="modal-body">
              <div id="course-dashboard-template-picker-instruction">${this._i18n.template_picker_instruction}</div>

              <div id="course-dashboard-template-picker-template-block">
                <div class=${this.data.template === 1 ? "course-dashboard-template-picker-selected" : ""}>
                  <a href="javascript:;" @click=${this.templateSelected} data-template="1">
                    <img data-template="1" src="${this.data.courseTemplate1ThumbnailUrl}" class="thumbnail" alt="${this._i18n.layout1_alt}" />
                  </a>
                  <h2>${this._i18n.option1}</h2>
                </div>
                <div class=${this.data.template === 2 ? "course-dashboard-template-picker-selected" : ""}>
                  <a href="javascript:;" @click=${this.templateSelected} data-template="2">
                    <img data-template="2" src="${this.data.courseTemplate2ThumbnailUrl}" class="thumbnail" alt="${this._i18n.layout2_alt}" />
                  </a>
                  <h2>${this._i18n.option2}</h2>
                </div>
                <div class=${this.data.template === 3 ? "course-dashboard-template-picker-selected" : ""}>
                  <a href="javascript:;" @click=${this.templateSelected} data-template="3">
                    <img data-template="3" src="${this.data.courseTemplate3ThumbnailUrl}" class="thumbnail" alt="${this._i18n.layout3_alt}" />
                  </a>
                  <h2>${this._i18n.option3}</h2>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-primary" data-bs-dismiss="modal">${this._i18n.close}</button>
            </div>
          </div>
        </div>
      </div>

      <div class="course-dashboard-container mt-2">
        ${this.data.template === 1 ? this.template1() : nothing }
        ${this.data.template === 2 ? this.template2() : nothing }
        ${this.data.template === 3 ? this.template3() : nothing }
      </div>
    `;
  }
}
