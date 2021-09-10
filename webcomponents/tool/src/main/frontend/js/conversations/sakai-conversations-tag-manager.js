import { html } from "../assets/@lion/core/index.js";
import { SakaiElement } from "../sakai-element.js";
import "../sakai-icon.js";

export class SakaiConversationsTagManager extends SakaiElement {

  static get properties() {

    return {
      siteId: { attribute: "site-id", type: String },
      tags: { type: Array },
      tagsBeingEdited: { attribute: false, type: Array },
      saveable: { attribute: false, type: Boolean },
    };
  }

  constructor() {

    super();

    this.tagsBeingEdited = [];

    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  shouldUpdate() {
    return this.i18n;
  }

  createTags() {

    const field = this.querySelector("#tag-creation-field");
    const tagLabels = field?.value?.split(",").map(s => s.trim());

    if (!tagLabels?.length) return;

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
    })
    .catch (error => {
      console.error(error);
    });
  }

  editTag(e) {

    this.tagsBeingEdited.push(parseInt(e.target.dataset.tagId));
    this.requestUpdate();
  }

  cancelTagEditing(e) {

    const index = this.tagsBeingEdited.indexOf(e.target.dataset.tagId);
    this.tagsBeingEdited.splice(index, 1);
    this.requestUpdate();
  }

  saveTag(e) {

    const id = e.target.dataset.tagId;
    const label = this.querySelector(`#tag-${id}-editor`)?.value;

    const tag = this.tags.find(t => t.id == id);
    tag && (tag.label = label);

    const url = `/api/sites/${this.siteId}/conversations/tags/${id}`;
    fetch(url, {
      method: "PUT",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(tag),
    })
    .then(r => {

      if (!r.ok) {
        throw new Error("Network error while saving tag.");
      } else {
        this.cancelTagEditing(e);
        this.dispatchEvent(new CustomEvent("tag-updated", { detail: { tag }, bubbles: true }));
      }
    })
    .catch (error => {
      console.error(error);
    });
  }

  deleteTag(e) {

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

  render() {

    return html`
      <div class="add-topic-wrapper">
        <h1>Manage Tags</h1>
        <div class="add-topic-label">Tags</div>
        <div id="tag-creation-block" style="flex-wrap: wrap;">
          <div>
            <textarea id="tag-creation-field" @input=${() => this.saveable = true}></textarea>
            <div id="tag-creation-instruction" class="topic-option-label-text">Add multiple tags separated by a comma</div>
          </div>
          <div class="act" style="white-space: nowrap;">
            <input type="button" @click=${this.cancel} value="${this.i18n.cancel}" ?disabled=${!this.saveable}>
            <input type="button" class="active" @click=${this.createTags} value="Add New Tags" ?disabled=${!this.saveable}>
          </div>
        </div
        <div id="current-tags">
          ${this.tags.map(tag => html`
          <div class="tag-row">
            <div class="tag-label">${tag.label}</div>
            <div>
              <div class="tag-buttons">
                <input type="button" data-tag-id="${tag.id}" @click=${this.editTag} value="${this.i18n.edit}">
                <input type="button" data-tag-id="${tag.id}" @click=${this.deleteTag} value="${this.i18n.delete}">
              </div>
            </div>
          </div>
          ${this.tagsBeingEdited.includes(tag.id) ? html`
          <div class="add-topic-label">Tag Name</div>
          <div class="tag-editor">
            <div><input id="tag-${tag.id}-editor" type="text" value="${tag.label}"></div>
            <div class="act">
              <input type="button" class="active" data-tag-id="${tag.id}" @click=${this.saveTag} value="${this.i18n.save}">
              <input type="button" data-tag-id="${tag.id}" @click=${this.cancelTagEditing} value="${this.i18n.cancel}">
            </div>
          </div>
          ` : ""}
          `)}
        </div>
      </div>
    `;
  }
}

const tagName = "sakai-conversations-tag-manager";
!customElements.get(tagName) && customElements.define(tagName, SakaiConversationsTagManager);
