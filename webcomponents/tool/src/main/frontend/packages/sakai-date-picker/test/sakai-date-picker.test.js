import "../sakai-date-picker.js";
import { expect, fixture, waitUntil } from "@open-wc/testing";
import { html } from "lit";
import { stub } from "sinon";

describe("sakai-date-picker tests", () => {

  window.top.portal = { user: { offsetFromServerMillis: 0, timezone: "Europe/London" } };

  it ("renders correctly", async () => {
 
    const el = await fixture(html`<sakai-date-picker epoch-millis="1674026961000"></sakai-date-picker>`);

    expect(el.querySelector("input[type='datetime-local']")).to.exist;
  });

  it ("disables correctly", async () => {
 
    const el = await fixture(html`<sakai-date-picker></sakai-date-picker>`);

    el.disable();

    await el.updateComplete;
    expect(el.querySelector("input[type='datetime-local']").disabled).to.be.true;
  });

  it ("adds hidden fields correctly", async () => {
 
    const el = await fixture(html`<sakai-date-picker iso-date="2019-09-07T15:50" add-hidden-fields hidden-prefix="test-"></sakai-date-picker>`);
    expect(el.querySelectorAll("input[type='hidden']").length).to.equal(5);
    expect(el.querySelector("input[name='test-year']").value).to.equal("2019");
    expect(el.querySelector("input[name='test-month']").value).to.equal("9");
    expect(el.querySelector("input[name='test-day']").value).to.equal("7");
    expect(el.querySelector("input[name='test-hour']").value).to.equal("15");
    expect(el.querySelector("input[name='test-min']").value).to.equal("50");
  });

  it ("updates hidden fields and isoDate correctly", async () => {
 
    const el = await fixture(html`<sakai-date-picker add-hidden-fields hidden-prefix="test-"></sakai-date-picker>`);

    const picker = el.querySelector("input[type='datetime-local']");
    picker.value = "2017-06-01T08:30";

    picker.dispatchEvent(new Event("change"));

    await el.updateComplete;

    expect(el.querySelectorAll("input[type='hidden']").length).to.equal(5);
    expect(el.querySelector("input[name='test-year']").value).to.equal("2017");
    expect(el.querySelector("input[name='test-month']").value).to.equal("6");
    expect(el.querySelector("input[name='test-day']").value).to.equal("1");
    expect(el.querySelector("input[name='test-hour']").value).to.equal("8");
    expect(el.querySelector("input[name='test-min']").value).to.equal("30");
  });

  it ("is accessible", async () => {

    const el = await fixture(html`<sakai-date-picker iso-date="2019-09-07T15:50" label="eggs" add-hidden-fields hidden-prefix="test-"></sakai-date-picker>`);

    await expect(el).to.be.accessible();
  });

  it ("handles timezones and DST correctly", async () => {
    window.top.portal = { user: { offsetFromServerMillis: 0, timezone: "America/New_York" } };

    let el = await fixture(html`<sakai-date-picker epoch-millis="1710269364000" add-hidden-fields hidden-prefix="test-"></sakai-date-picker>`);
    expect(el.querySelectorAll("input[type='hidden']").length).to.equal(5);
    expect(el.querySelector("input[name='test-year']").value).to.equal("2024");
    expect(el.querySelector("input[name='test-month']").value).to.equal("3");
    expect(el.querySelector("input[name='test-day']").value).to.equal("12");
    expect(el.querySelector("input[name='test-hour']").value).to.equal("14");
    expect(el.querySelector("input[name='test-min']").value).to.equal("49");

    window.top.portal = { user: { offsetFromServerMillis: 0, timezone: "US/Pacific" } };

    el = await fixture(html`<sakai-date-picker epoch-millis="1710269364000" add-hidden-fields hidden-prefix="test-"></sakai-date-picker>`);
    expect(el.querySelectorAll("input[type='hidden']").length).to.equal(5);
    expect(el.querySelector("input[name='test-year']").value).to.equal("2024");
    expect(el.querySelector("input[name='test-month']").value).to.equal("3");
    expect(el.querySelector("input[name='test-day']").value).to.equal("12");
    expect(el.querySelector("input[name='test-hour']").value).to.equal("11");
    expect(el.querySelector("input[name='test-min']").value).to.equal("49");
  });

});
