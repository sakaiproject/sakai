import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {unsafeHTML} from "/webcomponents/assets/lit-html/directives/unsafe-html.js";

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
          ${this.isCriterionGroup(c) ? html`
            <div id="criterion_row_${c.id}" class="criterion-row criterion-group">
              <div class="criterion-detail">
                <h4 class="criterion-title">${c.title}</h4>
                <p>${unsafeHTML(c.description)}</p>
              </div>
            </div>
          ` : html`
            <div id="criterion_row_${c.id}" class="criterion-row">
              <div class="criterion-detail">
                <h4 class="criterion-title">${c.title}</h4>
                <p>${unsafeHTML(c.description)}</p>
                ${this.weighted ? html`
                    <div class="criterion-weight">
                        <span>
                          <sr-lang key="weight">Weight</sr-lang>
                        </span>
                        <span>${c.weight.toLocaleString(this.locale)}</span>
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
                              (${parseFloat((r.points * (c.weight / 100)).toFixed(2)).toLocaleString(this.locale)})
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
          `}
        `)}
      </div>
    `;
  }

  isCriterionGroup(criterion) {
    return criterion.ratings.length === 0;
  }
}

customElements.define("sakai-rubric-criterion-preview", SakaiRubricCriterionPreview);
