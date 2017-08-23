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
                postId: '${postId}'
            };

        </script>
        ${sakaiHtmlHead}
        <link rel="stylesheet" type="text/css" href="/library/webjars/jquery-ui/1.12.1/jquery-ui.min.css"></script>
        <link href="/profile2-tool/css/profile2-profile-entity.css" type="text/css" rel="stylesheet" media="all" />
        <link rel="stylesheet" type="text/css" href="/library/js/jquery/qtip/jquery.qtip-latest.min.css" media="all"/>
    </head>
    <body>

        <script src="/library/webjars/jquery/1.12.4/jquery.min.js"></script>
        <script src="/library/webjars/jquery-ui/1.12.1/jquery-ui.min.js"></script>
        <script src="/library/webjars/momentjs/2.11.1/min/moment-with-locales.min.js"></script>
        <script src="/library/js/jquery/qtip/jquery.qtip-latest.min.js"></script>
        <script src="/commons-tool/lib/handlebars.runtime-v4.0.5.js"></script>
        <script src="/commons-tool/templates/templates.js"></script>
        <script src="/commons-tool/lib/autosize.min.js"></script>
        <script src="/commons-tool/js/commons_utils.js"></script>
        <script src="/commons-tool/js/commons_permissions.js"></script>
        <script src="/profile2-tool/javascript/profile2-eb.js"></script>

        <div id="Mrphs-sakai-commons" class="portletBody commons-portletBody">

            <ul id="commons-toolbar" class="navIntraTool actionToolBar" role="menu"></ul>

            <div id="commons-main-container">
                <div id="commons-content"></div>
            </div>

        </div> <!-- /portletBody-->

        <script src="/commons-tool/js/commons.js"></script>

    </body>
</html>
