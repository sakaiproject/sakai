import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { css, html, nothing } from "lit";

/** Shared context and translation lifecycle for the Lessons condition controls. */
export class ConditionElement extends SakaiShadowElement {

  static properties = {
    siteId: { attribute: "site-id", type: String },
    toolId: { attribute: "tool-id", type: String },
    itemId: { attribute: "item-id", type: String },
    _loading: { state: true },
    _saving: { state: true },
    _error: { state: true },
  };

  constructor() {
    super();
    this._loading = true;
    this._saving = false;
    this._error = false;
    this.loadTranslations("condition");
  }

  get _context() {
    return JSON.stringify([ this.siteId, this.toolId, this.itemId, this.lessonId ]);
  }

  updated(changed) {
    if ([ "siteId", "toolId", "itemId", "lessonId" ].some(name => changed.has(name))) {
      this._load();
    }
  }

  async _load() {
    if (!this.siteId || !this.toolId || !this.itemId) { return; }
    const context = this._context;
    this._loading = true;
    this._error = false;
    try {
      await this._loadData(context);
    } catch (error) {
      if (this._context === context) {
        this._error = true;
        console.error("Unable to load item conditions", error);
      }
    } finally {
      if (this._context === context) { this._loading = false; }
    }
  }

  _status() {
    return html`
      ${this._loading ? html`<span role="status">${this._i18n.loading}</span>` : nothing}
      ${this._error ? html`<p role="alert">${this._i18n.operation_failed}
        <button type="button" @click=${this._load}>${this._i18n.retry}</button></p>` : nothing}`;
  }

  static styles = [ ...SakaiShadowElement.styles, css`
    :host { display: block; }
    .argument { width: 5em; }
    .condition-form { display: flex; flex-wrap: wrap; align-items: center; gap: .5rem; }
    .condition-form label { display: flex; align-items: center; gap: .5rem; }
    .condition-form select { width: auto; }
    .conditions { margin-top: .5rem; }
    .condition-row { display: flex; align-items: center; gap: .5rem; padding: .5rem;
      border: 1px solid var(--sakai-border-color); }
    .condition-row sakai-condition-text { flex: 1; }
    button { font: inherit; }
  ` ];
}
