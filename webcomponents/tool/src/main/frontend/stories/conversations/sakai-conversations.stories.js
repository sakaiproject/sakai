import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { localStyles } from "./styles/local.js";
import { conversationsI18n } from "../i18n/conversations-i18n.js";
import { playpenData } from "./data/playpen-data.js";
import { playpenStatsPage1 } from "./data/playpen-stats-page1.js";
import { playpenStatsPage2 } from "./data/playpen-stats-page2.js";
import { sandpitData } from "./data/sandpit-data.js";
import { topic3Posts } from "./data/topic3-posts.js";
import { topic1 } from "./data/topic1.js";
import { post1 } from "./data/post1.js";
import { post1Comments } from "./data/post1-comments.js";

import '../../js/conversations/sakai-conversations.js';

export default {
  title: 'Sakai Conversations',
  decorators: [storyFn => {

    parent.portal = {locale: "en-GB", userId: "adrian"};
    window.top.portal.user = window.top.portal.user || {};
    window.top.portal.user.id = "adrian";

    const post = {
      id: "post5",
      creator: "adrian",
      created: Date.now(),
      creatorDisplayName: "Adrian Fish",
      replyable: true,
      canView: true,
      canEdit: true,
      canVerify: true,
      canComment: true,
      comments: [],
      upvotes: 0,
      canUpvote: false,
    };

    const comment = {
      creator: "adrian",
      created: Date.now(),
      creatorDisplayName: "Adrian Fish",
    };

    fetchMock
      .get(/sakai-ws\/rest\/i18n\/getI18nProperties.*/, conversationsI18n, {overwriteRoutes: true})
      .get(/api\/sites\/playpen\/conversations$/, playpenData, {overwriteRoutes: true})
      .get(/api\/sites\/sandpit\/conversations$/, sandpitData, {overwriteRoutes: true})
      .get(/api\/sites\/playpen\/conversations\/stats\?page=1/, playpenStatsPage1, {overwriteRoutes: true})
      .get(/api\/sites\/playpen\/conversations\/stats\?page=2/, playpenStatsPage2, {overwriteRoutes: true})
      .get(/api\/sites\/\w+\/conversations\/agree$/, 200, {overwriteRoutes: true})
      .get(/api\/sites\/\w*\/topics\/\w*\/posts\/\w*\/upvote$/, 200, {overwriteRoutes: true})
      .get(/api\/sites\/\w*\/topics\/\w*\/posts\/\w*\/unupvote$/, 200, {overwriteRoutes: true})
      .get(/api\/posts\/\w*\/softdelete$/, 200, {overwriteRoutes: true})
      .get(/api\/posts\/\w*\/restore$/, 200, {overwriteRoutes: true})
      .get(/api\/sites\/playpen\/topics\/topic3\/posts$/, topic3Posts, {overwriteRoutes: true})
      .get(/api\/sites\/playpen\/topics\/\w*\/posts$/, "[]", {overwriteRoutes: true})
      .delete(/api\/sites\/\w+\/topics\/\w+$/, 200, {overwriteRoutes: true})
      .delete(/api\/topics\/\w+\/posts\/\w+$/, 200, {overwriteRoutes: true})
      .delete(/api\/sites\/playpen\/topics\/topic3\/posts\/\w*\/comments\/\w+$/, 200, {overwriteRoutes: true})
      .delete(/api\/sites\/\w*\/conversations\/tags\/\w+$/, 200, {overwriteRoutes: true})
      .put(/api\/sites\/\w*\/conversations\/tags\/\w+$/, 200, {overwriteRoutes: true})
      .put(/api\/sites\/\w+\/topics\/\w+$/, 200, {overwriteRoutes: true})
      .put(/api\/sites\/\w+\/topics\/\w+\/posts\/\w+$/, 200, {overwriteRoutes: true})
      .get(/api\/topics\/.*\/posts$/, "[]", {overwriteRoutes: true})
      .get(/api\/sites\/playpen\/topics\/topic3\/posts\/post1\/comments$/, post1Comments, {overwriteRoutes: true})
      .post(/api\/sites\/\w*\/conversations\/settings\/\w+$/, 200, { overwriteRoutes: true })
      .post(/api\/sites\/\w*\/topics$/, (url, opts) => {

        const requestTopic = JSON.parse(opts.body);
        return {
          id: "" + Math.floor(Math.random() * 20) + 1,
          creator: "adrian",
          created: Date.now(),
          title: requestTopic.title,
          message: requestTopic.message,
          creatorDisplayName: requestTopic.anonymous ? "Anonymous" : "Adrian Fish",
          type: requestTopic.type,
          pinned: requestTopic.pinned,
          draft: requestTopic.draft,
          canPost: true,
          canEdit: true,
          canDelete: true,
          numberOfPosts: 0,
          replies: [],
          tags: requestTopic.tags,
          groups: requestTopic.groups,
          anonymous: requestTopic.anonymous,
          visibility: requestTopic.visibility,
        };
      }, {overwriteRoutes: true})
      .post(/api\/sites\/\w*\/topics\/\w+$/, (url, opts) => {

        const requestTopic = JSON.parse(opts.body);
        return {
          id: requestTopic.id,
          creator: "adrian",
          created: Date.now(),
          title: requestTopic.title,
          message: requestTopic.message,
          creatorDisplayName: "Adrian Fish",
          type: requestTopic.type,
          canPost: true,
          canEdit: true,
          canDelete: true,
          pinned: requestTopic.pinned,
          draft: requestTopic.draft,
          replies: requestTopic.replies,
          tags: requestTopic.tags,
          visibility: requestTopic.visibility,
        };

      }, {overwriteRoutes: true})
      .post(/api\/sites\/\w*\/topics\/\w+\/posts$/, (url, opts) => {

        const requestPost = JSON.parse(opts.body);
        post.id = ""+Math.floor(Math.random() * 20) + 1;
        post.message = requestPost.message;
        post.parentPost = requestPost.parentPost;
        post.canEdit = true;
        post.canDeleteEdit = true;
        post.draft = requestPost.draft;
        post.topic = requestPost.topic;
        console.log(post);
        return post;
      }, {overwriteRoutes: true})
      .post(/api\/topics\/\w*\/posts\/\w*$/, 200, {overwriteRoutes: true})
      .post(/api\/sites\/\w+\/topics\/\w+\/pinned$/, 200, {overwriteRoutes: true})
      .post(/api\/sites\/\w+\/topics\/\w+\/locked$/, (url, opts) => {

        const locked = JSON.parse(opts.body);

        if (url.includes("topic1")) {
          const t1 = JSON.parse(topic1);
          t1.locked = locked;
          return JSON.stringify(t1);
        }
      }, {overwriteRoutes: true})
      .post(/api\/sites\/\w+\/topics\/\w+\/hidden$/, (url, opts) => {

        const hidden = JSON.parse(opts.body);

        if (url.includes("topic1")) {
          const t1 = JSON.parse(topic1);
          t1.hidden = hidden;
          return JSON.stringify(t1);
        }
      }, {overwriteRoutes: true})

      .post(/api\/sites\/\w+\/topics\/\w+\/bookmarked$/, 200, {overwriteRoutes: true})
      .post(/api\/sites\/\w+\/topics\/\w+\/reactions$/, (url, opts) => {

        return {
          "LOVE_IT": 4,
          "GOOD_QUESTION": 1,
          "GOOD_IDEA": 1,
          "KEY": 1
        };
      }, {overwriteRoutes: true})
      .post(/api\/sites\/\w+\/topics\/\w+\/posts\/\w+\/reactions$/, (url, opts) => {

        return {
          "LOVE_IT": 4,
          "GOOD_ANSWER": 1,
          "GOOD_IDEA": 1,
          "KEY": 1
        };
      }, {overwriteRoutes: true})
      .post(/api\/sites\/\w+\/topics\/\w+\/posts\/post1\/locked$/, (url, opts) => {

        const locked = JSON.parse(opts.body);

        if (url.includes("post1")) {
          const p1 = JSON.parse(post1);
          p1.locked = locked;
          return JSON.stringify(p1);
        }
      }, {overwriteRoutes: true})
      .post(/api\/sites\/\w+\/topics\/\w+\/posts\/post1\/hidden$/, (url, opts) => {

        const hidden = JSON.parse(opts.body);

        if (url.includes("post1")) {
          const p1 = JSON.parse(post1);
          p1.hidden = hidden;
          return JSON.stringify(p1);
        }
      }, {overwriteRoutes: true})

      .post(/api\/topics\/\w*\/posts\/\w*\/comments$/, (url, opts) => {

        const requestComment = JSON.parse(opts.body);
        comment.id = ""+Math.floor(Math.random() * 20) + 1;
        comment.message = requestComment.message;
        comment.postId = requestComment.postId;
        comment.canEdit = true;
        comment.canDelete = true;
        return comment;
      }, {overwriteRoutes: true})
      .post(/api\/sites\/\w*\/conversations\/tags$/, (url, opts) => {

        const requestTags = JSON.parse(opts.body);
        const tags = requestTags.map(rt => ({id: Math.floor(Math.random() * 20) + 1, label: rt.label}));
        return tags;
      }, {overwriteRoutes: true})
      .put(/api\/sites\/\w+\/topics\/\w+\/posts\/\w+\/comments\/\w*$/, (url, opts) => {

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

export const BasicDisplay = () => {

  return html`
    ${unsafeHTML(localStyles)}
    <div>
      <sakai-conversations site-id="playpen"></sakai-conversations>
    </div>
  `;
};

export const NoTopics = () => {

  return html`
    ${unsafeHTML(localStyles)}
    <div>
      <sakai-conversations site-id="sandpit"></sakai-conversations>
    </div>
  `;
};
