# Sakai Hibernate 3.6 to 4.3.11 Upgrade
The goal of this document is to outline the changes that were required for upgrading sakai to Hibernate 4.
In doing so this will can serve as a guide for updating sakai contrib tools which will be required to upgrade to Hibernate 4 as well.

## Tasks
- [X] upgrade Hibernate lib to 4.3.11 from 3.6.10
- [X] upgrade HSQLDB lib to 2.3.4 from 1.8.0.10
  - (required since H4 does not work with HSQLDB 1.8)
- [X] respectively upgrade sakai's generic-dao lib to Hibernate 4.3.11
- [X] [AddableSessionFactoryBean](kernel/kernel-private/src/main/java/org/sakaiproject/springframework/orm/hibernate/AddableSessionFactoryBean.java) refactored to support Spring's Hibernate 4 LocalSessionFactoryBean
  - this allowed for a simpler configuration and ClassicLocalSessionFactoryBeanHibernate31 was removed
  - allows for configuring more mapping resources like JPA
- [X] respectively update the [HibernateTransactionManager](kernel/kernel-component/src/main/webapp/WEB-INF/db-components.xml) to Springs version 4
- [X] update all classes extending HibernateDaoSupport to use version 4
  - many changes were required that used HibernateTemplate as the method signature changed
  - lambda's were introduced which reduces code and is easier to read
- [ ] Fix remaining issues that were not encountered
  - meaning that there are still some issues remaining that we will find in QA!
- [ ] New Database comparison between H3 and H4
- [ ] Performance impact
  - need to analyze if there was any impact (positive/negative) to queries and make adjustments.
- [ ] JMX Support

## Notes
While performing the changes there were some unplanned changes which will list here.
- Samigo DAO (Facade) Layer extensively passes around concrete Collections like ArrayList and HashMap and it quickly became cumbersome so a lot of this was refactored to use interfaces and Generics which made it clearer what was being passed around.
- logging was updated in some classes to use lombok annotation @SLF4J
- Since JPA 2.1 is now supported a few HBM Mappings were migrated to JPA.
  - this provided some examples of how to migrate from a traditional hbm to JPA
  - also the respective services were marked with @Transactional also providing examples of [Springs declarative transaction implementation](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/transaction.html#transaction-declarative)
- SiteStats had a rather unconventional way to work around an issue with mysql aggregates which has now been solved see [SAK-]
- Issue with ORACLE 1000 param limit, I came up with a solution that is much simpler see class HibernateCriterionUtils. 

## Changes
Below are some examples of some of the common changes that needed to be performed and when appropriate the patterns that were used.
Note these are just examples of how they were solved (patterns used) and there could be other ways, it is only meant to serve as a guide.
They are ordered according to the most common issue encountered. 

###### Update Spring Hibernate 4 vs 3 imports
Use the appropriate Spring Hibernate import matching the hibernate version your using.
```
-import org.springframework.orm.hibernate3.HibernateCallback;
-import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
+import org.springframework.orm.hibernate4.HibernateCallback;
+import org.springframework.orm.hibernate4.support.HibernateDaoSupport;
```

###### Hibernate type constants have been moved 
Constants like Hibernate.STRING are now StringType.INSTANCE
```
-q.setParameter(TYPE_UUID, recordType.getUuid(), Hibernate.STRING);
+q.setParameter(TYPE_UUID, recordType.getUuid(), StringType.INSTANCE);
```
I find it easier to use the method matching the type `Query.setString()` instead of using `Query.setParameter()`.

###### SQLException removed from doInHibernate signature changed
Remove `SQLException`
```
-public Object doInHibernate(Session session) throws HibernateException, SQLException
+public Object doInHibernate(Session session) throws HibernateException
```

###### org.hibernate.hql.internal.ast.HqlSqlWalker.generatePositionalParameter [DEPRECATION] Encountered positional parameter near line 1, column 101 in HQL
In Hibernate 4 position parameters have been deprecated so you will need to update the query to use named parameters.
Just a matter of substituting "?" (positional parameters) in the HQL query for named parameters ":name"
```
-Query.setParamter(0, name, Hibernate.STRING);
+Query.setString("name", name);
```

###### java.lang.ClassCastException: java.lang.Long cannot be cast to java.lang.Integer
Hibernate 3 returned Integer from count queries "select count(*)" where Hibernate 4 returns a Long.
Solving this issue is straight forward however we can go one step further and prevent this type of issue in the future using the supertype Number.
```
HibernateCallback<Number> hcb = session -> {
   Query q = session.getNamedQuery(QUERY_GET_NUM_MOD_TOPICS_WITH_MOD_PERM_BY_PERM_LEVEL);
   q.setParameterList("membershipList", membershipList);
   q.setString("contextId", getContextId());
   return (Number) q.uniqueResult();
};
return getHibernateTemplate().execute(hcb).intValue();
```
Notice the return type is Number for the HibernateCallback so it does not if Hibernate returns an Integer or Long and later the type Integer is returned matching the type in the signature of the method.

###### org.springframework.dao.InvalidDataAccessApiUsageException: Write operations are not allowed in read-only mode (FlushMode.MANUAL): Turn your Session into FlushMode.COMMIT/AUTO or remove 'readOnly' marker from transaction definition
The Spring Hibernate 4 HibernateTemplate is a little more cautious about executing write statements and the presence of a properly advised transaction see `org.springframework.orm.hibernate4.HibernateTemplate.checkWriteOperationAllowed`.
Interestingly this issue has appeared in a number of locations where the TransactionManager was not provided via the spring AOP proxy. What this indicates is that in these locations queries performing a write were not occurring in a transaction which would typically not be ideal.
Usually in this case since the transaction AOP was missing you can [programmatically add the transaction](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/transaction.html#transaction-programmatic).
Some of the places where I've seen this are:
 - init methods (init methods are not advised using AOP proxying)
 - methods that are called that don't go through the AOP proxy i.e. non public methods
 - Threads

While Spring does have alternate methods of handling such cases i.e. using Aspectj load-time weaving (mode=aspectj) or cglib proxying (proxy-target-class="true") it would introduce more changes. Typically applications are built from the beginning using a strategy so as to avoid switching in the future and incurring the associated cost. Since it's not something that is not common I was fine with handling these issues programmatically using springs [TransactionTemplate](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/transaction.html#transaction-programmatic).  

###### getHibernateTemplate().find() and ordinal parameters ######
Do not use Springs HibernateTemplate find() method with parameters as Spring and Hibernate do positional params differently.
Spring is 0 based where Hibernate starts at 1!
Switch to using the findByNamedParam() and type methods where you give the parametes names vs positional.

`getHibernateTemplate().find("select co from CourseOfferingCmImpl as co where co.academicSession.eid = ?", eid);`

switch to:

`getHibernateTemplate().findByNamedParam("select co from CourseOfferingCmImpl as co where co.academicSession.eid = :eid", "eid", eid);`