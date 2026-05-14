// scripts for the #searchFilterPanel velocity macro (see VM_chef_library.vm in the velocity project)
(() => {

    const MIN_SEARCH_CHARS = 3;

    document.querySelectorAll('.sakai-table-searchFilter-searchField[data-search-url]').forEach(field => {
        const clearBtn = field.nextElementSibling;

        const doFetch = async (url) => {
            if (!url) return;
            field._ac?.abort();
            field._ac = new AbortController();
            try {
                const resp = await fetch(url, { credentials: 'same-origin', signal: field._ac.signal });
                if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
                const tmp = document.createElement('div');
                tmp.innerHTML = await resp.text();

                // Update each named table's body by matching on table id
                document.querySelectorAll('table[id] tbody').forEach(tbody => {
                    const fresh = tmp.querySelector('#' + CSS.escape(tbody.closest('table').id) + ' tbody');
                    tbody.innerHTML = fresh?.innerHTML ?? '';
                });

                // Update pager containers by position (template always renders them in the same order)
                const newPagers = tmp.querySelectorAll('.sakai-table-pagerContainer');
                document.querySelectorAll('.sakai-table-pagerContainer').forEach((el, i) => {
                    el.innerHTML = newPagers[i]?.innerHTML ?? '';
                });

                history.replaceState(null, '', url);
                document.dispatchEvent(new CustomEvent('sfp:updated'));
            } catch (e) {
                if (e.name !== 'AbortError') location = url;
            }
        };

        const updateClearBtn = () => clearBtn && (clearBtn.style.display = field.value ? '' : 'none');

        const search = () => {
            const q = field.value.trim();
            if (!q) return doFetch(field.dataset.clearUrl);
            if (q.length < MIN_SEARCH_CHARS) return;
            doFetch(`${field.dataset.searchUrl}&search=${encodeURIComponent(q)}`);
        };

        field.addEventListener('input', () => {
            updateClearBtn();
        });

        field.addEventListener('keydown', e => {
            if (e.key === 'Enter' && !e.isComposing) { e.preventDefault(); search(); }
        });

        clearBtn?.addEventListener('click', () => {
            field.value = '';
            updateClearBtn();
            search();
        });
    });

})();
