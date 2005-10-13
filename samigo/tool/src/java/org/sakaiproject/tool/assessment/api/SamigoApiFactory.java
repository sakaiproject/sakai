/**********************************************************************************
* $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/tool/src/java/org/sakaiproject/tool/assessment/integration/context/IntegrationContextFactory.java $
* $Id: IntegrationContextFactory.java 2503 2005-10-11 18:59:41Z esmiley@stanford.edu $
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.tool.assessment.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.api.spring.FactoryUtil;

import org.sakaiproject.tool.assessment.shared.api.assessment.AssessmentServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.ItemServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.SectionServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.common.MediaServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.common.TypeServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.grading.GradebookServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.qti.QTIServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI;


/**
 * <p>Description: Factory for Samigo API</p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p>This is an abstract class.  It defines the public methods available for
 * the properties that it furnishes.  </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 *
 */
public abstract class SamigoApiFactory
{
  private static Log log = LogFactory.getLog(SamigoApiFactory.class);
  private static SamigoApiFactory instance;

  /**
   * Static method returning an implementation instance of this factory.
   * @return the factory singleton
   */
  public static SamigoApiFactory getInstance()
  {
    log.info("SamigoApiFactory.getInstance()");
    if (instance==null)
    {
      try
      {
        FactoryUtil.setUseLocator(true);
        instance = FactoryUtil.lookup();
      }
      catch (Exception ex)
      {
        log.error("Unable to read integration context: " + ex);
      }
    }
    log.info("instance="+instance);
    return instance;
  }

  // the factory api
  public abstract AssessmentServiceAPI getAssessmentServiceAPI();

  public abstract ItemServiceAPI getItemServiceAPI();

  public abstract PublishedAssessmentServiceAPI
    getPublishedAssessmentServiceAPI();

  public abstract SectionServiceAPI getSectionServiceAPI();

  public abstract MediaServiceAPI getMediaServiceAPI();

  public abstract TypeServiceAPI getTypeServiceAPI();

  public abstract GradebookServiceAPI getGradebookServiceAPI();

  public abstract GradingServiceAPI getGradingServiceAPI();

  public abstract QTIServiceAPI getQtiServiceAPI();

  public abstract QuestionPoolServiceAPI getQuestionPoolServiceAPI();

}