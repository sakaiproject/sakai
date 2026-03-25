import { html, nothing } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-table/sakai-table.js";
import "@sakai-ui/sakai-pager/sakai-pager.js";
import { SORT_NAME, SORT_TOPICS_CREATED, SORT_TOPICS_VIEWED, SORT_TOPIC_REACTIONS,
  SORT_TOPIC_UPVOTES, SORT_POSTS_CREATED, SORT_POSTS_VIEWED, SORT_POST_REACTIONS,
  SORT_POST_UPVOTES, ALL_TIME, THIS_WEEK } from "./sakai-conversations-constants.js";

// Maps sakai-table field + order to the server sort enum strings.
const SORT_MAP = {
  "name:asc": "nameAscending",
  "name:desc": "nameDescending",
  "topicsCreated:asc": "topicsCreatedAscending",
  "topicsCreated:desc": "topicsCreatedDescending",
  "topicsViewed:asc": "topicsViewedAscending",
  "topicsViewed:desc": "topicsViewedDescending",
  "topicReactions:asc": "topicReactionsAscending",
  "topicReactions:desc": "topicReactionsDescending",
  "topicUpvotes:asc": "topicUpvotesAscending",
  "topicUpvotes:desc": "topicUpvotesDescending",
  "postsCreated:asc": "postsCreatedAscending",
  "postsCreated:desc": "postsCreatedDescending",
  "postsViewed:asc": "postsViewedAscending",
  "postsViewed:desc": "postsViewedDescending",
  "postReactions:asc": "postReactionsAscending",
  "postReactions:desc": "postReactionsDescending",
  "postUpvotes:asc": "postUpvotesAscending",
  "postUpvotes:desc": "postUpvotesDescending",
};

// Keep constants referenced so bundlers don't tree-shake them from the import.
void [ SORT_NAME, SORT_TOPICS_CREATED, SORT_TOPICS_VIEWED, SORT_TOPIC_REACTIONS,
  SORT_TOPIC_UPVOTES, SORT_POSTS_CREATED, SORT_POSTS_VIEWED, SORT_POST_REACTIONS,
  SORT_POST_UPVOTES ];

export class ConversationsStatistics extends SakaiElement {

  static properties = {
    statsUrl: { attribute: "stats-url", type: String },
    _stats: { state: true },
    _count: { state: true },
    _currentPage: { state: true },
  };

  constructor() {

    super();

    this._sort = "nameAscending";
    this._count = 0;
    this._currentPage = 1;
    this._stats = null;

    this.loadTranslations("conversations");
  }

  set statsUrl(value) {

    const oldValue = this._statsUrl;
    this._statsUrl = value;
    this._fetchPage(1);
    this.requestUpdate("statsUrl", oldValue);
  }

  get statsUrl() { return this._statsUrl; }

  set interval(value) {

    this._interval = value;
    this._fetchPage(1);
  }

  get interval() { return this._interval; }

  _fetchPage(page) {

    if (!this._statsUrl) return;

    fetch(this._statsUrl, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        page,
        sort: this._sort,
        interval: this._interval ?? THIS_WEEK,
      }),
    })
    .then(r => {

      if (r.ok) return r.json();
      throw new Error(`Network error fetching statistics from ${this._statsUrl}`);
    })
    .then(data => {

      this._count = Math.ceil(data.total / data.pageSize);
      this._stats = data.stats;
      this._currentPage = data.currentPage;
    })
    .catch(error => console.error(error));
  }

  _onSort(e) {

    const { field, order } = e.detail;
    this._sort = SORT_MAP[`${field}:${order}`] ?? "nameAscending";
    this._fetchPage(1);
  }

  _onPage(e) {

    this._fetchPage(e.detail.page);
  }

  _setThisWeek() { this.interval = THIS_WEEK; }

  _setAllTime() { this.interval = ALL_TIME; }

  shouldUpdate() {

    return this._i18n;
  }

  _columns() {

    return [
      { label: this._i18n.name_header, field: "name", sortable: true },
      { label: this._i18n.topics_header, field: "topicsCreated", sortable: true },
      { label: this._i18n.topics_viewed_header, field: "topicsViewed", sortable: true },
      { label: this._i18n.topic_reactions_header, field: "topicReactions", sortable: true },
      { label: this._i18n.topic_upvotes_header, field: "topicUpvotes", sortable: true },
      { label: this._i18n.posts_header, field: "postsCreated", sortable: true },
      { label: this._i18n.posts_viewed_header, field: "postsViewed", sortable: true },
      { label: this._i18n.post_reactions_header, field: "postReactions", sortable: true },
      { label: this._i18n.post_upvotes_header, field: "postUpvotes", sortable: true },
    ];
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
                @click=${this._setThisWeek}
                checked>
            ${this._i18n.this_week}
          </label>
          <label>
            <input type="radio"
                name="timeframe"
                value="${ALL_TIME}"
                @click=${this._setAllTime}>
            ${this._i18n.all_time}
          </label>
        </div>

        ${this._stats ? html`
          <sakai-table
            id="conversations-stats-table"
            .columns=${this._columns()}
            .data=${this._stats}
            page-size="0"
            @sakai-table-sort=${this._onSort}
          ></sakai-table>

          ${this._count > 1 ? html`
            <div class="d-flex justify-content-center mt-2">
              <sakai-pager
                count="${this._count}"
                current="${this._currentPage}"
                @page-selected=${this._onPage}
              ></sakai-pager>
            </div>
          ` : nothing}
        ` : nothing}
      </div>
    `;
  }
}
