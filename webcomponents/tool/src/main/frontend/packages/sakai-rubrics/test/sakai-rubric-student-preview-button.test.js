import "../sakai-rubric-student-preview-button.js";
import "../sakai-rubrics-utils.js";
import { html } from "lit";
import * as data from "./data.js";
import { expect, fixture,  waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

window.top.portal = { locale: "en_GB" };

fetchMock
  .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
  .get(data.rubric1Url, data.rubric1, { overwriteRoutes: true })
  .get(data.associationUrl, data.association, { overwriteRoutes: true })
  .get("*", 500, { overwriteRoutes: true });

describe("sakai-rubric-student-preview-button tests", () => {

  it ("rubric student preview button renders correctly", async () => {

    let el = await fixture(html`
      <sakai-rubric-student-preview-button
          site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="${data.entityId}">
      </sakai-rubric-student-preview-button>
    `);

    await waitUntil(() => el._i18n);

    await waitUntil(() => el.querySelector("h3"), "No h3 rendered");

    const button = el.querySelector("button");
    expect(button).to.exist;
    button.click();

    await waitUntil(() => document.getElementById("rubric-preview"), "No lightbox displayed");

    el.setAttribute("display", "span");
    await waitUntil(() => el.querySelector("span"), "No span rendered");
    expect(el.querySelector("button")).to.not.exist;
  });
});
