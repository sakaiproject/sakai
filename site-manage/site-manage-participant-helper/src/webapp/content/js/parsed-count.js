/*
 * Live "parsed count" indicator for the Add Participants textareas.
 *
 * Mirrors SiteAddParticipantHandler.normalizeDelimited / effectiveOfficialMode so the user
 * sees how many entries were parsed out of their paste. Loaded as an external file (rather than
 * inline) because the template is processed by RSF/IKAT as XML, which mangles inline scripts.
 *
 * RSF re-writes the id/name it renders on bound form controls, so we do NOT look the textareas
 * up by id. Instead we anchor off the static count <p> elements we control (ids RSF leaves
 * alone) and find each textarea by walking the DOM near it. Keep EMAIL_RE in sync with
 * EMAIL_EXTRACT_PATTERN in SiteAddParticipantHandler.
 */
(function () {
	"use strict";

	var EMAIL_RE = /[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}/g;

	/**
	 * Read the trimmed text content of an element, falling back to a default when the element is
	 * missing or empty. Used to pull the localized count templates rendered into hidden nodes.
	 * @param {string} id       the element id to read
	 * @param {string} fallback text to return when the element is absent or blank
	 * @returns {string} the element's text, or {@code fallback}
	 */
	function txt(id, fallback) {
		var el = document.getElementById(id);
		var s = el ? (el.textContent || "").trim() : "";
		return s || fallback;
	}

	/**
	 * Return the value of the currently-checked radio in a named group.
	 * @param {string} name the radio group's name attribute
	 * @returns {?string} the checked radio's value, or null when none is checked
	 */
	function checkedValue(name) {
		var el = document.querySelector('input[name="' + name + '"]:checked');
		return el ? el.value : null;
	}

	/**
	 * Find the textarea associated with a count output: the nearest textarea that appears before
	 * the output element in the DOM, walking previous siblings and then up to the parent. Needed
	 * because RSF rewrites the ids/names it renders, so the textarea can't be looked up by id.
	 * @param {Element} outputEl the count <p> element we anchor off
	 * @returns {?HTMLTextAreaElement} the associated textarea, or null if none is found
	 */
	function textareaFor(outputEl) {
		var node = outputEl;
		while (node) {
			var prev = node.previousElementSibling;
			while (prev) {
				if (prev.tagName === "TEXTAREA") return prev;
				var inner = prev.querySelector ? prev.querySelector("textarea") : null;
				if (inner) return inner;
				prev = prev.previousElementSibling;
			}
			node = node.parentElement;
		}
		return null;
	}

	/**
	 * Resolve the effective input format, mirroring SiteAddParticipantHandler.effectiveOfficialMode:
	 * usernames can't be regex-extracted, so "username" + "smart" falls back to "delimited".
	 * @param {?string} accountType "auto", "email", or "username" (null for the non-official box)
	 * @param {?string} delimiter   the chosen format ("smart", "delimited", or "line")
	 * @returns {string} the format to actually parse with (defaults to "smart")
	 */
	function effectiveMode(accountType, delimiter) {
		if (accountType === "username" && delimiter === "smart") return "delimited";
		return delimiter || "smart";
	}

	/**
	 * Mirror of SiteAddParticipantHandler.normalizeDelimited for the official box: return the list
	 * of entries the server would parse out of the raw textarea value.
	 * @param {string} raw  the raw textarea value
	 * @param {string} mode "smart" (extract emails / split usernames), "delimited", or "line"
	 * @returns {string[]} the parsed entries (empty when the input is blank)
	 */
	function normalize(raw, mode) {
		raw = raw || "";
		if (!raw.trim()) return [];
		if (mode === "smart") {
			if (raw.indexOf("@") >= 0) return raw.match(EMAIL_RE) || [];
			return raw.trim().split(/[,;\s]+/).filter(Boolean);
		}
		if (mode === "delimited") return raw.trim().split(/[,;\s]+/).filter(Boolean);
		// "line": one entry per non-empty line
		return raw.split(/\r\n|\r|\n/).map(function (s) { return s.trim(); }).filter(Boolean);
	}

	/**
	 * Mirror of SiteAddParticipantHandler.normalizeNonOfficial for the non-official (guest) box.
	 * Under smart mode it keeps structured "email,lastName,firstName" rows intact (one person per
	 * line or semicolon) and only splits a chunk that holds more than one email; "line"/"delimited"
	 * defer to {@link normalize}.
	 * @param {string} raw  the raw textarea value
	 * @param {string} mode "smart", "delimited", or "line"
	 * @returns {string[]} one entry per guest (empty when the input is blank)
	 */
	function normalizeNonOfficial(raw, mode) {
		raw = raw || "";
		if (!raw.trim()) return [];
		if (mode !== "smart") return normalize(raw, mode);
		var out = [];
		raw.split(/[;\r\n]+/).forEach(function (chunk) {
			var person = chunk.trim();
			if (!person) return;
			var emails = person.match(EMAIL_RE) || [];
			if (emails.length > 1) {
				emails.forEach(function (e) { out.push(e); });
			} else {
				out.push(person);
			}
		});
		return out;
	}

	/**
	 * Tally how many parsed entries are emails vs usernames, honoring an explicit account type or
	 * auto-detecting by the presence of "@".
	 * @param {string[]} entries      the parsed entries
	 * @param {?string}  accountType  "email", "username", or anything else / null for auto-detect
	 * @returns {{e: number, u: number, n: number}} email count, username count, and total
	 */
	function classify(entries, accountType) {
		var e = 0, u = 0;
		entries.forEach(function (entry) {
			var isEmail;
			if (accountType === "email") isEmail = true;
			else if (accountType === "username") isEmail = false;
			else isEmail = entry.indexOf("@") >= 0;
			if (isEmail) e++; else u++;
		});
		return { e: e, u: u, n: entries.length };
	}

	/**
	 * Render the count message from the localized templates, choosing the emails-only,
	 * usernames-only, or mixed phrasing based on the tally.
	 * @param {{emails: string, usernames: string, mixed: string}} templates localized strings
	 * @param {{e: number, u: number, n: number}} c the tally from {@link classify}
	 * @returns {string} the display text (empty when there are no entries)
	 */
	function format(templates, c) {
		if (c.n === 0) return "";
		if (c.e > 0 && c.u > 0) {
			return templates.mixed.replace("#N#", c.n).replace("#E#", c.e).replace("#U#", c.u);
		}
		if (c.u === 0) return templates.emails.replace("#N#", c.e);
		return templates.usernames.replace("#N#", c.u);
	}

	/**
	 * Wire a textarea to its live count output: recompute the count on input and on any radio
	 * change, and render it immediately. No-op when the output or its textarea can't be found.
	 * @param {{emails: string, usernames: string, mixed: string}} templates localized strings
	 * @param {string}  outputId       id of the count <p> element to update
	 * @param {string}  delimiterName  name of the input-format radio group
	 * @param {?string} accountTypeName name of the account-type radio group (null for guests)
	 * @param {boolean} [nonOfficial]  true to use the name-preserving guest normalizer
	 * @returns {void}
	 */
	function wire(templates, outputId, delimiterName, accountTypeName, nonOfficial) {
		var out = document.getElementById(outputId);
		if (!out) return;
		var ta = textareaFor(out);
		if (!ta) return;

		/**
		 * Recompute and render the count from the textarea's current value and selected options.
		 * @returns {void}
		 */
		function update() {
			var delimiter = checkedValue(delimiterName);
			var accountType = accountTypeName ? checkedValue(accountTypeName) : null;
			var mode = effectiveMode(accountType, delimiter);
			var entries = nonOfficial ? normalizeNonOfficial(ta.value, mode) : normalize(ta.value, mode);
			out.textContent = format(templates, classify(entries, accountType));
		}

		ta.addEventListener("input", update);
		// radio names may be re-written by RSF, so recompute on any change in the document
		document.addEventListener("change", update);
		update();
	}

	/**
	 * Read the localized count templates and wire up both the official and non-official boxes.
	 * @returns {void}
	 */
	function init() {
		var templates = {
			emails: txt("countEmailsTmpl", "#N# emails detected"),
			usernames: txt("countUsernamesTmpl", "#N# usernames detected"),
			mixed: txt("countMixedTmpl", "#N# entries detected (#E# emails, #U# usernames)")
		};
		wire(templates, "officialAccountCount", "officialDelimiter", "officialAccountType");
		wire(templates, "nonOfficialAccountCount", "nonOfficialDelimiter", null, true);
	}

	if (document.readyState === "loading") {
		document.addEventListener("DOMContentLoaded", init);
	} else {
		init();
	}
})();
