(() => {
  let draggedItem = null;

  const itemSelector = ".reorder-item";
  const containerSelector = ".reorderItemsContainer";

  const getDirectItems = container => [...container.children].filter(child => child.matches(itemSelector));

  // Sections can nest reorder containers, so only leaf containers and the delete bin are drop targets.
  const findDropContainers = () => [...document.querySelectorAll(containerSelector)].filter(container =>
    container.id === "deleteListHead"
      || getDirectItems(container).length > 0
      || !container.querySelector(`:scope > ${containerSelector}`));

  const removeMarkerItems = () => {
    document.querySelectorAll(`${itemSelector} .marker`).forEach(marker => {
      marker.closest(itemSelector)?.remove();
    });
  };

  const getSequenceText = item => item.querySelector(".reorderSeq")?.textContent.trim() ?? "";

  const getOrder = selector => [...document.querySelectorAll(selector)]
    .map(getSequenceText)
    .filter(Boolean)
    .join(" ");

  const recalculate = () => {
    removeMarkerItems();

    const keepList = getOrder(`.col1 ${itemSelector}`);
    const removeList = getOrder(`.col2 ${itemSelector}`);
    const order = document.getElementById("order");
    if (order) {
      order.value = `${keepList} --- ${removeList}`.replaceAll("  ", " ").trim();
    }

    const deleteListHead = document.getElementById("deleteListHead");
    if (deleteListHead) {
      const className = document.querySelectorAll(`.col2 ${itemSelector}`).length === 0
        ? "deleteListMessageEmpty panel-heading"
        : "deleteListMessage panel-heading";
      deleteListHead.className = `${className} reorderItemsContainer`;
    }
  };

  const setupItems = () => {
    document.querySelectorAll(itemSelector).forEach(item => {
      item.draggable = true;
      item.tabIndex = 0;
    });
  };

  const getDragAfterElement = (container, y) => {
    return getDirectItems(container)
      .filter(item => item !== draggedItem)
      .reduce((closest, child) => {
        const box = child.getBoundingClientRect();
        const offset = y - box.top - box.height / 2;

        if (offset < 0 && offset > closest.offset) {
          return { offset, element: child };
        }

        return closest;
      }, { offset: Number.NEGATIVE_INFINITY, element: null }).element;
  };

  const moveItemToContainer = (item, container, before = null, options = {}) => {
    if (!item || !container || item === before) {
      return;
    }

    container.insertBefore(item, before);
    if (options.recalculate !== false) {
      recalculate();
    }
    if (options.focus !== false) {
      item.focus();
    }
  };

  const moveItemByOffset = (item, offset) => {
    const siblings = getDirectItems(item.parentElement);
    const index = siblings.indexOf(item);
    const target = siblings[index + offset];

    if (!target) {
      return;
    }

    if (offset < 0) {
      item.parentElement.insertBefore(item, target);
    } else {
      item.parentElement.insertBefore(item, siblings[index + 2] ?? null);
    }

    recalculate();
    item.focus();
  };

  const findPrimaryContainer = () => {
    return [...document.querySelectorAll(`#reorderCol1 ${containerSelector}`)]
      .find(container => !container.querySelector(`:scope > ${containerSelector}`));
  };

  const moveItemBetweenBins = item => {
    const currentColumn = item.closest(".reorder-column");
    const targetContainer = currentColumn?.classList.contains("col2")
      ? findPrimaryContainer()
      : document.getElementById("deleteListHead");

    moveItemToContainer(item, targetContainer);
  };

  const handleItemKeydown = event => {
    const item = event.target.closest(itemSelector);
    if (!item || !event.ctrlKey) {
      return;
    }

    if (event.shiftKey && event.key.startsWith("Arrow")) {
      event.preventDefault();
      moveItemBetweenBins(item);
      return;
    }

    if (event.key === "ArrowUp") {
      event.preventDefault();
      moveItemByOffset(item, -1);
    } else if (event.key === "ArrowDown") {
      event.preventDefault();
      moveItemByOffset(item, 1);
    }
  };

  const setupDragAndDrop = () => {
    const dropContainers = findDropContainers();

    document.addEventListener("dragstart", event => {
      const item = event.target.closest(itemSelector);
      if (!item) {
        return;
      }

      draggedItem = item;
      item.classList.add("reorder-item-dragging");
      event.dataTransfer.effectAllowed = "move";
      event.dataTransfer.setData("text/plain", getSequenceText(item));
    });

    document.addEventListener("dragend", () => {
      draggedItem?.classList.remove("reorder-item-dragging");
      draggedItem = null;
      recalculate();
    });

    dropContainers.forEach(container => {
      container.addEventListener("dragover", event => {
        if (!draggedItem) {
          return;
        }

        event.preventDefault();
        event.dataTransfer.dropEffect = "move";
        moveItemToContainer(draggedItem, container, getDragAfterElement(container, event.clientY), {
          focus: false,
          recalculate: false
        });
      });

      container.addEventListener("drop", event => {
        event.preventDefault();
        recalculate();
      });
    });
  };

  const moveItemToDeleteBin = link => {
    const item = link.closest(itemSelector);
    const deleteListHead = document.getElementById("deleteListHead");
    if (!item || !deleteListHead) {
      return;
    }

    item.classList.add("highlightEl");
    moveItemToContainer(item, deleteListHead);
    window.setTimeout(() => item.classList.remove("highlightEl"), 200);
  };

  const setupDeleteControls = () => {
    document.querySelectorAll(".deleteAnswerTrashLink, .deleteAnswerLink").forEach(link => {
      link.addEventListener("click", event => {
        event.preventDefault();
        moveItemToDeleteBin(link);
      });
    });

    document.querySelectorAll(".deleteAllLink").forEach(link => {
      link.addEventListener("click", event => {
        event.preventDefault();
        link.closest(".section-container")
          ?.querySelectorAll("a.deleteAnswerTrashLink")
          .forEach(deleteLink => moveItemToDeleteBin(deleteLink));
      });
    });
  };

  document.addEventListener("DOMContentLoaded", () => {
    removeMarkerItems();
    setupItems();
    setupDragAndDrop();
    setupDeleteControls();
    document.addEventListener("keydown", handleItemKeydown);
    document.getElementById("save")?.addEventListener("click", recalculate);
    recalculate();
  });
})();
