package org.sakaiproject.poll.api.entity;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.model.Vote;

public class PollEntity implements Entity {

    private final Poll poll;
    private final Reference reference;
    private final boolean currentUserVoted;
    private final List<Vote> votes;

    /**
     * Basic constructor for backward compatibility.
     * Creates PollEntity without presentation fields.
     */
    public PollEntity(Reference reference, Poll poll) {
        this(reference, poll, false, null);
    }

    /**
     * Full constructor with all presentation fields.
     * Used by service layer to build complete entities.
     *
     * @param reference EntityBroker reference
     * @param poll The wrapped Poll domain entity
     * @param currentUserVoted Whether current user has voted
     * @param votes Optional list of votes (nullable)
     */
    public PollEntity(Reference reference, Poll poll, boolean currentUserVoted, List<Vote> votes) {
        Objects.requireNonNull(reference, "Reference must not be null");
        Objects.requireNonNull(poll, "Poll must not be null");
        this.reference = reference;
        this.poll = poll;
        this.currentUserVoted = currentUserVoted;
        this.votes = votes;
    }

    @Override
    public String getId() {
        return poll.getId();
    }

    @Override
    public String getReference() {
        return reference.getReference();
    }

    @Override
    public String getReference(String rootProperty) {
        return getReference();
    }

    @Override
    public String getUrl() {
        return reference.getUrl();
    }

    @Override
    public String getUrl(String rootProperty) {
        return getUrl();
    }

    public String getOwner() {
        return poll.getOwner();
    }

    public String getSiteId() {
        return poll.getSiteId();
    }

    public Date getCreationDate() {
        return poll.getCreationDate() != null ? Date.from(poll.getCreationDate()) : null;
    }

    public String getText() {
        return poll.getText();
    }

    public String getDescription() {
        return poll.getDescription();
    }

    public int getMinOptions() {
        return poll.getMinOptions();
    }

    public int getMaxOptions() {
        return poll.getMaxOptions();
    }

    public Date getVoteOpen() {
        return poll.getVoteOpen() != null ? Date.from(poll.getVoteOpen()) : null;
    }

    public Date getVoteClose() {
        return poll.getVoteClose() != null ? Date.from(poll.getVoteClose()) : null;
    }

    public String getDisplayResult() {
        return poll.getDisplayResult();
    }

    public boolean isLimitVoting() {
        return poll.isLimitVoting();
    }

    public boolean isPublic() {
        return poll.isPublic();
    }

    public List<Option> getOptions() {
        return poll.getOptions();
    }

    public boolean isCurrentUserVoted() {
        return currentUserVoted;
    }

    public List<Vote> getVotes() {
        return votes;
    }

    /**
     * Get the wrapped Poll domain entity.
     * For use by service layer when extracting data for updates.
     */
    public Poll getPoll() {
        return poll;
    }

}
