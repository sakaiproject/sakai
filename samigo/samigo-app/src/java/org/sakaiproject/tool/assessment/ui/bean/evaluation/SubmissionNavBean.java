package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import java.io.Serializable;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import lombok.Data;

import lombok.extern.slf4j.Slf4j;

/* For evaluation: Submission navigation backing bean. */
@Slf4j
@ManagedBean(name="submissionNav")
@SessionScoped
@Data
public class SubmissionNavBean extends SpringBeanAutowiringSupport implements Serializable {


    private static final long serialVersionUID = 5517587781720762297L;

    private String nextGradingId;
    private String currentGradingId;
    private String previousGradingId;
    private Boolean displaySubmissionDate;
    private SelectItem[] submissionsSelection;

    @Autowired
    @Qualifier("org.sakaiproject.time.api.UserTimeService")
    private UserTimeService userTimeService;


    public SubmissionNavBean() {
        log.debug("Creating a new SubmissionNavBean");
    }


    public void populate(List<AgentResults> agentResultsList, String currentGradingId, boolean displaySubmissionDate) {

        this.displaySubmissionDate = displaySubmissionDate;
        this.currentGradingId = currentGradingId;

        Map<String, AgentResults> agentResultsMap = agentResultsList.stream()
                .filter(agent -> AssessmentGradingData.SUBMITTED.equals(agent.getStatus()))
                .filter(agent -> agent.getSubmissionCount() > 0)
                .collect(Collectors.toMap((agent) -> agent.getAssessmentGradingId().toString(), Function.identity(),
                        (a, b) -> a, LinkedHashMap::new));

        List<String> submissionIds = new ArrayList<>(agentResultsMap.keySet());

        int currentIndex = submissionIds.indexOf(currentGradingId);

        // If current submission is not found or is the first item,
        // or we don't have more then one item
        // we don't have a previous agent
        previousGradingId = currentIndex > 0 && submissionIds.size() > 1
                ? submissionIds.get(currentIndex - 1)
                : null;

        // If current submission is not found or the last item
        // or we don't have more then one item
        // we don't have a next agent
        nextGradingId = currentIndex != -1 && currentIndex != submissionIds.size() - 1 && submissionIds.size() > 1
                ? submissionIds.get(currentIndex + 1)
                : null;

        submissionsSelection = agentResultsMap.values().stream()
                .map(agent -> {
                    String displayName = agent.getLastName() + ", " + agent.getFirstName()
                        + " (" + agent.getAgentDisplayId()  + ")";
                    String optionLabel = displaySubmissionDate
                            ? displayName + " - " + userTimeService.dateTimeFormat(agent.getSubmittedDate().toInstant(),
                                    FormatStyle.MEDIUM, FormatStyle.SHORT)
                            : displayName;
                    return new SelectItem(agent.getAssessmentGradingId(), optionLabel);
                })
                .toArray(size -> new SelectItem[size]);
    }

}
