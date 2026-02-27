import "../sakai-rubric-criterion-edit.js";
import "../sakai-rubrics-utils.js";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, html, oneEvent, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";
window.top.portal = { locale: "en_GB" };

window.sakai = window.sakai || {
  editor: {
    launch: () => ({ focus: () => "", on: () => "", setData: (data, callback) => "" })
  },
};

describe("sakai-rubric-criterion-edit tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.rubric1Url, data.rubric1)
      .get(data.associationUrl, data.association)
      .get(data.evaluationUrl, data.evaluation)
      .put(data.rubric4CriteriaSortUrl, 200)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });


  it ("criterion edit with textarea works correctly", async () => {

    let el = await fixture(html`
      <sakai-rubric-criterion-edit
          site-id="${data.siteId}"
          rubric-id="${data.rubric1.id}"
          .criterion=${data.criterion1}
          textarea>
      </sakai-rubric-criterion-edit>
    `);

    await waitUntil(() => el.querySelector("button.edit-criterion-button"), "edit button does not exist");
    expect(el.querySelector(`#edit-criterion-${data.criterion1.id}`)).to.exist;
    expect(el.querySelector("sakai-editor")).to.exist;
    const button = el.querySelector("button.edit-criterion-button");
    expect(button.getAttribute("title")).to.equal(el._i18n.edit_criterion + " " + data.criterion1.title);
    let modal = el.querySelector(`#edit-criterion-${data.criterion1.id}`);

    const listener = oneEvent(modal, "shown.bs.modal");
    button.click();
    await listener;

    modal = el.querySelector(".modal.show");
    expect(modal).to.exist;
  });

  it ("criterion edit does not keep data changes in the modal after cancel", async () => {

    let el = await fixture(html`
      <sakai-rubric-criterion-edit
          site-id="${data.siteId}"
          rubric-id="${data.rubric1.id}"
          .criterion=${data.criterion1}
          textarea>
      </sakai-rubric-criterion-edit>
    `);

    await waitUntil(() => el.querySelector("button.edit-criterion-button"), "edit button does not exist");
    expect(el.querySelector(`#edit-criterion-${data.criterion1.id}`)).to.exist;
    expect(el.querySelector("sakai-editor")).to.exist;
    const button = el.querySelector("button.edit-criterion-button");
    expect(button.getAttribute("title")).to.equal(el._i18n.edit_criterion + " " + data.criterion1.title);
    let modal = el.querySelector(`#edit-criterion-${data.criterion1.id}`);

    const listener = oneEvent(modal, "shown.bs.modal");
    button.click();
    await listener;

    modal = el.querySelector(".modal.show");
    expect(modal).to.exist;

    let titleInput = modal.querySelector(`#criterion-title-edit-${data.criterion1.id}`);
    expect(titleInput.getAttribute("value")).to.equal(data.criterion1.title);
    titleInput.value = 'foobar';
    titleInput.dispatchEvent(new Event("input"));
    await el.updateComplete;

    expect(titleInput.value).to.not.equal(data.criterion1.title);

    let descriptionInput = modal.querySelector(`#criterion-description-edit-${data.criterion1.id}`);

    expect(descriptionInput.getContent()).to.equal(data.criterion1.description);
    descriptionInput.setContent('qwerty');
    descriptionInput.dispatchEvent(new Event("input"));
    await el.updateComplete;

    expect(descriptionInput.getContent()).to.not.equal(data.criterion1.description);

    let cancelButton = modal.querySelector(`#criterion-cancel-${data.criterion1.id}`);

    const cancelListener = oneEvent(modal, "hidden.bs.modal");
    cancelButton.click();
    await cancelListener;

    //Open modal again
    button.click();
    await listener;

    titleInput = modal.querySelector(`#criterion-title-edit-${data.criterion1.id}`);
    expect(titleInput.value).to.equal(data.criterion1.title);

    descriptionInput = modal.querySelector(`#criterion-description-edit-${data.criterion1.id}`);
    expect(descriptionInput.getContent()).to.equal(data.criterion1.description);
  });
});
