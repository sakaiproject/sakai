/**********************************************************************************
*
* $Header: /cvs/sakai2/sam/sakai-samigo/src-SakaiBaseFrameworkJar/ThreadLocalMapProvider.java,v 1.3 2005/06/05 03:04:50 ggolden.umich.edu Exp $
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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

package org.sakaiproject.framework;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.service.framework.current.cover.CurrentService;

/**
 * Provides access to thread-scoped variables, delegating to the Sakai CurrentService.
 * This implementation should only be used in a Sakai-Samigo integrated installation.
 * @author <a href="mailto:jonandersen@umich.edu">Jon Andersen</a>
 * @version $Id$
 */
public class ThreadLocalMapProvider
{
  private static final Log LOG = LogFactory
      .getLog(ThreadLocalMapProvider.class);

  /** A Map that delegates to the CurrentService for get() and put() (only supported operations). */
  private static Map CURRENT_SERVICE_MAPPER = new AbstractMap()
  {
        public Object get(Object key)
        { 
            return CurrentService.getInThread((String) key);
        }

        public Object put(Object key, Object value)
        {
            CurrentService.setInThread((String) key, value);
            return null;
        }

        public Set entrySet()
        {
            throw new UnsupportedOperationException(); 
        }
  };

  private ThreadLocalMapProvider()
  {
    throw new IllegalAccessError();
  }

  public static Map getMap()
  {
    LOG.debug("getMap()");

    return CURRENT_SERVICE_MAPPER;
  }

  public static void clearMap()
  {
    LOG.debug("clearMap()");
	// Note: code should not attempt to clear the thread local / current service - the Sakai filter takes care of that.
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return getMap().toString();
  }

}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/sam/sakai-samigo/src-SakaiBaseFrameworkJar/ThreadLocalMapProvider.java,v 1.3 2005/06/05 03:04:50 ggolden.umich.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
