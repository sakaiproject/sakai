import { SakaiElement } from "@sakai-ui/sakai-element";
import { getUserLocale } from "@sakai-ui/sakai-portal-utils";

export class RubricsElement extends SakaiElement {

  static properties = { _i18n: { state: true } };

  constructor() {

    super();

    this.locale = getUserLocale();

    // Keep a reference to the i18n promise so that sub classes can wait on it.
    this.i18nLoaded = this.loadTranslations("rubrics");
    this.i18nLoaded.then(r => this._i18n = r);
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

    console.debug("RubricsElement.showRubricLightbox");

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

