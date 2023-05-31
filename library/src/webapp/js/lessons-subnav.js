(function() {
    var LESSONS_SUBPAGE_TOOLTIP_MAX_LENGTH = 90;

    function LessonsSubPageNavigation(data) {
        if (!data.hasOwnProperty('pages')) {
          console.warn('No page data for LessonsSubPageNavigation');
          return;
        }

        if (!data.hasOwnProperty('i18n')) {
          console.warn('No i18n data for LessonsSubPageNavigation');
          return;
        }

        this.data = data.pages;
        this.i18n = data.i18n;
        this.siteId = data.siteId;
        this.isInstructor = data.isInstructor;

        this.topLevelPageProps = {};
	data.topLevelPageProps.forEach((p) => this.topLevelPageProps[p.toolId] = p);

        this.setup();
    };
    
    LessonsSubPageNavigation.prototype.setup = function() {
        var self = this;

        for (var pageId in self.data) {
            if (self.data.hasOwnProperty(pageId) && self.topLevelPageProps.hasOwnProperty(pageId)) {
                self.renderSubnavForPage(pageId, self.data[pageId], self.topLevelPageProps[pageId]);
            }
        }
    };

    LessonsSubPageNavigation.prototype.renderSubnavForPage = function(pageId, subpages, props) {
        var self = this;
        const lessonsElement = document.querySelector('#toolMenu a[href*="/tool/'+pageId+'"], #toolMenu [href*="/tool-reset/'+pageId+'"]');
        const pageName = lessonsElement.innerText;

        const siteListItem = lessonsElement.parentElement;
        const mainLink = lessonsElement.href?.replace(/\/tool\//, "/tool-reset/");

        const collapseId = `page-${pageId}-lessons-subpages`;
	const isExpanded = (subpages[0].toolId === self.getCurrentPlacement());
        const template = `
            <div class="d-inline-flex align-items-stretch">
                <button class="btn btn-nav btn-subsite rounded-end text-start ${(isExpanded) ? `` : `collapsed`} border-0 ps-4"
                        data-bs-toggle="collapse"
                        data-bs-target="#${collapseId}"
                        aria-expanded="${(isExpanded) ? `true` : `false`}"
                        aria-controls="${collapseId}">
                    <i class="${(isExpanded) ? `bi-chevron-down` : `bi-chevron-right`}" aria-hidden="true"></i>
                    <span>${pageName}</span>
                </button>
            </div>
            <div id="${collapseId}" class="lessons-subpages-collapse ${(isExpanded) ? `show` : `collapse`}">
                <ul class="nav flex-column pe-2">
                    <li class="nav-item">
                        <a class="btn btn-nav rounded-end text-start ps-5" href="${mainLink}">
                            <i class="me-2 si si-sakai-lessonbuildertool" aria-hidden="true"></i>
                            <span>${self.i18n.main_link_name}</span>
                            ${(props.disabled === 'true') ? `<i class="bi-slash-circle ms-2"></i>` : ``}
                            ${(props.hidden === 'true' && props.disabled !== 'true') ? `<i class = "si si-hidden ms-2"></i>` : ``}
                        </a>
                    </li>
                    ${subpages.map((subpage) => `
                        <li class="nav-item">
                            <a class="btn btn-nav rounded-end text-start ps-5 ${((props.disabled === 'true' && props.disabledDueToPrerequisite === 'true') || (subpage.disabled === 'true' && subpage.disabledDueToPrerequisite === 'true')) ? `disabled` : ``}" href="${self.buildSubpageUrlFor(subpage)}">
                                <i class="me-2 bi bi-arrow-return-right" aria-hidden="true"></i>
                                <span>${subpage.name}</span>
                                ${(props.disabled === 'true' || subpage.disabled === 'true') ? `<i class="bi-slash-circle ms-2"></i>` : ``}
                                ${(subpage.hidden === 'true' && ! (props.disabled === 'true' || subpage.disabled === 'true')) ? `<i class="si si-hidden ms-2"></i>` : ``}
                            </a>
                        </li>
                    `).join("")}
                </ul>
            </div>
        `;
        lessonsElement.remove()
        siteListItem.insertAdjacentHTML("afterbegin", template);

        window.addEventListener("DOMContentLoaded", () => {

          const collapseEl = document.getElementById(collapseId);
          const chevron = document.querySelector(`[data-bs-target='#${collapseId}'] > i`);
          collapseEl.addEventListener("show.bs.collapse", e => {
            e.stopPropagation();
            chevron.classList.replace("bi-chevron-right", "bi-chevron-down");
          });

          collapseEl.addEventListener("hide.bs.collapse", e => {
            e.stopPropagation();
            chevron.classList.replace("bi-chevron-down", "bi-chevron-right");
          });
        });
    };

    LessonsSubPageNavigation.prototype.buildSubpageUrlFor = function(subpage) {
        var url = '/portal/site/' + subpage.siteId;
        url += '/tool/' + subpage.toolId;
        url += '/ShowPage?sendingPage='+subpage.sendingPage;
        url += '&itemId='+subpage.itemId;
        url += '&path=clear_and_push';
        url += '&title=' + subpage.name;
        url += '&newTopLevel=false';
        return url;
    };

    LessonsSubPageNavigation.prototype.getCurrentPlacement = function() {
        const url = new URL(window.location.href);
        const parts = url.pathname.split('/');
        return (parts.length >= 6) ? parts[5] : '';
    };

    LessonsSubPageNavigation.prototype.getSubpageElement = function() {
        const lessonsSubnavToolIdInput = document.getElementById('lessonsSubnavToolId');
        const lessonsSubnavPageIdInput = document.getElementById('lessonsSubnavPageId');
        const lessonsSubnavItemIdInput = document.getElementById('lessonsSubnavItemId');
        let subpageElement = null;

        if ((lessonsSubnavToolIdInput !== null) && (lessonsSubnavPageIdInput !== null) &&
            (lessonsSubnavItemIdInput !== null)) {
            subpageElement = document.querySelector('#toolMenu a[href*="/tool/' + lessonsSubnavToolIdInput.value + 
                                                    '/ShowPage?sendingPage=' + lessonsSubnavPageIdInput.value +
                                                    '&itemId=' + lessonsSubnavItemIdInput.value + '&"]');
        }

        // If the current page is not a subpage, then highlight the main page.
        if (subpageElement == null) {
            subpageElement = document.querySelector('#toolMenu a[href$="/tool-reset/' + lessonsSubnavToolIdInput.value + '"]');
        }

        return subpageElement;
    };

    LessonsSubPageNavigation.prototype.setCurrentLessonsPage = function() {
        let self = this;
        let subpageElement = self.getSubpageElement();

        if (subpageElement == null) {
             // We're not on a ShowPage (e.g., Index of Pages, ShowItem, etc.), so highlight Main Page in the site nav.
            subpageElement = document.querySelector('#toolMenu a[href$="/tool-reset/' + self.getCurrentPlacement() + '"]');
        }
        subpageElement.classList.add("selected-page");
    };

    window.LessonsSubPageNavigation = LessonsSubPageNavigation;
})();
