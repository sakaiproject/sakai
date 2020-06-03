package org.sakaiproject.tool.assessment.services.assessment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.crypto.Mac;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.assessment.data.dao.grading.SecureDeliveryData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SecureDeliveryModuleIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI.Phase;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI.PhaseStatus;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class SecureDeliveryProctorio implements SecureDeliveryModuleIfc {

	// Encode as ASCII when communicating with Proctorio API
	final private static String ENCODING = java.nio.charset.StandardCharsets.US_ASCII.toString();
	// The property added to a site to enable and override options
	final private static String SITE_PROPERTY = "proctorio";
	// The header set by Proctorio
	final private static String SESSION_PROPERTY = "x-proctorio";
	// Default options if institution does not set
	final private static String DEFAULT_OPTIONS = "recordvideo,linksonly";
	// These are the options pulled from Proctorio API documentation. May need to be updated as their API changes over time.
	final private static String[] VALID_OPTIONS = {
			"recordvideo", "recordaudio", "recordscreen", "recordwebtraffic", "recordroomstart", 
			"verifyvideo", "verifyaudio", "verifydesktop", "verifyidauto", "verifyidlive", "verifysignature",
			"fullscreenlenient", "fullscreenmoderate", "fullscreensevere", "clipboard", "notabs", "linksonly",
			"closetabs", "onescreen", "print", "downloads", "cache", "rightclick", "noreentry", "agentreentry", 
			"calculatorbasic", "calculatorsci", "whiteboard"
	};

	private static String proctorioKey;
	private static String proctorioSecret;
	private static String proctorioUrl;
	private static String proctorioEnabled;
	private Cache<String, String> urlCache;

	private UserDirectoryService userDirectoryService = ComponentManager.get(UserDirectoryService.class);
	private ServerConfigurationService serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
	private MemoryService memoryService = ComponentManager.get(MemoryService.class);
	private SiteService siteService = ComponentManager.get(SiteService.class);
	private SessionManager sessionManager = ComponentManager.get(SessionManager.class);


	@Override
	public boolean initialize() {
		proctorioKey = serverConfigurationService.getString("proctorio.key", null);
		proctorioSecret = serverConfigurationService.getString("proctorio.secret", null);
		proctorioUrl = serverConfigurationService.getString("proctorio.url", null);
		proctorioEnabled = serverConfigurationService.getString("proctorio.enabled", "always");
		
		// Init the caches to hold the URLs
		urlCache = memoryService.getCache("proctorio.urlCache");
		
		log.debug("Proctorio init: key={}", proctorioKey);

		return (proctorioKey != null) && (proctorioSecret != null) && (proctorioUrl != null);
	}

	@Override
	public boolean isEnabled() {
		if (!StringUtils.equals(proctorioEnabled, "always")) {
			// This will come back null if from an assessment URL
			String siteId = AgentFacade.getCurrentSiteId();
			return isSiteProctorioEnabled(siteId);
		}

		return true;
	}
	
	@Override
	public boolean isEnabled(Long assessmentId) {
		if (!StringUtils.equals(proctorioEnabled, "always")) {
			PublishedAssessmentService pubService = new PublishedAssessmentService();
			PublishedAssessmentFacade pub = pubService.getPublishedAssessment(assessmentId.toString());
			String siteId = pub.getOwnerSiteId();
			return isSiteProctorioEnabled(siteId);
		}

		return true;
	}

	private boolean isSiteProctorioEnabled(final String siteId) {
		try {
			Site site = siteService.getSite(siteId);
			String proctorioOptions = site.getProperties().getProperty(SITE_PROPERTY);
			return StringUtils.isNotBlank(proctorioOptions);
		} catch (IdUnusedException e) {
			// Ignore missing site
			log.warn("Proctorio could not find siteId={}", siteId);
		}

		return false;
	}

	@Override
	public String getModuleName(Locale locale) {
		return "Proctorio";
	}

	@Override
	public String getTitleDecoration(Locale locale) {
		return " (Proctorio required)	";
	}

	@Override
	public PhaseStatus validatePhase(Phase phase, PublishedAssessmentIfc assessment, HttpServletRequest request) {
		log.debug("validatePhase: {}", phase);
		switch (phase) {
			case ASSESSMENT_START:
				return SecureDeliveryServiceAPI.PhaseStatus.SUCCESS;
			default:
				return SecureDeliveryServiceAPI.PhaseStatus.SUCCESS;
		}
	}

	@Override
	public String getInitialHTMLFragment(HttpServletRequest request, Locale locale) {
		return ""; //"<strong>Proctorio initial HTML fragment</strong>";
	}

	@Override
	public String getHTMLFragment(PublishedAssessmentIfc assessment, HttpServletRequest request, Phase phase,
			PhaseStatus status, Locale locale) {
		
		switch (phase) {
			case ASSESSMENT_START:
				final String currentAgentId = userDirectoryService.getCurrentUser().getId();
				final String url = getAlternativeDeliveryUrl(assessment.getPublishedAssessmentId(), currentAgentId);
				log.debug("Proctorio: phase={}, agentId={}, url={}", phase, currentAgentId, url);

				return "<script> window.addEventListener(\"message\", function(event) {\n" + 
						"    if(event.origin === \"https://getproctorio.com\") {\n" + 
						"        // event.data.active should be true if Proctorio is running\n" + 
						"        console.log(\"Proctorio is running: \" + event.data.active)\n" + 
						"    } else {\n" +
						"        fetch('/samigo-app/jsf/delivery/stopTimerProgress.faces').then(data => { window.location.href='" + url + "' }); \n" +
						"    }\n" +
						"});" +
						" if (window.top.location.origin != 'https://getproctorio.com') fetch('/samigo-app/jsf/delivery/stopTimerProgress.faces').then(data => {window.top.location.replace('" + url + "')}); \n" +
						"try { window.top.postMessage([10, \"proctorio_status\"], \"https://getproctorio.com\"); } " +
						" catch(e) { jQuery('#takeAssessmentForm input.active').prop('disabled', true); fetch('/samigo-app/jsf/delivery/stopTimerProgress.faces').then(data => { window.location.href='" + url + "'; }); }" +
						" </script>";
			case ASSESSMENT_FINISH:
			case ASSESSMENT_REVIEW:
				return "";
		}

		return "<strong>Proctorio HTML</strong>";
	}

	@Override
	public boolean validateContext(Object context) {
		return true;
	}

	@Override
	public String encryptPassword(String password) {
		return "";
	}

	@Override
	public String decryptPassword(String password) {
		return "";
	}

	@Override
	public String getAlternativeDeliveryUrl (Long assessmentId, String uid) {
		// Check cache first
		final String cacheKey = "student-" + assessmentId + "-" + uid;
		final String cacheRet = urlCache.get(cacheKey);
		
		if (StringUtils.isNotBlank(cacheRet)) {
			return cacheRet;
		}

		String[] urls = getProctorioUrls(assessmentId, uid);
		urlCache.put(cacheKey, urls[0]);
		return urls[0];
	}
		
	@Override
	public String getInstructorReviewUrl (Long assessmentId, String studentId) {
		// Check cache first to avoid DB and API
		final String cacheKey = "instructor-" + assessmentId + "-" + studentId;
		final String cacheRet = urlCache.get(cacheKey);
		
		if (StringUtils.isNotBlank(cacheRet)) {
			return cacheRet;
		}

		String[] urls = getProctorioUrls(assessmentId, studentId);
		urlCache.put(cacheKey, urls[1]);
		return urls[1];
	}

	private String[] getProctorioUrls(final Long assessmentId, final String studentUid) {
		
		// First check database
		List<SecureDeliveryData> sds = PersistenceService.getInstance().getSecureDeliveryFacadeQueries().getUrlsForAssessmentAndUser(assessmentId, studentUid);
		if (sds != null) {
			for (SecureDeliveryData sd : sds) {
				return new String[] {sd.getStudentUrl(), sd.getInstructorUrl()};
			}
		}

		// Not in database, so build an API call up
		PublishedAssessmentService pubService = new PublishedAssessmentService();
		PublishedAssessmentFacade assessment = pubService.getPublishedAssessment(assessmentId.toString());
		
		// We need the user's full name to send over to Proctorio
		User user = null;
		try {
			user = userDirectoryService.getUser(studentUid);
		} catch (UserNotDefinedException e) {
			log.warn("ProctorIO secure delivery could not find user ({})", studentUid);
			return null;
		}

		// Lookup the site properties to see what settings are allowed
		final String siteId = assessment.getOwnerSiteId();
		String proctorioOptions = null;
		try {
			Site site = siteService.getSite(siteId);
			proctorioOptions = site.getProperties().getProperty(SITE_PROPERTY);
		} catch (IdUnusedException e1) {
			// Ignoring to use defaults instead
		}

		// No site override so we will use system-wide defaults
		if (StringUtils.isBlank(proctorioOptions)) {
			proctorioOptions = serverConfigurationService.getString("proctorio.options", DEFAULT_OPTIONS);
		}

		final String assessmentPath = serverConfigurationService.getServerUrl() + 
				"/samigo-app/servlet/Login?id=" + assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.ALIAS);
		
		try {
			String[] urls = buildURL(user.getId(), user.getDisplayName(), assessmentId, assessmentPath, proctorioOptions);

			// We expect two URLs: one for student and one for instructor
			if (urls != null && urls.length == 2) {
				// Persist to database via Hibernate
				PersistenceService.getInstance().getSecureDeliveryFacadeQueries().saveUrlsForAssessmentAndUser(assessmentId, user.getId(), urls[1], urls[0]);
	
				return urls;
			}
		} catch (IOException e) {
			log.warn("ProctorIO could not build the URL", e);
		}

		return null;
	}

	/*
	 * private static String[] UriRfc3986CharsToEscape = { "!", "*", "'", "(", ")"
	 * };
	 * 
	 * private static String EscapeUriDataStringRfc3986(String value) { // Start
	 * with RFC 2396 escaping by calling the .NET method to do the
	 * work.documentation; // If it does, the escaping we do that follows it will be
	 * a no-op since the string StringBuilder escaped = new
	 * StringBuilder(URLEncoder.encode(value,
	 * java.nio.charset.StandardCharsets.ISO_8859_1.toString())); // Upgrade the
	 * escaping to RFC 3986, if necessary. for (String s : UriRfc3986CharsToEscape)
	 * { escaped.Replace(s, Uri.HexEscape(s[0])); }
	 * 
	 * // Return the fully-RFC3986-escaped string. return escaped.toString(); }
	 */

	private static String toNormalizedString(Map<String, String> collection, List<String> excludedNames) {
		StringBuilder normalizedString = new StringBuilder();

		for (String key : collection.keySet()) {
			if (excludedNames != null && excludedNames.contains(key)) {
				continue;
			}
			
			String value = collection.getOrDefault(key, "");

			String encodedKey = null;
			String encodedValue = null;
			try {
				String decodedKey = UriUtils.decode(key, ENCODING);
				encodedKey = UriUtils.encode(decodedKey, ENCODING);
				
				String decodedValue = UriUtils.decode(value, ENCODING);
				encodedValue = UriUtils.encode(decodedValue, ENCODING);
			} catch (UnsupportedEncodingException e) {
				log.warn("Could not encode", e);
			}
			
			normalizedString.append("&").append(encodedKey != null ? encodedKey : key).append("=").append(encodedValue != null ? encodedValue : value);
		}

		return normalizedString.substring(1); // remove the leading ampersand
	}

	private String[] buildURL(String uid, String fullname, Long assessmentId, String launchUrl, String options) throws ClientProtocolException, IOException {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("launch_url", launchUrl);
        parameters.put("user_id", uid);
        parameters.put("oauth_consumer_key", proctorioKey);
        parameters.put("exam_start", "(.)*\\/samigo-app\\/servlet\\/Login.*");
        parameters.put("exam_take", "(.*)\\/samigo-app\\/jsf\\/delivery.*");
        parameters.put("exam_end", "(.*)confirmSubmit.*");
        parameters.put("exam_settings", validateOptions(options));
        parameters.put("fullname", fullname);
        parameters.put("exam_tag", assessmentId + "");
        parameters.put("oauth_signature_method", "HMAC-SHA1");
        parameters.put("oauth_version", "1.0");
        parameters.put("oauth_timestamp", (System.currentTimeMillis() / 1000) + "");
        parameters.put("oauth_nonce", (int)(Math.random() * 100000000) + "");
        
        String signature_base_string = "POST&" 
                    + UriUtils.encode(proctorioUrl, ENCODING) 
                    + "&" + UriUtils.encode(toNormalizedString(parameters, null), ENCODING);
        
        log.debug("Proctorio signature_base_string: {}", signature_base_string);

        final Mac mac = HmacUtils.getHmacSha1(proctorioSecret.getBytes());
        HmacUtils.updateHmac(mac, signature_base_string);
        byte[] hmac = mac.doFinal();
        //  BASE64 ENCODED
        String oauthSignature = Base64.getEncoder().encodeToString(hmac);
        //  Add the signature to the params list
        parameters.put("oauth_signature", oauthSignature);

        //  Build data parameters
        StringBuilder parameterBuilder = new StringBuilder();
        for (String paramKey : parameters.keySet()) {
            //  build it for HTTP transport
        	String value = parameters.getOrDefault(paramKey, "");
            parameterBuilder.append("&");
            parameterBuilder.append(paramKey);
            parameterBuilder.append("=");
            parameterBuilder.append(UriUtils.encode(value, ENCODING));
        }
        
        //  Convert to a single string
        String parameterString = parameterBuilder.toString();
        //  Slice off the leading ampersand
        parameterString = parameterString.substring(1);
        
        log.debug("Proctorio parameterString: {}", parameterString);
        
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(proctorioUrl);
        httpPost.setEntity(new StringEntity(parameterString));
        CloseableHttpResponse response = client.execute(httpPost);
        final int statusCode = response.getStatusLine().getStatusCode();
        final HttpEntity returnEntity = response.getEntity();
        final String r = EntityUtils.toString(returnEntity);
        log.debug("Proctorio return status={}, text={}", statusCode, r);
        
        // Good return now take the JSON and unsplit it
        if (statusCode == 200 && r.length() > 100) {
          try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(r);
            if (root.isArray()) {
              String studentUrl = root.get(0).asText();
              String instructorUrl = root.get(1).asText();

              if (StringUtils.isNoneBlank(studentUrl, instructorUrl)) {
                studentUrl = UriUtils.decode(studentUrl, ENCODING);
                instructorUrl = UriUtils.decode(instructorUrl, ENCODING);
              }

              log.debug("Proctorio studentUrl={}, instructorUrl={}", studentUrl, instructorUrl);
              return new String[] { studentUrl, instructorUrl };
            }
            else {
              log.warn("Proctorio JSON was not an array as expected={}", r);
            }
          }
          catch (Exception e) {
            log.warn("Proctorio error", e);
          }
        }
        else {
          log.warn("Proctorio statusCode={}, return={}", statusCode, r);
        }

        return null;
	}

	// Proctorio has a set of valid API options
	private String validateOptions(String options) {
		StringBuilder sb = new StringBuilder();

		String[] splitOptions = StringUtils.split(options, ",");
		for (String splitOption : splitOptions) {
			if (StringUtils.isBlank(splitOption)) continue;

			final String o = splitOption.trim().toLowerCase();
			if (Arrays.stream(VALID_OPTIONS).anyMatch(o::equals)) {
				if (sb.length() > 1) {
					sb.append(",");
				}
				sb.append(o);
			}
		}

		final String validatedOptions = sb.toString();
		if (!validatedOptions.equals(options)) {
			log.warn("Proctorio validated options from={} to={}", options, validatedOptions);
		}

		return validatedOptions;
	}

}
