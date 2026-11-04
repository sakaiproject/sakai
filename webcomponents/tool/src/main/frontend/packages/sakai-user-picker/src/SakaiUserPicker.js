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

  /** Preserve the existing form field and its submitted values. */
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
    select.form?.addEventListener("reset", () => queueMicrotask(syncSelection));
    select.labels?.[0]?.addEventListener("click", event => {
      if (select.hidden) {
        event.preventDefault();
        this.renderRoot.querySelector("input")?.focus();
      }
    });
    select.after(this);
    // The native select remains usable if the custom element fails to load.
    this._translations.then(() => this.updateComplete).then(() => {
      if (this._i18n) {
        select.hidden = true;
        select.style.display = "none";
      }
    });
  }

  get _matches() {
    const terms = normalize(this._query).split(/\s+/u).filter(Boolean);
    return this.options.filter(option => {
      const searchable = normalize([ option.name, option.username ].filter(Boolean).join(" "));
      return terms.every(term => searchable.includes(term));
    });
  }

  _change(values) {
    this.values = values;
    this._query = "";
    this._active = -1;
    this.renderRoot.querySelector("input").focus();
    this._open = false;
    this.dispatchEvent(new CustomEvent("picker-change", {
      detail: { values: [ ...values ] }, bubbles: true, composed: true,
    }));
  }

  _choose(option) {
    if (option.disabled) { return; }
    this._change(this.multiple
      ? this.values.includes(option.id) ? this.values.filter(value => value !== option.id) : [ ...this.values, option.id ]
      : [ option.id ]);
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
      if (choices[this._active]) { this._choose(choices[this._active]); }
    }
  }

  shouldUpdate() {
    return !!this._i18n;
  }

  render() {
    const matches = this._matches;
    const choices = matches.slice(0, 100);
    const selected = this.values.map(value => this.options.find(option => option.id === value)).filter(Boolean);
    const noResults = this.options.length ? this._i18n.no_results : this._i18n.no_options;
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
          ${selected.map(option => html`
            <button class="chip" type="button" ?disabled=${this.disabled}
                aria-label=${this._i18n.remove.replace("{}", option.username ? `${option.name} (${option.username})` : option.name)}
                @click=${() => this._change(this.values.filter(value => value !== option.id))}>
              ${option.name} <span aria-hidden="true">×</span>
            </button>`)}
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
          <div id="options" role="listbox" aria-label=${label}
              aria-multiselectable=${this.multiple ? "true" : nothing}>
            ${Array.from(groups, ([ group, options ]) => html`
              <div role=${group ? "group" : "presentation"} aria-label=${group || nothing}>
                ${group ? html`<div class="group" aria-hidden="true">${group}</div>` : nothing}
                ${options.map(({ option, index }) => {
      const picked = this.values.includes(option.id);
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
          ${!choices.length ? html`<p>${noResults}</p>` : nothing}
        </div>
        <span class="sr" role="status">${open
          ? choices.length ? this._i18n.result_count.replace("{}", matches.length) : noResults
          : this._i18n.selected_count.replace("{}", selected.length)}</span>
      </div>`;
  }

  static styles = css`
    :host { display: block; color: var(--sakai-text-color-1); }
    .field, .results {
      background: var(--sakai-background-color-1, white);
      border: 1px solid var(--sakai-border-color, #767676);
      border-radius: .375rem;
    }
    .field { display: flex; flex-wrap: wrap; align-items: center; gap: .375rem; padding: .375rem; }
    input {
      flex: 1 1 12rem;
      min-inline-size: 0;
      border: 0;
      padding: .375rem;
      background: transparent;
      color: inherit;
      font: inherit;
    }
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
    small { display: block; }
    .group { padding: .375rem .75rem; font-weight: 600; }
    .check { position: absolute; inset-inline-end: .75rem; }
    p { margin: .75rem; }
    :focus-visible { outline: 2px solid var(--focus-outline-color, #005fcc); outline-offset: 2px; }
    .sr { position: absolute; inline-size: 1px; block-size: 1px; overflow: hidden; clip-path: inset(50%); }
  `;
}
