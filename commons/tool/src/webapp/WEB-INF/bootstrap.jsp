<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html
      xmlns="http://www.w3.org/1999/xhtml"
      xml:lang="${isolanguage}"
      lang="${isolanguage}">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script>

            var commons = {
                i18n: {},
                commonsId: '${commonsId}',
                siteId: '${siteId}',
                embedder: '${embedder}',
                userId: '${userId}',
                isUserSite: ${isUserSite},
                postId: '${postId}',
                maxUploadSize: '${maxUploadSize}'
            };

        </script>
        ${sakaiHtmlHead}
        <link href="/profile2-tool/css/profile2-profile-entity.css${portalCDNQuery}" type="text/css" rel="stylesheet" media="all" />
    </head>
    <body>

        <script>includeLatestJQuery("commons");</script>
        <script>includeWebjarLibrary("qtip2");</script>
        <script>includeWebjarLibrary("handlebars");</script>
        <script src="/commons-tool/templates/templates.js${portalCDNQuery}"></script>
        <script src="/commons-tool/lib/autosize.min.js${portalCDNQuery}"></script>
        <script src="/commons-tool/js/commons_utils.js${portalCDNQuery}"></script>
        <script src="/commons-tool/js/commons_permissions.js${portalCDNQuery}"></script>
        <script src="/profile2-tool/javascript/profile2-eb.js${portalCDNQuery}"></script>

        <div id="Mrphs-sakai-commons" class="portletBody commons-portletBody">

            <ul id="commons-toolbar" class="navIntraTool actionToolBar hidden" role="menu"></ul>
            <div id="commons-main-container">
                <div id="commons-content"></div>
            </div>

        </div> <!-- /portletBody-->

        <script src="/commons-tool/js/commons.js${portalCDNQuery}"></script>

    </body>
</html>
