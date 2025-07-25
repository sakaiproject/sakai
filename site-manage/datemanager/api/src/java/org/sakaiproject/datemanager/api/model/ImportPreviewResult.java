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
package org.sakaiproject.datemanager.api.model;

import java.util.List;
import java.util.Map;

public class ImportPreviewResult {
    private boolean hasChanges;
    private String errorMessage;
    private Map<String, List<String[]>> toolsToImport;
    
    public ImportPreviewResult() {
    }
    
    public ImportPreviewResult(boolean hasChanges, Map<String, List<String[]>> toolsToImport) {
        this.hasChanges = hasChanges;
        this.toolsToImport = toolsToImport;
    }
    
    public ImportPreviewResult(String errorMessage) {
        this.hasChanges = false;
        this.errorMessage = errorMessage;
    }
    
    public boolean hasChanges() {
        return hasChanges;
    }
    
    public void setHasChanges(boolean hasChanges) {
        this.hasChanges = hasChanges;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Map<String, List<String[]>> getToolsToImport() {
        return toolsToImport;
    }
    
    public void setToolsToImport(Map<String, List<String[]>> toolsToImport) {
        this.toolsToImport = toolsToImport;
    }
    
    public boolean hasErrors() {
        return errorMessage != null;
    }
}