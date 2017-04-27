/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.rubrics;

import lombok.Data;
import org.sakaiproject.rubrics.model.Criterion;
import org.sakaiproject.rubrics.model.Rating;
import org.sakaiproject.rubrics.model.Rubric;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;

@Data
public class RubricsInstanceConfiguration {

    Boolean overallCommentEnabled =  false;

    @Pattern(regexp = "flex|classic")
    @NotNull
    String defaultLayout;

    @NotNull
    String defaultLanguage;

    @NotNull
    Map<String, RubricsLayoutConfiguration> layouts = new HashMap<>();

    public RubricsLayoutConfigurationLanguage getDefaultLayoutConfiguration(String lang) {
        return i18nLayout(lang);
    }

    @PostConstruct
    private void postConstruct() {
        if (!layouts.containsKey(this.defaultLayout)) {
            throw new IllegalStateException(String.format("The Rubrics configuration specifies default layout of " +
                    "'%1$s'. That layout style MUST be properly configured, but it is not. Insure the '%1$s' " +
                    "layout style is configured.", this.defaultLayout));

        }
        if (!layouts.get(defaultLayout).enabled) {
            throw new IllegalStateException(String.format("The Rubrics configuration specifies default layout of " +
                    "'%1$s'. That layout style MUST be enabled, but it is not properly configured. Insure the '%1$s' " +
                    "layout style is enabled.", this.defaultLayout));

        }

        if (!layouts.get(defaultLayout).language.containsKey(defaultLanguage)) {
            throw new IllegalStateException(String.format("The Rubrics configuration specifies default language of " +
                    "'%1$s'. That layout styles MUST be properly configured to have at least the default language defined" +
                    ", but it is not. Insure the '%1$s' " +
                    "language in the layout style is configured.", this.defaultLanguage));

        }
    }

    private RubricsLayoutConfigurationLanguage i18nLayout(String lang) {

        RubricsLayoutConfigurationLanguage defaultLayoutConfiguration;

        if (layouts.get(defaultLayout).language.containsKey(lang)){
            defaultLayoutConfiguration = layouts.get(defaultLayout).language.get(lang);
        }else {
             defaultLayoutConfiguration = layouts.get(defaultLayout).language.get(defaultLanguage);
        }
        if (defaultLayoutConfiguration.getDefaultRubric() == null) {
            throw new IllegalStateException(String.format("The Rubrics default layout configuration of " +
                    "'%1$s' does not specify a required default rubric. See default.sakai.properties for " +
                    "example configuration.", this.defaultLayout));
        }
        if (defaultLayoutConfiguration.getDefaultCriterion() == null) {
            throw new IllegalStateException(String.format("The Rubrics default layout configuration of " +
                    "'%1$s' does not specify a required default criterion. See default.sakai.properties for " +
                    "example configuration.", this.defaultLayout));
        }
        if (defaultLayoutConfiguration.getDefaultRating() == null) {
            throw new IllegalStateException(String.format("The Rubrics default layout configuration of " +
                    "'%1$s' does not specify a required default rating. See default.sakai.properties for " +
                    "example configuration.", this.defaultLayout));
        }
        return defaultLayoutConfiguration;
    }

    @Data
    public static class RubricsLayoutConfiguration {

        @NotNull
        boolean enabled;

        @Pattern(regexp = "hilo|lohi")
        @NotNull
        String ratingsOrder;

        @NotNull
        boolean criterionFineTuneEnabled;
        @NotNull
        boolean criterionNotApplicableEnabled;
        @NotNull
        boolean ratingsTitleOnlyOptionEnabled;
        @NotNull
        boolean ratingsFeedbackOnlyOptionEnabled;
        @NotNull
        Map<String, RubricsLayoutConfigurationLanguage> language = new HashMap<>();
    }

    @Data
    public static class RubricsLayoutConfigurationLanguage {

        @NotNull
        Rubric defaultRubric;
        @NotNull
        Criterion defaultCriterion;
        @NotNull
        Rating defaultRating;
    }
}
