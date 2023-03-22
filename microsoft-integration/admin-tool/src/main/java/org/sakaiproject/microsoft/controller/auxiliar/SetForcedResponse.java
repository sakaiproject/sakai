package org.sakaiproject.microsoft.controller.auxiliar;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SetForcedResponse {
	private Boolean status = true;
	private String error = "";
}
