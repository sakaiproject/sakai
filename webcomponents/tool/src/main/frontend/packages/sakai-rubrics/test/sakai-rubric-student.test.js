import "../sakai-rubric-student.js";
import "../sakai-rubrics-utils.js";
import * as data from "./data.js";
import { elementUpdated, expect, html, fixture, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

fetchMock
  .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
  .get(data.rubric1Url, data.rubric1, { overwriteRoutes: true })
  .get(data.associationUrl, data.association, { overwriteRoutes: true })
  .get(data.evaluationUrl, data.evaluation, { overwriteRoutes: true })
  .get("*", 500, { overwriteRoutes: true });

window.sakai = window.sakai || {
  editor: {
    launch: () => ({ focus: () => "", on: () => "" })
  },
};

describe("sakai-rubric-student tests", () => {

  it ("renders a rubric student correctly", async () => {

    let el = await fixture(html`
      <sakai-rubric-student site-id="${data.siteId}"
          tool-id="${data.toolId}"
          entity-id="${data.entityId}"
          evaluated-item-id="${data.evaluatedItemId}"
          evaluated-item-owner-id="${data.evaluatedItemOwnerId}">
      </sakai-rubric-student>
    `);

    await el.updateComplete;

    await waitUntil(() => el.querySelector(".rubric-details"), "No .rubric-details created");
    expect(el.querySelector("sakai-rubric-criterion-preview")).to.not.exist;

    await el.updateComplete;
    expect(el.querySelector("sakai-rubric-criterion-student")).to.exist;

    await expect(el).to.be.accessible();
  });

  it ("rubric student preview renders correctly", async () => {

    let el = await fixture(html`
      <sakai-rubric-student site-id="${data.siteId}" rubric-id="1" preview="true"></sakai-rubric-student>
    `);

    await el.updateComplete;

    await expect(el).to.be.accessible();

    await waitUntil(() => el.querySelector("sakai-rubric-criterion-preview"), "No sakai-rubric-criterion-preview created");
  });
});
