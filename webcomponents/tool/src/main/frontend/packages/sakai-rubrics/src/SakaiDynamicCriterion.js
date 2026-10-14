import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { css, html, nothing } from "lit";
import "@sakai-ui/sakai-icon/sakai-icon.js";

/** Criterion editor. The rubric owns the model; edits and selections are events. */
export class SakaiDynamicCriterion extends SakaiShadowElement {

  static properties = {
    crit: { attribute: false },
    idpos: { type: Number },
    saving: { type: Boolean },
    disabled: { type: Boolean },
    _editing: { state: true },
    _points: { state: true },
    _description: { state: true },
  };

  constructor() {
    super();
    this._editing = false;
    this.loadTranslations("dynamic-criterion");
  }

  willUpdate(changed) {
    if (changed.has("crit") && this.crit) {
      this._points = Number(this.crit.pointsVal).toFixed(2);
      this._description = this.crit.description;
    }
    if (changed.has("saving") && !this.saving) { this._editing = false; }
  }

  _commit() {
    const points = Number(this._points);
    if (!Number.isFinite(points) || points === 0) {
      this._points = "1.00";
      window.alert(this._i18n.criterion_zero);
    }
    this.dispatchEvent(new CustomEvent("criterion-changed", {
      detail: { ...this.crit, pointsVal: Number(this._points).toFixed(2), description: this._description },
      bubbles: true, composed: true,
    }));
  }

  _select() {
    this.dispatchEvent(new CustomEvent("criterion-selected", {
      detail: { id: this.crit.id, selected: !this.crit.selected }, bubbles: true, composed: true,
    }));
  }

  _delete() {
    if (window.confirm(this._i18n.confirm_remove)) {
      this.dispatchEvent(new CustomEvent("criterion-removed", {
        detail: { id: this.crit.id }, bubbles: true, composed: true,
      }));
    }
  }

  render() {
    if (!this._i18n || !this.crit) { return nothing; }
    const color = this.crit.selected ? Number(this.crit.pointsVal) > 0 ? "success" : "danger" : "secondary";
    return html`
      <div class="criterion">
        <div class="score">
          <button type="button" class="btn btn-${color}" aria-label=${this._i18n.select_criterion}
              aria-pressed=${Boolean(this.crit.selected)} ?disabled=${this.disabled || this.saving || this._editing || !this.crit.storedId}
              @click=${this._select}>${this.idpos + 1}</button>
          ${this.saving && this._editing ? html`
            <input type="number" class="points form-control" step="any" aria-label=${this._i18n.criterion_points}
                .value=${this._points} ?disabled=${this.disabled} @input=${event => { this._points = event.target.value; }} @change=${this._commit}>
          ` : html`<span class="points">${Number(this.crit.pointsVal).toFixed(2)}</span>`}
        </div>
        <div class="description">
          ${this.saving && this._editing ? html`
            <input class="form-control" maxlength="255" aria-label=${this._i18n.criterion_description}
                .value=${this._description} ?disabled=${this.disabled} @input=${event => { this._description = event.target.value; }} @change=${this._commit}>
          ` : this.crit.description}
        </div>
        ${this.saving ? html`<div class="actions">
          <button type="button" class="btn btn-${this._editing ? "success" : "secondary"}"
              aria-label=${this._i18n[this._editing ? "confirm_criterion" : "edit_criterion"]}
              ?disabled=${this.disabled} @click=${() => {
      if (this._editing) { this._commit(); }
      this._editing = !this._editing;
    }}><sakai-icon type=${this._editing ? "check_circle" : "edit"} size="small" aria-hidden="true"></sakai-icon></button>
          <button type="button" class="btn btn-danger" aria-label=${this._i18n.remove_criterion} ?disabled=${this.disabled} @click=${this._delete}>
            <sakai-icon type="delete" size="small" aria-hidden="true"></sakai-icon>
          </button>
        </div>` : nothing}
      </div>`;
  }

  static styles = [ ...SakaiShadowElement.styles, css`
    :host { display: block; margin-bottom: .5rem; }
    .criterion { display: flex; flex-wrap: wrap; align-items: center; gap: .5rem; padding: .5rem;
      border: 1px solid var(--sakai-border-color); border-radius: 6px; background: var(--sakai-background-color-2); max-width: 1080px; }
    .score { display: flex; align-items: center; gap: 1rem; }
    .points { width: 70px; }
    .description { flex: 1; min-width: 12rem; }
    .actions { display: flex; gap: .25rem; }
  ` ];
}
