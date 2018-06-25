/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.component.app.scheduler;

import org.hibernate.SessionFactory;
import org.sakaiproject.scheduler.events.hibernate.DelayedInvocation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;


/**
 * Just used for migration to the quartz scheduler.
 */
@Transactional
public class DelayedInvocationDAO {

    private SessionFactory sessionFactory;

    @Inject
    @Named("org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<DelayedInvocation> all() {
        return sessionFactory.getCurrentSession().createCriteria(DelayedInvocation.class).list();
    }

    public void remove(DelayedInvocation invocation) {
        sessionFactory.getCurrentSession().delete(invocation);
    }
}
