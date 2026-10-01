import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { css, html, nothing } from "lit";
import { repeat } from "lit/directives/repeat.js";
import { getRubricElement, updateAdhocRubric, updateEvaluation } from "./dynamic-rubrics-api.js";
import "../sakai-dynamic-criterion.js";

export class SakaiDynamicRubric extends SakaiShadowElement {

  static properties = {
    gradingId: { attribute: "grading-id", type: String },
    entityId: { attribute: "entity-id", type: String },
    previousGrade: { attribute: "previous-grade", type: Number },
    siteId: { attribute: "site-id", type: String },
    evaluatedItemOwnerId: { attribute: "evaluated-item-owner-id", type: String },
    origin: { type: String },
    _criteria: { state: true },
    _editing: { state: true },
    _loading: { state: true },
    _busy: { state: true },
    _error: { state: true },
  };

  constructor() {
    super();
    this.previousGrade = 0;
    this._criteria = [];
    this._editing = false;
    this._loading = true;
    this._baseGrade = 0;
    this._nextId = 1;
    this._translations = this.loadTranslations("dynamic-rubric");
  }

  get _context() {
    return JSON.stringify([ this.siteId, this.entityId, this.gradingId, this.evaluatedItemOwnerId, this.previousGrade ]);
  }

  updated(changed) {
    if ([ "siteId", "entityId", "gradingId", "evaluatedItemOwnerId", "previousGrade" ].some(name => changed.has(name))) {
      this._load();
    }
  }

  async _load() {
    if (!this.siteId || !this.entityId || !this.gradingId || !this.evaluatedItemOwnerId) { return; }
    const context = this._context;
    this._loading = true;
    this._error = false;
    this._editing = false;
    this._rubric = null;
    this._association = null;
    this._criteria = [];
    const base = `/api/sites/${encodeURIComponent(this.siteId)}`;
    try {
      await this._translations;
      const association = await getRubricElement(`${base}/rubric-associations/tools/sakai.samigo/items/${encodeURIComponent(this.entityId)}`);
      if (!association) { throw new Error("Rubric association not found"); }
      const [ rubric, evaluation ] = await Promise.all([
        getRubricElement(`${base}/rubrics/${encodeURIComponent(association.rubricId)}`),
        getRubricElement(`${base}/rubric-evaluations/tools/sakai.samigo/items/${encodeURIComponent(this.entityId)}/evaluations/${encodeURIComponent(this.gradingId)}/owners/${encodeURIComponent(this.evaluatedItemOwnerId)}`),
      ]);
      if (!rubric) { throw new Error("Dynamic rubric not found"); }
      if (context !== this._context) { return; }
      this._association = association;
      this._rubric = rubric;
      this._evaluation = evaluation ?? { criterionOutcomes: [] };
      this._criteria = rubric.criteria.map(criterion => ({
        id: this._nextId++, pointsVal: Number(criterion.ratings[0].points).toFixed(2), description: criterion.title,
        storedId: criterion.id, storedRatingId: criterion.ratings[0].id, selected: false,
      }));
      if (!this._criteria.length) { this._addRow(); }
      this._storedCriteria = this._projectCriteria();
      this.cancel();
    } catch (error) {
      if (context === this._context) {
        this._error = true;
        console.error("Unable to load dynamic rubric", error);
      }
    } finally {
      if (context === this._context) { this._loading = false; }
    }
  }

  _projectCriteria() {
    return this._criteria.map(criterion => ({
      title: criterion.description, id: criterion.storedId,
      ratings: [ { title: "-", points: Number(criterion.pointsVal).toFixed(2), id: criterion.storedRatingId } ],
    }));
  }

  get _updated() {
    return !!this._storedCriteria && JSON.stringify(this._projectCriteria()) !== JSON.stringify(this._storedCriteria);
  }

  get _calculatedPoints() {
    const partial = this._criteria.filter(criterion => criterion.selected).reduce((total, criterion) => total + Number(criterion.pointsVal), 0);
    return Math.max(this._baseGrade + partial, 0).toFixed(2);
  }

  _addRow() {
    this._criteria = [ ...this._criteria, {
      id: this._nextId++, pointsVal: "1.00", description: this._i18n.criterion_default,
      storedId: null, storedRatingId: null, selected: false,
    } ];
  }

  _criterionChanged(event) {
    this._criteria = this._criteria.map(criterion => criterion.id === event.detail.id ? { ...event.detail } : criterion);
  }

  _criterionSelected(event) {
    this._criteria = this._criteria.map(criterion => criterion.id === event.detail.id ? { ...criterion, selected: event.detail.selected } : criterion);
  }

  _criterionRemoved(event) {
    this._criteria = this._criteria.filter(criterion => criterion.id !== event.detail.id);
  }

  /** Restore the saved evaluation when Samigo's grading dialog is cancelled. */
  cancel() {
    if (!this._evaluation) { return; }
    const selectedIds = new Set(this._evaluation.criterionOutcomes.filter(outcome => outcome.selectedRatingId !== null).map(outcome => outcome.criterionId));
    this._criteria = this._criteria.map(criterion => ({ ...criterion, selected: selectedIds.has(criterion.storedId) }));
    this._baseGrade = this.previousGrade - this._criteria.filter(criterion => criterion.selected).reduce((total, criterion) => total + Number(criterion.pointsVal), 0);
  }

  /** Persist the selected criteria when Samigo's grading dialog is accepted. */
  async release() {
    if (this._loading || this._busy || !this._association) { return; }
    this._busy = true;
    this._error = false;
    const context = this._context;
    const evaluation = {
      evaluatorId: window.top?.portal?.user?.id,
      id: this._evaluation.id,
      evaluatedItemId: this.gradingId,
      evaluatedItemOwnerId: this.evaluatedItemOwnerId,
      evaluatedItemOwnerType: "USER",
      overallComment: this._calculatedPoints,
      criterionOutcomes: this._criteria.map(criterion => ({
        criterionId: criterion.storedId, points: criterion.pointsVal,
        selectedRatingId: criterion.selected ? criterion.storedRatingId : null,
      })),
      associationId: this._association.id,
      status: 2,
      ...(this._evaluation.id ? { metadata: this._evaluation.metadata } : {}),
    };
    const base = `/api/sites/${encodeURIComponent(this.siteId)}/rubric-evaluations`;
    try {
      const saved = await updateEvaluation(this._evaluation.id ? `${base}/${encodeURIComponent(this._evaluation.id)}` : base,
        evaluation, this._evaluation.id ? "PUT" : "POST");
      if (saved && context === this._context) { this._evaluation = saved; }
      return saved;
    } catch (error) {
      if (context === this._context) { this._error = true; }
      console.error("Unable to save dynamic rubric evaluation", error);
    } finally {
      this._busy = false;
    }
  }

  async _confirmChanges() {
    if (this._busy || !window.confirm(this._i18n.confirm_recalculation)) { return; }
    this._busy = true;
    this._error = false;
    try {
      const criteria = this._projectCriteria();
      const ratings = rows => rows.map(({ id, ratings: values }) => ({ id, ratings: values }));
      const pointsUpdated = JSON.stringify(ratings(criteria)) !== JSON.stringify(ratings(this._storedCriteria));
      const saved = await updateAdhocRubric(`/api/sites/${encodeURIComponent(this.siteId)}/rubrics/adhoc${pointsUpdated ? "?pointsUpdated=true" : ""}`,
        { ...this._rubric, criteria });
      if (saved) {
        const [ publishedId, itemId ] = this._rubric.title.replace("pub.", "").split(".");
        const url = new URL(this.origin, window.location.href);
        url.search = new URLSearchParams({ resetCache: "true", publishedId, itemId }).toString();
        window.location.href = url.href;
      }
    } catch (error) {
      this._error = true;
      console.error("Unable to update dynamic rubric", error);
    } finally {
      this._busy = false;
    }
  }

  render() {
    if (!this._i18n) { return nothing; }
    return html`
      ${this._loading ? html`<span role="status">${this._i18n.loading}</span>` : nothing}
      ${this._error ? html`<p role="alert">${this._i18n.operation_failed}
        <button type="button" @click=${this._load}>${this._i18n.retry}</button></p>` : nothing}
      ${!this._loading && this._rubric ? html`
        <div class="rubric-actions">
          ${this._editing ? html`
            <button type="button" class="btn btn-primary" ?disabled=${this._busy} @click=${this._confirmChanges}>${this._i18n.confirm_changes}</button>
            <button type="button" class="btn btn-primary" ?disabled=${this._busy} @click=${this._addRow}>${this._i18n.add_criterion}</button>
            <button type="button" class="btn btn-primary" ?disabled=${this._busy} @click=${this._load}>${this._i18n.cancel_changes}</button>
          ` : html`<button type="button" class="btn btn-primary" @click=${() => { this._editing = true; }}>${this._i18n.edit_criterions}</button>`}
        </div>
        ${repeat(this._criteria, criterion => criterion.id, (criterion, index) => html`
          <sakai-dynamic-criterion .crit=${criterion} .idpos=${index} .saving=${this._editing} .disabled=${this._busy}
              @criterion-changed=${this._criterionChanged} @criterion-selected=${this._criterionSelected}
              @criterion-removed=${this._criterionRemoved}></sakai-dynamic-criterion>`)}
        <input type="hidden" name="updated${this.gradingId}" .value=${String(this._updated)}>
        <input type="hidden" name="newtotal${this.gradingId}" .value=${this._calculatedPoints}>
        <input type="hidden" name="previous${this.gradingId}" .value=${String(this.previousGrade)}>
      ` : nothing}`;
  }

  static styles = [ ...SakaiShadowElement.styles, css`
    :host { display: block; }
    .rubric-actions { display: flex; flex-wrap: wrap; gap: 5px; margin-bottom: 8px; }
  ` ];
}
