// Default options matching the original localDatePicker
const defaults = {
	input: null, // CSS selector for the input field
	useTime: true, // Whether to include time in the picker (accepts 1/true)
	parseFormat: null, // Optional format for parsing initial value (e.g., 'YYYY-MM-DD HH:mm:ss')
	allowEmptyDate: false, // Whether empty dates are allowed
	val: "", // Initial value
	ashidden: {}, // Hidden fields to sync (e.g., { iso8601: "fieldName" })
	duration: null, // Optional: { hour: "hourField", minute: "minuteField", update: "updateField" }
	onDateTimeSelected: null, // Optional callback for when a date/time is selected
};

class SakaiDateTimePicker {
	constructor(options) {
		this.options = { ...defaults, ...options };
		// Normalize useTime to boolean (accepts 1 or true)
		this.options.useTime = this.options.useTime === 1 || this.options.useTime === true;
		
		// Handle both DOM element and selector string
		if (typeof this.options.input === 'string') {
			this.element = document.querySelector(this.options.input);
		} else if (this.options.input instanceof Element) {
			this.element = this.options.input;
		}

		if (!this.element) {
			console.error("Input element not found:", this.options.input);
			return;
		}
		this.initTime = Date.now(); // Save initialization time for server offset
		this.init();
	}

	init() {
		this.element.type = this.options.useTime ? "datetime-local" : "date";
		this.element.style.minWidth = "200px"; // Consistent with original design
		this.setInitialValue();
		this.createHiddenFields();
		this.setupEventListeners();
		this.handleDuration();
	}

	setInitialValue() {
		let initialValue = this.options.val || this.element.value.trim();

		if (!initialValue && !this.options.allowEmptyDate) {
			initialValue = this.getPreferredSakaiDatetime();
		}

		if (initialValue) {
			const date = this.parseDate(initialValue);
			this.element.value = this.formatForInput(date);
			this.syncHiddenFields(date);
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
			? endDate.toLocaleString()
			: endDate.toLocaleDateString();

		document.querySelector(`#${duration.update}`).textContent = endString;
	}

	parseDate(value) {
		if (!value) return this.options.allowEmptyDate ? null : new Date();

		// If parseFormat is provided, attempt to parse with it (requires a library like Moment.js)
		if (this.options.parseFormat && typeof moment !== "undefined") {
			const parsed = moment(value, this.options.parseFormat, true);
			if (parsed.isValid()) return parsed.toDate();
		}

		const date = new Date(value);
		return isNaN(date.getTime()) ? new Date() : date;
	}

	formatForInput(date) {
		if (!date) return "";
		const pad = (num) => String(num).padStart(2, "0");
		const year = date.getFullYear();
		const month = pad(date.getMonth() + 1);
		const day = pad(date.getDate());
		if (!this.options.useTime) return `${year}-${month}-${day}`;
		const hours = pad(date.getHours());
		const minutes = pad(date.getMinutes());
		return `${year}-${month}-${day}T${hours}:${minutes}`;
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
						newValue = date.toISOString();
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
		if (p && p.serverTimeMillis && p.user && p.user.offsetFromServerMillis) {
			const osTzOffset = new Date().getTimezoneOffset();
			return new Date(
				parseInt(p.serverTimeMillis) +
				p.user.offsetFromServerMillis +
				(Date.now() - this.initTime) +
				osTzOffset * 60 * 1000
			);
		}
		console.debug("No Sakai timezone or server time. Using local time.");
		return new Date();
	}
}

// Wrapper function to match original localDatePicker API
function localDatePicker(options) {
	return new SakaiDateTimePicker(options);
}

// Expose SDP.initSakaiDatePicker for compatibility with previous modern version
function initSakaiDatePicker(inputField, value, useTime, allowEmptyDate) {
	return new SakaiDateTimePicker({
		input: `#${inputField}`,
		useTime,
		allowEmptyDate,
		val: value,
		ashidden: { iso8601: `${inputField}ISO8601` },
	});
}

window.SDP = window.SDP || {};
window.SDP.initSakaiDatePicker = initSakaiDatePicker;