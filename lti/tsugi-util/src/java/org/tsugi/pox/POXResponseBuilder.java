package org.tsugi.pox;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.tsugi.lti.objects.POXEnvelopeResponse;
import org.tsugi.lti.objects.POXResponseBody;
import org.tsugi.lti.objects.POXResponseHeader;
import org.tsugi.lti.objects.POXResponseHeaderInfo;
import org.tsugi.lti.objects.POXStatusInfo;
import org.tsugi.lti.objects.POXCodeMinor;
import org.tsugi.lti.objects.POXCodeMinorField;
import org.tsugi.lti.objects.ReadResultResponse;
import org.tsugi.lti.objects.ReplaceResultResponse;
import org.tsugi.lti.objects.DeleteResultResponse;
import org.tsugi.lti.POXJacksonParser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class POXResponseBuilder {
    
    // Reuse the shared thread-safe XmlMapper from POXJacksonParser
    // It's configured for both serialization and deserialization with XXE protection
    private static final XmlMapper XML_MAPPER = POXJacksonParser.XML_MAPPER;
    
    private String description;
    private String major = POXConstants.MAJOR_FAILURE;
    private String severity = POXConstants.SEVERITY_ERROR;
    private String messageId;
    private String messageRefIdentifier;
    private String operation;
    private POXCodeMinor codeMinor;
    private String bodyXml;
    private Object bodyObject;
    
    
    public static POXResponseBuilder create() {
        return new POXResponseBuilder();
    }
    
    public POXResponseBuilder withDescription(String description) {
        this.description = description;
        return this;
    }
    
    public POXResponseBuilder withMajor(String major) {
        this.major = major;
        return this;
    }
    
    public POXResponseBuilder withSeverity(String severity) {
        this.severity = severity;
        return this;
    }
    
   
    public POXResponseBuilder withMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }
    
    /**
     * Set the message reference identifier (references the original request message ID).
     * If not set, falls back to using the response messageId.
     * 
     * @param messageRefIdentifier The message reference identifier from the original request
     * @return this builder for method chaining
     */
    public POXResponseBuilder withMessageRefIdentifier(String messageRefIdentifier) {
        this.messageRefIdentifier = messageRefIdentifier;
        return this;
    }
    
    
    public POXResponseBuilder withOperation(String operation) {
        this.operation = operation;
        return this;
    }
    
    
    public POXResponseBuilder withMinorCodes(Properties minorCodes) {
        if (minorCodes != null && !minorCodes.isEmpty()) {
            List<POXCodeMinorField> fields = new ArrayList<>();
            for (Object key : minorCodes.keySet()) {
                String fieldName = (String) key;
                String fieldValue = minorCodes.getProperty(fieldName);
                if (fieldName != null && fieldValue != null) {
                    POXCodeMinorField field = new POXCodeMinorField();
                    field.setFieldName(fieldName);
                    field.setFieldValue(fieldValue);
                    fields.add(field);
                }
            }
            if (!fields.isEmpty()) {
                this.codeMinor = new POXCodeMinor();
                this.codeMinor.setCodeMinorFields(fields);
            }
        }
        return this;
    }
    
    
    public POXResponseBuilder withMinorCodes(List<POXCodeMinorField> minorFields) {
        if (minorFields != null && !minorFields.isEmpty()) {
            this.codeMinor = new POXCodeMinor();
            this.codeMinor.setCodeMinorFields(minorFields);
        }
        return this;
    }
    
    public POXResponseBuilder asSuccess() {
        this.major = POXConstants.MAJOR_SUCCESS;
        this.severity = POXConstants.SEVERITY_STATUS;
        return this;
    }
    
    public POXResponseBuilder asFailure() {
        this.major = POXConstants.MAJOR_FAILURE;
        this.severity = POXConstants.SEVERITY_ERROR;
        return this;
    }
    
    public POXResponseBuilder asUnsupported() {
        this.major = POXConstants.MAJOR_UNSUPPORTED;
        this.severity = POXConstants.SEVERITY_ERROR;
        return this;
    }
    
    public POXResponseBuilder asProcessing() {
        this.major = POXConstants.MAJOR_PROCESSING;
        this.severity = POXConstants.SEVERITY_STATUS;
        return this;
    }
    
    /**
     * Set body content from XML string
     * The XML should be the serialized response body (e.g., &lt;readResultResponse&gt;...&lt;/readResultResponse&gt;)
     * 
     * @param bodyXml The XML string containing the response body
     * @return this builder for method chaining
     */
    public POXResponseBuilder withBodyXml(String bodyXml) {
        this.bodyXml = bodyXml;
        return this;
    }
    
    /**
     * Set body content from response object directly
     * Accepts ReadResultResponse, ReplaceResultResponse, or DeleteResultResponse objects
     * 
     * @param bodyObject The response object (ReadResultResponse, ReplaceResultResponse, or DeleteResultResponse)
     * @return this builder for method chaining
     */
    public POXResponseBuilder withBodyObject(Object bodyObject) {
        this.bodyObject = bodyObject;
        return this;
    }
    
    public POXEnvelopeResponse build() {
        if (messageId == null) {
            messageId = String.valueOf(new Date().getTime());
        }
        
        POXEnvelopeResponse response = new POXEnvelopeResponse();
        
        POXResponseHeader header = new POXResponseHeader();
        POXResponseHeaderInfo headerInfo = new POXResponseHeaderInfo();
        headerInfo.setVersion("V1.0");
        headerInfo.setMessageIdentifier(messageId);
        
        POXStatusInfo statusInfo = new POXStatusInfo();
        statusInfo.setCodeMajor(major);
        statusInfo.setSeverity(severity);
        statusInfo.setDescription(description);
        // Use messageRefIdentifier if provided (references original request), otherwise fall back to messageId
        statusInfo.setMessageRefIdentifier(messageRefIdentifier != null ? messageRefIdentifier : messageId);
        statusInfo.setOperationRefIdentifier(operation);
        statusInfo.setCodeMinor(codeMinor);
        
        headerInfo.setStatusInfo(statusInfo);
        header.setResponseHeaderInfo(headerInfo);
        response.setPoxHeader(header);
        
        POXResponseBody body = new POXResponseBody();
        
        // Set body object directly if provided (preferred method)
        if (bodyObject != null) {
            if (bodyObject instanceof ReadResultResponse) {
                body.setReadResultResponse((ReadResultResponse) bodyObject);
            } else if (bodyObject instanceof ReplaceResultResponse) {
                body.setReplaceResultResponse((ReplaceResultResponse) bodyObject);
            } else if (bodyObject instanceof DeleteResultResponse) {
                body.setDeleteResultResponse((DeleteResultResponse) bodyObject);
            } else {
                log.warn("Unknown body object type: {}", bodyObject.getClass().getName());
            }
        } else if (bodyXml != null && !bodyXml.trim().isEmpty()) {
            // Parse body XML if provided - always try to parse based on XML content regardless of operation
            // This allows bodyXml to be included even when operation doesn't match known types
            try {
                String trimmedBodyXml = bodyXml.trim();
                // Parse based on XML content (preferred - more reliable)
                // Only parse if XML contains the expected root element to avoid creating empty objects
                if (trimmedBodyXml.contains("<readResultResponse")) {
                    ReadResultResponse readResponse = XML_MAPPER.readValue(trimmedBodyXml, ReadResultResponse.class);
                    body.setReadResultResponse(readResponse);
                } else if (trimmedBodyXml.contains("<replaceResultResponse")) {
                    ReplaceResultResponse replaceResponse = XML_MAPPER.readValue(trimmedBodyXml, ReplaceResultResponse.class);
                    body.setReplaceResultResponse(replaceResponse);
                } else if (trimmedBodyXml.contains("<deleteResultResponse")) {
                    DeleteResultResponse deleteResponse = XML_MAPPER.readValue(trimmedBodyXml, DeleteResultResponse.class);
                    body.setDeleteResultResponse(deleteResponse);
                } else {
                    log.debug("Body XML provided but doesn't match known response types. Body will be empty.");
                }
            } catch (Exception e) {
                log.warn("Failed to parse body XML, using empty body: {}", e.getMessage());
                // Continue with empty body if parsing fails
            }
        }
        
        response.setPoxBody(body);
        
        return response;
    }

    public String buildAsXml() {
        try {
            POXEnvelopeResponse response = build();
            return XML_MAPPER.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Error serializing POXEnvelopeResponse to XML", e);
            throw new RuntimeException("Failed to serialize POXEnvelopeResponse to XML", e);
        }
    }
}
