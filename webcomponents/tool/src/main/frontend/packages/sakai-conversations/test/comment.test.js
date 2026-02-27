import "../sakai-comment.js";
import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import fetchMock from "fetch-mock";
describe("sakai-comment tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
		fetchMock.get(data.i18nUrl, data.i18n);

    data.comment.canEdit = false;
    data.comment.canDelete = false;
  });

	afterEach(() => {
		fetchMock.hardReset();
	});

  it ("does not render if no comment is supplied", async () => {

    const el = await fixture(html`<sakai-comment></sakai-comment>`);

    await waitUntil(() => el._i18n);

    expect(el.querySelector("div.post-comment")).to.not.exist;
  });

  it ("renders a comment", async () => {

    const el = await fixture(html`<sakai-comment .comment=${data.comment}></sakai-comment>`);

    await waitUntil(() => el._i18n);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector("div.post-comment")).to.exist;
    expect(el.querySelector(".post-comment-topbar > div:nth-child(2) > div").innerHTML).to.contain(data.commentCreatorDisplayName);
    expect(el.querySelector(".post-comment-topbar > div:nth-child(2) > div:nth-child(2)").innerHTML).to.contain(data.formattedCommentCreatedDate);

    expect(el.querySelector(".post-message").innerHTML).to.contain(data.commentMessage);

    expect(el.querySelector(".conv-dropdown-menu")).to.not.exist;

  });

  it ("renders a comment with edit permission", async () => {

    data.comment.canEdit = true;
    data.comment.canDelete = true;

    const el = await fixture(html`<sakai-comment .comment=${data.comment}></sakai-comment>`);

    await waitUntil(() => el._i18n);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector(".conv-dropdown-menu")).to.exist;
    expect(el.querySelectorAll(".conv-dropdown-menu > li").length).to.equal(2);
    expect(el.querySelector(".conv-dropdown-menu > li > button").innerHTML).to.contain(el._i18n.edit);
    expect(el.querySelector(".conv-dropdown-menu > li > button").innerHTML).to.contain(el._i18n.edit);
    expect(el.querySelector(".conv-dropdown-menu > li:nth-child(2) > button").innerHTML).to.contain(el._i18n.delete);
  });

  it ("renders the comment editor", async () => {

    data.comment.canEdit = true;

    const el = await fixture(html`<sakai-comment .comment=${data.comment}></sakai-comment>`);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    const ddButton = el.querySelector(".dropdown > button");
    expect(ddButton).to.exist;

    setTimeout(() => ddButton.click());
    await oneEvent(el, "shown.bs.dropdown");
    await expect(el).to.be.accessible();

    expect(el.querySelector(".post-edit-comment-block")).to.not.exist;

    const editButton = el.querySelector(".conv-dropdown-menu > li > button");
    expect(editButton).to.exist;

    editButton.click();

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector(".post-edit-comment-block")).to.exist;
  });

  it ("handles deletion of the comment", async () => {

    data.comment.canDelete = true;

    const el = await fixture(html`
      <sakai-comment site-id="${data.siteId}"
          topic-id="${data.topic1.id}"
          .comment=${data.comment}>
      </sakai-comment>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    const ddButton = el.querySelector(".dropdown > button");
    expect(ddButton).to.exist;

    setTimeout(() => ddButton.click());
    await oneEvent(el, "shown.bs.dropdown");

    await expect(el).to.be.accessible();

    const deleteButton = el.querySelector(".conv-dropdown-menu > li > button");
    expect(deleteButton).to.exist;

    fetchMock.delete(`/api/sites/${data.siteId}/topics/${data.topic1.id}/posts/${data.post1.id}/comments/${data.comment.id}`, 200)

    const originalConfirm = window.confirm;
    window.confirm = () => true;

    deleteButton.click();

    const { detail } = await oneEvent(el, "comment-deleted");

    expect(detail.comment.message).to.equal(data.comment.message);

    window.confirm = originalConfirm;
  });
});
