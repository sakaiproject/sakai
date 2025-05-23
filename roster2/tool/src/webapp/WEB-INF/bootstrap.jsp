<!--

    Copyright (c) 2008-2010 The Sakai Foundation

    Licensed under the Educational Community License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

                http://www.osedu.org/licenses/ECL-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html  
      xmlns="http://www.w3.org/1999/xhtml"
      xml:lang="${language}"
      lang="${language}">
    <head>
        <link rel="stylesheet" media="print" type="text/css" href="/sakai-roster2-tool/css/print.css${portalCDNQuery}" />

        <script>

            var roster = {
                userId: '${userId}',
                state: '${state}',
                siteId: '${siteId}',
                language: '${language}',
                defaultSortColumn: '${defaultSortColumn}',
                defaultOverviewMode: '${defaultOverviewMode}',
                firstNameLastName: ${firstNameLastName},
                hideSingleGroupFilter: ${hideSingleGroupFilter},
                viewUserDisplayId: ${viewUserDisplayId},
                viewPronouns: ${viewPronouns},
                viewProfileLink: ${viewProfileLink},
                viewUserNamePronunciation: ${viewUserNamePronunciation},
                viewUserProperty: ${viewUserProperty},
                viewCandidateDetails: ${viewCandidateDetails},
                officialPictureMode: ${officialPicturesByDefault},
                viewEmail: ${viewEmail},
                showPermsToMaintainers: ${showPermsToMaintainers},
                siteMaintainer: ${siteMaintainer},
                i18n: {},
                showVisits: ${showVisits},
                profileNamePronunciationLink: '${profileNamePronunciationLink}',
            };
    
        </script>
        ${sakaiHtmlHead}
    </head>

    <body>

        <!-- wrap tool in portletBody div for PDA portal compatibility -->
        <div class="portletBody container-fluid">

            <div id="roster-header-loading-image"><img src="/sakai-roster2-tool/images/ajax-loader.gif" /></div>

            <ul id="roster_navbar" class="navIntraTool actionToolBar" role="menu"></ul>

            <div id="rosterMainContainer">
                <div id="roster_content" class="view_mode_${defaultOverviewMode}"></div>
            </div>

        </div> <!-- portletBody -->

        <script>includeLatestJQuery("roster");</script>
        <script src="/profile2-tool/javascript/profile2-eb.js${portalCDNQuery}"></script>
        <script>includeWebjarLibrary("handlebars");</script>
        <script>includeWebjarLibrary("select2");</script>
        <script src="/sakai-roster2-tool/templates.js${portalCDNQuery}"></script>
        <script type="module">
            import {loadRoster} from "/sakai-roster2-tool/js/roster.js${portalCDNQuery}";
            loadRoster();
        </script>

    </body>
</html>
