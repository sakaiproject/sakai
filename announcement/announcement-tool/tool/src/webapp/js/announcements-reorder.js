(() => {

const initialise = () => {

  const reorderer = document.getElementById("announcements-reorderer");
  const list = document.getElementById("reorder-list");
  if (!reorderer || !list) {
    return;
  }

  const undoLast = document.getElementById("undo-last");
  const undoAll = document.getElementById("undo-all");
  const lastMoveArray = document.getElementById("lastMoveArray");
  const lastItemMoved = document.getElementById("lastItemMoved");

  const items = () => Array.from(list.querySelectorAll(":scope > .reorder-element"));
  const order = () => items().map(item => item.dataset.reorderableId);
  const restore = itemOrder => {
    const itemsById = new Map(items().map(item => [ item.dataset.reorderableId, item ]));
    itemOrder.forEach(id => {
      const item = itemsById.get(id);
      if (item) {
        list.append(item);
      }
    });
    reorderer.refresh();
  };

  const initialOrder = order();
  let previousOrder = initialOrder;

  const updateMoveButtons = () => {
    const reorderItems = items();
    reorderItems.forEach((item, index) => {
      const moveUp = item.querySelector('[data-action="move-up"]');
      const moveDown = item.querySelector('[data-action="move-down"]');
      if (moveUp) {
        moveUp.disabled = index === 0;
      }
      if (moveDown) {
        moveDown.disabled = index === reorderItems.length - 1;
      }
    });
  };

  const enableUndo = () => {
    if (undoLast) {
      undoLast.disabled = false;
    }
    if (undoAll) {
      undoAll.disabled = false;
    }
  };

  const updateOrder = movedItem => {
    updateMoveButtons();
    enableUndo();

    const reorderedItems = items();
    if (lastItemMoved?.dataset.movedMessage && movedItem) {
      lastItemMoved.textContent = lastItemMoved.dataset.movedMessage
        .replace("{0}", reorderedItems.indexOf(movedItem) + 1)
        .replace("{1}", reorderedItems.length);
    }
  };

  const savePreviousOrder = itemOrder => {
    previousOrder = itemOrder || order();
    if (lastMoveArray) {
      lastMoveArray.textContent = previousOrder.join(" ");
    }
  };

  const lastMoveArrayInit = document.getElementById("lastMoveArrayInit");
  if (lastMoveArrayInit) {
    lastMoveArrayInit.textContent = initialOrder.join(" ");
  }
  if (lastMoveArray) {
    lastMoveArray.textContent = initialOrder.join(" ");
  }

  updateMoveButtons();

  reorderer.addEventListener("reordered", event => {
    savePreviousOrder(event.detail.previousReorderedIds);
    const movedItem = items().find(item => item.dataset.reorderableId === event.detail.data.reorderableId);
    updateOrder(movedItem);
  });

  list.addEventListener("click", event => {
    const button = event.target.closest("button[data-action]");
    if (!button || !list.contains(button)) {
      return;
    }

    const row = button.closest(".reorder-element");
    if (!row) {
      return;
    }

    if (button.dataset.action === "move-up") {
      const previous = row.previousElementSibling;
      if (!previous) {
        return;
      }
      savePreviousOrder();
      list.insertBefore(row, previous);
      reorderer.refresh();
      updateOrder(row);
    } else if (button.dataset.action === "move-down") {
      const next = row.nextElementSibling;
      if (!next) {
        return;
      }
      savePreviousOrder();
      list.insertBefore(next, row);
      reorderer.refresh();
      updateOrder(row);
    }
  });

  undoLast?.addEventListener("click", event => {
    event.preventDefault();
    restore(previousOrder);
    updateOrder(items()[0]);
  });

  undoAll?.addEventListener("click", event => {
    event.preventDefault();
    restore(initialOrder);
    updateOrder(items()[0]);
  });
};

customElements.whenDefined("sakai-reorderer").then(initialise);

})();
