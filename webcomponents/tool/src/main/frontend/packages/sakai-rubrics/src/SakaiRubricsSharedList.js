import { html } from "lit";
import "../sakai-rubric-readonly.js";
import { SakaiRubricsHelpers } from "./SakaiRubricsHelpers.js";
import { SakaiRubricsList } from "./SakaiRubricsList.js";

const rubricName = "name";
const rubricTitle = "title";
const rubricCreator = "creator";
const rubricModified = "modified";

export class SakaiRubricsSharedList extends SakaiRubricsList {

  render() {

    if (!this._rubrics) {
      return html`
        <div class="sak-banner-warn">${this._i18n.loading}</div>
      `;
    }

    return html`
      <div role="tablist">
      ${this._rubrics.map(r => html`
        <sakai-rubric-readonly .rubric=${r} @copy-to-site=${this.copyToSite} ?enablePdfExport=${this.enablePdfExport}></sakai-rubric-readonly>
      `)}
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

  copyToSite(e) {

    SakaiRubricsHelpers.get(`/api/sites/${this.siteId}/rubrics/${e.detail}/copyToSite`, {})
      .then(() => this.dispatchEvent(new CustomEvent("copy-share-site")));
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
