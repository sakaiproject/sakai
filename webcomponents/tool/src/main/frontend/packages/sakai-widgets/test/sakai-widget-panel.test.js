import "../sakai-widget-panel.js";
import * as data from "./data.js";
import * as gradesData from "../../sakai-grades/test/data.js";
import * as announcementsData from "../../sakai-announcements/test/data.js";
import * as forumsData from "../../sakai-forums/test/data.js";
import * as sitePickerData from "../../sakai-site-picker/test/data.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";
describe("sakai-widget-panel tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.dashboardWidgetI18nUrl, data.dashboardWidgetI18n)
      .get(data.toolnamesI18nUrl, data.toolnamesI18n)
      .get(data.widgetpickerI18nUrl, data.widgetpickerI18n)
      .get(gradesData.i18nUrl, gradesData.i18n)
      .get(gradesData.gradesUrl, { grades: gradesData.grades, sites: sitePickerData.sites })
      .get(announcementsData.i18nUrl, announcementsData.i18n)
      .get(announcementsData.announcementsUrl, { announcements: announcementsData.announcements, sites: sitePickerData.sites })
      .get(forumsData.i18nUrl, forumsData.i18n)
      .get(sitePickerData.i18nUrl, sitePickerData.i18n)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  window.top.portal = { locale: "en_GB" };

  it ("renders in user mode correctly", async () => {

    // In user mode, we'd expect to get announcements from multiple sites.
    let el = await fixture(html`
      <sakai-widget-panel user-id="${data.userId}" .widgetIds=${data.widgetIds} .layout=${data.layout1}></sakai-widget-panel>
    `);

    await elementUpdated(el);

    const a11yIgnored = [ "landmark-unique", "color-contrast", "select-name", "label", "button-name" ];
    await expect(el).to.be.accessible({ ignoredRules: a11yIgnored });

    expect(el.shadowRoot.getElementById("grid")).to.exist;

    data.layout1.forEach(tile => expect(el.shadowRoot.querySelector(`sakai-${tile.id}-widget`)?.shadowRoot).to.exist);

    const layoutIds = data.layout1.map(tile => tile.id);
    data.widgetIds.filter(id => !layoutIds.includes(id))
      .forEach(id => expect(el.shadowRoot.querySelector(`sakai-${id}-widget`)).to.not.exist);

    el.editing = true;
    await elementUpdated(el);
    await expect(el).to.be.accessible({ ignoredRules: a11yIgnored });

    const available = el.renderRoot.querySelector("#available-widgets");
    expect(available).to.exist;
    const addButton = available.querySelector("button");
    expect(addButton).to.exist;
    const beforeCount = el.layout.length;
    addButton.click();
    await el.updateComplete;
    expect(el.layout.length).to.equal(beforeCount + 1);

    let forums = el.shadowRoot.querySelector("#grid sakai-forums-widget");
    expect(forums).to.exist;
    forums.dispatchEvent(new CustomEvent("move", { detail: { widgetId: "forums", direction: "left" }, bubbles: true }));
    await el.updateComplete;
    forums = el.shadowRoot.querySelector("#grid sakai-forums-widget");
    expect(forums).to.exist;
    forums.dispatchEvent(new CustomEvent("move", { detail: { widgetId: "forums", direction: "right" }, bubbles: true }));
    await el.updateComplete;
    forums = el.shadowRoot.querySelector("#grid sakai-forums-widget");
    expect(forums).to.exist;

    forums.dispatchEvent(new CustomEvent("remove"));
    await el.updateComplete;
    await expect(el).to.be.accessible({ ignoredRules: a11yIgnored });
    expect(el.shadowRoot.querySelector("#grid sakai-forums-widget")).to.not.exist;
  });
});
