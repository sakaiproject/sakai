package org.sakaiproject.microsoft.api.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MicrosoftAuthorizationAccount {

	private String homeAccountId;
	private String environment;
	private String username;
	private int hashCode;
}
