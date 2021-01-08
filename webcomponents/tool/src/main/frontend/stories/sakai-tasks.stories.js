import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { withA11y } from "@storybook/addon-a11y";
import { styles } from "./styles/sakai-styles.js";
import { dialogcontentI18n } from "./i18n/dialog-content-i18n.js";
import { tasksI18n } from "./i18n/tasks-i18n.js";
import { tasksData } from "./data/tasks-data.js";

import '../js/tasks/sakai-tasks.js';

export default {
  title: 'Sakai Tasks',
  decorators: [withA11y, (storyFn) => {
    parent.portal = {locale: "en-GB"};
    fetchMock
      .get(/.*dialog-content.*/, dialogcontentI18n, {overwriteRoutes: true})
      .get("/api/tasks", tasksData, {overwriteRoutes: true})
      .get(/.*tasks.*/, tasksI18n, {overwriteRoutes: true})
      .put(/api\/tasks\/add/, () => Math.floor(Math.random() * Math.floor(1000)).toString(), {overwriteRoutes: true})
      .put(/api\/tasks\/.*/, 200, {overwriteRoutes: true})
      .delete(/api\/tasks\/.*/, 200, {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});
    return storyFn();
  }],
};

export const BasicDisplay = () => {

  return html`
    ${unsafeHTML(styles)}
    <div style="width: 400px;">
      <sakai-tasks user-id="adrian">
    </div>
  `;
};
