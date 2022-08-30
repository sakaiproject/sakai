import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { conversationsI18n } from "../i18n/conversations-i18n.js";
import { topic1 } from "./data/discussion-topic1.js";
import { postStyles } from "./styles/sakai-post.js";

import '../../js/conversations/sakai-topic.js';

export default {
  title: 'Sakai Discussion Topic',
  decorators: [storyFn => {

    parent.portal = {locale: "en-GB", user: { id: "mike"}};
    window.top.portal = parent.portal;

    const post = {
      id: "post5",
      creator: "adrian",
      created: Date.now(),
      creatorDisplayName: "Adrian Fish",
      replyable: true,
    };

    fetchMock
      .get(/sakai-ws\/rest\/i18n\/getI18nProperties.*/, conversationsI18n, {overwriteRoutes: true})
      .get(/api\/conversations\/topics\/topic1/, topic1, {overwriteRoutes: true})
      .post(/api\/sites\/playpen\/topics\/topic1\/posts/, (url, opts) => {

        const requestPost = JSON.parse(opts.body);
        post.id = ""+Math.floor(Math.random() * 20) + 1;
        post.message = requestPost.message;
        post.parentPost = requestPost.parentPost;
        post.parentTopic = requestPost.parentTopic;
        post.canView = true;
        post.myReactions = {};
        post.reactionTotals = {};
        return post;
      }, {overwriteRoutes: true})
      .post(/api\/sites\/playpen\/topics\/topic3\/posts/, (url, opts) => {

        const requestPost = JSON.parse(opts.body);
        post.id = ""+Math.floor(Math.random() * 20) + 1;
        post.message = requestPost.message;
        post.parentPost = requestPost.parentPost;
        post.parentTopic = requestPost.parentTopic;
        post.canView = true;
        post.links = [
          { "href": `/api/sites/playpen/topics/topic1/posts/${post.id}/hidden`, "rel": "hidden" },
          { "href": `/api/sites/playpen/topics/topic1/posts/${post.id}/locked`, "rel": "locked" },
          { "href": `/api/sites/playpen/topics/topic1/posts/${post.id}/reactions`, "rel": "reactions" },
          { "href": `/api/sites/playpen/topics/topic1/posts`, "rel": "reply" }
        ];
        post.myReactions = {};
        post.reactionTotals = {};
        return post;
      }, {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});
    return storyFn();
  }],
};

export const BasicDisplay = () => {

  return html`
    ${unsafeHTML(postStyles)}
    <div>
      <sakai-topic topic="${topic1}"></sakai-topic>
    </div>
  `;
};
