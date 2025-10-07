window.dialogutil = window.dialogutil || {};

(function($, dialogutil) {

	const ensureModal = function(divId) {
		const modalEl = document.getElementById(divId);
		if (!modalEl) {
			return null;
		}
		const modal = bootstrap.Modal.getOrCreateInstance(modalEl, { backdrop: 'static', focus: true });
		if (!modalEl.dataset.dialogUtilInit) {
			modalEl.addEventListener('hidden.bs.modal', function() {
				const frameId = modalEl.dataset.dialogFrame;
				if (frameId) {
					const iframe = document.getElementById(frameId);
					if (iframe) {
						iframe.removeAttribute('src');
					}
				}
			});
			modalEl.dataset.dialogUtilInit = 'true';
		}
		return modal;
	};

	dialogutil.openDialog = function(divId, frameId) {
		const modal = ensureModal(divId);
		if (!modal) {
			return;
		}

		const modalEl = document.getElementById(divId);
		if (modalEl && frameId && modalEl.dataset.dialogFrame !== frameId) {
			modalEl.dataset.dialogFrame = frameId;
		}

		modal.show();
	};

	dialogutil.turnOnPortalOverlay = function() {
		// Bootstrap handles the modal overlay; no additional work required.
	};

	dialogutil.turnOffPortalOverlay = function() {
		// Legacy hook retained for backward compatibility.
	};

	dialogutil.closeDialog = function(divId, frameId) {
		const modalEl = document.getElementById(divId);
		if (!modalEl) {
			return;
		}
		const modal = bootstrap.Modal.getInstance(modalEl);
		if (modal) {
			modal.hide();
		}

		if (frameId) {
			const iframe = document.getElementById(frameId);
			if (iframe) {
				iframe.removeAttribute('src');
			}
		}
	};

	const VISIBLE_DURATION_MS = 5000;
	const FADE_DURATION_MS = 1000;

	const clearTimer = function(div, key) {
		const timerId = div.dataset[key];
		if (timerId) {
			clearTimeout(Number(timerId));
			delete div.dataset[key];
		}
	};

	const getDisplayValue = function(div) {
		if (div.dataset.dialogutilDisplay) {
			return div.dataset.dialogutilDisplay;
		}
		const computedDisplay = window.getComputedStyle(div).display;
		const defaultDisplay = computedDisplay === 'none' ? 'block' : computedDisplay;
		div.dataset.dialogutilDisplay = defaultDisplay;
		return defaultDisplay;
	};

	const scheduleHide = function(div) {
		const hideTimeout = window.setTimeout(function() {
			div.style.transition = 'opacity ' + FADE_DURATION_MS + 'ms';
			div.style.opacity = '0';
			const fadeTimeout = window.setTimeout(function() {
				div.style.display = 'none';
				div.style.removeProperty('transition');
				delete div.dataset.dialogutilFadeTimer;
			}, FADE_DURATION_MS);
			div.dataset.dialogutilFadeTimer = String(fadeTimeout);
			delete div.dataset.dialogutilHideTimer;
		}, VISIBLE_DURATION_MS);
		div.dataset.dialogutilHideTimer = String(hideTimeout);
	};

	// Show the target element briefly before fading it out, without relying on jQuery animations.
	dialogutil.showDiv = function(divId) {
		const div = document.getElementById(divId);
		if (!div) {
			return;
		}

		clearTimer(div, 'dialogutilHideTimer');
		clearTimer(div, 'dialogutilFadeTimer');
		div.style.removeProperty('transition');
		div.style.opacity = '1';
		div.style.display = getDisplayValue(div);
		// Force reflow so the fade transition restarts when scheduled.
		void div.offsetWidth;

		scheduleHide(div);
	};

	dialogutil.updateMainFrameHeight = function () {
		// Height management is handled by CSS in the Bootstrap modal implementation.
	};

	dialogutil.replaceBodyOnLoad = function (newOnLoad, contextObject) {
		if (!contextObject || !contextObject.document) {
			return;
		}
		contextObject.document.body.setAttribute('onload', newOnLoad);
	};

})(jQuery, window.dialogutil);
