/**
* Copyright (c) 2023 Apereo Foundation
* 
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*             http://opensource.org/licenses/ecl2
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.sakaiproject.microsoft.impl;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.messaging.api.MicrosoftMessage;
import org.sakaiproject.messaging.api.MicrosoftMessagingService;
import org.sakaiproject.microsoft.api.MicrosoftCommonService;
import org.sakaiproject.microsoft.api.MicrosoftSynchronizationService;
import org.sakaiproject.microsoft.api.SakaiProxy;
import org.sakaiproject.microsoft.api.data.MicrosoftChannel;
import org.sakaiproject.microsoft.api.data.MicrosoftCredentials;
import org.sakaiproject.microsoft.api.data.MicrosoftMembersCollection;
import org.sakaiproject.microsoft.api.data.MicrosoftTeam;
import org.sakaiproject.microsoft.api.data.MicrosoftUser;
import org.sakaiproject.microsoft.api.data.MicrosoftUserIdentifier;
import org.sakaiproject.microsoft.api.data.SakaiMembersCollection;
import org.sakaiproject.microsoft.api.data.SakaiSiteFilter;
import org.sakaiproject.microsoft.api.data.SakaiUserIdentifier;
import org.sakaiproject.microsoft.api.data.SynchronizationStatus;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftCredentialsException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftGenericException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftInvalidInvitationException;
import org.sakaiproject.microsoft.api.model.GroupSynchronization;
import org.sakaiproject.microsoft.api.model.MicrosoftLog;
import org.sakaiproject.microsoft.api.model.MicrosoftLog.MicrosoftLogBuilder;
import org.sakaiproject.microsoft.api.model.SiteSynchronization;
import org.sakaiproject.microsoft.api.persistence.MicrosoftConfigRepository;
import org.sakaiproject.microsoft.api.persistence.MicrosoftGroupSynchronizationRepository;
import org.sakaiproject.microsoft.api.persistence.MicrosoftLoggingRepository;
import org.sakaiproject.microsoft.api.persistence.MicrosoftSiteSynchronizationRepository;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Transactional
public class MicrosoftSynchronizationServiceImpl implements MicrosoftSynchronizationService {

	@Setter
	MicrosoftSiteSynchronizationRepository microsoftSiteSynchronizationRepository;
	
	@Setter
	MicrosoftGroupSynchronizationRepository microsoftGroupSynchronizationRepository;
	
	@Setter
	MicrosoftConfigRepository microsoftConfigRepository;
	
	@Setter
	MicrosoftLoggingRepository microsoftLoggingRepository;
	
	@Autowired
	private MicrosoftCommonService microsoftCommonService;
	
	@Autowired
	private SakaiProxy sakaiProxy;
	
	@Autowired
	private SessionManager sessionManager;
	
	@Autowired
	private MicrosoftMessagingService microsoftMessagingService;
	
	//used in hooks. Sometimes we need to stop listening some events
	private Set<String> disabledGroupListeners = ConcurrentHashMap.newKeySet();
	//used in hooks to synchronize. "add users to group" must happen after "create group" 
	private ConcurrentHashMap<String, Object> newGroupLock = new ConcurrentHashMap<>();

	public void init() {
		log.info("Initializing MicrosoftSynchService Service");
		
		microsoftMessagingService.listen(MicrosoftMessage.Topic.CREATE_ELEMENT, message -> {
			printMessage(MicrosoftMessage.Topic.CREATE_ELEMENT, message);
			elementCreated(message);
		});
		
		microsoftMessagingService.listen(MicrosoftMessage.Topic.DELETE_ELEMENT, message -> {
			printMessage(MicrosoftMessage.Topic.DELETE_ELEMENT, message);
			elementDeleted(message);
		});
		
		microsoftMessagingService.listen(MicrosoftMessage.Topic.MODIFY_ELEMENT, message -> {
			printMessage(MicrosoftMessage.Topic.MODIFY_ELEMENT, message);
			elementModified(message);
		});
		
		microsoftMessagingService.listen(MicrosoftMessage.Topic.ADD_MEMBER_TO_AUTHZGROUP, message -> {
			Session sakaiSession = startAdminSession();
			printMessage(MicrosoftMessage.Topic.ADD_MEMBER_TO_AUTHZGROUP, message);
			userAddedToAuthzGroup(message);
			endAdminSession(sakaiSession);
		});
		
		microsoftMessagingService.listen(MicrosoftMessage.Topic.REMOVE_MEMBER_FROM_AUTHZGROUP, message -> {
			printMessage(MicrosoftMessage.Topic.REMOVE_MEMBER_FROM_AUTHZGROUP, message);
			userRemovedFromAuthzGroup(message);
		});
		
		microsoftMessagingService.listen(MicrosoftMessage.Topic.TEAM_CREATION, message -> {
			printMessage(MicrosoftMessage.Topic.TEAM_CREATION, message);
			teamCreated(message);
		});

		microsoftMessagingService.listen(MicrosoftMessage.Topic.CHANGE_LISTEN_GROUP_EVENTS, message -> {
			printMessage(MicrosoftMessage.Topic.CHANGE_LISTEN_GROUP_EVENTS, message);
			updateListenGroupEvents(message);
		});
	}
	
	private Session startAdminSession() {
		Session sakaiSession = sessionManager.getCurrentSession();
		sakaiSession.setUserId("admin");
		sakaiSession.setUserEid("admin");
		return sakaiSession;
	}
	
	private void endAdminSession(Session sakaiSession) {
		sakaiSession.setUserId(null);
		sakaiSession.setUserId(null);
		sakaiSession.clear();
	}
	
	private void printMessage(MicrosoftMessage.Topic topic, MicrosoftMessage msg) {
		log.debug("Message listen from MicrosoftSynchronizationServiceImpl: TOPIC={} => action={}, type={}, siteId={}, groupId={}, userId={}, status={}, owner={}, forced={}",
				topic,
				msg.getAction(),
				msg.getType(),
				msg.getSiteId(),
				msg.getGroupId(),
				msg.getUserId(),
				msg.getStatus(),
				msg.isOwner(),
				msg.isForce()
		);
	}

	// ---------------------------------------- SITE SYNCHRONIZATION ------------------------------------------------
	private boolean checkTeam(String teamId) throws MicrosoftCredentialsException {
		MicrosoftTeam mt = microsoftCommonService.getTeam(teamId);
		//if Team does not exist
		//check if group exists
		if(mt == null && microsoftCommonService.getGroup(teamId) != null) {
			try {
				//create Team from given group
				microsoftCommonService.createTeamFromGroup(teamId);
			}catch(MicrosoftCredentialsException e) {
				throw e;
			} catch(Exception e) {
				log.debug("Error creting team from group: {}", teamId);
				//save log
				microsoftLoggingRepository.save(MicrosoftLog.builder()
						.event(MicrosoftLog.EVENT_CREATE_TEAM_FROM_GROUP)
						.status(MicrosoftLog.Status.KO)
						.addData("groupId", teamId)
						.build());
			}
			
			//force cache update (try-repeat every 5 seconds x5)
			mt = microsoftCommonService.getTeam(teamId, true);
			int count = 5;
			while(mt == null && count > 0) {
				try { Thread.sleep(5000); } catch(Exception e) {}
				mt = microsoftCommonService.getTeam(teamId, true);
				count--;
			}
		}
		return mt != null;
	}
	
	@Override
	public List<SiteSynchronization> getAllSiteSynchronizations(boolean fillSite) {
		List<SiteSynchronization> result = StreamSupport.stream(microsoftSiteSynchronizationRepository.findAll().spliterator(), false)
				.map(ss -> {
					if(fillSite) {
						ss.setSite(sakaiProxy.getSite(ss.getSiteId()));
					}
					return ss;
				})
				.collect(Collectors.toList());
		return result;
	}
	
	@Override
	public SiteSynchronization getSiteSynchronization(SiteSynchronization ss) {
		return getSiteSynchronization(ss, false);
	}
	
	@Override
	public SiteSynchronization getSiteSynchronization(SiteSynchronization ss, boolean fillSite) {
		if(ss == null) {
			return null;
		}
		Optional<SiteSynchronization> oss = Optional.empty();
		if(StringUtils.isNotBlank(ss.getId())) {
			log.debug("looking for SiteSynchronization with: id={}", ss.getId());
			oss = microsoftSiteSynchronizationRepository.findById(ss.getId());
		} else {
			log.debug("looking for SiteSynchronization with: siteId={}, teamId={}", ss.getSiteId(), ss.getTeamId());
			oss = microsoftSiteSynchronizationRepository.findBySiteTeam(ss.getSiteId(), ss.getTeamId());
		}
		if(fillSite && oss.isPresent()) {
			oss.get().setSite(sakaiProxy.getSite(oss.get().getSiteId()));
		}
		return oss.orElse(null);
	}
	
	@Override
	public List<SiteSynchronization> getSiteSynchronizationsBySite(String siteId){
		return microsoftSiteSynchronizationRepository.findBySite(siteId);
	}
	
	@Override
	public List<SiteSynchronization> getSiteSynchronizationsByTeam(String teamId){
		return microsoftSiteSynchronizationRepository.findByTeam(teamId);
	}
	
	@Override
	public long countSiteSynchronizationsByTeamId(String teamId, boolean forced) {
		return microsoftSiteSynchronizationRepository.countSiteSynchronizationsByTeamId(teamId, forced);
	}
	
	@Override
	public Integer deleteSiteSynchronizations(List<String> ids) {
		return microsoftSiteSynchronizationRepository.deleteSiteSynchronizationsById(ids);
	}
	
	@Override
	public void saveOrUpdateSiteSynchronization(SiteSynchronization ss) {
		if(StringUtils.isBlank(ss.getId())) {
			microsoftSiteSynchronizationRepository.save(ss);
		} else {
			microsoftSiteSynchronizationRepository.update(ss);
		}
	}
	
	@Override
	public boolean removeUsersFromSynchronization(SiteSynchronization ss) throws MicrosoftCredentialsException {
		boolean ok = false;
		if(ss != null) {
			//remove all users from team
			ok = microsoftCommonService.removeAllMembersFromTeam(ss.getTeamId());

			//get all relationships for selected team
			List<SiteSynchronization> auxList = getSiteSynchronizationsByTeam(ss.getTeamId());
			for(SiteSynchronization aux_ss : auxList) {
				//update status
				if(!ok) {
					aux_ss.setStatus(SynchronizationStatus.ERROR);
				} else {
					aux_ss.setStatus(SynchronizationStatus.KO);
				}
				aux_ss.setStatusUpdatedAt(ZonedDateTime.now());
				saveOrUpdateSiteSynchronization(aux_ss);

				if(aux_ss.getGroupSynchronizationsList().size() > 0) {
					for(GroupSynchronization gs : aux_ss.getGroupSynchronizationsList()) {
						if(!ok) {
							gs.setStatus(SynchronizationStatus.ERROR);
						} else {
							gs.setStatus(SynchronizationStatus.KO);
						}
						gs.setStatusUpdatedAt(ZonedDateTime.now());
						saveOrUpdateGroupSynchronization(gs);
					}
				}
			}
			
			//save log
			microsoftLoggingRepository.save(MicrosoftLog.builder()
					.event(MicrosoftLog.EVENT_ALL_USERS_REMOVED_FROM_TEAM)
					.status((ok) ? MicrosoftLog.Status.OK : MicrosoftLog.Status.KO)
					.addData("siteId", ss.getSiteId())
					.addData("teamId", ss.getTeamId())
					.build());
		}
		return ok;
	}
	
	@Override
	public SynchronizationStatus checkStatus(SiteSynchronization ss) {
		SynchronizationStatus ret = SynchronizationStatus.NONE;
		try {
			if(checkTeam(ss.getTeamId())) {
			
				SakaiUserIdentifier mappedSakaiUserId = microsoftConfigRepository.getMappedSakaiUserId();
				SakaiMembersCollection siteMembers = sakaiProxy.getSiteMembers(ss.getSiteId(), mappedSakaiUserId);
				
				MicrosoftUserIdentifier mappedMicrosoftUserId = microsoftConfigRepository.getMappedMicrosoftUserId();
				MicrosoftMembersCollection teamMembers = microsoftCommonService.getTeamMembers(ss.getTeamId(), mappedMicrosoftUserId);
	
				boolean aux = siteMembers.compareWith(teamMembers, ss.isForced());
				ret = (aux) ? SynchronizationStatus.OK : SynchronizationStatus.KO;
				
				if(ss.getGroupSynchronizationsList() != null && ss.getGroupSynchronizationsList().size() > 0) {
					boolean all_ok = true;
					boolean error = false;
					for(GroupSynchronization gs : ss.getGroupSynchronizationsList()) {
						SynchronizationStatus aux_status = checkGroupStatus(ss, gs, mappedSakaiUserId, mappedMicrosoftUserId);
						if(aux_status == SynchronizationStatus.ERROR) {
							error = true;
						}
						if(aux_status == SynchronizationStatus.KO) {
							all_ok = false;
						}
					}
					
					if(error) {
						ret = SynchronizationStatus.ERROR;
					} else if(!all_ok && ret == SynchronizationStatus.OK){
						ret = SynchronizationStatus.PARTIAL_OK;
					}
				}
			} else {                
				ret = SynchronizationStatus.ERROR;
			}
		} catch (Exception e) {
			ret = SynchronizationStatus.ERROR;
		}
		ss.setStatus(ret);
		ss.setStatusUpdatedAt(ZonedDateTime.now());
		saveOrUpdateSiteSynchronization(ss);
		
		return ret;
	}
	
	//private helper to add member and store log
	private boolean addMemberToMicrosoftGroupOrTeam(SiteSynchronization ss, MicrosoftUser mu) throws MicrosoftCredentialsException {
		//from Microsoft API: "Using application permissions to add guest members to a team is not supported."
		//https://learn.microsoft.com/en-us/graph/api/team-post-members
		//guest users are added to group instead
		log.debug("-> teamId={}, userId={}, email={}, guest={}", ss.getTeamId(), mu.getId(), mu.getEmail(), mu.isGuest());
		boolean res = (mu.isGuest()) ? microsoftCommonService.addMemberToGroup(mu.getId(), ss.getTeamId()) : microsoftCommonService.addMemberToTeam(mu.getId(), ss.getTeamId());

		//save log
		MicrosoftLogBuilder builder = MicrosoftLog.builder();
		if(mu.isGuest()) {
			builder.event(MicrosoftLog.EVENT_USER_ADDED_TO_MICROSOFT_GROUP)
				.addData("guest", Boolean.TRUE.toString());
		} else {
			builder.event(MicrosoftLog.EVENT_USER_ADDED_TO_TEAM)
				.addData("owner", Boolean.FALSE.toString());
		}
		builder.status((res) ? MicrosoftLog.Status.OK : MicrosoftLog.Status.KO)
			.addData("email", mu.getEmail())
			.addData("microsoftUserId", mu.getId())
			.addData("siteId", ss.getSiteId())
			.addData("teamId", ss.getTeamId());
		microsoftLoggingRepository.save(builder.build());
		return res;
	}
	
	//private helper to add owner and store log
	private boolean addOwnerToMicrosoftGroupOrTeam(SiteSynchronization ss, MicrosoftUser mu) throws MicrosoftCredentialsException {
		//from Microsoft API: "Using application permissions to add guest members to a team is not supported."
		//https://learn.microsoft.com/en-us/graph/api/team-post-members
		//also, guest users can not be added as owners --> guest users are added as members to group
		log.debug("-> teamId={}, userId={}, email={}, guest={}", ss.getTeamId(), mu.getId(), mu.getEmail(), mu.isGuest());
		boolean res = (mu.isGuest()) ? microsoftCommonService.addMemberToGroup(mu.getId(), ss.getTeamId()) : microsoftCommonService.addOwnerToTeam(mu.getId(), ss.getTeamId());

		//save log
		MicrosoftLogBuilder builder = MicrosoftLog.builder();
		if(mu.isGuest()) {
			builder.event(MicrosoftLog.EVENT_USER_ADDED_TO_MICROSOFT_GROUP)
				.addData("guest", Boolean.TRUE.toString());
		} else {
			builder.event(MicrosoftLog.EVENT_USER_ADDED_TO_TEAM)
				.addData("owner", Boolean.TRUE.toString());
		}
		builder.status((res) ? MicrosoftLog.Status.OK : MicrosoftLog.Status.KO)
			.addData("email", mu.getEmail())
			.addData("microsoftUserId", mu.getId())
			.addData("siteId", ss.getSiteId())
			.addData("teamId", ss.getTeamId());
		microsoftLoggingRepository.save(builder.build());
		return res;
	}
	
	//private helper to remove member and store log
	private boolean removeMemberFromMicrosoftTeam(SiteSynchronization ss, MicrosoftUser mu) throws MicrosoftCredentialsException {
		log.debug("-> teamId={}, userId={}, email={}, guest={}", ss.getTeamId(), mu.getId(), mu.getEmail(), mu.isGuest());
		boolean res = microsoftCommonService.removeMemberFromTeam(mu.getMemberId(), ss.getTeamId());
		
		//save log
		microsoftLoggingRepository.save(MicrosoftLog.builder()
				.event(MicrosoftLog.EVENT_USER_REMOVED_FROM_TEAM)
				.status((res) ? MicrosoftLog.Status.OK : MicrosoftLog.Status.KO)
				.addData("email", mu.getEmail())
				.addData("microsoftUserId", mu.getId())
				.addData("siteId", ss.getSiteId())
				.addData("teamId", ss.getTeamId())
				.addData("owner", Boolean.toString(mu.isOwner()))
				.addData("guest", Boolean.toString(mu.isGuest()))
				.build());
		return res;
	}
	
	//private helper to create invitation and store log
	private MicrosoftUser createInvitation(SiteSynchronization ss, User u, SakaiUserIdentifier mappedSakaiUserId, MicrosoftUserIdentifier mappedMicrosoftUserId) throws MicrosoftGenericException {
		MicrosoftUser mu = null;
		try {
			if(microsoftConfigRepository.isAllowedCreateInvitation() && u != null) {
				mu = microsoftCommonService.createInvitation(u.getEmail(), "https://teams.microsoft.com");
				
				//if Sakai User is identified by user-property and the property is null or empty --> update that property
				if(mu != null && mappedSakaiUserId == SakaiUserIdentifier.USER_PROPERTY && StringUtils.isBlank(sakaiProxy.getMemberKeyValue(u, mappedSakaiUserId))) {
					//save microsoft id in sakai (as user property)
					String value = null;
					if(mappedMicrosoftUserId == MicrosoftUserIdentifier.EMAIL) {
						value = (mu.getEmail() != null) ? mu.getEmail().toLowerCase() : null;
					} else if(mappedMicrosoftUserId == MicrosoftUserIdentifier.USER_ID) {
						value = mu.getId();
					}
					if(StringUtils.isNotBlank(value)) {
						sakaiProxy.setUserProperty(u.getId(), value);
					}
				}

				//save log
				microsoftLoggingRepository.save(MicrosoftLog.builder()
						.event(MicrosoftLog.EVENT_INVITATION_SENT)
						.status((mu != null) ? MicrosoftLog.Status.OK : MicrosoftLog.Status.KO)
						.addData("email", (u != null) ? u.getEmail() : "-null-")
						.addData("microsoftUserId", (mu != null) ? mu.getId() : "-null-")
						.addData("siteId", ss.getSiteId())
						.addData("teamId", ss.getTeamId())
						.build());
			} else {
				//invitation not sent because of config - save log
				microsoftLoggingRepository.save(MicrosoftLog.builder()
						.event(MicrosoftLog.EVENT_INVITATION_NOT_SENT)
						.status(MicrosoftLog.Status.OK)
						.addData("email", (u != null) ? u.getEmail() : "-null-")
						.addData("siteId", ss.getSiteId())
						.addData("teamId", ss.getTeamId())
						.build());
			}
		} catch (MicrosoftInvalidInvitationException e) {
			//save log
			microsoftLoggingRepository.save(MicrosoftLog.builder()
					.event(MicrosoftLog.ERROR_INVITATION)
					.status(MicrosoftLog.Status.KO)
					.addData("email", (u != null) ? u.getEmail() : "-null-")
					.addData("siteId", ss.getSiteId())
					.addData("teamId", ss.getTeamId())
					.build());
		}
		return mu;
	}
	
	@Override
	public SynchronizationStatus runSiteSynchronization(SiteSynchronization ss) throws MicrosoftGenericException {
		SynchronizationStatus ret = SynchronizationStatus.ERROR;
		log.debug(".................runSiteSynchronization................");
		if(!ss.onDate()) {
			log.debug("SS: siteId={}, teamId={} --> OUT OF DATE", ss.getSiteId(), ss.getTeamId());
			return ret;
		}
		
		//save log
		microsoftLoggingRepository.save(MicrosoftLog.builder()
				.event(MicrosoftLog.EVENT_SITE_SYNCRHO_START)
				.status(MicrosoftLog.Status.OK)
				.addData("siteId", ss.getSiteId())
				.addData("teamId", ss.getTeamId())
				.addData("forced", Boolean.toString(ss.isForced()))
				.build());
		
		if(checkTeam(ss.getTeamId())) {
			ret = SynchronizationStatus.OK;
		
			SakaiUserIdentifier mappedSakaiUserId = microsoftConfigRepository.getMappedSakaiUserId();
			SakaiMembersCollection siteMembers = sakaiProxy.getSiteMembers(ss.getSiteId(), mappedSakaiUserId);
			
			MicrosoftUserIdentifier mappedMicrosoftUserId = microsoftConfigRepository.getMappedMicrosoftUserId();
			MicrosoftMembersCollection teamMembers = microsoftCommonService.getTeamMembers(ss.getTeamId(), mappedMicrosoftUserId);
			
			//get site users that are not in team
			SakaiMembersCollection filteredSiteMembers = siteMembers.diffWith(teamMembers);
			
			if(log.isDebugEnabled()) {
				log.debug("diff members...");
				filteredSiteMembers.getMemberIds().stream().forEach(id -> log.debug("> {}", id));
				log.debug("diff owners...");
				filteredSiteMembers.getOwnerIds().stream().forEach(id -> log.debug("> {}", id));
			}
			
			//if forced --> we need to remove users in Microsoft that do not exist in Sakai
			if (ss.isForced()) {
				
				//process all group synchronizations related - only to remove
				//users will be removed first from channels
				if(ss.getGroupSynchronizationsList() != null && ss.getGroupSynchronizationsList().size() > 0) {
					for(GroupSynchronization gs : ss.getGroupSynchronizationsList()) {
						SynchronizationStatus aux_status = runGroupSynchronizationForced(ss, gs, mappedSakaiUserId, mappedMicrosoftUserId);
						if(aux_status == SynchronizationStatus.ERROR_GUEST && ret != SynchronizationStatus.ERROR) {
							//once ERROR status is set, do not check it again
							ret = SynchronizationStatus.ERROR_GUEST;
						}
						if(aux_status == SynchronizationStatus.ERROR) {
							ret = SynchronizationStatus.ERROR;
						}
					}
				}
				
				//get team members that are not in site
				MicrosoftMembersCollection filteredTeamMembers = teamMembers.diffWith(siteMembers);
				
				if(log.isDebugEnabled()) {
					log.debug("** is forcing");
					log.debug("inv diff members...");
					filteredTeamMembers.getMemberIds().stream().forEach(id -> log.debug("> {} -> email={}", id, ((MicrosoftUser)filteredTeamMembers.getMembers().get(id)).getEmail()));
					log.debug("inv diff owners...");
					filteredTeamMembers.getOwnerIds().stream().forEach(id -> log.debug("> {} -> email={}", id, ((MicrosoftUser)filteredTeamMembers.getOwners().get(id)).getEmail()));
					log.debug("inv diff guests...");
					filteredTeamMembers.getGuestIds().stream().forEach(id -> log.debug("> {} -> email={}", id, ((MicrosoftUser)filteredTeamMembers.getGuests().get(id)).getEmail()));
				}

				MicrosoftCredentials credentials = microsoftConfigRepository.getCredentials();

				for(Object o : filteredTeamMembers.getMembers().values()) {
					MicrosoftUser mu = (MicrosoftUser)o;
					//remove from team
					boolean res = removeMemberFromMicrosoftTeam(ss, mu);
					if(!res) {
						ret = SynchronizationStatus.ERROR;
					}
				}
				
				for(Object o : filteredTeamMembers.getOwners().values()) {
					MicrosoftUser mu = (MicrosoftUser)o;
					//never remove microsoft "admin" user
					if(!credentials.getEmail().equalsIgnoreCase(mu.getEmail())) {
						//remove from team
						boolean res = removeMemberFromMicrosoftTeam(ss, mu);
						if(!res) {
							ret = SynchronizationStatus.ERROR;
						}
					}
				}
				
				for(MicrosoftUser mu : filteredTeamMembers.getGuests().values()) {
					//remove from team
					boolean res = removeMemberFromMicrosoftTeam(ss, mu);
					if(!res && ret != SynchronizationStatus.ERROR) {
						//once ERROR status is set, do not check it again
						ret = SynchronizationStatus.ERROR_GUEST;
					}
				}
			}
			
			Map<String, MicrosoftUser> guestUsers = new HashMap<>();
			
			//process sakai members not in the team
			for(String id : filteredSiteMembers.getMembers().keySet()) {
				boolean res = false;
				MicrosoftUser mu = null;
				//if ID is NOT empty. (can be empty if user property does not exist or is blank)
				if(!id.startsWith("EMPTY_")) {
					mu = microsoftCommonService.getUser(id, mappedMicrosoftUserId);
				}
				if(mu == null) {
					//user does not exist -> create invitation
					User u = (User)filteredSiteMembers.getMembers().get(id);
					mu = createInvitation(ss, u, mappedSakaiUserId, mappedMicrosoftUserId);
					
					if(mu != null) {
						//store newly invited user in getsUsers map -> used in group synch in case this user do not appear yet in Microsoft registers
						id = sakaiProxy.getMemberKeyValue(sakaiProxy.getUser(u.getId()), mappedSakaiUserId);
						guestUsers.put(id, mu);
					}
				}
				if(mu != null) {
					//add to team/group
					res = addMemberToMicrosoftGroupOrTeam(ss, mu);
				}
				if(!res && ret != SynchronizationStatus.ERROR) {
					//once ERROR status is set, do not check it again
					ret = (mu != null && mu.isGuest()) ? SynchronizationStatus.ERROR_GUEST : SynchronizationStatus.ERROR;
				}
			}
			
			//process sakai owners not in the team
			for(String id : filteredSiteMembers.getOwners().keySet()) {
				boolean res = false;
				MicrosoftUser mu = null;
				if(!id.startsWith("EMPTY_")) {
					mu = microsoftCommonService.getUser(id, mappedMicrosoftUserId);
				}
				if(mu == null) {
					//user does not exist -> create invitation
					User u = (User)filteredSiteMembers.getOwners().get(id);
					mu = createInvitation(ss, u, mappedSakaiUserId, mappedMicrosoftUserId);
					
					if(mu != null) {
						//store newly invited user in getsUsers map -> used in group synch in case this user do not appear yet in Microsoft registers
						id = sakaiProxy.getMemberKeyValue(sakaiProxy.getUser(u.getId()), mappedSakaiUserId);
						guestUsers.put(id, mu);
					}
				}
				if(mu != null) {
					//add to team/group
					res = addOwnerToMicrosoftGroupOrTeam(ss, mu);
				}
				if(!res && ret != SynchronizationStatus.ERROR) {
					//once ERROR status is set, do not check it again
					ret = (mu != null && mu.isGuest()) ? SynchronizationStatus.ERROR_GUEST : SynchronizationStatus.ERROR;
				}
			}
			
			//process all group synchronizations related
			if(ss.getGroupSynchronizationsList() != null && ss.getGroupSynchronizationsList().size() > 0) {
				for(GroupSynchronization gs : ss.getGroupSynchronizationsList()) {
					SynchronizationStatus aux_status = runGroupSynchronization(ss, gs, guestUsers, mappedSakaiUserId, mappedMicrosoftUserId);
					if(aux_status == SynchronizationStatus.ERROR_GUEST && ret != SynchronizationStatus.ERROR) {
						//once ERROR status is set, do not check it again
						ret = SynchronizationStatus.ERROR_GUEST;
					}
					if(aux_status == SynchronizationStatus.ERROR) {
						ret = SynchronizationStatus.ERROR;
					}
				}
			}
		}
		
		ss.setStatus(ret);
		ss.setStatusUpdatedAt(ZonedDateTime.now());
		saveOrUpdateSiteSynchronization(ss);
		
		//save log
		microsoftLoggingRepository.save(MicrosoftLog.builder()
				.event(MicrosoftLog.EVENT_SITE_SYNCRHO_END)
				.status((ret == SynchronizationStatus.OK) ? MicrosoftLog.Status.OK : MicrosoftLog.Status.KO)
				.addData("siteId", ss.getSiteId())
				.addData("teamId", ss.getTeamId())
				.addData("forced", Boolean.toString(ss.isForced()))
				.build());
		return ret;
	}
	
	// ---------------------------------------- GROUP SYNCHRONIZATION ------------------------------------------------
	private boolean checkChannel(String teamId, String channelId) throws MicrosoftCredentialsException {
		MicrosoftChannel mc = microsoftCommonService.getChannel(teamId, channelId);
		return (mc != null);
	}
	
	@Override
	public List<GroupSynchronization> getAllGroupSynchronizationsBySiteSynchronizationId(String siteSynchronizationId) {
		return microsoftGroupSynchronizationRepository.findBySiteSynchronizationId(siteSynchronizationId);
	}
	
	@Override
	public GroupSynchronization getGroupSynchronization(GroupSynchronization gs) {
		if(gs == null) {
			return null;
		}
		Optional<GroupSynchronization> ogs = Optional.empty();
		if(StringUtils.isNotBlank(gs.getId())) {
			log.debug("looking for GroupSynchronization with: id={}", gs.getId());
			ogs = microsoftGroupSynchronizationRepository.findById(gs.getId());
		} else {
			log.debug("looking for GroupSynchronization with: groupId={}, channelId={}", gs.getGroupId(), gs.getChannelId());
			ogs = microsoftGroupSynchronizationRepository.findByGroupChannel(gs.getGroupId(), gs.getChannelId());
		}
		return ogs.orElse(null);
	}
	
	@Override
	public long countGroupSynchronizationsByChannelId(String channelId) {
		return microsoftGroupSynchronizationRepository.countGroupSynchronizationsByChannelId(channelId);
	}
	
	@Override
	public void saveOrUpdateGroupSynchronization(GroupSynchronization gs) {
		if(StringUtils.isBlank(gs.getId())) {
			microsoftGroupSynchronizationRepository.save(gs);
		} else {
			microsoftGroupSynchronizationRepository.update(gs);
		}
	}
	
	@Override
	public boolean deleteGroupSynchronization(String id) {
		try {
			microsoftGroupSynchronizationRepository.delete(id);
			return true;
		}catch(Exception e) {
			log.error("Error deleting GroupSynchronization with id={}", id);
		}
		return false;
		
	}
	
	@Override
	public void deleteAllGroupSynchronizationsBySiteSynchronizationId(String siteSynchronizationId) {
		microsoftGroupSynchronizationRepository.deleteBySiteSynchronizationId(siteSynchronizationId);
	}
	
	private SynchronizationStatus checkGroupStatus(SiteSynchronization ss, GroupSynchronization gs, SakaiUserIdentifier mappedSakaiUserId, MicrosoftUserIdentifier mappedMicrosoftUserId) {
		SynchronizationStatus ret = SynchronizationStatus.NONE;
		try {
			Group g = ss.getSite().getGroup(gs.getGroupId());
			if(g != null && checkChannel(ss.getTeamId(), gs.getChannelId())) {
				SakaiMembersCollection groupMembers = sakaiProxy.getGroupMembers(g, mappedSakaiUserId);
				MicrosoftMembersCollection channelMembers = microsoftCommonService.getChannelMembers(ss.getTeamId(), gs.getChannelId(), mappedMicrosoftUserId);
	
				boolean aux = groupMembers.compareWith(channelMembers, ss.isForced());
				ret = (aux) ? SynchronizationStatus.OK : SynchronizationStatus.KO;
			} else {
				ret = SynchronizationStatus.ERROR;
			}

		} catch (Exception e) {
			ret = SynchronizationStatus.ERROR;
		}
		gs.setStatus(ret);
		gs.setStatusUpdatedAt(ZonedDateTime.now());
		saveOrUpdateGroupSynchronization(gs);
		
		return ret;
	}
	
	//private helper to add member and store log
	private boolean addMemberToMicrosoftChannel(SiteSynchronization ss, GroupSynchronization gs, MicrosoftUser mu) throws MicrosoftCredentialsException {
		log.debug("-> teamId={}, channelId={}, userId={}, email={}, guest={}", ss.getTeamId(), gs.getChannelId(), mu.getId(), mu.getEmail(), mu.isGuest());
		boolean res = microsoftCommonService.addMemberToChannel(mu.getId(), ss.getTeamId(), gs.getChannelId());
		
		//save log
		microsoftLoggingRepository.save(MicrosoftLog.builder()
				.event(MicrosoftLog.EVENT_USER_ADDED_TO_CHANNEL)
				.status((res) ? MicrosoftLog.Status.OK : MicrosoftLog.Status.KO)
				.addData("email", mu.getEmail())
				.addData("microsoftUserId", mu.getId())
				.addData("siteId", ss.getSiteId())
				.addData("teamId", ss.getTeamId())
				.addData("groupId", gs.getGroupId())
				.addData("channelId", gs.getChannelId())
				.addData("owner", Boolean.FALSE.toString())
				.addData("guest", Boolean.toString(mu.isGuest()))
				.build());
		return res;
	}
	
	//private helper to add owner and store log
	private boolean addOwnerToMicrosoftChannel(SiteSynchronization ss, GroupSynchronization gs, MicrosoftUser mu) throws MicrosoftCredentialsException {
		log.debug("-> teamId={}, channelId={}, userId={}, email={}, guest={}", ss.getTeamId(), gs.getChannelId(), mu.getId(), mu.getEmail(), mu.isGuest());
		boolean res = (mu.isGuest()) ? microsoftCommonService.addMemberToChannel(mu.getId(), ss.getTeamId(), gs.getChannelId()) : microsoftCommonService.addOwnerToChannel(mu.getId(), ss.getTeamId(), gs.getChannelId());
		
		//save log
		microsoftLoggingRepository.save(MicrosoftLog.builder()
				.event(MicrosoftLog.EVENT_USER_ADDED_TO_CHANNEL)
				.status((res) ? MicrosoftLog.Status.OK : MicrosoftLog.Status.KO)
				.addData("email", mu.getEmail())
				.addData("microsoftUserId", mu.getId())
				.addData("siteId", ss.getSiteId())
				.addData("teamId", ss.getTeamId())
				.addData("groupId", gs.getGroupId())
				.addData("channelId", gs.getChannelId())
				.addData("owner", Boolean.toString(!mu.isGuest()))
				.addData("guest", Boolean.toString(mu.isGuest()))
				.build());
		return res;
	}
	
	//private helper to add member and store log
	private boolean removeMemberFromMicrosoftChannel(SiteSynchronization ss, GroupSynchronization gs, MicrosoftUser mu) throws MicrosoftCredentialsException {
		log.debug("-> teamId={}, channelId={}, userId={}, email={}, guest={}", ss.getTeamId(), gs.getChannelId(), mu.getId(), mu.getEmail(), mu.isGuest());
		boolean res = microsoftCommonService.removeMemberFromChannel(mu.getMemberId(), ss.getTeamId(), gs.getChannelId());
		
		//save log
		microsoftLoggingRepository.save(MicrosoftLog.builder()
				.event(MicrosoftLog.EVENT_USER_REMOVED_FROM_CHANNEL)
				.status((res) ? MicrosoftLog.Status.OK : MicrosoftLog.Status.KO)
				.addData("email", mu.getEmail())
				.addData("microsoftUserId", mu.getId())
				.addData("siteId", ss.getSiteId())
				.addData("teamId", ss.getTeamId())
				.addData("groupId", gs.getGroupId())
				.addData("channelId", gs.getChannelId())
				.addData("owner", Boolean.toString(mu.isOwner()))
				.addData("guest", Boolean.toString(mu.isGuest()))
				.build());
		return res;
	}
	
	private SynchronizationStatus runGroupSynchronization(SiteSynchronization ss, GroupSynchronization gs, Map<String, MicrosoftUser> guestUsers, SakaiUserIdentifier mappedSakaiUserId, MicrosoftUserIdentifier mappedMicrosoftUserId) throws MicrosoftGenericException {
		log.debug(".................runGroupSynchronization................");
		SynchronizationStatus ret = SynchronizationStatus.ERROR;

		Group g = ss.getSite().getGroup(gs.getGroupId());
		MicrosoftChannel mc = microsoftCommonService.getChannel(ss.getTeamId(), gs.getChannelId());
		//check channel
		if(g != null && mc != null) {
			ret = SynchronizationStatus.OK;
			
			SakaiMembersCollection groupMembers = sakaiProxy.getGroupMembers(g, mappedSakaiUserId);
			MicrosoftMembersCollection channelMembers = microsoftCommonService.getChannelMembers(ss.getTeamId(), gs.getChannelId(), mappedMicrosoftUserId);
			
			//get group users that are not in channel
			SakaiMembersCollection filteredGroupMembers = groupMembers.diffWith(channelMembers);
			
			if(log.isDebugEnabled()) {
				log.debug("diff group members...");
				filteredGroupMembers.getMemberIds().stream().forEach(id -> log.debug("> {}", id));
				log.debug("diff group owners...");
				filteredGroupMembers.getOwnerIds().stream().forEach(id -> log.debug("> {}", id));
			}
			
			for(String id : filteredGroupMembers.getMemberIds()) {
				boolean res = false;
				MicrosoftUser mu = microsoftCommonService.getUser(id, mappedMicrosoftUserId);
				if(mu == null) {
					//user not found in Microsoft. Check if is a newly created guest user
					mu = guestUsers.get(id);
				}
				if(mu != null) {
					//user exists -> add to channel
					//IMPORTANT: all non-existent users in Site, have been invited. So, should be no users in Group that do not exist in Microsoft
					//IMPORTANT 2: if user is just added to a group (because is guest/invited), maybe can not be added immediately to a channel
					res = addMemberToMicrosoftChannel(ss, gs, mu);
				}

				if(!res && ret != SynchronizationStatus.ERROR) {
					//once ERROR status is set, do not check it again
					ret = (mu != null && mu.isGuest()) ? SynchronizationStatus.ERROR_GUEST : SynchronizationStatus.ERROR;
				}
			}
			
			for(String id : filteredGroupMembers.getOwnerIds()) {
				boolean res = false;
				MicrosoftUser mu = microsoftCommonService.getUser(id, mappedMicrosoftUserId);
				if(mu == null) {
					//user not found in Microsoft. Check if is a newly created guest user
					mu = guestUsers.get(id);
				}
				if(mu != null) {
					//user exists -> add to channel
					//IMPORTANT: all non-existent users in Site, have been invited. So, there are no users in Group that do not exist in Microsoft
					//IMPORTANT 2: if user is just added to a group (because is guest/invited), maybe can not be added immediately to a channel
					res = addOwnerToMicrosoftChannel(ss, gs, mu);
				}
				
				if(!res && ret != SynchronizationStatus.ERROR) {
					//once ERROR status is set, do not check it again
					ret = (mu != null && mu.isGuest()) ? SynchronizationStatus.ERROR_GUEST : SynchronizationStatus.ERROR;
				}
			}
		}
		gs.setStatus(ret);
		gs.setStatusUpdatedAt(ZonedDateTime.now());
		saveOrUpdateGroupSynchronization(gs);
		
		return ret;
	}
	
	private SynchronizationStatus runGroupSynchronizationForced(SiteSynchronization ss, GroupSynchronization gs, SakaiUserIdentifier mappedSakaiUserId, MicrosoftUserIdentifier mappedMicrosoftUserId) throws MicrosoftGenericException {
		log.debug(".................runGroupSynchronization - forced................");
		SynchronizationStatus ret = SynchronizationStatus.ERROR;

		Group g = ss.getSite().getGroup(gs.getGroupId());
		MicrosoftChannel mc = microsoftCommonService.getChannel(ss.getTeamId(), gs.getChannelId());
		//check channel
		if(g != null && mc != null) {
			ret = SynchronizationStatus.OK;
			
			SakaiMembersCollection groupMembers = sakaiProxy.getGroupMembers(g, mappedSakaiUserId);
			MicrosoftMembersCollection channelMembers = microsoftCommonService.getChannelMembers(ss.getTeamId(), gs.getChannelId(), mappedMicrosoftUserId);
			
			//is forced --> we need to remove users in Microsoft that do not exist in Sakai
			//get channel members that are not in group
			MicrosoftMembersCollection filteredChannelMembers = channelMembers.diffWith(groupMembers);
			
			if(log.isDebugEnabled()) {
				log.debug("** is forcing");
				log.debug("inv group diff members...");
				filteredChannelMembers.getMemberIds().stream().forEach(id -> log.debug("> {} -> email={}", id, ((MicrosoftUser)filteredChannelMembers.getMembers().get(id)).getEmail()));
				log.debug("inv group diff owners...");
				filteredChannelMembers.getOwnerIds().stream().forEach(id -> log.debug("> {} -> email={}", id, ((MicrosoftUser)filteredChannelMembers.getOwners().get(id)).getEmail()));
				log.debug("inv group diff guests...");
				filteredChannelMembers.getGuestIds().stream().forEach(id -> log.debug("> {} -> email={}", id, ((MicrosoftUser)filteredChannelMembers.getGuests().get(id)).getEmail()));
			}

			MicrosoftCredentials credentials = microsoftConfigRepository.getCredentials();
			
			for(Object o : filteredChannelMembers.getMembers().values()) {
				MicrosoftUser mu = (MicrosoftUser)o;
				//remove from channel
				boolean res = removeMemberFromMicrosoftChannel(ss, gs, mu);
				if(!res && ret != SynchronizationStatus.ERROR) {
					//once ERROR status is set, do not check it again
					ret = (mu != null && mu.isGuest()) ? SynchronizationStatus.ERROR_GUEST : SynchronizationStatus.ERROR;
				}
			}
			
			for(Object o : filteredChannelMembers.getOwners().values()) {
				MicrosoftUser mu = (MicrosoftUser)o;
				//never remove microsoft "admin" user
				if(!credentials.getEmail().equalsIgnoreCase(mu.getEmail())) {
					//remove from channel
					boolean res = removeMemberFromMicrosoftChannel(ss, gs, mu);
					if(!res && ret != SynchronizationStatus.ERROR) {
						//once ERROR status is set, do not check it again
						ret = (mu != null && mu.isGuest()) ? SynchronizationStatus.ERROR_GUEST : SynchronizationStatus.ERROR;
					}
				}
			}
			
			for(MicrosoftUser mu : filteredChannelMembers.getGuests().values()) {
				//remove from channel
				boolean res = removeMemberFromMicrosoftChannel(ss, gs, mu);
				if(!res && ret != SynchronizationStatus.ERROR) {
					//once ERROR status is set, do not check it again
					ret = (mu != null && mu.isGuest()) ? SynchronizationStatus.ERROR_GUEST : SynchronizationStatus.ERROR;
				}
			}
		}
		gs.setStatus(ret);
		gs.setStatusUpdatedAt(ZonedDateTime.now());
		saveOrUpdateGroupSynchronization(gs);
		
		return ret;
	}
	
	// ---------------------------------------- HOOKS ------------------------------------------------
	//used by site-manage (GUI -> GroupController)
	//we need to stop listening some events because how group membership is processed
	private void updateListenGroupEvents(MicrosoftMessage msg) {
		switch(msg.getAction()) {
			case DISABLE:
				disabledGroupListeners.add(msg.getGroupId());
				break;
			case ENABLE:
				disabledGroupListeners.remove(msg.getGroupId());
				break;
			default:
				break;
		}
	}
	
	//new element created (Site or Group)
	private void elementCreated(MicrosoftMessage msg) {
		try {
			switch(msg.getType()) {
				case SITE:
					siteCreated(msg.getSiteId());
					break;
				case GROUP:
					groupCreated(msg.getSiteId(), msg.getGroupId());
					break;
				default:
					break;
			}
		}catch(Exception e) {
			//save log
			MicrosoftLogBuilder builder = MicrosoftLog.builder();
			builder.event(MicrosoftLog.ERROR_ELEMENT_CREATED)
				.status(MicrosoftLog.Status.KO)
				.addData("type", msg.getType().name())
				.addData("siteId", msg.getSiteId());
				if(msg.getType() == MicrosoftMessage.Type.GROUP) {
					builder.addData("groupId", msg.getGroupId());
				}
			microsoftLoggingRepository.save(builder.build());
		}
	}
	
	//new site created
	private void siteCreated(String siteId) throws MicrosoftGenericException {
		if(microsoftConfigRepository.isAllowedCreateTeam()) {
			MicrosoftCredentials credentials = microsoftConfigRepository.getCredentials();
			SakaiSiteFilter siteFilter = microsoftConfigRepository.getNewSiteFilter();

			int count = 5;
			String teamId = null;
			while(teamId == null && count > 0) {
				//wait 1 sec
				//site creation process in Sakai is done in two steps: 1) create empty site, 2) set title + update site
				//hook is launched on creation, so at this point it's possible title is not establish yet... so, we wait a bit
				try { Thread.sleep(1000); } catch (InterruptedException e) {}
				
				Site site = sakaiProxy.getSite(siteId);
				if(site != null) {
					//check filters
					if(!siteFilter.match(site)) {
						break;
					}

					teamId = microsoftCommonService.createTeam(site.getTitle(), credentials.getEmail());
					if(teamId != null) {
						long syncDuration = microsoftConfigRepository.getSyncDuration();
						ZonedDateTime today_midnight = ZonedDateTime.now().with(LocalTime.of(0, 0));
						ZonedDateTime future_midnight = today_midnight.plusMonths(syncDuration).with(LocalTime.of(23, 59));
						
						//create relationship
						SiteSynchronization ss = SiteSynchronization.builder()
								.siteId(siteId)
								.teamId(teamId)
								.forced(false)
								.syncDateFrom(today_midnight)
								.syncDateTo(future_midnight)
								.build();
		
						log.debug("saving NEW site-team: siteId={}, teamId={}", siteId, teamId);
						saveOrUpdateSiteSynchronization(ss);
					}
				}
				count--;
			}
				
			//save log
			microsoftLoggingRepository.save(MicrosoftLog.builder()
					.event(MicrosoftLog.EVENT_SITE_CREATED)
					.status((teamId != null) ? MicrosoftLog.Status.OK : MicrosoftLog.Status.KO)
					.addData("siteId", siteId)
					.addData("teamId", (teamId != null) ? teamId : "-null-")
					.build());
		
		}
	}
	
	//new group created
	private void groupCreated(String siteId, String groupId) throws MicrosoftGenericException {
		if(microsoftConfigRepository.isAllowedCreateChannel()) {
			log.debug("-> siteId={}, groupId={}", siteId, groupId);
			
			Object lock = newGroupLock.computeIfAbsent(groupId, k -> new Object());
			synchronized(lock) {
				MicrosoftCredentials credentials = microsoftConfigRepository.getCredentials();
				int count = 5;
				String channelId = null;
				while(channelId == null && count > 0) {
					//wait 1 sec
					//group creation process in Sakai is done in two steps: 1) create empty group, 2) set title + update group
					//hook is launched on creation, so at this point it's possible title is not establish yet... so, we wait a bit
					try { Thread.sleep(1000); } catch (InterruptedException e) {}
					
					Site site = sakaiProxy.getSite(siteId);
					if(site != null) {
						Group group = site.getGroup(groupId);
						if(group != null) {
							//exclude automatic lesson groups
							if(group.getTitle().startsWith("Access:")) {
								return;
							}
							
							//get all synchronizations linked to this site
							List<SiteSynchronization> list = microsoftSiteSynchronizationRepository.findBySite(siteId);
							if(list != null) {
								//for every relationship found
								for(SiteSynchronization ss : list) {
									//create new channel
									channelId = microsoftCommonService.createChannel(ss.getTeamId(), group.getTitle(), credentials.getEmail());
									if(channelId != null) {
										//create relationship
										GroupSynchronization gs = GroupSynchronization.builder()
												.siteSynchronization(ss)
												.groupId(groupId)
												.channelId(channelId)
												.build();
						
										log.debug("saving NEW group-channel: siteId={}, groupId={}, channelId={}", siteId, groupId, channelId);
										saveOrUpdateGroupSynchronization(gs);
										
										//save log
										microsoftLoggingRepository.save(MicrosoftLog.builder()
												.event(MicrosoftLog.EVENT_CHANNEL_CREATED)
												.status(MicrosoftLog.Status.OK)
												.addData("siteId", siteId)
												.addData("teamId", ss.getTeamId())
												.addData("groupId", groupId)
												.addData("channelId", channelId)
												.build());
									}
								}
							}
						}
					}
					count--;
				}

				//save log
				microsoftLoggingRepository.save(MicrosoftLog.builder()
						.event(MicrosoftLog.EVENT_GROUP_CREATED)
						.status((channelId != null) ? MicrosoftLog.Status.OK : MicrosoftLog.Status.KO)
						.addData("siteId", siteId)
						.addData("groupId", groupId)
						.build());

				newGroupLock.remove(groupId);
			}
		}
	}
	
	//element removed (Site or Group)
	private void elementDeleted(MicrosoftMessage msg) {
		try {
			switch(msg.getType()) {
				case SITE:
					siteDeleted(msg.getSiteId());
					break;
				case GROUP:
					groupDeleted(msg.getSiteId(), msg.getGroupId());
					break;
				default:
					break;
			}
		}catch(Exception e) {
			//save log
			MicrosoftLogBuilder builder = MicrosoftLog.builder();
			builder.event(MicrosoftLog.ERROR_ELEMENT_DELETED)
				.status(MicrosoftLog.Status.KO)
				.addData("type", msg.getType().name())
				.addData("siteId", msg.getSiteId());
				if(msg.getType() == MicrosoftMessage.Type.GROUP) {
					builder.addData("groupId", msg.getGroupId());
				}
			microsoftLoggingRepository.save(builder.build());
		}
	}
	
	private void siteDeleted(String siteId) throws MicrosoftGenericException {
		if(microsoftConfigRepository.isAllowedDeleteSynch()) {
			//get all synchronizations linked to this site
			List<SiteSynchronization> list = microsoftSiteSynchronizationRepository.findBySite(siteId);
			//for every synchronization
			for(SiteSynchronization ss : list) {
				//remove synch
				microsoftSiteSynchronizationRepository.delete(ss.getId());;
			
				if(microsoftConfigRepository.isAllowedDeleteTeam()) {
					//check if Team is no longer related to any other site
					if(microsoftSiteSynchronizationRepository.countSiteSynchronizationsByTeamId(ss.getTeamId(), false) == 0) {
						//remove team
						microsoftCommonService.deleteTeam(ss.getTeamId());
					}
				}
			}
			
			//save log
			microsoftLoggingRepository.save(MicrosoftLog.builder()
					.event(MicrosoftLog.EVENT_SITE_DELETED)
					.status(MicrosoftLog.Status.OK)
					.addData("siteId", siteId)
					.build());
		}
	}
	
	private void groupDeleted(String siteId, String groupId) throws MicrosoftGenericException{
		if(microsoftConfigRepository.isAllowedDeleteChannel()) {
			//get all synchronizations linked to this channel
			List<GroupSynchronization> list = microsoftGroupSynchronizationRepository.findByGroup(groupId);
			//for every synchronization
			for(GroupSynchronization gs : list) {
				//remove synch
				microsoftGroupSynchronizationRepository.delete(gs.getId());
				//check if Channel is no longer related to any other group
				if(microsoftGroupSynchronizationRepository.countGroupSynchronizationsByChannelId(gs.getChannelId()) == 0) {
					//remove channel
					microsoftCommonService.deleteChannel(gs.getSiteSynchronization().getTeamId(), gs.getChannelId());
				}
			}
			
			//save log
			microsoftLoggingRepository.save(MicrosoftLog.builder()
					.event(MicrosoftLog.EVENT_GROUP_DELETED)
					.status(MicrosoftLog.Status.OK)
					.addData("siteId", siteId)
					.addData("groupId", groupId)
					.build());
		}
	}
	
	//element modified (at this moment, only Unpublished Sites)
	private void elementModified(MicrosoftMessage msg) {
		try {
			if(msg.getType() == MicrosoftMessage.Type.SITE && msg.getAction() == MicrosoftMessage.Action.UNPUBLISH) {
				siteUnpublished(msg.getSiteId());
			}
		}catch(Exception e) {
			//save log
			MicrosoftLogBuilder builder = MicrosoftLog.builder();
			builder.event(MicrosoftLog.ERROR_ELEMENT_MODIFIED)
				.status(MicrosoftLog.Status.KO)
				.addData("type", msg.getType().name())
				.addData("action", msg.getAction().name())
				.addData("siteId", msg.getSiteId());
				if(msg.getType() == MicrosoftMessage.Type.GROUP) {
					builder.addData("groupId", msg.getGroupId());
				}
			microsoftLoggingRepository.save(builder.build());
		}
	}
	
	//new site created
	private void siteUnpublished(String siteId) throws MicrosoftGenericException {
		if(microsoftConfigRepository.isAllowedRemoveUsersWhenUnpublish()) {
			int count = 5;
			boolean end = false;
			while(!end && count > 0) {
				//wait 1 sec
				//site unpublish process in Sakai is done in two steps: 1) set unpublished, 2) save site
				//hook is launched when property is set, so at this point it's possible the value is not stored yet... so, we wait a bit
				try { Thread.sleep(1000); } catch (InterruptedException e) {}
				
				Site site = sakaiProxy.getSite(siteId);
				if(site != null && !site.isPublished()) {
					end = true;
					//get all synchronizations linked to this site
					List<SiteSynchronization> list = microsoftSiteSynchronizationRepository.findBySite(siteId);
					if(list != null) {
						for(SiteSynchronization ss : list) {
							log.debug("Removing users from temId={}, iter={}", ss.getTeamId(), 6 - count);
							end = end && removeUsersFromSynchronization(ss);
						}
					}
				}
				
				count--;
			}
			
			//save log
			microsoftLoggingRepository.save(MicrosoftLog.builder()
				.event(MicrosoftLog.EVENT_SITE_UNPUBLISHED)
				.status((end) ? MicrosoftLog.Status.OK : MicrosoftLog.Status.KO)
				.addData("siteId", siteId)
				.build());
		}
	}
	
	//User added to Site or Group
	private void userAddedToAuthzGroup(MicrosoftMessage msg) {
		try {
			switch(msg.getType()) {
				case SITE:
					userAddedToSite(msg.getUserId(), msg.getSiteId(), msg.isOwner());
					break;
				case GROUP:
					//we check force property to be sure we are going to process these special actions even if the ENABLE event has not arrived yet
					if(msg.isForce() || !disabledGroupListeners.contains(msg.getGroupId())) {
						userAddedToGroup(msg.getUserId(), msg.getSiteId(), msg.getGroupId(), msg.isOwner());
					}
					break;
				default:
					break;
			}
		}catch(Exception e) {
			//save log
			MicrosoftLogBuilder builder = MicrosoftLog.builder();
			builder.event(MicrosoftLog.ERROR_USER_ADDED_TO_AUTHZGROUP)
				.status(MicrosoftLog.Status.KO)
				.addData("type", msg.getType().name())
				.addData("userId", msg.getUserId())
				.addData("siteId", msg.getSiteId())
				.addData("owner", Boolean.toString(msg.isOwner()));
				if(msg.getType() == MicrosoftMessage.Type.GROUP) {
					builder.addData("groupId", msg.getGroupId());
				}
			microsoftLoggingRepository.save(builder.build());
		}
	}
	
	//User added to Site
	private void userAddedToSite(String userId, String siteId, boolean owner) throws MicrosoftGenericException{
		if(microsoftConfigRepository.isAllowedAddUserToTeam()) {
			log.debug("-> userId={}, siteId={}, owner={}", userId, siteId, owner);
			
			//check if Microsoft Credentials are valid (will throw an exception if not)
			microsoftCommonService.checkConnection();
			
			User user = sakaiProxy.getUser(userId);
			
			//get all synchronizations linked to this site
			List<SiteSynchronization> list = microsoftSiteSynchronizationRepository.findBySite(siteId);
			if(list != null) {
				SakaiUserIdentifier mappedSakaiUserId = microsoftConfigRepository.getMappedSakaiUserId();
				MicrosoftUserIdentifier mappedMicrosoftUserId = microsoftConfigRepository.getMappedMicrosoftUserId();
				
				//get user identifier
				String identifier = sakaiProxy.getMemberKeyValue(user, mappedSakaiUserId);
				
				MicrosoftUser mu = null;
				
				for(SiteSynchronization ss : list) {
					boolean res = false;
					//check if team exists
					if(checkTeam(ss.getTeamId())) {
						//check if identifier is empty (maybe is a User-Property and is not set)
						if(StringUtils.isBlank(identifier)) {
							mu = createInvitation(ss, user, mappedSakaiUserId, mappedMicrosoftUserId); //just created, so we don't need to check if it pertains to the team
							//after invitation, user properties could be changed. We need to get the user again
							user = sakaiProxy.getUser(userId);
							//get identifier for future iterations
							identifier = sakaiProxy.getMemberKeyValue(user, mappedSakaiUserId);
							
							if(mu != null) {
								//guest users always as members (not owners)
								res = addMemberToMicrosoftGroupOrTeam(ss, mu);
							}
						//if users does not pertain to that team
						} else if(microsoftCommonService.checkUserInTeam(identifier, ss.getTeamId(), mappedMicrosoftUserId) == null) {
							//(only the firt time) check if user exists
							if(mu == null) {
								//Getting Microsoft User with identifier
								mu = microsoftCommonService.getUser(identifier, mappedMicrosoftUserId);
								
								//user does not exist -> create invitation
								if(mu == null) {
									mu = createInvitation(ss, user, mappedSakaiUserId, mappedMicrosoftUserId);
								}
							}
							if(mu != null) {
								res = (owner) ? addOwnerToMicrosoftGroupOrTeam(ss, mu) : addMemberToMicrosoftGroupOrTeam(ss, mu);
							}
						}
					}
					
					//save log
					microsoftLoggingRepository.save(MicrosoftLog.builder()
							.event(MicrosoftLog.EVENT_USER_ADDED_TO_SITE)
							.status((res) ? MicrosoftLog.Status.OK : MicrosoftLog.Status.KO)
							.addData("email", (user != null) ? user.getEmail() : "-null-")
							.addData("sakaiUserId", (user != null) ? user.getId() : "-null-")
							.addData("microsoftUserId", (mu != null) ? mu.getId() : "-null-")
							.addData("siteId", ss.getSiteId())
							.addData("teamId", ss.getTeamId())
							.addData("owner", Boolean.toString(owner))
							.addData("guest", (mu != null) ? Boolean.toString(mu.isGuest()) : "-null-")
							.build());
				}
			}
		} else {
			log.debug("NOT allowed to add user to team");
		}
	}
	
	//User added to Group
	private void userAddedToGroup(String userId, String siteId, String groupId, boolean owner) throws MicrosoftGenericException{
		if(microsoftConfigRepository.isAllowedAddUserToChannel()) {
			Object lock = newGroupLock.computeIfAbsent(groupId, k -> new Object());
			synchronized(lock) {
				log.debug("-> userId={}, siteId={}, groupId={}, owner={}", userId, siteId, groupId, owner);
				
				//check if Microsoft Credentials are valid (will throw an exception if not)
				microsoftCommonService.checkConnection();
				
				User user = sakaiProxy.getUser(userId);
				
				//get all synchronizations linked to this group
				List<GroupSynchronization> list = microsoftGroupSynchronizationRepository.findByGroup(groupId);
				if(list != null) {
					SakaiUserIdentifier mappedSakaiUserId = microsoftConfigRepository.getMappedSakaiUserId();
					MicrosoftUserIdentifier mappedMicrosoftUserId = microsoftConfigRepository.getMappedMicrosoftUserId();
					
					//get user identifier
					String identifier = sakaiProxy.getMemberKeyValue(user, mappedSakaiUserId);
					
					MicrosoftUser mu = null;
					
					for(GroupSynchronization gs : list) {
						SiteSynchronization ss = gs.getSiteSynchronization();
	
						boolean res = false;
						//check if channel exists
						//check if user does not pertain to that channel
						if(checkChannel(ss.getTeamId(), gs.getChannelId()) && microsoftCommonService.checkUserInChannel(identifier, ss.getTeamId(), gs.getChannelId(), mappedMicrosoftUserId) == null) {
							//we assume all users added to a group, are previously added to a Site... so, we don't need to manage invitations here
							//(only the first time) check if user exists
							if(mu == null) {
								//Getting Microsoft User with identifier
								mu = microsoftCommonService.getUser(identifier, mappedMicrosoftUserId);
							}
							if(mu != null) {
								res = (owner) ? addOwnerToMicrosoftChannel(ss, gs, mu) : addMemberToMicrosoftChannel(ss, gs, mu);
							}
						}
						
						//save log
						microsoftLoggingRepository.save(MicrosoftLog.builder()
								.event(MicrosoftLog.EVENT_USER_ADDED_TO_GROUP)
								.status((res) ? MicrosoftLog.Status.OK : MicrosoftLog.Status.KO)
								.addData("email", (user != null) ? user.getEmail() : "-null-")
								.addData("sakaiUserId", (user != null) ? user.getId() : "-null-")
								.addData("microsoftUserId", (mu != null) ? mu.getId() : "-null-")
								.addData("siteId", ss.getSiteId())
								.addData("teamId", ss.getTeamId())
								.addData("groupId", gs.getGroupId())
								.addData("channelId", gs.getChannelId())
								.addData("owner", Boolean.toString(owner))
								.addData("guest", (mu != null) ? Boolean.toString(mu.isGuest()) : "-null-")
								.build());
					}
				}
				newGroupLock.remove(groupId);
			}
		} else {
			log.debug("NOT allowed to add user to channel");
		}
	}
	
	//User removed from Site or Group
	private void userRemovedFromAuthzGroup(MicrosoftMessage msg) {
		try {
			switch(msg.getType()) {
				case SITE:
					userRemovedFromSite(msg.getUserId(), msg.getSiteId());
					break;
				case GROUP:
					//we check force property to be sure we are going to process these special actions even if the ENABLE event has not arrived yet
					if(msg.isForce() || !disabledGroupListeners.contains(msg.getGroupId())) {
						switch(msg.getAction()) {
							case REMOVE:
								userRemovedFromGroup(msg.getUserId(), msg.getSiteId(), msg.getGroupId());
								break;
							case REMOVE_ALL:
								allUsersRemovedFromGroup(msg.getSiteId(), msg.getGroupId());
								break;
							default:
								break;
						}
					}
					break;
				default:
					break;
			}
		}catch(Exception e) {
			//save log
			MicrosoftLogBuilder builder = MicrosoftLog.builder();
			builder.event(MicrosoftLog.ERROR_USER_REMOVED_FROM_AUTHZGROUP)
				.status(MicrosoftLog.Status.KO)
				.addData("type", msg.getType().name())
				.addData("action", msg.getAction().name())
				.addData("userId", msg.getUserId())
				.addData("siteId", msg.getUserId())
				.addData("owner", Boolean.toString(msg.isOwner()));
				if(msg.getType() == MicrosoftMessage.Type.GROUP) {
					builder.addData("groupId", msg.getGroupId());
				}
			microsoftLoggingRepository.save(builder.build());
		}
	}
	
	//User removed from Site
	private void userRemovedFromSite(String userId, String siteId) throws MicrosoftGenericException{
		if(microsoftConfigRepository.isAllowedRemoveUserFromTeam()) {
			log.debug("-> userId={}, siteId={}", userId, siteId);
			
			//check if Microsoft Credentials are valid (will throw an exception if not)
			microsoftCommonService.checkConnection();
			
			MicrosoftCredentials credentials = microsoftConfigRepository.getCredentials();
			
			User user = sakaiProxy.getUser(userId);
			
			//get all synchronizations linked to this site
			List<SiteSynchronization> list = microsoftSiteSynchronizationRepository.findBySite(siteId);
			if(list != null) {
				SakaiUserIdentifier mappedSakaiUserId = microsoftConfigRepository.getMappedSakaiUserId();
				MicrosoftUserIdentifier mappedMicrosoftUserId = microsoftConfigRepository.getMappedMicrosoftUserId();
				
				//get user identifier
				String identifier = sakaiProxy.getMemberKeyValue(user, mappedSakaiUserId);
				 
				for(SiteSynchronization ss : list) {
					//only forced relationships will remove users from Microsoft
					if(ss.isForced()) {
						boolean res = false;
						MicrosoftUser mu = null;
						
						//check if team exists
						if(checkTeam(ss.getTeamId())) {
							//check if user pertains to that team
							mu = microsoftCommonService.checkUserInTeam(identifier, ss.getTeamId(), mappedMicrosoftUserId);
							
							//check if user to remove is microsoft "admin"
							if(mu != null && !credentials.getEmail().equalsIgnoreCase(mu.getEmail())) {
								res = removeMemberFromMicrosoftTeam(ss, mu);
							}
						}
						
						//save log
						microsoftLoggingRepository.save(MicrosoftLog.builder()
								.event(MicrosoftLog.EVENT_USER_REMOVED_FROM_SITE)
								.status((res) ? MicrosoftLog.Status.OK : MicrosoftLog.Status.KO)
								.addData("email", (user != null) ? user.getEmail() : "-null-")
								.addData("sakaiUserId", (user != null) ? user.getId() : "-null-")
								.addData("microsoftUserId", (mu != null) ? mu.getId() : "-null-")
								.addData("siteId", ss.getSiteId())
								.addData("teamId", ss.getTeamId())
								.addData("owner", (mu != null) ? Boolean.toString(mu.isOwner()) : "-null-")
								.addData("guest", (mu != null) ? Boolean.toString(mu.isGuest()) : "-null-")
								.build());
						
					}
				}
			}
		} else {
			log.debug("NOT allowed to remove user from team");
		}
	}
	
	//User removed from Group
	private void userRemovedFromGroup(String userId, String siteId, String groupId) throws MicrosoftGenericException{
		if(microsoftConfigRepository.isAllowedRemoveUserFromChannel()) {
			log.debug("-> userId={}, siteId={}, groupId={}", userId, siteId, groupId);
			
			//check if Microsoft Credentials are valid (will throw an exception if not)
			microsoftCommonService.checkConnection();
			
			MicrosoftCredentials credentials = microsoftConfigRepository.getCredentials();
			
			User user = sakaiProxy.getUser(userId);
			
			//get all synchronizations linked to this group
			List<GroupSynchronization> list = microsoftGroupSynchronizationRepository.findByGroup(groupId);
			if(list != null) {
				SakaiUserIdentifier mappedSakaiUserId = microsoftConfigRepository.getMappedSakaiUserId();
				MicrosoftUserIdentifier mappedMicrosoftUserId = microsoftConfigRepository.getMappedMicrosoftUserId();
				
				//get user identifier
				String identifier = sakaiProxy.getMemberKeyValue(user, mappedSakaiUserId);
				
				for(GroupSynchronization gs : list) {
					SiteSynchronization ss = gs.getSiteSynchronization();
					//only forced relationships will remove users from Microsoft
					if(ss.isForced()) {
						boolean res = false;
						MicrosoftUser mu = null;
						
						//check if channel exists
						if(checkChannel(ss.getTeamId(), gs.getChannelId())) {
							//check if user pertains to that channel
							mu = microsoftCommonService.checkUserInChannel(identifier, ss.getTeamId(), gs.getChannelId(), mappedMicrosoftUserId);
							
							//check if user to remove is microsoft "admin"
							if(mu != null && !credentials.getEmail().equalsIgnoreCase(mu.getEmail())) {
								res = removeMemberFromMicrosoftChannel(ss, gs, mu);
							}
						}
						
						//save log
						microsoftLoggingRepository.save(MicrosoftLog.builder()
								.event(MicrosoftLog.EVENT_USER_REMOVED_FROM_GROUP)
								.status((res) ? MicrosoftLog.Status.OK : MicrosoftLog.Status.KO)
								.addData("email", (user != null) ? user.getEmail() : "-null-")
								.addData("sakaiUserId", (user != null) ? user.getId() : "-null-")
								.addData("microsoftUserId", (mu != null) ? mu.getId() : "-null-")
								.addData("siteId", ss.getSiteId())
								.addData("teamId", ss.getTeamId())
								.addData("groupId", gs.getGroupId())
								.addData("channelId", gs.getChannelId())
								.addData("owner", (mu != null) ? Boolean.toString(mu.isOwner()) : "-null-")
								.addData("guest", (mu != null) ? Boolean.toString(mu.isGuest()) : "-null-")
								.build());
					}
				}
			}
		} else {
			log.debug("NOT allowed to remove user from channel");
		}
	}
	
	//All users removed from group
	//TODO: support this. At this point, this only can be called from webservices, not form GUI.
	private void allUsersRemovedFromGroup(String siteId, String groupId) throws MicrosoftGenericException{
		log.warn("(NOT SUPPORTED YET) allUsersRemovedFromGroup: siteId="+siteId+", groupId="+groupId);
	}
	
	//Team creation process has finish (send by MicrosoftCommonService::createTeamFromGroupAsync)
	private void teamCreated(MicrosoftMessage msg) {
		if(msg.getType() == MicrosoftMessage.Type.TEAM && msg.getAction() == MicrosoftMessage.Action.CREATE) {
			switch(msg.getStatus()) {
				case 0:
					log.error("MSG received: Failed to create team associated with group: {}", msg.getReference());
					break;
				case 1:
					log.info("MSG received: Succes in creating team associated with group: {}", msg.getReference());
					try {
						//force cache reset
						microsoftCommonService.getTeam(msg.getReference(), true);
					} catch (Exception e) {
						log.error("Error getting team: {}", msg.getReference());
					}
					break;
				//this should never happen
				default:
					return;
			}
			
			//save log
			microsoftLoggingRepository.save(MicrosoftLog.builder()
					.event(MicrosoftLog.EVENT_CREATE_TEAM_FROM_GROUP)
					.status((msg.getStatus() == 1) ? MicrosoftLog.Status.OK : MicrosoftLog.Status.KO)
					.addData("groupId", msg.getReference())
					.build());
		}
	}
}
