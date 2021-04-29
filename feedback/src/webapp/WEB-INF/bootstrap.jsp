<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html  
    xmlns="http://www.w3.org/1999/xhtml"
    xml:lang="${language}"
    lang="${language}">
    <head>
        <link rel="stylesheet" type="text/css" href="/feedback-tool/css/feedback.css" />
        <script src="/library/webjars/jquery/1.12.4/jquery.min.js"></script>
        <script src="/feedback-tool/lib/jquery.form.min.js"></script>
        <script src="/library/webjars/multifile/2.2.2/jquery.MultiFile.min.js"></script>
        <script src="/feedback-tool/lib/handlebars.runtime-v1.3.0.js"></script>
        <script src="/feedback-tool/templates/all.handlebars"></script>

        <c:if test="${recaptchaEnabled}">
            <script src="//www.google.com/recaptcha/api/js/recaptcha_ajax.js"></script>
        </c:if>

        <script>

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
        <script src="/feedback-tool/js/feedbackutils.js"></script>
        ${sakaiHtmlHead}
    </head>

    <body>

        <!-- wrap tool in portletBody div for PDA portal compatibility -->
        <div class="portletBody">
            <ul id="feedback-toolbar" class="navIntraTool actionToolBar" role="menu"></ul>
            <div class="portletBody">
                <div id="feedback-error-message-wrapper" class="sak-banner-error">
                    <span></span>
                </div>
                <div id="feedback-info-message-wrapper" class="sak-banner-success">
                    <span></span>
                </div>
                <div id="feedback-content"></div>
            </div>
        </div>
        <script src="/feedback-tool/js/feedback.js"></script>

    </body>
</html>