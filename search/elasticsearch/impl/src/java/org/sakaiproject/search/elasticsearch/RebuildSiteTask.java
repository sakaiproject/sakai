package org.sakaiproject.search.elasticsearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.search.api.EntityContentProducer;

import java.util.Iterator;
import java.util.TimerTask;

public class RebuildSiteTask extends TimerTask {
    private final ElasticSearchIndexBuilder elasticSearchIndexBuilder;
    private final String siteId;
    private static Logger log = LoggerFactory.getLogger(RebuildSiteTask.class);

    public RebuildSiteTask(ElasticSearchIndexBuilder elasticSearchIndexBuilder, String siteId) {
        this.elasticSearchIndexBuilder = elasticSearchIndexBuilder;
        this.siteId = siteId;
    }

    /**
     * Rebuild the index from the entities own stored state {@inheritDoc}, for just
     * the supplied siteId
     */
    public void run() {
        log.info("Rebuilding the index for '" + siteId + "'");

        try {
            elasticSearchIndexBuilder.enableAzgSecurityAdvisor();
            elasticSearchIndexBuilder.deleteAllDocumentForSite(siteId);

            for (final EntityContentProducer entityContentProducer : elasticSearchIndexBuilder.getProducers()) {
                try {
                    for (Iterator<String> i = entityContentProducer.getSiteContentIterator(siteId); i.hasNext(); ) {
                        String ref = i.next();
                        // this won't index the actual content just load the doc
                        // another thread will pick up the content, this allows the task to finish
                        // quickly and spread the content digesting across the cluster
                        elasticSearchIndexBuilder.prepareIndexAdd(ref, entityContentProducer, false);
                    }
                } catch (Exception e) {
                    log.error("An exception occurred while rebuilding the index of '" + siteId + "'", e);
                }
            }

            elasticSearchIndexBuilder.flushIndex();
            elasticSearchIndexBuilder.refreshIndex();
        } catch (Exception e) {
            log.error("An exception occurred while rebuilding the index of '" + siteId + "'", e);
        } finally {
            elasticSearchIndexBuilder.disableAzgSecurityAdvisor();
        }

    }

}
