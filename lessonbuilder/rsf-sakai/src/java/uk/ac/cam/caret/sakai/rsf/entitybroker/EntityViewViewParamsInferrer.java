/*
 * Created on 18 May 2007
 */

package uk.ac.cam.caret.sakai.rsf.entitybroker;

import org.sakaiproject.entitybroker.EntityView;

import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * The equivalent of {@link EntityViewParamsInferrer} for the {@link EntityView}
 * system supported in versions 1.3 of the EntityBroker and above.
 * Allows a developer to define the default view and view params associated with a 
 * particular entity view.
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
 * classpath:conf/sakai-entitybroker-applicationContext.xml,
 * classpath:conf/sakai-entitybroker-13-applicationContext.xml<br/>
 * requestContextConfigLocation<br/>
 * classpath:conf/sakai-entitybroker-requestContext.xml,
 * classpath:conf/sakai-entitybroker-13-requestContext.xml<br/>
 * 
 * @author Antranig Basman
 * @author AZ (commenting)
 */
public interface EntityViewViewParamsInferrer extends PrefixHandler {
	/**
	 * Allows you to define where (view) in the tool a entity url should direct the user when it
	 * receives a URL template which matches the particular EntityView supplied. 
	 * 
	 * @param view An EntityView object representing a particular matched incoming request
	 * representing a view of a particular entity.
	 * @return a view parameters which points to a view in your tool and includes any additional params
	 */
	public ViewParameters inferDefaultViewParameters(EntityView view);

}
