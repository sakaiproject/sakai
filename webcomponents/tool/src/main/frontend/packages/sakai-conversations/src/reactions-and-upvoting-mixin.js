import { html, nothing } from "lit";

export const reactionsAndUpvotingMixin = Base => class extends Base {

  renderMyReactions(myReactions) {

    const reactionIcons = {
      "THUMBS_UP": "hand-thumbs-up",
      "LOVE_IT": "heart",
      "GOOD_IDEA": "lightbulb",
      "KEY": "key",
    };

    return html`
      ${Object.entries(myReactions).map(pair => html`
        <li>
          <button class="dropdown-item ${pair[1] ? "reaction-on" : undefined}"
              type="button"
              data-reaction="${pair[0]}"
              @click=${this.toggleReaction}
              title="${this._i18n[pair[0]]}"
              aria-label="${this._i18n[pair[0]]}">
            <i class="bi bi-${reactionIcons[pair[0]]}-fill ${reactionIcons[pair[0]]}"></i>
          </button>
        </li>
      `)}
    `;
  }

  renderReactionsBar(reactable) {

    const reactionIcons = {
      "THUMBS_UP": "hand-thumbs-up",
      "LOVE_IT": "heart",
      "GOOD_IDEA": "lightbulb",
      "KEY": "key",
    };

    return html`
      <div class="d-flex mb-2" tabindex="0">
      ${Object.entries(reactable.reactionTotals).map(pair => html`
        <div class="d-flex align-items-center border border-1 rounded-pill px-2 me-1">
          <div>
            <i class="bi bi-${reactionIcons[pair[0]]}-fill ${reactionIcons[pair[0]]} small"></i>
          </div>
          <div class="ms-1 small">${pair[1]}</div>
        </div>
      `)}
      </div>
    `;
  }

  toggleReaction(e) {

    e.stopPropagation();

    const reaction = e.currentTarget.dataset.reaction;
    this.myReactions[reaction] = !this.myReactions[reaction];
    this._postReactions();
  }

  _renderReactionsBlock(reactable) {

    return html`

      ${reactable.canReact ? html`
      <div class="conversations-action post-option single border border-1 rounded-pill px-2 py-1 me-1">
        <div class="dropdown">
          <button class="btn btn-transparent"
              id="post-reactions-${reactable.id}"
              type="button"
              @click=${this._toggleShowingMyReactions}
              data-bs-toggle="dropdown"
              aria-label="${this._i18n.reactions_tooltip}"
              aria-expanded="false"
              title="${this._i18n.reactions_tooltip}">
            <div class="d-flex align-items-center">
              <div><i class="bi bi-emoji-smile fs-6"></i></div>
              <div class="ms-1 small">${this._i18n.add_reaction}</div>
            </div>
          </button>
          <ul class="dropdown-menu conv-dropdown-menu"
              aria-labelledby="post-reactions-${reactable.id}">
            ${this.renderMyReactions(reactable.myReactions)}
          </ul>
        </div>
      </div>
      ` : nothing }
    `;
  }

  _renderUpvote(upvotable) {

    return html`
      <div class="converations-action d-flex align-items-center border border-1 rounded-pill py-1 px-2">
        ${upvotable.canUpvote ? html`
        <div><i class="si si-up"></i></div>
        ` : nothing}
        <div class="mx-1 small">${this._i18n.upvotes}</div>
        <div class="ms-1 small">${upvotable.upvotes || 0}</div>
      </div>
    `;
  }

  _renderUpvoteBlock(upvotable) {

    return upvotable.canViewUpvotes ? html`
      <div>
        ${upvotable.canUpvote ? html`
          <button type="button" class="btn btn-transparent"
              @click=${this._toggleUpvote}
              aria-label="${upvotable.upvoted ? this._i18n.downvote_tooltip : this._i18n.upvote_tooltip}"
              title="${upvotable.upvoted ? this._i18n.downvote_tooltip : this._i18n.upvote_tooltip}"
              ?disabled=${!upvotable.canUpvote}>
            ${this._renderUpvote(upvotable)}
          </button>
        ` : this._renderUpvote(upvotable)
        }
      </div>
    ` : nothing;
  }

  _toggleUpvote() {

    const upvotable = this?.post || this?.topic;

    const upvoted = this?.post?.upvoted || this?.topic?.upvoted;

    const topicId = this?.post?.topic || this?.topic?.id;

    const siteId = this?.siteId || this?.topic?.siteId;

    const url = `/api/sites/${siteId}/topics/${topicId}` + (this.post ? `/posts/${this.post.id}` : "") + `/${upvoted ? "unupvote" : "upvote"}`;
    fetch(url, {
      credentials: "include",
    })
    .then(r => {

      if (r.ok) {
        if (upvotable.upvoted) {
          upvotable.upvotes -= 1;
          upvotable.upvoted = false;
        } else {
          upvotable.upvotes += 1;
          upvotable.upvoted = true;
        }

        if (this.post) {
          this.dispatchEvent(new CustomEvent("post-updated", { detail: { post: this.post }, bubbles: true }));
        } else {
          this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
        }
      } else {
        throw new Error("Network error while upvoting a post or topic.");
      }
    })
    .catch (error => console.error(error));
  }

};
