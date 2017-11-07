package org.sakaiproject.search.elasticsearch;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.admin.cluster.node.stats.NodeStats;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsRequest;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsResponse;
import org.elasticsearch.action.admin.indices.status.IndexStatus;
import org.elasticsearch.action.admin.indices.status.IndicesStatusRequest;
import org.elasticsearch.action.admin.indices.status.IndicesStatusResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.search.api.*;
import org.sakaiproject.search.elasticsearch.filter.SearchItemFilter;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.*;

import static org.sakaiproject.search.elasticsearch.ElasticSearchIndexBuilder.getFieldFromSearchHit;
import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static org.elasticsearch.index.query.FilterBuilders.*;
import static org.elasticsearch.index.query.FilterBuilders.termsFilter;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import static org.elasticsearch.search.facet.FacetBuilders.*;

/**
 * <p>Drop in replacement for sakai's legacy search service which uses {@link <a href="http://elasticsearch.org">elasticsearch</a>}.
 * This service runs embedded in Sakai it does not require a standalone search server.  Like the legacy search service
 * docs are indexed by notification sent via the event service.  This handoff simply puts the reference and associated
 * metadata into ES.  A timer task runs and looks for docs that do not have content, and then digests the content
 * and stores that in the ES index.  This task is necessary to off load this work from any user initiated thread so in the
 * case of large files we aren't holding on to user threads for log periods of time. </p>
 * <p/>
 * <p>Any elasticsearch properties are automatically fed to initialization by looking for properties that start with
 * "elasticsearch."  For example to enable the REST access set the following in sakai.properties:
 * <p/>
 * <pre>
 *    elasticsearch.http.enabled=true
 *    elasticsearch.http.port=9201
 * </pre>
 * <p/>
 * </p>
 */
public class ElasticSearchService implements SearchService {
    private static final Logger log = LoggerFactory.getLogger(ElasticSearchService.class);

    /* constant config */
    public static final String CONFIG_PROPERTY_PREFIX = "elasticsearch.";
    public final static String SAKAI_DOC_TYPE = "sakai_doc";
    public static final String FACET_NAME = "tag";

    /* ElasticSearch handles */
    private Node node;
    private Client client;

    /* injected dependencies */
    private NotificationEdit notification;
    private List<String> triggerFunctions;
    private NotificationService notificationService;
    private EventTrackingService eventTrackingService;
    private ServerConfigurationService serverConfigurationService;
    private ElasticSearchIndexBuilder indexBuilder;
    private SiteService siteService;
    private boolean localNode = false;

    /**
     * set this to true if you intend to run an ElasticSearch cluster that is external to Sakai
     * this instructs ES to not store any data on the local node but only act as a client
     */
    private boolean clientNode = false;

    private boolean useSiteFilters = false;

    /**
     * This property is ignored at the present time it here to preserve backwards capatability.
     *
     * TODO We could interpret this to mean whether or not this node will hold data indices
     * and shards be allocated to it.  So setting this to false for this node to only be a search client.
     * That would take some rework to assure nodes don't attempt indexing work that will fail.
     */
    private boolean searchServer = true;
    
    private boolean useFacetting = true;
    private boolean useSuggestions = true;

    /**
     * dependency
     */
    private UserDirectoryService userDirectoryService;

    /**
     * dependency
     */
    private SessionManager sessionManager;

    /* injectable configuration */

    /**
     * The ES node name for this server. Defaults to the serverId config property
     */
    private String nodeName;

    /**
     * The ES cluster name.  Defaults to the serverName config property
     */
    private String clusterName;

    /**
     * set to true to force an index rebuild at startup time, defaults to false.  This is probably something
     * you never want to use, other than in development or testing
     */
    private boolean rebuildIndexOnStartup = false;

    /**
     * the ES indexname defaults 'sakai_index'
     */
    private String indexName = "sakai_index";

    /**
     * max number of suggestions to return when looking for suggestions (this populates the autocomplete drop down in the UI)
     */
    private int maxNumberOfSuggestions = 10;

    /**
     *  N most frequent terms
     */
    private int facetTermSize = 10;

    /**
     * used in searchXML() to maintain backwards compatibility
     */
    private String sharedKey = null;

    private SearchItemFilter filter;

    /**
     * Register a notification action to listen to events and modify the search
     * index
     */
    public void init() {
        if (!isEnabled()) {
            log.info("ElasticSearch is not enabled. Set search.enable=true to change that.");
            return;
        }

        log.info("Initializing ElasticSearch...");

        initializeElasticSearch();

        log.debug("Register a notification to trigger indexation on new elements");

        // register a transient notification for resources
        notification = notificationService.addTransientNotification();

        // add all the functions that are registered to trigger search index modification
        notification.setFunction(SearchService.EVENT_TRIGGER_SEARCH);
        for (String function : triggerFunctions) {
            notification.addFunction(function);
        }

        // set the filter to any site related resource
        notification.setResourceFilter("/");

        // set the action
        notification.setAction(new SearchNotificationAction(indexBuilder));
    }



    protected void initializeElasticSearch() {
        Map properties = new HashMap<String, String>();

        // load anything set into the ServerConfigurationService that starts with "elasticsearch."
        for (ServerConfigurationService.ConfigItem configItem : serverConfigurationService.getConfigData().getItems()) {
            if (configItem.getName().startsWith(ElasticSearchService.CONFIG_PROPERTY_PREFIX + "index.")){
                continue;
            }
            if (configItem.getName().startsWith(CONFIG_PROPERTY_PREFIX)) {
                properties.put(configItem.getName().replaceFirst(CONFIG_PROPERTY_PREFIX, ""), configItem.getValue());
            }
        }

        // these properties are required at an minimum, assure they are set to reasonable defaults.
        if (!properties.containsKey("node.name")) {
            properties.put("node.name", serverConfigurationService.getServerId());
        }
        if (!properties.containsKey("cluster.name")) {
            properties.put("cluster.name", serverConfigurationService.getServerName());
        }
        if (!properties.containsKey("script.disable_dynamic")) {
            properties.put("script.disable_dynamic", "true");
        }

        // ES calls need these, store these away
        setNodeName((String) properties.get("node.name"));
        setClusterName((String) properties.get("cluster.name"));

        if (!properties.containsKey("path.data")) {
            properties.put("path.data", serverConfigurationService.getSakaiHomePath() + "/elasticsearch/" + getNodeName());
        }

        log.info("Setting ElasticSearch storage area to: " + properties.get("path.data"));

        // initialize elasticsearch
        ImmutableSettings.Builder settings = settingsBuilder().put(properties);

        node = nodeBuilder()
                .client(clientNode)
                .settings(settings)
                .local(localNode).node();

        client = node.client();

        // initialized indexBuilder with references it needs
        indexBuilder.setClient(client);

        indexBuilder.setIndexName(indexName);

        // init index and kick off rebuild if necessary
        if (rebuildIndexOnStartup) {
            indexBuilder.rebuildIndex();
        } else {
            indexBuilder.assureIndex();
        }
    }

    public SearchResponse search(String searchTerms, List<String> siteIds, int start, int end, List<String> references) throws InvalidSearchQueryException {
        return  search(searchTerms, siteIds, start, end, null, null, references);
    }

     public SearchResponse search(String searchTerms, List<String> siteIds, int start, int end, String filterName, String sorterName, List<String> references) throws InvalidSearchQueryException {
         if (references == null) {
             references = new ArrayList();
         }
         if (siteIds == null) {
             siteIds = new ArrayList();
         }

         BoolQueryBuilder query = boolQuery();

         if (searchTerms.contains(":")) {
             String[] termWithType = searchTerms.split(":");
             String termType = termWithType[0];
             String termValue = termWithType[1];
             // little fragile but seems like most providers follow this convention, there isn't a nice way to get the type
             // without a handle to a reference.
             query.must(termQuery(SearchService.FIELD_TYPE, "sakai:" + termType));
             query.must(matchQuery(SearchService.FIELD_CONTENTS, termValue));
         } else {
             query.must(matchQuery(SearchService.FIELD_CONTENTS, searchTerms));
         }

         if (references.size() > 0){
             query.must(termsQuery(SearchService.FIELD_REFERENCE, references.toArray(new String[references.size()])));
         }

         SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName)
                 .setSearchType(SearchType.QUERY_THEN_FETCH)
                 .setQuery(query)
                 .setTypes(SAKAI_DOC_TYPE)
                 .addFields(SearchService.FIELD_REFERENCE, SearchService.FIELD_SITEID,
                         SearchService.FIELD_TITLE, SearchService.FIELD_URL, SearchService.FIELD_TYPE, SearchService.FIELD_TOOL)
                 .setFrom(start).setSize(end - start);

         if(useFacetting) {
            searchRequestBuilder.addFacet(termsFacet(FACET_NAME).field("contents.lowercase").size(facetTermSize));
         }

         // if we have sites filter results to include only the sites included
         if (siteIds.size() > 0) {
             searchRequestBuilder.setRouting(siteIds.toArray(new String[siteIds.size()]));

             // creating config whether or not to use filter, there are performance and caching differences that
             // maybe implementation decisions
             if (useSiteFilters) {
                 OrFilterBuilder siteFilter = orFilter().add(
                         termsFilter(SearchService.FIELD_SITEID, siteIds.toArray(new String[siteIds.size()])).execution("bool"));

                 searchRequestBuilder.setPostFilter(siteFilter);
             } else {
                 query.must(termsQuery(SearchService.FIELD_SITEID, siteIds.toArray(new String[siteIds.size()])));
             }
         }

         log.debug("search request: " + searchRequestBuilder.toString());

         SearchResponse response = searchRequestBuilder.execute().actionGet();

         log.debug("search request took: " + response.getTook().format());

         eventTrackingService.post(eventTrackingService.newEvent(EVENT_SEARCH,
                 EVENT_SEARCH_REF + query.toString(), true,
                 NotificationService.PREF_IMMEDIATE));

         return response;

     }


    @Override
    public SearchList search(String searchTerms, List<String> siteIds, int searchStart, int searchEnd) throws InvalidSearchQueryException {
        SearchResponse response = search(searchTerms, siteIds, searchStart, searchEnd, null, null, new ArrayList<String>());
        return new ElasticSearchList(searchTerms.toLowerCase(), response, this, indexBuilder, FACET_NAME, filter);
    }

    @Override
    public SearchList search(String searchTerms, List<String> siteIds, int start, int end, String filterName, String sorterName) throws InvalidSearchQueryException {
        return search(searchTerms, siteIds, start, end);
    }

    public String searchXML(Map parameterMap) {
        String userid = null;
        String searchTerms = null;
        String checksum = null;
        String contexts = null;
        String ss = null;
        String se = null;
        try {
            String[] useridA = (String[]) parameterMap.get(REST_USERID);
            String[] searchTermsA = (String[]) parameterMap.get(REST_TERMS);
            String[] checksumA = (String[]) parameterMap.get(REST_CHECKSUM);
            String[] contextsA = (String[]) parameterMap.get(REST_CONTEXTS);
            String[] ssA = (String[]) parameterMap.get(REST_START);
            String[] seA = (String[]) parameterMap.get(REST_END);

            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\"?>"); //$NON-NLS-1$

            boolean requestError = false;
            if (useridA == null || useridA.length != 1) {
                requestError = true;
            } else {
                userid = useridA[0];
            }
            if (searchTermsA == null || searchTermsA.length != 1) {
                requestError = true;
            } else {
                searchTerms = searchTermsA[0];
            }
            if (checksumA == null || checksumA.length != 1) {
                requestError = true;
            } else {
                checksum = checksumA[0];
            }
            if (contextsA == null || contextsA.length != 1) {
                requestError = true;
            } else {
                contexts = contextsA[0];
            }
            if (ssA == null || ssA.length != 1) {
                requestError = true;
            } else {
                ss = ssA[0];
            }
            if (seA == null || seA.length != 1) {
                requestError = true;
            } else {
                se = seA[0];
            }

            if (requestError) {
                throw new Exception("Invalid Request"); //$NON-NLS-1$

            }

            int searchStart = Integer.parseInt(ss);
            int searchEnd = Integer.parseInt(se);
            String[] ctxa = contexts.split(";"); //$NON-NLS-1$
            List<String> ctx = new ArrayList<String>(ctxa.length);
            for (int i = 0; i < ctxa.length; i++) {
                ctx.add(ctxa[i]);
            }

            if (sharedKey != null && sharedKey.length() > 0) {
                String check = digestCheck(userid, searchTerms);
                if (!check.equals(checksum)) {
                    throw new Exception("Security Checksum is not valid"); //$NON-NLS-1$
                }
            }

            org.sakaiproject.tool.api.Session s = sessionManager.startSession();
            User u = userDirectoryService.getUser("admin"); //$NON-NLS-1$
            s.setUserId(u.getId());
            sessionManager.setCurrentSession(s);
            try {

                SearchList sl = search(searchTerms, ctx, searchStart, searchEnd);
                sb.append("<results "); //$NON-NLS-1$
                sb.append(" fullsize=\"").append(sl.getFullSize()) //$NON-NLS-1$
                        .append("\" "); //$NON-NLS-1$
                sb.append(" start=\"").append(sl.getStart()).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
                sb.append(" size=\"").append(sl.size()).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
                sb.append(" >"); //$NON-NLS-1$
                for (Iterator<SearchResult> si = sl.iterator(); si.hasNext(); ) {
                    SearchResult sr = (SearchResult) si.next();
                    sr.toXMLString(sb);
                }
                sb.append("</results>"); //$NON-NLS-1$
                return sb.toString();
            } finally {
                sessionManager.setCurrentSession(null);
            }
        } catch (Exception ex) {
            log.error("Search Service XML response failed ", ex);
            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\"?>"); //$NON-NLS-1$
            sb.append("<fault>"); //$NON-NLS-1$
            sb.append("<request>"); //$NON-NLS-1$
            sb.append("<![CDATA["); //$NON-NLS-1$
            sb.append(" userid = ").append(StringEscapeUtils.escapeXml(userid)).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb
                    .append(" searchTerms = ").append(StringEscapeUtils.escapeXml(searchTerms)).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb
                    .append(" checksum = ").append(StringEscapeUtils.escapeXml(checksum)).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb
                    .append(" contexts = ").append(StringEscapeUtils.escapeXml(contexts)).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append(" ss = ").append(StringEscapeUtils.escapeXml(ss)).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append(" se = ").append(StringEscapeUtils.escapeXml(se)).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("]]>"); //$NON-NLS-1$
            sb.append("</request>"); //$NON-NLS-1$
            sb.append("<error>"); //$NON-NLS-1$
            sb.append("<![CDATA["); //$NON-NLS-1$
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                pw.flush();
                sb.append(sw.toString());
                pw.close();
                sw.close();
            } catch (Exception ex2) {
                sb.append("Failed to serialize exception " + ex.getMessage()) //$NON-NLS-1$
                        .append("\n"); //$NON-NLS-1$
                sb.append("Case:  " + ex2.getMessage()); //$NON-NLS-1$

            }
            sb.append("]]>"); //$NON-NLS-1$
            sb.append("</error>"); //$NON-NLS-1$
            sb.append("</fault>"); //$NON-NLS-1$
            return sb.toString();
        }
    }

    private String digestCheck(String userid, String searchTerms)
            throws GeneralSecurityException, IOException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA1"); //$NON-NLS-1$
        String chstring = sharedKey + userid + searchTerms;
        return byteArrayToHexStr(sha1.digest(chstring.getBytes("UTF-8"))); //$NON-NLS-1$
    }

    private static String byteArrayToHexStr(byte[] data) {
        char[] chars = new char[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            byte current = data[i];
            int hi = (current & 0xF0) >> 4;
            int lo = current & 0x0F;
            chars[2 * i] = (char) (hi < 10 ? ('0' + hi) : ('A' + hi - 10));
            chars[2 * i + 1] = (char) (lo < 10 ? ('0' + lo) : ('A' + lo - 10));
        }
        return new String(chars);
    }


    @Override
    public void registerFunction(String function) {
        log.info("Register " + function + " as a trigger for the search service");

        if (!isEnabled()) {
            log.debug("ElasticSearch is not enabled. Set search.enable=true to change that.");
            return;
        }

        notification.addFunction(function);
    }

    public void refreshInstance() {
        indexBuilder.refreshIndex();
    }

    public void rebuildInstance() {
        indexBuilder.rebuildIndex();
    }

    public void refreshSite(String currentSiteId) {
        indexBuilder.refreshIndex(currentSiteId);
    }

    public void rebuildSite(String currentSiteId) {
        indexBuilder.rebuildIndex(currentSiteId);

    }

    @Override
    public void reload() {

    }

    @Override
    public String getStatus() {
        indexBuilder.assureIndex();
        IndicesStatusResponse response = client.admin().indices().status(new IndicesStatusRequest(indexName)).actionGet();
        IndexStatus status = response.getIndices().get(indexName);
        StringBuffer sb = new StringBuffer();

        long pendingDocs = indexBuilder.getPendingDocuments();

        if (pendingDocs != 0) {
            sb.append( "active. " + pendingDocs + " pending items in queue. ");
        } else {
            sb.append("idle. ");
        }

        sb.append("Index Size: " + roundTwoDecimals(status.getStoreSize().getGbFrac()) + " GB" +
                " Refresh Time: " + status.getRefreshStats().getTotalTimeInMillis() + "ms" +
                " Flush Time: " + status.getFlushStats().getTotalTimeInMillis() + "ms" +
                " Merge Time: " + status.getMergeStats().getTotalTimeInMillis() + "ms");

        return sb.toString();
    }

    @Override
    public int getNDocs() {
        indexBuilder.assureIndex();
        CountResponse response = client.prepareCount(indexName)
                .setQuery(filteredQuery(matchAllQuery(),termFilter(SearchService.FIELD_INDEXED, true)))
                .execute()
                .actionGet();
        return (int) response.getCount();
    }

    private String roundTwoDecimals(double d) {
        return String.format("%1.2g", d);
    }

    @Override
    public int getPendingDocs() {
        return (int) indexBuilder.getPendingDocuments();
    }

    @Override
    public List<SearchBuilderItem> getAllSearchItems() {
        return Collections.emptyList();
    }

    @Override
    public List<SearchBuilderItem> getSiteMasterSearchItems() {
        return Collections.emptyList();
    }

    @Override
    public List<SearchBuilderItem> getGlobalMasterSearchItems() {
        return Collections.emptyList();
    }

    @Override
    public SearchStatus getSearchStatus() {
        final String lastLoad = new Date(indexBuilder.getLastLoad()).toString();
        final String loadTime = String.valueOf((double) (0.001 * (indexBuilder.getLastLoad())));
        final String pdocs = String.valueOf(indexBuilder.getPendingDocuments());
        final String ndocs = String.valueOf(getNDocs());

        final NodesInfoResponse nodesInfoResponse = client.admin().cluster().nodesInfo(new NodesInfoRequest()).actionGet();
        final String[] nodes = new String[nodesInfoResponse.getNodes().length];

        int i = 0;

        for (NodeInfo nodeInfo : nodesInfoResponse.getNodes()) {
            nodes[i++] = nodeInfo.getNode().getName();
        }

        final NodesStatsResponse nodesStatsResponse = client.admin().cluster().nodesStats(new NodesStatsRequest(nodes)).actionGet();

        //TODO will show same status for each node, need to deal with that

        return new SearchStatus() {
            public String getLastLoad() {
                return lastLoad;
            }

            public String getLoadTime() {
                return loadTime;
            }

            public String getCurrentWorker() {
                return getNodeName();
            }

            public String getCurrentWorkerETC() {
                return getNodeName();

            }

            public List<Object[]> getWorkerNodes() {
                List<Object[]> workers = new ArrayList();

                for (NodeStats nodeStat : nodesStatsResponse.getNodes()) {
                    workers.add(new Object[]{nodeStat.getNode().getName() + "(" + nodeStat.getHostname() + ")",
                            indexBuilder.getStartTime(),
                            getStatus()});

                }
                return workers;
            }

            public String getNDocuments() {
                return ndocs;
            }

            public String getPDocuments() {
                return pdocs;
            }
        };
    }


    @Override
    public boolean removeWorkerLock() {
        return true;
    }

    @Override
    public List<Object[]> getSegmentInfo() {
        return Collections.singletonList(new Object[]{"Index Segment Info is not implemented", "", ""});
    }

    @Override
    public void forceReload() {
    }

    @Override
    public TermFrequency getTerms(int documentId) throws IOException {
        throw new UnsupportedOperationException("ElasticSearch can't does not support this operation at this time.");
    }

    @Override
    public boolean isEnabled() {
        return serverConfigurationService.getBoolean("search.enable", false);
    }

    @Override
    public String getDigestStoragePath() {
        return null;
    }

    public String getSearchSuggestion(String searchString) {
        String[] suggestions = getSearchSuggestions(searchString, null, true);
        if (suggestions != null && suggestions.length > 0) {
            for (String suggestion : suggestions) {
                if (searchString.equalsIgnoreCase(suggestion)) {
                    continue;
                }
                return suggestion;
            }
        }
        return null;
    }

    /**
	 * Get all the sites a user has access to.
	 * @return An array of site IDs.
	 */
	protected String[] getAllUsersSites(String currentUser) {
		List<Site> sites = siteService.getSites(
				org.sakaiproject.site.api.SiteService.SelectionType.ACCESS,
				null, null, null, null, null);
        List<String> siteIds = convertSitesToStringArray(sites);
		siteIds.add(siteService.getUserSiteId(currentUser));
		return siteIds.toArray(new String[siteIds.size()]);
	}

    protected List<String> convertSitesToStringArray(List<Site> sites) {
        List<String> siteIds = new ArrayList<String>(sites.size());
        for (Site site: sites) {
            if (site != null && site.getId() != null) {
                siteIds.add(site.getId());
            }
        }
        return siteIds;
    }

    public String[] getSearchSuggestions(String searchString, String currentSite, boolean allMySites) {
        if (!useSuggestions) {
            return new String[0];
        }

        String currentUser = "";
        User user = userDirectoryService.getCurrentUser();
		if (user != null)  {
			currentUser = user.getId();
        }
        String[] sites;
        if (allMySites || currentSite == null) {
            sites = getAllUsersSites(currentUser);
        } else {
            sites = new String[]{currentSite};
        }

        TermQueryBuilder query = termQuery("title", searchString);

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName)
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setQuery(query)
                .setTypes(SAKAI_DOC_TYPE)
                .setSize(maxNumberOfSuggestions)
                .setRouting(sites)
         //       .addHighlightedField(SearchService.FIELD_TITLE, 255, 0)
                .addField(SearchService.FIELD_TYPE)
                .addField(SearchService.FIELD_REFERENCE)
                //.addField(SearchService.FIELD_ID)
                .addField(SearchService.FIELD_SITEID)
                .addField(SearchService.FIELD_TITLE);

        OrFilterBuilder siteFilter = orFilter().add(
                termsFilter(SearchService.FIELD_SITEID, sites).execution("bool"));

        searchRequestBuilder.setPostFilter(siteFilter);

        log.debug("search request: " + searchRequestBuilder.toString());

        SearchResponse response = searchRequestBuilder.execute().actionGet();

        log.debug("search request took: " + response.getTook().format());

        List<String> suggestions = new ArrayList();

        for (SearchHit hit : response.getHits()) {
            suggestions.add(getFieldFromSearchHit(SearchService.FIELD_TITLE, hit));
//            suggestions.add(hit.getHighlightFields().get(SearchService.FIELD_TITLE).getFragments()[0].string());
        }

        return suggestions.toArray(new String[suggestions.size()]);
    }

    @Override
    public boolean isSearchServer() {
        return true;
    }

    public void destroy(){
        if (node != null) {
            node.close();
        }
    }

    //------------------------------------------------------------------------------------------
    //As far as I know, this implementation isn't diagnosable, so this is a dummy implementation
    //------------------------------------------------------------------------------------------
    @Override
    public void enableDiagnostics() {
    }

    @Override
    public void disableDiagnostics() {
    }

    @Override
    public boolean hasDiagnostics() {
        return false;
    }


    public void setTriggerFunctions(List<String> triggerFunctions) {
        this.triggerFunctions = triggerFunctions;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    public void setIndexName(String indexName) {
        //elasticsearch wants lowers case index names
        this.indexName = indexName.toLowerCase();
    }


    public void setIndexBuilder(ElasticSearchIndexBuilder indexBuilder) {
        this.indexBuilder = indexBuilder;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setRebuildIndexOnStartup(boolean rebuildIndexOnStartup) {
        this.rebuildIndexOnStartup = rebuildIndexOnStartup;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setSharedKey(String sharedKey) {
        this.sharedKey = sharedKey;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setMaxNumberOfSuggestions(int maxNumberOfSuggestions) {
        this.maxNumberOfSuggestions = maxNumberOfSuggestions;
    }

    public void setFacetTermSize(int facetTermSize) {
        this.facetTermSize = facetTermSize;
    }

    public void setLocalNode(boolean localNode) {
        this.localNode = localNode;
    }

    public void setClientNode(boolean clientNode) {
        this.clientNode = clientNode;
    }

    public void setUseSiteFilters(boolean useSiteFilters) {
        this.useSiteFilters = useSiteFilters;
    }

    public boolean getUseFacetting() {
        return useFacetting;
    }

    public void setUseFacetting(boolean useFacetting) {
        this.useFacetting = useFacetting;
    }

    public boolean getUseSuggestions() {
        return useSuggestions;
    }

    public void setUseSuggestions(boolean useSuggestions) {
        this.useSuggestions = useSuggestions;
    }

    public void setFilter(SearchItemFilter filter) {
        this.filter = filter;
    }

    public void setSearchServer(boolean searchServer) {
        this.searchServer = searchServer;
    }
}
