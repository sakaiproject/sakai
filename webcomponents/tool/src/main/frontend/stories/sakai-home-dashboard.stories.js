import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { withA11y } from "@storybook/addon-a11y";
import { styles } from "./styles/sakai-styles.js";
import { coursecardI18n } from "./i18n/course-card-i18n.js";
import { courselistI18n } from "./i18n/course-list-i18n.js";
import { dashboardI18n } from "./i18n/dashboard-i18n.js";
import { widgetpanelI18n } from "./i18n/widgetpanel-i18n.js";
import { tasksI18n } from "./i18n/tasks-i18n.js";
import { gradesI18n } from "./i18n/grades-i18n.js";
import { announcementsI18n } from "./i18n/announcements-i18n.js";
import { calendarI18n } from "./i18n/calendar-i18n.js";
import { forumsI18n } from "./i18n/forums-i18n.js";
import { widgetpickerI18n } from "./i18n/widget-picker-i18n.js";
import { dashboardwidgetI18n } from "./i18n/dashboard-widget-i18n.js";
import { imageeditorI18n } from "./i18n/dialog-content-i18n.js";
import { dialogcontentI18n } from "./i18n/dialog-content-i18n.js";
import { toolnameMappings } from "./data/toolname-mappings.js";
import { toolnamesI18n } from "./i18n/toolnames-i18n.js";
import { dashboardData } from "./data/home-dashboard-data.js";
import { announcementsData } from "./data/home-announcements-data.js";
import { calendarData } from "./data/calendar-data.js";
import { tasksData } from "./data/tasks-data.js";
import { forumsData } from "./data/forums-data.js";
import { gradesData } from "./data/home-grades-data.js";
import { sitesData } from "./data/sites-data.js";

import '../js/sakai-home-dashboard.js';

export default {
  title: 'Sakai Home Dashboard',
  decorators: [withA11y, (storyFn) => {
    parent.portal = {locale: "en-GB"};
    const baseUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=en-GB&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=";
    const coursecardI18nUrl = `${baseUrl}coursecard`;
    const courselistI18nUrl = `${baseUrl}courselist`;
    const dashboardI18nUrl = `${baseUrl}dashboard`;
    const widgetpanelI18nUrl = `${baseUrl}widgetpanel`;
    const tasksI18nUrl = `${baseUrl}tasks`;
    const gradesI18nUrl = `${baseUrl}gradebookng`;
    const announcementsI18nUrl = `${baseUrl}announcement`;
    const calendarI18nUrl = `${baseUrl}calendar`;
    const toolnamesI18nUrl = `${baseUrl}toolnames`;
    const forumsI18nUrl = `${baseUrl}org.sakaiproject.api.app.messagecenter.bundle.Messages`;
    const widgetpickerI18nUrl = `${baseUrl}widget-picker`;
    const dialogcontentI18nUrl = `${baseUrl}dialog-content`;
    const dashboardwidgetI18nUrl = `${baseUrl}dashboard-widget`;
    const toolnameMappingsUrl = `${baseUrl}toolname-mappings`;
    fetchMock
      .get(coursecardI18nUrl, coursecardI18n, {overwriteRoutes: true})
      .get(courselistI18nUrl, courselistI18n, {overwriteRoutes: true})
      .get(dashboardI18nUrl, dashboardI18n, {overwriteRoutes: true})
      .get(widgetpanelI18nUrl, widgetpanelI18n, {overwriteRoutes: true})
      .get(tasksI18nUrl, tasksI18n, {overwriteRoutes: true})
      .get(gradesI18nUrl, gradesI18n, {overwriteRoutes: true})
      .get(announcementsI18nUrl, announcementsI18n, {overwriteRoutes: true})
      .get(calendarI18nUrl, calendarI18n, {overwriteRoutes: true})
      .get(toolnamesI18nUrl, toolnamesI18n, {overwriteRoutes: true})
      .get(forumsI18nUrl, forumsI18n, {overwriteRoutes: true})
      .get(widgetpickerI18nUrl, widgetpickerI18n, {overwriteRoutes: true})
      .get(dashboardwidgetI18nUrl, dashboardwidgetI18n, {overwriteRoutes: true})
      .get(dialogcontentI18nUrl, dialogcontentI18n, {overwriteRoutes: true})
      .get(toolnameMappingsUrl, toolnameMappings, {overwriteRoutes: true})
      .get(/api\/addfavourite/, 200, {overwriteRoutes: true})
      .get(/api\/removefavourite/, 200, {overwriteRoutes: true})
      .get(/api\/users\/.*\/dashboard/, dashboardData, {overwriteRoutes: true})
      .put(/api\/users\/.*\/dashboard/, 200, {overwriteRoutes: true})
      .get(/api\/users\/.*\/sites/, sitesData, {overwriteRoutes: true})
      .get(/api\/tasks/, tasksData, {overwriteRoutes: true})
      .get(/api\/users\/.*\/announcements/, announcementsData, {overwriteRoutes: true})
      .get(/api\/users\/.*\/calendar/, calendarData, {overwriteRoutes: true})
      .get(/api\/users\/.*\/forums/, forumsData, {overwriteRoutes: true})
      .get(/api\/users\/.*\/grades/, gradesData, {overwriteRoutes: true})
      .put(/api\/tasks\/add/, () => Math.floor(Math.random() * Math.floor(1000)).toString(), {overwriteRoutes: true})
      .put(/api\/tasks\/.*/, 200, {overwriteRoutes: true})
      .delete(/api\/tasks\/.*/, 200, {overwriteRoutes: true})
      .put(/userPrefs/, 200, {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});
    return storyFn();
  }],
};

export const BasicDisplay = () => {

  return html`
    ${unsafeHTML(styles)}
    <style>
      body {
        background: white;
      }
    </style>
    <sakai-home-dashboard user-id="adrian">
  `;
};
