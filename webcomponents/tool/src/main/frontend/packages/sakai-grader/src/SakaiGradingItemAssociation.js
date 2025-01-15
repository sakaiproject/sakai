import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { html, nothing } from "lit";

export class SakaiGradingItemAssociation extends SakaiShadowElement {

  static properties = {
    gradableType: { attribute: "gradable-type", type: String },
    gradableRef: { attribute: "gradable-ref", type: String },
    siteId : { attribute: "site-id", type: String },
    gradingItemId: { state: true },
    useGrading: { attribute: "use-grading", type: Boolean },
    createGradingItem: { state: true },
    _categories: { state: true },
    _gradingItems: { state: true },
    _useCategory: { state: true },
  };

  connectedCallback() {

    super.connectedCallback();

    this.createGradingItem = !this.gradingItemId;

    this._useCategory = false;

    Promise.all([ this.loadTranslations("grader"), this._fetchItemData() ]).then(values => {

      this._i18n = values[0];

      const data = values[1];

      this._categories = data.categories;
      this._gradingItems = data.items;

      this.updateComplete.then(() => {

        if (this.gradingItemId) {
          const gradingItem = this._gradingItems.find(gi => gi.id == this.gradingItemId);
          this.renderRoot.getElementById("points").value = gradingItem.points;
          if (gradingItem.externalId != this.gradableRef) {
            this.renderRoot.getElementById("points").disabled = true;
          } else {
            this.renderRoot.getElementById("points").disabled = false;
          }
        }
      });
    });
  }

  _fetchItemData() {

    const url = `/api/sites/${this.siteId}/grading/item-data`;

    return fetch(url)
    .then(r => {

      if (r.ok) return r.json();

      throw new Error(`Network error while getting categories from ${url}`);
    })
    .catch(error => console.error(error));
  }

  focusPoints() {
    this.renderRoot.getElementById("points").focus();
  }

  _toggleGrading(e) { this.useGrading = e.target.checked; }

  _showNewItemBlock() {

    this.createGradingItem = true;
    const pointsInput = this.renderRoot.getElementById("points");
    if (pointsInput) {
      pointsInput.disabled = false;
      pointsInput.value = "";
    }
  }

  _showExistingItemBlock() {

    this.createGradingItem = false;

    this.updateComplete.then(() => {

      this.gradingItemId = this.renderRoot.getElementById("items").value;
      const gradingItem = this._gradingItems.find(gi => gi.id == this.gradingItemId);
      this.renderRoot.getElementById("points").value = gradingItem.points;
    });

    this._useCategory = false;
  }

  _itemSelected(e) {

    this.gradingItemId = e.target.value;
    const gradingItem = this._gradingItems.find(gi => gi.id == e.target.value);
    this.renderRoot.getElementById("points").value = gradingItem.points;
    this.points = gradingItem.points;
    if (gradingItem.externalId != this.gradableRef) {
      this.renderRoot.getElementById("points").disabled = true;
    } else {
      this.renderRoot.getElementById("points").disabled = false;
    }
  }

  _setUseCategory(e) {

    this._useCategory = e.target.checked;

    if (this._useCategory) {
      this.category = this.renderRoot.getElementById("categories").value;
    } else {
      this.category = undefined;
    }
  }

  _categorySelected(e) { this.category = e.target.value; }

  _inputPoints(e) { this.points = e.target.value; }

  shouldUpdate() { return this._i18n && this._gradingItems; }

  render() {

    return html`

      <label>
        <input type="checkbox" @click=${this._toggleGrading} .checked=${this.useGrading} />
        ${this._i18n.grade_this} ${this.gradableType}
      </label>

      ${this.useGrading ? html`
        <div class="ms-4">
          <label>
            <span>${this._i18n.points}</span>
            <input id="points" type="text" @input=${this._inputPoints}>
          </label>
        </div>
        <div class="ms-4 mt-3">
          <div>
            <label>
              <input id="create" type="radio" name="new-or-existing-item" @click=${this._showNewItemBlock} checked />
              ${this._i18n.create_new_item}
            </label>
            ${this.createGradingItem ? html`
              <div class="ms-4">
                ${this._categories?.length ? html`
                  <div class="mt-2">
                    <label>
                      <input id="use-categories-checkbox" type="checkbox" @click=${this._setUseCategory} />
                      ${this._i18n.pick_category}
                    </label>
                    <select id="categories" aria-label="${this._i18n.select_category_label}"  @change=${this._categorySelected} ?disabled=${!this._useCategory}>
                      ${this._categories.map(c => html`
                        <option value="${c.id}">${c.name}</option>
                      `)}
                    </select>
                  </div>
                ` : nothing}
              </div>
            ` : nothing}
          </div>
          <div class="mt-2">
            <label>
              <input id="associate" type="radio" name="new-or-existing-item" @click=${this._showExistingItemBlock} .checked=${this.gradingItemId}>
              ${this.tr("associate_with_existing", [ this.gradableType ])}
            </label>
            ${!this.createGradingItem ? html`
            <div class="ms-4">
              ${this._gradingItems?.length ? html`
                <div>
                <label>
                  ${this._i18n.select_existing}
                  <select id="items" @change=${this._itemSelected}>
                    ${this._gradingItems.map(i => html`
                      <option value="${i.id}" ?selected=${this.gradingItemId == i.id}>${i.name}</option>
                    `)}
                  </select>
                </label>
                </div>
              ` : nothing}
            </div>
            ` : nothing}
          </div>
        </div>
      ` : nothing}
    `;
  }
}
