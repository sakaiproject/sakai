package org.tsugi.pox;

import java.util.List;
import java.util.Properties;

import org.tsugi.lti.objects.POXEnvelopeResponse;
import org.tsugi.lti.objects.POXCodeMinorField;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class POXResponseFactory {
    
    private static final XmlMapper xmlMapper = new XmlMapper();
    
    static {
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        xmlMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        xmlMapper.setDefaultUseWrapper(false);
    }
    
    /**
     * Create a success response
     * 
     * @param description The success description
     * @param messageId The message ID (optional, will generate if null)
     * @param operation The operation type
     * @return XML response string
     */
    public static String createSuccessResponse(String description, String messageId, String operation) {
        return POXResponseBuilder.create()
            .withDescription(description)
            .withMessageId(messageId)
            .withOperation(operation)
            .asSuccess()
            .buildAsXml();
    }
    
    public static String createSuccessResponse(String description, String operation) {
        return createSuccessResponse(description, null, operation);
    }
    
    /**
     * Create a failure response
     * 
     * @param description The failure description
     * @param minorCodes Minor error codes (optional)
     * @param messageId The message ID (optional, will generate if null)
     * @param operation The operation type
     * @return XML response string
     */
    public static String createFailureResponse(String description, Properties minorCodes, String messageId, String operation) {
        return POXResponseBuilder.create()
            .withDescription(description)
            .withMinorCodes(minorCodes)
            .withMessageId(messageId)
            .withOperation(operation)
            .asFailure()
            .buildAsXml();
    }
    
    public static String createFailureResponse(String description, Properties minorCodes, String operation) {
        return createFailureResponse(description, minorCodes, null, operation);
    }
    
    public static String createFailureResponse(String description, String operation) {
        return createFailureResponse(description, null, operation);
    }
    
    /**
     * Create an unsupported response
     * 
     * @param description The unsupported description
     * @param messageId The message ID (optional, will generate if null)
     * @param operation The operation type
     * @return XML response string
     */
    public static String createUnsupportedResponse(String description, String messageId, String operation) {
        return POXResponseBuilder.create()
            .withDescription(description)
            .withMessageId(messageId)
            .withOperation(operation)
            .asUnsupported()
            .buildAsXml();
    }
    
    public static String createUnsupportedResponse(String description, String operation) {
        return createUnsupportedResponse(description, null, operation);
    }
    
    /**
     * Create a processing response
     * 
     * @param description The processing description
     * @param messageId The message ID (optional, will generate if null)
     * @param operation The operation type
     * @return XML response string
     */
    public static String createProcessingResponse(String description, String messageId, String operation) {
        return POXResponseBuilder.create()
            .withDescription(description)
            .withMessageId(messageId)
            .withOperation(operation)
            .asProcessing()
            .buildAsXml();
    }
    
    public static String createProcessingResponse(String description, String operation) {
        return createProcessingResponse(description, null, operation);
    }
    
    /**
     * Create a custom response with full control over all parameters
     * 
     * @param description The response description
     * @param major The major code
     * @param severity The severity level
     * @param messageId The message ID
     * @param operation The operation type
     * @param minorCodes Minor error codes
     * @return XML response string
     */
    public static String createCustomResponse(String description, String major, String severity, 
            String messageId, String operation, Properties minorCodes) {
        return POXResponseBuilder.create()
            .withDescription(description)
            .withMajor(major)
            .withSeverity(severity)
            .withMessageId(messageId)
            .withOperation(operation)
            .withMinorCodes(minorCodes)
            .buildAsXml();
    }
    
    /**
     * Create a response from a POXEnvelopeResponse object
     * 
     * @param response The POX response object
     * @return XML response string
     */
    public static String createResponseFromObject(POXEnvelopeResponse response) {
        try {
            return xmlMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Error serializing POX response object to XML", e);
            throw new RuntimeException("Failed to serialize POX response object", e);
        }
    }
    
    /**
     * Create a response with minor codes from a list of field objects
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
        return POXResponseBuilder.create()
            .withDescription(description)
            .withMajor(major)
            .withSeverity(severity)
            .withMessageId(messageId)
            .withOperation(operation)
            .withMinorCodes(minorFields)
            .buildAsXml();
    }
    
    /**
     * Validate response parameters
     * 
     * @param major The major code
     * @param severity The severity level
     * @param minorCodes Minor codes (if any)
     * @return true if valid, false otherwise
     */
    public static boolean validateResponseParameters(String major, String severity, Properties minorCodes) {
        if (major != null && !isValidMajorCode(major)) {
            log.warn("Invalid major code: {}", major);
            return false;
        }
        
        if (severity != null && !isValidSeverity(severity)) {
            log.warn("Invalid severity: {}", severity);
            return false;
        }
        
        if (minorCodes != null) {
            for (Object key : minorCodes.keySet()) {
                String value = minorCodes.getProperty((String) key);
                if (value != null && !isValidMinorCode(value)) {
                    log.warn("Invalid minor code: {}", value);
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private static boolean isValidMajorCode(String major) {
        return POXConstants.isValidMajorCode(major);
    }

    private static boolean isValidSeverity(String severity) {
        return POXConstants.isValidSeverity(severity);
    }
    
    private static boolean isValidMinorCode(String minor) {
        return POXConstants.isValidMinorCode(minor);
    }
}

