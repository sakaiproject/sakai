package org.sakaiproject.roster.api;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RosterData {

    private List<RosterMember> members;
    private int membersTotal;
    private Map<String, Integer> roleCounts;
    private String status;
}

