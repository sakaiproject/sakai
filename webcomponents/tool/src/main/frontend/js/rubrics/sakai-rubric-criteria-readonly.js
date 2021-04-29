import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {tr} from "./sakai-rubrics-language.js";

export class SakaiRubricCriteriaReadonly extends RubricsElement {

  constructor() {

    super();
    this.criteria;
  }

  static get properties() {

    return {
      criteria: { type: Array },
      weighted: Boolean};
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
                ${this.weighted ? html`
                    <div class="criterion-weight">
                        <span>
                          <sr-lang key="weight">Weight</sr-lang>
                        </span>
                        <span>${c.weight}</span>
                        <span>
                          <sr-lang key="percent_sign">%</sr-lang>
                        </span>
                    </div>`
                  : ""
                }
              </div>
              <div class="criterion-ratings">
                <div class="cr-table">
                  <div class="cr-table-row">
                  ${c.ratings.map(r => html`
                    <div tabindex="0" title="${tr("rating_title")}: ${r.title}. ${tr("rating_description")}: ${r.description}. ${tr("point_value")}: ${r.points.toLocaleString(this.locale)}" class="rating-item" id="rating_item_${r.id}">
                      <h5 class="criterion-item-title">${r.title}</h5>
                      <p>${r.description}</p>
                      <span class="points">
                        ${this.weighted && r.points > 0 ? html`
                            <b>
                              (${(r.points * (c.weight / 100)).toFixed(2)})
                            </b>`
                          : ""
                        }
                        ${r.points.toLocaleString(this.locale)}
                        <sr-lang key="points">Points</sr-lang>
                      </span>
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

if (!customElements.get("sakai-rubric-criteria-readonly", SakaiRubricCriteriaReadonly)) {
  customElements.define("sakai-rubric-criteria-readonly", SakaiRubricCriteriaReadonly);
}
