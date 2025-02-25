package org.sakaiproject.util;

import java.util.HashMap;
import java.util.Map;

public class MergeConfig {
    public String creatorId;
    public Map<Long, Map<String, Object>> ltiContentItems = new HashMap();
    public Map<String, String> attachmentNames = new HashMap();

    public MergeConfig() {}
}
