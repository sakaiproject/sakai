package org.sakaiproject.search.elasticsearch;

import java.util.Set;

/**
 * Created by dmccallum on 10/11/16.
 */
public interface ElasticSearchIndexBuilderEventRegistrar {
    void updateEventsFor(ElasticSearchIndexBuilder indexBuilder);
}
