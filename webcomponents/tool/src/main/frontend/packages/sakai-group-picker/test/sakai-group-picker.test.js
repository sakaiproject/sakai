import '../sakai-group-picker.js';
import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from '@open-wc/testing';
import * as data from "./data.js";
import fetchMock from "fetch-mock";
describe("sakai-group-picker tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.groupsUrl, data.groups)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
    sessionStorage.removeItem("user-picker");
  });

  it("filters a bound select and updates only matching choices", async () => {
    sessionStorage.setItem("user-picker", JSON.stringify({
      search: "Search", remove: "Remove {}", clear_selection: "Clear all selections", one_result: "1 result", result_count: "{} results",
      no_options: "No choices", no_results: "No matches", selected_count: "{} selected",
      select_matching: "Select matching groups", clear_matching: "Clear matching groups",
    }));
    const form = await fixture(html`<form>
      <select name="groups" multiple>
        <option value="north" selected>North</option>
        <option value="south" selected>South</option>
        <option value="east" selected>East</option>
        <option value="west" selected>West</option>
        <option value="north-2" disabled>North 2</option>
      </select>
    </form>`);
    const select = form.querySelector("select");
    const picker = document.createElement("sakai-group-picker");
    picker.bindSelect(select);
    await waitUntil(() => select.hidden);
    const root = picker.querySelector("sakai-user-picker").renderRoot;
    expect(root.querySelector(".results").hidden).to.be.true;
    expect(root.querySelector(".selection-summary").textContent).to.equal("4 selected");
    const input = root.querySelector("input");
    input.value = "NORTH";
    input.dispatchEvent(new Event("input"));
    await waitUntil(() => !root.querySelector(".results").hidden);
    expect(root.querySelectorAll("[role=option]")).to.have.length(2);
    const cleared = oneEvent(form, "change");
    root.querySelectorAll(".matching-actions button")[1].click();
    expect((await cleared).target).to.equal(select);
    expect(new FormData(form).getAll("groups")).to.deep.equal([ "south", "east", "west" ]);
    root.querySelectorAll(".matching-actions button")[0].click();
    expect(new FormData(form).getAll("groups")).to.deep.equal([ "north", "south", "east", "west" ]);
    await elementUpdated(picker.querySelector("sakai-user-picker"));
    root.querySelector('[aria-label="Clear all selections"]').click();
    expect(new FormData(form).getAll("groups")).to.deep.equal([]);
  });

  it("keeps a selected disabled group locked in a declaratively bound field", async () => {
    sessionStorage.setItem("user-picker", JSON.stringify({
      search: "Search", remove: "Remove {}", clear_selection: "Clear all selections", one_result: "1 result", result_count: "{} results",
      no_options: "No choices", no_results: "No matches", selected_count: "{} selected",
      select_matching: "Select matching groups", clear_matching: "Clear matching groups",
    }));
    const form = await fixture(html`<form>
      <sakai-group-picker bind-select>
        <select name="groups" multiple>
          <option value="locked" selected disabled>Locked group</option>
          <option value="open" selected>Open group</option>
        </select>
      </sakai-group-picker>
    </form>`);
    const select = form.querySelector("select");
    await waitUntil(() => select.hidden);
    const root = form.querySelector("sakai-user-picker").renderRoot;
    expect(root.querySelector('[aria-label="Remove Locked group"]').disabled).to.be.true;
    root.querySelector('[aria-label="Clear all selections"]').click();
    expect(Array.from(select.selectedOptions, option => option.value)).to.deep.equal([ "locked" ]);
  });

  it ("renders with site and group id", async () => {

    const el = await fixture(html`
      <sakai-group-picker site-id="${data.siteId}">
      </sakai-group-picker>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

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

    await elementUpdated(el);

    await expect(el).to.be.accessible();

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

    await elementUpdated(el);

    await expect(el).to.be.accessible();

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

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    const select = el.querySelector("select");
    expect(select).to.exist;
    expect(select.querySelectorAll("option").length).to.equal(4);

    const footballOption = select.querySelector(`option[value='${data.footballRef}']`);
    expect(footballOption).to.exist;
    expect(footballOption.selected).to.be.true;
  });

  it ("allows us to set multiple selections from an attribute", async () => {

    const el = await fixture(html`<sakai-group-picker .groups=${data.groups} .selectedGroups=${data.selectedGroups} site-id="${data.siteId}" multiple></sakai-group-picker>`);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    const select = el.querySelector("select");
    expect(select).to.exist;

    expect(select.selectedOptions.length).to.equal(data.selectedGroups.length);
  });
});
