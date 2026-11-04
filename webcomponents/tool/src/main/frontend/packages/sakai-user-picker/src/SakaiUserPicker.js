import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { css, html, nothing } from "lit";
import { live } from "lit/directives/live.js";

const normalize = value => value.toLocaleLowerCase().normalize("NFD")
  .replace(/\p{Mark}/gu, "").replaceAll(",", " ").trim();

export class SakaiUserPicker extends SakaiShadowElement {

  static properties = {
    options: { attribute: false },
    values: { attribute: false },
    multiple: { type: Boolean },
    bulkActions: { attribute: "bulk-actions", type: Boolean },
    disabled: { type: Boolean },
    label: { type: String },
    placeholder: { type: String },
    _query: { state: true },
    _open: { state: true },
    _active: { state: true },
  };

  constructor() {
    super();
    this.options = [];
    this.values = [];
    this.multiple = false;
    this.disabled = false;
    this._query = "";
    this._open = false;
    this._active = -1;
    this._translations = this.loadTranslations("user-picker");
  }

  /**
   * Bind a native form field with a fixed option list and disabled state.
   * Selection stays synchronized on change and form reset.
   */
  bindSelect(select) {
    const syncSelection = () => {
      this.values = Array.from(select.selectedOptions, option => option.value).filter(Boolean);
    };
    this.options = Array.from(select.options).filter(option => option.value).map(option => ({
      id: option.value,
      name: option.dataset.name || option.text,
      username: option.dataset.username,
      group: option.closest("optgroup")?.label,
      disabled: option.disabled || !!option.closest("optgroup")?.disabled,
    }));
    this.multiple = select.multiple;
    this.disabled = select.disabled;
    this.label = select.labels?.[0]?.textContent.trim() || select.title;
    syncSelection();
    this.addEventListener("picker-change", () => {
      for (const option of select.options) {
        option.selected = this.values.includes(option.value);
      }
      select.dispatchEvent(new Event("change", { bubbles: true }));
    });
    select.addEventListener("change", syncSelection);
    // Native reset restores the defaults after the reset event.
    select.form?.addEventListener("reset", () => setTimeout(syncSelection, 0));
    select.labels?.[0]?.addEventListener("click", event => {
      if (select.hidden) {
        event.preventDefault();
        this.renderRoot.querySelector("input")?.focus();
      }
    });
    this.hidden = true;
    select.after(this);
    // The native select remains usable if the custom element fails to load.
    this._translations.then(() => this.updateComplete).then(() => {
      if (this._i18n) {
        select.hidden = true;
        select.style.display = "none";
        this.hidden = false;
      }
    });
  }

  willUpdate(changedProperties) {
    const optionsChanged = this._indexedOptions !== this.options;
    if (optionsChanged) {
      this._indexedOptions = this.options;
      this._optionsById = new Map(this.options.map(option => [ option.id, option ]));
      this._nameCounts = new Map();
      this._searchIndex = this.options.map(option => {
        this._nameCounts.set(option.name, (this._nameCounts.get(option.name) ?? 0) + 1);
        return { option, text: normalize([ option.name, option.username ].filter(Boolean).join(" ")) };
      });
    }
    if (optionsChanged || changedProperties.has("_query")) {
      const terms = normalize(this._query).split(/\s+/u).filter(Boolean);
      this._matches = this._searchIndex.filter(({ text }) => terms.every(term => text.includes(term)))
        .map(({ option }) => option);
    }
  }

  _change(values, keepResults = false) {
    this.values = values;
    if (!keepResults) {
      this._query = "";
      this._active = -1;
    }
    this.renderRoot.querySelector("input").focus();
    this._open = keepResults;
    this.dispatchEvent(new CustomEvent("picker-change", {
      detail: { values: [ ...values ] }, bubbles: true, composed: true,
    }));
  }

  _choose(option) {
    if (option.disabled) { return; }
    this._change(this.multiple
      ? this.values.includes(option.id) ? this.values.filter(value => value !== option.id) : [ ...this.values, option.id ]
      : [ option.id ], this.multiple);
  }

  _setMatchingSelection(selected) {
    const values = new Set(this.values);
    for (const option of this._matches) {
      if (option.disabled) { continue; }
      if (selected) {
        values.add(option.id);
      } else {
        values.delete(option.id);
      }
    }
    this._change([ ...values ], true);
  }

  _key(event, choices) {
    if (event.isComposing) { return; }
    if (event.key === "Escape" && this._open) {
      event.preventDefault();
      event.stopPropagation();
      this._open = false;
      this._active = -1;
    } else if (event.key === "ArrowDown" || event.key === "ArrowUp") {
      event.preventDefault();
      this._open = true;
      const direction = event.key === "ArrowDown" ? 1 : -1;
      let index = this._active < 0 && direction === -1 ? choices.length : this._active;
      do { index += direction; } while (choices[index]?.disabled);
      if (choices[index]) {
        this._active = index;
        this.updateComplete.then(() => this.renderRoot.getElementById(`option-${index}`)?.scrollIntoView({ block: "nearest" }));
      }
    } else if (event.key === "Enter" && this._open) {
      event.preventDefault();
      const choice = choices[this._active] || (this._query.trim() ? choices.find(option => !option.disabled) : undefined);
      if (choice) { this._choose(choice); }
    }
  }

  shouldUpdate() {
    return !!this._i18n;
  }

  render() {
    const matches = this._matches;
    const choices = matches.slice(0, 100);
    const selected = this.values.map(value => this._optionsById.get(value)).filter(Boolean);
    const selectedIds = new Set(this.values);
    const bulkActions = this.multiple && this.bulkActions;
    const compact = bulkActions && selected.length > 3;
    const noResults = this.options.length ? this._i18n.no_results : this._i18n.no_options;
    const truncated = matches.length > choices.length;
    const resultStatus = truncated ? this.tr("truncated_results", { shown: choices.length, total: matches.length })
      : choices.length === 1 ? this._i18n.one_result
        : choices.length ? this.tr("result_count", [ choices.length ]) : noResults;
    const placeholder = this.placeholder || this._i18n.search;
    const label = this.label || placeholder;
    const open = this._open && !this.disabled;
    const groups = new Map();
    choices.forEach((option, index) => {
      const options = groups.get(option.group) || [];
      options.push({ option, index });
      groups.set(option.group, options);
    });

    return html`
      <div @focusout=${event => {
      if (!this.renderRoot.contains(event.relatedTarget)) {
        this._open = false;
        this._active = -1;
      }
    }}>
        <div class="field">
          ${compact ? html`<span class="selection-summary">${this.tr("selected_count", [ selected.length ])}</span>` : selected.map(option => {
      const duplicateName = option.username && this._nameCounts.get(option.name) > 1;
      const identity = option.username ? `${option.name} (${option.username})` : option.name;
      return html`
        <button class="chip" type="button" ?disabled=${this.disabled}
            aria-label=${this.tr("remove", [ identity ])}
            @click=${() => this._change(this.values.filter(value => value !== option.id))}>
          ${option.name}${duplicateName ? html` <small>(${option.username})</small>` : nothing}
          <span aria-hidden="true">×</span>
        </button>`;
    })}
          <input type="text" role="combobox" autocomplete="off" aria-autocomplete="list"
              aria-label=${label} aria-controls="options" aria-expanded=${open}
              aria-activedescendant=${open && choices[this._active] ? `option-${this._active}` : nothing}
              placeholder=${placeholder} ?disabled=${this.disabled} .value=${live(this._query)}
              @focus=${() => { this._open = true; }} @click=${() => { this._open = true; }}
              @input=${event => {
      this._query = event.target.value;
      this._active = -1;
      this._open = true;
    }}
              @keydown=${event => this._key(event, choices)}>
        </div>
        <div class="results" ?hidden=${!open}>
          ${bulkActions && choices.length ? html`
            <div class="matching-actions">
              <button type="button" @click=${() => this._setMatchingSelection(true)}>${this._i18n.select_matching}</button>
              <button type="button" @click=${() => this._setMatchingSelection(false)}>${this._i18n.clear_matching}</button>
            </div>` : nothing}
          ${truncated || !choices.length ? html`<p>${resultStatus}</p>` : nothing}
          <div id="options" role="listbox" aria-label=${label}
              aria-multiselectable=${this.multiple ? "true" : nothing}>
            ${Array.from(groups, ([ group, options ]) => html`
              <div role=${group ? "group" : "presentation"} aria-label=${group || nothing}>
                ${group ? html`<div class="group" aria-hidden="true">${group}</div>` : nothing}
                ${options.map(({ option, index }) => {
      const picked = selectedIds.has(option.id);
      return html`
                    <div role="option" id=${`option-${index}`} class=${index === this._active ? "active" : ""}
                        aria-selected=${picked} aria-disabled=${option.disabled ? "true" : nothing}
                        @mousedown=${event => event.preventDefault()} @click=${() => this._choose(option)}>
                      <span>${option.name}</span>
                      ${option.username ? html`<small>${option.username}</small>` : nothing}
                      ${picked ? html`<span class="check" aria-hidden="true">✓</span>` : nothing}
                    </div>`;
    })}
              </div>`)}
          </div>
        </div>
        <span class="sr" role="status">${open ? resultStatus : this.tr("selected_count", [ selected.length ])}</span>
      </div>`;
  }

  static styles = css`
    :host([hidden]) { display: none; }
    :host { display: block; color: var(--sakai-text-color-1); }
    .field, .results {
      background: var(--sakai-background-color-1, white);
      border: 1px solid var(--sakai-border-color, #767676);
      border-radius: .375rem;
    }
    .field { display: flex; flex-wrap: wrap; align-items: center; gap: .375rem; padding: .375rem; }
    input {
      flex: 1 1 var(--sakai-user-picker-input-basis, 12rem);
      min-inline-size: 0;
      border: 0;
      padding: .375rem;
      background: transparent;
      color: inherit;
      font: inherit;
    }
    .selection-summary { padding-inline: .375rem; }
    .matching-actions {
      display: flex;
      flex-wrap: wrap;
      gap: .375rem;
      padding: .375rem;
      border-block-end: 1px solid var(--sakai-border-color, #767676);
    }
    .matching-actions button {
      border: 0;
      border-radius: .25rem;
      padding: .375rem;
      background: transparent;
      color: inherit;
      font: inherit;
    }
    .matching-actions button:hover { background: var(--sakai-background-color-2, #eee); }
    .chip {
      max-inline-size: 100%;
      overflow-wrap: anywhere;
      padding: .25rem .5rem;
      border-radius: .375rem;
      border: 1px solid var(--sakai-border-color, #767676);
      color: var(--infoBanner-color, #174f78);
      background: var(--infoBanner-bgcolor, #e6f2fa);
      font: inherit;
    }
    .chip span { margin-inline-start: .25rem; }
    .results { margin-block-start: .25rem; max-block-size: 15rem; overflow: auto; }
    [role="option"] { position: relative; padding: .5rem 2rem .5rem .75rem; cursor: pointer; overflow-wrap: anywhere; }
    [role="option"]:hover, .active { background: var(--sakai-background-color-2, #eee); }
    [aria-disabled="true"] { opacity: .65; cursor: default; }
    [role="option"] small { display: block; }
    .group { padding: .375rem .75rem; font-weight: 600; }
    .check { position: absolute; inset-inline-end: .75rem; }
    p { margin: 0; padding: .5rem .75rem; }
    :focus-visible { outline: 2px solid var(--focus-outline-color, #005fcc); outline-offset: 2px; }
    .sr { position: absolute; inline-size: 1px; block-size: 1px; overflow: hidden; clip-path: inset(50%); }
  `;
}
