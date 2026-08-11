window.top.rubrics = window.top.rubrics || {};
window.top.rubrics.utils = window.top.rubrics.utils || {

  lightbox: null,
  windowRef: window != window.top ? window.top : window,

  initLightbox(i18n, siteId) {
    if (this.lightbox) {
      this.setOptionalAttribute(this.getRubricElement(), "site-id", siteId);
      return;
    }

    const tpl = document.createElement("template");

    tpl.innerHTML = `
      <div id="rubric-preview" class="modal" tabindex="-1">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">${i18n.preview_rubric}</h5>
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
    this.lightbox.addEventListener("hidden.bs.modal", () => this.closeLightbox());
  },

  getRubricElement() {
    return this.lightbox?.querySelector("sakai-rubric-student");
  },

  setOptionalAttribute(el, name, value) {

    if (value == null) {
      el.removeAttribute(name);
    } else {
      el.setAttribute(name, value);
    }
  },

  closeLightbox() {

    const el = this.getRubricElement();

    el.handleClose();

    el.removeAttribute("rubric-id");
    el.removeAttribute("preview");
    el.removeAttribute("tool-id");
    el.removeAttribute("entity-id");
    el.removeAttribute("evaluated-item-id");
    el.removeAttribute("instructor");
    el.removeAttribute("evaluated-item-owner-id");
    el.removeAttribute("force-preview");
    el.removeAttribute("is-peer-or-self");
  },

  showRubric(id, attributes) {

    const el = this.getRubricElement();

    if (!attributes) {
      this.setOptionalAttribute(el, "rubric-id", id);
      el.toggleAttribute("preview", true);
      el.removeAttribute("tool-id");
      el.removeAttribute("entity-id");
      el.removeAttribute("evaluated-item-id");
      el.removeAttribute("instructor");
      el.removeAttribute("evaluated-item-owner-id");
      el.removeAttribute("force-preview");
      el.removeAttribute("is-peer-or-self");
    } else {
      el.removeAttribute("rubric-id");
      el.removeAttribute("preview");
      el.toggleAttribute("force-preview", attributes["force-preview"] === true);
      if (!attributes["force-preview"]) {
        // If a dropdown menu of views can be selected, initialize the view with the Grading Rubric
        el.displayGradingTab();
      }
      this.setOptionalAttribute(el, "tool-id", attributes["tool-id"]);
      this.setOptionalAttribute(el, "entity-id", attributes["entity-id"]);
      this.setOptionalAttribute(el, "evaluated-item-id", attributes["evaluated-item-id"]);
      this.setOptionalAttribute(el, "evaluated-item-owner-id", attributes["evaluated-item-owner-id"]);
      el.toggleAttribute("instructor", attributes.instructor === true);
      el.toggleAttribute("is-peer-or-self", attributes["is-peer-or-self"] === true);
    }

    bootstrap.Modal.getOrCreateInstance(this.lightbox).show();
  }
};
