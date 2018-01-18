package org.sakaiproject.assignment.impl.conversion.impl;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.assignment.api.conversion.AssignmentConversionService;

@Slf4j
public class Assignment12ConversionJob implements Job {

    @Setter
    private AssignmentConversionService assignmentConversionService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("<===== Assignment migration start =====>");

        assignmentConversionService.runConversion();

        log.info("<===== Assignment migration end =====>");
    }
}
