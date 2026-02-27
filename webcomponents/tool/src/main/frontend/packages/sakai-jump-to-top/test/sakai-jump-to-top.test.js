import { init } from "../sakai-jump-to-top.js";
import { expect, fixture, html } from "@open-wc/testing";
import * as data from "./data.js";
import * as sinon from "sinon";
import fetchMock from "fetch-mock";
describe("sakai-jump-to-top tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock.get(data.i18nUrl, data.i18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it ("renders correctly with i18n", async () => {

    const el = await fixture(html`<div class="portal-main-container"></div>`);

    await init();

    const scrollToSpy = sinon.spy(el.scrollTo);

    expect(el.querySelector("div button")).to.exist;
    el.querySelector("div button").click();

    scrollToSpy.withArgs({ top: 0, behavior: "smooth" });
  });
});
