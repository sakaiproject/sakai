import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";

export class SakaiRubricCriterionPreview extends RubricsElement {

  static get properties() {

    return {
      criteria: { type: Array }, weighted: Boolean
    };
  }

  render() {

    return html`
      <div class="criterion grading style-scope sakai-rubric-criterion-preview">
        ${this.criteria.map(c => html`
          <div id="criterion_row_${c.id}" class="criterion-row">
            <div class="criterion-detail">
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
                  <div class="rating-item" id="rating_item_${r.id}" >
                    <h5 class="criterion-item-title">${r.title}</h5>
                    <div class="div-description">
                      <p>${r.description}</p>
                    </div>
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
    `;
  }
}

customElements.define("sakai-rubric-criterion-preview", SakaiRubricCriterionPreview);
