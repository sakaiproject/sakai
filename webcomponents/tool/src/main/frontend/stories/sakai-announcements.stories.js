import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { styles } from "./styles/sakai-styles.js";
import { announcementsI18n } from "./i18n/announcements-i18n.js";
import { announcementsData } from "./data/home-announcements-data.js";

import '../js/announcements/sakai-announcements.js';

export default {
  title: 'Sakai Announcements',
  decorators: [(storyFn) => {
    parent.portal = {locale: "en-GB"};
    fetchMock
      .get(/.*i18n.*announcements$/, announcementsI18n, {overwriteRoutes: true})
      .get(/api\/users\/.*\/announcements/, announcementsData, {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});
    return storyFn();
  }],
};

export const BasicDisplay = () => {

  return html`
    ${unsafeHTML(styles)}
    <div style="width: 400px;">
      <sakai-announcements user-id="adrian">
    </div>
  `;
};
