import { html, nothing } from "lit";

export const TopicMenuMixin = Base => class extends Base {

  _editTopic(e) {

    //e.preventDefault();
    //e.stopPropagation();
    console.log("asdfasdfasdfsdfasdfasdf");
    this.dispatchEvent(new CustomEvent("edit-topic", { detail: { topic: this.topic }, bubbles: true }));
  }

  _deleteTopic() {

    if (!confirm(this._i18n.confirm_topic_delete)) return;

    const url = this.topic.links.find(l => l.rel === "delete").href;
    fetch(url, { method: "DELETE" })
    .then(r => {

      if (r.ok) {
        this.dispatchEvent(new CustomEvent("topic-deleted", { detail: { topic: this.topic }, bubbles: true }));
      } else {
        throw new Error(`Network error while deleting topic with id ${this.topic.id}`);
      }
    })
    .catch(error => console.error(error));
  }

  _toggleHidden() {

    const url = this.topic.links.find(l => l.rel === "hide").href;
    fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(!this.topic.hidden),
    })
    .then(r => {

      if (r.ok) {
        this.topic.hidden = !this.topic.hidden;
        this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
      } else {
        throw new Error("Network error while hiding/showing topic");
      }
    })
    .catch(error => console.error(error));
  }

  _toggleLocked() {

    const url = this.topic.links.find(l => l.rel === "lock").href;
    fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(!this.topic.locked),
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error("Network error while locking/unlocking topic");

    })
    .then(topic => {

      if (this._getPosts) {
        this._getPosts(topic).then(posts => {

          topic.posts = posts;
          this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic }, bubbles: true }));
        });
      } else {
        this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic }, bubbles: true }));
      }
    })
    .catch(error => console.error(error));
  }

  _renderMenu() {

    return html`
      <div class="topic-options-menu">
        ${this.topic.canModerate || this.topic.canEdit || this.topic.canDelete || this.topic.canViewStatistics ? html`
          <div class="dropdown">
            <button class="btn btn-icon btn-sm"
                id="topic-options-toggle-${this.topic.id}"
                type="button"
                title="${this._i18n.topic_options_menu_tooltip}"
                data-bs-toggle="dropdown"
                aria-expanded="false"
                aria-haspopup="true"
                aria-label="${this._i18n.topic_options_menu_tooltip}">
              <sakai-icon type="menu" size="small"></sakai-icon>
            </button>
            <ul class="dropdown-menu conv-dropdown-menu"
                aria-labelledby="topic-options-toggle-${this.topic.id}">

              ${this.topic.canEdit ? html`
              <li>
                <button type="button"
                    class="dropdown-item"
                    @click=${this._editTopic}
                    aria-label="${this._i18n.edit_topic_tooltip}"
                    title="${this._i18n.edit_topic_tooltip}">
                  ${this._i18n.edit}
                </button>
              </li>
              ` : nothing }

              ${this.topic.canDelete ? html`
              <li>
                <button type="button"
                    class="dropdown-item"
                    @click=${this._deleteTopic}
                    aria-label="${this._i18n.delete_topic_tooltip}"
                    title="${this._i18n.delete_topic_tooltip}">
                  ${this._i18n.delete}
                </button>
              </li>
              ` : nothing }

              ${this.topic.canModerate ? html`
              <li>
                <button type="button"
                    class="dropdown-item"
                    aria-label="${this._i18n[this.topic.hidden ? "show_topic_tooltip" : "hide_topic_tooltip"]}"
                    title="${this._i18n[this.topic.hidden ? "show_topic_tooltip" : "hide_topic_tooltip"]}"
                    @click=${this._toggleHidden}>
                  ${this._i18n[this.topic.hidden ? "show" : "hide"]}
                </button>
              </li>
              <li>
                <button type="button"
                    class="dropdown-item"
                    aria-label="${this._i18n[this.topic.locked ? "unlock_topic_tooltip" : "lock_topic_tooltip"]}"
                    title="${this._i18n[this.topic.locked ? "unlock_topic_tooltip" : "lock_topic_tooltip"]}"
                    @click=${this._toggleLocked}>
                  ${this._i18n[this.topic.locked ? "unlock" : "lock"]}
                </button>
              </li>
              ` : nothing }
              ${this.topic.canViewStatistics ? html`
              <li>
                <button type="button"
                    class="dropdown-item"
                    @click=${this.showStatistics}>
                  ${this._i18n.view_statistics}
                </button>
              </li>
              ` : nothing }
            </ul>
          </div>
        ` : nothing }
      </div>
    `;
  }
};
