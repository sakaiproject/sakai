/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.entity.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.Validator;

/**
 * <p>
 * EntityManagerComponent is an implementation of the EntityManager.
 * </p>
 */
@Slf4j
public class EntityManagerComponent implements EntityManager
{
	/**
	 * @author ieb
	 */
	public class Calls
	{

		private long lastStart = System.currentTimeMillis();

		private long lookups;

		private long lookupMatch;

		private long iterate;

		private long iterateMatch;

		private long tlookup;

		private long titerate;

		private EntityProducer manager;

		/**
		 * @param manager
		 */
		public Calls(EntityProducer manager)
		{
			this.manager = manager;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			double rate = (1.0 * (tlookup + titerate)) / (1.0 * (lookups + iterate));
			StringBuilder sb = new StringBuilder();
			sb.append("EP Performance ").append("directCalls [").append(lookupMatch)
					.append(" of ").append(lookups).append("] iterate [").append(
							iterateMatch).append(" of ").append(iterate).append(
							"] per parse [").append(rate).append("] ").append(manager);
			return sb.toString();
		}

		/**
		 * 
		 */
		public void lookupStart()
		{
			lastStart = System.currentTimeMillis();
			lookups++;
		}

		/**
		 * 
		 */
		public void lookupMatch()
		{
			lookupMatch++;
		}

		/**
		 * 
		 */
		public void iterateStart()
		{
			lastStart = System.currentTimeMillis();
			iterate++;
		}

		/**
		 * 
		 */
		public void iterateMatch()
		{
			iterateMatch++;
		}

		/**
		 * 
		 */
		public void lookupEnd()
		{
			tlookup += (System.currentTimeMillis() - lastStart);
		}

		/**
		 * 
		 */
		public void iterateEnd()
		{
			titerate += (System.currentTimeMillis() - lastStart);
		}

	}

	/** Set of EntityProducer services. */
	protected ConcurrentHashMap<String, EntityProducer> m_producersIn = new ConcurrentHashMap<String, EntityProducer>();

	protected ConcurrentHashMap<EntityProducer, Calls> m_performanceIn = new ConcurrentHashMap<EntityProducer, Calls>();

	protected ConcurrentHashMap<String, String> m_rejectRefIn = new ConcurrentHashMap<String, String>();

	protected Map<String, EntityProducer> m_producers = new HashMap<String, EntityProducer>();

	protected Map<EntityProducer, Calls> m_performance = new HashMap<EntityProducer, Calls>();

	private Map<String, String> m_rejectRef = new HashMap<String, String>();

	private int nparse = 0;

	private long total;

	private UserDirectoryService userDirectoryService;

	/***************************************************************************
	 * Constructors, Dependencies and their setter methods
	 **************************************************************************/

	/***************************************************************************
	 * Init and Destroy
	 **************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			// library references appear to come through all the time with no
			// resolution
			m_rejectRefIn.put("library", "library");

			m_rejectRef = new HashMap<String, String>(m_rejectRefIn);
			log.info("init()");
		}
		catch (Exception t)
		{
		}
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		log.info("destroy()");
	}

	/***************************************************************************
	 * EntityManager implementation
	 **************************************************************************/

	/**
	 * @inheritDoc
	 */
	public List getEntityProducers()
	{
		List rv = new ArrayList<EntityProducer>();
		rv.addAll(m_producers.values());

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public void registerEntityProducer(EntityProducer manager, String referenceRoot)
	{
		// some services dont provide a reference root, in that case they
		// get something that will neve match.
		if (referenceRoot == null || referenceRoot.trim().length() == 0)
		{
			referenceRoot = String.valueOf(System.currentTimeMillis());
			log.warn("Entity Producer does not provide a root reference :" + manager);
		}
		if (referenceRoot.startsWith("/"))
		{
			referenceRoot = referenceRoot.substring(1);
		}
		m_producersIn.put(referenceRoot, manager);
		m_performanceIn.put(manager, new Calls(manager));

		m_producers = new HashMap<String, EntityProducer>(m_producersIn);
		m_performance = new HashMap<EntityProducer, Calls>(m_performanceIn);
	}

	/**
	 * @param re
	 */
	private void addRejectRef(String shortReference)
	{
		// some services dont provide a reference root, in that case they
		// get something that will neve match.
		if (shortReference == null || shortReference.trim().length() == 0)
		{
			return;
		}
		m_rejectRefIn.put(shortReference, shortReference);
		m_rejectRef = new HashMap<String, String>(m_rejectRefIn);

	}

	/**
	 * @inheritDoc
	 */
	public Reference newReference(String refString)
	{
		return new ReferenceComponent(this,refString);
	}

	/**
	 * @inheritDoc
	 */
	public Reference newReference(Reference copyMe)
	{
		return new ReferenceComponent(copyMe);
	}

	/**
	 * @inheritDoc
	 */
	public List newReferenceList()
	{
		return new ReferenceVectorComponent();
	}

	/**
	 * @inheritDoc
	 */
	public List newReferenceList(List copyMe)
	{
		return new ReferenceVectorComponent(copyMe);
	}

	/**
	 * @inheritDoc
	 */
	public boolean checkReference(String ref)
	{
		// the rules:
		// Null is rejected
		// all blank is rejected
		// INVALID_CHARS_IN_RESOURCE_ID characters are rejected

		Reference r = newReference(ref);

		// just check the id... %%% need more? -ggolden
		String id = r.getId();

		if (id == null) return false;
		if (id.trim().length() == 0) return false;

		// we must reject certain characters that we cannot even escape and get
		// into Tomcat via a URL
		for (int i = 0; i < id.length(); i++)
		{
			if (Validator.INVALID_CHARS_IN_RESOURCE_ID.indexOf(id.charAt(i)) != -1)
				return false;
		}

		return true;
	}

	public EntityProducer getEntityProducer(String reference, Reference target)
	{
		if ( log.isDebugEnabled() ) {
			return getEntityProducerWithDebug(reference, target);
		} else {
			return getEntityProducerNoDebug(reference, target);		
		}
	}

	private final EntityProducer getEntityProducerWithDebug(String reference,
			Reference target)
	{
		nparse++;
		long start = System.currentTimeMillis();
		try
		{
			if (reference.trim().length() == 0)
			{
				return null;
			}
			if (nparse == 1000)
			{
				long t = total;
				double rate = (1.0 * t) / (1.0 * nparse);
				nparse = 0;
				StringBuilder sb = new StringBuilder();
				for (Calls c : m_performance.values())
				{
					sb.append("\n     ").append(c);
				}
				for (String c : m_producers.keySet())
				{
					sb.append("\n     [").append(c).append("]")
							.append(m_producers.get(c));
				}
				log.debug("EntityManager Monitor " + sb.toString());
				log.info("EntityManager Monitor Average " + rate + " ms per parse");
			}

			String ref = reference;
			int n = ref.indexOf('/', 1);
			if (n > 0)
			{
				if (ref.charAt(0) == '/')
				{
					ref = ref.substring(1, n);
				}
				else
				{
					ref = ref.substring(0, n);
				}
			}
			if (m_rejectRef.get(ref) != null)
			{
				return null;
			}
			EntityProducer ep = m_producers.get(ref);
			if (ep != null)
			{
				Calls c = m_performance.get(ep);
				c.lookupStart();
				try
				{
					if (ep.parseEntityReference(reference, target))
					{
						c.lookupMatch();
						return ep;
					}
				}
				finally
				{
					c.lookupEnd();
				}
			}
			log.info("Entity Scan for " + ref + " for " + reference);
			for (Iterator<EntityProducer> iServices = m_producers.values().iterator(); iServices
					.hasNext();)
			{
				EntityProducer service = iServices.next();
				Calls c = m_performance.get(service);
				c.iterateStart();
				try
				{
					if (service.parseEntityReference(reference, target))
					{
						c.iterateMatch();
						return service;
					}
				}
				finally
				{
					c.iterateEnd();
				}
			}
			log.info("Nothing Found for  " + ref + " for " + reference + " adding "
					+ ref + " to the reject list");
			Exception e = new Exception("Traceback");
			log.info("Traceback ", e);
			addRejectRef(ref);
			return null;
		}
		finally
		{
			total += (System.currentTimeMillis() - start);
		}

	}

	private final EntityProducer getEntityProducerNoDebug(String reference,
			Reference target)
	{
		if (reference.trim().length() == 0)
		{
			return null;
		}
		String ref = reference;
		int n = ref.indexOf('/', 1);
		if (n > 0)
		{
			if (ref.charAt(0) == '/')
			{
				ref = ref.substring(1, n);
			}
			else
			{
				ref = ref.substring(0, n);
			}
		}
		if (m_rejectRef.get(ref) != null)
		{
			return null;
		}
		EntityProducer ep = m_producers.get(ref);
		if (ep != null)
		{
			if (ep.parseEntityReference(reference, target))
			{
				return ep;
			}
		}
		for (Iterator<EntityProducer> iServices = m_producers.values().iterator(); iServices
				.hasNext();)
		{
			EntityProducer service = iServices.next();
			Calls c = m_performance.get(service);
			c.iterateStart();
			try
			{
				if (service.parseEntityReference(reference, target))
				{
					c.iterateMatch();
					return service;
				}
			}
			finally
			{
				c.iterateEnd();
			}
		}
		return null;
	}

	public UserDirectoryService getUserDirectoryService() {
		return userDirectoryService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}
}
