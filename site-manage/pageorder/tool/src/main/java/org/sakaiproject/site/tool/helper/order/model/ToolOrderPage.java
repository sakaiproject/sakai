/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.site.tool.helper.order.model;

public class ToolOrderPage {

    private String id;
    private String title;
    private String toolId;
    private String toolIconClass;
    private String iframeSource;
    private boolean visible;
    private boolean enabled;
    private boolean hidden;
    private boolean locked;
    private boolean allowsHide;
    private boolean allowsLock;
    private boolean allowsEdit;
    private boolean deletable;
    private boolean iframe;
    private boolean first;
    private boolean last;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getToolId() {
        return toolId;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }

    public String getToolIconClass() {
        return toolIconClass;
    }

    public void setToolIconClass(String toolIconClass) {
        this.toolIconClass = toolIconClass;
    }

    public String getIframeSource() {
        return iframeSource;
    }

    public void setIframeSource(String iframeSource) {
        this.iframeSource = iframeSource;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isAllowsHide() {
        return allowsHide;
    }

    public void setAllowsHide(boolean allowsHide) {
        this.allowsHide = allowsHide;
    }

    public boolean isAllowsLock() {
        return allowsLock;
    }

    public void setAllowsLock(boolean allowsLock) {
        this.allowsLock = allowsLock;
    }

    public boolean isAllowsEdit() {
        return allowsEdit;
    }

    public void setAllowsEdit(boolean allowsEdit) {
        this.allowsEdit = allowsEdit;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public boolean isIframe() {
        return iframe;
    }

    public void setIframe(boolean iframe) {
        this.iframe = iframe;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }
}
