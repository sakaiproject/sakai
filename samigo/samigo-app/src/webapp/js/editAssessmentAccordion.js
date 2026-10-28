/*
 * editAssessmentAccordion.js
 *
 * Accordion of sections on the assessment editing screen (Samigo).
 *
 * Requires that each section be rendered as:
 *   <details open="open" data-part-id="<assessmentId>-<sectionId>">
 *     <summary>...section header...</summary>
 *     ...content...
 *   </details>
 *
 * - Stores in sessionStorage which sections the user has collapsed.
 *   The key is "accordion-state-<assessmentId>-<sectionId>", so
 *   the state is independent for each assessment and section.
 * - The state only persists while the browser tab is open.
 */

document.addEventListener("DOMContentLoaded", () => {
  const accordions = document.querySelectorAll("details[data-part-id]");

  accordions.forEach((details) => {
    const partId = details.getAttribute("data-part-id");
    if (!partId) return;

    const storageKey = `accordion-state-${partId}`;

    try {
      const savedState = window.sessionStorage.getItem(storageKey);
      if (savedState === "open") {
        details.setAttribute("open", "open");
      } else if (savedState === "closed") {
        details.removeAttribute("open");
      }
    } catch (error) {
      console.warn("Could not read from sessionStorage:", error);
    }

    details.addEventListener("toggle", (event) => {
      const isOpen = details.hasAttribute("open");

      try {
        window.sessionStorage.setItem(storageKey, isOpen ? "open" : "closed");
      } catch (error) {
        console.warn("Could not save to sessionStorage:", error);
      }
    });
  });
});
