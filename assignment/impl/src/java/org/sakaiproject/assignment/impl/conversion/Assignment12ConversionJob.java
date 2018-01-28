package org.sakaiproject.assignment.impl.conversion;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.assignment.api.conversion.AssignmentConversionService;

@Slf4j
@DisallowConcurrentExecution
public class Assignment12ConversionJob implements Job {

    @Setter
    private AssignmentConversionService assignmentConversionService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("<===== Assignment Conversion Job start =====>");

        // never run as a recovery
        if (context.isRecovering()) {
            log.warn("<===== Assignment Conversion Job doesn't support recovery, job will terminate... =====>");
        } else {
            assignmentConversionService.runConversion();
        }

        log.info("<===== Assignment Conversion Job end =====>");
    }
}
