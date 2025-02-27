package org.sakaiproject.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * MergeConfig is a configuration container for merge operations across services.
 * It acts as a shared context between different services during merge operations,
 * eliminating the need to pass data through multiple service layers.
 *
 * The merge() operations are executed in a specific order, allowing later services
 * to access data populated by earlier services through this shared configuration.
 */
public class MergeConfig {
    public String creatorId;
    public String archiveContext = "";
    public String archiveServerUrl = "";
    public Map<Long, Map<String, Object>> ltiContentItems = new HashMap();
    public Map<String, String> attachmentNames = new HashMap();
    public Map<String, String> userIdTrans = new HashMap();
    public Set<String> userListAllowImport = new HashSet();

    public MergeConfig() {}
}
