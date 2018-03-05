package org.sakaiproject.assignment.impl.conversion;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.assignment.api.conversion.AssignmentConversionService;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
@DisallowConcurrentExecution
public class Assignment12ConversionJob implements Job {

    public static final String SIZE_PROPERTY = "length.attribute.property";
    public static final String NUMBER_PROPERTY = "number.attributes.property";

    @Setter
    private AssignmentConversionService assignmentConversionService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("<===== Assignment Conversion Job start =====>");

        // never run as a recovery
        if (context.isRecovering()) {
            log.warn("<===== Assignment Conversion Job doesn't support recovery, job will terminate... =====>");
        } else {
            JobDataMap map = context.getMergedJobDataMap();
            Integer size = Integer.parseInt((String) map.get(SIZE_PROPERTY));
            Integer number = Integer.parseInt((String) map.get(NUMBER_PROPERTY));
            assignmentConversionService.runConversion(number, size);
        }

        log.info("<===== Assignment Conversion Job end =====>");
    }
}
