import '../sakai-group-picker.js';
import { expect, fixture, oneEvent, waitUntil } from '@open-wc/testing';
import { html } from "lit";
import * as data from "./data.js";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-group-picker tests", () => {

  window.top.portal = { locale: 'en_GB' };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get(data.groupsUrl, data.groups, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders with site and group id", async () => {

    const el = await fixture(html`<sakai-group-picker site-id="${data.siteId}"></sakai-group-picker>`);

    await waitUntil(() => el._i18n);

    const select = el.querySelector("select");
    expect(select).to.exist;
    expect(select.getAttribute("aria-label")).to.equal(data.label);
    expect(select.children.length).to.equal(4);
    const footballOption = select.querySelector(`option[value='${data.footballRef}']`);
    expect(footballOption).to.exist;
    const tennisOption = select.querySelector(`option[value='${data.tennisRef}']`);
    expect(tennisOption).to.exist;

    const listener = oneEvent(el, "groups-selected");

    footballOption.selected = true;
    select.dispatchEvent(new Event("change"));
    let { detail } = await listener;
    expect(detail.value[0]).to.equal(data.footballRef);
  });

  it ("renders with multiple", async () => {

    const el = await fixture(html`<sakai-group-picker site-id="${data.siteId}" multiple></sakai-group-picker>`);

    await waitUntil(() => el._i18n && el.groups);

    const select = el.querySelector("select");
    expect(select).to.exist;
    expect(select.getAttribute("aria-label")).to.equal(data.label);
    expect(select.getAttribute("multiple")).to.exist;
    expect(select.children.length).to.equal(4);
    const footballOption = select.querySelector(`option[value='${data.footballRef}']`);
    expect(footballOption).to.exist;
    const tennisOption = select.querySelector(`option[value='${data.tennisRef}']`);
    expect(tennisOption).to.exist;

    const listener = oneEvent(el, "groups-selected");
    footballOption.selected = true;
    tennisOption.selected = true;
    select.dispatchEvent(new Event("change"));
    let { detail } = await listener;
    expect(detail.value[0]).to.equal(data.tennisRef);
    expect(detail.value[1]).to.equal(data.footballRef);
  });

  it ("renders with groups", async () => {

    const el = await fixture(html`<sakai-group-picker .groups=${data.groups} site-id="${data.siteId}"></sakai-group-picker>`);

    await waitUntil(() => el._i18n && el.groups);

    const select = el.querySelector("select");
    expect(select).to.exist;
    expect(select.querySelectorAll("option").length).to.equal(data.groups.length + 1);

    const footballOption = select.querySelector(`option[value='${data.footballRef}']`);
    expect(footballOption).to.exist;
    const tennisOption = select.querySelector(`option[value='${data.tennisRef}']`);
    expect(tennisOption).to.exist;

    const listener = oneEvent(el, "groups-selected");

    footballOption.selected = true;
    select.dispatchEvent(new Event("change"));
    let { detail } = await listener;
    expect(detail.value[0]).to.equal(data.footballRef);
  });

  it ("renders with pre-selected groupRef", async () => {

    const el = await fixture(html`<sakai-group-picker site-id="${data.siteId}" group-ref="${data.footballRef}"></sakai-group-picker>`);

    await waitUntil(() => el._i18n && el.groups);

    const select = el.querySelector("select");
    expect(select).to.exist;
    expect(select.querySelectorAll("option").length).to.equal(4);

    const footballOption = select.querySelector(`option[value='${data.footballRef}']`);
    expect(footballOption).to.exist;
    expect(footballOption.selected).to.be.true;
  });

  it ("allows us to set multiple selections from an attribute", async () => {

    const el = await fixture(html`<sakai-group-picker .groups=${data.groups} .selectedGroups=${data.selectedGroups} site-id="${data.siteId}" multiple></sakai-group-picker>`);

    await waitUntil(() => el._i18n && el.groups);

    const select = el.querySelector("select");
    expect(select).to.exist;

    expect(select.selectedOptions.length).to.equal(data.selectedGroups.length);
  });

  it ("is accessible", async () => {

    const el = await fixture(html`<sakai-group-picker site-id="${data.siteId}" group-ref="${data.tennisRef}"></sakai-group-picker>`);

    await Promise.all([waitUntil(() => el._i18n), expect(el).to.be.accessible()]);
  });
});
