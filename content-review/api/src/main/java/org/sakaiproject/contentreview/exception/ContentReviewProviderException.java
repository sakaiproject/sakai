/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.contentreview.exception;

import lombok.Setter;

public class ContentReviewProviderException extends RuntimeException {

	private static final long serialVersionUID = -4280645805106323556L;

	@Setter
	private String i18nXml = null;

	public ContentReviewProviderException() {
		super();
	}

	public ContentReviewProviderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ContentReviewProviderException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContentReviewProviderException(String message) {
		super(message);
	}

	public ContentReviewProviderException(Throwable cause) {
		super(cause);
	}

	public ContentReviewProviderException(String message, String i18nXml) {
		super(message);
		this.i18nXml = i18nXml;
	}

	public ContentReviewProviderException(String message, String i18nXml, Throwable cause) {
		super(message, cause);
		this.i18nXml = i18nXml;
	}

	/**
	 * Returns the an xml representation of formatted messages if i18nXml is set; otherwise fallsback to getLocalizedMessage()
	 */
	public String getI18nXml() {
		return i18nXml == null ? getLocalizedMessage() : i18nXml;
	}

}
