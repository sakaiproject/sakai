package org.sakaiproject.scoringservice.api;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 5/22/13
 * Time: 12:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScoringComponent {
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
