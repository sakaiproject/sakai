import { html } from "../assets/lit-element/lit-element.js";

export const reactionsMixin = Base => class extends Base {

  renderMyReactions(myReactions) {

    const reactionIcons = {
      "LOVE_IT": "heart",
      "GOOD_IDEA": "lightbulb",
      "KEY": "key",
    };

    return html`
      ${Object.entries(myReactions).map(pair => html`
        ${pair[0] !== "GOOD_QUESTION" && pair[0] !== "GOOD_ANSWER"  && pair[0] !== "GOOD_COMMENT" ? html`
        <div class="${pair[1] ? "reaction-on" : ""}">
          <div>
            <a href="javascript:;"
                @click=${this.toggleReaction}
                title="${this.i18n[pair[0]]}"
                aria-label="${this.i18n[pair[0]]}">
              <sakai-icon type="${reactionIcons[pair[0]]}"
                  data-reaction="${pair[0]}"
                  size="small"
                  class="${reactionIcons[pair[0]]}">
              </sakai-icon>
            </a>
          </div>
        </div>
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
      <div class="topic-message-reactions-bar">
      ${Object.entries(reactionTotals).map(pair => html`
        ${pair[0] !== "GOOD_QUESTION" && pair[0] !== "GOOD_ANSWER"  && pair[0] !== "GOOD_COMMENT" && pair[1] > 0 ? html`
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
    this.postReactions();
  }
};
