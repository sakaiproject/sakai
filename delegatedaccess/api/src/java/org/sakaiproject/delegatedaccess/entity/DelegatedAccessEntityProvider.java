package org.sakaiproject.delegatedaccess.entity;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * This is the RESTful service for the Shopping Period Admin.  This allows an instructor to
 * update their own shopping period information through site-manage
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public interface DelegatedAccessEntityProvider extends EntityProvider{
	public final static String ENTITY_PREFIX = "delegated_access";
}
