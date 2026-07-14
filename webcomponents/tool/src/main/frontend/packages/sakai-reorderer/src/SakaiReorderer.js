import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { html } from "lit";

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
 * the container needs a unique reorderableId as a data attribute. By default, when this code
 * reorders the elements, it fires an event with the new sequence in the event payload. It is up to
 * the user of this tag to do something with that new sequence. Typically, the caller would be a Lit
 * component, and it would reorder its data and trigger a re-render. Set `apply-dom` for
 * server-rendered pages that need the component to reorder light-DOM children before firing the
 * event.
 *
 * let reorderedEvent = new CustomEvent("reordered"
 *          , { detail: {reorderedIds: [ "item2", "item1", .. ], data: draggedElement.dataset } });
 *
 * As you can see from that example, the reordered event has the dataset of the dragged element as a
 * part of its payload. This lets the caller attach data to help it make sense of the reordered
 * elements.
 *
 * This tag also takes care of keyboard navigation, using the 'e' and 'd' keys to move
 * up/down or left/right.
 *
 * @element sakai-reorderer
 * @property {boolean} horizontal - Indicates that ordering goes from left to right, not top to
 *                                  bottom
 * @property {boolean} applyDom - Indicates that the component should reorder the light-DOM
 *                                 children before firing the event
 *
 * @extends SakaiShadowElement
 * @see {@link https://lit.dev/docs/v1/api/lit-element/LitElement/}
 */
export class SakaiReorderer extends SakaiShadowElement {

  static properties = {
    applyDom: { attribute: "apply-dom", type: Boolean },
    horizontal: { type: Boolean }
  };

  constructor() {

    super();

    this._configuredContainers = new WeakSet();
    this._configuredReorderables = new WeakSet();

    this._dragStartListener = e => {

      e.stopPropagation();

      if (this._dragSource !== e.currentTarget) {
        e.preventDefault();
        return;
      }

      this.draggingElement = e.currentTarget;
      this.draggingElement.classList.add("dragging");
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
      this._dragSource = undefined;
      e.currentTarget.classList.remove("dragging");
    };

    this._pointerEndListener = () => this._dragSource = undefined;

    this._dropListener = e => {

      e.stopPropagation();

      if (!this.draggingElement || !this.draggingElement.dataset.reorderableId) {
        return;
      }

      const afterElement = this._getDragAfterElement(this.container, this.horizontal ? e.clientX : e.clientY);

      const draggingIndex = this._reorderableIds.findIndex(id => id === this.draggingElement.dataset.reorderableId);

      if (draggingIndex === -1) {
        return;
      }

      const previousReorderedIds = [ ...this._reorderableIds ];
      this._reorderableIds.splice(draggingIndex, 1);

      if (!afterElement) {
        this._reorderableIds.push(this.draggingElement.dataset.reorderableId);
      } else {
        const afterIndex = this._reorderableIds.findIndex(id => id === afterElement.dataset.reorderableId);
        if (afterIndex === -1) {
          this._reorderableIds.push(this.draggingElement.dataset.reorderableId);
        } else {
          this._reorderableIds.splice(afterIndex, 0, this.draggingElement.dataset.reorderableId);
        }
      }

      this._dispatchReordered(this.draggingElement.dataset, previousReorderedIds);
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
        const previousReorderedIds = [ ...this._reorderableIds ];

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
          this._dispatchReordered(reorderable.dataset, previousReorderedIds);
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
  _dispatchReordered(data, previousReorderedIds) {

    if (this.applyDom) {
      const reorderablesById = new Map([ ...this.container.children ].map(reorderable => [ reorderable.dataset.reorderableId, reorderable ]));
      this._reorderableIds.forEach(id => {
        const reorderable = reorderablesById.get(id);
        if (reorderable) {
          this.container.append(reorderable);
        }
      });
    }

    this.dispatchEvent(new CustomEvent("reordered", {
      bubbles: true,
      composed: true,
      detail: {
        data,
        previousReorderedIds,
        reorderedIds: [ ...this._reorderableIds ]
      }
    }));
  }

  /**
   * @private
   */
  _setupKeyboard(reorderable) {

    const dragHandle = reorderable.querySelector(".drag-handle");
    dragHandle?.addEventListener("pointerdown", () => this._dragSource = reorderable);
    dragHandle?.addEventListener("pointerup", this._pointerEndListener);
    dragHandle?.addEventListener("pointercancel", this._pointerEndListener);
    dragHandle?.addEventListener("keyup", this._keyupListener);
  }

  /**
   * Synchronise the component with changes made outside the component to its light-DOM children.
   *
   * Call this after externally reordering, adding, removing, or changing the reorderable ID of a
   * child. The component maintains its own order for keyboard and drag-and-drop operations, so
   * callers using `apply-dom` only need this for separate actions such as undo or a number input.
   */
  refresh() {

    this._syncReorderables();
  }

  /**
   * @private
   */
  _syncReorderables() {

    if (!this.container) {
      return;
    }

    this._reorderableIds = [];

    [ ...this.container.children ].filter(n => n.nodeType === Node.ELEMENT_NODE).forEach(reorderable => {

      this._reorderableIds.push(reorderable.dataset.reorderableId);

      if (!reorderable.hasAttribute("draggable")) {
        reorderable.setAttribute("draggable", "true");
      }

      let dragHandle = reorderable.querySelector(".drag-handle");

      if (!dragHandle && reorderable.getAttribute("draggable") === "true") {
        dragHandle = document.createElement("span");
        dragHandle.classList.add("si", "si-drag-handle", "drag-handle");
        dragHandle.style.cursor = "grab";
        reorderable.insertBefore(dragHandle, reorderable.firstChild);
      }

      if (!this._configuredReorderables.has(reorderable)) {
        this._setupKeyboard(reorderable);
        reorderable.addEventListener("dragstart", this._dragStartListener);
        reorderable.addEventListener("drag", this._dragListener);
        reorderable.addEventListener("dragend", this._dragEndListener);
        this._configuredReorderables.add(reorderable);
      }
    });
  }

  /**
   * @private
   * @override
   */
  updated() {

    this.container = this.shadowRoot.querySelector("slot").assignedNodes().find(n => n.nodeType === Node.ELEMENT_NODE);

    this._syncReorderables();

    if (this.container && !this._configuredContainers.has(this.container)) {
      this.container.addEventListener("dragenter", this._dragEnterListener);
      this.container.addEventListener("dragover", this._dragOverListener);
      this.container.addEventListener("dragleave", this._dragLeaveListener);
      this.container.addEventListener("drop", this._dropListener);
      this._configuredContainers.add(this.container);
    }
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
