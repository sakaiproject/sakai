package org.sakaiproject.portal.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PortalSubPageData {
    private String siteId;
    private String userId;

    private Map<String, List<PageData>> pages;
    private List<PageProps> topLevelPageProps;
    private I18n i18n;

    public PortalSubPageData(String siteId, String userId) {
        this();
        this.siteId = siteId;
        this.userId = userId;
    }

    public PortalSubPageData() {
        pages = new HashMap<>();
        topLevelPageProps = new ArrayList<>();
        i18n = new I18n();
    }

    @Data
    public static class PageProps {
        private String icon;
        private String name;
        private String releaseDate;
        private String siteId;
        private String toolId;
        private String pageId;
        private boolean completed;
        private boolean disabled;
        private boolean disabledDueToPrerequisite;
        private boolean hidden;
        private boolean prerequisite;
        private boolean required;
    }

    @Data
    public static class PageData extends PageProps {
        private String description;
        private String itemId;
        private String sakaiPageId;
        private String sendingPage;
        private String url;
    }

    @Data
    public static class I18n {
        @JsonProperty("prerequisite_and_disabled")
        private String prerequisiteAndDisabled;
        private String expand;
        @JsonProperty("open_top_level_page")
        private String openTopLevelPage;
        private String hidden;
        @JsonProperty("hidden_with_release_date")
        private String hiddenWithReleaseDate;
        private String prerequisite;
        @JsonProperty("main_link_name")
        private String mainLinkName;
        private String collapse;
    }
}
