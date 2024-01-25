import { html } from "lit";

export const reactionsMixin = Base => class extends Base {

  renderMyReactions(myReactions) {

    const reactionIcons = {
      "LOVE_IT": "heart",
      "GOOD_IDEA": "lightbulb",
      "KEY": "key",
    };

    return html`
      ${Object.entries(myReactions).map(pair => html`
        ${pair[0] !== "GOOD_QUESTION" && pair[0] !== "GOOD_ANSWER" && pair[0] !== "GOOD_COMMENT" ? html`
        <li>
          <button class="dropdown-item ${pair[1] ? "reaction-on" : undefined}"
              type="button"
              data-reaction="${pair[0]}"
              @click=${this.toggleReaction}
              title="${this._i18n[pair[0]]}"
              aria-label="${this._i18n[pair[0]]}">
            <div>
              <sakai-icon type="${reactionIcons[pair[0]]}"
                  size="small"
                  class="${reactionIcons[pair[0]]}">
              </sakai-icon>
            </div>
          </button>
        </li>
        ` : ""}
      `)}
    `;
  }

  renderReactionsBar(reactionTotals) {

    const reactionIcons = {
      "LOVE_IT": "heart",
      "GOOD_IDEA": "lightbulb",
      "KEY": "key",
    };

    return html`
      <div class="topic-message-reactions-bar" tabindex="0">
      ${Object.entries(reactionTotals).map(pair => html`
        ${pair[0] !== "GOOD_QUESTION" && pair[0] !== "GOOD_ANSWER" && pair[0] !== "GOOD_COMMENT" && pair[1] > 0 ? html`
        <div class="topic-reaction">
          <div>
            <sakai-icon type="${reactionIcons[pair[0]]}" size="small" class="${reactionIcons[pair[0]]}"></sakai-icon>
          </div>
          ${this.isInstructor ? html`
          <div>
          ${pair[1]}
          </div>
          ` : ""}
        </div>
        ` : ""}
      `)}
      </div>
    `;
  }

  toggleReaction(e) {

    e.stopPropagation();

    const reaction = e.target.dataset.reaction;

    this.myReactions[reaction] = !this.myReactions[reaction];
    this._postReactions();
  }
};
