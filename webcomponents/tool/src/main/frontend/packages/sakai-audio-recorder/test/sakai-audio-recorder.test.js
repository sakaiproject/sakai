import "../sakai-audio-recorder.js";
import * as data from "./data.js";
import { aTimeout, waitUntil, elementUpdated, expect, fixture, html } from "@open-wc/testing";
import fetchMock from "fetch-mock";
describe("sakai-audio-recorder tests", () => {

  beforeEach(async () => {
    fetchMock.mockGlobal();
    fetchMock.get(data.i18nUrl, data.i18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it ("starts recording", async () => {

    const el = await fixture(html`
      <sakai-audio-recorder></sakai-audio-recorder>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    const startButton = el.renderRoot.querySelector("#start-recording-button");
    expect(startButton.disabled).to.be.false;
    expect(el.renderRoot.querySelector("#stop-recording-button")).to.be.null;
  });
});

