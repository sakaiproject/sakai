import "../sakai-topic.js";
import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import fetchMock from "fetch-mock";

import sinon from "sinon";
import * as constants from "../src/sakai-conversations-constants.js";
describe("sakai-topic tests", () => {

  window.top.portal = { siteId: data.siteId, siteTitle: data.siteTitle, user: { id: "user1", timezone: "Europe/London" } };
  window.MathJax = { Hub: { Queue: () => {} } };

  const postsUrl = `${data.discussionTopic.links.find(l => l.rel === "posts").href}?page=0`;
  const markViewedUrl = data.discussionTopic.links.find(l => l.rel === "markpostsviewed").href;

  beforeEach(() => {
    fetchMock.mockGlobal();

    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(postsUrl, [])
      .post(markViewedUrl, 200);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it("renders a discussion topic", async () => {

    const unviewedPosts = [
      { id: "post1", message: "Post 1", viewed: false, canView: true },
      { id: "post2", message: "Post 2", viewed: false, canView: true }
    ];

    const topic = { ...data.discussionTopic, posts: unviewedPosts };

    const el = await fixture(html`
      <sakai-topic
          .topic=${topic}>
      </sakai-topic>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector(".topic")).to.exist;

    expect(el.querySelector(".conv-graded-no-grading-item-id-warning")).to.not.exist;

    // No status icon for discussion topics
    expect(el.querySelector(".topic-status-icon-and-text")).to.not.exist;

    expect(el.querySelector(".conversations-topic__creator-name").innerHTML).to.contain(data.discussionTopic.creatorDisplayName);
    expect(el.querySelector(".topic-question-asked").innerHTML).to.contain(el._i18n.posted);
    expect(el.querySelector(".conversations-topic__created-date").innerHTML).to.contain(data.discussionTopic.formattedCreatedDate);
    expect(el.querySelector(`.author-and-tools sakai-user-photo[user-id="${data.discussionTopic.creator}"]`)).to.exist;

    expect(el.querySelector(".conversations-topic__title").innerHTML).to.include(data.discussionTopic.title);
    expect(el.querySelector(".topic-message").innerHTML).to.include(data.discussionTopic.message);
  });

  it("renders topic tags correctly", async () => {

    const topic = { ...data.discussionTopic };

    const el = await fixture(html`
      <sakai-topic .topic=${topic}>
      </sakai-topic>
    `);

    await elementUpdated(el);

    // Check if the tags container exists
    const tagsContainer = el.querySelector(".topic-tags");
    expect(tagsContainer).to.exist;

    // Check if all tags are rendered
    const tagElements = tagsContainer.querySelectorAll(".tag");
    expect(tagElements.length).to.equal(data.discussionTopic.tags.length);

    // Verify each tag's content
    data.discussionTopic.tags.forEach((tag, index) => {
      expect(tagElements[index].textContent).to.equal(tag.label);
    });
  });

  it("renders a question topic", async () => {

    const topic = { ...data.questionTopic, resolved: true };

    const el = await fixture(html`
      <sakai-topic
          .topic=${topic}>
      </sakai-topic>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // This question hase been resolved. It should have a check circle icon.
    expect(el.querySelector(".topic-status-icon sakai-icon[type='check_circle']")).to.exist;

    expect(el.querySelector(".topic-question-asked").innerHTML).to.contain(el._i18n.asked);

    // Only question topics have a status icon
    expect(el.querySelector(".topic-status-icon-and-text")).to.exist;
  });

  it("handles bookmarking a topic", async () => {

    const topic = { ...data.discussionTopic, canBookmark: true };

    fetchMock.post(topic.links.find(l => l.rel === "bookmark").href, 200);

    const el = await fixture(html`
      <sakai-topic .topic=${topic}>
      </sakai-topic>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    const link = el.querySelector(".topic-message-bottom-bar > div > a");
    expect(link).to.exist;
    setTimeout(() => link.click());
    const { detail } = await oneEvent(el, "topic-updated");
    expect(detail.topic.bookmarked).to.be.true;
  });

  it("handles pinning a topic", async () => {

    fetchMock.post(data.discussionTopic.links.find(l => l.rel === "pin").href, 200);

    const topic = { ...data.discussionTopic, canPin: true };

    const el = await fixture(html`
      <sakai-topic .topic=${topic}>
      </sakai-topic>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    const link = el.querySelector(".topic-message-bottom-bar > div > a");
    expect(link).to.exist;
    setTimeout(() => link.click());
    const { detail } = await oneEvent(el, "topic-updated");
    expect(detail.topic.pinned).to.be.true;
  });

  it("handles posting a reply to topic", async () => {

    const topic = { ...data.discussionTopic };

    fetchMock.post(topic.links.find(l => l.rel === "posts").href,
      ({ url, options }) => ({ ...JSON.parse(options.body), id: "posted1", viewed: true }));

    const el = await fixture(html`
      <sakai-topic .topic=${topic}>
      </sakai-topic>
    `);

    await elementUpdated(el);

    expect(el.querySelector("sakai-editor")).to.not.exist;

    // Click reply placeholder to show editor
    const replyPlaceholder = el.querySelector(".editor-placeholder").parentElement;
    replyPlaceholder.click();
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector(".conv-post-editor-header")).to.exist;
    expect(el.querySelector(".conv-post-editor-header > span:nth-child(2)").innerHTML).to.contain(data.discussionTopic.title);

    const editor = el.querySelector("sakai-editor");
    expect(editor).to.exist;

    const message = "This is a reply";
    editor.setContent(message);

    const eventPromise = oneEvent(el, "topic-updated");
    el.querySelector(".topic-reply-block input[type='button']").click();
    const { detail } = await eventPromise;
    expect(detail.topic.id).to.equal(data.discussionTopic.id);
    expect(detail.topic.posts[0].message).to.equal(message);
  });

  it("shows appropriate warnings for draft, hidden and locked topics", async () => {

    const topic = { ...data.discussionTopic, draft: true, hidden: true, locked: true };

    const el = await fixture(html`
      <sakai-topic .topic=${topic}>
      </sakai-topic>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector(".sak-banner-warn")).to.exist;
    expect(el.querySelector(".sak-banner-warn").innerHTML).to.contain(el._i18n.draft_warning);

    expect(el.querySelector(".sak-banner-warn:nth-child(2)")).to.exist;
    expect(el.querySelector(".sak-banner-warn:nth-child(2)").innerHTML).to.contain(el._i18n.topic_hidden);

    expect(el.querySelector(".sak-banner-warn:nth-child(3)")).to.exist;
    expect(el.querySelector(".sak-banner-warn:nth-child(3)").innerHTML).to.contain(el._i18n.moderator_topic_locked);

    topic.visibility = constants.INSTRUCTORS;
    el.requestUpdate();
    await elementUpdated(el);
    await expect(el).to.be.accessible();
    expect(el.querySelector(".sak-banner-warn:nth-child(4)")).to.exist;
    expect(el.querySelector(".sak-banner-warn:nth-child(4)").innerHTML).to.contain(el._i18n.topic_instructors_only_tooltip);

    topic.visibility = constants.GROUP;
    el.requestUpdate();
    await elementUpdated(el);
    await expect(el).to.be.accessible();
    expect(el.querySelector(".sak-banner-warn:nth-child(4)")).to.exist;
    expect(el.querySelector(".sak-banner-warn:nth-child(4)").innerHTML).to.contain(el._i18n.topic_groups_only_tooltip);
  });

  it("dispatches events when options menu items are clicked", async () => {

    const topic = { ...data.discussionTopic };

    // Setup delete URL mock
    const deleteUrl = topic.links.find(l => l.rel === "delete").href;
    fetchMock.delete(deleteUrl, 200);

    const hideUrl = topic.links.find(l => l.rel === "hide").href;
    fetchMock.post(hideUrl, {});

    // Setup lock URL mock
    const lockUrl = topic.links.find(l => l.rel === "lock").href;
    fetchMock.post(lockUrl, ({ url, options }) => {
      return { ...topic, locked: !data.discussionTopic.locked, selected: true };
    });

    const el = await fixture(html`
      <sakai-topic .topic=${topic}>
      </sakai-topic>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Verify options menu exists
    const optionsMenu = el.querySelector(".topic-options-menu");
    expect(optionsMenu).to.exist;

    // Test edit button
    const editButton = el.querySelector(".dropdown-menu button[title='" + el._i18n.edit_topic_tooltip + "']");
    expect(editButton).to.exist;

    // Setup listener for edit-topic event
    const editPromise = oneEvent(el, "edit-topic");
    editButton.click();
    const editEvent = await editPromise;
    expect(editEvent.detail.topic.id).to.equal(data.discussionTopic.id);

    // Test delete button with confirm mocked
    const deleteButton = el.querySelector(".dropdown-menu button[title='" + el._i18n.delete_topic_tooltip + "']");
    expect(deleteButton).to.exist;

    // Mock confirm to return true
    const originalConfirm = window.confirm;
    window.confirm = () => true;

    // Setup listener for topic-deleted event
    const deletePromise = oneEvent(el, "topic-deleted");
    deleteButton.click();
    const deleteEvent = await deletePromise;
    expect(deleteEvent.detail.topic.id).to.equal(data.discussionTopic.id);

    // Test hide button
    const hideButton = el.querySelector(".dropdown-menu button[title='" + el._i18n.hide_topic_tooltip + "']");
    expect(hideButton).to.exist;

    // Setup listener for topic-updated event
    const hidePromise = oneEvent(el, "topic-updated");
    hideButton.click();
    const hideEvent = await hidePromise;
    expect(hideEvent.detail.topic.hidden).to.be.true;

    // Test lock button
    const lockButton = el.querySelector(".dropdown-menu button[title='" + el._i18n.lock_topic_tooltip + "']");
    expect(lockButton).to.exist;

    // Setup listener for topic-updated event
    const lockPromise = oneEvent(el, "topic-updated");
    lockButton.click();
    await lockPromise;

    // Restore original confirm
    window.confirm = originalConfirm;

  });

  it("fires topic-updated event when _postUpdated is called", async () => {

    const testPost = {
      id: "test-post-1",
      message: "Original message"
    };

    const updatedPost = {
      id: "test-post-1",
      message: "Updated message"
    };

    // Add the test post to the topic
    const topic = { ...data.discussionTopic, posts: [ testPost ] };

    const el = await fixture(html`
      <sakai-topic .topic=${topic}>
      </sakai-topic>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Create a synthetic event with the updated post and call _postUpdated method
    const eventPromise = oneEvent(el, "topic-updated");
    el._postUpdated(new CustomEvent("post-updated", { detail: { post: updatedPost } }));
    const { detail } = await eventPromise;
    expect(detail.topic.id).to.equal(data.discussionTopic.id);

    // Verify the post was updated in the topic
    const updatedTopicPost = detail.topic.posts.find(p => p.id === testPost.id);
    expect(updatedTopicPost).to.exist;
    expect(updatedTopicPost.message).to.equal(updatedPost.message);
  });

  it("fires topic-updated event when _postDeleted is called", async () => {

    const testPost = {
      id: "post-to-delete",
      message: "This post will be deleted"
    };

    const topic = { ...data.discussionTopic, posts: [ testPost ], numberOfPosts: 1 };

    const el = await fixture(html`
      <sakai-topic .topic=${topic}>
      </sakai-topic>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    const eventPromise = oneEvent(el, "topic-updated");
    el._postDeleted(new CustomEvent("post-deleted", { detail: { post: testPost } }));
    const { detail } = await eventPromise;

    // Verify the topic was updated correctly
    expect(detail.topic.id).to.equal(data.discussionTopic.id);
    expect(detail.topic.posts.length).to.equal(0);
    expect(detail.topic.numberOfPosts).to.equal(0);
  });

  it("renders anonymous topics correctly", async () => {

    const topic = { ...data.anonymousTopic };
    // Test with canViewAnonymous = false (regular user)
    const el = await fixture(html`
      <sakai-topic
          .topic=${topic}>
      </sakai-topic>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Verify the creator name shows as Anonymous
    expect(el.querySelector(".conversations-topic__creator-name").innerHTML).to.contain(el._i18n.anonymous);

    // Verify the user photo has user-id="blank" when anonymous and user can't view anonymous
    const userPhoto = el.querySelector(".author-block sakai-user-photo");
    expect(userPhoto).to.exist;
    expect(userPhoto.getAttribute("user-id")).to.equal("blank");

    // Now test with canViewAnonymous = true (admin/instructor)
    const elAdmin = await fixture(html`
      <sakai-topic
          .topic=${data.anonymousTopic}
          ?can-view-anonymous=${true}>
      </sakai-topic>
    `);

    await waitUntil(() => elAdmin._i18n);
    await elementUpdated(elAdmin);

    await expect(elAdmin).to.be.accessible();

    // Admin should see the actual user ID in the photo component
    const adminUserPhoto = elAdmin.querySelector(".author-block sakai-user-photo");
    expect(adminUserPhoto).to.exist;
    expect(adminUserPhoto.getAttribute("user-id")).to.equal(data.anonymousTopic.creator);
  });

  it("handles anonymous post creation correctly", async () => {

    // Create a copy of the topic with allowAnonymousPosts set to true
    const topic = { ...data.discussionTopic, allowAnonymousPosts: true };

    // Mock the post endpoint
    fetchMock.post(topic.links.find(l => l.rel === "posts").href, ({ url, options }) => {

      const postData = JSON.parse(options.body);
      return { ...postData,
        creatorDisplayName: postData.anonymous ? "Anonymous" : "Regular User",
        viewed: true
      };
    });

    const el = await fixture(html`
      <sakai-topic .topic=${topic}>
      </sakai-topic>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Click reply placeholder to show editor
    const replyPlaceholder = el.querySelector(".editor-placeholder").parentElement;
    replyPlaceholder.click();
    await elementUpdated(el);
    await expect(el).to.be.accessible();

    // Verify anonymous checkbox exists
    const anonymousCheckbox = el.querySelector("#conv-post-editor-anonymous-checkbox");
    expect(anonymousCheckbox).to.exist;

    // Set content and check anonymous checkbox
    el.querySelector(".topic-reply-block sakai-editor").setContent("This is an anonymous reply");
    anonymousCheckbox.checked = true;

    // Submit the post
    const eventPromise = oneEvent(el, "topic-updated");
    el.querySelector(".topic-reply-block input[type='button']").click();
    const { detail } = await eventPromise;

    // Verify the post was created with anonymous flag
    expect(detail.topic.posts[0].anonymous).to.be.true;
    expect(detail.topic.posts[0].creatorDisplayName).to.equal("Anonymous");
  });

  it("does not show anonymous checkbox when anonymous posts are not allowed", async () => {

    // Create a copy of the topic with allowAnonymousPosts set to false
    const topic = { ...data.discussionTopic, allowAnonymousPosts: false };

    const el = await fixture(html`
      <sakai-topic .topic=${topic}>
      </sakai-topic>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Click reply placeholder to show editor
    const replyPlaceholder = el.querySelector(".editor-placeholder").parentElement;
    replyPlaceholder.click();
    await elementUpdated(el);
    await expect(el).to.be.accessible();

    // Verify anonymous checkbox does not exist
    const anonymousCheckbox = el.querySelector("#conv-post-editor-anonymous-checkbox");
    expect(anonymousCheckbox).to.not.exist;
  });

  it("handles post sorting for QUESTION topic types", async () => {

    const posts = [
      { id: "post1", message: "Post 1", created: 1111, viewed: true, canView: true },
      { id: "post2", message: "Post 2", created: 2222, viewed: true, canView: true },
      { id: "post3", message: "Post 3", created: 3333, viewed: true, canView: true }
    ];
    const sortedNewestFirst = posts.toReversed();

    const topic = { ...data.questionTopic, posts };

    const el = await fixture(html`
      <sakai-topic .topic=${topic}>
      </sakai-topic>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Mock the fetch for the sorted posts URL
    const postsUrl = `${data.questionTopic.links.find(l => l.rel === "posts").href}?page=0&sort=${constants.SORT_NEWEST}`;
    fetchMock.get(postsUrl, sortedNewestFirst);

    // Find the sort dropdown and trigger a change event
    const sortSelect = el.querySelector(".topic-posts-header select");
    expect(sortSelect).to.exist;
    sortSelect.value = constants.SORT_NEWEST;
    sortSelect.dispatchEvent(new Event("change"));

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Wait for the fetch to complete and the component to update
    await waitUntil(() => el.topic.posts[0].id === sortedNewestFirst[0].id);

    // Verify the posts were updated with the sorted results
    expect(el.topic.posts.length).to.equal(posts.length);
    expect(el.topic.posts[0].id).to.equal(sortedNewestFirst[0].id);
    expect(el.topic.posts[1].id).to.equal(sortedNewestFirst[1].id);
    expect(el.topic.posts[2].id).to.equal(sortedNewestFirst[2].id);
  });

  it("correctly marks posts as viewed", async () => {

    // Setup test data
    const posts = [
      { id: "post1", message: "Post 1", viewed: false, canView: true },
      { id: "post2", message: "Post 2", viewed: false, canView: true }
    ];

    const topic = { ...data.discussionTopic, posts };

    const el = await fixture(html`
      <sakai-topic .topic=${topic}>
      </sakai-topic>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Create mock IntersectionObserver entries
    const mockEntries = [
      {
        isIntersecting: true,
        target: { dataset: { postId: "post1" } }
      },
      {
        isIntersecting: true,
        target: { dataset: { postId: "post2" } }
      }
    ];

    // Mock the observer.unobserve method
    const observeSpy = sinon.spy();
    const unobserveSpy = sinon.spy();
    const originalObserver = el.observer;
    el.observer = { observe: observeSpy, unobserve: unobserveSpy };

    // Clear any previously observed posts to ensure the event will fire
    el._observedPosts = new Set();

    // Mock the fetch to return success
    const originalFetch = window.fetch;
    window.fetch = sinon.stub().resolves({ ok: true });

    try {
      // Set up event listener for "posts-viewed" before calling the method
      const eventPromise = oneEvent(el, "posts-viewed");
      
      // Call the method directly with the post IDs
      el._markPostsViewed(["post1"]);

      const { detail } = await eventPromise;

      expect(detail.postIds).to.include("post1");
      expect(detail.topicId).to.equal(topic.id);
    } finally {
      // Restore original fetch
      window.fetch = originalFetch;
    }

    expect(unobserveSpy.calledOnce).to.be.true;
  });

  it ("should detect that a draft topic is graded but does not have a gradingItemId", async () => {

    const topic = { ...data.discussionTopic, draft: true, graded: true };

    const el = await fixture(html`
      <sakai-topic .topic=${topic}>
      </sakai-topic>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.topic.graded).to.be.true;

    expect(el.querySelector(".conv-graded-no-grading-item-id-warning")).to.exist;
    expect(el.querySelector(".conv-graded-no-grading-item-id-warning").innerHTML).to.contain(el._i18n.graded_no_item_id);
  });
});
