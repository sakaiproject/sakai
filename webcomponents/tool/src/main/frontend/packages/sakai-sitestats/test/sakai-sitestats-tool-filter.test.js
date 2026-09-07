import "../sakai-sitestats-tool-filter.js";
import * as i18n from "./i18n.js";
import { elementUpdated, expect, fixture, html } from "@open-wc/testing";
import fetchMock from "fetch-mock";

class EndpointStub extends HTMLElement {

  static get observedAttributes() {
    return [ "endpoint" ];
  }

  get endpoint() {
    return this.getAttribute("endpoint");
  }

  set endpoint(value) {
    this.setAttribute("endpoint", value);
  }
}

if (!customElements.get("sakai-sitestats-widget-metrics")) {
  customElements.define("sakai-sitestats-widget-metrics", class extends EndpointStub {});
}
if (!customElements.get("sakai-sitestats-highlights")) {
  customElements.define("sakai-sitestats-highlights", class extends EndpointStub {});
}
if (!customElements.get("sakai-sitestats-widget-tab")) {
  customElements.define("sakai-sitestats-widget-tab", class extends EndpointStub {});
}

describe("sakai-sitestats-tool-filter tests", () => {

  const metricsEndpoint = "/api/sites/site1/sitestats/widgets/submissions/metrics";
  const highlightsEndpoint = "/api/sites/site1/sitestats/widgets/submissions/highlights";
  const tabEndpoint = "/api/sites/site1/sitestats/widgets/submissions/tabs/byuser?include=table,chart&page=1";

  beforeEach(() => {
    window.sessionStorage.clear();
    window.sakai = undefined;
    fetchMock.mockGlobal();
    fetchMock.get(i18n.i18nUrl, i18n.i18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  const widget = () => html`
    <section class="sitestats-widget"
             data-metrics-endpoint=${metricsEndpoint}
             data-highlights-endpoint=${highlightsEndpoint}>
      <sakai-sitestats-tool-filter>
        <button type="button" data-tool-id="sakai.assignment.grades" aria-pressed="true">Assignments</button>
        <button type="button" data-tool-id="sakai.samigo" aria-pressed="true">Tests &amp; Quizzes</button>
      </sakai-sitestats-tool-filter>
      <sakai-sitestats-widget-metrics endpoint=${metricsEndpoint}></sakai-sitestats-widget-metrics>
      <sakai-sitestats-highlights></sakai-sitestats-highlights>
      <sakai-sitestats-widget-tab endpoint=${tabEndpoint}></sakai-sitestats-widget-tab>
    </section>
  `;

  it("keeps every tool selected by default and recalculates when a chip is toggled", async () => {

    const el = await fixture(widget());
    const filter = el.querySelector("sakai-sitestats-tool-filter");
    const quiz = filter.querySelector("[data-tool-id='sakai.samigo']");

    expect(filter.querySelector("[data-tool-id='sakai.assignment.grades']").getAttribute("aria-pressed")).to.equal("true");
    expect(quiz.getAttribute("aria-pressed")).to.equal("true");

    quiz.click();
    await elementUpdated(filter);

    expect(quiz.getAttribute("aria-pressed")).to.equal("false");
    expect(quiz.classList.contains("btn-primary")).to.be.false;
    expect(quiz.classList.contains("btn-outline-secondary")).to.be.false;
    expect(el.querySelector("sakai-sitestats-widget-metrics").endpoint)
      .to.equal(`${metricsEndpoint}?itemType=sakai.assignment.grades`);
    expect(el.querySelector("sakai-sitestats-highlights").endpoint)
      .to.equal(`${highlightsEndpoint}?itemType=sakai.assignment.grades`);
    expect(el.querySelector("sakai-sitestats-widget-tab").endpoint)
      .to.equal(`${tabEndpoint}&itemType=sakai.assignment.grades`);
  });

  it("does not allow the last selected tool to be cleared", async () => {

    const el = await fixture(widget());
    const filter = el.querySelector("sakai-sitestats-tool-filter");
    const assignment = filter.querySelector("[data-tool-id='sakai.assignment.grades']");
    const quiz = filter.querySelector("[data-tool-id='sakai.samigo']");

    quiz.click();
    await elementUpdated(filter);
    assignment.click();
    await elementUpdated(filter);

    expect(assignment.getAttribute("aria-pressed")).to.equal("true");
    expect(quiz.getAttribute("aria-pressed")).to.equal("false");
    expect(assignment.hasAttribute("aria-disabled")).to.be.true;
    expect(el.querySelector("sakai-sitestats-widget-metrics").endpoint)
      .to.equal(`${metricsEndpoint}?itemType=sakai.assignment.grades`);
  });

  it("restores the unfiltered endpoints when every tool is selected again", async () => {

    const el = await fixture(widget());
    const filter = el.querySelector("sakai-sitestats-tool-filter");
    const quiz = filter.querySelector("[data-tool-id='sakai.samigo']");

    quiz.click();
    await elementUpdated(filter);
    quiz.click();
    await elementUpdated(filter);

    expect(quiz.getAttribute("aria-pressed")).to.equal("true");
    expect(el.querySelector("sakai-sitestats-widget-metrics").endpoint).to.equal(metricsEndpoint);
    expect(el.querySelector("sakai-sitestats-highlights").endpoint).to.equal(highlightsEndpoint);
    expect(el.querySelector("sakai-sitestats-widget-tab").endpoint).to.equal(tabEndpoint);
  });
});
