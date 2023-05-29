package org.sakaiproject.microsoft.api.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder=true)
public class MicrosoftRedirectURL {

	private String URL;
	private String state;
	private boolean auto;
}
