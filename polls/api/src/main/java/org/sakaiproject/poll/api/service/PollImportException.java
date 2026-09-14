/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.poll.api.service;

public class PollImportException extends IllegalArgumentException {

    public static final int UNKNOWN_ROW = -1;

    private final PollImportError error;
    private final int rowNumber;
    private final Object[] messageArgs;

    public PollImportException(PollImportError error) {
        this(error, UNKNOWN_ROW, null, null);
    }

    public PollImportException(PollImportError error, Throwable cause) {
        this(error, UNKNOWN_ROW, null, cause);
    }

    public PollImportException(PollImportError error, int rowNumber) {
        this(error, rowNumber, null, null);
    }

    public PollImportException(PollImportError error, int rowNumber, Throwable cause) {
        this(error, rowNumber, null, cause);
    }

    public PollImportException(PollImportError error, int rowNumber, Object[] messageArgs) {
        this(error, rowNumber, messageArgs, null);
    }

    public PollImportException(PollImportError error, int rowNumber, Object[] messageArgs, Throwable cause) {
        super(error.name(), cause);
        this.error = error;
        this.rowNumber = rowNumber;
        this.messageArgs = messageArgs;
    }

    public PollImportError getError() {
        return error;
    }

    /**
     * 1-based row number in the CSV (counting the header as row 1), or {@link #UNKNOWN_ROW}
     * when the failure isn't tied to a single row.
     */
    public int getRowNumber() {
        return rowNumber;
    }

    public Object[] getMessageArgs() {
        return messageArgs;
    }
}
