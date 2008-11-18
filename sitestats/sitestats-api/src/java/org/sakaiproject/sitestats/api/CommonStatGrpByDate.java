/**********************************************************************************
 *
 * Copyright (c) 2006 Universidade Fernando Pessoa
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
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
