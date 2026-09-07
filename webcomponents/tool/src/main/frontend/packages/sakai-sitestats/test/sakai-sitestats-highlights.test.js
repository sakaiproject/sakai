import "../sakai-sitestats-highlights.js";
import { elementUpdated, expect, fixture, html } from "@open-wc/testing";

describe("sakai-sitestats-highlights tests", () => {

  const funnelChart = {
    title: "Grading funnel",
    type: "bar",
    compact: true,
    datasets: [
      {
        key: "funnel",
        label: "Grading funnel",
        points: [
          { x: "Enrolled", label: "Enrolled", y: 2 },
          { x: "With grades", label: "With grades", y: 1 },
        ],
      },
    ],
  };

  it("hides itself when there are no chart values", async () => {

    const el = await fixture(html`<sakai-sitestats-highlights></sakai-sitestats-highlights>`);
    await elementUpdated(el);

    expect(el.hidden).to.be.true;
    expect(el.shadowRoot.querySelector("sakai-sitestats-chart")).to.not.exist;
  });

  it("hides itself when every chart value is zero", async () => {

    const emptyChart = {
      ...funnelChart,
      datasets: [ {
        ...funnelChart.datasets[0],
        points: [
          { x: "Enrolled", label: "Enrolled", y: 0 },
          { x: "With grades", label: "With grades", y: 0 },
        ],
      } ],
    };
    const el = await fixture(html`<sakai-sitestats-highlights .charts=${[ emptyChart ]}></sakai-sitestats-highlights>`);
    await elementUpdated(el);

    expect(el.hidden).to.be.true;
    expect(el.shadowRoot.querySelector("sakai-sitestats-chart")).to.not.exist;
  });

  it("shows compact charts when they have values", async () => {

    const el = await fixture(html`<sakai-sitestats-highlights .charts=${[ funnelChart ]}></sakai-sitestats-highlights>`);
    await elementUpdated(el);

    expect(el.hidden).to.be.false;
    expect(el.shadowRoot.querySelector("sakai-sitestats-chart")).to.exist;
  });
});
