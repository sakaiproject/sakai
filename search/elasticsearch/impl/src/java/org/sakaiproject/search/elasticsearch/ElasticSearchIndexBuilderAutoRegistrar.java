/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.search.elasticsearch;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.util.spring.EntityProviderAutoRegistrar;

/**
 * ElasticSearch equivalent of {@code EntityBroker}'s {@code EntityProviderAutoRegistrar}.
 * Unlike that class, though, this class does not require a marker interface other than
 * {@link ElasticSearchIndexBuilder}.
 */
@Slf4j
public class ElasticSearchIndexBuilderAutoRegistrar implements ApplicationContextAware {

    private ElasticSearchService elasticSearchService;
    private ApplicationContext applicationContext;

    public void setElasticSearchService(ElasticSearchService elasticSearchService) {
        this.elasticSearchService = elasticSearchService;
    }

    public void init() {
        registerIndexBuilders();
    }

    private void registerIndexBuilders() {
        final Map<String, ElasticSearchIndexBuilder> indexBuilderBeanRegs =
                applicationContext.getBeansOfType(ElasticSearchIndexBuilder.class, false, false);
        for ( Map.Entry<String, ElasticSearchIndexBuilder> indexBuilderBeanReg : indexBuilderBeanRegs.entrySet() ) {
            try {
                elasticSearchService.registerIndexBuilder(indexBuilderBeanReg.getValue());
            } catch ( Exception e ) {
                log.error("Failed to register ElasticSearchIndexBuilder with bean name: "
                        + indexBuilderBeanReg.getKey(), e);
            }
        }
    }

    public void destroy() {
        applicationContext = null;
    }

    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.applicationContext = context;
    }
}
