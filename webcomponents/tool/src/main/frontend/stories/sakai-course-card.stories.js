import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { withA11y } from "@storybook/addon-a11y";
import { styles } from "./styles/sakai-styles.js";
import { coursecardI18n } from "./i18n/course-card-i18n.js";
import { toolnameMappings } from "./data/toolname-mappings.js";

import '../js/sakai-course-card.js';

export default {
  title: 'Sakai Course Card',
  decorators: [withA11y, (storyFn) => {
    parent.portal = {locale: "en-GB"};
    fetchMock
      .get(/sakai-ws\/rest\/i18n\/getI18nProperties.*/, coursecardI18n, {overwriteRoutes: true})
      .get(/sakai-ws\/rest\/i18n\/getI18nProperties.*toolname-mappings.*/, toolnameMappings, {overwriteRoutes: true})
      .get(/addfavourite/, 200, {overwriteRoutes: true})
      .get(/removefavourite/, 200, {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});
    return storyFn();
  }],
};

export const WithImage = () => {

  const courseData = {
    id: "xyz",
    title: "Marine Biology 101",
    code: "MB 101",
    alerts: ["gradebook", "forums"],
    favourite: false,
    image: "https://static.wixstatic.com/media/e441d1_6c7bdbdb1ef84fc6bfc09f8365b77e67~mv2.png"
  };

  return html`
    ${unsafeHTML(styles)}
    <style>
      body { background-color: lightgrey; padding: 40px;}
    </style>
    <sakai-course-card course-data=${JSON.stringify(courseData)} tool-urls='{"assignments": "http://www.gmail.com"}'>
  `;
};

export const TwoWithData = () => {

  const courseData1 = {
    id: "xyz",
    title: "Biogeochemical Oceanography",
    code: "BCO 104",
    alerts: ["forums"],
    favourite: false,
  };

  const courseData2 = {
    id: "xyz",
    title: "Marine Biology 101",
    code: "MB 101",
    alerts: ["gradebook", "forums"],
    favourite: false,
    image: "https://static.wixstatic.com/media/e441d1_6c7bdbdb1ef84fc6bfc09f8365b77e67~mv2.png"
  };

  return html`
    ${unsafeHTML(styles)}
    <style>
      body { background-color: lightgrey !important; padding: 40px;}
    </style>
    <sakai-course-card course-data=${JSON.stringify(courseData1)}></sakai-course-card>
    <br />
    <br />
    <sakai-course-card course-data=${JSON.stringify(courseData2)} tool-urls='{"assignments": "http://www.gmail.com"}'></sakai-course-card>
  `;
};
