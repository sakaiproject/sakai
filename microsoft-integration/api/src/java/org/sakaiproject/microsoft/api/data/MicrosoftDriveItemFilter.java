/**
 * Copyright (c) 2024 The Apereo Foundation
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
package org.sakaiproject.microsoft.api.data;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MicrosoftDriveItemFilter{
	public static final String VIDEO_CONTENT_TYPE = "video/";
	public static final String AUDIO_CONTENT_TYPE = "audio/";
	public static final String MICROSOFT_DOCUMENT_TYPE = "application/vnd.openxmlformats-officedocument.";
	public static final String DOCUMENT_EXCEL_TYPE = MICROSOFT_DOCUMENT_TYPE + "spreadsheetml.sheet";
	public static final String DOCUMENT_WORD_TYPE = MICROSOFT_DOCUMENT_TYPE + "wordprocessingml.document";
	public static final String DOCUMENT_POWERPOINT_TYPE = MICROSOFT_DOCUMENT_TYPE + "presentationml.presentation";
	
	private String id;
	private String name;
	@Singular private List<String> contentTypes;

	
	public boolean matches(MicrosoftDriveItem item) {
		boolean ret = false;
		if(item != null) {
			if(item.isFolder()) {
				return true;
			}
			ret = true;
			if(StringUtils.isNotBlank(id)) {
				ret = ret && item.getId().equalsIgnoreCase(id);
			}
			
			if(StringUtils.isNotBlank(name)) {
				ret = ret && item.getName().toLowerCase().contains(name.toLowerCase());
			}
			
			if(contentTypes != null && !contentTypes.isEmpty()) {
				ret = ret && contentTypes.stream().anyMatch(ct -> (item.getMimeType() != null) ? item.getMimeType().toLowerCase().startsWith(ct.toLowerCase()) : false);
			}
		}
		return ret;
	}
}
