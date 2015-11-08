/**
 * Copyright © 2005, CARET, University of Cambridge
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
/*
 * Created on 18 May 2007
 */

package org.sakaiproject.rsf.entitybroker;

import org.sakaiproject.entitybroker.EntityView;

import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * The equivalent of {@link EntityViewParamsInferrer} for the {@link EntityView}
 * system supported in versions 1.3 of the EntityBroker and above. Allows a
 * developer to define the default view and view params associated with a
 * particular entity view. this is the tie in to the
 * {@link org.sakaiproject.entitybroker.EntityBroker} in Sakai<br/>
 * This will be called whenever an entity URL is accessed which starts with the
 * prefix returned by the handled prefix method<br/>
 * <br/>
 * Best practices usage:<br/>
 * 1) Implement this interface in your tool<br/>
 * 2) Add this as a spring bean to your applicationContext.xml in your tool<br/>
 * Example:
 * <xmp> <bean class="org.sakaiproject.sample.tool.inferrers.SampleVPInferrer">
 * <property name="externalLogic" ref=
 * "org.sakaiproject.sample.logic.ExternalLogic" /> </bean> </xmp> 3) Add in the
 * extra spring config files to web.xml if they are not already there:<br/>
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
	 * Allows you to define where (view) in the tool a entity url should direct
	 * the user when it receives a URL template which matches the particular
	 * EntityView supplied.
	 * 
	 * @param view
	 *            An EntityView object representing a particular matched
	 *            incoming request representing a view of a particular entity.
	 * @return a view parameters which points to a view in your tool and
	 *         includes any additional params
	 */
	public ViewParameters inferDefaultViewParameters(EntityView view);

}
