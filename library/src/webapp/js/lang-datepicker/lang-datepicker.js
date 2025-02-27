	// Default options
const defaults = {
	input: null,
	useTime: true,
	parseFormat: null,
	allowEmptyDate: false,
	val: "",
	ashidden: {},
	duration: null,
	onDateTimeSelected: null,
  };
  
  class SakaiDateTimePicker {
	constructor(options) {
	  this.options = { ...defaults, ...options };
	  this.options.useTime = this.options.useTime === 1 || this.options.useTime === true;
	  this.element = document.querySelector(this.options.input);
	  if (!this.element) {
		console.error("Input element not found:", this.options.input);
		return;
	  }
	  this.initTime = Date.now();
	  this.init();
	}
  
	init() {
	  this.element.style.minWidth = "200px";
	  
	  // First try to parse options.val if it exists
	  let initialDate = null;
	  if (this.options.val) {
		initialDate = this.parseRawValue(this.options.val);
	  }
	  // If no valid date from options.val, try the input element's value
	  if (!initialDate && this.element.value.trim()) {
		initialDate = this.parseDate(this.element.value.trim());
	  }
	  // If still no valid date and we don't allow empty, use current date
	  if (!initialDate && !this.options.allowEmptyDate) {
		initialDate = this.getPreferredSakaiDatetime();
	  }

	  // First set a valid value
	  if (initialDate) {
		this.element.value = this.formatForInput(initialDate);
	  } else {
		// Clear the value if we allow empty dates
		this.element.value = '';
	  }

	  // Only NOW change the input type, after we have a valid value or empty string
	  this.element.type = this.options.useTime ? "datetime-local" : "date";

	  this.createHiddenFields();
	  // Use the same initialDate for hidden fields to maintain consistency
	  this.syncHiddenFields(initialDate);
	  this.setupEventListeners();
	  this.handleDuration();
	}
  
	setInitialValue() {
		// If we have an explicit value in options, use that
		if (this.options.val) {
			const parsedValue = this.parseRawValue(this.options.val);
			if (parsedValue) {
				this.element.value = this.formatForInput(parsedValue);
				return;
			}
		}

		// If input has a value, try to use that
		const inputValue = this.element.value.trim();
		if (inputValue) {
			const parsedValue = this.parseDate(inputValue);
			if (parsedValue) {
				this.element.value = this.formatForInput(parsedValue);
				return;
			}
		}

		// If we get here and don't allow empty dates, use current date
		if (!this.options.allowEmptyDate) {
			const currentDate = this.getPreferredSakaiDatetime();
			this.element.value = this.formatForInput(currentDate);
		}
	}
  
	createHiddenFields() {
	  const hiddenFields = this.options.ashidden;
	  if (!hiddenFields) return;
  
	  for (const [key, id] of Object.entries(hiddenFields)) {
		if (!document.getElementById(id)) {
		  const hiddenInput = document.createElement("input");
		  hiddenInput.type = "hidden";
		  hiddenInput.name = id;
		  hiddenInput.id = id;
		  this.element.insertAdjacentElement("afterend", hiddenInput);
		}
	  }
	}
  
	setupEventListeners() {
	  this.element.addEventListener("change", () => {
		const date = this.parseDate(this.element.value);
		this.syncHiddenFields(date);
		if (this.options.onDateTimeSelected && date) {
		  this.options.onDateTimeSelected(date.getTime());
		}
	  });
  
	  this.element.addEventListener("blur", () => {
		if (this.options.allowEmptyDate && !this.element.value) {
		  this.syncHiddenFields(null);
		}
	  });
  
	  if (this.options.duration) {
		this.element.addEventListener("change", () => this.updateDuration());
	  }
	}
  
	handleDuration() {
	  const duration = this.options.duration;
	  if (!duration) return;
  
	  const hourField = document.querySelector(`#${duration.hour}`);
	  const minuteField = document.querySelector(`#${duration.minute}`);
	  const updateField = document.querySelector(`#${duration.update}`);
  
	  if (!hourField || !minuteField || !updateField) {
		console.warn("Duration fields not found:", duration);
		return;
	  }
  
	  this.updateDuration();
	  [hourField, minuteField, this.element].forEach((el) => {
		el.addEventListener("change", () => this.updateDuration());
	  });
	}
  
	updateDuration() {
	  const duration = this.options.duration;
	  if (!duration) return;
  
	  const date = this.parseDate(this.element.value);
	  if (!date) return;
  
	  const hours = parseInt(document.querySelector(`#${duration.hour}`).value) || 0;
	  const minutes = parseInt(document.querySelector(`#${duration.minute}`).value) || 0;
  
	  const endDate = new Date(date.getTime() + hours * 60 * 60 * 1000 + minutes * 60 * 1000);
	  const endString = this.options.useTime
		? endDate.toLocaleString() // Display only, no storage impact
		: endDate.toLocaleDateString();
  
	  document.querySelector(`#${duration.update}`).textContent = endString;
	}
  
	// Parse raw value without Date object to avoid timezone shifts
	parseRawValue(value) {
	  if (!value) return null;

	  // If it's already a Date object, return it
	  if (value instanceof Date) {
		return value;
	  }

	  // Handle string input
	  if (typeof value === 'string') {
		// Handle YYYY-MM-DD HH:mm[:ss] format
		if (value.includes(' ')) {
		  const [datePart, timePart] = value.split(' ');
		  const [year, month, day] = datePart.split('-').map(n => parseInt(n, 10));
		  const [hours, minutes] = timePart.split(':').map(n => parseInt(n, 10));
		  
		  // Validate all parts are actual numbers
		  if (!isNaN(year) && !isNaN(month) && !isNaN(day) && !isNaN(hours) && !isNaN(minutes)) {
			return {
			  year: year,
			  month: month,
			  day: day,
			  hours: hours,
			  minutes: minutes,
			  seconds: 0
			};
		  }
		}
		
		// Handle ISO format (YYYY-MM-DDTHH:mm)
		if (value.includes('T')) {
		  const [datePart, timePart] = value.split('T');
		  const [year, month, day] = datePart.split('-').map(n => parseInt(n, 10));
		  const [hours, minutes] = timePart.split(':').map(n => parseInt(n, 10));
		  
		  if (!isNaN(year) && !isNaN(month) && !isNaN(day) && !isNaN(hours) && !isNaN(minutes)) {
			return {
			  year: year,
			  month: month,
			  day: day,
			  hours: hours,
			  minutes: minutes,
			  seconds: 0
			};
		  }
		}
	  }

	  // If all else fails, try to create a valid Date object
	  const date = new Date(value);
	  if (!isNaN(date.getTime())) {
		return date;
	  }

	  // If we get here, we couldn't parse the date, return current date as fallback
	  console.warn('Could not parse date value:', value, 'using current date as fallback');
	  return this.getPreferredSakaiDatetime();
	}
  
	// Parse datetime-local input values or fallback default
	parseDate(value) {
	  if (!value) return this.options.allowEmptyDate ? null : this.getPreferredSakaiDatetime();
	  const date = new Date(value);
	  return isNaN(date.getTime()) ? this.getPreferredSakaiDatetime() : date;
	}
  
	formatForInput(date) {
	  if (!date) return "";
	  const pad = (num) => String(num).padStart(2, "0");
	  // If date is a raw object from parseRawValue, use its components
	  if (typeof date === 'object' && 'year' in date) {
		return `${date.year}-${pad(date.month)}-${pad(date.day)}T${pad(date.hours)}:${pad(date.minutes)}`;
	  }
	  // Otherwise, use Date object (for user-entered values)
	  const year = date.getFullYear();
	  const month = pad(date.getMonth() + 1);
	  const day = pad(date.getDate());
	  if (!this.options.useTime) return `${year}-${month}-${day}`;
	  const hours = pad(date.getHours());
	  const minutes = pad(date.getMinutes());
	  return `${year}-${month}-${day}T${hours}:${minutes}`;
	}

	formatForSakai(d) {
		const pad = n => n.toString().padStart(2, '0');
		
		// Handle our custom date object
		if (typeof d === 'object' && 'year' in d) {
			// For custom objects, we'll add the local timezone offset for debugging convenience
			const now = new Date();
			const offset = -now.getTimezoneOffset();
			return `${d.year}-${pad(d.month)}-${pad(d.day)}T${pad(d.hours)}:${pad(d.minutes)}:${pad(d.seconds || 0)}${offset >= 0 ? '+' : '-'}${pad(Math.abs(offset / 60))}:${pad(Math.abs(offset % 60))}`;
		}
		
		// Handle regular Date object
		if (d instanceof Date) {
			const offset = -d.getTimezoneOffset();
			return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}${offset >= 0 ? '+' : '-'}${pad(Math.abs(offset / 60))}:${pad(Math.abs(offset % 60))}`;
		}

		// If we somehow get here with an invalid input, return current time
		console.warn('Invalid date passed to formatForSakai:', d);
		const now = new Date();
		const offset = -now.getTimezoneOffset();
		return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}${offset >= 0 ? '+' : '-'}${pad(Math.abs(offset / 60))}:${pad(Math.abs(offset % 60))}`;
	}
  
	syncHiddenFields(date) {
	  const hiddenFields = this.options.ashidden;
	  if (!hiddenFields) return;
  
	  for (const [key, id] of Object.entries(hiddenFields)) {
		const hiddenInput = document.getElementById(id);
		if (!hiddenInput) continue;
  
		const oldValue = hiddenInput.value;
		let newValue = "";
  
		if (date) {
		  const pad = (num) => String(num).padStart(2, "0");
		  // Handle raw value object from initial val
		  if (typeof date === 'object' && 'year' in date) {
			switch (key) {
			  case "month":
				newValue = String(date.month).padStart(2, '0');
				break;
			  case "day":
				newValue = date.day;
				break;
			  case "year":
				newValue = date.year;
				break;
			  case "hour":
				newValue = this.options.useTime ? date.hours : "";
				break;
			  case "minute":
				newValue = this.options.useTime ? date.minutes : "";
				break;
			  case "ampm":
				newValue = this.options.useTime ? (date.hours < 12 ? "am" : "pm") : "";
				break;
			  case "iso8601":
				newValue = this.formatForSakai(date);
				break;
			}
		  } else {
			// Handle Date object from user input
			switch (key) {
			  case "month":
				newValue = String(date.getMonth() + 1).padStart(2, '0');
				break;
			  case "day":
				newValue = String(date.getDate()).padStart(2, '0');
				break;
			  case "year":
				newValue = date.getFullYear();
				break;
			  case "hour":
				newValue = this.options.useTime ? date.getHours() : "";
				break;
			  case "minute":
				newValue = this.options.useTime ? date.getMinutes() : "";
				break;
			  case "ampm":
				newValue = this.options.useTime ? (date.getHours() < 12 ? "am" : "pm") : "";
				break;
			  case "iso8601":
				newValue = this.formatForSakai(date);
				break;
			}
		  }
		}
  
		hiddenInput.value = newValue;
		if (oldValue !== newValue) {
		  hiddenInput.dispatchEvent(new Event("change"));
		}
	  }
	}
  
	getPreferredSakaiDatetime() {
	  const p = window.portal || (window.parent && window.parent.portal);
	  if (p && p.serverTimeMillis) {
		return new Date(parseInt(p.serverTimeMillis));
	  }
	  console.debug("No Sakai server time available. Using local time without adjustment.");
	  return new Date();
	}
  }
  
  function localDatePicker(options) {
	return new SakaiDateTimePicker(options);
  }
  
  window.SDP = window.SDP || {};
  window.SDP.initSakaiDatePicker = function(inputField, value, useTime, allowEmptyDate) {
	return new SakaiDateTimePicker({
	  input: `#${inputField}`,
	  useTime,
	  allowEmptyDate,
	  val: value,
	  ashidden: { iso8601: `${inputField}ISO8601` },
	});
  };
