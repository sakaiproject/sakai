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

DTMN.initShifter = function(updates, notModified) {

  DTMN.validShiftRegex = /^-{0,1}\d{1,4}$/;

  DTMN.shiftErrorBanner = document.getElementById("dateShifterError");
  DTMN.shiftInput = document.getElementById("dateShifterDays");
  DTMN.shiftAllBtn = document.getElementById("shiftAllDates");
  DTMN.shiftVisibleBtn = document.getElementById("shiftVisibleDates");

  // Add event listener to the submit button to clear visual indication
  $('#modal-btn-confirm').on('click', function() {
    DTMN.clearChangedDateIndication();
  });

  DTMN.shiftInput.addEventListener("input", () => DTMN.validateShiftInput(), false);

  DTMN.shiftAllBtn.addEventListener("click", function() {
    DTMN.handleShiftButtonClick(this, DTMN.collapseElements, updates, notModified);
  }, false);

  DTMN.shiftVisibleBtn.addEventListener("click", function() {
    DTMN.handleShiftButtonClick(this, DTMN.findExpandedSections(), updates, notModified);
  }, false);
};

// ----- Date difference helper: whole days between two dates, loaded into the shift field -----

// Pure: signed whole days from `from` to `to` (to - from), counting calendar days, ignoring time of day.
DTMN.computeDayDiff = function(from, to) {
  return to.clone().startOf("day").diff(from.clone().startOf("day"), "days");
};

DTMN.initDateDiff = function() {

  DTMN.diffFromInput = document.getElementById("date-diff-from");
  DTMN.diffFromHidden = document.getElementById("date-diff-from-hidden");
  DTMN.diffToInput = document.getElementById("date-diff-to");
  DTMN.diffToHidden = document.getElementById("date-diff-to-hidden");
  DTMN.diffApplyBtn = document.getElementById("date-diff-apply");
  DTMN.diffResult = document.getElementById("date-diff-result");

  if (!DTMN.diffApplyBtn || !DTMN.diffFromHidden || !DTMN.diffToHidden) {
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
    hidden.addEventListener("change", () => DTMN.validateDateDiff(), false);
  });

  DTMN.diffApplyBtn.addEventListener("click", () => DTMN.applyDateDiff(), false);

  DTMN.validateDateDiff();
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

// Show the count live as soon as both dates are valid, and enable the "use as shift" button.
DTMN.validateDateDiff = function() {
  if (!DTMN.diffApplyBtn) {
    return;
  }

  const days = DTMN.getDateDiffDays();
  DTMN.diffApplyBtn.disabled = days === null;

  if (DTMN.diffResult) {
    if (days === null) {
      DTMN.diffResult.textContent = "";
    } else {
      const template = DTMN.diffResult.dataset.template || "{0}";
      DTMN.diffResult.textContent = template.replace("{0}", days);
    }
  }
};

DTMN.applyDateDiff = function() {
  const days = DTMN.getDateDiffDays();
  if (days === null) {
    return;
  }

  // Load the day count into the shift field and let the shifter's own validation enable its buttons.
  const shiftInput = document.getElementById("dateShifterDays");
  if (shiftInput) {
    shiftInput.value = days;
    DTMN.validateShiftInput();
  }
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
  DTMN.fitAllBtn = document.getElementById("fit-all-dates");
  DTMN.fitVisibleBtn = document.getElementById("fit-visible-dates");

  if (!DTMN.fitAllBtn || !DTMN.fitVisibleBtn || !DTMN.fitFirstHidden || !DTMN.fitLastHidden) {
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

  DTMN.fitAllBtn.addEventListener("click", function() {
    DTMN.handleFitButtonClick(this, false, updates, notModified);
  }, false);

  DTMN.fitVisibleBtn.addEventListener("click", function() {
    DTMN.handleFitButtonClick(this, true, updates, notModified);
  }, false);

  DTMN.validateFitInputs();
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
  if (!DTMN.fitAllBtn || !DTMN.fitVisibleBtn) {
    return;
  }

  const anchors = DTMN.getFitAnchors();
  const rangeOk = anchors !== null && anchors.last.valueOf() > anchors.first.valueOf();

  // Surface the ordering error only once both dates are present but out of order.
  if (anchors !== null && !rangeOk) {
    DTMN.showFitError();
  } else {
    DTMN.hideFitError();
  }

  DTMN.fitAllBtn.disabled = !rangeOk;
  DTMN.fitVisibleBtn.disabled = !rangeOk || DTMN.findExpandedSections().length === 0;
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

DTMN.handleFitButtonClick = function(button, restrictToExpanded, updates, notModified) {

  const anchors = DTMN.getFitAnchors();
  if (!anchors || anchors.last.valueOf() <= anchors.first.valueOf()) {
    return;
  }

  button.classList.add("spinButton");
  DTMN.fitAllBtn.disabled = true;
  DTMN.fitVisibleBtn.disabled = true;

  window.setTimeout(function() {
    DTMN.fitDates(anchors, restrictToExpanded, updates, notModified);
    button.classList.remove("spinButton");
    DTMN.validateFitInputs();
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

// Map one source instant onto the new [first, last] range. Pure (no DOM): the earliest source
// (frac <= 0) lands exactly on anchors.first, the latest (frac >= 1) exactly on anchors.last, and a
// middle source is placed proportionally then, when snap is on, snapped to its own weekday/time.
// `sourceMoment` is only used by the snap branch. A zero old span (all dates identical) collapses
// every date onto anchors.first.
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

  const newStartMs = anchors.first.valueOf();
  const newSpan = anchors.last.valueOf() - newStartMs;
  const target = moment(newStartMs + frac * newSpan);
  return snap ? DTMN.snapToSourceWeekday(target, sourceMoment) : target;
};

DTMN.fitDates = function(anchors, restrictToExpanded, updates, notModified) {

  const sections = restrictToExpanded ? DTMN.findExpandedSections() : DTMN.collapseElements;
  if (sections.length === 0) {
    return;
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
      cells.push({ datepicker, useTime, current, currentMs: current.valueOf() });
    });
  });

  if (cells.length === 0) {
    return;
  }

  let oldStartMs = cells[0].currentMs;
  let oldEndMs = cells[0].currentMs;
  cells.forEach(function(cell) {
    if (cell.currentMs < oldStartMs) { oldStartMs = cell.currentMs; }
    if (cell.currentMs > oldEndMs) { oldEndMs = cell.currentMs; }
  });

  const oldSpan = oldEndMs - oldStartMs;
  const snap = DTMN.fitSnapCheckbox ? DTMN.fitSnapCheckbox.checked : false;

  // Pass 2: map each cell onto the new range. The earliest cell lands exactly on the new first date and
  // the latest exactly on the new last date; everything between is placed proportionally, then snapped.
  cells.forEach(function(cell) {
    const newDate = DTMN.computeFittedDate(cell.currentMs, oldStartMs, oldSpan, anchors, snap, cell.current);
    DTMN.setDatePickerValue(cell.datepicker, newDate, cell.useTime);
  });
};

// Returns true when blank cells should also be filled. When false, only cells that already have a
// date are overwritten and empty cells are left untouched. Driven by the "bulk-fill-mode" radios.
DTMN.shouldFillEmptyCells = function() {
  const selected = document.querySelector('input[name="bulk-fill-mode"]:checked');
  return !selected || selected.value !== "existing";
};

// Fill every row of a single column (identified by data-field) within one section with the given date.
// Always overwrites existing values; honors the global fill mode for blank cells.
DTMN.fillColumn = function(rootElementId, field, date, updates, notModified) {

  const rootElement = "#" + rootElementId;

  DTMN.attachDatePicker(rootElement + " .datepicker:not(.hasDatepicker)", updates, notModified);

  const fillEmpty = DTMN.shouldFillEmptyCells();
  const hiddenFields = document.querySelectorAll(rootElement + ' tbody input[type=hidden][data-field="' + field + '"]');

  hiddenFields.forEach(function(hiddenField) {
    const td = hiddenField.closest('td');
    const datepicker = td ? td.querySelector('input.datepicker') : null;

    if (!datepicker || datepicker.disabled) {
      return;
    }

    // "Only cells that already have a date" mode: leave blanks alone.
    if (!fillEmpty && hiddenField.value === "") {
      return;
    }

    const useTime = hiddenField.dataset.tool !== 'gradebookItems';
    DTMN.setDatePickerValue(datepicker, date, useTime);
  });
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

  button.classList.add("spinButton");
  button.disabled = true;

  window.setTimeout(function() {
    DTMN.fillColumn(section.id, button.dataset.field, date, updates, notModified);
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

// Default term-date -> column mapping applied on load so a fresh Bulk Anchor arrives pre-checked with
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

  DTMN.termAllBtn = document.getElementById("apply-term-dates-all");
  DTMN.termVisibleBtn = document.getElementById("apply-term-dates-visible");

  if (!DTMN.termAllBtn || !DTMN.termVisibleBtn) {
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

  DTMN.termAllBtn.addEventListener("click", function() {
    DTMN.handleTermButtonClick(this, false, updates, notModified);
  }, false);

  DTMN.termVisibleBtn.addEventListener("click", function() {
    DTMN.handleTermButtonClick(this, true, updates, notModified);
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

// Like termHasActionableInput, but only counts a checked target whose section is currently expanded.
// The "apply to expanded sections only" path skips collapsed sections, so without this the button could
// be enabled while every checked column lives in a collapsed section, making the click a no-op.
DTMN.termHasActionableInputInExpandedSections = function() {
  return DTMN.termFields.some(function(term) {
    const hidden = document.getElementById(DTMN.getTermHiddenId(term));
    if (!hidden || hidden.value === "") {
      return false;
    }
    const checks = document.querySelectorAll('.term-target[data-term="' + term + '"]:checked');
    return Array.prototype.some.call(checks, function(check) {
      const section = document.getElementById(check.dataset.root);
      return section !== null && section.classList.contains("show");
    });
  });
};

DTMN.validateTermInputs = function() {
  if (!DTMN.termAllBtn || !DTMN.termVisibleBtn) {
    return;
  }

  DTMN.termAllBtn.disabled = !DTMN.termHasActionableInput();
  DTMN.termVisibleBtn.disabled = !DTMN.termHasActionableInputInExpandedSections();
};

DTMN.handleTermButtonClick = function(button, restrictToExpanded, updates, notModified) {

  if (!DTMN.termHasActionableInput()) {
    return;
  }

  button.classList.add("spinButton");
  DTMN.termAllBtn.disabled = true;
  DTMN.termVisibleBtn.disabled = true;

  window.setTimeout(function() {
    DTMN.applyTermDates(restrictToExpanded, updates, notModified);
    button.classList.remove("spinButton");
    DTMN.validateTermInputs();
  }, 25);
};

DTMN.applyTermDates = function(restrictToExpanded, updates, notModified) {

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
      const sectionEl = document.getElementById(root);

      if (!sectionEl) {
        return;
      }
      if (restrictToExpanded && !sectionEl.classList.contains("show")) {
        return;
      }

      DTMN.fillColumn(root, field, date, updates, notModified);
    });
  });
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
  DTMN.disableShiftControls(button);
  window.setTimeout(function()
  {
    for (let i = 0; i < collapseElements.length; i++)
    {
      // use setTimeout() to space out the function calls so the browser doesn't report the page as unresponsive
      // the last function will remove the spinner and re-enable the button
      window.setTimeout(function() { DTMN.shiftDates(updates, notModified, collapseElements[i].id, button, i === collapseElements.length - 1); }, 10);
    }
  }, 25);
};

DTMN.validateShiftInput = function()
{
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

  DTMN.shiftAllBtn.disabled = days === 0;
  DTMN.shiftVisibleBtn.disabled = days === 0 || DTMN.findExpandedSections().length === 0;
};

DTMN.findExpandedSections = function()
{
  return DTMN.collapseElements.filter(function (e) { return e.classList.contains("show") === true; });
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
  DTMN.shiftAllBtn.disabled = true;
  DTMN.shiftVisibleBtn.disabled = true;
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
    return;
  }

  const days = parseInt(DTMN.shiftInput.value, 10);
  const rootElement = "#" + rootElementId;

  DTMN.attachDatePicker(rootElement + " .datepicker:not(.hasDatepicker)", updates, notModified);

  const datepickers = document.querySelectorAll(rootElement + " .datepicker.hasDatepicker");

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

    } catch (error) {
      console.error('Error processing date:', dateValue, error);
    }
  });

  if (enableButton)
  {
    DTMN.enableShiftControls(button);
  }
};
