package org.tsugi.lti;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.text.StringEscapeUtils;
import org.tsugi.lti.objects.*;
import org.tsugi.pox.POXConstants;
import org.tsugi.pox.POXResponseBuilder;
import org.tsugi.pox.POXResponseFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class POXJacksonParser {
    
    private static final Logger log = LoggerFactory.getLogger(POXJacksonParser.class);
    
    public final static String MAJOR_SUCCESS = POXConstants.MAJOR_SUCCESS;
    public final static String MAJOR_FAILURE = POXConstants.MAJOR_FAILURE;
    public final static String MAJOR_UNSUPPORTED = POXConstants.MAJOR_UNSUPPORTED;
    public final static String MAJOR_PROCESSING = POXConstants.MAJOR_PROCESSING;
    
    public final static String SEVERITY_ERROR = POXConstants.SEVERITY_ERROR;
    public final static String SEVERITY_WARNING = POXConstants.SEVERITY_WARNING;
    public final static String SEVERITY_STATUS = POXConstants.SEVERITY_STATUS;
    
    public final static String MINOR_FULLSUCCESS = POXConstants.MINOR_FULLSUCCESS;
    public final static String MINOR_NOSOURCEDIDS = POXConstants.MINOR_NOSOURCEDIDS;
    public final static String MINOR_IDALLOC = POXConstants.MINOR_IDALLOC;
    public final static String MINOR_OVERFLOWFAIL = POXConstants.MINOR_OVERFLOWFAIL;
    public final static String MINOR_IDALLOCINUSEFAIL = POXConstants.MINOR_IDALLOCINUSEFAIL;
    public final static String MINOR_INVALIDDATAFAIL = POXConstants.MINOR_INVALIDDATAFAIL;
    public final static String MINOR_INCOMPLETEDATA = POXConstants.MINOR_INCOMPLETEDATA;
    public final static String MINOR_PARTIALSTORAGE = POXConstants.MINOR_PARTIALSTORAGE;
    public final static String MINOR_UNKNOWNOBJECT = POXConstants.MINOR_UNKNOWNOBJECT;
    public final static String MINOR_DELETEFAILURE = POXConstants.MINOR_DELETEFAILURE;
    public final static String MINOR_TARGETREADFAILURE = POXConstants.MINOR_TARGETREADFAILURE;
    public final static String MINOR_SAVEPOINTERROR = POXConstants.MINOR_SAVEPOINTERROR;
    public final static String MINOR_SAVEPOINTSYNCERROR = POXConstants.MINOR_SAVEPOINTSYNCERROR;
    public final static String MINOR_UNKNOWNQUERY = POXConstants.MINOR_UNKNOWNQUERY;
    public final static String MINOR_UNKNOWNVOCAB = POXConstants.MINOR_UNKNOWNVOCAB;
    public final static String MINOR_TARGETISBUSY = POXConstants.MINOR_TARGETISBUSY;
    public final static String MINOR_UNKNOWNEXTENSION = POXConstants.MINOR_UNKNOWNEXTENSION;
    public final static String MINOR_UNAUTHORIZEDREQUEST = POXConstants.MINOR_UNAUTHORIZEDREQUEST;
    public final static String MINOR_LINKFAILURE = POXConstants.MINOR_LINKFAILURE;
    public final static String MINOR_UNSUPPORTED = POXConstants.MINOR_UNSUPPORTED;

    /**
     * Parse a POX request from XML string
     * 
     * @param xmlString The XML string to parse
     * @return POXEnvelopeRequest object or null if parsing fails
     */
    public static POXEnvelopeRequest parseRequest(String xmlString) {
        if (xmlString == null || xmlString.trim().isEmpty()) {
            log.warn("XML string is null or empty");
            return null;
        }

        try {
            XmlMapper mapper = new XmlMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.setDefaultUseWrapper(false);

            POXEnvelopeRequest request = mapper.readValue(xmlString.trim(), POXEnvelopeRequest.class);
            
            if (request != null && request.getPoxHeader() == null && request.getPoxBody() == null) {
                log.warn("Parsed request but both header and body are null - likely invalid XML");
                return null;
            }
            
            log.debug("Successfully parsed POX request");
            return request;

        } catch (Exception e) {
            log.warn("POXJacksonParser exception parsing POX request: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Parse a POX response from XML string
     * 
     * @param xmlString The XML string to parse
     * @return POXEnvelopeResponse object or null if parsing fails
     */
    public static POXEnvelopeResponse parseResponse(String xmlString) {
        if (xmlString == null || xmlString.trim().isEmpty()) {
            log.warn("XML string is null or empty");
            return null;
        }

        try {
            XmlMapper mapper = new XmlMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.setDefaultUseWrapper(false);

            POXEnvelopeResponse response = mapper.readValue(xmlString.trim(), POXEnvelopeResponse.class);
            log.debug("Successfully parsed POX response");
            return response;

        } catch (Exception e) {
            log.warn("POXJacksonParser exception parsing POX response: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get the operation type from a parsed request
     * 
     * @param request The parsed POX request
     * @return The operation type or null if not found
     */
    public static String getOperation(POXEnvelopeRequest request) {
        if (request == null || request.getPoxBody() == null) {
            return null;
        }
        
        POXRequestBody body = request.getPoxBody();
        if (body.getReplaceResultRequest() != null) {
            return "replaceResultRequest";
        } else if (body.getReadResultRequest() != null) {
            return "readResultRequest";
        } else if (body.getDeleteResultRequest() != null) {
            return "deleteResultRequest";
        } else if (body.getReadMembershipRequest() != null) {
            return "readMembershipRequest";
        }
        
        return null;
    }

    /**
     * Get header information from a parsed request
     * 
     * @param request The parsed POX request
     * @return Map containing header information
     */
    public static Map<String, String> getHeaderInfo(POXEnvelopeRequest request) {
        Map<String, String> headerInfo = new HashMap<>();
        
        if (request == null || request.getPoxHeader() == null || 
            request.getPoxHeader().getRequestHeaderInfo() == null) {
            return headerInfo;
        }
        
        POXRequestHeaderInfo headerInfoObj = request.getPoxHeader().getRequestHeaderInfo();
        headerInfo.put("version", headerInfoObj.getVersion());
        headerInfo.put("messageIdentifier", headerInfoObj.getMessageIdentifier());
        
        return headerInfo;
    }

    /**
     * Get sourcedId from the POX body.
     * Works with replaceResultRequest, readResultRequest, deleteResultRequest, and readMembershipRequest.
     * 
     * @param request The parsed POX request
     * @return The sourcedId string, or null if not found
     */
    public static String getBodySourcedId(POXEnvelopeRequest request) {
        if (request == null || request.getPoxBody() == null) {
            return null;
        }
        
        POXRequestBody body = request.getPoxBody();
        ResultRecord resultRecord = null;
        
        if (body.getReplaceResultRequest() != null) {
            resultRecord = body.getReplaceResultRequest().getResultRecord();
        } else if (body.getReadResultRequest() != null) {
            resultRecord = body.getReadResultRequest().getResultRecord();
        } else if (body.getDeleteResultRequest() != null) {
            resultRecord = body.getDeleteResultRequest().getResultRecord();
        }
        
        if (resultRecord != null && resultRecord.getSourcedGUID() != null) {
            return resultRecord.getSourcedGUID().getSourcedId();
        }
        
        if (body.getReadMembershipRequest() != null) {
            return body.getReadMembershipRequest().getSourcedId();
        }
        
        return null;
    }

    /**
     * Get language from the resultScore in the POX body.
     * Works with replaceResultRequest, readResultRequest, and deleteResultRequest.
     * 
     * @param request The parsed POX request
     * @return The language string, or null if not found
     */
    public static String getBodyLanguage(POXEnvelopeRequest request) {
        if (request == null || request.getPoxBody() == null) {
            return null;
        }
        
        POXRequestBody body = request.getPoxBody();
        ResultRecord resultRecord = null;
        
        if (body.getReplaceResultRequest() != null) {
            resultRecord = body.getReplaceResultRequest().getResultRecord();
        } else if (body.getReadResultRequest() != null) {
            resultRecord = body.getReadResultRequest().getResultRecord();
        } else if (body.getDeleteResultRequest() != null) {
            resultRecord = body.getDeleteResultRequest().getResultRecord();
        }
        
        if (resultRecord != null && resultRecord.getResult() != null && 
            resultRecord.getResult().getResultScore() != null) {
            return resultRecord.getResult().getResultScore().getLanguage();
        }
        
        return null;
    }

    /**
     * Get textString from the resultScore in the POX body.
     * Works with replaceResultRequest, readResultRequest, and deleteResultRequest.
     * 
     * @param request The parsed POX request
     * @return The textString value, or null if not found
     */
    public static String getBodyTextString(POXEnvelopeRequest request) {
        if (request == null || request.getPoxBody() == null) {
            return null;
        }
        
        POXRequestBody body = request.getPoxBody();
        ResultRecord resultRecord = null;
        
        if (body.getReplaceResultRequest() != null) {
            resultRecord = body.getReplaceResultRequest().getResultRecord();
        } else if (body.getReadResultRequest() != null) {
            resultRecord = body.getReadResultRequest().getResultRecord();
        } else if (body.getDeleteResultRequest() != null) {
            resultRecord = body.getDeleteResultRequest().getResultRecord();
        }
        
        if (resultRecord != null && resultRecord.getResult() != null && 
            resultRecord.getResult().getResultScore() != null) {
            return resultRecord.getResult().getResultScore().getTextString();
        }
        
        return null;
    }

    /**
     * Create a fatal response
     * 
     * @param description The error description
     * @return XML response string
     */
    public static String createFatalResponse(String description) {
        return createFatalResponse(description, "unknown");
    }

    /**
     * Create a fatal response with message ID
     * 
     * @param description The error description
     * @param messageId The message ID
     * @return XML response string
     */
    public static String createFatalResponse(String description, String messageId) {
        Date dt = new Date();
        String messageIdValue = "" + dt.getTime();

        return String.format(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<imsx_POXEnvelopeResponse xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
            "    <imsx_POXHeader>\n" +
            "        <imsx_POXResponseHeaderInfo>\n" + 
            "            <imsx_version>V1.0</imsx_version>\n" +
            "            <imsx_messageIdentifier>%s</imsx_messageIdentifier>\n" + 
            "            <imsx_statusInfo>\n" +
            "                <imsx_codeMajor>failure</imsx_codeMajor>\n" +
            "                <imsx_severity>error</imsx_severity>\n" +
            "                <imsx_description>%s</imsx_description>\n" +
            "                <imsx_operationRefIdentifier>%s</imsx_operationRefIdentifier>" + 
            "            </imsx_statusInfo>\n" +
            "        </imsx_POXResponseHeaderInfo>\n" + 
            "    </imsx_POXHeader>\n" +
            "    <imsx_POXBody/>\n" +
            "</imsx_POXEnvelopeResponse>",
            StringEscapeUtils.escapeXml11(messageIdValue), 
            StringEscapeUtils.escapeXml11(description),
            StringEscapeUtils.escapeXml11(messageId)
        );
    }

    /**
     * Create a success response
     * 
     * @param description The success description
     * @param bodyString The response body content
     * @param messageId The message ID
     * @param operation The operation type
     * @return XML response string
     */
    public static String createSuccessResponse(String description, String bodyString, 
                                             String messageId, String operation) {
        return createResponse(description, MAJOR_SUCCESS, SEVERITY_STATUS, messageId, 
                            operation, null, bodyString);
    }

    /**
     * Create a failure response
     * 
     * @param description The failure description
     * @param minorCodes Minor error codes
     * @param messageId The message ID
     * @param operation The operation type
     * @return XML response string
     */
    public static String createFailureResponse(String description, Properties minorCodes,
                                             String messageId, String operation) {
        return createResponse(description, MAJOR_FAILURE, SEVERITY_ERROR, messageId, 
                            operation, minorCodes, null);
    }

    /**
     * Create an unsupported response
     * 
     * @param description The unsupported description
     * @param messageId The message ID
     * @param operation The operation type
     * @return XML response string
     */
    public static String createUnsupportedResponse(String description, String messageId, String operation) {
        return createResponse(description, MAJOR_UNSUPPORTED, SEVERITY_ERROR, messageId, 
                            operation, null, null);
    }

    /**
     * Create a generic response
     * 
     * @param description The response description
     * @param major The major code
     * @param severity The severity level
     * @param messageId The message ID
     * @param operation The operation type
     * @param minorCodes Minor error codes
     * @param bodyString The response body content
     * @return XML response string
     */
    public static String createResponse(String description, String major, String severity, 
                                      String messageId, String operation, Properties minorCodes, 
                                      String bodyString) {
        if (major == null) major = MAJOR_FAILURE;
        if (severity == null && MAJOR_PROCESSING.equals(major)) severity = SEVERITY_STATUS;
        if (severity == null && MAJOR_SUCCESS.equals(major)) severity = SEVERITY_STATUS;
        if (severity == null) severity = SEVERITY_ERROR;
        if (messageId == null) {
            Date dt = new Date();
            messageId = "" + dt.getTime();
        }

        StringBuilder minorString = new StringBuilder();
        if (minorCodes != null && minorCodes.size() > 0) {
            minorString.append("\n        <imsx_codeMinor>\n");
            for (Object okey : minorCodes.keySet()) {
                String key = (String) okey;
                String value = minorCodes.getProperty(key);
                if (key == null || value == null) continue;
                
                minorString.append("          <imsx_codeMinorField>\n");
                minorString.append("            <imsx_codeMinorFieldName>");
                minorString.append(StringEscapeUtils.escapeXml11(key));
                minorString.append("</imsx_codeMinorFieldName>\n");
                minorString.append("            <imsx_codeMinorFieldValue>");
                minorString.append(StringEscapeUtils.escapeXml11(value));
                minorString.append("</imsx_codeMinorFieldValue>\n");
                minorString.append("          </imsx_codeMinorField>\n");
            }
            minorString.append("        </imsx_codeMinor>");
        }

        if (bodyString == null) bodyString = "";

        if (bodyString.startsWith("<?xml")) {
            int pos = bodyString.indexOf("<", 1);
            if (pos > 0) bodyString = bodyString.substring(pos);
        }
        bodyString = bodyString.trim();
        String newLine = "";
        if (bodyString.length() > 0) newLine = "\n";

        return String.format(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<imsx_POXEnvelopeResponse xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
            "  <imsx_POXHeader>\n" +
            "    <imsx_POXResponseHeaderInfo>\n" + 
            "      <imsx_version>V1.0</imsx_version>\n" +
            "      <imsx_messageIdentifier>%s</imsx_messageIdentifier>\n" + 
            "      <imsx_statusInfo>\n" +
            "        <imsx_codeMajor>%s</imsx_codeMajor>\n" +
            "        <imsx_severity>%s</imsx_severity>\n" +
            "        <imsx_description>%s</imsx_description>\n" +
            "        <imsx_messageRefIdentifier>%s</imsx_messageRefIdentifier>\n" +       
            "        <imsx_operationRefIdentifier>%s</imsx_operationRefIdentifier>" + 
            "%s\n"+ 
            "      </imsx_statusInfo>\n" +
            "    </imsx_POXResponseHeaderInfo>\n" + 
            "  </imsx_POXHeader>\n" +
            "  <imsx_POXBody>\n" +
            "%s%s"+
            "  </imsx_POXBody>\n" +
            "</imsx_POXEnvelopeResponse>",
            StringEscapeUtils.escapeXml11(messageId), 
            StringEscapeUtils.escapeXml11(major), 
            StringEscapeUtils.escapeXml11(severity), 
            StringEscapeUtils.escapeXml11(description), 
            StringEscapeUtils.escapeXml11(messageId), 
            StringEscapeUtils.escapeXml11(operation), 
            StringEscapeUtils.escapeXml11(minorString.toString()), 
            bodyString, newLine
        );
    }
    
    /**
     * Create a response using POXResponseBuilder for more control
     * 
     * @param description The response description
     * @param major The major code
     * @param severity The severity level
     * @param messageId The message ID
     * @param operation The operation type
     * @param minorCodes Minor error codes
     * @param bodyString The response body content
     * @return POXEnvelopeResponse object
     */
    public static POXEnvelopeResponse createResponseObject(String description, String major, String severity, 
                                                          String messageId, String operation, Properties minorCodes, 
                                                          String bodyString) {
        return POXResponseBuilder.create()
            .withDescription(description)
            .withMajor(major)
            .withSeverity(severity)
            .withMessageId(messageId)
            .withOperation(operation)
            .withMinorCodes(minorCodes)
            .withBodyContent(bodyString)
            .build();
    }
    
    /**
     * Create a response with minor codes from field objects
     * 
     * @param description The response description
     * @param major The major code
     * @param severity The severity level
     * @param messageId The message ID
     * @param operation The operation type
     * @param minorFields List of minor code fields
     * @param bodyString The response body content
     * @return XML response string
     */
    public static String createResponseWithMinorFields(String description, String major, String severity, 
                                                     String messageId, String operation, List<POXCodeMinorField> minorFields, 
                                                     String bodyString) {
        return POXResponseFactory.createResponseWithMinorFields(description, major, severity, 
                messageId, operation, minorFields, bodyString);
    }
    
    /**
     * Validate POX request parameters
     * 
     * @param request The POX request to validate
     * @return ValidationResult containing validation status and errors
     */
    public static ValidationResult validateRequest(POXEnvelopeRequest request) {
        ValidationResult result = new ValidationResult();
        
        if (request == null) {
            result.addError("Request is null");
            return result;
        }
        
        if (request.getPoxHeader() == null) {
            result.addError("Missing POX header");
        } else {
            POXRequestHeaderInfo headerInfo = request.getPoxHeader().getRequestHeaderInfo();
            if (headerInfo == null) {
                result.addError("Missing request header info");
            } else {
                if (headerInfo.getVersion() == null || headerInfo.getVersion().trim().isEmpty()) {
                    result.addError("Missing version");
                }
                if (headerInfo.getMessageIdentifier() == null || headerInfo.getMessageIdentifier().trim().isEmpty()) {
                    result.addError("Missing message identifier");
                }
            }
        }

        if (request.getPoxBody() == null) {
            result.addError("Missing POX body");
        } else {
            POXRequestBody body = request.getPoxBody();
            int operationCount = 0;
            
            if (body.getReplaceResultRequest() != null) operationCount++;
            if (body.getReadResultRequest() != null) operationCount++;
            if (body.getDeleteResultRequest() != null) operationCount++;
            if (body.getReadMembershipRequest() != null) operationCount++;
            
            if (operationCount == 0) {
                result.addError("No valid operation found in body");
            } else if (operationCount > 1) {
                result.addError("Multiple operations found in body");
            }
        }
        
        return result;
    }
    
    /**
     * Get detailed operation information from a request
     * 
     * @param request The POX request
     * @return OperationInfo containing detailed operation data
     */
    public static OperationInfo getOperationInfo(POXEnvelopeRequest request) {
        OperationInfo info = new OperationInfo();
        
        if (request == null || request.getPoxBody() == null) {
            return info;
        }
        
        POXRequestBody body = request.getPoxBody();
        
        if (body.getReplaceResultRequest() != null) {
            info.setOperationType(POXConstants.OPERATION_REPLACE_RESULT);
            ReplaceResultRequest replaceRequest = body.getReplaceResultRequest();
            if (replaceRequest.getResultRecord() != null) {
                ResultRecord record = replaceRequest.getResultRecord();
                if (record.getSourcedGUID() != null) {
                    info.setSourcedId(record.getSourcedGUID().getSourcedId());
                }
                if (record.getResult() != null && record.getResult().getResultScore() != null) {
                    ResultScore score = record.getResult().getResultScore();
                    info.setLanguage(score.getLanguage());
                    info.setScore(score.getTextString());
                }
            }
        } else if (body.getReadResultRequest() != null) {
            info.setOperationType(POXConstants.OPERATION_READ_RESULT);
            if (body.getReadResultRequest().getResultRecord() != null && 
                body.getReadResultRequest().getResultRecord().getSourcedGUID() != null) {
                info.setSourcedId(body.getReadResultRequest().getResultRecord().getSourcedGUID().getSourcedId());
            }
        } else if (body.getDeleteResultRequest() != null) {
            info.setOperationType(POXConstants.OPERATION_DELETE_RESULT);
            if (body.getDeleteResultRequest().getResultRecord() != null && 
                body.getDeleteResultRequest().getResultRecord().getSourcedGUID() != null) {
                info.setSourcedId(body.getDeleteResultRequest().getResultRecord().getSourcedGUID().getSourcedId());
            }
        } else if (body.getReadMembershipRequest() != null) {
            info.setOperationType(POXConstants.OPERATION_READ_MEMBERSHIP);
            info.setSourcedId(body.getReadMembershipRequest().getSourcedId());
        }
        
        return info;
    }

    public static class ValidationResult {
        private boolean valid = true;
        private List<String> errors = new ArrayList<>();
        
        public boolean isValid() {
            return valid && errors.isEmpty();
        }
        
        public void addError(String error) {
            valid = false;
            errors.add(error);
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }

    public static class OperationInfo {
        private String operationType;
        private String sourcedId;
        private String language;
        private String score;
        
        public String getOperationType() {
            return operationType;
        }
        
        public void setOperationType(String operationType) {
            this.operationType = operationType;
        }
        
        public String getSourcedId() {
            return sourcedId;
        }
        
        public void setSourcedId(String sourcedId) {
            this.sourcedId = sourcedId;
        }
        
        public String getLanguage() {
            return language;
        }
        
        public void setLanguage(String language) {
            this.language = language;
        }
        
        public String getScore() {
            return score;
        }
        
        public void setScore(String score) {
            this.score = score;
        }
    }
}
