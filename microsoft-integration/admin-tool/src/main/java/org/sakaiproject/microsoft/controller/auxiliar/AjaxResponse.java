package org.sakaiproject.microsoft.controller.auxiliar;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AjaxResponse {
	private Boolean status = true;
	private String error = "";
	private String body = "";
}
