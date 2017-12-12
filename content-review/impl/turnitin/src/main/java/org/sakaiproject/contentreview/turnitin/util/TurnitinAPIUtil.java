/**
 * Copyright (c) 2003 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.contentreview.turnitin.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;

import org.azeckoski.reflectutils.transcoders.XMLTranscoder;

import org.w3c.dom.Document;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.exception.TransientSubmissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.util.Xml;

/**
 * This is a utility class for wrapping the physical https calls to the
 * Turn It In Service.
 * 
 * @author sgithens
 *
 */
@Slf4j
public class TurnitinAPIUtil {

	private static String encodeSakaiTitles(String assignTitle) {
		String assignEnc = assignTitle;
		try {
			if (assignTitle.contains("&")) {
				//log.debug("replacing & in assingment title");
				assignTitle = assignTitle.replace('&', 'n');
			}
			assignEnc = assignTitle;
			log.debug("Assign title is " + assignEnc);

		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return assignEnc;
	}

	private String encodeMultipartParam(String name, String value, String boundary) {
		return "--" + boundary + "\r\nContent-Disposition: form-data; name=\""
		+ name + "\"\r\n\r\n" + value + "\r\n";
	}

	private static HttpsURLConnection fetchConnection(String apiURL, int timeout, Proxy proxy)
	throws MalformedURLException, IOException, ProtocolException {
		HttpsURLConnection connection;
		URL hostURL = new URL(apiURL);
		if (proxy == null) {
			connection = (HttpsURLConnection) hostURL.openConnection();
		} else {
			connection = (HttpsURLConnection) hostURL.openConnection(proxy);
		}

		// This actually turns into a POST since we are writing to the
		// resource body. ( You can see this in Webscarab or some other HTTP
		// interceptor.
		connection.setRequestMethod("GET"); 
		connection.setConnectTimeout(timeout);
		connection.setReadTimeout(timeout);
		connection.setDoOutput(true);
		connection.setDoInput(true);

		return connection;
	}

	public static String getGMTime() {
		// calculate function2 data
		SimpleDateFormat dform = ((SimpleDateFormat) DateFormat
				.getDateInstance());
		dform.applyPattern("yyyyMMddHH");
		dform.setTimeZone(TimeZone.getTimeZone("GMT"));
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

		String gmtime = dform.format(cal.getTime());
		gmtime += Integer.toString(((int) Math.floor((double) cal
				.get(Calendar.MINUTE) / 10)));

		return gmtime;
	}

	@SuppressWarnings({ "unchecked" })
	public static Map packMap(Map map, Object... vargs) {
		if (map == null) {
			map = new HashMap();
		}
		if (vargs.length % 2 != 0) {
			throw new IllegalArgumentException("You need to supply an even number of vargs for the key-val pairs.");
		}
		for (int i = 0; i < vargs.length; i+=2) {
			map.put(vargs[i], vargs[i+1]);
		}
		return map;
	}

	public static void writeBytesToOutputStream(OutputStream outStream, String... vargs) throws UnsupportedEncodingException, IOException {
		for (String next: vargs) {
			outStream.write(next.getBytes("UTF-8"));
		}
	}

	public static String getMD5(String md5_string) throws NoSuchAlgorithmException {

		MessageDigest md = MessageDigest.getInstance("MD5");

		md.update(md5_string.getBytes());

		// convert the binary md5 hash into hex
		String md5 = "";
		byte[] b_arr = md.digest();

		for (int i = 0; i < b_arr.length; i++) {
			// convert the high nibble
			byte b = b_arr[i];
			b >>>= 4;
		b &= 0x0f; // this clears the top half of the byte
		md5 += Integer.toHexString(b);

		// convert the low nibble
		b = b_arr[i];
		b &= 0x0F;
		md5 += Integer.toHexString(b);
		}

		return md5;
	}

	public static Map callTurnitinReturnMap(String apiURL, Map<String,Object> parameters, 
			String secretKey, int timeout, Proxy proxy) throws TransientSubmissionException, SubmissionException 
	{
		XMLTranscoder xmlt = new XMLTranscoder();

		try (InputStream inputStream = callTurnitinReturnInputStream(apiURL, parameters, secretKey, timeout, proxy, false)) {
			Map togo = xmlt.decode(IOUtils.toString(inputStream));
			log.debug("Turnitin Result Payload: " + togo);
			return togo;
		} catch (Exception t) {
			// Could be 'java.lang.IllegalArgumentException: xml cannot be null or empty' from IO errors
			throw new TransientSubmissionException ("Cannot parse Turnitin response. Assuming call was unsuccessful", t);
		}
	}

	public static Document callTurnitinReturnDocument(String apiURL, Map<String,Object> parameters, 
			String secretKey, int timeout, Proxy proxy) throws TransientSubmissionException, SubmissionException {
		return callTurnitinReturnDocument(apiURL, parameters, secretKey, timeout, proxy, false);
	}
	
	public static String buildTurnitinURL(String apiURL, Map<String,Object> parameters, String secretKey) {
		if (!parameters.containsKey("fid")) {
			throw new IllegalArgumentException("You must to include a fid in the parameters");
		}
		
		StringBuilder apiDebugSB = new StringBuilder();
		if (log.isDebugEnabled()) {
			apiDebugSB.append("Starting URL TII Construction:\n");
		}
		
		parameters.put("gmtime", getGMTime());
		
		List<String> sortedkeys = new ArrayList<String>();
		sortedkeys.addAll(parameters.keySet());

		String md5 = buildTurnitinMD5(parameters, secretKey, sortedkeys);
		
		StringBuilder sb = new StringBuilder();
		sb.append(apiURL);
		if (log.isDebugEnabled()) {
			apiDebugSB.append("The TII Base URL is:\n");
			apiDebugSB.append(apiURL);
		}
		
		sb.append(sortedkeys.get(0));
		sb.append("=");
		sb.append(parameters.get(sortedkeys.get(0)));
		if (log.isDebugEnabled()) {
			apiDebugSB.append(sortedkeys.get(0));
			apiDebugSB.append("=");
			apiDebugSB.append(parameters.get(sortedkeys.get(0)));
			apiDebugSB.append("\n");
		}
		
		for (int i = 1; i < sortedkeys.size(); i++) {
			sb.append("&");
			sb.append(sortedkeys.get(i));
			sb.append("=");
			sb.append(parameters.get(sortedkeys.get(i)));
			if (log.isDebugEnabled()) {
				apiDebugSB.append(sortedkeys.get(i));
				apiDebugSB.append(" = ");
				apiDebugSB.append(parameters.get(sortedkeys.get(i)));
				apiDebugSB.append("\n");
			}
		}
		
		sb.append("&");
		sb.append("md5=");
		sb.append(md5);
		if (log.isDebugEnabled()) {
			apiDebugSB.append("md5 = ");
			apiDebugSB.append(md5);
			apiDebugSB.append("\n");
			log.debug(apiDebugSB.toString());
		}
		
		return sb.toString();
	}

	public static Document callTurnitinReturnDocument(String apiURL, Map<String,Object> parameters, 
			String secretKey, int timeout, Proxy proxy, boolean isMultipart) throws TransientSubmissionException, SubmissionException {
		InputStream inputStream = callTurnitinReturnInputStream(apiURL, parameters, secretKey, timeout, proxy, isMultipart);

		BufferedReader in;
		in = new BufferedReader(new InputStreamReader(inputStream));
		Document document = null;
		try {   
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder  parser = documentBuilderFactory.newDocumentBuilder();
			document = parser.parse(new org.xml.sax.InputSource(in));
		}
		catch (ParserConfigurationException pce){
			log.error("parser configuration error: " + pce.getMessage());
			throw new TransientSubmissionException ("Parser configuration error", pce);
		} catch (Exception t) {
			throw new TransientSubmissionException ("Cannot parse Turnitin response. Assuming call was unsuccessful", t);
		}
		
		if (log.isDebugEnabled()) {
			log.debug(" Result from call: " + Xml.writeDocumentToString(document));
		}

		return document;
	}

	public static InputStream callTurnitinReturnInputStream(String apiURL, Map<String,Object> parameters, 
			String secretKey, int timeout, Proxy proxy, boolean isMultipart) throws TransientSubmissionException, SubmissionException {
		InputStream togo = null;
		
		StringBuilder apiDebugSB = new StringBuilder();

		if (!parameters.containsKey("fid")) {
			throw new IllegalArgumentException("You must to include a fid in the parameters");
		}

		//if (!parameters.containsKey("gmttime")) {
		parameters.put("gmtime", getGMTime());
		//}

		
		/**
		 * Some debug logging
		 */
		if (log.isDebugEnabled()) {
			Set<Entry<String, Object>> ets = parameters.entrySet();
			Iterator<Entry<String, Object>> it = ets.iterator();
			while (it.hasNext()) {
				Entry<String, Object> entr = it.next();
				log.debug("Paramater entry: " + entr.getKey() + ": " + entr.getValue());
			}
		}
		
		List<String> sortedkeys = new ArrayList<String>();
		sortedkeys.addAll(parameters.keySet());

		String md5 = buildTurnitinMD5(parameters, secretKey, sortedkeys);

		HttpsURLConnection connection;
		String boundary = "";
		try {
			connection = fetchConnection(apiURL, timeout, proxy);
			connection.setHostnameVerifier(new HostnameVerifier() {
				
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});

			if (isMultipart) {
				Random rand = new Random();
				//make up a boundary that should be unique
				boundary = Long.toString(rand.nextLong(), 26)
				+ Long.toString(rand.nextLong(), 26)
				+ Long.toString(rand.nextLong(), 26);
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type","multipart/form-data; boundary=" + boundary);
			}

			log.debug("HTTPS Connection made to Turnitin");

			OutputStream outStream = connection.getOutputStream();

			if (isMultipart) {
				if (log.isDebugEnabled()) {
					apiDebugSB.append("Starting Multipart TII CALL:\n");
				}
				for (int i = 0; i < sortedkeys.size(); i++) {
					if (parameters.get(sortedkeys.get(i)) instanceof ContentResource) {
						ContentResource resource = (ContentResource) parameters.get(sortedkeys.get(i));
						outStream.write(("--" + boundary
								+ "\r\nContent-Disposition: form-data; name=\"pdata\"; filename=\""
								+ resource.getId() + "\"\r\n"
								+ "Content-Type: " + resource.getContentType()
								+ "\r\ncontent-transfer-encoding: binary" + "\r\n\r\n")
								.getBytes());
						//TODO this loads the doc into memory rather use the stream method
						byte[] content = resource.getContent();
						if (content == null) {
							throw new SubmissionException("zero length submission!");
						}
						outStream.write(content);
						outStream.write("\r\n".getBytes("UTF-8"));
						if (log.isDebugEnabled()) {
							apiDebugSB.append(sortedkeys.get(i));
							apiDebugSB.append(" = ContentHostingResource: ");
							apiDebugSB.append(resource.getId());
							apiDebugSB.append("\n");
						}
					}
					else {
						if (log.isDebugEnabled()) {
							apiDebugSB.append(sortedkeys.get(i));
							apiDebugSB.append(" = ");
							apiDebugSB.append(parameters.get(sortedkeys.get(i)).toString());
							apiDebugSB.append("\n");
						}
						outStream.write(encodeParam(sortedkeys.get(i),parameters.get(sortedkeys.get(i)).toString(), boundary).getBytes());
					}
				}
				outStream.write(encodeParam("md5",md5, boundary).getBytes());
				outStream.write(("--" + boundary + "--").getBytes());
				
				if (log.isDebugEnabled()) {
					apiDebugSB.append("md5 = ");
					apiDebugSB.append(md5);
					apiDebugSB.append("\n");
					log.debug(apiDebugSB.toString());
				}
			}
			else {
				writeBytesToOutputStream(outStream, sortedkeys.get(0),"=",
						parameters.get(sortedkeys.get(0)).toString());
				if (log.isDebugEnabled()) {
					apiDebugSB.append("Starting TII CALL:\n");
					apiDebugSB.append(sortedkeys.get(0));
					apiDebugSB.append(" = ");
					apiDebugSB.append(parameters.get(sortedkeys.get(0)).toString());
					apiDebugSB.append("\n");
				}

				for (int i = 1; i < sortedkeys.size(); i++) {
					writeBytesToOutputStream(outStream, "&", sortedkeys.get(i), "=", 
							parameters.get(sortedkeys.get(i)).toString());
					if (log.isDebugEnabled()) {
						apiDebugSB.append(sortedkeys.get(i));
						apiDebugSB.append(" = ");
						apiDebugSB.append(parameters.get(sortedkeys.get(i)).toString());
						apiDebugSB.append("\n");
					}
				}

				writeBytesToOutputStream(outStream, "&md5=", md5);
				if (log.isDebugEnabled()) {
					apiDebugSB.append("md5 = ");
					apiDebugSB.append(md5);
					log.debug(apiDebugSB.toString());
				}
			}

			outStream.close();

			togo = connection.getInputStream();
		}
		catch (IOException t) {
			log.error("IOException making turnitin call.", t);
			throw new TransientSubmissionException("IOException making turnitin call.", t);
		}
		catch (ServerOverloadException t) {
			throw new TransientSubmissionException("Unable to submit the content data from ContentHosting", t);
		}

		return togo;

	}

	private static String buildTurnitinMD5(Map<String, Object> parameters,
			String secretKey, List<String> sortedkeys)
			 {
		
		TIIFID fid = TIIFID.getFid(Integer.parseInt((String) parameters.get("fid")));
		Collections.sort(sortedkeys);

		StringBuilder md5sb = new StringBuilder();
		for (int i = 0; i < sortedkeys.size(); i++) {
			if (fid.includeParamInMD5(sortedkeys.get(i))) {
				md5sb.append(parameters.get(sortedkeys.get(i)));
			}
		}

		md5sb.append(secretKey);

		String md5;
		try{
			md5 = getMD5(md5sb.toString());
		} catch (NoSuchAlgorithmException t) {
			log.warn("MD5 error creating class on turnitin");
			throw new RuntimeException("Cannot generate MD5 hash for Turnitin API call", t);
		}
		return md5;
	}

	private static String encodeParam(String name, String value, String boundary) {
		return "--" + boundary + "\r\nContent-Disposition: form-data; name=\""
		+ name + "\"\r\n\r\n" + value + "\r\n";
	}

}
