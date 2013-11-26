package org.sakaiproject.scoringservice.api;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 5/21/13
 * Time: 7:02 PM
 * To change this template use File | Settings | File Templates.
 */
abstract public class AbstractScoringAgent implements ScoringAgent {
    private String agentId;
    private String name;
    private int order = 0;
    private ScoringService scoringService;

    public void init(){
        scoringService.register(this, true);
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setScoringService(ScoringService scoringService) {
        this.scoringService = scoringService;
    }
}
