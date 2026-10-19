import { html, nothing } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import { getUserLocale } from "@sakai-ui/sakai-portal-utils";

export class RubricsElement extends SakaiElement {

  static properties = { _saveError: { state: true } };

  constructor() {

    super();

    this.locale = getUserLocale();

    // Keep a reference to the i18n promise so that sub classes can wait on it.
    this._i18nLoaded = this.loadTranslations("rubrics");
  }

  enqueueMutation(run) {
    const owner = this.closest("sakai-rubric") ?? this;
    // Wait for the previous request, while leaving failures visible to its caller.
    const queued = (owner._mutationQueue ?? Promise.resolve()).then(run, run);
    owner._mutationQueue = queued;
    return queued;
  }

  async readSaveResponse(response) {
    if (!response.ok) {
      const error = new Error(`Rubric save failed (${response.status})`);
      error.status = response.status;
      throw error;
    }
    this._saveError = null;
    const saved = await response.json();
    this.updateVersions(saved);
    return saved;
  }

  updateVersions(saved) {
    const rubric = this.closest("sakai-rubric")?.rubric;
    if (!rubric) { return; }
    rubric.version = saved.rubricVersion ?? saved.version;
    rubric.lastModifiedBy = saved.rubricLastModifiedBy ?? saved.lastModifiedBy;
    rubric.lastModifiedByDisplayName = saved.rubricLastModifiedByDisplayName ?? saved.lastModifiedByDisplayName;
    rubric.formattedModifiedDate = saved.rubricFormattedModifiedDate ?? saved.formattedModifiedDate;
    this.closest("sakai-rubric").requestUpdate();
    for (const criterion of rubric.criteria) {
      criterion.rubricVersion = rubric.version;
      criterion.ratings.forEach(rating => rating.rubricVersion = rubric.version);
    }
    const criteria = saved.criteria ?? (saved.ratings ? [ saved ] : []);
    for (const incoming of criteria) {
      const criterion = rubric.criteria.find(c => c.id === incoming.id);
      if (!criterion) { continue; }
      criterion.version = incoming.version;
      for (const rating of incoming.ratings) {
        const current = criterion.ratings.find(r => r.id === rating.id);
        if (current) { current.version = rating.version; }
      }
    }
  }

  reportSaveError(error) {
    this._saveError = error.status === 409 ? "save_conflict" : "operation_failed";
    console.error(error);
  }

  renderSaveError() {
    return this._saveError ? html`<div class="sak-banner-error" role="alert">${this.tr(this._saveError)}</div>` : nothing;
  }

  isUtilsAvailable() {

    const available = window.top.rubrics && window.top.rubrics.utils;
    if (!available) {
      console.error("Rubrics Utils has not been loaded (sakai-rubrics-utils.js). THINGS WILL BREAK!");
    }
    return available;
  }

  initLightbox(i18n, siteId, enablePdfExport) {

    if (this.isUtilsAvailable()) {
      window.top.rubrics.utils.initLightbox(i18n, siteId, enablePdfExport);
    }
  }

  showRubricLightbox(id, attributes) {

    if (this.isUtilsAvailable()) {
      window.top.rubrics.utils.showRubric(id, attributes);
    }
  }

  getHighLow(myArray) {

    let lowest = Number.POSITIVE_INFINITY;
    let highest = Number.NEGATIVE_INFINITY;
    let tmp;

    for (let i = myArray.length - 1; i >= 0; i--) {
      tmp = myArray[i].points;
      if (tmp < lowest) lowest = tmp;
      if (tmp > highest) highest = tmp;
    }

    return {
      high: highest,
      low: lowest
    };
  }

  isCriterionGroup(criterion) {
    return criterion.ratings.length === 0;
  }

  openRubricsTab(tabId) {

    this.querySelectorAll(".rubric-tab-content").forEach(tab => {

      // put all tabs' styling back to default [invisible]
      tab.setAttribute("class", "rubric-tab-content");
      if (tab.getAttribute("id").indexOf("summary") !== -1 && tab.getAttribute("id").indexOf(tabId) === -1) { //remove any summary in this tab; only one should exist at a time
        tab.innerHTML = "";
      }
    });

    const tabNow = document.getElementById(tabId);
    tabNow && tabNow.setAttribute("class", "rubric-tab-content rubrics-visible"); // style the clicked tab to be visible
    this.querySelectorAll(".rubrics-tab-button").forEach(tb => tb.setAttribute("class", "rubrics-tab-button"));
    const tabButtonNow = this.querySelector(`#${tabId}-button`);
    tabButtonNow && tabButtonNow.setAttribute("class", "rubrics-tab-button rubrics-tab-selected"); //select styling on current tab button
  }

  makeASummary(type, siteId) {

    if (this.querySelector(`${type}-summary`)) { //avoid adding an extra summary by accident
      this.openRubricsTab(`rubric-${type}-summary-${this.instanceSalt}`);
    }
    const summary = document.createElement("sakai-rubric-summary");
    summary.setAttribute("id", `${type}-summary`);
    summary.setAttribute("site-id", siteId);
    summary.setAttribute("entity-id", this.entityId);
    summary.setAttribute("tool-id", this.toolId);
    if (this.evaluatedItemId) {
      summary.setAttribute("evaluated-item-id", this.evaluatedItemId);
    }
    summary.setAttribute("evaluated-item-owner-id", this.evaluatedItemOwnerId);
    summary.setAttribute("summary-type", type);
    const div = document.getElementById(`rubric-${type}-summary-${this.instanceSalt}`);
    div && div.appendChild(summary);
    this.openRubricsTab(`rubric-${type}-summary-${this.instanceSalt}`);
  }

  _stopPropagation(e) { e.stopPropagation(); }

  shouldUpdate() {
    return this._i18n;
  }

  tr(key, options) {
    return super.tr(key, options, "rubrics");
  }
}

