package org.sakaiproject.tool.assessment.facade;

import java.util.List;

import org.sakaiproject.tool.assessment.data.dao.assessment.FavoriteColChoices;

public interface FavoriteColChoicesFacadeQueriesAPI {

	public List getFavoriteColChoicesByAgent(String siteAgentId);
	public void saveOrUpdate(FavoriteColChoices list);
}
