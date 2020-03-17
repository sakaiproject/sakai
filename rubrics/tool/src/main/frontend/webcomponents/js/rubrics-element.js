import {SakaiElement} from "/webcomponents/sakai-element.js";

class RubricsElement extends SakaiElement {

  constructor() {

    super();

    this.locale = window.portal ? window.portal.locale : window.top.portal.locale;
  }

  isUtilsAvailable() {

    let available = window.top.rubrics && window.top.rubrics.utils;
    if (!available) {
      console.error("Rubrics Utils has not been loaded (sakai-rubrics-utils.js). THINGS WILL BREAK!");
    }
    return available;
  }

  initLightbox(token, i18n) {

    if (this.isUtilsAvailable()) {
      window.top.rubrics.utils.initLightbox(token, i18n);
    }
  }

  showRubricLightbox(id, attributes) {

    if (this.isUtilsAvailable()) {
      window.top.rubrics.utils.showRubric(id, attributes);
    }
  }

  // get high low values from objects in array
  getHighLow(myArray, property) {

    var lowest = Number.POSITIVE_INFINITY;
    var highest = Number.NEGATIVE_INFINITY;
    var tmp;

    for (var i=myArray.length-1; i>=0; i--) {
      tmp = myArray[i][property];
      if (tmp < lowest) lowest = tmp;
      if (tmp > highest) highest = tmp;
    }

    return {
      high: highest,
      low: lowest
    }
  }
}

export {RubricsElement};
