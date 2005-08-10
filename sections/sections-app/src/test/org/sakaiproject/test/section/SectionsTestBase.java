/**********************************************************************************
*
* $Id: SectionsTestBase.java 632 2005-07-14 21:22:50Z janderse@umich.edu $
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.test.section;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.AbstractTransactionalSpringContextTests;

public class SectionsTestBase extends AbstractTransactionalSpringContextTests {
    private static final Log log = LogFactory.getLog(SectionsTestBase.class);
    protected String[] getConfigLocations() {
        String mem = System.getProperty("mem");
        String[] configLocations = {"", "spring-beans.xml", "spring-beans-test.xml", "spring-hib.xml", "spring-services.xml"};
        if("false".equals(mem)) {
            log.debug("Using configured database for testing");
            configLocations[0] = "spring-db.xml";
        } else {
            log.debug("Using in-memory database for testing");
            configLocations[0] = "spring-db-mem.xml";
        }
        return configLocations;
    }

}




