import { html } from "../assets/@lion/core/index.js";
import { SakaiElement } from "../sakai-element.js";
import "../sakai-icon.js";
import "../sakai-pager.js";
import { SORT_NAME, SORT_TOPICS_CREATED, SORT_TOPICS_VIEWED, SORT_POSTS_CREATED, SORT_REACTIONS_MADE, ALL_TIME, THIS_WEEK } from "./sakai-conversations-constants.js";

export class ConversationsStatistics extends SakaiElement {

  static get properties() {

    return {
      statsUrl: { attribute: "stats-url", type: String },
      stats: { attribute: false, type: Object },
    };
  }

  constructor() {

    super();

    this.sort = "nameAscending";
    this.sortByNameAscending = true;

    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  set statsUrl(value) {

    this._statsUrl = value;
    this.loadStatsPage(1);
    this.requestUpdate();
  }

  get statsUrl() { return this._statsUrl; }

  loadStatsPage(page) {

    const options = {
      page,
      sort: this.sort,
      interval: typeof(this.interval) === "undefined" ? THIS_WEEK : this.interval,
    };

    fetch(`${this.statsUrl}`, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(options),
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error(`Network error while getting statistics from ${this.siteUrl}`);
    })
    .then(data => {

      this.count = Math.ceil(data.total / data.pageSize);
      this.stats = data.stats;
      this.currentPage = data.currentPage;
      this.requestUpdate();
    })
    .catch(error => console.log(error));
  }

  set interval(value) {

    this._interval = value;
    this.loadStatsPage(1);
  }

  get interval() { return this._interval; }

  set sort(value) {

    this._sort = value;
    this.loadStatsPage(1);
  }

  get sort() { return this._sort; }

  selectTopic(e) {

    const topicId = e.target.dataset.topicId;
    this.dispatchEvent(new CustomEvent("select-topic", { detail: { topicId }, bubbles: true }));
  }

  pageClicked(e) {

    this.loadStatsPage(e.detail.page);
    this.requestUpdate();
  }

  toggleSort(e) {

    switch (e.target.dataset.sort) {
      case SORT_NAME:
        this.sortByNameAscending = !this.sortByNameAscending;
        this.sort = this.sortByNameAscending ? "nameAscending" : "nameDescending";
        break;
      case SORT_TOPICS_CREATED:
        this.sortByTopicsCreatedAscending = !this.sortByTopicsCreatedAscending;
        this.sort = this.sortByTopicsCreatedAscending ? "topicsCreatedAscending" : "topicsCreatedDescending";
        break;
      case SORT_TOPICS_VIEWED:
        this.sortByTopicsViewedAscending = !this.sortByTopicsViewedAscending;
        this.sort = this.sortByTopicsViewedAscending ? "topicsViewedAscending" : "topicsViewedDescending";
        break;
      case SORT_REACTIONS_MADE:
        this.sortByReactionsMadeAscending = !this.sortByReactionsMadeAscending;
        this.sort = this.sortByReactionsMadeAscending ? "reactionsMadeAscending" : "reactionsMadeDescending";
        break;
      case SORT_POSTS_CREATED:
        this.sortByPostsCreatedAscending = !this.sortByPostsCreatedAscending;
        this.sort = this.sortByPostsCreatedAscending ? "postsCreatedAscending" : "postsCreatedDescending";
        break;
      default:
    }

    this.loadStatsPage(1);
  }

  shouldUpdate() {
    return this.i18n && this.stats;
  }

  render() {

    return html`
      <div class="add-topic-wrapper">
        <h1>${this.i18n.statistics}</h1>
        <div id="statistics-timeframe-block">
          <div>${this.i18n.timeframe}</div>
          <input type="radio"
              name="timeframe"
              value="${THIS_WEEK}"
              @click=${() => this.interval = THIS_WEEK}
              checked>${this.i18n.this_week}
          <input type="radio"
              name="timeframe"
              value="${ALL_TIME}"
              @click=${() => this.interval = ALL_TIME}>${this.i18n.all_time}
        </div>
      </div>
        <sakai-pager count="${this.count}" current="${this.currentPage}" @page-selected=${this.pageClicked}></sakai-pager>
        <table id="statistics-report-table" class="table table-hover table-striped table-bordered">
          <thead>
            <tr>
              <th>
                <a href="javascript:;"
                    data-sort="${SORT_NAME}"
                    title="${this.i18n.sort_by_author}"
                    aria-label="${this.i18n.sort_by_author}"
                    @click=${this.toggleSort}>
                  <div class="header-sort-block">
                    <div>${this.i18n.name_header}</div>
                    <div>
                      <sakai-icon type="${this.sortByNameAscending ? "down" : "up"}" size="small"></sakai-icon>
                    </div>
                  </div>
                </a>
              </th>
              <th>
                <a href="javascript:;"
                    data-sort="${SORT_TOPICS_CREATED}"
                    title="${this.i18n.sort_by_created_topics}"
                    aria-label="${this.i18n.sort_by_created_topics}"
                    @click=${this.toggleSort}>
                  <div class="header-sort-block">
                    <div>${this.i18n.topics_created_header}</div>
                    <div>
                      <sakai-icon type="${this.sortByTopicsCreatedAscending ? "down" : "up"}" size="small"></sakai-icon>
                    </div>
                  </div>
                </a>
              </th>
              <th>
                <a href="javascript:;"
                    data-sort="${SORT_TOPICS_VIEWED}"
                    title="${this.i18n.sort_by_viewed_topics}"
                    aria-label="${this.i18n.sort_by_viewed_topics}"
                    @click=${this.toggleSort}>
                  <div class="header-sort-block">
                    <div>${this.i18n.topics_read_header}</div>
                    <div>
                      <sakai-icon type="${this.sortByTopicsViewedAscending ? "down" : "up"}" size="small"></sakai-icon>
                    </div>
                  </div>
                </a>
              </th>
              <th>
                <a href="javascript:;"
                    data-sort="${SORT_REACTIONS_MADE}"
                    title="${this.i18n.sort_by_reactions_made}"
                    aria-label="${this.i18n.sort_by_reactions_made}"
                    @click=${this.toggleSort}>
                  <div class="header-sort-block">
                    <div>${this.i18n.reactions_header}</div>
                    <div>
                      <sakai-icon type="${this.sortByReactionsMadeAscending ? "down" : "up"}" size="small"></sakai-icon>
                    </div>
                  </div>
                </a>
              </th>
              <th>
                <a href="javascript:;"
                    data-sort="${SORT_POSTS_CREATED}"
                    title="${this.i18n.sort_by_created_posts}"
                    aria-label="${this.i18n.sort_by_created_posts}"
                    @click=${this.toggleSort}>
                  <div class="header-sort-block">
                    <div>${this.i18n.posts_header}</div>
                    <div>
                      <sakai-icon type="${this.sortByPostsCreatedAscending ? "down" : "up"}" size="small"></sakai-icon>
                    </div>
                  </div>
                </a>
              </th>
            </tr
          </thead>
          <tbody>
        ${this.stats.map(stat => html`
          <tr>
            <td>${stat.name}</td>
            <td>${stat.topicsCreated}</td>
            <td>${stat.topicsViewed}</td>
            <td>${stat.reactionsMade}</td>
            <td>${stat.postsCreated}</td>
          </tr>
        `)}
          </tbody>
        </table>
      </div>
    `;
  }
}

const tagName = "conversations-statistics";
!customElements.get(tagName) && customElements.define(tagName, ConversationsStatistics);
