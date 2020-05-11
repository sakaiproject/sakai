/**
 * Copyright (c) 2003-2020 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool.ccexport;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class CCConfig {
    private Locale locale = Locale.getDefault();
    // currently we don't support 1.0
    private CCVersion version = CCVersion.V12;
    private String siteId;
    private boolean doBank = false;
    private CCResourceItem samigoBank = null;
    private int nextId = 1;

    private Map<String, CCResourceItem> fileMap = new HashMap<>();                  // file resources
    private Map<String, CCResourceItem> samigoMap = new HashMap<>();                // Samigo tests
    private Map<Long, CCResourceItem> poolMap = new HashMap<>();                    // Samigo question pools
    private Map<String, CCResourceItem> assignmentMap = new HashMap<>();            // Assignments
    private Map<String, CCResourceItem> forumsMap = new HashMap<>();                // Forums
    private Map<String, CCResourceItem> bltiMap = new HashMap<>();                  // BLTI instances
    private Set<Long> pagesDone = new HashSet<>();                              // to prevent pages from being output more than once
    private Map<Long, String> embedMap = new HashMap<>();                       // Embed codes with fixups done
    private Set<CCResourceItem> linkSet = new HashSet<>();                          // Links

    private Path root;
    private Path resultsPath;
    private PrintWriter resultWriter;

    // the error messages are a problem. They won't show until the next page display
    // however errrors at this level are unusual, and we interrupt the download, so the
    // user should never see an incomplete one. Most common errors have to do with
    // problems converting for CC format. Those go into a log file that's included in
    // the ZIP, so the user will see those errors (if he knows the look)

    public CCConfig(String siteId, Locale locale) {
        this.siteId = siteId;
        this.locale = locale;

        String tempDir = System.getProperty("java.io.tmpdir");
        root = Paths.get(tempDir);
    }

    public String getResourceId() {
        return "res" + (nextId++);
    }

    public String getResourceIdPeek() {
        return "res" + nextId;
    }

    public String getLocation(String sakaiId) {
        CCResourceItem ref = fileMap.get(sakaiId);
        if (ref == null) return null;
        return ref.getLocation();
    }

    public CCResourceItem addFile(String sakaiId, String location) {
        return addFile(sakaiId, location, null);
    }

    public CCResourceItem addFile(String sakaiId, String location, String use) {
        CCResourceItem res = new CCResourceItem(sakaiId, getResourceId(), location, use, null, null);
        fileMap.put(sakaiId, res);
        return res;
    }

    public void addDependency(CCResourceItem CCResourceItem, String sakaiId) {
        CCResourceItem ref = fileMap.get(sakaiId);
        if (ref != null) CCResourceItem.getDependencies().add(ref.getResourceId());
    }

}
