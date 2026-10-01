import "../sakai-timer-bar.js";
import { expect, fixture, html, waitUntil, elementUpdated } from "@open-wc/testing";
import fetchMock from "fetch-mock";
import sinon from "sinon";
import { mockTranslations } from "../../../test/migrated-components-i18n.js";

describe("sakai-timer-bar", () => {
  let clock;
  let messages;
  const onMessage = event => { if (event.data.id === "timer") { messages.push(event.data.msg); } };

  beforeEach(() => {
    fetchMock.mockGlobal();
    mockTranslations(fetchMock);
    clock = sinon.useFakeTimers({ toFake: [ "setInterval", "clearInterval" ] });
    messages = [];
    window.addEventListener("message", onMessage);
  });

  afterEach(() => {
    window.removeEventListener("message", onMessage);
    clock.restore();
    fetchMock.hardReset();
  });

  it("requests a save before expiry and ends exactly once", async () => {
    const el = await fixture(html`<sakai-timer-bar id="timer" time-limit="8"></sakai-timer-bar>`);
    await waitUntil(() => el.renderRoot.querySelector(".time-value"));
    clock.tick(3000);
    await waitUntil(() => messages.length === 1);
    expect(messages).to.deep.equal([ "SAVE" ]);
    clock.tick(5000);
    await waitUntil(() => messages.length === 2);
    expect(messages).to.deep.equal([ "SAVE", "END" ]);
    clock.tick(10000);
    await elementUpdated(el);
    expect(messages).to.have.length(2);
    expect(el.renderRoot.querySelector(".time-value").textContent).to.equal("00:00:00");
  });

  it("preserves server synchronization and stops work when disconnected", async () => {
    fetchMock.get("/timerinfo", { id: "timer", timeElapsed: 98 });
    const el = await fixture(html`<sakai-timer-bar id="timer" time-limit="100" sync-call="/timerinfo"></sakai-timer-bar>`);
    await waitUntil(() => el.renderRoot.querySelector(".time-value"));
    clock.tick(60000);
    await waitUntil(() => messages.includes("SAVE"));
    await elementUpdated(el);
    expect(el.renderRoot.querySelector(".time-value").textContent).to.equal("00:00:02");
    el.remove();
    clock.tick(60000);
    expect(messages).not.to.include("END");
    expect(fetchMock.callHistory.calls("/timerinfo")).to.have.length(1);
  });

  it("can hide, show and dismiss the warning without submitting the host form", async () => {
    const el = await fixture(html`<sakai-timer-bar time-limit="100" time-elapsed="95"></sakai-timer-bar>`);
    await waitUntil(() => el.renderRoot.querySelector("button"));
    el.renderRoot.querySelector(".show-hide").click();
    await elementUpdated(el);
    expect(el.renderRoot.querySelector("#remaining").hidden).to.be.true;
    expect(el.renderRoot.querySelector("[role=alert]")).to.exist;
    el.renderRoot.querySelector(".warning button").click();
    await elementUpdated(el);
    expect(el.renderRoot.querySelector("[role=alert]")).not.to.exist;
    el.renderRoot.querySelector(".show-hide").click();
    await elementUpdated(el);
    expect(el.renderRoot.querySelector("#remaining").hidden).to.be.false;
    await expect(el).to.be.accessible();
  });

  it("keeps elapsed time when its synchronization endpoint changes", async () => {
    const el = await fixture(html`<sakai-timer-bar time-limit="100" sync-call="/first"></sakai-timer-bar>`);
    await waitUntil(() => el.renderRoot.querySelector(".time-value"));
    clock.tick(10000);
    el.setAttribute("sync-call", "/second");
    await elementUpdated(el);
    expect(el.renderRoot.querySelector(".time-value").textContent).to.equal("00:01:30");
  });
});
