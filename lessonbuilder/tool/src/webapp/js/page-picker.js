document.addEventListener("DOMContentLoaded", () => {
    const itemToggles = Array.from(document.querySelectorAll("button.itemListToggle"));
    const showItemsButton = document.querySelector("#show-items");
    const hideItemsButton = document.querySelector("#hide-items");
    const selectAllCheckbox = document.querySelector("#chooseall");
    const deletionCheckboxes = Array.from(document.querySelectorAll("input.deletebox"));
    const deleteButton = document.querySelector("#delete-pages-button");
    const deleteDialog = document.querySelector("#delete-pages-dialog");
    const restorePageId = document.querySelector("#restore-page-id");

    const resizeToolFrame = () => {
        if (window.frameElement?.id && typeof setMainFrameHeight === "function") {
            setMainFrameHeight(window.frameElement.id);
        }
    };

    const getItemList = (toggle) => toggle.closest("li")
        ?.querySelector(":scope > .itemListContainer > .itemList");

    const setItemListVisibility = (toggle, list, show) => {
        list.hidden = !show;
        toggle.setAttribute("aria-expanded", show.toString());
        toggle.querySelector(".item-list-toggle-label-show").hidden = show;
        toggle.querySelector(".item-list-toggle-label-hide").hidden = !show;
        toggle.querySelector(".bi")?.classList.toggle("bi-caret-down-fill", !show);
        toggle.querySelector(".bi")?.classList.toggle("bi-caret-up-fill", show);
    };

    itemToggles.forEach((toggle) => {
        const list = getItemList(toggle);
        if (!list) {
            return;
        }

        if (list.id) {
            toggle.setAttribute("aria-controls", list.id);
        }

        toggle.addEventListener("click", () => {
            setItemListVisibility(toggle, list, list.hidden);
            resizeToolFrame();
        });
    });

    showItemsButton?.addEventListener("click", () => {
        itemToggles.forEach((toggle) => {
            const list = getItemList(toggle);
            if (list) {
                setItemListVisibility(toggle, list, true);
            }
        });
        showItemsButton.hidden = true;
        if (hideItemsButton) {
            hideItemsButton.hidden = false;
        }
        resizeToolFrame();
    });

    hideItemsButton?.addEventListener("click", () => {
        itemToggles.forEach((toggle) => {
            const list = getItemList(toggle);
            if (list) {
                setItemListVisibility(toggle, list, false);
            }
        });
        if (showItemsButton) {
            showItemsButton.hidden = false;
        }
        hideItemsButton.hidden = true;
        resizeToolFrame();
    });

    const updateDeletionControls = () => {
        const selectedCount = deletionCheckboxes.filter((checkbox) => checkbox.checked).length;
        if (deleteButton) {
            deleteButton.disabled = selectedCount === 0;
        }
        if (selectAllCheckbox) {
            selectAllCheckbox.checked = selectedCount > 0 && selectedCount === deletionCheckboxes.length;
            selectAllCheckbox.indeterminate = selectedCount > 0 && selectedCount < deletionCheckboxes.length;
        }
    };

    selectAllCheckbox?.addEventListener("change", () => {
        deletionCheckboxes.forEach((checkbox) => {
            checkbox.checked = selectAllCheckbox.checked;
        });
        updateDeletionControls();
    });

    deletionCheckboxes.forEach((checkbox) => checkbox.addEventListener("change", updateDeletionControls));
    updateDeletionControls();

    deleteButton?.addEventListener("click", () => {
        const selectedPages = deletionCheckboxes
            .filter((checkbox) => checkbox.checked)
            .map((checkbox) => checkbox.dataset.pageTitle);
        if (selectedPages.length === 0 || !deleteDialog) {
            return;
        }

        const confirmationMessage = document.querySelector("#delete-pages-confirm-message");
        const confirmationList = document.querySelector("#delete-pages-confirm-list");
        const onePageTemplate = document.querySelector("#delete-confirm-one-template")?.textContent;
        const manyPagesTemplate = document.querySelector("#delete-confirm-many-template")?.textContent;
        if (!confirmationMessage || !confirmationList || !onePageTemplate || !manyPagesTemplate) {
            return;
        }

        confirmationMessage.textContent = selectedPages.length === 1
            ? onePageTemplate.replace("{}", selectedPages[0])
            : manyPagesTemplate.replace("{}", selectedPages.length.toString());
        confirmationList.replaceChildren(...selectedPages.map((title) => {
            const item = document.createElement("li");
            item.textContent = title;
            return item;
        }));

        bootstrap.Modal.getOrCreateInstance(deleteDialog).show();
    });

    document.querySelectorAll("button.removed-page-restore").forEach((button) => {
        button.addEventListener("click", () => {
            if (restorePageId) {
                restorePageId.value = button.dataset.pageId;
            }
        });
    });
});
