package org.sakaiproject.onedrive.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class OneDriveFile {

	public String mimeType;
}
