import "../sakai-rubric-student-preview-button.js";
import * as data from "./data.js";
import { expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

describe("sakai-rubric-student-preview-button tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.rubric1Url, data.rubric1)
      .get(data.associationUrl, data.association)
      .get("*", 500);
  });

  afterEach(() => {
    document.querySelectorAll("sakai-rubric-modal").forEach(modal => modal.remove());
    fetchMock.hardReset();
  });


  it ("rubric student preview button renders correctly", async () => {

    const el = await fixture(html`
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

    await waitUntil(() => document.querySelector("sakai-rubric-modal")?.shadowRoot?.querySelector("dialog")?.open, "No rubric modal displayed");

    el.setAttribute("display", "span");
    await waitUntil(() => el.querySelector("span"), "No span rendered");
    expect(el.querySelector("button")).to.not.exist;
  });
});
