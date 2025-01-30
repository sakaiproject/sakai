window.top.rubrics = window.top.rubrics || {};
window.top.rubrics.utils = window.top.rubrics.utils || {

  lightbox: null,
  windowRef: window != window.top ? window.top : window,

  initLightbox(i18n, siteId) {

    const rubrics = window.top.rubrics;

    if (this.lightbox) {
      return;
    }

    const tpl = document.createElement("template");

    tpl.innerHTML = `
      <div id="rubric-preview" class="modal" tabindex="-1">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Rubric Preview</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="${i18n.close_dialog}"></button>
            </div>
            <div class="modal-body">
              <sakai-rubric-student site-id="${siteId}"></sakai-rubric-student>
            </div>
          </div>
        </div>
      </div>
    `;

    document.body.prepend(tpl.content);

    this.lightbox = this.windowRef.document.getElementById("rubric-preview");
  },

  closeLightbox() {

    const el = this.windowRef.document.querySelector("sakai-rubric-student");

    el.handleClose();

    el.removeAttribute("rubric-id");
    el.removeAttribute("preview");
    el.removeAttribute("tool-id");
    el.removeAttribute("entity-id");
    el.removeAttribute("evaluated-item-id");
    el.removeAttribute("instructor");
    el.removeAttribute("evaluated-item-owner-id");
    el.removeAttribute("peer-or-self");
  },

  showRubric(id, attributes, launchingElement) {

    const rubrics = this.windowRef.rubrics;

    const el = this.windowRef.document.querySelector("sakai-rubric-student");

    if (!attributes) {
      el.setAttribute("rubric-id", id);
      el.setAttribute("preview", "");
      el.removeAttribute("tool-id");
      el.removeAttribute("entity-id");
      el.removeAttribute("evaluated-item-id");
      el.removeAttribute("instructor");
      el.removeAttribute("evaluated-item-owner-id");
      el.removeAttribute("peer-or-self");
    } else {
      el.removeAttribute("rubric-id");
      if (attributes["force-preview"]) {
        el.setAttribute("force-preview", "");
      } else {
        el.removeAttribute("force-preview");
      }
      el.setAttribute("tool-id", attributes["tool-id"]);
      el.setAttribute("entity-id", attributes["entity-id"]);
      el.setAttribute("evaluated-item-id", attributes["evaluated-item-id"]);
      el.setAttribute("instructor", attributes.instructor);
      el.setAttribute("evaluated-item-owner-id", attributes["evaluated-item-owner-id"]);
      el.setAttribute("peer-or-self", attributes["peer-or-self"]);
    }

    bootstrap.Modal.getOrCreateInstance(this.lightbox).show();

    this.lightbox.addEventListener("hidden.bs.modal", () => this.closeLightbox());
  }
};
