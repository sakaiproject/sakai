import { html, nothing } from "lit";
import { repeat } from "lit/directives/repeat.js";
import { ConditionElement } from "./ConditionElement.js";
import { createCondition, getRootCondition, getToolItemsWithConditionsForLesson, updateCondition } from "./conditions-api.js";
import { ConditionType, ConditionOperator, formatConditionText, makeParentCondition, makeRootCondition, plainText } from "./condition-utils.js";
import "../sakai-condition-text.js";

export class SakaiConditionPicker extends ConditionElement {

  static properties = {
    lessonId: { attribute: "lesson-id", type: Number },
    _toolItems: { state: true },
    _root: { state: true },
    _selected: { state: true },
    _conjunction: { state: true },
  };

  constructor() {
    super();
    this._toolItems = [];
    this._selected = "";
    this._conjunction = ConditionOperator.AND;
  }

  async _loadData(context) {
    this._toolItems = [];
    this._root = null;
    const [ items, root ] = await Promise.all([
      getToolItemsWithConditionsForLesson(this.siteId, this.lessonId),
      getRootCondition(this.siteId, this.toolId, this.itemId),
    ]);
    if (items === null) { throw new Error("Unable to retrieve lesson conditions"); }
    if (context === this._context) {
      this._toolItems = items;
      this._root = root;
      this._selected = "";
      this._conjunction = ConditionOperator.AND;
    }
  }

  get _availableItems() {
    return this._toolItems.filter(item => String(item.id) !== this.itemId);
  }

  _parent(operator) {
    return this._root?.subConditions?.find(condition => condition.type === ConditionType.PARENT && condition.operator === operator);
  }

  get _savedIds() {
    return new Set((this._root?.subConditions ?? []).filter(parent => parent.type === ConditionType.PARENT).flatMap(parent => parent.subConditions ?? []).map(condition => condition.id));
  }

  async _addSubCondition() {
    const condition = this._availableItems.flatMap(item => item.conditions).find(candidate => candidate.id === this._selected);
    if (!condition || this._savedIds.has(condition.id) || this._saving || this._loading) { return; }
    const context = this._context;
    const operator = this._conjunction;
    this._saving = true;
    this._error = false;
    try {
      let root = this._root;
      if (!root) {
        root = await createCondition(makeRootCondition(this.siteId, this.toolId, this.itemId));
        if (!root) { throw new Error("Root condition was not created"); }
        root = { ...root, subConditions: root.subConditions ?? [] };
        if (context === this._context) { this._root = root; }
      }
      const existingParent = root.subConditions.find(candidate => candidate.type === ConditionType.PARENT && candidate.operator === operator);
      const parent = existingParent ?? await createCondition(makeParentCondition(root.siteId, operator));
      if (!parent) { throw new Error("Parent condition was not created"); }
      if (!existingParent) {
        const updatedRoot = await updateCondition({ ...root, subConditions: [ ...root.subConditions, parent ] });
        if (!updatedRoot) { throw new Error("Root condition was not updated"); }
        root = updatedRoot;
        if (context === this._context) { this._root = root; }
      }
      const updatedParent = await updateCondition({ ...parent, subConditions: [ ...(parent.subConditions ?? []), condition ] });
      if (!updatedParent) { throw new Error("Prerequisite was not added"); }
      if (context === this._context) {
        this._root = { ...root, subConditions: root.subConditions.map(candidate => candidate.id === parent.id ? updatedParent : candidate) };
        this._selected = "";
        this._conjunction = ConditionOperator.AND;
      }
    } catch (error) {
      if (context === this._context) { this._error = true; }
      console.error("Unable to add prerequisite condition", error);
    } finally {
      this._saving = false;
    }
  }

  async _removeSubCondition(parent, condition) {
    if (this._saving || this._loading) { return; }
    const context = this._context;
    const root = this._root;
    this._saving = true;
    this._error = false;
    try {
      const updated = await updateCondition({ ...parent, subConditions: parent.subConditions.filter(candidate => candidate.id !== condition.id) });
      if (!updated) { throw new Error("Prerequisite was not removed"); }
      if (context === this._context) {
        this._root = { ...root, subConditions: root.subConditions.map(candidate => candidate.id === parent.id ? updated : candidate) };
      }
    } catch (error) {
      if (context === this._context) { this._error = true; }
      console.error("Unable to remove prerequisite condition", error);
    } finally {
      this._saving = false;
    }
  }

  _renderPrerequisites(operator) {
    const parent = this._parent(operator);
    if (!parent?.subConditions?.length) { return nothing; }
    return html`<div class="conditions">
      <b>${this._i18n[operator === ConditionOperator.AND ? "existing_prereq_conditions_and" : "existing_prereq_conditions_or"]}</b>
      ${repeat(parent.subConditions, condition => condition.id, condition => html`
        <div class="condition-row">
          <sakai-condition-text .condition=${condition}
              .item=${this._toolItems.find(item => String(item.id) === String(condition.itemId))?.name}></sakai-condition-text>
          <button type="button" class="btn btn-secondary" ?disabled=${this._saving || this._loading}
              @click=${() => this._removeSubCondition(parent, condition)}>${this._i18n.remove_condition}</button>
        </div>`)}
    </div>`;
  }

  render() {
    if (!this._i18n) { return nothing; }
    const available = this._availableItems.some(item => item.conditions.some(condition => !this._savedIds.has(condition.id)));
    return html`${this._status()}
      ${available ? html`
        <b>${this._i18n.pick_condition_as_prereq}</b>
        <div class="condition-form">
          <label>${this._i18n.condition}
            <select class="form-select" .value=${this._selected} ?disabled=${this._saving || this._loading}
                @change=${event => { this._selected = event.target.value; }}>
              <option value=""></option>
              ${this._availableItems.map(item => html`<optgroup label=${plainText(item.name)}>
                ${item.conditions.map(condition => html`<option value=${condition.id}
                    ?selected=${condition.id === this._selected} ?disabled=${this._savedIds.has(condition.id)}>
                  ${plainText(formatConditionText(this._i18n, condition, item.name))}</option>`)}
              </optgroup>`)}
            </select>
          </label>
          <label>${this._i18n.conjunction}
            <select class="form-select" .value=${this._conjunction} ?disabled=${this._saving || this._loading}
                @change=${event => { this._conjunction = event.target.value; }}>
              ${[ ConditionOperator.AND, ConditionOperator.OR ].map(operator => html`
                <option value=${operator} ?selected=${operator === this._conjunction}>
                  ${this._i18n[`conjunction_${operator.toLowerCase()}`]}</option>`)}
            </select>
          </label>
          <button type="button" class="btn btn-primary" ?disabled=${!this._selected || this._saving || this._loading}
              @click=${this._addSubCondition}>${this._i18n[this._saving ? "saving_condition_as_prereq" : "add_condition_as_prereq"]}</button>
        </div>` : this._loading ? nothing : html`<div class="alert alert-info">${this._i18n.no_condition_to_pick}</div>`}
      ${this._renderPrerequisites(ConditionOperator.AND)}
      ${this._renderPrerequisites(ConditionOperator.OR)}`;
  }
}
