import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SakaiRubricsLanguage, tr} from "./sakai-rubrics-language.js";
import {SakaiRubricsList} from "./sakai-rubrics-list.js";
import {SakaiRubricsSharedList} from "./sakai-rubrics-shared-list.js";
import {loadProperties} from "/webcomponents/sakai-i18n.js";

class SakaiRubricsManager extends RubricsElement {

  constructor() {

    super();

    this.siteRubricsExpanded = "false";
    this.sharedRubricsExpanded = "false";

    SakaiRubricsLanguage.loadTranslations().then(result => this.i18nLoaded = result );
  }

  static get properties() {
    return { token: String, i18nLoaded: Boolean };
  }

  shouldUpdate(changedProperties) {
    return this.i18nLoaded;
  }

  render() {

    return html`
      <h1>${tr("manage_rubrics")}</h1>

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
          <div class="hidden-xs"><sr-lang key="site_title">site_title</sr-lang></div>
          <div class="hidden-xs"><sr-lang key="creator_name">creator_name</sr-lang></div>
          <div class="hidden-xs"><sr-lang key="modified">modified</sr-lang></div>
          <div class="actions"><sr-lang key="actions">actions</sr-lang></div>
        </div>

        <div role="tabpanel" aria-labelledby="site-rubrics-title" id="site_rubrics">
          <sakai-rubrics-list @sharing-change="${this.handleSharingChange}" @copy-share-site="${this.copyShareSite}" token="Bearer ${this.token}"></sakai-rubrics-list>
        </div>

        <div id="shared-rubrics-title" aria-expanded="${this.sharedRubricsExpanded}" role="tab" aria-multiselectable="true" class="manager-collapse-title" title="${tr("toggle_shared_rubrics")}" tabindex="0" @click="${this.toggleSharedRubrics}">
          <div>
            <span class="collpase-icon fa fa-chevron-down"></span>
            <sr-lang key="shared_rubrics">shared_rubrics</sr-lang>
          </div>
          <div class="hidden-xs"><sr-lang key="site_title">site_title</sr-lang></div>
          <div class="hidden-xs"><sr-lang key="creator_name">creator_name</sr-lang></div>
          <div class="hidden-xs"><sr-lang key="modified">modified</sr-lang></div>
          <div class="actions"><sr-lang key="actions">actions</sr-lang></div>
        </div>

        <div role="tabpanel" aria-labelledby="shared-rubrics-title" id="shared_rubrics">
          <div id="sharedlist">
            <sakai-rubrics-shared-list token="Bearer ${this.token}" id="sakai-rubrics-shared-list" @copy-share-site="${this.copyShareSite}" ></sakai-rubrics-shared-list>
          </div>
        </div>

      </div>
    `;
  }

  handleSharingChange(e) {
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
    var searchInput = document.getElementById('rubrics-search-bar');
    var searchInputValue = searchInput.value.toLowerCase();

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
}

customElements.define("sakai-rubrics-manager", SakaiRubricsManager);
