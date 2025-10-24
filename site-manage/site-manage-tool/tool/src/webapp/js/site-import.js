document.addEventListener("DOMContentLoaded", function() {
    function updateGradebookWarning() {
        const gradebookToolSelected = Array
            .from(document.querySelectorAll('.siteimport-tool-checkbox'))
            .some(cb => ((cb.dataset?.toolId || '').includes('sakai.gradebook')) && cb.checked);
        const gradebookWarning = document.getElementById('gradebook-warning');
        if (!gradebookWarning) return;
        gradebookWarning.classList.toggle('d-none', !gradebookToolSelected);
    }
    updateGradebookWarning();
    document.querySelectorAll('.siteimport-tool-checkbox').forEach(function(checkbox) {
        checkbox.addEventListener('change', updateGradebookWarning);
    });
});
