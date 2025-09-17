import { html } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-icon";
import "@sakai-ui/sakai-pager/sakai-pager.js";
import { SORT_NAME, SORT_TOPICS_CREATED, SORT_TOPICS_VIEWED, SORT_TOPIC_REACTIONS,
  SORT_TOPIC_UPVOTES, SORT_POSTS_CREATED, SORT_POSTS_VIEWED, SORT_POST_REACTIONS,
  SORT_POST_UPVOTES, ALL_TIME, THIS_WEEK } from "./sakai-conversations-constants.js";

export class ConversationsStatistics extends SakaiElement {

  static properties = {

    statsUrl: { attribute: "stats-url", type: String },
    _stats: { state: true },
  };

  constructor() {

    super();

    this._sort = "nameAscending";
    this._sortByNameAscending = true;
    this._count = 0;

    this.loadTranslations("conversations");
  }

  set statsUrl(value) {

    const oldValue = this._statsUrl;
    this._statsUrl = value;

    this._loadStatsPage(1);

    this.requestUpdate("statsUrl", oldValue);
  }

  get statsUrl() { return this._statsUrl; }

  _loadStatsPage(page) {

    if (!this._statsUrl) return;

    const options = {
      page,
      sort: this._sort,
      interval: typeof(this.interval) === "undefined" ? THIS_WEEK : this.interval,
    };

    fetch(`${this._statsUrl}`, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(options),
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error(`Network error while getting statistics from ${this._statsUrl}`);
    })
    .then(data => {

      this._count = Math.ceil(data.total / data.pageSize);
      this._stats = data.stats;
      this._currentPage = data.currentPage;
    })
    .catch (error => console.error(error));
  }

  set interval(value) {

    this._interval = value;
    this._loadStatsPage(1);
  }

  get interval() { return this._interval; }

  set sort(value) {

    this._sort = value;
    this._loadStatsPage(1);
  }

  get sort() { return this._sort; }

  _pageClicked(e) {

    this._loadStatsPage(e.detail.page);
    this.requestUpdate();
  }

  _toggleSort(e) {

    switch (e.target.dataset.sort) {
      case SORT_NAME:
        this._sortByNameAscending = !this._sortByNameAscending;
        this._sort = this._sortByNameAscending ? "nameAscending" : "nameDescending";
        break;
      case SORT_TOPICS_CREATED:
        this._sortByTopicsCreatedAscending = !this._sortByTopicsCreatedAscending;
        this._sort = this._sortByTopicsCreatedAscending ? "topicsCreatedAscending" : "topicsCreatedDescending";
        break;
      case SORT_TOPICS_VIEWED:
        this._sortByTopicsViewedAscending = !this._sortByTopicsViewedAscending;
        this._sort = this._sortByTopicsViewedAscending ? "topicsViewedAscending" : "topicsViewedDescending";
        break;
      case SORT_TOPIC_REACTIONS:
        this._sortByTopicReactionsAscending = !this._sortByTopicReactionsAscending;
        this._sort = this._sortByTopicReactionsAscending ? "topicReactionsAscending" : "topicReactionsDescending";
        break;
      case SORT_TOPIC_UPVOTES:
        this._sortByTopicUpvotesAscending = !this._sortByTopicUpvotesAscending;
        this._sort = this._sortByTopicUpvotesAscending ? "topicUpvotesAscending" : "topicUpvotesDescending";
        break;
      case SORT_POSTS_CREATED:
        this._sortByPostsCreatedAscending = !this._sortByPostsCreatedAscending;
        this._sort = this._sortByPostsCreatedAscending ? "postsCreatedAscending" : "postsCreatedDescending";
        break;
      case SORT_POSTS_VIEWED:
        this._sortByPostsViewedAscending = !this._sortByPostsViewedAscending;
        this._sort = this._sortByPostsViewedAscending ? "postsViewedAscending" : "postsViewedDescending";
        break;
      case SORT_POST_REACTIONS:
        this._sortByPostReactionsAscending = !this._sortByPostReactionsAscending;
        this._sort = this._sortByPostReactionsAscending ? "postReactionsAscending" : "postReactionsDescending";
        break;
      case SORT_POST_UPVOTES:
        this._sortByPostUpvotesAscending = !this._sortByPostUpvotesAscending;
        this._sort = this._sortByPostUpvotesAscending ? "postUpvotesAscending" : "postUpvotesDescending";
        break;
      default:
    }

    this._loadStatsPage(1);
  }

  _setThisWeek() { this.interval = THIS_WEEK; }

  _setAllTime() { this.interval = ALL_TIME; }

  shouldUpdate() {
    return this._i18n && this._stats;
  }

  render() {

    return html`
      <div class="add-topic-wrapper">
        <h1>${this._i18n.statistics}</h1>
        <div id="statistics-timeframe-block">
          <div>${this._i18n.timeframe}</div>
          <label>
            <input type="radio"
                name="timeframe"
                value="${THIS_WEEK}"
                @click="${this._setThisWeek}"
                checked>
              ${this._i18n.this_week}
          </label>
          <label>
            <input type="radio"
                name="timeframe"
                value="${ALL_TIME}"
                @click="${this._setAllTime}">
              ${this._i18n.all_time}
          </label>
        </div>
      </div>
        <sakai-pager count="${this._count}" current="${this._currentPage}" @page-selected=${this._pageClicked}></sakai-pager>
        <div class="table-responsive">
          <table id="statistics-report-table" class="table table-hover table-striped table-bordered">
            <thead>
              <tr>
                <th>
                  <a href="javascript:;"
                      data-sort="${SORT_NAME}"
                      title="${this._i18n.sort_by_author}"
                      aria-label="${this._i18n.sort_by_author}"
                      @click=${this._toggleSort}>
                    <div class="header-sort-block">
                      <div>${this._i18n.name_header}</div>
                      <div>
                        <sakai-icon type="${this._sortByNameAscending ? "down" : "up"}" size="small"></sakai-icon>
                      </div>
                    </div>
                  </a>
                </th>
                <th>
                  <a href="javascript:;"
                      data-sort="${SORT_TOPICS_CREATED}"
                      title="${this._i18n.sort_by_created}"
                      aria-label="${this._i18n.sort_by_created}"
                      @click=${this._toggleSort}>
                    <div class="header-sort-block">
                      <div>${this._i18n.topics_header}</div>
                      <div>
                        <sakai-icon type="${this._sortByTopicsCreatedAscending ? "down" : "up"}" size="small"></sakai-icon>
                      </div>
                    </div>
                  </a>
                </th>
                <th>
                  <a href="javascript:;"
                      data-sort="${SORT_TOPICS_VIEWED}"
                      title="${this._i18n.sort_by_viewed}"
                      aria-label="${this._i18n.sort_by_viewed}"
                      @click=${this._toggleSort}>
                    <div class="header-sort-block">
                      <div>${this._i18n.topics_viewed_header}</div>
                      <div>
                        <sakai-icon type="${this._sortByTopicsViewedAscending ? "down" : "up"}" size="small"></sakai-icon>
                      </div>
                    </div>
                  </a>
                </th>
                <th>
                  <a href="javascript:;"
                      data-sort="${SORT_TOPIC_REACTIONS}"
                      title="${this._i18n.sort_by_reactions}"
                      aria-label="${this._i18n.sort_by_reactions}"
                      @click=${this._toggleSort}>
                    <div class="header-sort-block">
                      <div>${this._i18n.topic_reactions_header}</div>
                      <div>
                        <sakai-icon type="${this._sortByTopicReactionsAscending ? "down" : "up"}" size="small"></sakai-icon>
                      </div>
                    </div>
                  </a>
                </th>
                <th>
                  <a href="javascript:;"
                      data-sort="${SORT_TOPIC_UPVOTES}"
                      title="${this._i18n.sort_by_upvotes}"
                      aria-label="${this._i18n.sort_by_upvotes}"
                      @click=${this._toggleSort}>
                    <div class="header-sort-block">
                      <div>${this._i18n.topic_upvotes_header}</div>
                      <div>
                        <sakai-icon type="${this._sortByTopicUpvotesAscending ? "down" : "up"}" size="small"></sakai-icon>
                      </div>
                    </div>
                  </a>
                </th>
                <th>
                  <a href="javascript:;"
                      data-sort="${SORT_POSTS_CREATED}"
                      title="${this._i18n.sort_by_created}"
                      aria-label="${this._i18n.sort_by_created}"
                      @click=${this._toggleSort}>
                    <div class="header-sort-block">
                      <div>${this._i18n.posts_header}</div>
                      <div>
                        <sakai-icon type="${this._sortByPostsCreatedAscending ? "down" : "up"}" size="small"></sakai-icon>
                      </div>
                    </div>
                  </a>
                </th>
                <th>
                  <a href="javascript:;"
                      data-sort="${SORT_POSTS_VIEWED}"
                      title="${this._i18n.sort_by_viewed}"
                      aria-label="${this._i18n.sort_by_viewed}"
                      @click=${this._toggleSort}>
                    <div class="header-sort-block">
                      <div>${this._i18n.posts_viewed_header}</div>
                      <div>
                        <sakai-icon type="${this._sortByPostsViewedAscending ? "down" : "up"}" size="small"></sakai-icon>
                      </div>
                    </div>
                  </a>
                </th>
                <th>
                  <a href="javascript:;"
                      data-sort="${SORT_POST_REACTIONS}"
                      title="${this._i18n.sort_by_reactions}"
                      aria-label="${this._i18n.sort_by_reactions}"
                      @click=${this._toggleSort}>
                    <div class="header-sort-block">
                      <div>${this._i18n.post_reactions_header}</div>
                      <div>
                        <sakai-icon type="${this._sortByPostReactionsAscending ? "down" : "up"}" size="small"></sakai-icon>
                      </div>
                    </div>
                  </a>
                </th>
                <th>
                  <a href="javascript:;"
                      data-sort="${SORT_POST_UPVOTES}"
                      title="${this._i18n.sort_by_upvotes}"
                      aria-label="${this._i18n.sort_by_upvotes}"
                      @click=${this._toggleSort}>
                    <div class="header-sort-block">
                      <div>${this._i18n.post_upvotes_header}</div>
                      <div>
                        <sakai-icon type="${this._sortByPostUpvotesAscending ? "down" : "up"}" size="small"></sakai-icon>
                      </div>
                    </div>
                  </a>
                </th>
              </tr>
            </thead>
            <tbody>
          ${this._stats.map(stat => html`
            <tr>
              <td>${stat.name}</td>
              <td>${stat.topicsCreated}</td>
              <td>${stat.topicsViewed}</td>
              <td>${stat.topicReactions}</td>
              <td>${stat.topicUpvotes}</td>
              <td>${stat.postsCreated}</td>
              <td>${stat.postsViewed}</td>
              <td>${stat.postReactions}</td>
              <td>${stat.postUpvotes}</td>
            </tr>
          `)}
            </tbody>
          </table>
        </div>
      </div>
    `;
  }
}
