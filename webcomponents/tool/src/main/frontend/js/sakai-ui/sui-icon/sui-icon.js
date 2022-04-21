// TODO Review commented lines
// TODO Review need for shadown DOM
import {
  html,
  LitElement,
  // unsafeCSS,
} from "@assets/lit-element/lit-element.js";
import {
  icon,
  library,
} from "@assets/@fortawesome/fontawesome-svg-core/index.es.js";
import {
  faCompressArrowsAlt,
  faEyeSlash,
  faChalkboardTeacher,
  faExpandArrowsAlt,
  faLock,
  faStar,
  faEllipsisV,
  faBell,
  faCircle,
  faCog,
  faChevronUp,
  faChevronDown,
  faList,
  faThumbsUp,
  faThumbtack,
  faTimes,
  faCheckSquare,
  faCheckCircle,
  faComment,
  faComments,
  faBook,
  faFile,
  faFileAlt,
  faGripVertical,
  faLightbulb,
  faHeart,
  faUsers,
  faUserSecret,
  faMinus,
  faPlus,
  faQuestion,
  faQuestionCircle,
  faFlag,
  faAngleRight,
  faAngleLeft,
  faHourglass,
  faFileWord,
  faSync,
  faSmile,
  faTrash,
  faTrashRestore,
  faEdit,
  faKey,
  faArrowDown,
  faArrowLeft,
  faArrowRight,
  faArrowUp,
  faPlusCircle,
  faSlidersH,
  faSearch,
  faPencilAlt,
  faClone,
  faFilter,
  faPlay,
} from "@assets/@fortawesome/free-solid-svg-icons/index.es.js";

// import styles from "./sui-icon.scss";
export class SakaiUIIcon extends LitElement {
  createRenderRoot() {
    // Render to the real dom, not the shadow. We can now pull
    // in Sakai's css and js. This makes any this, and any subclasses,
    // custom elements, not full blown web components
    return this;
  }
  static get properties() {
    return {
      hasAlerts: { attribute: "has-alerts", type: Boolean },
      type: String,
      class: String,
    };
  }

  constructor() {
    super();
    this.class = this.classList.value;
    // This prevents duplicate styles from being added to the component
    this.classList = "";
  }

  shouldUpdate() {
    return this.type && this.type !== "undefined";
  }

  render() {
    return html`${icon(SakaiUIIcon.lookups.get(this.type), {
      classes: `sui-icon ${this.class}`,
    }).node}${this.hasAlerts ? html`<div class="alert"></div>` : ""}`;
  }

  static get styles() {
    return [
      // (typeof styles !== 'undefined' ? unsafeCSS(styles) : null)
    ];
  }
}

library.add(faEllipsisV); // Menu
library.add(faLock);
library.add(faStar); // Favourite
library.add(faBell); // General alerts
library.add(faCog); // Settings
library.add(faList);
library.add(faComment);
library.add(faComments); // Forums
library.add(faBook); // Gradebook
library.add(faCircle);
library.add(faCheckCircle);
library.add(faCheckSquare); // Gradebook
library.add(faChevronUp);
library.add(faChevronDown);
library.add(faFile);
library.add(faFileAlt); // Assignments
library.add(faGripVertical); // Drag gripper
library.add(faThumbsUp);
library.add(faThumbtack);
library.add(faPlus); // Drag gripper
library.add(faTimes); // Close
library.add(faFlag); // Priority
library.add(faAngleRight); // Right
library.add(faAngleLeft); // Left
library.add(faHourglass); // Deadline
library.add(faFileWord); // MS word
library.add(faTrash); // Delete
library.add(faTrashRestore); // Restore
library.add(faKey);
library.add(faEdit); // Edit
library.add(faArrowUp); // Up
library.add(faArrowDown); // Down
library.add(faArrowLeft); // Left
library.add(faArrowRight); // Right
library.add(faSync); // Refresh
library.add(faSmile);
library.add(faCompressArrowsAlt); //unfullscreen
library.add(faEyeSlash);
library.add(faChalkboardTeacher);
library.add(faExpandArrowsAlt); //fullscreen
library.add(faHeart);
library.add(faLightbulb);
library.add(faMinus);
library.add(faUsers);
library.add(faUserSecret);
library.add(faQuestion);
library.add(faQuestionCircle);
library.add(faPlusCircle);
library.add(faSlidersH);
library.add(faSearch);
library.add(faPencilAlt);
library.add(faClone);
library.add(faFilter);
library.add(faPlay);

SakaiUIIcon.lookups = new Map();
SakaiUIIcon.lookups.set("favourite", faStar);
SakaiUIIcon.lookups.set("lock", faLock);
SakaiUIIcon.lookups.set("menu", faEllipsisV);
SakaiUIIcon.lookups.set("cog", faCog);
SakaiUIIcon.lookups.set("list", faList);
SakaiUIIcon.lookups.set("alert", faBell);
SakaiUIIcon.lookups.set("add", faPlus);
SakaiUIIcon.lookups.set("comment", faComment);
SakaiUIIcon.lookups.set("forums", faComments);
SakaiUIIcon.lookups.set("gradebook", faBook);
SakaiUIIcon.lookups.set("file", faFile);
SakaiUIIcon.lookups.set("pin", faThumbtack);
SakaiUIIcon.lookups.set("thumbs-up", faThumbsUp);
SakaiUIIcon.lookups.set("assignments", faFileAlt);
SakaiUIIcon.lookups.set("gripper", faGripVertical);
SakaiUIIcon.lookups.set("close", faTimes);
SakaiUIIcon.lookups.set("priority", faFlag);
SakaiUIIcon.lookups.set("right", faAngleRight);
SakaiUIIcon.lookups.set("left", faAngleLeft);
SakaiUIIcon.lookups.set("deadline", faHourglass);
SakaiUIIcon.lookups.set("word", faFileWord);
SakaiUIIcon.lookups.set("delete", faTrash);
SakaiUIIcon.lookups.set("trash", faTrash);
SakaiUIIcon.lookups.set("restore", faTrashRestore);
SakaiUIIcon.lookups.set("edit", faEdit);
SakaiUIIcon.lookups.set("key", faKey);
SakaiUIIcon.lookups.set("quizzes", faCheckSquare);
SakaiUIIcon.lookups.set("check-square", faCheckSquare);
SakaiUIIcon.lookups.set("up", faArrowUp);
SakaiUIIcon.lookups.set("down", faArrowDown);
SakaiUIIcon.lookups.set("left", faArrowLeft);
SakaiUIIcon.lookups.set("right", faArrowRight);
SakaiUIIcon.lookups.set("chevron-up", faChevronUp);
SakaiUIIcon.lookups.set("chevron-down", faChevronDown);
SakaiUIIcon.lookups.set("refresh", faSync);
SakaiUIIcon.lookups.set("smile", faSmile);
SakaiUIIcon.lookups.set("minus", faMinus);
SakaiUIIcon.lookups.set("users", faUsers);
SakaiUIIcon.lookups.set("secret", faUserSecret);
SakaiUIIcon.lookups.set("heart", faHeart);
SakaiUIIcon.lookups.set("lightbulb", faLightbulb);
SakaiUIIcon.lookups.set("fs-expand", faExpandArrowsAlt);
SakaiUIIcon.lookups.set("teacher", faChalkboardTeacher);
SakaiUIIcon.lookups.set("hidden", faEyeSlash);
SakaiUIIcon.lookups.set("fs-compress", faCompressArrowsAlt);
SakaiUIIcon.lookups.set("question", faQuestion);
SakaiUIIcon.lookups.set("questioncircle", faQuestionCircle);
SakaiUIIcon.lookups.set("circle", faCircle);
SakaiUIIcon.lookups.set("check_circle", faCheckCircle);
SakaiUIIcon.lookups.set("sliders-h", faSlidersH);
SakaiUIIcon.lookups.set("plus-circle", faPlusCircle);
SakaiUIIcon.lookups.set("search", faSearch);
SakaiUIIcon.lookups.set("pencil", faPencilAlt);
SakaiUIIcon.lookups.set("clone", faClone);
SakaiUIIcon.lookups.set("filter", faFilter);
SakaiUIIcon.lookups.set("play", faPlay);
if (!customElements.get("sui-icon")) {
  customElements.define("sui-icon", SakaiUIIcon);
}
