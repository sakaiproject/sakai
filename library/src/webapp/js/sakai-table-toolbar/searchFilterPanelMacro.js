// scripts for the #searchFilterPanel velocity macro (see VM_chef_library.vm in the velocity project)
(() => {

    const MIN_SEARCH_CHARS = 3;

    document.querySelectorAll('.sakai-table-searchFilter-searchField[data-search-url]').forEach(field => {
        // the search the listing shows, which emptying the field has to drop
        let applied = field.value.trim();

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

        const search = () => {
            const q = field.value.trim();
            if (q && q.length < MIN_SEARCH_CHARS) return;
            applied = q;
            doFetch(q ? `${field.dataset.searchUrl}&search=${encodeURIComponent(q)}` : field.dataset.clearUrl);
        };

        // emptying the field, with the native clear button or by deleting the text, drops the search
        field.addEventListener('input', () => !field.value && applied && search());

        field.addEventListener('keydown', e => {
            if (e.key === 'Enter' && !e.isComposing) { e.preventDefault(); search(); }
        });
    });

})();
