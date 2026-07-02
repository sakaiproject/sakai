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

// Test-only helper: load the browser script initDatePicker.js into Node so its pure date functions
// can be unit tested. The script is a plain browser global (it does `var DTMN = ...` and attaches
// functions, with no module.exports) and only touches document/$/moment INSIDE function bodies, so it
// loads cleanly in a vm context as long as we provide `moment`. The date logic is deliberately
// timezone-agnostic (wall-clock only - Sakai resolves the zone server-side), so plain `moment` is all
// that's needed; run the suite under TZ=UTC (see package.json) for deterministic wall-clock math.

const fs = require("fs");
const path = require("path");
const vm = require("vm");
const moment = require("moment");

const SCRIPT_PATH = path.resolve(
  __dirname,
  "../../main/resources/static/js/initDatePicker.js"
);

/**
 * Load a fresh DTMN object.
 *
 * @returns {{DTMN: object, moment: import("moment")}} the populated DTMN plus the same moment instance
 *          the script uses, so tests build inputs from the identical library.
 */
function loadDtmn() {
  const source = fs.readFileSync(SCRIPT_PATH, "utf8");

  const sandbox = { moment, console };
  // In a vm context globalThis is the sandbox itself; provide a minimal sakai stub so any incidental
  // sakai.locale lookups elsewhere in the script can't throw.
  sandbox.globalThis = sandbox;
  sandbox.sakai = { locale: {} };

  vm.createContext(sandbox);
  vm.runInContext(source, sandbox, { filename: SCRIPT_PATH });

  if (!sandbox.DTMN) {
    throw new Error("initDatePicker.js did not define DTMN when loaded");
  }
  return { DTMN: sandbox.DTMN, moment };
}

module.exports = { loadDtmn };
