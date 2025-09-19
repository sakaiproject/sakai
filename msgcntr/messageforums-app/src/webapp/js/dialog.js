var dialogutil = dialogutil || {};

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

	dialogutil.showDiv = function(divId) {
		const $div = $("#" + divId);
		$div.show();
		$div.delay(5000, function() {
			$div.fadeOut(1000);
		});
	};

	$.fn.delay = function(time, func) {
		return this.each(function() {
			setTimeout(func, time);
		});
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

})(jQuery, dialogutil);
