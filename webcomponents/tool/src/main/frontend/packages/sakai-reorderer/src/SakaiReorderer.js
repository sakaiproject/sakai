import { html, LitElement } from "lit";

/**
 * @example
 *  <sakai-reorderer>
 *    <div id="container">
 *      <div data-reorderable-id="item1"><span class="drag-handle"></span><span>Item 1</span></div>
 *      <div data-reorderable-id="item2"><span class="drag-handle"></span><span>Item 2</span></div>
 *      <div data-reorderable-id="item3"><span class="drag-handle"></span><span>Item 3</span></div>
 *      <div data-reorderable-id="item4"><span class="drag-handle"></span><span>Item 4</span></div>
 *    </div>
 *  </sakai-reorderer>
 *
 * A tag which is used to wrap a sequence of elements which you would like to be reorderable. The
 * tag expecs a sequence of reorderable elements contained in a "container" element. Each child of
 * the container needs a unique reorderableId as a data attribute. When this code reorders the
 * elements, all it does is fire an event with the new sequence in the event payload. It is up to
 * the user of this tag to do something with that new sequence. Typically, the caller would be a lit
 * component, and it would reorder its data and trigger a re-render.
 *
 * let reorderedEvent = new CustomEvent("reordered"
 *          , { detail: {reorderedIds: [ "item2", "item1", .. ], data: draggedElement.dataset } });
 *
 * As you can see from that example, the reordered event has the dataset of the dragged element as a
 * part of its payload. This lets the caller attach data to help it make sense of the reordered
 * elements.
 *
 * This tag also takes care of keyboard navigation, using the 'e' and 'd' keys to move up/down or
 * left/right.
 *
 * @element sakai-reorderer
 * @property {boolean} horizontal - Indicates that ordering goes from left to right, not top to
 *                                  bottom
 *
 * @extends LitElement
 * @see {@link https://lit.dev/docs/v1/api/lit-element/LitElement/}
 */
export class SakaiReorderer extends LitElement {

  static properties = { horizontal: { type: Boolean } };

  constructor() {

    super();

    this._dragStartListener = e => {

      e.stopPropagation();

      this.draggingElement = e.target;
      e.target.classList.add("dragging");
    };

    this._dragOverListener = e => {

      e.stopPropagation();

      if (this.draggingElement && this.container.contains(this.draggingElement)) {
        e.preventDefault();
      }
    };

    this._dragEndListener = e => {

      e.preventDefault();
      e.stopPropagation();
      this.draggingElement = undefined;
      e.target.classList.remove("dragging");
    };

    this._dropListener = e => {

      e.stopPropagation();

      const afterElement = this._getDragAfterElement(this.container, this.horizontal ? e.clientX : e.clientY);

      const total = this._reorderableIds.length;

      const draggingIndex = this._reorderableIds.findIndex(id => id === this.draggingElement.dataset.reorderableId);
      const afterIndex = afterElement ? this._reorderableIds.findIndex(id => id === afterElement.dataset.reorderableId) : this._reorderableIds.length - 1;
      this._reorderableIds.splice(draggingIndex, 1);

      if (afterIndex === 0) {
        this._reorderableIds.unshift(this.draggingElement.dataset.reorderableId);
      } else if (afterIndex === total - 1) {
        this._reorderableIds.push(this.draggingElement.dataset.reorderableId);
      } else {
        this._reorderableIds.splice(afterIndex, 0, this.draggingElement.dataset.reorderableId);
      }

      this.dispatchEvent(new CustomEvent("reordered", { detail: { reorderedIds: this._reorderableIds, data: this.draggingElement.dataset } }));
    };

    this._dragEnterListener = e => e.stopPropagation();

    this._dragLeaveListener = e => e.stopPropagation();

    this._dragListener = e => e.stopPropagation();

    this._keyupListener = e => {

      e.stopPropagation();

      const reorderable = e.target.closest("[draggable='true']");
      const reorderableId = reorderable?.dataset.reorderableId;

      if ([ "e", "d" ].includes(e.key.toLowerCase())) {
        const index = this._reorderableIds.indexOf(reorderableId);

        let changed = false;

        if (e.key.toLowerCase() === "e") {
          if (reorderable.previousElementSibling) {
            this._reorderableIds.splice(index, 1);
            this._reorderableIds.splice(index - 1, 0, reorderableId);
            changed = true;
          }
        } else if (e.key.toLowerCase() === "d") {
          if (reorderable.nextElementSibling) {
            this._reorderableIds.splice(index, 1);
            this._reorderableIds.splice(index + 1, 0, reorderableId);
            changed = true;
          }
        }

        if (changed) {
          this.dispatchEvent(new CustomEvent("reordered", { detail: { reorderedIds: this._reorderableIds, data: reorderable.dataset } }));
        }
      }
    };
  }

  /**
   * @private
   */
  _getDragAfterElement(container, coord) {

    return [ ...container.querySelectorAll("[draggable='true']:not(.dragging)") ]
      .reduce((closest, child) => {

        const box = child.getBoundingClientRect();
        const offset = coord - (this.horizontal ? box.left : box.top) - (this.horizontal ? box.width : box.height) / 2;
        if (offset < 0 && offset > closest.offset) {
          return { offset, element: child };
        }
        return closest;
      }, { offset: Number.NEGATIVE_INFINITY }).element;
  }

  /**
   * @private
   */
  _setupKeyboard(reorderable) {
    reorderable.querySelector(".drag-handle")?.addEventListener("keyup", this._keyupListener);
  }

  /**
   * @private
   * @override
   */
  updated() {

    this.container = this.shadowRoot.querySelector("slot").assignedNodes().find(n => n.nodeType === Node.ELEMENT_NODE);

    this._reorderableIds = [];

    [ ...this.container.children ].filter(n => n.nodeType === Node.ELEMENT_NODE).forEach(reorderable => {

      this._setupKeyboard(reorderable);

      this._reorderableIds.push(reorderable.dataset.reorderableId);

      !reorderable.hasAttribute("draggable") && reorderable.setAttribute("draggable", "true");

      let dragHandle = reorderable.querySelector(".drag-handle");

      if (!dragHandle && reorderable.getAttribute("draggable") === "true") {
        dragHandle = document.createElement("span");
        dragHandle.classList.add("si", "si-drag-handle", "drag-handle");
        dragHandle.style.cursor = "grab;";
        reorderable.insertBefore(dragHandle, reorderable.firstChild);
      }

      reorderable.addEventListener("dragstart", this._dragStartListener);
      reorderable.addEventListener("drag", this._dragListener);
      reorderable.addEventListener("dragend", this._dragEndListener);
    });

    this.container.addEventListener("dragenter", this._dragEnterListener);
    this.container.addEventListener("dragover", this._dragOverListener);
    this.container.addEventListener("dragleave", this._dragLeaveListener);
    this.container.addEventListener("drop", this._dropListener);
  }

  /**
   * @private
   * @override
   */
  render() {

    return html`
      <slot>
      </slot>
    `;
  }
}
