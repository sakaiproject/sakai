import "../sakai-reorderer.js";
import { expect, fixture, html } from "@open-wc/testing";

describe("sakai-reorderer tests", () => {

  it ("uses E and D for keyboard reordering after refresh", async () => {

    const el = await fixture(html`
      <sakai-reorderer>
        <div>
          <div data-reorderable-id="item-1"><span class="drag-handle" tabindex="0"></span>Item 1</div>
          <div data-reorderable-id="item-2"><span class="drag-handle" tabindex="0"></span>Item 2</div>
        </div>
      </sakai-reorderer>
    `);

    await el.updateComplete;
    el.requestUpdate();
    await el.updateComplete;

    const events = [];
    el.addEventListener("reordered", event => events.push(event.detail));

    el.querySelector("[data-reorderable-id='item-2'] .drag-handle").dispatchEvent(new KeyboardEvent("keyup", { bubbles: true, key: "u" }));
    expect(events).to.have.length(0);

    el.querySelector("[data-reorderable-id='item-2'] .drag-handle").dispatchEvent(new KeyboardEvent("keyup", { bubbles: true, key: "e" }));
    expect(events).to.have.length(1);
    expect(events[0].reorderedIds).to.deep.equal([ "item-2", "item-1" ]);

    const container = el.querySelector("div");
    container.append(el.querySelector("[data-reorderable-id='item-1']"));
    el.refresh();
    el.querySelector("[data-reorderable-id='item-2'] .drag-handle").dispatchEvent(new KeyboardEvent("keyup", { bubbles: true, key: "d" }));
    expect(events[1].reorderedIds).to.deep.equal([ "item-1", "item-2" ]);
  });

  it ("only starts native dragging from the drag handle", async () => {

    const el = await fixture(html`
      <sakai-reorderer apply-dom>
        <div>
          <div data-reorderable-id="item-1"><span class="drag-handle" tabindex="0"></span>Item 1</div>
          <div data-reorderable-id="item-2"><span class="drag-handle" tabindex="0"></span>Item 2</div>
        </div>
      </sakai-reorderer>
    `);

    await el.updateComplete;

    const item = el.querySelector("[data-reorderable-id='item-1']");
    const dragStart = new DragEvent("dragstart", { bubbles: true, cancelable: true });
    item.dispatchEvent(dragStart);
    expect(dragStart.defaultPrevented).to.be.true;

    const events = [];
    el.addEventListener("reordered", event => events.push(event.detail));
    item.querySelector(".drag-handle").dispatchEvent(new PointerEvent("pointerdown", { bubbles: true }));
    item.dispatchEvent(new DragEvent("dragstart", { bubbles: true, cancelable: true }));
    el.querySelector("div").dispatchEvent(new DragEvent("drop", { bubbles: true, cancelable: true, clientY: 1000 }));

    expect(events).to.have.length(1);
    expect(events[0].reorderedIds).to.deep.equal([ "item-2", "item-1" ]);
    expect(events[0].previousReorderedIds).to.deep.equal([ "item-1", "item-2" ]);
    expect(Array.from(el.querySelector("div").children).map(item => item.dataset.reorderableId)).to.deep.equal([ "item-2", "item-1" ]);
  });
});
