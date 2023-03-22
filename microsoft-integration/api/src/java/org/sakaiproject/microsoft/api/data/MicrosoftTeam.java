package org.sakaiproject.microsoft.api.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MicrosoftTeam {

	private String id;
	private String name;
	private String description;
	@Builder.Default
	private boolean fullyCreated = true;
}
