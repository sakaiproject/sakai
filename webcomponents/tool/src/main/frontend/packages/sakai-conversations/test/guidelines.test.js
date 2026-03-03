import { expect } from "@open-wc/testing";
import { elementUpdated, fixture, html, waitUntil } from "@open-wc/testing";
import "../sakai-conversations-guidelines.js";
import  * as data from "./data.js";
import fetchMock from "fetch-mock";

describe("sakai-conversations-guidelines tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock.get(data.i18nUrl, data.i18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });


  it("renders with default guidelines", async () => {

    const el = await fixture(html`<sakai-conversations-guidelines></sakai-conversations-guidelines>`);

    await waitUntil(() => el._i18n);

    expect(el.querySelector("h1").innerHTML).to.contain(el._i18n.community_guidelines);
    expect(el.querySelector(".sak-banner-info").innerHTML).to.contain(el._i18n.community_guidelines_instruction);
    expect(el.querySelector("#conv-guidelines").innerHTML).to.contain(el._i18n.no_guidelines_yet);
  });

  it("renders with custom guidelines", async () => {

    const customGuidelines = "<p>These are custom guidelines</p>";
    const el = await fixture(html`
      <sakai-conversations-guidelines .guidelines=${customGuidelines}>
      </sakai-conversations-guidelines>
    `);

    // Wait for i18n to load
    await waitUntil(() => el._i18n);

    expect(el.querySelector("h1").innerHTML).to.contain(el._i18n.community_guidelines);
    expect(el.querySelector(".sak-banner-info").innerHTML).to.contain(el._i18n.community_guidelines_instruction);
    expect(el.querySelector("#conv-guidelines").innerHTML).to.contain(customGuidelines);
  });

  it("updates when guidelines property changes", async () => {

    const el = await fixture(html`
      <sakai-conversations-guidelines>
      </sakai-conversations-guidelines>
    `);

    // Wait for i18n to load
    await waitUntil(() => el._i18n);

    expect(el.querySelector("#conv-guidelines").innerHTML).to.contain(el._i18n.no_guidelines_yet);

    const updatedGuidelines = "<p>Updated guidelines</p>";
    el.guidelines = updatedGuidelines;
    await elementUpdated(el);

    expect(el.querySelector("#conv-guidelines").innerHTML).to.contain(updatedGuidelines);
  });
});
