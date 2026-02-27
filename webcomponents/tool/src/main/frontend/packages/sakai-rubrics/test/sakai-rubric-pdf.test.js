import "../sakai-rubric-pdf.js";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

describe("sakai-rubric-pdf tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });


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

    await expect(el).to.be.accessible();

    const link = el.querySelector("a");
    expect(link).to.exist;
    const href = `/api/sites/${data.siteId}/rubrics/${data.rubric1.id}/pdf`;
    expect(link.href).to.match(new RegExp(`.*${href}$`));

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

    await expect(el).to.be.accessible();

    const link = el.querySelector("a");
    expect(link).to.exist;
    const href = `/api/sites/${data.siteId}/rubrics/${data.rubric1.id}/pdf\\?toolId=${data.toolId}&itemId=${data.entityId}&evaluatedItemId=${data.evaluatedItemId}`;
    expect(link.href).to.match(new RegExp(`.*${href}$`));

  });
});

