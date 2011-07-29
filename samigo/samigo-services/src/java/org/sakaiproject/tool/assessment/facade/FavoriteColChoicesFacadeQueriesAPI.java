package org.sakaiproject.tool.assessment.facade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.dao.assessment.FavoriteColChoices;
//import org.sakaiproject.tool.assessment.data.dao.assessment.FavoriteColChoicesItem;

public interface FavoriteColChoicesFacadeQueriesAPI {

	public List getFavoriteColChoicesByAgent(String siteAgentId);
	public void saveOrUpdate(FavoriteColChoices list);
}
