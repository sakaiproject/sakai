import { SakaiElement } from "../sakai-element.js";
import { getUserLocale } from "../sakai-portal-utils.js";

class RubricsElement extends SakaiElement {

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

    const allTabs = this.querySelectorAll('.rubric-tab-content');
    for(let i=0; i<allTabs.length; i++){    // put all tabs' styling back to default [invisible]
      allTabs[i].setAttribute('class', 'rubric-tab-content');
      if(allTabs[i].getAttribute('id').indexOf('summary')!==-1 && allTabs[i].getAttribute('id').indexOf(tabname)===-1){	//remove any summary in this tab; only one should exist at a time
        allTabs[i].innerHTML = '';
      }
    }
    const tabNow = this.querySelector(`#${tabname}`);
    if (tabNow !== null) {
      tabNow.setAttribute('class', 'rubric-tab-content rubrics-visible'); // style the clicked tab to be visible
    }
    const allTabButtons = this.querySelectorAll('.rubrics-tab-button');
    for(let i=0; i<allTabButtons.length; i++){    // put all tab buttons' styling back to default
      allTabButtons[i].setAttribute('class', 'rubrics-tab-button');
    }
    const tabButtonNow = this.querySelector(`#${tabname}-button`);
    if (tabButtonNow !== null) {
      tabButtonNow.setAttribute('class', 'rubrics-tab-button rubrics-tab-selected'); //select styling on current tab button
    }
  }

  makeASummary(type) {

    const div = this.querySelector(`#rubric-${type}-summary`);
    if(this.querySelector(`${type}-summary`) !== null){	//avoid adding an extra summary by accident
      this.openRubricsTab('rubric-' + type + '-summary');
    }
    const summary = document.createElement('sakai-rubric-summary');
    summary.setAttribute('id', type + '-summary');
    summary.setAttribute('token', this.token);
    summary.setAttribute('entity-id', this.entityId);
    summary.setAttribute('tool-id', this.toolId);
    summary.setAttribute('evaluated-item-id', this.evaluatedItemId);
    summary.setAttribute('evaluated-item-owner-id', this.evaluatedItemOwnerId);
    summary.setAttribute('summarytype',type);
    if (div !== null) {
      div.appendChild(summary);
    }
    this.openRubricsTab('rubric-' + type + '-summary');
  }
}

export { RubricsElement };
