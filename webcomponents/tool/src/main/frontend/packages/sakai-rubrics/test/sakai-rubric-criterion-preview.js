import "../sakai-rubric-criterion-preview.js";
import { html } from "lit";
import * as data from "./data.js";
import { expect, fixture, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

window.top.portal = { locale: "en_GB" };

fetchMock
  .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
  .get("*", 500, { overwriteRoutes: true });

window.sakai = window.sakai || {
  editor: {
    launch: () => ({ focus: () => "", on: () => "", setData: (data, callback) => "" })
  },
};

describe("sakai-rubric-criterion-preview tests", () => {

  it ("criterion preview renders correctly", async () => {

    let el = await fixture(html`
      <sakai-rubric-criterion-preview .criteria=${data.criteria1}></sakai-rubric-criterion-preview>
    `);

    await waitUntil(() => el._i18n);

    const criteriaRows = el.querySelectorAll(".criterion-row");
    expect(criteriaRows.length).to.equal(data.criteria1.length);
    const ratingItems = criteriaRows[0].querySelectorAll(".rating-item");
    expect(ratingItems.length).to.equal(data.criteria1[0].ratings.length);
    expect(ratingItems[0].querySelector(".div-description").innerHTML).to.contain(data.criteria1[0].ratings[0].description);
    expect(el.querySelectorAll(".criterion-group").length).to.equal(1);
  });
});
