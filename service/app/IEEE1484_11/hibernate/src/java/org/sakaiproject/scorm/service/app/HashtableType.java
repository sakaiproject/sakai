package org.sakaiproject.scorm.service.app;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;

import org.hibernate.HibernateException;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.collection.PersistentList;
import org.hibernate.collection.PersistentMap;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.usertype.UserCollectionType;

public class HashtableType implements UserCollectionType {
	
	public HashtableType() { }
	
	public boolean contains(Object collection, Object obj) {
		Hashtable Hashtable = (Hashtable)collection;
		return Hashtable.contains(obj);
	}
	
	public Iterator getElementsIterator(Object collection) {	
		return ((Hashtable)collection).entrySet().iterator();
	}

	public Object indexOf(Object collection, Object obj) {

		for (Object key : ((List)((Hashtable)collection).keys())) {
			Object value = ((Hashtable)collection).get(key);
			if (obj.equals(value))
				return key;
		}
		
		return null;
	}

	public Object instantiate() {
		return new Hashtable();
	}

	public PersistentCollection instantiate(SessionImplementor session, CollectionPersister persister) throws HibernateException {
		return new PersistentMap(session, new Hashtable());
	}

	public Object replaceElements(Object collection1, Object collection2, 
			CollectionPersister persister, Object owner, Map copyCache, 
			SessionImplementor implementor) throws HibernateException {
		
		Hashtable Hashtable1 = (Hashtable)collection1;
		Hashtable Hashtable2 = (Hashtable)collection2;
		Hashtable2.clear();
		Hashtable2.putAll(Hashtable1);
		
		return Hashtable2;
	}

	public PersistentCollection wrap(SessionImplementor session, Object collection) {
		
		return new PersistentMap(session, (Hashtable)collection);
	}


		
	
	
}
