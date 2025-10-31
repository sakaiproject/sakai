package org.tsugi.pox;

import java.util.Date;
import java.util.Properties;

import org.tsugi.lti.objects.POXEnvelopeResponse;
import org.tsugi.lti.objects.POXResponseBody;
import org.tsugi.lti.objects.POXResponseHeader;
import org.tsugi.lti.objects.POXResponseHeaderInfo;
import org.tsugi.lti.objects.POXStatusInfo;
import org.tsugi.lti.objects.POXCodeMinor;
import org.tsugi.lti.objects.POXCodeMinorField;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

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
     * @param bodyContent The response body content (optional)
     * @param messageId The message ID (optional, will generate if null)
     * @param operation The operation type
     * @return XML response string
     */
    public static String createSuccessResponse(String description, String bodyContent, String messageId, String operation) {
        return POXResponseBuilder.create()
            .withDescription(description)
            .withBodyContent(bodyContent)
            .withMessageId(messageId)
            .withOperation(operation)
            .asSuccess()
            .buildAsXml();
    }
    
    public static String createSuccessResponse(String description, String bodyContent, String operation) {
        return createSuccessResponse(description, bodyContent, null, operation);
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
     * Create a fatal error response
     * 
     * @param description The error description
     * @param messageId The message ID (optional, will generate if null)
     * @return XML response string
     */
    public static String createFatalResponse(String description, String messageId) {
        if (messageId == null) {
            messageId = String.valueOf(new Date().getTime());
        }
        
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
            messageId, 
            description,
            messageId
        );
    }
    
    public static String createFatalResponse(String description) {
        return createFatalResponse(description, null);
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
     * @param bodyContent The response body content
     * @return XML response string
     */
    public static String createCustomResponse(String description, String major, String severity, 
            String messageId, String operation, Properties minorCodes, String bodyContent) {
        return POXResponseBuilder.create()
            .withDescription(description)
            .withMajor(major)
            .withSeverity(severity)
            .withMessageId(messageId)
            .withOperation(operation)
            .withMinorCodes(minorCodes)
            .withBodyContent(bodyContent)
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
     * @param bodyContent The response body content
     * @return XML response string
     */
    public static String createResponseWithMinorFields(String description, String major, String severity, 
            String messageId, String operation, List<POXCodeMinorField> minorFields, String bodyContent) {
        return POXResponseBuilder.create()
            .withDescription(description)
            .withMajor(major)
            .withSeverity(severity)
            .withMessageId(messageId)
            .withOperation(operation)
            .withMinorCodes(minorFields)
            .withBodyContent(bodyContent)
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
        for (String validMajor : IMSPOXRequestJackson.validMajor) {
            if (validMajor.equals(major)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidSeverity(String severity) {
        for (String validSeverity : IMSPOXRequestJackson.validSeverity) {
            if (validSeverity.equals(severity)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isValidMinorCode(String minor) {
        for (String validMinor : IMSPOXRequestJackson.validMinor) {
            if (validMinor.equals(minor)) {
                return true;
            }
        }
        return false;
    }
}

