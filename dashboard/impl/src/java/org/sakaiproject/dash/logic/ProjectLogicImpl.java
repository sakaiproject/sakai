package org.sakaiproject.dash.logic;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

import org.sakaiproject.dash.dao.DashboardDao;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.dash.model.Thing;

/**
 * Implementation of {@link ProjectLogic}
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class ProjectLogicImpl implements ProjectLogic {

	private static final Logger logger = Logger.getLogger(ProjectLogicImpl.class);

	
	/**
	 * {@inheritDoc}
	 */
	public Thing getThing(long id) {
		
		//check cache 
		Element element = cache.get(id);
		if(element != null) {
			if(logger.isDebugEnabled()) {
				logger.debug("Fetching item from cache for: " + id);
			}
			return (Thing)element.getValue();
		}
		
		//if nothing from cache, get from db and cache it 
		Thing t = dao.getThing(id);
			
		if(t != null) {
			if(logger.isDebugEnabled()) {
				logger.debug("Adding item to cache for: " + id);
			}
			cache.put(new Element(id,t));
		}

		return t;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<Thing> getThings() {
		return dao.getThings();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean addThing(Thing t) {
		return dao.addThing(t);
	}
	
	/**
	 * init - perform any actions required here for when this bean starts up
	 */
	public void init() {
		logger.info("init");
	}
	
	@Setter
	private DashboardDao dao;
	
	@Setter
	private Cache cache;

}
