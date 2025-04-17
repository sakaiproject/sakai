/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.export;

import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.user.api.User;

import java.io.OutputStream;
import java.util.List;

public interface PDFEventExporter {

    /**
     * Generates a PDF sign-in sheet for an AttendanceEvent and places it in the
     * output stream so it can be sent to the user.
     * @param attendanceEvent
     * @param outputStream
     */
    void createSignInPdf(AttendanceEvent attendanceEvent, OutputStream outputStream, List<User> usersToPrint, String groupOrSiteTitle);

    /**
     * Generates a PDF for an AttendanceEvent and places it in the output stream so it
     * can be sent to the user.
     * @param attendanceEvent
     * @param outputStream
     */
    void createAttendanceSheetPdf(AttendanceEvent attendanceEvent, OutputStream outputStream, List<User> usersToPrint, String groupOrSiteTitle);
}
