package org.sakaiproject.scheduler.util;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.simpl.CascadingClassLoadHelper;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.xml.ValidationException;
import org.quartz.xml.XMLSchedulingDataProcessor;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

/**
 * This allows quartz jobs to be automatically provisioned at startup. It has the advantage over the
 * builtin Spring Quartz support of optionally not overwriting any changes that are made. This is in a JAR as
 * there's no way to load resources from a component apart from having a class in there.
 * Although SchedulerManagerImpl can automatically provision jobs these have to be in the job scheduler and can't
 * be in other projects. So in the future this method is preferred.
 *
 * @see XMLSchedulingDataProcessor
 */
@Slf4j
public class AutoProvisionJobs {

    @Setter
    private SchedulerManager schedulerManager;

    @Setter
    private List<String> files;

    public void init() throws ParserConfigurationException, XPathException, ParseException, IOException, ValidationException, SchedulerException, SAXException, ClassNotFoundException {

        boolean noFiles = files == null || files.isEmpty();
        if (noFiles || !schedulerManager.isAutoProvisioning()) {
            log.info("Not auto provisioning jobs: "+ ((noFiles)?"no files.":String.join(", ", files)));
            return;
        }

        Scheduler scheduler = schedulerManager.getScheduler();
        ClassLoadHelper clh = new CascadingClassLoadHelper();
        clh.initialize();

        for (String file : files ) {
            XMLSchedulingDataProcessor proc = new XMLSchedulingDataProcessor(clh);
            InputStream in = getClass().getResourceAsStream(file);
            if (in == null) {
                throw new IllegalArgumentException("Couldn't find resource on classpath: "+ file);
            }
            try {
                proc.processStreamAndScheduleJobs(in, file, scheduler);
                log.info("Successfully provisioned jobs/triggers from :"+ file);
            } catch (ObjectAlreadyExistsException e) {
                log.info("Not fully processing: "+ file+ " because some parts already exist");
            }
        }
    }

}
