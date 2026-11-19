import "../sakai-user-picker.js";
import { sendKeys, sendMouse } from "@web/test-runner-commands";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";

const translations = `search=Search by name or username
no_results=No matches found
no_options=No choices available
one_result=1 result
result_count={} results
truncated_results=Showing {shown} of {total} results. Refine your search to see other matches.
clear_selection=Clear all selections
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

  it("keeps following form controls stationary when the results close", async () => {
    const form = await fixture(html`<form style="width: 400px">
      <sakai-user-picker multiple .options=${[ { id: "a", name: "Ada" } ]}></sakai-user-picker>
      <label style="display: block; margin-top: 100px"><input type="checkbox"> Bcc</label>
    </form>`);
    const picker = form.querySelector("sakai-user-picker");
    const checkbox = form.querySelector("input");
    await waitUntil(() => input(picker));
    const before = checkbox.getBoundingClientRect();
    input(picker).focus();
    await elementUpdated(picker);
    expect(checkbox.getBoundingClientRect().top).to.equal(before.top);
    const point = checkbox.getBoundingClientRect();
    await sendMouse({ type: "click", position: [ Math.round(point.x + point.width / 2), Math.round(point.y + point.height / 2) ] });
    expect(checkbox.checked).to.equal(true);
  });

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

  it("shows all 189 results and caps larger searches at 999", async () => {
    const options = Array.from({ length: 1001 }, (_, index) => ({
      id: `id-${index}`, name: `Person ${index}`, username: `username-${index}`,
    }));
    const picker = await fixture(html`<sakai-user-picker .options=${options.slice(0, 189)}></sakai-user-picker>`);
    await waitUntil(() => input(picker));
    await search(picker, "Person");
    expect(results(picker)).to.have.length(189);
    expect(root(picker).querySelector("p")).to.be.null;
    expect(root(picker).querySelector("[role=status]").textContent).to.equal("189 results");
    picker.options = options;
    await elementUpdated(picker);
    expect(results(picker)).to.have.length(999);
    const hint = "Showing 999 of 1001 results. Refine your search to see other matches.";
    expect(root(picker).querySelector("p").textContent).to.equal(hint);
    expect(root(picker).querySelector("[role=status]").textContent).to.equal(hint);
    await search(picker, "username-1000");
    expect(results(picker)).to.have.length(1);
    expect(root(picker).querySelector("[role=status]").textContent).to.equal("1 result");
  });

  it("clears all selections from the keyboard and updates only the bound field", async () => {
    const form = await fixture(html`<form>
      <select name="people" multiple><option value="a" selected>Alice</option><option value="b" selected>Bob</option></select>
      <select name="other"><option value="c" selected>Carol</option></select>
    </form>`);
    const picker = document.createElement("sakai-user-picker");
    picker.bindSelect(form.querySelector('[name="people"]'));
    await waitUntil(() => !picker.hidden);
    await search(picker, "Bob");
    const clear = root(picker).querySelector('[aria-label="Clear all selections"]');
    expect(clear.type).to.equal("button");
    let changes = 0;
    form.addEventListener("change", () => changes++);
    clear.focus();
    await sendKeys({ press: "Enter" });
    await waitUntil(() => picker.values.length === 0);
    await elementUpdated(picker);
    expect(new FormData(form).getAll("people")).to.deep.equal([]);
    expect(new FormData(form).getAll("other")).to.deep.equal([ "c" ]);
    expect(changes).to.equal(1);
    expect(input(picker).value).to.equal("");
    expect(root(picker).activeElement).to.equal(input(picker));
    expect(root(picker).querySelector('[aria-label="Clear all selections"]')).to.be.null;
  });

  it("disables clear-all with the field and preserves locked selections", async () => {
    const picker = await fixture(html`<sakai-user-picker multiple disabled
      .options=${[ { id: "locked", name: "Locked", disabled: true }, { id: "open", name: "Open" } ]}
      .values=${[ "locked", "open" ]}></sakai-user-picker>`);
    await waitUntil(() => input(picker));
    const clear = root(picker).querySelector('[aria-label="Clear all selections"]');
    expect(clear.disabled).to.be.true;
    clear.click();
    expect(picker.values).to.deep.equal([ "locked", "open" ]);
    picker.disabled = false;
    await elementUpdated(picker);
    clear.click();
    await elementUpdated(picker);
    expect(picker.values).to.deep.equal([ "locked" ]);
    expect(root(picker).querySelector('[aria-label="Clear all selections"]')).to.be.null;
  });

  it("keeps multi-select results open and refreshes them when options change", async () => {
    const picker = await fixture(html`<sakai-user-picker multiple .options=${[
      { id: "disabled", name: "Alex Jones", disabled: true },
      { id: "a", name: "Alex Kim" }, { id: "b", name: "Alex Lee" },
    ]}></sakai-user-picker>`);
    await waitUntil(() => input(picker));
    await search(picker, "");
    key(picker, "Enter");
    expect(picker.values).to.deep.equal([]);
    await search(picker, "Alex");
    key(picker, "Enter");
    await elementUpdated(picker);
    expect(input(picker).value).to.equal("Alex");
    expect(input(picker).getAttribute("aria-expanded")).to.equal("true");
    results(picker)[2].click();
    await elementUpdated(picker);
    expect(picker.values).to.deep.equal([ "a", "b" ]);

    picker.options = [ { id: "c", name: "Alex Smith" } ];
    await elementUpdated(picker);
    expect(results(picker)).to.have.length(1);
    expect(results(picker)[0].textContent).to.contain("Alex Smith");
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
    expect(to.hidden).to.be.true;
    expect(form.querySelector("#to").hidden).to.be.false;
    await waitUntil(() => form.querySelector("#to").hidden && form.querySelector("#bcc").hidden);
    expect(to.hidden).to.be.false;
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
