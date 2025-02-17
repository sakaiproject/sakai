import "../sakai-rubric-pdf.js";
import { html } from "lit";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, oneEvent, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

window.top.portal = { locale: "en_GB" };

fetchMock
  .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
  .get("*", 500, { overwriteRoutes: true });

window.top.portal = { locale: "en_GB" };

describe("sakai-rubric-pdf tests", () => {

  it ("tests that a rubric pdf component does not render if no site-id or rubric-id are supplied", async () => {

    let el = await fixture(html`
      <sakai-rubric-pdf>
      </sakai-rubric-pdf>
    `);

    await elementUpdated(el);

    expect(el.querySelector("a")).to.not.exist;
  });

  it ("tests that a rubric pdf component render the base url if no evaluation attributes are supplied", async () => {

    let el = await fixture(html`
      <sakai-rubric-pdf site-id="${data.siteId}" rubric-id="${data.rubric1.id}">
      </sakai-rubric-pdf>
    `);

    await elementUpdated(el);

    const link = el.querySelector("a");
    expect(link).to.exist;
    const href = `/api/sites/${data.siteId}/rubrics/${data.rubric1.id}/pdf`;
    expect(link.href).to.match(new RegExp(`.*${href}$`));

    expect(el).to.be.accessible();
  });

  it ("tests that a rubric pdf component renders the url when evaluation attributes are supplied", async () => {

    let el = await fixture(html`
      <sakai-rubric-pdf site-id="${data.siteId}"
          rubric-id="${data.rubric1.id}"
          tool-id="${data.toolId}"
          entity-id="${data.entityId}"
          evaluated-item-id="${data.evaluatedItemId}">
      </sakai-rubric-pdf>
    `);

    await elementUpdated(el);

    const link = el.querySelector("a");
    expect(link).to.exist;
    const href = `/api/sites/${data.siteId}/rubrics/${data.rubric1.id}/pdf\\?toolId=${data.toolId}&itemId=${data.entityId}&evaluatedItemId=${data.evaluatedItemId}`;
    expect(link.href).to.match(new RegExp(`.*${href}$`));

    expect(el).to.be.accessible();
  });
});

