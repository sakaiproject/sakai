import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SakaiRubricsLanguage, tr} from "./sakai-rubrics-language.js";
import "./sakai-rubrics-list.js";
import "./sakai-rubrics-shared-list.js";

class SakaiRubricsManager extends RubricsElement {

  constructor() {

    super();

    this.siteRubricsExpanded = "true";
    this.sharedRubricsExpanded = "false";

    SakaiRubricsLanguage.loadTranslations().then(result => this.i18nLoaded = result );
  }

  static get properties() {
    return { token: String, i18nLoaded: Boolean };
  }

  shouldUpdate() {
    return this.i18nLoaded;
  }

  render() {

    return html`
      <h1>${tr("manage_rubrics")}</h1>

      <div class="sak-banner-info"><sr-lang key="locked_message">locked_message</sr></div>

      <div class="row">
        <div class="col-md-4 form-group">
          <label for="rubrics-search-bar"><sr-lang key="search_rubrics">Search Rubrics by title or author</sr-lang></label>
          <input type="text" id="rubrics-search-bar" name="rubrics-search-bar" class="form-control" @keyup="${this.filterRubrics}">
        </div>
      </div>

      <div role="tablist">
        <div id="site-rubrics-title" aria-expanded="${this.siteRubricsExpanded}"
            role="tab" aria-multiselectable="true" class="manager-collapse-title"
            title="${tr("toggle_site_rubrics")}" tabindex="0" @click="${this.toggleSiteRubrics}">
          <div>
            <span class="collpase-icon fa fa-chevron-down"></span>
            <sr-lang key="site_rubrics">site_rubrics</sr-lang>
          </div>
        </div>

      <div role="tabpanel" aria-labelledby="site-rubrics-title" id="site_rubrics">
        <div class="rubric-title-sorting">
          <div @click="${this.sortRubrics}"><a href="javascript:void(0)" style="text-decoration: none;"><sr-lang class="site-name" key="site_name">site_name</sr-lang><span class="collpase-icon fa fa-chevron-up site-name sort-element-site"></span></a></div>
          <div @click="${this.sortRubrics}"><a href="javascript:void(0)" style="text-decoration: none;"><sr-lang class="site-title" key="site_title">site_title</sr-lang><span class="collpase-icon fa site-title sort-element-site"></span></a></div>
          <div @click="${this.sortRubrics}"><a href="javascript:void(0)" style="text-decoration: none;"><sr-lang class="site-creator" key="creator_name">creator_name</sr-lang><span class="collpase-icon fa site-creator sort-element-site"></span></a></div>
          <div @click="${this.sortRubrics}"><a href="javascript:void(0)" style="text-decoration: none;"><sr-lang class="site-modified" key="modified">modified</sr-lang><span class="collpase-icon fa site-modified sort-element-site"></span></a></div>
          <div class="actions"><sr-lang key="actions">actions</sr-lang></div>
        </div>
        <br>
        <sakai-rubrics-list id="sakai-rubrics" @sharing-change="${this.handleSharingChange}" @copy-share-site="${this.copyShareSite}" token="Bearer ${this.token}"></sakai-rubrics-list>
      </div>
      
      <hr>
      <h3>${tr("public_rubrics_title")}</h3>
      <p>${tr("public_rubrics_info")}</p>

        <div id="shared-rubrics-title" aria-expanded="${this.sharedRubricsExpanded}" role="tab" aria-multiselectable="true" class="manager-collapse-title" title="${tr("toggle_shared_rubrics")}" tabindex="0" @click="${this.toggleSharedRubrics}">
          <div>
            <span class="collpase-icon fa fa-chevron-right"></span>
            <sr-lang key="shared_rubrics">shared_rubrics</sr-lang>
          </div>
        </div>

      <div role="tabpanel" aria-labelledby="shared-rubrics-title" id="shared_rubrics" style="display:none;">
        <div id="sharedlist">
          <div class="rubric-title-sorting">
          <div @click="${this.sortRubrics}"><a href="javascript:void(0)" style="text-decoration: none;"><sr-lang class="shared-name" key="site_name">site_name</sr-lang><span class="collpase-icon fa fa-chevron-up shared-name sort-element-shared"></span></a></div>
          <div @click="${this.sortRubrics}"><a href="javascript:void(0)" style="text-decoration: none;"><sr-lang class="shared-title" key="site_title">site_title</sr-lang><span class="collpase-icon fa shared-title sort-element-shared"></span></a></div>
          <div @click="${this.sortRubrics}"><a href="javascript:void(0)" style="text-decoration: none;"><sr-lang class="shared-creator" key="creator_name">creator_name</sr-lang><span class="collpase-icon fa shared-creator sort-element-shared"></span></a></div>
          <div @click="${this.sortRubrics}"><a href="javascript:void(0)" style="text-decoration: none;"><sr-lang class="shared-modified" key="modified">modified</sr-lang><span class="collpase-icon fa shared-modified sort-element-shared"></span></a></div>
          <div class="actions"><sr-lang key="actions">actions</sr-lang></div>
        </div>
        <br>
        <sakai-rubrics-shared-list token="Bearer ${this.token}" id="sakai-rubrics-shared-list" @copy-share-site="${this.copyShareSite}" ></sakai-rubrics-shared-list>
      </div>
      <br>
      </div>

      </div>
    `;
  }

  handleSharingChange() {
    document.getElementById("sakai-rubrics-shared-list").refresh();
  }

  copyShareSite() {
    this.querySelector("sakai-rubrics-list").refresh();
  }

  toggleSiteRubrics() {

    var siteRubrics = $("#site_rubrics");
    siteRubrics.toggle();
    var icon = $("#site-rubrics-title .collpase-icon");
    if (siteRubrics.is(":visible")) {
      this.siteRubricsExpanded = "true";
      icon.removeClass("fa-chevron-right").addClass("fa-chevron-down");
    } else {
      this.siteRubricsExpanded = "false";
      icon.removeClass("fa-chevron-down").addClass("fa-chevron-right");
    }
  }

  toggleSharedRubrics() {

    var sharedRubrics = $("#shared_rubrics");
    sharedRubrics.toggle();
    var icon = $("#shared-rubrics-title .collpase-icon");
    if (sharedRubrics.is(":visible")) {
      this.sharedRubricsExpanded = "true";
      icon.removeClass("fa-chevron-right").addClass("fa-chevron-down");
    } else {
      this.sharedRubricsExpanded = "false";
      icon.removeClass("fa-chevron-down").addClass("fa-chevron-right");
    }
  }

  filterRubrics() {

    const searchInput = document.getElementById('rubrics-search-bar');
    const searchInputValue = searchInput.value.toLowerCase();

    this.querySelectorAll('sakai-rubrics-list, sakai-rubrics-shared-list').forEach(rubricList => {
      rubricList.querySelectorAll('.rubric-item').forEach(rubricItem => {
        rubricItem.classList.remove('hidden');
        var rubricTitle = rubricItem.querySelector('.rubric-name').textContent;
        var rubricAuthor = rubricItem.querySelector('sakai-rubric-creator-name').textContent;
        var rubricSite = rubricItem.querySelector('sakai-rubric-site-title').textContent;
        if (!rubricAuthor.toLowerCase().includes(searchInputValue) &&
            !rubricTitle.toLowerCase().includes(searchInputValue) &&
            !rubricSite.toLowerCase().includes(searchInputValue)
        ) {
          rubricItem.classList.add('hidden');
        }
      });
    });
  }

  sortRubrics(event) {
	
    const sortInput = event.target.className.toLowerCase();

    if (!sortInput) {
      return;
    }

    const [rubricClass, rubricType] = sortInput.split("-");
    const query = `.${sortInput}.sort-element-${rubricClass}`;
    const arrowUpIcon = 'fa-chevron-up';
    const arrowDownIcon = 'fa-chevron-down';
    let ascending = this.querySelector(query).classList.contains(arrowUpIcon);
    this.querySelectorAll(`.sort-element-${rubricClass}`).forEach((item) => {
      item.classList.remove(arrowDownIcon);
      item.classList.remove(arrowUpIcon);
    });
    this.querySelector(query).classList.add(ascending ? arrowDownIcon : arrowUpIcon);
    ascending = !ascending;

    const elementChildSite = this.querySelector(rubricClass === 'site' ? 'sakai-rubrics-list' : 'sakai-rubrics-shared-list');
    elementChildSite.sortRubrics(rubricType, ascending);
  }

}

customElements.define("sakai-rubrics-manager", SakaiRubricsManager);
