package org.sakaiproject.poll.api.entity;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;

public class PollEntity implements Entity {

    private final Poll poll;
    private final Reference reference;

    public PollEntity(Reference reference, Poll poll) {
        Objects.requireNonNull(reference, "Reference must not be null");
        Objects.requireNonNull(poll, "Poll must not be null");
        this.reference = reference;
        this.poll = poll;
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
        return poll.getCreationDate();
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
        return poll.getVoteOpen();
    }

    public Date getVoteClose() {
        return poll.getVoteClose();
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
        return poll.isCurrentUserVoted();
    }

}
