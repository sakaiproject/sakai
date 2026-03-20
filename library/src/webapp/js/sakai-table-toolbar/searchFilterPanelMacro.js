// scripts for the #searchFilterPanel velocity macro (see VM_chef_library.vm in the velocity project)
(() => {

    document.querySelectorAll('.sakai-table-searchFilter-searchField[data-search-url]').forEach(field => {
        const clearBtn = field.nextElementSibling;
        let debounceTimer;

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
            if (!field.value) return doFetch(field.dataset.clearUrl);
            doFetch(`${field.dataset.searchUrl}&search=${encodeURIComponent(field.value)}`);
        };

        field.addEventListener('input', () => {
            updateClearBtn();
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(search, 400);
        });

        field.addEventListener('keydown', e => {
            if (e.key === 'Enter') { e.preventDefault(); clearTimeout(debounceTimer); search(); }
        });

        clearBtn?.addEventListener('click', () => {
            clearTimeout(debounceTimer);
            field.value = '';
            updateClearBtn();
            search();
        });
    });

})();
