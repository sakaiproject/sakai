import "../sakai-tasks-create-task.js";
import { html } from "lit";
import * as data from "./data.js";
import * as dialogContentData from "../../sakai-dialog-content/test/data.js";
import * as groupPickerData from "../../sakai-group-picker/test/data.js";
import { SITE } from "../src/assignation-types.js";
import { expect, fixture, waitUntil, aTimeout } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-tasks-create-task tests", () => {

  const minusFiveHours = -5 * 60 * 60 * 1000;
  window.top.portal = { locale: "en_GB", user: { offsetFromServerMillis: minusFiveHours, timezone: "America/New_York" } };
  window.moment = { duration: () => { return { humanize: () => "3 days ago" } } };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get(dialogContentData.i18nUrl, dialogContentData.i18n, { overwriteRoutes: true })
    .get(groupPickerData.i18nUrl, groupPickerData.i18n, { overwriteRoutes: true })
    .get(groupPickerData.groupsUrl, groupPickerData.groups, { overwriteRoutes: true })
    .get(data.tasksUrl, data.tasks, { overwriteRoutes: true })
    .post(data.tasksPostUrl, (url, opts) => {

      return Object.assign({
        id: "" + Math.floor(Math.random() * 20) + 1,
        creator: "adrian",
        created: Date.now(),
        creatorDisplayName: "Adrian Fish",
      }, JSON.parse(opts.body));
    }, {overwriteRoutes: true})
    .get("*", 500, {overwriteRoutes: true});

  it ("renders in user mode correctly", async () => {

    // In user mode, we'd expect to get announcements from multiple sites.
    let el = await fixture(html`
      <sakai-tasks-create-task user-id="${data.userId}"></sakai-tasks-create-task>
    `);

    el.assignationType = SITE;

    const description = "Go to space";
    const notes = "This task is about going to space";
    const priority = "5";

    await waitUntil(() => el._i18n);

    const descriptionEl = el.shadowRoot.getElementById("description");
    expect(descriptionEl).to.exist;
    descriptionEl.value = description;
    descriptionEl.dispatchEvent(new Event("input"));
    expect(el.task.description).to.equal(description);

    const notesEl = el.shadowRoot.querySelector("[element-id='task-text-editor']");
    expect(notesEl).to.exist;
    notesEl.setContent(notes);
    notesEl._dispatchChangedEvent(notes);

    const priorityEl = el.shadowRoot.getElementById("priority");
    expect(priorityEl).to.exist;
    priorityEl.value = priority;
    priorityEl.dispatchEvent(new Event("change"));

    el.addEventListener("task-created", e => {

      expect(e.detail.task).to.exist;
      expect(e.detail.task.description).to.equal(description);
      expect(e.detail.task.notes).to.equal(notes);
      expect(e.detail.task.priority).to.equal(priority);
      expect(e.detail.task.assignationType).to.equal(SITE);
    });

    const saveEl = el.shadowRoot.querySelector("sakai-button");
    expect(saveEl).to.exist;
    saveEl.click();
  });

  it ("renders in site mode correctly", async () => {

    const description = "Go to space";
    const notes = "This task is about going to space";
    const priority = "5";

    // In user mode, we'd expect to get announcements from multiple sites.
    let el = await fixture(html`
      <sakai-tasks-create-task site-id="${data.siteId}" deliver-tasks></sakai-tasks-create-task>
    `);

    await waitUntil(() => el._i18n && el.groups);

    const descriptionEl = el.shadowRoot.getElementById("description");
    expect(descriptionEl).to.exist;
    descriptionEl.value = description;

    expect(el.shadowRoot.getElementById("create-task-block")).to.exist;
    expect(el.shadowRoot.getElementById("task-current-user")).to.exist;
    expect(el.shadowRoot.getElementById("task-students")).to.exist;
    expect(el.shadowRoot.getElementById("task-groups")).to.not.exist;
    expect(el.shadowRoot.querySelector("sakai-group-picker")).to.not.exist;
  });

  it ("renders in site mode with groups correctly", async () => {

    let el = await fixture(html`
      <sakai-tasks-create-task site-id="${data.siteId}" .groups=${groupPickerData.groups} deliver-tasks></sakai-tasks-create-task>
    `);

    await waitUntil(() => el._i18n && el.groups);

    expect(el.shadowRoot.getElementById("task-groups")).to.exist;
    expect(el.shadowRoot.querySelector("sakai-group-picker")).to.exist;
  });

  it ("is accessible", async () => {

    let el = await fixture(html`
      <sakai-tasks-create-task user-id="${data.userId}"></sakai-tasks-create-task>
    `);

    await waitUntil(() => el._i18n);

    expect(el.shadowRoot).to.be.accessible();
  });
});
