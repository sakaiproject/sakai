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
				ret = ret && contentTypes.stream().anyMatch(ct -> item.getMimeType().toLowerCase().contains(ct.toLowerCase()));
			}
		}
		return ret;
	}
}
