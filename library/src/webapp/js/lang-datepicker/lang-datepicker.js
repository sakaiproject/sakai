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
  
  // DateHelper utility to standardize date handling
  class DateHelper {
    // Pad a number with leading zeros
    static pad(num) {
      return String(num).padStart(2, "0");
    }
    
    // Convert any date representation to a standardized object
    static normalize(value) {
      if (!value) return null;
      
      // Already a Date object
      if (value instanceof Date) {
        return value;
      }
      
      // Our custom date object format
      if (typeof value === 'object' && 'year' in value) {
        // Convert to a proper Date object
        const d = new Date();
        d.setFullYear(value.year);
        d.setMonth(value.month - 1); // JS months are 0-indexed
        d.setDate(value.day);
        d.setHours(value.hours || 0);
        d.setMinutes(value.minutes || 0);
        d.setSeconds(value.seconds || 0);
        d.setMilliseconds(0);
        return d;
      }
      
      // String parsing
      if (typeof value === 'string') {
        // Handle YYYY-MM-DD HH:mm[:ss] format
        if (value.includes(' ')) {
          const [datePart, timePart] = value.split(' ');
          const [year, month, day] = datePart.split('-').map(n => parseInt(n, 10));
          const [hours, minutes] = timePart.split(':').map(n => parseInt(n, 10));
          
          if (!isNaN(year) && !isNaN(month) && !isNaN(day) && !isNaN(hours) && !isNaN(minutes)) {
            const d = new Date();
            d.setFullYear(year);
            d.setMonth(month - 1); // JS months are 0-indexed
            d.setDate(day);
            d.setHours(hours);
            d.setMinutes(minutes);
            d.setSeconds(0);
            d.setMilliseconds(0);
            return d;
          }
        }
        
        // Handle ISO format (YYYY-MM-DDTHH:mm)
        if (value.includes('T')) {
          const [datePart, timePart] = value.split('T');
          const [year, month, day] = datePart.split('-').map(n => parseInt(n, 10));
          const timeComponents = timePart.split(':').map(n => parseInt(n, 10));
          const hours = timeComponents[0] || 0;
          const minutes = timeComponents[1] || 0;
          
          if (!isNaN(year) && !isNaN(month) && !isNaN(day) && !isNaN(hours) && !isNaN(minutes)) {
            const d = new Date();
            d.setFullYear(year);
            d.setMonth(month - 1); // JS months are 0-indexed
            d.setDate(day);
            d.setHours(hours);
            d.setMinutes(minutes);
            d.setSeconds(0);
            d.setMilliseconds(0);
            return d;
          }
        }
      }
      
      // Try standard Date parsing
      const date = new Date(value);
      if (!isNaN(date.getTime())) {
        return date;
      }
      
      // Invalid date
      return null;
    }
    
    // Format a date for the input field
    static formatForInput(date, useTime = true) {
      if (!date) return "";
      
      date = DateHelper.normalize(date);
      if (!date) return "";
      
      const year = date.getFullYear();
      const month = DateHelper.pad(date.getMonth() + 1);
      const day = DateHelper.pad(date.getDate());
      
      if (!useTime) return `${year}-${month}-${day}`;
      
      const hours = DateHelper.pad(date.getHours());
      const minutes = DateHelper.pad(date.getMinutes());
      return `${year}-${month}-${day}T${hours}:${minutes}`;
    }
    
    // Format a date for Sakai (ISO8601 with timezone)
    static formatForSakai(date) {
      if (!date) return "";
      
      date = DateHelper.normalize(date);
      if (!date) return "";
      
      const offset = -date.getTimezoneOffset();
      const offsetSign = offset >= 0 ? '+' : '-';
      const offsetHours = DateHelper.pad(Math.floor(Math.abs(offset) / 60));
      const offsetMinutes = DateHelper.pad(Math.abs(offset) % 60);
      
      return `${date.getFullYear()}-${DateHelper.pad(date.getMonth() + 1)}-${DateHelper.pad(date.getDate())}T${DateHelper.pad(date.getHours())}:${DateHelper.pad(date.getMinutes())}:${DateHelper.pad(date.getSeconds())}${offsetSign}${offsetHours}:${offsetMinutes}`;
    }
  }
  
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
		initialDate = DateHelper.normalize(this.options.val);
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
		this.element.value = DateHelper.formatForInput(initialDate, this.options.useTime);
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
			const parsedValue = DateHelper.normalize(this.options.val);
			if (parsedValue) {
				this.element.value = DateHelper.formatForInput(parsedValue, this.options.useTime);
				return;
			}
		}

		// If input has a value, try to use that
		const inputValue = this.element.value.trim();
		if (inputValue) {
			const parsedValue = this.parseDate(inputValue);
			if (parsedValue) {
				this.element.value = DateHelper.formatForInput(parsedValue, this.options.useTime);
				return;
			}
		}

		// If we get here and don't allow empty dates, use current date
		if (!this.options.allowEmptyDate) {
			const currentDate = this.getPreferredSakaiDatetime();
			this.element.value = DateHelper.formatForInput(currentDate, this.options.useTime);
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
  
	// Parse raw value without Date object to avoid timezone shifts - now uses DateHelper
	parseRawValue(value) {
	  if (!value) return null;
      
      const date = DateHelper.normalize(value);
      if (date) return date;
      
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
	  return DateHelper.formatForInput(date, this.options.useTime);
	}

	formatForSakai(date) {
	  return DateHelper.formatForSakai(date);
	}
  
	syncHiddenFields(date) {
	  const hiddenFields = this.options.ashidden;
	  if (!hiddenFields) return;
  
	  // Normalize the date to ensure we're always working with a Date object
	  const normalizedDate = date ? DateHelper.normalize(date) : null;
  
	  for (const [key, id] of Object.entries(hiddenFields)) {
		const hiddenInput = document.getElementById(id);
		if (!hiddenInput) continue;
  
		const oldValue = hiddenInput.value;
		let newValue = "";
  
		if (normalizedDate) {
		  switch (key) {
			case "month":
			  newValue = String(normalizedDate.getMonth() + 1).padStart(2, '0');
			  break;
			case "day":
			  newValue = String(normalizedDate.getDate()).padStart(2, '0');
			  break;
			case "year":
			  newValue = normalizedDate.getFullYear();
			  break;
			case "hour":
			  newValue = this.options.useTime ? normalizedDate.getHours() : "";
			  break;
			case "minute":
			  newValue = this.options.useTime ? normalizedDate.getMinutes() : "";
			  break;
			case "ampm":
			  newValue = this.options.useTime ? (normalizedDate.getHours() < 12 ? "am" : "pm") : "";
			  break;
			case "iso8601":
			  newValue = DateHelper.formatForSakai(normalizedDate);
			  break;
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
