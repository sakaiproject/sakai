import "../sakai-rubric-edit.js";
import { html } from "lit";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, oneEvent, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

window.top.portal = { locale: "en_GB" };

fetchMock
  .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
  .get("*", 500, { overwriteRoutes: true });

window.top.portal = { locale: "en_GB" };

describe("sakai-rubrics tests", () => {

  beforeEach(() => {
    delete data.rubric1.new;
  });

  it ("tests that a rubric edit component renders correctly, opening and closing bootstrap modals", async () => {

    let el = await fixture(html`
      <sakai-rubric-edit>
      </sakai-rubric-edit>
    `);

    await waitUntil(() => el._i18n);

    expect(el.querySelector("button")).to.not.exist;

    el.rubric = data.rubric1;

    await elementUpdated(el);

    const button = el.querySelector("button.edit-button");
    expect(button).to.exist;
    button.click();

    await oneEvent(el, "shown.bs.modal");

    const modal = el.querySelector("div.modal");
    expect(modal).to.exist;

    expect(modal.classList.contains("show")).to.be.true;

    button.click();

    await oneEvent(el, "hidden.bs.modal");

    expect(modal.classList.contains("show")).to.be.false;

    data.rubric1.new = true;

    el.rubric = data.rubric1;

    await elementUpdated(el);

    await oneEvent(el, "shown.bs.modal");

    expect(modal.classList.contains("show")).to.be.true;
  });

  it ("tests that you can edit a rubric title", async () => {

    let el = await fixture(html`
      <sakai-rubric-edit .rubric=${data.rubric1}>
      </sakai-rubric-edit>
    `);

    await waitUntil(() => el._i18n);

    const button = el.querySelector("button.edit-button");
    expect(button).to.exist;
    button.click();
    await oneEvent(el, "shown.bs.modal");

    const newTitle = "Chips";

    // Modal should be vislble now, enter some text in the title field
    const titleField = el.querySelector("input[type='text']");
    expect(titleField).to.exist;
    titleField.value = newTitle;

    const saveButton = el.querySelector("button.btn-primary");
    expect(saveButton).to.exist;

    setTimeout(() => saveButton.click());

    const { detail } = await oneEvent(el, "update-rubric-title");

    expect(detail).to.equal(newTitle);

    await oneEvent(el, "hidden.bs.modal");

    expect(el.querySelector("div.modal").classList.contains("show")).to.be.false;
  });

  it ("tests that a new rubric pops up the modal and that you can cancel the edit", async () => {

    data.rubric1.new = true;

    let el = await fixture(html`
      <sakai-rubric-edit .rubric=${data.rubric1}>
      </sakai-rubric-edit>
    `);

    await waitUntil(() => el._i18n);
    await el.updateComplete;

    const titleField = el.querySelector("input[type='text']");

    // Make sure title field is selected by default
    expect(document.activeElement.value === titleField.value).to.be.equal;

    await oneEvent(el, "shown.bs.modal");

    expect(titleField).to.exist;
    titleField.value = "cheeses";

    const cancelButton = el.querySelector("button.btn-secondary");
    expect(cancelButton).to.exist;
    cancelButton.click();

    await oneEvent(el, "hidden.bs.modal");

    expect(titleField.value).to.equal(data.rubric1.title);
  });
});

