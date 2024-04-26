/*
 * Copyright (c) 2003-2023 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.search.elasticsearch;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.opensearch.action.admin.cluster.node.stats.NodeStats;
import org.opensearch.action.admin.cluster.node.stats.NodesStatsResponse;
import org.opensearch.cluster.node.DiscoveryNode;
import org.opensearch.common.unit.ByteSizeValue;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.indices.NodeIndicesStats;
import org.sakaiproject.search.elasticsearch.serialization.NodeStatsResponseFactory;

import lombok.extern.slf4j.Slf4j;

@RunWith(JUnit4.class)
@Slf4j
public class NodeStatsResponseFactoryTest {

    private static NodeStatsResponseFactory factory;

    @BeforeClass
    public static void setupOnce() {
        factory = new NodeStatsResponseFactory();
    }

    @Test
    public void parseNodeStatsIndicesEndpoint() {
        try (ByteArrayInputStream in = new ByteArrayInputStream(sampleNodeStatsIndicesJSON().getBytes())) {
            NodesStatsResponse response = factory.createNodeStatsIndicesFromJSON(in);

            // cluster name
            Assert.assertEquals("sakai", response.getClusterName().value());

            // NodeStats
            Assert.assertTrue(response.getNodes().size() == 1);
            NodeStats stats = response.getNodes().get(0);
            Assert.assertTrue(stats != null);

            // Discovery Node
            DiscoveryNode node = stats.getNode();
            Assert.assertTrue(node != null);
            Assert.assertEquals("jhRM_56oSU-X6lcQ8XGcGQ", node.getId());
            Assert.assertEquals("sakaios", node.getName());

            // stats for Indices
            NodeIndicesStats indicesStats = response.getNodes().get(0).getIndices();
            Assert.assertTrue(indicesStats != null);

            // Doc Stats
            Assert.assertEquals(2428, indicesStats.getDocs().getCount());
            Assert.assertEquals(2, indicesStats.getDocs().getDeleted());

            // Store stats
            Assert.assertEquals(3629678, indicesStats.getStore().getSizeInBytes());
            Assert.assertEquals(new ByteSizeValue(0), indicesStats.getStore().getReservedSize());

            // Indexing stats
            Assert.assertEquals(4, indicesStats.getIndexing().getTotal().getIndexCount());
            Assert.assertEquals(new TimeValue(29), indicesStats.getIndexing().getTotal().getIndexTime());
            Assert.assertEquals(0, indicesStats.getIndexing().getTotal().getIndexCurrent());
            Assert.assertEquals(0, indicesStats.getIndexing().getTotal().getIndexFailedCount());
            Assert.assertEquals(2, indicesStats.getIndexing().getTotal().getDeleteCount());
            Assert.assertEquals(new TimeValue(5), indicesStats.getIndexing().getTotal().getDeleteTime());
            Assert.assertEquals(0, indicesStats.getIndexing().getTotal().getDeleteCurrent());
            Assert.assertEquals(0, indicesStats.getIndexing().getTotal().getNoopUpdateCount());
            Assert.assertEquals(new TimeValue(0), indicesStats.getIndexing().getTotal().getThrottleTime());
            Assert.assertEquals(false, indicesStats.getIndexing().getTotal().isThrottled());

            // Get stats
            Assert.assertEquals(0, indicesStats.getGet().getCount());
            Assert.assertEquals(0, indicesStats.getGet().getTimeInMillis());
            Assert.assertEquals(0, indicesStats.getGet().getExistsCount());
            Assert.assertEquals(0, indicesStats.getGet().getExistsTimeInMillis());
            Assert.assertEquals(0, indicesStats.getGet().getMissingCount());
            Assert.assertEquals(0, indicesStats.getGet().getMissingTimeInMillis());

            // Search stats
            Assert.assertEquals(0, indicesStats.getSearch().getOpenContexts());
            Assert.assertEquals(26364, indicesStats.getSearch().getTotal().getQueryCount());
            Assert.assertEquals(1583, indicesStats.getSearch().getTotal().getQueryTimeInMillis());
            Assert.assertEquals(0, indicesStats.getSearch().getTotal().getQueryCurrent());
            Assert.assertEquals(27, indicesStats.getSearch().getTotal().getFetchCount());
            Assert.assertEquals(13, indicesStats.getSearch().getTotal().getFetchTimeInMillis());
            Assert.assertEquals(0, indicesStats.getSearch().getTotal().getFetchCurrent());
            Assert.assertEquals(0, indicesStats.getSearch().getTotal().getScrollCount());
            Assert.assertEquals(0, indicesStats.getSearch().getTotal().getScrollTimeInMillis());
            Assert.assertEquals(0, indicesStats.getSearch().getTotal().getScrollCurrent());
            Assert.assertEquals(0, indicesStats.getSearch().getTotal().getSuggestCount());
            Assert.assertEquals(0, indicesStats.getSearch().getTotal().getScrollTimeInMillis());
            Assert.assertEquals(0, indicesStats.getSearch().getTotal().getSuggestCurrent());

            // Merges stats
            Assert.assertEquals(0, indicesStats.getMerge().getCurrent());
            Assert.assertEquals(0, indicesStats.getMerge().getCurrentNumDocs());
            Assert.assertEquals(0, indicesStats.getMerge().getCurrentSizeInBytes());
            Assert.assertEquals(0, indicesStats.getMerge().getTotal());
            Assert.assertEquals(0, indicesStats.getMerge().getTotalTimeInMillis());
            Assert.assertEquals(0, indicesStats.getMerge().getTotalNumDocs());
            Assert.assertEquals(0, indicesStats.getMerge().getTotalSizeInBytes());
            Assert.assertEquals(0, indicesStats.getMerge().getTotalStoppedTimeInMillis());
            Assert.assertEquals(0, indicesStats.getMerge().getTotalThrottledTimeInMillis());
            Assert.assertEquals(272629760, indicesStats.getMerge().getTotalBytesPerSecAutoThrottle());

            // Refresh stats
            Assert.assertEquals(30, indicesStats.getRefresh().getTotal());
            Assert.assertEquals(84, indicesStats.getRefresh().getTotalTimeInMillis());
            Assert.assertEquals(29, indicesStats.getRefresh().getExternalTotal());
            Assert.assertEquals(85, indicesStats.getRefresh().getExternalTotalTimeInMillis());
            Assert.assertEquals(0, indicesStats.getRefresh().getListeners());

            // Flush stats
            Assert.assertEquals(14, indicesStats.getFlush().getTotal());
            Assert.assertEquals(0, indicesStats.getFlush().getPeriodic());
            Assert.assertEquals(55, indicesStats.getFlush().getTotalTimeInMillis());

            // Warmer stats
            Assert.assertEquals(0, indicesStats.getWarmer().current());
            Assert.assertEquals(16, indicesStats.getWarmer().total());
            Assert.assertEquals(0, indicesStats.getWarmer().totalTimeInMillis());

            // Query Cache stats
            Assert.assertEquals(0, indicesStats.getQueryCache().getMemorySizeInBytes());
            Assert.assertEquals(0, indicesStats.getQueryCache().getTotalCount());
            Assert.assertEquals(0, indicesStats.getQueryCache().getHitCount());
            Assert.assertEquals(0, indicesStats.getQueryCache().getMissCount());
            Assert.assertEquals(0, indicesStats.getQueryCache().getCacheSize());
            Assert.assertEquals(0, indicesStats.getQueryCache().getCacheCount());
            Assert.assertEquals(0, indicesStats.getQueryCache().getEvictions());

            // Field Data stats
            Assert.assertEquals(0, indicesStats.getFieldData().getMemorySizeInBytes());
            Assert.assertEquals(0, indicesStats.getFieldData().getEvictions());

            // Completion stats
            Assert.assertEquals(0, indicesStats.getCompletion().getSizeInBytes());

            // Segments stats
            Assert.assertEquals(23, indicesStats.getSegments().getCount());
            Assert.assertEquals(85244, indicesStats.getSegments().getMemoryInBytes());
            Assert.assertEquals(61504, indicesStats.getSegments().getTermsMemoryInBytes());
            Assert.assertEquals(11336, indicesStats.getSegments().getStoredFieldsMemoryInBytes());
            Assert.assertEquals(488, indicesStats.getSegments().getTermVectorsMemoryInBytes());
            Assert.assertEquals(8320, indicesStats.getSegments().getNormsMemoryInBytes());
            Assert.assertEquals(0, indicesStats.getSegments().getPointsMemoryInBytes());
            Assert.assertEquals(3596, indicesStats.getSegments().getDocValuesMemoryInBytes());
            Assert.assertEquals(0, indicesStats.getSegments().getIndexWriterMemoryInBytes());
            Assert.assertEquals(0, indicesStats.getSegments().getVersionMapMemoryInBytes());
            Assert.assertEquals(0, indicesStats.getSegments().getBitsetMemoryInBytes());

            // Translog stats
            Assert.assertEquals(0, indicesStats.getTranslog().estimatedNumberOfOperations());
            Assert.assertEquals(715, indicesStats.getTranslog().getTranslogSizeInBytes());
            Assert.assertEquals(0, indicesStats.getTranslog().getUncommittedOperations());
            Assert.assertEquals(715, indicesStats.getTranslog().getUncommittedSizeInBytes());
            Assert.assertEquals(158554217, indicesStats.getTranslog().getEarliestLastModifiedAge());

            // Request Cache stats
            Assert.assertEquals(14100, indicesStats.getRequestCache().getMemorySizeInBytes());
            Assert.assertEquals(0, indicesStats.getRequestCache().getEvictions());
            Assert.assertEquals(26222, indicesStats.getRequestCache().getHitCount());
            Assert.assertEquals(23, indicesStats.getRequestCache().getMissCount());

            // Recovery stats
            Assert.assertEquals(new TimeValue(0), indicesStats.getRecoveryStats().throttleTime());
            Assert.assertEquals(0, indicesStats.getRecoveryStats().currentAsSource());
            Assert.assertEquals(0, indicesStats.getRecoveryStats().currentAsTarget());

        } catch (IOException ioe) {
            log.warn("Could not stream sample json data, {}", ioe.toString());
        }
    }

    public String sampleNodeStatsIndicesJSON() {
        return "{\n" +
                "  \"_nodes\": {\n" +
                "    \"total\": 1,\n" +
                "    \"successful\": 1,\n" +
                "    \"failed\": 0\n" +
                "  },\n" +
                "  \"cluster_name\": \"sakai\",\n" +
                "  \"nodes\": {\n" +
                "    \"jhRM_56oSU-X6lcQ8XGcGQ\": {\n" +
                "      \"timestamp\": 1690728237426,\n" +
                "      \"name\": \"sakaios\",\n" +
                "      \"transport_address\": \"127.0.0.1:9300\",\n" +
                "      \"host\": \"127.0.0.1\",\n" +
                "      \"ip\": \"127.0.0.1:9300\",\n" +
                "      \"roles\": [\n" +
                "        \"data\",\n" +
                "        \"ingest\",\n" +
                "        \"master\",\n" +
                "        \"remote_cluster_client\"\n" +
                "      ],\n" +
                "      \"attributes\": {\n" +
                "        \"shard_indexing_pressure_enabled\": \"true\"\n" +
                "      },\n" +
                "      \"indices\": {\n" +
                "        \"docs\": {\n" +
                "          \"count\": 2428,\n" +
                "          \"deleted\": 2\n" +
                "        },\n" +
                "        \"store\": {\n" +
                "          \"size_in_bytes\": 3629678,\n" +
                "          \"reserved_in_bytes\": 0\n" +
                "        },\n" +
                "        \"indexing\": {\n" +
                "          \"index_total\": 4,\n" +
                "          \"index_time_in_millis\": 29,\n" +
                "          \"index_current\": 0,\n" +
                "          \"index_failed\": 0,\n" +
                "          \"delete_total\": 2,\n" +
                "          \"delete_time_in_millis\": 5,\n" +
                "          \"delete_current\": 0,\n" +
                "          \"noop_update_total\": 0,\n" +
                "          \"is_throttled\": false,\n" +
                "          \"throttle_time_in_millis\": 0\n" +
                "        },\n" +
                "        \"get\": {\n" +
                "          \"total\": 0,\n" +
                "          \"time_in_millis\": 0,\n" +
                "          \"exists_total\": 0,\n" +
                "          \"exists_time_in_millis\": 0,\n" +
                "          \"missing_total\": 0,\n" +
                "          \"missing_time_in_millis\": 0,\n" +
                "          \"current\": 0\n" +
                "        },\n" +
                "        \"search\": {\n" +
                "          \"open_contexts\": 0,\n" +
                "          \"query_total\": 26364,\n" +
                "          \"query_time_in_millis\": 1583,\n" +
                "          \"query_current\": 0,\n" +
                "          \"fetch_total\": 27,\n" +
                "          \"fetch_time_in_millis\": 13,\n" +
                "          \"fetch_current\": 0,\n" +
                "          \"scroll_total\": 0,\n" +
                "          \"scroll_time_in_millis\": 0,\n" +
                "          \"scroll_current\": 0,\n" +
                "          \"suggest_total\": 0,\n" +
                "          \"suggest_time_in_millis\": 0,\n" +
                "          \"suggest_current\": 0\n" +
                "        },\n" +
                "        \"merges\": {\n" +
                "          \"current\": 0,\n" +
                "          \"current_docs\": 0,\n" +
                "          \"current_size_in_bytes\": 0,\n" +
                "          \"total\": 0,\n" +
                "          \"total_time_in_millis\": 0,\n" +
                "          \"total_docs\": 0,\n" +
                "          \"total_size_in_bytes\": 0,\n" +
                "          \"total_stopped_time_in_millis\": 0,\n" +
                "          \"total_throttled_time_in_millis\": 0,\n" +
                "          \"total_auto_throttle_in_bytes\": 272629760\n" +
                "        },\n" +
                "        \"refresh\": {\n" +
                "          \"total\": 30,\n" +
                "          \"total_time_in_millis\": 84,\n" +
                "          \"external_total\": 29,\n" +
                "          \"external_total_time_in_millis\": 85,\n" +
                "          \"listeners\": 0\n" +
                "        },\n" +
                "        \"flush\": {\n" +
                "          \"total\": 14,\n" +
                "          \"periodic\": 0,\n" +
                "          \"total_time_in_millis\": 55\n" +
                "        },\n" +
                "        \"warmer\": {\n" +
                "          \"current\": 0,\n" +
                "          \"total\": 16,\n" +
                "          \"total_time_in_millis\": 0\n" +
                "        },\n" +
                "        \"query_cache\": {\n" +
                "          \"memory_size_in_bytes\": 0,\n" +
                "          \"total_count\": 0,\n" +
                "          \"hit_count\": 0,\n" +
                "          \"miss_count\": 0,\n" +
                "          \"cache_size\": 0,\n" +
                "          \"cache_count\": 0,\n" +
                "          \"evictions\": 0\n" +
                "        },\n" +
                "        \"fielddata\": {\n" +
                "          \"memory_size_in_bytes\": 0,\n" +
                "          \"evictions\": 0\n" +
                "        },\n" +
                "        \"completion\": {\n" +
                "          \"size_in_bytes\": 0\n" +
                "        },\n" +
                "        \"segments\": {\n" +
                "          \"count\": 23,\n" +
                "          \"memory_in_bytes\": 85244,\n" +
                "          \"terms_memory_in_bytes\": 61504,\n" +
                "          \"stored_fields_memory_in_bytes\": 11336,\n" +
                "          \"term_vectors_memory_in_bytes\": 488,\n" +
                "          \"norms_memory_in_bytes\": 8320,\n" +
                "          \"points_memory_in_bytes\": 0,\n" +
                "          \"doc_values_memory_in_bytes\": 3596,\n" +
                "          \"index_writer_memory_in_bytes\": 0,\n" +
                "          \"version_map_memory_in_bytes\": 0,\n" +
                "          \"fixed_bit_set_memory_in_bytes\": 0,\n" +
                "          \"max_unsafe_auto_id_timestamp\": 1690502440340,\n" +
                "          \"file_sizes\": {}\n" +
                "        },\n" +
                "        \"translog\": {\n" +
                "          \"operations\": 0,\n" +
                "          \"size_in_bytes\": 715,\n" +
                "          \"uncommitted_operations\": 0,\n" +
                "          \"uncommitted_size_in_bytes\": 715,\n" +
                "          \"earliest_last_modified_age\": 158554217\n" +
                "        },\n" +
                "        \"request_cache\": {\n" +
                "          \"memory_size_in_bytes\": 14100,\n" +
                "          \"evictions\": 0,\n" +
                "          \"hit_count\": 26222,\n" +
                "          \"miss_count\": 23\n" +
                "        },\n" +
                "        \"recovery\": {\n" +
                "          \"current_as_source\": 0,\n" +
                "          \"current_as_target\": 0,\n" +
                "          \"throttle_time_in_millis\": 0\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }
}
