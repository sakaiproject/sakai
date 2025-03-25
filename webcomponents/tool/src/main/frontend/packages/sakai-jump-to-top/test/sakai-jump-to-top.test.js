import { html } from "lit";
import { init } from "../sakai-jump-to-top.js";
import { expect, fixture } from "@open-wc/testing";
import * as data from "./data.js";
import * as sinon from "sinon";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-jump-to-top tests", () => {

  window.top.portal = { locale: "en_GB", siteId: data.siteId };

  fetchMock.get(data.i18nUrl, data.i18n);

  it ("renders correctly with i18n", async () => {

    const el = await fixture(html`<div class="portal-main-container"></div>`);

    await init();

    const scrollToSpy = sinon.spy(el.scrollTo);

    expect(el.querySelector("div button")).to.exist;
    el.querySelector("div button").click();

    scrollToSpy.withArgs({ top: 0, behavior: "smooth" });
  });
});
