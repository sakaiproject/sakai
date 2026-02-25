import "../sakai-forums.js";
import * as data from "./data.js";
import * as sitePickerData from "../../sakai-site-picker/test/data.js";
import { elementUpdated, expect, fixture, html } from "@open-wc/testing";
import fetchMock from "fetch-mock";
describe("sakai-forums tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(sitePickerData.i18nUrl, sitePickerData.i18n)
      .get(data.userForumsUrl, {forums: data.userForums, sites: sitePickerData.sites})
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it ("renders in user mode correctly", async () => {

    // In user mode, we'd expect to get announcements from multiple sites.
    const el = await fixture(html`
      <sakai-forums user-id="${data.userId}"></sakai-forums>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.shadowRoot.querySelectorAll(".messages > div").length).to.equal(9);

    const sortByMessagesLink = el.shadowRoot.querySelector(`a[title="${el._i18n.sort_by_messages_tooltip}"]`);
    expect(sortByMessagesLink).to.exist;
    sortByMessagesLink.click();

    await elementUpdated(el);

    expect(el.shadowRoot.querySelectorAll(".messages > div.cell > a").item(0).innerHTML).to.contain("3");

    sortByMessagesLink.click();

    await elementUpdated(el);

    expect(el.shadowRoot.querySelectorAll(".messages > div.cell > a").item(0).innerHTML).to.contain("5");

    const sortByForumsLink = el.shadowRoot.querySelector(`a[title="${el._i18n.sort_by_forums_tooltip}"]`);
    expect(sortByForumsLink).to.exist;
    sortByForumsLink.click();

    await elementUpdated(el);

    expect(el.shadowRoot.querySelectorAll(".messages > div.cell > a").item(1).innerHTML).to.contain("2");
    sortByForumsLink.click();

    await elementUpdated(el);

    expect(el.shadowRoot.querySelectorAll(".messages > div.cell > a").item(1).innerHTML).to.contain("8");
    await expect(el).to.be.accessible();
  });
});
