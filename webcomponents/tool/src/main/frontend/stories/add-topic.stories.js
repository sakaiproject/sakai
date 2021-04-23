import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { sakaiStyles } from "./styles/sakai-styles.js";
import { addTopicStyles } from "./styles/add-topic-styles.js";
import { conversationsI18n } from "./i18n/conversations-i18n.js";
import { topic1Data } from "./data/conversations/topic1-data.js";

import '../js/conversations/add-topic.js';

export default {
  title: 'Add Topic',
  decorators: [storyFn => {

    parent.portal = {locale: "en-GB", userId: "mike"};

    fetchMock
      .get(/sakai-ws\/rest\/i18n\/getI18nProperties.*/, conversationsI18n, {overwriteRoutes: true})
      .get(/api\/conversations\/topics\/topic1/, topic1Data, {overwriteRoutes: true})
      .post(/api\/topics$/, (url, opts) => {

        const requestTopic = JSON.parse(opts.body);
        return {
          id: "" + Math.floor(Math.random() * 20) + 1,
          creator: "adrian",
          created: Date.now(),
          title: requestTopic.title,
          message: requestTopic.message,
          creatorDisplayName: "Adrian Fish",
          type: requestTopic.type,
          pinned: requestTopic.pinned,
          draft: requestTopic.draft,
          visibility: requestTopic.visibility,
        };
      }, {overwriteRoutes: true})
      .post(/api\/topics\/.*/, (url, opts) => {

        const requestTopic = JSON.parse(opts.body);
        return {
          id: requestTopic.id,
          creator: "adrian",
          created: Date.now(),
          title: requestTopic.title,
          message: requestTopic.message,
          creatorDisplayName: "Adrian Fish",
          type: requestTopic.type,
          pinned: requestTopic.pinned,
          draft: requestTopic.draft,
          visibility: requestTopic.visibility,
        };

      }, {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});

    return storyFn();
  }],
};

export const AddTopic = () => {

  return html`
    ${unsafeHTML(sakaiStyles)}
    ${unsafeHTML(addTopicStyles)}
    <div>
      <add-topic about-reference="/site/playpen"></add-topic>
    </div>
  `;
};

export const UpdateTopic = () => {

  return html`
    ${unsafeHTML(sakaiStyles)}
    ${unsafeHTML(addTopicStyles)}
    <div>
      <add-topic @topic-saved=${e => console.log(e.detail.topic)} topic="${topic1Data}"></add-topic>
    </div>
  `;
};
