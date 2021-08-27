var DTMN = DTMN || {};

DTMN.toolList = [ "assignments", "assessments", "signup", "gradebook", "resources", "calendar", "forums", "announcements", "lessons" ];
DTMN.collapseElements = [ ];
DTMN.nextIndex = -1;

DTMN.initDatePicker = function(updates, notModified)
{
	// use an event listener to populate the date pickers on demand instead of populating everything on page load
	for (let i = 0; i < DTMN.toolList.length; i++)
	{
		const collapseId = "#collapse-" + DTMN.toolList[i];
		const collapseElement = document.querySelector(collapseId);
		if (collapseElement !== null)
		{
			DTMN.collapseElements.push(collapseElement);
		}
		const link = document.querySelector("a[href='" + collapseId + "']");
		if (link !== null)
		{
			const selector = collapseId + " .datepicker:not(.hasDatepicker)";
			const target = document.querySelector(collapseId);
			$(target).on("show.bs.collapse", function()
			{
				const spinner = link.querySelector(".allocatedSpinPlaceholder");
				spinner.classList.add("spinPlaceholder");
				window.setTimeout(function()
				{
					DTMN.attachDatePicker(selector, updates, notModified);
					spinner.classList.remove("spinPlaceholder");
				}, 25); // delay 25ms to give browser time to render the spinner
		    });
		}
	}
};

DTMN.attachDatePicker = function(selector, updates, notModified)
{
	$(selector).each(function (idx, elt) {
		DTMN.nextIndex = DTMN.nextIndex + 1;
		var $td = $(elt).closest('td');
		var $hidden = $td.find('input[type=hidden]');

		var dataTool = $hidden.data('tool');
		var dataField = $hidden.data('field');
		var dataIdx = $hidden.data('idx');
		$td.attr('id', 'cell_' + dataTool + '_' + dataField + '_' + dataIdx);

		$hidden.on('change', function () {
			var idx = $(this).data('idx');
			var field = $(this).data('field');
			var tool = $(this).data('tool');

			updates[tool][idx][field] = $(this).val().split('+')[0];
			updates[tool][idx][field + '_label'] = $(this).siblings('input.datepicker').val();
			// Show day of the week in case there is a date selected
			if ($(this).parent().find('.datepicker').val() !== '') {
				updates[tool][idx][field + '_day_of_week'] = moment(updates[tool][idx][field]).locale(sakai.locale.userLocale).format('dddd');
				$(this).parent().find('.day-of-week').text(updates[tool][idx][field + '_day_of_week']);
			}

			if (notModified.includes(tool + idx + field)) {
				updates[tool][idx].idx = idx;
				updates[tool + 'Upd'][idx] = updates[tool][idx];
				$('#submit-form-button').prop('disabled', false);
			}
			notModified.push(tool + idx + field);
		});

		$hidden.attr('id', 'hidden_datepicker_' + DTMN.nextIndex);
		var dateFormat = 'YYYY-MM-DD HH:mm:ss';
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
		if ($hidden.val() !== '') datepickerOpts.val = $hidden.val();
		localDatePicker(datepickerOpts);

		// Disable accept_until date input if no late submissions (assessments) allowed
		if (dataTool === 'assessments' && dataField === 'accept_until') {
			var disabled = !updates[dataTool][dataIdx].late_handling;
			$(elt).prop('disabled', disabled);
			$td.find('.ui-datepicker-trigger').prop('disabled', disabled);
		}
		// Disable feedback start and end date inputs if feedback on date not used (assessments)
		if (dataTool === 'assessments' && (dataField === 'feedback_start' || dataField === 'feedback_end')) {
			var disabled = !updates[dataTool][dataIdx].feedback_by_date;
			$(elt).prop('disabled', disabled);
			$td.find('.ui-datepicker-trigger').prop('disabled', disabled);
		}
		if (dataTool === 'forums' && (dataField === 'open_date' || dataField === 'due_date')) {
			var disabled = !updates[dataTool][dataIdx].restricted;
			$(elt).prop('disabled', disabled);
			$td.find('.ui-datepicker-trigger').prop('disabled', disabled);
		}
	});
};
