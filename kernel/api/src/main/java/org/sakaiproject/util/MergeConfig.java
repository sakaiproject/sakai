package org.sakaiproject.util;

import java.util.HashMap;
import java.util.Map;

public class MergeConfig {
    public String siteId;
    public String fromContext;
    public String fromServerUrl;
    public Map<Long, Map<String, Object>> ltiContentItems = new HashMap();

    public MergeConfig() {}
}
