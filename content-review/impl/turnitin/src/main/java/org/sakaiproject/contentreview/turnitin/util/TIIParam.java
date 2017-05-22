/**
 * Copyright (c) 2003 The Apereo Foundation
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
package org.sakaiproject.contentreview.turnitin.util;

public enum TIIParam {
    aid,
    ainst,
    assign,
    assignid,
    ced,
    cid,
    cpw,
    ctl,
    diagnostic,
    dis,
    dtdue,
    dtstart,
    encrypt,
    fcmd,
    fid,
    gmtime,
    newassign,
    newupw,
    oid,
    pdata,
    pfn,
    pln,
    ptl,
    ptype,
    said,
    sessionId,
    tem,
    uem,
    ufn,
    uid,
    uln,
    upw,
    username,
    utp,
    erater,
    ets_handbook,
    ets_dictionary,
    ets_spelling,
    ets_style,
    ets_grammar,
    ets_mechanics,
    ets_usage;

    public String toString() {
        // Unfortunately you can't put dashes in enum constant names, so this
        // special case is for the newly added session-id TII Parameter.
        if (name().equals("sessionId")) {
            return "session-id";
        }
        else {
            return name();
        }
    }
}
