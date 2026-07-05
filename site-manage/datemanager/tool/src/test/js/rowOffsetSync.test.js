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

// SAK-45285 when a row's open date is edited, the row's later dates shift by
// the same wall-clock delta, preserving the offsets the item already had.

const { test } = require("node:test");
const assert = require("node:assert/strict");
const { loadDtmn } = require("./loadDtmn");

const { DTMN, moment } = loadDtmn();

function m(value) {
  return moment(value, "YYYY-MM-DDTHH:mm:ss", true);
}

function shifted(dateStr, oldAnchorStr, newAnchorStr) {
  return DTMN.shiftPreservingWallClock(m(dateStr), m(oldAnchorStr), m(newAnchorStr))
    .format("YYYY-MM-DDTHH:mm:ss");
}

test("offset suffixes are stripped whatever their sign (the Americas write negative offsets)", () => {
  assert.equal(DTMN.stripOffset("2026-07-04T16:25:00-06:00"), "2026-07-04T16:25:00");
  assert.equal(DTMN.stripOffset("2026-07-04T16:25:00+02:00"), "2026-07-04T16:25:00");
  assert.equal(DTMN.stripOffset("2026-07-04T16:25:00+0000"), "2026-07-04T16:25:00");
  assert.equal(DTMN.stripOffset("2026-07-04T16:25:00Z"), "2026-07-04T16:25:00");
  // untouched when there is no offset: bare seconds are not an offset
  assert.equal(DTMN.stripOffset("2026-07-04T16:25:00"), "2026-07-04T16:25:00");
  assert.equal(DTMN.stripOffset("2026-07-04"), "2026-07-04");
  assert.equal(DTMN.stripOffset(""), "");
});

test("anchor fields are the open date and the sign-up begins column", () => {
  // copy out of the vm realm so deepEqual compares contents, not prototypes
  assert.deepEqual(Array.from(DTMN.rowAnchorFields), ["open_date", "signup_begins"]);
});

test("moving the open date forward a week moves the due date the same week, wall clock preserved", () => {
  assert.equal(
    shifted("2026-08-20T23:59:00", "2026-08-13T08:00:00", "2026-08-20T08:00:00"),
    "2026-08-27T23:59:00");
});

test("moving the open date backward shifts siblings backward", () => {
  assert.equal(
    shifted("2026-08-20T23:59:00", "2026-08-13T08:00:00", "2026-08-06T08:00:00"),
    "2026-08-13T23:59:00");
});

test("a year rollover preserves the original open-to-due spread", () => {
  // Aug 13 2025 -> Aug 12 2026 (364 days); due follows exactly
  assert.equal(
    shifted("2025-08-27T23:59:00", "2025-08-13T08:00:00", "2026-08-12T08:00:00"),
    "2026-08-26T23:59:00");
});

test("changing only the open time shifts sibling times by the same amount", () => {
  assert.equal(
    shifted("2026-08-20T23:00:00", "2026-08-13T08:00:00", "2026-08-13T08:30:00"),
    "2026-08-20T23:30:00");
});

test("changing date and time together applies both parts", () => {
  assert.equal(
    shifted("2026-08-20T22:00:00", "2026-08-13T08:00:00", "2026-08-14T09:15:00"),
    "2026-08-21T23:15:00");
});

test("a shift is a no-op when the anchor did not move", () => {
  assert.equal(
    shifted("2026-08-20T23:59:00", "2026-08-13T08:00:00", "2026-08-13T08:00:00"),
    "2026-08-20T23:59:00");
});

test("a shift across a DST boundary keeps wall-clock time (zone-aware moments)", () => {
  // America/New_York springs forward 2026-03-08: the anchor moves Mar 6 -> Mar 13
  // (7 calendar days across the transition) and the 23:00 due time stays 23:00,
  // even though the interval is only 167 real hours
  const tz = require("moment-timezone");
  const oldAnchor = tz.tz("2026-03-06T08:00:00", "America/New_York");
  const newAnchor = tz.tz("2026-03-13T08:00:00", "America/New_York");
  const due = tz.tz("2026-03-06T23:00:00", "America/New_York");
  const shiftedDue = DTMN.shiftPreservingWallClock(due, oldAnchor, newAnchor);
  assert.equal(shiftedDue.format("YYYY-MM-DDTHH:mm:ss"), "2026-03-13T23:00:00");
  assert.equal(newAnchor.diff(oldAnchor, "hours"), 167); // proves we really crossed DST
  // and back again across the same boundary
  const restored = DTMN.shiftPreservingWallClock(shiftedDue, newAnchor, oldAnchor);
  assert.equal(restored.format("YYYY-MM-DDTHH:mm:ss"), "2026-03-06T23:00:00");
});

test("days are applied as calendar days so time-of-day never drifts", () => {
  // 90-day jump lands at the same wall-clock time
  assert.equal(
    shifted("2026-01-10T17:45:00", "2026-01-05T09:00:00", "2026-04-05T09:00:00"),
    "2026-04-10T17:45:00");
});

test("date-only values (gradebook style) shift by whole days", () => {
  const result = DTMN.shiftPreservingWallClock(
    moment("2026-08-20", "YYYY-MM-DD", true),
    m("2026-08-13T08:00:00"),
    m("2026-08-20T08:00:00"));
  assert.equal(result.format("YYYY-MM-DD"), "2026-08-27");
});
