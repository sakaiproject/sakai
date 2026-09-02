var DTMN = DTMN || {};

DTMN.toolList = [ "assignments", "assessments", "signup", "gradebook", "resources", "calendar", "forums", "announcements", "lessons" ];
DTMN.collapseElements = [ ];
DTMN.termFields = [ "classes_start", "classes_end", "exam_begins", "exam_ends" ];
DTMN.nextIndex = -1;

DTMN.initDatePicker = function(updates, notModified) {

  // use an event listener to populate the date pickers on demand instead of populating everything on page load
  DTMN.toolList.forEach(tool => {

    const collapseId = `collapse-${tool}`;
    const link = document.querySelector(`a[href="#${collapseId}"]`);

    if (!link) {
      console.debug(`No collapse toggle for tool ${tool}, which is not right`);
      return;
    }

    const collapseElement = document.getElementById(collapseId);
    collapseElement && DTMN.collapseElements.push(collapseElement);

    collapseElement.addEventListener("show.bs.collapse", () => {

      const spinner = link.querySelector(".allocatedSpinPlaceholder");
      spinner.classList.add("spinPlaceholder");
      window.setTimeout(() => {

        DTMN.attachDatePicker(`#${collapseId} .datepicker:not(.hasDatepicker)`, updates, notModified);
        spinner.classList.remove("spinPlaceholder");
      }, 25); // delay 25ms to give browser time to render the spinner
    });

    collapseElement.addEventListener("shown.bs.collapse", () => {
      DTMN.validateShiftInput();
      if (DTMN.validateTermInputs) {
        DTMN.validateTermInputs();
      }
      if (DTMN.validateFitInputs) {
        DTMN.validateFitInputs();
      }
    });
    collapseElement.addEventListener("hidden.bs.collapse", () => {
      DTMN.validateShiftInput();
      if (DTMN.validateTermInputs) {
        DTMN.validateTermInputs();
      }
      if (DTMN.validateFitInputs) {
        DTMN.validateFitInputs();
      }
    });
  });
};

// ----- Bulk method radios: show one bulk-edit panel (and its Apply button) at a time -----

DTMN.initBulkModeRadios = function() {
  const scopeRow = document.querySelector(".dm-scope");
  document.querySelectorAll("input[name='dm-bulk-mode']").forEach(function(radio) {
    radio.addEventListener("change", function() {
      document.querySelectorAll(".dm-bulk-panel").forEach(function(panel) {
        panel.classList.toggle("d-none", panel.id !== radio.value);
      });
      document.querySelectorAll(".dm-bulk-apply").forEach(function(button) {
        button.classList.toggle("d-none", button.dataset.dmPanel !== radio.value);
      });
      // The milestone matrix already targets tools column by column, so the scope row
      // would be redundant there — it only accompanies the shift and fit methods.
      if (scopeRow) {
        scopeRow.classList.toggle("d-none", radio.value === "dm-bulk-panel-set");
      }
    }, false);
  });
};

// ----- Tool scope row: which tool sections the bulk applies act on -----

DTMN.initScopePicker = function() {
  DTMN.scopeChecks = Array.from(document.querySelectorAll(".dm-scope-tool"));
  DTMN.scopeAllCheck = document.getElementById("dm-scope-all");

  if (!DTMN.scopeChecks.length) {
    return;
  }

  // A scope change can enable or disable the shift/fit Apply buttons, so re-run their
  // validators, and re-label the buttons so a narrowed scope stays visible before the click.
  // The milestone matrix ignores the scope row, so its validator is not involved.
  const revalidate = function() {
    DTMN.updateScopeAllState();
    DTMN.updateApplyButtonLabels();
    DTMN.validateShiftInput();
    DTMN.validateFitInputs();
    DTMN.updateFitAnchorHints();
  };

  DTMN.scopeChecks.forEach(function(check) {
    check.addEventListener("change", revalidate, false);
  });

  if (DTMN.scopeAllCheck) {
    DTMN.scopeAllCheck.addEventListener("change", function() {
      DTMN.scopeChecks.forEach(function(check) { check.checked = DTMN.scopeAllCheck.checked; });
      revalidate();
    }, false);
  }

  DTMN.updateScopeAllState();
  DTMN.updateApplyButtonLabels();
};

DTMN.updateScopeAllState = function() {
  if (!DTMN.scopeAllCheck) {
    return;
  }
  const checkedCount = DTMN.scopeChecks.filter(function(check) { return check.checked; }).length;
  DTMN.scopeAllCheck.checked = checkedCount === DTMN.scopeChecks.length;
  DTMN.scopeAllCheck.indeterminate = checkedCount > 0 && checkedCount < DTMN.scopeChecks.length;
};

// {0}/{1}-style placeholder substitution for the localized banner/button templates.
DTMN.formatTemplate = function(template, args) {
  return args.reduce(function(acc, val, i) {
    return acc.split("{" + i + "}").join(String(val));
  }, template);
};

// Reflect a narrowed scope right on the apply buttons ("Apply shift (2 of 7 tools)") so a
// selection left over from an earlier apply is visible before the next click. The term
// apply is exempt: the milestone matrix ignores the scope row.
DTMN.updateApplyButtonLabels = function() {
  if (!DTMN.scopeChecks || !DTMN.scopeChecks.length || !DTMN.bulkFeedbackText) {
    return;
  }
  const checked = DTMN.scopeChecks.filter(function(check) { return check.checked; }).length;
  const total = DTMN.scopeChecks.length;
  document.querySelectorAll(".dm-bulk-apply:not(#dm-apply-term)").forEach(function(button) {
    if (!button.dataset.baseLabel) {
      button.dataset.baseLabel = button.textContent.trim();
    }
    button.textContent = button.dataset.baseLabel + (checked < total
        ? " " + DTMN.formatTemplate(DTMN.bulkFeedbackText.applySuffix, [checked, total]) : "");
  });
};

// Post-apply summary banner: says what the apply just changed (and what it skipped), since the
// affected grid lives on the other tab. Names the ticked tools whenever the scope was narrowed.
DTMN.showApplyFeedback = function(kind, stats) {
  const banner = document.getElementById("dm-apply-feedback");
  const msgSpan = document.getElementById("dm-apply-feedback-msg");
  const scopeSpan = document.getElementById("dm-apply-feedback-scope");
  if (!banner || !msgSpan || !DTMN.bulkFeedbackText) {
    return;
  }

  let msg = stats.changed === 0
      ? DTMN.bulkFeedbackText.none
      : DTMN.formatTemplate(DTMN.bulkFeedbackText[kind], [stats.changed, stats.tools]);
  if (kind === "term" && stats.blanksSkipped > 0) {
    msg += " " + DTMN.formatTemplate(DTMN.bulkFeedbackText.termBlanks, [stats.blanksSkipped]);
  }
  msgSpan.textContent = msg + " ";

  if (scopeSpan) {
    // The term apply ignores the scope row (the matrix picks its own tools), so a narrowed
    // scope is only worth calling out for the shift and fit applies.
    const narrowed = kind !== "term"
        && DTMN.scopeChecks && DTMN.scopeChecks.some(function(check) { return !check.checked; });
    if (narrowed) {
      const names = DTMN.scopeChecks.filter(function(check) { return check.checked; })
          .map(function(check) { return check.closest("label").textContent.trim(); });
      scopeSpan.textContent = DTMN.formatTemplate(DTMN.bulkFeedbackText.scope, [names.join(", ")]) + " ";
      scopeSpan.classList.remove("d-none");
    } else {
      scopeSpan.classList.add("d-none");
    }
  }

  banner.classList.remove("d-none");

  // Every apply changes dates, which can change which items are the fit anchors.
  DTMN.updateFitAnchorHints();
};

// ----- Unsaved-changes guard: staged edits live only in the page until Save Changes -----

// The save button is enabled exactly while edits are staged and disabled again once they
// are saved, so it doubles as the pending-changes flag.
DTMN.hasUnsavedChanges = function() {
  const saveButton = document.getElementById("submit-form-button");
  return saveButton !== null && !saveButton.disabled;
};

// Prompts the browser-native "leave site?" dialog on any navigation away while changes are
// staged. The Cancel flow is exempt (it sets the suppress flag): clicking Cancel is already
// an explicit discard, so it should not be second-guessed.
DTMN.initUnsavedGuard = function() {
  window.addEventListener("beforeunload", function(e) {
    if (!DTMN.suppressUnsavedGuard && DTMN.hasUnsavedChanges()) {
      e.preventDefault();
      e.returnValue = "";
    }
  });
};

// Cancel abandons the staged edits and returns to the Site Info main page. Staged edits live
// only in the page, so leaving the helper discards them; dropping the helper segment from the
// URL hands control back to the host tool (Site Info).
DTMN.initCancelRevert = function() {
  const cancelButton = document.getElementById("datemanager-cancel");
  cancelButton && cancelButton.addEventListener("click", function(e) {
    e.preventDefault();
    // Clicking Cancel is already an explicit discard, so don't let the unsaved-changes guard
    // second-guess the navigation away.
    DTMN.suppressUnsavedGuard = true;
    // The helper is mounted one path segment below the tool placement; dropping that segment
    // returns to the host tool (Site Info).
    const path = window.location.pathname.replace(/\/$/, "");
    window.location = path.substring(0, path.lastIndexOf("/"));
  }, false);

  let reverted = false;
  try {
    reverted = window.sessionStorage.getItem("dm-cancel-reverted") === "1";
    window.sessionStorage.removeItem("dm-cancel-reverted");
  } catch (err) { /* storage unavailable */ }

  if (reverted) {
    const banner = document.getElementById("dm-cancel-banner");
    if (banner) {
      banner.classList.remove("d-none");
      window.setTimeout(function() {
        const animation = banner.animate({ opacity: [1, 0] }, { duration: 400, easing: "ease" });
        animation.finished.then(function() {
          banner.classList.add("d-none");
          animation.cancel();
        });
      }, 4000);
    }
  }
};

// Section elements for the tools ticked in the scope row. Falls back to every section when the
// scope row is absent so the apply paths keep working without it.
DTMN.getScopedSections = function() {
  if (!DTMN.scopeChecks || !DTMN.scopeChecks.length) {
    return DTMN.collapseElements;
  }
  const roots = new Set(DTMN.scopeChecks.filter(function(check) { return check.checked; })
      .map(function(check) { return check.dataset.root; }));
  return DTMN.collapseElements.filter(function(section) { return roots.has(section.id); });
};

DTMN.initShifter = function(updates, notModified) {

  DTMN.validShiftRegex = /^-{0,1}\d{1,4}$/;

  DTMN.shiftErrorBanner = document.getElementById("dateShifterError");
  DTMN.shiftInput = document.getElementById("dateShifterDays");
  DTMN.shiftApplyBtn = document.getElementById("dm-apply-shift");

  // Add event listener to the submit button to clear visual indication
  $('#modal-btn-confirm').on('click', function() {
    DTMN.clearChangedDateIndication();
  });

  DTMN.shiftInput.addEventListener("input", () => DTMN.validateShiftInput(), false);

  // The whole page is one form; Enter in the day-count field must not trigger a save.
  DTMN.shiftInput.addEventListener("keydown", function(e) {
    if (e.key === "Enter") {
      e.preventDefault();
    }
  }, false);

  DTMN.shiftApplyBtn.addEventListener("click", function() {
    DTMN.handleShiftButtonClick(this, DTMN.getScopedSections(), updates, notModified);
  }, false);

  DTMN.initDateDiff();
};

// ----- Term start pickers: whole days between the old and new term start, driving the shift field -----

// Pure: signed whole days from `from` to `to` (to - from), counting calendar days, ignoring time of day.
DTMN.computeDayDiff = function(from, to) {
  return to.clone().startOf("day").diff(from.clone().startOf("day"), "days");
};

DTMN.initDateDiff = function() {

  DTMN.diffFromInput = document.getElementById("date-diff-from");
  DTMN.diffFromHidden = document.getElementById("date-diff-from-hidden");
  DTMN.diffToInput = document.getElementById("date-diff-to");
  DTMN.diffToHidden = document.getElementById("date-diff-to-hidden");
  DTMN.diffResult = document.getElementById("date-diff-result");

  if (!DTMN.diffFromHidden || !DTMN.diffToHidden) {
    return;
  }

  [[DTMN.diffFromInput, DTMN.diffFromHidden], [DTMN.diffToInput, DTMN.diffToHidden]].forEach(function(pair) {
    const input = pair[0];
    const hidden = pair[1];
    if (!input || !hidden) {
      return;
    }
    localDatePicker({
      input,
      useTime: 0,
      parseFormat: 'YYYY-MM-DD',
      allowEmptyDate: true,
      ashidden: {
        iso8601: hidden.id,
      }
    });
    hidden.addEventListener("change", () => DTMN.applyDateDiff(), false);
  });
};

// Returns the whole-day difference when both dates are present and valid, else null.
DTMN.getDateDiffDays = function() {
  if (!DTMN.diffFromHidden || !DTMN.diffToHidden) {
    return null;
  }
  if (DTMN.diffFromHidden.value === "" || DTMN.diffToHidden.value === "") {
    return null;
  }
  const from = DTMN.parseInputDateValue(DTMN.diffFromHidden.value, false);
  const to = DTMN.parseInputDateValue(DTMN.diffToHidden.value, false);
  if (!from.isValid() || !to.isValid()) {
    return null;
  }
  return DTMN.computeDayDiff(from, to);
};

// Show the count as soon as both term start dates are valid and load it straight into the shift
// field, where it stays editable for fine-tuning. The shifter's own validation then enables its
// buttons (a zero-day count leaves them disabled). Spans beyond what the shifter accepts
// (+/-9999 days) are shown but not loaded, so the field can never dead-end.
DTMN.applyDateDiff = function() {
  const days = DTMN.getDateDiffDays();

  if (DTMN.diffResult) {
    if (days === null) {
      DTMN.diffResult.textContent = "";
    } else {
      const template = DTMN.diffResult.dataset.template || "{0}";
      DTMN.diffResult.textContent = template.replace("{0}", days);
    }
  }

  if (days === null || days < -9999 || days > 9999) {
    return;
  }

  DTMN.shiftInput.value = days;
  DTMN.validateShiftInput();
};

// ----- Re-anchor / fit dates between a new first and last date -----
//
// Unlike the offset shifter (which adds a constant number of days and lets the end float), the fitter
// pins the earliest dated item to the entered "new first" date and the latest to the entered "new last"
// date, then spreads everything in between proportionally. With "Snap to weeks" on, each in-between item
// keeps its own weekday and clock time and only moves by whole weeks, so the weekly cadence of a copied
// course survives the move into a shorter or longer term. The fitter only ever rewrites cells that
// already hold a date - it never fills blanks.

DTMN.initFitter = function(updates, notModified) {

  DTMN.fitErrorBanner = document.getElementById("date-fit-error");
  DTMN.fitFirstInput = document.getElementById("date-fit-first");
  DTMN.fitFirstHidden = document.getElementById("date-fit-first-hidden");
  DTMN.fitLastInput = document.getElementById("date-fit-last");
  DTMN.fitLastHidden = document.getElementById("date-fit-last-hidden");
  DTMN.fitSnapCheckbox = document.getElementById("date-fit-snap");
  DTMN.fitApplyBtn = document.getElementById("dm-apply-fit");

  if (!DTMN.fitApplyBtn || !DTMN.fitFirstHidden || !DTMN.fitLastHidden) {
    return;
  }

  [[DTMN.fitFirstInput, DTMN.fitFirstHidden], [DTMN.fitLastInput, DTMN.fitLastHidden]].forEach(function(pair) {
    const input = pair[0];
    const hidden = pair[1];
    if (!input || !hidden) {
      return;
    }
    localDatePicker({
      input,
      useTime: 1,
      parseFormat: 'YYYY-MM-DDTHH:mm:ss',
      allowEmptyDate: true,
      ashidden: {
        iso8601: hidden.id,
      }
    });
    hidden.addEventListener("change", () => DTMN.validateFitInputs(), false);
  });

  DTMN.fitApplyBtn.addEventListener("click", function() {
    DTMN.handleFitButtonClick(this, DTMN.getScopedSections(), updates, notModified);
  }, false);

  DTMN.validateFitInputs();
  DTMN.updateFitAnchorHints();
};

// Show the current earliest/latest date under the two destination fields ("currently:
// Thu, Apr 9, 2026") so the range being re-fitted — and the weekday the anchors hold today —
// is visible before applying. Dates only: several items routinely tie for earliest/latest, so
// naming one of them would be arbitrary. Recomputed when the scope changes and after every
// apply, since both change the range.
DTMN.updateFitAnchorHints = function() {
  const firstHint = document.getElementById("dm-fit-anchor-first");
  const lastHint = document.getElementById("dm-fit-anchor-last");
  if (!firstHint || !lastHint) {
    return;
  }

  let earliest = null;
  let latest = null;
  DTMN.getScopedSections().forEach(function(section) {
    section.querySelectorAll("tbody input[type=hidden][data-tool][data-field]").forEach(function(hidden) {
      if (!hidden.value) {
        return;
      }
      const useTime = hidden.dataset.tool !== "gradebookItems";
      const parsed = DTMN.parseInputDateValue(hidden.value, useTime);
      if (!parsed.isValid()) {
        return;
      }
      if (earliest === null || DTMN.wallClockMs(parsed) < DTMN.wallClockMs(earliest)) { earliest = parsed; }
      if (latest === null || DTMN.wallClockMs(parsed) > DTMN.wallClockMs(latest)) { latest = parsed; }
    });
  });

  [[firstHint, earliest], [lastHint, latest]].forEach(function(pair) {
    const hint = pair[0];
    const anchor = pair[1];
    if (anchor === null) {
      hint.textContent = hint.dataset.none || "";
    } else {
      const when = anchor.locale(sakai.locale.userLocale).format("ddd, ll");
      hint.textContent = DTMN.formatTemplate(hint.dataset.template || "{0}", [when]);
    }
  });
};

DTMN.getFitAnchors = function() {
  if (!DTMN.fitFirstHidden || !DTMN.fitLastHidden) {
    return null;
  }
  if (DTMN.fitFirstHidden.value === "" || DTMN.fitLastHidden.value === "") {
    return null;
  }
  const first = DTMN.parseInputDateValue(DTMN.fitFirstHidden.value, true);
  const last = DTMN.parseInputDateValue(DTMN.fitLastHidden.value, true);
  if (!first.isValid() || !last.isValid()) {
    return null;
  }
  return { first, last };
};

DTMN.validateFitInputs = function() {
  if (!DTMN.fitApplyBtn) {
    return;
  }

  const anchors = DTMN.getFitAnchors();
  const rangeOk = anchors !== null && DTMN.wallClockMs(anchors.last) > DTMN.wallClockMs(anchors.first);

  // Surface the ordering error only once both dates are present but out of order.
  if (anchors !== null && !rangeOk) {
    DTMN.showFitError();
  } else {
    DTMN.hideFitError();
  }

  DTMN.fitApplyBtn.disabled = !rangeOk || DTMN.getScopedSections().length === 0;
};

DTMN.showFitError = function() {
  if (!DTMN.fitErrorBanner) {
    return;
  }
  DTMN.fitErrorBanner.classList.remove("d-none");
  DTMN.fitErrorBanner.setAttribute("role", "alert");
};

DTMN.hideFitError = function() {
  if (!DTMN.fitErrorBanner) {
    return;
  }
  DTMN.fitErrorBanner.classList.add("d-none");
  DTMN.fitErrorBanner.removeAttribute("role");
};

DTMN.handleFitButtonClick = function(button, sections, updates, notModified) {

  const anchors = DTMN.getFitAnchors();
  if (!anchors || DTMN.wallClockMs(anchors.last) <= DTMN.wallClockMs(anchors.first) || sections.length === 0) {
    return;
  }

  button.classList.add("spinButton");
  DTMN.fitApplyBtn.disabled = true;

  window.setTimeout(function() {
    const stats = DTMN.fitDates(anchors, sections, updates, notModified) || { changed: 0, tools: 0 };
    button.classList.remove("spinButton");
    DTMN.validateFitInputs();
    DTMN.showApplyFeedback("fit", stats);
  }, 25);
};

// Move `target` to the nearest day (within +/- 3 days) that shares `source`'s weekday, carrying
// `source`'s clock time. Two items that share a weekday therefore stay a whole number of weeks apart.
DTMN.snapToSourceWeekday = function(target, source) {
  const result = target.clone();
  result.hours(source.hours());
  result.minutes(source.minutes());
  result.seconds(source.seconds());
  result.milliseconds(0);

  let deltaDays = source.day() - result.day();
  if (deltaDays > 3) {
    deltaDays -= 7;
  } else if (deltaDays < -3) {
    deltaDays += 7;
  }
  if (deltaDays !== 0) {
    result.add(deltaDays, "days");
  }
  return result;
};

// Timezone-neutral wall-clock milliseconds: the moment's own calendar fields laid onto the UTC
// scale. All fitting arithmetic runs on these, never on valueOf(): valueOf() is a browser-local
// instant, and across a DST transition elapsed instant-milliseconds differ from elapsed wall-clock
// time, so the same spread would fit to different clock times depending on the browser's timezone.
DTMN.wallClockMs = function(date) {
  return Date.UTC(date.year(), date.month(), date.date(), date.hours(), date.minutes(), date.seconds());
};

// Map one source wall-clock value onto the new [first, last] range. Pure (no DOM): the earliest
// source (frac <= 0) lands exactly on anchors.first, the latest (frac >= 1) exactly on anchors.last,
// and a middle source is placed proportionally then, when snap is on, snapped to its own
// weekday/time. `currentMs` and `oldStartMs` are wallClockMs values; the result is built in UTC
// mode so an interpolated time that does not exist in the browser's zone (spring-forward gap)
// cannot be shifted by moment. `sourceMoment` is only used by the snap branch. A zero old span
// (all dates identical) collapses every date onto anchors.first.
DTMN.computeFittedDate = function(currentMs, oldStartMs, oldSpan, anchors, snap, sourceMoment) {
  if (oldSpan <= 0) {
    return anchors.first.clone();
  }

  const frac = (currentMs - oldStartMs) / oldSpan;
  if (frac <= 0) {
    return anchors.first.clone();
  }
  if (frac >= 1) {
    return anchors.last.clone();
  }

  const newStartMs = DTMN.wallClockMs(anchors.first);
  const newSpan = DTMN.wallClockMs(anchors.last) - newStartMs;
  const target = moment.utc(newStartMs + frac * newSpan);
  return snap ? DTMN.snapToSourceWeekday(target, sourceMoment) : target;
};

// Map every cell of one item (table row) onto the new range. Snapping is decided per row, not per
// cell: each cell is snapped independently first, but if that would reorder the row's own dates
// (an open/due pair can invert under heavy term compression, which the server rejects on save) or
// push a date outside the new [first, last] window, the whole row falls back to the plain
// proportional placement, which preserves both by construction. Pure (no DOM). `rowCells` entries
// need `current` (moment) and `currentMs`; returns one new moment per cell, in the same order.
DTMN.computeRowFittedDates = function(rowCells, oldStartMs, oldSpan, anchors, snap) {
  const proportional = rowCells.map(cell =>
      DTMN.computeFittedDate(cell.currentMs, oldStartMs, oldSpan, anchors, false, cell.current));
  if (!snap) {
    return proportional;
  }

  const snapped = rowCells.map(cell =>
      DTMN.computeFittedDate(cell.currentMs, oldStartMs, oldSpan, anchors, true, cell.current));
  const firstMs = DTMN.wallClockMs(anchors.first);
  const lastMs = DTMN.wallClockMs(anchors.last);
  for (let i = 0; i < rowCells.length; i++) {
    const snappedMs = DTMN.wallClockMs(snapped[i]);
    if (snappedMs < firstMs || snappedMs > lastMs) {
      return proportional;
    }
    for (let j = 0; j < rowCells.length; j++) {
      if (rowCells[i].currentMs < rowCells[j].currentMs && snappedMs > DTMN.wallClockMs(snapped[j])) {
        return proportional;
      }
    }
  }
  return snapped;
};

DTMN.fitDates = function(anchors, sections, updates, notModified) {

  if (sections.length === 0) {
    return { changed: 0, tools: 0 };
  }

  // Pass 1: initialise every in-scope datepicker, then collect the editable, populated cells together
  // with their current moment value. All sections share one timeline so the span is computed globally.
  const cells = [];
  sections.forEach(function(section) {
    const rootElement = "#" + section.id;
    DTMN.attachDatePicker(rootElement + " .datepicker:not(.hasDatepicker)", updates, notModified);

    document.querySelectorAll(rootElement + " .datepicker.hasDatepicker").forEach(function(datepicker) {
      if (datepicker.disabled || !datepicker.value) {
        return;
      }
      const td = datepicker.closest("td");
      const hiddenField = td ? td.querySelector("input[type=hidden]") : null;
      if (!hiddenField) {
        return;
      }
      const useTime = hiddenField.dataset.tool !== "gradebookItems";
      const current = DTMN.parseDatePickerInputValue(datepicker.value, useTime);
      if (!current.isValid()) {
        return;
      }
      cells.push({ datepicker, useTime, current, currentMs: DTMN.wallClockMs(current), row: datepicker.closest("tr"), sectionId: section.id });
    });
  });

  if (cells.length === 0) {
    return { changed: 0, tools: 0 };
  }

  let oldStartMs = cells[0].currentMs;
  let oldEndMs = cells[0].currentMs;
  cells.forEach(function(cell) {
    if (cell.currentMs < oldStartMs) { oldStartMs = cell.currentMs; }
    if (cell.currentMs > oldEndMs) { oldEndMs = cell.currentMs; }
  });

  const oldSpan = oldEndMs - oldStartMs;
  const snap = DTMN.fitSnapCheckbox ? DTMN.fitSnapCheckbox.checked : false;

  // Pass 2: map each item's cells onto the new range, one row at a time. The earliest cell lands
  // exactly on the new first date and the latest exactly on the new last date; everything between
  // is placed proportionally, then snapped - unless snapping would corrupt the row (see
  // computeRowFittedDates), in which case that row keeps the proportional placement.
  const rows = new Map();
  cells.forEach(function(cell) {
    const key = cell.row || cell.datepicker;
    if (!rows.has(key)) {
      rows.set(key, []);
    }
    rows.get(key).push(cell);
  });
  rows.forEach(function(rowCells) {
    const newDates = DTMN.computeRowFittedDates(rowCells, oldStartMs, oldSpan, anchors, snap);
    rowCells.forEach(function(cell, i) {
      DTMN.setDatePickerValue(cell.datepicker, newDates[i], cell.useTime);
    });
  });

  const toolsTouched = new Set(cells.map(function(cell) { return cell.sectionId; }));
  return { changed: cells.length, tools: toolsTouched.size };
};

// Returns true when blank cells should also be filled. When false, only cells that already have a
// date are overwritten and empty cells are left untouched. Driven by the "bulk-fill-mode" radios;
// defaults to the conservative existing-only mode when no radio is selected, matching the
// template's checked option.
DTMN.shouldFillEmptyCells = function() {
  const selected = document.querySelector('input[name="bulk-fill-mode"]:checked');
  return selected != null && selected.value !== "existing";
};

// Fill every row of a single column (identified by data-field) within one section with the given date.
// Always overwrites existing values. Blank cells are filled only when `fillEmpty` is true; when the
// caller omits it, the term-date matrix's "bulk-fill-mode" radios decide (the per-column header
// setters pass their own "Fill Empty" checkbox state instead).
DTMN.fillColumn = function(rootElementId, field, date, updates, notModified, fillEmpty) {

  const rootElement = "#" + rootElementId;

  DTMN.attachDatePicker(rootElement + " .datepicker:not(.hasDatepicker)", updates, notModified);

  if (fillEmpty === undefined) {
    fillEmpty = DTMN.shouldFillEmptyCells();
  }
  const hiddenFields = document.querySelectorAll(rootElement + ' tbody input[type=hidden][data-field="' + field + '"]');
  let changed = 0;
  let blanksSkipped = 0;

  hiddenFields.forEach(function(hiddenField) {
    const td = hiddenField.closest('td');
    const datepicker = td ? td.querySelector('input.datepicker') : null;

    if (!datepicker || datepicker.disabled) {
      return;
    }

    // "Only update items that already have a date" mode: leave blanks alone.
    if (!fillEmpty && hiddenField.value === "") {
      blanksSkipped++;
      return;
    }

    const useTime = hiddenField.dataset.tool !== 'gradebookItems';
    DTMN.setDatePickerValue(datepicker, date, useTime);
    changed++;
  });

  return { changed: changed, blanksSkipped: blanksSkipped };
};

// ----- Per-column bulk setters (one small date input inside each editable column header) -----

DTMN.initColumnBulkSetters = function(updates, notModified) {

  const setters = Array.from(document.querySelectorAll(".bulk-col-setter"));
  if (setters.length === 0) {
    return;
  }

  setters.forEach(function(setter) {
    const input = setter.querySelector(".bulk-col-input");
    const hidden = setter.querySelector(".bulk-col-hidden");
    const button = setter.querySelector(".bulk-col-apply");

    if (!input || !hidden || !button) {
      return;
    }

    const useTime = input.dataset.tool !== 'gradebookItems';
    localDatePicker({
      input,
      useTime: useTime ? 1 : 0,
      parseFormat: useTime ? 'YYYY-MM-DDTHH:mm:ss' : 'YYYY-MM-DD',
      allowEmptyDate: true,
      ashidden: {
        iso8601: hidden.id,
      }
    });

    button.disabled = true;
    hidden.addEventListener("change", function() {
      button.disabled = hidden.value === "";
    }, false);

    button.addEventListener("click", function() {
      DTMN.applyColumnBulkDates(button, updates, notModified);
    }, false);
  });
};

DTMN.applyColumnBulkDates = function(button, updates, notModified) {

  const setter = button.closest(".bulk-col-setter");
  const hidden = setter ? setter.querySelector(".bulk-col-hidden") : null;
  const section = button.closest(".collapse");

  if (!hidden || hidden.value === "" || !section) {
    return;
  }

  const useTime = button.dataset.tool !== 'gradebookItems';
  const date = DTMN.parseInputDateValue(hidden.value, useTime);
  if (!date.isValid()) {
    return;
  }

  // this column's own "Fill Empty" checkbox decides whether blank cells get the date too
  const fillEmptyCheckbox = setter.querySelector(".bulk-col-fill-empty");
  const fillEmpty = fillEmptyCheckbox != null && fillEmptyCheckbox.checked;

  button.classList.add("spinButton");
  button.disabled = true;

  window.setTimeout(function() {
    DTMN.fillColumn(section.id, button.dataset.field, date, updates, notModified, fillEmpty);
    button.classList.remove("spinButton");
    button.disabled = hidden.value === "";
  }, 25);
};

// ----- Term dates panel (named term dates mapped onto columns via a checkbox matrix) -----

DTMN.getTermInputId = function(term) {
  return "term-input-" + term.replaceAll("_", "-");
};

DTMN.getTermHiddenId = function(term) {
  return "term-hidden-" + term.replaceAll("_", "-");
};

// Default term-date -> column mapping applied on load so a fresh Term Dates panel arrives pre-checked with
// sane targets: the term's earliest date (Classes Start) fills every open/start column, and the latest
// (Exam Ends) fills every due/close column. The instructor can tick or untick freely from there.
DTMN.defaultTermTargets = {
  classes_start: "open_date",
  exam_ends: "due_date"
};

DTMN.applyDefaultTermTargets = function() {
  Object.keys(DTMN.defaultTermTargets).forEach(function(term) {
    const field = DTMN.defaultTermTargets[term];
    document.querySelectorAll('.term-target[data-term="' + term + '"][data-field="' + field + '"]').forEach(function(check) {
      check.checked = true;
    });
  });
};

DTMN.initTermDates = function(updates, notModified) {

  DTMN.termApplyBtn = document.getElementById("dm-apply-term");

  if (!DTMN.termApplyBtn) {
    return;
  }

  DTMN.termFields.forEach(function(term) {
    const input = document.getElementById(DTMN.getTermInputId(term));
    const hidden = document.getElementById(DTMN.getTermHiddenId(term));

    if (!input || !hidden) {
      return;
    }

    hidden.addEventListener("change", () => DTMN.validateTermInputs(), false);

    localDatePicker({
      input,
      useTime: 1,
      parseFormat: 'YYYY-MM-DDTHH:mm:ss',
      allowEmptyDate: true,
      ashidden: {
        iso8601: hidden.id,
      }
    });
  });

  document.querySelectorAll(".term-target").forEach(function(check) {
    check.addEventListener("change", () => DTMN.validateTermInputs(), false);
  });

  DTMN.termApplyBtn.addEventListener("click", function() {
    DTMN.handleTermButtonClick(this, updates, notModified);
  }, false);

  DTMN.applyDefaultTermTargets();
  DTMN.validateTermInputs();
};

// A term date is actionable only when it has both a date AND at least one target column ticked.
DTMN.termHasActionableInput = function() {
  return DTMN.termFields.some(function(term) {
    const hidden = document.getElementById(DTMN.getTermHiddenId(term));
    if (!hidden || hidden.value === "") {
      return false;
    }
    return document.querySelector('.term-target[data-term="' + term + '"]:checked') !== null;
  });
};

DTMN.validateTermInputs = function() {
  if (!DTMN.termApplyBtn) {
    return;
  }

  DTMN.termApplyBtn.disabled = !DTMN.termHasActionableInput();
};

DTMN.handleTermButtonClick = function(button, updates, notModified) {

  if (!DTMN.termHasActionableInput()) {
    return;
  }

  button.classList.add("spinButton");
  DTMN.termApplyBtn.disabled = true;

  // The matrix checkboxes already say exactly which tools each term date fills, so the term
  // apply deliberately ignores the shift/fit scope row and acts on every section.
  window.setTimeout(function() {
    const stats = DTMN.applyTermDates(DTMN.collapseElements, updates, notModified);
    button.classList.remove("spinButton");
    DTMN.validateTermInputs();
    DTMN.showApplyFeedback("term", stats);
  }, 25);
};

DTMN.applyTermDates = function(sections, updates, notModified) {

  const sectionIds = new Set(sections.map(function(section) { return section.id; }));
  const toolsTouched = new Set();
  let changed = 0;
  let blanksSkipped = 0;

  // Process term dates top-to-bottom so that when two term dates target the same column,
  // the lower one in the panel wins (applied last).
  DTMN.termFields.forEach(function(term) {
    const hidden = document.getElementById(DTMN.getTermHiddenId(term));
    if (!hidden || hidden.value === "") {
      return;
    }

    const date = DTMN.parseInputDateValue(hidden.value, true);
    if (!date.isValid()) {
      return;
    }

    const checks = document.querySelectorAll('.term-target[data-term="' + term + '"]:checked');
    checks.forEach(function(check) {
      const root = check.dataset.root;
      const field = check.dataset.field;

      if (!sectionIds.has(root) || !document.getElementById(root)) {
        return;
      }

      const result = DTMN.fillColumn(root, field, date, updates, notModified);
      changed += result.changed;
      blanksSkipped += result.blanksSkipped;
      if (result.changed > 0) {
        toolsTouched.add(root);
      }
    });
  });

  return { changed: changed, tools: toolsTouched.size, blanksSkipped: blanksSkipped };
};

// These helpers deliberately do NO timezone conversion. Sakai treats picker values as wall-clock and
// resolves the timezone server-side: UserTimeService.parseISODateInUserTimezone() truncates the value
// to its first 19 chars (StringUtils.left(value, 19)), discards any browser offset, and interprets the
// wall-clock in the user's Sakai timezone. So the client just parses, formats, and does calendar math
// on the wall-clock fields and hands a bare wall-clock string back for the backend to interpret.
DTMN.getDatePickerInputValue = function(date, useTime)
{
  return useTime ? date.format("YYYY-MM-DDTHH:mm") : date.format("YYYY-MM-DD");
};

DTMN.getHiddenDateValue = function(date, useTime)
{
  return useTime ? date.format("YYYY-MM-DDTHH:mm:ss") : date.format("YYYY-MM-DD");
};

DTMN.parseDatePickerInputValue = function(value, useTime)
{
  const formats = useTime ? ["YYYY-MM-DDTHH:mm:ss", "YYYY-MM-DDTHH:mm", "YYYY-MM-DD"] : "YYYY-MM-DD";
  return moment(value, formats, true);
};

// Parse a value read from a localDatePicker "ashidden" iso8601 field. Those fields hold a full ISO8601
// string with the browser's offset (e.g. 2026-09-01T09:00:00+02:00). We strip the trailing offset to
// recover the wall-clock the user picked - the same thing the backend does with StringUtils.left(.,19) -
// and drop the time entirely for date-only fields, then parse the wall-clock with no timezone applied.
DTMN.parseInputDateValue = function(value, useTime)
{
  if (!value) {
    return moment.invalid();
  }
  let stripped = value.replace(/([+-]\d{2}:?\d{2}|Z)$/, "");
  if (!useTime) {
    stripped = stripped.split("T")[0];
  }
  return DTMN.parseDatePickerInputValue(stripped, useTime);
};

DTMN.hasTime = function(date)
{
  return date.hours() !== 0 || date.minutes() !== 0 || date.seconds() !== 0;
};

DTMN.setDatePickerValue = function(datepicker, date, useTime)
{
  datepicker.value = DTMN.getDatePickerInputValue(date, useTime);

  const td = datepicker.closest("td");
  const hiddenField = td ? td.querySelector("input[type=hidden]") : null;
  if (hiddenField) {
    hiddenField.value = DTMN.getHiddenDateValue(date, useTime);
    hiddenField.dispatchEvent(new Event("change", {bubbles: true}));
  } else {
    datepicker.dispatchEvent(new Event("change", {bubbles: true}));
  }

  datepicker.classList.add("border-warning");
};

DTMN.attachDatePicker = function (selector, updates, notModified) {

  $(selector).each(function (idx, elt) {

    DTMN.nextIndex = DTMN.nextIndex + 1;
    const $td = $(elt).closest('td');
    const $hidden = $td.find('input[type=hidden]');

    var dataTool = $hidden.data('tool');
    var dataField = $hidden.data('field');
    var dataIdx = $hidden.data('idx');
    var $clearBtn = $(elt).siblings('a');

    if (dataTool === 'assessments' || dataTool === 'gradebookItems' || dataTool === 'resources' || dataTool === 'forums' || dataTool === 'lessons'
       || dataTool === 'announcements' || dataTool === 'assignments' || dataTool === 'signupMeetings' || dataTool === 'calendarEvents') {
       $clearBtn.addClass('ui-datepicker-clear-date');
       $clearBtn.show();
    } else {
      $clearBtn.hide();
    }

    $td.attr('id', 'cell_' + dataTool + '_' + dataField + '_' + dataIdx);

    $clearBtn.on('click', function() {
    if ($(this).nextAll('input').attr('data-null-date') === 'false') {
      // clear date on datepicker
      $(this).parent().children('.form-control.datepicker.hasDatepicker').val('');
      // clear date on hidden element
      $(this).nextAll('input').val('');
      // force event for hidden element so that clear btn will follow same update logic as backspace/delete in datapicker
      $(this).nextAll('input').trigger('change');
    }
  });

    $hidden.on('change', function () {
      const idx = $(this).data('idx');
      const field = $(this).data('field');
      const tool = $(this).data('tool');
      const fieldVal = $(this).val();
      const dataDateWasNull = $(this).attr('data-null-date');
      const elemDateTime = $(this).siblings('input.datepicker').val();

      updates[tool][idx][field] = $(this).val().split('+')[0];
      updates[tool][idx][field + '_label'] = $(this).siblings('input.datepicker').val();

      // set title for date input field
      $(this).parent().find('.datepicker').attr('title', elemDateTime);

      // Show day of the week in case there is a date selected
      if ($(this).parent().find('.datepicker').val() !== '') {
        updates[tool][idx][field + '_day_of_week'] = moment(updates[tool][idx][field]).locale(sakai.locale.userLocale).format('dddd');
        $(this).parent().find('.day-of-week').text(updates[tool][idx][field + '_day_of_week']);
      // Clear day of the week if date has been cleared
      } else {
          $(this).parent().find('.day-of-week').text('');
      }

      if (notModified.includes(tool + idx + field) || dataDateWasNull === 'true') {
        updates[tool][idx].idx = idx;
        updates[tool + 'Upd'][idx] = updates[tool][idx];
        if (dataDateWasNull === 'true' && fieldVal !== '') {
          $(this).attr('data-null-date', false);
        } else if (dataDateWasNull === 'false' && fieldVal === '') {
                 $(this).attr('data-null-date', true);
        }
        $('#submit-form-button').prop('disabled', false);

        // Add visual indication that the field has been changed
        $(this).siblings('input.datepicker').addClass('border-warning');
      }
      notModified.push(tool + idx + field);
    });

    $hidden.attr('id', 'hidden_datepicker_' + DTMN.nextIndex);
    var dateFormat = 'YYYY-MM-DDTHH:mm:ss';
    var toolTime = 1;
    if(dataTool === 'gradebookItems') {
      dateFormat = 'YYYY-MM-DD';
      toolTime = 0;
    }
    var datepickerOpts = {
      input: elt,
      useTime: toolTime,
      parseFormat: dateFormat,
      allowEmptyDate: false,
      ashidden: {
        iso8601: 'hidden_datepicker_' + DTMN.nextIndex,
      }
    };
    // Allow null dates during editing then enforce rules for required fields serverside
    if (dataTool === 'assessments' || dataTool === 'gradebookItems' || dataTool === 'resources' || dataTool === 'forums' || dataTool === 'lessons'
       || dataTool === 'announcements' || dataTool === 'assignments' || dataTool === 'signupMeetings' || dataTool === 'calendarEvents') {
      datepickerOpts.allowEmptyDate = true;
    }

    // If it's already null, lets not force a date
    if ($hidden.val() === '') {
      datepickerOpts.allowEmptyDate = true;
    }

    if ($hidden.val() !== '') datepickerOpts.val = $hidden.val();
    localDatePicker(datepickerOpts);

    //reposition the clear button between the datepicker and datepicker trigger button
    $(elt).after($clearBtn);

    // Disable accept_until date input if no late submissions (assessments) allowed
    if (dataTool === 'assessments' && dataField === 'accept_until') {
      var disabled = !updates[dataTool][dataIdx].late_handling;
      $(elt).prop('disabled', disabled);
      $td.find('.ui-datepicker-trigger').prop('disabled', disabled);
      $td.find('.ui-datepicker-clear-date > i').attr('disabled',disabled);
    }
    // Disable feedback start and end date inputs if feedback on date not used (assessments)
    if (dataTool === 'assessments' && (dataField === 'feedback_start' || dataField === 'feedback_end')) {
      var disabled = !updates[dataTool][dataIdx].feedback_by_date;
      $(elt).prop('disabled', disabled);
      $td.find('.ui-datepicker-trigger').prop('disabled', disabled);
      $td.find('.ui-datepicker-clear-date > i').attr('disabled',disabled);
    }
    if (dataTool === 'forums' && (dataField === 'open_date' || dataField === 'due_date')) {
      var disabled = !updates[dataTool][dataIdx].restricted;
      $(elt).prop('disabled', disabled);
      $td.find('.ui-datepicker-trigger').prop('disabled', disabled);
      $td.find('.ui-datepicker-clear-date > i').attr('disabled',disabled);
    }
  });
};

DTMN.handleShiftButtonClick = function(button, collapseElements, updates, notModified)
{
  if (collapseElements.length === 0)
  {
    return;
  }

  DTMN.disableShiftControls(button);
  window.setTimeout(function()
  {
    let changed = 0;
    let tools = 0;
    for (let i = 0; i < collapseElements.length; i++)
    {
      // use setTimeout() to space out the function calls so the browser doesn't report the page as unresponsive
      // the last function will remove the spinner, re-enable the button, and report the totals
      window.setTimeout(function() {
        const sectionChanged = DTMN.shiftDates(updates, notModified, collapseElements[i].id, button, i === collapseElements.length - 1) || 0;
        changed += sectionChanged;
        if (sectionChanged > 0) { tools++; }
        if (i === collapseElements.length - 1) {
          DTMN.showApplyFeedback("shift", { changed: changed, tools: tools });
        }
      }, 10);
    }
  }, 25);
};

DTMN.validateShiftInput = function()
{
  if (!DTMN.shiftInput || !DTMN.shiftApplyBtn)
  {
    return;
  }

  const val = DTMN.shiftInput.value;
  if (val === "" || val === "-")
  {
    DTMN.hideShiftError();
    DTMN.disableShiftButtons();
    return;
  }
  else if (!val.match(DTMN.validShiftRegex))
  {
    DTMN.showShiftError();
    DTMN.disableShiftButtons();
    return;
  }

  const days = parseInt(val);

  DTMN.hideShiftError();

  DTMN.shiftApplyBtn.disabled = days === 0 || DTMN.getScopedSections().length === 0;
};

DTMN.hideShiftError = function()
{
  DTMN.shiftErrorBanner.classList.add("d-none");
  DTMN.shiftErrorBanner.removeAttribute("role");
};

DTMN.showShiftError = function()
{
  DTMN.shiftErrorBanner.classList.remove("d-none");
  DTMN.shiftErrorBanner.setAttribute("role", "alert");
};

DTMN.disableShiftControls = function(button)
{
  DTMN.shiftInput.disabled = true;
  DTMN.disableShiftButtons();
  button.classList.add("spinButton");
};

DTMN.disableShiftButtons = function()
{
  DTMN.shiftApplyBtn.disabled = true;
};

DTMN.enableShiftControls = function(button)
{
  button.classList.remove("spinButton");
  DTMN.validateShiftInput();
  DTMN.shiftInput.disabled = false;
};

DTMN.clearChangedDateIndication = function() {
  $('.datepicker').removeClass('border-warning');
};

DTMN.shiftDates = function (updates, notModified, rootElementId, button, enableButton) {

  // validate input again just in case
  if (!DTMN.shiftInput.value.match(DTMN.validShiftRegex)) {
    DTMN.showShiftError();
    DTMN.disableShiftButtons();
    return 0;
  }

  const days = parseInt(DTMN.shiftInput.value, 10);
  const rootElement = "#" + rootElementId;

  DTMN.attachDatePicker(rootElement + " .datepicker:not(.hasDatepicker)", updates, notModified);

  const datepickers = document.querySelectorAll(rootElement + " .datepicker.hasDatepicker");
  let changed = 0;

  datepickers.forEach(function (datepicker) {
    const dateValue = datepicker.value;

    if (!dateValue) {
      return;
    }

    // Find the associated hidden field using modern DOM traversal
    const td = datepicker.closest('td');
    const hiddenField = td ? td.querySelector('input[type=hidden]') : null;

    if (!hiddenField) {
      console.warn('No hidden field found for datepicker', datepicker);
      return;
    }

    const dataTool = hiddenField.dataset.tool;

    // Determine the correct date format based on the tool type
    const useTime = dataTool !== 'gradebookItems';

    try {
      // Parse the date string and add days
      const currentDate = DTMN.parseDatePickerInputValue(dateValue, useTime);

      if (!currentDate.isValid()) {
        console.warn('Invalid date format:', dateValue);
        return;
      }

      const newDate = currentDate.clone().add(days, 'days');

      DTMN.setDatePickerValue(datepicker, newDate, useTime);
      changed++;

    } catch (error) {
      console.error('Error processing date:', dateValue, error);
    }
  });

  if (enableButton)
  {
    DTMN.enableShiftControls(button);
  }

  return changed;
};

// ---------------------------------------------------------------------------
// Calendar preview: a read-only month grid that plots every date currently
// entered in the tables (one marker per date field) so the user can visually
// check the outcome of a shift/fit/anchor before choosing Save Changes.
// ---------------------------------------------------------------------------

// Accent colour per tool, so markers are visually grouped by tool type.
DTMN.previewToolColors = {
  assignments:    "#0d6efd",
  assessments:    "#6f42c1",
  gradebookItems: "#198754",
  signupMeetings: "#fd7e14",
  resources:      "#20c997",
  calendarEvents: "#0dcaf0",
  forums:         "#d63384",
  announcements:  "#dc3545",
  lessons:        "#6c757d"
};

// FullCalendar defaults every event to white text, which fails WCAG AA on the lighter
// accents above (cyan, teal, orange). Pick black or white per fill by relative
// luminance — whichever contrasts more; the crossover is where the two ratios are equal.
DTMN.previewEventTextColor = function(hex) {
  const n = parseInt(hex.slice(1), 16);
  const toLinear = function(c) {
    const s = c / 255;
    return s <= 0.03928 ? s / 12.92 : Math.pow((s + 0.055) / 1.055, 2.4);
  };
  const luminance = 0.2126 * toLinear((n >> 16) & 255)
    + 0.7152 * toLinear((n >> 8) & 255)
    + 0.0722 * toLinear(n & 255);
  return luminance > 0.1791 ? '#000000' : '#ffffff';
};

// Read the live hidden-input values and build one FullCalendar event per non-empty
// date. Events are all-day and keyed on the calendar day so they land squarely in the
// day cell regardless of time zone; the time (when present) is shown in the marker text.
DTMN.collectPreviewEvents = function() {
  const events = [];
  // tbody-scoped: the column-header "set whole column" boxes carry the same data attributes
  // (.bulk-col-hidden in thead) but hold candidate values that are not saved dates
  document.querySelectorAll('tbody input[type=hidden][data-tool][data-field]').forEach(function(hidden) {
    const value = hidden.value;
    if (!value) {
      return;
    }
    const tool = hidden.getAttribute('data-tool');
    const field = hidden.getAttribute('data-field');
    const useTime = tool !== 'gradebookItems';
    const parsed = DTMN.parseInputDateValue(value, useTime);
    if (!parsed || !parsed.isValid()) {
      return;
    }

    const row = hidden.closest('tr');
    const titleEl = row ? row.querySelector('td a span') : null;
    const itemTitle = titleEl ? titleEl.textContent.trim() : '';
    const fieldLabel = (DTMN.fieldLabels && DTMN.fieldLabels[field]) || field;
    const timeSuffix = (useTime && DTMN.hasTime(parsed)) ? ' ' + parsed.locale(sakai.locale.userLocale).format('LT') : '';

    const fill = DTMN.previewToolColors[tool] || '#6c757d';
    events.push({
      title: (itemTitle ? itemTitle + ' — ' : '') + fieldLabel + timeSuffix,
      start: parsed.format('YYYY-MM-DD'),
      allDay: true,
      color: fill,
      textColor: DTMN.previewEventTextColor(fill)
    });
  });
  return events;
};

// Whole-month grid range covering every event, so the default "Overview" view shows the full
// span from the first date to the last in one continuous scrollable grid. Very long spans are
// clamped to a year past the first date to keep the grid a sane size.
DTMN.computePreviewRange = function(events) {
  let earliest = null;
  let latest = null;
  events.forEach(function(ev) {
    if (earliest === null || ev.start < earliest) earliest = ev.start;
    if (latest === null || ev.start > latest) latest = ev.start;
  });
  if (earliest === null) {
    return null;
  }
  const start = moment(earliest, 'YYYY-MM-DD').startOf('month');
  let end = moment(latest, 'YYYY-MM-DD').endOf('month').add(1, 'day').startOf('day');
  const cap = start.clone().add(12, 'months');
  if (end.isAfter(cap)) {
    end = cap;
  }
  return {
    earliest,
    start: start.format('YYYY-MM-DD'),
    end: end.format('YYYY-MM-DD'),
  };
};

DTMN.renderCalendarPreview = function() {
  const events = DTMN.collectPreviewEvents();
  const emptyMsg = document.getElementById('datemanager-calendar-empty');
  const calendarEl = document.getElementById('datemanager-calendar');
  if (!calendarEl) {
    return;
  }

  if (!events.length) {
    emptyMsg && emptyMsg.classList.remove('d-none');
    calendarEl.classList.add('d-none');
    return;
  }
  emptyMsg && emptyMsg.classList.add('d-none');
  calendarEl.classList.remove('d-none');

  const locale = (sakai && sakai.locale && sakai.locale.userLanguage) || 'en';
  const range = DTMN.computePreviewRange(events);
  // Held on DTMN so the custom view's visibleRange callback always sees the current span,
  // including after events are replaced on a later open of the modal.
  DTMN.previewRange = { start: range.start, end: range.end };

  if (!DTMN.previewCalendar) {
    DTMN.previewCalendar = new FullCalendar.Calendar(calendarEl, {
      // The "Overview" view: a duration-less dayGrid driven by visibleRange, one continuous
      // grid from the month of the first date through the month of the last.
      initialView: 'dmSpan',
      views: {
        dmSpan: {
          type: 'dayGrid',
          buttonText: DTMN.previewSpanButtonText || 'Overview',
        }
      },
      visibleRange: function() {
        return DTMN.previewRange;
      },
      themeSystem: 'bootstrap5',
      locale: locale,
      displayEventTime: false,
      height: 'auto',
      headerToolbar: {
        left: 'prev,next today',
        center: 'title',
        right: 'dmSpan,dayGridMonth,dayGridWeek'
      },
      buttonIcons: {
        prev: 'chevron-left',
        next: 'chevron-right'
      },
      events: events
    });
    DTMN.previewCalendar.render();
  } else {
    DTMN.previewCalendar.removeAllEvents();
    DTMN.previewCalendar.addEventSource(events);
    DTMN.previewCalendar.updateSize();
  }

  // Anchor on the earliest date: the overview starts there, and switching to the Month or
  // Week views lands on the first populated month/week rather than today.
  DTMN.previewCalendar.gotoDate(range.earliest);
};

DTMN.initCalendarPreview = function() {
  const button = document.getElementById('datemanager-preview');
  const modalEl = document.getElementById('modal-calendar-preview');
  if (!button || !modalEl) {
    return;
  }

  button.addEventListener('click', function() {
    bootstrap.Modal.getOrCreateInstance(modalEl).show();
  });

  // Render only once the modal is visible so FullCalendar can measure the container.
  modalEl.addEventListener('shown.bs.modal', function() {
    DTMN.renderCalendarPreview();
  });
};
