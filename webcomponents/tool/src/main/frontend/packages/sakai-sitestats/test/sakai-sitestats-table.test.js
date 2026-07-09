import "../sakai-sitestats-table.js";
import * as i18n from "./i18n.js";
import { expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

describe("sakai-sitestats-table tests", () => {

  beforeEach(() => {
    window.sessionStorage.clear();
    window.sakai = undefined;
    fetchMock.mockGlobal();
    fetchMock.get(i18n.i18nUrl, i18n.i18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it("renders table captions visibly by default", async () => {

    const table = {
      caption: "Visits",
      columns: [{ key: "date", label: "Date", type: "date" }],
      rows: [{ cells: { date: { raw: "2026-06-17", display: "6/17/26" } } }],
    };

    const el = await fixture(html`<sakai-sitestats-table .table=${table}></sakai-sitestats-table>`);
    await waitUntil(() => el.shadowRoot.querySelector("caption"));

    const caption = el.shadowRoot.querySelector("caption");
    expect(caption.textContent).to.equal("Visits");
    expect(caption.classList.contains("visually-hidden")).to.be.false;
  });

  it("can keep captions available to assistive technology without rendering them visibly", async () => {

    const table = {
      caption: "Visits",
      columns: [{ key: "date", label: "Date", type: "date" }],
      rows: [{ cells: { date: { raw: "2026-06-17", display: "6/17/26" } } }],
    };

    const el = await fixture(html`
      <sakai-sitestats-table
          .hideCaption=${true}
          .table=${table}>
      </sakai-sitestats-table>
    `);
    await waitUntil(() => el.shadowRoot.querySelector("caption"));

    const caption = el.shadowRoot.querySelector("caption");
    expect(caption.textContent).to.equal("Visits");
    expect(caption.classList.contains("visually-hidden")).to.be.true;
  });
});
