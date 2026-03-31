(function () {
	function initializeVideoDetails() {
		const videoElement = document.getElementById("vt-main-player");
		const overlay = document.getElementById("pip-overlay");
		const popupBlockedWarning = document.getElementById("pip-popup-blocked-warning");

		if (!videoElement || typeof Plyr === "undefined") {
			return;
		}

		const rawUrl = videoElement.dataset.playbackUrl || videoElement.getAttribute("src") || "";
		const contentType = videoElement.dataset.contentType || "video/mp4";
		const providerType = videoElement.dataset.videoProvider || "NATIVE";

		const showOverlay = function () {
			if (!overlay) {
				return;
			}

			overlay.classList.add("is-visible");
			overlay.setAttribute("aria-hidden", "false");
		};

		const hideOverlay = function () {
			if (!overlay) {
				return;
			}

			overlay.classList.remove("is-visible");
			overlay.setAttribute("aria-hidden", "true");
			if (popupBlockedWarning) {
				popupBlockedWarning.classList.add("vt-is-hidden");
			}
		};

		const initializePlyr = function (qualityConfig) {
			return new Plyr(videoElement, {
				blankVideo: "",
				speed: { selected: 1, options: [0.5, 0.75, 1, 1.25, 1.5, 1.75, 2] },
				settings: ["quality", "speed"],
				...(qualityConfig ? { quality: qualityConfig } : {}),
			});
		};

		const initializeHlsQualityControls = function (hls) {
			const qualityLevels = Array.from(
				new Set(
					hls.levels
						.map(function (level) {
							return level && level.height ? level.height : null;
						})
						.filter(function (height) {
							return Number.isFinite(height) && height > 0;
						})
					)
				).sort(function (left, right) {
					return right - left;
				});

			if (!qualityLevels.length) {
				return null;
			}

			const initialQuality = qualityLevels[0];
			const player = initializePlyr({
				default: initialQuality,
				options: ["auto"].concat(qualityLevels),
				forced: true,
				onChange: function (newQuality) {
					if (newQuality === "auto") {
						hls.currentLevel = -1;
						return;
					}

					const selectedLevel = hls.levels.findIndex(function (level) {
						return level && level.height === newQuality;
					});
					hls.currentLevel = selectedLevel >= 0 ? selectedLevel : -1;
				},
			});

			hls.on(Hls.Events.LEVEL_SWITCHED, function (event, data) {
				if (!player || !player.elements || !player.elements.container) {
					return;
				}

				const selectedLevel = hls.levels[data.level];
				if (selectedLevel && selectedLevel.height) {
					player.elements.container.setAttribute("data-plyr-quality", String(selectedLevel.height));
				}
			});

			return player;
		};

		const bindPlayerHandlers = function (activePlayer) {
			if (!activePlayer) {
				return;
			}

			activePlayer.on("enterpip", showOverlay);
			activePlayer.on("exitpip", hideOverlay);
		};

		let player = null;
		const isManagedByHls = providerType === "HLS_UPLOAD" && Boolean(window.Hls && Hls.isSupported());
		if (providerType === "HLS_UPLOAD" && rawUrl) {
			if (isManagedByHls) {
				const hls = new Hls();
				hls.loadSource(rawUrl);
				hls.attachMedia(videoElement);
				hls.on(Hls.Events.MANIFEST_PARSED, function () {
					player = initializeHlsQualityControls(hls) || initializePlyr();
					bindPlayerHandlers(player);
				});
			} else if (videoElement.canPlayType("application/vnd.apple.mpegurl")) {
				videoElement.src = rawUrl;
				player = initializePlyr();
				bindPlayerHandlers(player);
			} else {
				player = initializePlyr();
				bindPlayerHandlers(player);
			}
		} else {
			player = initializePlyr();
			bindPlayerHandlers(player);
			if (rawUrl) {
				player.source = {
					type: "video",
					sources: [{ src: rawUrl, type: contentType }],
				};
			}
		}

		videoElement.addEventListener(
			"error",
			function () {
				if (isManagedByHls) {
					return;
				}
				videoElement.removeAttribute("crossorigin");
				if (rawUrl) {
					videoElement.src = rawUrl;
					videoElement.load();
				}
			},
			{ once: true }
		);

		document.addEventListener("click", function (event) {
			if (!(event.target instanceof Element)) {
				return;
			}

			const pipButton = event.target.closest('button[data-plyr="pip"]');
			if (!pipButton) {
				return;
			}

			showOverlay();

            let targetUrl = window.location.href;
            if (videoElement.dataset.portalUrl) {
                targetUrl = videoElement.dataset.portalUrl;
            } else if (window.self !== window.top) {
                try {
                    const parentUrl = window.parent.location.href;
                    if (parentUrl.includes('/portal/site/')) {
                        targetUrl = parentUrl;
                    }
                } catch (e) {
                    console.warn("VTM: The parent portal URL could not be accessed.");
                }
            }

            const popupWindow = window.open(targetUrl, "_blank");
			const popupBlocked =
				!popupWindow || popupWindow.closed || typeof popupWindow.closed === "undefined";

			if (popupBlocked && popupBlockedWarning) {
				popupBlockedWarning.classList.remove("vt-is-hidden");
			} else if (popupBlockedWarning) {
				popupBlockedWarning.classList.add("vt-is-hidden");
			}
		});

		videoElement.addEventListener("enterpictureinpicture", showOverlay);
		videoElement.addEventListener("leavepictureinpicture", hideOverlay);

		videoElement.addEventListener("webkitpresentationmodechanged", function () {
			if (videoElement.webkitPresentationMode === "picture-in-picture") {
				showOverlay();
			} else {
				hideOverlay();
			}
		});

	}

	if (document.readyState === "loading") {
		document.addEventListener("DOMContentLoaded", initializeVideoDetails);
	} else {
		initializeVideoDetails();
	}
})();
