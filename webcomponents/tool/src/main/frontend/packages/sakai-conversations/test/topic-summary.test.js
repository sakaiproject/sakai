import "../sakai-topic-summary.js";
import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import * as constants from "../src/sakai-conversations-constants.js";
import fetchMock from "fetch-mock";
describe("sakai-topic-summary tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock.get(data.i18nUrl, data.i18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  window.top.portal = { siteId: data.siteId, siteTitle: data.siteTitle, user: { id: "user1", timezone: "Europe/London" } };

  it("renders a discussion topic summary", async () => {

    const el = await fixture(html`
      <sakai-topic-summary
          .topic=${data.discussionTopic}>
      </sakai-topic-summary>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector(".topic-summary")).to.exist;
    expect(el.querySelector(".topic-summary-title").textContent).to.include(data.discussionTopic.title);
    expect(el.querySelector(".discussion-icon")).to.exist;
    expect(el.querySelector(".topic-summary-creator-block").textContent).to.include(data.discussionTopic.creatorDisplayName);
    expect(el.querySelector(".topic-summary-creator-block").textContent).to.include(data.discussionTopic.formattedCreatedDate);
  });

  it("renders a question topic summary", async () => {

    const el = await fixture(html`
      <sakai-topic-summary
          .topic=${data.questionTopic}>
      </sakai-topic-summary>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector(".topic-summary")).to.exist;
    expect(el.querySelector(".topic-summary-title").textContent).to.include(data.questionTopic.title);
    expect(el.querySelector(".question-icon")).to.exist;
  });

  it("renders tags correctly", async () => {

    const el = await fixture(html`
      <sakai-topic-summary
          .topic=${data.discussionTopic}>
      </sakai-topic-summary>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    const tagsContainer = el.querySelector(".topic-tags");
    expect(tagsContainer).to.exist;

    const tagElements = tagsContainer.querySelectorAll(".tag");
    expect(tagElements.length).to.equal(data.discussionTopic.tags.length);

    data.discussionTopic.tags.forEach((tag, index) => {
      expect(tagElements[index].textContent).to.include(tag.label);
    });
  });

  it("shows pinned indicator when topic is pinned", async () => {

    const pinnedTopic = { ...data.discussionTopic, pinned: true, bookmarked: false };

    const el = await fixture(html`
      <sakai-topic-summary
          .topic=${pinnedTopic}>
      </sakai-topic-summary>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector(".topic-summary-pinned-indicator sakai-icon[type='pin']")).to.exist;
  });

  it("shows bookmarked indicator when topic is bookmarked", async () => {

    const bookmarkedTopic = { ...data.discussionTopic, bookmarked: true };

    const el = await fixture(html`
      <sakai-topic-summary
          .topic=${bookmarkedTopic}>
      </sakai-topic-summary>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector(".topic-summary-pinned-indicator .si-bookmark-fill")).to.exist;
  });

  it("shows locked indicator when topic is locked", async () => {

    const lockedTopic = { ...data.discussionTopic, locked: true };

    const el = await fixture(html`
      <sakai-topic-summary
          .topic=${lockedTopic}>
      </sakai-topic-summary>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector(".topic-summary-pinned-indicator sakai-icon[type='lock']")).to.exist;
  });

  it("shows hidden indicator when topic is hidden", async () => {

    const hiddenTopic = { ...data.discussionTopic, hidden: true };

    const el = await fixture(html`
      <sakai-topic-summary
          .topic=${hiddenTopic}>
      </sakai-topic-summary>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector(".topic-summary-pinned-indicator sakai-icon[type='hidden']")).to.exist;
  });

  it("shows draft indicator when topic is a draft", async () => {

    const draftTopic = { ...data.discussionTopic, draft: true };

    const el = await fixture(html`
      <sakai-topic-summary
          .topic=${draftTopic}>
      </sakai-topic-summary>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector(".topic-summary-title .draft")).to.exist;
  });

  it("shows unread indicator when topic has unread posts", async () => {

    const topicWithUnread = { ...data.discussionTopic, numberOfUnreadPosts: 5 };

    const el = await fixture(html`
      <sakai-topic-summary
          .topic=${topicWithUnread}>
      </sakai-topic-summary>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector(".unread-icon sakai-icon[type='circle']")).to.exist;
    expect(el.querySelector(".new-posts-number").textContent).to.include(topicWithUnread.numberOfUnreadPosts);
  });

  it("shows resolved indicator for answered questions", async () => {

    const resolvedQuestion = { ...data.questionTopic, type: constants.QUESTION, resolved: true };

    const el = await fixture(html`
      <sakai-topic-summary
          .topic=${resolvedQuestion}>
      </sakai-topic-summary>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector("sakai-icon[type='check_circle']")).to.exist;
  });

  it("shows unanswered indicator for unresolved questions", async () => {

    const unresolvedQuestion = { ...data.questionTopic, type: constants.QUESTION, resolved: false };

    const el = await fixture(html`
      <sakai-topic-summary
          .topic=${unresolvedQuestion}>
      </sakai-topic-summary>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector("sakai-icon[type='questioncircle'].unanswered-icon")).to.exist;
  });

  it("shows instructor label when creator is an instructor", async () => {

    const instructorTopic = { ...data.discussionTopic, isInstructor: true };

    const el = await fixture(html`
      <sakai-topic-summary
          .topic=${instructorTopic}>
      </sakai-topic-summary>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    const creatorBlock = el.querySelector(".topic-summary-creator-block");
    expect(creatorBlock.textContent).to.include(el._i18n.instructor);
  });

  it("shows due date when topic has a due date", async () => {

    const topicWithDueDate = {
      ...data.discussionTopic,
      formattedDueDate: "Dec 31, 2023"
    };

    const el = await fixture(html`
      <sakai-topic-summary
          .topic=${topicWithDueDate}>
      </sakai-topic-summary>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    const dueDateElement = el.querySelector(".topic-summary-duedate");
    expect(dueDateElement).to.exist;
    expect(dueDateElement.textContent).to.include(topicWithDueDate.formattedDueDate);
  });
});
