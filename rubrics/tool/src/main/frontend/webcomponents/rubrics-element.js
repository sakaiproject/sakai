import {SakaiElement} from "/webcomponents/sakai-element.js";
import {rubricsUtils} from "./sakai-rubrics-utils.js";

class RubricsElement extends SakaiElement {

  constructor() {

    super();

    this.rubricsUtils = rubricsUtils;
  }
}

export {RubricsElement};
