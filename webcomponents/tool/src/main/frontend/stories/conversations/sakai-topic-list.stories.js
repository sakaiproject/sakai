import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { sakaiStyles } from "../styles/sakai-styles.js";
import { topicListStyles } from "./styles/sakai-topic-list.js";
import { conversationsI18n } from "../i18n/conversations-i18n.js";
import { topicListData } from "./data/sakai-topic-list.js";

import '../../js/conversations/sakai-topic-list.js';

export default {
  title: 'Sakai Topic List',
  decorators: [storyFn => {
    parent.portal = {locale: "en-GB"};
    fetchMock
      .get(/sakai-ws\/rest\/i18n\/getI18nProperties.*/, conversationsI18n, {overwriteRoutes: true})
      .get(/api\/sites\/.*\/topics/, topicListData, {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});
    return storyFn();
  }],
};

export const BasicDisplay = () => {

  return html`
    ${unsafeHTML(sakaiStyles)}
    <div>
      <sakai-topic-list site-id="playpen"></sakai-topic-list>
    </div>
  `;
};
