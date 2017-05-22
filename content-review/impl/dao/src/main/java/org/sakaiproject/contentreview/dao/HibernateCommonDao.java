/**
 * Copyright (c) 2003 The Apereo Foundation
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
package org.sakaiproject.contentreview.dao;

import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import lombok.Setter;

public abstract class HibernateCommonDao<T> {

	@Setter
	protected Class<T> persistentClass;
	
	@Setter
	protected SessionFactory sessionFactory;

	/**
	 * Persists the object t
	 * @param t
	 */
	public void create(T t) {
		sessionFactory.getCurrentSession().persist(t);
	}

	/**
	 * Saves any changes made to object t
	 * @param t
	 */
	public void save(T t) {
		sessionFactory.getCurrentSession().saveOrUpdate(t);
	}
	
	/**
	 * Get the object with id
	 * @param id
	 * @return object of type T
	 */
	@SuppressWarnings("unchecked")
	public Optional<T> get(long id) {
		return  Optional.ofNullable((T) sessionFactory.getCurrentSession().get(persistentClass , id));
	}
	
	/**
	 * Delete the object
	 * @param t
	 */
	public void delete(T t) {
		Session session = sessionFactory.getCurrentSession();
		session.delete(t);
	}

	/**
	 * Delete the object with id
	 * @param id
	 */
	public void delete(long id) {
		Optional<T> t = get(id);
		if (t.isPresent()) sessionFactory.getCurrentSession().delete(t.get());;
	}
}
