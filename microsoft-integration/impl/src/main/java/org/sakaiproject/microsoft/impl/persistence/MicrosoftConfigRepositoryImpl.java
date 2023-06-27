package org.sakaiproject.microsoft.impl.persistence;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.microsoft.api.data.MicrosoftCredentials;
import org.sakaiproject.microsoft.api.data.MicrosoftUserIdentifier;
import org.sakaiproject.microsoft.api.data.SakaiSiteFilter;
import org.sakaiproject.microsoft.api.data.SakaiUserIdentifier;
import org.sakaiproject.microsoft.api.model.MicrosoftConfigItem;
import org.sakaiproject.microsoft.api.persistence.MicrosoftConfigRepository;
import org.sakaiproject.serialization.BasicSerializableRepository;

public class MicrosoftConfigRepositoryImpl extends BasicSerializableRepository<MicrosoftConfigItem, String> implements MicrosoftConfigRepository {

	public Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}
	
	public Optional<MicrosoftConfigItem> getConfigItemByKey(String key){
		MicrosoftConfigItem ret = (MicrosoftConfigItem)startCriteriaQuery().add(Restrictions.eq("key", key)).uniqueResult();
		return Optional.ofNullable(ret);
	}
	
	public String getConfigItemValueByKey(String key){
		MicrosoftConfigItem ret = (MicrosoftConfigItem)startCriteriaQuery().add(Restrictions.eq("key", key)).uniqueResult();
		if(ret != null) {
			return ret.getValue(); 
		}
		return null;
	}
	
	//------------------------------ CREDENTIALS -------------------------------------------------------
	public MicrosoftCredentials getCredentials() {
		return MicrosoftCredentials.builder()
			.clientId(getConfigItemValueByKey(MicrosoftCredentials.KEY_CLIENT_ID))
			.authority(getConfigItemValueByKey(MicrosoftCredentials.KEY_AUTHORITY))
			.secret(getConfigItemValueByKey(MicrosoftCredentials.KEY_SECRET))
			.scope(getConfigItemValueByKey(MicrosoftCredentials.KEY_SCOPE))
			.delegatedScope(getConfigItemValueByKey(MicrosoftCredentials.KEY_DELEGATED_SCOPE))
			.email(getConfigItemValueByKey(MicrosoftCredentials.KEY_EMAIL))
		.build();
	}
	
	//------------------------------- SAKAI - MICROSOFT USER MAPPING ------------------------------------
	public SakaiUserIdentifier getMappedSakaiUserId() {
		return SakaiUserIdentifier.fromString(getConfigItemValueByKey(SakaiUserIdentifier.KEY));
	}
	
	public MicrosoftUserIdentifier getMappedMicrosoftUserId() {
		return MicrosoftUserIdentifier.fromString(getConfigItemValueByKey(MicrosoftUserIdentifier.KEY));
	}
	
	//------------------------------- MICROSOFT SYNCHRONIZATION ------------------------------------
	public Map<String, MicrosoftConfigItem> getDefaultSynchronizationConfigItems() {
		List<String> valid_keys = Arrays.asList(
				CREATE_TEAM,
				CREATE_CHANNEL,
				DELETE_SYNCH,
				DELETE_TEAM,
				DELETE_CHANNEL,
				ADD_USER_TO_TEAM,
				ADD_USER_TO_CHANNEL,
				REMOVE_USER_FROM_TEAM,
				REMOVE_USER_FROM_CHANNEL,
				REMOVE_USERS_WHEN_UNPUBLISH,
				CREATE_INVITATION
		);
		return IntStream.range(0, valid_keys.size())
			.mapToObj(index -> {
				String key = valid_keys.get(index);
				return MicrosoftConfigItem.builder()
						.key(key)
						.value(Boolean.FALSE.toString())
						.index(index)
						.build();
			}).collect(Collectors.toMap(MicrosoftConfigItem::getKey, Function.identity()));
	}
	
	public Map<String, MicrosoftConfigItem> getAllSynchronizationConfigItems() {
		Map<String, MicrosoftConfigItem> map = getDefaultSynchronizationConfigItems();
		
		List<MicrosoftConfigItem> list = (List<MicrosoftConfigItem>)startCriteriaQuery().add(Restrictions.like("key", PREFIX_SYNCH, MatchMode.START)).list();
		list.stream().forEach(item -> map.get(item.getKey()).setValue(item.getValue()));
		
		return map;
	}
	
	public Boolean isAllowedCreateTeam() {
		return Boolean.valueOf(getConfigItemValueByKey(CREATE_TEAM));
	}
	public Boolean isAllowedDeleteSynch() {
		return Boolean.valueOf(getConfigItemValueByKey(DELETE_SYNCH));
	}
	public Boolean isAllowedDeleteTeam() {
		return Boolean.valueOf(getConfigItemValueByKey(DELETE_TEAM));
	}
	public Boolean isAllowedAddUserToTeam() {
		return Boolean.valueOf(getConfigItemValueByKey(ADD_USER_TO_TEAM));
	}
	public Boolean isAllowedRemoveUserFromTeam() {
		return Boolean.valueOf(getConfigItemValueByKey(REMOVE_USER_FROM_TEAM));
	}
	public Boolean isAllowedCreateChannel() {
		return Boolean.valueOf(getConfigItemValueByKey(CREATE_CHANNEL));
	}
	public Boolean isAllowedDeleteChannel() {
		return Boolean.valueOf(getConfigItemValueByKey(DELETE_CHANNEL));
	}
	public Boolean isAllowedAddUserToChannel() {
		return Boolean.valueOf(getConfigItemValueByKey(ADD_USER_TO_CHANNEL));
	}
	public Boolean isAllowedRemoveUserFromChannel() {
		return Boolean.valueOf(getConfigItemValueByKey(REMOVE_USER_FROM_CHANNEL));
	}
	public Boolean isAllowedRemoveUsersWhenUnpublish() {
		return Boolean.valueOf(getConfigItemValueByKey(REMOVE_USERS_WHEN_UNPUBLISH));
	}
	public Boolean isAllowedCreateInvitation() {
		return Boolean.valueOf(getConfigItemValueByKey(CREATE_INVITATION));
	}
	
	//------------------------------- MICROSOFT SYNCHRONIZATION - NEW SITE ------------------------------------
	public SakaiSiteFilter getNewSiteFilter() {
		SakaiSiteFilter ret = new SakaiSiteFilter();
		ret.setSiteType(getConfigItemValueByKey(NEW_SITE_TYPE));
		ret.setPublished(Boolean.valueOf(getConfigItemValueByKey(NEW_SITE_PUBLISHED)));
		ret.setSiteProperty(getConfigItemValueByKey(NEW_SITE_PROPERTY));
		return ret;
	}
	
	public long getSyncDuration() {
		try {
			return Long.valueOf(getConfigItemValueByKey(MicrosoftConfigRepository.NEW_SITE_SYNC_DURATION));
		} catch(NumberFormatException e) {
			//DEFAULT: 12 months
			return 12;
		}
	}
	
	//------------------------------- MICROSOFT SYNCHRONIZATION - JOB ------------------------------------
	public SakaiSiteFilter getJobSiteFilter() {
		SakaiSiteFilter ret = new SakaiSiteFilter();
		ret.setSiteType(getConfigItemValueByKey(JOB_SITE_TYPE));
		ret.setPublished(Boolean.valueOf(getConfigItemValueByKey(JOB_SITE_PUBLISHED)));
		ret.setSiteProperty(getConfigItemValueByKey(JOB_SITE_PROPERTY));
		return ret;
	}
}
