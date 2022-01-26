package org.sakaiproject.entitybroker.providers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.IgniteSpringBean;
import org.apache.ignite.cache.CachePeekMode;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputFormattable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.FormatUnsupportedException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Ignite provider that exposes information from ignite.
 *
 * see https://ignite.apache.org/docs/latest/monitoring-metrics/intro
 */

@Slf4j
public class IgniteEntityProvider extends AbstractEntityProvider implements ActionsExecutable, OutputFormattable {

    public static final String PREFIX = "ignite";

    @Setter private IgniteSpringBean ignite;
    @Setter private DeveloperHelperService developerHelperService;

    @Override
    public String getEntityPrefix() {
        return PREFIX;
    }

    @Override
    public String[] getHandledOutputFormats() {
        return new String[] { Formats.XML, Formats.JSON, Formats.HTML };
    }

    @EntityCustomAction(viewKey=EntityView.VIEW_LIST)
    public ActionReturn clusterInfo(EntityView view) {
        Map<String, Object> data = new HashMap<>();
        IgniteCluster cluster = ignite.cluster();
        data.put("IgniteInstanceName", ignite.name());
        data.put("IgniteClusterId", cluster.id().toString());
        data.put("IgniteClusterState", cluster.state().toString());
        data.put("IgniteClusterTopologyVersion", Long.toString(cluster.topologyVersion()));
        List<String> nodeIds = cluster.nodes().stream()
                .map(n -> "id=" + n.id().toString() + ", consistentId=" + n.consistentId() + ", version=" + n.version())
                .collect(Collectors.toList());

        Integer configuredNodeSize = (Integer) ignite.getConfiguration().getUserAttributes().get("DiscoveryAddressesSize");
        data.put("IgniteCurrentNodeSize", String.valueOf(nodeIds.size()));
        data.put("IgniteConfiguredNodeSize", String.valueOf(configuredNodeSize));
        if (nodeIds.size() == configuredNodeSize) {
            data.put("IgniteNodeStatus", "CONNECTED");
        } else {
            data.put("IgniteNodeStatus", "SEGMENTED");
        }
        data.put("IgniteClusterNodeList", nodeIds);

        return new ActionReturn(data);
    }

    @EntityCustomAction(viewKey=EntityView.VIEW_LIST)
    public ActionReturn cacheNames(EntityView view) {
        return new ActionReturn(ignite.cacheNames());
    }

    @EntityCustomAction
    public ActionReturn cacheMetrics(EntityView view) {
        String cacheName = view.getEntityReference().getId();
        if (ignite.cacheNames().contains(cacheName)) {
            return new ActionReturn(ignite.cache(cacheName).metrics());
        } else {
            return new ActionReturn((Object) ("The cache [" + cacheName + "] does not exist please supply a valid cache name"));
        }
    }

    @EntityCustomAction
    public ActionReturn clearCache(EntityView view) {
        String currentUserRef = developerHelperService.getCurrentUserReference();
        if (developerHelperService.isUserAdmin(currentUserRef)) {
            String cacheName = view.getEntityReference().getId();
            if (ignite.cacheNames().contains(cacheName)) {
                IgniteCache cache = ignite.cache(cacheName);
                long cacheSize = cache.sizeLong(CachePeekMode.ALL);
                cache.clear();
                log.info("Cleared cache [{}:{}] by user {}", cacheName, cacheSize, developerHelperService.getCurrentUserId());
                return new ActionReturn((Object) ("Cleared cache [" + cacheName + ":" + cacheSize + "] at " + LocalDateTime.now()));
            } else {
                return new ActionReturn((Object) ("The cache [" + cacheName + "] does not exist please supply a valid cache name"));
            }
        } else {
            log.warn("User [{}] attempted to clear a cache without the proper authortity", currentUserRef);
            throw new SecurityException("User [" + currentUserRef + "] doesn't have the authority to perform this action");
        }
    }

    @EntityCustomAction(viewKey= EntityView.VIEW_LIST)
    public ActionReturn localNodeMetrics(EntityView view) {
        return new ActionReturn(ignite.cluster().localNode().metrics());
    }

    @EntityCustomAction(viewKey= EntityView.VIEW_LIST)
    public ActionReturn transactionMetrics(EntityView view) {
        return new ActionReturn(ignite.transactions().metrics());
    }

    @Override
    public void formatOutput(EntityReference ref, String format, List<EntityData> entities, Map<String, Object> params, OutputStream output) {
        if (Formats.HTML.equals(format) && "cacheNames".equals(ref.getId())) {
            StringBuilder out = new StringBuilder();
            entities.forEach(e -> outputEntityCacheNames(out, e));
            try {
                output.write(out.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException ioe) {
                log.warn("While writing entity data to OutputStream, {}", ioe.toString());
            }
            return;
        }
        throw new FormatUnsupportedException("Use internal formatter", ref.getReference(), format);
    }

    private void outputEntityCacheNames(StringBuilder out, EntityData entityData) {
        boolean canClearCache = developerHelperService.isUserAdmin(developerHelperService.getCurrentUserReference());

        if (entityData.getDisplayTitle() != null) {
            out.append("<div style=\"font-weight:bold;\">")
                    .append(entityData.getDisplayTitle())
                    .append("</div>");
        }

        out.append("<table border=\"1\">")
                .append("<tbody><tr>");

        if (entityData.getData() instanceof Collection) {
            Collection<String> data = (Collection<String>) entityData.getData();

            out.append("<td width=\"3%\">data type=collection size=")
                    .append(data.size())
                    .append("</td><td>")
                    .append("<table border=\"1\">")
                    .append("<tbody>");

            data.forEach(n -> {
                // create link to view cache metrics
                out.append("<tr><td><a href=/direct/")
                        .append(PREFIX)
                        .append("/")
                        .append(n)
                        .append("/cacheMetrics>")
                        .append(n)
                        .append("</a>");
                if (canClearCache) {
                    // create link for clearing cache
                    out.append("</td><td><a href=/direct/")
                            .append(PREFIX)
                            .append("/")
                            .append(n)
                            .append("/clearCache>clear</a></td></tr>");
                }
            });

            out.append("</tbody>")
                .append("</table></td>");
        }

        out.append("</tr>")
                .append("</tbody>")
                .append("</table>");
    }
}
