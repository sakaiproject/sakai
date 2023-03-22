package org.sakaiproject.microsoft.api.persistence;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.microsoft.api.model.SiteSynchronization;
import org.sakaiproject.serialization.SerializableRepository;

public interface MicrosoftSiteSynchronizationRepository extends SerializableRepository<SiteSynchronization, String> {
	Optional<SiteSynchronization> findById(String id);
	Optional<SiteSynchronization> findBySiteTeam(String siteId, String teamId);
	List<SiteSynchronization> findBySite(String siteId);
	List<SiteSynchronization> findByTeam(String teamId);
	List<String> findBySiteIdList(List<String> siteIds);
	
	long countSiteSynchronizationsByTeamId(String teamId, boolean forced);
	
	Integer deleteSiteSynchronizationsById(List<String> ids);
}
