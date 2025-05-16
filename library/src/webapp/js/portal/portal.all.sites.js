const allsites = { pinnedQueue: { favoriteSiteIds: [] } };

allsites.resetSearch = function () {

  const searchInput = document.getElementById("search-all-sites");
  searchInput && (searchInput.value = "");
  searchInput?.focus();
  document.querySelectorAll(".fav-sites-term, .fav-sites-entry").forEach(el => el.style.display = "block");
  document.querySelectorAll(".fav-sites-card").forEach(el => el.style.display = "flex");
  const noResults = document.getElementById("no-search-results");
  noResults && (noResults.style.display = "none");
}

allsites.updatePinned = function (reorder) {

  const data = new URLSearchParams();
  data.append("userFavorites", JSON.stringify(allsites.pinnedQueue));
  if (reorder) data.append("reorder", "true");

  const url = "/portal/favorites/update";
  fetch(url, {
    credentials: "include",
    headers: { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" },
    method: "POST",
    body: data,
  })
  .then(r => {

    if (!r.ok) {
      throw new Error(`Network error while updating pinned sites at url ${url}`);
    } else {
      document.getElementById("allsites-refresh-notification").style.display = "block";
    }
  })
  .catch (error => console.error(error));
};

allsites.setAllOrNoneStarStates = function () {

  const sidebar = document.getElementById("select-site-sidebar");

  sidebar.querySelectorAll(".favorites-select-all-none").forEach(function (selectAllNone) {

    const termContainer = selectAllNone.closest(".fav-sites-card");

    const siteCount = termContainer.querySelectorAll(".fav-sites-entry:not(.my-workspace)").length;
    const favoritedSiteCount = termContainer.querySelectorAll(".site-favorite-btn[aria-pressed='true']").length;

    if (siteCount === 0) {
      // No favoritable sites under this section
      selectAllNone.style.display = "none";
    } else {
      if (favoritedSiteCount === siteCount) {
        selectAllNone.dataset.pinnedState = "pinned";
        selectAllNone.setAttribute("aria-pressed", "true");
        selectAllNone.querySelector(".si-pin-fill").style.display = "inline";
        selectAllNone.querySelector(".si-pin").style.display = "none";
      } else {
        selectAllNone.dataset.pinnedState = "unpinned";
        selectAllNone.querySelector(".si-pin-fill").style.display = "none";
        selectAllNone.querySelector(".si-pin").style.display = "inline";
        selectAllNone.setAttribute("aria-pressed", true);
      }

      selectAllNone.style.display = "initial";
    }
  });
};

allsites.getPinnedSiteIds = () => {

  const sidebar = document.getElementById("select-site-sidebar");
  return Array.from(sidebar.querySelectorAll(".site-favorite-btn[aria-pressed='true']"))
      .map(btn => btn.dataset.siteId);
};

allsites.updateButton = (btn, state) => {
 
  btn.dataset.pinnedState = state;

  if (state === "pinned") {
    btn.setAttribute("aria-pressed", "true");
    btn.querySelector(".si-pin-fill").style.display = "inline";
    btn.querySelector(".si-pin").style.display = "none";
  } else if (state === "unpinned") {
    btn.setAttribute("aria-pressed", "false");
    btn.querySelector(".si-pin-fill").style.display = "none";
    btn.querySelector(".si-pin").style.display = "inline";
  } else {
    btn.removeAttribute("aria-pressed");
  }

  btn.style.display = "initial";
};

allsites.setButton = function (btn, state) {

  allsites.updateButton(btn, state);

  // TODO: this needs to be implemented at some point. It would remove the annoying 
  // refresh message in the all sites sidebar
  //document.body.dispatchEvent(new CustomEvent("site-pin-changed", { detail: { siteId: btn.dataset.siteId, pinned: state === "pinned", source: "all-sites" } }));
};

allsites.setup = function () {

  const sidebar = document.getElementById("select-site-sidebar");

  sidebar.querySelectorAll("button, a")[0]?.focus();

  allsites.setAllOrNoneStarStates();

  const organizeItems = sidebar.querySelectorAll(".organize-favorite-item");

  if (!organizeItems.length) {
    document.getElementById("pinned-to-show").classList.add("d-none");
    document.getElementById("no-pinned-to-show").classList.remove("d-none");
  }

  sidebar.querySelectorAll(".site-favorite-btn").forEach(btn => {

    btn.addEventListener("click", function () {

      const self = this;

      const siteId = self.dataset.siteId;
      const originalState = self.dataset.pinnedState;

      if (originalState === 'myworkspace') {
        // No unfavoriting your workspace!
        return;
      }

      const newState = originalState === 'pinned' ? "unpinned" : "pinned";
      allsites.setButton(self, newState);

      allsites.pinnedQueue.favoriteSiteIds = allsites.getPinnedSiteIds();

      allsites.updateTimeoutId && clearTimeout(allsites.updateTimeoutId);
      allsites.updateTimeoutId = setTimeout(allsites.updatePinned, 500);
      allsites.setAllOrNoneStarStates();
    });
  });

  sidebar.querySelectorAll(".favorites-select-all-none").forEach(btn => {

    btn.addEventListener("click", function (e) {

      const state = this.dataset.pinnedState;
      const buttons = this.closest('.fav-sites-card').querySelectorAll(".fav-sites-entry:not(.my-workspace) .site-favorite-btn");

      var newState;

      if (state === 'pinned') {
        newState = 'unpinned';
      } else {
        newState = 'pinned';
      }

      buttons.forEach(button => allsites.setButton(button, newState));

      allsites.pinnedQueue.favoriteSiteIds = allsites.getPinnedSiteIds();

      allsites.updatePinned();

      allsites.setAllOrNoneStarStates();
    });
  });

  const list = document.getElementById('organize-favorites-list');

  const update = () => {

    allsites.pinnedQueue.favoriteSiteIds
      = Array.from(list.querySelectorAll(".organize-favorite-item")).map(el => el.dataset.siteId);

    allsites.updatePinned(true);
  };

  const sortable = Sortable.create(list, { 
    dataIdAttr: "data-sortable-id",
    onUpdate: e => update(),
  });

  list.querySelectorAll(".organize-favorite-item").forEach(li => {

    li.addEventListener("keyup", e => {

      if (["e", "d"].includes(e.key.toLowerCase())) {
        const order = sortable.toArray();
        const sortableId = li.dataset.sortableId;
        const index = order.indexOf(sortableId);

        if (e.key.toLowerCase() === "e") {
          if (li.previousSibling) {
            order.splice(index, 1);
            order.splice(index - 1, 0, sortableId);
          }
        } else if (e.key.toLowerCase() === "d") {
          if (li.nextSibling) {
            order.splice(index, 1);
            order.splice(index + 1, 0, sortableId);
          }
        }

        sortable.sort(order, true);
        list.querySelector(`.organize-favorite-item[data-sortable-id='${sortableId}']`).focus();
        update();
      }
    });
  });

  const numPinned = list.querySelectorAll("li").length;

  // Setup the search field
  const searchInput = sidebar.querySelector("#search-all-sites");
  searchInput?.addEventListener("keyup", event => {

    if (event.keyCode == 27) {
      allsites.resetSearch();
    }

    if (searchInput.value.length > 0) {
      const queryString = searchInput.value.toLowerCase();

      sidebar.querySelectorAll(".fav-sites-card, .fav-sites-term, .fav-sites-entry")
        .forEach(el => el.style.display = "none");

      const matchedSites = Array.from(sidebar.querySelectorAll(".fav-sites-entry")).filter(el => {
        return el.querySelector(".fav-title a span.fullTitle").textContent.toLowerCase().indexOf(queryString) >= 0;
      });

      matchedSites.forEach(el => {

        el.style.display = "block";
        const card = el.closest(".fav-sites-card");
        card.style.display = "flex";
      });

      document.getElementById("all-sites-no-search-results").style.display = matchedSites.length === 0 ? "initial" : "none";
    } else {
      allsites.resetSearch();
    }
  });
};

document.addEventListener("DOMContentLoaded", () => {

  document.body.addEventListener("site-pin-changed", e => {

    if (e.detail.source === "all-sites") return;

    const newState = e.detail.pinned ? "pinned" : "unpinned";
    const button = document.querySelector(`#selectSite button.site-favorite-btn[data-site-id='${e.detail.siteId}']`);
    allsites.updateButton(button, newState);
    allsites.setAllOrNoneStarStates();

    if (!e.detail.pinned) {
      document.querySelector(`#organize-favorites-list li.organize-favorite-item[data-site-id='${e.detail.siteId}']`)?.remove();
    } else {
      const html = `
        <div>
          <div class="pinned-drag-handle">
            <i class="si si-drag-handle"></i>
          </div>
          <div class="ms-2">${e.detail.siteTitle}</div>
        </div>
      `;

      const li = document.createElement("li");
      li.classList.add("organize-favorite-item");
      li.tabIndex = "0";
      li.dataset.siteId = e.detail.siteId;
      li.dataset.sortableId = e.detail.siteId;
      li.innerHTML = html;

      document.getElementById("organize-favorites-list").append(li);
      document.getElementById("no-pinned-to-show").classList.add("d-none");
      document.getElementById("pinned-to-show").classList.remove("d-none");
    }
  });

  document.getElementById("select-site-sidebar")?.addEventListener("shown.bs.offcanvas", function() {

    allsites.setup();
  });
});
