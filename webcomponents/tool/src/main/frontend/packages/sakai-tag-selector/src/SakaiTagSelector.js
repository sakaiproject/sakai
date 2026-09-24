import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { css, html, nothing } from "lit";

/** Shared tag selection. input-id names the host form's comma-separated hidden input. */
export class SakaiTagSelector extends SakaiShadowElement {

  static properties = {
    siteId: { attribute: "site-id" },
    tool: { type: String },
    collectionId: { attribute: "collection-id" },
    itemId: { attribute: "item-id" },
    selectedTemp: { attribute: "selected-temp" },
    extraOptions: { attribute: "extra-options" },
    inputId: { attribute: "input-id" },
    addNew: { attribute: "add-new", converter: value => value !== null && value !== "false" },
    _options: { state: true },
    _value: { state: true },
    _query: { state: true },
    _open: { state: true },
    _active: { state: true },
    _loading: { state: true },
    _error: { state: true },
  };

  constructor() {
    super();
    this._options = [];
    this._value = [];
    this._query = "";
    this._open = false;
    this._active = -1;
    this.addNew = false;
    this.loadTranslations("tag-selector");
  }

  updated(changed) {
    if ([ "siteId", "tool", "collectionId", "itemId", "selectedTemp" ].some(p => changed.has(p))) {
      this._initializeSelection();
    }
  }

  connectedCallback() {
    super.connectedCallback();
    if (this._loading) { this._initializeSelection(); }
  }

  disconnectedCallback() {
    this._request?.abort();
    super.disconnectedCallback();
  }

  get selectedTags() {
    return this._value.map(tag => ({ ...tag }));
  }

  clear() {
    this._request?.abort();
    this._loading = false;
    this._query = "";
    this._value = [];
    this._publish();
  }

  async _initializeSelection() {
    if (!this.siteId || !this.tool || !this.collectionId) { return; }
    this._request?.abort();
    const request = this._request = new AbortController();
    this._loading = true;
    this._error = false;
    this._open = false;
    const url = `/api/sites/${encodeURIComponent(this.siteId)}/tools/${encodeURIComponent(this.tool)}/tags/${encodeURIComponent(this.collectionId)}`;
    const readTags = async path => {
      const response = await fetch(path, { signal: request.signal });
      if (!response.ok) { throw new Error(`Unable to load tags: ${response.status}`); }
      return (await response.json()).map(tag => ({ name: tag.tagLabel, code: tag.tagId }));
    };
    try {
      const [ options, saved ] = await Promise.all([
        readTags(url),
        !this.selectedTemp && this.itemId && this.addNew
          ? readTags(`${url}/items/${encodeURIComponent(this.itemId)}`) : Promise.resolve([]),
      ]);
      if (request.signal.aborted) { return; }
      this._options = options;
      this._value = this.selectedTemp
        ? [ ...new Set(this.selectedTemp.split(",").filter(s => s.trim())) ]
          .map(code => this._options.find(tag => tag.code === code) || { name: code, code })
        : saved;
      this._publish();
    } catch (error) {
      if (!request.signal.aborted) {
        this._error = true;
        console.error(error);
      }
    } finally {
      if (!request.signal.aborted) { this._loading = false; }
    }
  }

  _publish() {
    const input = this.ownerDocument.getElementById(this.inputId);
    if (input) { input.value = this._value.map(tag => tag.code).join(","); }
    this.dispatchEvent(new CustomEvent("tags-changed", {
      detail: { value: this.selectedTags }, bubbles: true, composed: true,
    }));
  }

  get _availableOptions() {
    const extras = (this.extraOptions || "").split(",").filter(label => label.trim())
      .map(label => ({ name: label, code: label }));
    const options = new Map(this._options.map(tag => [ tag.code, tag ]));
    for (const tag of [ ...extras, ...this._value ]) {
      if (!options.has(tag.code)) { options.set(tag.code, tag); }
    }
    return [ ...options.values() ];
  }

  get _choices() {
    const query = this._query.trim().toLowerCase();
    const options = this._availableOptions;
    const choices = options.filter(tag => tag.name.toLowerCase().includes(query));
    const label = this._query.replaceAll(",", "").trim();
    if (this.addNew && label && !options.some(tag => tag.name.toLowerCase() === label.toLowerCase() || tag.code === label)) {
      choices.push({ name: label, code: label, create: true });
    }
    return choices;
  }

  _selectTag(tag) {
    if (this._value.some(value => value.code === tag.code)) {
      this._removeTag(tag);
      return;
    }
    const value = { name: tag.name, code: tag.code };
    if (tag.create) {
      this._options = [ ...this._options, value ];
    }
    this._updateSelection([ ...this._value, value ]);
  }

  _removeTag(tag) {
    this._updateSelection(this._value.filter(value => value.code !== tag.code));
  }

  _updateSelection(value) {
    this._value = value;
    this._query = "";
    this._active = -1;
    this._publish();
    this.shadowRoot.querySelector("input").focus();
    this._open = false;
  }

  _focusout(event) {
    if (!event.relatedTarget || !this.shadowRoot.contains(event.relatedTarget)) {
      this._open = false;
    }
  }

  _input(event) {
    this._query = event.target.value;
    this._active = -1;
    this._open = true;
  }

  _keydown(event) {
    if (event.isComposing) { return; }
    if (event.key === "Escape") {
      event.preventDefault();
      this._open = false;
      this._active = -1;
    } else if (event.key === "ArrowDown" || event.key === "ArrowUp") {
      event.preventDefault();
      this._open = true;
      const count = this._choices.length;
      if (count) {
        this._active = this._active < 0
          ? (event.key === "ArrowDown" ? 0 : count - 1)
          : (this._active + (event.key === "ArrowDown" ? 1 : -1) + count) % count;
        this.updateComplete.then(() => this.shadowRoot.getElementById(`option-${this._active}`)?.scrollIntoView({ block: "nearest" }));
      }
    } else if (event.key === "Enter") {
      event.preventDefault();
      const choice = this._choices[this._active] || (this._query.trim() ? this._choices[0] : undefined);
      if (choice) { this._selectTag(choice); }
    }
  }

  render() {
    if (!this._i18n) { return nothing; }
    const choices = this._choices;
    const label = this.addNew ? this._i18n.search_or_add : this._i18n.search_filter;
    return html`
      <div class="picker" @focusout=${this._focusout}>
        <div class="selector" aria-busy=${Boolean(this._loading)}>
          ${this._value.map(tag => html`
            <button class="tag" type="button" aria-label="${this._i18n.deselect}: ${tag.name}"
                ?disabled=${this._loading || this._error}
                @click=${() => this._removeTag(tag)}>
              ${tag.name} <span aria-hidden="true">×</span>
            </button>
          `)}
          <input id="search" type="text" role="combobox" autocomplete="off"
              aria-label=${label} placeholder=${label} aria-autocomplete="list"
              aria-expanded=${this._open} aria-controls="options"
              aria-activedescendant=${this._open && this._active >= 0 ? `option-${this._active}` : nothing}
              ?disabled=${this._loading || this._error} .value=${this._query}
              @focus=${() => { this._open = true; }}
              @click=${() => { this._open = true; }}
              @input=${this._input}
              @keydown=${this._keydown}>
        </div>
        <ul id="options" role="listbox" aria-label=${label} aria-multiselectable="true" ?hidden=${!this._open}>
          ${choices.map((tag, index) => html`
            <li id="option-${index}" role="option" aria-selected=${this._value.some(value => value.code === tag.code)}
                class=${index === this._active ? "active" : ""}
                @mousedown=${event => event.preventDefault()} @click=${() => this._selectTag(tag)}>
              ${tag.create ? html`${this._i18n.add_new}: ${tag.name}` : tag.name}
              ${this._value.some(value => value.code === tag.code) ? html`<span aria-hidden="true"> ✓</span>` : nothing}
            </li>
          `)}
          ${!choices.length ? html`
            <li role="presentation">
              <span role="status">
                ${this._availableOptions.length ? this._i18n.no_results : this._i18n.no_options}
              </span>
            </li>
          ` : nothing}
        </ul>
        ${this._loading ? html`<p role="status">${this._i18n.loading}</p>` : nothing}
        ${this._error ? html`
          <p role="alert">
            ${this._i18n.load_error}
            <button type="button" @click=${this._initializeSelection}>${this._i18n.retry}</button>
          </p>
        ` : nothing}
        <span class="status" role="status">
          ${this._value.length
            ? `${this._i18n.selected}: ${this._value.map(tag => tag.name).join(", ")}`
            : this._i18n.none_selected}
        </span>
      </div>
    `;
  }

  static styles = css`
    :host {
      display: block;
      min-width: 12rem;
      font: inherit;
      color: var(--sakai-text-color-1, #262626);
    }

    .picker {
      position: relative;
    }

    .selector {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: .3rem;
      padding: .3rem;
      border: 1px solid var(--sakai-border-color, #767676);
      border-radius: .25rem;
      background: var(--sakai-background-color-1, white);
    }

    input {
      flex: 1;
      min-width: 10rem;
      padding: .3rem;
      border: 0;
      font: inherit;
      color: inherit;
      background: transparent;
    }

    button {
      font: inherit;
      cursor: pointer;
    }

    .tag {
      padding: .15rem .5rem;
      border: 1px solid var(--sakai-border-color, #767676);
      border-radius: 1rem;
      background: var(--infoBanner-bgcolor, #e6f2fa);
      color: var(--infoBanner-color, #174f78);
    }

    :focus-visible {
      outline: 2px solid var(--focus-outline-color, #005fcc);
      outline-offset: 2px;
    }

    ul {
      position: absolute;
      top: 100%;
      left: 0;
      right: 0;
      z-index: 1000;
      max-height: 14rem;
      overflow-y: auto;
      margin: 0;
      padding: 0;
      list-style: none;
      border: 1px solid var(--sakai-border-color, #767676);
      background: var(--sakai-background-color-1, white);
    }

    li {
      padding: .5rem;
      cursor: pointer;
    }

    li:hover, li.active {
      background: var(--sakai-background-color-2, #eee);
    }

    li[aria-selected="true"] {
      font-weight: bold;
    }

    p {
      margin: .3rem 0;
    }

    .status {
      position: absolute;
      width: 1px;
      height: 1px;
      overflow: hidden;
      clip-path: inset(50%);
    }

  `;
}
