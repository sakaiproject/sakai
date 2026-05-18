(() => {
  const app = document.getElementById("tool-order-app");
  const list = document.getElementById("tool-order-list");
  const alert = document.getElementById("tool-order-alert");

  if (!app) {
    return;
  }

  const reorderAllowed = list?.dataset.reorderAllowed === "true";
  let draggedRow = null;
  let dragStartOrder = "";
  let savingOrder = false;

  const jsonRequest = async (url, method = "POST", body = null) => {
    const options = {
      method,
      credentials: "same-origin",
      headers: { "Accept": "application/json" },
    };

    if (body !== null) {
      options.headers["Content-Type"] = "application/json";
      options.body = JSON.stringify(body);
    }

    const response = await fetch(url, options);
    const payload = await response.json();
    if (!response.ok || payload.success !== true) {
      throw new Error(payload.message || app.dataset.errorMessage);
    }
    return payload;
  };

  const showMessage = (message, success = true) => {
    if (!alert) {
      return;
    }
    alert.classList.toggle("sak-banner-info", success);
    alert.classList.toggle("sak-banner-error", !success);
    alert.textContent = message;
  };

  const setBusy = (busy) => {
    app.toggleAttribute("aria-busy", busy);
    app.classList.toggle("tool-order-busy", busy);
  };

  const getRows = () => Array.from(list?.querySelectorAll(".tool-order-row") || []);

  const getOrder = () => getRows().map(row => row.dataset.pageId);

  const restoreOrder = (order) => {
    const rowsById = new Map(getRows().map(row => [row.dataset.pageId, row]));
    order.forEach(pageId => {
      const row = rowsById.get(pageId);
      if (row) {
        list.append(row);
      }
    });
    updateMoveButtons();
  };

  const updateMoveButtons = () => {
    const rows = getRows();
    rows.forEach((row, index) => {
      const up = row.querySelector('[data-action="move-up"]');
      const down = row.querySelector('[data-action="move-down"]');
      if (up) {
        up.disabled = index === 0;
      }
      if (down) {
        down.disabled = index === rows.length - 1;
      }
    });
  };

  const persistOrder = async (previousOrder) => {
    if (!reorderAllowed || savingOrder) {
      return;
    }

    savingOrder = true;
    try {
      setBusy(true);
      showMessage(app.dataset.savingMessage);
      const payload = await jsonRequest(app.dataset.orderUrl, "POST", { pageIds: getOrder() });
      showMessage(payload.message || app.dataset.savedMessage);
      updateMoveButtons();
    } catch (error) {
      if (previousOrder) {
        restoreOrder(previousOrder);
      }
      showMessage(error.message, false);
    } finally {
      savingOrder = false;
      setBusy(false);
    }
  };

  const showEdit = (row) => {
    const form = row.querySelector(".tool-order-edit-form");
    const input = form?.querySelector('input[name="title"]');
    if (!form || !input) {
      return;
    }
    form.classList.remove("d-none");
    input.focus();
    input.select();
  };

  const cancelEdit = (row) => {
    const form = row.querySelector(".tool-order-edit-form");
    if (!form) {
      return;
    }

    const titleInput = form.querySelector('input[name="title"]');
    if (titleInput) {
      titleInput.value = row.dataset.title || "";
    }
    form.classList.add("d-none");
  };

  const updateRow = (row, data) => {
    row.dataset.title = data.title;
    row.dataset.removeConfirm = app.dataset.removeConfirmTemplate.replace("__TITLE__", data.title);

    const title = row.querySelector(".tool-order-title");
    if (title) {
      title.textContent = data.title;
    }

    const titleInput = row.querySelector('input[name="title"]');
    if (titleInput) {
      titleInput.value = data.title;
    }

    const iframeInput = row.querySelector('input[name="iframeSource"]');
    if (iframeInput) {
      iframeInput.value = data.iframeSource || "";
    }

    row.querySelector(".tool-order-hidden-badge")?.classList.toggle("d-none", !data.hidden);
    row.querySelector(".tool-order-locked-badge")?.classList.toggle("d-none", !data.locked);

    const visibilityButton = row.querySelector('[data-action="visibility"]');
    if (visibilityButton) {
      visibilityButton.disabled = !data.enabled;
      visibilityButton.dataset.nextVisible = String(!data.visible || !data.enabled);
      visibilityButton.title = data.visible ? app.dataset.menuHide : app.dataset.menuShow;
      visibilityButton.setAttribute("aria-label", `${visibilityButton.title}: ${data.title}`);
      const icon = visibilityButton.querySelector(".bi");
      if (icon) {
        icon.className = data.visible ? "bi bi-eye-slash" : "bi bi-eye";
      }
    }

    const accessButton = row.querySelector('[data-action="access"]');
    if (accessButton) {
      accessButton.dataset.nextEnabled = String(!data.enabled);
      accessButton.title = data.enabled ? app.dataset.menuLock : app.dataset.menuUnlock;
      accessButton.setAttribute("aria-label", `${accessButton.title}: ${data.title}`);
      const icon = accessButton.querySelector(".bi");
      if (icon) {
        icon.className = data.enabled ? "bi bi-lock" : "bi bi-unlock";
      }
    }
  };

  const saveTitle = async (row, form) => {
    const body = {
      title: form.querySelector('input[name="title"]')?.value || "",
      iframeSource: form.querySelector('input[name="iframeSource"]')?.value,
    };

    try {
      setBusy(true);
      showMessage(app.dataset.savingMessage);
      const payload = await jsonRequest(row.dataset.titleUrl, "POST", body);
      updateRow(row, payload.row);
      form.classList.add("d-none");
      showMessage(payload.message || app.dataset.savedMessage);
    } catch (error) {
      showMessage(error.message, false);
    } finally {
      setBusy(false);
    }
  };

  const updateVisibility = async (row, button) => {
    try {
      setBusy(true);
      showMessage(app.dataset.savingMessage);
      const payload = await jsonRequest(row.dataset.visibilityUrl, "POST", {
        visible: button.dataset.nextVisible === "true",
      });
      updateRow(row, payload.row);
      showMessage(payload.message || app.dataset.savedMessage);
    } catch (error) {
      showMessage(error.message, false);
    } finally {
      setBusy(false);
    }
  };

  const updateAccess = async (row, button) => {
    try {
      setBusy(true);
      showMessage(app.dataset.savingMessage);
      const payload = await jsonRequest(row.dataset.accessUrl, "POST", {
        enabled: button.dataset.nextEnabled === "true",
      });
      updateRow(row, payload.row);
      showMessage(payload.message || app.dataset.savedMessage);
    } catch (error) {
      showMessage(error.message, false);
    } finally {
      setBusy(false);
    }
  };

  const deleteRow = async (row) => {
    if (!confirm(row.dataset.removeConfirm)) {
      return;
    }

    try {
      setBusy(true);
      showMessage(app.dataset.savingMessage);
      const payload = await jsonRequest(row.dataset.deleteUrl);
      row.remove();
      updateMoveButtons();
      showMessage(payload.message || app.dataset.savedMessage);
    } catch (error) {
      showMessage(error.message, false);
    } finally {
      setBusy(false);
    }
  };

  const resetOrder = async (button) => {
    if (!confirm(button.dataset.confirm)) {
      return;
    }

    try {
      setBusy(true);
      showMessage(app.dataset.savingMessage);
      await jsonRequest(app.dataset.resetUrl, "POST");
      window.location.reload();
    } catch (error) {
      showMessage(error.message, false);
      setBusy(false);
    }
  };

  app.addEventListener("click", event => {
    const button = event.target.closest("button[data-action]");
    if (!button) {
      return;
    }

    const row = button.closest(".tool-order-row");
    switch (button.dataset.action) {
      case "move-up": {
        if (savingOrder) {
          break;
        }
        const previousOrder = getOrder();
        const previous = row?.previousElementSibling;
        if (row && previous) {
          list.insertBefore(row, previous);
          updateMoveButtons();
          persistOrder(previousOrder);
        }
        break;
      }
      case "move-down": {
        if (savingOrder) {
          break;
        }
        const previousOrder = getOrder();
        const next = row?.nextElementSibling;
        if (row && next) {
          list.insertBefore(next, row);
          updateMoveButtons();
          persistOrder(previousOrder);
        }
        break;
      }
      case "edit":
        showEdit(row);
        break;
      case "cancel-edit":
        cancelEdit(row);
        break;
      case "visibility":
        updateVisibility(row, button);
        break;
      case "access":
        updateAccess(row, button);
        break;
      case "delete":
        deleteRow(row);
        break;
      case "reset-order":
        resetOrder(button);
        break;
      default:
        break;
    }
  });

  app.addEventListener("submit", event => {
    const form = event.target.closest(".tool-order-edit-form");
    if (!form) {
      return;
    }
    event.preventDefault();
    saveTitle(form.closest(".tool-order-row"), form);
  });

  if (list && reorderAllowed) {
    list.addEventListener("dragstart", event => {
      const row = event.target.closest(".tool-order-row");
      if (savingOrder || !row || !event.target.closest('[data-action="drag-handle"]')) {
        event.preventDefault();
        return;
      }
      draggedRow = row;
      dragStartOrder = getOrder().join(" ");
      event.dataTransfer.effectAllowed = "move";
      event.dataTransfer.setData("text/plain", row.dataset.pageId);
      row.classList.add("dragging");
    });

    list.addEventListener("dragover", event => {
      if (!draggedRow) {
        return;
      }

      event.preventDefault();
      const targetRow = event.target.closest(".tool-order-row");
      if (!targetRow || targetRow === draggedRow) {
        return;
      }

      const bounds = targetRow.getBoundingClientRect();
      const afterTarget = event.clientY > bounds.top + bounds.height / 2;
      list.insertBefore(draggedRow, afterTarget ? targetRow.nextSibling : targetRow);
    });

    list.addEventListener("dragend", () => {
      if (!draggedRow) {
        return;
      }

      draggedRow.classList.remove("dragging");
      const previousOrder = dragStartOrder.split(" ");
      const changed = dragStartOrder !== getOrder().join(" ");
      draggedRow = null;
      dragStartOrder = "";
      updateMoveButtons();

      if (changed) {
        persistOrder(previousOrder);
      }
    });
  }

  if (window.bootstrap?.Tooltip && window.matchMedia("(hover: hover) and (pointer: fine)").matches) {
    app.querySelectorAll("[title]").forEach(element => new window.bootstrap.Tooltip(element));
  }

  updateMoveButtons();
})();
