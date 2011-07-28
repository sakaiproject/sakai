/*
 * Created on 18 May 2007
 */

package uk.ac.cam.caret.sakai.rsf.entitybroker;

import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Allows a developer to define the view and view params associated with an entity,
 * this effectively directs an entity URL (e.g. http://server/access/entity/id/) to
 * a view in a tool and adds in needed view params,
 * this is the tie in to the {@link org.sakaiproject.entitybroker.EntityBroker} in Sakai<br/>
 * This will be called whenever an entity URL is accessed which starts with the prefix
 * returned by the handled prefix method<br/>
 * <br/>
 * Best practices usage:<br/>
 * 1) Implement this interface in your tool<br/>
 * 2) Add this as a spring bean to your applicationContext.xml in your tool<br/>
 * Example:
 * <xmp>
	<bean class="org.sakaiproject.sample.tool.inferrers.SampleVPInferrer">
		<property name="externalLogic" 
			ref="org.sakaiproject.sample.logic.ExternalLogic" />
	</bean>
 * </xmp>
 * 3) Add in the extra spring config files to web.xml if they are not already there:<br/>
 * contextConfigLocation should have:<br/>
 * classpath:conf/sakai-entitybroker-applicationContext.xml<br/>
 * requestContextConfigLocation<br/>
 * classpath:conf/sakai-entitybroker-requestContext.xml<br/>
 * 
 * @author Antranig Basman
 * @author AZ (commenting)
 */
public interface EntityViewParamsInferrer extends PrefixHandler {

	/**
	 * Allows you to define where (view) in the tool a entity url should direct the user when it
	 * has no additional parameters (hence the word default), 
	 * a single entity URL can point to as many views as you like<br/>
	 * <b>NOTE</b>: this cannot deal with the case of having http get parameters included with the entity URL
	 * (e.g. http://server/access/entity/id/?apple=1&banana=2 or http://server/access/entity/id/thing/), 
	 * the system will not process an entity URL which includes additional information after the entity reference
	 * 
	 * @param reference a globally unique reference to an entity
	 * @return a view parameters which points to a view in your tool and includes any additional params
	 */
	public ViewParameters inferDefaultViewParameters(String reference);

}
