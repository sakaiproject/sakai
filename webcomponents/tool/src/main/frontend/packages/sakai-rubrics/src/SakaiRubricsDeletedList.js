import { html, nothing } from "lit";
import "@sakai-ui/sakai-pager/sakai-pager.js";
import { SakaiRubricsList } from "./SakaiRubricsList.js";

export class SakaiRubricsDeletedList extends SakaiRubricsList {

  static properties = {
    _rubricIdToDelete: { state: true },
    _rubricTitleToDelete: { state: true },
    _rubricVersionToDelete: { state: true },
  };

  render() {

    if (this._loadError) {
      return html`
        ${this.renderSaveError()}
        <div class="sak-banner-error">${this._i18n.operation_failed}</div>
      `;
    }

    if (!this._rubrics) {
      return html`
        ${this.renderSaveError()}
        <div class="sak-banner-warn">${this._i18n.loading}</div>
      `;
    }

    if (this._rubrics.length === 0) {
      return html`
        ${this.renderSaveError()}
        <div class="sak-banner-info">${this._i18n.no_deleted_rubrics}</div>
      `;
    }

    return html`
      ${this.renderSaveError()}
      <div role="presentation">
        ${this._paginatedRubrics?.map(r => html`
          <div class="rubric-title">
            <div>
              <span class="rubric-name">${r.title}</span>
            </div>
            <div class="d-none d-sm-block rubric-site-title">${r.siteTitle || nothing}</div>
            <div class="d-none d-sm-block rubric-creator-name">${r.creatorDisplayName || nothing}</div>
            <div class="d-none d-sm-block">${r.formattedSoftRemovedDate || r.formattedModifiedDate || nothing}</div>
            <div class="actions">
              <div class="action-container">
                <button type="button"
                    class="btn btn-sm restore-rubric"
                    data-rubric-id="${r.id}"
                    data-rubric-version="${r.version}"
                    title="${this.tr("restore_rubric", [ r.title ])}"
                    aria-label="${this.tr("restore_rubric", [ r.title ])}"
                    @click=${this._restoreRubric}>
                  <span class="fa fa-undo" aria-hidden="true"></span>
                </button>
              </div>
              <div class="action-container">
                <button type="button"
                    class="btn btn-sm permanently-delete-rubric"
                    data-rubric-id="${r.id}"
                    data-rubric-version="${r.version}"
                    data-rubric-title="${r.title}"
                    title="${this.tr("permanently_delete", [ r.title ])}"
                    aria-label="${this.tr("permanently_delete", [ r.title ])}"
                    @click=${this._showPermanentDeleteModal}>
                  <span class="fa fa-times" aria-hidden="true"></span>
                </button>
              </div>
            </div>
          </div>
        `)}
      </div>
      <sakai-pager
        .current=${this._currentPage}
        .count=${this._totalPages}
        @page-selected=${this._onPageSelected}
        ?hidden=${this._totalPages <= 1}>
      </sakai-pager>
      <div class="modal fade" id="permanent-delete-modal" tabindex="-1" aria-labelledby="permanent-delete-modal-label" aria-hidden="true">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="permanent-delete-modal-label">${this.tr("delete_item_title", [ this._i18n.rubric ])}</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="${this.tr("close_dialog")}"></button>
            </div>
            <div class="modal-body">
              <p>${this.tr("confirm_permanent_remove", [ this._rubricTitleToDelete || "" ])}</p>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-danger" @click=${this._confirmPermanentDelete}>${this._i18n.permanently_delete_label}</button>
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">${this._i18n.cancel}</button>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  getRubrics() {

    this._loadError = false;
    const url = `/api/sites/${this.siteId}/deleted-rubrics`;
    fetch(url, {
      headers: { "Content-Type": "application/json" },
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error(`Network error while loading deleted rubrics at ${url}`);
    })
    .then(rubrics => {
      this._rubrics = rubrics;
      this._currentPage = 1;
      this.repage();
    })
    .catch(error => {
      console.error(error);
      this._loadError = true;
      this._rubrics = [];
    });
  }

  sortRubrics(rubricType, ascending) {

    this._lastSortType = rubricType;
    this._lastSortAscending = ascending;

    switch (rubricType) {
      case "name":
        this._rubrics.sort((a, b) => ascending ? a.title.localeCompare(b.title) : b.title.localeCompare(a.title));
        break;
      case "title":
        this._rubrics.sort((a, b) => ascending ? (a.siteTitle || "").localeCompare(b.siteTitle || "") : (b.siteTitle || "").localeCompare(a.siteTitle || ""));
        break;
      case "creator":
        this._rubrics.sort((a, b) => ascending ? (a.creatorDisplayName || "").localeCompare(b.creatorDisplayName || "") : (b.creatorDisplayName || "").localeCompare(a.creatorDisplayName || ""));
        break;
      case "modified":
        this._rubrics.sort((a, b) => {
          const aDate = a.softRemovedDate || a.modified || 0;
          const bDate = b.softRemovedDate || b.modified || 0;
          return ascending ? aDate - bDate : bDate - aDate;
        });
        break;
    }
    this.repage();
  }

  _removeRubricFromList(rubricId) {

    this._rubrics = this._rubrics.filter(rubric => String(rubric.id) !== String(rubricId));
    this.repage();
    if (this._currentPage > this._totalPages) {
      this._currentPage = Math.max(1, this._totalPages);
      this.repage();
    }
  }

  _restoreRubric(e) {

    e.stopPropagation();
    const rubricId = e.currentTarget.dataset.rubricId;
    const version = e.currentTarget.dataset.rubricVersion;
    const url = `/api/sites/${this.siteId}/rubrics/${rubricId}/restore`;

    fetch(url, {
      method: "POST",
      credentials: "include",
      headers: { "If-Match": `"${version}"` },
    })
    .then(r => {

      if (!r.ok) {
        const error = new Error(`Failed to restore rubric ${rubricId}`);
        error.status = r.status;
        throw error;
      }

      this._removeRubricFromList(rubricId);
      this.dispatchEvent(new CustomEvent("rubric-restored", { bubbles: true, composed: true }));
    })
    .catch(error => this.reportSaveError(error));
  }

  _showPermanentDeleteModal(e) {

    e.stopPropagation();
    this._rubricIdToDelete = e.currentTarget.dataset.rubricId;
    this._rubricVersionToDelete = e.currentTarget.dataset.rubricVersion;
    this._rubricTitleToDelete = e.currentTarget.dataset.rubricTitle;
    this.updateComplete.then(() => {
      bootstrap.Modal.getOrCreateInstance(this.querySelector("#permanent-delete-modal")).show();
    });
  }

  _confirmPermanentDelete(e) {

    e.stopPropagation();
    const url = `/api/sites/${this.siteId}/rubrics/${this._rubricIdToDelete}/permanent`;

    fetch(url, {
      method: "DELETE",
      credentials: "include",
      headers: { "If-Match": `"${this._rubricVersionToDelete}"` },
    })
    .then(r => {

      if (!r.ok) {
        const error = new Error(`Failed to permanently delete rubric ${this._rubricIdToDelete}`);
        error.status = r.status;
        throw error;
      }

      this._removeRubricFromList(this._rubricIdToDelete);
      bootstrap.Modal.getOrCreateInstance(this.querySelector("#permanent-delete-modal")).hide();
    })
    .catch(error => this.reportSaveError(error));
  }
}
