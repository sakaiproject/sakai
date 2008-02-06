package org.sakaiproject.springframework.orm.hibernate.impl;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.function.ClassicAvgFunction;
import org.hibernate.dialect.function.ClassicCountFunction;
import org.hibernate.dialect.function.ClassicSumFunction;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * Provide backwards compatibility with Hibernate 3.1.x behavior for
 * aggregate functions.
 * 
 * @author lance
 *
 */
public class ClassicLocalSessionFactoryBeanHibernate31 extends
		LocalSessionFactoryBean {
	@Override
	protected Configuration newConfiguration() throws HibernateException 
	{
		final Configuration classicCfg = new Configuration();
		classicCfg.addSqlFunction("count", new ClassicCountFunction());
		classicCfg.addSqlFunction("avg", new ClassicAvgFunction());
		classicCfg.addSqlFunction("sum", new ClassicSumFunction());
		return classicCfg;
	}
}
