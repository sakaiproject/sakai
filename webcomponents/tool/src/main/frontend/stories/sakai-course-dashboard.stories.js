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
import { toolnameMappings } from "./data/toolname-mappings.js";
import { toolnamesI18n } from "./i18n/toolnames-i18n.js";
import { statuspanelI18n } from "./i18n/statuspanel-i18n.js";
import { imageeditorI18n } from "./i18n/image-editor-i18n.js";
import { dialogcontentI18n } from "./i18n/dialog-content-i18n.js";
import { dashboardData } from "./data/course-dashboard-data.js";
import { calendarData } from "./data/calendar-data.js";
import { tasksData } from "./data/tasks-data.js";
import { forumsData } from "./data/forums-data.js";
import { announcementsData } from "./data/course-announcements-data.js";
import { gradesData } from "./data/course-grades-data.js";
import { siteData } from "./data/site-data.js";

import '../js/coursedashboard/sakai-course-dashboard.js';

export default {
  title: 'Sakai Course Dashboard',
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
    const statuspanelI18nUrl = `${baseUrl}statuspanel`;
    const imageeditorI18nUrl = `${baseUrl}image-editor`;
    const dialogcontentI18nUrl = `${baseUrl}dialog-content`;
    const forumsI18nUrl = `${baseUrl}org.sakaiproject.api.app.messagecenter.bundle.Messages`;
    const widgetpickerI18nUrl = `${baseUrl}widget-picker`;
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
      .get(statuspanelI18nUrl, statuspanelI18n, {overwriteRoutes: true})
      .get(imageeditorI18nUrl, imageeditorI18n, {overwriteRoutes: true})
      .get(dialogcontentI18nUrl, dialogcontentI18n, {overwriteRoutes: true})
      .get(forumsI18nUrl, forumsI18n, {overwriteRoutes: true})
      .get(widgetpickerI18nUrl, widgetpickerI18n, {overwriteRoutes: true})
      .get(dashboardwidgetI18nUrl, dashboardwidgetI18n, {overwriteRoutes: true})
      .get(toolnameMappingsUrl, toolnameMappings, {overwriteRoutes: true})
      .get(/api\/addfavourite/, 200, {overwriteRoutes: true})
      .get(/api\/removefavourite/, 200, {overwriteRoutes: true})
      .get(/api\/sites\/.*\/dashboard/, dashboardData, {overwriteRoutes: true})
      .put(/api\/sites\/.*\/dashboard/, 200, {overwriteRoutes: true})
      .put(/api\/sites\/.*\/image/, 200, {overwriteRoutes: true})
      .get(/api\/sites\/.*\/announcements/, announcementsData, {overwriteRoutes: true})
      .get(/api\/sites\/.*\/calendar/, calendarData, {overwriteRoutes: true})
      .get(/api\/sites\/.*\/forums/, forumsData, {overwriteRoutes: true})
      .get(/api\/sites\/.*\/grades/, gradesData, {overwriteRoutes: true})
      .get(/api\/sites\/.*/, siteData, {overwriteRoutes: true})
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
        background-color: rgb(248,248,248);
        padding: 40px;
      }
      #course-dashboard-container {
        padding: var(--sakai-dashboard-container-padding);
        background-color: var(--sakai-tool-bg-color);
      }

      #course-dashboard-programme {
        font-style: italic;
        font-weight: bold;
      }
        #course-dashboard-widgets {
          flex: 1;
        }
        #course-dashboard-header-and-edit-block {
          display: flex;
        }
          #course-dashboard-header-block {
            flex: 2;
          }
          #course-dashboard-edit-block {
            flex: 1;
            text-align:right;
          }
          #course-dashboard-edit-block div {
            display: inline-block;
          }
        sakai-editor div {
          padding: 0 0 20px 0;
        }

        #course-dashboard-title-and-edit-block {
          display: flex;
          margin-bottom: 20px;
        }
          #course-dashboard-title-and-edit-block h2 {
            margin-top: 0px;
          }

        #course-dashboard-l1-overview-and-widgets-block {
          display: flex;
        }
          #course-dashboard-l1-overview-block {
            flex: 2;
            margin-right: 16px;
            margin-top: 24px;
          }
          #course-dashboard-l1-widgets {
            flex: 1;
          }

        #course-dashboard-l2-header-and-overview-block {
          display: flex;
        }
          #course-dashboard-l2-header-block {
            flex: 1;
          }
          #course-dashboard-l2-course-overview {
            flex: 2;
          }

        #course-dashboard-l3-overview-and-widgets-block {
          display: flex;
        }
          #course-dashboard-l3-overview-block {
            flex: 2;
            margin-right: 16px;
            margin-top: 24px;
          }
          #course-dashboard-l3-widgets {
            flex: 2;
          }

    </style>
    <sakai-course-dashboard site-id="python101">
  `;
};
