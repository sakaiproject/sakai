import {SakaiElement} from "/webcomponents/sakai-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";

export class SakaiRubricCriterionReadonly extends SakaiElement {

  constructor() {
    super();

    this.token = "";
    this.rubric = {};
  }

  static get properties() {

    return {
      token: String,
      rubric: { type: Object, observer: 'rubricChanged' },
    };
  }

  render() {

    html`
      <div class="criterion style-scope sakai-rubric-criterion-readonly">
        <div id="sort-criterion">
          ${this.rubric.criterions.map(c => html`
            <div id="criterion_row_${c.id}" class="criterion-row">
              <div tabindex="0" class="criterion-detail">
                <h4 class="criterion-title">
                  ${c.title}
                </h4>
                <p>
                  ${c.description}
                </p>
              </div>
              <div class="criterion-ratings">
                <div class="cr-table">
                  <div class="cr-table-row">
                    ${c.ratings.map(r => html`
                      <div tabindex="0" title$="Rating Title: ${r.title}. Rating Description: ${r.description}. Point Value: ${r.points}" class="rating-item" id="rating_item_${r.id}" on-save-ratings="saveRatings" @on-delete-rating="${this.deleteCriterionRating}">
                        <h5 class="criterion-item-title">
                          ${r.title}
                        </h5>
                        <p>
                          ${r.description}
                        </p>
                        <span class="points">
                          ${r.points} Points
                        </span>
                      </div>
                    `)}
                  </div>
                </div>
              </div>
              <div class="criterion-actions">

              </div>
            </div>
          `)}
        </div>
      </div>
    `;
  }

  rubricChanged() {
    this.set('criterions', this.rubric.criterions);
  }

  ready() {
    this.criterions = this.rubric.criterions;
  }
}

customElements.define("sakai-rubric-criterion-readonly", SakaiRubricCriterionReadonly);
