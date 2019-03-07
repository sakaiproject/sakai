import {SakaiElement} from "/webcomponents/sakai-element.js";
import {css,html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SakaiRubricEdit} from "./sakai-rubric-edit.js";
import {SakaiItemDelete} from "./sakai-item-delete.js";
import {SakaiRubricCriterionEdit} from "./sakai-rubric-criterion-edit.js";
import {SakaiRubricCriterionRatingEdit} from "./sakai-rubric-criterion-rating-edit.js";
import {SharingChangeEvent} from "./sharing-change-event.js";
import * as Unused from "/webcomponents/assets/sortablejs/Sortable.js";
import {tr} from "./sakai-rubrics-language.js";

export class SakaiRubricCriteria extends SakaiElement {

  static get properties() {

    return {
      token: { type: String },
      rubricId: { type: String },
      criteria: { type: Array }
    };
  }

  updated(changedProperties) {

    super.updated(changedProperties);

    var sortableOptions = {
      handle: ".reorder-icon",
      onUpdate: (e) => this.handleSortedCriterionRatings(e),
      draggable: ".rating-item",
      animation: "200",
    };

    document.querySelectorAll(".cr-table").forEach(cr => new Sortable(cr, sortableOptions));

    sortableOptions.onUpdate = (e) => this.handleSortedCriteria(e);
    sortableOptions.draggable = ".criterion-row";

    document.querySelectorAll(".criterion").forEach(c => new Sortable(c, sortableOptions));

    if (changedProperties.has("criteria")) {
      this.criteriaMap = new Map(this.criteria.map(c => [c.id, c]));
    }
  }

  render() {

    return html`
      <div class="criterion style-scope sakai-rubric-criterion">
      ${this.criteria.map(c => html`
        <div id="criterion_row_${c.id}" class="criterion-row">
          <div class="criterion-detail">
            <h4 class="criterion-title">
              <span tabindex="0" role="button" title="${tr("drag_order")}" class="reorder-icon fa fa-bars"></span>
              ${c.title}
              <sakai-rubric-criterion-edit @criterion-edited="${this.criterionEdited}" criterion="${JSON.stringify(c)}" token="${this.token}"></sakai-rubric-criterion-edit>
            </h4>
            <p>
              ${c.description}
            </p>
            <div class="add-criterion-item">
              <span tabindex="0" role="button" data-criterion-id="${c.id}" title="${tr("add_rating")} ${c.title}" class="fa fa-plus" @click="${this.addRating}" data-rating-pos="0"></span>
            </div>
          </div>
          <div class="criterion-ratings">
            <div class="cr-table" data-criterion-id="${c.id}">
            ${c.ratings.map((r,i) => html`
              <div class="rating-item" id="rating_item_${r.id}">
                <h5 class="criterion-item-title">
                  ${r.title}
                  <sakai-rubric-criterion-rating-edit criterion-id="${c.id}" @save-rating="${this.saveRating}" @delete-rating="${this.deleteRating}" minpoints="${c.pointrange ? c.pointrange.low : 0}" maxpoints="${c.pointrange ? c.pointrange.high : 0}" rating="${JSON.stringify(r)}"></sakai-rubric-criterion-rating-edit>
                </h5>
                <p>
                  ${r.description}
                </p>
                <span class="points">
                  ${r.points} <sr-lang key="points">Points</sr-lang>
                </span>

                <div class="add-criterion-item">
                  <span tabindex="0" role="button" title="${tr("add_rating")} ${c.title}" data-criterion-id="${c.id}" class="fa fa-plus" @click="${this.addRating}" data-rating-pos="${i+1}"></span>
                </div>

                <span tabindex="0" role="button" title="${tr("drag_order")}" class="reorder-icon sideways fa fa-bars"></span>
              </div>
            `)}
            </div>
          </div>
          <div class="criterion-actions">
            <span tabindex="0" role="button" data-criterion-id="${c.id}" title="${tr("clone")} ${c.title}" class="clone fa fa-copy" @click="${this.cloneCriterion}"></span>
            <sakai-item-delete criterion="${JSON.stringify(c)}" rubricId="${this.rubricId}" @delete-item="${this.deleteCriterion}" token="${this.token}"></sakai-item-delete>
          </div>
        </div>
      `)}
      </div>
      <br>
      <button class="add-criterion" @click="${this.createCriterion}">
        <span tabindex="0" role="button" class="add fa fa-plus"></span>
        <sr-lang key="add_criterion">Add Criterion</sr-lang>
      </button>
    `;
  }

  handleSortedCriteria(e) {

    var tmp = this.criteria[e.newIndex];
    this.criteria[e.newIndex] = this.criteria[e.oldIndex];
    this.criteria[e.oldIndex] = tmp;

    var baseUrl = `${window.location.protocol}//${window.location.host}/rubrics-service/rest/criterions/`;
    const urlList = this.criteria.slice().reverse().reduce((a, c) => { return `${baseUrl}${c.id}\n${a}` }, '');

    $.ajax({
      url: `/rubrics-service/rest/rubrics/${this.rubricId}/criterions`,
      headers: {"authorization": this.token},
      method: "PUT",
      contentType: "text/uri-list",
      data: urlList
    })
    .done(() => this.letShareKnow())
    .fail((jqXHR, error, message) => {console.log(error); console.log(message); });

    this.letShareKnow();
  }

  letShareKnow() {
    this.dispatchEvent(new SharingChangeEvent());
  }

  handleSortedCriterionRatings(e) {

    e.stopPropagation();

    var criterionId = e.target.dataset.criterionId;
    var criterion = this.criteria.find(c => c.id == criterionId);

    var tmp = criterion.ratings[e.newIndex];
    criterion.ratings[e.newIndex] = criterion.ratings[e.oldIndex];
    criterion.ratings[e.oldIndex] = tmp;

    var baseUrl = `${window.location.protocol}//${window.location.host}/rubrics-service/rest/ratings/`;
    const urlList = criterion.ratings.slice().reverse().reduce((a, r) => { return `${baseUrl}${r.id}\n${a}` }, '');
    var url = `/rubrics-service/rest/criterions/${criterionId}/ratings`;
    this.updateRatings(url, urlList);

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
    .done((data) => {

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
    .fail((jqXHR, error, message) => {console.log(error); console.log(message); });
  }

  deleteCriterion(e) {

    e.stopPropagation();
    var index = this.criteria.map(c => c.id).indexOf(e.detail.id);
    this.criteria.splice(index,1);
    this.criteriaMap.delete(e.detail.id);
    this.requestUpdate();
    this.letShareKnow();
  }

  addRating(e) {

    var criterionId = e.target.dataset.criterionId;
    var ratingPos = e.target.dataset.ratingPos;

    $.ajax({
      url: "/rubrics-service/rest/ratings",
      headers: {"Content-Type": "application/json" , "x-copy-source": "default", "authorization": this.token},
      method: "POST",
      data: "{}"
    })
    .done(data => this.addRatingResponse(criterionId, ratingPos, data))
    .fail((jqXHR, error, message) => {console.log(error); console.log(message); });
  }

  addRatingResponse(criterionId, ratingPos, newRating) {

    newRating.new = true;

    var criterion = this.criteriaMap.get(parseInt(criterionId));

    if (!criterion.ratings) criterion.ratings = [];

    criterion.ratings.splice(parseInt(ratingPos), 0, newRating);

    var getUrl = window.location;
    var baseUrl = getUrl.protocol + "//" + getUrl.host + "/rubrics-service/rest/ratings/";

    var urlList = '';
    for (var i = criterion.ratings.length - 1; i >= 0; i--) {
      urlList = baseUrl + criterion.ratings[i].id + '\n' + urlList;
    }

    var url = `/rubrics-service/rest/criterions/${criterionId}/ratings`;
    this.updateRatings(url, urlList);

    this.letShareKnow();
    this.requestUpdate();
  }

  deleteRating(e) {

    e.stopPropagation();

    var criterion = this.criteriaMap.get(parseInt(e.detail.criterionId));
    var ratingIndex = criterion.ratings.findIndex(r => r.id == e.detail.id);
    criterion.ratings.splice(ratingIndex, 1);
    this.requestUpdate();

    var getUrl = window.location;
    var baseUrl = `${getUrl.protocol}//${getUrl.host}/rubrics-service/rest/criterions/`;

    const urlList = criterion.ratings.slice().reverse().reduce((a, v) => { return `${baseUrl}${v.id}\n${a}` }, '');

    var url = `/rubrics-service/rest/criterions/${e.detail.criterionId}/ratings`;
    this.updateRatings(url, urlList);
  }

  cloneCriterion(e) {

    e.stopPropagation();

    $.ajax({
      url: "/rubrics-service/rest/criterions/",
      headers: {"x-copy-source": e.target.dataset.criterionId, "authorization": this.token, "lang": portal.locale},
      contentType: "application/json",
      method: "POST",
      data: "{}"
    })
    .done((data) => this.createCriterionResponse(data))
    .fail((jqXHR, error, message) => {console.log(error); console.log(message); });
  }

  createCriterionResponse(nc) {

    nc.new = true;
    if (!nc.ratings) {
      nc.ratings = [];
    }
    this.criteria.push(nc);
    this.criteriaMap.set(nc.id, nc);

    // Add the association to the rubric
    var getUrl = window.location;
    var baseUrl = getUrl.protocol + "//" + getUrl.host + "/rubrics-service/rest/criterions/";
    var criterionRows = this.querySelectorAll('.criterion-row');
    var urlList = baseUrl + nc.id + '\n';
    this.requestUpdate();

    $.ajax({
      url: `/rubrics-service/rest/rubrics/${this.rubricId}/criterions`,
      headers: {"authorization": this.token},
      method: "POST",
      contentType: "text/uri-list",
      data: urlList
    })
    .done(() => this.letShareKnow())
    .fail((jqXHR, error, message) => { console.log(error); console.log(message); });
  }

  criterionEdited(e) {

    var criterion = this.criteriaMap.get(e.detail.id);
    criterion.title = e.detail.title;
    criterion.description = e.detail.description;
    criterion.new = false;
    this.requestUpdate();
  }

  createCriterion(e) {

    $.ajax({
      url: "/rubrics-service/rest/criterions/",
      headers: {"Content-Type": "application/json", "x-copy-source": "default", "authorization": this.token},
      method: "POST",
      data: "{}"
    })
    .done((data) => this.createCriterionResponse(data))
    .fail((jqXHR, error, message) => { console.log(error); console.log(message); });
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
    .fail((jqXHR, error, message) => {console.log(error); console.log(message);});
  }
}

customElements.define("sakai-rubric-criteria", SakaiRubricCriteria);
