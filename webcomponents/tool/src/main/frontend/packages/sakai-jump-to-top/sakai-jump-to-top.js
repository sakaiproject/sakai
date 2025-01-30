import { loadProperties } from "@sakai-ui/sakai-i18n";

// load i18n properties and initialize the component when ready
async function init() {

  const i18n = await loadProperties("jumptotop");
  let jumptotopLabel = i18n.jumptotop_title;

  // DOM Elements
  const portalMainContainer = document.querySelector(".portal-main-container");
  const jumpToTopButton = document.createElement("button");
  jumpToTopButton.title = jumptotopLabel;
  jumpToTopButton.ariaLabel = jumptotopLabel;
  jumpToTopButton.classList.add("jump-to-top");
  jumpToTopButton.insertAdjacentHTML("beforeend", "<i class=\"si si-arrow-up-circle-fill\"></i>");
  jumpToTopButton.addEventListener("click", function () {
    portalMainContainer.scrollTo({ top: 0, behavior: "smooth" });
  });
  portalMainContainer.appendChild(jumpToTopButton);
  portalMainContainer.addEventListener("scroll", function () {

    if (portalMainContainer.scrollTop > 500) {
      jumpToTopButton.classList.add("show");
    } else {
      jumpToTopButton.classList.remove("show");
    }
  });
}

export { init };
