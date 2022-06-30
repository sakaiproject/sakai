import { SakaiElement } from "../sakai-element.js";
import { getUserLocale } from "../sakai-portal-utils.js";

export class RubricsElement extends SakaiElement {

  constructor() {

    super();

    this.locale = getUserLocale();
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

  openRubricsTab(tabname) {

    this.querySelectorAll('.rubric-tab-content').forEach(tab => {

      // put all tabs' styling back to default [invisible]
      tab.setAttribute("class", "rubric-tab-content");
      if (tab.getAttribute("id").indexOf("summary") !== -1 && tab.getAttribute("id").indexOf(tabname) === -1) { //remove any summary in this tab; only one should exist at a time
        tab.innerHTML = "";
      }
    });

    const tabNow = document.getElementById(tabname);
    tabNow && tabNow.setAttribute("class", "rubric-tab-content rubrics-visible"); // style the clicked tab to be visible
    this.querySelectorAll(".rubrics-tab-button").forEach(tb => tb.setAttribute("class", "rubrics-tab-button"));
    const tabButtonNow = this.querySelector(`#${tabname}-button`);
    tabButtonNow && tabButtonNow.setAttribute("class", "rubrics-tab-button rubrics-tab-selected"); //select styling on current tab button
  }

  makeASummary(type, siteId) {

    if (this.querySelector(`${type}-summary`)) { //avoid adding an extra summary by accident
      this.openRubricsTab(`rubric-${type}-summary`);
    }
    const summary = document.createElement('sakai-rubric-summary');
    summary.setAttribute('id', `${type}-summary`);
    summary.setAttribute('site-id', siteId);
    summary.setAttribute('entity-id', this.entityId);
    summary.setAttribute('tool-id', this.toolId);
    if (this.evaluatedItemId) {
      summary.setAttribute('evaluated-item-id', this.evaluatedItemId);
    }
    summary.setAttribute('evaluated-item-owner-id', this.evaluatedItemOwnerId);
    summary.setAttribute('summary-type', type);
    const div = document.getElementById(`rubric-${type}-summary`);
    div && div.appendChild(summary);
    this.openRubricsTab(`rubric-${type}-summary`);
  }
}
