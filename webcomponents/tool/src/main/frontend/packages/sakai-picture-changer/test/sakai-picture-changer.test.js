import "../sakai-picture-changer.js";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, html } from "@open-wc/testing";
import fetchMock from "fetch-mock";
describe("sakai-picture-changer tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(/\/api\/users\/me\/profile\/image\/details/, { status: "SUCCESS", url: "/packages/sakai-picture-changer/test/images/orville.jpeg" })
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });


  window.top.portal = { locale: "en_GB" };

  it ("renders correctly", async () => {

    let el = await fixture(html`
      <sakai-picture-changer></sakai-picture-changer>
    `);

    await elementUpdated(el);

    expect(document.getElementById("image")).to.not.exist;
  });
});
