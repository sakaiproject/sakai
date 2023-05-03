package org.sakaiproject.bulk.membership.model;

import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

public class Summary {

    private final String EMAIL_SEPARATOR = "@";

    @Getter @Setter
    private String userCriteria;
    
    @Getter @Setter
    private String userName;

    @Getter @Setter
    private ArrayList<String> failedSites;

    @Getter @Setter
    private ArrayList<String> workedSites;

    public Summary () {
        userCriteria = "";
        userName = "";
        failedSites = new ArrayList<String>();
        workedSites = new ArrayList<String>();
    }

    public Summary (String userCriteria) {
        this.userCriteria = userCriteria;
        userName = "";
        failedSites = new ArrayList<String>();
        workedSites = new ArrayList<String>();
    }

    public Summary (String userCriteria, String userName) {
        this.userCriteria = userCriteria;
        this.userName = userName;
        failedSites = new ArrayList<String>();
        workedSites = new ArrayList<String>();
    }

    public void addFailedSite(String failedSite) {
        failedSites.add(failedSite);
    }

    public void addWorkedSite(String workedSite) {
        workedSites.add(workedSite);
    }

    public String getCleanedUserName() {
        int indexEmailSep = userName.indexOf(EMAIL_SEPARATOR);
        return (indexEmailSep == -1 ? userName : userName.substring(0, indexEmailSep));
    }
} 