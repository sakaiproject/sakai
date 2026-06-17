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
