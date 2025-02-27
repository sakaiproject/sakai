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
        return new Date(value); // Create a new instance to avoid mutation issues
      }
      
      // Our custom date object format
      if (typeof value === 'object' && 'year' in value) {
        // Use Date constructor with specific components
        return new Date(
          value.year, 
          (value.month - 1), // JS months are 0-indexed
          value.day,
          value.hours || 0,
          value.minutes || 0,
          value.seconds || 0,
          0 // milliseconds
        );
      }
      
      // String parsing - try built-in parsing first
      if (typeof value === 'string') {
        // Try native parsing first
        const nativeDate = new Date(value);
        if (!isNaN(nativeDate.getTime())) {
          return nativeDate;
        }
        
        // Handle YYYY-MM-DD HH:mm[:ss] format
        if (value.includes(' ')) {
          const [datePart, timePart] = value.split(' ');
          const [year, month, day] = datePart.split('-').map(Number);
          const [hours, minutes] = timePart.split(':').map(Number);
          
          if (!isNaN(year) && !isNaN(month) && !isNaN(day) && !isNaN(hours) && !isNaN(minutes)) {
            return new Date(year, month - 1, day, hours, minutes);
          }
        }
        
        // Handle ISO format (YYYY-MM-DDTHH:mm)
        if (value.includes('T')) {
          // Try ISO format parsing with the browser
          const nativeDate = new Date(value);
          if (!isNaN(nativeDate.getTime())) {
            return nativeDate;
          }
          
          // Manual parsing as fallback
          const [datePart, timePart] = value.split('T');
          const [year, month, day] = datePart.split('-').map(Number);
          const timeComponents = timePart.split(':').map(Number);
          
          if (!isNaN(year) && !isNaN(month) && !isNaN(day)) {
            return new Date(
              year, 
              month - 1, 
              day, 
              timeComponents[0] || 0, 
              timeComponents[1] || 0
            );
          }
        }
      }
      
      // Invalid date
      return null;
    }
    
    // Format a date for the input field using Intl.DateTimeFormat where possible
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
      
      // Use toISOString and then replace the Z with the timezone offset
      const isoBase = date.toISOString().slice(0, 19); // YYYY-MM-DDTHH:mm:ss
      const offset = -date.getTimezoneOffset();
      const offsetSign = offset >= 0 ? '+' : '-';
      const offsetHours = DateHelper.pad(Math.floor(Math.abs(offset) / 60));
      const offsetMinutes = DateHelper.pad(Math.abs(offset) % 60);
      
      return `${isoBase}${offsetSign}${offsetHours}:${offsetMinutes}`;
    }
    
    // Get a formatted value for a field type from a date
    static getFieldValue(date, fieldType, useTime = true) {
      if (!date) return "";
      
      date = DateHelper.normalize(date);
      if (!date) return "";
      
      switch (fieldType) {
        case "month":
          return DateHelper.pad(date.getMonth() + 1);
        case "day":
          return DateHelper.pad(date.getDate());
        case "year":
          return date.getFullYear();
        case "hour":
          return useTime ? date.getHours() : "";
        case "minute":
          return useTime ? date.getMinutes() : "";
        case "ampm":
          return useTime ? (date.getHours() < 12 ? "am" : "pm") : "";
        case "iso8601":
          return DateHelper.formatForSakai(date);
        default:
          return "";
      }
    }
  }
  
  class SakaiDateTimePicker {
	constructor(options) {
	  this.options = { ...defaults, ...options };
	  this.options.useTime = Boolean(this.options.useTime === 1 || this.options.useTime === true);
	  this.element = document.querySelector(this.options.input);
	  
	  if (!this.element) {
		console.error("Input element not found:", this.options.input);
		return;
	  }
	  
	  this.init();
	}
  
	init() {
	  this.element.style.minWidth = "200px";
	  
	  // Process initial date value
	  const initialDate = this.getInitialDate();

	  // Set input value
	  this.element.value = initialDate 
	    ? DateHelper.formatForInput(initialDate, this.options.useTime) 
	    : '';

	  // Set input type after setting value
	  this.element.type = this.options.useTime ? "datetime-local" : "date";

	  // Complete setup
	  this.createHiddenFields();
	  this.syncHiddenFields(initialDate);
	  this.setupEventListeners();
	  this.setupDuration();
	}
    
    // Determine the initial date based on options and input value
    getInitialDate() {
      // Try options.val first
      if (this.options.val) {
        const date = DateHelper.normalize(this.options.val);
        if (date) return date;
      }
      
      // Try input value next
      const inputValue = this.element.value.trim();
      if (inputValue) {
        const date = DateHelper.normalize(inputValue);
        if (date) return date;
      }
      
      // Fall back to current date if empty dates not allowed
      return this.options.allowEmptyDate ? null : this.getPreferredSakaiDatetime();
    }
  
	createHiddenFields() {
	  const hiddenFields = this.options.ashidden;
	  if (!hiddenFields) return;
  
	  Object.entries(hiddenFields).forEach(([key, id]) => {
	    if (!document.getElementById(id)) {
	      const hiddenInput = document.createElement("input");
	      hiddenInput.type = "hidden";
	      hiddenInput.name = id;
	      hiddenInput.id = id;
	      this.element.insertAdjacentElement("afterend", hiddenInput);
	    }
	  });
	}
  
	setupEventListeners() {
	  // Handle input change
	  this.element.addEventListener("change", () => {
	    const date = DateHelper.normalize(this.element.value);
	    this.syncHiddenFields(date);
	    
	    // Trigger callback if provided and we have a valid date
	    this.options.onDateTimeSelected?.call(this, date?.getTime());
	    
	    // Update duration if configured
	    if (this.options.duration) this.updateDuration();
	  });
  
	  // Handle empty date when input is cleared
	  this.element.addEventListener("blur", () => {
	    if (this.options.allowEmptyDate && !this.element.value) {
	      this.syncHiddenFields(null);
	    }
	  });
	}
  
	setupDuration() {
	  const { duration } = this.options;
	  if (!duration) return;
  
	  // Get duration-related elements
	  const elements = {
	    hour: document.querySelector(`#${duration.hour}`),
	    minute: document.querySelector(`#${duration.minute}`),
	    update: document.querySelector(`#${duration.update}`)
	  };
	  
	  // Validate elements exist
	  if (!elements.hour || !elements.minute || !elements.update) {
	    console.warn("Duration fields not found:", duration);
	    return;
	  }
	  
	  // Initial update
	  this.updateDuration();
	  
	  // Set up change listeners
	  [elements.hour, elements.minute].forEach(el => {
	    el.addEventListener("change", () => this.updateDuration());
	  });
	}
  
	updateDuration() {
	  const { duration } = this.options;
	  if (!duration) return;
  
	  // Get base date and duration elements
	  const date = DateHelper.normalize(this.element.value);
	  if (!date) return;
	  
	  const hourField = document.querySelector(`#${duration.hour}`);
	  const minuteField = document.querySelector(`#${duration.minute}`);
	  const updateField = document.querySelector(`#${duration.update}`);
	  
	  if (!hourField || !minuteField || !updateField) return;
	  
	  // Calculate end date
	  const hours = parseInt(hourField.value) || 0;
	  const minutes = parseInt(minuteField.value) || 0;
	  const durationMs = (hours * 60 + minutes) * 60 * 1000;
	  const endDate = new Date(date.getTime() + durationMs);
	  
	  // Display end date using Intl.DateTimeFormat
	  const formatter = new Intl.DateTimeFormat(undefined, { 
	    dateStyle: 'short', 
	    timeStyle: this.options.useTime ? 'short' : undefined 
	  });
	  
	  updateField.textContent = formatter.format(endDate);
	}
  
	syncHiddenFields(date) {
	  const hiddenFields = this.options.ashidden;
	  if (!hiddenFields) return;
  
	  Object.entries(hiddenFields).forEach(([key, id]) => {
	    const hiddenInput = document.getElementById(id);
	    if (!hiddenInput) return;
	    
	    const oldValue = hiddenInput.value;
	    const newValue = date ? DateHelper.getFieldValue(date, key, this.options.useTime) : "";
	    
	    if (oldValue !== newValue) {
	      hiddenInput.value = newValue;
	      hiddenInput.dispatchEvent(new Event("change"));
	    }
	  });
	}
  
	getPreferredSakaiDatetime() {
	  // Use optional chaining for nested property access
	  const timestamp = window.portal?.serverTimeMillis || window.parent?.portal?.serverTimeMillis;
	  
	  if (timestamp) {
	    return new Date(parseInt(timestamp));
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
