package org.sakaiproject.microsoft.api.persistence;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.microsoft.api.model.GroupSynchronization;
import org.sakaiproject.serialization.SerializableRepository;

public interface MicrosoftGroupSynchronizationRepository extends SerializableRepository<GroupSynchronization, String> {
	Optional<GroupSynchronization> findById(String id);
	Optional<GroupSynchronization> findByGroupChannel(String groupId, String channelId);
	List<GroupSynchronization> findBySiteSynchronizationId(String siteSynchronizationId);
	List<GroupSynchronization> findByGroup(String groupId);
	long countGroupSynchronizationsByChannelId(String channelId);
	
	Integer deleteBySiteSynchronizationId(String siteSynchronizationId);
}
