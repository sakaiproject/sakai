package org.tsugi.pox;

import java.io.Reader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.servlet.http.HttpServletRequest;

import org.tsugi.lti.Base64;
import org.tsugi.lti.POXJacksonParser;
import org.tsugi.lti.objects.POXEnvelopeRequest;
import org.tsugi.lti.objects.POXRequestBody;
import org.tsugi.lti.objects.POXCodeMinor;
import org.tsugi.lti.objects.POXCodeMinorField;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

import lombok.extern.slf4j.Slf4j;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import net.oauth.server.OAuthServlet;
import net.oauth.signature.OAuthSignatureMethod;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class POXRequestHandler {

    public final static String MAJOR_SUCCESS = "success";
    public final static String MAJOR_FAILURE = "failure";
    public final static String MAJOR_UNSUPPORTED = "unsupported";
    public final static String MAJOR_PROCESSING = "processing";

    public final static String [] validMajor = {
        MAJOR_SUCCESS, MAJOR_FAILURE, MAJOR_UNSUPPORTED, MAJOR_PROCESSING };

    public final static String SEVERITY_ERROR = "error";
    public final static String SEVERITY_WARNING = "warning";
    public final static String SEVERITY_STATUS = "status";

    public final static String [] validSeverity = {
        SEVERITY_ERROR, SEVERITY_WARNING, SEVERITY_STATUS };

    public final static String MINOR_FULLSUCCESS ="fullsuccess";
    public final static String MINOR_NOSOURCEDIDS = "nosourcedids";
    public final static String MINOR_IDALLOC = "idalloc";
    public final static String MINOR_OVERFLOWFAIL = "overflowfail";
    public final static String MINOR_IDALLOCINUSEFAIL = "idallocinusefail";
    public final static String MINOR_INVALIDDATAFAIL = "invaliddata";
    public final static String MINOR_INCOMPLETEDATA = "incompletedata";
    public final static String MINOR_PARTIALSTORAGE = "partialdatastorage";
    public final static String MINOR_UNKNOWNOBJECT = "unknownobject";
    public final static String MINOR_DELETEFAILURE = "deletefailure";
    public final static String MINOR_TARGETREADFAILURE = "targetreadfailure";
    public final static String MINOR_SAVEPOINTERROR = "savepointerror";
    public final static String MINOR_SAVEPOINTSYNCERROR = "savepointsyncerror";
    public final static String MINOR_UNKNOWNQUERY = "unknownquery";
    public final static String MINOR_UNKNOWNVOCAB = "unknownvocab";
    public final static String MINOR_TARGETISBUSY = "targetisbusy";
    public final static String MINOR_UNKNOWNEXTENSION = "unknownextension";
    public final static String MINOR_UNAUTHORIZEDREQUEST = "unauthorizedrequest";
    public final static String MINOR_LINKFAILURE = "linkfailure";
    public final static String MINOR_UNSUPPORTED = "unsupported";

    public final static String [] validMinor = {
        MINOR_FULLSUCCESS, MINOR_NOSOURCEDIDS, MINOR_IDALLOC, MINOR_OVERFLOWFAIL,
        MINOR_IDALLOCINUSEFAIL, MINOR_INVALIDDATAFAIL, MINOR_INCOMPLETEDATA,
        MINOR_PARTIALSTORAGE, MINOR_UNKNOWNOBJECT, MINOR_DELETEFAILURE,
        MINOR_TARGETREADFAILURE, MINOR_SAVEPOINTERROR, MINOR_SAVEPOINTSYNCERROR,
        MINOR_UNKNOWNQUERY, MINOR_UNKNOWNVOCAB, MINOR_TARGETISBUSY,
        MINOR_UNKNOWNEXTENSION, MINOR_UNAUTHORIZEDREQUEST, MINOR_LINKFAILURE,
        MINOR_UNSUPPORTED
    };

    private POXEnvelopeRequest poxRequest;
    private String postBody;
    private String header;
    private String oauth_body_hash;
    private String oauth_consumer_key;
    private String oauth_signature_method;
    public String base_string;

    public boolean valid = false;
    private String operation = null;
    public String errorMessage = null;

    private static final XmlMapper xmlMapper = new XmlMapper();

    static {
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        xmlMapper.setDefaultUseWrapper(false);
    }

    public POXRequestHandler(String oauth_consumer_key, String oauth_secret, HttpServletRequest request) {
        loadFromRequest(request);
        if (!valid) return;
        validateRequest(oauth_consumer_key, oauth_secret, request);
    }

    public POXRequestHandler(HttpServletRequest request) {
        loadFromRequest(request);
    }

    public POXRequestHandler(String bodyString) {
        postBody = bodyString;
        parsePostBody();
    }

    @SuppressWarnings("deprecation")
    public void loadFromRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        String baseContentType;

        try {
            MimeType mimeType = new MimeType(contentType);
            baseContentType = mimeType.getBaseType();
        } catch (MimeTypeParseException e) {
            errorMessage = "Unable to parse mime type";
            log.info("{}\n{}", errorMessage, contentType);
            return;
        }

        if (!"application/xml".equals(baseContentType)) {
            errorMessage = "Content Type must be application/xml";
            log.info("{}\n{}", errorMessage, contentType);
            return;
        }

        header = request.getHeader("Authorization");
        oauth_body_hash = null;
        if (header != null) {
            if (header.startsWith("OAuth ")) header = header.substring(6);
            String[] parms = header.split(",");
            for (String parm : parms) {
                parm = parm.trim();
                if (parm.startsWith("oauth_body_hash=")) {
                    String[] pieces = parm.split("\"");
                    if (pieces.length > 1) {
                        oauth_body_hash = URLDecoder.decode(pieces[1], StandardCharsets.UTF_8);
                    } else {
                        // Fallback: extract value after '=' and decode
                        int eqIndex = parm.indexOf('=');
                        if (eqIndex >= 0 && eqIndex < parm.length() - 1) {
                            oauth_body_hash = URLDecoder.decode(parm.substring(eqIndex + 1), StandardCharsets.UTF_8);
                        }
                    }
                }
                if (parm.startsWith("oauth_consumer_key=")) {
                    String[] pieces = parm.split("\"");
                    if (pieces.length > 1) {
                        oauth_consumer_key = URLDecoder.decode(pieces[1], StandardCharsets.UTF_8);
                    } else {
                        // Fallback: extract value after '=' and decode
                        int eqIndex = parm.indexOf('=');
                        if (eqIndex >= 0 && eqIndex < parm.length() - 1) {
                            oauth_consumer_key = URLDecoder.decode(parm.substring(eqIndex + 1), StandardCharsets.UTF_8);
                        }
                    }
                }
                if (parm.startsWith("oauth_signature_method=")) {
                    String[] pieces = parm.split("\"");
                    if (pieces.length > 1) {
                        oauth_signature_method = URLDecoder.decode(pieces[1], StandardCharsets.UTF_8);
                    } else {
                        // Fallback: extract value after '=' and decode
                        int eqIndex = parm.indexOf('=');
                        if (eqIndex >= 0 && eqIndex < parm.length() - 1) {
                            oauth_signature_method = URLDecoder.decode(parm.substring(eqIndex + 1), StandardCharsets.UTF_8);
                        }
                    }
                }
            }
        }

        if (oauth_body_hash == null) {
            errorMessage = "Did not find oauth_body_hash";
            log.info("{}\n{}", errorMessage, header);
            return;
        }

        log.debug("OBH={}", oauth_body_hash);
        log.debug("OSM={}", oauth_signature_method);
        
        final char[] buffer = new char[0x10000];
        try {
            StringBuilder out = new StringBuilder();
            Reader in = request.getReader();
            int read;
            do {
                read = in.read(buffer, 0, buffer.length);
                if (read > 0) {
                    out.append(buffer, 0, read);
                }
            } while (read >= 0);
            postBody = out.toString();
        } catch (Exception e) {
            errorMessage = "Could not read message body:" + e.getMessage();
            return;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            if ("HMAC-SHA256".equals(oauth_signature_method)) {
                md = MessageDigest.getInstance("SHA-256");
            }
            md.update(postBody.getBytes(StandardCharsets.UTF_8));
            byte[] output = Base64.encode(md.digest());
            String hash = new String(output);
            log.debug("HASH={}", hash);
            if (!hash.equals(oauth_body_hash)) {
                errorMessage = "Body hash does not match header";
                return;
            }
        } catch (Exception e) {
            errorMessage = "Could not compute body hash";
            return;
        }
        
        parsePostBody();
    }

    public void parsePostBody() {
        try {
            poxRequest = xmlMapper.readValue(postBody, POXEnvelopeRequest.class);
            if (poxRequest == null) {
                errorMessage = "Could not parse POX request";
                return;
            }

            POXRequestBody body = poxRequest.getPoxBody();
            if (body != null) {
                if (body.getReplaceResultRequest() != null) {
                    operation = "replaceResultRequest";
                } else if (body.getReadResultRequest() != null) {
                    operation = "readResultRequest";
                } else if (body.getDeleteResultRequest() != null) {
                    operation = "deleteResultRequest";
                } else if (body.getReadMembershipRequest() != null) {
                    operation = "readMembershipRequest";
                }
            }

            if (operation == null) {
                errorMessage = "Could not find operation";
                return;
            }
            
            valid = true;
        } catch (Exception e) {
            errorMessage = "Could not parse XML: " + e.getMessage();
            log.warn("POX parsing error", e);
        }
    }

    public void validateRequest(String oauth_consumer_key, String oauth_secret, HttpServletRequest request) {
        validateRequest(oauth_consumer_key, oauth_secret, request, null);
    }

    public void validateRequest(String oauth_consumer_key, String oauth_secret, HttpServletRequest request, String URL) {
        valid = false;
        OAuthMessage oam = OAuthServlet.getMessage(request, URL);
        OAuthValidator oav = new SimpleOAuthValidator();
        OAuthConsumer cons = new OAuthConsumer("about:blank#OAuth+CallBack+NotUsed", 
                oauth_consumer_key, oauth_secret, null);

        OAuthAccessor acc = new OAuthAccessor(cons);

        try {
            base_string = OAuthSignatureMethod.getBaseString(oam);
            log.debug("POX base_string={}", base_string);
        } catch (Exception e) {
            base_string = null;
        }

        try {
            oav.validateMessage(oam, acc);
        } catch (Exception e) {
            errorMessage = "Launch fails OAuth validation: " + e.getMessage();
            return;
        }
        valid = true;
    }

    public String getOperation() {
        return operation;
    }

    public String getOAuthConsumerKey() {
        return oauth_consumer_key;
    }

    public String getHeaderVersion() {
        if (poxRequest == null || poxRequest.getPoxHeader() == null || 
            poxRequest.getPoxHeader().getRequestHeaderInfo() == null) {
            return null;
        }
        return poxRequest.getPoxHeader().getRequestHeaderInfo().getVersion();
    }

    public String getHeaderMessageIdentifier() {
        if (poxRequest == null || poxRequest.getPoxHeader() == null || 
            poxRequest.getPoxHeader().getRequestHeaderInfo() == null) {
            return null;
        }
        return poxRequest.getPoxHeader().getRequestHeaderInfo().getMessageIdentifier();
    }

    public String getHeaderItem(String path) {
        if ("/imsx_version".equals(path)) {
            return getHeaderVersion();
        } else if ("/imsx_messageIdentifier".equals(path)) {
            return getHeaderMessageIdentifier();
        }
        return null;
    }

    public Map<String, String> getHeaderMap() {
        return POXJacksonParser.getHeaderInfo(poxRequest);
    }

    public String getPostBody() {
        return postBody;
    }

    public POXEnvelopeRequest getPoxRequest() {
        return poxRequest;
    }

    public String getResponseUnsupported(String desc) {
        return getResponse(desc, MAJOR_UNSUPPORTED, null, null, null, null);
    }

    public String getResponseFailure(String desc, Properties minor) {
        return getResponse(desc, null, null, null, minor, null);
    }

    public String getResponseFailure(String desc, Properties minor, String bodyString) {
        return getResponse(desc, null, null, null, minor, bodyString);
    }

    public String getResponseSuccess(String desc, String bodyString) {
        return getResponse(desc, MAJOR_SUCCESS, null, null, null, bodyString);
    }
    
    public String getResponseSuccess(String desc, Object bodyObject) {
        return getResponse(desc, MAJOR_SUCCESS, null, null, null, bodyObject);
    }

    public String getResponse(String description, String major, String severity, 
            String messageId, Properties minor, String bodyString) {
        return getResponse(description, major, severity, messageId, minor, (Object) bodyString);
    }
    
    public String getResponse(String description, String major, String severity, 
            String messageId, Properties minor, Object body) {
        
        StringBuffer internalError = new StringBuffer();
        if (major == null) major = MAJOR_FAILURE;
        if (severity == null && MAJOR_PROCESSING.equals(major)) severity = SEVERITY_STATUS;
        if (severity == null && MAJOR_SUCCESS.equals(major)) severity = SEVERITY_STATUS;
        if (severity == null) severity = SEVERITY_ERROR;
        if (messageId == null) {
            Date dt = new Date();
            messageId = "" + dt.getTime();
        }

        POXCodeMinor codeMinor = null;
        if (minor != null && minor.size() > 0) {
            List<POXCodeMinorField> minorFields = new ArrayList<>();
            for (Object okey : minor.keySet()) {
                String key = (String) okey;
                String value = minor.getProperty(key);
                if (key == null || value == null) continue;
                if (!POXConstants.isValidMinorCode(value)) {
                    if (internalError.length() > 0) internalError.append(", ");
                    internalError.append("Invalid imsx_codeMinorFieldValue=").append(value);
                    continue;
                }
                
                POXCodeMinorField field = new POXCodeMinorField();
                field.setFieldName(key);
                field.setFieldValue(value);
                minorFields.add(field);
            }
            if (!minorFields.isEmpty()) {
                codeMinor = new POXCodeMinor();
                codeMinor.setCodeMinorFields(minorFields);
            }
        }

        if (!POXConstants.isValidMajorCode(major)) {
            if (internalError.length() > 0) internalError.append(", ");
            internalError.append("Invalid imsx_codeMajor=").append(major);
        }
        if (!POXConstants.isValidSeverity(severity)) {
            if (internalError.length() > 0) internalError.append(", ");
            internalError.append("Invalid imsx_severity=").append(severity);
        }

        if (internalError.length() > 0) {
            description = description + " (Internal error: " + internalError.toString() + ")";
            log.warn(internalError.toString());
        }

        return createResponseXml(description, major, severity, messageId, operation, codeMinor, body);
    }

    private String createResponseXml(String description, String major, String severity, 
            String messageId, String operation, POXCodeMinor codeMinor, Object body) {
        
        return createFallbackResponse(description, major, severity, messageId, operation, codeMinor, body);
    }

    private String createFallbackResponse(String description, String major, String severity, 
            String messageId, String operation, POXCodeMinor codeMinor, Object body) {
        
        // Use POXResponseBuilder instead of hand-constructed XML
        POXResponseBuilder builder = POXResponseBuilder.create()
            .withDescription(description)
            .withMajor(major)
            .withSeverity(severity)
            .withMessageId(messageId)
            .withOperation(operation);
        
        // Add minor codes if present
        if (codeMinor != null && codeMinor.getCodeMinorFields() != null && !codeMinor.getCodeMinorFields().isEmpty()) {
            builder.withMinorCodes(codeMinor.getCodeMinorFields());
        }
        
        // Add body content if provided
        if (body != null) {
            if (body instanceof String) {
                String bodyString = (String) body;
                if (!bodyString.trim().isEmpty()) {
                    builder.withBodyXml(bodyString);
                }
            } else {
                // Pass the object directly
                builder.withBodyObject(body);
            }
        }
        
        return builder.buildAsXml();
    }

    public boolean inArray(final String[] theArray, final String theString) {
        if (theString == null) return false;
        for (String str : theArray) {
            if (theString.equals(str)) return true;
        }
        return false;
    }

    public static String getFatalResponse(String description) {
        return getFatalResponse(description, "unknown");
    }

    public static String getFatalResponse(String description, String operation) {
        Date dt = new Date();
        String messageId = "" + dt.getTime();

        return POXResponseBuilder.create()
            .withDescription(description)
            .asFailure()
            .withMessageId(messageId)
            .withOperation(operation)
            .buildAsXml();
    }
}