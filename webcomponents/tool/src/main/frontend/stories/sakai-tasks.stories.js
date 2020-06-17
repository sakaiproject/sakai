import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { withA11y } from "@storybook/addon-a11y";
import { styles } from "./styles/sakai-styles.js";
import { tasksI18n } from "./i18n/tasks-i18n.js";
import { tasksData } from "./data/tasks-data.js";

import '../js/tasks/sakai-tasks.js';

export default {
  title: 'Sakai Tasks',
  decorators: [withA11y, (storyFn) => {
    parent.portal = {locale: "en-GB"};
    const baseUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=en-GB&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=";
    const tasksI18nUrl = `${baseUrl}tasks`;
    fetchMock
      .get(tasksI18nUrl, tasksI18n, {overwriteRoutes: true})
      .get("/api/tasks.json", tasksData, {overwriteRoutes: true})
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
    <sakai-tasks load>
  `;
};
