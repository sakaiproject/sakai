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
	  this.element.type = this.options.useTime ? "datetime-local" : "date";
	  this.element.style.minWidth = "200px";
	  this.setInitialValue();
	  this.createHiddenFields();
	  // Sync hidden fields with the initial value, preserving exact val if provided
	  this.syncHiddenFields(this.options.val ? this.parseRawValue(this.options.val) : this.parseDate(this.element.value));
	  this.setupEventListeners();
	  this.handleDuration();
	}
  
	setInitialValue() {
		let initialValue = this.options.val || this.element.value.trim();
	  
		if (!initialValue && !this.options.allowEmptyDate) {
		  initialValue = this.getPreferredSakaiDatetime();
		  const date = this.parseDate(initialValue);
		  this.element.value = this.formatForInput(date);
		} else if (initialValue) {
		  // Handle Date object input
		  if (initialValue instanceof Date) {
			this.element.value = this.formatForInput(initialValue);
		  }
		  // Handle string input
		  else {
			// Use raw value for datetime-local input, no Date object manipulation
			if (this.options.parseFormat === 'YYYY-MM-DD HH:mm:ss' && initialValue.includes(' ')) {
			  const [datePart, timePart] = initialValue.split(' ');
			  const [year, month, day] = datePart.split('-');
			  const [hours, minutes] = timePart.split(':');
			  this.element.value = `${year}-${month}-${day}T${hours}:${minutes}`;
			} else {
			  this.element.value = initialValue; // Assume it's already in datetime-local format
			}
		  }
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
  
	  if (this.options.parseFormat === 'YYYY-MM-DD HH:mm:ss' && value.includes(' ')) {
		const [datePart, timePart] = value.split(' ');
		const [year, month, day] = datePart.split('-').map(Number);
		const [hours, minutes, seconds] = timePart.split(':').map(Number);
		return { year, month, day, hours, minutes, seconds };
	  }
  
	  // Fallback to Date parsing if not in expected format
	  return this.parseDate(value);
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
		const offset = -d.getTimezoneOffset();
		return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}${offset >= 0 ? '+' : '-'}${pad(Math.abs(offset / 60))}:${pad(offset % 60)}`;
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
				newValue = date.month;
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
				newValue = date.getMonth() + 1;
				break;
			  case "day":
				newValue = date.getDate();
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