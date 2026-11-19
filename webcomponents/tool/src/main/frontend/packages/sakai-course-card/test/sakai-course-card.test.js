import "../sakai-course-card.js";
import { expect, elementUpdated, oneEvent, fixture, html, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import { dialogI18nUrl, dialogI18n } from "@sakai-ui/sakai-dialog/test-utils";
import { imageEditorI18nUrl, imageEditorI18n } from "@sakai-ui/sakai-image-editor/test-utils";
import fetchMock from "fetch-mock";

describe("sakai-course-card tests", () => {

  beforeEach(() => {

    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(dialogI18nUrl, dialogI18n)
      .get(imageEditorI18nUrl, imageEditorI18n)
      .get(data.toolnameMappingsUrl, data.toolnameMappings)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it ("renders correctly", async () => {

    const el = await fixture(html`<sakai-course-card .courseData=${data.course1}></sakai-course-card>`);

    await waitUntil(() => el._i18n);
    await waitUntil(() => el.courseData);

    await expect(el).to.be.accessible();

    expect(el.renderRoot.querySelector("div.info-block")).to.exist;
  });

  it ("hides settings button correctly", async () => {

    const el = await fixture(html`<sakai-course-card .courseData=${data.course1}></sakai-course-card>`);

    await elementUpdated(el);

    expect(el.querySelector("button.settings-button")).to.not.exist;
  });

  it ("shows settings button correctly", async () => {

    const courseData = { ...data.course1, canEdit: true };
    const el = await fixture(html`<sakai-course-card .courseData=${courseData}></sakai-course-card>`);

    await elementUpdated(el);

    const settingsButton = el.renderRoot.querySelector("button.settings-button");
    expect(settingsButton).to.exist;

    expect(settingsButton.getAttribute("title")).to.equal(el._i18n.settings_tooltip);
  });
});
