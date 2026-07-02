# DateManager JS unit tests

Standalone unit tests for the **pure date logic** in
`../../main/resources/static/js/initDatePicker.js` (the DateManager client-side
engine): timezone-offset parsing, weekday snapping, the Smart Shift proportional
fit, and format round-trips.

These are **not part of the Maven build** — the WAR plugin only packages
`src/main/webapp`, so nothing here is ever shipped. They exist to guard the tricky
date math against regression (e.g. the timezone-offset parse bug that made the bulk
apply silently no-op).

## Run

Requires **Node 18+** (uses the built-in `node:test` runner). The only dependency is `moment`.
The date logic is timezone-agnostic (wall-clock only — Sakai resolves the zone server-side), and the
`test` script pins `TZ=UTC` for deterministic wall-clock math.

```sh
cd site-manage/datemanager/tool/src/test/js
npm install
npm test          # runs: TZ=UTC node --test
```

On Windows (cmd), run `set TZ=UTC && node --test`, or use Git Bash / WSL.

## What is and isn't covered

- **Covered:** `parseInputDateValue`, `parseDatePickerInputValue`, `snapToSourceWeekday`,
  `computeFittedDate`, `computeRowFittedDates`, `computeDayDiff`,
  `getDatePickerInputValue` / `getHiddenDateValue`.
- **Not covered:** DOM/jQuery glue (fill, apply, collapse, attach, init/validate). Those
  need a jsdom or Playwright harness and are out of scope for this suite.

`loadDtmn.js` loads the browser script into a Node `vm` context with `moment` injected, then returns
the populated `DTMN` object — so the tests exercise the real shipped code with the real date library.
