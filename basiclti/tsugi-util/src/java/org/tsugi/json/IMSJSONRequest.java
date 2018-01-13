package org.tsugi.json;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import net.oauth.server.OAuthServlet;
import net.oauth.signature.OAuthSignatureMethod;

import org.tsugi.basiclti.Base64;
import org.json.simple.JSONValue;

@Slf4j
public class IMSJSONRequest {

	public final static String STATUS = "status";
	public final static String STATUS_CODE = "code";
	public final static String STATUS_DESCRIPTION = "description";

	public final static String CODE_MAJOR_SUCCESS = "success";
	public final static String CODE_MAJOR_FAILURE = "failure";
	public final static String CODE_MAJOR_UNSUPPORTED = "unsupported";

	public String postBody = null;
	private String header = null;
	private String oauth_body_hash = null;
	private String oauth_consumer_key = null;
	private String oauth_signature_method = null;

	public boolean valid = false;
	public String errorMessage = null;
	public String base_string = null;

	private static final String APPLICATION_JSON = "application/json";

	public String getOAuthConsumerKey()
	{
		return oauth_consumer_key;
	}

	public String getPostBody()
	{
		return postBody;
	}

	// Normal Constructor
	public IMSJSONRequest(String oauth_consumer_key, String oauth_secret, HttpServletRequest request) 
	{
		loadFromRequest(request);
		if ( ! valid ) return;
		validateRequest(oauth_consumer_key, oauth_secret, request);
	}

	// Constructor for delayed validation
	public IMSJSONRequest(HttpServletRequest request) 
	{
		loadFromRequest(request);
	}

	// Constructor for testing...
	public IMSJSONRequest(String bodyString)
	{
		postBody = bodyString;
	}

	// Load but do not check the authentication
	@SuppressWarnings("deprecation")
	public void loadFromRequest(HttpServletRequest request) 
	{
		header = request.getHeader("Authorization");
		log.debug("Header: {}", header);
		oauth_body_hash = null;
		oauth_signature_method = null;
		if ( header != null ) {
			if (header.startsWith("OAuth ")) header = header.substring(5);
			String [] parms = header.split(",");
			for ( String parm : parms ) {
				parm = parm.trim();
				if ( parm.startsWith("oauth_body_hash=") ) {
					String [] pieces = parm.split("\"");
					if ( pieces.length == 2 ) oauth_body_hash = URLDecoder.decode(pieces[1]);
				}
				if ( parm.startsWith("oauth_signature_method=") ) {
					String [] pieces = parm.split("\"");
					if ( pieces.length == 2 ) oauth_signature_method = URLDecoder.decode(pieces[1]);
				}
				if ( parm.startsWith("oauth_consumer_key=") ) {
					String [] pieces = parm.split("\"");
					if ( pieces.length == 2 ) oauth_consumer_key = URLDecoder.decode(pieces[1]);
				}
			}
		}		

		if ( oauth_body_hash == null ) {
			errorMessage = "Did not find oauth_body_hash";
			log.info("{}\n{}", errorMessage, header);
			return;
		}

		log.debug("OBH={}", oauth_body_hash);
		byte[] buf = new byte[1024];
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int chars = 0;
		try {
			ServletInputStream is = request.getInputStream();
			int readNum;
			do {
				readNum = is.read(buf);
				if (readNum>0) {
					bos.write(buf, 0, readNum); 
					chars = chars + readNum;
					// We dont' want a DOS - Also helps w.r.t. SHA1
					if ( chars > 10000000 ) {
						errorMessage = "Message body size exceeded";
						return;
					}
				}
			} while (readNum>=0);
		} catch(Exception e) {
			errorMessage = "Could not read message body:"+e.getMessage();
			return;
		}

		byte[] bytes = bos.toByteArray();

		try {
			postBody = new String(bytes, "UTF-8");
			MessageDigest md = null;
			if ( "HMAC-SHA256".equalsIgnoreCase(oauth_signature_method) ) {
				md = MessageDigest.getInstance("SHA-256");
			} else {
				md = MessageDigest.getInstance("SHA-1");
			}
			md.update(bytes); 
			byte[] output = Base64.encode(md.digest());
			String hash = new String(output);
			log.debug("HASH={} bytes={}", hash, bytes.length);
			if ( ! hash.equals(oauth_body_hash) ) {
				errorMessage = "Body hash does not match. bytes="+bytes.length;
				if ( oauth_signature_method != null ) errorMessage += " oauth_signature_method="+oauth_signature_method;
				log.debug(postBody);
				return;
			}
		} catch (Exception e) {
			errorMessage = "Could not compute body hash.  bytes="+bytes.length;
			if ( oauth_signature_method != null ) errorMessage += " oauth_signature_method="+oauth_signature_method;
			errorMessage += " Exception:" + e.getMessage();
			return;
		}
		valid = true;  // So far we are valid
	}

	// Assumes data is all loaded
	public void validateRequest(String oauth_consumer_key, String oauth_secret, HttpServletRequest request) 
	{
		validateRequest(oauth_consumer_key, oauth_secret, request, null) ;
	}

	public void validateRequest(String oauth_consumer_key, String oauth_secret, HttpServletRequest request, String URL) 
	{
		valid = false;
		OAuthMessage oam = OAuthServlet.getMessage(request, URL);
		OAuthValidator oav = new SimpleOAuthValidator();
		OAuthConsumer cons = new OAuthConsumer("about:blank#OAuth+CallBack+NotUsed", 
				oauth_consumer_key, oauth_secret, null);

		OAuthAccessor acc = new OAuthAccessor(cons);

		try {
			base_string = OAuthSignatureMethod.getBaseString(oam);
		} catch (Exception e) {
			base_string = null;
		}

		try {
			oav.validateMessage(oam,acc);
		} catch(Exception e) {
			errorMessage = "Launch fails OAuth validation: "+e.getMessage();
			return;
		}
		valid = true;
	}

	public boolean inArray(final String [] theArray, final String theString)
	{
		if ( theString == null ) return false;
		for ( String str : theArray ) {
			if ( theString.equals(str) ) return true;
		}
		return false;
	}

	public static Map<String, String> getStatusUnsupported(String desc)
	{
		return getStatus(desc, CODE_MAJOR_UNSUPPORTED);
	}

	public static Map<String, String> getStatusFailure(String desc)
	{
		return getStatus(desc, CODE_MAJOR_FAILURE);
	}

	public static Map<String, String> getStatusSuccess(String desc)
	{
		return getStatus(desc, CODE_MAJOR_SUCCESS);
	}

	public static Map<String, String> getStatus(String description, String major)
	{
		Map<String, String> retval = new LinkedHashMap<String, String>();
		retval.put(STATUS_CODE,major);
		retval.put(STATUS_DESCRIPTION,description);
		return retval;
	}

	/* IMS JSON version of Errors - does the complete request - returns the JSON in case
	   the code above us wants to log it. */
	@SuppressWarnings("static-access")
	public static String doErrorJSON(HttpServletRequest request,HttpServletResponse response, 
			IMSJSONRequest json, String message, Exception e) 
		throws java.io.IOException 
	{
		response.setContentType(APPLICATION_JSON);
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		Map<String, Object> jsonResponse = new TreeMap<String, Object>();

		Map<String, String> status = null;
		if ( json == null ) {
			status = IMSJSONRequest.getStatusFailure(message);
		} else {
			status = json.getStatusFailure(message);
			if ( json.base_string != null ) {
				jsonResponse.put("base_string", json.base_string);
			}
			if ( json.errorMessage != null ) {
				jsonResponse.put("error_message", json.errorMessage);
			}
		}
		jsonResponse.put(IMSJSONRequest.STATUS, status);
		if ( e != null ) {
			jsonResponse.put("exception", e.getLocalizedMessage());
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw, true);
				log.error("{}", pw);
				pw.flush();
				sw.flush();
				jsonResponse.put("traceback", sw.toString() );
			} catch ( Exception f ) {
				jsonResponse.put("traceback", f.getLocalizedMessage());
			}
		}
		String jsonText = JSONValue.toJSONString(jsonResponse);
		PrintWriter out = response.getWriter();
		out.println(jsonText);
		return jsonText;
	}

	/** Unit Tests */
	static final String inputTestData = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n" +  
		"<imsx_POXEnvelopeRequest xmlns = \"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" + 
		"<imsx_POXHeader>\n" + 
		"<imsx_POXRequestHeaderInfo>\n" + 
		"<imsx_version>V1.0</imsx_version>\n" + 
		"<imsx_messageIdentifier>999999123</imsx_messageIdentifier>\n" + 
		"</imsx_POXRequestHeaderInfo>\n" + 
		"</imsx_POXHeader>\n" + 
		"<imsx_POXBody>\n" + 
		"<replaceResultRequest>\n" + 
		"<resultRecord>\n" + 
		"<sourcedGUID>\n" + 
		"<sourcedId>3124567</sourcedId>\n" + 
		"</sourcedGUID>\n" + 
		"<result>\n" + 
		"<resultScore>\n" + 
		"<language>en-us</language>\n" + 
		"<textString>A</textString>\n" + 
		"</resultScore>\n" + 
		"</result>\n" + 
		"</resultRecord>\n" + 
		"</replaceResultRequest>\n" + 
		"</imsx_POXBody>\n" + 
		"</imsx_POXEnvelopeRequest>";

	public static void runTest() {
/*
		log.debug("Runnig test.");
		IMSJSONRequest pox = new IMSJSONRequest(inputTestData);
		log.debug("Version = {}", pox.getHeaderVersion());
		log.debug("Operation = {}", pox.getOperation());
		Map<String,String> bodyMap = pox.getBodyMap();
		String guid = bodyMap.get("/resultRecord/sourcedGUID/sourcedId");
		log.debug("guid={}", guid);
		String grade = bodyMap.get("/resultRecord/result/resultScore/textString");
		log.debug("grade={}", grade);

		String desc = "Message received and validated operation="+pox.getOperation()+
			" guid="+guid+" grade="+grade;

		String output = pox.getResponseUnsupported(desc);
		log.debug("---- Unsupported ----");
		log.debug(output);

		Properties props = new Properties();
		props.setProperty("fred","zap");
		props.setProperty("sam",IMSPOXRequest.MINOR_IDALLOC);
		log.debug("---- Generate logger Error ----");
		output = pox.getResponseFailure(desc,props);
		log.debug("---- Failure ----");
		log.debug(output);



		Map<String, Object> theMap = new TreeMap<String, Object> ();
		theMap.put("/readMembershipResponse/membershipRecord/sourcedId", "123course456");

		List<Map<String,String>> lm = new ArrayList<Map<String,String>>();
		Map<String,String> mm = new TreeMap<String,String>();
		mm.put("/personSourcedId","123user456");
		mm.put("/role/roleType","Learner");
		lm.add(mm);

		mm = new TreeMap<String,String>();
		mm.put("/personSourcedId","789user123");
		mm.put("/role/roleType","Instructor");
		lm.add(mm);
		theMap.put("/readMembershipResponse/membershipRecord/membership/member", lm);

		String theXml = XMLMap.getXMLFragment(theMap, true);
		log.debug("th={}", theXml);
		output = pox.getResponseSuccess(desc,theXml);
		log.debug("---- Success String ----");
		log.debug(output);
*/
	}

	/*

roleType:
Learner
Instructor
ContentDeveloper
Member
Manager
Mentor
Administrator
TeachingAssistant

fieldType:
Boolean
Integer
Real
String

<readMembershipResponse
xmlns="http://www.imsglobal.org/services/lis/mms2p0/wsdl11/sync/imsmms_v2p0">
<membershipRecord>
<sourcedId>GUID.TYPE</sourcedId>
<membership>
<collectionSourcedId>GUID.TYPE</collectionSourcedId>
<membershipIdType>MEMBERSHIPIDTYPE.TYPE</membershipIdType>
<member>
<personSourcedId>GUID.TYPE</personSourcedId>
<role>
<roleType>STRING</roleType>
<subRole>STRING</subRole>
<timeFrame>
<begin>DATETIME</begin>
<end>DATETIME</end>
<restrict>BOOLEAN</restrict>
<adminPeriod>
<language>LANGUAGESET.TYPE</language>
<textString>STRING</textString>
</adminPeriod>
</timeFrame>
<status>STATUS.TYPE</status>
<dateTime>DATETIME</dateTime>
<dataSource>GUID.TYPE</dataSource>
<recordInfo>
<extensionField>
<fieldName>STRING</fieldName>
<fieldType>FIELDTYPE.TYPE</fieldType>
<fieldValue>STRING</fieldValue>
</extensionField>
</recordInfo>
<extension>
<extensionField>
<fieldName>STRING</fieldName>
<fieldType>FIELDTYPE.TYPE</fieldType>
<fieldValue>STRING</fieldValue>
</extensionField>
</extension>
</role>
</member>
<creditHours>INTEGER</creditHours>
<dataSource>GUID.TYPE</dataSource>
</membership>
</membershipRecord>
</readMembershipResponse>
	 */
}
