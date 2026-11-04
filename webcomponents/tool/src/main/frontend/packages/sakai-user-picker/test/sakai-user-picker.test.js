import "../sakai-user-picker.js";
import { sendKeys } from "@web/test-runner-commands";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";

const translations = `search=Search by name or username
no_results=No matches found
no_options=No choices available
one_result=1 result
result_count={} results
truncated_results=Showing {shown} of {total} results. Refine your search to see other matches.
remove=Remove {}
selected_count={} selected`;

const root = picker => picker.renderRoot;
const input = picker => root(picker).querySelector("input");
const results = picker => [ ...root(picker).querySelectorAll("[role=option]") ];

async function search(picker, query) {
  input(picker).value = query;
  input(picker).dispatchEvent(new InputEvent("input", { bubbles: true }));
  await elementUpdated(picker);
}

function key(picker, key) {
  input(picker).dispatchEvent(new KeyboardEvent("keydown", { key, bubbles: true, cancelable: true }));
}

describe("sakai-user-picker", () => {
  before(() => { sessionStorage.setItem("user-picker", JSON.stringify(Object.fromEntries(
    translations.split("\n").map(line => line.split("="))))); });
  after(() => { sessionStorage.removeItem("user-picker"); });

  it("matches both name orders, accents and IDs and retains separate identities", async () => {
    const picker = await fixture(html`<sakai-user-picker multiple .options=${[
      { id: "a", name: "Zachary Ánderson", username: "z001", group: "People" },
      { id: "b", name: "Zachary Ánderson", username: "z002", group: "People" },
      { id: "c", name: "Seminar", group: "Sections" },
    ]}></sakai-user-picker>`);
    await waitUntil(() => input(picker));
    for (const term of [ "Zachary Anderson", "anderson, zach", "Ánderson ZACH" ]) {
      await search(picker, term);
      expect(results(picker)).to.have.length(2);
    }
    await search(picker, "z002");
    key(picker, "ArrowDown");
    await elementUpdated(picker);
    expect(input(picker).getAttribute("aria-activedescendant")).to.equal(results(picker)[0].id);
    key(picker, "Enter");
    await elementUpdated(picker);
    expect(picker.values).to.deep.equal([ "b" ]);
    const remove = root(picker).querySelector("button");
    expect(remove.getAttribute("aria-label")).to.equal("Remove Zachary Ánderson (z002)");
    expect(remove.querySelector("small").textContent).to.equal("(z002)");
    remove.click();
    await elementUpdated(picker);
    expect(picker.values).to.be.empty;
    expect(root(picker).activeElement).to.equal(input(picker));

    await search(picker, "Seminar");
    expect(root(picker).querySelector("[role=group]").getAttribute("aria-label")).to.equal("Sections");
    key(picker, "Escape");
    await elementUpdated(picker);
    expect(input(picker).getAttribute("aria-expanded")).to.equal("false");
    await search(picker, "No matching person");
    expect(root(picker).querySelector("p").textContent).to.equal("No matches found");
  });

  it("describes the visible result limit and announces a single match", async () => {
    const options = Array.from({ length: 102 }, (_, index) => ({
      id: `id-${index}`, name: `Person ${index}`, username: `username-${index}`,
    }));
    const picker = await fixture(html`<sakai-user-picker .options=${options}></sakai-user-picker>`);
    await waitUntil(() => input(picker));
    await search(picker, "Person");
    expect(results(picker)).to.have.length(100);
    const hint = "Showing 100 of 102 results. Refine your search to see other matches.";
    expect(root(picker).querySelector("p").textContent).to.equal(hint);
    expect(root(picker).querySelector("[role=status]").textContent).to.equal(hint);
    await search(picker, "username-101");
    expect(results(picker)).to.have.length(1);
    expect(root(picker).querySelector("[role=status]").textContent).to.equal("1 result");
  });

  it("submits only each bound field's values and restores preselection and resets", async () => {
    const form = await fixture(html`
      <form>
        <label for="to">To</label>
        <select id="to" name="to" multiple>
          <option value="a" selected>Alice</option><option value="b">Bob</option>
        </select>
        <label for="bcc">Bcc</label>
        <select id="bcc" name="bcc" multiple>
          <option value="a">Alice</option><option value="b" selected>Bob</option>
        </select>
        <button type="reset">Reset</button>
      </form>`);
    const to = document.createElement("sakai-user-picker");
    const bcc = document.createElement("sakai-user-picker");
    to.bindSelect(form.querySelector("#to"));
    bcc.bindSelect(form.querySelector("#bcc"));
    await waitUntil(() => form.querySelector("#to").hidden && form.querySelector("#bcc").hidden);
    expect(input(to).getAttribute("aria-label")).to.equal("To");
    expect(input(bcc).getAttribute("aria-label")).to.equal("Bcc");
    await search(to, "Bob");
    results(to)[0].click();
    expect(new FormData(form).getAll("to")).to.deep.equal([ "a", "b" ]);
    expect(new FormData(form).getAll("bcc")).to.deep.equal([ "b" ]);
    bcc.disabled = true;
    await elementUpdated(bcc);
    expect(input(bcc).disabled).to.be.true;
    expect(root(bcc).querySelector("button").disabled).to.be.true;
    form.querySelector("button[type=reset]").focus();
    await sendKeys({ press: "Enter" });
    await waitUntil(() => to.values.length === 1);
    await elementUpdated(to);
    expect(new FormData(form).getAll("to")).to.deep.equal([ "a" ]);
    expect(to.values).to.deep.equal([ "a" ]);
    expect(bcc.values).to.deep.equal([ "b" ]);
  });
});
