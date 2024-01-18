import { RubricsElement } from "./RubricsElement.js";
import { html } from "lit";
import { rubricsApiMixin } from "./SakaiRubricsApiMixin.js";

/**
 * @property {string} siteId
 * @property {string} toolId
 * @property {string} entityId
 * @property {string} summaryType
 */
export class SakaiRubricSummary extends rubricsApiMixin(RubricsElement) {

  static properties = {

    siteId: { attribute: "site-id", type: String },
    toolId: { attribute: "tool-id", type: String },
    entityId: { attribute: "entity-id", type: String },
    summaryType: { attribute: "summary-type", type: String },

    _criteria: { state: true },
    _rubric: { state: true },
    _allEvaluations: { state: true },
    _averages: { state: true },
    _averageScore: { state: true },
  };

  constructor() {

    super();

    this.summaryType = "student";
  }

  connectedCallback() {

    super.connectedCallback();

    this._criteria = [];
    if (this.isConnected) {

      if (this.siteId && this.toolId && this.entityId) {
        this._getAssociation();
        this._getAllEvaluations();
      }
    }
  }

  render() {

    if (this.summaryType === "criteria") {
      return html`
        <div class="d-flex justify-content-between align-items-center mb-2">
          <h3>${this._i18n.criteria_summary}</h3>
          <div class="collapse-toggle-buttons">
            <button type="button" class="btn btn-link" @click=${this._expandAll}>${this._i18n.expand_all}</button>
            <button type="button" class="btn btn-link" @click=${this._collapseAll}>${this._i18n.collapse_all}</button>
          </div>
        </div>
        ${!this._allEvaluations?.length ? html`
          <div class="sak-banner-warn">${this._i18n.no_evaluations_warning}</div>
        ` : html`
          ${this._criteria.map(c => html`
            <div class="mb-2">
              <div class="card">
                <div class="card text-center bg-light">
                  <h4>
                    <a class="collapse-toggle collapsed" data-bs-toggle="collapse" href="#collapse${c.id}">${c.title}</a>
                  </h4>
                </div>
                <div id="collapse${c.id}" class="collapse">
                  <div class="card-body">
                    <div class="table">
                      <table class="rubrics-summary-table table table-bordered table-sm">
                        <tr>
                          ${c.ratings.map(r => html`
                            <th class="rubrics-summary-table-cell">
                                <div>
                                    ${this._rubric.weighted ? html`
                                      (${r.weightedPoints})        
                                    ` : html``}
                                    ${r.points} 
                                    ${this._i18n.points}
                                  </div>
                                <div class="summary-rating-name" title="${r.title}">${this._limitCharacters(r.title, 20)}</div>
                            </th>
                            ${this.association.parameters.fineTunePoints && this._getCustomCount(c.id, r.weightedPoints) > 0 ? html`
                              <th class="rubrics-summary-table-cell">${this._i18n.adjusted_score}</th>
                            ` : ""}
                          `)}
                          <th class="rubrics-summary-table-cell rubrics-summary-average-cell d-none">${this._i18n.average}</th>
                          <th  class="rubrics-summary-table-cell d-none">${this._i18n.median}</th>
                          <th  class="rubrics-summary-table-cell d-none">${this._i18n.stdev}</th>
                        </tr>
                        <tr>
                          ${c.ratings.map(r => html`
                            <td class="points-${r.points} rubrics-summary-table-cell point-cell-${c.id}">${this._getACount(c.id, r.id)}</td>
                            ${this.association.parameters.fineTunePoints && this._getCustomCount(c.id, r.weightedPoints) > 0 ? html`
                              <td class="rubrics-summary-table-cell">${this._getCustomCount(c.id, r.weightedPoints)}</td>
                            ` : html``}
                          `)}
                          <td class="rubrics-summary-table-cell rubrics-summary-average-cell d-none">${this._getPointsAverage(c.id)}</td>
                          <td class="rubrics-summary-table-cell d-none">${this._getPointsMedian(c.id)}</td>
                          <td  class="rubrics-summary-table-cell d-none">${this._getPointsStdev(c.id)}</td>
                        </tr>
                      </table>
                    </div>
                    <dl class="dl-horizontal mb-0">
                        <dt>${this._i18n.average}</dt>
                        <dd>
                            ${this._getPointsAverage(c.id)}
                        </dd>
                        <dt>${this._i18n.median}</dt>
                        <dd>
                            ${this._getPointsMedian(c.id)}
                        </dd>
                        <dt>${this._i18n.stdev}</dt>
                        <dd>
                            ${this._getPointsStdev(c.id)}
                        </dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>
          `)}
          <div>${this._i18n.adjusted_score_warning}</div>
        `}
      `;
    }

    return html`
      <h3>${this._i18n.student_summary}</h3>
      ${!this._allEvaluations?.length ? html`
      <div class="sak-banner-warn">${this._i18n.no_evaluations_warning}</div>
      ` : html`
      <div class="table">
        <table id="student-summary" class="rubrics-summary-table table table-bordered table-sm">
          <thead>
            <tr>
              <th class="rubrics-summary-table-cell rubrics-summary-table-cell-wide">${this._i18n.student_name}</th>
              ${this._criteria.map(c => html`<th class="rubrics-summary-table-cell" >${c.title}</th>`)}
              <th class="rubrics-summary-table-cell rubrics-summary-average-cell">${this._i18n.score}</th>
            </tr>
          </thead>
          <tbody>
          ${this._allEvaluations.map(e => html`
            <tr>
              <td class="rubrics-summary-table-cell rubrics-summary-table-cell-wide nameColumn">${e.sortName}</td>
              ${e.criterionOutcomes.map(o => html`
              <td class="rubrics-summary-table-cell rubrics-summary-table-cell point-cell-${o.criterionId}" >${o.points}</td>
              `)}
              <td class="rubrics-summary-table-cell rubrics-summary-average-cell point-cell-score" >${e.score}</td>
            </tr>
          `)}
          </tbody>
          <tfoot>
            <tr>
              <th class="rubrics-summary-table-cell rubrics-summary-table-cell-wide rubrics-summary-average-row " >${this._i18n.average}</th>
              ${this._criteria.map(c => html`
              <td class="rubrics-summary-table-cell rubrics-summary-average-row">${this._averages.get(c.id)}</td>
              `)}
              <td class="rubrics-summary-table-cell rubrics-summary-average-cell rubrics-summary-average-row" >${this._averageScore}</td>
            </tr>
          </tfoot>
        </table>
      </div>
      ` }
    `;
  }

  _getAssociation() {

    this.apiGetAssociation()
      .then(association => {

        this.association = association;
        this.getRubric(association.rubricId);
      })
      .catch (error => console.error(error));
  }

  getRubric(rubricId) {

    this.apiGetRubric(rubricId)
      .then(rubric => {

        this._rubric = rubric;
        this._criteria = this._rubric.criteria;
      })
      .catch (error => console.error(error));
  }

  _getAllEvaluations() {

    this.apiGetAllEvaluations()
      .then(evaluations => {

        this._allEvaluations = evaluations;
        this._averages = new Map();
        this._averageScore = 0;
        let totalScores = 0;
        this._allEvaluations.forEach(evaluation => {

          totalScores += evaluation.score;

          evaluation.criterionOutcomes.forEach(co => {

            if (!this._averages.has(co.criterionId)) {
              this._averages.set(co.criterionId, co.points);
            } else {
              this._averages.set(co.criterionId, this._averages.get(co.criterionId) + co.points);
            }
          });
        });

        this._averages.forEach((v, k, m) => m.set(k, (v / this._allEvaluations.length).toFixed(2)));
        this._averageScore = (totalScores / this._allEvaluations.length).toFixed(2);
      })
      .catch (error => console.error(error));
  }

  _limitCharacters(text, chars) {

    if (text.length > parseInt(chars)) {
      return `${text.substring(0, parseInt(chars))}...`;
    }
    return text;
  }

  _getACount(criterionId, ratingId) {

    let total = 0;
    this._allEvaluations.forEach(evaluation => {

      evaluation.criterionOutcomes.forEach(oc => {

        if (oc.criterionId === parseInt(criterionId)
            && this._doesScoreMatchRating(oc.points, criterionId, ratingId)) {
          total = total + 1;
        }
      });
    });
    return total;
  }

  _doesScoreMatchRating(score, criterionId, ratingId) {

    const criterion = this._criteria.find(c => c.id === criterionId);
    //We can always use weightedPoints because it will simply be the normal value if the rubric is not weighted.
    return criterion.ratings.some(r => r.weightedPoints === parseFloat(score) && r.id === ratingId);
  }

  _getPointsAverage(criterionId) {

    let total = 0;
    this._allEvaluations.forEach(ev => {

      ev.criterionOutcomes.forEach(oc => {

        if (oc.criterionId === criterionId) {
          total = total + oc.points;
        }
      });
    });

    return (total / this._allEvaluations.length).toFixed(2);
  }

  _getPoints(criterionId) {

    const values = [];
    this._allEvaluations.forEach(ev => {

      ev.criterionOutcomes.forEach(oc => {

        if (oc.criterionId === criterionId) {
          values.push(oc.points);
        }
      });
    });
    return values;
  }

  _getPointsMedian(criterionId) {

    const values = this._getPoints(criterionId);

    if (values.length === 0) {
      return 0;
    }
    values.sort((a, b) => a - b);
    const half = Math.floor(values.length / 2);
    if (values.length % 2) {
      return values[half];
    }
    return ((values[half - 1] + values[half]) / 2.0).toFixed(2);
  }

  _getPointsStdev(criterionId) {

    const average = this._getPointsAverage(criterionId);
    const values = this._getPoints(criterionId);
    let total = 0;
    values.forEach(v => total = total + (average - v) * (average - v));
    total = total / values.length;
    return Math.sqrt(total).toFixed(2);
  }

  _getCustomCount(criterionId, floorPointsParam) {

    let ceilingPoints = 5000;
    const criterion = this._criteria.find(c => c.id === criterionId);
    const floorPoints = parseFloat(floorPointsParam);
    criterion.ratings.every(r => {

      if (r.weightedPoints > floorPoints) {
        ceilingPoints = r.weightedPoints;
        return false;
      }
      return true;
    });
    let total = 0;
    this._allEvaluations.forEach(ev => {

      ev.criterionOutcomes.forEach(oc => {

        if (oc.criterionId === parseFloat(criterionId) && oc.points > floorPoints && oc.points < ceilingPoints) {
          total = total + 1;
        }
      });
    });
    return total;
  }

  _expandAll() {

    this.querySelectorAll(".collapse-toggle.collapsed").forEach(el => el.click());
  }

  _collapseAll() {

    Array.from(this.querySelectorAll(".collapse-toggle"))
      .filter(el => !el.classList.contains("collapsed")).forEach(el => el.click());
  }
}
