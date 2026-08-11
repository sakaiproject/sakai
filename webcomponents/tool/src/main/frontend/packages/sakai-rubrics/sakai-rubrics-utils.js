import "./sakai-rubric-student.js";

window.rubrics = window.rubrics || {};
window.rubrics.utils = window.rubrics.utils || {

  lightbox: null,

  initLightbox(i18n) {

    if (this.lightbox?.isConnected) {
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
            <div class="modal-body"></div>
          </div>
        </div>
      </div>
    `;

    this.lightbox = tpl.content.firstElementChild;
    document.body.prepend(this.lightbox);
    this.lightbox.addEventListener("hidden.bs.modal", () => this.closeLightbox());
  },

  getRubricElement() {
    return this.lightbox?.querySelector("sakai-rubric-student");
  },

  closeLightbox() {

    const el = this.getRubricElement();
    if (!el) {
      return;
    }

    el.handleClose();
    el.remove();
  },

  showRubric(config) {

    const modalBody = this.lightbox?.querySelector(".modal-body");
    if (!modalBody) {
      return;
    }

    this.getRubricElement()?.handleClose();

    const el = document.createElement("sakai-rubric-student");
    modalBody.replaceChildren(el);
    el.loadRubric(config);

    bootstrap.Modal.getOrCreateInstance(this.lightbox).show();
  }
};
