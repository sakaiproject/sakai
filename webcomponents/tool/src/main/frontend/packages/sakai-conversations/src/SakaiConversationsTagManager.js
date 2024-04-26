import { html, nothing } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";

export class SakaiConversationsTagManager extends SakaiElement {

  static properties = {

    siteId: { attribute: "site-id", type: String },
    tags: { type: Array },

    _tagsBeingEdited: { state: true },
    _saveable: { state: true },
  };

  constructor() {

    super();

    this._tagsBeingEdited = [];

    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  _createTags() {

    const field = this.querySelector("#tag-creation-field");

    // Split it, trim it, filter it and use a Set to make them unique, no duplicates!
    const tagLabels
      = [ ...new Set(field?.value?.split(",").map(t => t.trim()).filter(t => t.length > 0)) ];

    // If any tags are already defined, ignore them
    this.tags.map(t => t.label).forEach(t => {

      const i = tagLabels.indexOf(t);
      (i !== -1) && tagLabels.splice(i, 1);
    });

    if (!tagLabels?.length) {
      field.value = "";
      return;
    }

    const tagsData = tagLabels.map(label => ({ label, siteId: this.siteId }));

    const url = `/api/sites/${this.siteId}/conversations/tags`;
    fetch(url, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(tagsData),
    })
    .then(r => {

      if (!r.ok) {
        throw new Error("Network error while creating tags.");
      }
      return r.json();
    })
    .then(tags => {

      field.value = "";
      this.dispatchEvent(new CustomEvent("tags-created", { detail: { tags }, bubbles: true }));
      this._saveable = false;
    })
    .catch (error => console.error(error));
  }

  _editTag(e) {

    this._tagsBeingEdited.push(parseInt(e.target.dataset.tagId));
    this.requestUpdate();
  }

  _cancelTagEditing(e) {

    const index = this._tagsBeingEdited.indexOf(e.target.dataset.tagId);
    this._tagsBeingEdited.splice(index, 1);
    this.requestUpdate();
  }

  _saveTag(e) {

    const id = e.target.dataset.tagId;
    const label = this.querySelector(`#tag-${id}-editor`)?.value;

    const body = this.tags.find(t => t.id == id);
    body && (body.label = label);

    const url = `/api/sites/${this.siteId}/conversations/tags/${id}`;
    fetch(url, {
      method: "PUT",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error("Network error while saving tag.");
    })
    .then(tag => {

      this._cancelTagEditing(e);
      this.dispatchEvent(new CustomEvent("tag-updated", { detail: { tag }, bubbles: true }));
    })
    .catch (error => console.error(error));
  }

  _deleteTag(e) {

    if (!confirm("Deleting this tag will remove it from all the topics in this site. Continue?")) {
      return;
    }

    const id = e.target.dataset.tagId;

    const url = `/api/sites/${this.siteId}/conversations/tags/${id}`;
    fetch(url, {
      credentials: "include",
      method: "DELETE",
    })
    .then(r => {

      if (!r.ok) {
        throw new Error(`Network error while deleting tag ${id}`);
      } else {
        this.dispatchEvent(new CustomEvent("tag-deleted", { detail: { id }, bubbles: true }));
      }
    })
    .catch(error => {
      console.error(error);
    });
  }

  _setSaveable() { this._saveable = true; }

  shouldUpdate() {
    return this.i18n;
  }

  render() {

    return html`
      <div class="add-topic-wrapper">
        <h1>${this.i18n.manage_tags}</h1>
        <div class="add-topic-label">${this.i18n.tags}</div>
        <div>
          <div>
            <textarea id="tag-creation-field"
                aria-labelledby="tag-creation-instruction"
                @input=${this._setSaveable}></textarea>
            <div id="tag-creation-instruction" class="topic-option-label-text">${this.i18n.add_new_tags_instruction}</div>
          </div>
        </div>
        <div class="act">
          <button type="button" class="btn btn-secondary" @click=${this.cancel} ?disabled=${!this._saveable}>${this.i18n.cancel}</button>
          <button type="button" class="btn btn-primary" @click=${this._createTags} ?disabled=${!this._saveable}>${this.i18n.add_new_tags}</button>
        </div>
        <div id="current-tags">
          ${this.tags.map(tag => html`
          <div class="tag-row">
            <div class="tag-label">${tag.label}</div>
            <div>
              <div class="tag-buttons">
                <input type="button" data-tag-id="${tag.id}" @click=${this._editTag} value="${this.i18n.edit}">
                <input type="button" data-tag-id="${tag.id}" @click=${this._deleteTag} value="${this.i18n.delete}">
              </div>
            </div>
          </div>
          ${this._tagsBeingEdited.includes(tag.id) ? html`
          <div class="add-topic-label">${this.i18n.tag_name}</div>
          <div class="tag-editor">
            <div><input id="tag-${tag.id}-editor" type="text" value="${tag.label}"></div>
            <div class="act">
              <input type="button" class="active" data-tag-id="${tag.id}" @click=${this._saveTag} value="${this.i18n.save}">
              <input type="button" data-tag-id="${tag.id}" @click=${this._cancelTagEditing} value="${this.i18n.cancel}">
            </div>
          </div>
          ` : nothing }
          `)}
        </div>
      </div>
    `;
  }
}
