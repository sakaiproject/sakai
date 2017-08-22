<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html  
    xmlns="http://www.w3.org/1999/xhtml"
    xml:lang="${language}"
    lang="${language}">
    <head>
        <link rel="stylesheet" type="text/css" href="/feedback-tool/css/feedback.css" />
        <script type="text/javascript" src="/library/webjars/jquery/1.12.4/jquery.min.js"></script>
        <script type="text/javascript" src="/feedback-tool/lib/jquery.form.min.js"></script>
        <script type="text/javascript" src="/feedback-tool/lib/jquery.MultiFile.pack.js"></script>
        <script type="text/javascript" src="//www.google.com/recaptcha/api/js/recaptcha_ajax.js"></script>
        <script type="text/javascript" src="/feedback-tool/lib/handlebars.runtime-v1.3.0.js"></script>
        <script type="text/javascript" src="/feedback-tool/templates/all.handlebars"></script>

        <script type="text/javascript">

            var feedback = {
                state: 'home',
                previousState: '',
                userId: '${userId}',
                siteId: '${siteId}',
                siteExists: '${siteExists}',
                language: '${language}',
                featureSuggestionUrl: '${featureSuggestionUrl}',
                technicalToAddress: '${technicalToAddress}',
                helpToAddress: '${helpToAddress}',
                suggestionsToAddress: '${suggestionsToAddress}',
                supplementalAToAddress: '${supplementalAToAddress}',
                supplementalBToAddress: '${supplementalBToAddress}',
                contactName: '${contactName}',
                enableTechnical: ${enableTechnical},
                enableHelp: ${enableHelp},
                enableSuggestions: ${enableSuggestions},
                enableSupplementalA: ${enableSupplementalA},
                enableSupplementalB: ${enableSupplementalB},
                helpPagesUrl: '${helpPagesUrl}',
                helpdeskUrl: '${helpdeskUrl}',
                technicalUrl: '${technicalUrl}',
                supplementalAUrl: '${supplementalAUrl}',
                supplementalBUrl: '${supplementalBUrl}',
                helpPagesTarget: '${helpPagesTarget}',
                supplementaryInfo: '${supplementaryInfo}',
                recaptchaPublicKey: '${recaptchaPublicKey}',
                maxAttachmentsMB: ${maxAttachmentsMB},
                showContentPanel: ${showContentPanel},
                showHelpPanel: ${showHelpPanel},
                showTechnicalPanel: ${showTechnicalPanel},
                showSuggestionsPanel: ${showSuggestionsPanel},
                showSupplementalAPanel: ${showSupplementalAPanel},
                showSupplementalBPanel: ${showSupplementalBPanel},
                helpPanelAsLink: ${helpPanelAsLink},
                technicalPanelAsLink: ${technicalPanelAsLink},
                suggestionsPanelAsLink: ${suggestionsPanelAsLink},
                supplementalAPanelAsLink: ${supplementalAPanelAsLink},
                supplementalBPanelAsLink: ${supplementalBPanelAsLink},
                siteUpdaters: [
                    <c:forEach items="${siteUpdaters}" var="su" varStatus="sus">
                    {id: '${su.key}', displayName: '${su.value}'}<c:if test="${!sus.last}">,</c:if>
                    </c:forEach>
                ],
                i18n: {
                    <c:forEach items="${i18n}" var="i" varStatus="is">
                    ${i.key}: "${i.value}"<c:if test="${!is.last}">,</c:if>
                    </c:forEach>
                }
            };
    
        </script>
        <script type="text/javascript" src="/feedback-tool/js/feedbackutils.js"></script>
        ${sakaiHtmlHead}
    </head>

    <body>

        <!-- wrap tool in portletBody div for PDA portal compatibility -->
        <div class="portletBody">
            <ul id="feedback-toolbar" class="navIntraTool actionToolBar" role="menu"></ul>
            <div class="portletBody">
                <div id="feedback-error-message-wrapper">
                    <div>
                        <span></span><a href="javascript:;" alt="${i18n.close}" title="${i18n.close}"></a>
                    </div>
                </div>
                <div id="feedback-info-message-wrapper">
                    <div>
                        <span></span><a href="javascript:;" alt="${i18n.close}" title="${i18n.close}"></a>
                    </div>
                </div>
                <div id="feedback-content"></div>
            </div>
        </div>
        <script type="text/javascript" src="/feedback-tool/js/feedback.js"></script>

    </body>
</html>