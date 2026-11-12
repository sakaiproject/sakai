import { html, nothing } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-widgets/sakai-widget-panel.js";
import "../sakai-course-header.js";
import "../sakai-course-overview.js";

const HERO_FULL = "full";
const HERO_WIDE = "wide";
const HERO_NARROW = "narrow";

export class SakaiCourseDashboard extends SakaiElement {

  static properties = {

    state: String,
    userId: { attribute: "user-id", type: String },
    siteId: { attribute: "site-id", type: String },
    editing: { type: Boolean },

    data: { state: true },
    _userWidgetFilters: { state: true },
    _availableWidgetCount: { state: true },
    _heroAutoHeight: { state: true },
  };

  constructor() {

    super();

    this._availableWidgetCount = 0;
    this.loadTranslations({
      bundle: "dashboard",
      requiredKeys: [
        "hero_title",
        "hero_width",
        "hero_width_full",
        "hero_width_wide",
        "hero_width_narrow",
        "show_image",
        "widgets_available",
      ],
    });
  }

  connectedCallback() {

    super.connectedCallback();

    this._loadData();
  }

  disconnectedCallback() {

    this._heroResizeObserver?.disconnect();
    this._heroResizeObserver = undefined;
    this._heroObservedHero = undefined;
    super.disconnectedCallback();
  }

  updated() {

    this._syncHeroGridPlacement();
  }

  get _layoutEditable() {
    return this.data?.layoutEditable !== false;
  }

  get _siteEditable() {
    return Boolean(this.data?.siteEditable);
  }

  get _canEdit() {
    return this._layoutEditable || this._siteEditable;
  }

  _loadData() {

    const siteUrl = `/api/sites/${this.siteId}/dashboard`;
    const userFiltersUrl = this.userId ? `/api/users/${this.userId}/dashboard` : null;

    const sitePromise = fetch(siteUrl, { credentials: "include" })
      .then(r => {

        if (r.ok) {
          return r.json();
        }
        throw new Error(`Failed to get dashboard data from ${siteUrl}`);
      });

    const filtersPromise = userFiltersUrl
      ? fetch(userFiltersUrl, { credentials: "include" })
        .then(r => (r.ok ? r.json() : null))
        .catch(() => null)
      : Promise.resolve(null);

    Promise.all([ sitePromise, filtersPromise ])
      .then(([ siteData, userData ]) => {

        this.data = {
          ...siteData,
          heroWidth: this._normalizeHeroWidth(siteData.heroWidth),
          showImage: siteData.showImage !== false,
          layoutEditable: siteData.layoutEditable !== false,
          siteEditable: Boolean(siteData.siteEditable),
        };
        this._userWidgetFilters = userData?.widgetFilters;
        this._updateAvailableWidgetCount();
      })
      .catch(error => console.error(error));
  }

  _normalizeHeroWidth(value) {

    if (value === HERO_FULL || value === HERO_WIDE || value === HERO_NARROW) {
      return value;
    }
    return HERO_FULL;
  }

  _updateAvailableWidgetCount() {

    if (!this.data?.widgets || !this.data?.widgetLayout) {
      this._availableWidgetCount = 0;
      return;
    }
    const inLayout = new Set((this.data.widgetLayout || []).map(t => (typeof t === "string" ? t : t?.id)).filter(Boolean));
    this._availableWidgetCount = (this.data.widgets || []).filter(id => id && !inLayout.has(id)).length;
  }

  widgetLayoutChanged(e) {

    this.data = { ...this.data, widgetLayout: e.detail.layout };
    if (typeof e.detail.availableCount === "number") {
      this._availableWidgetCount = e.detail.availableCount;
    } else {
      this._updateAvailableWidgetCount();
    }
  }

  _availableWidgetsChanged(e) {

    if (typeof e.detail?.count === "number") {
      this._availableWidgetCount = e.detail.count;
    }
  }

  overviewChanged(e) {

    this.data = { ...this.data, overview: e.detail.content };
  }

  edit() {

    this.editing = !this.editing;
    this.imageBackup = this.data.image;
    this.overviewBackup = this.data.overview;
    this.programmeBackup = this.data.programme;
    this.widgetLayoutBackup = structuredClone(this.data.widgetLayout);
    this.heroWidthBackup = this.data.heroWidth;
    this.showImageBackup = this.data.showImage;
  }

  cancel() {

    this.editing = false;
    if (this.data.image !== this.imageBackup) {
      URL.revokeObjectURL(this.data.image);
    }
    this.data = {
      ...this.data,
      overview: this.overviewBackup,
      programme: this.programmeBackup,
      widgetLayout: structuredClone(this.widgetLayoutBackup),
      heroWidth: this.heroWidthBackup,
      showImage: this.showImageBackup,
      image: this.imageBackup,
    };
    this._updateAvailableWidgetCount();
    this.newImageBlob = undefined;
    const programmeEl = document.getElementById("course-dashboard-programme");
    if (programmeEl) {
      programmeEl.textContent = this.data.programme ?? "";
    }
  }

  save() {

    this.editing = !this.editing;

    const imageBlob = this.newImageBlob;
    this.newImageBlob = undefined;

    if (this._siteEditable && imageBlob) {

      const fd = new FormData();
      fd.append("siteImage", imageBlob);

      const imageUrl = `/api/sites/${this.siteId}/image`;
      fetch(imageUrl, {
        method: "POST",
        body: fd,
      }).then(r => {

        if (!r.ok) {
          throw new Error(`Failed to update image for url ${imageUrl}`);
        }
        URL.revokeObjectURL(this.data.image);
        return r.text();
      })
      .then(newUrl => {

        this.data = { ...this.data, image: `${newUrl}?version=${Math.random()}` };
      })
      .catch(error => console.error(error.message));
    }

    const data = {
      widgetLayout: this.data.widgetLayout,
      heroWidth: this._normalizeHeroWidth(this.data.heroWidth),
    };

    if (this._siteEditable) {
      data.overview = this.data.overview;
      data.programme = this.data.programme;
      data.showImage = Boolean(this.data.showImage);
    }

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

    this.data = { ...this.data, image: e.detail.url };
    this.newImageBlob = e.detail.blob;
    this.querySelector("sakai-course-header")?.requestUpdate();
  }

  programmeUpdated(e) {
    this.data.programme = e.target.innerText;
  }

  _heroWidthChanged(e) {

    this.data = {
      ...this.data,
      heroWidth: this._normalizeHeroWidth(e.target.value),
    };
  }

  _showImageChanged(e) {

    this.data = { ...this.data, showImage: e.target.checked };
  }

  shouldUpdate() {
    return this._i18n && this.data;
  }

  _widgetsAvailableLabel() {

    const count = this._availableWidgetCount;
    if (count <= 0) {
      return nothing;
    }
    const template = this._i18n.widgets_available || "{0} widgets available";
    return html`
      <span class="dashboard-widgets-available text-muted small">
        ${template.replace("{0}", String(count))}
      </span>
    `;
  }

  titleBlock() {

    return html`
      <div id="course-dashboard-title-and-edit-block">
        <div id="course-dashboard-title-block">
          <h2>${this.data.title}</h2>
          <div id="course-dashboard-programme"
              @input=${this.programmeUpdated}
              ?contenteditable=${this.editing && this._siteEditable}>${this.data.programme}</div>
        </div>
        <div id="course-dashboard-edit-block">
          ${this._widgetsAvailableLabel()}
          ${this._canEdit ? html`
            ${this.editing ? html`
              <button type="button"
                  id="course-dashboard-save"
                  class="btn btn-primary"
                  @click=${this.save}
                  title="${this._i18n.save_tooltip}"
                  aria-label="${this._i18n.save_tooltip}">
                ${this._i18n.save}
              </button>
              <button type="button"
                  id="course-dashboard-cancel"
                  class="btn btn-secondary"
                  @click=${this.cancel}
                  title="${this._i18n.cancel_tooltip}"
                  aria-label="${this._i18n.cancel_tooltip}">
                ${this._i18n.cancel}
              </button>
            ` : html`
              <button type="button"
                  id="course-dashboard-edit"
                  class="btn btn-icon"
                  @click=${this.edit}
                  title="${this._i18n.edit_tooltip}"
                  aria-label="${this._i18n.edit_tooltip}">
                <span class="si si-edit fs-5" aria-hidden="true"></span>
              </button>
            `}
          ` : nothing}
        </div>
      </div>
    `;
  }

  widgetPanel() {

    const heroWidth = this._normalizeHeroWidth(this.data?.heroWidth);

    return html`
      <sakai-widget-panel
        id="course-dashboard-widget-grid"
        hero-width="${heroWidth}"
        @changed=${this.widgetLayoutChanged}
        @available-changed=${this._availableWidgetsChanged}
        .widgetIds=${this.data.widgets}
        .layout=${this.data.widgetLayout}
        .widgetFilters=${this._userWidgetFilters}
        ?grades-reveal-on-click=${this.data.gradesRevealOnClick !== false}
        site-id="${this.siteId}"
        user-id="${this.userId}"
        ?editing=${this.editing && this._layoutEditable}>
        ${this._heroBlock()}
      </sakai-widget-panel>
    `;
  }

  _syncHeroGridPlacement() {

    const hero = this.querySelector(".course-dashboard-hero");
    if (!hero) {
      this._heroResizeObserver?.disconnect();
      this._heroObservedHero = undefined;
      return;
    }

    if (!this._heroResizeObserver) {
      this._heroResizeObserver = new ResizeObserver(() => {
        if (this._heroObservedHero) {
          this._applyHeroRowSpan(this._heroObservedHero);
        }
      });
    }

    if (this._heroObservedHero !== hero) {
      this._heroResizeObserver.disconnect();
      this._heroObservedHero = hero;
      this._heroResizeObserver.observe(hero);
    }

    this._applyHeroRowSpan(hero);
  }

  _applyHeroRowSpan(hero) {

    const width = this._normalizeHeroWidth(this.data?.heroWidth);
    const panel = this.querySelector("sakai-widget-panel");
    const autoHeight = typeof panel?.heroFillsRow === "function"
      ? panel.heroFillsRow()
      : (width === HERO_FULL || width === HERO_WIDE);
    if (this._heroAutoHeight !== autoHeight) {
      this._heroAutoHeight = autoHeight;
    }

    if (autoHeight) {
      if (hero.style.gridColumn !== "1 / -1") {
        hero.style.gridColumn = "1 / -1";
      }
      if (hero.style.gridRow) {
        hero.style.gridRow = "";
      }
      return;
    }

    const col = width === HERO_WIDE ? "span 2" : "span 1";
    let span = 1;
    const prevHeight = hero.style.height;
    const prevMaxHeight = hero.style.maxHeight;
    const prevOverflow = hero.style.overflow;
    const prevRow = hero.style.gridRow;
    hero.style.height = "max-content";
    hero.style.maxHeight = "none";
    hero.style.overflow = "visible";
    hero.style.gridRow = "span 1";
    const height = hero.getBoundingClientRect().height;
    hero.style.height = prevHeight;
    hero.style.maxHeight = prevMaxHeight;
    hero.style.overflow = prevOverflow;
    hero.style.gridRow = prevRow;

    const styles = panel ? getComputedStyle(panel) : null;
    const rowHeight = parseFloat(styles?.getPropertyValue("--sakai-widget-panel-row-height")) || 420;
    const gap = parseFloat(styles?.getPropertyValue("--sakai-widget-panel-gutter-width")) || 16;
    const unit = rowHeight + gap;
    span = Math.max(1, Math.ceil((height + gap) / unit));
    const row = `span ${span}`;

    if (hero.style.gridColumn !== col) {
      hero.style.gridColumn = col;
    }
    if (hero.style.gridRow !== row) {
      hero.style.gridRow = row;
    }
  }

  _overviewEmpty() {

    if (this.editing && this._siteEditable) {
      return false;
    }
    if (!this.data?.overview) {
      return true;
    }
    const text = String(this.data.overview).replace(/<[^>]*>/g, "").replace(/&nbsp;/gi, " ").trim();
    return text.length === 0;
  }

  _heroVisible() {

    if (this.editing) {
      return true;
    }
    return this.data.showImage || !this._overviewEmpty();
  }

  _heroChrome() {

    if (!this.editing) {
      return nothing;
    }

    const width = this._normalizeHeroWidth(this.data.heroWidth);

    return html`
      <div class="course-dashboard-hero-chrome">
        <div class="course-dashboard-hero-chrome-title">${this._i18n.hero_title}</div>
        <div class="course-dashboard-hero-chrome-controls">
          ${this._layoutEditable ? html`
            <label class="course-dashboard-hero-control">
              <span class="course-dashboard-hero-control-label">${this._i18n.hero_width}</span>
              <select class="form-select form-select-sm"
                  .value=${width}
                  @change=${this._heroWidthChanged}
                  aria-label="${this._i18n.hero_width}">
                <option value="${HERO_WIDE}" ?selected=${width === HERO_WIDE}>${this._i18n.hero_width_wide}</option>
                <option value="${HERO_FULL}" ?selected=${width === HERO_FULL}>${this._i18n.hero_width_full}</option>
                <option value="${HERO_NARROW}" ?selected=${width === HERO_NARROW}>${this._i18n.hero_width_narrow}</option>
              </select>
            </label>
          ` : nothing}
          ${this._siteEditable ? html`
            <div class="form-check course-dashboard-hero-control">
              <input class="form-check-input"
                  type="checkbox"
                  id="course-dashboard-show-image"
                  .checked=${Boolean(this.data.showImage)}
                  @change=${this._showImageChanged}>
              <label class="form-check-label" for="course-dashboard-show-image">${this._i18n.show_image}</label>
            </div>
          ` : nothing}
        </div>
      </div>
    `;
  }

  _heroImageCssUrl() {

    if (!this.data?.image) {
      return "";
    }
    const escaped = String(this.data.image).replace(/\\/g, "\\\\").replace(/"/g, "\\\"");
    return `url("${escaped}")`;
  }

  _heroBlock() {

    if (!this._heroVisible()) {
      return nothing;
    }

    const showImage = Boolean(this.data.showImage);
    const overviewEmpty = this._overviewEmpty();
    const heroWidth = this._normalizeHeroWidth(this.data.heroWidth);
    const editSite = this.editing && this._siteEditable;
    const heroImageUrl = showImage ? this._heroImageCssUrl() : "";

    return html`
      <section slot="hero"
          class="course-dashboard-hero"
          data-hero-width="${heroWidth}"
          ?data-show-image=${showImage}
          ?data-overview-empty=${overviewEmpty}
          ?data-hero-auto-height=${this._heroAutoHeight}>
        ${this._heroChrome()}
        <div class="course-dashboard-hero-media"
            style="${heroImageUrl ? `--course-dashboard-hero-image: ${heroImageUrl}` : ""}">
          ${showImage ? html`
            <div class="course-dashboard-hero-image">
              <div class="course-dashboard-media-block course-dashboard-image-block">
                <sakai-course-header .site=${this.data} @image-edited=${this.imageEdited} ?editing=${editSite}></sakai-course-header>
              </div>
            </div>
          ` : nothing}
          ${overviewEmpty ? nothing : html`
            <div class="course-dashboard-hero-overview">
              <div class="course-dashboard-media-block course-dashboard-overview-block">
                <sakai-course-overview @changed=${this.overviewChanged}
                    overview="${this.data.overview || ""}"
                    ?editing=${editSite}>
                </sakai-course-overview>
              </div>
            </div>
          `}
        </div>
      </section>
    `;
  }

  render() {

    const heroWidth = this._normalizeHeroWidth(this.data.heroWidth);
    const showImage = Boolean(this.data.showImage);
    const overviewEmpty = this._overviewEmpty();

    return html`
      <div class="course-dashboard-container mt-2"
          data-hero-width="${heroWidth}"
          ?data-show-image=${showImage}
          ?data-overview-empty=${overviewEmpty}
          ?data-has-hero=${this._heroVisible()}>
        ${this.titleBlock()}
        ${this.widgetPanel()}
      </div>
    `;
  }
}
