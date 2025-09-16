import { css, unsafeCSS } from "lit";

let globalSheets = null;

/**
 * Returns an array of Lit CSSResult instances, one for every stylesheet in document.styleSheets.
 * You can add these to your "styles" array in your Lit components like this:
 *
 * <pre><code>
 *  static styles = [
 *    ...getDocumentStyleSheets(),
 *    css`
 *      select[multiple], select[size]:not([size='1']) {
 *        background-image: none;
 *      }
*     `,
 *  ];
 * </code></pre>
 */
export function getDocumentStyleSheets() {

  if (globalSheets !== null) return globalSheets;

  globalSheets = [];

  for (const sheet of document.styleSheets) {
    try {
      const cssText = Array.from(sheet.cssRules || [])
        .filter(rule => rule.constructor.name !== "CSSImportRule")
        .map(rule => rule.cssText)
        .join(" ");
      cssText && globalSheets.push(css`${unsafeCSS(cssText)}`);
    } catch (e) {
      console.debug(`Skipping stylesheet (${e.name}): ${sheet.href || "inline"}`);
    }
  }
  return globalSheets;
}
