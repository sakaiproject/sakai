/**
 * Copyright (c) 2010 onwards - The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.meetings.api;

public class MeetingsException extends Exception {

    private static final long   serialVersionUID            = 2421100107566638321L;

    public static final String  MESSAGEKEY_HTTPERROR            = "httpError";
    public static final String  MESSAGEKEY_NOTFOUND             = "notFound";
    public static final String  MESSAGEKEY_NOACTION             = "noActionSpecified";
    public static final String  MESSAGEKEY_IDNOTUNIQUE          = "idNotUnique";
    public static final String  MESSAGEKEY_NOTSTARTED           = "notStarted";
    public static final String  MESSAGEKEY_ALREADYENDED         = "alreadyEnded";
    public static final String  MESSAGEKEY_INTERNALERROR        = "internalError";
    public static final String  MESSAGEKEY_UNREACHABLE          = "unreachableServerError";
    public static final String  MESSAGEKEY_INVALIDRESPONSE      = "invalidResponseError";
    public static final String  MESSAGEKEY_GENERALERROR         = "generalError";

    private String messageKey;

    public MeetingsException(String messageKey, String message, Throwable cause) {

        super(message, cause);
        this.messageKey = messageKey;
    }

    public MeetingsException(String messageKey, String message) {

        super(message);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }
    
    public String getPrettyMessage() {

        String _message = getMessage();
        String _messageKey = getMessageKey();
        
        StringBuilder pretty = new StringBuilder();
        if(_message != null) {
            pretty.append(_message);
        }
        if(_messageKey != null && !"".equals(_messageKey.trim())) {
            pretty.append(" (");
            pretty.append(_messageKey);
            pretty.append(")");
        }
        return pretty.toString();
    }
}
