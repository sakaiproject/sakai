import "../sakai-conversations.js";
import { expect, fixture, waitUntil } from "@open-wc/testing";
import { html } from "lit";
import * as data from "./data.js";
import fetchMock from "fetch-mock/esm/client";
import * as constants from "../src/sakai-conversations-constants.js";

describe("sakai-conversations tests", () => {

  window.top.portal = { locale: "en_GB", siteId: data.siteId, siteTitle: data.siteTitle };
  window.top.sakai = {
    editor: { launch: function() { return { setData: function () {}, on: function () {} } } },
  };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get(`/api/sites/${data.siteId}/conversations`, data.data, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders add topic button correctly", async () => {
 
    const el = await fixture(html`<sakai-conversations site-id="${data.siteId}"></sakai-conversations>`);
    await waitUntil(() => el._i18n);
    await waitUntil(() => el._data);

    expect(el.querySelector(".conv-add-topic")).to.exist;

    data.data.canCreateTopic = false;
    el._data = data.data;
    await el.updateComplete;
    expect(el.querySelector(".conv-add-topic")).to.not.exist;
  });

  it ("renders add new topic screen correctly", async () => {

    data.data.canCreateTopic = true;
 
    const el = await fixture(html`<sakai-conversations site-id="${data.siteId}"></sakai-conversations>`);
    await waitUntil(() => el._i18n);
    await waitUntil(() => el._data);

    const addTopicButton = el.querySelector(".conv-add-topic").parentElement;
    expect(addTopicButton).to.exist;

    expect(el.querySelector("sakai-add-topic")).to.not.exist;

    addTopicButton.click();

    await waitUntil(() => el.querySelector("sakai-add-topic"), "sakai-add-topic not created");

    const sakaiAddTopic = el.querySelector("sakai-add-topic")
    expect(sakaiAddTopic).to.exist;

    expect(sakaiAddTopic.querySelector(`div[data-type='${constants.DISCUSSION}']`)).to.exist;
    expect(sakaiAddTopic.querySelector(`div[data-type='${constants.QUESTION}']`)).to.exist;
    expect(sakaiAddTopic.querySelector("#tag-post-block select")).to.exist;
    expect(sakaiAddTopic.querySelectorAll("#tag-post-block select > option").length).to.equal(2);

    expect(sakaiAddTopic.querySelector("#conv-edit-tags-link-wrapper")).to.not.exist;
    sakaiAddTopic.canEditTags = true;
    await el.updateComplete;
    expect(sakaiAddTopic.querySelector("#conv-edit-tags-link-wrapper")).to.exist;
  });

  it ("is accessible", async () => {

    const el = await fixture(html`<sakai-conversations site-id="${data.siteId}"></sakai-conversations>`);
    await waitUntil(() => el._i18n);
    await waitUntil(() => el._data);

    await expect(el).to.be.accessible({ ignoredRules: [ "duplicate-id" ] });
  });
});
