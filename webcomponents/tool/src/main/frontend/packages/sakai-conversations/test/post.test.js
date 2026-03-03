import "../sakai-post.js";
import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import { DISCUSSION, QUESTION, REACTION_ICONS } from "../src/sakai-conversations-constants.js";
import fetchMock from "fetch-mock";
window.top.portal = { user: { id: "user1" }};

describe("post tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock.get(data.i18nUrl, data.i18n);

    delete data.answer.isInstructor;
    delete data.answer.canEdit;
    delete data.answer.canDelete;
    delete data.answer.comments;

    delete data.post1.isInstructor;
    delete data.post1.canEdit;
    delete data.post1.canDelete;
    delete data.post1.comments;
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it ("does not render if no post is supplied", async () => {

    const el = await fixture(html`<sakai-post></sakai-post>`);

    await waitUntil(() => el._i18n);

    expect(el.querySelector("div")).to.not.exist;
  });

  it ("renders an instructor answer with no comments", async () => {

    data.answer.isInstructor = true;

    const el = await fixture(html`
      <sakai-post .post=${data.answer}
          post-type="${data.topic1.type}"
          is-instructor>
      <sakai-post>
    `);

    await waitUntil(() => el._i18n && el.post);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector("div")).to.exist;
    expect(el.querySelector(`#post-${data.answer.id}`).classList.contains("instructor")).to.be.true;

    // No comments have been set on this post object
    expect(el.querySelector(`#post-${data.answer.id}`).classList.contains("post-without-comment-block")).to.be.true;

    expect(el.querySelector(".conv-author-details > div").innerHTML).to.contain(data.answer.creatorDisplayName);
    expect(el.querySelector(".conv-author-details div.post-creator-instructor")).to.exist;
    expect(el.querySelector(".conv-author-details div:nth-child(3)").innerHTML).to.contain(data.answer.formattedCreatedDate);

    const postEl = el.querySelector(".post-main");
    expect(postEl).to.exist;

    expect(postEl.querySelector("div:nth-child(2) > div:nth-child(2)").innerHTML).to.contain(data.answer.message);

    expect(postEl.querySelectorAll("sakai-post").length).to.equal(0);
  });

  it ("edits an instructor answer", async () => {

    data.answer.isInstructor = true;
    data.answer.canEdit = true;

    const newMessage = "chips";

    const el = await fixture(html`
      <sakai-post .post=${data.answer}
          post-type="${data.topic1.type}"
          is-instructor>
      <sakai-post>
    `);

    await waitUntil(() => el._i18n && el.post);

    await elementUpdated(el);

    const postEl = el.querySelector(".post-main");
    expect(postEl).to.exist;

    const ddButton = postEl.querySelector("div.dropdown > button");
    expect(ddButton).to.exist;

    expect(el.querySelector(".post-editor-block")).to.not.exist;

    setTimeout(() => ddButton.click());

    await oneEvent(el, "shown.bs.dropdown");

    await expect(el).to.be.accessible();

    const editButton = el.querySelector("ul > li > button");
    expect(editButton).to.exist;

    editButton.click();

    await elementUpdated(el);

    expect(el.querySelector(".post-editor-block")).to.exist;

    await expect(el).to.be.accessible();

    const editor = el.querySelector("sakai-editor");
    expect(editor).to.exist;

    editor.setContent(newMessage);

    fetchMock.put(data.answer.links.find(l => l.rel === "self").href, ({ url, options }) => JSON.parse(options.body));

    el.querySelector(".post-editor-block input[type='button']").click();

    const { detail } = await oneEvent(el, "post-updated");

    expect(detail.post.message).to.equal(newMessage);
  });

  it ("renders a post with comments", async () => {

    data.answer.comments = [ data.comment ];
    data.answer.numberOfComments = 1;

    const el = await fixture(html`
      <sakai-post .post=${data.answer}
          post-type="${data.topic1.type}"
          is-instructor>
      <sakai-post>
    `);

    await waitUntil(() => el._i18n && el.post);

    await elementUpdated(el);

    expect(el.querySelector(`#post-${data.answer.id}`).classList.contains("post-without-comment-block")).to.be.false;

    // Comments are showing by default
    expect(el.querySelector(".post-comments-block-inner")).to.exist;

    expect(el.querySelectorAll("sakai-comment").length).to.equal(data.answer.comments.length);

    const commentsToggle = el.querySelector(".post-comment-toggle-icon").closest("a");
    expect(commentsToggle).to.exist;

    commentsToggle.click();

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector(".post-comments-block-inner")).to.not.exist;

    expect(el.querySelector(".post-comment-toggle-icon").nextElementSibling.innerHTML).to.contain(data.answer.numberOfComments);
  });

  it ("renders a discussion post", async () => {

    data.post1.isInstructor = true;

    const el = await fixture(html`
      <sakai-post .post=${data.post1}
          post-type="${DISCUSSION}"
          is-instructor>
      <sakai-post>
    `);

    await waitUntil(() => el._i18n && el.post);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Reactions and upvotes not allowed
    expect(el.querySelector(".reactions-block")).to.not.exist;

    expect(el.querySelector(`#discussion-post-block-${data.post1.id}`)).to.exist;

    // No replies yet, so no bar needed
    expect(el.querySelector(".discussion-post-vbar")).to.not.exist;

    // If the post is not viewed, it is "new"
    expect(el.querySelector(".discussion-post-content-wrapper.new")).to.exist;
    expect(el.querySelector(".discussion-post-new")).to.exist;

    // When not editing, the reactions etc should be visible
    expect(el.querySelector(".discussion-post-bottom-bar")).to.exist;
  });

  it ("renders a discussion thread", async () => {

    data.thread.isInstructor = true;

    const el = await fixture(html`
      <sakai-post .post=${data.thread}
          post-type="${DISCUSSION}"
          is-instructor>
      <sakai-post>
    `);

    await waitUntil(() => el._i18n && el.post);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector(".discussion-post-vbar")).to.exist;

    // When not editing, the reactions etc should be visible
    expect(el.querySelector(".discussion-post-bottom-bar")).to.exist;

    expect(el.querySelectorAll(".discussion-post-block").length).to.equal(3);

    data.thread.posts.forEach(p => {
      expect(el.querySelector(`#discussion-post-block-${p.id}`)).to.exist;
    });

    // Now test collapsing the posts
    const repliesToggle = el.querySelector(".post-replies-toggle");
    expect(repliesToggle).to.exist;
    repliesToggle.click();
    await elementUpdated(repliesToggle);
    expect(el.querySelectorAll(".discussion-post-block").length).to.equal(1);
  });

  it ("replies to a post", async () => {

    data.post1.canReply = true;

    const el = await fixture(html`
      <sakai-post .post=${data.post1}
          post-type="${DISCUSSION}">
      <sakai-post>
    `);

    await waitUntil(() => el._i18n && el.post);

    await elementUpdated(el);

    expect(el.querySelector(".discussion-post-reply-block")).to.not.exist;
    fetchMock.post(data.post1.links.find(l => l.rel === "reply").href, ({ url, options }) => JSON.parse(options.body));

    // Click on reply
    const replyButton = el.querySelector(".post-reply-button");
    expect(replyButton).to.exist;
    replyButton.click();
    await elementUpdated(el);
    const editor = el.querySelector(".post-reply-editor-block sakai-editor");
    expect(editor).to.exist;

    await expect(el).to.be.accessible();

    const newMessage = "chips";
    editor.setContent(newMessage);

    // Click save
    setTimeout(() => el.querySelector(".post-reply-editor-block input.active").click());

    const { detail } = await oneEvent(el, "post-updated");
    expect(detail.post.posts.length).to.equal(1);
    expect(detail.post.posts[0].message).to.equal(newMessage);
  });

  it ("deletes a post", async () => {

    data.post1.canDelete = true;

    const el = await fixture(html`
      <sakai-post .post=${data.post1}
          post-type="${DISCUSSION}"
          skip-delete-confirm>
      <sakai-post>
    `);

    await waitUntil(() => el._i18n && el.post);

    await elementUpdated(el);

    const ddButton = el.querySelector("div.dropdown > button");
    expect(ddButton).to.exist;

    setTimeout(() => ddButton.click());

    await oneEvent(el, "shown.bs.dropdown");

    await expect(el).to.be.accessible();

    const deleteButton = el.querySelector(".dropdown-menu button");
    expect(deleteButton).to.exist;

    fetchMock.delete(data.post1.links.find(l => l.rel === "delete").href, 200);

    deleteButton.click();

    const { detail } = await oneEvent(el, "post-deleted");
    expect(detail.post.id).to.equal(data.post1.id);
  });

  it ("toggles hidden and locked", async () => {

    data.post1.canModerate = true;

    const el = await fixture(html`
      <sakai-post .post=${data.post1}
          post-type="${DISCUSSION}"
          skip-delete-confirm>
      <sakai-post>
    `);

    await waitUntil(() => el._i18n && el.post);

    await elementUpdated(el);

    const ddButton = el.querySelector("div.dropdown > button");
    expect(ddButton).to.exist;

    setTimeout(() => ddButton.click());

    await oneEvent(el, "shown.bs.dropdown");

    await expect(el).to.be.accessible();

    const hiddenButton = el.querySelector(".conv-dropdown-menu button");
    expect(hiddenButton).to.exist;

    fetchMock.post(data.post1.links.find(l => l.rel === "hide").href, 200);

    hiddenButton.click();

    let { detail } = await oneEvent(el, "post-updated");
    expect(detail.post.hidden).to.be.true;

    hiddenButton.click();
    ({ detail } = await oneEvent(el, "post-updated"));
    expect(detail.post.hidden).to.be.false;

    const lockedButton = el.querySelector(".conv-dropdown-menu li:nth-child(2) > button");
    expect(lockedButton).to.exist;

    fetchMock.post(data.post1.links.find(l => l.rel === "lock").href,
      ({ url, options }) => ({ ...data.post1, locked: JSON.parse(options.body) }));

    lockedButton.click();
    ({ detail } = await oneEvent(el, "post-updated"));
    expect(detail.post.locked).to.be.true;
  });

  it ("displays discussion post reactions correctly", async () => {

    data.post1.canReact = true;

    const el = await fixture(html`
      <sakai-post .post=${data.post1}
          post-type="${DISCUSSION}"
          reactions-allowed>
      <sakai-post>
    `);

    await waitUntil(() => el._i18n && el.post);

    await elementUpdated(el);

    expect(el.querySelectorAll(".reaction-block").length).to.equal(Object.keys(data.post1.reactionTotals).length);

    Object.entries(data.post1.reactionTotals).forEach((pair, index) => {
      expect(el.querySelector(`.reaction-block:nth-child(${index + 1}) > div:nth-child(2)`).innerHTML).to.contain(pair[1]);
    });

    // Now open the reactions bar
    const ddButton = el.querySelector(`#post-reactions-${data.post1.id}`);
    expect(ddButton).to.exist;

    setTimeout(() => ddButton.click());

    await oneEvent(el, "shown.bs.dropdown");

    await expect(el).to.be.accessible();

    Object.entries(data.post1.myReactions).forEach(pair => {

      const reactionButton = el.querySelector(`button[data-reaction="${pair[0]}"]`);
      expect(reactionButton).to.exist;
      if (pair[1]) {
        expect(reactionButton.classList.contains("reaction-on")).to.be.true;
      }
    });

    // Now find the "KEY" reaction and click it
    const keyReaction = Object.entries(data.post1.myReactions).find(pair => pair[0] === "KEY");
    expect(keyReaction[1]).to.be.false;
    const reactionButton = el.querySelector("button[data-reaction='KEY']");
    expect(reactionButton).to.exist;

    fetchMock.post(data.post1.links.find(l => l.rel === "react").href, { "THUMBS_UP": 3, "KEY": 3 });

    reactionButton.click();

    const { detail } = await oneEvent(el, "post-updated");

    expect(detail.post.reactionTotals["KEY"]).to.equal(3);
  });

  it ("displays discussion post upvotes correctly", async () => {

    const el = await fixture(html`
      <sakai-post .post=${data.post1}
          post-type="${DISCUSSION}">
      <sakai-post>
    `);

    await waitUntil(() => el._i18n && el.post);

    await elementUpdated(el);

    const upvoteButton = el.querySelector(".upvote-button");

    expect(upvoteButton).to.exist;
    expect(el.querySelector(".upvote div:nth-child(3)")).to.exist;
    expect(el.querySelector(".upvote div:nth-child(3)").innerHTML).to.contain(data.post1.upvotes);

    let upvotesBefore = data.post1.upvotes;
    fetchMock.get(data.post1.links.find(l => l.rel === "upvote").href, 200);

    // Upvote it
    upvoteButton.click();

    let { detail } = await oneEvent(el, "post-updated");

    expect(detail.post.upvotes).to.equal(upvotesBefore + 1);

    fetchMock.get(data.post1.links.find(l => l.rel === "unupvote").href, 200);

    //  Unupvote it
    upvoteButton.click();

    ({ detail } = await oneEvent(el, "post-updated"));

    expect(detail.post.upvotes).to.equal(upvotesBefore);
  });

  it ("handles grading correctly", async () => {

    data.post1.canGrade = true;

    const el = await fixture(html`
      <sakai-post .post=${data.post1}
          site-id="${data.siteId}"
          post-type="${DISCUSSION}"
          grading-item-id="${data.gradingItemId}">
      <sakai-post>
    `);

    await waitUntil(() => el._i18n && el.post);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    const gradeButton = el.querySelector(".dropdown-toggle");
    expect(gradeButton).to.exist;

    setTimeout(() => gradeButton.click());

    await oneEvent(el, "shown.bs.dropdown");

    await expect(el).to.be.accessible();

    const gradePoints = 25;
    const gradeInput = el.querySelector(`#post-${data.post1.id}-grade-dropdown input[type="text"]`);
    expect(gradeInput).to.exist;
    gradeInput.value = gradePoints;

    const gradeComment = "Great";
    el._gradeComment = gradeComment;

    fetchMock.post(`/api/sites/${data.siteId}/grades/${data.gradingItemId}/${data.post1.creator}`, {});
    fetchMock.post(`/api/sites/${data.siteId}/conversations/cache/clear`, {});

    const saveGradeButton = el.querySelector(`#post-${data.post1.id}-grade-dropdown button`);
    expect(saveGradeButton).to.exist;
    saveGradeButton.click();

    await oneEvent(el, "hidden.bs.dropdown");
  });
});
