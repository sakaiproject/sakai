package org.sakaiproject.user.detail;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.User;

public class ChainedDetailProvider implements CandidateDetailProvider {

	private List<CandidateDetailProvider> providers;
	private Log M_log = LogFactory.getLog(ChainedDetailProvider.class);

	public void setProviders(List<CandidateDetailProvider> providers) {
		this.providers = providers;
	}

	public List<CandidateDetailProvider> getProviders() {
		if(providers == null)
			providers = new ArrayList<>();
		return providers;
	}
	public Optional<String> getCandidateID(User user, Site site) {
		if(site == null) {
			M_log.error("getCandidateID: Null site.");
			return Optional.empty();
		}
		for(CandidateDetailProvider provider : getProviders()) {
			String candidateID = provider.getCandidateID(user, site).orElse("");
			if(StringUtils.isNotBlank(candidateID)){
				return Optional.ofNullable(candidateID);
			}
		}
		return Optional.empty();
	}

	public boolean useInstitutionalAnonymousId(Site site) {
		for(CandidateDetailProvider provider : getProviders()) {
			if(provider.useInstitutionalAnonymousId(site)){
				return true;
			}
		}
		return false;
	}

	public Optional<List<String>> getAdditionalNotes(User user, Site site) {
		if(site == null) {
			M_log.error("getAdditionalNotes: Null site.");
			return Optional.empty();
		}
		for(CandidateDetailProvider provider : getProviders()) {
			Optional<List<String>> notes = provider.getAdditionalNotes(user, site);
			if(notes.isPresent()){
				return notes;
			}
		}
		return Optional.empty();
	}

	public boolean isAdditionalNotesEnabled(Site site) {
		for(CandidateDetailProvider provider : getProviders()) {
			if(provider.isAdditionalNotesEnabled(site)){
				return true;
			}
		}
		return false;
	}
}