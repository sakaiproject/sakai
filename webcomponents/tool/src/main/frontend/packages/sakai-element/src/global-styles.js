import { css, unsafeCSS } from "lit";

let globalSheets = null;

export function getGlobalStyleSheets() {
  if (globalSheets === null) {
    globalSheets = Array.from(document.styleSheets)
      .map(x => {
        const cssText = Array.from(x.cssRules)
          .filter(rule => rule.constructor.name !== "CSSImportRule")
          .map(rule => rule.cssText)
          .join(" ");
        return css`${unsafeCSS(cssText)}`;
      });
  }

  return globalSheets;
}

export function addGlobalStylesToShadowRoot(shadowRoot) {
  shadowRoot.adoptedStyleSheets.push(
    ...getGlobalStyleSheets()
  );
}
