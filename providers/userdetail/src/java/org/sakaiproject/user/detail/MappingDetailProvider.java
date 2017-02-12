package org.sakaiproject.user.detail;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.User;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This takes some values that are reported back and maps them to other values.
 * This is mainly just a proxy, but it allows deployments to have data stored in one form but manipulate to another
 * value based on a simple map.
 */
public class MappingDetailProvider implements CandidateDetailProvider {

    private CandidateDetailProvider wrapped;
    private Map<String, String> map;

    public MappingDetailProvider(CandidateDetailProvider wrapped, Map<String, String> map) {
        this.wrapped = Objects.requireNonNull(wrapped);
        this.map = Objects.requireNonNull(map);
    }

    @Override
    public Optional<String> getCandidateID(User user, Site site) {
        return wrapped.getCandidateID(user, site);
    }

    @Override
    public boolean useInstitutionalAnonymousId(Site site) {
        return wrapped.useInstitutionalAnonymousId(site);
    }

    @Override
    public Optional<List<String>> getAdditionalNotes(User user, Site site) {
        // Map our known additional notes.
        return wrapped.getAdditionalNotes(user, site).map(
                l -> l.stream().map(v -> map.containsKey(v)?map.get(v):v).collect(Collectors.toList())
        );
    }

    @Override
    public boolean isAdditionalNotesEnabled(Site site) {
        return wrapped.isAdditionalNotesEnabled(site);
    }

}

