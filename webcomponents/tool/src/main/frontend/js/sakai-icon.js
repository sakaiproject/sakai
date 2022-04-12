import { html, css, LitElement } from './assets/lit-element/lit-element.js';
import { icon, library } from "./assets/@fortawesome/fontawesome-svg-core/index.es.js";
import { faCompressArrowsAlt, faEyeSlash, faChalkboardTeacher, faExpandArrowsAlt, faLock, faStar, faEllipsisV, faBell, faCircle, faCog, faChevronUp, faChevronDown, faList, faThumbsUp, faThumbtack, faTimes, faCheckSquare, faCheckCircle, faComment, faComments, faBook, faFile, faFileAlt,
  faGripVertical, faLightbulb, faHeart, faUsers, faUserSecret, faMinus, faPlus, faQuestion, faQuestionCircle, faFlag, faAngleRight, faAngleLeft, faHourglass, faFileWord, faSync, faSmile,
  faTrash, faTrashRestore, faEdit, faKey, faArrowDown, faArrowLeft, faArrowRight, faArrowUp, faPlay, faVolumeUp }
  from './assets/@fortawesome/free-solid-svg-icons/index.es.js';

export class SakaiIcon extends LitElement {

  static get properties() {

    return {
      hasAlerts: { attribute: "has-alerts", type: Boolean },
      type: String,
      size: String,
    };
  }

  constructor() {

    super();
    this.size = "medium";
  }

  shouldUpdate() {
    return this.type && this.type !== "undefined";
  }

  render() {
    return html`${icon(SakaiIcon.lookups.get(this.type), {classes: `sakai-${this.size}-icon`}).node}${this.hasAlerts ? html`<div class="alert"></div>` : ""}`;
  }

  static get styles() {

    return css`
      :host(*) {
        display: inline-block;
      }
      .sakai-smallest-icon {
        pointer-events: none;
        width: var(--sakai-smallest-icon-width);
        height: var(--sakai-smallest-icon-height);
      }
      .sakai-small-icon {
        pointer-events: none;
        width: var(--sakai-small-icon-width);
        height: var(--sakai-small-icon-height);
      }
      .sakai-medium-icon {
        pointer-events: none;
        width: var(--sakai-medium-icon-width);
        height: var(--sakai-medium-icon-height);
      }
      .sakai-large-icon {
        pointer-events: none;
        width: var(--sakai-large-icon-width);
        height: var(--sakai-large-icon-height);
      }

      .alert {
        background-color: var(--sakai-icon-alert-color);
        width: var(--sakai-icon-alert-width);
        height: var(--sakai-icon-alert-width);
        position: absolute;
        margin-top: var(--sakai-icon-alert-margin-top);
        margin-left: var(--sakai-icon-alert-margin-left);
        -webkit-border-radius: calc(var(--sakai-icon-alert-width) / 2);
        -moz-border-radius: calc(var(--sakai-icon-alert-width) / 2);
        border-radius: calc(var(--sakai-icon-alert-width) / 2);
      }
    `;
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

SakaiIcon.lookups = new Map();
SakaiIcon.lookups.set("favourite", faStar);
SakaiIcon.lookups.set("lock", faLock);
SakaiIcon.lookups.set("menu", faEllipsisV);
SakaiIcon.lookups.set("cog", faCog);
SakaiIcon.lookups.set("list", faList);
SakaiIcon.lookups.set("alert", faBell);
SakaiIcon.lookups.set("add", faPlus);
SakaiIcon.lookups.set("comment", faComment);
SakaiIcon.lookups.set("forums", faComments);
SakaiIcon.lookups.set("gradebook", faBook);
SakaiIcon.lookups.set("file", faFile);
SakaiIcon.lookups.set("pin", faThumbtack);
SakaiIcon.lookups.set("thumbs-up", faThumbsUp);
SakaiIcon.lookups.set("assignments", faFileAlt);
SakaiIcon.lookups.set("gripper", faGripVertical);
SakaiIcon.lookups.set("close", faTimes);
SakaiIcon.lookups.set("priority", faFlag);
SakaiIcon.lookups.set("right", faAngleRight);
SakaiIcon.lookups.set("left", faAngleLeft);
SakaiIcon.lookups.set("deadline", faHourglass);
SakaiIcon.lookups.set("word", faFileWord);
SakaiIcon.lookups.set("delete", faTrash);
SakaiIcon.lookups.set("restore", faTrashRestore);
SakaiIcon.lookups.set("edit", faEdit);
SakaiIcon.lookups.set("key", faKey);
SakaiIcon.lookups.set("quizzes", faCheckSquare);
SakaiIcon.lookups.set("up", faArrowUp);
SakaiIcon.lookups.set("down", faArrowDown);
SakaiIcon.lookups.set("left", faArrowLeft);
SakaiIcon.lookups.set("right", faArrowRight);
SakaiIcon.lookups.set("chevron-up", faChevronUp);
SakaiIcon.lookups.set("chevron-down", faChevronDown);
SakaiIcon.lookups.set("refresh", faSync);
SakaiIcon.lookups.set("smile", faSmile);
SakaiIcon.lookups.set("minus", faMinus);
SakaiIcon.lookups.set("users", faUsers);
SakaiIcon.lookups.set("secret", faUserSecret);
SakaiIcon.lookups.set("heart", faHeart);
SakaiIcon.lookups.set("lightbulb", faLightbulb);
SakaiIcon.lookups.set("fs-expand", faExpandArrowsAlt);
SakaiIcon.lookups.set("teacher", faChalkboardTeacher);
SakaiIcon.lookups.set("hidden", faEyeSlash);
SakaiIcon.lookups.set("fs-compress", faCompressArrowsAlt);
SakaiIcon.lookups.set("question", faQuestion);
SakaiIcon.lookups.set("questioncircle", faQuestionCircle);
SakaiIcon.lookups.set("circle", faCircle);
SakaiIcon.lookups.set("check_circle", faCheckCircle);
SakaiIcon.lookups.set("volume_up", faVolumeUp);
SakaiIcon.lookups.set("play", faPlay);

if (!customElements.get("sakai-icon")) {
  customElements.define("sakai-icon", SakaiIcon);
}
