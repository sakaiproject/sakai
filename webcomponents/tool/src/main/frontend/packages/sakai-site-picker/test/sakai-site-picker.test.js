import '../sakai-site-picker.js';
import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from '@open-wc/testing';
import { SakaiSitePicker } from "../src/SakaiSitePicker.js"
import * as data from "./data.js";
import fetchMock from "fetch-mock";
describe("sakai-site-picker tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.sitesUrl, {sites: data.sites})
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it ("renders with sites", async () => {

    const el = await fixture(html`<sakai-site-picker .sites=${data.sites}></sakai-site-picker>`);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    const select = el.renderRoot.querySelector("select");
    expect(select).to.exist;
    expect(select.querySelectorAll("option").length).to.equal(data.sites.length + 1);
    expect(select.getAttribute("aria-label")).to.equal(el._i18n.site_selector_label);

    const allOption = select.querySelector(`option[value='${SakaiSitePicker.ALL}']`);
    expect(allOption).to.exist;
    const siteOneOption = select.querySelector(`option[value='${data.siteOneId}']`);
    expect(siteOneOption).to.exist;
    const siteTwoOption = select.querySelector(`option[value='${data.siteTwoId}']`);
    expect(siteTwoOption).to.exist;
    const siteThreeOption = select.querySelector(`option[value='${data.siteThreeId}']`);
    expect(siteThreeOption).to.exist;

    siteTwoOption.selected = true;
    setTimeout(() => select.dispatchEvent(new Event("change")));
    const { detail } = await oneEvent(el, "sites-selected");
    expect(detail.value).to.equal(data.siteTwoId);
  });

  it ("renders with multiple", async () => {

    const el = await fixture(html`<sakai-site-picker .sites=${data.sites} multiple></sakai-site-picker>`);

    await elementUpdated(el);
    await expect(el).to.be.accessible();

    const select = el.renderRoot.querySelector("select");
    expect(select).to.exist;
    expect(select.getAttribute("multiple")).to.exist;
    expect(select.querySelectorAll("option").length).to.equal(data.sites.length + 1);
    const allOption = select.querySelector(`option[value='${SakaiSitePicker.ALL}']`);
    expect(allOption).to.exist;
    const siteOneOption = select.querySelector(`option[value='${data.siteOneId}']`);
    expect(siteOneOption).to.exist;
    const siteTwoOption = select.querySelector(`option[value='${data.siteTwoId}']`);
    expect(siteTwoOption).to.exist;

    allOption.selected = false;
    siteOneOption.selected = true;
    siteTwoOption.selected = true;
    setTimeout(() => select.dispatchEvent(new Event("change")));
    const { detail } = await oneEvent(el, "sites-selected");
    expect(detail.value[0]).to.equal(data.siteOneId);
    expect(detail.value[1]).to.equal(data.siteTwoId);
  });

  it ("renders without sites supplied", async () => {

    const el = await fixture(html`<sakai-site-picker user-id="${data.userId}"></sakai-site-picker>`);

    await waitUntil(() => el._i18n && el.sites);

    const select = el.renderRoot.querySelector("select");
    expect(select).to.exist;
    expect(select.querySelectorAll("option").length).to.equal(data.sites.length + 1);
  });

  it ("renders with pre-selected site-id", async () => {

    const el = await fixture(html`<sakai-site-picker .sites=${data.sites} site-id="${data.siteOneId}"></sakai-site-picker>`);

    await waitUntil(() => el._i18n);

    const select = el.renderRoot.querySelector("select");
    expect(select).to.exist;

    const siteOneOption = select.querySelector(`option[value='${data.siteOneId}']`);
    expect(siteOneOption).to.exist;
    expect(siteOneOption.selected).to.be.true;
  });

  it ("allows us to set multiple selections from an attribute", async () => {

    const el = await fixture(html`
      <sakai-site-picker .sites=${data.sites}
          .selectedSites=${data.selectedSites}
          multiple>
      </sakai-site-picker>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    const select = el.renderRoot.querySelector("select");
    expect(select).to.exist;

    expect(select.selectedOptions.length).to.equal(data.selectedSites.length);
  });
});
