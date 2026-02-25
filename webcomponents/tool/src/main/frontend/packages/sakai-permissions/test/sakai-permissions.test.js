import "../sakai-permissions.js";
import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import * as groupPickerData from "../../sakai-group-picker/test/data.js";
import * as sinon from "sinon";
import fetchMock from "fetch-mock";
describe("sakai-permissions tests", () => {

  window.top.portal = { siteId: data.siteId };

  beforeEach(async () => {
    fetchMock.mockGlobal();


    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.toolI18nUrl, data.toolI18n)
      .get(groupPickerData.i18nUrl, groupPickerData.i18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it ("renders correctly", async () => {

    // Encode the ref parameter in the mock URL for the initial load
    fetchMock.get(`/api/sites/${data.siteId}/permissions/tool?ref=${encodeURIComponent(`/site/${data.siteId}`)}`, data.perms);

    const el = await fixture(html`
      <sakai-permissions tool="tool">
      </sakai-permissions>
    `);

    await waitUntil(() => el._i18n);

    await expect(el).to.be.accessible();
    expect(el.querySelector("button:first-child").innerHTML).to.contain(el._i18n["per.lis.restoredef"]);

    const roles = Object.keys(data.perms.on);

    data.perms.available.forEach(perm => {

      roles.forEach(role => {
        expect(el.querySelector(`input[data-perm='${perm}'][data-role='${role}']`)).to.exist;
      });
    });

    Object.entries(data.perms.on).forEach(([role, perms]) => {

      perms.forEach(perm => {
        expect(el.querySelector(`input[data-perm='${perm}'][data-role='${role}']`).checked).to.be.true;
      });
    });

    el.querySelector("#permissions-container .row:nth-child(2) button").click();
    expect(el.querySelectorAll("table tr:nth-child(2) input:checked").length).to.equal(0);

    el.querySelector("#permissions-container .row:nth-child(2) button").click();
    expect(el.querySelectorAll("#permissions-container .row:nth-child(2) input:checked").length).to.equal(2);

    el.querySelector("#permissions-container button:first-child").click();
    expect(el.querySelectorAll("#permissions-container input:checked").length).to.equal(0);

    el.querySelector("#permissions-container button:first-child").click();
    expect(el.querySelectorAll("#permissions-container input:checked").length).to.equal(6);

    // Reset the permissions
    el.querySelector("button:first-child").click();
    await el.updateComplete;

    await expect(el).to.be.accessible();

    expect(el.querySelectorAll("#permissions-container input:checked").length).to.equal(4);

    expect(el.querySelectorAll(".access-checkbox-cell input:checked").length).to.equal(1);
    el.querySelector("button[data-role='access']").click();
    expect(el.querySelectorAll(".access-checkbox-cell input:checked").length).to.equal(0);
    el.querySelector("button[data-role='access']").click();
    expect(el.querySelectorAll(".access-checkbox-cell input:checked").length).to.equal(3);
  });

  it ("tests the _handlePermissionClick method", async () => {

    // Encode the ref parameter in the mock URL for the initial load
    fetchMock.get(`/api/sites/${data.siteId}/permissions/tool?ref=${encodeURIComponent(`/site/${data.siteId}`)}`, data.perms);

    const el = await fixture(html`
      <sakai-permissions tool="tool">
      </sakai-permissions>
    `);

    await waitUntil(() => el._i18n);

    // Wait for the data fetch to complete
    await waitUntil(() => el.roles);

    await elementUpdated(el);

    // First, uncheck all checkboxes
    el.querySelectorAll("#permissions-container input[type='checkbox']").forEach(checkbox => {
      checkbox.checked = false;
    });

    // Verify all checkboxes are unchecked
    expect(el.querySelectorAll("#permissions-container input:checked").length).to.equal(0);

    // Click the permission header button to select all
    expect(el.querySelector("#permission-header button")).to.exist;
    el.querySelector("#permission-header button").click();

    // Verify all checkboxes are now checked
    const totalCheckboxes = Object.keys(data.perms.on).length * data.perms.available.length;
    expect(el.querySelectorAll("#permissions-container input:checked").length).to.equal(totalCheckboxes);

    // Click the permission header button again to deselect all
    el.querySelector("#permission-header button").click();

    // Verify all checkboxes are unchecked again
    expect(el.querySelectorAll("#permissions-container input:checked").length).to.equal(0);
  });

  it ("displays an error banner if the override ref is invalid", async () => {

    const ref = "main_ref";
    const overrideRef = "override_ref";

    // Mock up a 400 (bad request) response
    const url = `/api/sites/${data.siteId}/permissions/tool?ref=${encodeURIComponent(ref)}&overrideRef=${encodeURIComponent(overrideRef)}`;
    fetchMock.get(url, 400);

    const consoleErrorStub = sinon.stub(console, "error");

    const el = await fixture(html`
      <sakai-permissions tool="tool"
          reference="${ref}"
          override-reference="${overrideRef}">
      </sakai-permissions>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(consoleErrorStub.calledOnce).to.be.true;

    consoleErrorStub.restore();

    expect(el.querySelector(".sak-banner-error")).to.exist;
  });

  it ("disables overridden functions", async () => {

    const ref = "main_ref";
    const overrideRef = "override_ref";
    const overriddenPerms = { ...data.perms, locked: { maintain: [ "tool.create", "tool.delete" ] } };

    const url = `/api/sites/${data.siteId}/permissions/tool?ref=${encodeURIComponent(ref)}&overrideRef=${encodeURIComponent(overrideRef)}`;
    fetchMock.get(url, overriddenPerms);

    const el = await fixture(html`
      <sakai-permissions tool="tool"
          reference="${ref}"
          override-reference="${overrideRef}">
      </sakai-permissions>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector("input[data-perm='tool.create'][data-role='maintain']").disabled).to.be.true;
    expect(el.querySelector("input[data-perm='tool.delete'][data-role='maintain']").disabled).to.be.true;
  });

  it ("saves permissions correctly", async () => {

    const unsetPerms = { ...data.perms, on: { "maintain": [], "access": [] } };

    // Encode the ref parameter in the mock URL for the initial load
    fetchMock.get(`/api/sites/${data.siteId}/permissions/tool?ref=${encodeURIComponent(`/site/${data.siteId}`)}`, unsetPerms);

    // Mock the POST request for saving permissions
    const saveUrl = `/api/sites/${data.siteId}/permissions`;
    fetchMock.post(saveUrl, 200);

    const el = await fixture(html`
      <sakai-permissions tool="tool"
        fire-event>
      </sakai-permissions>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    // Wait for the data fetch to complete
    await waitUntil(() => el.roles);

    // Turn on everything for the maintain role
    el.querySelector("input[data-perm='tool.create'][data-role='maintain']").checked = true;
    el.querySelector("input[data-perm='tool.read'][data-role='maintain']").checked = true;
    el.querySelector("input[data-perm='tool.delete'][data-role='maintain']").checked = true;

    // Turn on tool.read for the access role
    el.querySelector("input[data-perm='tool.read'][data-role='access']").checked = true;

    // Click the save button
    const saveButton = el.querySelector(".act input[type='button'].active");
    expect(saveButton).to.exist;
    saveButton.click();

    await oneEvent(el, "permissions-complete");

    // Verify the POST request was made with the correct parameters
    expect(fetchMock.callHistory.called(saveUrl)).to.be.true;
    const params = fetchMock.callHistory.lastCall(saveUrl).options.body;
    expect(params.get("ref")).to.equal(`/site/${data.siteId}`);
    expect(params.get("maintain:tool.create")).to.equal("true");
    expect(params.get("maintain:tool.read")).to.equal("true");
    expect(params.get("maintain:tool.delete")).to.equal("true");
    expect(params.get("access:tool.read")).to.equal("true");
    expect(params.get("access:tool.create")).to.equal("false");
    expect(params.get("access:tool.delete")).to.equal("false");
  });

  it ("loads group permissions correctly", async () => {

    const initialRef = `/site/${data.siteId}`;
    // Encode the initial ref parameter in the mock URL
    fetchMock.get(`/api/sites/${data.siteId}/permissions/tool?ref=${encodeURIComponent(initialRef)}`, data.perms);

    const el = await fixture(html`
      <sakai-permissions tool="tool"
        reference="${initialRef}" // Explicitly set initial reference
        enable-groups
        fire-event>
      </sakai-permissions>
    `);

    await waitUntil(() => el._i18n);

    await waitUntil(() => el.groups);

    await elementUpdated(el);

    const groupPicker = el.querySelector("sakai-group-picker");
    expect(groupPicker).to.exist;

    const groupPerms = {
      available: [ "tool.read", "tool.create", "tool.delete" ],
      on: {
        "maintain": [],
        "access": [ "tool.read", "tool.create", "tool.delete" ],
      },
      roleNameMappings: { maintain: "Maintain", access: "Access" },
      groups : data.groups,
    };

    fetchMock.get(`/api/sites/${data.siteId}/permissions/tool?ref=${encodeURIComponent(data.groups[0].reference)}`, groupPerms);

    groupPicker.dispatchEvent(new CustomEvent("groups-selected", { detail: { value: data.groups[0].reference }, bubbles: true }));

    expect(el.reference).to.equal(data.groups[0].reference);

    await waitUntil(() => el.on.maintain.length === 0);

    await elementUpdated(el);

    Object.entries(groupPerms.on).forEach(([role, perms]) => {

      perms.forEach(perm => {
        expect(el.querySelector(`input[data-perm='${perm}'][data-role='${role}']`).checked).to.be.true;
      });
    });
  });

  it ("handles disable-groups correctly", async () => {

    // Replace the incorrect mock with the proper URL pattern
    fetchMock.get(`/api/sites/${data.siteId}/permissions/tool?ref=${encodeURIComponent(`/site/${data.siteId}`)}`, data.perms);

    const el = await fixture(html`
      <sakai-permissions tool="tool"
        disable-groups
        fire-event>
      </sakai-permissions>
    `);

    await waitUntil(() => el._i18n);

    await elementUpdated(el);

    expect(el.groups).to.be.undefined;

    expect(el.querySelector("sakai-group-picker")).to.not.exist;
  });

});
