document.addEventListener("show.bs.modal", event => {
  if (event.defaultPrevented) return;
  const popup = event.target;
  const parentOverlay = popup.closest(".gb-modal-overlay");
  if (parentOverlay) {
    const existingBackdrops = new Set(document.querySelectorAll(".modal-backdrop"));
    // Bootstrap appends its backdrop after the show event. Keep a nested popup's
    // backdrop in the same stacking context instead of above the entire overlay.
    queueMicrotask(() => {
      if (event.defaultPrevented) return;
      const backdrop = [...document.querySelectorAll("body > .modal-backdrop")]
        .find(element => !existingBackdrops.has(element));
      if (backdrop) parentOverlay.append(backdrop);
    });
    return;
  }

  const previousFocus = document.activeElement;
  const overlay = previousFocus.closest(".gb-modal-overlay");
  if (!overlay) return;

  // Wicket must not pull focus back from a Bootstrap popup outside its overlay.
  overlay.inert = true;
  const restore = () => {
    overlay.inert = false;
    popup.removeEventListener("hidden.bs.modal", restore);
    if (previousFocus.isConnected) previousFocus.focus();
  };
  popup.addEventListener("hidden.bs.modal", restore, { once: true });

  // A later show listener may cancel opening the popup.
  queueMicrotask(() => {
    if (event.defaultPrevented) restore();
  });
});
