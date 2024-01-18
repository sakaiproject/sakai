import { html } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-icon";
import "@sakai-ui/sakai-pager";
import { SORT_NAME, SORT_TOPICS_CREATED, SORT_TOPICS_VIEWED, SORT_POSTS_CREATED, SORT_REACTIONS_MADE, ALL_TIME, THIS_WEEK } from "./sakai-conversations-constants.js";

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

    this.loadTranslations("conversations").then(r => this.i18n = r);
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
      throw new Error(`Network error while getting statistics from ${this.siteUrl}`);
    })
    .then(data => {

      this._count = Math.ceil(data.total / data.pageSize);
      this._stats = data.stats;
      this._currentPage = data.currentPage;
    })
    .catch(error => console.error(error));
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
      case SORT_REACTIONS_MADE:
        this._sortByReactionsMadeAscending = !this._sortByReactionsMadeAscending;
        this._sort = this._sortByReactionsMadeAscending ? "reactionsMadeAscending" : "reactionsMadeDescending";
        break;
      case SORT_POSTS_CREATED:
        this._sortByPostsCreatedAscending = !this._sortByPostsCreatedAscending;
        this._sort = this._sortByPostsCreatedAscending ? "postsCreatedAscending" : "postsCreatedDescending";
        break;
      default:
    }

    this._loadStatsPage(1);
  }

  _setThisWeek() { this.interval = THIS_WEEK; }

  _setAllTime() { this.interval = ALL_TIME; }

  shouldUpdate() {
    return this.i18n && this._stats;
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
              @click="${this._setThisWeek}"
              checked>${this.i18n.this_week}
          <input type="radio"
              name="timeframe"
              value="${ALL_TIME}"
              @click="${this._setAllTime}">${this.i18n.all_time}
        </div>
      </div>
        <sakai-pager count="${this._count}" current="${this._currentPage}" @page-selected=${this._pageClicked}></sakai-pager>
        <table id="statistics-report-table" class="table table-hover table-striped table-bordered">
          <thead>
            <tr>
              <th>
                <a href="javascript:;"
                    data-sort="${SORT_NAME}"
                    title="${this.i18n.sort_by_author}"
                    aria-label="${this.i18n.sort_by_author}"
                    @click=${this._toggleSort}>
                  <div class="header-sort-block">
                    <div>${this.i18n.name_header}</div>
                    <div>
                      <sakai-icon type="${this._sortByNameAscending ? "down" : "up"}" size="small"></sakai-icon>
                    </div>
                  </div>
                </a>
              </th>
              <th>
                <a href="javascript:;"
                    data-sort="${SORT_TOPICS_CREATED}"
                    title="${this.i18n.sort_by_created_topics}"
                    aria-label="${this.i18n.sort_by_created_topics}"
                    @click=${this._toggleSort}>
                  <div class="header-sort-block">
                    <div>${this.i18n.topics_created_header}</div>
                    <div>
                      <sakai-icon type="${this._sortByTopicsCreatedAscending ? "down" : "up"}" size="small"></sakai-icon>
                    </div>
                  </div>
                </a>
              </th>
              <th>
                <a href="javascript:;"
                    data-sort="${SORT_TOPICS_VIEWED}"
                    title="${this.i18n.sort_by_viewed_topics}"
                    aria-label="${this.i18n.sort_by_viewed_topics}"
                    @click=${this._toggleSort}>
                  <div class="header-sort-block">
                    <div>${this.i18n.topics_read_header}</div>
                    <div>
                      <sakai-icon type="${this._sortByTopicsViewedAscending ? "down" : "up"}" size="small"></sakai-icon>
                    </div>
                  </div>
                </a>
              </th>
              <th>
                <a href="javascript:;"
                    data-sort="${SORT_REACTIONS_MADE}"
                    title="${this.i18n.sort_by_reactions_made}"
                    aria-label="${this.i18n.sort_by_reactions_made}"
                    @click=${this._toggleSort}>
                  <div class="header-sort-block">
                    <div>${this.i18n.reactions_header}</div>
                    <div>
                      <sakai-icon type="${this._sortByReactionsMadeAscending ? "down" : "up"}" size="small"></sakai-icon>
                    </div>
                  </div>
                </a>
              </th>
              <th>
                <a href="javascript:;"
                    data-sort="${SORT_POSTS_CREATED}"
                    title="${this.i18n.sort_by_created_posts}"
                    aria-label="${this.i18n.sort_by_created_posts}"
                    @click=${this._toggleSort}>
                  <div class="header-sort-block">
                    <div>${this.i18n.posts_header}</div>
                    <div>
                      <sakai-icon type="${this._sortByPostsCreatedAscending ? "down" : "up"}" size="small"></sakai-icon>
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
