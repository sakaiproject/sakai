import "../sakai-tasks.js";
import { html } from "lit";
import * as data from "./data.js";
import * as dialogContentData from "../../sakai-dialog-content/test/data.js";
import * as pagerData from "../../sakai-pager/test/data.js";
import { expect, fixture, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-tasks tests", () => {

  const minusFiveHours = -5 * 60 * 60 * 1000;
  window.top.portal = { locale: "en_GB", user: { offsetFromServerMillis: minusFiveHours, timezone: "America/New_York" } };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get(dialogContentData.i18nUrl, dialogContentData.i18n, { overwriteRoutes: true })
    .get(pagerData.i18nUrl, pagerData.i18n, { overwriteRoutes: true })
    .get(data.tasksUrl, data.tasks, { overwriteRoutes: true })
    .post(data.tasksUrl, (url, opts) => {

      return Object.assign({
        id: "" + Math.floor(Math.random() * 20) + 1,
        creator: "adrian",
        created: Date.now(),
        creatorDisplayName: "Adrian Fish",
      }, JSON.parse(opts.body));
    }, {overwriteRoutes: true})
    .get("*", 500, { overwriteRoutes: true });

  it ("renders in user mode correctly", async () => {

    // In user mode, we'd expect to get announcements from multiple sites.
    let el = await fixture(html`
      <sakai-tasks user-id="${data.userId}"></sakai-tasks>
    `);

    await waitUntil(() => el._data);

    await waitUntil(() => el.shadowRoot.getElementById("controls"), "controls not created");
    expect(el.shadowRoot.getElementById("controls")).to.exist;
    expect(el.shadowRoot.getElementById("add-block")).to.exist;
    expect(el.shadowRoot.getElementById("add-edit-dialog")).to.exist;

    el._canAddTask = false;
    await el.updateComplete;
    expect(el.shadowRoot.getElementById("add-block")).to.not.exist;
    el._canAddTask = true;
    await el.updateComplete;
    expect(el.shadowRoot.getElementById("add-block")).to.exist;

    expect(el.shadowRoot.querySelectorAll("#tasks > .cell").length).to.equal(6);
    const addTaskButton = el.shadowRoot.querySelector(".add-task-button");
    expect(addTaskButton).to.exist;

    const pager = el.shadowRoot.querySelector("sakai-pager");
    expect(pager).to.exist;
    expect(pager.count).to.equal(1);
  });

  it ("is accessible", async () => {

    let el = await fixture(html`
      <sakai-tasks user-id="${data.userId}"></sakai-tasks>
    `);

    await waitUntil(() => el._data);

    expect(el.shadowRoot).to.be.accessible();
  });
});
