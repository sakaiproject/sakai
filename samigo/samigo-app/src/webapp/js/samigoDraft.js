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
const samigoDraft = (() => {
    "use strict";

    const PREFIX = "samigoDraft_";
    const LAST_PURGE_KEY = "samigoDraftLastPurge";
    const MAX_FIELD_LENGTH = 262144; // 256 KB per field
    const TTL_MS = 7 * 24 * 60 * 60 * 1000;
    const PURGE_INTERVAL_MS = 24 * 60 * 60 * 1000;
    const DEBOUNCE_MS = 2000;

    let attemptKey = null;
    let pageKey = "";
    const timers = {};
    // Pending debounced writes by field name, so a Save/Next submit or a
    // page hide can commit them before the debounce timer would have fired.
    const pendingWrites = {};
    // Fields we restored a draft into this render: their content is NOT
    // server-confirmed, so the confirmed-content cleanup must skip them.
    const restoredFields = {};

    const username = () => {
        try {
            if (typeof portal !== "undefined" && portal.user && typeof portal.user.eid === "string") {
                return portal.user.eid;
            }
        } catch (e) { }
        return "";
    };

    const assessmentForm = () => document.getElementById("takeAssessmentForm");

    const hiddenValue = (name) => {
        try {
            const field = assessmentForm().elements[`takeAssessmentForm:${name}`];
            return field && field.value ? field.value : "";
        } catch (e) { return ""; }
    };

    const fieldKey = (name) => {
        // JSF client ids are positional and repeat across pages in
        // question-per-page and part-per-page layouts, so the page position
        // must be part of the key or drafts leak between questions.
        return `${PREFIX}${attemptKey}_${pageKey}_${name}_${username()}`;
    };

    const isBlank = (html) => {
        if (!html) { return true; }
        // Embedded media/structure is content even when there is no text.
        if (/<\s*(img|iframe|audio|video|object|embed|svg|math|canvas|table|hr)\b/i.test(html)) { return false; }
        return html.replace(/<[^>]*>/g, "").replace(/&nbsp;/gi, " ").replace(/\s+/g, "") === "";
    };

    const store = (name, value) => {
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
    };

    const read = (name) => {
        try {
            const raw = localStorage.getItem(fieldKey(name));
            if (!raw) { return null; }
            const parsed = JSON.parse(raw);
            return parsed && parsed.v ? parsed.v : null;
        } catch (e) { return null; }
    };

    const remove = (name) => {
        try { localStorage.removeItem(fieldKey(name)); } catch (e) { }
    };

    const purgeExpired = () => {
        try {
            const now = Date.now();
            const last = parseInt(localStorage.getItem(LAST_PURGE_KEY), 10);
            if (!isNaN(last) && (now - last) < PURGE_INTERVAL_MS) {
                return;
            }
            localStorage.setItem(LAST_PURGE_KEY, String(now));
            for (let i = localStorage.length - 1; i >= 0; i--) {
                const key = localStorage.key(i);
                if (key && key.indexOf(PREFIX) === 0) {
                    let drop = true;
                    try {
                        const parsed = JSON.parse(localStorage.getItem(key));
                        drop = !parsed || !parsed.t || (now - parsed.t) > TTL_MS;
                    } catch (e) { /* unparseable: drop */ }
                    if (drop) { localStorage.removeItem(key); }
                }
            }
        } catch (e) { }
    };

    const debounced = (name, fn) => {
        if (timers[name]) { clearTimeout(timers[name]); }
        pendingWrites[name] = fn;
        timers[name] = setTimeout(() => {
            delete pendingWrites[name];
            delete timers[name];
            fn();
        }, DEBOUNCE_MS);
    };

    // A Save/Next click right after typing must not outrun the debounce: the
    // whole point is surviving a POST that dies, so the snapshot has to be in
    // localStorage BEFORE the request leaves.
    const flushPending = () => {
        Object.keys(pendingWrites).forEach((name) => {
            clearTimeout(timers[name]);
            const fn = pendingWrites[name];
            delete pendingWrites[name];
            delete timers[name];
            fn();
        });
    };

    const showRestoredNotice = () => {
        const notice = document.getElementById("draft-restored-info");
        if (notice) { notice.style.display = "block"; }
    };

    const hookTextarea = (area) => {
        if (!area.name || area.samigoDraftHooked) { return; }
        area.samigoDraftHooked = true;

        const draft = read(area.name);
        if (draft && isBlank(area.value)) {
            area.value = draft;
            restoredFields[area.name] = true;
            showRestoredNotice();
        } else if (!isBlank(area.value)) {
            // Server-confirmed content: the local draft is obsolete.
            remove(area.name);
        }
        area.addEventListener("input", () => {
            debounced(area.name, () => store(area.name, area.value));
        });
    };

    const hookEditor = (editor) => {
        if (!editor || editor.samigoDraftHooked) { return; }
        editor.samigoDraftHooked = true;

        const draft = read(editor.name);
        if (draft && isBlank(editor.getData())) {
            // Restore into the live editor even when hookTextarea already put
            // the draft into the underlying textarea: an editor launched
            // before init() was instantiated from the still-empty textarea,
            // so the textarea restore never reached what the student sees.
            editor.setData(draft);
            restoredFields[editor.name] = true;
            showRestoredNotice();
        } else if (!restoredFields[editor.name] && !isBlank(editor.getData())) {
            // Confirmed by the server - but only when the content did not
            // come from our own just-restored, still-unsaved draft.
            remove(editor.name);
        }
        editor.on("change", () => {
            debounced(editor.name, () => store(editor.name, editor.getData()));
        });
    };

    const init = (gradingId) => {
        if (!gradingId || !window.localStorage) { return; }
        attemptKey = String(gradingId);
        pageKey = `p${hiddenValue("partIndex")}q${hiddenValue("questionIndex")}`;
        purgeExpired();

        const form = assessmentForm();
        if (!form) { return; }

        // Capture phase so the flush runs even if another submit handler
        // stops propagation; pagehide/visibilitychange cover programmatic
        // form.submit() calls and the tab being closed or backgrounded.
        form.addEventListener("submit", flushPending, true);
        window.addEventListener("pagehide", flushPending);
        document.addEventListener("visibilitychange", () => {
            if (document.visibilityState === "hidden") { flushPending(); }
        });

        Array.from(form.getElementsByTagName("textarea")).forEach(hookTextarea);

        if (window.CKEDITOR) {
            CKEDITOR.on("instanceReady", (evt) => hookEditor(evt.editor));
            Object.values(CKEDITOR.instances)
                .filter((instance) => instance.status === "ready")
                .forEach(hookEditor);
        }
    };

    return { init };
})();
