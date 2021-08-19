import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {repeat} from "/webcomponents/assets/lit-html/directives/repeat.js";
import "./sakai-rubric-edit.js";
import "./sakai-item-delete.js";
import "./sakai-rubric-criterion-edit.js";
import "./sakai-rubric-criterion-rating-edit.js";
import {SharingChangeEvent} from "./sharing-change-event.js";
import { Sortable } from "/webcomponents/assets/sortablejs/modular/sortable.esm.js";
import {tr} from "./sakai-rubrics-language.js";

export class SakaiRubricCriteria extends RubricsElement {

  static get properties() {

    return {
      token: { type: String },
      rubricId: { attribute: "rubric-id", type: String },
      weighted: { type: Boolean },
      totalWeight: { attribute: "total-weight", type: String },
      validWeight: { attribute: "valid-weight", type: Boolean },
      criteria: { type: Array }
    };
  }

  updated(changedProperties) {

    super.updated(changedProperties);

    const sortableOptions = {
      handle: ".reorder-icon",
      onUpdate: (e) => this.handleSortedCriterionRatings(e),
      draggable: ".rating-item",
      animation: "200",
    };

    this.querySelectorAll(".cr-table").forEach(cr => new Sortable(cr, sortableOptions));

    sortableOptions.onUpdate = (e) => this.handleSortedCriteria(e);
    sortableOptions.draggable = ".criterion-row";

    this.querySelectorAll(".criterion").forEach(c => new Sortable(c, sortableOptions));

    if (changedProperties.has("criteria")) {
      this.criteriaMap = new Map(this.criteria.map(c => [c.id, c]));
    }
  }

  render() {

    return html`
      <div data-rubric-id="${this.rubricId}" class="criterion style-scope sakai-rubric-criterion">
      ${repeat(this.criteria, c => c.id, c => html`
        <div id="criterion_row_${c.id}" data-criterion-id="${c.id}" class="criterion-row">
          <div class="criterion-detail">
            <h4 class="criterion-title">
              <span @focus="${this.onFocus}" @focusout="${this.focusOut}" tabindex="0" role="button" title="${tr("drag_order")}" class="reorder-icon fa fa-bars"></span>
              ${c.title}
              <sakai-rubric-criterion-edit @criterion-edited="${this.criterionEdited}" criterion="${JSON.stringify(c)}" token="${this.token}"></sakai-rubric-criterion-edit>
            </h4>
            <p>
              ${c.description}
            </p>
            ${this.weighted ? html`
                <div class="form-inline weight-field">
                    <div class="form-group input-group-sm ${this.validWeight ? "" : "has-error"}">
                      <label
                          for="weight_input_${c.id}"
                          class="control-label"
                          title="${!this.validWeight ? tr("total_weight_wrong") : ""}"
                      >
                        <sr-lang key="weight">Weight</sr-lang>
                      </label>
                      <input
                        id="weight_input_${c.id}"
                        data-criterion-id="${c.id}"
                        type="text"
                        class="form-control"
                        placeholder="0.0"
                        @input="${this.debounce(this.emitWeightChanged, 500)}"
                        value="${c.weight.toLocaleString(this.locale)}"
                        title="${!this.validWeight ? tr("total_weight_wrong") : ""}"
                      >
                      <span class="control-label"
                          title="${!this.validWeight ? tr("total_weight_wrong") : ""}"
                      >
                        <sr-lang key="percent_sign">%</sr-lang>
                      </span>
                    </div>
                </div>` : ""
              }
            <div class="add-criterion-item">
              <span tabindex="0" role="button" data-criterion-id="${c.id}" title="${tr("add_rating")} ${c.title}" class="fa fa-plus" @click="${this.addRating}" data-rating-pos="0"></span>
            </div>
          </div>
          <div class="criterion-ratings">
            <div id="cr-table-${c.id}" class="cr-table" data-criterion-id="${c.id}">
            ${repeat(c.ratings, (r) => r.id, (r, i) => html`
              <div class="rating-item" data-rating-id="${r.id}" id="rating_item_${r.id}">
                <h5 class="criterion-item-title">
                  ${r.title}
                  <sakai-rubric-criterion-rating-edit criterion-id="${c.id}" @save-rating="${this.saveRating}" @delete-rating="${this.deleteRating}" minpoints="${c.pointrange ? c.pointrange.low : 0}" maxpoints="${c.pointrange ? c.pointrange.high : 0}" rating="${JSON.stringify(r)}"></sakai-rubric-criterion-rating-edit>
                </h5>
                <div class="div-description">
                  <p>
                  ${r.description}
                  </p>
                </div>
                <span class="points">
                  ${r.points.toLocaleString(this.locale)} <sr-lang key="points">Points</sr-lang>
                </span>

                <div class="add-criterion-item">
                  <span tabindex="0" role="button" title="${tr("add_rating")} ${c.title}" data-criterion-id="${c.id}" class="fa fa-plus" @click="${this.addRating}" data-rating-pos="${i+1}"></span>
                </div>

                <span @focus="${this.onFocus}" @focusout="${this.focusOut}" tabindex="0" role="button" title="${tr("drag_order")}" class="reorder-icon sideways fa fa-bars"></span>
              </div>
            `)}
            </div>
          </div>
          <div class="criterion-actions">
            <a @focus="${this.onFocus}" @focusout="${this.focusOut}" tabindex="0" role="button" data-criterion-id="${c.id}" title="${tr("copy")} ${c.title}" class="linkStyle clone fa fa-copy" @keyup="${this.openEditWithKeyboard}" @click="${this.cloneCriterion}" href="#"></a>
            <sakai-item-delete criterion-id="${c.id}" criterion="${JSON.stringify(c)}" rubric-id="${this.rubricId}" @delete-item="${this.deleteCriterion}" token="${this.token}"></sakai-item-delete>
          </div>
        </div>
      `)}
      </div>
      ${this.weighted ? html`
        <br>
        <div class="total-weight ${this.validWeight ? "" : "has-error"}">
          <span class="control-label">${tr('total_weight', [this.totalWeight])}</span>
        </div>`
        : ""
      }
      <br>
      <div>
        ${this.weighted ? html`
          <button class="save-weights" @click="${this.saveWeights}" ?disabled="${!this.validWeight}">
            <span tabindex="0" role="button" class="add fa fa-save"></span>
            <sr-lang key="save_weights">Save Weights</sr-lang>
          </button>`
          : ""
        }
        <button class="add-criterion" @click="${this.createCriterion}">
          <span tabindex="0" role="button" class="add fa fa-plus"></span>
          <sr-lang key="add_criterion">Add Criterion</sr-lang>
        </button>
      </div>
      ${this.weighted ? html`
        <br>
        <div class="save-success has-success fade">
          <span class="control-label">${tr("saved_successfully")}</span>
        </div>`
        : ""
      }
    `;
  }

  onFocus(e){
    e.target.closest('.criterion-row').classList.add("focused");
  }
  focusOut(e){
    e.target.closest('.criterion-row').classList.remove("focused");
  }

  handleSortedCriteria() {

    const baseUrl = `${window.location.protocol}//${window.location.host}/rubrics-service/rest/criterions/`;
    const sortedIds = Array.from(this.querySelectorAll(".criterion-row")).map(c => c.dataset.criterionId);
    const urlList = sortedIds.reverse().reduce((a, cid) => { return `${baseUrl}${cid}\n${a}`; }, '');

    $.ajax({
      url: `/rubrics-service/rest/rubrics/${this.rubricId}/criterions`,
      headers: {"authorization": this.token},
      method: "PUT",
      contentType: "text/uri-list",
      data: urlList
    })
    .done(() => this.letShareKnow())
    .fail((jqXHR, error, message) => {

      console.error(error);
      console.info(message);
    });
  }

  letShareKnow() {
    this.dispatchEvent(new SharingChangeEvent());
  }

  debounce(fn, delay) {

    let timer = null;
    return function() {
      const args = arguments;
      clearTimeout(timer);
      timer = setTimeout(() => {
        fn.apply(this, args);
      }, delay);
    };
  }

  handleSortedCriterionRatings(e) {

    e.stopPropagation();

    const criterionId = e.target.dataset.criterionId;
    const criterion = this.criteria.find(c => c.id == criterionId);

    const sortedIds = Array.from(this.querySelectorAll(`#cr-table-${criterionId} .rating-item`)).map(r => r.dataset.ratingId);

    // Reorder the ratings based on the sort result
    criterion.ratings = sortedIds.map(id => criterion.ratings.find(r => r.id == id));

    const baseUrl = `${window.location.protocol}//${window.location.host}/rubrics-service/rest/ratings/`;
    const urlList = sortedIds.reverse().reduce((a, rid) => { return `${baseUrl}${rid}\n${a}`; }, '');
    const url = `/rubrics-service/rest/criterions/${criterionId}/ratings`;
    this.updateRatings(url, urlList);

    this.criteria = [...this.criteria];

    this.letShareKnow();
  }

  saveRating(e) {

    e.stopPropagation();

    $.ajax({
      url: `/rubrics-service/rest/ratings/${e.detail.id}`,
      headers: {"authorization": this.token},
      contentType: "application/json",
      method: "PATCH",
      data: JSON.stringify(e.detail)
    })
    .done(() => {

      this.criteria.forEach(c => {
        if (c.id == e.detail.criterionId) {
          c.ratings.forEach(r => {
            if (r.id == e.detail.id) {
              r.title = e.detail.title;
              r.description = e.detail.description;
              r.points = e.detail.points;
              r.new = false;
            }
          });
        }
      });
      this.requestUpdate();
      this.letShareKnow();
    })
    .fail((jqXHR, error, message) => {

      console.error(error);
      console.info(message);
    });
  }

  deleteCriterion(e) {

    e.stopPropagation();
    const index = this.criteria.findIndex(c => c.id === e.detail.id);
    this.criteria.splice(index, 1);
    this.criteriaMap.delete(e.detail.id);
    this.requestUpdate();
    this.letShareKnow();
    this.dispatchEvent(new CustomEvent('refresh-total-weight', { detail: { criteria: this.criteria } }));
  }

  emitWeightChanged(e) {

    if (e.target.value == '') {
      e.target.value = 0;
    }
    let value = e.target.value.replace(',', '.');
    value = parseFloat(value);
    if (isNaN(value)) {
      value = 0;
    }
    const id = parseInt(e.target.getAttribute('data-criterion-id'));
    if (isNaN(id)) {
      return;
    }
    this.dispatchEvent(new CustomEvent('weight-changed', { detail: { criterionId: id, value, criteria: this.criteria } }));
    this.requestUpdate();
  }

  addRating(e) {

    const criterionId = e.target.dataset.criterionId;
    const ratingPos = e.target.dataset.ratingPos;

    $.ajax({
      url: "/rubrics-service/rest/ratings",
      headers: {"Content-Type": "application/json", "x-copy-source": "default", "authorization": this.token, "lang": this.locale},
      method: "POST",
      data: "{}"
    })
    .done(data => this.addRatingResponse(criterionId, ratingPos, data))
    .fail((jqXHR, error, message) => {

      console.error(error);
      console.info(message);
    });
  }

  addRatingResponse(criterionId, ratingPos, newRating) {

    newRating.new = true;

    const criterion = this.criteriaMap.get(parseInt(criterionId));

    if (!criterion.ratings) criterion.ratings = [];

    criterion.ratings.splice(parseInt(ratingPos), 0, newRating);

    const getUrl = window.location;
    const baseUrl = getUrl.protocol + "//" + getUrl.host + "/rubrics-service/rest/ratings/";

    let urlList = '';
    for (let i = criterion.ratings.length - 1; i >= 0; i--) {
      urlList = baseUrl + criterion.ratings[i].id + '\n' + urlList;
    }

    const url = `/rubrics-service/rest/criterions/${criterionId}/ratings`;
    this.updateRatings(url, urlList);

    this.letShareKnow();
    this.requestUpdate();
  }

  deleteRating(e) {

    e.stopPropagation();

    const criterion = this.criteriaMap.get(parseInt(e.detail.criterionId));
    const ratingIndex = criterion.ratings.findIndex(r => r.id == e.detail.id);
    criterion.ratings.splice(ratingIndex, 1);
    this.requestUpdate();

    const getUrl = window.location;
    const baseUrl = `${getUrl.protocol}//${getUrl.host}/rubrics-service/rest/criterions/`;

    const urlList = criterion.ratings.slice().reverse().reduce((a, v) => { return `${baseUrl}${v.id}\n${a}`; }, '');

    const url = `/rubrics-service/rest/criterions/${e.detail.criterionId}/ratings`;
    this.updateRatings(url, urlList);
  }

  cloneCriterion(e) {
    e.preventDefault();
    e.stopPropagation();

    $.ajax({
      url: "/rubrics-service/rest/criterions/",
      headers: {"x-copy-source": e.target.dataset.criterionId, "authorization": this.token, "lang": this.locale},
      contentType: "application/json",
      method: "POST",
      data: "{}"
    })
    .done(data => this.createCriterionResponse(data))
    .fail((jqXHR, error, message) => {

      console.error(error);
      console.info(message);
    });
  }

  createCriterionResponse(nc) {

    nc.new = true;
    if (!nc.ratings) {
      nc.ratings = [];
    }
    this.criteria.push(nc);
    this.criteriaMap.set(nc.id, nc);

    // Add the criterion to the rubric
    const getUrl = window.location;
    const baseUrl = getUrl.protocol + "//" + getUrl.host + "/rubrics-service/rest/criterions/";
    const urlList = baseUrl + nc.id + '\n';
    this.requestUpdate();

    $.ajax({
      url: `/rubrics-service/rest/rubrics/${this.rubricId}/criterions`,
      headers: {"authorization": this.token},
      method: "POST",
      contentType: "text/uri-list",
      data: urlList
    })
    .done(() => this.letShareKnow())
    .fail((jqXHR, error, message) => {

      console.error(error);
      console.info(message);
    });
  }

  criterionEdited(e) {

    const criterion = this.criteriaMap.get(e.detail.id);
    criterion.title = e.detail.title;
    criterion.description = e.detail.description;
    criterion.new = false;
    this.requestUpdate();

    const sakaiItemDelete = this.querySelector(`sakai-item-delete[criterion-id="${e.detail.id}"]`);
    sakaiItemDelete.requestUpdate("criterion", criterion);
  }

  saveWeights() {

    this.dispatchEvent(new CustomEvent('save-weights'));
  }

  createCriterion() {

    $.ajax({
      url: "/rubrics-service/rest/criterions/",
      headers: {"Content-Type": "application/json", "x-copy-source": "default", "authorization": this.token, "lang": this.locale},
      method: "POST",
      data: "{}"
    })
    .done(data => this.createCriterionResponse(data))
    .fail((jqXHR, error, message) => {

      console.error(error);
      console.info(message);
    });
  }

  updateRatings(url, urlList) {

    $.ajax({
      url: url,
      headers: {"authorization": this.token},
      method: "PUT",
      contentType: "text/uri-list",
      data: urlList
    })
    .done(() => this.letShareKnow())
    .fail((jqXHR, error, message) => {

      console.error(error);
      console.info(message);
    });
  }

  openEditWithKeyboard(e) {
	
    if (e.keyCode == 32) {
      this.cloneCriterion(e);
    }
  }
}

customElements.define("sakai-rubric-criteria", SakaiRubricCriteria);
