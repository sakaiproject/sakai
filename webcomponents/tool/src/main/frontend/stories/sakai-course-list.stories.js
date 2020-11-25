import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { withA11y } from "@storybook/addon-a11y";
import { styles } from "./styles/sakai-styles.js";
import { coursecardI18n } from "./i18n/course-card-i18n.js";
import { courselistI18n } from "./i18n/course-list-i18n.js";
import { sitesData } from "./data/sites-data.js";
import { toolnameMappings } from "./data/toolname-mappings.js";

import '../js/sakai-course-list.js';

export default {
  title: 'Sakai Course List',
  decorators: [withA11y, (storyFn) => {
    parent.portal = {locale: "en-GB"};
    fetchMock
      .get(/sakai-ws\/rest\/i18n\/getI18nProperties.*coursecard/, coursecardI18n, {overwriteRoutes: true})
      .get(/sakai-ws\/rest\/i18n\/getI18nProperties.*courselist/, courselistI18n, {overwriteRoutes: true})
      .get(/sakai-ws\/rest\/i18n\/getI18nProperties.*toolname-mappings.*/, toolnameMappings, {overwriteRoutes: true})
      .get(/api\/users\/.*\/sites/, sitesData, {overwriteRoutes: true})
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
