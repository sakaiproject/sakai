/**
 * SAK-52710: client-side draft persistence for text answers in delivery.
 *
 * Snapshots what the student types (plain textareas and CKEditor instances)
 * into localStorage, keyed by attempt + page position + field + user. Unlike
 * the CKEditor autosave plugin, drafts are NEVER deleted when a form button
 * is clicked - a Next/Save click whose POST dies (network drop, stale-tab
 * resync) must not take the draft down with it. Drafts are deleted when the
 * student clears the field or the server renders it with confirmed content,
 * expire after 7 days, and are capped in size. A draft is only restored into
 * a field the server rendered EMPTY, with a visible notice. Everything is
 * best-effort: storage failures are silent.
 */
var samigoDraft = (function () {
    "use strict";

    var PREFIX = "samigoDraft_";
    var LAST_PURGE_KEY = "samigoDraftLastPurge";
    var MAX_FIELD_LENGTH = 262144; // 256 KB per field
    var TTL_MS = 7 * 24 * 60 * 60 * 1000;
    var PURGE_INTERVAL_MS = 24 * 60 * 60 * 1000;
    var DEBOUNCE_MS = 2000;

    var attemptKey = null;
    var pageKey = "";
    var timers = {};
    // Fields we restored a draft into this render: their content is NOT
    // server-confirmed, so the confirmed-content cleanup must skip them.
    var restoredFields = {};

    function username() {
        try {
            if (typeof portal !== "undefined" && portal.user && typeof portal.user.eid === "string") {
                return portal.user.eid;
            }
        } catch (e) { }
        return "";
    }

    function hiddenValue(name) {
        try {
            var field = document.forms[0].elements["takeAssessmentForm:" + name];
            return field && field.value ? field.value : "";
        } catch (e) { return ""; }
    }

    function fieldKey(name) {
        // JSF client ids are positional and repeat across pages in
        // question-per-page and part-per-page layouts, so the page position
        // must be part of the key or drafts leak between questions.
        return PREFIX + attemptKey + "_" + pageKey + "_" + name + "_" + username();
    }

    function store(name, value) {
        try {
            if (isBlank(value) || value.length > MAX_FIELD_LENGTH) {
                // A cleared field must not resurrect later - including one an
                // editor "cleared" to blank markup like <p>&nbsp;</p> - and an
                // over-long field must not freeze at a misleading older snapshot.
                localStorage.removeItem(fieldKey(name));
                return;
            }
            localStorage.setItem(fieldKey(name), JSON.stringify({ t: Date.now(), v: value }));
        } catch (e) { /* quota exceeded or private mode */ }
    }

    function read(name) {
        try {
            var raw = localStorage.getItem(fieldKey(name));
            if (!raw) { return null; }
            var parsed = JSON.parse(raw);
            return parsed && parsed.v ? parsed.v : null;
        } catch (e) { return null; }
    }

    function remove(name) {
        try { localStorage.removeItem(fieldKey(name)); } catch (e) { }
    }

    function purgeExpired() {
        try {
            var now = Date.now();
            var last = parseInt(localStorage.getItem(LAST_PURGE_KEY), 10);
            if (!isNaN(last) && (now - last) < PURGE_INTERVAL_MS) {
                return;
            }
            localStorage.setItem(LAST_PURGE_KEY, String(now));
            for (var i = localStorage.length - 1; i >= 0; i--) {
                var k = localStorage.key(i);
                if (k && k.indexOf(PREFIX) === 0) {
                    var drop = true;
                    try {
                        var parsed = JSON.parse(localStorage.getItem(k));
                        drop = !parsed || !parsed.t || (now - parsed.t) > TTL_MS;
                    } catch (e) { /* unparseable: drop */ }
                    if (drop) { localStorage.removeItem(k); }
                }
            }
        } catch (e) { }
    }

    function debounced(name, fn) {
        if (timers[name]) { clearTimeout(timers[name]); }
        timers[name] = setTimeout(fn, DEBOUNCE_MS);
    }

    function isBlank(html) {
        if (!html) { return true; }
        // Embedded media/structure is content even when there is no text.
        if (/<\s*(img|iframe|audio|video|object|embed|svg|math|canvas|table|hr)\b/i.test(html)) { return false; }
        return html.replace(/<[^>]*>/g, "").replace(/&nbsp;/gi, " ").replace(/\s+/g, "") === "";
    }

    function showRestoredNotice() {
        var notice = document.getElementById("draft-restored-info");
        if (notice) { notice.style.display = "block"; }
    }

    function hookTextarea(area) {
        if (!area.name || area.samigoDraftHooked) { return; }
        area.samigoDraftHooked = true;

        var draft = read(area.name);
        if (draft && isBlank(area.value)) {
            area.value = draft;
            restoredFields[area.name] = true;
            showRestoredNotice();
        } else if (!isBlank(area.value)) {
            // Server-confirmed content: the local draft is obsolete.
            remove(area.name);
        }
        var handler = function () {
            debounced(area.name, function () { store(area.name, area.value); });
        };
        if (area.addEventListener) {
            area.addEventListener("input", handler, false);
        } else {
            area.onkeyup = handler;
        }
    }

    function hookEditor(editor) {
        if (!editor || editor.samigoDraftHooked) { return; }
        editor.samigoDraftHooked = true;

        if (!restoredFields[editor.name]) {
            var draft = read(editor.name);
            if (draft && isBlank(editor.getData())) {
                editor.setData(draft);
                restoredFields[editor.name] = true;
                showRestoredNotice();
            } else if (!isBlank(editor.getData())) {
                // Confirmed by the server - but only when the content did not
                // come from our own just-restored, still-unsaved draft.
                remove(editor.name);
            }
        }
        editor.on("change", function () {
            debounced(editor.name, function () { store(editor.name, editor.getData()); });
        });
    }

    function init(gradingId) {
        if (!gradingId || !window.localStorage) { return; }
        attemptKey = String(gradingId);
        pageKey = "p" + hiddenValue("partIndex") + "q" + hiddenValue("questionIndex");
        purgeExpired();

        var form = document.getElementById("takeAssessmentForm");
        if (!form) { return; }

        var areas = form.getElementsByTagName("textarea");
        for (var i = 0; i < areas.length; i++) {
            hookTextarea(areas[i]);
        }

        if (window.CKEDITOR) {
            CKEDITOR.on("instanceReady", function (evt) { hookEditor(evt.editor); });
            for (var name in CKEDITOR.instances) {
                if (CKEDITOR.instances.hasOwnProperty(name) && CKEDITOR.instances[name].status === "ready") {
                    hookEditor(CKEDITOR.instances[name]);
                }
            }
        }
    }

    return { init: init };
})();
