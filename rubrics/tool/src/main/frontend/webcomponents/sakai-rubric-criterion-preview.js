import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";

export class SakaiRubricCriterionPreview extends RubricsElement {

  static get properties() {

    return {
      criteria: { type: Array },
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
                      ${r.points.toLocaleString(this.locale)} <sr-lang key="points">Points</sr-lang>
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
