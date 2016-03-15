/*
 * $URL$
 * $Id$
 *
 * Copyright (c) 2008-2016 Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.tsugi.basiclti;

import static org.tsugi.basiclti.BasicLTIConstants.LTI_MESSAGE_TYPE;
import static org.tsugi.basiclti.BasicLTIConstants.LTI_MESSAGE_TYPE_TOOLPROXYREGISTRATIONREQUEST;
import static org.tsugi.basiclti.BasicLTIConstants.LTI_MESSAGE_TYPE_TOOLPROXY_RE_REGISTRATIONREQUEST;
import static org.tsugi.basiclti.BasicLTIConstants.LTI_MESSAGE_TYPE_BASICLTILAUNCHREQUEST;
import static org.tsugi.basiclti.BasicLTIConstants.LTI_MESSAGE_TYPE_CONTENTITEMSELECTIONREQUEST;
import static org.tsugi.basiclti.BasicLTIConstants.LTI_VERSION;
import static org.tsugi.basiclti.BasicLTIConstants.LTI_VERSION_1;
import static org.tsugi.basiclti.BasicLTIConstants.LTI_VERSION_2;
import static org.tsugi.basiclti.BasicLTIConstants.CUSTOM_PREFIX;
import static org.tsugi.basiclti.BasicLTIConstants.EXTENSION_PREFIX;
import static org.tsugi.basiclti.BasicLTIConstants.LTI_MESSAGE_TYPE;
import static org.tsugi.basiclti.BasicLTIConstants.LTI_VERSION;
import static org.tsugi.basiclti.BasicLTIConstants.OAUTH_PREFIX;
import static org.tsugi.basiclti.BasicLTIConstants.TOOL_CONSUMER_INSTANCE_CONTACT_EMAIL;
import static org.tsugi.basiclti.BasicLTIConstants.TOOL_CONSUMER_INSTANCE_DESCRIPTION;
import static org.tsugi.basiclti.BasicLTIConstants.TOOL_CONSUMER_INSTANCE_GUID;
import static org.tsugi.basiclti.BasicLTIConstants.TOOL_CONSUMER_INSTANCE_NAME;
import static org.tsugi.basiclti.BasicLTIConstants.TOOL_CONSUMER_INSTANCE_URL;

import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import net.oauth.server.OAuthServlet;
import net.oauth.signature.OAuthSignatureMethod;

import java.net.HttpURLConnection;
import java.net.URL;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/* Leave out until we have JTidy 0.8 in the repository 
 import org.w3c.tidy.Tidy;
 import java.io.ByteArrayOutputStream;
 */

/**
 * Some Utility code for IMS LTI
 * http://www.anyexample.com/programming/java
 * /java_simple_class_to_compute_sha_1_hash.xml
 * <p>
 * Sample Descriptor
 * 
 * <pre>
 * &lt;?xml&nbsp;version=&quot;1.0&quot;&nbsp;encoding=&quot;UTF-8&quot;?&gt;
 * &lt;basic_lti_link&nbsp;xmlns=&quot;http://www.imsglobal.org/xsd/imsbasiclti_v1p0&quot;&nbsp;xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot;&gt;
 *   &lt;title&gt;generated&nbsp;by&nbsp;tp+user&lt;/title&gt;
 *   &lt;description&gt;generated&nbsp;by&nbsp;tp+user&lt;/description&gt;
 *   &lt;custom&gt;
 *	 &lt;parameter&nbsp;key=&quot;keyname&quot;&gt;value&lt;/parameter&gt;
 *   &lt;/custom&gt;
 *   &lt;extensions&nbsp;platform=&quot;www.lms.com&quot;&gt;
 *	 &lt;parameter&nbsp;key=&quot;keyname&quot;&gt;value&lt;/parameter&gt;
 *   &lt;/extensions&gt;
 *   &lt;launch_url&gt;url&nbsp;to&nbsp;the&nbsp;basiclti&nbsp;launch&nbsp;URL&lt;/launch_url&gt;
 *   &lt;secure_launch_url&gt;url&nbsp;to&nbsp;the&nbsp;basiclti&nbsp;launch&nbsp;URL&lt;/secure_launch_url&gt;
 *   &lt;icon&gt;url&nbsp;to&nbsp;an&nbsp;icon&nbsp;for&nbsp;this&nbsp;tool&nbsp;(optional)&lt;/icon&gt;
 *   &lt;secure_icon&gt;url&nbsp;to&nbsp;an&nbsp;icon&nbsp;for&nbsp;this&nbsp;tool&nbsp;(optional)&lt;/secure_icon&gt;
 *   &lt;cartridge_icon&nbsp;identifierref=&quot;BLTI001_Icon&quot;/&gt;
 *   &lt;vendor&gt;
 *	 &lt;code&gt;vendor.com&lt;/code&gt;
 *	 &lt;name&gt;Vendor&nbsp;Name&lt;/name&gt;
 *	 &lt;description&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;This&nbsp;is&nbsp;a&nbsp;Grade&nbsp;Book&nbsp;that&nbsp;supports&nbsp;many&nbsp;column&nbsp;types.
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/description&gt;
 *	 &lt;contact&gt;
 *	   &lt;email&gt;support@vendor.com&lt;/email&gt;
 *	 &lt;/contact&gt;
 *	 &lt;url&gt;http://www.vendor.com/product&lt;/url&gt;
 *   &lt;/vendor&gt;
 * &lt;/basic_lti_link&gt;
 * </pre>
 */
public class BasicLTIUtil {

	// We use the built-in Java logger because this code needs to be very generic
	private static Logger M_log = Logger.getLogger(BasicLTIUtil.class.toString());

	/** To turn on really verbose debugging */
	private static boolean verbosePrint = false;

	private static final Pattern CUSTOM_REGEX = Pattern.compile("[^A-Za-z0-9]");
	private static final String UNDERSCORE = "_";

	// Simple Debug Print Mechanism
	public static void dPrint(String str) {
		if (verbosePrint)
			System.out.println(str);
		M_log.fine(str);
	}

	// Returns true if this is a Basic LTI message with minimum values to meet the protocol
	public static boolean isRequest(HttpServletRequest request) {

		String message_type = request.getParameter(LTI_MESSAGE_TYPE);
		if ( message_type == null ) return false;
		if ( message_type.equals(LTI_MESSAGE_TYPE_BASICLTILAUNCHREQUEST) ||
		     message_type.equals(LTI_MESSAGE_TYPE_TOOLPROXYREGISTRATIONREQUEST) ||
		     message_type.equals(LTI_MESSAGE_TYPE_TOOLPROXY_RE_REGISTRATIONREQUEST) ||
		     message_type.equals(LTI_MESSAGE_TYPE_CONTENTITEMSELECTIONREQUEST) ) {
			// Seems plausible
		} else {
			return false;
		}

		String version = request.getParameter(LTI_VERSION);
		if ( version == null ) return true;
		if ( version.equals(LTI_VERSION_1) || version.equals(LTI_VERSION_2) ) {
			// Another pass
		} else {
			return false;
		}

		return true;
	}

	// expected_oauth_key can be null - if it is non-null it must match the key in the request
	public static Object validateMessage(HttpServletRequest request, String URL, 
		String oauth_secret, String expected_oauth_key)
	{
		OAuthMessage oam = OAuthServlet.getMessage(request, URL);
		String oauth_consumer_key = null;
		try {
			oauth_consumer_key = oam.getConsumerKey();
		} catch (Exception e) {
			return "Unable to find consumer key in message";
		}

		if ( expected_oauth_key != null && ! expected_oauth_key.equals(oauth_consumer_key) ) {
			M_log.warning("BasicLTIUtil.validateMessage Incorrect consumer key="+oauth_consumer_key+
				" expected key="+expected_oauth_key);
			return "Incorrect consumer key "+oauth_consumer_key;
		}

		OAuthValidator oav = new SimpleOAuthValidator();
		OAuthConsumer cons = new OAuthConsumer("about:blank#OAuth+CallBack+NotUsed", oauth_consumer_key,oauth_secret, null);

		OAuthAccessor acc = new OAuthAccessor(cons);

		String base_string = null;
		try {
			base_string = OAuthSignatureMethod.getBaseString(oam);
		} catch (Exception e) {
            return "Unable to find base string";
		}

		try {
			oav.validateMessage(oam, acc);
		} catch (Exception e) {
			if (base_string != null) {
				return "Failed to validate: "+e.getLocalizedMessage()+"\nBase String\n"+base_string;
			}
			return "Failed to validate: "+e.getLocalizedMessage();
		}
		return Boolean.TRUE;
	}

	public static String validateDescriptor(String descriptor) {
		if (descriptor == null)
			return null;
		if (descriptor.indexOf("<basic_lti_link") < 0)
			return null;

		Map<String, Object> tm = XMLMap.getFullMap(descriptor.trim());
		if (tm == null)
			return null;

		// We demand at least an endpoint
		String ltiSecureLaunch = XMLMap.getString(tm,
				"/basic_lti_link/secure_launch_url");
		// We demand at least an endpoint
		if (ltiSecureLaunch != null && ltiSecureLaunch.trim().length() > 0)
			return ltiSecureLaunch;
		String ltiLaunch = XMLMap.getString(tm, "/basic_lti_link/launch_url");
		if (ltiLaunch != null && ltiLaunch.trim().length() > 0)
			return ltiLaunch;
		return null;
	}

	/**
	 * A simple utility method which implements the specified semantics of custom
	 * properties.
	 * <p>
	 * i.e. The parameter names are mapped to lower case and any character that is
	 * neither a number nor letter in a parameter name is replaced with an
	 * "underscore".
	 * <p>
	 * e.g. Review:Chapter=1.2.56 would map to custom_review_chapter=1.2.56.
	 * 
	 * @param propertyName
	 * @return
	 */
	public static String adaptToCustomPropertyName(final String propertyName) {
		if (propertyName == null || "".equals(propertyName)) {
			throw new IllegalArgumentException("propertyName cannot be null");
		}
		String customName = propertyName.toLowerCase();
		customName = CUSTOM_REGEX.matcher(customName).replaceAll(UNDERSCORE);
		if (!customName.startsWith(CUSTOM_PREFIX)) {
			customName = CUSTOM_PREFIX + customName;
		}
		return customName;
	}

	/**
	 * Add the necessary fields and sign.
	 * 
	 * @deprecated See:
	 *	 {@link BasicLTIUtil#signProperties(Map, String, String, String, String, String, String, String, String, String)}
	 * 
	 * @param postProp
	 * @param url
	 * @param method
	 * @param oauth_consumer_key
	 * @param oauth_consumer_secret
	 * @param org_id
	 *		  See: {@link BasicLTIConstants#TOOL_CONSUMER_INSTANCE_GUID}
	 * @param org_desc
	 *		  See: {@link BasicLTIConstants#TOOL_CONSUMER_INSTANCE_DESCRIPTION}
	 * @param org_url
	 *		  See: {@link BasicLTIConstants#TOOL_CONSUMER_INSTANCE_URL}
	 * @param extra
	 * @return
	 */
	public static Properties signProperties(Properties postProp, String url,
			String method, String oauth_consumer_key, String oauth_consumer_secret,
			String org_id, String org_desc, String org_url, Map<String,String> extra) {
		final Map<String, String> signedMap = signProperties(
				convertToMap(postProp), url, method, oauth_consumer_key,
				oauth_consumer_secret, org_id, org_desc, org_url, null, null, extra);
		return convertToProperties(signedMap);
	}

	/**
	 * Add the necessary fields and sign.
	 * 
	 * @param postProp
	 * @param url
	 * @param method
	 * @param oauth_consumer_key
	 * @param oauth_consumer_secret
	 * @param tool_consumer_instance_guid
	 *		  See: {@link BasicLTIConstants#TOOL_CONSUMER_INSTANCE_GUID}
	 * @param tool_consumer_instance_description
	 *		  See: {@link BasicLTIConstants#TOOL_CONSUMER_INSTANCE_DESCRIPTION}
	 * @param tool_consumer_instance_url
	 *		  See: {@link BasicLTIConstants#TOOL_CONSUMER_INSTANCE_URL}
	 * @param tool_consumer_instance_name
	 *		  See: {@link BasicLTIConstants#TOOL_CONSUMER_INSTANCE_NAME}
	 * @param tool_consumer_instance_contact_email
	 *		  See:
	 *		  {@link BasicLTIConstants#TOOL_CONSUMER_INSTANCE_CONTACT_EMAIL}
	 * @param extra
	 * @return
	 */
	public static Map<String, String> signProperties(
			Map<String, String> postProp, String url, String method,
			String oauth_consumer_key, String oauth_consumer_secret,
			String tool_consumer_instance_guid,
			String tool_consumer_instance_description,
			String tool_consumer_instance_url, String tool_consumer_instance_name,
			String tool_consumer_instance_contact_email,
			Map<String, String> extra) {

		if ( postProp.get(LTI_VERSION) == null ) postProp.put(LTI_VERSION, "LTI-1p0");
		if ( postProp.get(LTI_MESSAGE_TYPE) == null ) postProp.put(LTI_MESSAGE_TYPE, "basic-lti-launch-request");

		if (tool_consumer_instance_guid != null)
			postProp.put(TOOL_CONSUMER_INSTANCE_GUID, tool_consumer_instance_guid);
		if (tool_consumer_instance_description != null)
			postProp.put(TOOL_CONSUMER_INSTANCE_DESCRIPTION,
					tool_consumer_instance_description);
		if (tool_consumer_instance_url != null)
			postProp.put(TOOL_CONSUMER_INSTANCE_URL, tool_consumer_instance_url);
		if (tool_consumer_instance_name != null)
			postProp.put(TOOL_CONSUMER_INSTANCE_NAME, tool_consumer_instance_name);
		if (tool_consumer_instance_contact_email != null)
			postProp.put(TOOL_CONSUMER_INSTANCE_CONTACT_EMAIL,
					tool_consumer_instance_contact_email);

		if (postProp.get("oauth_callback") == null)
			postProp.put("oauth_callback", "about:blank");

		if (oauth_consumer_key == null || oauth_consumer_secret == null) {
			dPrint("No signature generated in signProperties");
			return postProp;
		}

		OAuthMessage oam = new OAuthMessage(method, url, postProp.entrySet());
		OAuthConsumer cons = new OAuthConsumer("about:blank", oauth_consumer_key,
				oauth_consumer_secret, null);
		OAuthAccessor acc = new OAuthAccessor(cons);
		try {
			oam.addRequiredParameters(acc);
			String base_string = OAuthSignatureMethod.getBaseString(oam);
			M_log.fine("Base Message String\n"+base_string+"\n");
			if ( extra != null ) {
				extra.put("BaseString", base_string);
			}

			List<Map.Entry<String, String>> params = oam.getParameters();

			Map<String, String> nextProp = new HashMap<String, String>();
			// Convert to Map<String, String>
			for (final Map.Entry<String, String> entry : params) {
				nextProp.put(entry.getKey(), entry.getValue());
			}
			return nextProp;
		} catch (net.oauth.OAuthException e) {
			M_log.warning("BasicLTIUtil.signProperties OAuth Exception "
					+ e.getMessage());
			throw new Error(e);
		} catch (java.io.IOException e) {
			M_log.warning("BasicLTIUtil.signProperties IO Exception "
					+ e.getMessage());
			throw new Error(e);
		} catch (java.net.URISyntaxException e) {
			M_log.warning("BasicLTIUtil.signProperties URI Syntax Exception "
					+ e.getMessage());
			throw new Error(e);
		}

	}

	/**
	 * Check if the properties are properly signed
	 * 
	 * @deprecated See:
	 *			 {@link BasicLTIUtil#checkProperties(Map, String, String, String, String, String, String, String, String, String)}
	 * 
	 * @param postProp
	 * @param url
	 * @param method
	 * @param oauth_consumer_key
	 * @param oauth_consumer_secret
	 * @return
	 */
	public static boolean checkProperties(Properties postProp, String url,
			String method, String oauth_consumer_key, String oauth_consumer_secret) 
	{

		return checkProperties( convertToMap(postProp), url, method, 
				oauth_consumer_key, oauth_consumer_secret);
	}

	/**
	 * Check if the fields are properly signed
	 * 
	 * @param postProp
	 * @param url
	 * @param method
	 * @param oauth_consumer_key
	 * @param oauth_consumer_secret

	 * @return
	 */
	public static boolean checkProperties(
			Map<String, String> postProp, String url, String method,
			String oauth_consumer_key, String oauth_consumer_secret) {

		OAuthMessage oam = new OAuthMessage(method, url, postProp.entrySet());
		OAuthConsumer cons = new OAuthConsumer("about:blank", oauth_consumer_key,
				oauth_consumer_secret, null);
		OAuthValidator oav = new SimpleOAuthValidator();


		OAuthAccessor acc = new OAuthAccessor(cons);

		String base_string = null;
		try {
			base_string = OAuthSignatureMethod.getBaseString(oam);
		} catch (Exception e) {
			M_log.warning(e.getLocalizedMessage());
			base_string = null;
			return false;
		}

		try {
			oav.validateMessage(oam, acc);
		} catch (Exception e) {
			M_log.warning("Provider failed to validate message");
			M_log.warning(e.getLocalizedMessage());
			if (base_string != null) {
				M_log.warning(base_string);
			}
			return false;
		}
		return true;
	}

	/**
	 * Create the HTML to render a POST form and then automatically submit it.
	 * 
	 * @deprecated Moved to {@link #postLaunchHTML(Map, String, boolean)}
	 * @param cleanProperties
	 * @param endpoint
	 *		  The LTI launch url.
	 * @param launchtext
	 *		  The LTI launch text. Used if javascript is turned off.
	 * @param debug
	 *		  Useful for viewing the HTML before posting to end point.
	 * @param extra
	 * @return the HTML ready for IFRAME src = inclusion.
	 */
	public static String postLaunchHTML(final Properties cleanProperties,
			String endpoint, String launchtext, boolean debug, Map<String,String> extra) {
		Map<String, String> map = convertToMap(cleanProperties);
		return postLaunchHTML(map, endpoint, launchtext, debug, extra);
	}

	/**
	 * Create the HTML to render a POST form and then automatically submit it.
	 * 
	 * @deprecated Moved to {@link #postLaunchHTML(Map, String, boolean)}
	 * @param cleanProperties
	 * @param endpoint
	 *		  The LTI launch url.
	 * @param launchtext
	 *		  The LTI launch text. Used if javascript is turned off.
	 * @param autosubmit
	 *		  Whether or not we want the form autosubmitted
	 * @param debug
	 *		  Useful for viewing the HTML before posting to end point.
	 * @param extra
	 * @return the HTML ready for IFRAME src = inclusion.
	 */
	public static String postLaunchHTML(final Properties cleanProperties,
			String endpoint, String launchtext, boolean autosubmit, boolean debug, Map<String,String> extra) {
		Map<String, String> map = convertToMap(cleanProperties);
		return postLaunchHTML(map, endpoint, launchtext, autosubmit, debug, extra);
	}

	/**
	 * Create the HTML to render a POST form and then automatically submit it.
	 * 
	 * @param cleanProperties
	 * @param endpoint
	 *		  The LTI launch url.
	 * @param launchtext
	 *		  The LTI launch text. Used if javascript is turned off.
	 * @param debug
	 *		  Useful for viewing the HTML before posting to end point.
	 * @param extra
	 *		  Useful for viewing the HTML before posting to end point.
	 * @return the HTML ready for IFRAME src = inclusion.
	 */
	public static String postLaunchHTML(
			final Map<String, String> cleanProperties, String endpoint, 
			String launchtext, boolean debug, Map<String,String> extra) {
		// Assume autosubmit is true for backwards compatibility
		boolean autosubmit = true;
		return postLaunchHTML(cleanProperties, endpoint, launchtext, autosubmit, debug, extra);
	}
	/**
	 * Create the HTML to render a POST form and then automatically submit it.
	 * 
	 * @param cleanProperties
	 * @param endpoint
	 *		  The LTI launch url.
	 * @param launchtext
	 *		  The LTI launch text. Used if javascript is turned off.
	 * @param autosubmit
	 *		  Whether or not we want the form autosubmitted
	 * @param extra
	 *		  Useful for viewing the HTML before posting to end point.
	 * @return the HTML ready for IFRAME src = inclusion.
	 */
	public static String postLaunchHTML(
			final Map<String, String> cleanProperties, String endpoint, 
			String launchtext, boolean autosubmit, boolean debug, 
			Map<String,String> extra) {

		if (cleanProperties == null || cleanProperties.isEmpty()) {
			throw new IllegalArgumentException(
					"cleanProperties == null || cleanProperties.isEmpty()");
		}
		if (endpoint == null) {
			throw new IllegalArgumentException("endpoint == null");
		}
		Map<String, String> newMap = null;
		if (debug) {
			// sort the properties for readability
			newMap = new TreeMap<String, String>(cleanProperties);
		} else {
			newMap = cleanProperties;
		}
		StringBuilder text = new StringBuilder();
		// paint form
		String submit_uuid = UUID.randomUUID().toString().replace("-","_");
		text.append("<div id=\"ltiLaunchFormArea_");
		text.append(submit_uuid);
		text.append("\">\n");
		text.append("<form action=\"");
		text.append(endpoint);
		text.append("\" name=\"ltiLaunchForm\" id=\"ltiLaunchForm_"+submit_uuid+"\" method=\"post\" ");
		text.append(" encType=\"application/x-www-form-urlencoded\" accept-charset=\"utf-8\">\n");
		if ( debug ) {
		}
		for (Entry<String, String> entry : newMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (value == null)
				continue;
			// This will escape the contents pretty much - at least
			// we will be safe and not generate dangerous HTML
			key = htmlspecialchars(key);
			value = htmlspecialchars(value);
			text.append("<input type=\"hidden\" name=\"");
			text.append(key);
			text.append("\" value=\"");
			text.append(value);
			text.append("\"/>\n");
		}

		// Paint the submit button
		text.append("<input type=\"submit\" value=\"");
		text.append(htmlspecialchars(launchtext));
		text.append("\">\n");

		if ( debug ) {
			text.append(" <input type=\"Submit\" value=\"Show Launch Data\" onclick=\"document.getElementById('ltiLaunchDebug_");
			text.append(submit_uuid);
			text.append("').style.display = 'block';return false;\">\n");
		}

		if ( extra != null ) {
			String button_html = extra.get("button_html");
			if ( button_html != null ) text.append(button_html);
		}

		text.append("</form>\n");
		text.append("</div>\n");

		// Paint the auto-pop up if we are transitioning from https: to http:
		// and are not already the top frame...
		text.append("<script type=\"text/javascript\">\n");
		text.append("if (window.top!=window.self) {\n");
		text.append("  theform = document.getElementById('ltiLaunchForm_");
		text.append(submit_uuid);
		text.append("');\n");
		text.append("  if ( theform && theform.action ) {\n");
		text.append("   formAction = theform.action;\n");
		text.append("   ourUrl = window.location.href;\n");
		text.append("   if ( formAction.indexOf('http://') == 0 && ourUrl.indexOf('https://') == 0 ) {\n");
		text.append("      theform.target = '_blank';\n");
		text.append("      window.console && console.log('Launching http from https in new window!');\n");
		text.append("    }\n");
		text.append("  }\n");
		text.append("}\n");
		text.append("</script>\n");

		// paint debug output
		if (debug) {
			text.append("<pre id=\"ltiLaunchDebug_");
			text.append(submit_uuid);
			text.append("\" style=\"display: none\">\n");
			text.append("<b>BasicLTI Endpoint</b>\n");
			text.append(endpoint);
			text.append("\n\n");
			text.append("<b>BasicLTI Parameters:</b>\n");
			for (Entry<String, String> entry : newMap.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (value == null)
					continue;
				text.append(htmlspecialchars(key));
				text.append("=");
				text.append(htmlspecialchars(value));
				text.append("\n");
			}
			text.append("</pre>\n");
			if ( extra != null ) {
				String base_string = extra.get("BaseString");
				if ( base_string != null ) {
					text.append("<!-- Base String\n");
					text.append(base_string.replaceAll("-->","__>"));
					text.append("\n-->\n");
				}
			}
		} else if ( autosubmit ) {
			// paint auto submit script
			text.append("<script language=\"javascript\"> \n");
			text.append("    document.getElementById('ltiLaunchFormArea_");
			text.append(submit_uuid);
			text.append("').style.display = \"none\";\n");
			text.append("    document.getElementById('ltiLaunchForm_");
			text.append(submit_uuid);
			text.append("').submit(); \n</script> \n");
		}

		String htmltext = text.toString();
		return htmltext;
	}

	/** 
         * getOAuthURL - Form a GET request signed by OAuth
	 * @param method
	 * @param url
	 * @param oauth_consumer_key
	 * @param oauth_consumer_secret
	 */
	public static String getOAuthURL(String method, String url, 
		String oauth_consumer_key, String oauth_secret)
	{
		return getOAuthURL(method, url, oauth_consumer_key, oauth_secret, null);
	}

	/** 
         * getOAuthURL - Form a GET request signed by OAuth
	 * @param method
	 * @param url
	 * @param oauth_consumer_key
	 * @param oauth_consumer_secret
	 * @param signature
	 */
	public static String getOAuthURL(String method, String url, 
		String oauth_consumer_key, String oauth_secret, String signature)
	{
		OAuthMessage om = new OAuthMessage(method, url, null);
		om.addParameter(OAuth.OAUTH_CONSUMER_KEY, oauth_consumer_key);
		if ( signature == null ) signature = OAuth.HMAC_SHA1;
		om.addParameter(OAuth.OAUTH_SIGNATURE_METHOD, signature);
		om.addParameter(OAuth.OAUTH_VERSION, "1.0");
		om.addParameter(OAuth.OAUTH_TIMESTAMP, new Long((new Date().getTime()) / 1000).toString());
		om.addParameter(OAuth.OAUTH_NONCE, UUID.randomUUID().toString());

		OAuthConsumer oc = new OAuthConsumer(null, oauth_consumer_key, oauth_secret, null);
		try {
		    OAuthSignatureMethod osm = OAuthSignatureMethod.newMethod(signature, new OAuthAccessor(oc));
		    osm.sign(om);
		    url = OAuth.addParameters(url, om.getParameters());
		    return url;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/** 
         * getOAuthURL - Form a GET request signed by OAuth
	 * @param method
	 * @param url
	 * @param oauth_consumer_key
	 * @param oauth_consumer_secret
	 * HttpURLConnection connection = sendOAuthURL('GET', url, oauth_consumer_key, oauth_secret)
	 * int responseCode = connection.getResponseCode();
	 * String data = readHttpResponse(connection)
	 */
	public static HttpURLConnection sendOAuthURL(String method, String url, String oauth_consumer_key, String oauth_secret)
	{
		String oauthURL = getOAuthURL(method, url, oauth_consumer_key, oauth_secret);

		try {
			URL urlConn = new URL(oauthURL);
			HttpURLConnection connection = (HttpURLConnection) urlConn.openConnection();
			connection.setRequestMethod(method);

			// Since Java won't send Content-length unless we really send 
			// content - send some data character so we don't 
			// send a broken PUT
			if ( ! "GET".equals(method) ) {
				connection.setDoOutput(true);
				OutputStreamWriter out = new OutputStreamWriter(
				connection.getOutputStream());
				out.write("42");
				out.close();
			}
			connection.connect();
			int responseCode = connection.getResponseCode();
			return connection;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/** 
         * getResponseCode - Read the HTTP Response
	 * @param connection
	 */
	public static int getResponseCode(HttpURLConnection connection)
	{
		try {
			return connection.getResponseCode();
		} catch(Exception e) {
			return HttpURLConnection.HTTP_INTERNAL_ERROR;
		}
	}


	/** 
         * readHttpResponse - Read the HTTP Response
	 * @param connection
	 */
	public static String readHttpResponse(HttpURLConnection connection)
	{
		try {
			BufferedReader in = new BufferedReader(
			new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			return response.toString();
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @deprecated See: {@link #parseDescriptor(Map, Map, String)}
	 * @param launch_info
	 *		  Variable is mutated by this method.
	 * @param postProp
	 *		  Variable is mutated by this method.
	 * @param descriptor
	 * @return
	 */
	public static boolean parseDescriptor(Properties launch_info,
			Properties postProp, String descriptor) {
		// this is an ugly copy/paste of the non-@deprecated method
		// could not convert data types as they variables get mutated (ugh)
		Map<String, Object> tm = null;
		try {
			tm = XMLMap.getFullMap(descriptor.trim());
		} catch (Exception e) {
			M_log.warning("BasicLTIUtil exception parsing BasicLTI descriptor: "
					+ e.getMessage());
			return false;
		}
		if (tm == null) {
			M_log.warning("Unable to parse XML in parseDescriptor");
			return false;
		}

		String launch_url = toNull(XMLMap.getString(tm,
					"/basic_lti_link/launch_url"));
		String secure_launch_url = toNull(XMLMap.getString(tm,
					"/basic_lti_link/secure_launch_url"));
		if (launch_url == null && secure_launch_url == null)
			return false;

		setProperty(launch_info, "launch_url", launch_url);
		setProperty(launch_info, "secure_launch_url", secure_launch_url);

		// Extensions for hand-authored placements - The export process should scrub
		// these
		setProperty(launch_info, "key", toNull(XMLMap.getString(tm,
						"/basic_lti_link/x-secure/launch_key")));
		setProperty(launch_info, "secret", toNull(XMLMap.getString(tm,
						"/basic_lti_link/x-secure/launch_secret")));

		List<Map<String, Object>> theList = XMLMap.getList(tm,
				"/basic_lti_link/custom/parameter");
		for (Map<String, Object> setting : theList) {
			dPrint("Setting=" + setting);
			String key = XMLMap.getString(setting, "/!key"); // Get the key attribute
			String value = XMLMap.getString(setting, "/"); // Get the value
			if (key == null || value == null)
				continue;
			key = "custom_" + mapKeyName(key);
			dPrint("key=" + key + " val=" + value);
			postProp.setProperty(key, value);
		}
		return true;
	}

	/**
	 * 
	 * @param launch_info
	 *		  Variable is mutated by this method.
	 * @param postProp
	 *		  Variable is mutated by this method.
	 * @param descriptor
	 * @return
	 */
	public static boolean parseDescriptor(Map<String, String> launch_info,
			Map<String, String> postProp, String descriptor) {
		Map<String, Object> tm = null;
		try {
			tm = XMLMap.getFullMap(descriptor.trim());
		} catch (Exception e) {
			M_log.warning("BasicLTIUtil exception parsing BasicLTI descriptor: "
					+ e.getMessage());
			return false;
		}
		if (tm == null) {
			M_log.warning("Unable to parse XML in parseDescriptor");
			return false;
		}

		String launch_url = toNull(XMLMap.getString(tm,
					"/basic_lti_link/launch_url"));
		String secure_launch_url = toNull(XMLMap.getString(tm,
					"/basic_lti_link/secure_launch_url"));
		if (launch_url == null && secure_launch_url == null)
			return false;

		setProperty(launch_info, "launch_url", launch_url);
		setProperty(launch_info, "secure_launch_url", secure_launch_url);

		// Extensions for hand-authored placements - The export process should scrub
		// these
		setProperty(launch_info, "key", toNull(XMLMap.getString(tm,
						"/basic_lti_link/x-secure/launch_key")));
		setProperty(launch_info, "secret", toNull(XMLMap.getString(tm,
						"/basic_lti_link/x-secure/launch_secret")));

		List<Map<String, Object>> theList = XMLMap.getList(tm,
				"/basic_lti_link/custom/parameter");
		for (Map<String, Object> setting : theList) {
			dPrint("Setting=" + setting);
			String key = XMLMap.getString(setting, "/!key"); // Get the key attribute
			String value = XMLMap.getString(setting, "/"); // Get the value
			if (key == null || value == null)
				continue;
			key = "custom_" + mapKeyName(key);
			dPrint("key=" + key + " val=" + value);
			postProp.put(key, value);
		}
		return true;
	}

	// Remove fields that should not be exported
	public static String prepareForExport(String descriptor) {
		Map<String, Object> tm = null;
		try {
			tm = XMLMap.getFullMap(descriptor.trim());
		} catch (Exception e) {
			M_log.warning("BasicLTIUtil exception parsing BasicLTI descriptor"
					+ e.getMessage());
			return null;
		}
		if (tm == null) {
			M_log.warning("Unable to parse XML in prepareForExport");
			return null;
		}
		XMLMap.removeSubMap(tm, "/basic_lti_link/x-secure");
		String retval = XMLMap.getXML(tm, true);
		return retval;
	}

	/**
	 * The parameter name is mapped to lower case and any character that is
	 * neither a number or letter is replaced with an "underscore". So if a custom
	 * entry was as follows:
	 * 
	 * <parameter name="Vendor:Chapter">1.2.56</parameter>
	 * 
	 * Would map to: custom_vendor_chapter=1.2.56
	 */
	public static String mapKeyName(String keyname) {
		StringBuffer sb = new StringBuffer();
		if (keyname == null)
			return null;
		keyname = keyname.trim();
		if (keyname.length() < 1)
			return null;
		for (int i = 0; i < keyname.length(); i++) {
			Character ch = Character.toLowerCase(keyname.charAt(i));
			if (Character.isLetter(ch) || Character.isDigit(ch)) {
				sb.append(ch);
			} else {
				sb.append('_');
			}
		}
		return sb.toString();
	}

	public static String toNull(String str) {
		if (str == null)
			return null;
		if (str.trim().length() < 1)
			return null;
		return str;
	}

	/**
	 * Mutates the passed Map<String, String> map variable. Puts the key,value
	 * into the Map if the value is not null and is not empty.
	 * 
	 * @param map
	 *		  Variable is mutated by this method.
	 * @param key
	 * @param value
	 */
	public static void setProperty(final Map<String, String> map,
			final String key, final String value) {
		if (value != null && !"".equals(value)) {
			map.put(key, value);
		}
	}

	/**
	 * Mutates the passed Properties props variable. Puts the key,value into the
	 * Map if the value is not null and is not empty.
	 * 
	 * @deprecated See: {@link #setProperty(Map, String, String)}
	 * @param props
	 *		  Variable is mutated by this method.
	 * @param key
	 * @param value
	 */
	public static void setProperty(Properties props, String key, String value) {
		if (value == null) return;
		if (value.trim().length() < 1) return;
		props.setProperty(key, value);
	}

	// Basic utility to encode form text - handle the "safe cases"
	public static String htmlspecialchars(String input) {
		if (input == null)
			return null;
		String retval = input.replace("&", "&amp;");
		retval = retval.replace("\"", "&quot;");
		retval = retval.replace("<", "&lt;");
		retval = retval.replace(">", "&gt;");
		retval = retval.replace(">", "&gt;");
		retval = retval.replace("=", "&#61;");
		return retval;
	}

	/**
	 * Simple utility method deal with a request that has the wrong URL when behind 
     * a proxy.
	 * 
	 * @param request
     * @param extUrl
     *   The url that the external world sees us as responding to.  This needs to be
     *   up to but not including the last slash like and not include any path information
     *   http://www.sakaiproject.org - although we do compensate for extra stuff at the end.
	 * @return
     *   The full path of the request with extUrl in place of whatever the request
     *   thinks is the current URL.
	 */
    static public String getRealPath(String servletUrl, String extUrl)
    {
        Pattern pat = Pattern.compile("^https??://[^/]*");
        // Deal with potential bad extUrl formats
        Matcher m = pat.matcher(extUrl);
        if (m.find()) {
            extUrl = m.group(0);
        }

        String retval = pat.matcher(servletUrl).replaceFirst(extUrl);
        return retval;
    }

	static public String getRealPath(HttpServletRequest request, String extUrl)
    {
        String URLstr = request.getRequestURL().toString();
        String retval = getRealPath(URLstr, extUrl);
        return retval;
    }

	/**
	 * Simple utility method to help with the migration from Properties to
	 * Map<String, String>.
	 * 
	 * @param properties
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
		public static Map<String, String> convertToMap(final Properties properties) {
			final Map<String, String> map = new HashMap(properties);
			return map;
		}

	/**
	 * Simple utility method to help with the migration from Map<String, String>
	 * to Properties.
	 * 
	 * @deprecated Should migrate to Map<String, String> signatures.
	 * @param map
	 * @return
	 */
	public static Properties convertToProperties(final Map<String, String> map) {
		final Properties properties = new Properties();
		if (map != null) {
			for (Entry<String, String> entry : map.entrySet()) {
				properties.setProperty(entry.getKey(), entry.getValue());
			}
		}
		return properties;
	}

	/**
	 * <p>
	 * Checks if a String is whitespace, empty ("") or null.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.isBlank(null)	  = true
	 * StringUtils.isBlank("")		= true
	 * StringUtils.isBlank(" ")	   = true
	 * StringUtils.isBlank("bob")	 = false
	 * StringUtils.isBlank("  bob  ") = false
	 * </pre>
	 * 
	 * @param str
	 *		  the String to check, may be null
	 * @return <code>true</code> if the String is null, empty or whitespace
	 * @since 2.0
	 */
	public static boolean isBlank(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((Character.isWhitespace(str.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * <p>
	 * Checks if a String is not empty (""), not null and not whitespace only.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.isNotBlank(null)	  = false
	 * StringUtils.isNotBlank("")		= false
	 * StringUtils.isNotBlank(" ")	   = false
	 * StringUtils.isNotBlank("bob")	 = true
	 * StringUtils.isNotBlank("  bob  ") = true
	 * </pre>
	 * 
	 * @param str
	 *		  the String to check, may be null
	 * @return <code>true</code> if the String is not empty and not null and not
	 *		 whitespace
	 * @since 2.0
	 */
	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}

	/**
	 * <p>
	 * Compares two Strings, returning <code>true</code> if they are equal.
	 * </p>
	 * 
	 * <p>
	 * <code>null</code>s are handled without exceptions. Two <code>null</code>
	 * references are considered to be equal. The comparison is case sensitive.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.equals(null, null)   = true
	 * StringUtils.equals(null, "abc")  = false
	 * StringUtils.equals("abc", null)  = false
	 * StringUtils.equals("abc", "abc") = true
	 * StringUtils.equals("abc", "ABC") = false
	 * </pre>
	 * 
	 * @see java.lang.String#equals(Object)
	 * @param str1
	 *		  the first String, may be null
	 * @param str2
	 *		  the second String, may be null
	 * @return <code>true</code> if the Strings are equal, case sensitive, or both
	 *		 <code>null</code>
	 */
	public static boolean equals(String str1, String str2) {
		return str1 == null ? str2 == null : str1.equals(str2);
	}

	/**
	 * <p>
	 * Compares two Strings, returning <code>true</code> if they are equal
	 * ignoring the case.
	 * </p>
	 * 
	 * <p>
	 * <code>null</code>s are handled without exceptions. Two <code>null</code>
	 * references are considered equal. Comparison is case insensitive.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.equalsIgnoreCase(null, null)   = true
	 * StringUtils.equalsIgnoreCase(null, "abc")  = false
	 * StringUtils.equalsIgnoreCase("abc", null)  = false
	 * StringUtils.equalsIgnoreCase("abc", "abc") = true
	 * StringUtils.equalsIgnoreCase("abc", "ABC") = true
	 * </pre>
	 * 
	 * @see java.lang.String#equalsIgnoreCase(String)
	 * @param str1
	 *		  the first String, may be null
	 * @param str2
	 *		  the second String, may be null
	 * @return <code>true</code> if the Strings are equal, case insensitive, or
	 *		 both <code>null</code>
	 */
	public static boolean equalsIgnoreCase(String str1, String str2) {
		return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
	}
}
