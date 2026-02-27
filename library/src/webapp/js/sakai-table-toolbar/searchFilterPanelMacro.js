// scripts for the #searchFilterPanel velocity macro (see VM_chef_library.vm in the velocity project)
(() => {

    const sel = 'table[id] tbody, .sakai-table-pagerContainer';
    const targets = document.querySelectorAll(sel);

    const doFetch = async (url, field) => {
        if (!url) return;
        field._ac?.abort();
        field._ac = new AbortController();
        try {
            const tmp = document.createElement('div');
            tmp.innerHTML = await (await fetch(url, { credentials: 'same-origin', signal: field._ac.signal })).text();

            const newTargets = tmp.querySelectorAll(sel);
            targets.forEach((el, i) => { if (newTargets[i]) el.innerHTML = newTargets[i].innerHTML; });

            history.replaceState(null, '', url);
            document.dispatchEvent(new CustomEvent('sfp:updated'));
        } catch (e) {
            if (e.name !== 'AbortError') location = url;
        }
    };

    document.querySelectorAll('.sakai-table-searchFilter-searchField[data-search-url]').forEach(field => {
        const clearBtn = field.nextElementSibling;
        let debounceTimer;

        const updateClearBtn = () => clearBtn && (clearBtn.style.display = field.value ? '' : 'none');

        const search = () => {
            if (!field.value) return doFetch(field.dataset.clearUrl, field);
            doFetch(`${field.dataset.searchUrl}&search=${encodeURIComponent(field.value)}`, field);
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
            doFetch(field.dataset.clearUrl, field);
        });
    });

})();
