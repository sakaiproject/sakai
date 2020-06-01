import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {tr} from "./sakai-rubrics-language.js";

export class SakaiRubricCriteriaReadonly extends RubricsElement {

  constructor() {

    super();
    this.criteria;
  }

  static get properties() {
    return { criteria: {type: Array} };
  }

  render() {

    return html`
      <div class="criterion style-scope sakai-rubric-criterion-readonly">
        <div id="sort-criterion">
          ${this.criteria.map(c => html`
            <div id="criterion_row_${c.id}" class="criterion-row">
              <div tabindex="0" class="criterion-detail">
                <h4 class="criterion-title">${c.title}</h4>
                <p>${c.description}</p>
              </div>
              <div class="criterion-ratings">
                <div class="cr-table">
                  <div class="cr-table-row">
                  ${c.ratings.map(r => html`
                    <div tabindex="0" title="${tr("rating_title")}: ${r.title}. ${tr("rating_description")}: ${r.description}. ${tr("point_value")}: ${r.points.toLocaleString(this.locale)}" class="rating-item" id="rating_item_${r.id}" on-save-ratings="saveRatings" @on-delete-rating="${this.deleteCriterionRating}">
                      <h5 class="criterion-item-title">${r.title}</h5>
                      <p>${r.description}</p>
                      <span class="points">${r.points.toLocaleString(this.locale)} ${tr("points")}</span>
                    </div>
                  `)}
                  </div>
                </div>
              </div>
            </div>
          `)}
        </div>
      </div>
    `;
  }
}

customElements.define("sakai-rubric-criteria-readonly", SakaiRubricCriteriaReadonly);
