import {SakaiElement} from "/webcomponents/sakai-element.js";
import {rubricsUtils} from "./sakai-rubrics-utils.js";

class RubricsElement extends SakaiElement {

  constructor() {

    super();

    this.locale = (window.top?.portal?.locale || window.top?.sakai?.locale?.userLocale || "en-US").replace("_", "-");

    this.rubricsUtils = rubricsUtils;
  }
}

export {RubricsElement};
