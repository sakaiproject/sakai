/*
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
package org.sakaiproject.hierarchy.impl.test;

import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.hierarchy.model.HierarchyNodePermission;
import org.sakaiproject.hierarchy.impl.repository.HierarchyNodePermissionRepositoryImpl;
import org.sakaiproject.hierarchy.impl.repository.HierarchyNodeRepositoryImpl;
import org.sakaiproject.hierarchy.impl.test.data.TestDataPreload;
import org.sakaiproject.hierarchy.repository.HierarchyNodePermissionRepository;
import org.sakaiproject.hierarchy.repository.HierarchyNodeRepository;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappingsImpl;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import org.hibernate.SessionFactory;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@ImportResource("classpath:/WEB-INF/components.xml")
@PropertySource("classpath:/hibernate.properties")
public class HierarchyTestConfiguration extends SakaiTestConfiguration {

    @Autowired
    private SessionFactory sessionFactory;

    protected AdditionalHibernateMappings getAdditionalHibernateMappings() {
        AdditionalHibernateMappingsImpl mappings = new AdditionalHibernateMappingsImpl();
        mappings.setAnnotatedClasses(new Class<?>[] {
                HierarchyNode.class,
                HierarchyNodePermission.class});
        return mappings;
    }

    @Bean
    public TestDataPreload testDataPreload(HierarchyNodeRepository nodeRepository,
                                           HierarchyNodePermissionRepository permissionRepository) {
        TestDataPreload tdp = new TestDataPreload();
        tdp.setNodeRepository(nodeRepository);
        tdp.setPermissionRepository(permissionRepository);
        return tdp;
    }
}
