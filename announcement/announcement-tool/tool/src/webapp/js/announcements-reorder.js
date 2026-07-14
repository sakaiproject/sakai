(function ($) {

  $(function () {

    const list = $("#reorder-list");
    if (!list.length || !$.fn.sortable) {
      return;
    }

    const items = () => list.children(".reorder-element");
    const order = () => items().map((_, item) => item.id).get();
    const restore = itemOrder => {
      itemOrder.forEach(id => list.append(document.getElementById(id)));
    };

    const initialOrder = order();
    let previousOrder = initialOrder;

    const updateOrder = movedItem => {
      items().each((index, item) => {
        $(item).find("input[id^='index'], input[id^='holder']").val(index + 1).attr("value", index + 1);
      });

      $("#lastItemMoved").text(movedItem.attr("id"));
      $("#undo-last").show();
      $("#undo-last-inact").hide();
      $("#undo-all").show();
      $("#undo-all-inact").hide();
    };

    const savePreviousOrder = () => {
      previousOrder = order();
      $("#lastMoveArray").text(previousOrder.join(" "));
    };

    $("#lastMoveArrayInit").text(initialOrder.join(" "));
    $("#lastMoveArray").text(initialOrder.join(" "));

    list.sortable({
      axis: "y",
      items: "> .reorder-element",
      handle: ".grabHandle",
      cancel: "input,textarea,select,option",
      start: savePreviousOrder,
      update: (_, ui) => updateOrder(ui.item),
    });

    list.on("change", "input[id^='index']", function () {
      const newPosition = Number.parseInt(this.value, 10);
      const reorderItems = items();

      if (!Number.isInteger(newPosition) || newPosition < 1 || newPosition > reorderItems.length) {
        this.value = $(this).closest(".reorder-element").index() + 1;
        return;
      }

      savePreviousOrder();
      const movedItem = $(this).closest(".reorder-element");
      const currentPosition = movedItem.index() + 1;
      if (newPosition > currentPosition) {
        movedItem.insertAfter(reorderItems.eq(newPosition - 1));
      } else {
        movedItem.insertBefore(reorderItems.eq(newPosition - 1));
      }
      updateOrder(movedItem);
    });

    list.on("keydown", ".reorder-element", function (event) {
      if ($(event.target).is(":input")) {
        return;
      }

      const movedItem = $(this);
      if (event.key.toLowerCase() === "u" && movedItem.prev(".reorder-element").length) {
        savePreviousOrder();
        movedItem.insertBefore(movedItem.prev(".reorder-element"));
      } else if (event.key.toLowerCase() === "d" && movedItem.next(".reorder-element").length) {
        savePreviousOrder();
        movedItem.insertAfter(movedItem.next(".reorder-element"));
      } else {
        return;
      }

      event.preventDefault();
      updateOrder(movedItem);
      movedItem.trigger("focus");
    });

    $("#undo-last").on("click", event => {
      event.preventDefault();
      restore(previousOrder);
      updateOrder(items().first());
    });

    $("#undo-all").on("click", event => {
      event.preventDefault();
      restore(initialOrder);
      updateOrder(items().first());
    });
  });
}(jQuery));
