import "../sakai-submission-messager.js";
import * as data from "./data.js";
import * as groupPickerData from "../../sakai-group-picker/test/data.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";
describe("sakai-submission-messager tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(groupPickerData.i18nUrl, groupPickerData.i18n)
      .get(groupPickerData.groupsUrl, groupPickerData.groups)
      .post("/direct/gbng/listMessageRecipients.json", data.recipients)
      .post("/direct/gbng/messageStudents.json", data.recipients)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  window.top.portal = { siteId: data.siteId };

  it ("renders correctly", async () => {

    let el = await fixture(html`
      <sakai-submission-messager assignment-id="${data.assignmentId}" title="${data.title}"></sakai-submission-messager>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector("div.submission-messager div").innerHTML).to.contain(data.title);

    const subjectInput = el.querySelector("div > div:nth-child(2) input");
    expect(subjectInput).to.exist;
    subjectInput.value = data.subject;
    subjectInput.dispatchEvent(new Event("change"));
    expect(el.subject).to.equal(data.subject);

    const messageInput = el.querySelector("textarea");
    expect(messageInput).to.exist;
    messageInput.value = data.body;
    messageInput.dispatchEvent(new Event("change"));
    expect(el.body).to.equal(data.body);

    const showRecipientsButton = el.querySelector(".sm-show-recipients-button");
    expect(showRecipientsButton).to.exist;
    showRecipientsButton.click();
    await waitUntil(() => el.recipients);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    await waitUntil(() => el.querySelector(".sm-recipients"), "Element did not render results");
    const recipientDivs = el.querySelectorAll(".sm-recipients > div");
    expect(recipientDivs.length).to.equal(data.recipients.length);

    data.recipients.forEach((r, i) => {
      expect(recipientDivs.item(i).innerHTML).to.contain(data.recipients[i].displayName);
    });

    const sendButton = el.querySelector("button.btn-primary");
    expect(sendButton).to.exist;
    sendButton.click();
  });
});
