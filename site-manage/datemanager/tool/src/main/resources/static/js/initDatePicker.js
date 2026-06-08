var DTMN = DTMN || {};

DTMN.toolList = [ "assignments", "assessments", "signup", "gradebook", "resources", "calendar", "forums", "announcements", "lessons" ];
DTMN.collapseElements = [ ];
DTMN.bulkFields = DTMN.bulkFields || [];
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
      if (DTMN.validateBulkInputs) {
        DTMN.validateBulkInputs();
      }
    });
    collapseElement.addEventListener("hidden.bs.collapse", () => {
      DTMN.validateShiftInput();
      if (DTMN.validateBulkInputs) {
        DTMN.validateBulkInputs();
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

DTMN.initBulkSetter = function(updates, notModified) {

  DTMN.bulkErrorBanner = document.getElementById("dateBulkSetterError");
  DTMN.bulkAllBtn = document.getElementById("applyAllDates");
  DTMN.bulkVisibleBtn = document.getElementById("applyVisibleDates");
  DTMN.bulkInputs = Array.from(document.querySelectorAll(".bulk-date-input"));

  if (!DTMN.bulkAllBtn || !DTMN.bulkVisibleBtn) {
    return;
  }

  if (DTMN.bulkFields.length === 0) {
    DTMN.bulkFields = Array.from(document.querySelectorAll(".date-manager-setter [data-field]"))
      .map(function(el) { return el.getAttribute("data-field"); });
  }

  DTMN.initBulkDatePickers();

  DTMN.bulkAllBtn.addEventListener("click", function() {
    DTMN.handleBulkButtonClick(this, DTMN.collapseElements, updates, notModified);
  }, false);

  DTMN.bulkVisibleBtn.addEventListener("click", function() {
    DTMN.handleBulkButtonClick(this, DTMN.findExpandedSections(), updates, notModified);
  }, false);

  DTMN.validateBulkInputs();
};

DTMN.initBulkDatePickers = function() {
  DTMN.bulkFields.forEach(function(field) {
    const input = document.getElementById(DTMN.getBulkInputId(field));
    const hidden = document.getElementById(DTMN.getBulkHiddenId(field));

    if (!input || !hidden) {
      return;
    }

    hidden.addEventListener("change", () => DTMN.validateBulkInputs(), false);

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
};

DTMN.getBulkInputId = function(field)
{
  return "bulk-" + field.replaceAll("_", "-");
};

DTMN.getBulkHiddenId = function(field)
{
  return "bulk-hidden-" + field.replaceAll("_", "-");
};

DTMN.getUserTimeZone = function()
{
  const userTimeZone = globalThis.sakai?.locale?.userTimeZone;
  if (!userTimeZone && DTMN.warnDatePickerTimeZoneFallback) {
    DTMN.warnDatePickerTimeZoneFallback("getUserTimeZone");
  }
  return userTimeZone || null;
};

DTMN.warnDatePickerTimeZoneFallback = function(functionName, value, useTime)
{
  const warningFlag = "_" + functionName + "TimeZoneFallbackWarned";
  if (DTMN[warningFlag]) {
    return;
  }

  DTMN[warningFlag] = true;
  console.warn(functionName + " falling back to browser timezone because moment-timezone or sakai.locale.userTimeZone is unavailable.", {value, useTime});
};

DTMN.getDatePickerInputValue = function(date, useTime)
{
  const userTimeZone = DTMN.getUserTimeZone();
  let userDate = date;
  if (moment.tz && userTimeZone) {
    userDate = date.clone().tz(userTimeZone);
  } else {
    DTMN.warnDatePickerTimeZoneFallback("getDatePickerInputValue", date && date.format ? date.format() : date, useTime);
  }
  return useTime ? userDate.format("YYYY-MM-DDTHH:mm") : userDate.format("YYYY-MM-DD");
};

DTMN.getHiddenDateValue = function(date, useTime)
{
  const userTimeZone = DTMN.getUserTimeZone();
  let userDate = date;
  if (moment.tz && userTimeZone) {
    userDate = date.clone().tz(userTimeZone);
  } else {
    DTMN.warnDatePickerTimeZoneFallback("getHiddenDateValue", date && date.format ? date.format() : date, useTime);
  }
  return useTime ? userDate.format("YYYY-MM-DDTHH:mm:ss") : userDate.format("YYYY-MM-DD");
};

DTMN.parseDatePickerInputValue = function(value, useTime)
{
  const formats = useTime ? ["YYYY-MM-DDTHH:mm:ss", "YYYY-MM-DDTHH:mm", "YYYY-MM-DD"] : "YYYY-MM-DD";
  const userTimeZone = DTMN.getUserTimeZone();
  if (moment.tz && userTimeZone) {
    return moment.tz(value, formats, true, userTimeZone);
  }

  DTMN.warnDatePickerTimeZoneFallback("parseDatePickerInputValue", value, useTime);
  return moment(value, formats, true);
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

DTMN.handleBulkButtonClick = function(button, collapseElements, updates, notModified)
{
  const hasValue = DTMN.bulkFields.some(function(field) {
    const hidden = document.getElementById(DTMN.getBulkHiddenId(field));
    return hidden && hidden.value !== "";
  });

  if (!hasValue) {
    DTMN.showBulkError();
    DTMN.validateBulkInputs();
    return;
  }

  DTMN.hideBulkError();
  DTMN.disableBulkControls(button);
  window.setTimeout(function()
  {
    if (collapseElements.length === 0)
    {
      DTMN.enableBulkControls(button);
      return;
    }

    for (let i = 0; i < collapseElements.length; i++)
    {
      window.setTimeout(function() { DTMN.applyBulkDates(updates, notModified, collapseElements[i].id, button, i === collapseElements.length - 1); }, 10);
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

DTMN.validateBulkInputs = function()
{
  if (!DTMN.bulkAllBtn || !DTMN.bulkVisibleBtn) {
    return;
  }

  const hasValue = DTMN.bulkFields.some(function(field) {
    const hidden = document.getElementById(DTMN.getBulkHiddenId(field));
    return hidden && hidden.value !== "";
  });

  if (hasValue) {
    DTMN.hideBulkError();
  }

  DTMN.bulkAllBtn.disabled = !hasValue;
  DTMN.bulkVisibleBtn.disabled = !hasValue || DTMN.findExpandedSections().length === 0;
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

DTMN.hideBulkError = function()
{
  DTMN.activeBulkError = null;
  DTMN.bulkErrorBanner.classList.add("d-none");
  DTMN.bulkErrorBanner.removeAttribute("role");
};

DTMN.showBulkError = function(errorType)
{
  errorType = errorType || "empty";
  DTMN.activeBulkError = errorType;
  DTMN.bulkErrorBanner.querySelectorAll("[data-error]").forEach(function(error) {
    error.classList.toggle("d-none", error.dataset.error !== errorType);
  });
  DTMN.bulkErrorBanner.classList.remove("d-none");
  DTMN.bulkErrorBanner.setAttribute("role", "alert");
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

DTMN.disableBulkControls = function(button)
{
  DTMN.bulkInputs.forEach(function(input) {
    input.disabled = true;
  });
  DTMN.disableBulkButtons();
  button.classList.add("spinButton");
};

DTMN.disableBulkButtons = function()
{
  DTMN.bulkAllBtn.disabled = true;
  DTMN.bulkVisibleBtn.disabled = true;
};

DTMN.enableBulkButtons = function()
{
  DTMN.bulkAllBtn.disabled = false;
  DTMN.bulkVisibleBtn.disabled = DTMN.findExpandedSections().length === 0;
};

DTMN.enableShiftControls = function(button)
{
  button.classList.remove("spinButton");
  DTMN.validateShiftInput();
  DTMN.shiftInput.disabled = false;
};

DTMN.enableBulkControls = function(button)
{
  button.classList.remove("spinButton");
  DTMN.bulkInputs.forEach(function(input) {
    input.disabled = false;
  });
  DTMN.validateBulkInputs();
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

DTMN.applyBulkDates = function (updates, notModified, rootElementId, button, enableButton) {

  const rootElement = "#" + rootElementId;

  DTMN.attachDatePicker(rootElement + " .datepicker:not(.hasDatepicker)", updates, notModified);

  DTMN.bulkFields.forEach(function(field) {
    const bulkInput = document.getElementById(DTMN.getBulkInputId(field));
    const bulkHidden = document.getElementById(DTMN.getBulkHiddenId(field));

    if (!bulkInput || !bulkHidden || bulkHidden.value === "") {
      return;
    }

    const bulkDate = DTMN.parseDatePickerInputValue(bulkInput.value, true);

    if (!bulkDate.isValid()) {
      return;
    }

    const hiddenFields = document.querySelectorAll(rootElement + ' input[type=hidden][data-field="' + field + '"]');
    hiddenFields.forEach(function(hiddenField) {
      const td = hiddenField.closest('td');
      const datepicker = td ? td.querySelector('input.datepicker') : null;

      if (!datepicker || datepicker.disabled) {
        return;
      }

      const dataTool = hiddenField.dataset.tool;
      const useTime = dataTool !== 'gradebookItems';

      DTMN.setDatePickerValue(datepicker, bulkDate, useTime);
    });
  });

  if (enableButton)
  {
    DTMN.enableBulkControls(button);
  }
};
