(() => {

const initialise = () => {

  const reorderer = document.getElementById("announcements-reorderer");
  const list = document.getElementById("reorder-list");
  if (!reorderer || !list) {
    return;
  }

  const undoLast = document.getElementById("undo-last");
  const undoLastInactive = document.getElementById("undo-last-inact");
  const undoAll = document.getElementById("undo-all");
  const undoAllInactive = document.getElementById("undo-all-inact");
  const lastMoveArray = document.getElementById("lastMoveArray");
  const lastItemMoved = document.getElementById("lastItemMoved");

  const items = () => Array.from(list.querySelectorAll(":scope > .reorder-element"));
  const order = () => items().map(item => item.dataset.reorderableId);
  const restore = itemOrder => {
    const itemsById = new Map(items().map(item => [ item.dataset.reorderableId, item ]));
    itemOrder.forEach(id => list.append(itemsById.get(id)));
    reorderer.refresh();
  };

  const initialOrder = order();
  let previousOrder = initialOrder;

  const updateOrder = movedItem => {
    const reorderedItems = items();
    reorderedItems.forEach((item, index) => {
      const position = index + 1;
      item.querySelectorAll("input[id^='index'], input[id^='holder']").forEach(input => {
        input.value = position;
        input.setAttribute("value", position);
      });
    });

    lastItemMoved.textContent = lastItemMoved.dataset.movedMessage
      .replace("{0}", reorderedItems.indexOf(movedItem) + 1)
      .replace("{1}", reorderedItems.length);
    undoLast.style.display = "";
    undoLastInactive.style.display = "none";
    undoAll.style.display = "";
    undoAllInactive.style.display = "none";
  };

  const savePreviousOrder = itemOrder => {
    previousOrder = itemOrder || order();
    lastMoveArray.textContent = previousOrder.join(" ");
  };

  document.getElementById("lastMoveArrayInit").textContent = initialOrder.join(" ");
  lastMoveArray.textContent = initialOrder.join(" ");

  reorderer.addEventListener("reordered", event => {
    savePreviousOrder(event.detail.previousReorderedIds);
    const movedItem = items().find(item => item.dataset.reorderableId === event.detail.data.reorderableId);
    updateOrder(movedItem);
  });

  list.addEventListener("change", event => {
    const input = event.target.closest("input[id^='index']");
    if (!input || !list.contains(input)) {
      return;
    }

    const newPosition = Number.parseInt(input.value, 10);
    const reorderItems = items();
    const movedItem = input.closest(".reorder-element");
    const currentPosition = reorderItems.indexOf(movedItem) + 1;
    if (!Number.isInteger(newPosition) || newPosition < 1 || newPosition > reorderItems.length) {
      input.value = currentPosition;
      return;
    }

    if (newPosition === currentPosition) {
      return;
    }

    savePreviousOrder();
    if (newPosition > currentPosition) {
      reorderItems[newPosition - 1].after(movedItem);
    } else {
      reorderItems[newPosition - 1].before(movedItem);
    }
    reorderer.refresh();
    updateOrder(movedItem);
  });

  undoLast.addEventListener("click", event => {
    event.preventDefault();
    restore(previousOrder);
    updateOrder(items()[0]);
  });

  undoAll.addEventListener("click", event => {
    event.preventDefault();
    restore(initialOrder);
    updateOrder(items()[0]);
  });
};

customElements.whenDefined("sakai-reorderer").then(initialise);

})();
