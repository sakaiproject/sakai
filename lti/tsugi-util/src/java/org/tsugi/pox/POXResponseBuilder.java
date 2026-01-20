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

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class POXResponseBuilder {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    private String description;
    private String major = IMSPOXRequestJackson.MAJOR_FAILURE;
    private String severity = IMSPOXRequestJackson.SEVERITY_ERROR;
    private String messageId;
    private String operation;
    private POXCodeMinor codeMinor;
    private String bodyContent;
    
    
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
    
  
    public POXResponseBuilder withBodyContent(String bodyContent) {
        this.bodyContent = bodyContent;
        return this;
    }
    
    public POXResponseBuilder asSuccess() {
        this.major = IMSPOXRequestJackson.MAJOR_SUCCESS;
        this.severity = IMSPOXRequestJackson.SEVERITY_STATUS;
        return this;
    }
    
    public POXResponseBuilder asFailure() {
        this.major = IMSPOXRequestJackson.MAJOR_FAILURE;
        this.severity = IMSPOXRequestJackson.SEVERITY_ERROR;
        return this;
    }
    
    public POXResponseBuilder asUnsupported() {
        this.major = IMSPOXRequestJackson.MAJOR_UNSUPPORTED;
        this.severity = IMSPOXRequestJackson.SEVERITY_ERROR;
        return this;
    }
    
    public POXResponseBuilder asProcessing() {
        this.major = IMSPOXRequestJackson.MAJOR_PROCESSING;
        this.severity = IMSPOXRequestJackson.SEVERITY_STATUS;
        return this;
    }
    
    private String cleanBodyContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        String cleanBody = content.trim();
        if (cleanBody.startsWith("<?xml")) {
            int pos = cleanBody.indexOf("<", 1);
            if (pos > 0) cleanBody = cleanBody.substring(pos);
        }
        return cleanBody;
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
        statusInfo.setMessageRefIdentifier(messageId);
        statusInfo.setOperationRefIdentifier(operation);
        statusInfo.setCodeMinor(codeMinor);
        
        headerInfo.setStatusInfo(statusInfo);
        header.setResponseHeaderInfo(headerInfo);
        response.setPoxHeader(header);
        
        POXResponseBody body = new POXResponseBody();
        String cleanBody = cleanBodyContent(bodyContent);
        if (!cleanBody.isEmpty()) {
            body.setRawContent(cleanBody);
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

    public static String createSuccessResponse(String description, String bodyContent, String messageId, String operation) {
        return POXResponseBuilder.create()
            .withDescription(description)
            .withBodyContent(bodyContent)
            .withMessageId(messageId)
            .withOperation(operation)
            .asSuccess()
            .buildAsXml();
    }

    public static String createFailureResponse(String description, Properties minorCodes, String messageId, String operation) {
        return POXResponseBuilder.create()
            .withDescription(description)
            .withMinorCodes(minorCodes)
            .withMessageId(messageId)
            .withOperation(operation)
            .asFailure()
            .buildAsXml();
    }
    
    public static String createUnsupportedResponse(String description, String messageId, String operation) {
        return POXResponseBuilder.create()
            .withDescription(description)
            .withMessageId(messageId)
            .withOperation(operation)
            .asUnsupported()
            .buildAsXml();
    }

    public static String createProcessingResponse(String description, String messageId, String operation) {
        return POXResponseBuilder.create()
            .withDescription(description)
            .withMessageId(messageId)
            .withOperation(operation)
            .asProcessing()
            .buildAsXml();
    }
}
