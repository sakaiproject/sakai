package org.adl.datamodels;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class HibernateTestCase extends AbstractDependencyInjectionSpringContextTests {
	
	private SessionFactory sessionFactory;
	private HibernateTemplate hibernateTemplate;
	
	public HibernateTestCase() {
		super();
	}
	
	protected String[] getConfigLocations() {
		return new String[] { "classpath*:**/scorm-test-db.xml" };
	}
	
	protected void onSetUp() throws Exception {
		hibernateTemplate = new HibernateTemplate(sessionFactory);
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public List find(Class type) {
        return hibernateTemplate.find("from " + type.getName());
    }
	
	public void put(Object r) {
        hibernateTemplate.save(r);
    }
	
	
}
