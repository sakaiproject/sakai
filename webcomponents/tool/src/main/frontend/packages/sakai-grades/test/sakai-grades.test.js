import "../sakai-grades.js";
import { html } from "lit";
import * as data from "./data.js";
import { expect, fixture, waitUntil, aTimeout } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

import { ASSIGNMENT_A_TO_Z, ASSIGNMENT_Z_TO_A, COURSE_A_TO_Z
  , COURSE_Z_TO_A, NEW_HIGH_TO_LOW, NEW_LOW_TO_HIGH
  , AVG_LOW_TO_HIGH, AVG_HIGH_TO_LOW } from "../src/sakai-grades-constants.js";

describe("sakai-grades tests", () => {

  window.top.portal = { locale: "en_GB" };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get(data.gradesUrl, data.grades, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders in user mode correctly", async () => {

    // In user mode, we'd expect to get announcements from multiple sites.
    let el = await fixture(html`
      <sakai-grades user-id="${data.userId}"></sakai-grades>
    `);

    await waitUntil(() => el.dataPage);

    const gradesEl = el.shadowRoot.getElementById("grades");
    expect(gradesEl).to.exist;

    expect(gradesEl.children.length).to.equal(12);

    const filterSelect = el.shadowRoot.querySelector("#filter select");
    expect(filterSelect).to.exist;

    filterSelect.value = NEW_LOW_TO_HIGH;
    filterSelect.dispatchEvent(new Event("change"));
    await el.updateComplete;
    expect(el.shadowRoot.querySelector(".new-count")).to.exist;
    expect(el.shadowRoot.querySelector(".new-count").innerHTML).to.contain(3);
    let all = el.shadowRoot.querySelectorAll(".new-count");
    expect(all.item(all.length - 1).innerHTML).to.contain(8);

    filterSelect.value = NEW_HIGH_TO_LOW;
    filterSelect.dispatchEvent(new Event("change"));
    await el.updateComplete;
    expect(el.shadowRoot.querySelector(".new-count").innerHTML).to.contain(8);
    all = el.shadowRoot.querySelectorAll(".new-count");
    expect(all.item(all.length - 1).innerHTML).to.contain(3);

    filterSelect.value = AVG_LOW_TO_HIGH;
    filterSelect.dispatchEvent(new Event("change"));
    await el.updateComplete;
    expect(el.shadowRoot.querySelector(".average").innerHTML).to.contain(12.40);
    all = el.shadowRoot.querySelectorAll(".average");
    expect(all.item(all.length - 1).innerHTML).to.contain(47.30);

    filterSelect.value = AVG_HIGH_TO_LOW;
    filterSelect.dispatchEvent(new Event("change"));
    await el.updateComplete;
    expect(el.shadowRoot.querySelector(".average").innerHTML).to.contain(47.30);
    all = el.shadowRoot.querySelectorAll(".average");
    expect(all.item(all.length - 1).innerHTML).to.contain(12.40);

    filterSelect.value = ASSIGNMENT_A_TO_Z;
    filterSelect.dispatchEvent(new Event("change"));
    await el.updateComplete;
    expect(el.shadowRoot.querySelector(".title").innerHTML).to.contain("Bayes");
    all = el.shadowRoot.querySelectorAll(".title");
    expect(all.item(all.length - 1).innerHTML).to.contain("Infin");

    filterSelect.value = ASSIGNMENT_Z_TO_A;
    filterSelect.dispatchEvent(new Event("change"));
    await el.updateComplete;
    expect(el.shadowRoot.querySelector(".title").innerHTML).to.contain("Infin");
    all = el.shadowRoot.querySelectorAll(".title");
    expect(all.item(all.length - 1).innerHTML).to.contain("Bayes");

    filterSelect.value = COURSE_A_TO_Z;
    filterSelect.dispatchEvent(new Event("change"));
    await el.updateComplete;
    expect(el.shadowRoot.querySelector(".course").innerHTML).to.contain("French");
    all = el.shadowRoot.querySelectorAll(".course");
    expect(all.item(all.length - 1).innerHTML).to.contain("Prob");

    filterSelect.value = COURSE_Z_TO_A;
    filterSelect.dispatchEvent(new Event("change"));
    await el.updateComplete;
    expect(el.shadowRoot.querySelector(".course").innerHTML).to.contain("Prob");
    all = el.shadowRoot.querySelectorAll(".course");
    expect(all.item(all.length - 1).innerHTML).to.contain("French");
  });

  it ("is accessible", async () => {

    let el = await fixture(html`
      <sakai-grades user-id="${data.userId}"></sakai-grades>
    `);

    await waitUntil(() => el.dataPage);

    expect(el.shadowRoot).to.be.accessible();
  });
});
