import "../sakai-topic-list.js";
import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import fetchMock from "fetch-mock";

import * as constants from "../src/sakai-conversations-constants.js";
describe("sakai-topic-list tests", () => {

  window.top.portal = { siteId: data.siteId, siteTitle: data.siteTitle };

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock.get(data.i18nUrl, data.i18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it("renders with no topics", async () => {

    const emptyData = { ...data.data, topics: [] };

    const el = await fixture(html`
      <sakai-topic-list
          site-id="${data.siteId}"
          .data=${emptyData}>
      </sakai-topic-list>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector("#no-topics-yet-message")).to.exist;
  });

  it("renders pinned topics correctly", async () => {

    const pinnedTopic = { ...data.discussionTopic, pinned: true };
    const testData = { ...data.data, topics: [ pinnedTopic ] };

    const el = await fixture(html`
      <sakai-topic-list
          site-id="${data.siteId}"
          .data=${testData}>
      </sakai-topic-list>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector(".topic-list-pinned-header")).to.exist;
    expect(el.querySelector(".topic-list-pinned-header").textContent).to.contain(el._i18n.pinned);
    expect(el.querySelector("sakai-topic-summary")).to.exist;
  });

  it("renders draft topics correctly", async () => {

    const draftTopic = { ...data.discussionTopic, draft: true };
    const testData = { ...data.data, topics: [draftTopic] };

    const el = await fixture(html`
      <sakai-topic-list
          site-id="${data.siteId}"
          .data=${testData}>
      </sakai-topic-list>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector(".topic-list-pinned-header")).to.exist;
    expect(el.querySelector(".topic-list-pinned-header").textContent).to.contain(el._i18n.draft);
    expect(el.querySelector("sakai-topic-summary")).to.exist;
  });

  it("renders non pinned and non draft topics correctly", async () => {

    const regularTopic = { ...data.discussionTopic, pinned: false, draft: false };
    const testData = { ...data.data, topics: [ regularTopic ] };

    const el = await fixture(html`
      <sakai-topic-list
          site-id="${data.siteId}"
          .data=${testData}>
      </sakai-topic-list>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector(".topic-list-pinned-header")).to.exist;
    expect(el.querySelector(".topic-list-pinned-header").textContent).to.contain(el._i18n.all_topics);
    expect(el.querySelector("sakai-topic-summary")).to.exist;
  });

  it("filters topics by tag", async () => {

    const topic1 = { ...data.discussionTopic, id: "topic1", tags: [{ id: "1", label: "eggs" }] };
    const topic2 = { ...data.discussionTopic, id: "topic2", tags: [{ id: "2", label: "sports" }] };
    const testData = { ...data.data, topics: [ topic1, topic2 ] };

    const el = await fixture(html`
      <sakai-topic-list
          site-id="${data.siteId}"
          .data=${testData}>
      </sakai-topic-list>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Verify tag filter dropdown exists and has correct options
    const tagSelect = el.querySelector("#topic-list-filters select");
    expect(tagSelect).to.exist;
    expect(tagSelect.options.length).to.equal(3); // "any" + 2 tags

    // Select the first tag
    tagSelect.value = "1";
    tagSelect.dispatchEvent(new Event("change"));
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Should only show topic1
    expect(el._filteredUnpinnedTopics.length).to.equal(1);
    expect(el._filteredUnpinnedTopics[0].id).to.equal(topic1.id);
  });

  it("filters topics by type", async () => {

    const discussionTopic = { ...data.discussionTopic, type: constants.DISCUSSION };
    const questionTopic = { ...data.questionTopic, type: constants.QUESTION };
    const testData = { ...data.data, topics: [discussionTopic, questionTopic] };

    const el = await fixture(html`
      <sakai-topic-list
          site-id="${data.siteId}"
          .data=${testData}>
      </sakai-topic-list>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Verify filter dropdown exists
    const filterSelect = el.querySelector("#topic-list-filters select:nth-child(1)");
    expect(filterSelect).to.exist;

    // Filter by questions
    el._currentFilter = el.BY_QUESTION;
    el._filter();
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Should only show question topics
    expect(el._filteredUnpinnedTopics.length).to.equal(1);
    expect(el._filteredUnpinnedTopics[0].type).to.equal(constants.QUESTION);
  });

  it("toggles expansion of topic sections", async () => {

    const draftTopic = { ...data.discussionTopic, draft: true };
    const regularTopic = { ...data.discussionTopic, draft: false, pinned: false };
    const testData = { ...data.data, topics: [draftTopic, regularTopic] };

    const el = await fixture(html`
      <sakai-topic-list
          site-id="${data.siteId}"
          .data=${testData}>
      </sakai-topic-list>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Initially both sections should be expanded
    expect(el._expandDraft).to.be.true;
    expect(el._expandTheRest).to.be.true;

    // Toggle draft section
    el._toggleExpandDraft();
    await elementUpdated(el);
    expect(el._expandDraft).to.be.false;

    // Toggle regular topics section
    el._toggleExpandTheRest();
    await elementUpdated(el);
    expect(el._expandTheRest).to.be.false;
  });

  it("loads topics when aboutRef is set", async () => {

    const aboutRef = "site/test/ref";
    const topicsUrl = `/api/sites/${data.siteId}/topics?aboutRef=${aboutRef}`;

    fetchMock.get(topicsUrl, data.data);

    const el = await fixture(html`
      <sakai-topic-list
          site-id="${data.siteId}">
      </sakai-topic-list>
    `);

    await waitUntil(() => el._i18n);

    // Set the aboutRef property which should trigger the fetch
    el.aboutRef = aboutRef;

    // Wait for the fetch to complete
    await waitUntil(() => el.data);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Verify the data was loaded
    expect(el.data).to.deep.equal(data.data);
  });
});
