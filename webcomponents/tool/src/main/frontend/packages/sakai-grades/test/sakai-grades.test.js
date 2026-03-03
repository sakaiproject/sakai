import "../sakai-grades.js";
import * as data from "./data.js";
import * as sitePickerData from "../../sakai-site-picker/test/data.js";
import { elementUpdated, fixture, expect, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";
import { ASSIGNMENT_A_TO_Z, ASSIGNMENT_Z_TO_A, COURSE_A_TO_Z
  , COURSE_Z_TO_A, UNGRADED_MOST_TO_LEAST, UNGRADED_LEAST_TO_MOST
  , SCORE_LOW_TO_HIGH, SCORE_HIGH_TO_LOW } from "../src/sakai-grades-constants.js";
describe("sakai-grades tests", () => {

  beforeEach(async () => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(sitePickerData.i18nUrl, sitePickerData.i18n);
  });

  afterEach(async () => {
    fetchMock.hardReset();
  });

  it ("renders in user mode correctly", async () => {

    fetchMock.get(data.gradesUrl, { grades: data.grades, sites: sitePickerData.sites });

    // In user mode, we'd expect to get announcements from multiple sites.
    let el = await fixture(html`
      <sakai-grades user-id="${data.userId}"></sakai-grades>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    const gradesEl = el.shadowRoot.getElementById("grades");
    expect(gradesEl).to.exist;

    expect(gradesEl.children.length).to.equal(15);

    const filterSelect = el.shadowRoot.querySelector("#filter select");
    expect(filterSelect).to.exist;

    filterSelect.value = UNGRADED_LEAST_TO_MOST;
    filterSelect.dispatchEvent(new Event("change"));
    await el.updateComplete;
    expect(el.shadowRoot.querySelector(".ungraded-count")).to.exist;
    expect(el.shadowRoot.querySelector(".ungraded-count").innerHTML).to.contain(3);
    let all = el.shadowRoot.querySelectorAll(".ungraded-count");
    expect(all.item(all.length - 1).innerHTML).to.contain(8);

    filterSelect.value = UNGRADED_MOST_TO_LEAST;
    filterSelect.dispatchEvent(new Event("change"));
    await elementUpdated(el);
    await expect(el).to.be.accessible();
    expect(el.shadowRoot.querySelector(".ungraded-count").innerHTML).to.contain(8);
    all = el.shadowRoot.querySelectorAll(".ungraded-count");
    expect(all.item(all.length - 1).innerHTML).to.contain(3);

    filterSelect.value = SCORE_LOW_TO_HIGH;
    filterSelect.dispatchEvent(new Event("change"));

    await elementUpdated(el);
    await expect(el).to.be.accessible();
  });

  it ("renders a sakai info banner when there are no grades", async () => {

    fetchMock.get(data.gradesUrl, { grades: [], sites: sitePickerData.sites });

    const el = await fixture(html`
      <sakai-grades user-id="${data.userId}"></sakai-grades>
    `);

    await elementUpdated(el);

    await waitUntil(() => el.dataPage);

    expect(el.shadowRoot.querySelector(".sak-banner-info")).to.exist;
    expect(el.shadowRoot.querySelector(".sak-banner-info").innerHTML).to.contain(el._i18n.no_grades);
  });
});
