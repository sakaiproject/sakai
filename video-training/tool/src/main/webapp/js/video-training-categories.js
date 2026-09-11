(function () {
    const rootBody = document.getElementById('vt-category-root-body');
    const modalElement = document.getElementById('vtCategoryModal');
    const modalForm = document.getElementById('vt-category-modal-form');
    const modalIdInput = document.getElementById('vt-category-modal-id');
    const modalNameInput = document.getElementById('vt-category-modal-name');
    const modalParentSelect = document.getElementById('vt-category-modal-parent');
    const modalTitle = document.getElementById('vtCategoryModalLabel');
    const editTriggers = document.querySelectorAll('.vt-category-edit-trigger');

    if (!rootBody || !modalElement || !modalForm || !modalIdInput || !modalNameInput || !modalParentSelect || !modalTitle) {
        return;
    }

    modalTitle.dataset.defaultLabel = modalTitle.textContent;

    function buildOrderPayload() {
        const payload = [];
        const roots = Array.from(rootBody.children).filter(node => node.classList.contains('vt-category-node'));

        roots.forEach(function (rootNode, rootIndex) {
            const rootId = rootNode.dataset.categoryId;
            if (!rootId) return;

            payload.push({ id: rootId, parentCategoryId: null, sortOrder: rootIndex });

            const childContainer = rootNode.querySelector(':scope > .vt-category-children');
            if (!childContainer) return;

            const children = Array.from(childContainer.children).filter(node => node.classList.contains('vt-category-node'));
            children.forEach(function (childNode, childIndex) {
                const childId = childNode.dataset.categoryId;
                if (!childId) return;
                payload.push({ id: childId, parentCategoryId: rootId, sortOrder: childIndex });
            });
        });

        return payload;
    }

    let orderSaveQueue = Promise.resolve();

    function saveOrder() {
        const reorderUrl = rootBody.dataset.reorderUrl;
        if (!reorderUrl) return;

                'X-Requested-With': 'XMLHttpRequest'
        const payload = buildOrderPayload();

        orderSaveQueue = orderSaveQueue
            .catch(() => undefined)
            .then(() => fetch(reorderUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify(payload)
            }))
            .then(function (response) {
                if (!response.ok) throw new Error('Failed to save category order');
            });

        orderSaveQueue.catch(function () {
            window.location.reload();
        });
    }

    function canDropNodeIntoContainer(node, container) {
        if (!node || !container) return false;

        const hasChildren = String(node.dataset.hasChildren) === 'true';
        const isChildContainer = container.classList.contains('vt-category-children');
        if (isChildContainer && hasChildren) return false;

        return true;
    }

    function markForbidden(dropEl, forbidden) {
        if (!dropEl) return;
        dropEl.classList.toggle('vt-drop-forbidden', forbidden);
    }

    function updateVisualClasses() {
        const nodes = rootBody.querySelectorAll('.vt-category-node');
        nodes.forEach(function (node) {
            const isRoot = node.parentElement === rootBody;

            node.classList.toggle('vt-is-root', isRoot);
            node.classList.toggle('vt-is-child', !isRoot);

            const childContainer = node.querySelector(':scope > .vt-category-children');
            const hasChildren = !!(childContainer && childContainer.querySelector(':scope > .vt-category-node'));

            node.dataset.hasChildren = hasChildren ? 'true' : 'false';
            node.classList.toggle('vt-has-children', hasChildren);
            node.classList.toggle('vt-is-empty', !hasChildren);
        });
    }

    function setupSortable(container, options) {
        if (!window.Sortable || !container) return null;
        return window.Sortable.create(container, options);
    }

    function resetModalParentOptions() {
        modalParentSelect.querySelectorAll('option').forEach(option => option.disabled = false);
    }

    function openEditModal(trigger) {
        const categoryId = trigger.dataset.categoryId || '';
        const categoryName = trigger.dataset.categoryName || '';
        const parentCategoryId = trigger.dataset.categoryParentId || '';

        modalTitle.textContent = modalTitle.dataset.defaultLabel || modalTitle.textContent;
        modalIdInput.value = categoryId;
        modalNameInput.value = categoryName;

        resetModalParentOptions();
        const currentOption = Array.from(modalParentSelect.options).find(option => option.value === categoryId);
        if (currentOption) currentOption.disabled = true;

        modalParentSelect.value = parentCategoryId;
    }

    editTriggers.forEach(trigger => {
        trigger.addEventListener('click', () => openEditModal(trigger));
    });

    modalElement.addEventListener('hidden.bs.modal', function () {
        modalForm.reset();
        modalIdInput.value = '';
        resetModalParentOptions();
        modalTitle.textContent = modalTitle.dataset.defaultLabel || modalTitle.textContent;
    });

    const rootSortable = setupSortable(rootBody, {
        animation: 150,
        handle: '.vt-category-drag-handle',
        draggable: '.vt-category-node',
        group: {
            name: 'vt-categories',
            pull: true,
            put: (to, from, dragEl) => canDropNodeIntoContainer(dragEl, to.el)
        },
        onMove: function (evt) {
            const forbidden = !canDropNodeIntoContainer(evt.dragged, evt.to);
            markForbidden(evt.to, forbidden);
            return !forbidden;
        },
        onEnd: function (evt) {
            markForbidden(evt.to, false);
            markForbidden(evt.from, false);
            updateVisualClasses();
            saveOrder();
        }
    });

    

    const initialChildContainers = rootBody.querySelectorAll('.vt-category-children');
    initialChildContainers.forEach(function (container) {
        setupSortable(container, {
            animation: 150,
            handle: '.vt-category-drag-handle',
            draggable: '.vt-category-node',
            group: rootSortable ? rootSortable.options.group : { name: 'vt-categories', pull: true, put: true },
            onMove: function (evt) {
                const forbidden = !canDropNodeIntoContainer(evt.dragged, evt.to);
                markForbidden(evt.to, forbidden);
                return !forbidden;
            },
            onEnd: function (evt) {
                markForbidden(evt.to, false);
                markForbidden(evt.from, false);
                updateVisualClasses();
                saveOrder();
            }
        });
    });

    function moveCategoryNode(node, direction) {
        if (!node) return;
        const parent = node.parentElement;
        const isRoot = parent === rootBody;

        if (direction === 'down') {
            if (isRoot) {
                const nextRoot = node.nextElementSibling;
                if (!nextRoot || !nextRoot.classList.contains('vt-category-node')) return;

                const nodeHasChildren = String(node.dataset.hasChildren) === 'true';
                const nextChildContainer = nextRoot.querySelector(':scope > .vt-category-children');

                // Move inside the adjacent root node if empty to prevent depth > 2
                if (!nodeHasChildren && nextChildContainer) {
                    nextChildContainer.insertBefore(node, nextChildContainer.firstChild);
                } else {
                    // Swap root positions if node already has children
                    rootBody.insertBefore(nextRoot, node);
                }
            } else {
                const nextChild = node.nextElementSibling;
                if (nextChild && nextChild.classList.contains('vt-category-node')) {
                    // Reorder within the same child container
                    parent.insertBefore(node, nextChild.nextElementSibling);
                } else {
                    // Promote to root level after the parent node
                    const parentNode = parent.closest('.vt-category-node');
                    if (parentNode) {
                        rootBody.insertBefore(node, parentNode.nextElementSibling);
                    }
                }
            }
        } else if (direction === 'up') {
            if (isRoot) {
                const prevRoot = node.previousElementSibling;
                if (!prevRoot || !prevRoot.classList.contains('vt-category-node')) return;

                const nodeHasChildren = String(node.dataset.hasChildren) === 'true';
                const prevChildContainer = prevRoot.querySelector(':scope > .vt-category-children');

                // Move as last child of previous root node if empty
                if (!nodeHasChildren && prevChildContainer) {
                    prevChildContainer.appendChild(node);
                } else {
                    // Swap root positions if node already has children
                    rootBody.insertBefore(node, prevRoot);
                }
            } else {
                const prevChild = node.previousElementSibling;
                if (prevChild && prevChild.classList.contains('vt-category-node')) {
                    // Reorder within the same child container
                    parent.insertBefore(node, prevChild);
                } else {
                    // Promote to root level before the parent node
                    const parentNode = parent.closest('.vt-category-node');
                    if (parentNode) {
                        rootBody.insertBefore(node, parentNode);
                    }
                }
            }
        }

        updateVisualClasses();
        saveOrder();
    }

    document.addEventListener('click', function (evt) {
        const btnUp = evt.target.closest('.vt-btn-move-up');
        const btnDown = evt.target.closest('.vt-btn-move-down');

        if (btnUp) {
            evt.preventDefault();
            const node = btnUp.closest('.vt-category-node');
            moveCategoryNode(node, 'up');
            btnUp.focus();
        } else if (btnDown) {
            evt.preventDefault();
            const node = btnDown.closest('.vt-category-node');
            moveCategoryNode(node, 'down');
            btnDown.focus();
        }
    });

    updateVisualClasses();
})();
