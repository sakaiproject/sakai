import "../sakai-conversations.js";
import { aTimeout, elementUpdated, expect, fixture, html, oneEvent, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import fetchMock from "fetch-mock/esm/client";
import * as constants from "../src/sakai-conversations-constants.js";

describe("sakai-conversations tests", () => {

  window.top.portal = { user: { id: "user1", timezone: "Europe/London" } };

  beforeEach(() => {
    fetchMock.get(data.i18nUrl, data.i18n);
  });

  afterEach(() => {
    fetchMock.restore();
  });

  it ("renders add topic button correctly", async () => {

    fetchMock.get(`/api/sites/${data.siteId}/conversations`, { ...data.data, canCreateTopic: true });

    const el = await fixture(html`
      <sakai-conversations site-id="${data.siteId}">
      </sakai-conversations>
    `);

    await waitUntil(() => el._data);

    await elementUpdated(el);

    await expect(el).to.be.accessible({ ignoredRules: [ "duplicate-id" ] });

    expect(el.querySelector("#conv-add-topic")).to.exist;

    data.data.canCreateTopic = false;
    el._data = data.data;
    await el.updateComplete;
    await expect(el).to.be.accessible({ ignoredRules: [ "duplicate-id" ] });
    expect(el.querySelector("#conv-add-topic")).to.not.exist;
  });

  it ("renders guidelines correctly", async () => {

    fetchMock.get(`/api/sites/${data.siteId}/conversations`, { ...data.data, showGuidelines: true });

    const el = await fixture(html`
      <sakai-conversations site-id="${data.siteId}">
      </sakai-conversations>
    `);

    await waitUntil(() => el._i18n);
    await waitUntil(() => el._data);

    await expect(el).to.be.accessible({ ignoredRules: [ "duplicate-id" ] });

    expect(el.querySelector("sakai-conversations-guidelines")).to.exist;

    fetchMock.get(`/api/sites/${data.siteId}/conversations/agree`, 200);

    el._agreeToGuidelines();

    expect(el._data.showGuidelines).to.be.false;
  });

  it ("renders loading data correctly", async () => {

    fetchMock.get(`/api/sites/${data.siteId}/conversations`, data.data);

    const el = await fixture(html`
      <sakai-conversations site-id="${data.siteId}">
      </sakai-conversations>
    `);

    await waitUntil(() => el._i18n);
    await waitUntil(() => el._data);

    el._loadingData = true;
    await elementUpdated(el);
    await expect(el).to.be.accessible({ ignoredRules: [ "duplicate-id" ] });

    expect(el.querySelector(".sak-banner-info > div").innerText).to.equal(el._i18n.loading_1);

    el._loadingData = false;
    await elementUpdated(el);
    expect(el.querySelector(".sak-banner-info")).to.not.exist;
  });

  it("renders settings menu correctly for instructors", async () => {

    fetchMock.get(`/api/sites/${data.siteId}/conversations`,
      { ...data.data, isInstructor: true, canUpdatePermissions: true, canEditTags: true });

    const el = await fixture(html`
      <sakai-conversations site-id="${data.siteId}">
      </sakai-conversations>
    `);

    await waitUntil(() => el._i18n);
    await waitUntil(() => el._data);

    await elementUpdated(el);

    // Set state to settings to show the settings menu
    el._state = constants.STATE_SETTINGS;
    await elementUpdated(el);
    await expect(el).to.be.accessible({ ignoredRules: [ "duplicate-id" ] });

    const settingsButton = el.querySelector(".conv-settings-link > button");
    expect(settingsButton).to.exist;

    const listener = oneEvent(settingsButton, "shown.bs.dropdown");

    // This should drop the settings dropdown down
    settingsButton.click();

    await listener;

    // Check if settings menu is rendered
    const settingsMenu = el.querySelector(".conv-settings-link > .dropdown-menu");
    expect(settingsMenu).to.exist;

    // Check if all menu items are present
    expect(settingsMenu.querySelector("button[class*='dropdown-item']")).to.exist;
    expect(settingsMenu.textContent).to.include(el._i18n.general_settings);
    expect(settingsMenu.textContent).to.include(el._i18n.permissions);
    expect(settingsMenu.textContent).to.include(el._i18n.manage_tags);
  });

  it("renders settings menu correctly for non-instructors", async () => {

    fetchMock.get(`/api/sites/${data.siteId}/conversations`, data.data);

    const el = await fixture(html`
      <sakai-conversations site-id="${data.siteId}">
      </sakai-conversations>
    `);

    await waitUntil(() => el._i18n);
    await waitUntil(() => el._data);

    // Settings menu should not be visible for non-instructors
    expect(el.querySelector(".conv-settings-link")).to.not.exist;
  });

  it("handles topic selection correctly", async () => {

    fetchMock
      .get(`/api/sites/${data.siteId}/conversations`, { ...data.data, topics: [ data.discussionTopic, data.questionTopic ] })
      .get(/.*posts.*/, []);

    const el = await fixture(html`
      <sakai-conversations site-id="${data.siteId}">
      </sakai-conversations>
    `);

    await waitUntil(() => el._i18n);
    await waitUntil(() => el._data);

    // Initially no topic should be selected
    expect(el._state).to.equal(constants.STATE_NOTHING_SELECTED);
    expect(el._currentTopic).to.be.undefined;

    // Simulate topic selection
    const topicId = el._data.topics[0].id;
    await el._selectTopic(topicId);
    await elementUpdated(el);

    await expect(el).to.be.accessible({ ignoredRules: [ "duplicate-id" ] });

    // Check if topic is selected and state is updated
    expect(el._state).to.equal(constants.STATE_DISPLAYING_TOPIC);
    expect(el._currentTopic).to.exist;
    expect(el._currentTopic.id).to.equal(topicId);
    expect(el._data.topics.find(t => t.id === topicId).selected).to.be.true;
  });

  it("handles adding a new topic correctly", async () => {

    fetchMock
      .get(`/api/sites/${data.siteId}/conversations`, data.data)

    const el = await fixture(html`
      <sakai-conversations site-id="${data.siteId}">
      </sakai-conversations>
    `);

    await waitUntil(() => el._i18n);
    await waitUntil(() => el._data);

    // Simulate clicking add topic button
    await el._addTopic({ preventDefault: () => {} });
    await elementUpdated(el);

    // Check if state is updated to adding topic
    expect(el._state).to.equal(constants.STATE_ADDING_TOPIC);
    expect(el._addingTopic).to.be.true;
    expect(el._topicBeingEdited).to.exist;
  });

  it("handles search functionality correctly", async () => {

    fetchMock.get(`/api/sites/${data.siteId}/conversations`, { ...data.data, searchEnabled: true });

    // TODO: this should not need to be set to use search!
    //data.data.isInstructor = true;

    const el = await fixture(html`
      <sakai-conversations site-id="${data.siteId}">
      </sakai-conversations>
    `);

    await waitUntil(() => el._i18n);
    await waitUntil(() => el._data);

    // Check if search button is rendered when search is enabled
    expect(el._searchEnabled).to.be.true;
    const searchButton = el.querySelector("button[data-bs-target='#sakai-search-panel']");
    expect(searchButton).to.exist;

    // Disable search and check if button is removed
    data.data.searchEnabled = false;
    el._searchEnabled = false;
    await elementUpdated(el);
    expect(el.querySelector("button[data-bs-target='#sakai-search-panel']")).to.not.exist;
  });

  it("handles topic saving correctly", async () => {

    fetchMock
      .get(`/api/sites/${data.siteId}/conversations`, data.data)
      .get(/.*posts.*/, []);

    const el = await fixture(html`
      <sakai-conversations site-id="${data.siteId}">
      </sakai-conversations>
    `);

    await waitUntil(() => el._i18n);
    await waitUntil(() => el._data);

    // Create a mock topic
    const mockTopic = {
      id: "new-topic-123",
      title: "New Test Topic",
      message: "This is a test topic",
      creator: data.data.userId,
      selected: true,
      links: data.links,
    };

    // Simulate topic saved event
    el._topicSaved({ detail: { topic: mockTopic } });
    await elementUpdated(el);

    // Check if topic was added to the list and selected
    await waitUntil(() => el._state === constants.STATE_DISPLAYING_TOPIC);
    expect(el._data.topics[0].id).to.equal(mockTopic.id);
  });

  it("handles topic editing correctly", async () => {

    fetchMock.get(`/api/sites/${data.siteId}/conversations`, data.data)

    const el = await fixture(html`
      <sakai-conversations site-id="${data.siteId}">
      </sakai-conversations>
    `);

    await waitUntil(() => el._i18n);
    await waitUntil(() => el._data);

    // Create a mock topic
    const mockTopic = {
      id: "new-topic-123",
      title: "New Test Topic",
      message: "This is a test topic",
      creator: data.data.userId,
      selected: true,
      links: data.links,
    };

    await el._editTopic({ detail: { topic: mockTopic } });
    await elementUpdated(el);

    // Check if topic was added to the list and selected
    expect(el._topicBeingEdited).to.equal(mockTopic);
    expect(el._state).to.equal(constants.STATE_ADDING_TOPIC);
  });

  it("hides the stats button correctly", async () => {

    fetchMock.get(`/api/sites/${data.siteId}/conversations`, data.data);

    const el = await fixture(html`
      <sakai-conversations site-id="${data.siteId}">
      </sakai-conversations>
    `);

    await waitUntil(() => el._i18n);
    await waitUntil(() => el._data);

    expect(el.querySelector("#conv-stats-button")).to.not.exist;
  });

  it("displays the stats button correctly", async () => {

    fetchMock
      .get(`/api/sites/${data.siteId}/conversations`, { ...data.data, canViewStatistics: true });

    const el = await fixture(html`
      <sakai-conversations site-id="${data.siteId}">
      </sakai-conversations>
    `);

    await waitUntil(() => el._i18n);
    await waitUntil(() => el._data);

    await expect(el).to.be.accessible({ ignoredRules: [ "duplicate-id" ] });

    expect(el.querySelector("#conv-stats-button")).to.exist;
  });
});
