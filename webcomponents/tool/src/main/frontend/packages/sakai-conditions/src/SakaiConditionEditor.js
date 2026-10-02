import { html, nothing } from "lit";
import { repeat } from "lit/directives/repeat.js";
import { ConditionElement } from "./ConditionElement.js";
import { createCondition, deleteCondition, getConditionsForItem } from "./conditions-api.js";
import { CONDITION_OPERATORS, ConditionType, formatOperator, nonParentConditionFilter, nonRootConditionFilter } from "./condition-utils.js";
import "../sakai-condition-text.js";

export class SakaiConditionEditor extends ConditionElement {

  static properties = {
    labelCreateCondition: { attribute: "label-create-condition", type: String },
    labelExistingConditions: { attribute: "label-existing-conditions", type: String },
    _conditions: { state: true },
    _operator: { state: true },
    _argument: { state: true },
    _removing: { state: true },
  };

  constructor() {
    super();
    this._conditions = [];
    this._operator = "GREATER_THAN";
    this._argument = "";
  }

  async _loadData(context) {
    this._conditions = [];
    const conditions = await getConditionsForItem(this.siteId, this.toolId, this.itemId);
    if (conditions === null) { throw new Error("Unable to retrieve item conditions"); }
    if (context === this._context) {
      this._conditions = conditions.filter(nonRootConditionFilter).filter(nonParentConditionFilter);
      this._argument = "";
      this._operator = "GREATER_THAN";
    }
  }

  get _inputValid() {
    const value = this._argument.trim();
    return value !== "" && Number.isFinite(Number(value)) && Number(value) >= 0;
  }

  async _addCondition() {
    if (!this._inputValid || this._saving || this._loading) { return; }
    const context = this._context;
    this._saving = true;
    this._error = false;
    try {
      const condition = await createCondition({
        type: ConditionType.SCORE, siteId: this.siteId, toolId: this.toolId, itemId: this.itemId,
        operator: this._operator, argument: this._argument,
      });
      if (!condition) { throw new Error("Condition was not created"); }
      if (context === this._context) {
        this._conditions = [ ...this._conditions, condition ];
        this._argument = "";
        this._operator = "GREATER_THAN";
      }
    } catch (error) {
      if (context === this._context) { this._error = true; }
      console.error("Unable to create item condition", error);
    } finally {
      this._saving = false;
    }
  }

  async _removeCondition(condition) {
    if (condition.hasParent || this._removing) { return; }
    const context = this._context;
    this._removing = condition.id;
    this._error = false;
    try {
      const deleted = await deleteCondition(condition);
      if (deleted === null) { throw new Error("Condition was not deleted"); }
      if (context === this._context) {
        this._conditions = this._conditions.filter(candidate => candidate.id !== condition.id);
      }
    } catch (error) {
      if (context === this._context) { this._error = true; }
      console.error("Unable to remove item condition", error);
    } finally {
      this._removing = null;
    }
  }

  render() {
    if (!this._i18n) { return nothing; }
    return html`${this._status()}
      <b>${this.labelCreateCondition ?? this._i18n.create_condition_for_this_item}</b>
      <div class="condition-form">
        <label>${this._i18n.form_require_item_points}
          <select class="form-select" .value=${this._operator} ?disabled=${this._saving || this._loading}
              @change=${event => { this._operator = event.target.value; }}>
            ${CONDITION_OPERATORS.map(operator => html`<option value=${operator} ?selected=${operator === this._operator}>
              ${formatOperator(this._i18n, operator)}</option>`)}
          </select>
        </label>
        <label><input type="text" class="form-control argument" aria-label=${this._i18n.points}
            .value=${this._argument} ?disabled=${this._saving || this._loading}
            @input=${event => { this._argument = event.target.value; }}>${this._i18n.points}</label>
        <button type="button" class="btn btn-primary" @click=${this._addCondition}
            ?disabled=${!this._inputValid || this._saving || this._loading}>
          ${this._i18n[this._saving ? "saving_condition" : "add_condition"]}
        </button>
      </div>
      ${this._conditions.length ? html`<div class="conditions">
        <b>${this.labelExistingConditions ?? this._i18n.existing_conditions_for_this_item}</b>
        ${repeat(this._conditions, condition => condition.id, condition => html`
          <div class="condition-row">
            <sakai-condition-text .condition=${condition}></sakai-condition-text>
            <span class="badge text-bg-info">${this._i18n[condition.hasParent ? "tag_in_use" : "tag_unused"]}</span>
            <button type="button" class="btn btn-secondary" ?disabled=${condition.hasParent || !!this._removing || this._loading}
                @click=${() => this._removeCondition(condition)}>
              ${this._i18n[this._removing === condition.id ? "removing_condition" : "remove_condition"]}
            </button>
          </div>`)}
      </div>` : nothing}`;
  }
}
