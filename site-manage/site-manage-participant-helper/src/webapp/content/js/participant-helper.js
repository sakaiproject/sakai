(() => {
  const form = document.querySelector("[data-role-form]");
  if (!form) {
    return;
  }

  const panels = Array.from(form.querySelectorAll("[data-role-panel]"));
  const updatePanels = () => {
    const selected = form.querySelector("[data-role-choice]:checked")?.value;
    panels.forEach(panel => {
      panel.hidden = panel.dataset.rolePanel !== selected;
    });
  };

  form.addEventListener("change", event => {
    if (event.target.matches("[data-role-choice]")) {
      updatePanels();
    }
  });

  updatePanels();
})();
