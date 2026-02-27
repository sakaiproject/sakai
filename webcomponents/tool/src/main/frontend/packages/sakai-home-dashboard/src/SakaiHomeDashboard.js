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
      template: this._data.template,
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

  _templateSelected(e) {

    this._data.template = parseInt(e.target.dataset.template);

    this.requestUpdate();

    this.updateComplete.then(() => {
      this.querySelector("#dashboard-save-button")?.focus();
    });
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
            <div id="course-dashboard-layout">
              <button type="button"
                  class="btn btn-secondary me-3"
                  data-bs-toggle="modal"
                  data-bs-target="#course-dashboard-template-picker">
                ${this._i18n.layout}
              </button>
            </div>
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

  _template1() {

    return html`

      <div class="container">
        ${this._renderHeaderBlock()}

        ${this._data.motd ? html`
          <div class="w-100 row g-0">
            <div class="col-12 mb-3 border border-1 rounded-1 fs-5 fw-normal">
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
              <div class="mt-4 ps-3 fs-6" style="display: ${this._showMotd ? "block" : "none"}">${unsafeHTML(this._data.motd)}</div>
            </div>
          </div>
        ` : nothing}
        ${this._renderWidgetPanel()}
      </div>
    `;
  }

  _template2() {

    return html`

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
            <div class="mt-4 ps-4 fs-6" style="display: ${this._showMotd ? "block" : "none"}">${unsafeHTML(this._data.motd)}</div>
          </div>
        ` : nothing}

        ${this._renderWidgetPanel()}
      </div>
    `;
  }

  _template3() {

    return html`

      <div class="container">
        ${this._renderHeaderBlock()}

        <div class="row d-lg-flex">
          <div class="${this._data.motd ? "col-3" : "d-none"}">
            ${this._data.motd ? html`
              <div class="mb-3 border border-1 rounded-1 fs-5 fw-normal">
                <div id="dashboard-motd-title-block" class="p-1">
                  <div class="me-3 fw-bold d-block d-md-none">${this._i18n.motd_short}</div>
                  <div class="me-3 fw-bold d-none d-md-block">${this._i18n.motd}</div>
                </div>
                <div class="mt-4 ps-3 fs-6" style="display: ${this._showMotd ? "block" : "none"}">
                  ${unsafeHTML(this._data.motd)}
                </div>
              </div>
            ` : nothing}
          </div>
          <div class="${this._data.motd ? "col-9" : "col-12"}">
          ${this._renderWidgetPanel()}
          </div>
        </div>
      </div>
    `;
  }


  render() {

    return html`

      <div class="modal fade" id="course-dashboard-template-picker" tabindex="-1" aria-labelledby="course-dashboard-template-picker-label" aria-hidden="true">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <div class="modal-header">
              <h1 class="modal-title fs-5" id="course-dashboard-template-picker-label">${this._i18n.layout_title}</h1>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="${this._i18n.close}"></button>
            </div>
            <div class="modal-body">
              <div id="course-dashboard-template-picker-instruction">${this._i18n.template_picker_instruction}</div>

              <div id="course-dashboard-template-picker-template-block">
                <div class="${this._data.template === 1 ? "course-dashboard-template-picker-selected" : ""} me-2">
                  <a href="javascript:;" @click=${this._templateSelected} data-template="1">
                    <img data-template="1" src="${this._data.homeTemplate1ThumbnailUrl}" class="thumbnail" alt="${this._i18n.course_layout1_alt}" />
                  </a>
                  <h2>${this._i18n.option1}</h2>
                </div>
                <div class="${this._data.template === 2 ? "course-dashboard-template-picker-selected" : ""} me-2">
                  <a href="javascript:;" @click=${this._templateSelected} data-template="2">
                    <img data-template="2" src="${this._data.homeTemplate2ThumbnailUrl}" class="thumbnail" alt="${this._i18n.course_layout2_alt}" />
                  </a>
                  <h2>${this._i18n.option2}</h2>
                </div>
                <div class="${this._data.template === 3 ? "course-dashboard-template-picker-selected" : ""}">
                  <a href="javascript:;" @click=${this._templateSelected} data-template="3">
                    <img data-template="3" src="${this._data.homeTemplate3ThumbnailUrl}" class="thumbnail" alt="${this._i18n.course_layout3_alt}" />
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

      <div class="home-dashboard-container mt-2">
        ${this._data.template === 1 ? this._template1() : nothing }
        ${this._data.template === 2 ? this._template2() : nothing }
        ${this._data.template === 3 ? this._template3() : nothing }
      </div>
    `;
  }
}
