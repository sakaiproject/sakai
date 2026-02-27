import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from "@open-wc/testing";
import "../sakai-comment-editor.js";
import * as data from "./data.js";
import sinon from "sinon";
import * as constants from "../src/sakai-conversations-constants.js";
import fetchMock from "fetch-mock";
describe("sakai-comment-editor tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock.get(data.i18nUrl, data.i18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it("renders in add mode correctly", async () => {

    const el = await fixture(html`
      <sakai-comment-editor
        post-id="${data.post1.id}"
        site-id="site1"
        topic-id="topic1">
      </sakai-comment-editor>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Should start in non-editing mode with the placeholder text
    expect(el.querySelector(".comment-editor-input")).to.exist;
    expect(el.querySelector("sakai-editor")).to.not.exist;
  });

  it("switches to edit mode on click", async () => {

    const el = await fixture(html`
      <sakai-comment-editor
        post-id="${data.post1.id}"
        site-id="site1"
        topic-id="topic1">
      </sakai-comment-editor>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    // Click the input to start editing
    el.querySelector(".comment-editor-input").click();
    await elementUpdated(el);
    await expect(el).to.be.accessible();

    // Should now be in editing mode with the editor visible
    expect(el.querySelector(".comment-editor-input")).to.not.exist;
    expect(el.querySelector("sakai-editor")).to.exist;
  });

  it("renders in edit mode with existing comment", async () => {

    const el = await fixture(html`
      <sakai-comment-editor
        .comment=${data.comment}
        post-id="${data.post1.id}"
        site-id="site1"
        topic-id="topic1">
      </sakai-comment-editor>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Should start in editing mode with the comment content
    expect(el.querySelector(".comment-editor-input")).to.not.exist;
    expect(el.querySelector("sakai-editor")).to.exist;
  });

  it("creates a new comment", async () => {

    const newId = "new-comment-id";
    fetchMock.post("/api/sites/site1/topics/topic1/posts/post1/comments", ({ url, options }) => {
      return { ...JSON.parse(options.body), id: newId };
    });

    const el = await fixture(html`
      <sakai-comment-editor
        post-id="${data.post1.id}"
        site-id="site1"
        topic-id="topic1">
      </sakai-comment-editor>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    const newComment = "This is a new comment";

    // Start editing
    el.querySelector(".comment-editor-input").click();

    await elementUpdated(el);
    await expect(el).to.be.accessible();

    // Mock the editor content
    const sakaiEditor = el.querySelector("sakai-editor");
    expect(sakaiEditor).to.exist;
    sakaiEditor.setContent(newComment);

    // Click publish button
    el.querySelector("input.active").click();

    // Wait for the event to be dispatched
    const { detail } = await oneEvent(el, "comment-created");

    // Verify the event contains the new comment
    expect(detail.comment.message).to.equal(newComment);
    expect(detail.comment.id).to.equal(newId);

    await elementUpdated(el);

    // Verify the component returned to non-editing state
    //await waitUntil(() => editor.querySelector(".comment-editor-input"));
    expect(el.querySelector(".comment-editor-input")).to.exist;
  });

  it("updates an existing comment", async () => {

    fetchMock.put(`/api/sites/site1/topics/topic1/posts/${data.post1.id}/comments/${data.comment.id}`, ({ url, options }) => JSON.parse(options.body));

    const el = await fixture(html`
      <sakai-comment-editor
        .comment="${data.comment}"
        post-id="${data.post1.id}"
        site-id="site1"
        topic-id="topic1">
      </sakai-comment-editor>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    const newComment = "This is the new comment";

    const sakaiEditor = el.querySelector("sakai-editor");
    sakaiEditor.setContent(newComment);

    // Click publish button
    el.querySelector("input.active").click();

    // Wait for the event to be dispatched
    const { detail } = await oneEvent(el, "comment-updated");

    // Verify the event contains the updated comment
    expect(detail.comment.message).to.equal(newComment);
  });

  it("cancels editing", async () => {

    const el = await fixture(html`
      <sakai-comment-editor
        post-id="${data.post1.id}"
        site-id="site1"
        topic-id="topic1">
      </sakai-comment-editor>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    // Start editing
    el.querySelector(".comment-editor-input").click();
    await elementUpdated(el);
    await expect(el).to.be.accessible();

    // Click cancel button
    setTimeout(() => el.querySelector(`input[value="${el._i18n.cancel}"]`).click());
    await elementUpdated(el);

    // Wait for the event to be dispatched
    await oneEvent(el, "editing-cancelled");

    // Verify the component returned to non-editing state
    expect(el.querySelector(".comment-editor-input")).to.exist;
  });
});
