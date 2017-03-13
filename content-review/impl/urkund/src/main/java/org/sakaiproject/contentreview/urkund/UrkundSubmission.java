/**********************************************************************************
 *
 * Copyright (c) 2017 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.contentreview.urkund;

import lombok.Getter;
import lombok.Setter;

import java.util.Base64;


public class UrkundSubmission {

	@Getter @Setter private String submitterEmail;
	@Getter @Setter private String filename;
	@Getter @Setter private String mimeType;
	@Getter @Setter private byte[] content;
	@Getter @Setter private String subject = "";
	@Getter @Setter private String message = "";
	@Getter @Setter private boolean anon = false;
	@Getter @Setter private String language = "en-US";

	public String getFilenameEncoded() {
		try {
			return new String(Base64.getEncoder().encode(filename.getBytes("UTF-8")));
		}catch(Exception e){}
		return "";
	}
}
