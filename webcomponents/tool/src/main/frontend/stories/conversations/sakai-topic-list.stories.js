import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { conversationsI18n } from "../i18n/conversations-i18n.js";
import { topicListData } from "./data/sakai-topic-list.js";

import '../../js/conversations/sakai-topic-list.js';

export default {
  title: 'Sakai Topic List',
  decorators: [storyFn => {
    parent.portal = {locale: "en-GB"};
    fetchMock
      .get(/sakai-ws\/rest\/i18n\/getI18nProperties.*/, conversationsI18n, {overwriteRoutes: true})
      .get(/api\/topics.*$/, topicListData, {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});
    return storyFn();
  }],
};

export const BasicDisplay = () => {

  return html`
    <div>
      <sakai-topic-list topics="${topicListData}"></sakai-topic-list>
    </div>
  `;
};
