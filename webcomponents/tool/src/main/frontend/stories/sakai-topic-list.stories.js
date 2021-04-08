import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { styles } from "./styles/sakai-styles.js";
import { conversationsI18n } from "./i18n/conversations-i18n.js";
import { topicsData } from "./data/conversations/topics-data.js";

import '../js/conversations/sakai-topic-list.js';

export default {
  title: 'Sakai Topic List',
  decorators: [storyFn => {
    parent.portal = {locale: "en-GB"};
    fetchMock
      .get(/sakai-ws\/rest\/i18n\/getI18nProperties.*/, conversationsI18n, {overwriteRoutes: true})
      .get(/api\/sites\/.*\/topics/, topicsData, {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});
    return storyFn();
  }],
};

export const BasicDisplay = () => {

  return html`
    ${unsafeHTML(styles)}
    <div>
      <sakai-topic-list site-id="playpen"></sakai-topic-list>
    </div>
  `;
};
