document.addEventListener("show.bs.modal", event => {
  const previousFocus = document.activeElement;
  const overlay = previousFocus.closest(".gb-modal-overlay");
  const popup = event.target;
  if (!overlay || overlay.contains(popup) || event.defaultPrevented) return;

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
