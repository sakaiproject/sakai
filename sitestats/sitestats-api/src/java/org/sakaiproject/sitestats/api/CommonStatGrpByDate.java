/**
 * $URL:$
 * $Id:$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.api;


/**
 * This class is deprecated and will be removed in version 2.1.
 * Please use {@link StatsRecord} instead.
 */
@Deprecated public interface CommonStatGrpByDate 
	extends Stat {	
	/**
	 * This method is deprecated and will be removed in version 2.1.
	 * @see {@link StatsRecord#getEventId()} and {@link StatsRecord#getResourceRef()}
	*/	
	@Deprecated public String getRef();	
	
	/**
	 * This method is deprecated and will be removed in version 2.1.
	 * @see {@link StatsRecord#setEventId(String)} and {@link StatsRecord#setResourceRef(String)}
	*/	
	@Deprecated public void setRef(String ref);
	
	/**
	 * This method is deprecated and will be removed in version 2.1.
	 * @see {@link StatsManager#getResourceImage(String)}
	*/	
	@Deprecated public String getRefImg();
	
	/**
	 * This method is deprecated and will be removed in version 2.1.
	 * @see {@link StatsManager#getResourceImage(String)}
	*/	
	@Deprecated public void setRefImg(String ref);
	
	/**
	 * This method is deprecated and will be removed in version 2.1.
	 * @see {@link StatsManager#getResourceURL(String)}
	*/	
	@Deprecated public String getRefUrl();
	
	/**
	 * This method is deprecated and will be removed in version 2.1.
	 * @see {@link StatsManager#getResourceURL(String)}
	*/	
	@Deprecated public void setRefUrl(String ref);
	
	/**
	 * This method is deprecated and will be removed in version 2.1.
	 * @see {@link StatsRecord#getResourceAction()}
	*/	
	@Deprecated public String getRefAction();
	
	/**
	 * This method is deprecated and will be removed in version 2.1.
	 * @see {@link StatsRecord#setResourceAction(String)}
	*/	
	@Deprecated public void setRefAction(String ref);
	
	/**
	 * This method is deprecated and will be removed in version 2.1.
	*/	
	@Deprecated public String getDateAsString();
}
