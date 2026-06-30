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

// Test-only helper: load the browser script initDatePicker.js into Node so its pure date
// functions can be unit tested. The script is a plain browser global (it does `var DTMN = ...`
// and attaches functions, with no module.exports) and only touches document/$/moment INSIDE
// function bodies, so it loads cleanly in a vm context as long as we provide the globals its
// functions read at call time: `moment` (moment-timezone) and `sakai.locale.userTimeZone`.

const fs = require("fs");
const path = require("path");
const vm = require("vm");
const moment = require("moment-timezone");

const SCRIPT_PATH = path.resolve(
  __dirname,
  "../../main/resources/static/js/initDatePicker.js"
);

/**
 * Load a fresh DTMN object with the given user timezone in scope.
 *
 * @param {string} userTimeZone IANA zone fed to sakai.locale.userTimeZone (default America/New_York).
 * @returns {{DTMN: object, moment: import("moment-timezone")}} the populated DTMN plus the same
 *          moment-timezone instance the script uses, so tests build inputs from the identical library.
 */
function loadDtmn(userTimeZone = "America/New_York") {
  const source = fs.readFileSync(SCRIPT_PATH, "utf8");

  const sandbox = {
    moment,
    sakai: { locale: { userTimeZone } },
    console,
  };
  // The script reads globalThis.sakai?.locale?.userTimeZone; in a vm context globalThis is the
  // sandbox itself, so `sakai` above is reachable that way too.
  sandbox.globalThis = sandbox;

  vm.createContext(sandbox);
  vm.runInContext(source, sandbox, { filename: SCRIPT_PATH });

  if (!sandbox.DTMN) {
    throw new Error("initDatePicker.js did not define DTMN when loaded");
  }
  return { DTMN: sandbox.DTMN, moment };
}

module.exports = { loadDtmn };
