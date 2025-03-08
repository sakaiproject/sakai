import "../sakai-rubrics-manager.js";
import { html } from "lit";
import * as data from "./data.js";
import { expect, fixture,  waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

window.top.portal = { locale: "en_GB" };

fetchMock
  .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
  .get(data.sharedRubricsUrl, data.sharedRubrics, { overwriteRoutes: true })
  .get(data.rubricsUrl, data.rubrics, { overwriteRoutes: true })
  .get("*", 500, { overwriteRoutes: true });

describe("sakai-rubric-manager tests", () => {

  it ("manager renders correctly", async () => {

    let el = await fixture(html`
      <sakai-rubrics-manager site-id="${data.siteId}"></sakai-rubrics-manager>
    `);

    await waitUntil(() => el._i18n);

    expect(el.querySelector("h1:first-child")).to.exist;
    expect(el.querySelector("h1:first-child").innerHTML).to.contain(el._i18n.manage_rubrics);
  });
});
