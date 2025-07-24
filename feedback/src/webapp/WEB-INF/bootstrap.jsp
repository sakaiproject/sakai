<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
      xml:lang="${language}"
      lang="${language}">

<head>
    <link rel="stylesheet" type="text/css" href="/feedback-tool/css/feedback.css"/>

    <script>
      var feedback = {
        state: "home",
        previousState: "",
        userId: "${userId}",
        siteId: "${siteId}",
        siteExists: "${siteExists}",
        language: "${language}",
        featureSuggestionUrl: "${featureSuggestionUrl}",
        technicalToAddress: "${technicalToAddress}",
        helpToAddress: "${helpToAddress}",
        suggestionsToAddress: "${suggestionsToAddress}",
        supplementalAToAddress: "${supplementalAToAddress}",
        supplementalBToAddress: "${supplementalBToAddress}",
        contactName: "${contactName}",
        enableTechnical: ${enableTechnical},
        enableHelp: ${enableHelp},
        enableSuggestions: ${enableSuggestions},
        enableSupplementalA: ${enableSupplementalA},
        enableSupplementalB: ${enableSupplementalB},
        helpPagesUrl: "${helpPagesUrl}",
        helpdeskUrl: "${helpdeskUrl}",
        technicalUrl: "${technicalUrl}",
        supplementalAUrl: "${supplementalAUrl}",
        supplementalBUrl: "${supplementalBUrl}",
        helpPagesTarget: "${helpPagesTarget}",
        supplementaryInfo: "${supplementaryInfo}",
        recaptchaPublicKey: "${recaptchaPublicKey}",
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
            {id: "${su.key}", displayName: "${su.value}"}<c:if test="${!sus.last}">, </c:if>
          </c:forEach>
        ],
        i18n: {
          <c:forEach items="${i18n}" var="i" varStatus="is">
            ${i.key}: "${i.value}"<c:if test="${!is.last}">,</c:if>
          </c:forEach>
        }
      }
    </script>
    ${sakaiHtmlHead}
</head>

<body>
    <!-- wrap tool in portletBody div for PDA portal compatibility -->
    <div class="portletBody container-fluid">
        <ul id="feedback-toolbar" class="navIntraTool actionToolBar" role="menu"></ul>
        <div id="feedback-error-message-wrapper" class="sak-banner-error">
            <span></span>
        </div>
        <div id="feedback-info-message-wrapper" class="sak-banner-success">
            <span></span>
        </div>
        <div id="feedback-content"></div>
    </div>


    <script>includeLatestJQuery("feedback");</script>
    <script>includeWebjarLibrary("multifile");</script>
    <script>includeWebjarLibrary("handlebars");</script>
    <script src="/feedback-tool/templates/all.handlebars.js"></script>

    <c:if test="${recaptchaEnabled}">
        <script src="//www.google.com/recaptcha/api/js/recaptcha_ajax.js"></script>
    </c:if>

    <script type="module">
      import Feedback from "/feedback-tool/js/feedback.js";
      $(document).ready(function () {
        new Feedback(feedback);
      });
    </script>
</body>
</html>