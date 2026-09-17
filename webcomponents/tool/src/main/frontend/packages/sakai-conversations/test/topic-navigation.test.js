import "../sakai-conversations.js";
import "../sakai-topic.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";
import * as data from "./data.js";

describe("Conversations topic navigation", () => {
  beforeEach(() => {
    window.top.portal = { user: { id: "user1", timezone: "Europe/London" } };
    fetchMock.mockGlobal();
    fetchMock.get(data.i18nUrl, data.i18n);
    fetchMock.get(/.*posts.*/, []);
  });

  afterEach(() => fetchMock.hardReset());

  async function renderTopics(topics) {
    fetchMock.get(`/api/sites/${data.siteId}/conversations`, { ...data.data, topics });
    const el = await fixture(html`<sakai-conversations site-id=${data.siteId}></sakai-conversations>`);
    await waitUntil(() => el.querySelectorAll(".topic-summary-link").length === topics.length);
    return el;
  }

  async function selectFilter(el, index, value) {
    const select = el.querySelectorAll("#topic-list-filters select")[index];
    select.value = value;
    select.dispatchEvent(new Event("change"));
    await elementUpdated(el.querySelector("sakai-topic-list"));
  }

  function titles(el) {
    return [...el.querySelectorAll(".topic-summary-title")].map(title => title.textContent.trim());
  }

  async function expectTopic(el, title) {
    await waitUntil(() => el.querySelector(".conversations-topic__title")?.textContent === title);
  }

  async function returnToTopics(el) {
    el.querySelector("#conv-back-button-block button").click();
    await waitUntil(() => el.querySelector("#topic-list-filters"));
  }

  it("retains tag and type filters after reading a topic", async () => {
    const first = { ...data.questionTopic, tags: [data.tags[0]] };
    const el = await renderTopics([
      first,
      { ...data.questionTopic, id: "other", title: "Other question", tags: [data.tags[1]] },
      data.discussionTopic,
    ]);
    await selectFilter(el, 0, "1");
    await selectFilter(el, 1, "by_question");
    expect(titles(el)).to.deep.equal([first.title]);
    el.querySelector(".topic-summary-link").click();
    await expectTopic(el, first.title);
    expect(el.querySelector("#conv-next-topic").disabled).to.be.true;
    await returnToTopics(el);
    expect([...el.querySelectorAll("#topic-list-filters select")].map(select => select.value))
      .to.deep.equal(["1", "by_question"]);
    expect(titles(el)).to.deep.equal([first.title]);
  });

  it("visits pinned, draft and ordinary topics in displayed order without wrapping", async () => {
    const regular = { ...data.discussionTopic, id: "regular", title: "Regular topic" };
    const pinned = { ...data.discussionTopic, id: "pinned", title: "Pinned topic", pinned: true };
    const draft = { ...data.discussionTopic, id: "draft", title: "Draft topic", draft: true };
    const el = await renderTopics([regular, pinned, draft]);
    el.querySelector(".topic-summary-link").click();
    await expectTopic(el, pinned.title);
    el.querySelector("#conv-next-topic").click();
    await expectTopic(el, draft.title);
    el.querySelector("#conv-next-topic").click();
    await expectTopic(el, regular.title);
    expect(el.querySelector("#conv-next-topic").disabled).to.be.true;
  });

  it("keeps the original sequence as unviewed topics are read", async () => {
    const markViewedUrl = data.discussionTopic.links.find(link => link.rel === "markpostsviewed").href;
    fetchMock.post(markViewedUrl, 200);
    const first = { ...data.discussionTopic, id: "first", title: "First unread", viewed: false, numberOfUnreadPosts: 0 };
    const second = { ...data.discussionTopic, id: "second", title: "Second unread", viewed: false, numberOfUnreadPosts: 0 };
    const el = await renderTopics([
      first,
      { ...data.discussionTopic, id: "read", title: "Already read", viewed: true },
      second,
    ]);
    await selectFilter(el, 1, "by_unviewed");
    el.querySelector(".topic-summary-link").click();
    await expectTopic(el, first.title);
    el.querySelector("#conv-next-topic").click();
    await expectTopic(el, second.title);
    expect(el.querySelector("#conv-next-topic").disabled).to.be.true;
    await returnToTopics(el);
    expect(el.querySelectorAll("#topic-list-filters select")[1].value).to.equal("by_unviewed");
    await waitUntil(() => titles(el).length === 0);
    expect(fetchMock.callHistory.calls(markViewedUrl)).to.have.lengthOf(2);
    expect(titles(el)).to.deep.equal([]);
    await waitUntil(() => document.activeElement === el.querySelector("#topic-list-filters select:not([disabled])"));
  });

  for (const pinned of [false, true]) {
    it(`updates hide and lock menu labels immediately for ${pinned ? "pinned" : "ordinary"} topics`, async () => {
      fetchMock.post("/hide-url", 200);
      fetchMock.post("/lock-url", { ...data.discussionTopic, pinned, hidden: true, locked: true });
      const el = await renderTopics([{ ...data.discussionTopic, pinned }]);
      const menuButton = label => [...el.querySelectorAll(".dropdown-item")]
        .find(button => button.textContent.trim() === label);
      menuButton("Hide").click();
      await waitUntil(() => menuButton("Show"));
      expect(el.querySelector(".topic-status sakai-icon[type='hidden']")).to.exist;
      menuButton("Lock").click();
      await waitUntil(() => menuButton("Unlock"));
      expect(el.querySelector(".topic-status sakai-icon[type='lock']")).to.exist;
    });

  }

  it("removes a deleted row immediately", async () => {
    fetchMock.delete("/delete-url", 200);
    const el = await renderTopics([data.discussionTopic]);
    const confirm = window.confirm;
    try {
      window.confirm = () => true;
      [...el.querySelectorAll(".dropdown-item")].find(button => button.textContent.trim() === "Delete").click();
      await waitUntil(() => el.querySelector("#no-topics-yet-message"));
      expect(titles(el)).to.deep.equal([]);
    } finally {
      window.confirm = confirm;
    }
  });

  it("clears a tag filter when its last topic is deleted", async () => {
    fetchMock.delete("/delete-url", 200);
    const el = await renderTopics([
      { ...data.discussionTopic, tags: [data.tags[0]] },
      { ...data.discussionTopic, id: "other", title: "Sports topic", tags: [data.tags[1]] },
    ]);
    await selectFilter(el, 0, "1");
    const confirm = window.confirm;
    try {
      window.confirm = () => true;
      [...el.querySelectorAll(".dropdown-item")].find(button => button.textContent.trim() === "Delete").click();
      await waitUntil(() => titles(el).length === 1 && titles(el)[0] === "Sports topic");
      expect(el.querySelectorAll("#topic-list-filters select")[0].value).to.equal("any");
      el.querySelector(".topic-summary-link").click();
      await expectTopic(el, "Sports topic");
      await returnToTopics(el);
      expect(el.querySelectorAll("#topic-list-filters select")[0].value).to.equal("any");
      expect(titles(el)).to.deep.equal(["Sports topic"]);
    } finally {
      window.confirm = confirm;
    }
  });

  it("focuses the topic heading and restores focus to its list link", async () => {
    const el = await renderTopics([data.discussionTopic]);
    const link = el.querySelector(".topic-summary-link");
    link.focus();
    expect(document.activeElement).to.equal(link);
    link.click();
    await expectTopic(el, data.discussionTopic.title);
    expect(document.activeElement).to.equal(el.querySelector(".conversations-topic__title"));

    fetchMock.post("/hide-url", 200);
    const menuToggle = el.querySelector("sakai-topic [data-bs-toggle='dropdown']");
    menuToggle.focus();
    el.querySelector("sakai-topic button[aria-label='Hide this topic']").click();
    await waitUntil(() => el.querySelector("sakai-topic button[aria-label='Show this topic']"));
    expect(document.activeElement).to.equal(menuToggle);

    await returnToTopics(el);
    await waitUntil(() => document.activeElement === el.querySelector(".topic-summary-link"));
  });

  it("scrolls the topic heading into view from a long list", async () => {
    const topics = Array.from({ length: 25 }, (_, index) => ({
      ...data.discussionTopic, id: `topic-${index}`, title: `Topic ${index}`,
      message: '<div style="height:2000px">Long topic content</div>',
    }));
    const el = await renderTopics(topics);
    const container = document.createElement("div");
    container.className = "portal-main-container";
    container.style.cssText = "height:300px;overflow:auto";
    el.parentElement.insertBefore(container, el);
    container.append(el);
    container.scrollTop = 700;
    expect(container.scrollTop).to.equal(700);
    el.querySelectorAll(".topic-summary-link")[15].click();
    await expectTopic(el, "Topic 15");
    const heading = el.querySelector(".conversations-topic__title");
    expect(heading.getBoundingClientRect().top).to.be.closeTo(container.getBoundingClientRect().top, 1);
  });

  it("preserves the scroll target of a post deep link", async () => {
    const topic = {
      ...data.discussionTopic,
      message: '<div style="height:2000px">Long topic content</div>',
      posts: [{ ...data.post1, id: "target", canView: true, viewed: true, keepExpanded: true,
        message: '<div style="height:500px">Linked post</div>' }],
    };
    const container = await fixture(html`
      <div style="height:300px;overflow:auto">
        <sakai-topic post-id="target" .topic=${topic}></sakai-topic>
      </div>
    `);
    await waitUntil(() => container.querySelector("#post-target"));
    await waitUntil(() => container.scrollTop > 1000);
    const heading = container.querySelector(".conversations-topic__title");
    expect(document.activeElement).not.to.equal(heading);
    expect(heading.getBoundingClientRect().top).to.be.lessThan(container.getBoundingClientRect().top);
  });
});
