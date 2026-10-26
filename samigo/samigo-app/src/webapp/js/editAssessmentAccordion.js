/*
 * editAssessmentAccordion.js
 *
 * Accordion of sections on the assessment editing screen (Samigo).
 *
 * Requires that each section be rendered as:
 *   <details open="open" data-part-id=“<assessmentId>-<sectionId>”>
 *     <summary>...section header...</summary>
 *     ...content...
 *   </details>
 *
 * - Stores in sessionStorage which sections the user has collapsed.
 *   The key is “samigo.editAssessment.<assessmentId>-<sectionId>”, so
 *   the state is independent for each assessment and section.
 * - The state only persists while the browser tab is open.
 * - Sections with an empty header (for example, when editing a question pool) remain
 *   always open, without an arrow and without saved state.
 */

(function () {
  var PREFIX = 'samigo.editAssessment.';

  var storage = null;
  try {
    storage = window.sessionStorage;
    storage.setItem(PREFIX + 'test', '1');
    storage.removeItem(PREFIX + 'test');
  } catch (e) {
    storage = null;
  }

  function keyFor(partId) {
    return PREFIX + partId;
  }

  function save(el, partId) {
    if (!storage || !partId) { return; }
    try {
      storage.setItem(keyFor(partId), el.open ? '1' : '0');
    } catch (e) {
      /* sessionStorage full or unavailable: ignored */
    }
  }

  /* Header with no text or controls (e.g., when editing a pool, where the
     panelGrid for that section is rendered with empty cells). */
  function headerIsEmpty(header) {
    if (header.querySelector('input, select, button, textarea, a, img, i')) {
      return false;
    }
    return header.textContent.replace(/\s+/g, '') === '';
  }

  function init() {
    var parts = document.querySelectorAll('details[data-part-id]');
    var i;

    for (i = 0; i < parts.length; i++) {
      (function (el) {
        var partId = el.getAttribute('data-part-id');
        var summary = el.querySelector('summary');
        if (!partId || !summary) { return; }

        // Empty header: cannot be collapsed; it is always left open and without an arrow.
        if (headerIsEmpty(summary)) {
          el.open = true;
          summary.style.listStyle = 'none';
          summary.style.cursor = 'default';
          summary.addEventListener('click', function (e) { e.preventDefault(); });
          el.removeAttribute('data-part-id');
          return;
        }

        if (storage) {
          var state = null;
          try { state = storage.getItem(keyFor(partId)); } catch (e) {}
          if (state === '0') { el.open = false; }
        }

        // Saves the state after each user interaction (mouse or keyboard).
        // setTimeout is used to read the updated value of “open”.
        summary.addEventListener('click', function () {
          window.setTimeout(function () { save(el, partId); }, 0);
        });
      })(parts[i]);
    }

    // Backup: saves the status of all parts when the form is submitted
    // or when the page is left (update points, save, etc.).
    window.addEventListener('pagehide', function () {
      for (var j = 0; j < parts.length; j++) {
        save(parts[j], parts[j].getAttribute('data-part-id'));
      }
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
