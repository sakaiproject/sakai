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
 * Constants for common character encodings.
 */
public interface CharacterSet
{
	String ISO_8859_1 = "iso-8859-1";
	String US_ASCII = "us-ascii";
	String UTF_8 = "utf-8";
	String UTF_16 = "utf-16";
	String UTF_16BE = "utf-16BE";
	String UTF_16LE = "utf-16LE";

	String ISO_8859_15 = "iso-8859-15";
	String WINDOWS_1252 = "windows-1252";
}