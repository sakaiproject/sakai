var portal = portal || {};
portal.allsites = { pinnedQueue: { autoFavoritesEnabled: false, favoriteSiteIds: [] } };

portal.allsites.resetSearch = function () {

  const searchInput = document.getElementById("search-all-sites");
  searchInput && (searchInput.value = "");
  searchInput?.focus();
  document.querySelectorAll(".fav-sites-term, .fav-sites-entry").forEach(el => el.style.display = "initial");
  const noResults = document.getElementById("no-search-results");
  noResults && (noResults.style.display = "none");
}

portal.allsites.getUserFavourites = function () {

  return new Promise((resolve, reject) => {

    const url = "/portal/favorites/list";
    fetch(url, { credentials: "include" })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error(`Network error while getting favourites list from url ${url}`);
    })
    .then(data => resolve(data.favoriteSiteIds.filter(e => e)))
    .catch (error => console.error(error));
  });
};

portal.allsites.updatePinned = function () {

  const data = new URLSearchParams();
  data.append("userFavorites", JSON.stringify(portal.allsites.pinnedQueue));

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

portal.allsites.setAllOrNoneStarStates = function () {

  const modal = document.getElementById("select-site-modal");

  modal.querySelectorAll(".favorites-select-all-none").forEach(function (selectAllNone) {

    const termContainer = selectAllNone.closest(".fav-sites-term");

    const siteCount = termContainer.querySelectorAll(".fav-sites-entry:not(.my-workspace)").length;
    const favoritedSiteCount = termContainer.querySelectorAll(".site-favorite-btn[aria-pressed='true']").length;

    if (siteCount === 0) {
      // No favoritable sites under this section
      selectAllNone.style.display = "none";
    } else {
      if (favoritedSiteCount === siteCount) {
        selectAllNone.dataset.favoriteState = "favorite";
        selectAllNone.setAttribute("aria-pressed", "true");
        selectAllNone.querySelector(".bi-pin-fill").style.display = "inline";
        selectAllNone.querySelector(".bi-pin").style.display = "none";
      } else {
        selectAllNone.dataset.favoriteState = "nonfavorite";
        selectAllNone.querySelector(".bi-pin-fill").style.display = "none";
        selectAllNone.querySelector(".bi-pin").style.display = "inline";
        selectAllNone.setAttribute("aria-pressed", true);
      }

      selectAllNone.style.display = "initial";
    }
  });
};

portal.allsites.setButton = function (btn, state) {

  btn.dataset.favoriteState = state;

  if (state === "favorite") {
    btn.setAttribute("aria-pressed", "true");
    btn.querySelector(".bi-pin-fill").style.display = "inline";
    btn.querySelector(".bi-pin").style.display = "none";
  } else if (state === "nonfavorite") {
    btn.setAttribute("aria-pressed", "false");
    btn.querySelector(".bi-pin-fill").style.display = "none";
    btn.querySelector(".bi-pin").style.display = "inline";
  } else {
    btn.removeAttribute("aria-pressed");
  }

  btn.style.display = "initial";
};

portal.allsites.loadFromServer = function (attempt) {

  const pinButtons = document.querySelectorAll("#select-site-modal .site-favorite-btn");
  pinButtons.forEach(el => el.style.display = "none");

  portal.allsites.getUserFavourites().then(favourites => {

    if (!portal.allsites.initialFavourites) {
      portal.allsites.initialFavourites = favourites;
      portal.allsites.pinnedQueue.favoriteSiteIds = favourites;
    }

    portal.allsites.itemsBySiteId = {};
    pinButtons.forEach(btn => {

      const buttonSiteId = btn.dataset.siteId;

      if (btn.closest(".my-workspace")) {
        portal.allsites.setButton(btn, 'myworkspace');
      } else {
        if (favourites.includes(buttonSiteId)) {
          portal.allsites.setButton(btn, 'favorite');
        } else {
          portal.allsites.setButton(btn, 'nonfavorite');
        }
        portal.allsites.itemsBySiteId[buttonSiteId] = btn.parentElement;
      }
    });

    portal.allsites.setAllOrNoneStarStates();
  });
};

portal.allsites.setup = function () {

  const modal = document.getElementById("select-site-modal");

  modal.querySelectorAll("button, a")[0]?.focus();

  portal.allsites.loadFromServer();

  modal.querySelectorAll(".site-favorite-btn").forEach(btn => {

    btn.addEventListener("click", function () {

      const self = this;

      const siteId = self.dataset.siteId;
      const originalState = self.dataset.favoriteState;

      if (originalState === 'myworkspace') {
        // No unfavoriting your workspace!
        return;
      }

      const newState = originalState === 'favorite' ? "nonfavorite" : "favorite";

      portal.allsites.setButton(self, newState);
      if (newState === "favorite") {
        portal.allsites.pinnedQueue.favoriteSiteIds.push(siteId);
      } else {
        const index = portal.allsites.pinnedQueue.favoriteSiteIds.indexOf(siteId);
        if (index !== -1) {
          portal.allsites.pinnedQueue.favoriteSiteIds.splice(index, 1);
        }
      }

      portal.allsites.updateTimeoutId && clearTimeout(portal.allsites.updateTimeoutId);
      portal.allsites.updateTimeoutId = setTimeout(portal.allsites.updatePinned, 500);
      portal.allsites.setAllOrNoneStarStates();
    });
  });

  modal.querySelectorAll(".favorites-select-all-none").forEach(btn => {

    btn.addEventListener("click", function (e) {

      const state = this.dataset.favoriteState;
      const buttons = this.closest('.fav-sites-term').querySelectorAll(".fav-sites-entry:not(.my-workspace) .site-favorite-btn");

      var newState;

      if (state === 'favorite') {
        newState = 'nonfavorite';
      } else {
        newState = 'favorite';
      }

      buttons.forEach(button => portal.allsites.setButton(button, newState));

      portal.allsites.pinnedQueue.favoriteSiteIds
        = Array.from(modal.querySelectorAll(".site-favorite-btn[aria-pressed='true']"))
          .map(btn => btn.dataset.siteId);
      portal.allsites.updatePinned();

      portal.allsites.setAllOrNoneStarStates();
    });
  });

  const list = document.getElementById('organize-favorites-list');

  const update = () => {

    const items = Array.from(list.querySelectorAll(".organize-favorite-item"));
    portal.allsites.pinnedQueue.favoriteSiteIds = items.map(el => el.dataset.siteId);

    items.forEach((item, index) => {
      if (index === 0) {
        item.querySelector(".up-btn").classList.add("d-none");
        item.querySelector(".down-btn").classList.remove("d-none");
      } else if (index < (items.length - 1)) {
        item.querySelector(".up-btn").classList.remove("d-none");
        item.querySelector(".down-btn").classList.remove("d-none");
      } else {
        item.querySelector(".up-btn").classList.remove("d-none");
        item.querySelector(".down-btn").classList.add("d-none");
      }
    });

    portal.allsites.updatePinned();
  };

  const sortable = Sortable.create(list, { 
    dataIdAttr: "data-sortable-id",
    onUpdate: e => update(),
  });

  list.querySelectorAll(".up-btn, .down-btn").forEach(btn => {

    btn.addEventListener("click", e => {

      const order = sortable.toArray();
      const sortableId = e.target.closest(".organize-favorite-item").dataset.sortableId;
      const index = order.indexOf(sortableId);

      order.splice(index, 1);
      if (e.target.classList.contains("up-btn")) {
        order.splice(index - 1, 0, sortableId);
      } else if (e.target.classList.contains("down-btn")) {
        order.splice(index + 1, 0, sortableId);
      }

      sortable.sort(order, true);
      update();
    });
  });

  const numPinned = list.querySelectorAll("li").length;
  //document.getElementById('no-pinned-to-show').style.display = numPinned ? "none" : "block";
  //document.getElementById('pinned-to-show').style.display = numPinned ? "block" : "none";

  // Setup the search field
  const searchInput = modal.querySelector("#search-all-sites");
  searchInput?.addEventListener("keyup", event => {

    if (event.keyCode == 27) {
      portal.allsites.resetSearch();
    }

    if (searchInput.value.length > 0) {
      const queryString = searchInput.value.toLowerCase();

      document.querySelectorAll(".fav-sites-term, .fav-sites-entry")
        .forEach(el => el.style.display = "none");

      const matchedSites = Array.from(document.querySelectorAll(".fav-sites-entry")).filter(el => {
        return el.querySelector(".fav-title a span.fullTitle").textContent.toLowerCase().indexOf(queryString) >= 0;
      });

      matchedSites.forEach(el => {

        el.style.display = "initial";
        const term = el.closest(".fav-sites-term");
        term && (term.style.display = "initial");
      });

      document.getElementById("all-sites-no-search-results").style.display = matchedSites.length === 0 ? "initial" : "none";
    } else {
      portal.allsites.resetSearch();
    }
  });
};

document.addEventListener("DOMContentLoaded", () => {

  document.getElementById("select-site-modal")?.addEventListener("shown.bs.modal", function() {

    portal.allsites.setup();
  });
});
