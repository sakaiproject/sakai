/*
 * Live "parsed count" indicator for the Add Participants textareas.
 *
 * Mirrors SiteAddParticipantHandler.normalizeDelimited / effectiveOfficialMode so the user
 * sees how many entries were parsed out of their paste. Loaded as an external file (rather than
 * inline) because the template is processed by RSF/IKAT as XML, which mangles inline scripts
 * containing '<', '>' or '&&'.
 *
 * Keep EMAIL_RE in sync with EMAIL_EXTRACT_PATTERN in SiteAddParticipantHandler.
 */
(function () {
	"use strict";

	var EMAIL_RE = /[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}/g;

	function txt(id, fallback) {
		var el = document.getElementById(id);
		var s = el ? (el.textContent || "").trim() : "";
		return s || fallback;
	}

	function checkedValue(name) {
		var el = document.querySelector('input[name="' + name + '"]:checked');
		return el ? el.value : null;
	}

	// usernames can't be regex-extracted, so username+smart falls back to delimited
	function effectiveMode(accountType, delimiter) {
		if (accountType === "username" && delimiter === "smart") return "delimited";
		return delimiter || "smart";
	}

	// returns the list of entries the server would parse out of the raw textarea value
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

	function format(templates, c) {
		if (c.n === 0) return "";
		if (c.e > 0 && c.u > 0) {
			return templates.mixed.replace("#N#", c.n).replace("#E#", c.e).replace("#U#", c.u);
		}
		if (c.u === 0) return templates.emails.replace("#N#", c.e);
		return templates.usernames.replace("#N#", c.u);
	}

	function wire(templates, textareaId, delimiterName, accountTypeName, outputId) {
		var ta = document.getElementById(textareaId);
		var out = document.getElementById(outputId);
		if (!ta || !out) return;

		function update() {
			var delimiter = checkedValue(delimiterName);
			var accountType = accountTypeName ? checkedValue(accountTypeName) : null;
			var entries = normalize(ta.value, effectiveMode(accountType, delimiter));
			out.textContent = format(templates, classify(entries, accountType));
		}

		ta.addEventListener("input", update);
		document.querySelectorAll('input[name="' + delimiterName + '"]').forEach(function (r) {
			r.addEventListener("change", update);
		});
		if (accountTypeName) {
			document.querySelectorAll('input[name="' + accountTypeName + '"]').forEach(function (r) {
				r.addEventListener("change", update);
			});
		}
		update();
	}

	function init() {
		var templates = {
			emails: txt("countEmailsTmpl", "#N# emails detected"),
			usernames: txt("countUsernamesTmpl", "#N# usernames detected"),
			mixed: txt("countMixedTmpl", "#N# entries detected (#E# emails, #U# usernames)")
		};
		wire(templates, "officialAccountParticipant", "officialDelimiter", "officialAccountType", "officialAccountCount");
		wire(templates, "nonOfficialAccountParticipant", "nonOfficialDelimiter", null, "nonOfficialAccountCount");
	}

	if (document.readyState === "loading") {
		document.addEventListener("DOMContentLoaded", init);
	} else {
		init();
	}
})();
