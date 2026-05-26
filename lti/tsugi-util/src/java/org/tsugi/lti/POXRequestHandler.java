package org.tsugi.lti;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.tsugi.lti.objects.POXEnvelopeRequest;
import org.tsugi.lti.objects.POXRequestBody;
import org.tsugi.lti.objects.POXCodeMinor;
import org.tsugi.lti.objects.POXCodeMinorField;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

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

    // Arrays maintained for backward compatibility with tests
    public final static String [] validMajor = POXConstants.VALID_MAJOR.toArray(new String[0]);
    public final static String [] validSeverity = POXConstants.VALID_SEVERITY.toArray(new String[0]);
    public final static String [] validMinor = POXConstants.VALID_MINOR.toArray(new String[0]);
    
    public static final long MAX_REQUEST_BODY_SIZE = 256L * 1024;

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

    // Reuse the shared thread-safe XmlMapper from POXJacksonParser
    private static final XmlMapper xmlMapper = POXJacksonParser.XML_MAPPER;

    public POXRequestHandler(HttpServletRequest request) {
        loadFromRequest(request);
    }

    public POXRequestHandler(String bodyString) {
        postBody = bodyString;
        parsePostBody();
    }

    public void loadFromRequest(HttpServletRequest request) {
        valid = false;
        errorMessage = null;
        operation = null;
        poxRequest = null;
        postBody = null;
        header = null;
        oauth_body_hash = null;
        oauth_consumer_key = null;
        oauth_signature_method = null;
        base_string = null;

        String contentType = request.getContentType();

        if (contentType == null || contentType.isEmpty()) {
            errorMessage = "Content Type is missing";
            log.info("{}", errorMessage);
            return;
        }

        if (!isXmlContentType(contentType)) {
            errorMessage = "Content Type must be application/xml or text/xml";
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
                String val;
                if ((val = extractOAuthParam(parm, "oauth_body_hash=")) != null) {
                    oauth_body_hash = val;
                } else if ((val = extractOAuthParam(parm, "oauth_consumer_key=")) != null) {
                    oauth_consumer_key = val;
                } else if ((val = extractOAuthParam(parm, "oauth_signature_method=")) != null) {
                    oauth_signature_method = val;
                }
            }
        }

        if (oauth_consumer_key == null) {
            errorMessage = "Did not find oauth_consumer_key";
            log.info("{}", errorMessage);
            return;
        }

        if (!"HMAC-SHA1".equals(oauth_signature_method) && !"HMAC-SHA256".equals(oauth_signature_method)) {
            errorMessage = "Unsupported oauth_signature_method";
            log.info("{}", errorMessage);
            return;
        }

        if (oauth_body_hash == null) {
            errorMessage = "Did not find oauth_body_hash";
            log.info("{}", errorMessage);
            log.debug("Authorization header: {}", header);
            return;
        }

        log.debug("OBH={}", oauth_body_hash);
        log.debug("OSM={}", oauth_signature_method);

        byte[] bodyBytes;
        final byte[] buffer = new byte[0x4000];
        try (InputStream in = request.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            long totalBytesRead = 0;
            int read;
            do {
                read = in.read(buffer, 0, buffer.length);
                if (read > 0) {
                    totalBytesRead += read;
                    if (totalBytesRead > MAX_REQUEST_BODY_SIZE) {
                        errorMessage = "Request body too large";
                        log.warn("Request body exceeds maximum size: {} bytes", totalBytesRead);
                        return;
                    }
                    out.write(buffer, 0, read);
                }
            } while (read >= 0);
            bodyBytes = out.toByteArray();
        } catch (Exception e) {
            errorMessage = "Could not read message body:" + e.getMessage();
            return;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            if ("HMAC-SHA256".equals(oauth_signature_method)) {
                md = MessageDigest.getInstance("SHA-256");
            }
            md.update(bodyBytes);
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

        postBody = new String(bodyBytes, StandardCharsets.UTF_8);
        
        parsePostBody();
    }

    public static boolean isXmlContentType(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return false;
        }

        int semicolonIndex = contentType.indexOf(';');
        String baseContentType = semicolonIndex >= 0
            ? contentType.substring(0, semicolonIndex)
            : contentType;
        baseContentType = baseContentType.trim().toLowerCase(Locale.ROOT);
        return "application/xml".equals(baseContentType) || "text/xml".equals(baseContentType);
    }

    /**
     * Extracts an OAuth parameter value from a header parameter string.
     * Handles both quoted values (e.g., oauth_body_hash="value") and unquoted values (e.g., oauth_body_hash=value).
     *
     * @param parm The parameter string to parse
     * @param prefix The prefix to match (e.g., "oauth_body_hash=")
     * @return The decoded parameter value, or null if the parameter doesn't match the prefix
     */
    private String extractOAuthParam(String parm, String prefix) {
        if (!parm.startsWith(prefix)) return null;
        String[] pieces = parm.split("\"");
        if (pieces.length > 1) {
            return URLDecoder.decode(pieces[1], StandardCharsets.UTF_8);
        }
        // Fallback: extract value after '=' and decode
        int eqIndex = parm.indexOf('=');
        if (eqIndex >= 0 && eqIndex < parm.length() - 1) {
            return URLDecoder.decode(parm.substring(eqIndex + 1), StandardCharsets.UTF_8);
        }
        return null;
    }

    public void parsePostBody() {
        // Reset mutable parser state to prevent prior invocation results from leaking
        valid = false;
        errorMessage = null;
        operation = null;
        poxRequest = null;
        
        if (postBody == null || postBody.trim().isEmpty()) {
            errorMessage = "Post body is null or empty";
            return;
        }
        
        try {
            poxRequest = xmlMapper.readValue(postBody, POXEnvelopeRequest.class);
            if (poxRequest == null) {
                errorMessage = "Could not parse POX request";
                return;
            }

            POXRequestBody body = poxRequest.getPoxBody();
            if (body != null) {
                if (body.getReplaceResultRequest() != null) {
                    operation = POXConstants.OPERATION_REPLACE_RESULT;
                } else if (body.getReadResultRequest() != null) {
                    operation = POXConstants.OPERATION_READ_RESULT;
                } else if (body.getDeleteResultRequest() != null) {
                    operation = POXConstants.OPERATION_DELETE_RESULT;
                }
            }

            if (operation == null) {
                errorMessage = "Could not find operation";
                return;
            }
            
            valid = true;
        } catch (Exception e) {
            errorMessage = "Could not parse XML: " + e.getMessage();
            log.debug("POX parsing error: {}", e.getMessage());
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

    public boolean isValid() {
        return valid;
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
        return getResponse(desc, POXConstants.MAJOR_UNSUPPORTED, null, null, null, null);
    }

    public String getResponseFailure(String desc, Properties minor) {
        return getResponse(desc, null, null, null, minor, null);
    }

    public String getResponseFailure(String desc, Properties minor, String bodyString) {
        return getResponse(desc, null, null, null, minor, bodyString);
    }

    /**
     * Generate a success response with optional body content.
     * 
     * @param desc The description message for the response
     * @param bodyString The XML string containing the response body. Must be valid POX response XML
     *                   (e.g., &lt;readResultResponse&gt;...&lt;/readResultResponse&gt;,
     *                   &lt;replaceResultResponse&gt;...&lt;/replaceResultResponse&gt;, or
     *                   &lt;deleteResultResponse&gt;...&lt;/deleteResultResponse&gt;).
     *                   The bodyXml will be parsed and included in the response regardless of the
     *                   operation value, as long as it matches one of the known response types.
     *                   If bodyString doesn't match known response types, it will be ignored.
     * @return XML response string
     */
    public String getResponseSuccess(String desc, String bodyString) {
        return getResponse(desc, POXConstants.MAJOR_SUCCESS, null, null, null, bodyString);
    }
    
    public String getResponseSuccess(String desc, Object bodyObject) {
        return getResponse(desc, POXConstants.MAJOR_SUCCESS, null, null, null, bodyObject);
    }

    /**
     * String overload that delegates to the Object overload.
     * This allows callers to pass String body content without explicit casting.
     */
    public String getResponse(String description, String major, String severity, 
            String messageId, Properties minor, String bodyString) {
        return getResponse(description, major, severity, messageId, minor, (Object) bodyString);
    }
    
    public String getResponse(String description, String major, String severity, 
            String messageId, Properties minor, Object body) {
        
        StringBuilder internalError = new StringBuilder();
        if (major == null) major = POXConstants.MAJOR_FAILURE;
        if (severity == null && POXConstants.MAJOR_PROCESSING.equals(major)) severity = POXConstants.SEVERITY_STATUS;
        if (severity == null && POXConstants.MAJOR_SUCCESS.equals(major)) severity = POXConstants.SEVERITY_STATUS;
        if (severity == null) severity = POXConstants.SEVERITY_ERROR;
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
            major = POXConstants.MAJOR_FAILURE;
        }
        if (!POXConstants.isValidSeverity(severity)) {
            if (internalError.length() > 0) internalError.append(", ");
            internalError.append("Invalid imsx_severity=").append(severity);
            severity = POXConstants.SEVERITY_ERROR;
        }

        if (internalError.length() > 0) {
            description = description + " (Internal error: " + internalError.toString() + ")";
            log.warn("Internal error: {}", internalError.toString());
        }

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
        
        // Set messageRefIdentifier to reference the original request message ID if available
        String requestMessageId = getHeaderMessageIdentifier();
        if (requestMessageId != null) {
            builder.withMessageRefIdentifier(requestMessageId);
        }
        
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

    public static boolean inArray(final String[] theArray, final String theString) {
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
