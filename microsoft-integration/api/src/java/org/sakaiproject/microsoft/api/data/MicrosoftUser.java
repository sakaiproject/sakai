package org.sakaiproject.microsoft.api.data;

import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MicrosoftUser {
	public static final String OWNER = "owner";
	public static final String GUEST = "guest";

	private String id;
	private String name;
	private String email;
	/**
	 * Member Id in a Team or Channel. Only set by getTeamMembers, getChannelMembers, checkUserInTeam or checkUserInChannel
	 */
	private String memberId;
	
	@Builder.Default
	private boolean owner = false;
	
	@Builder.Default
	private boolean guest = false;
}
