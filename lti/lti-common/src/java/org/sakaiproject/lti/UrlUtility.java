/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.basiclti;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;

/**
 * Utility class implementing the Base64 modified Base64 for URL variant. See:
 * <a href="http://en.wikipedia.org/wiki/Base64">http://en.wikipedia.org/wiki/
 * Base64</a>.
 */
@Slf4j
public class UrlUtility {
	private UrlUtility() {
		// non-instantiable, no setup needed
	}

	/**
	 * Encode a URL to be embedded as a query parameter.
	 * 
	 * @param url
	 *            Plaintext URL to encode
	 * @return Properly encoded String, ready to be embedded as a query parmeter
	 */
	public static String encodeUrl(String url) {
		return cleanUrl(rawEncodeUrl(url));
	}

	/**
	 * Decode a URL encoded by this class, typically from a query parameter.
	 * 
	 * @param encodedUrl
	 *            Encoded URL as processed by this class
	 * @return Decoded String, in its original form
	 */
	public static String decodeUrl(String encodedUrl) {
		return rawDecodeUrl(repairUrl(encodedUrl));
	}

	/**
	 * Utility method to do non-URL safe encoding. This method provides the base
	 * encoding, while cleanUrl scrubs it for URL safety. You will not generally
	 * be concerned with this method.
	 * 
	 * @param url
	 *            Plaintext URL to encode
	 * @return Encoded String, not guaranteed to be URL-safe
	 */
	public static String rawEncodeUrl(String url) {
		return new String(Base64.encodeBase64(url.getBytes()));
	}

	/**
	 * Utility method to do non-URL safe decoding. As with rawEncodeUrl, you
	 * will generally not be concerned with this method.
	 * 
	 * @param encodedUrl
	 *            Raw encoded URL to decode
	 * @return Decoded String URL
	 */
	public static String rawDecodeUrl(String encodedUrl) {
		return new String(Base64.decodeBase64(encodedUrl.getBytes()));
	}

	/**
	 * Clean an encoded string so it may be embedded as a query parameter.
	 * 
	 * @param encodedUrl
	 *            Encoded String to clean
	 * @return Processed URL that is safe to embed as a query parameter. It will
	 *         not contain any characters that will interfere with other
	 *         parameters.
	 */
	public static String cleanUrl(String encodedUrl) {
		return encodedUrl.replaceAll("\\+", "-").replaceAll("/", "_").replaceAll(
				"=", "");
	}

	/**
	 * Repair a URL cleaned by this class, so the raw encode/decode methods will
	 * operate.
	 * 
	 * @param cleanedUrl
	 *            Cleaned, encoded URL to repair
	 * @return Encoded URL, ready for native encoding and decoding.
	 * 
	 */
	public static String repairUrl(String cleanedUrl) {
		int padLength = (4 - (cleanedUrl.length() % 4)) % 4;
		String padding = "";
		for (int i = 0; i < padLength; i++)
			padding += "=";
		return cleanedUrl.replaceAll("-", "+").replaceAll("_", "/") + padding;
	}

	public static void main(String[] args) {
		String sample = "12345:/sites/foo/bar !@#$%^&*()_+|}{\":?><[]\';/.,'����������Э��ҹ������ό�ĩ����ɾֳ���ÍŽ";
		String encoded = encodeUrl(sample);
		String decoded = decodeUrl(encoded);
		boolean same = sample.equals(decoded);
		log.debug("encoded={}", encoded);
		log.debug("sample={}", sample);
		log.debug("decoded={}", decoded);
		log.debug("sample.equals(decoded)={}", same);
	}
}
