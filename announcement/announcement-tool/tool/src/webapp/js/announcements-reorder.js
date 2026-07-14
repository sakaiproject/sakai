(function ($) {

  const initialise = () => {
    const list = document.getElementById("reorder-list");
    if (!list || !$.fn.sortable) {
      return;
    }

    const undoLast = document.getElementById("undo-last");
    const undoLastInactive = document.getElementById("undo-last-inact");
    const undoAll = document.getElementById("undo-all");
    const undoAllInactive = document.getElementById("undo-all-inact");
    const lastMoveArray = document.getElementById("lastMoveArray");
    const lastItemMoved = document.getElementById("lastItemMoved");

    const items = () => Array.from(list.querySelectorAll(":scope > .reorder-element"));
    const order = () => items().map(item => item.id);
    const restore = itemOrder => {
      itemOrder.forEach(id => list.append(document.getElementById(id)));
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

    const savePreviousOrder = () => {
      previousOrder = order();
      lastMoveArray.textContent = previousOrder.join(" ");
    };

    document.getElementById("lastMoveArrayInit").textContent = initialOrder.join(" ");
    lastMoveArray.textContent = initialOrder.join(" ");

    $(list).sortable({
      axis: "y",
      items: "> .reorder-element",
      handle: ".grabHandle",
      cancel: "input,textarea,select,option",
      start: savePreviousOrder,
      update: (_, ui) => updateOrder(ui.item[0]),
    });

    list.addEventListener("change", event => {
      const input = event.target.closest("input[id^='index']");
      if (!input || !list.contains(input)) {
        return;
      }

      const newPosition = Number.parseInt(input.value, 10);
      const reorderItems = items();

      if (!Number.isInteger(newPosition) || newPosition < 1 || newPosition > reorderItems.length) {
        input.value = reorderItems.indexOf(input.closest(".reorder-element")) + 1;
        return;
      }

      savePreviousOrder();
      const movedItem = input.closest(".reorder-element");
      const currentPosition = reorderItems.indexOf(movedItem) + 1;
      if (newPosition > currentPosition) {
        reorderItems[newPosition - 1].after(movedItem);
      } else {
        reorderItems[newPosition - 1].before(movedItem);
      }
      updateOrder(movedItem);
    });

    list.addEventListener("keydown", event => {
      if (event.target.matches("input,textarea,select,option")) {
        return;
      }

      const movedItem = event.target.closest(".reorder-element");
      if (!movedItem) {
        return;
      }

      const previousItem = movedItem.previousElementSibling;
      const nextItem = movedItem.nextElementSibling;
      if (event.key.toLowerCase() === "u" && previousItem && previousItem.classList.contains("reorder-element")) {
        savePreviousOrder();
        previousItem.before(movedItem);
      } else if (event.key.toLowerCase() === "d" && nextItem && nextItem.classList.contains("reorder-element")) {
        savePreviousOrder();
        nextItem.after(movedItem);
      } else {
        return;
      }

      event.preventDefault();
      updateOrder(movedItem);
      movedItem.focus();
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

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initialise);
  } else {
    initialise();
  }
}(jQuery));
