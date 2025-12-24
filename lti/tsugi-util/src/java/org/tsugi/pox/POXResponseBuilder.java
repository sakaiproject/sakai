package org.tsugi.pox;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.text.StringEscapeUtils;
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

@Slf4j
public class POXResponseBuilder {
    
    private static final XmlMapper xmlMapper = new XmlMapper();
    
    static {
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        xmlMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        xmlMapper.setDefaultUseWrapper(false);
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
        if (bodyContent != null && !bodyContent.trim().isEmpty()) {
            String cleanBody = bodyContent.trim();
            if (cleanBody.startsWith("<?xml")) {
                int pos = cleanBody.indexOf("<", 1);
                if (pos > 0) cleanBody = cleanBody.substring(pos);
            }
            body.setRawContent(cleanBody);
        }
        response.setPoxBody(body);
        
        return response;
    }

    public String buildAsXml() {
        POXEnvelopeResponse response = build();
        return buildManualXml(response);
    }
    
    private String buildManualXml(POXEnvelopeResponse response) {
        StringBuilder minorString = new StringBuilder();
        if (codeMinor != null && codeMinor.getCodeMinorFields() != null) {
            minorString.append("\n        <imsx_codeMinor>\n");
            for (POXCodeMinorField field : codeMinor.getCodeMinorFields()) {
                minorString.append("          <imsx_codeMinorField>\n");
                minorString.append("            <imsx_codeMinorFieldName>");
                minorString.append(StringEscapeUtils.escapeXml11(field.getFieldName()));
                minorString.append("</imsx_codeMinorFieldName>\n");
                minorString.append("            <imsx_codeMinorFieldValue>");
                minorString.append(StringEscapeUtils.escapeXml11(field.getFieldValue()));
                minorString.append("</imsx_codeMinorFieldValue>\n");
                minorString.append("          </imsx_codeMinorField>\n");
            }
            minorString.append("        </imsx_codeMinor>");
        }

        String bodyStr = "";
        if (bodyContent != null && !bodyContent.trim().isEmpty()) {
            String cleanBody = bodyContent.trim();
            if (cleanBody.startsWith("<?xml")) {
                int pos = cleanBody.indexOf("<", 1);
                if (pos > 0) cleanBody = cleanBody.substring(pos);
            }
            bodyStr = cleanBody;
        }
        String newLine = bodyStr.length() > 0 ? "\n" : "";

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
            bodyStr, newLine
        );
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
