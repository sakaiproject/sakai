/*
 * Copyright (c) 2003-2025 The Apereo Foundation
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
