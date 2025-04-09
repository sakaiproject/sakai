import "../sakai-conversations-tag-manager.js";
import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import sinon from "sinon";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-conversations-tag-manager tests", () => {

  window.top.portal = { siteId: data.siteId, siteTitle: data.siteTitle };

  beforeEach(() => {
    fetchMock.get(data.i18nUrl, data.i18n);
  });

  afterEach(() => {
    fetchMock.restore();
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

    // Check edit and delete buttons exist for each tag
    expect(el.querySelectorAll(".tag-buttons input[type='button']").length).to.equal(data.tags.length * 2);
  });

  it("creates new tags", async () => {

    const createTagsUrl = `/api/sites/${data.siteId}/conversations/tags`;
    const newTags = [
      { label: "newTag1", siteId: data.siteId, id: 3 },
      { label: "newTag2", siteId: data.siteId, id: 4 }
    ];

    fetchMock.post(createTagsUrl, newTags);

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
    tagField.value = newTags.map(t => t.label).join(", ");
    tagField.dispatchEvent(new Event("input"));

    expect(el._saveable).to.be.true;

    // Click the add button
    //el.querySelector(".btn-primary").click();
    setTimeout(() => el.querySelector(".btn-primary").click());

    const { detail } = await oneEvent(el, "tags-created");

    // Verify the event contains the new tags
    expect(detail.tags).to.deep.equal(newTags);

    // Verify the field was cleared
    expect(tagField.value).to.be.empty;

    // Verify _saveable is set to false after successful creation
    expect(el._saveable).to.be.false;
  });

  it("edits an existing tag", async () => {

    const tagId = data.tags[0].id;
    const updateUrl = `/api/sites/${data.siteId}/conversations/tags/${tagId}`;
    const updatedTag = { ...data.tags[0], label: "updated-eggs" };

    fetchMock.put(updateUrl, updatedTag);

    const el = await fixture(html`
      <sakai-conversations-tag-manager
          site-id="${data.siteId}"
          .tags=${data.tags}>
      </sakai-conversations-tag-manager>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Click edit button for the first tag
    const editButton = el.querySelector(`.tag-buttons input[data-tag-id="${tagId}"][value="${el._i18n.edit}"]`);
    editButton.click();
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Verify tag editor is displayed
    const tagEditor = el.querySelector(`#tag-${tagId}-editor`);
    expect(tagEditor).to.exist;
    expect(tagEditor.value).to.equal(data.tags[0].label);

    // Change the tag name
    tagEditor.value = updatedTag.label;

    // Set up listener for tag-updated event
    const tagUpdatedPromise = oneEvent(el, "tag-updated");

    // Click save button
    setTimeout(() => el.querySelector(`.tag-editor input[data-tag-id="${tagId}"][value="${el._i18n.save}"]`).click());

    // Wait for the event
    const { detail } = await tagUpdatedPromise;

    // Verify the event contains the updated tag
    expect(detail.tag).to.deep.equal(updatedTag);

    // Verify editor is no longer displayed
    expect(el._tagsBeingEdited.includes(parseInt(tagId))).to.be.false;
  });

  it("cancels tag editing", async () => {

    const el = await fixture(html`
      <sakai-conversations-tag-manager
          site-id="${data.siteId}"
          .tags=${data.tags}>
      </sakai-conversations-tag-manager>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Click edit button for the first tag
    const tagId = data.tags[0].id;
    const editButton = el.querySelector(`.tag-buttons input[data-tag-id="${tagId}"][value="${el._i18n.edit}"]`);
    editButton.click();
    await elementUpdated(el);

    // Verify tag editor is displayed
    expect(el._tagsBeingEdited.includes(parseInt(tagId))).to.be.true;
    expect(el.querySelector(`#tag-${tagId}-editor`)).to.exist;

    // Click cancel button
    el.querySelector(`.tag-editor input[data-tag-id="${tagId}"][value="${el._i18n.cancel}"]`).click();
    await elementUpdated(el);

    // Verify editor is no longer displayed
    expect(el._tagsBeingEdited.includes(parseInt(tagId))).to.be.false;
    expect(el.querySelector(`#tag-${tagId}-editor`)).to.not.exist;
  });

  it("deletes a tag", async () => {

    const tagId = data.tags[0].id;
    const deleteUrl = `/api/sites/${data.siteId}/conversations/tags/${tagId}`;

    fetchMock.delete(deleteUrl, 200);

    const el = await fixture(html`
      <sakai-conversations-tag-manager
          site-id="${data.siteId}"
          .tags=${data.tags}>
      </sakai-conversations-tag-manager>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Mock confirm to return true
    const originalConfirm = window.confirm;
    window.confirm = () => true;

    // Set up listener for tag-deleted event
    const tagDeletedPromise = oneEvent(el, "tag-deleted");

    // Click delete button
    setTimeout(() => el.querySelector(`.tag-buttons input[data-tag-id="${tagId}"][value="${el._i18n.delete}"]`).click());

    // Wait for the event
    const { detail } = await tagDeletedPromise;

    // Verify the event contains the deleted tag id
    expect(detail.id == tagId).to.be.true;

    // Restore original confirm
    window.confirm = originalConfirm;
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

    const createTagsUrl = `/api/sites/${data.siteId}/conversations/tags`;
    const newTag = { label: "unique", siteId: data.siteId, id: "3" };

    fetchMock.post(createTagsUrl, [newTag]);

    const el = await fixture(html`
      <sakai-conversations-tag-manager
          site-id="${data.siteId}"
          .tags=${data.tags}>
      </sakai-conversations-tag-manager>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Enter new tags including an existing one
    const tagField = el.querySelector("#tag-creation-field");
    tagField.value = `${data.tags[0].label}, unique`;
    tagField.dispatchEvent(new Event("input"));

    // Set up listener for tags-created event
    const tagsCreatedPromise = oneEvent(el, "tags-created");

    // Click the add button
    setTimeout(() => el.querySelector(".btn-primary").click());

    // Wait for the event
    const { detail } = await tagsCreatedPromise;

    // Verify only the unique tag was created
    expect(detail.tags.length).to.equal(1);
    expect(detail.tags[0].label).to.equal("unique");
  });
});
