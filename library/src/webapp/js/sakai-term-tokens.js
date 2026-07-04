/*
 * Display-time substitution of academic term tokens in rendered content.
 *
 * When portal.termtokens.enabled=true, the portal injects sakai.termsInfo
 * (see PortalService.getTermTokensScript) and this script into the head of
 * every tool page. Authors put the tokens below into rich-text content (a
 * CKEditor dropdown inserts them); this script replaces them in the rendered
 * DOM, so the stored content always keeps the raw tokens.
 *
 * Only these exact tokens are replaced — never arbitrary {{...}} — so
 * client-side template syntax (mustache/handlebars) in tool markup is safe.
 */
(function () {

  "use strict";

  if (window.sakaiTermTokens) return;

  // Mirrors the token list in editor/ckextraplugins/sakaitermtokens/plugin.js
  // and the field names emitted by PortalServiceImpl.buildTermTokensScript —
  // the three must stay in sync when adding or renaming a token.
  const TOKEN_KEYS = {
    "{{sitetitle}}": "siteTitle",
    "{{siteterm}}": "siteTerm",
    "{{sitetermshort}}": "siteTermShort",
    "{{termstart}}": "termStart",
    "{{termstartshort}}": "termStartShort",
    "{{termend}}": "termEnd",
    "{{termendshort}}": "termEndShort",
    "{{termyear}}": "termYear",
    "{{weekofterm}}": "weekOfTerm",
    "{{weeksinterm}}": "weeksInTerm",
    "{{daysleftinterm}}": "daysLeftInTerm",
    "{{currentterm}}": "currentTerm",
    "{{currenttermshort}}": "currentTermShort",
    "{{currenttermstart}}": "currentTermStart",
    "{{currenttermend}}": "currentTermEnd",
    "{{nextterm}}": "nextTerm",
    "{{nexttermstart}}": "nextTermStart",
    "{{firstname}}": "firstName",
    "{{lastname}}": "lastName",
    "{{fullname}}": "fullName",
    "{{useremail}}": "userEmail",
    "{{instructor}}": "instructor",
    "{{instructoremail}}": "instructorEmail",
    "{{institution}}": "institution",
    "{{siteurl}}": "siteUrl",
    "{{today}}": "today",
    "{{dayofweek}}": "dayOfWeek",
    "{{currentmonth}}": "currentMonth",
    "{{currentyear}}": "currentYear",
  };
  // longer alternatives first within each family so prefixes never win
  const TOKEN_REGEX = /\{\{(?:sitetitle|sitetermshort|siteterm|siteurl|termstartshort|termstart|termendshort|termend|termyear|weekofterm|weeksinterm|daysleftinterm|currenttermshort|currenttermstart|currenttermend|currentterm|currentmonth|currentyear|nexttermstart|nextterm|firstname|lastname|fullname|useremail|instructoremail|instructor|institution|today|dayofweek)\}\}/g;

  // Never rewrite text the user is editing, or non-content subtrees.
  const SKIP_SELECTOR = "script,style,textarea,noscript,[contenteditable],.cke";

  function getTermsInfo() {

    if (window.sakai && window.sakai.termsInfo) return window.sakai.termsInfo;
    try {
      // Iframed tools can fall back to the top portal frame (same pattern as mathjax-config.js).
      return window.top && window.top.sakai && window.top.sakai.termsInfo;
    } catch (e) {
      return undefined;
    }
  }

  function replaceTextNode(node, info) {

    const text = node.nodeValue;
    if (!text || text.indexOf("{{") === -1) return;

    const parent = node.parentElement;
    if (!parent || parent.closest(SKIP_SELECTOR)) return;

    // Tokens with no value for this site (e.g. a project site with no term) become empty.
    const replaced = text.replace(TOKEN_REGEX, token => info[TOKEN_KEYS[token]] || "");
    if (replaced !== text) node.nodeValue = replaced;
  }

  function replaceIn(root) {

    const info = getTermsInfo();
    if (!info || !root) return;

    if (root.nodeType === Node.TEXT_NODE) {
      replaceTextNode(root, info);
      return;
    }

    if (root.nodeType !== Node.ELEMENT_NODE
        && root.nodeType !== Node.DOCUMENT_NODE
        && root.nodeType !== Node.DOCUMENT_FRAGMENT_NODE) {
      return;
    }

    if (root.textContent.indexOf("{{") === -1) return;

    const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT);
    const textNodes = [];
    while (walker.nextNode()) textNodes.push(walker.currentNode);
    textNodes.forEach(node => replaceTextNode(node, info));
  }

  function start() {

    replaceIn(document.body);

    // Sakai webcomponents render into light DOM and many tools load content
    // over ajax, so keep substituting as nodes arrive or change.
    const observer = new MutationObserver(mutations => {

      const info = getTermsInfo();
      if (!info) return;

      mutations.forEach(mutation => {
        if (mutation.type === "characterData") {
          replaceTextNode(mutation.target, info);
        } else {
          mutation.addedNodes.forEach(node => replaceIn(node));
        }
      });
    });
    observer.observe(document.body, { childList: true, subtree: true, characterData: true });
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", start);
  } else {
    start();
  }

  // Shadow-DOM components can opt in by calling this on their render root.
  window.sakaiTermTokens = { replaceIn };
})();
