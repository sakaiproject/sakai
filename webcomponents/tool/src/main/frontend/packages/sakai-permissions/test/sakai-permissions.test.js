import "../sakai-permissions.js";
import { expect, fixture, waitUntil } from "@open-wc/testing";
import { html } from "lit";
import * as data from "./data.js";
import * as groupPickerData from "../../sakai-group-picker/test/data.js";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-permissions tests", () => {

  window.top.portal = { locale: "en_GB", siteId: data.siteId };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get(groupPickerData.i18nUrl, groupPickerData.i18n, { overwriteRoutes: true })
    .get(data.toolI18nUrl, data.toolI18n, { overwriteRoutes: true })
    .get(groupPickerData.groupsUrl, groupPickerData.groups, { overwriteRoutes: true })
    .get(data.permsUrl, data.perms, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders correctly", async () => {
 
    const el = await fixture(html`<sakai-permissions tool="tool" site-id="${data.siteId}"></sakai-permissions>`);

    await waitUntil(() => el.i18n);

    expect(el.querySelector("button:first-child").innerHTML).to.contain(el.i18n["per.lis.restoredef"]);

    expect(el.querySelectorAll("#permissions-container .row").length).to.equal(4);
    expect(el.querySelectorAll("#permissions-container .row:first-child div").length).to.equal(3);
    expect(el.querySelectorAll("#permissions-container .row:nth-child(2) input:checked").length).to.equal(2);
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
    expect(el.querySelectorAll("#permissions-container input:checked").length).to.equal(4);

    expect(el.querySelectorAll(".access-checkbox-cell input:checked").length).to.equal(1);
    el.querySelector("button[data-role='access']").click();
    expect(el.querySelectorAll(".access-checkbox-cell input:checked").length).to.equal(0);
    el.querySelector("button[data-role='access']").click();
    expect(el.querySelectorAll(".access-checkbox-cell input:checked").length).to.equal(3);
  });

  it ("is accessible", async () => {

    const el = await fixture(html`<sakai-permissions tool="tool" site-id="${data.siteId}"></sakai-permissions>`);

    await waitUntil(() => el.i18n);

    await expect(el).to.be.accessible();
  });
});
