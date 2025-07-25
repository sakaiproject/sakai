/**
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
package org.sakaiproject.datemanager.tool;

import java.util.List;

public class ToolImportData {
    private String toolId;
    private List<String[]> toolData;
    
    public ToolImportData() {
    }
    
    public ToolImportData(String toolId, List<String[]> toolData) {
        this.toolId = toolId;
        this.toolData = toolData;
    }
    
    public String getToolId() {
        return toolId;
    }
    
    public void setToolId(String toolId) {
        this.toolId = toolId;
    }
    
    public List<String[]> getToolData() {
        return toolData;
    }
    
    public void setToolData(List<String[]> toolData) {
        this.toolData = toolData;
    }
}