package org.sakaiproject.microsoft.controller.auxiliar;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AutoConfigError {
	private String siteId;
	private String siteTitle;
	private String errorMessage;
}
