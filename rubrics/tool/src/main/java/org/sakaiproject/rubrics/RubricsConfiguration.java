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

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Data;
import org.sakaiproject.rubrics.logic.model.Criterion;
import org.sakaiproject.rubrics.logic.model.Rating;
import org.sakaiproject.rubrics.logic.model.Rubric;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rubrics")
public class RubricsConfiguration {

    public static final String RUBRICS_TOKEN_SIGNING_SHARED_SECRET_PROPERTY = "rubrics.integration.token-secret";
    public static final String RUBRICS_TOKEN_SIGNING_SHARED_SECRET_DEFAULT = "12345678900909091234";

    Boolean overallCommentEnabled =  false;

    @NotNull
    String defaultLanguage;

    @NotNull
    Map<String, RubricsLayoutConfiguration> language = new HashMap<>();

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

    public RubricsLayoutConfiguration getDefaultLayoutConfiguration(String lang) {
        return i18nLayout(lang);
    }

    @PostConstruct
    private void postConstruct() {

        if (!language.containsKey(defaultLanguage)) {
            throw new IllegalStateException(String.format("The Rubrics configuration specifies default language of " +
                    "'%1$s'. That layout styles MUST be properly configured to have at least the default language defined" +
                    ", but it is not. Insure the '%1$s' " +
                    "language in the layout style is configured.", this.defaultLanguage));

        }
    }

    private RubricsLayoutConfiguration i18nLayout(String lang) {

        RubricsLayoutConfiguration defaultLayoutConfiguration;

        if (language.containsKey(lang)){
            defaultLayoutConfiguration = language.get(lang);
        }else {
             defaultLayoutConfiguration = language.get(defaultLanguage);
        }
        if (defaultLayoutConfiguration.getDefaultRubric() == null) {
            throw new IllegalStateException(String.format("The Rubrics default layout configuration of " +
                    "'%1$s' does not specify a required default rubric.", this.defaultLanguage));
        }
        if (defaultLayoutConfiguration.getDefaultCriterion() == null) {
            throw new IllegalStateException(String.format("The Rubrics default layout configuration of " +
                    "'%1$s' does not specify a required default criterion.", this.defaultLanguage));
        }
        if (defaultLayoutConfiguration.getDefaultRating() == null) {
            throw new IllegalStateException(String.format("The Rubrics default layout configuration of " +
                    "'%1$s' does not specify a required default rating.", this.defaultLanguage));
        }
        return defaultLayoutConfiguration;
    }

    @Data
    public static class RubricsLayoutConfiguration {

        @NotNull
        Rubric defaultRubric;
        @NotNull
        Criterion defaultCriterion;
        @NotNull
        Rating defaultRating;
    }
}
