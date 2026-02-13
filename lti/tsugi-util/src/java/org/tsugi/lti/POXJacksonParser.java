package org.tsugi.lti;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

import org.tsugi.lti.objects.*;
import org.tsugi.lti.POXConstants;
import org.tsugi.lti.POXResponseBuilder;
import org.tsugi.lti.POXResponseHelper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.stream.XMLInputFactory;

public class POXJacksonParser {
    
    private static final Logger log = LoggerFactory.getLogger(POXJacksonParser.class);
    
    /**
     * Shared thread-safe XmlMapper instance for POX operations.
     * Configured for both serialization and deserialization with XXE protection.
     * This mapper can be safely reused across all POX-related classes.
     */
    public static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        // Deserialization configuration
        XML_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        XML_MAPPER.setDefaultUseWrapper(false);
        
        // Serialization configuration (doesn't affect deserialization)
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        // Harden against XXE (XML External Entity) attacks
        XMLInputFactory xmlInputFactory = XML_MAPPER.getFactory().getXMLInputFactory();
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        xmlInputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
        XML_MAPPER.getFactory().setXMLInputFactory(xmlInputFactory);
    }

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
            POXEnvelopeRequest request = XML_MAPPER.readValue(xmlString.trim(), POXEnvelopeRequest.class);
            
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
            POXEnvelopeResponse response = XML_MAPPER.readValue(xmlString.trim(), POXEnvelopeResponse.class);
            
            if (response != null && response.getPoxHeader() == null && response.getPoxBody() == null) {
                log.warn("Parsed response but both header and body are null - likely invalid XML");
                return null;
            }
            
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
            return POXConstants.OPERATION_REPLACE_RESULT;
        } else if (body.getReadResultRequest() != null) {
            return POXConstants.OPERATION_READ_RESULT;
        } else if (body.getDeleteResultRequest() != null) {
            return POXConstants.OPERATION_DELETE_RESULT;
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
     * Get ResultRecord from the POX body.
     * Works with replaceResultRequest, readResultRequest, and deleteResultRequest.
     * 
     * @param body The POX request body
     * @return The ResultRecord, or null if not found
     */
    private static ResultRecord getResultRecord(POXRequestBody body) {
        if (body.getReplaceResultRequest() != null) {
            return body.getReplaceResultRequest().getResultRecord();
        } else if (body.getReadResultRequest() != null) {
            return body.getReadResultRequest().getResultRecord();
        } else if (body.getDeleteResultRequest() != null) {
            return body.getDeleteResultRequest().getResultRecord();
        }
        return null;
    }

    /**
     * Get sourcedId from the POX body.
     * Works with replaceResultRequest, readResultRequest, and deleteResultRequest.
     * 
     * @param request The parsed POX request
     * @return The sourcedId string, or null if not found
     */
    public static String getBodySourcedId(POXEnvelopeRequest request) {
        if (request == null || request.getPoxBody() == null) {
            return null;
        }
        
        POXRequestBody body = request.getPoxBody();
        ResultRecord resultRecord = getResultRecord(body);
        
        if (resultRecord != null && resultRecord.getSourcedGUID() != null) {
            return resultRecord.getSourcedGUID().getSourcedId();
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
        ResultRecord resultRecord = getResultRecord(body);
        
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
        ResultRecord resultRecord = getResultRecord(body);
        
        if (resultRecord != null && resultRecord.getResult() != null && 
            resultRecord.getResult().getResultScore() != null) {
            return resultRecord.getResult().getResultScore().getTextString();
        }
        
        return null;
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
     * @return POXEnvelopeResponse object
     */
    public static POXEnvelopeResponse createResponseObject(String description, String major, String severity, 
                                                          String messageId, String operation, Properties minorCodes) {
        return POXResponseBuilder.create()
            .withDescription(description)
            .withMajor(major)
            .withSeverity(severity)
            .withMessageId(messageId)
            .withOperation(operation)
            .withMinorCodes(minorCodes)
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
     * @return XML response string
     */
    public static String createResponseWithMinorFields(String description, String major, String severity, 
                                                     String messageId, String operation, List<POXCodeMinorField> minorFields) {
        return POXResponseHelper.createResponseWithMinorFields(description, major, severity, 
                messageId, operation, minorFields);
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
