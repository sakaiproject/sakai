
package org.tsugi.HACK;

import org.tsugi.basiclti.BasicLTIUtil;
import org.json.simple.JSONValue;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class HackMoodle {

	/* In the IMS Dynamic Registration spec, messages_supported is a (kind of weird) array of objects:
	 *
	 * {
	 *   "https://purl.imsglobal.org/spec/lti-platform-configuration": {
     *      "product_family_code": "ExampleLMS",
     *      "messages_supported": [
     *        {"type": "LtiResourceLinkRequest"},
     *        {"type": "LtiDeepLinkingRequest"}],
     *      "variables": ["CourseSection.timeFrame.end", "CourseSection.timeFrame.begin", "Context.id.history", "ResourceLink.id.history"]
	 *  }
	 *
	 * In Moodle (at least in 3.10) these come back as an array of strings.
	 *
	 * {
	 *    "https://purl.imsglobal.org/spec/lti-platform-configuration": {
     *      "product_family_code": "moodle",
     *      "version": "3.10.9+ (Build: 20220129)",
     *      "messages_supported": [
     *          "LtiResourceLinkRequest",
     *          "LtiDeepLinkingRequest"
     *      ],
     *    "variables": ["CourseSection.timeFrame.end", "CourseSection.timeFrame.begin", "Context.id.history", "ResourceLink.id.history"]
	 *  }
	 *
	 *  Usage:
	 *
     *  body = org.tsugi.HACK.HackMoodle.hackOpenIdConfiguration(body);
     *  openIDConfig = mapper.readValue(body, OpenIDProviderConfiguration.class);
	 */

	public static String hackOpenIdConfiguration(String body)
	{
		JSONObject jso = BasicLTIUtil.parseJSONObject(body);
		if ( jso == null ) return body;

		JSONObject pc = (JSONObject) jso.get("https://purl.imsglobal.org/spec/lti-platform-configuration");
		if ( pc == null ) return body;

		JSONArray messages_supported = (JSONArray) pc.get("messages_supported");
		JSONArray new_messages = new JSONArray();

		boolean changed = false;
		for (Object jo : messages_supported) {
			if ( jo instanceof String ) {
				JSONObject new_message = new JSONObject();
				new_message.put("type", (String) jo);
				new_messages.add(new_message);
				changed = true;
			} else { 
				new_messages.add(jo);
			}
		}

		if ( ! changed ) return body;

		pc.put("messages_supported", new_messages);
		jso.put("https://purl.imsglobal.org/spec/lti-platform-configuration", pc);

		return jso.toJSONString();
	}
			
}
