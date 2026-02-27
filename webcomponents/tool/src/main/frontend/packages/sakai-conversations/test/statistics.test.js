import { elementUpdated, fixture, expect, html, aTimeout, waitUntil } from "@open-wc/testing";
import "../conversations-statistics.js";
import * as constants from "../src/sakai-conversations-constants.js";
import * as data from "./data.js";
import fetchMock from "fetch-mock";
describe("conversations-statistics", () => {

  const statsData = {
    total: 10,
    pageSize: 5,
    currentPage: 1,
    stats: [
      {
        name: "User 1",
        topicsCreated: 5,
        topicsViewed: 10,
        topicReactions: 3,
        topicUpvotes: 2,
        postsCreated: 8,
        postsViewed: 15,
        postReactions: 4,
        postUpvotes: 3
      },
      {
        name: "User 2",
        topicsCreated: 3,
        topicsViewed: 7,
        topicReactions: 2,
        topicUpvotes: 1,
        postsCreated: 5,
        postsViewed: 12,
        postReactions: 3,
        postUpvotes: 2
      }
    ]
  };

  const statsUrl = "/stats-url";

  beforeEach(async () => {
    fetchMock.mockGlobal();


    // Mock the stats endpoint
    fetchMock.get(data.i18nUrl, data.i18n)
      .post(statsUrl, { body: statsData, status: 200 });
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it("renders the statistics table with correct data", async () => {

    // Create the element
    const el = await fixture(html`
      <conversations-statistics stats-url="${statsUrl}">
      </conversations-statistics>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    // Verify the table exists
    const table = el.querySelector("#statistics-report-table");
    expect(table).to.exist;

    // Verify the table has the correct number of rows (2 users)
    const rows = table.querySelectorAll("tbody tr");
    expect(rows.length).to.equal(2);

    // Verify the first row contains the correct data
    const firstRowCells = rows[0].querySelectorAll("td");
    expect(firstRowCells[0].textContent).to.equal("User 1");
    expect(firstRowCells[1].textContent).to.equal("5");
    expect(firstRowCells[2].textContent).to.equal("10");
    expect(firstRowCells[3].textContent).to.equal("3");
    expect(firstRowCells[4].textContent).to.equal("2");
    expect(firstRowCells[5].textContent).to.equal("8");
    expect(firstRowCells[6].textContent).to.equal("15");
    expect(firstRowCells[7].textContent).to.equal("4");
    expect(firstRowCells[8].textContent).to.equal("3");
  });

  it("changes interval when radio buttons are clicked", async () => {

    // Create the element
    const el = await fixture(html`
      <conversations-statistics stats-url="${statsUrl}">
      </conversations-statistics>
    `);

    await waitUntil(() => el._stats);

    // Verify initial fetch was made with THIS_WEEK interval
    expect(fetchMock.callHistory.called(statsUrl)).to.be.true;
    const initialCall = fetchMock.callHistory.lastCall(statsUrl);
    const initialBody = JSON.parse(initialCall.options.body);
    expect(initialBody.interval).to.equal(constants.THIS_WEEK);

    // Click the "All Time" radio button
    const allTimeRadio = el.querySelector(`input[value="${constants.ALL_TIME}"]`);
    allTimeRadio.click();

    // Wait for any async operations to complete
    await aTimeout(100);

    // Verify the interval was changed and a new fetch was made
    expect(el.interval).to.equal(constants.ALL_TIME);
    expect(fetchMock.callHistory.called(statsUrl)).to.be.true;
    const allTimeCall = fetchMock.callHistory.lastCall(statsUrl);
    const allTimeBody = JSON.parse(allTimeCall.options.body);
    expect(allTimeBody.interval).to.equal(constants.ALL_TIME);
  });

  it("changes sort when column headers are clicked", async () => {

    // Create the element
    const el = await fixture(html`
      <conversations-statistics stats-url="${statsUrl}">
      </conversations-statistics>
    `);

    await waitUntil(() => el._stats);

    // Click the name sort header
    const nameSortHeader = el.querySelector(`a[data-sort="${constants.SORT_NAME}"]`);
    nameSortHeader.click();
    expect(el._sort).to.equal("nameDescending");

    // Wait for any async operations to complete
    await aTimeout(100);

    // Verify the sort was changed and a new fetch was made
    expect(fetchMock.callHistory.called(statsUrl)).to.be.true;
    const sortCall = fetchMock.callHistory.lastCall(statsUrl);
    const sortBody = JSON.parse(sortCall.options.body);
    expect(sortBody.sort).to.equal("nameDescending");

    // Reset the mock to track the next call
    fetchMock.clearHistory();

    // Click it again to toggle the sort direction
    nameSortHeader.click();
    expect(el._sort).to.equal("nameAscending");

    // Wait for any async operations to complete
    await aTimeout(100);

    // Verify the sort was toggled and a new fetch was made
    expect(fetchMock.callHistory.called(statsUrl)).to.be.true;
    const toggleCall = fetchMock.callHistory.lastCall(statsUrl);
    const toggleBody = JSON.parse(toggleCall.options.body);
    expect(toggleBody.sort).to.equal("nameAscending");
  });

  it("handles pager clicks", async () => {

    // Create the element
    const el = await fixture(html`
      <conversations-statistics stats-url="${statsUrl}">
      </conversations-statistics>
    `);

    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    // Simulate a page-selected event
    el._pageClicked({ detail: { page: 2 } });

    // Wait for any async operations to complete
    await aTimeout(100);

    // Verify a new fetch was made with the correct page
    expect(fetchMock.callHistory.called(statsUrl)).to.be.true;
    const pageCall = fetchMock.callHistory.lastCall(statsUrl);
    const pageBody = JSON.parse(pageCall.options.body);
    expect(pageBody.page).to.equal(2);
  });
});
