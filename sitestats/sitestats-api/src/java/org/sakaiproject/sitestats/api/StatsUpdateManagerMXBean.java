package org.sakaiproject.sitestats.api;

/**
 * Interface for exporting to JMX.
 * This needs to be in the API so that it is accessible to the correct classloaders.
 */
public interface StatsUpdateManagerMXBean {
    long getNumberOfEventsProcessed();

    long getTotalTimeInEventProcessing();

    long getResetTime();

    long getTotalTimeElapsedSinceReset();

    double getNumberOfEventsProcessedPerSec();

    double getNumberOfEventsGeneratedPerSec();

    long getAverageTimeInEventProcessingPerEvent();
}
