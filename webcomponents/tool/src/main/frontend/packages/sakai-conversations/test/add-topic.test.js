import "../sakai-add-topic.js";
import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import * as graderData from "../../sakai-grader/test/data.js";
import fetchMock from "fetch-mock";

import * as constants from "../src/sakai-conversations-constants.js";
describe("add-topic tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock.get(/getI18nProperties.*tag-selector$/, data.tagSelectorI18n);
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(graderData.i18nUrl, graderData.i18n)
      .get(`/api/sites/${data.siteId}/grading/item-data`, {})
      .post(data.topicsUrl, ({ url, options }) => JSON.parse(options.body))
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });


  window.top.portal = { siteId: data.siteId, siteTitle: data.siteTitle, user: { timezone: "Europe/London" } };

  it("detaches a tag from the topic while keeping it available for reuse", async () => {
    const topic = { ...data.questionTopic, tags: [...data.tags] };
    fetchMock.put(topic.url, ({ options }) => JSON.parse(options.body));
    const el = await fixture(html`
      <sakai-add-topic .topic=${topic}
          .tags=${data.tags}
          .groups=${data.groups}
          site-id=${data.siteId}
          can-create-question>
      </sakai-add-topic>
    `);
    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    const selector = el.querySelector("sakai-tag-selector");
    await waitUntil(() => selector.shadowRoot.querySelector("button.tag"));
    selector.shadowRoot.querySelector("button.tag").click();
    await elementUpdated(el);
    await elementUpdated(selector);
    expect(selector.selectedTags).to.have.length(1);
    expect(selector.options.map(tag => tag.code)).to.include(String(data.tags[0].id));

    const saved = oneEvent(el, "topic-saved");
    el.querySelector("#button-block input").click();
    const { detail } = await saved;
    expect(detail.topic.tags).to.deep.equal([data.tags[1]]);
    expect(data.tags).to.have.length(2);
    expect(fetchMock.callHistory.calls(/conversations\/tags/)).to.have.length(0);
  });

  it("creates the first tag inline and saves its returned id with the topic", async () => {
    const tag = { id: "new-tag-id", label: "New tag", siteId: data.siteId };
    let finishCreation;
    fetchMock.post(`/api/sites/${data.siteId}/conversations/tags`, () => new Promise(resolve => {
      finishCreation = () => resolve([tag]);
    }));
    const topic = { ...data.questionTopic, tags: [] };
    fetchMock.put(topic.url, ({ options }) => JSON.parse(options.body));
    const el = await fixture(html`<sakai-add-topic .topic=${topic} .tags=${[]}
        .groups=${data.groups} site-id=${data.siteId} can-edit-tags can-create-question></sakai-add-topic>`);
    await waitUntil(() => el.querySelector("sakai-tag-selector"));
    const selector = el.querySelector("sakai-tag-selector");
    await waitUntil(() => selector.shadowRoot.querySelector("input"));
    expect(el.querySelector("#conv-edit-tags-link-wrapper")).not.to.exist;
    const input = selector.shadowRoot.querySelector("input");
    input.value = tag.label;
    input.dispatchEvent(new Event("input"));
    await elementUpdated(selector);
    selector.shadowRoot.querySelector("[role=option]").click();
    await waitUntil(() => finishCreation);
    await elementUpdated(el);
    expect(selector.inert).to.be.true;
    expect(el.querySelector("#button-block input").disabled).to.be.true;
    const created = oneEvent(el, "tags-created");
    finishCreation();
    expect((await created).detail.tags).to.deep.equal([tag]);
    await elementUpdated(el);
    await elementUpdated(selector);
    expect(selector.selectedTags).to.deep.equal([{ code: tag.id, name: tag.label }]);
    expect(selector.inert).to.be.false;
    const saved = oneEvent(el, "topic-saved");
    el.querySelector("#button-block input").click();
    expect((await saved).detail.topic.tags).to.deep.equal([tag]);
  });

  it("retains the previous selection after a failed creation and allows retry", async () => {
    const url = `/api/sites/${data.siteId}/conversations/tags`;
    fetchMock.post(url, 403, { repeat: 1 });
    const tag = { id: "retry-id", label: "Retry tag", siteId: data.siteId };
    fetchMock.post(url, [tag]);
    const el = await fixture(html`<sakai-add-topic .topic=${{ ...data.questionTopic, tags: [data.tags[0]] }}
        .tags=${data.tags} .groups=${data.groups} site-id=${data.siteId} can-edit-tags can-create-question></sakai-add-topic>`);
    await waitUntil(() => el.querySelector("sakai-tag-selector"));
    const selector = el.querySelector("sakai-tag-selector");
    await waitUntil(() => selector.shadowRoot.querySelector("input"));
    const addTag = async () => {
      const input = selector.shadowRoot.querySelector("input");
      input.value = tag.label;
      input.dispatchEvent(new Event("input"));
      await elementUpdated(selector);
      selector.shadowRoot.querySelector("[role=option]").click();
    };
    await addTag();
    await waitUntil(() => el.querySelector("[role=alert]"));
    await elementUpdated(selector);
    expect(selector.selectedTags.map(t => t.code)).to.deep.equal([String(data.tags[0].id)]);
    expect(el.querySelector("#button-block input").disabled).to.be.false;
    const created = oneEvent(el, "tags-created");
    await addTag();
    await created;
    await elementUpdated(el);
    expect(el.querySelector("[role=alert]")).not.to.exist;
    expect(el.topic.tags).to.deep.equal([data.tags[0], tag]);
  });

  it("offers existing tags but no inline creation without permission", async () => {
    const el = await fixture(html`<sakai-add-topic .topic=${{ ...data.questionTopic, tags: [] }}
        .tags=${data.tags} .groups=${data.groups} site-id=${data.siteId} can-create-question></sakai-add-topic>`);
    await waitUntil(() => el.querySelector("sakai-tag-selector"));
    const selector = el.querySelector("sakai-tag-selector");
    await waitUntil(() => selector.shadowRoot.querySelector("input"));
    expect(selector.addNew).not.to.be.true;
    const input = selector.shadowRoot.querySelector("input");
    input.value = "Not an existing tag";
    input.dispatchEvent(new Event("input"));
    await elementUpdated(selector);
    expect(selector.shadowRoot.querySelector("[role=option]")).not.to.exist;
    expect(fetchMock.callHistory.calls(/conversations\/tags/)).to.have.length(0);
  });

  it ("creates a new discussion topic", async () => {

    const el = await fixture(html`
      <sakai-add-topic .topic=${data.blankTopic}
          .tags=${data.tags}
          .groups=${data.groups}
          site-id="${data.siteId}"
          can-create-discussion
          can-create-question
          can-anon
          can-pin>
      </sakai-add-topic>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    el.querySelector(`div[data-type="${constants.DISCUSSION}"]`).click();

    const title = "Best Star Wars movie?";
    const message = "Best Star Wars movie? Rank them.";

    // Set the title
    const titleInput = el.querySelector("#summary");
    titleInput.value = title;
    setTimeout(() => titleInput.dispatchEvent(new Event("change")));
    let { detail } = await oneEvent(el, "save-wip-topic");
    expect(detail.topic.title).to.equal(title);

    const detailsInput = el.querySelector("#topic-details-editor");
    setTimeout(() => detailsInput.dispatchEvent(new CustomEvent("changed", { detail: { content: message } })));
    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.message).to.equal(message);

    const tagSelector = el.querySelector("sakai-tag-selector");
    await waitUntil(() => tagSelector.shadowRoot.querySelector("input"));
    const tagInput = tagSelector.shadowRoot.querySelector("input");
    tagInput.focus();
    await elementUpdated(tagSelector);
    const changed = oneEvent(el, "save-wip-topic");
    tagSelector.shadowRoot.querySelector("[role=option]").click();
    ({ detail } = await changed);
    expect(detail.topic.tags.find(t => t.id === data.tags[0].id)).to.exist;
    await elementUpdated(el);
    await expect(el).to.be.accessible();
    expect(tagSelector.selectedTags).to.have.length(1);

    // Pick a group
    expect(el.querySelector("#add-topic-groups-block")).to.not.exist;
    setTimeout(() => el.querySelector(`input[data-visibility="${constants.GROUP}"]`).click());
    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.visibility).to.equal(constants.GROUP);
    await elementUpdated(el);
    await expect(el).to.be.accessible();
    expect(el.querySelector(".add-topic-group-block")).to.exist;
    setTimeout(() => el.querySelector(".add-topic-group-block input").click());
    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.groups).to.contain(data.groups[0].reference);
    setTimeout(() => el.querySelector(".add-topic-group-block input").click());
    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.groups).to.not.contain(data.groups[0].reference);
    setTimeout(() => el.querySelector(".add-topic-group-block input").click());
    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.groups).to.contain(data.groups[0].reference);

    // Display the date options
    el.querySelector("input[aria-labelledby='availability-dated-label']").click();
    await elementUpdated(el);
    await expect(el).to.be.accessible();

    // Check the show date box
    el.querySelector("input[aria-labelledby='add-topic-show-label']").click();
    await elementUpdated(el);
    await expect(el).to.be.accessible();

    const day = 3600000;

    const showDate = Date.now() + day;

    const showdatePicker = el.querySelector("sakai-date-picker");
    setTimeout(() => showdatePicker.dispatchEvent(new CustomEvent("datetime-selected", { detail: { epochSeconds: showDate } })));

    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.showDate).to.equal(showDate);

    // Check the lock date box
    el.querySelector("input[aria-labelledby='add-topic-lock-label']").click();
    await elementUpdated(el);
    await expect(el).to.be.accessible();

    const lockDate = Date.now() + 3 * day;


    const lockdatePicker = el.querySelectorAll("sakai-date-picker").item(1);
    setTimeout(() => lockdatePicker.dispatchEvent(new CustomEvent("datetime-selected", { detail: { epochSeconds: lockDate } })));

    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.lockDate).to.equal(lockDate);

    // Check the hide date box
    el.querySelector("input[aria-labelledby='add-topic-hide-label']").click();
    await elementUpdated(el);
    await expect(el).to.be.accessible();

    const hideDate = Date.now() + 3 * day;

    const hidedatePicker = el.querySelectorAll("sakai-date-picker").item(2);
    setTimeout(() => hidedatePicker.dispatchEvent(new CustomEvent("datetime-selected", { detail: { epochSeconds: hideDate } })));

    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.hideDate).to.equal(hideDate);

    el.querySelector("input[aria-labelledby='add-topic-duedate-label']").click();
    await elementUpdated(el);
    await expect(el).to.be.accessible();

    const dueDate = Date.now() + 2 * day;
    const duedatePicker = el.querySelectorAll("sakai-date-picker").item(3);
    setTimeout(() => duedatePicker.dispatchEvent(new CustomEvent("datetime-selected", { detail: { epochSeconds: dueDate } })));

    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.dueDate).to.equal(dueDate);

    el.querySelector("input[aria-labelledby='add-topic-lockdate-label']").click();
    await elementUpdated(el);
    await expect(el).to.be.accessible();

    // Pinned checkbox on-off-on
    const pinnedCheckbox = el.querySelector("#pinned-checkbox");
    setTimeout(() => pinnedCheckbox.click());
    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.pinned).to.be.true;
    setTimeout(() => pinnedCheckbox.click());
    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.pinned).to.be.false;
    setTimeout(() => pinnedCheckbox.click());
    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.pinned).to.be.true;

    // Anon checkbox on-off-on
    const anonCheckbox = el.querySelector("#anon-checkbox");
    setTimeout(() => anonCheckbox.click());
    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.anonymous).to.be.true;
    setTimeout(() => anonCheckbox.click());
    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.anonymous).to.be.false;
    setTimeout(() => anonCheckbox.click());
    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.anonymous).to.be.true;

    // Allow anon checkbox on-off-on
    const allowAnonCheckbox = el.querySelectorAll("#post-options-block input[type='checkbox']").item(2);
    setTimeout(() => allowAnonCheckbox.click());
    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.allowAnonymousPosts).to.be.true;
    setTimeout(() => allowAnonCheckbox.click());
    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.allowAnonymousPosts).to.be.false;
    setTimeout(() => allowAnonCheckbox.click());
    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.allowAnonymousPosts).to.be.true;

    // must post checkbox on-off-on
    const mustPostCheckbox = el.querySelectorAll("#post-options-block input[type='checkbox']").item(3);
    setTimeout(() => mustPostCheckbox.click());
    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.mustPostBeforeViewing).to.be.true;
    setTimeout(() => mustPostCheckbox.click());
    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.mustPostBeforeViewing).to.be.false;
    setTimeout(() => mustPostCheckbox.click());
    ({ detail } = await oneEvent(el, "save-wip-topic"));
    expect(detail.topic.mustPostBeforeViewing).to.be.true;

    // Save as draft
    el.querySelector("#button-block input:nth-child(2)").click();
    ({ detail } = await oneEvent(el, "topic-saved"));
    expect(detail.topic.draft).to.be.true;
    expect(detail.topic.title).to.equal(title);
    expect(detail.topic.message).to.equal(message);

    await elementUpdated(el);
    await expect(el).to.be.accessible();

    // Publish
    el.querySelector("#button-block input").click();
    ({ detail } = await oneEvent(el, "topic-saved"));
    expect(detail.topic.draft).to.be.false;
    expect(detail.topic.title).to.equal(title);
    expect(detail.topic.message).to.equal(message);

    // Check that cancel fires the event
    setTimeout(() => el.querySelector("#button-block input:nth-child(3)").click());
    await oneEvent(el, "topic-add-cancelled");
  });
});
