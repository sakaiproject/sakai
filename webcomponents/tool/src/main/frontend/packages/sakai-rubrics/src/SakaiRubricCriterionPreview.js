import { RubricsElement } from "./RubricsElement.js";
import { html, nothing } from "lit";
import { unsafeHTML } from "lit/directives/unsafe-html.js";

export class SakaiRubricCriterionPreview extends RubricsElement {

  static properties = {

    criteria: { type: Array },
    weighted: { type: Boolean },
  };

  shouldUpdate() {
    return this.criteria && super.shouldUpdate();
  }

  render() {

    return html`
      <div class="criterion grading style-scope sakai-rubric-criterion-preview">
        ${this.criteria && this.criteria.map(c => html`
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
                    <span>${this._i18n.weight}</span>
                    <span>${c.weight.toLocaleString(this.locale)}</span>
                    <span>${this._i18n.percent_sign}</span>
                  </div>
                ` : nothing }
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
                        : nothing }
                        ${r.points.toLocaleString(this.locale)}
                        ${this._i18n.points}
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
}
