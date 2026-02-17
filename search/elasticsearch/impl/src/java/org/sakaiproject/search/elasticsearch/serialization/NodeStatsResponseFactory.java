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
package org.sakaiproject.search.elasticsearch.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.opensearch.Version;
import org.opensearch.action.admin.cluster.node.stats.NodeStats;
import org.opensearch.action.admin.cluster.node.stats.NodesStatsResponse;
import org.opensearch.action.admin.indices.stats.CommonStats;
import org.opensearch.action.admin.indices.stats.CommonStatsFlags;
import org.opensearch.cluster.ClusterName;
import org.opensearch.cluster.node.DiscoveryNode;
import org.opensearch.cluster.node.DiscoveryNodeRole;
import org.opensearch.core.common.io.stream.BytesStreamInput;
import org.opensearch.core.common.io.stream.InputStreamStreamInput;
import org.opensearch.core.common.io.stream.OutputStreamStreamOutput;
import org.opensearch.core.common.transport.TransportAddress;
import org.opensearch.index.cache.query.QueryCacheStats;
import org.opensearch.index.cache.request.RequestCacheStats;
import org.opensearch.index.engine.SegmentsStats;
import org.opensearch.index.fielddata.FieldDataStats;
import org.opensearch.index.get.GetStats;
import org.opensearch.index.recovery.RecoveryStats;
import org.opensearch.index.refresh.RefreshStats;
import org.opensearch.index.search.stats.SearchStats;
import org.opensearch.index.shard.DocsStats;
import org.opensearch.index.shard.IndexingStats;
import org.opensearch.index.store.StoreStats;
import org.opensearch.index.translog.TranslogStats;
import org.opensearch.indices.NodeIndicesStats;
import org.opensearch.search.suggest.completion.CompletionStats;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeStatsResponseFactory {

    public static final String NODE_STATS_API = "_nodes/stats/";
    public static final  String NODE_STATS_INDICES_API = "_nodes/stats/indices";
    private final JsonFactory jsonFactory;
    private final ObjectMapper mapper;

    public NodeStatsResponseFactory() {
        jsonFactory = new JsonFactory();
        mapper = createMapper();
    }

    public JsonParser createParser(InputStream in) {
        JsonParser parser = null;
        try {
            parser = jsonFactory.createParser(in);
            parser.enable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        } catch (IOException e) {
            log.warn("Can't create json parser, {}", e.toString());
        }
        return parser;
    }

    public ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        return mapper;
    }

    public NodesStatsResponse createNodeStatsIndicesFromJSON(InputStream in) {
        NodesStatsResponse response = null;
        JsonParser parser = createParser(in);
        if (parser != null) {
            NodeStatsResponseMarshaller marshaller = new NodeStatsResponseMarshaller(parser, mapper);
            response = marshaller.unmarshall();
        }
        return response;
    }


    private static class NodeStatsResponseMarshaller {

        final private JsonParser parser;
        final private ObjectMapper mapper;
        private final List<Node> nodes;
        private String clusterName;

        public NodeStatsResponseMarshaller(JsonParser parser, ObjectMapper mapper) {
            Objects.requireNonNull(parser);
            Objects.requireNonNull(mapper);

            this.mapper = mapper;
            this.parser = parser;
            this.nodes = new ArrayList<>();
        }

        void addNode(Node node) {
            if (node != null) {
                nodes.add(node);
            }
        }

        public NodesStatsResponse unmarshall() {
            try {
                deserialize();
                return MarshallNodeStatsResponse();
            } catch (IOException ioe) {
                log.warn("Could not marshall a search rest response to a NodeStatsResponse, {}", ioe.toString());
            }
            return null;
        }

        private NodesStatsResponse MarshallNodeStatsResponse() {
            List<NodeStats> nodeStats = new ArrayList<>();

            for (Node node : nodes) {
                String[] transportAddress = StringUtils.split(node.transportAddress, ':');

                Set<DiscoveryNodeRole> roles = new HashSet<>();
                for (String role : node.roles) {
                    switch (role) {
                        case "data":
                            roles.add(DiscoveryNodeRole.DATA_ROLE);
                            break;
                        case "ingest":
                            roles.add(DiscoveryNodeRole.INGEST_ROLE);
                            break;
                        case "master":
                            roles.add(DiscoveryNodeRole.MASTER_ROLE);
                            break;
                        case "remote_cluster_client":
                            roles.add(DiscoveryNodeRole.REMOTE_CLUSTER_CLIENT_ROLE);
                            break;
                        default:
                            log.warn("Unknown discovery role: {}", role);
                            break;
                    }
                }

                DiscoveryNode discoveryNode = new DiscoveryNode(
                        node.name,
                        node.id,
                        new TransportAddress(new InetSocketAddress(transportAddress[0], NumberUtils.createInteger(transportAddress[1]))),
                        node.attributes,
                        roles,
                        Version.CURRENT
                );

                CommonStats commonStats = new CommonStats(CommonStatsFlags.ALL);
                commonStats.getDocs().add(new DocsStats(node.indices.docs.get("count"), node.indices.docs.get("deleted"), -1));
                commonStats.getStore().add(new StoreStats(node.indices.store.get("size_in_bytes"), node.indices.store.get("reserved_in_bytes")));
                commonStats.getIndexing().add(new IndexingStats(new IndexingStats.Stats(
                        NumberUtils.createLong(node.indices.indexing.get("index_total")),
                        NumberUtils.createLong(node.indices.indexing.get("index_time_in_millis")),
                        NumberUtils.createLong(node.indices.indexing.get("index_current")),
                        NumberUtils.createLong(node.indices.indexing.get("index_failed")),
                        NumberUtils.createLong(node.indices.indexing.get("delete_total")),
                        NumberUtils.createLong(node.indices.indexing.get("delete_time_in_millis")),
                        NumberUtils.createLong(node.indices.indexing.get("delete_current")),
                        NumberUtils.createLong(node.indices.indexing.get("noop_update_total")),
                        BooleanUtils.toBoolean(node.indices.indexing.get("is_throttled")),
                        NumberUtils.createLong(node.indices.indexing.get("throttle_time_in_millis")),
                        null)));
                commonStats.getGet().add(new GetStats(
                        node.indices.get.get("exists_total"),
                        node.indices.get.get("exists_time_in_millis"),
                        node.indices.get.get("missing_total"),
                        node.indices.get.get("missing_time_in_millis"),
                        node.indices.get.get("current")));
                commonStats.getSearch().add(new SearchStats(
                        new SearchStats.Stats(
                                node.indices.search.get("query_total"),
                                node.indices.search.get("query_time_in_millis"),
                                node.indices.search.get("query_current"),
                                node.indices.search.get("concurrent_query_total"),
                                node.indices.search.get("concurrent_query_time_in_millis"),
                                node.indices.search.get("concurrent_query_current"),
                                node.indices.search.get("concurrent_avg_slice_count"),
                                node.indices.search.get("fetch_total"),
                                node.indices.search.get("fetch_time_in_millis"),
                                node.indices.search.get("fetch_current"),
                                node.indices.search.get("scroll_total"),
                                node.indices.search.get("scroll_time_in_millis"),
                                node.indices.search.get("scroll_current"),
                                node.indices.search.get("point_in_time_total"),
                                node.indices.search.get("point_in_time_time_in_millis"),
                                node.indices.search.get("point_in_time_current"),
                                node.indices.search.get("suggest_total"),
                                node.indices.search.get("suggest_time_in_millis"),
                                node.indices.search.get("suggest_current"),
                                node.indices.search.get("search_idle_reactivate_count_total")),
                        node.indices.search.get("open_contexts"),
                        Collections.emptyMap()));
                commonStats.getMerge().add(
                        node.indices.merges.get("total"),
                        node.indices.merges.get("total_time_in_millis"),
                        node.indices.merges.get("total_size_in_bytes"),
                        node.indices.merges.get("total_docs"),
                        node.indices.merges.get("current"),
                        node.indices.merges.get("current_docs"),
                        node.indices.merges.get("current_size_in_bytes"),
                        node.indices.merges.get("total_stopped_time_in_millis"),
                        node.indices.merges.get("total_throttled_time_in_millis"),
                        (double) node.indices.merges.get("total_auto_throttle_in_bytes") / (1024 * 1024));
                commonStats.getRefresh().add(new RefreshStats(
                        node.indices.refresh.get("total"),
                        node.indices.refresh.get("total_time_in_millis"),
                        node.indices.refresh.get("external_total"),
                        node.indices.refresh.get("external_total_time_in_millis"),
                        node.indices.refresh.get("listeners").intValue()));
                commonStats.getFlush().add(
                        node.indices.flush.get("total"),
                        node.indices.flush.get("periodic"),
                        node.indices.flush.get("total_time_in_millis"));
                commonStats.getWarmer().add(
                        node.indices.warmer.get("current"),
                        node.indices.warmer.get("total"),
                        node.indices.warmer.get("total_time_in_millis"));
                commonStats.getQueryCache().add(new QueryCacheStats(
                        node.indices.queryCache.get("memory_size_in_bytes"),
                        node.indices.queryCache.get("hit_count"),
                        node.indices.queryCache.get("miss_count"),
                        node.indices.queryCache.get("cache_count"),
                        node.indices.queryCache.get("cache_size")));
                commonStats.getFieldData().add(new FieldDataStats(
                        node.indices.fieldData.get("memory_size_in_bytes"),
                        node.indices.fieldData.get("evictions"),
                        null));
                commonStats.getCompletion().add(new CompletionStats(
                        node.indices.completion.get("size_in_bytes"),
                        null));
                SegmentsStats segmentsStats = new SegmentsStats();
                segmentsStats.add(node.indices.segments.get("count"));
                segmentsStats.addIndexWriterMemoryInBytes(node.indices.segments.get("index_writer_memory_in_bytes"));
                segmentsStats.addVersionMapMemoryInBytes(node.indices.segments.get("version_map_memory_in_bytes"));
                segmentsStats.addBitsetMemoryInBytes(node.indices.segments.get("fixed_bit_set_memory_in_bytes"));
                segmentsStats.addBitsetMemoryInBytes(node.indices.segments.get("fixed_bit_set_memory_in_bytes"));

                commonStats.getSegments().add(segmentsStats);
                commonStats.getTranslog().add(new TranslogStats(
                        node.indices.translog.get("operations").intValue(),
                        node.indices.translog.get("size_in_bytes"),
                        node.indices.translog.get("uncommitted_operations").intValue(),
                        node.indices.translog.get("uncommitted_size_in_bytes"),
                        node.indices.translog.get("earliest_last_modified_age")));
                commonStats.getRequestCache().add(new RequestCacheStats(
                        node.indices.requestCache.get("memory_size_in_bytes"),
                        node.indices.requestCache.get("evictions"),
                        node.indices.requestCache.get("hit_count"),
                        node.indices.requestCache.get("miss_count")));
                RecoveryStats recoveryStats = new RecoveryStats();
                recoveryStats.addThrottleTime(node.indices.recovery.get("throttle_time_in_millis") * 1000000);
                commonStats.getRecoveryStats().add(recoveryStats);

                OutputStreamStreamOutput osso = null;
                InputStreamStreamInput isso = null;

                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    osso = new OutputStreamStreamOutput(baos);
                    commonStats.writeTo(osso);
                    osso.writeBoolean(false); // no statsByIndex
                    osso.writeBoolean(false); // no statsByShard
                    osso.flush();
                    isso = new InputStreamStreamInput(new BytesStreamInput(baos.toByteArray()));
                    NodeIndicesStats indicesStats = new NodeIndicesStats(isso);

                    nodeStats.add(new NodeStats(
                            discoveryNode,
                            node.timestamp,
                            indicesStats,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                    ));
                } catch (IOException ioe) {
                    log.warn("Failed to write stats to stream, {}", ioe.toString());
                } finally {
                    try {
                        if (osso != null) osso.close();
                        if (isso != null) isso.close();
                    } catch (IOException ioe) {
                        log.warn("Failed to close stats stream, {}", ioe.toString());
                    }
                }
            }
            return new NodesStatsResponse(new ClusterName(clusterName), nodeStats, Collections.emptyList());
        }

        private void deserialize() throws IOException {
            parser.nextToken();
            if (parser.currentToken() != JsonToken.START_OBJECT) {
                throw new IOException("JSON Format is invalid, start not found");
            }
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.getCurrentName();
                if (field.equals("cluster_name")) {
                    parser.nextToken(); // moves to the fields value
                    clusterName = parser.getText();
                }

                if (field.equals("nodes")) {
                    parser.nextToken();
                    deserializeNodes();
                } else {
                    parser.skipChildren();
                }
            }
        }

        private void deserializeNodes() throws IOException {
            Map<String, Node> nodes;
            nodes = mapper.readValue(parser, new TypeReference<>() {
            });
            for (Map.Entry<String, Node> entry : nodes.entrySet()) {
                Node node = entry.getValue();
                node.id = entry.getKey();
                addNode(node);
            }
        }


        public static class Node {
            @JsonIgnore
            public String id;
            public long timestamp;
            public String name;
            @JsonProperty("transport_address")
            public String transportAddress;
            public String host;
            public String ip;
            public Set<String> roles;
            public Map<String, String> attributes;
            public Indices indices;
        }

        public static class Indices {
            public Map<String, Long> docs;
            public Map<String, Long> store;
            public Map<String, String> indexing;
            public Map<String, Long> get;
            public Map<String, Long> search;
            public Map<String, Long> merges;
            public Map<String, Long> refresh;
            public Map<String, Long> flush;
            public Map<String, Long> warmer;
            @JsonProperty("query_cache")
            public Map<String, Long> queryCache;
            @JsonProperty("fielddata")
            public Map<String, Long> fieldData;
            public Map<String, Long> completion;
            @JsonIgnoreProperties({"file_sizes"})
            public Map<String, Long> segments;
            public Map<String, Long> translog;
            @JsonProperty("request_cache")
            public Map<String, Long> requestCache;
            public Map<String, Long> recovery;
        }
    }
}
