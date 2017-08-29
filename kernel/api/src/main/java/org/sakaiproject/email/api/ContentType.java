/**
 * Copyright (c) 2003-2008 The Apereo Foundation
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
package org.sakaiproject.email.api;

/**
 * Common content types (primary/subtype) sent with message for use by client handling. Used when
 * sending email messages. The most commonly used are:
 * <p>
 * TEXT_PLAIN - for plain, unformated text only. Also consider {@link PlainTextFormat} when using
 * this.<br>
 * TEXT_HTML - for html formatted text
 * </p>
 */
public interface ContentType
{
	/**
	 * Plain message with no formatting
	 */
	String TEXT_PLAIN = "text/plain";

	/**
	 * Html formatted message
	 */
	String TEXT_HTML = "text/html";

	/**
	 * Richtext formatted message
	 */
	String TEXT_RICH = "text/richtext";
}