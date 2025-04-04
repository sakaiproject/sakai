import "../sakai-picture-changer.js";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, html } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-picture-changer tests", () => {

  window.top.portal = { locale: "en_GB" };

  fetchMock
    .get(data.i18nUrl, data.i18n, {overwriteRoutes: true})
    .get(/\/direct\/profile-image\/details/, { status: "SUCCESS", url: "/packages/sakai-picture-changer/test/images/orville.jpeg" }, { overwriteRoutes: true })
    .get("*", 500, {overwriteRoutes: true});

  it ("renders correctly", async () => {

    let el = await fixture(html`
      <sakai-picture-changer></sakai-picture-changer>
    `);

    await elementUpdated(el);

    expect(document.getElementById("image")).to.not.exist;
  });
});
