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
    // IMPORTANT: We preserve local time exactly as entered, no timezone manipulation
    static normalize(value) {
      if (!value) return null;
      
      // Already a Date object - preserve its local time components
      if (value instanceof Date) {
        const d = new Date();
        d.setFullYear(value.getFullYear());
        d.setMonth(value.getMonth());
        d.setDate(value.getDate());
        d.setHours(value.getHours());
        d.setMinutes(value.getMinutes());
        d.setSeconds(value.getSeconds());
        d.setMilliseconds(0);
        return d;
      }
      
      // Our custom date object format - use components directly
      if (typeof value === 'object' && 'year' in value) {
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
      
      // String parsing - try built-in parsing first
      if (typeof value === 'string') {
        // Handle YYYY-MM-DD HH:mm[:ss] format
        if (value.includes(' ')) {
          const [datePart, timePart] = value.split(' ');
          const [year, month, day] = datePart.split('-').map(Number);
          const [hours, minutes] = timePart.split(':').map(Number);
          
          if (!isNaN(year) && !isNaN(month) && !isNaN(day) && !isNaN(hours) && !isNaN(minutes)) {
            const d = new Date();
            d.setFullYear(year);
            d.setMonth(month - 1);
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
          const [year, month, day] = datePart.split('-').map(Number);
          const timeComponents = timePart.split(':').map(Number);
        }

        // Handle simple YYYY-MM-DD format
        if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
          const [year, month, day] = value.split('-').map(Number);
          if (!isNaN(year) && !isNaN(month) && !isNaN(day)) {
            const d = new Date();
            d.setFullYear(year);
            d.setMonth(month - 1); // JS months are 0-indexed
            d.setDate(day);
            d.setHours(0); // Explicitly set time to midnight local
            d.setMinutes(0);
            d.setSeconds(0);
            d.setMilliseconds(0);
            return d;
          }
        }
        
        // Try native parsing as last resort, but preserve local time components
        const parsed = new Date(value);
        if (!isNaN(parsed.getTime())) {
          const d = new Date();
          d.setFullYear(parsed.getFullYear());
          d.setMonth(parsed.getMonth());
          d.setDate(parsed.getDate());
          d.setHours(parsed.getHours());
          d.setMinutes(parsed.getMinutes());
          d.setSeconds(parsed.getSeconds());
          d.setMilliseconds(0);
          return d;
        }
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
    // IMPORTANT: We preserve local time exactly as entered, no timezone manipulation
    static formatForSakai(date) {
      if (!date) return "";
      
      date = DateHelper.normalize(date);
      if (!date) return "";
      
      // Format the date components directly, preserving local time
      const year = date.getFullYear();
      const month = DateHelper.pad(date.getMonth() + 1);
      const day = DateHelper.pad(date.getDate());
      const hours = DateHelper.pad(date.getHours());
      const minutes = DateHelper.pad(date.getMinutes());
      const seconds = DateHelper.pad(date.getSeconds());
      
      // Always use local timezone offset
      const offset = -new Date().getTimezoneOffset();
      const offsetSign = offset >= 0 ? '+' : '-';
      const offsetHours = DateHelper.pad(Math.floor(Math.abs(offset) / 60));
      const offsetMinutes = DateHelper.pad(Math.abs(offset) % 60);
      
      return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}${offsetSign}${offsetHours}:${offsetMinutes}`;
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

      // Normalize useTime to a boolean, accepting 1/0, '1'/'0', true/false, 'true'/'false'
      const normalizeBoolean = (v) => {
        if (typeof v === 'boolean') return v;
        if (typeof v === 'number') return v === 1;
        if (typeof v === 'string') {
          const s = v.trim().toLowerCase();
          return s === '1' || s === 'true' || s === 'yes' || s === 'on';
        }
        return Boolean(v);
      };
      this.options.useTime = normalizeBoolean(this.options.useTime);

      // Handle both selector strings and DOM elements
      this.element = typeof this.options.input === 'string' 
          ? document.querySelector(this.options.input)
          : this.options.input;

      if (!this.element) {
        console.error("Input element not found:", this.options.input);
        return;
      }

      this.init();
    }

	init() {
	  // Process initial date value
	  const initialDate = this.getInitialDate();

	  // Set input value
	  this.element.value = initialDate 
		? DateHelper.formatForInput(initialDate, this.options.useTime) 
		: '';

	  // Set input type after setting value
	  this.element.type = this.options.useTime ? "datetime-local" : "date";
	  // Add max date restriction
	  this.element.max = this.options.useTime ? "2099-12-31T23:59" : "2099-12-31";

	  // Complete setup
	  this.createHiddenFields();
	  this.syncHiddenFields(initialDate);
	  this.setupEventListeners();
	  this.setupDuration();
	  
	  // Add hasDatepicker class for backward compatibility with jquery-ui
	  this.element.classList.add('hasDatepicker');
	  this.element.style.minWidth = "200px";
	  
	  // Setup copy/paste functionality
	  this.setupCopyPasteFunctionality();
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

  setupCopyPasteFunctionality() {
    const input = this.element;

    // Add focus listener to show copy/paste icons
    input.addEventListener('focus', () => {
      const isDisabled = input.disabled;
      const isReadOnly = input.readOnly;
      if (isDisabled) return;

      // Remove previous icons
      input.parentNode.querySelectorAll('.date-input-clipboard-focus-icon, .date-input-paste-focus-icon').forEach(el => el.remove());

      // Copy icon
      const copyIcon = document.createElement('i');
      copyIcon.className = 'bi bi-copy date-input-clipboard-focus-icon';
      copyIcon.tabIndex = 0;
      copyIcon.setAttribute('role', 'button');
      const tCopy = "Copy to clipboard";
      copyIcon.setAttribute('title', tCopy);
      copyIcon.setAttribute('aria-label', tCopy);

      // Paste icon
      const pasteIcon = document.createElement('i');
      pasteIcon.className = 'bi bi-clipboard date-input-paste-focus-icon';
      pasteIcon.tabIndex = 0;
      pasteIcon.setAttribute('role', 'button');
      const tPaste = "Paste from clipboard";
      pasteIcon.setAttribute('title', tPaste);
      pasteIcon.setAttribute('aria-label', tPaste);

      // Ensure positioned container for absolute icons
      const parent = input.parentNode;
      const parentComputed = window.getComputedStyle(parent);
      const addedRelative = parentComputed.position === 'static';
      if (addedRelative) parent.style.position = 'relative';

      // Insert icons after the input
      input.parentNode.insertBefore(copyIcon, input.nextSibling);
      if (!isReadOnly) input.parentNode.insertBefore(pasteIcon, copyIcon.nextSibling);

      // Copy logic
      const doCopy = (e) => {
        e.preventDefault();
        const value = input.value;
        if (value) {
          if (navigator.clipboard && window.isSecureContext) {
            navigator.clipboard.writeText(value).catch(() => {});
          }
          document.querySelectorAll('.date-input-clipboard-focus-icon').forEach(el => el.remove());
          document.querySelectorAll('.date-input-clipboard-copied-icon').forEach(el => el.remove());
          const copiedIcon = document.createElement('i');
          copiedIcon.className = 'bi bi-clipboard-check date-input-clipboard-copied-icon';
          const tCopied = "Copied!";
          copiedIcon.setAttribute('title', tCopied);
          input.parentNode.insertBefore(copiedIcon, input.nextSibling);
          setTimeout(() => {
            copiedIcon.remove();
            input.parentNode.insertBefore(copyIcon, input.nextSibling);
          }, 1200);
        }
      };
      copyIcon.addEventListener('pointerdown', doCopy, { passive: false });
      copyIcon.addEventListener('click', doCopy);
      copyIcon.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') doCopy(e);
      });

      // Paste logic: read from clipboard and set value if valid
      const doPaste = (e) => {
        e.preventDefault();
        if (navigator.clipboard && window.isSecureContext) {
          navigator.clipboard.readText().then(text => {
            if (text) {
              let regex;
              if (input.type === 'datetime-local') {
                regex = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(?::\d{2}(?:\.\d{1,3})?)?$/;
              } else if (input.type === 'date') {
                regex = /^\d{4}-\d{2}-\d{2}$/;
              }
              if (regex && regex.test(text.trim())) {
                const normalizedDate = DateHelper.normalize(text.trim());
                if (normalizedDate) {
                  input.value = DateHelper.formatForInput(normalizedDate, this.options.useTime);
                  this.syncHiddenFields(normalizedDate);
                  input.dispatchEvent(new Event('input', { bubbles: true }));
                  input.dispatchEvent(new Event('change', { bubbles: true }));

                  // Trigger callback if provided
                  this.options.onDateTimeSelected?.call(this, normalizedDate.getTime());

                  // Update duration if configured
                  if (this.options.duration) this.updateDuration();
                }
              } else {
                console.warn('Invalid format. Must be ISO.');
              }
            }
          }).catch((err) => {
            alert("Clipboard access is blocked by your browser settings.");
            console.warn('Could not read from clipboard.', err);
          });
        } else {
          alert("Clipboard access is blocked by your browser settings.");
          console.warn('Clipboard access is not available in this browser.');
        }
      };
      pasteIcon.addEventListener('pointerdown', doPaste, { passive: false });
      pasteIcon.addEventListener('click', doPaste);
      pasteIcon.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') doPaste(e);
      });

      // Remove the icons on blur
      input.addEventListener('blur', function removeClipboardPasteIcons() {
        (input.parentNode ?? document).querySelectorAll('.date-input-clipboard-focus-icon, .date-input-paste-focus-icon').forEach(el => el.remove());
        if (parent && parent.style && parent.style.position === 'relative' && addedRelative) {
          parent.style.position = '';
        }
      }, { once: true });
    });
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
