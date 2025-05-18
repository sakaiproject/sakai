import { html } from "lit";
import "../sakai-rubric-readonly.js";
import "../sakai-rubrics-list.js";
import { SakaiRubricsHelpers } from "./SakaiRubricsHelpers.js";
import { SakaiRubricsList } from "./SakaiRubricsList.js";
import { SharingChangeEvent } from "./SharingChangeEvent.js";

const rubricName = "name";
const rubricTitle = "title";
const rubricCreator = "creator";
const rubricModified = "modified";

export class SakaiRubricsSharedList extends SakaiRubricsList {

  rubricIdToDelete = null;
  rubricTitleToDelete = null;

  static properties = { isSuperUser: { attribute: "is-super-user", type: Boolean } };

  constructor() {

    super();

    this.updateRubricOptions = {
      method: "PATCH",
      headers: { "Content-Type": "application/json-patch+json" },
    };
  }

  render() {

    if (!this._rubrics) {
      return html`
        <div class="sak-banner-warn">${this._i18n.loading}</div>
      `;
    }

    return html`
      <div role="tablist">
      ${this._rubrics.map(r => html`
        <sakai-rubric-readonly .rubric=${r}
        @copy-to-site=${this.copyToSite}
        @delete-rubric=${this.showDeleteModal}
        @revoke-shared-rubric=${this.sharingChange}
        ?enablePdfExport=${this.enablePdfExport}
        ?is-super-user=${this.isSuperUser}>
        </sakai-rubric-readonly>
      `)}
      </div>
      <div class="modal fade" id="delete-modal" tabindex="-1" aria-labelledby="delete-modal-label" aria-hidden="true">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="delete-modal-label">${this._i18n.delete_item_title.replace("{}", this.rubricTitleToDelete || "")}</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
              <p>${this._i18n.confirm_remove_shared.replace("{}", this.rubricTitleToDelete || "")}</p>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-danger" @click=${this.confirmDelete}>${this._i18n.remove_label}</button>
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">${this._i18n.cancel}</button>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  getRubrics() {

    const url = "/api/rubrics/shared";
    fetch(url)
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error("Network error while getting shared rubrics");
    })
    .then(rubrics => this._rubrics = rubrics)
    .catch (error => console.error(error));
  }

  showDeleteModal(e) {

    e.stopPropagation();
    this.rubricIdToDelete = e.detail.id;
    this.rubricTitleToDelete = e.detail.title;
    this.requestUpdate();
    const modal = new bootstrap.Modal(document.getElementById("delete-modal"));
    modal.show();
  }

  copyToSite(e) {

    SakaiRubricsHelpers.get(`/api/sites/${this.siteId}/rubrics/${e.detail}/copyToSite`, {})
      .then(() => this.dispatchEvent(new CustomEvent("copy-share-site")));
  }

  confirmDelete(e) {

    e.stopPropagation();
    const url = `/api/sites/${this.siteId}/rubrics/${this.rubricIdToDelete}`;
    fetch(url, {
      method: "DELETE",
      credentials: "include",
      headers: { "Content-Type": "application/json" }
    })
    .then(r => {

      if (!r.ok) {
        throw new Error(`Failed to delete shared rubric with id ${this.rubricIdToDelete}`);
      }

      this._rubrics = this._rubrics.filter(rubric => rubric.id !== this.rubricIdToDelete);
      this.requestUpdate();
      bootstrap.Modal.getOrCreateInstance(this.querySelector(".modal")).hide();
      this.dispatchEvent(new CustomEvent("update-rubric-list"));
    })
    .catch(error => console.error(error));
  }

  sharingChange(e) {

    e.stopPropagation();
    e.detail.shared = !e.detail.shared;

    this.updateRubricOptions.body = JSON.stringify([ { "op": "replace", "path": "/shared", "value": e.detail.shared } ]);
    const url = `/api/sites/${e.detail.ownerId}/rubrics/${e.detail.id}`;
    fetch(url, this.updateRubricOptions)
    .then(r => {

      if (r.ok) {
        this.dispatchEvent(new SharingChangeEvent());
        this.refresh();
        this.dispatchEvent(new CustomEvent("update-rubric-list"));
      } else {
        throw new Error("Network error while updating rubric");
      }
    })
    .catch (error => console.error(error));
  }

  sortRubrics(rubricType, ascending) {

    switch (rubricType) {
      case rubricName:
        this._rubrics.sort((a, b) => ascending ? a.title.localeCompare(b.title) : b.title.localeCompare(a.title));
        break;
      case rubricTitle:
        this._rubrics.sort((a, b) => ascending ? a.siteTitle.localeCompare(b.siteTitle) : b.siteTitle.localeCompare(a.siteTitle));
        break;
      case rubricCreator:
        this._rubrics.sort((a, b) => ascending ? a.creatorDisplayName.localeCompare(b.creatorDisplayName) : b.creatorDisplayName.localeCompare(a.creatorDisplayName));
        break;
      case rubricModified:
        this._rubrics.sort((a, b) => ascending ? a.modified - b.modified : b.modified - a.modified);
    }
    this.requestUpdate("_rubrics");
  }
}
