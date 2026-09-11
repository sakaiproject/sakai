(function () {
    const form = document.querySelector('.vt-video-form');
    const sourceModeInputs = document.querySelectorAll('input[name="sourceMode"]');
    const providerTypeInput = document.getElementById('providerType');
    const externalSection = document.getElementById('externalSourceSection');
    const uploadSection = document.getElementById('uploadSourceSection');
    const resourcesSection = document.getElementById('resourcesSourceSection');
    const storageDestinationSection = document.getElementById('storageDestinationSection');
    const storageDestinationSelect = document.getElementById('storageDestination');
    const sourceReferenceInput = document.getElementById('sourceReference');
    const existingResourceSelect = document.getElementById('existingResourceReference');
    const nativeFileInput = document.getElementById('nativeFile');
    const visibilityScopeSelect = document.getElementById('visibilityScope');
    const metadataUrl = form?.dataset.metadataUrl || '';
    const metadataButton = document.getElementById('fetchMetadataButton');
    const metadataSuggestionBox = document.getElementById('metadataSuggestionBox');
    const metadataSuggestionSource = document.getElementById('metadataSuggestionSource');
    const metadataSuggestionTitle = document.getElementById('metadataSuggestionTitle');
    const metadataSuggestionDescription = document.getElementById('metadataSuggestionDescription');
    const applySuggestedTitleButton = document.getElementById('applySuggestedTitle');
    const applySuggestedDescriptionButton = document.getElementById('applySuggestedDescription');
    const uploadTooLargeMessage = form?.dataset.nativeUploadTooLargeMessage || '';
    const maxNativeUploadBytes = Number.parseInt(form?.dataset.nativeUploadMaxBytes || '', 10);
    const visibilityReductionConfirmMessage = form?.dataset.visibilityReductionConfirmMessage || '';
    const hlsUploadEnabled = form?.dataset.hlsUploadEnabled === 'true';
    const youtubeUploadConfigured = form?.dataset.youtubeUploadConfigured === 'true';
    const defaultUploadProviderType = form?.dataset.defaultUploadProviderType || 'HLS_UPLOAD';
    const youtubeMetadataConfigured = form?.dataset.youtubeMetadataConfigured === 'true';
    const isEdit = form?.dataset.isEdit === 'true';
    const videoId = form?.dataset.videoId || '';
    const msgMetaNotConfigured = form?.dataset.msgMetaNotConfigured || '';
    const msgMetaNoSource = form?.dataset.msgMetaNoSource || '';
    const msgMetaTooltip = form?.dataset.msgMetaTooltip || '';
    const msgMetaSrcExt = form?.dataset.msgMetaSrcExt || '';
    const msgMetaSrcRes = form?.dataset.msgMetaSrcRes || '';
    const msgMetaSrcLoc = form?.dataset.msgMetaSrcLoc || '';
    const msgMetaNoTitle = form?.dataset.msgMetaNoTitle || '';
    const msgMetaNoDesc = form?.dataset.msgMetaNoDesc || '';
    const msgMetaError = form?.dataset.msgMetaError || '';
    let suggestedMetadata = { title: '', description: '' };
    let currentVisibilityScope = form?.dataset.initialVisibilityScope || visibilityScopeSelect?.value || 'COURSE';

    if (!form || !providerTypeInput || !sourceModeInputs.length) {
        return;
    }

    function isValidUploadFileSize(file) {
        if (!file || !Number.isFinite(maxNativeUploadBytes) || maxNativeUploadBytes <= 0) {
            return true;
        }
        return file.size <= maxNativeUploadBytes;
    }

    function validateUploadFileSize() {
        if (!nativeFileInput || selectedMode() !== 'upload' || !nativeFileInput.files || !nativeFileInput.files.length) {
            return true;
        }

        if (isValidUploadFileSize(nativeFileInput.files[0])) {
            return true;
        }

        nativeFileInput.value = '';
        if (uploadTooLargeMessage) {
            window.alert(uploadTooLargeMessage);
        }
        return false;
    }

    function currentMetadataSourceValue() {
        const mode = selectedMode();
        if (mode === 'external') {
            return sourceReferenceInput ? sourceReferenceInput.value.trim() : '';
        }
        if (mode === 'resources') {
            return existingResourceSelect ? existingResourceSelect.value.trim() : '';
        }
        if (mode === 'upload') {
            return nativeFileInput && nativeFileInput.files && nativeFileInput.files.length ? nativeFileInput.files[0].name : '';
        }
        return '';
    }

    function hasMetadataSource() {
        return Boolean(currentMetadataSourceValue());
    }

    function updateMetadataButtonState() {
        if (!metadataButton) {
            return;
        }

        const enabled = youtubeMetadataConfigured && hasMetadataSource();
        metadataButton.disabled = !enabled;
        metadataButton.title = !youtubeMetadataConfigured
            ? msgMetaNotConfigured
            : !hasMetadataSource()
                ? msgMetaNoSource
                : msgMetaTooltip;
    }

    function selectedMode() {
        const selected = document.querySelector('input[name="sourceMode"]:checked');
        return selected ? selected.value : 'upload';
    }

    function visibilityScopeRank(scope) {
        switch (scope) {
            case 'GLOBAL':
                return 3;
            case 'COURSE':
                return 2;
            case 'LESSON':
                return 1;
            default:
                return 0;
        }
    }

    function notifyVisibilityScopeChange(fromScope, toScope) {
        const detail = {
            event: 'video.training.visibility.scope.changed',
            videoId,
            fromScope,
            toScope,
            isMoreRestrictive: visibilityScopeRank(toScope) < visibilityScopeRank(fromScope)
        };
        document.dispatchEvent(new CustomEvent('sakai-event', {
            bubbles: true,
            detail
        }));
    }

    function applyMode(mode) {
        const isExternal = mode === 'external';
        const isResources = mode === 'resources';
        const isUpload = mode === 'upload';

        if (externalSection) {
            externalSection.hidden = !isExternal;
        }
        if (uploadSection) {
            uploadSection.hidden = isExternal || isResources;
        }
        if (resourcesSection) {
            resourcesSection.hidden = !isResources;
        }
        if (storageDestinationSection) {
            storageDestinationSection.hidden = !isUpload;
        }

        if (sourceReferenceInput) {
            sourceReferenceInput.required = isExternal;
            if (!isExternal) {
                sourceReferenceInput.value = '';
            }
        }

        if (existingResourceSelect) {
            existingResourceSelect.required = isResources;
            if (!isResources) {
                existingResourceSelect.value = '';
            }
        }

        syncProviderTypeInput();
    }

    function syncProviderTypeInput() {
        const mode = selectedMode();

        if (mode === 'external') {
            providerTypeInput.value = 'EXTERNAL';
            return;
        }

        if (mode === 'resources') {
            providerTypeInput.value = 'RESOURCES';
            return;
        }

        if (mode === 'upload') {
            const selectedStorageType = storageDestinationSelect && !storageDestinationSelect.disabled
                ? storageDestinationSelect.value
                : defaultUploadProviderType;
            providerTypeInput.value = selectedStorageType || defaultUploadProviderType;
            return;
        }

        providerTypeInput.value = defaultUploadProviderType;
    }

    if (storageDestinationSelect) {
        storageDestinationSelect.addEventListener('change', () => {
            syncProviderTypeInput();
        });
    }

    sourceModeInputs.forEach((input) => {
        input.addEventListener('change', () => {
            applyMode(selectedMode());
            updateMetadataButtonState();
        });
    });

    if (nativeFileInput) {
        nativeFileInput.addEventListener('change', validateUploadFileSize);
        nativeFileInput.addEventListener('change', updateMetadataButtonState);
    }

    if (visibilityScopeSelect) {
        visibilityScopeSelect.addEventListener('change', function () {
            const nextScope = visibilityScopeSelect.value;
            const currentRank = visibilityScopeRank(currentVisibilityScope);
            const nextRank = visibilityScopeRank(nextScope);

            if (isEdit && nextRank < currentRank && visibilityReductionConfirmMessage) {
                const shouldContinue = window.confirm(visibilityReductionConfirmMessage);
                if (!shouldContinue) {
                    visibilityScopeSelect.value = currentVisibilityScope;
                    return;
                }
            }

            notifyVisibilityScopeChange(currentVisibilityScope, nextScope);
            currentVisibilityScope = nextScope;
        });
    }

    form.addEventListener('submit', (event) => {
        syncProviderTypeInput();
        if (!validateUploadFileSize()) {
            event.preventDefault();
        }
    });

    if (storageDestinationSelect && providerTypeInput.value) {
        storageDestinationSelect.value = providerTypeInput.value;
    }

    applyMode(selectedMode());
    updateMetadataButtonState();

    function setMetadataSuggestionVisible(visible) {
        if (!metadataSuggestionBox) {
            return;
        }
        metadataSuggestionBox.hidden = !visible;

        if (visible) {
            metadataSuggestionBox.classList.remove('vt-is-hidden');
        } else {
            metadataSuggestionBox.classList.add('vt-is-hidden');
        }
    }

    function applySuggestedMetadataField(field) {
        const input = field === 'title' ? $('title') : $('description');
        const checkbox = field === 'title' ? $('inheritTitleMetadata') : $('inheritDescriptionMetadata');
        const value = suggestedMetadata[field] || '';

        if (input) {
            input.value = value;
            input.dispatchEvent(new Event('input', { bubbles: true }));
            input.dispatchEvent(new Event('change', { bubbles: true }));
        }

        if (checkbox && checkbox.checked) {
            checkbox.checked = false;
            if (typeof syncDisabled === 'function') {
                syncDisabled(checkbox, input);
            }
        }
    }

    async function fetchMetadataSuggestion() {
        if (!metadataUrl) {
            return;
        }

        const payload = new FormData(form);
        const response = await fetch(metadataUrl, {
            method: 'POST',
            body: payload,
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('metadata request failed');
        }

        suggestedMetadata = await response.json();
        if (metadataSuggestionSource) {
            metadataSuggestionSource.textContent = selectedMode() === 'external'
                ? msgMetaSrcExt
                : selectedMode() === 'resources'
                    ? msgMetaSrcRes
                    : msgMetaSrcLoc;
        }
        if (metadataSuggestionTitle) {
            metadataSuggestionTitle.textContent = suggestedMetadata.title || msgMetaNoTitle;
        }
        if (metadataSuggestionDescription) {
            metadataSuggestionDescription.textContent = suggestedMetadata.description || msgMetaNoDesc;
        }
        setMetadataSuggestionVisible(true);
    }

    if (metadataButton) {
        metadataButton.addEventListener('click', async () => {
            if (metadataButton.disabled) {
                return;
            }
            try {
                await fetchMetadataSuggestion();
            } catch (error) {
                window.alert(msgMetaError);
            }
        });
    }

    if (sourceReferenceInput) {
        sourceReferenceInput.addEventListener('input', updateMetadataButtonState);
    }

    if (existingResourceSelect) {
        existingResourceSelect.addEventListener('change', updateMetadataButtonState);
    }

    if (storageDestinationSelect) {
        storageDestinationSelect.addEventListener('change', updateMetadataButtonState);
    }

    if (applySuggestedTitleButton) {
        applySuggestedTitleButton.addEventListener('click', () => applySuggestedMetadataField('title'));
    }

    if (applySuggestedDescriptionButton) {
        applySuggestedDescriptionButton.addEventListener('click', () => applySuggestedMetadataField('description'));
    }

    function $(id) { return document.getElementById(id); }
    function syncDisabled(checkbox, input) {
        if (!checkbox || !input) return;
        input.readOnly = checkbox.checked;
        if (checkbox.checked) {
            input.classList.add('vt-readonly');
        } else {
            input.classList.remove('vt-readonly');
        }
    }

    document.addEventListener('DOMContentLoaded', function() {
        var titleCheckbox = $('inheritTitleMetadata');
        var descCheckbox = $('inheritDescriptionMetadata');
        var titleInput = $('title');
        var descInput = $('description');

        if (titleCheckbox) {
            syncDisabled(titleCheckbox, titleInput);
            titleCheckbox.addEventListener('change', function() { syncDisabled(titleCheckbox, titleInput); });
        }

        if (descCheckbox) {
            syncDisabled(descCheckbox, descInput);
            descCheckbox.addEventListener('change', function() { syncDisabled(descCheckbox, descInput); });
        }

        var searchInput = $('categorySearchInput');
        var categoryRows = document.querySelectorAll('.vt-category-row');
        var checkboxes = document.querySelectorAll('.category-checkbox');
        var countBadge = $('selectedCategoryCount');
        var previewContainer = $('selectedCategoriesPreview');

        function updateSelectedCategories() {
            var count = 0;
            if (previewContainer) previewContainer.innerHTML = '';
            checkboxes.forEach(function(cb) {
                if (cb.checked) {
                    count++;
                    var name = cb.nextElementSibling.textContent;
                    var badge = document.createElement('span');
                    badge.textContent = name;
                    badge.className = 'badge bg-primary';
                    if (previewContainer) previewContainer.appendChild(badge);
                }
            });
            if (countBadge) countBadge.textContent = count;
        }

        if (checkboxes.length > 0) {
            checkboxes.forEach(function(cb) {
                cb.addEventListener('change', updateSelectedCategories);
            });
            updateSelectedCategories();
        }

        if (searchInput) {
            searchInput.addEventListener('input', function(e) {
                var term = e.target.value.toLowerCase();

                categoryRows.forEach(function(row) {
                    var rootSpan = row.querySelector('.vt-category-root .category-name');
                    var rootName = rootSpan ? rootSpan.textContent.toLowerCase() : '';
                    var rowVisible = false;

                    if (rootName.includes(term)) {
                        rowVisible = true;
                        row.querySelectorAll('.vt-category-children .form-check').forEach(function(child) {
                            child.style.display = 'block';
                        });
                    } else {
                        var hasVisibleChild = false;
                        row.querySelectorAll('.vt-category-children .form-check').forEach(function(child) {
                            var childSpan = child.querySelector('.category-name');
                            var childName = childSpan ? childSpan.textContent.toLowerCase() : '';

                            if (childName.includes(term)) {
                                child.style.display = 'block';
                                hasVisibleChild = true;
                            } else {
                                child.style.display = 'none';
                            }
                        });
                        rowVisible = hasVisibleChild;
                    }

                    row.style.display = rowVisible ? 'block' : 'none';
                });
            });
        }

        var selectElementStorageDestination = document.getElementById("storageDestination");
        if (selectElementStorageDestination) {
            var selectElementSelectedOption = selectElementStorageDestination.querySelector('option[selected]');
            if (selectElementSelectedOption) {
                selectElementStorageDestination.value = selectElementSelectedOption.value;
                selectElementStorageDestination.dispatchEvent(new Event('change'));
            }
        }
    });
})();
