package org.sakaiproject.scorm.service.app;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.hibernate.HibernateException;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.collection.PersistentList;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.usertype.UserCollectionType;

public class VectorType implements UserCollectionType {
	
	public VectorType() { }
	
	public boolean contains(Object collection, Object obj) {
		Vector vector = (Vector)collection;
		return vector.contains(obj);
	}
	
	public Iterator getElementsIterator(Object collection) {	
		return ((Vector)collection).iterator();
	}

	public Object indexOf(Object collection, Object obj) {
		return ((Vector)collection).indexOf(obj);
	}

	public Object instantiate() {
		return new Vector();
	}

	public PersistentCollection instantiate(SessionImplementor session, CollectionPersister persister) throws HibernateException {
		return new PersistentList(session, new Vector());
	}

	public Object replaceElements(Object collection1, Object collection2, 
			CollectionPersister persister, Object owner, Map copyCache, 
			SessionImplementor implementor) throws HibernateException {
		
		Vector vector1 = (Vector)collection1;
		Vector vector2 = (Vector)collection2;
		vector2.clear();
		vector2.addAll(vector1);
		
		return vector2;
	}

	public PersistentCollection wrap(SessionImplementor session, Object collection) {
		
		return new PersistentList(session, (Vector)collection);
	}


		
	
	
}
