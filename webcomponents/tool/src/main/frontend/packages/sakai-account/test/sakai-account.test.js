import "../sakai-account.js";
import * as data from "./data.js";
import * as audioRecorderData from "../../sakai-audio-recorder/test/data.js";
import { waitUntil, elementUpdated, expect, fixture, html } from "@open-wc/testing";
import fetchMock from "fetch-mock";
import { spy } from "sinon";
describe("sakai-account tests", () => {

  beforeEach(async () => {
    fetchMock.mockGlobal();
    fetchMock.get(data.i18nUrl, data.i18n)
    .get(audioRecorderData.i18nUrl, audioRecorderData.i18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it ("allows a user to enter their basic details", async () => {

    const profile = { ...data.emptyProfile, phoneticPronunciation: "Jaymz", canEditNameAndEmail: true };
    fetchMock.get(data.profileUrl, profile);

    const el = await fixture(html`
      <sakai-account user-id="${data.userId}"></sakai-account>
    `);

    await waitUntil(() => el._profile);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.renderRoot.querySelectorAll(".profile_instruction").length).to.equal(3);

    el.renderRoot.querySelector("#basic-info-edit-button").click();
    await elementUpdated(el);

    const firstName = "James";
    const lastName = "Bond";
    const nickname = "Jimmy";
    const pronouns = "secret/agent";

    fetchMock.patch(data.profileUrl, { ...profile, firstName, lastName, nickname, pronouns }, { name: "patchRequest" });

    el.renderRoot.querySelector("#first-name-input").value = firstName;
    el.renderRoot.querySelector("#last-name-input").value = lastName;
    el.renderRoot.querySelector("#nickname-input").value = nickname;
    el.renderRoot.querySelector("#pronouns-input").value = pronouns;
    const saveButton = el.renderRoot.querySelector("#basic-info-save-button");
    expect(saveButton).to.exist;
    saveButton.click();

    // Now we can inspect the fetch call
    const patchCalls = fetchMock.callHistory.calls("patchRequest");
    expect(patchCalls.length).to.equal(1);
    const patch = JSON.parse(patchCalls[0].options.body);
    expect(patch[0].value).to.equal(firstName);
    expect(patch[1].value).to.equal(lastName);
    expect(patch[2].value).to.equal(nickname);
    expect(patch[3].value).to.equal(pronouns);

    await elementUpdated(el);
    await waitUntil(() => el._profile.firstName === firstName);

    expect(el._profile.firstName).to.equal(firstName);
    expect(el._profile.lastName).to.equal(lastName);
    expect(el._profile.nickname).to.equal(nickname);
    expect(el._profile.pronouns).to.equal(pronouns);
    expect(el._profile.phoneticName).to.equal(profile.phoneticName);
    expect(el.renderRoot.querySelector("#basic-info-save-button")).to.not.exist;
  });

  it ("allows a user to enter their pronunciation details", async () => {

    const profile = { ...data.emptyProfile, firstName: "James" };
    fetchMock.get(data.profileUrl, profile);

    const el = await fixture(html`
      <sakai-account user-id="${data.userId}"></sakai-account>
    `);

    await elementUpdated(el);

    await waitUntil(() => el.renderRoot.querySelectorAll(".content")[0]);

    el.renderRoot.querySelector("#pronunciation-info-edit-button").click();

    await elementUpdated(el);

    const phoneticName = "Jaymz";

    fetchMock.patch(data.profileUrl, { ...profile, phoneticPronunciation: phoneticName }, { name: "patchRequest" });

    el.renderRoot.querySelector("#phonetic-pronunciation-input").value = phoneticName;
    const saveButton = el.renderRoot.querySelector("#pronunciation-info-save-button");
    expect(saveButton).to.exist;
    saveButton.click();

    await waitUntil(() => !el._editingPronunciationInfo);

    // Now we can inspect the fetch call
    const patchCalls = fetchMock.callHistory.calls("patchRequest");
    expect(patchCalls.length).to.equal(1);
    const patch = JSON.parse(patchCalls[0].options.body);
    expect(patch[0].value).to.equal(phoneticName);

    await waitUntil(() => !el.editingPronunciationInfo);
    await elementUpdated(el);
    expect(el._profile.firstName).to.equal(profile.firstName);
    expect(el._profile.phoneticPronunciation).to.equal(phoneticName);
    expect(el.renderRoot.querySelector("#pronunciation-info-save-button")).to.not.exist;
  });

  it ("allows a user to enter their contact details", async () => {

    const profile = { ...data.emptyProfile, firstName: "James" };
    fetchMock.get(data.profileUrl, profile);

    const el = await fixture(html`
      <sakai-account user-id="${data.userId}"></sakai-account>
    `);

    await elementUpdated(el);

    await waitUntil(() => el.renderRoot.querySelectorAll(".content")[0]);

    el.renderRoot.querySelector("#contact-info-edit-button").click();

    await elementUpdated(el);

    const email = "bananas@pyjamas.com";
    const mobile = "0483 4444";

    fetchMock.patch(data.profileUrl, { ...profile, email, mobile }, { name: "patchRequest" });

    el.renderRoot.querySelector("#email-input").value = email;
    el.renderRoot.querySelector("#mobile-input").value = mobile;
    const saveButton = el.renderRoot.querySelector("#contact-info-save-button");
    expect(saveButton).to.exist;
    saveButton.click();

    // Now we can inspect the fetch call
    const patchCalls = fetchMock.callHistory.calls("patchRequest");
    expect(patchCalls.length).to.equal(1);
    const patch = JSON.parse(patchCalls[0].options.body);
    expect(patch[0].value).to.equal(email);
    expect(patch[1].value).to.equal(mobile);

    await elementUpdated(el);

    await waitUntil(() => el._profile.email === email);

    expect(el._profile.email).to.equal(email);
    expect(el._profile.mobile).to.equal(mobile);
    expect(el._profile.firstName).to.equal(profile.firstName);
    expect(el.renderRoot.querySelector("#contact-info-save-button")).to.not.exist;
  });

  it ("allows a user to enter their contact details", async () => {

    const profile = { ...data.emptyProfile, firstName: "James" };
    fetchMock.get(data.profileUrl, profile);

    const el = await fixture(html`
      <sakai-account user-id="${data.userId}"></sakai-account>
    `);

    await elementUpdated(el);

    await waitUntil(() => el.renderRoot.querySelectorAll(".content")[0]);

    el.renderRoot.querySelector("#social-info-edit-button").click();

    await elementUpdated(el);

    const facebookUrl = "https://facebook.com/adrian";
    const linkedinUrl = "https://linkedin.com/adrian";
    const instagramUrl = "https://instagram.com/adrian";

    fetchMock.patch(data.profileUrl, { ...profile, facebookUrl, linkedinUrl, instagramUrl }, { name: "patchRequest" });

    el.renderRoot.querySelector("#facebook-input").value = facebookUrl;
    el.renderRoot.querySelector("#linkedin-input").value = linkedinUrl;
    el.renderRoot.querySelector("#instagram-input").value = instagramUrl;
    const saveButton = el.renderRoot.querySelector("#social-info-save-button");
    expect(saveButton).to.exist;
    saveButton.click();

    // Now we can inspect the fetch call
    const patchCalls = fetchMock.callHistory.calls("patchRequest");
    expect(patchCalls.length).to.equal(1);
    const patch = JSON.parse(patchCalls[0].options.body);
    expect(patch[0].value).to.equal(facebookUrl);
    expect(patch[1].value).to.equal(instagramUrl);
    expect(patch[2].value).to.equal(linkedinUrl);

    await elementUpdated(el);

    await waitUntil(() => el._profile.facebookUrl === facebookUrl);

    expect(el._profile.facebookUrl).to.equal(facebookUrl);
    expect(el._profile.linkedinUrl).to.equal(linkedinUrl);
    expect(el._profile.instagramUrl).to.equal(instagramUrl);
    expect(el._profile.firstName).to.equal(profile.firstName);
    expect(el.renderRoot.querySelector("#social-info-save-button")).to.not.exist;
  });

  it ("allows a user to cancel their basic details changes", async () => {

    const profile = { ...data.emptyProfile, firstName: "Adrian", lastName: "Fish", nickname: "Fishy", pronouns: "he/him" };
    fetchMock.get(data.profileUrl, profile);

    const el = await fixture(html`
      <sakai-account user-id="${data.userId}"></sakai-account>
    `);

    await elementUpdated(el);

    await waitUntil(() => el.renderRoot.querySelectorAll(".content")[0]);

    el.renderRoot.querySelector("#basic-info-edit-button").click();

    await elementUpdated(el);

    el.renderRoot.querySelector("#first-name-input").value = "James";
    el.renderRoot.querySelector("#last-name-input").value = "Bond";
    el.renderRoot.querySelector("#nickname-input").value = "Jimmy";
    el.renderRoot.querySelector("#pronouns-input").value = "secret/agent";
    const cancelButton = el.renderRoot.querySelector("#basic-info-cancel-button");
    expect(cancelButton).to.exist;
    cancelButton.click();

    await elementUpdated(el);

    await waitUntil(() => !el.editingBasicInfo);

    expect(el.renderRoot.querySelector("#basic-info-save-button")).to.not.exist;

    el.renderRoot.querySelector("#basic-info-edit-button").click();
    await elementUpdated(el);

    expect(el.renderRoot.querySelector("#first-name-input").value).to.equal(profile.firstName);
    expect(el.renderRoot.querySelector("#last-name-input").value).to.equal(profile.lastName);
    expect(el.renderRoot.querySelector("#nickname-input").value).to.equal(profile.nickname);
    expect(el.renderRoot.querySelector("#pronouns-input").value).to.equal(profile.pronouns);
  });

  it ("allows a user to cancel their pronunciation details changes", async () => {

    const profile = { ...data.emptyProfile, phoneticPronunciation: "Ay-Dree-An" };
    fetchMock.get(data.profileUrl, profile);

    const el = await fixture(html`
      <sakai-account user-id="${data.userId}"></sakai-account>
    `);

    await elementUpdated(el);

    await waitUntil(() => el.renderRoot.querySelectorAll(".content")[0]);

    el.renderRoot.querySelector("#pronunciation-info-edit-button").click();

    await elementUpdated(el);

    el.renderRoot.querySelector("#phonetic-pronunciation-input").value = "Jaymz";
    const cancelButton = el.renderRoot.querySelector("#pronunciation-info-cancel-button");
    expect(cancelButton).to.exist;
    cancelButton.click();

    await elementUpdated(el);

    await waitUntil(() => !el.editingPronunciationInfo);

    expect(el.renderRoot.querySelector("#pronunciation-info-save-button")).to.not.exist;

    el.renderRoot.querySelector("#pronunciation-info-edit-button").click();
    await elementUpdated(el);
    expect(el.renderRoot.querySelector("#phonetic-pronunciation-input").value).to.equal(profile.phoneticPronunciation);
  });

  it ("allows a user to cancel their contact details changes", async () => {

    const profile = { ...data.emptyProfile, email: "adrian@mailinator.com", mobile: "0483 4444" };
    fetchMock.get(data.profileUrl, profile);

    const el = await fixture(html`
      <sakai-account user-id="${data.userId}"></sakai-account>
    `);

    await elementUpdated(el);

    await waitUntil(() => el.renderRoot.querySelectorAll(".content")[0]);

    el.renderRoot.querySelector("#contact-info-edit-button").click();

    await elementUpdated(el);

    el.renderRoot.querySelector("#email-input").value = "bananas@pyjamas.com";
    el.renderRoot.querySelector("#mobile-input").value = "0483 4444";

    const cancelButton = el.renderRoot.querySelector("#contact-info-cancel-button");
    expect(cancelButton).to.exist;
    cancelButton.click();

    await elementUpdated(el);

    await waitUntil(() => !el.editingContactInfo);

    expect(el.renderRoot.querySelector("#contact-info-save-button")).to.not.exist;

    el.renderRoot.querySelector("#contact-info-edit-button").click();
    await elementUpdated(el);
    expect(el.renderRoot.querySelector("#email-input").value).to.equal(profile.email);
    expect(el.renderRoot.querySelector("#mobile-input").value).to.equal(profile.mobile);
  });

  it ("allows a user to cancel their social details changes", async () => {

    const profile = { ...data.emptyProfile, facebookUrl: "https://facebook.com/adrian", linkedinUrl: "https://linkedin.com/adrian", instagramUrl: "https://instagram.com/adrian" };
    fetchMock.get(data.profileUrl, profile);

    const el = await fixture(html`
      <sakai-account user-id="${data.userId}"></sakai-account>
    `);

    await elementUpdated(el);

    await waitUntil(() => el.renderRoot.querySelectorAll(".content")[0]);

    el.renderRoot.querySelector("#social-info-edit-button").click();

    await elementUpdated(el);

    el.renderRoot.querySelector("#facebook-input").value = "https://facebook.com/james";
    el.renderRoot.querySelector("#linkedin-input").value = "https://linkedin.com/james";
    el.renderRoot.querySelector("#instagram-input").value = "https://instagram.com/james";

    const cancelButton = el.renderRoot.querySelector("#social-info-cancel-button");
    expect(cancelButton).to.exist;
    cancelButton.click();

    await elementUpdated(el);

    await waitUntil(() => !el.editingSocialInfo);

    expect(el.renderRoot.querySelector("#social-info-save-button")).to.not.exist;

    el.renderRoot.querySelector("#social-info-edit-button").click();
    await elementUpdated(el);
    expect(el.renderRoot.querySelector("#facebook-input").value).to.equal(profile.facebookUrl);
    expect(el.renderRoot.querySelector("#linkedin-input").value).to.equal(profile.linkedinUrl);
    expect(el.renderRoot.querySelector("#instagram-input").value).to.equal(profile.instagramUrl);
  });

  it ("displays an error banner when a user fails to update their basic details", async () => {

    const profile = { ...data.emptyProfile, firstName: "Adrian", lastName: "Fish", nickname: "Fishy", pronouns: "he/him" };
    displayErrorBannerTest(profile, "basic");
  });

  it ("displays an error banner when a user fails to update their contact details", async () => {

    const profile = { ...data.emptyProfile, email: "adrian@mailinator.com", mobile: "0483 4444" };
    displayErrorBannerTest(profile, "contact");
  });

  it ("displays an error banner when a user fails to update their social details", async () => {

    const profile = { ...data.emptyProfile, facebookUrl: "https://facebook.com/adrian", linkedinUrl: "https://linkedin.com/adrian", instagramUrl: "https://instagram.com/adrian" };
    displayErrorBannerTest(profile, "social");
  });

  async function displayErrorBannerTest(profile, prefix) {

    fetchMock.get(data.profileUrl, profile);

    const el = await fixture(html`
      <sakai-account user-id="${data.userId}"></sakai-account>
    `);

    await elementUpdated(el);

    await waitUntil(() => el.renderRoot.querySelectorAll(".content")[0]);

    el.renderRoot.querySelector(`#${prefix}-info-edit-button`).click();

    await elementUpdated(el);

    fetchMock.patch(data.profileUrl, 500, { name: "patchRequest" });

    const consoleSpy = spy(console, "error");

    el.renderRoot.querySelector(`#${prefix}-info-save-button`).click();

    await elementUpdated(el);

    await waitUntil(() => el.renderRoot.querySelector(".sak-banner-error"));

    expect(el.renderRoot.querySelector(".sak-banner-error").textContent).to.equal(el._i18n.contact_info_error);

    expect(consoleSpy.calledOnce).to.be.true;
    consoleSpy.restore();
  }
});
