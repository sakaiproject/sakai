import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { conversationsI18n } from "../i18n/conversations-i18n.js";
import { topic1 } from "./data/topic1.js";

import '../../js/conversations/sakai-topic-summary.js';

export default {
  title: 'Sakai Topic Summary',
  decorators: [storyFn => {
    parent.portal = {locale: "en-GB"};
    fetchMock
      .get(/sakai-ws\/rest\/i18n\/getI18nProperties.*/, conversationsI18n, {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});
    return storyFn();
  }],
};

export const BasicDisplay = () => {

  return html`
    <div>
      <sakai-topic-summary topic="${topic1}" @show-topic=${e => console.log(e.detail.topicId)}></sakai-topic-summary>
    </div>
  `;
};
