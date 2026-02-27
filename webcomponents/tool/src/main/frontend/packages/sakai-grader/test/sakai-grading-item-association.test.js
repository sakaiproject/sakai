import "../sakai-grading-item-association.js";
import { aTimeout, elementUpdated, expect, fixture, fixtureCleanup, waitUntil } from "@open-wc/testing";
import { html } from "lit";
import * as data from "./data.js";
import fetchMock from "fetch-mock";
describe("sakai-grading-item-association tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    setRoutes(data.gradingItemDataWithoutCategories);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });


  window.top.portal = { locale: "en_GB", siteId: data.siteId, siteTitle: data.siteTitle };

  const setRoutes = (itemData) => {
    fetchMock.removeRoutes();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.gradingItemDataUrl, itemData)
      .get("*", 500);
  };

  it ("displays with no current grading item and no categories", async () => {

    const el = await fixture(html`<sakai-grading-item-association id="balls" site-id="${data.siteId}" gradable-type="${data.gradableType}"></sakai-grading-item-association>`);

    await waitUntil(() => el._i18n);

    const label = el.renderRoot.querySelector("label");
    expect(label).to.exist;
    expect(label.innerHTML).to.contain(data.gradableType);
    const gradeCheckbox = label.querySelector("input");
    expect(gradeCheckbox.checked).to.be.false;

    expect(el.renderRoot.querySelector('input[type="radio"]')).to.not.exist;
    expect(el.renderRoot.getElementById("points")).to.not.exist;

    gradeCheckbox.click();

    await el.updateComplete;

    expect(el.renderRoot.getElementById("points")).to.exist;
    expect(el.renderRoot.querySelector('input[type="radio"]')?.checked).to.be.true;

    expect(el.renderRoot.getElementById("categories")).to.not.exist;

    const associateRadio = el.renderRoot.getElementById("associate");
    expect(associateRadio).to.exist;

    associateRadio.click();

    await el.updateComplete;

    const itemSelect = el.renderRoot.getElementById("items");
    expect(itemSelect).to.exist;
    expect(itemSelect.options.length).to.equal(data.gradingItemDataWithoutCategories.items.length);
    expect(itemSelect.options[0].value).to.equal(data.gradingItemDataWithoutCategories.items[0].id.toString());
    expect(itemSelect.options[1].value).to.equal(data.gradingItemDataWithoutCategories.items[1].id.toString());

    // Select an existing item and the points should update
    itemSelect.value = itemSelect.options[0].value;

    itemSelect.dispatchEvent(new Event("change"));

    await el.updateComplete;

    expect(el.gradingItemId).to.equal(data.gradingItemDataWithoutCategories.items[0].id.toString());

    const pointsInput = el.renderRoot.getElementById("points");
    expect(pointsInput.value).to.equal(data.gradingItemDataWithoutCategories.items[0].points.toString());
  });

  it ("displays with no current grading item but with categories", async () => {

    setRoutes(data.gradingItemDataWithCategories);

    const el = await fixture(html`<sakai-grading-item-association site-id="${data.siteId}" gradable-type="${data.gradableType}"></sakai-grading-item-association>`);

    await waitUntil(() => el._i18n);
    await waitUntil(() => el._categories);

    const gradeCheckbox = el.renderRoot.querySelector("input[type='checkbox']");
    expect(gradeCheckbox).to.exist;

    gradeCheckbox.click();

    await el.updateComplete;

    const useCategoriesCheckbox = el.renderRoot.getElementById("use-categories-checkbox");
    expect(useCategoriesCheckbox).to.exist;

    const categoriesSelect = el.renderRoot.getElementById("categories");
    expect(categoriesSelect).to.exist;
    expect(categoriesSelect.disabled).to.be.true;

    useCategoriesCheckbox.click();
    await el.updateComplete;
    expect(categoriesSelect.disabled).to.be.false;

    expect(categoriesSelect.options.length).to.equal(data.gradingItemDataWithCategories.categories.length);
    expect(categoriesSelect.options[0].value).to.equal(data.gradingItemDataWithCategories.categories[0].id.toString());
    expect(categoriesSelect.options[1].value).to.equal(data.gradingItemDataWithCategories.categories[1].id.toString());

    categoriesSelect.value = data.gradingItemDataWithCategories.categories[0].id;

    categoriesSelect.dispatchEvent(new Event("change"));
    expect(el.category).to.equal(data.gradingItemDataWithCategories.categories[0].id.toString());

    await expect(el).to.be.accessible();
  });

  it ("displays with pre selected grading item", async () => {

    const el = await fixture(html`
      <sakai-grading-item-association
          .gradingItemId=${data.gradingItemDataWithCategories.items[0].id}
          gradable-ref="${data.gradableRef}"
          site-id="${data.siteId}"
          gradable-type="${data.gradableType}"
          use-grading>
      </sakai-grading-item-association>
    `);

    await waitUntil(() => el._i18n);
    await waitUntil(() => el._gradingItems);

    await elementUpdated(el);

    const gradeCheckbox = el.renderRoot.querySelector("input[type='checkbox']");
    expect(gradeCheckbox?.checked).to.be.true;

    const associateRadio = el.renderRoot.getElementById("associate");
    expect(associateRadio?.checked).to.be.true;

    const itemSelect = el.renderRoot.getElementById("items");
    expect(itemSelect).to.exist;
    expect(itemSelect.value).to.equal(data.gradingItemDataWithCategories.items[0].id.toString());

    const pointsInput = el.renderRoot.getElementById("points");
    expect(pointsInput.disabled).to.be.false;
    expect(pointsInput.value).to.equal(data.gradingItemDataWithCategories.items[0].points.toString());

    await expect(el).to.be.accessible();

    itemSelect.value = data.gradingItemDataWithCategories.items[1].id;
    itemSelect.dispatchEvent(new Event("change"));

    await el.updateComplete;

    // Because we've now selected another item, not our external item, points should not be updatable
    expect(pointsInput.disabled).to.be.true;
    expect(pointsInput.value).to.equal(data.gradingItemDataWithCategories.items[1].points.toString());

    await expect(el).to.be.accessible();
  });
});
