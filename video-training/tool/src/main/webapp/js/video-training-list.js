(function () {
    const tableElement = document.getElementById('vt-video-table');
    let dataTable = null;

    function initializeTooltips(root) {
        if (!window.bootstrap || !window.bootstrap.Tooltip) {
            return;
        }
        const container = root || document;
        container.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(function (element) {
            if (element.dataset.vtTooltipBound === 'true') {
                return;
            }
            window.bootstrap.Tooltip.getOrCreateInstance(element);
            element.dataset.vtTooltipBound = 'true';
        });
    }

    function appendCacheBust(url) {
        if (!url) {
            return url;
        }
        const separator = url.indexOf('?') >= 0 ? '&' : '?';
        return url + separator + 'vtcb=' + Date.now();
    }

    function revealFallback(mediaElement) {
        const wrapper = mediaElement.closest('.vt-video-thumb-wrap, .vt-table-thumb-wrap');
        if (!wrapper) {
            return;
        }
        mediaElement.classList.add('vt-is-hidden');
        const fallback = wrapper.querySelector('.vt-video-thumb-placeholder, .vt-table-thumb-placeholder');
        if (fallback) {
            fallback.classList.remove('vt-is-hidden');
        }
    }

    function bindThumbnailRetry(mediaElement) {
        if (mediaElement.dataset.vtThumbBound === 'true') {
            return;
        }

        mediaElement.addEventListener('error', function () {
            const retries = Number(mediaElement.dataset.vtThumbRetries || '0');
            if (retries < 1) {
                mediaElement.dataset.vtThumbRetries = String(retries + 1);
                if (mediaElement.tagName === 'VIDEO') {
                    const currentSrc = mediaElement.getAttribute('src') || mediaElement.currentSrc;
                    mediaElement.setAttribute('src', appendCacheBust(currentSrc));
                    mediaElement.load();
                } else {
                    const currentSrc = mediaElement.getAttribute('src');
                    mediaElement.setAttribute('src', appendCacheBust(currentSrc));
                }
                return;
            }

            revealFallback(mediaElement);
        });

        mediaElement.dataset.vtThumbBound = 'true';
    }

    function initializeThumbnailRetry(root) {
        const container = root || document;
        container.querySelectorAll('[data-thumb-retry="true"]').forEach(bindThumbnailRetry);
    }

    const dtSortByToIndex = {
        title: 0,
        context: 1,
        scope: 2,
        status: 3,
        lesson: 4,
        release: 5,
        retract: 6
    };

    const dtIndexToSortBy = {
        0: 'title',
        1: 'context',
        2: 'scope',
        3: 'status',
        4: 'lesson',
        5: 'release',
        6: 'retract'
    };

    function getPageSize() {
        const currentSearch = new URLSearchParams(window.location.search);
        const fromSearch = Number(currentSearch.get('size'));
        if (Number.isFinite(fromSearch) && fromSearch > 0) {
            return fromSearch;
        }

        const sizeInput = document.querySelector('input[name="size"]');
        if (sizeInput) {
            const fromInput = Number(sizeInput.value);
            if (Number.isFinite(fromInput) && fromInput > 0) {
                return fromInput;
            }
        }

        if (tableElement) {
            const fromDataset = Number(tableElement.dataset.size || '0');
            if (Number.isFinite(fromDataset) && fromDataset > 0) {
                return fromDataset;
            }
        }

        return 15;
    }

    function buildSortedReloadUrl(sortBy, sortDir) {
        const params = new URLSearchParams(window.location.search);
        params.set('viewMode', tableElement.dataset.viewMode || 'table');
        params.set('q', tableElement.dataset.query || '');
        params.set('size', String(getPageSize()));
        params.set('offset', '0');
        params.delete('batchSize');
        params.delete('page');
        params.set('sortBy', sortBy);
        params.set('sortDir', sortDir);
        return window.location.pathname + '?' + params.toString();
    }

    if (tableElement && window.jQuery && window.jQuery.fn && window.jQuery.fn.DataTable) {
        const initialSortBy = tableElement.dataset.sortBy || 'modified';
        const initialSortDir = tableElement.dataset.sortDir === 'asc' ? 'asc' : 'desc';
        const initialOrderIndex = dtSortByToIndex[initialSortBy];
        const initialOrder = typeof initialOrderIndex === 'number' ? [[initialOrderIndex, initialSortDir]] : [];

        dataTable = window.jQuery(tableElement).DataTable({
            paging: false,
            searching: false,
            info: false,
            order: initialOrder,
            retrieve: true
        });

        window.jQuery(tableElement).on('order.dt', function () {
            const order = dataTable.order();
            if (!order || !order.length) {
                return;
            }

            const orderIndex = order[0][0];
            const orderDir = order[0][1] === 'asc' ? 'asc' : 'desc';
            const sortBy = dtIndexToSortBy[orderIndex];
            if (!sortBy) {
                return;
            }

            const currentSortBy = tableElement.dataset.sortBy || 'modified';
            const currentSortDir = tableElement.dataset.sortDir || 'desc';
            if (sortBy === currentSortBy && orderDir === currentSortDir) {
                return;
            }

            window.location.href = buildSortedReloadUrl(sortBy, orderDir);
        });
    }

    initializeTooltips(document);
    initializeThumbnailRetry(document);
    attachAjaxHandlers();

    function attachAjaxHandlers() {
        const actionForms = document.querySelectorAll('form.vt-inline-form');
        if (!actionForms || !actionForms.length) return;

        actionForms.forEach(form => {
            const action = form.getAttribute('action') || '';
            const m = action.match(/\/videos\/([^\/]+)\/(favorite|watch-later|delete|publish|withdraw|archive|restore-draft|submit-approval|reject-approval)(?:$|\?)/);
            if (!m) return;
            const videoId = m[1];
            const verb = m[2];

            form.addEventListener('submit', function (ev) {
                ev.preventDefault();

                if (verb === 'favorite' || verb === 'watch-later') {
                    const ajaxUrl = action.replace(/\/$/, '') + '/ajax';
                    const formData = new FormData(form);
                    fetch(ajaxUrl, {
                        method: 'POST',
                        credentials: 'same-origin',
                        headers: { 'X-Requested-With': 'XMLHttpRequest', 'Accept': 'application/json' },
                        body: formData
                    }).then(resp => resp.json()).then(json => {
                        if (json && json.success) {
                                const btn = form.querySelector('button');
                                if (btn) {
                                    const hidden = btn.querySelector('.visually-hidden');
                                    if (verb === 'favorite') {
                                        const isFav = !!json.favorite;
                                        const offDefault = btn.querySelector('[data-role="fav-off-default"]');
                                        const offHover = btn.querySelector('[data-role="fav-off-hover"]');
                                        const onDefault = btn.querySelector('[data-role="fav-on-default"]');
                                        const onHover = btn.querySelector('[data-role="fav-on-hover"]');
                                        if (offDefault) isFav ? offDefault.setAttribute('hidden', 'hidden') : offDefault.removeAttribute('hidden');
                                        if (offHover) isFav ? offHover.setAttribute('hidden', 'hidden') : offHover.removeAttribute('hidden');
                                        if (onDefault) isFav ? onDefault.removeAttribute('hidden') : onDefault.setAttribute('hidden', 'hidden');
                                        if (onHover) isFav ? onHover.removeAttribute('hidden') : onHover.setAttribute('hidden', 'hidden');
                                        const newLabelFav = isFav ? (btn.getAttribute('data-remove-text') || '') : (btn.getAttribute('data-add-text') || '');
                                        if (hidden) hidden.textContent = newLabelFav;
                                        try {
                                            const favInput = form.querySelector('input[name="favorite"]');
                                            if (favInput) favInput.value = isFav ? 'false' : 'true';
                                        } catch (e) {}
                                        try {
                                            if (newLabelFav) {
                                                btn.setAttribute('title', newLabelFav);
                                                btn.setAttribute('aria-label', newLabelFav);
                                            }
                                            if (window.bootstrap && window.bootstrap.Tooltip) {
                                                const inst = window.bootstrap.Tooltip.getInstance(btn);
                                                if (inst) { inst.hide(); inst.dispose(); }
                                            }
                                        } catch (e) {}
                                    } else {
                                        const isWatch = !!json.watchLater;
                                        const offDefault = btn.querySelector('[data-role="wl-off-default"]');
                                        const offHover = btn.querySelector('[data-role="wl-off-hover"]');
                                        const onDefault = btn.querySelector('[data-role="wl-on-default"]');
                                        const onHover = btn.querySelector('[data-role="wl-on-hover"]');
                                        if (offDefault) isWatch ? offDefault.setAttribute('hidden', 'hidden') : offDefault.removeAttribute('hidden');
                                        if (offHover) isWatch ? offHover.setAttribute('hidden', 'hidden') : offHover.removeAttribute('hidden');
                                        if (onDefault) isWatch ? onDefault.removeAttribute('hidden') : onDefault.setAttribute('hidden', 'hidden');
                                        if (onHover) isWatch ? onHover.removeAttribute('hidden') : onHover.setAttribute('hidden', 'hidden');
                                        const newLabelWl = isWatch ? (btn.getAttribute('data-remove-text') || '') : (btn.getAttribute('data-add-text') || '');
                                        if (hidden) hidden.textContent = newLabelWl;
                                        try {
                                            const wlInput = form.querySelector('input[name="watchLater"]');
                                            if (wlInput) wlInput.value = isWatch ? 'false' : 'true';
                                        } catch (e) {}
                                        try {
                                            if (newLabelWl) {
                                                btn.setAttribute('title', newLabelWl);
                                                btn.setAttribute('aria-label', newLabelWl);
                                            }
                                            if (window.bootstrap && window.bootstrap.Tooltip) {
                                                const inst = window.bootstrap.Tooltip.getInstance(btn);
                                                if (inst) { inst.hide(); inst.dispose(); }
                                            }
                                        } catch (e) {}
                                    }
                                    initializeTooltips(btn);
                                }
                                // If this is the favorites or watch-later preferred list and the action removed the item,
                                // remove the row from the DOM so the list updates without reload.
                                const path = window.location.pathname || '';
                                const onFavoritesPage = /\/favorites\/?$/.test(path);
                                const onWatchLaterPage = /\/watch-later\/?$/.test(path);
                                if ((onFavoritesPage && verb === 'favorite' && json.favorite === false) ||
                                    (onWatchLaterPage && verb === 'watch-later' && json.watchLater === false)) {
                                    const tr = form.closest('tr');
                                    if (tr) {
                                        try {
                                            if (window.bootstrap && window.bootstrap.Tooltip) {
                                                tr.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(el => {
                                                    const inst = window.bootstrap.Tooltip.getInstance(el);
                                                    if (inst) { inst.hide(); inst.dispose(); }
                                                });
                                            }
                                        } catch (e) {}
                                        const tableBody = tr.parentNode;
                                        const nextFocusable = tr.nextElementSibling || tr.previousElementSibling;
                                        tr.remove();
                                        if (nextFocusable) nextFocusable.querySelector('a, button, input, [tabindex]')?.focus();
                                        return;
                                    }
                                    const article = form.closest('article.vt-video-card');
                                    if (article) {
                                        try {
                                            if (window.bootstrap && window.bootstrap.Tooltip) {
                                                article.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(el => {
                                                    const inst = window.bootstrap.Tooltip.getInstance(el);
                                                    if (inst) { inst.hide(); inst.dispose(); }
                                                });
                                            }
                                        } catch (e) {}
                                        const parent = article.parentNode;
                                        const nextFocusable = article.nextElementSibling || article.previousElementSibling;
                                        article.remove();
                                        if (nextFocusable) nextFocusable.querySelector('a, button, [tabindex]')?.focus();
                                        return;
                                    }
                                }
                        } else {
                            // show a simple alert for errors
                            if (json && json.error) {
                                alert(json.error);
                            }
                        }
                    }).catch(err => {
                        console.error('AJAX error', err);
                        form.submit();
                    });
                    return;
                }

                if (verb === 'delete') {
                    const ajaxUrl = action.replace(/\/$/, '') + '/ajax';
                    const formData = new FormData(form);
                    fetch(ajaxUrl, {
                        method: 'POST',
                        credentials: 'same-origin',
                        headers: { 'X-Requested-With': 'XMLHttpRequest', 'Accept': 'application/json' },
                        body: formData
                    }).then(resp => resp.json()).then(json => {
                        if (json && json.success) {
                            // remove row or card
                            const tr = form.closest('tr');
                            if (tr) {
                                try {
                                    if (window.bootstrap && window.bootstrap.Tooltip) {
                                        tr.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(el => {
                                            const inst = window.bootstrap.Tooltip.getInstance(el);
                                            if (inst) { inst.hide(); inst.dispose(); }
                                        });
                                    }
                                } catch (e) {}
                                const tableBody = tr.parentNode;
                                tr.remove();
                                // maintain focus: focus next row or table
                                const next = tableBody.querySelector('tr');
                                if (next) next.querySelector('a, button, input, [tabindex]')?.focus();
                                return;
                            }
                            const article = form.closest('article.vt-video-card');
                            if (article) {
                                try {
                                    if (window.bootstrap && window.bootstrap.Tooltip) {
                                        article.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(el => {
                                            const inst = window.bootstrap.Tooltip.getInstance(el);
                                            if (inst) { inst.hide(); inst.dispose(); }
                                        });
                                    }
                                } catch (e) {}
                                const parent = article.parentNode;
                                article.remove();
                                parent.querySelector('a, button, [tabindex]')?.focus();
                                return;
                            }
                            window.location.reload();
                        } else {
                            if (json && json.error) alert(json.error);
                        }
                    }).catch(err => {
                        console.error('AJAX error', err);
                        form.submit();
                    });
                    return;
                }

                // status transitions: map form action to status value
                const statusMap = {
                    'publish': 'PUBLISHED',
                    'withdraw': 'WITHDRAWN',
                    'archive': 'ARCHIVED',
                    'restore-draft': 'DRAFT',
                    'submit-approval': 'PENDING_APPROVAL',
                    'reject-approval': 'DRAFT'
                };
                if (statusMap[verb]) {
                    const ajaxUrl = '/videos/' + videoId + '/status/ajax';
                    const formData = new FormData();
                    formData.append('status', statusMap[verb]);
                    fetch(ajaxUrl, {
                        method: 'POST',
                        credentials: 'same-origin',
                        headers: { 'X-Requested-With': 'XMLHttpRequest', 'Accept': 'application/json' },
                        body: formData
                    }).then(resp => resp.json()).then(json => {
                        if (json && json.success) {
                            // update status cell in table row
                            const tr = form.closest('tr');
                            if (tr) {
                                const statusCell = tr.querySelector('td:nth-child(3)');
                                if (statusCell) statusCell.textContent = json.status || '';
                                return;
                            }
                            const article = form.closest('article.vt-video-card');
                            if (article) {
                                const badges = article.querySelectorAll('[data-role^="status-"]');
                                badges.forEach(b => b.setAttribute('hidden', 'hidden'));
                                if (json.status) {
                                    const sel = '[data-role="status-' + json.status + '"]';
                                    const target = article.querySelector(sel);
                                    if (target) {
                                        target.removeAttribute('hidden');
                                        initializeTooltips(target);
                                    }
                                }
                                return;
                            }
                            window.location.reload();
                        } else {
                            if (json && json.error) alert(json.error);
                        }
                    }).catch(err => {
                        console.error('AJAX error', err);
                        form.submit();
                    });
                    return;
                }
            });
        });
    }
})();
