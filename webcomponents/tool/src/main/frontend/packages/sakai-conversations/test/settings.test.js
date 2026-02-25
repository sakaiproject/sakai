import "../sakai-conversations-settings.js";
import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import fetchMock from "fetch-mock";

describe("sakai-conversations-settings tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .post(`/api/sites/${data.siteId}/conversations/settings/allowReactions`, 200)
      .post(`/api/sites/${data.siteId}/conversations/settings/allowUpvoting`, 200)
      .post(`/api/sites/${data.siteId}/conversations/settings/allowAnonPosting`, 200)
      .post(`/api/sites/${data.siteId}/conversations/settings/allowBookmarking`, 200)
      .post(`/api/sites/${data.siteId}/conversations/settings/allowPinning`, 200)
      .post(`/api/sites/${data.siteId}/conversations/settings/siteLocked`, 200)
      .post(`/api/sites/${data.siteId}/conversations/settings/requireGuidelinesAgreement`, 200)
      .post(`/api/sites/${data.siteId}/conversations/settings/guidelines`, 200);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  window.top.portal = { siteId: data.siteId, siteTitle: data.siteTitle, user: { id: "user1", timezone: "Europe/London" } };

  const settingsData = {
    allowReactions: true,
    allowUpvoting: true,
    allowAnonPosting: false,
    allowBookmarking: true,
    allowPinning: false,
    siteLocked: false,
    requireGuidelinesAgreement: true,
    guidelines: "These are the community guidelines for this site."
  };

  it("renders settings correctly", async () => {

    const el = await fixture(html`
      <sakai-conversations-settings
          site-id="${data.siteId}"
          .settings=${settingsData}>
      </sakai-conversations-settings>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Check if all toggle components are rendered
    const toggles = el.querySelectorAll("sakai-toggle");
    expect(toggles.length).to.equal(7); // 7 settings with toggles

    // Check if toggle states match the settings data
    expect(toggles[0].on).to.equal(settingsData.allowReactions ? true : undefined);
    expect(toggles[1].on).to.equal(settingsData.allowUpvoting ? true : undefined);
    expect(toggles[2].on).to.equal(settingsData.allowAnonPosting ? true : undefined);
    expect(toggles[3].on).to.equal(settingsData.allowBookmarking ? true : undefined);
    expect(toggles[4].on).to.equal(settingsData.allowPinning ? true : undefined);
    expect(toggles[5].on).to.equal(settingsData.siteLocked ? true : undefined);
    expect(toggles[6].on).to.equal(settingsData.requireGuidelinesAgreement ? true : undefined);

    // Check if guidelines are displayed when requireGuidelinesAgreement is true
    const guidelinesBlock = el.querySelector("#settings-guidelines-block");
    expect(guidelinesBlock).to.exist;

    // Check if guidelines preview contains the correct content
    const guidelinesPreview = el.querySelector("sakai-conversations-guidelines");
    expect(guidelinesPreview).to.exist;
    expect(guidelinesPreview.getAttribute("guidelines")).to.equal(settingsData.guidelines);
  });

  it("handles toggling settings", async () => {

    const el = await fixture(html`
      <sakai-conversations-settings
          site-id="${data.siteId}"
          .settings=${settingsData}>
      </sakai-conversations-settings>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    // Get the allowAnonPosting toggle (which is initially false)
    const anonToggle = el.querySelector("sakai-toggle[data-setting='allowAnonPosting']");
    expect(anonToggle).to.exist;
    expect(anonToggle.on).to.be.undefined;

    // Simulate toggling the setting
    anonToggle.dispatchEvent(new CustomEvent("toggled", { detail: { on: true }, bubbles: true }));

    // Wait for the event and check its details
    const { detail } = await oneEvent(el, "setting-updated");
    expect(detail.setting).to.equal("allowAnonPosting");
    expect(detail.on).to.be.true;
  });

  it("handles editing guidelines", async () => {

    const el = await fixture(html`
      <sakai-conversations-settings
          site-id="${data.siteId}"
          .settings=${settingsData}>
      </sakai-conversations-settings>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    // Check initial state - edit button should be visible
    const editButton = el.querySelector("input[type='button'][value='" + el._i18n.edit_guidelines + "']");
    expect(editButton).to.exist;

    // Click edit button
    editButton.click();
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Editor should now be visible
    const editor = el.querySelector("#settings-guidelines-editor");
    expect(editor).to.exist;
    expect(editor.value).to.equal(settingsData.guidelines);

    // Modify the guidelines
    const newGuidelines = "These are the updated community guidelines.";
    editor.value = newGuidelines;

    // Click save button
    el.querySelector("input[type='button'][value='" + el._i18n.save + "']").click();

    // Wait for the event and check its details
    const { detail } = await oneEvent(el, "guidelines-saved");
    expect(detail.guidelines).to.equal(newGuidelines);

    // Editor should be hidden again
    await elementUpdated(el);
    expect(el.querySelector("#settings-guidelines-editor")).to.not.exist;
  });

  it("handles canceling guidelines edit", async () => {

    const el = await fixture(html`
      <sakai-conversations-settings
          site-id="${data.siteId}"
          .settings=${settingsData}>
      </sakai-conversations-settings>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    // Click edit button
    const editButton = el.querySelector("input[type='button'][value='" + el._i18n.edit_guidelines + "']");
    editButton.click();
    await elementUpdated(el);

    // Editor should be visible
    expect(el.querySelector("#settings-guidelines-editor")).to.exist;

    // Click cancel button
    const cancelButton = el.querySelector("input[type='button'][value='" + el._i18n.cancel + "']");
    cancelButton.click();
    await elementUpdated(el);

    // Editor should be hidden again
    expect(el.querySelector("#settings-guidelines-editor")).to.not.exist;
  });

  it("uses sample guidelines when none are provided", async () => {

    // Create settings without guidelines
    const settingsWithoutGuidelines = { ...settingsData, guidelines: "" };

    const el = await fixture(html`
      <sakai-conversations-settings
          site-id="${data.siteId}"
          .settings=${settingsWithoutGuidelines}>
      </sakai-conversations-settings>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Check if the sample guidelines are used
    const guidelinesPreview = el.querySelector("sakai-conversations-guidelines");
    expect(guidelinesPreview.getAttribute("guidelines")).to.equal(el._i18n.community_guidelines_sample);
  });

  it("hides guidelines section when requireGuidelinesAgreement is false", async () => {

    // Create settings with requireGuidelinesAgreement set to false
    const settingsWithoutRequirement = { ...settingsData, requireGuidelinesAgreement: false };

    const el = await fixture(html`
      <sakai-conversations-settings
          site-id="${data.siteId}"
          .settings=${settingsWithoutRequirement}>
      </sakai-conversations-settings>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Check if guidelines block is not displayed
    const guidelinesBlock = el.querySelector("#settings-guidelines-block");
    expect(guidelinesBlock).to.not.exist;
  });
});
