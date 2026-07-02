/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Unit tests for the PURE date logic in initDatePicker.js. The logic is intentionally timezone-agnostic
// (wall-clock only; Sakai resolves the zone server-side via UserTimeService), so these tests do plain
// wall-clock assertions. Run under TZ=UTC (see package.json) for determinism. DOM/jQuery glue (fill,
// apply, collapse, attach) is intentionally not covered here - see README.md. Run: npm test

const { test } = require("node:test");
const assert = require("node:assert/strict");
const { loadDtmn } = require("./loadDtmn");

const DAY_MS = 24 * 60 * 60 * 1000;

const { DTMN, moment } = loadDtmn();
// Build a moment from a wall-clock string the same way the tool does (strict, no timezone).
const mom = (s) => moment(s, ["YYYY-MM-DDTHH:mm:ss", "YYYY-MM-DD"], true);

// ---------------------------------------------------------------------------
// parseInputDateValue - the regression guard for the timezone-offset bug.
// ---------------------------------------------------------------------------

test("parseInputDateValue strips a positive offset and keeps the wall-clock time", () => {
  const d = DTMN.parseInputDateValue("2026-09-01T09:00:00+02:00", true);
  assert.ok(d.isValid());
  assert.equal(d.hours(), 9);
  assert.equal(d.minutes(), 0);
  assert.equal(d.format("YYYY-MM-DDTHH:mm:ss"), "2026-09-01T09:00:00");
});

test("parseInputDateValue strips a negative offset", () => {
  const d = DTMN.parseInputDateValue("2026-09-01T09:00:00-05:00", true);
  assert.ok(d.isValid());
  assert.equal(d.format("YYYY-MM-DDTHH:mm:ss"), "2026-09-01T09:00:00");
});

test("parseInputDateValue strips a Z suffix", () => {
  const d = DTMN.parseInputDateValue("2026-09-01T09:00:00Z", true);
  assert.ok(d.isValid());
  assert.equal(d.format("YYYY-MM-DDTHH:mm:ss"), "2026-09-01T09:00:00");
});

test("parseInputDateValue with useTime=false yields a valid date-only moment from a datetime+offset", () => {
  // Gradebook columns are date-only, but localDatePicker still stores a full datetime+offset.
  const d = DTMN.parseInputDateValue("2026-09-01T00:00:00+02:00", false);
  assert.ok(d.isValid());
  assert.equal(d.format("YYYY-MM-DD"), "2026-09-01");
});

test("parseInputDateValue still parses a value that has no offset", () => {
  const d = DTMN.parseInputDateValue("2026-09-01T09:00:00", true);
  assert.ok(d.isValid());
  assert.equal(d.format("YYYY-MM-DDTHH:mm:ss"), "2026-09-01T09:00:00");
});

test("parseInputDateValue returns an invalid moment for an empty value", () => {
  assert.equal(DTMN.parseInputDateValue("", true).isValid(), false);
});

test("documents the bug: parseDatePickerInputValue (strict, no offset token) rejects the offset string", () => {
  // This is exactly why parseInputDateValue exists: the raw strict parser cannot read the
  // localDatePicker hidden value, which is what made every apply silently no-op.
  const raw = DTMN.parseDatePickerInputValue("2026-09-01T09:00:00+02:00", true);
  assert.equal(raw.isValid(), false);
  // ...and the wrapper fixes it.
  assert.equal(DTMN.parseInputDateValue("2026-09-01T09:00:00+02:00", true).isValid(), true);
});

// ---------------------------------------------------------------------------
// Wall-clock fidelity: parse then format must not shift the value (no timezone math).
// ---------------------------------------------------------------------------

test("datetime round-trips through parse and the hidden/visible formatters", () => {
  const parsed = DTMN.parseDatePickerInputValue("2026-09-01T09:30:00", true);
  assert.equal(DTMN.getHiddenDateValue(parsed, true), "2026-09-01T09:30:00");
  assert.equal(DTMN.getDatePickerInputValue(parsed, true), "2026-09-01T09:30");
});

test("date-only round-trips through parse and the formatters", () => {
  const parsed = DTMN.parseDatePickerInputValue("2026-09-01", false);
  assert.equal(DTMN.getHiddenDateValue(parsed, false), "2026-09-01");
  assert.equal(DTMN.getDatePickerInputValue(parsed, false), "2026-09-01");
});

// ---------------------------------------------------------------------------
// snapToSourceWeekday - keep each item's own weekday + clock time, move by whole days (<= 3).
// ---------------------------------------------------------------------------

test("snapToSourceWeekday lands on the source weekday nearest the target, carrying source's time", () => {
  const source = mom("2026-09-14T09:30:15"); // a Monday
  const target = mom("2026-09-16T00:00:00"); // a Wednesday, 2 days later
  const result = DTMN.snapToSourceWeekday(target, source);

  assert.equal(result.day(), source.day());
  assert.equal(result.hours(), 9);
  assert.equal(result.minutes(), 30);
  assert.equal(result.seconds(), 15);
  assert.equal(result.format("YYYY-MM-DD"), "2026-09-14");
});

test("snapToSourceWeekday never moves more than 3 days and preserves the weekday for all combos", () => {
  const monday = mom("2026-09-14T00:00:00");
  for (let t = 0; t < 7; t++) {
    const target = monday.clone().add(t, "days");
    for (let s = 0; s < 7; s++) {
      const source = monday.clone().add(s, "days").hours(8).minutes(5).seconds(0);
      const result = DTMN.snapToSourceWeekday(target, source);

      assert.equal(result.day(), source.day(), `weekday preserved (t=${t}, s=${s})`);
      assert.equal(result.hours(), 8);
      assert.equal(result.minutes(), 5);

      const dayShift = Math.round(
        (result.clone().startOf("day").valueOf() - target.clone().startOf("day").valueOf()) / DAY_MS
      );
      assert.ok(dayShift >= -3 && dayShift <= 3, `within +/-3 days (t=${t}, s=${s}, shift=${dayShift})`);
    }
  }
});

test("snapToSourceWeekday keeps same-weekday items a whole number of weeks apart", () => {
  const source = mom("2026-09-16T10:00:00"); // Wednesday
  const target1 = mom("2026-09-14T00:00:00"); // Monday
  const target2 = target1.clone().add(14, "days"); // Monday, two weeks on

  const r1 = DTMN.snapToSourceWeekday(target1, source);
  const r2 = DTMN.snapToSourceWeekday(target2, source);

  const diffDays = Math.round((r2.startOf("day").valueOf() - r1.startOf("day").valueOf()) / DAY_MS);
  assert.equal(diffDays % 7, 0);
});

// ---------------------------------------------------------------------------
// computeFittedDate - the extracted Smart Shift per-cell mapping.
// ---------------------------------------------------------------------------

const anchors = () => ({ first: mom("2026-07-01T00:00:00"), last: mom("2026-09-19T00:00:00") });

test("computeFittedDate maps the earliest cell exactly onto the new first date", () => {
  const a = anchors();
  const result = DTMN.computeFittedDate(1000, 1000, 1000, a, false, null);
  assert.equal(result.valueOf(), a.first.valueOf());
});

test("computeFittedDate maps the latest cell exactly onto the new last date", () => {
  const a = anchors();
  const result = DTMN.computeFittedDate(2000, 1000, 1000, a, false, null);
  assert.equal(result.valueOf(), a.last.valueOf());
});

test("computeFittedDate collapses everything onto the new first date when the old span is zero", () => {
  const a = anchors();
  const result = DTMN.computeFittedDate(5000, 5000, 0, a, false, null);
  assert.equal(result.valueOf(), a.first.valueOf());
});

test("computeFittedDate places a middle cell proportionally when snap is off", () => {
  const a = anchors();
  const newSpan = a.last.valueOf() - a.first.valueOf();
  const result = DTMN.computeFittedDate(1500, 1000, 1000, a, false, null);
  assert.equal(result.valueOf(), a.first.valueOf() + newSpan / 2);
});

test("computeFittedDate snaps a middle cell to the source weekday, staying near the proportional target", () => {
  const a = anchors();
  const newSpan = a.last.valueOf() - a.first.valueOf();
  const source = mom("2026-08-12T13:00:00"); // arbitrary mid-term Wednesday
  const result = DTMN.computeFittedDate(1500, 1000, 1000, a, true, source);

  assert.equal(result.day(), source.day());
  const proportional = a.first.valueOf() + newSpan / 2;
  const shiftDays = Math.abs(result.valueOf() - proportional) / DAY_MS;
  assert.ok(shiftDays <= 3.5, `snap stays within ~half a week of the proportional target (shift=${shiftDays})`);
});

test("computeFittedDate: the LATEST date wins the end anchor regardless of column role", () => {
  // This is the behaviour that confused us during manual testing: an assignment's open date that
  // happened to be later than its due/accept dates correctly received the new LAST date.
  const a = anchors();
  const openIsLatest = DTMN.computeFittedDate(1000, 100, 900, a, false, null);
  assert.equal(openIsLatest.valueOf(), a.last.valueOf());
});

// ---------------------------------------------------------------------------
// computeRowFittedDates - per-row snap guard: snapping must never reorder an item's own dates
// (the server rejects due-before-open on save) or leave the new term window.
// ---------------------------------------------------------------------------

const rowCell = (m) => ({ current: m, currentMs: m.valueOf() });

test("computeRowFittedDates falls back to proportional when snapping would invert open/due under compression", () => {
  // 16-week course squeezed into 4 weeks: open Fri 09:00 / due the following Mon 17:00 snap to
  // their own weekdays independently, which used to yield due BEFORE open for every such item -
  // an unsaveable state. The row must fall back to proportional placement instead.
  const a = { first: mom("2026-09-01T00:00:00"), last: mom("2026-09-28T00:00:00") }; // 4 weeks
  const oldStart = mom("2026-01-05T00:00:00").valueOf();
  const oldSpan = mom("2026-04-27T00:00:00").valueOf() - oldStart; // ~16 weeks
  const open = rowCell(mom("2026-03-06T09:00:00")); // Friday
  const due = rowCell(mom("2026-03-09T17:00:00")); // the following Monday

  // premise: independent per-cell snapping really does invert this pair
  const cellSnappedOpen = DTMN.computeFittedDate(open.currentMs, oldStart, oldSpan, a, true, open.current);
  const cellSnappedDue = DTMN.computeFittedDate(due.currentMs, oldStart, oldSpan, a, true, due.current);
  assert.ok(cellSnappedOpen.valueOf() > cellSnappedDue.valueOf(), "scenario must trigger the inversion hazard");

  const out = DTMN.computeRowFittedDates([open, due], oldStart, oldSpan, a, true);

  assert.ok(out[0].valueOf() <= out[1].valueOf(), "open must not pass due");
  assert.ok(out[0].valueOf() >= a.first.valueOf(), "stays inside the new window (start)");
  assert.ok(out[1].valueOf() <= a.last.valueOf(), "stays inside the new window (end)");
});

test("computeRowFittedDates keeps weekday snapping on a same-length rollover", () => {
  // fall -> spring, identical length: snapping is safe, so it must stay active and both cells
  // keep their weekday and clock time.
  const oldStart = mom("2026-09-07T00:00:00").valueOf(); // Monday
  const oldSpan = mom("2026-12-28T00:00:00").valueOf() - oldStart; // 112 days
  const a = { first: mom("2027-01-04T00:00:00"), last: mom("2027-04-26T00:00:00") }; // 112 days
  const open = rowCell(mom("2026-10-09T09:00:00")); // Friday
  const due = rowCell(mom("2026-10-12T17:00:00")); // Monday

  const out = DTMN.computeRowFittedDates([open, due], oldStart, oldSpan, a, true);

  assert.equal(out[0].day(), 5, "open keeps its Friday");
  assert.equal(out[0].hours(), 9);
  assert.equal(out[1].day(), 1, "due keeps its Monday");
  assert.equal(out[1].hours(), 17);
  assert.ok(out[0].valueOf() < out[1].valueOf(), "row order preserved");
});

test("computeRowFittedDates without snap returns the plain proportional placement", () => {
  const a = anchors();
  const cell = rowCell(mom("2026-08-12T13:00:00"));
  const out = DTMN.computeRowFittedDates([cell], 1000, 1000, a, false);
  assert.equal(
    out[0].valueOf(),
    DTMN.computeFittedDate(cell.currentMs, 1000, 1000, a, false, null).valueOf()
  );
});

// ---------------------------------------------------------------------------
// computeDayDiff - the date-difference helper feeding the shift field.
// ---------------------------------------------------------------------------

test("computeDayDiff returns signed whole days between two dates", () => {
  assert.equal(DTMN.computeDayDiff(mom("2026-01-06"), mom("2026-01-20")), 14);
  assert.equal(DTMN.computeDayDiff(mom("2026-01-20"), mom("2026-01-06")), -14);
  assert.equal(DTMN.computeDayDiff(mom("2026-01-06"), mom("2026-01-06")), 0);
});

test("computeDayDiff counts calendar days, ignoring time of day", () => {
  assert.equal(DTMN.computeDayDiff(mom("2026-01-06T23:00:00"), mom("2026-01-07T01:00:00")), 1);
});

test("computeDayDiff counts whole days across a month boundary", () => {
  assert.equal(DTMN.computeDayDiff(mom("2026-01-25"), mom("2026-02-05")), 11);
});
