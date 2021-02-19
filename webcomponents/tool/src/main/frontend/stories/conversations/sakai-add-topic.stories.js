import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { conversationsI18n } from "../i18n/conversations-i18n.js";
import { topic1 } from "./data/topic1.js";

import '../../js/conversations/sakai-add-topic.js';

export default {
  title: 'Sakai Add Topic',
  decorators: [storyFn => {

    parent.portal = {locale: "en-GB", userId: "mike"};

    fetchMock
      .get(/sakai-ws\/rest\/i18n\/getI18nProperties.*/, conversationsI18n, {overwriteRoutes: true})
      .get(/api\/conversations\/topics\/topic1/, topic1, {overwriteRoutes: true})
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

const availableTags = [ "pheasant", "chicken", "turkey", "bigbird" ];


export const AddTopic = () => {

  return html`
    <div>
      <sakai-add-topic about-reference="/site/playpen" available-tags="${JSON.stringify(availableTags)}"></sakai-add-topic>
    </div>
  `;
};


export const UpdateTopic = () => {

  return html`
    <div>
      <sakai-add-topic @topic-saved=${e => console.log(e.detail.topic)} topic="${topic1}" available-tags="${JSON.stringify(availableTags)}"></sakai-add-topic>
    </div>
  `;
};
