import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { sakaiStyles } from "../styles/sakai-styles.js";
import { topicListStyles } from "./styles/sakai-topic-list.js";
import { topicSummaryStyles } from "./styles/sakai-topic-summary.js";
import { addTopicStyles } from "./styles/sakai-add-topic.js";
import { topicStyles } from "./styles/sakai-topic.js";
import { postStyles } from "./styles/sakai-post.js";
import { conversationsStyles } from "./styles/sakai-conversations.js";
import { conversationsI18n } from "../i18n/conversations-i18n.js";
import { playpenData } from "./data/playpen-data.js";
import { sandpitData } from "./data/sandpit-data.js";
import { topic3Posts } from "./data/topic3-posts.js";

import '../../js/conversations/sakai-comment-editor.js';

export default {
  title: 'Sakai Comment Editor',
  decorators: [storyFn => {

    parent.portal = {locale: "en-GB", userId: "adrian"};

    const comment = {
      creator: "adrian",
      created: Date.now(),
      creatorDisplayName: "Adrian Fish",
    };

    fetchMock
      .get(/sakai-ws\/rest\/i18n\/getI18nProperties.*/, conversationsI18n, {overwriteRoutes: true})
      .post(/api\/posts\/\w*\/comments$/, (url, opts) => {

        const requestComment = JSON.parse(opts.body);
        comment.id = ""+Math.floor(Math.random() * 20) + 1;
        comment.message = requestComment.message;
        comment.postId = requestComment.postId;
        comment.canEdit = true;
        comment.canDelete = true;
        return comment;
      }, {overwriteRoutes: true})
      .post(/api\/posts\/\w*\/comments\/\w*$/, (url, opts) => {

        const requestComment = JSON.parse(opts.body);

        comment.id = requestComment.id;
        comment.message = requestComment.message;
        comment.postId = requestComment.postId;
        comment.canEdit = true;
        comment.canDelete = true;
        return comment;
      }, {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});
    return storyFn();
  }],
};

export const NoComment = () => {

  return html`
    ${unsafeHTML(sakaiStyles)}
    <div>
      <sakai-comment-editor post-id="post1"></sakai-comment-editor>
    </div>
  `;
};

export const WithComment = () => {

  const comment = { message: "Chickens!" };

  return html`
    ${unsafeHTML(sakaiStyles)}
    <div>
      <sakai-comment-editor post-id="post1" comment="${JSON.stringify(comment)}"></sakai-comment-editor>
    </div>
  `;
};
