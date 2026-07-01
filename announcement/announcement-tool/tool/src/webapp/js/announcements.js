sakai.announcements = sakai.announcements || {

  toggleBulkButtons: disable => {

    ["remove", "publish", "unpublish"].forEach(action => {
      document.getElementById(`announcement-${action}-button`).disabled = disable;
    });
  },
};

document.querySelectorAll(".announcement-select-checkbox").forEach(cb => {

  cb.addEventListener("click", e => {

    const anySelected = (document.querySelectorAll(".announcement-select-checkbox:checked").length > 0);
    sakai.announcements.toggleBulkButtons(!anySelected);
    document.getElementById("announcement-reset-button").disabled = !anySelected;
  });
});

document.getElementById("announcement-reset-button")?.addEventListener("click", () => {
  sakai.announcements.toggleBulkButtons(true);
});

// --- Search-term highlighting ------------------------------------------------
// When a search is active, wrap the matched substring in the subject column with
// <mark> so it is obvious why each row matched. The server has already scoped the
// rows (see AnnouncementAction.filterMessagesBySearch); this is presentation only.
// Re-runs after the shared search filter swaps in fresh rows: searchFilterPanelMacro.js
// dispatches "sfp:updated" once it replaces the table body.
sakai.announcements.escapeRegExp = s => s.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");

sakai.announcements.highlightInNode = (root, rx) => {

  // Walk text nodes only, so links, icons and existing markup are left intact.
  const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT);
  const textNodes = [];
  while (walker.nextNode()) textNodes.push(walker.currentNode);

  textNodes.forEach(node => {
    const value = node.nodeValue;
    rx.lastIndex = 0;
    if (!rx.test(value)) return;

    rx.lastIndex = 0;
    const frag = document.createDocumentFragment();
    let last = 0, m;
    while ((m = rx.exec(value)) !== null) {
      if (m.index > last) frag.appendChild(document.createTextNode(value.slice(last, m.index)));
      const mark = document.createElement("mark");
      mark.className = "announcement-search-hit";
      mark.textContent = m[0];
      frag.appendChild(mark);
      last = m.index + m[0].length;
      if (m.index === rx.lastIndex) rx.lastIndex++; // never loop on a zero-length match
    }
    if (last < value.length) frag.appendChild(document.createTextNode(value.slice(last)));
    node.parentNode.replaceChild(frag, node);
  });
};

sakai.announcements.highlightSearch = () => {

  const field = document.getElementById("search-announcement");
  const table = document.getElementById("announcements-list");
  if (!field || !table) return;

  const term = field.value.trim();
  if (term.length < 3) return; // matches MIN_SEARCH_CHARS in the shared search macro

  const rx = new RegExp(sakai.announcements.escapeRegExp(term), "gi");
  table.querySelectorAll('td[headers="subject"]').forEach(cell => sakai.announcements.highlightInNode(cell, rx));
};

sakai.announcements.highlightSearch();
document.addEventListener("sfp:updated", () => sakai.announcements.highlightSearch());
