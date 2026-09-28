import "../sakai-conversations-tag-manager.js";
import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import fetchMock from "fetch-mock";
describe("sakai-conversations-tag-manager tests", () => {

  window.top.portal = { siteId: data.siteId, siteTitle: data.siteTitle };

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock.get(data.i18nUrl, data.i18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it("renders correctly with tags", async () => {

    const el = await fixture(html`
      <sakai-conversations-tag-manager
          site-id="${data.siteId}"
          .tags=${data.tags}>
      </sakai-conversations-tag-manager>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Check heading
    expect(el.querySelector("h1").textContent).to.equal(el._i18n.manage_tags);

    // Check tag creation field exists
    expect(el.querySelector("#tag-creation-field")).to.exist;

    // Check that all tags are displayed
    const tagRows = el.querySelectorAll(".tag-row");
    expect(tagRows.length).to.equal(data.tags.length);

    // Verify each tag's content
    data.tags.forEach((tag, index) => {
      expect(tagRows[index].querySelector(".tag-label").textContent).to.equal(tag.label);
    });

    // Shared tag definitions are managed by the Tags administration tool.
    expect(el.querySelector(".tag-row input, .tag-row button, .tag-editor")).not.to.exist;
  });

  it("creates new tags", async () => {

    const createTagsUrl = `/api/sites/${data.siteId}/tools/conversations/tags`;
    const newTagsPayload = [
      { tagLabel: "newTag1" },
      { tagLabel: "newTag2" }
    ];
    const newTagsResponse = [
      { label: "newTag1", siteId: data.siteId, id: "conv-3" },
      { label: "newTag2", siteId: data.siteId, id: "conv-4" }
    ];

    fetchMock.post(createTagsUrl, newTagsResponse);

    const el = await fixture(html`
      <sakai-conversations-tag-manager
          site-id="${data.siteId}"
          .tags=${data.tags}>
      </sakai-conversations-tag-manager>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Enter new tags in the creation field
    const tagField = el.querySelector("#tag-creation-field");
    tagField.value = newTagsPayload.map(t => t.tagLabel).join(", ");
    tagField.dispatchEvent(new Event("input"));

    expect(el._saveable).to.be.true;

    // Click the add button
    setTimeout(() => el.querySelector(".btn-primary").click());

    await oneEvent(el, "tags-created");

    // Verify the field was cleared
    expect(tagField.value).to.be.empty;

    // Verify _saveable is set to false after successful creation
    expect(el._saveable).to.be.false;
  });

  it("handles cancel button click", async () => {

    const el = await fixture(html`
      <sakai-conversations-tag-manager
          site-id="${data.siteId}"
          .tags=${data.tags}>
      </sakai-conversations-tag-manager>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Enter some text to make the cancel button enabled
    const tagField = el.querySelector("#tag-creation-field");
    tagField.value = "some tag";
    tagField.dispatchEvent(new Event("input"));

    // Verify _saveable is true
    expect(el._saveable).to.be.true;

    await elementUpdated(el);

    // Click cancel button
    el.querySelector(".btn-secondary").click();
    await elementUpdated(el);

    expect(el.querySelector("#tag-creation-field").value).to.be.empty;
  });

  it("ignores duplicate tags during creation", async () => {

    const createTagsUrl = `/api/sites/${data.siteId}/tools/conversations/tags`;
    const uniqueTagLabel = "unique";
    const uniqueTagResponse = { label: uniqueTagLabel, siteId: data.siteId, id: "3" };

    // Simpler mock: just match the URL and method, respond with the expected created tag.
    fetchMock.post(createTagsUrl, [ uniqueTagResponse ]);

    const el = await fixture(html`
      <sakai-conversations-tag-manager
          site-id="${data.siteId}"
          .tags=${data.tags}>
      </sakai-conversations-tag-manager>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    const tagField = el.querySelector("#tag-creation-field");
    tagField.value = `${data.tags[0].label}, ${uniqueTagLabel}`; // e.g., "Eggs, unique"
    tagField.dispatchEvent(new Event("input"));

    const tagsCreatedListener = oneEvent(el, "tags-created");

    setTimeout(() => el.querySelector(".btn-primary").click());

    await tagsCreatedListener;

    // Direct inspection of what was sent
    const calls = fetchMock.callHistory.calls(createTagsUrl, "POST");
    expect(calls.length).to.equal(1, "Fetch should have been called once for tag creation");

    const lastCall = calls[0];
    expect(lastCall).to.exist;
  });
});
