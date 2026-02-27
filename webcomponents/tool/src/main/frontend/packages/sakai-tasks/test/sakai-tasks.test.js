import "../sakai-tasks.js";
import * as data from "./data.js";
import * as dialogContentData from "../../sakai-dialog-content/test/data.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";
describe("sakai-tasks tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(dialogContentData.baseI18nUrl, dialogContentData.baseI18n)
      .get(data.tasksUrl, data.tasks)
      .post(data.tasksUrl, ({ url, options }) => {

        return Object.assign({
          id: "" + (Math.floor(Math.random() * 20) + 1),
          creator: "adrian",
          created: Date.now(),
          creatorDisplayName: "Adrian Fish",
        }, JSON.parse(options.body));
      })
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  const minusFiveHours = -5 * 60 * 60 * 1000;
  window.top.portal = { user: { offsetFromServerMillis: minusFiveHours, timezone: "America/New_York" } };

  it ("renders in user mode correctly", async () => {

    // In user mode, we'd expect to get announcements from multiple sites.
    let el = await fixture(html`
      <sakai-tasks user-id="${data.userId}"></sakai-tasks>
    `);

    await waitUntil(() => el.data);

    await elementUpdated(el);

    await expect(el).to.be.accessible({ ignoredRules: [ "aria-allowed-attr" ] });

    await waitUntil(() => el.shadowRoot.getElementById("controls"), "controls not created", { timeout: 3000 });
    expect(el.shadowRoot.getElementById("controls")).to.exist;
    expect(el.shadowRoot.getElementById("add-block")).to.exist;
    expect(el.shadowRoot.getElementById("add-edit-dialog")).to.exist;

    el._canAddTask = false;
    await elementUpdated(el);
    expect(el.shadowRoot.getElementById("add-block")).to.not.exist;
    el._canAddTask = true;
    await elementUpdated(el);
    expect(el.shadowRoot.getElementById("add-block")).to.exist;

    expect(el.shadowRoot.querySelectorAll("#tasks > .cell").length).to.equal(6);
    const addTaskButton = el.shadowRoot.querySelector("#add-block button");
    expect(addTaskButton).to.exist;

    await expect(el).to.be.accessible({ ignoredRules: [ "aria-allowed-attr" ] });

    expect(el.shadowRoot.querySelector("sakai-pager")).to.not.exist
  });
});
