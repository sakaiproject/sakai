/*
 * Live "parsed count" indicator for the Add Participants textareas.
 *
 * Mirrors SiteAddParticipantHandler.normalizeSmart / normalizeNonOfficial so the user sees how many
 * entries were parsed out of their paste. Loaded as an external file (rather than inline) because the
 * template is processed by RSF/IKAT as XML, which mangles inline scripts.
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
	 * Mirror of SiteAddParticipantHandler.normalizeSmart for the official box: return the list of
	 * entries the server would parse out of the raw textarea value. Each line is handled on its own so
	 * a mixed email/username list keeps working: a line with an "@" has its emails extracted; a line
	 * without an "@" is split into usernames on any delimiter.
	 * @param {string} raw the raw textarea value
	 * @returns {string[]} the parsed entries (empty when the input is blank)
	 */
	function normalize(raw) {
		raw = raw || "";
		if (!raw.trim()) return [];
		var out = [];
		raw.split(/\r\n|\r|\n/).forEach(function (line) {
			if (line.indexOf("@") >= 0) {
				(line.match(EMAIL_RE) || []).forEach(function (e) { out.push(e); });
			} else {
				line.trim().split(/[,;\s]+/).filter(Boolean).forEach(function (u) { out.push(u); });
			}
		});
		return out;
	}

	/**
	 * Mirror of SiteAddParticipantHandler.normalizeNonOfficial for the non-official (guest) box.
	 * Keeps structured "email,lastName,firstName" rows intact (one person per line or semicolon) and
	 * only splits a chunk that holds more than one email.
	 * @param {string} raw the raw textarea value
	 * @returns {string[]} one entry per guest (empty when the input is blank)
	 */
	function normalizeNonOfficial(raw) {
		raw = raw || "";
		if (!raw.trim()) return [];
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
	 * Tally how many parsed entries are emails vs usernames, auto-detecting by the presence of "@".
	 * @param {string[]} entries the parsed entries
	 * @returns {{e: number, u: number, n: number}} email count, username count, and total
	 */
	function classify(entries) {
		var e = 0, u = 0;
		entries.forEach(function (entry) {
			if (entry.indexOf("@") >= 0) e++; else u++;
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
	 * Wire a textarea to its live count output: recompute the count on input and render it
	 * immediately. No-op when the output or its textarea can't be found.
	 * @param {{emails: string, usernames: string, mixed: string}} templates localized strings
	 * @param {string}  outputId      id of the count <p> element to update
	 * @param {boolean} [nonOfficial] true to use the name-preserving guest normalizer
	 * @returns {void}
	 */
	function wire(templates, outputId, nonOfficial) {
		var out = document.getElementById(outputId);
		if (!out) return;
		var ta = textareaFor(out);
		if (!ta) return;

		/**
		 * Recompute and render the count from the textarea's current value.
		 * @returns {void}
		 */
		function update() {
			var entries = nonOfficial ? normalizeNonOfficial(ta.value) : normalize(ta.value);
			out.textContent = format(templates, classify(entries));
		}

		ta.addEventListener("input", update);
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
		wire(templates, "officialAccountCount");
		wire(templates, "nonOfficialAccountCount", true);
	}

	if (document.readyState === "loading") {
		document.addEventListener("DOMContentLoaded", init);
	} else {
		init();
	}
})();
