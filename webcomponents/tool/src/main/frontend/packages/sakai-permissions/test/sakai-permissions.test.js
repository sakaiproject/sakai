import "../sakai-permissions.js";
import { expect, fixture, waitUntil } from "@open-wc/testing";
import { html } from "lit";
import * as data from "./data.js";
import * as groupPickerData from "../../sakai-group-picker/test/data.js";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-permissions tests", () => {

  window.top.portal = { locale: "en_GB", siteId: data.siteId };

  console.log(groupPickerData);

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get(groupPickerData.i18nUrl, groupPickerData.i18n, { overwriteRoutes: true })
    .get(data.toolI18nUrl, data.toolI18n, { overwriteRoutes: true })
    .get(groupPickerData.groupsUrl, groupPickerData.groups, { overwriteRoutes: true })
    .get(data.permsUrl, data.perms, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  /*
  beforeEach(() =>  {


    window.fetch = url => {

      if (url === data.groupPickerI18nUrl) {
        return Promise.resolve({ text: () => Promise.resolve(data.groupPickerI18n)});
      } else if (url === data.permissionsI18nUrl) {
        return Promise.resolve({ text: () => Promise.resolve(data.permissionsI18n)});
      } else if (url === data.toolI18nUrl) {
        return Promise.resolve({ text: () => Promise.resolve(data.toolI18n) });
      } else if (url === data.groupsUrl) {
        return Promise.resolve({ json: () => Promise.resolve(data.groups) });
      } else if (url.startsWith(data.permsUrl)) {
        return Promise.resolve({ json: () => Promise.resolve(data.perms) });
      } else {
        console.error(`Miss on ${url}`);
      }
    };
  });
  */

  it ("renders correctly", async () => {
 
    const el = await fixture(html`<sakai-permissions tool="tool" site-id="${data.siteId}"></sakai-permissions>`);

    await waitUntil(() => el.i18n);

    expect(el.querySelector("button:first-child").innerHTML).to.contain(el.i18n["per.lis.restoredef"]);

    expect(el.querySelectorAll("table tr").length).to.equal(4);
    expect(el.querySelectorAll("table tr:first-child th").length).to.equal(3);
    expect(el.querySelectorAll("table tr:nth-child(2) input:checked").length).to.equal(2);
    el.querySelector("table tr:nth-child(2) button").click();
    expect(el.querySelectorAll("table tr:nth-child(2) input:checked").length).to.equal(0);
    el.querySelector("table tr:nth-child(2) button").click();
    expect(el.querySelectorAll("table tr:nth-child(2) input:checked").length).to.equal(2);
    el.querySelector("table button:first-child").click();
    expect(el.querySelectorAll("table input:checked").length).to.equal(0);
    el.querySelector("table button:first-child").click();
    expect(el.querySelectorAll("table input:checked").length).to.equal(6);

    // Reset the permissions
    el.querySelector("button:first-child").click();
    await el.updateComplete;
    expect(el.querySelectorAll("table input:checked").length).to.equal(4);

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
