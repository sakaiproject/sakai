import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { dialogcontentI18n } from "../i18n/dialog-content-i18n.js";
import { tasksI18n } from "./i18n/tasks.js";
import { tasksData } from "./data/tasks.js";

import '../../js/tasks/sakai-tasks.js';

export default {
  title: 'Sakai Tasks',
  decorators: [storyFn => {
    parent.portal = {locale: "en-GB"};
    fetchMock
      .get(/sakai-ws\/rest\/i18n\/getI18nProperties.*/, tasksI18n, {overwriteRoutes: true})
      .get(/.*dialog-content.*/, dialogcontentI18n, {overwriteRoutes: true})
      .get("/api/tasks", tasksData, {overwriteRoutes: true})
      .get(/.*tasks.*/, tasksI18n, {overwriteRoutes: true})
      .post(/api\/tasks$/, (url, opts) => {

        const requestTask = JSON.parse(opts.body);
        requestTask.taskId = "" + Math.floor(Math.random() * 20) + 1;
        requestTask.userTaskId = "" + Math.floor(Math.random() * 20) + 1;
        requestTask.userId = "8667b7bb-f0f8-41e4-bc91-35b76d4b199e";
        return requestTask;
      }, {overwriteRoutes: true})
      .put(/api\/tasks\/.*/, 200, {overwriteRoutes: true})
      .delete(/api\/tasks\/.*/, 200, {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});
    return storyFn();
  }],
};

export const BasicDisplay = () => {

  return html`
    <div style="width: 400px;">
      <sakai-tasks user-id="adrian">
    </div>
  `;
};
