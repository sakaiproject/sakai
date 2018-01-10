/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.api;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.api.spring.FactoryUtil;

import org.sakaiproject.tool.assessment.shared.api.assessment.AssessmentServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.ItemServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.SectionServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.common.MediaServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.common.TypeServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.grading.GradebookServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.qti.QTIServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI;


/**
 * <p>Description: Factory for Samigo API</p>
 * <p>This is an abstract class.  It defines the public methods available for
 * the properties that it furnishes.  </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 *
 */
@Slf4j
public abstract class SamigoApiFactory
{
  private static SamigoApiFactory instance = null;

  /**
   * Static method returning an implementation instance of this factory.
   * @return the factory singleton
   */
  public static SamigoApiFactory getInstance()
  {
    log.debug("SamigoApiFactory.getInstance()");
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
    log.debug("instance="+instance);
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

  public abstract SecureDeliveryServiceAPI getSecureDeliveryServiceAPI();
}
