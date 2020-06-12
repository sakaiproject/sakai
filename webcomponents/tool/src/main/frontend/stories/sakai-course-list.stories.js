import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { withA11y } from "@storybook/addon-a11y";
import { styles } from "./sakai-styles.js";
import { coursecardI18n } from "./course-card-i18n.js";
import { courselistI18n } from "./course-list-i18n.js";
import { toolnameMappings } from "./toolname-mappings.js";

import '../js/sakai-course-list.js';

export default {
  title: 'Sakai Course List',
  decorators: [withA11y, (storyFn) => {
    parent.portal = {locale: "en-GB"};
    const baseUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=en-GB&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=";
    const coursecardI18nUrl = `${baseUrl}coursecard`;
    const courselistI18nUrl = `${baseUrl}courselist`;
    fetchMock
      .get(coursecardI18nUrl, coursecardI18n, {overwriteRoutes: true})
      .get(courselistI18nUrl, courselistI18n, {overwriteRoutes: true})
      .get(/addfavourite/, 200, {overwriteRoutes: true})
      .get(/removefavourite/, 200, {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});
    return storyFn();
  }],
};

export const WithData = () => {

  const courseData = [{
    id: "bio",
    title: "Biogeochemical Oceanography",
    code: "BCO 104",
    url: "http://www.facebook.com",
    alerts: ["forums"],
    favourite: false,
    course: true,
  },
  {
    id: "fre",
    title: "French 101",
    code: "LING",
    url: "http://www.ebay.co.uk",
    alerts: ["assignments", "forums"],
    favourite: true,
    course: true,
  },
  {
    id: "footsoc",
    title: "Football Society",
    code: "FOOTSOC",
    url: "http://www.open.ac.uk",
    favourite: false,
    project: true,
  }];

  return html`
    ${unsafeHTML(styles)}
    <sakai-course-list course-data="${JSON.stringify(courseData)}">
  `;
};
