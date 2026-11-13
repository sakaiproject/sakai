import { sendKeys } from "@web/test-runner-commands";
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

    el.filtersOpen = true;
    await elementUpdated(el);

    const filterSelect = el.shadowRoot.querySelector(".dashboard-filter-panel select");
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
    expect(el.shadowRoot.querySelector(".ungraded-count").innerHTML).to.contain(8);
    all = el.shadowRoot.querySelectorAll(".ungraded-count");
    expect(all.item(all.length - 1).innerHTML).to.contain(3);

    filterSelect.value = SCORE_LOW_TO_HIGH;
    filterSelect.dispatchEvent(new Event("change"));

    await elementUpdated(el);
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

  for (const siteId of [ undefined, "course1" ]) {
    it(`toggles scores repeatedly in ${siteId ? "site" : "home"} mode`, async () => {

      fetchMock.get(siteId ? `/api/sites/${siteId}/grades` : data.gradesUrl,
        { grades: data.grades, sites: sitePickerData.sites });
      const el = await fixture(html`
        <sakai-grades user-id="${data.userId}" .siteId=${siteId} secret></sakai-grades>
      `);
      await waitUntil(() => el.renderRoot.querySelector(".score-toggle"));

      const button = el.renderRoot.querySelector(".score-toggle");
      const score = button.querySelector("span");
      const secondButton = el.renderRoot.querySelectorAll(".score-toggle")[1];
      const secondScore = secondButton.querySelector("span");
      expect(button.getAttribute("aria-label")).to.contain("Fast Fourier Transform");
      if (!siteId) {
        expect(button.getAttribute("aria-label")).to.contain("Math 101");
      }
      const label = button.getAttribute("aria-label");
      expect(button.hasAttribute("aria-describedby")).to.be.false;
      expect(el.renderRoot.querySelector("#score-reveal-instruction").textContent).to.equal("Reveal scores by clicking on them");
      expect(button.getAttribute("aria-pressed")).to.equal("false");
      expect(score.getAttribute("aria-hidden")).to.equal("true");
      expect(getComputedStyle(score).filter).to.equal("blur(3px)");
      await expect(el).to.be.accessible();

      // Clicking the assignment title must not reveal grades.
      el.renderRoot.querySelector(".assignment").click();
      await elementUpdated(el);
      expect(button.getAttribute("aria-pressed")).to.equal("false");

      for (const [ revealed, key ] of [ [ true, null ], [ false, "Enter" ], [ true, "Space" ] ]) {
        button.focus();
        if (key) {
          await sendKeys({ press: key });
        } else {
          button.click();
        }
        await elementUpdated(el);
        expect(button.getAttribute("aria-label")).to.equal(label);
        expect(button.getAttribute("aria-describedby")).to.equal(revealed ? score.id : null);
        expect(button.getAttribute("aria-pressed")).to.equal(String(revealed));
        expect(score.getAttribute("aria-hidden")).to.equal(String(!revealed));
        expect(getComputedStyle(score).filter).to.equal(revealed ? "none" : "blur(3px)");
        expect(secondButton.getAttribute("aria-pressed")).to.equal("false");
        expect(secondScore.getAttribute("aria-hidden")).to.equal("true");
        expect(getComputedStyle(secondScore).filter).to.equal("blur(3px)");
      }
      expect(el.secret).to.be.true;

      el.filtersOpen = true;
      await elementUpdated(el);
      expect(el.renderRoot.querySelector(".dashboard-widget-main").inert).to.be.true;
      el.renderRoot.querySelector(".dashboard-filter-panel button").focus();
      button.focus();
      expect(el.renderRoot.activeElement).to.not.equal(button);

      el.filtersOpen = false;
      await elementUpdated(el);
      button.focus();
      expect(el.renderRoot.activeElement).to.equal(button);
    });
  }

  it("shows scores without blur when secret is false", async () => {

    fetchMock.get(data.gradesUrl, { grades: data.grades, sites: sitePickerData.sites });

    const el = await fixture(html`
      <sakai-grades user-id="${data.userId}" .secret=${false}></sakai-grades>
    `);

    await elementUpdated(el);
    await waitUntil(() => el.dataPage?.length);

    expect(el.shadowRoot.querySelector(".score-msg")).to.not.exist;
    expect(el.shadowRoot.querySelector(".blurred")).to.not.exist;
  });
});
