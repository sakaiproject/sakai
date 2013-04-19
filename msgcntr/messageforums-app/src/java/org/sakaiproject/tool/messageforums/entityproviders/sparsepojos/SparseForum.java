package org.sakaiproject.tool.messageforums.entityproviders.sparsepojos;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.entitybroker.DeveloperHelperService;

/**
 * A json friendly representation of a DiscussionForum. This one adds the topic list to the json.
 * 
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
public class SparseForum extends SparsestForum {

	@Getter @Setter
	private List<SparsestTopic> topics;
	
	public SparseForum(DiscussionForum fatForum, DeveloperHelperService dhs) {
		
		super(fatForum,dhs);
	}
}
